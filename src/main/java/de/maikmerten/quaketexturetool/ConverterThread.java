package de.maikmerten.quaketexturetool;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Queue;

/**
 *
 * @author maik
 */
public class ConverterThread extends Thread {

	private final Queue<File> fileQueue;
	private final Converter conv;
	private final Wad wad;
	private final FileFinder fileFinder;
	private final boolean noLiquidFullbrights;

	public ConverterThread(Queue<File> fileQueue, Converter conv, Wad wad, FileFinder fileFinder, boolean noLiquidFullbrights) {
		this.fileQueue = fileQueue;
		this.conv = conv;
		this.wad = wad;
		this.fileFinder = fileFinder;
		this.noLiquidFullbrights = noLiquidFullbrights;
	}

	private boolean isLiquid(String name) {
		return name.startsWith("*");
	}

	@Override
	public void run() {

		while (true) {
			File colorFile;
			String name;
			byte[] result;

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
				name = colorFile.getName();
				name = name.substring(0, name.length() - 4);

				// try to find file with surface normals
				InputStream normInput = null;
				File normFile = fileFinder.findNormFile(name);
				if(normFile != null) {
					normInput = new FileInputStream(normFile);
				}

				// try to find file with luminance information
				InputStream glowInput = null;
				File glowFile = fileFinder.findGlowFile(name);
				if(glowFile != null) {
					glowInput = new FileInputStream(glowFile);
				}

				InputStream colorInput = new FileInputStream(colorFile);

				boolean ignoreFullbrights = isLiquid(name) && noLiquidFullbrights;

				result = conv.convert(colorInput, normInput, glowInput, name, ignoreFullbrights);

				System.out.println(name);
				if (name.length() > 15) {
					System.out.println("  name too long (will be truncated to 15 characters): " + name);
				}

			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

			if (result != null) {
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

}
