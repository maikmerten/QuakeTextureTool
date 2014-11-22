package de.maikmerten.quaketexturetool;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 *
 * @author maik
 */
public class FileFinder {

	private final File baseDir;

	private class ColorMapFileFilter implements FileFilter {
		@Override
		public boolean accept(File f) {
			String fname = f.getName().toLowerCase(Locale.ENGLISH);

			if (!f.isFile()) {
				return false;
			}

			if (!(fname.endsWith(".png"))) {
				return false;
			}

			if (fname.contains("_norm.") || fname.contains("_glow.") || fname.contains("_luma.") || fname.contains("_gloss.")) {
				return false;
			}

			return true;
		}
	}

	public FileFinder(File baseDir) throws Exception {
		if(!baseDir.isDirectory()) {
			throw new Exception("baseDir is not a directory!");
		}
		
		this.baseDir = baseDir;
	}

	public Queue<File> findColorMaps() {
		LinkedList<File> files = new LinkedList<>();
		FileFilter filter = new ColorMapFileFilter();

		for(File f : baseDir.listFiles()) {
			if(filter.accept(f)) {
				files.add(f);
			}
		}
		
		Collections.sort(files);

		return files;
	}
	
	public File findNormFile(String texname) {
		
		String common = baseDir.getAbsolutePath() + File.separator + texname + "_norm.";
		
		File f = new File(common + "png");
		if(f.exists()) {
			return f;
		}
		
		return null;
	}
	
	public File findGlowFile(String texname) {

		String common = baseDir.getAbsolutePath() + File.separator + texname + "_glow.";
		
		File f = new File(common + "png");
		if(f.exists()) {

			return f;
		}
		
		common = baseDir.getAbsolutePath() + File.separator + texname + "_luma.";
		
		f = new File(common + "png");
		if(f.exists()) {
			return f;
		}
		
		return null;
	}

}
