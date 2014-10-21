package de.maikmerten.quaketexturetool;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author maik
 */
public class Converter {

	private final double fullbrightThresh = 0.125;
	private final boolean showFullbright = !true;

	public BufferedImage convert(InputStream colorStream, InputStream normStream, InputStream glowStream, int reduce) throws Exception {

		BufferedImage colorImage = ImageIO.read(colorStream);
		int width = colorImage.getWidth();
		int height = colorImage.getHeight();

		BufferedImage normImage = ImageIO.read(normStream);
		normImage = resampleImage(normImage, width, height);

		BufferedImage glowImage = null;
		if (glowStream != null) {
			glowImage = ImageIO.read(glowStream);
			glowImage = resampleImage(glowImage, width, height);
		}

		BufferedImage img = renderImage(colorImage, normImage, glowImage);

		width = width / reduce;
		height = height / reduce;
		img = resampleImage(img, width, height);

		BufferedImage glowResampled = resampleImage(glowImage, width, height);
		BufferedImage indexedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, PaletteQ1.indexColorModel);

		// reduce to Quake palette
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
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

				indexedImage.getRaster().setSample(x, y, 0, index);
			}
		}

		return indexedImage;
	}

	public BufferedImage renderImage(BufferedImage colorImage, BufferedImage normImage, BufferedImage glowImage) {
		BufferedImage img = new BufferedImage(normImage.getWidth(), normImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		// TODO: find a proper vector
		Vec3 light = new Vec3(-0.95, -0.95, .75);
		light.normalize();

		for (int x = 0; x < colorImage.getWidth(); ++x) {
			for (int y = 0; y < colorImage.getHeight(); ++y) {
				// read surface normal
				int normrgb = normImage.getRGB(x, y);
				Vec3 normal = new Vec3((Color.getR(normrgb) - 128) / 128.0, (Color.getG(normrgb) - 128) / 128.0, (Color.getB(normrgb) - 128) / 128.0);
				normal.normalize();

				// apply lighting
				int color = colorImage.getRGB(x, y);
				color = Color.dim(color, normal.dot(light));

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

		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics g = newImage.createGraphics();
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();

		return newImage;
	}

}
