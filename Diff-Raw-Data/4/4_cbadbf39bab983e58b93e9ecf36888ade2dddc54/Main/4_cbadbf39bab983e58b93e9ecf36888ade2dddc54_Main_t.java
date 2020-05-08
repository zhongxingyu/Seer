 /*
  *  StatCvs-XML - XML output for StatCvs.
  *
  *  Copyright by Steffen Pingel, Tammo van Lessen.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  version 2 as published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package de.berlios.statcvs.xml;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Logger;
 
 import net.sf.statcvs.input.Builder;
 import net.sf.statcvs.input.CvsLogfileParser;
 import net.sf.statcvs.input.EmptyRepositoryException;
 import net.sf.statcvs.input.LogSyntaxException;
 import net.sf.statcvs.input.RepositoryFileManager;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.util.FilePatternMatcher;
 import net.sf.statcvs.util.LogFormatter;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import de.berlios.statcvs.xml.output.DocumentRenderer;
 import de.berlios.statcvs.xml.output.DocumentSuite;
 import de.berlios.statcvs.xml.output.HTMLRenderer;
 import de.berlios.statcvs.xml.output.ReportSettings;
 import de.berlios.statcvs.xml.util.FileHelper;
 
 /**
  * StatCvs Main Class; it starts the application and controls command-line
  * related stuff
  * @author Lukasz Pekacki
  * @author Richard Cyganiak
 * @version $Id: Main.java,v 1.14 2004-02-27 14:07:20 squig Exp $
  */
 public class Main {
 	private static String projectName;
 
 	private static Logger logger = Logger.getLogger("net.sf.statcvs");
 
 	public static final String VERSION = "@VERSION@";
 	/**
 	 * Main method of StatCvs
 	 * @param args command line options
 	 */
 	public static void main(String[] args) {
 		System.out.println(I18n.tr("StatCvs-XML - CVS statistics generation")+"\n");
 		System.setProperty("java.awt.headless", "true");
 		
 		if (args.length == 1) {
 			String arg = args[0].toLowerCase();
 			if (arg.equals("-h") || arg.equals("-help")) {
 				printProperUsageAndExit();
 			} else if (arg.equals("-version")) {
 				printVersionAndExit();
 			}
 		}
 
 		try {
 			ReportSettings settings = readSettings(args);
 			initLogger();
 			generateSuite(settings);
 		} catch (InvalidCommandLineException e) {
 			System.err.println(e.getMessage());
 			printProperUsageAndExit();
 		} catch (IOException e) {
 			printErrorMessageAndExit(e.getMessage());
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
 				  "Usage: java -jar @JAR@ [options] <logfile> <directory>\n"
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
 				+ "  -weburl <url>      integrate with web repository installation at <url>\n"
 				+ "  -verbose           print extra progress information\n"
 				+ "  -output-suite [class] use the xml renderer\n"
 				+ "  -use-history       use history file for proper loc counts\n"
 				+ "\n"
 				+ "If statcvs cannot recognize the type of your web repository, please use the\n"
 				+ "following switches:\n"
 				+ "  -viewcvs <url>     integrate with viewcvs installation at <url>\n"
 				+ "  -cvsweb <url>      integrate with cvsweb installation at <url>\n"
 				+ "  -chora <url>       integrate with chora installation at <url>\n"
 				+ "\n");
 				//+ "Full options list: http://statcvs.sf.net/manual");
 		System.exit(1);
 	}
 
 	private static void printVersionAndExit() {
 		System.out.println("Version " + VERSION);
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
 
 	public static ReportSettings readSettings(String[] args) throws IOException, InvalidCommandLineException 
 	{
 		Hashtable cmdlSettings = new Hashtable();
 		CommandLineParser parser = new CommandLineParser(args);
 		parser.parse(cmdlSettings);
 		
 		ReportSettings settings = new ReportSettings(cmdlSettings);
 		File file = new File("statcvs.xml");
 		if (file.exists()) {
 			try {
				logger.info(I18n.tr("Reading settings from {0}", file.getName()));
 				SAXBuilder builder = new SAXBuilder();
 				Document suite = builder.build(file);
 				Element element = suite.getRootElement().getChild("settings");
 				if (element != null) {
 					settings.load(element);
 				}
 			}
 			catch (JDOMException e) {
 				throw new IOException(e.getMessage());
 			}
 		}
 		return settings;
 	}				
 
 	public static void initLogger() throws LogSyntaxException {
 		ConsoleHandler ch = new ConsoleHandler();
 		ch.setFormatter(new LogFormatter());
 		//ch.setLevel(Settings.getLoggingLevel());
 		//LogManager.getLogManager().getLogger("net.sf.statcvs").addHandler(ch);
 		logger.addHandler(ch);
 		logger.setUseParentHandlers(false);
 	}
 
 	/**
 	 * @throws IOException if a required ConfigurationOption was 
 	 *                                not set 
 	 * @throws LogSyntaxException if the logfile contains unexpected syntax
 	 * @throws IOException if the log file can not be read
 	 */
 	public static void generateSuite(ReportSettings settings) 
 		throws IOException, LogSyntaxException, EmptyRepositoryException
 	{
 		FilePatternMatcher includeMatcher = null;
 		if (settings.getString("include") != null) {
 			includeMatcher = new FilePatternMatcher(settings.getString("include"));
 		} 
 		FilePatternMatcher excludeMatcher = null;
 		if (settings.getString("exclude") != null) {
 			includeMatcher = new FilePatternMatcher(settings.getString("include"));
 		} 
 		
 		String logFilename = settings.getString("logFile", "cvs.log");
 		Reader logReader = new FileReader(logFilename);
 
 		logger.info("Parsing CVS log '" + logFilename + "'");
 		RepositoryFileManager repFileMan
 			= new RepositoryFileManager(settings.getString("localRepository", "."));
 		Builder builder = new Builder(repFileMan, includeMatcher, excludeMatcher);
 		projectName = builder.getProjectName();
 		new CvsLogfileParser(logReader, builder).parse();
 		CvsContent content = builder.createCvsContent(settings.getBoolean("useHistory", false));
 
 		File outDir = settings.getOutputPath();
 		if (!outDir.exists() && !outDir.mkdirs()) {
 			throw new IOException(I18n.tr("Could not create output directory: {0}", outDir.getAbsolutePath()));
 		}
 
 
 		logger.info("Generating report for " + settings.getProjectName()
 					+ " into " + outDir.getAbsolutePath());
 
 //		if (Settings.getOutputSuite() == null) {
 //			Settings.setOutputSuite(HTMLRenderer.class.getName());
 //		}
 		if (settings.getWebRepository() != null) {
 			logger.info("Assuming web repository is " + settings.getWebRepository().getName());
 		}
 		
 		String rendererClassname = settings.getRendererClassname();
 		logger.info("Creating suite using " + rendererClassname);
 		DocumentRenderer renderer;
 		try {
 			Class c = Class.forName(rendererClassname);
 			Method m = c.getMethod("create", new Class[] { CvsContent.class, ReportSettings.class });
 			renderer = (DocumentRenderer)m.invoke(null, new Object[] { content, settings });
 		}
 		catch (Exception e) {
 			throw new IOException(I18n.tr("Could not create renderer: {0}", e.getLocalizedMessage()));
 		}
 		
 		URL suiteURL= FileHelper.getResource(settings.getString("suite", "resources/suite.xml"));
 		DocumentSuite suite = new DocumentSuite(suiteURL, content);
 		suite.generate(renderer, settings);
 	}
 
 }
