 
 package org.paxle.desktop.impl;
 
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.Dictionary;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 
 import org.paxle.core.IMWComponent;
 import org.paxle.core.norm.IReferenceNormalizer;
 import org.paxle.core.prefs.Properties;
 import org.paxle.core.queue.CommandProfile;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.core.queue.ICommandProfileManager;
 import org.paxle.desktop.IDesktopServices;
 import org.paxle.desktop.backend.IDIBackend;
 import org.paxle.desktop.impl.dialogues.CrawlingConsole;
 import org.paxle.desktop.impl.dialogues.DIServicePanel;
import org.paxle.desktop.impl.dialogues.SettingsPanel;
 
 public class DesktopServices implements IDesktopServices, ManagedService {
 	
 	private static final String TRAY_ICON_LOCATION = "/resources/trayIcon.png";
 	
 	public static enum MWComponents {
 		CRAWLER, PARSER, INDEXER
 		
 		;
 		
 		private final String query;
 		
 		private MWComponents() {
 			query = String.format("(%s=org.paxle.%s)", IMWComponent.COMPONENT_ID, name().toLowerCase());
 		}
 		
 		public String toQuery() {
 			return query;
 		}
 	}
 	
 	private class FrameDICloseListener extends WindowAdapter {
 		
 		private final Dialogues d;
 		private final DIComponent comp;
 		
 		public FrameDICloseListener(final Dialogues d, final DIComponent comp) {
 			this.d = d;
 			this.comp = comp;
 		}
 		
 		@Override
 		public void windowClosed(WindowEvent e) {
 			comp.shutdown();
 			dialogues.remove(d);
 		}
 	}
 	
 	private static final Class<IMWComponent> MWCOMP_CLASS = IMWComponent.class;
 	
 	private static final String ICOMMANDDB = "org.paxle.data.db.ICommandDB";
 	private static final String IROBOTSM = "org.paxle.filter.robots.IRobotsTxtManager";
 	
 	private static final int DEFAULT_PROFILE_MAX_DEPTH = 3;
 	private static final String DEFAULT_NAME = "desktop-crawl";
 	
 	private static final String PREF_PID = IDesktopServices.class.getName();
 	private static final String PREF_OPEN_BROWSER_STARTUP = "openBrowser";
 	private static final String PREF_SHOW_SYSTRAY = "showTrayMenu";
 	
 	// backend-specific properties
 	private static final int PROP_BROWSER_OPENABLE = 1;
 	
 	private static final Dimension DIM_CCONSOLE = new Dimension(600, 400);
 	private static final Dimension DIM_SETTINGS = new Dimension(1000, 600);
 	
 	private final Log logger = LogFactory.getLog(DesktopServices.class);
 	private final ServiceManager manager;
 	private final IDIBackend backend;
 	private final HashMap<Integer,Integer> profileDepthMap = new HashMap<Integer,Integer>();
 	private final HashSet<DIComponent> comps = new HashSet<DIComponent>();
 	private final Map<Dialogues,Frame> dialogues = new EnumMap<Dialogues,Frame>(Dialogues.class);
 	
 	private SystrayMenu trayMenu = null;
 	
 	private boolean browserOpenable = true;
 	
 	public DesktopServices(final ServiceManager manager, final IDIBackend backend) {
 		this.manager = manager;
 		this.backend = backend;
 		initDS();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void initDS() {
 		final Hashtable<String,Object> regProps = new Hashtable<String,Object>();
 		regProps.put(Constants.SERVICE_PID, IDesktopServices.class.getName());
 		manager.registerService(this, regProps, ManagedService.class);
 		
 		final Properties properties = manager.getServiceProperties();
 		if (properties != null) {
 			// get the backend properties, i.e. whether the browser can be opened by the backend
 			final Object backendProps = properties.get(backend.getClass().getName());
 			if (backendProps != null) {
 				final int bp = Integer.parseInt((String)backendProps);
 				browserOpenable = (bp & PROP_BROWSER_OPENABLE) != 0;
 			}
 		}
 		
 		// check whether starting the browser on startup is set in the config and open it if necessary
 		final ConfigurationAdmin cadmin = manager.getService(ConfigurationAdmin.class);
 		if (cadmin != null) try {
 			final Configuration conf = cadmin.getConfiguration(PREF_PID, manager.getBundle().getLocation());
 			Dictionary<?,?> props = null;
 			if (conf != null)
 				props = conf.getProperties();
 			if (props == null)
 				props = getDefaults();
 			
 			if (browserOpenable) {
 				final Object openBrowserStartup = props.get(PREF_OPEN_BROWSER_STARTUP);
 				if (openBrowserStartup != null && ((Boolean)openBrowserStartup).booleanValue())
 					browseUrl(getPaxleUrl("/search"), false);
 			}
 		} catch (IOException e) { e.printStackTrace(); }
 	}
 	
 	public void shutdown() {
 		for (final DIComponent dic : comps) try {
 			dic.shutdown();
 		} catch (Throwable e) { e.printStackTrace(); }
 		
 		final Properties props = manager.getServiceProperties();
 		if (props == null)
 			return;
 		
 		int bp = 0;
 		bp |= (browserOpenable) ? PROP_BROWSER_OPENABLE : 0;
 		props.put(backend.getClass().getName(), Integer.toString(bp));
 		
 		for (Frame f : dialogues.values())
 			f.dispose();
 		dialogues.clear();
 		
 		setTrayMenuVisible(false);
 	}
 	
 	public void shutdownFramework() {
 		try {
 			manager.shutdownFramework();
 		} catch (BundleException e) {
 			Utilities.showExceptionBox("error restarting framework", e);
 			logger.error("error restarting framework", e);
 		}
 	}
 	
 	public void restartFramework() {
 		try {
 			manager.restartFramework();
 		} catch (BundleException e) {
 			Utilities.showExceptionBox("error restarting framework", e);
 			logger.error("error restarting framework", e);
 		}
 	}
 	
 	public void setTrayMenuVisible(final boolean yes) {
 		if (yes && !isTrayMenuVisible()) {
 			trayMenu = new SystrayMenu(this, manager.getBundle().getResource(TRAY_ICON_LOCATION));
 		} else if (!yes && isTrayMenuVisible()) {
 			trayMenu.shutdown();
 			trayMenu = null;
 		}
 	}
 	
 	public boolean isTrayMenuVisible() {
 		return trayMenu != null;
 	}
 	
 	private static Hashtable<String,Object> getDefaults() {
 		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
 		defaults.put(PREF_OPEN_BROWSER_STARTUP, Boolean.TRUE);
 		defaults.put(PREF_SHOW_SYSTRAY, Boolean.TRUE);
 		return defaults;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public synchronized void updated(Dictionary properties) throws ConfigurationException {
 		try {
 			if (properties == null) {
 				properties = getDefaults();
 			}
 			
 			final Object showTrayMenu = properties.get(PREF_SHOW_SYSTRAY);
 			if (showTrayMenu != null) {
 				final boolean showTM = ((Boolean)showTrayMenu).booleanValue();
 				setTrayMenuVisible(showTM);
 			}
 		} catch (Throwable e) { e.printStackTrace(); }
 	}
 	
 	private Frame createDefaultFrame(final DIServicePanel container, final Dialogues d, final String title, final Dimension dim) {
 		return Utilities.wrapIntoFrame(container, title, dim, true, Utilities.LOCATION_CENTER, new FrameDICloseListener(d, container));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void openDialogue(final Dialogues d) {
 		Frame ccframe = dialogues.get(d);
 		if (ccframe == null) {
 			final Frame frame;
 			switch (d) {
 				case CCONSOLE: frame = createDefaultFrame(new CrawlingConsole(this), d, "Crawling Console", DIM_CCONSOLE); break;
				case SETTINGS: frame = createDefaultFrame(new SettingsPanel(this), d, "Paxle Settings", DIM_SETTINGS); break;
 				
 				default:
 					throw new RuntimeException("switch-statement does not cover " + d);
 			}
 			dialogues.put(d, frame);
 			ccframe = frame;
 		}
 		show(ccframe);
 	}
 	
 	public void closeDialogue(final Dialogues d) {
 		final Frame ccframe = dialogues.get(d);
 		if (ccframe != null) {
 			ccframe.dispose();
 			dialogues.remove(d);
 		}
 	}
 	
 	private static void show(final Frame frame) {
 		final int extstate = frame.getExtendedState();
 		if ((extstate & Frame.ICONIFIED) == Frame.ICONIFIED)
 			frame.setExtendedState(extstate ^ Frame.ICONIFIED);
 		if (!frame.isVisible())
 			frame.setVisible(true);
 		frame.toFront();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public IMWComponent<ICommand> getMWComponent(final MWComponents comp) {
 		try {
 			final IMWComponent<?>[] comps = manager.getServices(MWCOMP_CLASS, comp.toQuery());
 			if (comps != null && comps.length > 0)
 				return (IMWComponent<ICommand>)comps[0];
 		} catch (InvalidSyntaxException e) {
 			Utilities.showExceptionBox(e);
 			e.printStackTrace();
 		}
 		return null; 
 	}
 	
 	public boolean isServiceAvailable(MWComponents comp) {
 		try {
 			return manager.hasService(MWCOMP_CLASS, comp.toQuery());
 		} catch (InvalidSyntaxException e) {
 			Utilities.showExceptionBox(e);
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean isBrowserOpenable() {
 		return browserOpenable;
 	}
 	
 	public ServiceManager getServiceManager() {
 		return manager;
 	}
 	
 	public IDIBackend getBackend() {
 		return backend;
 	}
 	
 	public void startDefaultCrawl(final String location) {
 		try {
 			startCrawl(location, DEFAULT_PROFILE_MAX_DEPTH);
 		} catch (ServiceException ee) {
 			Utilities.showURLErrorMessage("Starting crawl failed: " + ee.getMessage(), location);
 			logger.error("Starting crawl of URL '" + location + "' failed: " + ee.getMessage(), ee);
 		}
 	}
 	
 	public void startCrawl(final String location, final int depth) throws ServiceException {
 		// get the command-db object and it's method to enqueue the URI
 		final Object commandDB;
 		final Method enqueueCommand;
 		try {
 			commandDB = manager.getService(ICOMMANDDB);
 			if (commandDB == null)
 				throw new ServiceException("Command-DB", ICOMMANDDB);
 			enqueueCommand = commandDB.getClass().getMethod("enqueue", URI.class, int.class, int.class);
 		} catch (NoSuchMethodException e) {
 			throw new ServiceException("Command-DB", "enqueue(URI, int, int)");
 		}
 		
 		final IReferenceNormalizer refNormalizer = manager.getService(IReferenceNormalizer.class);
 		if (refNormalizer == null)
 			throw new ServiceException("Reference normalizer", IReferenceNormalizer.class.getName());
 		final URI uri = refNormalizer.normalizeReference(location);
 		
 		// check uri against robots.txt
 		final Object robotsManager = manager.getService(IROBOTSM);
 		if (robotsManager != null) try {
 			final Method isDisallowed = robotsManager.getClass().getMethod("isDisallowed", URI.class);
 			final Object result = isDisallowed.invoke(robotsManager, uri);
 			if (((Boolean)result).booleanValue()) {
 				logger.info("Domain does not allow crawling of '" + uri + "' due to robots.txt blockage");
 				Utilities.showURLErrorMessage(
 						"This URI is blocked by the domain's robots.txt, see",
 						uri.resolve(URI.create("/robots.txt")).toString());
 				return;
 			}
 		} catch (Exception e) {
 			logger.warn(String.format("Error retrieving robots.txt from host '%s': [%s] %s - continuing crawl",
 					uri.getHost(), e.getClass().getName(), e.getMessage()));
 		}
 		
 		// get or create the crawl profile to use for URI
 		ICommandProfile cp = null;
 		final ICommandProfileManager profileDB = manager.getService(ICommandProfileManager.class);
 		if (profileDB == null)
 			throw new ServiceException("Profile manager", ICommandProfileManager.class.getName());
 		
 		final Integer depthInt = Integer.valueOf(depth);
 		final Integer id = profileDepthMap.get(depthInt);
 		if (id != null)
 			cp = profileDB.getProfileByID(id.intValue());
 		if (cp == null) {
 			// create a new profile
 			cp = new CommandProfile();
 			cp.setMaxDepth(depth);
 			cp.setName(DEFAULT_NAME);
 			profileDB.storeProfile(cp);
 		}
 		if (id == null || cp.getOID() != id.intValue())
 			profileDepthMap.put(depthInt, Integer.valueOf(cp.getOID()));
 		
 		try {
 			final Object result = enqueueCommand.invoke(commandDB, uri, Integer.valueOf(cp.getOID()), Integer.valueOf(0));
 			if (((Boolean)result).booleanValue()) {
 				logger.info("Initiated crawl of URL '" + uri + "'");
 			} else {
 				logger.info("Initiating crawl of URL '" + uri + "' failed, URL is already known");
 			}
 		} catch (Exception e) {
 			throw new ServiceException("Crawl start", e.getMessage(), e);
 		}
 	}
 	
 	public String getPaxleUrl(String... path) {
 		final String port = manager.getProperty("org.osgi.service.http.port");
 		if (port == null)
 			return null;
 		final StringBuffer sb = new StringBuffer("http://localhost:").append(port);
 		if (path.length == 0 || path[0].charAt(0) != '/')
 			sb.append('/');
 		for (final String s : path)
 			sb.append(s);
 		return sb.toString();
 	}
 	
 	public boolean browseUrl(final String url) {
 		return browseUrl(url, true);
 	}
 	
 	public boolean browseUrl(final String url, final boolean displayErrMsg) {
 		if (url == null) {
 			JOptionPane.showMessageDialog(null, "HTTP service not accessible", "Error", JOptionPane.ERROR_MESSAGE);
 		} else if (browserOpenable) try {
 			if ((browserOpenable = backend.getDesktop().browse(url))) {
 				return true;
 			} else if (displayErrMsg) {
 				Utilities.showURLErrorMessage(
 						"Couldn't launch system browser due to an error in Paxle's system integration\n" +
 						"bundle. Please review the log for details. The requested URL was:", url);
 			}
 		} catch (MalformedURLException e) {
 			Utilities.showExceptionBox("Generated mal-formed URL", e);
 			logger.error("Generated mal-formed URL '" + url + "': " + e.getMessage(), e);
 		}
 		return false;
 	}
 }
