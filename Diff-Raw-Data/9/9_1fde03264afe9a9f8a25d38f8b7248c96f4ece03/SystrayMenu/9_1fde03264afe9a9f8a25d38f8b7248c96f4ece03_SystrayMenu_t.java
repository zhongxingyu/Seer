 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.desktop.impl;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.ImageIcon;
 import javax.swing.SwingUtilities;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.osgi.service.http.HttpService;
 
 import org.paxle.core.IMWComponent;
 import org.paxle.desktop.IDIEventListener;
 import org.paxle.desktop.IDIServiceEvent;
import org.paxle.desktop.IDialogueServices.Dialogues;
 import org.paxle.desktop.backend.IDIBackend;
 import org.paxle.desktop.backend.IPopupMenuListener;
 import org.paxle.desktop.backend.tray.IMenuItem;
 import org.paxle.desktop.backend.tray.IPopupMenu;
 import org.paxle.desktop.backend.tray.ISystemTray;
 import org.paxle.desktop.backend.tray.ITrayIcon;
 import org.paxle.desktop.impl.ServiceManager.MWComponents;
 import org.paxle.desktop.impl.dialogues.SmallDialog;
 
 public class SystrayMenu implements ActionListener {
 	
 	private static final String CRAWL_PAUSE = Messages.getString("systrayMenu.crawlPause"); //$NON-NLS-1$
 	private static final String CRAWL_RESUME = Messages.getString("systrayMenu.crawlResume"); //$NON-NLS-1$
 	
 	private static final String CLZ_ISERVLET_MANAGER = "org.paxle.gui.IServletManager";
 	private static final String CLZ_ISEARCH_PROVIDER_MANAGER = "org.paxle.se.search.ISearchProviderManager";
 	
 	private static enum Actions {
 		SEARCH, BROWSE, CRAWL, CRAWLPR, BUNDLES, STATS, CCONSOLE, SETTINGS, RESTART, QUIT
 	}
 	
 	private static final Log logger = LogFactory.getLog(SystrayMenu.class);
 	private final ServiceManager services;
 	private final DesktopServices desktop;
 	private final DialogueServices dialogue;
 	private final CrawlStartHelper crawl;
 	private final PopupMenuUpdater updater;
 	
 	private final ITrayIcon ti;
 	private final ISystemTray systray;
 	private final IPopupMenu menu;
 	private final IMenuItem searchItem;
 	private final IMenuItem browseItem;
 	private final IMenuItem crawlItem;
 	private final IMenuItem crawlprItem;
 	
 	private final Timer tooltipTimer = new Timer("DI-TooltipTimer"); //$NON-NLS-1$
 	
 	private class PopupMenuUpdater implements IPopupMenuListener, ActionListener, Runnable, IDIEventListener {
 		
 		private final class Entry {
 			int location;
 			Entry prev, next;
 			
 			public Entry(final int location) {
 				this.location = location;
 			}
 		}
 		
 		private int end = 0;
 		
 		// map with doubly-linked entry-values
 		private final HashMap<Long,Entry> serviceMenuItems = new HashMap<Long,Entry>();
 		private Entry lastEntry = null;
 		
 		public PopupMenuUpdater(final int startIdx) {
 			end = startIdx;
 		}
 		
 		public void popupMenuWillBecomeVisible() {
 			logger.debug("popup becomes visible, refreshing menu-items"); //$NON-NLS-1$
 			SwingUtilities.invokeLater(this);
 		}
 		
 		public void serviceRegistered(IDIServiceEvent event) {
 			if (lastEntry == null)
 				menu.insertSeparator(end++);
 			final int location = end++;
 			final Entry e = new Entry(location);
 			e.prev = lastEntry;
 			if (lastEntry != null)
 				lastEntry.next = e;
 			lastEntry = e;
 			serviceMenuItems.put(event.getID(), e);
 			menu.insert(desktop.getBackend().createMenuItem(event.getComponent().getTitle(), event.getID().toString(), this), location);
 		}
 		
 		public void serviceUnregistering(IDIServiceEvent event) {
 			Entry e = serviceMenuItems.remove(event.getID());
 			menu.remove(e.location);
 			end--;
 			
 			if (e == lastEntry)
 				lastEntry = e.prev;
 			
 			if (e.prev == null && e.next == null) {
 				menu.remove(e.location - 1);
 				end--;
 			}
 			if (e.prev != null)
 				e.prev.next = e.next;
 			if (e.next != null)
 				e.next.prev = e.prev;
 			
 			while ((e = e.next) != null)
 				e.location--;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			Long id = null;
 			try {
 				id = Long.valueOf(e.getActionCommand());
 			} catch (NumberFormatException ee) {  }
 			if (id == null) {
 				logger.warn("actionPerformed for unknown actionCommand '" + e.getActionCommand() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 				return;
 			}
 			
 			if (dialogue.show(id) == null) {
 				logger.warn("actionPerformed for unknown di-component #" + id.toString() + ""); //$NON-NLS-1$ //$NON-NLS-2$
 				return;
 			}
 		}
 		
 		public void run() {
 			final IMWComponent<?> crawler = services.getMWComponent(MWComponents.CRAWLER);
 			final boolean hasCrawler = (crawler != null);
 			crawlItem.setEnabled(hasCrawler);
 			crawlprItem.setEnabled(hasCrawler);
 			
 			final ServiceManager manager = desktop.getServiceManager();
 			final boolean hasWebServer = manager.hasService(HttpService.class);
 			final boolean hasWebui = manager.hasService(CLZ_ISERVLET_MANAGER) && hasWebServer;
 			browseItem.setEnabled(desktop.isBrowserOpenable() && hasWebui);
 			
 			final boolean hasSearch = manager.hasService(CLZ_ISEARCH_PROVIDER_MANAGER);
 			// remove "&& hasWebUi" if we have other methods of displaying the searchresults
 			searchItem.setEnabled(desktop.isBrowserOpenable() && hasSearch && hasWebui);
 			
 			if (hasCrawler)
 				crawlprItem.setText((crawler.isPaused()) ? CRAWL_RESUME : CRAWL_PAUSE);
 		}
 	}
 	
 	public SystrayMenu(
 			final ServiceManager services,
 			final DesktopServices desktop,
 			final DialogueServices dialogue,
 			final CrawlStartHelper crawl,
 			final URL iconResource) {
 		this.services = services;
 		this.desktop = desktop;
 		this.dialogue = dialogue;
 		this.crawl = crawl;
 		
 		final IDIBackend backend = desktop.getBackend();
 		final IMenuItem[] items = {
 				this.searchItem 	= backend.createMenuItem(Messages.getString("systrayMenu.search"), Actions.SEARCH.name(), this),		//  0 //$NON-NLS-1$
 				null,																														//  1
 				this.browseItem		= backend.createMenuItem(Messages.getString("systrayMenu.webinterface"), Actions.BROWSE.name(), this),	//  2 //$NON-NLS-1$
 				null,																														//  3
 				this.crawlItem 		= backend.createMenuItem(Messages.getString("systrayMenu.crawl"), Actions.CRAWL.name(), this),			//  4 //$NON-NLS-1$
 				this.crawlprItem 	= backend.createMenuItem(CRAWL_PAUSE,    Actions.CRAWLPR.name(),  this),								//  5
 				backend.createMenuItem(Messages.getString("systrayMenu.crawlingConsole"), Actions.CCONSOLE.name(), this),					//  6 //$NON-NLS-1$
 				null,																														//  7
 				backend.createMenuItem(Messages.getString("systrayMenu.bundles"),         Actions.BUNDLES.name(),  this),					//  8 //$NON-NLS-1$
 				backend.createMenuItem(Messages.getString("systrayMenu.statistics"),      Actions.STATS.name(),    this),					//  9 //$NON-NLS-1$
 				backend.createMenuItem(Messages.getString("systrayMenu.settings"),        Actions.SETTINGS.name(), this),					// 10 //$NON-NLS-1$
 				null,																														// 11
 				backend.createMenuItem(Messages.getString("systrayMenu.restart"), 		  Actions.RESTART.name(),  this), //$NON-NLS-1$
 				backend.createMenuItem(Messages.getString("systrayMenu.quit"), 			  Actions.QUIT.name(),     this), //$NON-NLS-1$
 		};
 		menu = backend.createPopupMenu(items);
 		updater = new PopupMenuUpdater(items.length - 3);
 		dialogue.addDIEventListener(updater);
 		menu.addPopupMenuListener(updater);
 		
 		systray = backend.getSystemTray();
 		systray.add(ti = backend.createTrayIcon(new ImageIcon(iconResource), Messages.getString("systrayMenu.tray"), menu)); //$NON-NLS-1$
 		
 		tooltipTimer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				final IMWComponent<?> indexer = services.getMWComponent(MWComponents.INDEXER);
 				if (indexer != null) {
 					final StringBuilder sb = new StringBuilder("Paxle"); //$NON-NLS-1$
 					final IMWComponent<?> crawler = services.getMWComponent(MWComponents.CRAWLER);
 					
 					if (crawler == null || !crawler.isPaused() && crawler.getEnqueuedJobCount() == 0) {
 						sb.append(Messages.getString("systrayMenu.idle")); //$NON-NLS-1$
 					} else if (crawler.isPaused()) {
 						sb.append(Messages.getString("systrayMenu.crawlingPaused")); //$NON-NLS-1$
 					} else {
 						sb.append(MessageFormat.format(Messages.getString("systrayMenu.crawlingAtPPM"), Integer.valueOf(indexer.getPPM()))); //$NON-NLS-1$
 					}
 					ti.setToolTip(sb.toString());
 				}
 			}
 		}, 0L, 1000L);
 		
 		// desktop integration needs to be quite responsive, therefore we impudently increase
 		// the EventQueue's dispatcher thread's priority
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				Thread.currentThread().setPriority(8);
 			}
 		});
 	}
 	
 	// private long tmBecameVisible = 0;
 	
 	/*
 	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
 		AWTEvent event = queue.peekEvent();
 		System.out.println("##### invisible -> next event: " + event);
 		boolean canceled = false;
 		if (event == null)
 			canceled = true;
 		else {
 			if (event instanceof InvocationEvent) try {
 				queue.getNextEvent();
 				canceled = true;
 			} catch (Exception ee) {
 				ee.printStackTrace();
 			} else if (event instanceof KeyEvent) {
 				if (((KeyEvent)event).getKeyChar() == 27)
 					canceled = true;
 			}
 		}
 		
 		System.out.println("##### Popup menu has been canceled: " + canceled);
 	}
 	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 		logger.debug("popup becomes visible, refreshing menu-items");
 		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
 		try {
 			AWTEvent event = queue.peekEvent();
 			System.out.println("##### visible -> next event: " + event);
 		} catch (Exception ee) {
 			ee.printStackTrace();
 		}
 		SwingUtilities.invokeLater(refresh);
 		System.out.println("becoming visible: " + tmBecameVisible);
 		Thread.dumpStack();
 		if (tmBecameVisible < 0) {
 			tmBecameVisible = 0;
 		} else {
 			tmBecameVisible = System.currentTimeMillis();
 		}
 	}
 	 */
 	public void close() {
 		dialogue.removeDIEventListener(updater);
 		tooltipTimer.cancel();
 		logger.debug("removing systray icon"); //$NON-NLS-1$
 		systray.remove(ti);
 		logger.debug("removed systray icon successfully"); //$NON-NLS-1$
 	}
 	
 	/* TODO:
 	 * - set location and size of icon to display the dialog at the correct position next to the icon
 	 *   - what to do about the JRE6-backend? How to determine the position of the icon on the screen? */
 	public void actionPerformed(ActionEvent e) {
 		final String cmd = e.getActionCommand();
 		final Actions action = Actions.valueOf(cmd);
 		if (action == null)
 			throw new RuntimeException("unknown action '" + cmd + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 		new TrayThread(action).start();
 	}
 	
 	private class TrayThread extends Thread {
 		
 		private final Actions action;
 		
 		public TrayThread(final Actions action) {
 			this.action = action;
 		}
 		
 		@Override
 		public void run() {
 			switch (action) {
 				case SEARCH: {
 					final String data = SmallDialog.showDialog(Messages.getString("systrayMenu.enterQuery"), Messages.getString("systrayMenu.searchButton")); //$NON-NLS-1$ //$NON-NLS-2$
 					if (data != null && data.length() > 0)
 						desktop.browseUrl(desktop.getPaxleUrl("/search?query=", data)); //$NON-NLS-1$
 				} break;
 				
 				case CRAWL: {
 					final String data = SmallDialog.showDialog(Messages.getString("systrayMenu.enterUrl"), Messages.getString("systrayMenu.crawlButton")); //$NON-NLS-1$ //$NON-NLS-2$
 					if (data != null && data.length() > 0)
 						crawl.startDefaultCrawl(data);
 				} break;
 				
 				case BROWSE:
 					desktop.browseDefaultServlet(true); //$NON-NLS-1$
 					break;
 				
 				case CRAWLPR: {
 					final IMWComponent<?> crawler = services.getMWComponent(MWComponents.CRAWLER);
 					if (crawler != null) {
 						if (crawler.isPaused()) {
 							crawler.resume();
 						} else {
 							crawler.pause();
 						}
 					}
 				} break;
 				
 				case BUNDLES:
 					dialogue.openDialogue(Dialogues.BUNDLES);
 					break;
 				
 				case STATS:
 					dialogue.openDialogue(Dialogues.STATS);
 					break;
 				
 				case CCONSOLE:
 					dialogue.openDialogue(Dialogues.CCONSOLE);
 					break;
 				
 				case SETTINGS:
 					dialogue.openDialogue(Dialogues.SETTINGS);
 					break;
 				
 				case RESTART:
 					desktop.restartFramework();
 					break;
 				
 				case QUIT:
 					desktop.shutdownFramework();
 					break;
 				
 				default:
 					throw new RuntimeException("switch-statement does not cover action '" + action + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 }
