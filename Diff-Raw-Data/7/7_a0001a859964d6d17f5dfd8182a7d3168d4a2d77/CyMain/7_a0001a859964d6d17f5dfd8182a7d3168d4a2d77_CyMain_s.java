 /*
  File: CyMain.java
 
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 package cytoscape;
 
 import java.awt.Dimension;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.UIManager;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import com.install4j.api.launcher.StartupNotification;
 import com.jgoodies.looks.LookUtils;
 import com.jgoodies.looks.Options;
 import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
 
 import cytoscape.init.CyInitParams;
 import cytoscape.util.FileUtil;
 import cytoscape.logger.CyLogger;
 
 /**
  * This is the main startup class for Cytoscape. This parses the command line
  * and implements CyInitParams so that it can be used to initialize cytoscape.
  * 
  * <p>
  * Look and Feel is modified for jgoodies 2.1.4 by Kei Ono
  * </p>
  */
 public class CyMain implements CyInitParams {
 	protected String[] args;
 
 	protected Properties props;
 
 	protected String[] graphFiles;
 
 	protected String[] plugins;
 
 	protected Properties vizmapProps;
 
 	protected static String sessionFile;
 
 	protected String[] nodeAttrFiles;
 
 	protected String[] edgeAttrFiles;
 
 	protected String[] expressionFiles;
 
 	protected int mode;
 
 	protected org.apache.commons.cli.Options options;
 
 	protected CyLogger logger = null;
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param args
 	 *            DOCUMENT ME!
 	 * 
 	 * @throws Exception
 	 *             DOCUMENT ME!
 	 */
 	public static void main(String[] args) throws Exception {
 		if (System.getProperty("os.name").startsWith("Mac")) {
 			/*
 			 * By kono 4/2/2007 Fix Application name for Mac.
 			 */
 			final CytoscapeVersion ver = new CytoscapeVersion();
 			final String version = ver.getVersion();
 			System.setProperty(
 					"com.apple.mrj.application.apple.menu.about.name", version);
			System.setProperty("apple.awt.brushMetalRounded", "true");
			System.setProperty("apple.awt.antialiasing", "on");
			System.setProperty("apple.awt.rendering", "VALUE_RENDER_SPEED");
 		}
 
 		CyMain app = new CyMain(args);
 	}
 
 	/**
 	 * Creates a new CyMain object.
 	 * 
 	 * @param args
 	 *            DOCUMENT ME!
 	 * 
 	 * @throws Exception
 	 *             DOCUMENT ME!
 	 */
 	public CyMain(String[] args) throws Exception {
 		props = null;
 		graphFiles = null;
 		plugins = null;
 		vizmapProps = null;
 		sessionFile = null;
 		nodeAttrFiles = null;
 		edgeAttrFiles = null;
 		expressionFiles = null;
 		this.args = args;
 		mode = CyInitParams.ERROR;
 		options = new org.apache.commons.cli.Options();
 		logger = CyLogger.getLogger(CyMain.class);
 
 		// for (String asdf: args)
 		// logger.info("arg: '" + asdf + "'");
 		parseCommandLine(args);
 
 		// Register CyStartupListener to intercept arguments passed by file
 		// associations set by install4j for Mac OS
 		StartupNotification.registerStartupListener(new CyStartupListener());
 
 		CytoscapeInit initializer = new CytoscapeInit();
 
 		if (!initializer.init(this)) {
 			printHelp();
 			Cytoscape.exit(1);
 		}
 	}
 
 	protected void parseCommandLine(String[] args) {
 		// create the options
 		options.addOption("h", "help", false, "Print this message.");
 		options.addOption("v", "version", false, "Print the version number.");
 		// commented out until we actually support doing anything in headless
 		// mode
 		// options.addOption("H", "headless", false, "Run in headless (no gui)
 		// mode.");
 		options.addOption(OptionBuilder.withLongOpt("session").withDescription(
 				"Load a cytoscape session (.cys) file.").withValueSeparator(
 				'\0').withArgName("file").hasArg() // only allow one session!!!
 				.create("s"));
 
 		options.addOption(OptionBuilder.withLongOpt("network").withDescription(
 				"Load a network file (any format).").withValueSeparator('\0')
 				.withArgName("file").hasArgs().create("N"));
 
 		options
 				.addOption(OptionBuilder
 						.withLongOpt("edge-attrs")
 						.withDescription(
 								"Load an edge attributes file (edge attribute format).")
 						.withValueSeparator('\0').withArgName("file").hasArgs()
 						.create("e"));
 		options.addOption(OptionBuilder.withLongOpt("node-attrs")
 				.withDescription(
 						"Load a node attributes file (node attribute format).")
 				.withValueSeparator('\0').withArgName("file").hasArgs().create(
 						"n"));
 		options.addOption(OptionBuilder.withLongOpt("matrix").withDescription(
 				"Load a node attribute matrix file (table).")
 				.withValueSeparator('\0').withArgName("file").hasArgs().create(
 						"m"));
 
 		options
 				.addOption(OptionBuilder
 						.withLongOpt("plugin")
 						.withDescription(
 								"Load a plugin jar file, directory of jar files, plugin class name, or plugin jar URL.")
 						.withValueSeparator('\0').withArgName("file").hasArgs()
 						.create("p"));
 
 		options
 				.addOption(OptionBuilder
 						.withLongOpt("props")
 						.withDescription(
 								"Load cytoscape properties file (Java properties format) or individual property: -P name=value.")
 						.withValueSeparator('\0').withArgName("file").hasArgs()
 						.create("P"));
 		options.addOption(OptionBuilder.withLongOpt("vizmap").withDescription(
 				"Load vizmap properties file (Java properties format).")
 				.withValueSeparator('\0').withArgName("file").hasArgs().create(
 						"V"));
 
 		// try to parse the cmd line
 		CommandLineParser parser = new PosixParser();
 		CommandLine line = null;
 
 		try {
 			line = parser.parse(options, args);
 		} catch (ParseException e) {
 			System.err
 					.println("Parsing command line failed: " + e.getMessage());
 			printHelp();
 			System.exit(1);
 		}
 		
 		// Read any argument containing ".cys" as session file.
 		// Allows session files to be passed in via MIME type settings.
 		// This imprecise method is overwritten by -s option, if specified.
 		for (String freeArg : args) {
 			if (freeArg.contains(".cys")) {
 				sessionFile = freeArg;
 			}
 		}
 		
 		// use what is found on the command line to set values
 		if (line.hasOption("h")) {
 			printHelp();
 			System.exit(0);
 		}
 
 		if (line.hasOption("v")) {
 			CytoscapeVersion version = new CytoscapeVersion();
 			logger.info(version.getVersion());
 			System.exit(0);
 		}
 
 		if (line.hasOption("H")) {
 			mode = CyInitParams.TEXT;
 		} else {
 			mode = CyInitParams.GUI;
 			setupLookAndFeel();
 		}
 
 		if (line.hasOption("P"))
 			props = createProperties(line.getOptionValues("P"));
 		else
 			props = createProperties(new String[0]);
 
 		if (line.hasOption("N"))
 			graphFiles = line.getOptionValues("N");
 
 		if (line.hasOption("p"))
 			plugins = line.getOptionValues("p");
 
 		if (line.hasOption("V"))
 			vizmapProps = createProperties(line.getOptionValues("V"));
 		else
 			vizmapProps = createProperties(new String[0]);
 
 		if (line.hasOption("s"))
 			sessionFile = line.getOptionValue("s");
 
 		if (line.hasOption("n"))
 			nodeAttrFiles = line.getOptionValues("n");
 
 		if (line.hasOption("e"))
 			edgeAttrFiles = line.getOptionValues("e");
 
 		if (line.hasOption("m"))
 			expressionFiles = line.getOptionValues("m");
 	}
 
 	/**
 	 * Provides access to the session file parsed from arguments intercepted by
 	 * CyStartupListener
 	 * 
 	 * @return sessionFile
 	 */
 	public static void setSessionFile(String sf) {
 		sessionFile = sf;
 	}
 
 	protected void setupLookAndFeel() {
 		try {
 			if (LookUtils.IS_OS_WINDOWS) {
 				/*
 				 * For Windows: just use platform default look & feel.
 				 */
 				UIManager
 						.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 			} else if (LookUtils.IS_OS_MAC) {
 				/*
 				 * For Mac: move menue bar to OS X default bar (next to Apple
 				 * icon)
 				 */
				System.setProperty("apple.laf.useScreenMenuBar", "true");
 			} else {
 				/*
 				 * For Unix platforms, use JGoodies Looks
 				 */
 				UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
 				// UIManager.setLookAndFeel(new NimbusLookAndFeel());
 				Plastic3DLookAndFeel.set3DEnabled(true);
 				Plastic3DLookAndFeel
 						.setCurrentTheme(new com.jgoodies.looks.plastic.theme.SkyBluer());
 				Plastic3DLookAndFeel
 						.setTabStyle(Plastic3DLookAndFeel.TAB_STYLE_METAL_VALUE);
 				Plastic3DLookAndFeel.setHighContrastFocusColorsEnabled(true);
 
 				Options.setDefaultIconSize(new Dimension(18, 18));
 				Options.setHiResGrayFilterEnabled(true);
 				Options.setPopupDropShadowEnabled(true);
 				Options.setUseSystemFonts(true);
 
 				UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
 				UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
 			}
 		} catch (Exception e) {
 			logger.warn("Can't set look & feel:" + e.getMessage(), e);
 		}
 	}
 
 	protected void printHelp() {
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp("java -Xmx512M -jar cytoscape.jar [OPTIONS]",
 				options);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public Properties getProps() {
 		return props;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public Properties getVizProps() {
 		return vizmapProps;
 	}
 
 	private Properties createProperties(String[] potentialProps) {
 		// for ( String asdf: potentialProps)
 		// logger.info("prop: '" + asdf + "'");
 		Properties props = new Properties();
 		Properties argProps = new Properties();
 
 		Matcher propPattern = Pattern.compile("^((\\w+\\.*)+)\\=(.+)$")
 				.matcher("");
 
 		for (int i = 0; i < potentialProps.length; i++) {
 			propPattern.reset(potentialProps[i]);
 
 			// check to see if the string is a key value pair
 			if (propPattern.matches()) {
 				argProps
 						.setProperty(propPattern.group(1), propPattern.group(3));
 
 				// otherwise assume it's a file/url
 			} else {
 				try {
 					InputStream in = FileUtil.getInputStream(potentialProps[i]);
 
 					if (in != null)
 						props.load(in);
 					else
 						logger.info("Couldn't load property: "
 								+ potentialProps[i]);
 				} catch (IOException e) {
 					logger.warn("Couldn't load property '"+ potentialProps[i] + "' from file: "+e.getMessage(), e);
 				}
 			}
 		}
 
 		// Transfer argument properties into the full properties.
 		// We do this so that anything specified on the command line
 		// overrides anything specified in a file.
 		props.putAll(argProps);
 
 		return props;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public List getGraphFiles() {
 		return createList(graphFiles);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public List getEdgeAttributeFiles() {
 		return createList(edgeAttrFiles);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public List getNodeAttributeFiles() {
 		return createList(nodeAttrFiles);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public List getExpressionFiles() {
 		return createList(expressionFiles);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public List getPlugins() {
 		return createList(plugins);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public String getSessionFile() {
 		return sessionFile;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public int getMode() {
 		return mode;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public String[] getArgs() {
 		return args;
 	}
 
 	private List createList(String[] vals) {
 		if (vals == null)
 			return new ArrayList();
 
 		ArrayList a = new ArrayList(vals.length);
 
 		for (int i = 0; i < vals.length; i++)
 			a.add(i, vals[i]);
 
 		return a;
 	}
 }
