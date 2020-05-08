 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile: Main.java,v $ 
	Created on $Date: 2003-07-04 15:17:27 $ 
 */
 package net.sf.statcvs;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.lang.reflect.Method;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import net.sf.statcvs.input.Builder;
 import net.sf.statcvs.input.CvsLogfileParser;
 import net.sf.statcvs.input.LogSyntaxException;
 import net.sf.statcvs.input.RepositoryFileManager;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.output.CommandLineParser;
 import net.sf.statcvs.output.ConfigurationException;
 import net.sf.statcvs.output.ConfigurationOptions;
 import net.sf.statcvs.output.xml.HTMLRenderer;
 import net.sf.statcvs.output.xml.OutputSettings;
 
 /**
  * StatCvs Main Class; it starts the application and controls command-line
  * related stuff
  * @author Lukasz Pekacki
  * @author Richard Cyganiak
 * @version $Id: Main.java,v 1.7 2003-07-04 15:17:27 vanto Exp $
  */
 public class Main {
 	private static Logger logger = Logger.getLogger("net.sf.statcvs");
 	private static LogManager lm = LogManager.getLogManager();
 
 	/**
 	 * Main method of StatCvs
 	 * @param args command line options
 	 */
 	public static void main(String[] args) {
 		System.out.println(Messages.getString("PROJECT_NAME")
 				+ Messages.NL);
 		System.setProperty("java.awt.headless", "true");
 		
 		if (args.length == 0) {
 			printProperUsageAndExit();
 		}
 		if (args.length == 1) {
 			String arg = args[0].toLowerCase();
 			if (arg.equals("-h") || arg.equals("-help")) {
 				printProperUsageAndExit();
 			} else if (arg.equals("-version")) {
 				printVersionAndExit();
 			}
 		}
 
 		try {
 			new CommandLineParser(args).parse();
 			run();
 		} catch (ConfigurationException cex) {
 			System.err.println(cex.getMessage());
 			printProperUsageAndExit();
 		} catch (LogSyntaxException lex) {
 			printLogErrorMessageAndExit(lex.getMessage());
 		} catch (OutOfMemoryError oome) {
 			printOutOfMemMessageAndExit();
 		} catch (Exception ioex) {
 			ioex.printStackTrace();
 			printErrorMessageAndExit(ioex.getMessage());
 		}
 
 		System.exit(0);
 	}
 
 	private static void printProperUsageAndExit() {
 		System.out.println(
 		//max. 80 chars
 		//         12345678901234567890123456789012345678901234567890123456789012345678901234567890
 				  "Usage: java -jar statcvs.jar [options] <logfile> <directory>\n"
 				+ "\n"
 				+ "Required parameters:\n"
 				+ "  <logfile>          path to the cvs logfile of the module\n"
 				+ "  <directory>        path to the directory of the checked out module\n"
 				+ "\n"
 				+ "Some options:\n"
 				+ "  -version           print the version information and exit\n"
 				+ "  -output-dir <dir>  directory where HTML suite will be saved\n"
 				+ "  -include <pattern> include only files matching pattern, e.g. **/*.c;**/*.h\n"
 				+ "  -exclude <pattern> exclude matching files, e.g. tests/**;docs/**\n"
 				+ "  -title <title>     Project title to be used in reports\n"
 				+ "  -viewcvs <url>     integrate with ViewCVS installation at <url>\n"
 				+ "  -verbose           print extra progress information\n"
 				  + "  -output-suite [class] use the xml renderer\n"
 				+ "\n"
 				+ "Full options list: http://statcvs.sf.net/manual");
 		System.exit(1);
 	}
 
 	private static void printVersionAndExit() {
 		System.out.println("Version " + Messages.getString("CVS_VERSION_TAG"));
 		System.exit(1);
 	}
 
 	private static void printOutOfMemMessageAndExit() {
 		System.err.println("OutOfMemoryError.");
 		System.err.println("Try running java with the -mx option (e.g. -mx128m for 128Mb).");
 		System.exit(1);
 	}
 
 	private static void printLogErrorMessageAndExit(String message) {
 		System.err.println("Logfile parsing failed.");
 		System.err.println(message);
 		System.exit(1);
 	}
 
 	private static void printErrorMessageAndExit(String message) {
 		System.err.println(message);
 		System.exit(1);
 	}
 
 	public static void run() throws Exception
 	{
 		long memoryUsedOnStart = Runtime.getRuntime().totalMemory();
 		long startTime = System.currentTimeMillis();
 		
 		initLogger();
 		
 		CvsContent content = readLogFile();
 		generateSuite(content);
 		
 		long endTime = System.currentTimeMillis();
 		long memoryUsedOnEnd = Runtime.getRuntime().totalMemory();
 		logger.info("runtime: " + (((double) endTime - startTime) / 1000) 
 					+ " seconds");
 		logger.info("memory usage: "
 					+ (((double) memoryUsedOnEnd 
 						- memoryUsedOnStart) / 1024) + " kb");
 	}
 
 	public static void initLogger() throws LogSyntaxException {
 		try {
 			String props = ConfigurationOptions.getLoggingProperties();
 			lm.readConfiguration(Main.class.getResourceAsStream(props));
 		}
 		catch (IOException e) {
 			System.err.println("ERROR: Logging could not be initialized!");
 		}
 
 		logger.info("Generating report for " 
 					+ ConfigurationOptions.getProjectName()
 					+ " into " + ConfigurationOptions.getOutputDir());
 	}
 
 	/**
 	 * @throws ConfigurationException if a required ConfigurationOption was 
 	 *                                not set 
 	 * @throws LogSyntaxException if the logfile contains unexpected syntax
 	 * @throws IOException if the log file can not be read
 	 */
 	public static CvsContent readLogFile() 
 		throws ConfigurationException, IOException, LogSyntaxException
 	{
 		if (ConfigurationOptions.getLogFileName() == null) {
 			throw new ConfigurationException("Missing logfile name");
 		}
 		if (ConfigurationOptions.getCheckedOutDirectory() == null) {
 			throw new ConfigurationException("Missing checked out directory");
 		}
 		
 		logger.info("Parsing CVS log '"
 				+ ConfigurationOptions.getLogFileName() + "'");
 
 		Reader logReader = new FileReader(ConfigurationOptions.getLogFileName());
 		RepositoryFileManager repFileMan
 			= new RepositoryFileManager
 				(ConfigurationOptions.getCheckedOutDirectory());
 		Builder builder = new Builder(repFileMan);
 		new CvsLogfileParser(logReader, builder).parse();
 		return builder.getCvsContent();
 	}
 
 	/**
 	 * Generates HTML report. {@link net.sf.statcvs.output.ConfigurationOptions}
 	 * must be initialized before calling this method.
 	 * @throws Exception if somethings goes wrong
 	 */
 	public static void generateSuite(CvsContent content) throws Exception {	
		if (ConfigurationOptions.getOutputSuite() != null) {
 			ConfigurationOptions.setOutputSuite(HTMLRenderer.class.getName());
 		}
 		logger.info("Reading output settings");
 		String filename = getSettingsPath() + "output.properties";
 		try {
 			OutputSettings.getInstance().read(filename);
 		}
 		catch (IOException e) {
 			logger.warning("Could not read settings: " 
 						   + e.getMessage());
 		}
 
 		logger.info("Creating suite using "+ConfigurationOptions.getOutputSuite());
 	
 		Class c = Class.forName(ConfigurationOptions.getOutputSuite());
 		Method m = c.getMethod("generate", new Class[] { CvsContent.class });
 		m.invoke(null, new Object[] { content });
 	}
 
     public static String getSettingsPath()
     {
 		StringBuffer sb = new StringBuffer();
 		sb.append(System.getProperty("user.home"));
 		sb.append(File.separatorChar);
 		sb.append(".statcvs");
 		sb.append(File.separatorChar);
 		return sb.toString();
     }
 
 }
