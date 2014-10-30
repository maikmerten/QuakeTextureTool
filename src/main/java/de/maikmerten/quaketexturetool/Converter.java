package de.maikmerten.quaketexturetool;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author maik
 */
public class Converter {

	private final double fullbrightThresh = 0.125;
	private final boolean showFullbright = !true;

	public List<byte[][]> convert(InputStream colorStream, InputStream normStream, InputStream glowStream, int reduce) throws Exception {

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

		BufferedImage img = renderImage(colorImage, normImage, glowImage);
		
		width = width / reduce;
		height = height / reduce;
		
		List<byte[][]> mips = new ArrayList<>(4);
		
		// generate four MIP images
		for(int i = 0; i < 4; ++i) {
			byte[][] mip = createMip(img, glowImage, (width >> i), (height >> i));
			mips.add(mip);
		}
		
		return mips;
	}
	
	
	private byte[][] createMip(BufferedImage renderedImage, BufferedImage glowImage, int width, int height) {
		byte[][] mip = new byte[height][width];

		// resize to requested dimensions
		BufferedImage img = resampleImage(renderedImage, width, height);
		BufferedImage glowResampled = resampleImage(glowImage, width, height);

		// reduce to Quake palette
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				double minDistance = Double.MAX_VALUE;
				int index = 0;
				int color1 = img.getRGB(x, y);

				double luma = 0.0;
				if (glowResampled != null) {
					luma = Color.getY(glowResampled.getRGB(x, y));
				}

				int firstIdx = 0;
				int lastIdx = PaletteQ1.fullbrightStart - 1;

				if (luma > fullbrightThresh) {
					if (showFullbright) {
						firstIdx = lastIdx = 251;
					} else {
						firstIdx = PaletteQ1.fullbrightStart;
						lastIdx = PaletteQ1.colors.length - 1;
					}
				}

				for (int i = firstIdx; i <= lastIdx; ++i) {
					int color2 = PaletteQ1.colors[i];
					double distance = Color.getDistance(color1, color2);
					if (distance < minDistance) {
						index = i;
						minDistance = distance;
					}
				}

				mip[y][x] = (byte)(index & 0xFF);
			}
		}
		
		return mip;
	}
	

	public BufferedImage renderImage(BufferedImage colorImage, BufferedImage normImage, BufferedImage glowImage) {
		BufferedImage img = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Vec3 light = new Vec3(0.25, 0.25, 1);
		light.normalize();

		for (int x = 0; x < colorImage.getWidth(); ++x) {
			for (int y = 0; y < colorImage.getHeight(); ++y) {
				int color = colorImage.getRGB(x, y);
				
				if(normImage != null) {
					// read surface normal
					int normrgb = normImage.getRGB(x, y);
					Vec3 normal = new Vec3(Color.getR(normrgb), Color.getG(normrgb), Color.getB(normrgb));
					normal.normalize();

					// apply lighting
					color = Color.dim(color, normal.dot(light));
				}

				// add glow map
				if (glowImage != null) {
					int glow = glowImage.getRGB(x, y);
					color = Color.add(color, glow);
				}

				img.setRGB(x, y, color);
			}
		}

		return img;
	}

	public BufferedImage resampleImage(BufferedImage img, int width, int height) {
		if (img == null) {
			return null;
		}
		
		if(img.getWidth() == width && img.getHeight() == height) {
			return img;
		}

		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics g = newImage.createGraphics();
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();

		return newImage;
	}

}
