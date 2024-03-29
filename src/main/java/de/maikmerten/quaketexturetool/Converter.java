package de.maikmerten.quaketexturetool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;

/**
 *
 * @author maik
 */
public class Converter {

    private final double fullbrightThresh = 0.025;
    private final boolean showFullbright = !true;

    private boolean ditherFullbrights = false;
    private float ditherStrength = 0.25f;
    private int reduce = 4;

    private DistanceCalculator distColor = new DistanceCalculator(DistanceCalculator.Method.RGB);
    private DistanceCalculator distFullbright = new DistanceCalculator(DistanceCalculator.Method.HSY);

    private class MipTexHeader extends StreamOutput {

        private byte[] name = new byte[16]; // zero terminated
        private int width; // 4 bytes, little endian
        private int height; // 4 bytes, little endian
        private int[] mipOffsets = new int[4]; // four MIPs, first offset: 40 (header length)

        private int getSize() {
            return name.length + (2 * 4) + (mipOffsets.length * 4);
        }

        private void write(OutputStream os) throws Exception {
            os.write(name);
            writeLittle(width, os);
            writeLittle(height, os);
            for (int i = 0; i < mipOffsets.length; ++i) {
                writeLittle(mipOffsets[i], os);
            }
        }

    }

    public byte[] convert(InputStream colorStream, InputStream normStream, InputStream glowStream, String name, boolean ignoreFullbrights) throws Exception {
        BufferedImage colorImage = ImageIO.read(colorStream);
        int width = colorImage.getWidth();
        int height = colorImage.getHeight();

        BufferedImage normImage = null;
        if (normStream != null) {
            normImage = ImageIO.read(normStream);
            normImage = resampleImage(normImage, width, height);
        }

        BufferedImage glowImage = null;
        if (glowStream != null) {
            glowImage = ImageIO.read(glowStream);
            glowImage = resampleImage(glowImage, width, height);
        }
        
        return convert(colorImage, normImage, glowImage, name, ignoreFullbrights);
    }

    public byte[] convert(BufferedImage colorImage, BufferedImage normImage, BufferedImage glowImage, String name, boolean ignoreFullbrights) throws Exception {

        int width = colorImage.getWidth();
        int height = colorImage.getHeight();

        BufferedImage img = renderImage(colorImage, normImage, glowImage);

        width = width / getReduce();
        height = height / getReduce();

        width &= 0xFFFFFFF0;
        height &= 0xFFFFFFF0;

        // this should never be possible
        if (width % 16 != 0 || height % 16 != 0) {
            System.out.println("Could not convert " + name + " as target dimensions are not multiples of 16.");
            return null;
        }

        List<byte[][]> mips = new ArrayList<>(4);
        
        // generate four MIP images
        for (int i = 0; i < 4; ++i) {
            byte[][] mip = createMip(img, glowImage, (width >> i), (height >> i), ignoreFullbrights, name.startsWith("{"));
            mips.add(mip);
        }

        // generate MipTex data
        MipTexHeader mipHead = new MipTexHeader();
        byte[] namebytes = name.getBytes(Charset.forName("US-ASCII"));
        for (int i = 0; i < Math.min(15, namebytes.length); ++i) {
            mipHead.name[i] = namebytes[i];
        }
        mipHead.height = height;
        mipHead.width = width;

        mipHead.mipOffsets = new int[mips.size()];
        int offset = mipHead.getSize();
        for (int i = 0; i < mips.size(); ++i) {
            mipHead.mipOffsets[i] = offset;
            byte[][] mip = mips.get(i);
            offset += mip.length * mip[0].length;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mipHead.write(baos);
        for (int i = 0; i < mips.size(); ++i) {
            byte[][] mip = mips.get(i);
            for (int x = 0; x < mip.length; ++x) {
                baos.write(mip[x]);
            }
        }

        return baos.toByteArray();
    }

    private boolean isFullbright(BufferedImage glowImg, int x, int y) {
        if (glowImg == null) {
            return false;
        }

        int lumapixel = glowImg.getRGB(x, y);
        double luma = Color.getY(lumapixel);
        return luma > fullbrightThresh;
    }

    private byte[][] createMip(BufferedImage renderedImage, BufferedImage glowImage, int width, int height, boolean ignoreFullbrights, boolean transparent) {
        byte[][] mip = new byte[height][width];

        // resize to requested dimensions
        BufferedImage img = resampleImage(renderedImage, width, height);
        BufferedImage glowResampled = resampleImage(glowImage, width, height);
        
        boolean[][] transparentMap = null;
        if(transparent) {
            transparentMap = new boolean[img.getWidth()][img.getHeight()];
            for(int x = 0; x < img.getWidth(); x++) {
                for(int y = 0; y < img.getHeight(); y++) {
                    int a = (img.getRGB(x, y) >> 24) & 0xFF;
                    if(a < 128) {
                        transparentMap[x][y] = true;
                    }
                }
            }            
        }

        // reduce to Quake palette
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                double minDistance = Double.MAX_VALUE;
                int index = 0;
                final int color1 = img.getRGB(x, y);

                int firstIdx = 0;
                int lastIdx = PaletteQ1.fullbrightStart - 1;

                if (isFullbright(glowResampled, x, y) && !ignoreFullbrights) {
                    if (showFullbright) {
                        firstIdx = lastIdx = 251;
                    } else {
                        firstIdx = PaletteQ1.fullbrightStart;
                        lastIdx = PaletteQ1.colors.length - 1;
                    }
                }

                for (int i = firstIdx; i <= lastIdx; ++i) {
                    int color2 = PaletteQ1.colors[i];
                    double distance = Double.MAX_VALUE;

                    if (firstIdx == PaletteQ1.fullbrightStart) {
                        distance = getDistFullbright().getDistance(color1, color2);
                    } else {
                        distance = getDistColor().getDistance(color1, color2);
                    }

                    if (distance < minDistance) {
                        index = i;
                        minDistance = distance;
                    }
                }

                mip[y][x] = (byte) (index & 0xFF);

                if (firstIdx < PaletteQ1.fullbrightStart || getDitherFullbrights()) {
                    dither(img, x, y, color1, PaletteQ1.colors[index], glowResampled);
                }
            }
        }
        
        // special case: texture with transparent parts
        if(transparent) {
            for(int x = 0; x < img.getWidth(); x++) {
                for(int y = 0; y < img.getHeight(); y++) {
                    boolean t = transparentMap[x][y];
                    if(t) {
                        mip[y][x] = (byte) 255;
                    }
                }
            }
        }

        return mip;
    }

    public BufferedImage renderImage(BufferedImage colorImage, BufferedImage normImage, BufferedImage glowImage) {
        BufferedImage img = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Vec3 light = new Vec3(0.25, 0.25, 1);
        Vec3 normal = new Vec3(0, 0, 1);
        light.normalize();

        // make sure that the images are of a proper type (e.g., not indexed) by rendering
        // them onto new buffered images
        BufferedImage colorImageConverted = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        colorImageConverted.getGraphics().drawImage(colorImage, 0, 0, null);

        BufferedImage normImageConverted = null;
        if (normImage != null) {
            normImageConverted = new BufferedImage(normImage.getWidth(), normImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            normImageConverted.getGraphics().drawImage(normImage, 0, 0, null);
        }

        BufferedImage glowImageConverted = null;
        if (glowImage != null) {
            glowImageConverted = new BufferedImage(glowImage.getWidth(), glowImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            glowImageConverted.getGraphics().drawImage(glowImage, 0, 0, null);
        }

        for (int x = 0; x < colorImage.getWidth(); ++x) {
            for (int y = 0; y < colorImage.getHeight(); ++y) {
                int color = colorImageConverted.getRGB(x, y);

                if (normImageConverted != null) {
                    // read surface normal
                    int normrgb = normImageConverted.getRGB(x, y);
                    normal.setValues(Color.getR(normrgb), Color.getG(normrgb), Color.getB(normrgb));
                    normal.normalize();

                    // apply lighting
                    color = Color.dim(color, normal.dot(light));
                }

                // add glow map
                if (glowImageConverted != null) {
                    int glow = glowImageConverted.getRGB(x, y);
                    color = Color.add(color, glow);
                }

                img.setRGB(x, y, color);
            }
        }

        return img;
    }

    private void dither(BufferedImage img, int x, int y, int targetColor, int actualColor, BufferedImage glowImg) {

        if (ditherStrength < 0.001) {
            return;
        }

        float dR = (Color.getR(targetColor) - Color.getR(actualColor)) * ditherStrength;
        float dG = (Color.getG(targetColor) - Color.getG(actualColor)) * ditherStrength;
        float dB = (Color.getB(targetColor) - Color.getB(actualColor)) * ditherStrength;

        if ((x + 1) < img.getWidth() && !(isFullbright(glowImg, x + 1, y) && !ditherFullbrights)) {
            float w = 7f / 16f;
            int neighbour = img.getRGB(x + 1, y);
            neighbour = Color.add(neighbour, (int) ((dR * w) + .5f), (int) ((dG * w) + .5f), (int) ((dB * w) + .5f));
            img.setRGB(x + 1, y, neighbour);
        }

        if ((x + 1) < img.getWidth() && (y + 1) < img.getHeight() && !(isFullbright(glowImg, x + 1, y + 1) && !ditherFullbrights)) {
            float w = 1f / 16f;
            int neighbour = img.getRGB(x + 1, y + 1);
            neighbour = Color.add(neighbour, (int) ((dR * w) + .5f), (int) ((dG * w) + .5f), (int) ((dB * w) + .5f));
            img.setRGB(x + 1, y + 1, neighbour);
        }

        if ((y + 1) < img.getHeight() && !(isFullbright(glowImg, x, y + 1) && !ditherFullbrights)) {
            float w = 5f / 16f;
            int neighbour = img.getRGB(x, y + 1);
            neighbour = Color.add(neighbour, (int) ((dR * w) + .5f), (int) ((dG * w) + .5f), (int) ((dB * w) + .5f));
            img.setRGB(x, y + 1, neighbour);
        }

        if ((x - 1) >= 0 && (y + 1) < img.getHeight() && !(isFullbright(glowImg, x - 1, y + 1) && !ditherFullbrights)) {
            float w = 3f / 16f;
            int neighbour = img.getRGB(x - 1, y + 1);
            neighbour = Color.add(neighbour, (int) ((dR * w) + .5f), (int) ((dG * w) + .5f), (int) ((dB * w) + .5f));
            img.setRGB(x - 1, y + 1, neighbour);
        }
    }

    public BufferedImage resampleImage(BufferedImage img, int width, int height) {
        if (img == null) {
            return null;
        }

        if (img.getWidth() == width && img.getHeight() == height) {
            return img;
        }
        
        return Scalr.resize(img, Scalr.Method.QUALITY, width, height);
    }

    public boolean getDitherFullbrights() {
        return ditherFullbrights;
    }

    public void setDitherFullbrights(boolean ditherFullbrights) {
        this.ditherFullbrights = ditherFullbrights;
    }

    public float getDitherStrength() {
        return ditherStrength;
    }

    public void setDitherStrength(float ditherStrength) {
        this.ditherStrength = ditherStrength;
    }

    public int getReduce() {
        return reduce;
    }

    public void setReduce(int reduce) {
        this.reduce = reduce;
    }

    public DistanceCalculator getDistColor() {
        return distColor;
    }

    public void setDistColor(DistanceCalculator distColor) {
        this.distColor = distColor;
    }

    public DistanceCalculator getDistFullbright() {
        return distFullbright;
    }

    public void setDistFullbright(DistanceCalculator distFullbright) {
        this.distFullbright = distFullbright;
    }

}
