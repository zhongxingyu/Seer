 /*
  File: CytoscapeInit.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Pasteur Institute
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
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.JarURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.jar.JarFile;
 
 import javax.swing.ImageIcon;
 
 import cytoscape.data.servers.BioDataServer;
 import cytoscape.init.CyPropertiesReader;
 import cytoscape.init.CyInitParams;
 import cytoscape.plugin.CytoscapePlugin;
 import cytoscape.util.shadegrown.WindowUtilities;
 import cytoscape.view.CytoscapeDesktop;
 import cytoscape.data.readers.TextHttpReader;
 import cytoscape.util.FileUtil;
 
 /**
  * Cytoscape Init is responsible for starting Cytoscape in a way that makes
  * sense.
  * 
  * The comments below are more hopeful than accurate. We currently do not
  * support a "headless" mode (meaning there is no GUI). We do, however, hope to
  * support this in the future.
  * 
  * 
  * The two main modes of running Cytoscape are either in "headless" mode or in
  * "script" mode. This class will use the command-line options to figure out
  * which mode is desired, and run things accordingly.
  * 
  * The order for doing things will be the following: 1. deterimine script mode,
  * or headless mode 2. get options from properties files 3. get options from
  * command line ( these overwrite properties ) 4. Load all Networks 5. Load all
  * Data 6. Load all Plugins 7. Initialize all plugins, in order if specified. 8.
  * Start Desktop/ Print Output exit.
  */
 public class CytoscapeInit { //implements PropertyChangeListener {
 	
 	private static Properties properties; 
 	private static Properties visualProperties; 
 	private static Set pluginURLs;
 	private static Set resourcePlugins;
 
 	static { 
 		System.out.println("CytoscapeInit static initialization");
 		pluginURLs = new HashSet();
 		resourcePlugins = new HashSet();
 		properties = new Properties();	
 		loadStaticProperties("cytoscape.props",properties); 
 		visualProperties = new Properties();	
 		loadStaticProperties("vizmap.props",visualProperties); 
 	}
 
 	private static String[] args;
 
 	private static CyInitParams initParams;
 
 	private static URLClassLoader classLoader;
 
 	// Most-Recently-Used directories and files
 	private static File mrud;
 
 	private static File mruf;
 
 	// Configuration variables
 	private static boolean useView = true;
 
 	private static boolean suppressView = false;
 
 	private static int secondaryViewThreshold;
 
 
 	// View Only Variables
 	private static String vizmapPropertiesLocation;
 
 	public CytoscapeInit() {
 	}
 
 	//public void propertyChange(PropertyChangeEvent e) {
 	//}
 
 	/**
 	 * Cytoscape Init must be initialized using the command line arguments.
 	 * 
 	 * @param args the arguments from the command line
 	 * @return false, if we fail to initialize for some reason 
 	 */
 	public boolean init(CyInitParams params) {
 
 		initParams = params;
 
 		loadInputProperties("cytoscape.props", initParams.getProps(), properties);
 		loadInputProperties("vizmap.props", initParams.getVizProps(), visualProperties);
 		setVariablesFromProperties();
 
 		// see if we are in headless mode
 		// show splash screen, if appropriate
                 System.out.println("init mode: " + initParams.getMode() );
                 if ( initParams.getMode() == CyInitParams.GUI ) {
 
 			ImageIcon image = new ImageIcon(this.getClass().getResource(
 					"/cytoscape/images/CytoscapeSplashScreen.png"));
 			WindowUtilities.showSplash(image, 8000);
 			Cytoscape.getDesktop();
 			// This cannot be done in CytoscapeDesktop construction (like the other menus)
 			// because we need CytoscapeDesktop created first. This is because CytoPanel
 			// menu item listeners need to register for CytoPanel events via a CytoPanel 
 			// reference, and the only way to get a CytoPanel reference is via 
 			// CytoscapeDeskop: Cytoscape.getDesktop().getCytoPanel(...)
 			Cytoscape.getDesktop().getCyMenus().initCytoPanelMenus();
 
 			// Add a listener that will apply vizmaps every time attributes change
 			PropertyChangeListener attsChangeListener = new PropertyChangeListener() {
 
 				public void propertyChange(PropertyChangeEvent e) {
 					if (e.getPropertyName().equals(Cytoscape.ATTRIBUTES_CHANGED)) {
 						// apply vizmaps
 						Cytoscape.getCurrentNetworkView().redrawGraph(false, true); 
 					}
 				}
 			};
 
 			Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(attsChangeListener);
 		}
 
 		// now that we are properly set up,
 		// load all data, then load all plugins
 
 		// Load the BioDataServer(s)
 		BioDataServer bds = Cytoscape.loadBioDataServer(properties.getProperty("bioDataServer"));
 
 		// Load all requested networks
		boolean canonicalize = Boolean.getBoolean(properties.getProperty("canonicalizeNames"));
 		for (Iterator i = initParams.getGraphFiles().iterator(); i.hasNext();) {
 			String net = (String) i.next();
 			System.out.println("Load: " + net);
 
 			CyNetwork network = Cytoscape.createNetwork(net,
 					Cytoscape.FILE_BY_SUFFIX, canonicalize, bds,
 					properties.getProperty("defaultSpeciesName"));
 		}
 
 		// load any specified data attribute files
 		Cytoscape.loadAttributes(
 				(String[])initParams.getNodeAttributeFiles().toArray(new String[] {}),
 				(String[])initParams.getEdgeAttributeFiles().toArray(new String[] {}),
 				canonicalize, 
 				bds,
 				properties.getProperty("defaultSpeciesName"));
 
 
 		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 
 		loadExpressionFiles();
 
 		loadPlugins();
 
 		if ( initParams.getMode() == CyInitParams.GUI ) {
 			WindowUtilities.hideSplash();
 		}
 
 		// This is for browser and other plugins.
 		Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, null);
 
 		System.out.println("Cytoscape initialized successfully.");
 		return true;
 	}
 
 	public static Properties getProperties() {
 		return properties;
 	}
 
 	public static void setProperty(String key, String value) {
 		properties.setProperty(key,value);
 	}
 
 	public static String getProperty(String key) {
 		return properties.getProperty(key);
 	}
 
 	public static URLClassLoader getClassLoader() {
 		return classLoader;
 	}
 
 	public static Set getPluginURLs() {
 		return pluginURLs;
 	}
 
 	public static Set getResourcePlugins() {
 		return resourcePlugins;
 	}
 
 
 	/**
 	 * @deprecated This method will be removed April 2007. 
 	 * No one appears to use this method, so don't start.
 	 */
 	public String getHelp() {
 		return "Help! - you shouldn't be using this method";
 	}
 
 	/**
 	 * @deprecated This method will be removed April 2007. 
 	 * Use getMode() instead. 
 	 */
 	public static boolean isHeadless() {
 		return !useView;
 	}
 
 	/**
 	 * @deprecated This method will be removed April 2007. 
 	 * Use getMode() instead. 
 	 */
 	public static boolean useView() {
 		return useView;
 	}
 
 	/**
 	 * @deprecated This method will be removed April 2007. 
 	 * No one appears to use this method, so don't start.
 	 */
 	public static boolean suppressView() {
 		return suppressView;
 	}
 
 	/**
 	 * @deprecated Use Properties (getProperties()) instead of args for 
 	 * accessing initialization information.  
 	 * This method will be removed April 2007.
 	 */
 	public static String[] getArgs() {
 		return initParams.getArgs();
 	}
 
 	/**
 	 * @deprecated  This method will be removed April 2007. 
 	 */
 	public static String getPropertiesLocation() {
 		return "";
 	}
 
 	/**
 	 * @deprecated This method will be removed April 2007.
 	 * Use getProperty("bioDataServer") instead.
 	 */
 	public static String getBioDataServer() {
 		return properties.getProperty("bioDataServer");
 	}
 
 	/**
 	 * @deprecated Will be removed April 2007. Use getProperty( "canonicalizeNames" ) instead.
 	 */
 	public static boolean noCanonicalization() {
		return !Boolean.getBoolean(getProperty( "canonicalizeNames" ));
 	}
 
 	/**
 	 * @deprecated Will be removed April 2007.
 	 * No one appears to be using this method, so don't start.
 	 */
 	public static Set getExpressionFiles() {
 		return new HashSet(initParams.getExpressionFiles());
 	}
 
 	/**
 	 * @deprecated Will be removed April 2007.
 	 * No one appears to be using this method, so don't start.
 	 */
 	public static Set getGraphFiles() {
 		return new HashSet(initParams.getGraphFiles());
 	}
 
 	/**
 	 * @deprecated Will be removed April 2007.
 	 * No one appears to be using this method, so don't start.
 	 */
 	public static Set getEdgeAttributes() {
 		return new HashSet(initParams.getEdgeAttributeFiles());
 	}
 
 	/**
 	 * @deprecated Will be removed April 2007.
 	 * No one appears to be using this method, so don't start.
 	 */
 	public static Set getNodeAttributes() {
 		return new HashSet(initParams.getNodeAttributeFiles());
 	}
 
 	/**
 	 * @deprecated Will be removed April 2007. Use getProperty( "defaultSpeciesName" ) instead.
 	 */
 	public static String getDefaultSpeciesName() {
 		return properties.getProperty("defaultSpeciesName", "unknown");
 	}
 
 
 	/**
 	 * @deprecated Will be removed April 2007.  
 	 * Use CytoscapeDesktop.parseViewType(CytoscapeInit.getProperty("viewType"));
 	 */
 	public static int getViewType() {
 		return CytoscapeDesktop.parseViewType(CytoscapeInit.getProperty("viewType"));
 	}
 
 	/**
 	 * Gets the ViewThreshold. Networks with number of nodes below this
 	 * threshold will automatically have network views created.
 	 * 
 	 * @return view threshold.
 	 * @deprecated Will be removed April 2007. Use getProperty( "viewThreshold" ) instead.
 	 */
 	public static int getViewThreshold() {
 		return Integer.parseInt(properties.getProperty("viewThreshold") );
 	}
 
 	/**
 	 * Sets the ViewThreshold. Networks with number of nodes below this
 	 * threshold will automatically have network views created.
 	 * 
 	 * @param threshold
 	 *            view threshold.
 	 * @deprecated Will be removed April 2007.   Use setProperty( "viewThreshold", thresh ) instead.
 	 */
 	public static void setViewThreshold(int threshold) {
 		properties.setProperty("viewThreshold", Integer.toString(threshold) );
 	}
 
 	/**
 	 * Gets the Secondary View Threshold. This value is a secondary check on
 	 * rendering very large networks. It is primarily checked when a user wishes
 	 * to create a view for a large network.
 	 * 
 	 * @return threshold value, indicating number of nodes.
 	 * @deprecated Will be removed April 2007. Use getProperty( "secondaryViewThreshold" ) instead.
 	 */
 	public static int getSecondaryViewThreshold() {
 		return secondaryViewThreshold;
 	}
 
 	/**
 	 * Sets the Secondary View Threshold. This value is a secondary check on
 	 * rendering very large networks. It is primarily checked when a user wishes
 	 * to create a view for a large network.
 	 * 
 	 * @param threshold
 	 *            value, indicating number of nodes.
 	 * @deprecated Will be removed April 2007. 
 	 * Use setProperty( "secondaryViewThreshold", thresh ) instead.
 	 */
 	public static void setSecondaryViewThreshold(int threshold) {
 		secondaryViewThreshold = threshold;
 	}
 
 	// View Only Variables
 	/**
 	 * @deprecated Will be removed April 2007. Use getProperty( "TODO" ) instead.
 	 */
 	public static String getVizmapPropertiesLocation() {
 		return vizmapPropertiesLocation;
 	}
 
 	/**
 	 * @deprecated Will be removed April 2007. Use getProperty( "defaultVisualStyle" ) instead.
 	 */
 	public static String getDefaultVisualStyle() {
 		return properties.getProperty("defaultVisualStyle");
 	}
 
 	/**
 	 * Use the Properties Object that was retrieved from the CyPropertiesReader
 	 * to set all known global variables
 	 */
 	private static void setVariablesFromProperties() {
 
 		// plugins
 		if (properties.getProperty("plugins") != null) {
 			String[] pargs = properties.getProperty("plugins").split(",");
 			for (int i = 0; i < pargs.length; i++) {
 				String plugin = pargs[i];
 				URL url;
 				try {
 					if (plugin.matches(FileUtil.urlPattern())) {
 						url = jarURL(plugin);
 					} else {
 						File pf = new File(plugin);
 						url = jarURL(pf.getAbsolutePath());
 					}
 					pluginURLs.add(url);
 				} catch (Exception mue) {
 					mue.printStackTrace();
 					System.err.println("property plugin: " + plugin + " NOT added");
 				}
 			}
 		}
 
 		mrud = new File(properties.getProperty("mrud", System.getProperty("user.dir")));
 	}
 
 	/**
 	 * Parses the plugin input strings and transforms them into the appropriate
 	 * URLs or resource names.  The method first checks to see if the 
 	 */
 	private void loadPlugins() {
 
 		Set plugins = new HashSet();
 		List p = initParams.getPlugins();
 		if ( p != null )
 			plugins.addAll(p);	
 
 		// Parse the plugin strings and determine whether they're urls, files,
 		// directories, class names, or manifest file names.
 		for (Iterator iter = plugins.iterator(); iter.hasNext();) {
 			String plugin = (String)iter.next();
 			System.out.println("preparing to load plugin: " + plugin);
 			
 			File f = new File(plugin);
 
 			// If the file name ends with .jar add it to the list as a url.
 			if ( plugin.endsWith(".jar") ) {
 
 				// If the name doesn't match a url, turn it into one. 
 				if ( !plugin.matches( FileUtil.urlPattern() ) ) {
 					pluginURLs.add( jarURL(f.getAbsolutePath()) );
 				} else { 
 					pluginURLs.add( jarURL(plugin) );
 				}
 
 			// If the file doesn't exists, assume that it's a 
 			// resource plugin.
 			} else if ( !f.exists() ) {
 				resourcePlugins.add( plugin );
 
 			// If the file is a directory, load all of the jars
 			// in the directory.
 			} else if ( f.isDirectory() ) {
 
 				String[] fileList = f.list();
 				
 				for(int j = 0; j < fileList.length; j++) {
 					if( !fileList[j].endsWith(".jar") ) 
 						continue;
 					File jarFile = new File(f.getName() + System.getProperty("file.separator") + fileList[j]);
 					if ( jarFile != null ) 
 						pluginURLs.add( jarURL(jarFile.getAbsolutePath()) );
 					else
 						System.out.println("plugin: " + plugin + " NOT added");
 				}
 			
 			// Assume the file is a manifest (i.e. list of jar names)
 			// and make urls out of them.
 			} else {
 
 				try {
 				TextHttpReader reader = new TextHttpReader(plugin);
 				reader.read();
 				String text = reader.getText();
 				String lineSep = System.getProperty("line.separator");
 				String[] allLines = text.split(lineSep);
 				for (int j=0; j < allLines.length; j++) {
 					String pluginLoc = allLines[j];
 					if ( pluginLoc.endsWith( ".jar" ) ) {
 						if ( pluginLoc.matches( FileUtil.urlPattern() ) ) 
 							pluginURLs.add( pluginLoc );
 						else
 							System.err.println( "Plugin location specified in " + plugin + " is not a valid url: " + pluginLoc  + " -- NOT adding it.");
 							
 					}
 				}
 				} catch ( Exception exp ) {
 					exp.printStackTrace();
 					System.err.println( "error reading plugin manifest file "+plugin );
 				}
 			}
 		}
 		
 		// now load the plugins in the appropriate manner
 		loadURLPlugins(pluginURLs);
 		loadResourcePlugins(resourcePlugins);
 
 	}
 
 	/**
 	 * Load all plugins by using the given URLs loading them all on one
 	 * URLClassLoader, then interating through each Jar file looking for classes
 	 * that are CytoscapePlugins
 	 */
 	private void loadURLPlugins(Set plugin_urls) {
 
 		URL[] urls = new URL[plugin_urls.size()];
 		int count = 0;
 		for (Iterator iter = plugin_urls.iterator(); iter.hasNext();) {
 			urls[count] = (URL) iter.next();
 			count++;
 		}
 
 		// the creation of the class loader automatically loads the plugins
 		classLoader = new URLClassLoader(urls, Cytoscape.class.getClassLoader());
 
 		String fileSep = System.getProperty("file.separator");
 
 		// iterate through the given jar files and find classes that are
 		// assignable
 		// from CytoscapePlugin
 		for (int i = 0; i < urls.length; ++i) {
 
 			try {
 				JarURLConnection jc = (JarURLConnection) urls[i].openConnection();
 				JarFile jar = jc.getJarFile();
 
 				// if the jar file is null, do nothing
 				if (jar == null) {
 					continue;
 				}
 
 				// System.out.println("- - - - entries begin");
 				Enumeration entries = jar.entries();
 				if (entries == null) {
 					continue;
 				}
 
 				// System.out.println("entries is not null");
 
 				int totalEntries = 0;
 				int totalClasses = 0;
 				int totalPlugins = 0;
 
 				while (entries.hasMoreElements()) {
 					totalEntries++;
 
 					// get the entry
 					String entry = entries.nextElement().toString();
 
 					// System.out.println( "Entry: "+entry+ " is "+resource );
 
 					if (entry.endsWith("class")) {
 						// convert the entry to an assignable class name
 						entry = entry.replaceAll("\\.class$", "");
 						entry = entry.replaceAll(fileSep, ".");
 
 						// System.out.println(" CLASS: " + entry);
 						if (!(isClassPlugin(entry))) {
 							// System.out.println(" not plugin.");
 							continue;
 						}
 						// System.out.println(entry+" is a PLUGIN!");
 						totalPlugins++;
 						loadPlugin(classLoader.loadClass(entry));
 					}
 				}
 				// System.out.println("- - - - entries finis");
 				// System.out.println(".jar summary: " +
 				// " entries=" + totalEntries +
 				// " classes=" + totalClasses +
 				// " plugins=" + totalPlugins);
 			} catch (Exception e) {
 				System.err.println("Error thrown: " + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void loadResourcePlugins(Set rp) {
 		// attempt to load resource plugins
 		for (Iterator rpi = rp.iterator(); rpi.hasNext();) {
 			String resource = (String) rpi.next();
 			// try to get the class
 			Class rclass = null;
 			try {
 				rclass = Class.forName(resource);
 			} catch (Exception exc) {
 				System.out.println("Getting class: " + resource + " failed");
 				exc.printStackTrace();
 				return;
 			}
 			loadPlugin(rclass);
 		}
 	}
 
 
 	public void loadPlugin(Class plugin) {
 
 		if (CytoscapePlugin.class.isAssignableFrom(plugin)) {
 			System.out.println("CytoscapePlugin Loaded");
 			try {
 				CytoscapePlugin.loadPlugin(plugin);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Determines whether the class with a particular name extends
 	 * CytoscapePlugin.
 	 * 
 	 * @param name
 	 *            the name of the putative plugin class
 	 */
 	protected boolean isClassPlugin(String name) {
 		Class c = null;
 		try {
 			c = classLoader.loadClass(name);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} catch (NoClassDefFoundError e) {
 			e.printStackTrace();
 			return false;
 		}
 		Class cp = CytoscapePlugin.class;
 		return (cp.isAssignableFrom(c));
 	}
 
 	/**
 	 * @return the most recently used directory
 	 */
 	public static File getMRUD() {
 		return mrud;
 	}
 
 	/**
 	 * @return the most recently used file
 	 */
 	public static File getMRUF() {
 		return mruf;
 	}
 
 	/**
 	 * @param mrud the most recently used directory
 	 */
 	public static void setMRUD(File mrud_new) {
 		mrud = mrud_new;
 	}
 
 	/**
 	 * @param mruf the most recently used file
 	 */
 	public static void setMRUF(File mruf_new) {
 		mruf = mruf_new;
 	}
 
 	/**
 	 * @deprecated Will be removed April 2007. This doesn't do anything. 
 	 * To set the default species name use setProperty("defaultSpeciesName", newName),
 	 * which you were presumably doing already.
 	 */
 	public static void setDefaultSpeciesName() {
 		// Update defaultSpeciesName using current properties.
 		// This is necessary to reflect changes in the Preference Editor
 		// immediately
 	}
 
 	/**
 	 * If .cytoscape directory does not exist, it creates it and returns it
 	 * 
 	 * @return the directory ".cytoscape" in the users home directory.
 	 */
 	public static File getConfigDirectoy() {
 		File dir = null;
 		try {
 			File parent_dir = new File(System.getProperty("user.home"),
 					".cytoscape");
 			if (parent_dir.mkdir())
 				System.err.println("Parent_Dir: " + parent_dir + " created.");
 
 			return parent_dir;
 		} catch (Exception e) {
 			System.err.println("error getting config directory");
 		}
 		return null;
 	}
 
 	public static File getConfigFile(String file_name) {
 		try {
 			File parent_dir = getConfigDirectoy();
 			File file = new File(parent_dir, file_name);
 			if (file.createNewFile())
 				System.err.println("Config file: " + file + " created.");
 			return file;
 
 		} catch (Exception e) {
 			System.err.println("error getting config file:" + file_name);
 		}
 		return null;
 	}
 
 	public static Properties getVisualProperties() {
 		return visualProperties;
 	}
 
 	private static void loadStaticProperties(String defaultName, Properties props ) {
 		String tryName = "";
                 try {
                         // load the props from the jar file
                         tryName = "cytoscape.jar";
                         URL vmu = ClassLoader.getSystemClassLoader().getSystemResource(defaultName);
                         if ( vmu != null )
                                 props.load(vmu.openStream());
 
                         // load the props file from $HOME/.cytoscape 
                         tryName = "$HOME/.cytoscape";
                         File vmp = CytoscapeInit.getConfigFile(defaultName);
                         if (vmp != null)
                                 props.load(new FileInputStream(vmp));
 
                 } catch (IOException ioe) {
                         System.err.println("couldn't open " + tryName
                                         + " " + defaultName + 
                                         " file - creating a hardcoded default");
                         ioe.printStackTrace();
                 }
 
 	}
 
 	private void loadInputProperties(String defaultName, Properties initProps, Properties props ) {
 		if ( props == null )
 			loadStaticProperties(defaultName,props);
 
 		// transfer the properties found on the command line 
 		if ( initProps != null ) {
 			Enumeration names = initProps.propertyNames();
 			while (names.hasMoreElements()) {	
 				String name = (String)names.nextElement();
 				props.setProperty( name, initProps.getProperty(name) );
 			}
 		}
 	}
 
 	private static void loadExpressionFiles() {
 		// load expression data if specified
 		List ef = initParams.getExpressionFiles();
 		if (ef != null && ef.size() > 0) {
 			for (Iterator iter = ef.iterator(); iter.hasNext();) {
 				String expDataFilename = (String) iter.next();
 				if (expDataFilename != null) {
 					try {
 						Cytoscape.loadExpressionData(expDataFilename, true);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	private static URL jarURL(String urlString) {
 		URL url = null;
 		try {
 			String uString;
 			if ( urlString.matches( FileUtil.urlPattern() ) )
 				uString = "jar:" + urlString + "!/";	
 			else 
 				uString = "jar:file:" + urlString + "!/";	
 			url = new URL(uString);
 		} catch (MalformedURLException mue) {
 			mue.printStackTrace();	
 			System.out.println("couldn't create jar url from '" + urlString + "'");
 		}
 		return url;
 	}
 }
