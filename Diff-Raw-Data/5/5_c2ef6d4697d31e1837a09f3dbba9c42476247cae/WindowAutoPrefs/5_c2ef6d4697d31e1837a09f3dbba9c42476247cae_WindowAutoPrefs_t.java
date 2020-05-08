 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.ui.preferences;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Window;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.prefs.Preferences;
 
 import javax.swing.SwingUtilities;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WindowAutoPrefs {
 	private final ExecutorService delayed = Executors.newSingleThreadExecutor();
 	protected final Logger log;
 
 	private final Window window;
 	protected final Preferences pref;
 	private final String prefWidth;
 	private final String prefHeight;
 
 	private boolean loaded = false;
 	private boolean saved = true;
 
 	public WindowAutoPrefs(Window window) {
 		this.window = window;
 		this.log = LoggerFactory.getLogger(window.getClass());
 		pref = Preferences.userNodeForPackage(window.getClass());
 		prefWidth = makePrefName("/width"); //$NON-NLS-1$
 		prefHeight = makePrefName("/height"); //$NON-NLS-1$
 
 		window.addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(ComponentEvent e) {
 				delayedSaveWindowPreference();
 			}
 		});
 
 		window.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowOpened(WindowEvent e) {
 				if (!loaded) {
 					postVisible();
 					log.trace("Loaded preferences"); //$NON-NLS-1$
 					loaded = true;
 				}
 			}
 
 			@Override
 			public void windowClosed(WindowEvent e) {
 				delayed.shutdownNow();
 			}
 		});
 	}
 
 	protected String makePrefName(String key) {
 		return window.getClass().getSimpleName() + key;
 	}
 
 	public void display() {
 		display(null);
 	}
 
 	public void display(Component reference) {
 		assert (SwingUtilities.isEventDispatchThread());
 
 		if (!loaded) {
 			window.setLocationRelativeTo(reference);
 			preVisible();
 			window.setVisible(true);
 		}
 	}
 
 	protected void preVisible() {
 		loadSizePreference();
 	}
 
 	protected void postVisible() {
 	}
 
 	private void loadSizePreference() {
 		int width = pref.getInt(prefWidth, 0);
 		int height = pref.getInt(prefHeight, 0);
 
 		if (width > 0 && height > 0) {
 			window.setSize(width, height);
 			if (log.isTraceEnabled()) {
 				log.trace("Loaded size preference " + width + "x" + height); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	protected void delayedSaveWindowPreference() {
 		assert (SwingUtilities.isEventDispatchThread());
 
 		if (saved) {
 			saved = false;
 
 			if (log.isTraceEnabled()) {
 				log.trace("Scheduling preferences save"); //$NON-NLS-1$
 			}
 
 			delayed.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						Thread.sleep(500);
 					} catch (InterruptedException e) {
 						return;
 					}
 
 					SwingUtilities.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							saveWindowPreference();
 						}
 					});
 				}
 			});
 		} else {
 			if (log.isTraceEnabled()) {
 				log.trace("Preference save already scheduled"); //$NON-NLS-1$
 			}
 		}
 	}
 
 	protected void saveWidth(int width) {
 		pref.putInt(prefWidth, width);
 		log.trace("Saved width preference " + width); //$NON-NLS-1$
 	}
 
 	protected void saveHeight(int height) {
 		pref.putInt(prefHeight, height);
 		log.trace("Saved height preference " + height); //$NON-NLS-1$
 	}
 
 	private void saveWindowPreference() {
 		saved = true;
 
 		if (!loaded) {
 			return;
 		}
 
 		assert (SwingUtilities.isEventDispatchThread());
 
		log.trace("Saving preferences"); //$NON-NLS-1$

 		savePreferences();
 	}
 
 	protected void savePreferences() {
 		Dimension size = window.getSize();
 
 		saveWidth(size.width);
 		saveHeight(size.height);
 	}
 }
