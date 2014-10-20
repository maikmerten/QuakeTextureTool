package de.maikmerten.quaketexturetool;

import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maik
 */
public class PaletteQ1 {

	public final static int[] colors;
	public final static int fullbrightStart = 224;
	public final static IndexColorModel indexColorModel;

	static {
		colors = new int[256];
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];

		InputStream is = PaletteQ1.class.getClassLoader().getResourceAsStream("palette.lmp");

		for (int i = 0; i < colors.length; ++i) {
			try {
				int r_val = is.read();
				int g_val = is.read();
				int b_val = is.read();

				r[i] = (byte) r_val;
				g[i] = (byte) g_val;
				b[i] = (byte) b_val;

				colors[i] = Color.getRGB(r_val, g_val, b_val);

			} catch (IOException ex) {
				Logger.getLogger(PaletteQ1.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		indexColorModel = new IndexColorModel(8, 256, r, g, b);
	}

}
