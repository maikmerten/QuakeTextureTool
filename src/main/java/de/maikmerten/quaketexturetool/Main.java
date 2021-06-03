package de.maikmerten.quaketexturetool;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
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
	private final static String opt_ditherstrength = "ditherstrength";
	private final static String opt_liquidfullbrights = "liquidfullbrights";
	private final static String opt_output = "output";

	public static void main(String[] args) throws Exception {
		
		// set up command line parsing
		Options opts = new Options();
		opts.addOption("h", "help", false, "Displays help");
		opts.addOption(opt_reduce, true, "Downsampling factor (default: 1)");
		opts.addOption(opt_ditherfull, true, "Dither fullbrights (default: 0)");
		opts.addOption(opt_ditherstrength, true, "Dither strength (default: 0.25)");
		opts.addOption(opt_liquidfullbrights, true, "Allow fullbrights on liquids (default: 0)");
		opts.addOption(opt_output, true, "file name for output WAD");
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(opts, args);
		
		if(cmd.hasOption("help") || cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar QuakeTextureTool.jar", opts);
			System.exit(0);
		}
		
		if(!cmd.hasOption(opt_output)) {
			System.out.println("### You must specify an output file! ###");
			System.out.println("Use the -h option to get a list of options.");
			System.exit(1);
		}
		
		int reduce = Integer.parseInt(cmd.getOptionValue(opt_reduce, "1"));
		boolean ditherFullbrights = Integer.parseInt(cmd.getOptionValue(opt_ditherfull, "0")) != 0;
		float ditherStrength = 0.25f;
		if(cmd.hasOption(opt_ditherstrength)) {
			ditherStrength = Float.parseFloat(cmd.getOptionValue(opt_ditherstrength));
		}
		boolean noLiquidFullbrights = Integer.parseInt(cmd.getOptionValue(opt_liquidfullbrights, "0")) == 0;
		String outputFile = cmd.getOptionValue(opt_output);

		
		Converter conv = new Converter();
		conv.setReduce(reduce);
		conv.setDitherFullbrights(ditherFullbrights);
		conv.setDitherStrength(ditherStrength);

		File workingDir = new File(".");
		
		FileFinder fileFinder = new FileFinder(workingDir);
		Queue<File> colorMapFiles = fileFinder.findColorMaps();
		
		Wad wad = new Wad();

		List<Thread> threads = new ArrayList<>();
		for(int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i) {
			Thread t = new ConverterThread(colorMapFiles, conv, wad, fileFinder, noLiquidFullbrights);
			threads.add(t);
			t.start();
		}
		
		for(Thread t : threads) {
			t.join();
		}
		
		
		File wadFile = new File(outputFile);
		try (FileOutputStream fos = new FileOutputStream(wadFile)) {
			wad.write(fos);
		}

	}

}
