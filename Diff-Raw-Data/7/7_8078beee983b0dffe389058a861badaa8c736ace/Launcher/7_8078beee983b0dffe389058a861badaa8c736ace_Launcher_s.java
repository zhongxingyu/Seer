 package org.cytoscape.launcher.internal;
 
 import org.osgi.framework.launch.Framework;
 import org.osgi.framework.launch.FrameworkFactory;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Comparator;
 import java.util.Properties;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.InputStream;
 
 import org.cytoscape.cmdline.CommandLineArgs; 
 
 /** 
  * An OSGi launcher.
  */
 public class Launcher {
 
 	private static String[] args;
	private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
 	
 	public static void main(String[] a) {
 		args = a;
 		
 		launch("org.apache.felix.framework.FrameworkFactory");
 	}
 
 	// Don't make this public!  This should be used interally only!
 	static String[] getArgs() {
 		if ( args == null )
 			return new String[0];
 		else
 			return args;
 	}
 
 	private static void launch(String factoryName) {
 		try {
 			Map config = new HashMap();
 			config.putAll(System.getProperties());
 			config.put("org.osgi.framework.startlevel.beginning", "6");
 			config.put("org.osgi.framework.storage", "bundles/cache");
 			config.put("org.osgi.framework.storage.clean", "onFirstInit");
 			
 			// TODO: Hack to get around JAXB classloading errors
 			config.put("org.osgi.framework.system.packages.extra", "com.sun.xml.internal.bind");
 
 			FrameworkFactory factory = (FrameworkFactory) Class.forName( factoryName ).newInstance(); 
 			Framework framework = factory.newFramework(config);
 
 			framework.init();
 
 			loadStartupBundles(framework.getBundleContext());
 
 			framework.start();
 
 			//debug(framework.getBundleContext());
 
 			framework.waitForStop(0);
 		} catch (Exception e) {
			logger.warn(e.getMessage());
 		}
 	}
 
 	private static void debug(BundleContext bc) {
 		StartLevel sl = (StartLevel) bc.getService(bc.getServiceReference(StartLevel.class.getName()));
 		for (Bundle b : bc.getBundles())
 			System.out.println("bundle: " + b.getSymbolicName() + "("+ b.getState() + ") [startlevel " + sl.getBundleStartLevel(b) + "]");
 		System.out.println("initial start level: " + sl.getInitialBundleStartLevel());
 		System.out.println("current start level: " + sl.getStartLevel());
 	}
 
 	private static void loadStartupBundles(BundleContext bc) {
 		StartLevel sl = (StartLevel) bc.getService(bc.getServiceReference(StartLevel.class.getName()));
 		List<Bundle> startBundles = new ArrayList<Bundle>();
 
 		// look for startlevel directories in the startup dir
 		File dir = new File("bundles");
 		File[] levels = dir.listFiles(new StartLevelFilter());
 
 		// make sure there's something to start!
 		if ( levels == null || levels.length == 0 )
 			return;
 
 		// sort according to level so that we add bundles in order 
 		Arrays.sort(levels,new StartLevelComparator());
 
 		// for each level find the bundles to be started 
 		for ( File level : levels ) {
 			File[] bundles = level.listFiles(new BundleFilter());
 			int startLevel = getStartLevel(level.getName());
 			for ( File b : bundles ) {
 				try {
 					Bundle bundle = bc.installBundle(b.toURI().toURL().toString());
 					sl.setBundleStartLevel(bundle, startLevel);
 					startBundles.add(bundle);
 				} catch (Exception ex) {
 					System.err.println("failed installing bundle: " + b.getName());
 					ex.printStackTrace();
 				}
 			}
 		}
 
 		// register the command line args service
 		bc.registerService(CommandLineArgs.class.getName(),new CommandLineArgsImpl(args), new Properties());
 
 		// start the bundles
 		for ( Bundle b : startBundles ) {
 			try {
 				b.start();
 			} catch (BundleException be) {
 				System.err.println("failed to start bundle: " + b.getSymbolicName() + "  :  " + be.getMessage());
 			}
 		}
 	}
 
 	private static int getStartLevel(String name) {
 		try {
 			return Integer.parseInt(name.substring(11));
 		} catch (Exception e) {
 			e.printStackTrace();
 			return 10;
 		}
 	}
 
 	private static class StartLevelComparator implements Comparator<File> {
 		public int compare(File f1, File f2) {
 			int f1val = getStartLevel(f1.getName()); 
 			int f2val = getStartLevel(f2.getName()); 
 			if ( f1val < f2val )
 				return -1;
 			else if ( f1val > f2val )
 				return 1;
 			else
 				return 0;
 		}
 
 		public boolean equals(Object obj) {
 			return (obj == StartLevelComparator.this);
 		}
 	}
 
 	private static class StartLevelFilter implements FilenameFilter {
 		public boolean accept(File f, String name) {
 			// TODO make this more robust - should be nums greater than 1
 			return name.matches("^startlevel-\\d+$");
 		}
 	}
 
 	private static class BundleFilter implements FilenameFilter {
 		public boolean accept(File f, String name) {
 			return name.endsWith(".jar") ;
 		}
 	}
 
 }
