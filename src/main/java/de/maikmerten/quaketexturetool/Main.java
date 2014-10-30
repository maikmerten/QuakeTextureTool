package de.maikmerten.quaketexturetool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author maik
 */
public class Main {

	public static void main(String[] args) throws Exception {

		int reduce = 4;
		Converter conv = new Converter();

		File workingDir = new File(".");
		File outputDir = new File(workingDir.getAbsoluteFile() + File.separator + "output" + File.separator);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		List<File> colorMaps = new ArrayList<>();
		for (File f : workingDir.listFiles()) {
			String fname = f.getName();
			if (f.isDirectory() || !fname.endsWith(".png") || fname.contains("_norm.") || fname.contains("_glow.") || fname.contains("_gloss.")) {
				continue;
			}
			colorMaps.add(f);
		}
		
		Wad wad = new Wad();

		for (File colorFile : colorMaps) {
			String basepath = colorFile.getAbsolutePath();
			String normpath = basepath.substring(0, basepath.length() - 4) + "_norm.png";
			String glowpath = basepath.substring(0, basepath.length() - 4) + "_glow.png";

			InputStream normInput = null;
			File normFile = new File(normpath);
			if (normFile.exists()) {
				normInput = new FileInputStream(normFile);
			}

			InputStream glowInput = null;
			File glowFile = new File(glowpath);
			if (glowFile.exists()) {
				glowInput = new FileInputStream(glowFile);
			}

			InputStream colorInput = new FileInputStream(colorFile);

			List<byte[][]> result = conv.convert(colorInput, normInput, glowInput, reduce);

			String name = colorFile.getName();
			name = name.substring(0, name.length() - 4);

			System.out.println(name);
			if(name.length() > 15) {
				System.out.println("  name too long (will be truncated to 15 characters): " + name);
			}
			
			wad.addMipTexture(name, result);
		}
		
		File wadFile = new File(outputDir.getAbsolutePath() + File.separator + "output.wad");
		FileOutputStream fos = new FileOutputStream(wadFile);
		wad.write(fos);
		fos.close();

	}

}
