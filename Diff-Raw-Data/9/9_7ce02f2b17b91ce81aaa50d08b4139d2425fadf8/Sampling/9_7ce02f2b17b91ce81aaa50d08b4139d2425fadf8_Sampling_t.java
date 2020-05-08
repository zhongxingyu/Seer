 package it.unito.geosummly.api.cli;
 
 import fi.foyt.foursquare.api.FoursquareApiException;
 import it.unito.geosummly.CoordinatesNormalizationType;
 import it.unito.geosummly.InformationType;
 import it.unito.geosummly.SamplingOperator;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.json.JSONException;
 
 /**
  * @author James
  *
  * API for sampling state
  */
 
 public class Sampling {
 	
 	private String inFile=null;
 	private String outDir=null;
 	private ArrayList<Double> coordinates=new ArrayList<Double>();
 	private int gridCells=20;
 	private int randomCells=-1;
 	private long sleepMs=0;
 	private InformationType venueType=InformationType.SINGLE;
 	private CoordinatesNormalizationType coordType=CoordinatesNormalizationType.NORM;
 	//private String socNet="foursquare";
 
 	public static void main(String[] args) {
 		Sampling sampling = new Sampling();
         sampling.run(args);
 	}
 	
 	private void run (String[] args) {
 		Options options= initOptions(); //define list of options
 		CommandLineParser parser=new PosixParser(); //create the command line parser
 		HelpFormatter formatter = new HelpFormatter();
 		Boolean mandatory=false; //check the presence of mandatory options
 		Boolean inputFlag=false; //check the presence either of input or coord;
 		String helpUsage="geosummly sampling -input<path/to/file.geojson> -output<path/to/dir> [options]\ngeosummly sampling -coord <n,s,w,e> -output<path/to/dir> [options]";
 		String helpFooter="\nThe options coord, input (only if coord is not specified), output are mandatory. The options input and coord are mutually exclusive. The options input and gnum are mutually exclusive. "
 				 + "The options input and rnum are mutually exclusive. The output consist of a file of single venues, a file of grid-shaped aggregated venues, a file of density values of the previous "
 				 + "aggregates, a file with intra-feature normalized density values shifted in [0,1].";
 		
 		try {
 			CommandLine line = parser.parse(options, args);
 			
 			if(line.hasOption("input") && line.hasOption("output")) {
 				inFile=line.getOptionValue("input");
 				//file extension has to be geojson
				if(!inFile.endsWith("geojson")) {
 					formatter.printHelp(150, helpUsage, "\ncommands list:", options, helpFooter);
 					System.exit(-1);
 				}
 				outDir=line.getOptionValue("output");
 				mandatory=true;
 				inputFlag=true;
 			}
 			
 			if(line.hasOption("coord") && line.hasOption("output")) {
 				String[] c=line.getOptionValues("coord");
 				for(String s: c)
 					coordinates.add(Double.parseDouble(s));
 				outDir=line.getOptionValue("output");
 				if(line.hasOption("gnum")) {
 					gridCells=Integer.parseInt(line.getOptionValue("gnum"));
 					if(gridCells<0) {
 						formatter.printHelp(150, helpUsage, "\ncommands list:", options, helpFooter);
 						System.exit(-1);
 					}		
 				}
 				if(line.hasOption("rnum")) {
 					randomCells=Integer.parseInt(line.getOptionValue("rnum"));
 					if(randomCells<0) {
 						formatter.printHelp(150, helpUsage, "\ncommands list:", options, helpFooter);
 						System.exit(-1);
 					}
 				}
 				mandatory=true;
 				inputFlag=false;
 			}
 			
 			if(line.hasOption("ltype")) {
 				String l=line.getOptionValue("ltype");
 				if(l.equals("norm"))
 					coordType=CoordinatesNormalizationType.NORM;
 				else if(l.equals("notnorm"))
 					coordType=CoordinatesNormalizationType.NOTNORM;
 				else if(l.equals("missing"))
 					coordType=CoordinatesNormalizationType.MISSING;
 				else {
 					formatter.printHelp(150, helpUsage, "\ncommands list:", options, helpFooter);
 					System.exit(-1);
 				}
 			}
 			
 			if(line.hasOption("vtype")) {
 				String v=line.getOptionValue("vtype");
 				if(v.equals("single"))
 					venueType=InformationType.SINGLE;
 				else if(v.equals("cell"))
 					venueType=InformationType.CELL;
 				else {
 					formatter.printHelp(150, helpUsage, "\ncommands list:", options, helpFooter);
 					System.exit(-1);
 				}
 			}
 			
 			/*if(line.hasOption("social")) {
 				//manage social media
 			}
 			if(line.hasOption("cache")) {
 				//manage cache
 			}*/
 			
 			if(line.hasOption("sleep")) {
 				sleepMs=Long.parseLong(line.getOptionValue("sleep"));
 				if(sleepMs<0) {
 					formatter.printHelp(150, helpUsage, "\ncommands list:", options, helpFooter);
 					System.exit(-1);
 				}
 			}
 			
 			if (line.hasOption("help") || !mandatory) {
                 formatter.printHelp(150, helpUsage,"\ncommands list:", options, helpFooter);
                 System.exit(-1);
             }
 			SamplingOperator so=new SamplingOperator();
 			if(inputFlag)
 				so.executeWithInput(inFile, outDir, venueType, coordType, sleepMs);
 			else
 				so.executeWithCoord(coordinates, outDir, gridCells, randomCells, venueType, coordType, sleepMs);
 			
 		}
 		catch(ParseException | NumberFormatException | FoursquareApiException | IOException | JSONException | InterruptedException e) {
 			System.out.println("Unexpected exception: " + e.getMessage());
 		}
 	}
 	 
 	 
 	@SuppressWarnings("static-access")
 	private static Options initOptions() {
 			 
 		 Options options = new Options(); //define list of options
 		 
 		 //option input
 		 Option input=OptionBuilder.withLongOpt("input").withDescription("set the input file")
 						.hasArg().withArgName("path/to/file").create("I");
 		 
 		 //options input and coord have to be mutually exclusive
 		 OptionGroup g1=new OptionGroup();
 		 g1.addOption(input);
 		 g1.addOption(OptionBuilder.withLongOpt("coord").withDescription("set the input grid coordinates")
 						.hasArgs(4).withValueSeparator(',').withArgName("n,s,w,e").create("L"));
 		 
 		 //options input and gnum have to be mutually exclusive
 		 OptionGroup g2=new OptionGroup();
 		 g2.addOption(input);
 		 g2.addOption(OptionBuilder.withLongOpt("gnum").withDescription("set the number of cells of a side of the squared grid. Default 20")
 						.hasArg().withArgName("arg").create("g"));
 		 
 		 //options input and rnum have to be mutually exclusive
 		 OptionGroup g3=new OptionGroup();
 		 g3.addOption(input);
 		 g3.addOption(OptionBuilder.withLongOpt("rnum").withDescription("set the number of cells, taken randomly, chosen for the sampling")
 						.hasArg().withArgName("arg").create("r"));
 		 
 		 //add mutually exclusive options for sampling
 		 options.addOptionGroup(g1);
 		 options.addOptionGroup(g2);
 		 options.addOptionGroup(g3);
 		 
 		 //add all other options for sampling
 		 options.addOption(OptionBuilder.withLongOpt( "output" ).withDescription("set the output directory")
 							.hasArg().withArgName("path/to/dir").create("O"));
 		 options.addOption(OptionBuilder.withLongOpt( "vtype" ).withDescription("set the type of venue grouping. Allowed values: single, cell. Default single")
 							.hasArg().withArgName("arg").create("v"));
 		 options.addOption(OptionBuilder.withLongOpt( "ltype" ).withDescription("set the type of coordinates (latitude and langitude) normalization. Allowed values: norm, notnorm, missing. Default norm")
 							.hasArg().withArgName("arg").create("l"));
		 options.addOption(OptionBuilder.withLongOpt("social").withDescription("set the social network for meta-data collection. So far only foursquare is activable. Default fourquare")
 							.hasArg().withArgName("arg").create("s"));
		 options.addOption(OptionBuilder.withLongOpt( "sleep" ).withDescription("set the milliseconds between two calls to social media server. Default 0")
					.hasArg().withArgName("arg").create("z"));
 		 options.addOption("C", "cache", false, "cache activation. Default deactivated");
 		 
 		//more options
 		options.addOption("H", "help", false, "print the command list");
 		
 		return options;
 	}
 }
