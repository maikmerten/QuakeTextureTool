package de.maikmerten.quaketexturetool;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

/**
 *
 * @author maik
 */
public class ColorMapFileFilter implements FileFilter {

	@Override
	public boolean accept(File f) {
		String fname = f.getName().toLowerCase(Locale.ENGLISH);

		if (!f.isFile()) {
			return false;
		}

		if (!(fname.endsWith(".png"))) {
			return false;
		}

		if (fname.contains("_norm.") || fname.contains("_glow.") || fname.contains("_luma.")|| fname.contains("_gloss.")) {
			return false;
		}
		
		
		return true;
	}

}
