 
 package org.paxle.desktop.impl;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.service.http.HttpService;
 
 import org.paxle.core.IMWComponent;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.queue.Command;
 import org.paxle.core.queue.ICommand;
 import org.paxle.desktop.backend.IDIBackend;
 import org.paxle.desktop.backend.tray.IMenuItem;
 import org.paxle.desktop.backend.tray.IPopupMenu;
 import org.paxle.desktop.backend.tray.ITrayIcon;
 import org.paxle.desktop.impl.dialogues.AFinally;
 import org.paxle.desktop.impl.dialogues.SmallDialog;
 
 public class SystrayMenu2 implements ActionListener, PopupMenuListener {
 	
 	private static final Class<IDataSink> CRAWLER_SINK_CLASS = IDataSink.class;
 	private static final String CRAWLER_SINK_QUERY = "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)";
 	private static final Class<IMWComponent> MWCOMP_CLASS = IMWComponent.class;
 	private static final String CRAWLER_QUERY = String.format("(%s=org.paxle.crawler)", IMWComponent.COMPONENT_ID);
 	
 	private static final String CRAWL_PAUSE = "Pause Crawling";
 	private static final String CRAWL_RESUME = "Resume Crawling";
 	
 	private static final String CRAWL = new String();
 	private static final String CRAWLPR = new String();
 	private static final String SEARCH = new String();
 	private static final String BROWSE = new String();
 	private static final String RESTART = new String();
 	private static final String QUIT = new String();
 	
 	private final Log logger = LogFactory.getLog(SystrayMenu2.class);
 	private final ServiceManager manager;
 	private final IDIBackend backend;
 	private final ITrayIcon ti;
 	
 	private final IMenuItem searchItem;
 	private final IMenuItem browseItem;
 	private final IMenuItem crawlItem;
 	private final IMenuItem crawlprItem;
 	private final IMenuItem quitItem;
 	private final IMenuItem restartItem;
 	
 	public SystrayMenu2(ServiceManager manager, IDIBackend backend, URL iconResource) {
 		this.manager = manager;
 		this.backend = backend;
 		
 		final IPopupMenu pm = backend.createPopupMenu(
 				this.searchItem 	= backend.createMenuItem("Search...", 		SEARCH, 	this),
 				null,
 				this.browseItem		= backend.createMenuItem("Webinterface", 	BROWSE,		this),
 				null,
 				this.crawlItem 		= backend.createMenuItem("Crawl...", 		CRAWL, 		this),
 				this.crawlprItem 	= backend.createMenuItem("Pause Crawling", 	CRAWLPR, 	this),
 				null,
 				this.restartItem 	= backend.createMenuItem("Restart", 		RESTART, 	this),
 				this.quitItem 		= backend.createMenuItem("Quit", 			QUIT, 		this));
 		pm.addPopupMenuListener(this);
 		
 		this.ti = backend.createTrayIcon(new ImageIcon(iconResource), "Paxle Tray", pm);
 		backend.getSystemTray().add(this.ti);
 	}
 	
 	public void popupMenuCanceled(PopupMenuEvent e) { /* ignore */ }
 	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { /* ignore */ }
 	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 		logger.debug("popup becomes visible, refreshing menu-items");
 		refresh();
 	}
 	
 	public void shutdown() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				logger.debug("removing systray icon");
 				backend.getSystemTray().remove(ti);
 				logger.debug("removed systray icon successfully");
 			}
 		});
 	}
 	
 	private void refresh() {
 		try {
 			final boolean hasCrawler = manager.hasService(MWCOMP_CLASS, CRAWLER_QUERY);
 			crawlItem.setEnabled(hasCrawler);
 			crawlprItem.setEnabled(hasCrawler);
 		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
 		
 		final boolean hasSearch = manager.hasService("org.paxle.se.search.ISearchProviderManager");
 		searchItem.setEnabled(hasSearch);
 		
 		final boolean hasWebui = manager.hasService(HttpService.class) && manager.hasService("org.paxle.gui.IServletManager");
 		browseItem.setEnabled(hasWebui);
 		
 		crawlprItem.setText((crawlersPaused()) ? CRAWL_RESUME : CRAWL_PAUSE);
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		final String cmd = e.getActionCommand();
 		
 		if (cmd == CRAWL) {
 			/* TODO:
 			 * - set location and size of icon to display the dialog at the correct position next to the icon
 			 *   - what to do about the JRE6-backend? How to determine the position of the icon on the screen? */
 			new SmallDialog(new CrawlFinally(), "Enter URL:", "Crawl").setVisible(true);
 			
 		} else if (cmd == CRAWLPR) {
 			toggleCrawlersPR();
 			
 		} else if (cmd == SEARCH) {
 			new SmallDialog(new SearchFinally(), "Enter query:", "Search").setVisible(true);
 			
 		} else if (cmd == BROWSE) {
 			try {
 				final String url = getPaxleURL("/");
 				if (url == null) {
 					JOptionPane.showMessageDialog(null, "HTTP service not accessible", "Error", JOptionPane.ERROR_MESSAGE);
 				} else if (!backend.getDesktop().browse(url)) {
 					Utilities.showURLErrorMessage(
 							"Couldn't launch system browser due to an error in Paxle's system integration\n" +
 							"bundle. Please review the log for details. The requested URL was:", url);
 				}
 			} catch (MalformedURLException ee) { ee.printStackTrace(); }
 			
 		} else if (cmd == RESTART || cmd == QUIT) {
 			SwingUtilities.invokeLater(new ShutdownRunnable(cmd == RESTART));
 		}
 	}
 	
 	private boolean crawlersPaused() {
 		try {
 			final IMWComponent<?>[] crawlers = manager.getServices(MWCOMP_CLASS, CRAWLER_QUERY);
 			if (crawlers == null)
 				return false;
 			
 			for (int i=0; i<crawlers.length; i++)
 				if (!crawlers[i].isPaused())
 					return false;
 			return true;
 		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
 		return false;
 	}
 	
 	private void toggleCrawlersPR() {
 		try {
 			final IMWComponent<?>[] crawlers = manager.getServices(MWCOMP_CLASS, CRAWLER_QUERY);
 			if (crawlers == null)
 				return;
 			
 			boolean pause = false;
 			for (int i=0; i<crawlers.length; i++)
 				if (!crawlers[i].isPaused()) {
 					pause = true;
 					break;
 				}
 			
 			if (pause) {
 				for (int i=0; i<crawlers.length; i++)
 					crawlers[i].pause();
 			} else {
 				for (int i=0; i<crawlers.length; i++)
 					crawlers[i].resume();
 			}
 		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
 	}
 	
 	private String getPaxleURL(String path) {
 		final String port = SystrayMenu2.this.manager.getProperty("org.osgi.service.http.port");
 		if (port == null)
 			return null;
 		return String.format("http://localhost:%s%s", port, path);
 	}
 	
 	private class ShutdownRunnable implements Runnable {
 		
 		private final boolean restart;
 		
 		public ShutdownRunnable(final boolean restart) {
 			this.restart = restart;
 		}
 		
 		public void run() {
 			try {
 				if (restart) {
					logger.debug("SHUTTING DOWN FRAMEWORK");
					manager.shutdownFramework();
					logger.debug("FINISHED SHUTTING DOWN FRAMEWORK");
				} else {
 					logger.debug("RESTARTING FRAMEWORK");
 					manager.restartFramework();
 					logger.debug("FINISHED RESTARTING FRAMEWORK");
 				}
 			} catch (BundleException e) {
 				logger.error("error " + ((restart) ? "restarting" : "shutting down") + " framework", e);
 			}
 		}
 	}
 	
 	private class SearchFinally extends AFinally {
 		@Override
 		public void run() {
 			try {
 				final String url = getPaxleURL("/search?query=" + data);
 				if (url == null) {
 					JOptionPane.showMessageDialog(null, "HTTP service not accessible", "Error", JOptionPane.ERROR_MESSAGE);
 				} else if (!backend.getDesktop().browse(url)) {
 					Utilities.showURLErrorMessage(
 							"Couldn't launch system browser due to an error in Paxle's system integration\n" +
 							"bundle. Please review the log for details. The requested URL was:", url);
 				}
 			} catch (MalformedURLException e) { e.printStackTrace(); }
 		}
 	}
 	
 	private class CrawlFinally extends AFinally {
 		@Override
 		@SuppressWarnings("unchecked")
 		public void run() {
 			try {
 				final IDataSink<ICommand>[] sink = SystrayMenu2.this.manager.getServices(CRAWLER_SINK_CLASS, CRAWLER_SINK_QUERY);
 				if (sink != null)
 					sink[0].putData(Command.createCommand(super.data));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
