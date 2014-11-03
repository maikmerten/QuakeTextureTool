package de.maikmerten.quaketexturetool;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author maik
 */
public class ConverterThread extends Thread {

	private final Queue<File> fileQueue;
	private final Converter conv;
	private final Wad wad;

	public ConverterThread(Queue<File> fileQueue, Converter conv, Wad wad) {
		this.fileQueue = fileQueue;
		this.conv = conv;
		this.wad = wad;
	}

	@Override
	public void run() {

		while (true) {
			File colorFile;
			String name;
			List<byte[][]> result;

			// get next job
			synchronized (fileQueue) {
				colorFile = fileQueue.poll();
				if (colorFile == null) {
					// no jobs left;
					break;
				}
			}

			// find files and convert
			try {
				String basepath = colorFile.getAbsolutePath();

				// try to find file with surface normals
				String normpath = basepath.substring(0, basepath.length() - 4) + "_norm.png";
				InputStream normInput = null;
				File normFile = new File(normpath);
				if (normFile.exists()) {
					normInput = new FileInputStream(normFile);
				}

				// try to find file with luminance information
				String glowpath = basepath.substring(0, basepath.length() - 4) + "_glow.png";
				InputStream glowInput = null;
				File glowFile = new File(glowpath);
				if (glowFile.exists()) {
					glowInput = new FileInputStream(glowFile);
				} else {
					glowpath = basepath.substring(0, basepath.length() - 4) + "_luma.png";
					glowFile = new File(glowpath);
					if (glowFile.exists()) {
						glowInput = new FileInputStream(glowFile);
					}
				}

				InputStream colorInput = new FileInputStream(colorFile);

				result = conv.convert(colorInput, normInput, glowInput);

				name = colorFile.getName();
				name = name.substring(0, name.length() - 4);

				System.out.println(name);
				if (name.length() > 15) {
					System.out.println("  name too long (will be truncated to 15 characters): " + name);
				}

			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			
			// write result to WAD
			synchronized (wad) {
				try {
					wad.addMipTexture(name, result);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}

		}

	}

}
