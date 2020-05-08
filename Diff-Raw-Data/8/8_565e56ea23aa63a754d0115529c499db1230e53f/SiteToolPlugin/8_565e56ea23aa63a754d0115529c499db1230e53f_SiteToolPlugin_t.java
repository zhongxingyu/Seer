 package de.saces.fnplugins.SiteToolPlugin;
 
 import java.io.File;
 import java.io.IOException;
 
 import de.saces.fnplugins.SiteToolPlugin.fproxy.dav.sampleimpl.DAVToadlet;
 import de.saces.fnplugins.SiteToolPlugin.fproxy.dav.sampleimpl.LocalFileSystemStore;
 import de.saces.fnplugins.SiteToolPlugin.fproxy.dav.sampleimpl.SimpleResourceLocks;
 import de.saces.fnplugins.SiteToolPlugin.toadlets.EditSiteToadlet;
 import de.saces.fnplugins.SiteToolPlugin.toadlets.HomeToadlet;
 import de.saces.fnplugins.SiteToolPlugin.toadlets.SessionsToadlet;
 import de.saces.fnplugins.SiteToolPlugin.toadlets.SitesToadlet;
 
 import freenet.l10n.BaseL10n.LANGUAGE;
 import freenet.pluginmanager.FredPlugin;
 import freenet.pluginmanager.FredPluginFCP;
 import freenet.pluginmanager.FredPluginL10n;
 import freenet.pluginmanager.FredPluginRealVersioned;
 import freenet.pluginmanager.FredPluginThreadless;
 import freenet.pluginmanager.FredPluginVersioned;
 import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.Logger;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 import freenet.support.plugins.helpers1.PluginContext;
 import freenet.support.plugins.helpers1.WebInterface;
 /**
  * @author saces
  * 
  */
 public class SiteToolPlugin implements FredPlugin, FredPluginFCP,
 		FredPluginVersioned, FredPluginRealVersioned, FredPluginThreadless, FredPluginL10n {
 
 	public static final String PLUGIN_URI = "/SiteTool";
 	private static final String PLUGIN_CATEGORY = "Site Tools";
 	public static final String PLUGIN_TITLE = "SiteTool Plugin";
 
 	private static volatile boolean logMINOR;
 	private static volatile boolean logDEBUG;
 
 	static {
 		Logger.registerClass(SiteToolPlugin.class);
 	}
 
 	PluginRespirator pr;
 
 	private PluginContext pluginContext;
 	private WebInterface webInterface;
 
 	private SessionManager sessionManager;
 	private SiteManager siteManager;
 	private FCPHandler fcpHandler;
 
 	public void runPlugin(PluginRespirator pluginRespirator) {
 		this.pr = pluginRespirator;
 
 		pluginContext = new PluginContext(pluginRespirator);
 
 		File config = new File("sitetool.ini");
 		try {
 			siteManager = new SiteManager(config);
 		} catch (IOException e) {
 			Logger.error(this, "Error while reading config.", e);
 			e.printStackTrace();
 			siteManager = null;
 		}
 
 		sessionManager = new SessionManager(pluginContext.clientCore.getExecutor());
 
 		fcpHandler = new FCPHandler(sessionManager, pluginContext);
 
 		webInterface = new WebInterface(pluginContext);
 
 		webInterface.addNavigationCategory(PLUGIN_URI+"/", PLUGIN_CATEGORY, "Toolbox for maintaining sites and more...", this);
 
 		// Visible pages
 		HomeToadlet homeToadlet = new HomeToadlet(pluginContext, sessionManager);
 		webInterface.registerVisible(homeToadlet, PLUGIN_CATEGORY, "Tools", "Tool page");
 		SitesToadlet sitesToadlet = new SitesToadlet(pluginContext, siteManager);
 		webInterface.registerVisible(sitesToadlet, PLUGIN_CATEGORY, "Sites Manager", "Upload and manage your sites");
 		SessionsToadlet sessionsToadlet = new SessionsToadlet(pluginContext, sessionManager);
 		webInterface.registerVisible(sessionsToadlet, PLUGIN_CATEGORY, "Session Monitor", "View and manage sessions");
 
 		// Invisible pages
 		EditSiteToadlet editSiteToadlet = new EditSiteToadlet(pluginContext, sitesToadlet);
 		webInterface.registerInvisible(editSiteToadlet);
 
 		// set up DAV
 		// download dir
		LocalFileSystemStore store1 = new LocalFileSystemStore(pr.getNode().clientCore.getDownloadsDir());
 		SimpleResourceLocks resLocks1 = new SimpleResourceLocks();
 
 		// temp downloads
 		// TODO implement a filesystem for temp downloads
		LocalFileSystemStore store2 = new LocalFileSystemStore(pr.getNode().clientCore.getDownloadsDir());
 		SimpleResourceLocks resLocks2 = new SimpleResourceLocks();
 
 		// download dir
 		// TODO implement a filesystem for site editing/creating
		LocalFileSystemStore store3 = new LocalFileSystemStore(pr.getNode().clientCore.getDownloadsDir());
 		SimpleResourceLocks resLocks3 = new SimpleResourceLocks();
 
 		// Invisible pages
 		DAVToadlet davToadlet1 = new DAVToadlet(pluginContext, SiteToolPlugin.PLUGIN_URI, "DAV/downloads", sitesToadlet, store1, resLocks1, false);
 		webInterface.registerInvisible(davToadlet1);
 		DAVToadlet davToadlet2 = new DAVToadlet(pluginContext, SiteToolPlugin.PLUGIN_URI, "DAV/tempdownloads", sitesToadlet, store2, resLocks2, true);
 		webInterface.registerInvisible(davToadlet2);
 		DAVToadlet davToadlet3 = new DAVToadlet(pluginContext, SiteToolPlugin.PLUGIN_URI, "DAV/siteedit", sitesToadlet, store3, resLocks3, true);
 		webInterface.registerInvisible(davToadlet3);
 	}
 
 	public void terminate() {
 		webInterface.kill();
 		fcpHandler.kill();
 		fcpHandler = null;
 		sessionManager.kill();
 		sessionManager = null;
 	}
 
 	public String getVersion() {
 		return Version.longVersionString;
 	}
 
 	public void handle(PluginReplySender replysender, SimpleFieldSet params, Bucket data, int accesstype) {
 		try {
 			fcpHandler.handle(replysender, params, data, accesstype);
 		} catch (PluginNotFoundException pnfe) {
 			Logger.error(this, "Connection to request sender Lost.", pnfe);
 		}
 	}
 
 	public long getRealVersion() {
 		return Version.version;
 	}
 
 	public String getString(String key) {
 		//Logger.error(this, "TODO", new Exception("TODO"));
 		return key;
 	}
 
 	public void setLanguage(LANGUAGE newLanguage) {
 		//Logger.error(this, "TODO", new Exception("TODO"));		
 	}
 }
