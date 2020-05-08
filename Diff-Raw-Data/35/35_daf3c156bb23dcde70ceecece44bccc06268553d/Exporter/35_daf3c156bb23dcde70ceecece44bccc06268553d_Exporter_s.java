 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.workspace;
 
 import static org.oobium.utils.FileUtils.EXECUTABLE;
 import static org.oobium.utils.FileUtils.PERSIST_LAST_MODIFIED;
 import static org.oobium.utils.FileUtils.copy;
 import static org.oobium.utils.FileUtils.createFolder;
 import static org.oobium.utils.FileUtils.deleteContents;
 import static org.oobium.utils.FileUtils.writeFile;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.oobium.build.BuildBundle;
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.oobium.persist.migrate.MigratorService;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.Config.OsgiRuntime;
 import org.oobium.utils.FileUtils;
 
 public class Exporter {
 
 	/**
 	 * Bundles that are Applications only
 	 */
 	public static final int APP			= 1 << 0;
 
 	
 	/**
 	 * Bundles that are either Applications or Modules (application extends module)
 	 */
 	public static final int MODULE		= 1 << 1;
 	
 	/**
 	 * Bundles that are Migrations
 	 */
 	public static final int MIGRATION	= 1 << 2;
 	
 	/**
 	 * Bundles that export a Service
 	 */
 	public static final int SERVICE		= 1 << 3;
 
 	private static final String START_SCRIPT = 	"#!/bin/bash\n" +
 												"pidfile=../<APP_NAME>.pid\n" +
 												"if [ -e $pidfile ]; then\n" +
 												"  echo \"Already running\"\n" +
 												"  exit 1\n" +
 												"else\n" +
 												"  java -jar bin/felix.jar &\n" +
 												"  echo $! > $pidfile\n" +
 												"fi";
 
 	
 	private static final String STOP_SCRIPT = 	"#!/bin/bash\n" +
 												"pidfile=../<APP_NAME>.pid\n" +
 												"if [ -e $pidfile ]; then\n" +
 												"  if kill `cat $pidfile`\n" +
 												"    then\n" +
 												"      rm $pidfile\n" +
 												"      echo \"Process stopped\"\n" +
 												"  fi\n" +
 												"else\n" +
 												"  echo \"Cannot find $pidfile file - is process actually running?\"\n" +
 												"  exit 1\n" +
 												"fi";
 
 	/**
 	 * Export an individual bundle
 	 * @param mode
 	 * @param bundle
 	 * @return the exported Bundle (note that this bundle will not be available to Workspace.getBundle())
 	 * @throws IOException
 	 */
 	public static Bundle export(Workspace workspace, Application application, Bundle bundle) throws IOException {
 		Exporter exporter = new Exporter(workspace, application);
 		return exporter.export(bundle);
 	}
 
 
 	private final Logger logger;
 	
 	private final Workspace workspace;
 	private final Application application;
 	private final Set<Bundle> start;
 	private final Set<Bundle> includes;
 	private final File exportDir;
 	private final File binDir;
 	private final File bundleDir;
 
 	private boolean clean;
 	private boolean cleanCache;
 	private Mode mode;
 	private Map<String, String> properties;
 	
 	private Set<Bundle> bundles;
 	private Set<Bundle> exportedBundles;
 	private Set<Bundle> exportedStart;
 	
 	private int startTypes;
 	
 	private boolean includeMigrator;
 	private boolean isMigrator;
 	
 	public Exporter(Workspace workspace, Application application) {
 		this.logger = LogProvider.getLogger(BuildBundle.class);
 		this.workspace = workspace;
 		this.application = application;
 		this.start = new LinkedHashSet<Bundle>();
 		this.includes = new LinkedHashSet<Bundle>();
 		this.exportDir = new File(workspace.getExportDir(), application.name);
 		this.binDir = new File(exportDir, "bin");
 		this.bundleDir = new File(exportDir, "bundles");
 
 		this.mode = Mode.DEV;
 
 		this.exportedBundles = new LinkedHashSet<Bundle>();
 		this.exportedStart = new LinkedHashSet<Bundle>();
 
 		setStartTypes(MODULE | SERVICE);
 	}
 	
 	public void add(Bundle...bundles) {
 		add(Arrays.asList(bundles));
 	}
 	
 	public void add(Collection<? extends Bundle> bundles) {
 		includes.addAll(bundles);
 	}
 
 	private void addExported(Bundle bundle, Bundle exportedBundle) {
 		if(start.contains(bundle)) {
 			exportedStart.add(exportedBundle);
 		} else {
 			if(!isMigrator) {
 				if((startTypes & APP) != 0 && exportedBundle.isApplication()) {
 					exportedStart.add(exportedBundle);
 				}
 				if((startTypes & MODULE) != 0 && exportedBundle.isModule()) {
 					exportedStart.add(exportedBundle);
 				}
 				if((startTypes & MIGRATION) != 0 && exportedBundle.isMigration()) {
 					exportedStart.add(exportedBundle);
 				}
 			}
 			if((startTypes & SERVICE) != 0 && exportedBundle.isService()) {
 				exportedStart.add(exportedBundle);
 			}
 		}
 		exportedBundles.add(exportedBundle);
 	}
 	
 	public void addStart(Bundle...bundles) {
 		add(bundles);
 		addStart(Arrays.asList(bundles));
 	}
 	
 	public void addStart(Collection<? extends Bundle> bundles) {
 		add(bundles);
 		start.addAll(bundles);
 	}
 	
 	public void clearServices() {
 		includes.clear();
 	}
 	
 	public void clearStartBundles() {
 		start.clear();
 	}
 	
 	/**
 	 * Only good for Apache Felix - will need to modify if supporting
 	 * additional runtimes in the future...
 	 */
 	private void createConfig() {
 		File configDir = createFolder(exportDir, "conf");
 		
 		File system = new File(configDir, "system.properties");
 		StringBuilder sb = new StringBuilder();
 		for(Entry<String, String> property : getMergedProperties().entrySet()) {
 			sb.append(property.getKey()).append('=').append(property.getValue()).append('\n');
 		}
 		writeFile(system, sb.toString());
 
 		File config = new File(configDir, "config.properties");
 
 		List<Bundle> installed = new ArrayList<Bundle>();
 		List<Bundle> started1 = new ArrayList<Bundle>();
 		List<Bundle> started2 = new ArrayList<Bundle>();
 		for(Bundle bundle : exportedBundles) {
 			if(!bundle.isFramework()) {
 				if(bundle.name.equals("org.oobium.logging") || bundle.name.equals("org.apache.felix.log")) {
 					started1.add(bundle);
 				} else if(exportedStart.contains(bundle)) {
 					started2.add(bundle);
 				} else {
 					installed.add(bundle);
 				}
 			}
 		}
 
 		sb = new StringBuilder();
 		sb.append("felix.auto.install.1=");
 		for(Bundle bundle : installed) {
 			sb.append(" \\\n file:bundles/").append(bundle.file.getName());
 		}
 		sb.append("\n\nfelix.auto.start.1=");
 		for(Bundle bundle : started1) {
 			sb.append(" \\\n file:bundles/").append(bundle.file.getName());
 		}
 		sb.append("\n\nfelix.auto.start.2=");
 		for(Bundle bundle : started2) {
 			sb.append(" \\\n file:bundles/").append(bundle.file.getName());
 		}
 		sb.append("\n\norg.osgi.framework.startlevel.beginning=2");
 
 		writeFile(config, sb.toString());
 	}
 
 	private void createScripts() {
 		File startScript = new File(exportDir, "start.sh");
 		if(startScript.exists()) {
 			logger.info("skipping start script");
 		} else {
 			logger.info("writing start script");
 			writeFile(startScript, START_SCRIPT.replace("<APP_NAME>", application.name), EXECUTABLE);
 		}
 		
 		File stopScript = new File(exportDir, "stop.sh");
 		if(stopScript.exists()) {
 			logger.info("skipping stop script");
 		} else {
 			logger.info("writing stop script");
 			writeFile(stopScript, STOP_SCRIPT.replace("<APP_NAME>", application.name), EXECUTABLE);
 		}
 	}
 	
 	private Bundle doExport(Bundle bundle) throws IOException {
 		File jar;
 		if(bundle.isJar) {
 			jar = "felix.jar".equals(bundle.file.getName()) ? new File(binDir, bundle.file.getName()) : new File(bundleDir, bundle.file.getName());
 			if(jar.exists()) {
 				logger.info("  skipping " + bundle);
 			} else {
 				logger.info("  copying " + bundle);
 				copy(bundle.file, jar, PERSIST_LAST_MODIFIED);
 			}
 		} else {
 			Date date = new Date(FileUtils.getLastModified(bundle.bin));
 			Version version = bundle.version.resolve(date);
 			jar = new File(bundleDir, bundle.name + "_" + version + ".jar");
 			if(jar.exists()) {
 				logger.info("  skipping " + bundle.toString(date));
 			} else {
 				logger.info("  creating " + bundle.toString(date));
 				bundle.createJar(jar, version);
 				jar.setLastModified(date.getTime());
 			}
 		}
 		
 		return (Bundle) Project.load(jar);
 	}
 	
 	/**
 	 * Export the application, configured for the given mode.
 	 * @return a File object for the folder where the application was exported.
 	 * @throws IOException
 	 */
 	public File export() throws IOException {
 		if(exportDir.exists()) {
 			if(clean) {
 				FileUtils.deleteContents(exportDir);
 			} else if(cleanCache) {
 				File felixCache = new File(exportDir, "felix-cache");
 				if(felixCache.exists()) {
 					FileUtils.delete(felixCache);
 				}
 			}
 		} else {
 			exportDir.mkdirs();
 		}
 
 		List<Application> applications = workspace.getApplications(application, mode);
 
 		if(includeMigrator || isMigrator) {
 			Migrator migrator = workspace.getMigratorFor(application);
 			if(migrator == null) {
 				if(isMigrator) {
 					throw new IllegalStateException("migrator does not exist");
 				}
 			} else {
 				logger.info("including migrator");
 				addStart(migrator);
 				List<Bundle> services = workspace.getMigratorServicesFor(application, mode);
 				addStart(services);
 			}
 		}
 		
 		logger.info("determining required bundles");
 		bundles = new TreeSet<Bundle>();
 		for(Application application : applications) {
 			if(!bundles.contains(application)) {
 				Map<Bundle, List<Bundle>> deps = application.getDependencies(workspace, mode);
 				printDependencies(deps);
 				bundles.addAll(deps.keySet());
 				bundles.add(application);
 			}
 		}
 		for(Bundle bundle : includes) {
 			if(!bundles.contains(bundle)) {
 				Map<Bundle, List<Bundle>> deps = bundle.getDependencies(workspace, mode);
 				printDependencies(deps);
 				bundles.addAll(deps.keySet());
 				bundles.add(bundle);
 			}
 		}
 		workspace.setRuntimeBundle(OsgiRuntime.Felix, bundles);
 
 		for(Bundle bundle : start) {
 			if(!bundles.contains(bundle)) {
 				throw new IllegalStateException("bundle is in the start list, but is not being exported: " + bundle);
 			}
 		}
 		
 		if(logger.isLoggingInfo()) {
 			for(Bundle bundle : bundles) {
 				logger.info("  " + bundle);
 			}
 		}
 		
 		logger.info("creating and copying bundles");
 		for(Bundle bundle : bundles) {
 			Bundle exportedBundle = doExport(bundle);
 			addExported(bundle, exportedBundle);
 		}
 
 		deleteContents(new File(exportDir, "configuration"));
 		createConfig();
 		createScripts();
 		
 		return exportDir;
 	}
 
 	private Bundle export(Bundle bundle) throws IOException {
 		if(exportDir.exists()) {
 			if(clean) {
 				FileUtils.deleteContents(exportDir);
 			} else if(cleanCache) {
 				File felixCache = new File(exportDir, "felix-cache");
 				if(felixCache.exists()) {
 					FileUtils.delete(felixCache);
 				}
 			}
 		} else {
 			exportDir.mkdirs();
 		}
 		
 		return doExport(bundle);
 	}
 	
 	public List<Bundle> getBundles() {
 		return new ArrayList<Bundle>(bundles);
 	}
 	
 	public boolean getClean() {
 		return clean;
 	}
 
 	public boolean getCleanCache() {
 		return cleanCache;
 	}
 
 	public File getExportDir() {
 		return exportDir;
 	}
 	
 	public List<Bundle> getExportedBundles() {
 		return new ArrayList<Bundle>(exportedBundles);
 	}
 	
 	public File getExportedJar(Bundle bundle) {
 		if(!exportDir.exists()) {
 			return null;
 		}
 
 		if("felix.jar".equals(bundle.file.getName())) {
 			File exported = new File(exportDir + File.separator + "bin" + File.separator + "felix.jar");
 			return exported.exists() ? exported : null;
 		} else {
 			File bundleDir = new File(exportDir + File.separator + "bundles");
 			final String bundleName = bundle.file.getName() + "_";
 			File[] exportedBundles = bundleDir.listFiles(new FilenameFilter() {
 				@Override
 				public boolean accept(File dir, String name) {
 					return name.startsWith(bundleName) && name.endsWith(".jar");
 				}
 			});
 			if(exportedBundles != null && exportedBundles.length > 0) {
 				File exported = exportedBundles[0];
 				for(int i = 1; i < exportedBundles.length; i++) {
 					if(exported.getName().compareTo(exportedBundles[i].getName()) < 0) {
 						exported = exportedBundles[i];
 					}
 				}
 				return exported;
 			}
 			return null;
 		}
 	}
 	
 	private Map<String, String> getMergedProperties() {
 		Map<String, String> properties = new LinkedHashMap<String, String>();
 		properties.put(Mode.SYSTEM_PROPERTY, mode.name());
 		if(isMigrator) {
 			properties.put(Logger.SYS_PROP_CONSOLE, String.valueOf(Logger.INFO));
 			properties.put(Logger.SYS_PROP_EMAIL, String.valueOf(Logger.NEVER));
 			properties.put(Logger.SYS_PROP_FILE, String.valueOf(Logger.NEVER));
 			properties.put(MigratorService.SYS_PROP_MODE, MigratorService.ACTIVE);
 		} else {
 			switch(mode) {
 			case DEV:
 				properties.put(Logger.SYS_PROP_CONSOLE, String.valueOf(Logger.DEBUG));
 				properties.put(Logger.SYS_PROP_EMAIL, String.valueOf(Logger.NEVER));
 				properties.put(Logger.SYS_PROP_FILE, String.valueOf(Logger.NEVER));
 				break;
 			case TEST:
 				properties.put(Logger.SYS_PROP_CONSOLE, String.valueOf(Logger.DEBUG));
 				properties.put(Logger.SYS_PROP_EMAIL, String.valueOf(Logger.NEVER));
 				properties.put(Logger.SYS_PROP_FILE, String.valueOf(Logger.INFO));
 				break;
 			case PROD:
 				properties.put(Logger.SYS_PROP_CONSOLE, String.valueOf(Logger.WARNING));
 				properties.put(Logger.SYS_PROP_EMAIL, String.valueOf(Logger.NEVER));
 				properties.put(Logger.SYS_PROP_FILE, String.valueOf(Logger.INFO));
 				break;
 			}
 		}
 		if(this.properties != null) {
 			properties.putAll(this.properties);
 		}
 		return properties;
 	}
 
 	public Mode getMode() {
 		return mode;
 	}
 
 	private void printDependencies(Map<Bundle, List<Bundle>> deps) {
 		if(logger.isLoggingDebug()) {
 			logger.debug("dependencies:");
 			for(Entry<Bundle, List<Bundle>> entry : deps.entrySet()) {
 				logger.debug("  " + entry.getKey().name);
 				for(Bundle bundle : entry.getValue()) {
 					logger.debug("    " + bundle.name);
 				}
 			}
 		}
 	}
 	
 	public void setClean(boolean clean) {
 		this.clean = clean;
 	}
 	
 	public void setCleanCache(boolean cleanCache) {
 		this.cleanCache = cleanCache;
 	}
 	
 	public void setIncludeMigrator(boolean include) {
 		this.includeMigrator = include;
 	}
 	
	public void setMigrator(boolean migrator) {
		this.isMigrator = migrator;
	}
	
 	public void setMode(Mode mode) {
 		this.mode = mode;
 	}
 
 	public void setProperties(Map<String, String> properties) {
 		this.properties = properties;
 	}
 	
 	public void setProperties(String...properties) {
 		this.properties = new LinkedHashMap<String, String>();
 		for(String property : properties) {
 			String[] sa = property.split("\\s*=\\s*", 2);
 			this.properties.put(sa[0], sa[1]);
 		}
 	}
 	
 	public void setStartTypes(int startTypes) {
 		this.startTypes = startTypes;
 	}
 
 }
