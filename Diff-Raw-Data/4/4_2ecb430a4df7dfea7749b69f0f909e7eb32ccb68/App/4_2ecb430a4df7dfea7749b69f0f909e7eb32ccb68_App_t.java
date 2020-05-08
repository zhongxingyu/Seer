 package org.siraya.dconfig;
 import org.yaml.snakeyaml.Yaml;
 import org.apache.commons.cli.*;
 import java.io.File;
 import java.util.*;
 import java.io.*;
 /**
  * -in <input dir> -out <output_file> -format <output format> -query <query
  * string>
  * 
  */
 public class App {
 	public static void main(String[] args) throws Exception {
 		CommandLineParser parser = new BasicParser();
 		Option in = OptionBuilder.withArgName("in").hasArg()
 				.withDescription("input file").isRequired(true).create("in");
 		Option format = OptionBuilder.withArgName("fotmat").hasArg()
 				.withDescription("output format").isRequired(false)
 				.create("format");
		Option out = OptionBuilder.withArgName("out").hasArg()
				.withDescription("output file").isRequired(true)
				.create("out");
 		Option query = OptionBuilder.withArgName("query").hasArg()
 				.withDescription("query string").isRequired(false)
 				.create("query");
 		Options options = new Options();
 
 		options.addOption(in);
 		options.addOption(out);
 		options.addOption(format);
 		options.addOption(query);
 
 		try {
 			// parse the command line arguments
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp( "dconfig", options );
 			CommandLine line = parser.parse(options, args);
 			String inString = line.getOptionValue("in");
 			String queryString = line.getOptionValue("query");
 			String outString = line.getOptionValue("out");
 			QueryNode node = QueryNodeUtil.createQueryNode(inString, queryString);
 			
 			Yaml yaml = new Yaml();
 			String outputString = yaml.dump(node);			
 			FileOutputStream os  = new java.io.FileOutputStream(outString);
 			os.write(outputString.getBytes());
 			os.flush();
 			os.close();
 		}catch (ParseException exp) {
 			// oops, something went wrong
 			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
 		}
 
 	}
 }
