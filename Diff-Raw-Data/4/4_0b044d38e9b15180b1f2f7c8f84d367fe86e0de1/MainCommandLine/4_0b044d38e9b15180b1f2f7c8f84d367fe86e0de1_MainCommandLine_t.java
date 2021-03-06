 /**
 Khalid
 */
 package org.sikuli.slides.guis;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.sikuli.slides.utils.Constants;
 import org.sikuli.slides.utils.MyFileFilter;
 
 /**
  * sikuli-slides Command Line tool using POSIX like options.
  * 
  * @author Khalid
  *
  */
 public class MainCommandLine {
 	private static final String applicationName = "sikuli-slides";
 	private static final String versionNumber="1.2.0";
 	private static final String commandLineSyntax = "java -jar "+
 			applicationName+"-"+versionNumber+".jar "+
 			"presentation_file.pptx";
 	private static final String fileNotFoundError="No such file.";
 	/**
 	* Parse the command-line arguments as POSIX like options (one character long option).
 	* @param args Command-line arguments
 	* @return the .pptx file name
 	*/
 	private static String usePosixParser(final String[] args) throws Exception 
 	{
 		final CommandLineParser parser = new PosixParser();  
 	    final Options posixOptions = getPOSIXCommandLineOptions();  
 	    CommandLine cmd;  
 	    	cmd = parser.parse(posixOptions, args);
 	        if (cmd.hasOption("h")){
         		printHelp(getPOSIXCommandLineOptions(), 120, 
     					"sikuli-slides -- help", "sikuli-slides -- (END)", 5, 3, true, System.out);
         		return null;
 	        }
 	    	else if (cmd.hasOption("w")){
 	    		int wait=Integer.parseInt(cmd.getOptionValue("w"));
 	        	Constants.MaxWaitTime=wait;
 	    	}
 	    	else if (cmd.hasOption("oldsyntax")){
 	        	Constants.UseOldSyntax=true;
 	    	}
 	    	else if(cmd.hasOption("p")){
 	    		int precision=Integer.parseInt(cmd.getOptionValue("p"));
 	    		if(precision>0&&precision<11){
 	    			Constants.MinScore=(double)precision/10;
 	    		}
 	    		else{
 	    			String errorMessage="Invalid precision scale value.\n";
 	    			System.out.write(errorMessage.getBytes());
 	    			throw new Exception();
 	    		}
 	    	}
 	        
 	        // check arguments
 	        final String[] remainingArguments = cmd.getArgs();
 	        if(remainingArguments==null||remainingArguments.length==0){
 	        	printUsage(applicationName, getPOSIXCommandLineOptions(), System.out);
 	        	return null;
 	        }
 	        else if(remainingArguments.length>0){
 	        	MyFileFilter myFileFilter=new MyFileFilter();
 	        	String FileName=remainingArguments[0];
 	        	File source_file=new File(FileName);
 	        	if(myFileFilter.accept(source_file)){
 	        		if(source_file.exists()){
 	        			showTextHeader(System.out);
 	        			displayBlankLine();
 	        			return source_file.getAbsolutePath();
 	        		}
 	        		else{
 	        			System.out.write(fileNotFoundError.getBytes()); 
 	        			displayBlankLine();
 	        			printUsage(applicationName, getPOSIXCommandLineOptions(), System.out);
 	        		}
 	        	}
 	        	else{
 	        		printUsage(applicationName, getPOSIXCommandLineOptions(), System.out);
 	        	}
 	        }
 
 		return null;
 	}
 	
 	/**
 	 * Return all valid POSIX command-line options
 	 * @return valid POSIX command-line options
 	 */
 	@SuppressWarnings("static-access")
 	private static Options getPOSIXCommandLineOptions() {
 		final Options posixOptions=new Options();
 		
 		Option waitOption=OptionBuilder.withArgName("max_wait_time")
                 .hasArg()
                 .withDescription("The maximum time to wait in milliseconds to find a target on the screen (default 15000 ms)." )
                 .create("w");
 		
 		Option precisionOption=OptionBuilder.withArgName("precision")
                 .hasArg()
                 .withDescription("The precision value to control the degree of fuzziness of the image recognition search. It's a 10-point scale where 1 is the least precise search and 10 is the most precise search. (default is 7)." )
                 .create("p");
 		
 		Option oldSyntaxOption=OptionBuilder.withArgName("oldsyntax")
                .withDescription("Forces the system to use the old syntax that uses special shapes to represent actions. The syntax is based on the following annotations: Rectangle shape: left click. " +
                		"Rounded rectangle: drag and drop. Frame: double click. Oval: right click. Text Box: Keyboard typing. Cloud: open URL in default browser.")
                 .create("oldsyntax");
 		
 		Option helpOption=new Option("h", "help");
 		
 		posixOptions.addOption(helpOption);
 		posixOptions.addOption(waitOption);
 		posixOptions.addOption(precisionOption);
 		posixOptions.addOption(oldSyntaxOption);
 		
 		return posixOptions;
 	}
 	
 	private static void showTextHeader(final OutputStream out){
 		String textHeader="sikuli-slides -- accessible visual automation";
 		try{
 			out.write(textHeader.getBytes());
 		}
 		catch (IOException ioEx){
 			System.out.println(textHeader);
 		}
 	}
 	
 	/**
 	 * print usage information to provided OutputStream
 	 * @param applicationName Name of application to list in usage
 	 * @param options Command-line options to be part of usage
 	 * @param out OutputStream to which to write the usage information
 	 */
 	public static void printUsage(final String applicationName, final Options options,final OutputStream out)
 	{
 		final PrintWriter writer = new PrintWriter(out);  
 	    final HelpFormatter usageFormatter = new HelpFormatter();  
 	    usageFormatter.printUsage(writer, 120, commandLineSyntax, options);
 	    writer.flush();
 	}
 	/**
 	 * Write command-line tool help
 	 * @param options the possible options for the command-line
 	 * @param printedRowWidth the raw width
 	 * @param header the header text at the beginning of the help
 	 * @param footer the footer text at the end of the help
 	 * @param leftPad the number of white spaces before the option
 	 * @param descPad the number of white spaces before the option description
 	 * @param autoUsage indicates whether the usage is displayed in the help or not
 	 * @param out the OutputStream to write to
 	 */
 	private static void printHelp(final Options options, final int printedRowWidth,
 			final String header, final String footer,
 			final int leftPad, final int descPad,
 			final boolean autoUsage,final OutputStream out){
 		showTextHeader(System.out);
 		displayBlankLine();
 		final PrintWriter printWriter = new PrintWriter(out);
 		final HelpFormatter helpFormatter = new HelpFormatter();
 		helpFormatter.printHelp(printWriter, printedRowWidth, 
 				commandLineSyntax, header, options, leftPad, descPad, footer, autoUsage);
 		printWriter.flush();
 	}
 	
 	private static void displayBlankLine(){
 		try {
 			System.out.write("\n".getBytes());
 		} catch (IOException e) {
 			System.out.println();
 		}
 	}
 	public static String runCommandLineTool(final String[] args){
 		if (args.length < 1){
 			printUsage(applicationName, getPOSIXCommandLineOptions(), System.out);
 		}
 		else{
 			try{
 				return usePosixParser(args);
 			}
 	    	catch(Exception exception){
 				printUsage(applicationName, getPOSIXCommandLineOptions(), System.out);
 				displayBlankLine();
 	    	}
 		}
 		return null;
 	}
 	
 }
