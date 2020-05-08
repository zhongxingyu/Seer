 package uk.ac.warwick.dcs.boss.plugins;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.Lookups;
 
 import uk.ac.warwick.dcs.boss.frontend.PageDispatcherServlet;
 import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
 import uk.ac.warwick.dcs.boss.model.FactoryException;
 import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
 import uk.ac.warwick.dcs.boss.model.dao.DAOException;
 import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
 import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
 import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;
 import uk.ac.warwick.dcs.boss.plugins.spi.config.PluginConfiguration;
 
 class PluginManager {
 	static final Attributes.Name PLUGIN_ID = new Attributes.Name(
 			"BOSS-Plugin-Id");
 	static final Attributes.Name PLUGIN_NAME = new Attributes.Name(
 			"BOSS-Plugin-Name");
 	static final Attributes.Name PLUGIN_AUTHOR = new Attributes.Name(
 			"BOSS-Plugin-Author");
 	static final Attributes.Name PLUGIN_EMAIL = new Attributes.Name(
 			"BOSS-Plugin-Email");
 	static final Attributes.Name PLUGIN_VERSION = new Attributes.Name(
 			"BOSS-Plugin-Version");
 	static final Attributes.Name PLUGIN_DESCRIPTION = new Attributes.Name(
 			"BOSS-Plugin-Description");
 //	static final Attributes.Name PLUGIN_CONFIGURATION = new Attributes.Name(
 //			"BOSS-Plugin-Configuration");
 	static Logger logger = Logger.getLogger("plugin manager");
 
 	static PluginMetadata installPlugin(File pluginFile)
 			throws IOException, InvalidPluginException, DAOException {
 		JarFile jarFile = null;
 		Attributes atts = null;
 		try {
 			// we have the plugin in a jar file
 			jarFile = new JarFile(pluginFile);
 			atts = jarFile.getManifest().getMainAttributes();
 		} catch (IOException e) {
 			throw new InvalidPluginException(
 					"Supplied file is not a valid BOSS plugin");
 		}
 
 		// manifest file is required to supplied at least plugin's id,
 		// name, and version
 		if (!atts.containsKey(PLUGIN_ID) || !atts.containsKey(PLUGIN_NAME)
 				|| !atts.containsKey(PLUGIN_VERSION)) {
 			throw new InvalidPluginException(
 					"Supplied file is not a valid BOSS plugin");
 		}
 
 		// we have a valid plugin file (as far as MANIFEST file goes)
 		String pluginId = atts.getValue(PluginManager.PLUGIN_ID);
 		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
 		File pluginFolder = new File(webInfDir, "plugins");
 
 		// archiving the plugin file under WEB-INF/plugins/
 		// since the pluginId is unique, there shouldn't be a collision
 		// regarding name
 		File pluginJarFile = new File(pluginFolder, pluginId + ".jar");
 		try {
 			logger.log(Level.INFO, "Storing " + pluginId
 					+ " plugin into WEB-INF/plugins folder");
 			FileUtils.copyFile(pluginFile, pluginJarFile);
 		} catch (IOException e) {
 			if (pluginJarFile.exists()) {
 				logger.log(Level.INFO, "Deleting " + pluginId
 						+ " plugin file in WEB-INF/plugins folder");
 				pluginJarFile.delete();
 			}
 
 			// rethrow exception
 			throw e;
 		}
 
 		// get lib filenames if present
 		List<String> libFileNames = new LinkedList<String>();
 		Enumeration<JarEntry> enumeration = jarFile.entries();
 		while (enumeration.hasMoreElements()) {
 			JarEntry entry = enumeration.nextElement();
 			String entryName = entry.getName();
 			if (entryName.startsWith("lib/") && entryName.endsWith(".jar")) {
 				String[] entryPathComps = entryName.split("/");
 				String libFileName = entryPathComps[entryPathComps.length - 1];
 				libFileNames.add(libFileName);
 			}
 		}
 
 		// create an entity
 		PluginMetadata pluginMetadata = new PluginMetadata();
 		pluginMetadata.setPluginId(atts.getValue(PluginManager.PLUGIN_ID));
 		pluginMetadata.setName(atts.getValue(PluginManager.PLUGIN_NAME));
 		pluginMetadata.setVersion(atts.getValue(PluginManager.PLUGIN_VERSION));
 		if (atts.containsKey(PluginManager.PLUGIN_AUTHOR))
 			pluginMetadata
 					.setAuthor(atts.getValue(PluginManager.PLUGIN_AUTHOR));
 		if (atts.containsKey(PluginManager.PLUGIN_EMAIL))
 			pluginMetadata.setEmail(atts.getValue(PluginManager.PLUGIN_EMAIL));
 		if (atts.containsKey(PluginManager.PLUGIN_DESCRIPTION))
 			pluginMetadata.setDescription(atts
 					.getValue(PluginManager.PLUGIN_DESCRIPTION));
 		if (!libFileNames.isEmpty()) {
 			pluginMetadata.setLibFilenames(libFileNames.toArray(new String[0]));
 		}
 
 		try {
 			IDAOSession f = null;
 			try {
 				DAOFactory df = (DAOFactory) FactoryRegistrar
 						.getFactory(DAOFactory.class);
 				f = df.getInstance();
 			} catch (FactoryException e) {
 				e.printStackTrace();
 			}
 			try {
 				f.beginTransaction();
 				f.getPluginMetadataDAOInstance().createPluginCustomTables(
 						pluginId);
 				f.endTransaction();
 			} catch (DAOException e) {
 				f.abortTransaction();
 				throw e;
 			}
 
 			// enable the plugin
 			PluginManager.enablePlugin(pluginMetadata);
 
 			// handling plugin configuration if present
 			pluginMetadata.setConfigurable(PluginManager.initPluginConfig(pluginId));
 /*			if (atts.containsKey(PLUGIN_CONFIGURATION)) {
 				ZipEntry entry = jarFile.getEntry(atts
 						.getValue(PLUGIN_CONFIGURATION));
 				if (entry != null) {
 					InputStream in = null;
 					OutputStream out = null;
 					logger.log(Level.INFO, "Extracting configuration file of "
 							+ pluginId + " plugin into WEB-INF/plugins folder");
 					File destConfFile = new File(pluginFolder, pluginId
 							+ ".properties");
 					try {
 						in = new BufferedInputStream(
 								jarFile.getInputStream(entry));
 
 						out = new BufferedOutputStream(new FileOutputStream(
 								destConfFile));
 						int c;
 						while ((c = in.read()) != -1) {
 							out.write(c);
 						}
 						out.flush();
 					} catch (IOException e) {
 						if (destConfFile.exists())
 							destConfFile.delete();
 					} finally {
 						if (in != null)
 							in.close();
 						if (out != null)
 							out.close();
 					}
 					pluginMetadata.setConfigurable(true);
 				} else
 					throw new InvalidPluginException(
 							"Supplied plugin file doesn't contain initial configuration file "
 									+ atts.getValue(PLUGIN_CONFIGURATION)
 									+ " as advertised");
 			} else {
 				pluginMetadata.setConfigurable(false);
 			}
 */
 		} catch (IOException e) {
 			// installation is unsuccessful, delete plugin file in
 			// WEB-INF/plugins
 			if (pluginJarFile.exists()) {
 				logger.log(Level.INFO, "Deleting " + pluginId
 						+ " plugin file in WEB-INF/plugins folder");
 				pluginJarFile.delete();
 			}
 
 			// rethrow exception
 			throw e;
 		}
 /* 		catch (InvalidPluginException e) {
 			// installation is unsuccessful, delete plugin file in
 			// WEB-INF/plugins
 			if (pluginJarFile.exists()) {
 				logger.log(Level.INFO, "Deleting " + pluginId
 						+ " plugin file in WEB-INF/plugins folder");
 				pluginJarFile.delete();
 			}
 
 			// rethrow exception
 			throw e;
 		}
 */
 		return pluginMetadata;
 	}
 
 
 	static void uninstallPlugin(PluginMetadata pluginInfo)
 			throws IOException, DAOException, InvalidPluginException {
 		// delete plugin's database tables
 		// if this plugin didn't introduce any table, this piece of code simply
 		// does nothing
 		String pluginId = pluginInfo.getPluginId();
 		IDAOSession f = null;
 		try {
 			DAOFactory df = (DAOFactory) FactoryRegistrar
 					.getFactory(DAOFactory.class);
 			f = df.getInstance();
 		} catch (FactoryException e) {
 			e.printStackTrace();
 		}
 		try {
 			f.beginTransaction();
 			f.getPluginMetadataDAOInstance()
 					.destroyPluginCustomTables(pluginId);
 			f.endTransaction();
 		} catch (DAOException e) {
 			f.abortTransaction();
 			throw e;
 		}
 
 		// if this plugin is currently enable, disable it first
 		if (pluginInfo.getEnable())
 			PluginManager.disablePlugin(pluginInfo);
 
 		// delete plugin storage
 		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
 		File pluginStorageDir = new File(webInfDir, "plugins");
 		File thisPluginJar = new File(pluginStorageDir,
 				pluginInfo.getPluginId() + ".jar");
 		logger.log(Level.INFO, "Deleting " + pluginInfo.getPluginId()
 				+ " plugin file in WEB-INF/plugins folder");
 		thisPluginJar.delete();
 
 		if (pluginInfo.getConfigurable()) {
 			File thisPluginConf = new File(pluginStorageDir,
 					pluginInfo.getPluginId() + ".properties");
 			if (thisPluginConf.exists())
 				thisPluginConf.delete();
 		}
 	}
 
 	static void enablePlugin(PluginMetadata pluginInfo)
 			throws IOException {
 		String pluginId = pluginInfo.getPluginId();
 		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
 		// make the plugin active by copy the jar file into WEB-INF/lib
 		File pluginFile = new File(webInfDir, "plugins" + File.separator
 				+ pluginId + ".jar");
 		File webAppLibFolder = new File(webInfDir, "lib");
 		File pluginJarFile = new File(webAppLibFolder, "plugin_" + pluginId
 				+ ".jar");
 		try {
 			logger.log(Level.INFO, "Copying main jar file of " + pluginId
 					+ " plugin into WEB-INF/lib folder");
 			FileUtils.copyFile(pluginFile, pluginJarFile);
 		} catch (IOException e) {
 			if (pluginJarFile.exists()) {
 				logger.log(Level.INFO, "Deleting " + pluginId
 						+ " plugin file in WEB-INF/lib folder");
 				pluginJarFile.delete();
 			}
 			// rethrow exception
 			throw e;
 		}
 
 		// copy the dependencies of the plugin (residing under lib folder of
 		// the plugin's jar file) if exists.
 		JarFile jarFile = new JarFile(pluginFile);
 		String[] libFNStrs = pluginInfo.getLibFilenames();
 		if (libFNStrs != null) {
 			List<String> libFileNames = Arrays.asList(libFNStrs);
 			try {
 				Enumeration<JarEntry> enumeration = jarFile.entries();
 				while (enumeration.hasMoreElements()) {
 					JarEntry entry = enumeration.nextElement();
 					String entryName = entry.getName();
 					if (entryName.startsWith("lib/")
 							&& entryName.endsWith(".jar")) {
 						String[] entryPathComps = entryName.split("/");
 						String libFileName = entryPathComps[entryPathComps.length - 1];
 						if (libFileNames.contains(libFileName)) {
 							File destLibFile = new File(webAppLibFolder,
 									"plugin_" + pluginId + "_" + libFileName);
 							InputStream in = null;
 							OutputStream out = null;
 							logger.log(Level.INFO, "Extracting lib jar file "
 									+ libFileName + " of " + pluginId
 									+ " plugin into WEB-INF/lib folder");
 							try {
 								in = new BufferedInputStream(
 										jarFile.getInputStream(entry));
 								out = new BufferedOutputStream(
 										new FileOutputStream(destLibFile));
 								int c;
 								while ((c = in.read()) != -1) {
 									out.write(c);
 								}
 								out.flush();
 							} finally {
 								if (in != null)
 									in.close();
 								if (out != null)
 									out.close();
 							}
 						}
 					}
 				}
 			} catch (IOException e) {
 				// clean up any lib file which has been activated (copied to
 				// WEB-INF/lib)
 				for (String filename : libFileNames) {
 					File destLibFile = new File(webAppLibFolder, "plugin_"
 							+ pluginId + "_" + filename);
 					if (destLibFile.exists()) {
 						logger.log(Level.INFO, "Deleting lib jar file "
 								+ filename + " of " + pluginId
 								+ " plugin in WEB-INF/lib folder");
 						destLibFile.delete();
 					}
 				}
 
 				// rethrow exception
 				throw e;
 			}
 		}
 		pluginInfo.setEnable(true);
 	}
 
 	static void disablePlugin(PluginMetadata pluginInfo) {
 
 		// delete main jar file in webapp's lib folder
 		File mainJarFile = new File(PageDispatcherServlet.realPath, "WEB-INF"
 				+ File.separator + "lib" + File.separator + "plugin_"
 				+ pluginInfo.getPluginId() + ".jar");
 		logger.log(Level.INFO, "Deleting " + pluginInfo.getPluginId()
 				+ " plugin file in WEB-INF/lib folder");
 		mainJarFile.delete();
 
 		// delete lib jar file also in webapp's lib folder
 		String[] libFNs = pluginInfo.getLibFilenames();
 		if (libFNs != null) {
 			File webInfLibDir = new File(PageDispatcherServlet.realPath,
 					"WEB-INF" + File.separator + "lib");
 			for (int i = 0; i < libFNs.length; i++) {
 				File libFile = new File(webInfLibDir, "plugin_"
 						+ pluginInfo.getPluginId() + "_" + libFNs[i]);
 				logger.log(Level.INFO, "Deleting lib jar file " + libFNs[i]
 						+ " of " + pluginInfo.getPluginId()
 						+ " plugin in WEB-INF/lib folder");
 				libFile.delete();
 			}
 		}
 		pluginInfo.setEnable(false);
 	}
 
 	static Properties getConfiguration(String pluginId)
 			throws IOException, PluginNotConfigurableException {
 		Properties prop = new Properties();
 		File propFile = new File(PageDispatcherServlet.realPath, "WEB-INF"
 				+ File.separator + "plugins" + File.separator + pluginId
 				+ ".properties");
 		if (propFile.exists()) {
 			FileInputStream in = null;
 			try {
 				in = new FileInputStream(propFile);
 				prop.load(in);
 			} finally {
 				if (in != null)
 					in.close();
 			}
 		} else
 			throw new PluginNotConfigurableException("plugin " + pluginId
 					+ " is not configurable");
 		return prop;
 	}
 
 	static void setConfiguration(String pluginId, Properties prop)
 			throws IOException, PluginNotConfigurableException {
 		File propFile = new File(PageDispatcherServlet.realPath, "WEB-INF"
 				+ File.separator + "plugins" + File.separator + pluginId
 				+ ".properties");
 		if (propFile.exists()) {
 			FileOutputStream out = null;
 			try {
 				out = new FileOutputStream(propFile);
 				prop.store(out, null);
 			} finally {
 				if (out != null)
 					out.close();
 			}
 		} else
 			throw new PluginNotConfigurableException("plugin " + pluginId
 					+ " is not configurable");
 	}
 	
 	private static boolean initPluginConfig(String pluginId) throws IOException {
 		boolean retval = false;
 		File pluginFolder = new File(PageDispatcherServlet.realPath, "WEB-INF"
 				+ File.separator + "plugins"); 
 		File pluginFile = new File(pluginFolder, pluginId + ".jar");
 		URL url = null;
 		try {
 			url = pluginFile.toURI().toURL();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		URL[] urls = {url};
 		URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
 		Lookup lookup = Lookups.metaInfServices(classLoader);
 		Collection<? extends PluginConfiguration> configs = lookup.lookupAll(PluginConfiguration.class);
 		for (PluginConfiguration config : configs) {
 			// make sure it's not the one that already installed. If this is something which was 
 			// installed, the ClassLoader of it will not be the same as the one we have above
 			if (config.getClass().getClassLoader() == classLoader) {
 				Collection<ConfigurationOption> props = config.getConfigurationOptions();
 				Properties properties = new Properties();
 				for (ConfigurationOption prop : props) {
 					properties.setProperty(prop.getName(), prop.getDefaultValue());
 				}
 				
 				// write the properties file
 				FileOutputStream writer = null;
 				try {
 					File propertiesFile = new File(pluginFolder, pluginId + ".properties");
 					writer = new FileOutputStream(propertiesFile);
 					properties.store(writer, "Automatically generated by BOSS2");
 				} finally {
 					if (writer != null)
 						writer.close();
 				}
 				retval = true;
 			}
 		}
 		return retval;
 	}
 	
 	static Collection<ConfigurationOption> getPluginConfigOption(String pluginId) {
 		Collection<? extends PluginConfiguration> configs = Lookup.getDefault().lookupAll(PluginConfiguration.class);
 		for (PluginConfiguration config : configs) {
 			// make sure we get the right one
 			// the line below will get the full path of the jar file of which config is loaded from
 			// the correct one would be having the name plugin_<pluginId>.jar
 			String pluginFilePath = config.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
 			if (pluginFilePath.endsWith(pluginId + ".jar")) {
 				return config.getConfigurationOptions();
 			}
 		}
 		return null;
 	}
 }
