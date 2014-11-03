package de.maikmerten.quaketexturetool;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author maik
 */
public class Main {
	
	private final static String opt_reduce = "reduce";
	private final static String opt_ditherfull = "ditherfullbrights";

	public static void main(String[] args) throws Exception {
		
		
		
		// set up command line parsing
		Options opts = new Options();
		opts.addOption("h", "help", false, "Displays help");
		opts.addOption(opt_reduce, true, "Downsampling factor (default: 4)");
		opts.addOption(opt_ditherfull, true, "Dither fullbrights (default: 0)");
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(opts, args);
		
		if(cmd.hasOption("help") || cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar QuakeTextureTool.jar", opts);
			System.exit(0);
		}
		
		int reduce = Integer.parseInt(cmd.getOptionValue(opt_reduce, "4"));
		boolean ditherFullbrights = Integer.parseInt(cmd.getOptionValue(opt_ditherfull, "0")) != 0;

		
		Converter conv = new Converter();
		conv.setReduce(reduce);
		conv.setDitherFullbrights(ditherFullbrights);

		File workingDir = new File(".");
		File outputDir = new File(workingDir.getAbsoluteFile() + File.separator + "output" + File.separator);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		Queue<File> colorMapFiles = new LinkedList<>();
		FileFilter filefilter = new ColorMapFileFilter();
		for (File f : workingDir.listFiles()) {
			if(filefilter.accept(f)) {
				colorMapFiles.add(f);
			}
		}
		
		Wad wad = new Wad();

		List<Thread> threads = new ArrayList<>();
		for(int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i) {
			Thread t = new ConverterThread(colorMapFiles, conv, wad);
			threads.add(t);
			t.start();
		}
		
		for(Thread t : threads) {
			t.join();
		}
		
		
		File wadFile = new File(outputDir.getAbsolutePath() + File.separator + "output.wad");
		try (FileOutputStream fos = new FileOutputStream(wadFile)) {
			wad.write(fos);
		}

	}

}
