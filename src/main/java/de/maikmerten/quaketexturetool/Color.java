package de.maikmerten.quaketexturetool;

/**
 *
 * @author maik
 */
public class Color {

	public static int clampChannel(int c) {
		c = Math.min(c, 255);
		c = Math.max(c, 0);
		return c;
	}

	public static int getRGB(int r, int g, int b) {

		r = clampChannel(r);
		g = clampChannel(g);
		b = clampChannel(b);

		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	public static int getR(int rgb) {
		return (rgb >> 16) & 0xFF;
	}

	public static int getG(int rgb) {
		return (rgb >> 8) & 0xFF;
	}

	public static int getB(int rgb) {
		return rgb & 0xFF;
	}

	public static double getY(int rgb) {
		int r1 = getR(rgb);
		int g1 = getG(rgb);
		int b1 = getB(rgb);

		return (0.299 * r1 + 0.587 * g1 + 0.114 * b1) / 263.0;
	}

	public static double getDistanceYPrPb(int color1, int color2) {

		int r1 = getR(color1);
		int r2 = getR(color2);

		int g1 = getG(color1);
		int g2 = getG(color2);

		int b1 = getB(color1);
		int b2 = getB(color2);

		double y1 = 0.299 * r1 + 0.587 * g1 + 0.114 * b1;
		double pb1 = -0.168736 * r1 - 0.331264 * g1 + 0.5 * b1;
		double pr1 = 0.5 * r1 + 0.481688 * g1 + 0.081312 * b1;

		double y2 = 0.299 * r2 + 0.587 * g2 + 0.114 * b2;
		double pb2 = -0.168736 * r2 - 0.331264 * g2 + 0.5 * b2;
		double pr2 = 0.5 * r1 + 0.481688 * g2 + 0.081312 * b2;

		double dy = y1 - y2;
		double dpb = pb1 - pb2;
		double dpr = pr1 - pr2;

		return dy * dy + dpb * dpb + dpr * dpr;
	}
	
	public static double getDistancePlain(int color1, int color2) {
		int rdiff = getR(color1) - getR(color2);
		int gdiff = getG(color1) - getG(color2);
		int bdiff = getB(color1) - getB(color2);
		
		return rdiff*rdiff + gdiff*gdiff + bdiff*bdiff;
	}

	public static int add(int color1, int color2) {
		int r1 = getR(color1);
		int g1 = getG(color1);
		int b1 = getB(color1);

		int r2 = getR(color2);
		int g2 = getG(color2);
		int b2 = getB(color2);

		int r = clampChannel(r1 + r2);
		int g = clampChannel(g1 + g2);
		int b = clampChannel(b1 + b2);

		return getRGB(r, g, b);

	}
	
	public static int add(int color, int r, int g, int b) {
		int r1 = getR(color);
		int g1 = getG(color);
		int b1 = getB(color);

		int r2 = clampChannel(r1 + r);
		int g2 = clampChannel(g1 + g);
		int b2 = clampChannel(b1 + b);

		return getRGB(r2, g2, b2);

	}

	public static int dim(int color, double factor) {
		factor = Math.min(1.0, factor);
		factor = Math.max(0.0, factor);

		int r = getR(color);
		int g = getG(color);
		int b = getB(color);

		r = clampChannel((int) ((r * factor) + 0.5));
		g = clampChannel((int) ((g * factor) + 0.5));
		b = clampChannel((int) ((b * factor) + 0.5));

		return getRGB(r, g, b);
	}

}
