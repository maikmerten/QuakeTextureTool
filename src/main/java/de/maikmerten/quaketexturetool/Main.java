package de.maikmerten.quaketexturetool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

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
			if (f.isDirectory() || !fname.endsWith(".png") || fname.endsWith("_norm.png") || fname.endsWith("_glow.png") || fname.endsWith("_gloss.png")) {
				continue;
			}
			colorMaps.add(f);
		}

		for (File colorFile : colorMaps) {
			String basepath = colorFile.getAbsolutePath();
			String normpath = basepath.substring(0, basepath.length() - 4) + "_norm.png";
			String glowpath = basepath.substring(0, basepath.length() - 4) + "_glow.png";

			File normFile = new File(normpath);
			if (!normFile.exists()) {
				System.out.println(normpath + " does not exist, skipping " + basepath);
				continue;
			}
			InputStream normInput = new FileInputStream(normFile);

			InputStream glowInput = null;
			File glowFile = new File(glowpath);
			if (glowFile.exists()) {
				glowInput = new FileInputStream(glowFile);
			}

			InputStream colorInput = new FileInputStream(colorFile);

			BufferedImage result = conv.convert(colorInput, normInput, glowInput, reduce);

			String name = colorFile.getName();
			name = name.substring(0, name.length() - 4) + ".png";
			File outFile = new File(outputDir.getAbsolutePath() + File.separator + name);
			System.out.println(outFile.getAbsolutePath());

			ImageIO.write(result, "png", outFile);

		}

	}

}
