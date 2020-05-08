 /*******************************************************************************
  * Copyright (c) 2011 Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  *   podsalinan is a project written to 1, get me familiar with java again. and 2, create a
  *   program that will download any url it is given, as well as podcasts from rss feeds that a user
  *   sources.
  *   Podsalinan - loosely translated pod copy
  *   
  *   Written By: Sam Bell
  *   
  */
 package podsalinan;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.util.Timer;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 @SuppressWarnings({ "serial", "unused" })
 public class Podsalinan extends JFrame {
 	
 	private URLDownloadList downloads;
 	private Vector<Podcast> podcasts;
 	private Vector<ProgSettings> progSettings;
 	
 	private TreePane treePane;
 	private JPanel cardPane;
 	
 	private DataStorage settings;
 	
 	private DownloadQueue podcastQueue; // This scans through the lists of files to download and starts a downloader with the next available file
 	private DownloadList downloadView;
 	
 	private boolean programExiting=false;
 	private Object syncObject;
 
 	/**
 	 * Upon execution the program will create a new instance of bgdownloader, which is where
 	 * most of the work happens. Creating the new instance builds the main window.
 	 * 
 	 * @param args
 	 *   Nothing to pass in just yet
 	 */
 	public static void main(String[] args) {
 		Podsalinan mainProgram = new Podsalinan();
 	}
 
 	public Podsalinan(){
 		syncObject=new Object();
 		// Centralizing all of the podcast information, to make it easier to pass around the system
 		podcasts = new Vector<Podcast>();
 		// URL Downloads
 		downloads = new URLDownloadList(syncObject);
 		// Program Settings
 		progSettings = new Vector<ProgSettings>();
 		
 		// Main Window Pane;
 		JPanel pane = new JPanel();
 		
 		// Action Listener for the main window
 		CommandPass aListener = new CommandPass(programExiting,progSettings);
 		aListener.setParent(this);
 		/* 
 		 * creating a new MenuBar passing in the instance of bgdownloader, so bgdownloader can then handle all of
 		 * the gui events, care of ActionListener. This is true, up until the time I decide it's too much, and need
 		 * to create a whole class to handle the actions.  
 		 */
 		MenuBar menubar = new MenuBar(aListener);
		menubar.createMenu("src/podsalinan/menu.xml");
		//menubar.createMenu("menu.xml");
 		
 		//System.out.println("Menubar created");
 		
 		/**
 		 * Following lines are not compatible with gentoo :(
 		 */
 		/*
 		try {
             UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
             SwingUtilities.updateComponentTreeUI(this);
         } catch (Exception e) {
             System.err.println("Couldn't use system look and feel.");
         }
 		*/
 		
 		// disables sqlite4java's logging
 		Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
 		
 		/*
 		 *  For the moment the default size iss 800x600. Eventually I will configure a ini file to store useful information
 		 *  like windows size, position, and anything else I can think of that's needed.
 		 */
 		setSize (800,600);
 		setTitle ("podsalinan");
 	
 		setJMenuBar(menubar);
 		
 		treePane = new TreePane();
 		aListener.setTree(treePane.getTree());
 		
 		// cardPane is used for the individual views of each downloding screen, otherwise
 		// we can't swap between download queues
 		cardPane = new JPanel(new CardLayout()); 
 		treePane.cardView(cardPane);
 
 		//System.out.println("Adding Downloads to Tree and Main Pane");
 		// In the process of Sorting out DownloadList and the singleDownloads vector
 		treePane.setDownloads(downloads);
 		cardPane.add(downloads.getDownloadList(),"Downloads");
 		
 		settings = new DataStorage(syncObject);
 		settings.loadSettings(podcasts, downloads, progSettings, treePane, cardPane);
 		
 		for (int psc=0; psc < progSettings.size(); psc++){
 			if (progSettings.get(psc).setting.equals("urlDirectory")){
 				downloads.setDirectory(progSettings.get(psc).value);
 			}
 		}
 		
 		// New Code adding a Download pane to the screen
 		podcastQueue = new DownloadQueue(programExiting,treePane, progSettings,downloadView, syncObject);
 		JSplitPane downloadSplitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT,cardPane,new JScrollPane(podcastQueue.getdownloadQueue()));
 		downloadSplitPane.setDividerLocation(400);
 		
 		JSplitPane splitpane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,treePane,downloadSplitPane);
 		splitpane.setDividerLocation(250);
 		
 		getContentPane().add(splitpane,BorderLayout.CENTER);
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		
 		// Following 3 lines Set downloads window as current view 
 		CardLayout cardLayout = (CardLayout)(cardPane.getLayout());
 		cardLayout.show(cardPane, "Downloads");
 		
 		//System.out.println("Window Visible");
 		// show the window
 		setVisible(true);
 
 		/* This is the class that does all of the downloading. */
 		Thread downloadingQueue = new Thread(podcastQueue);
 		downloadingQueue.start();
 		//System.out.println("DownloadQueue Created and Started");
 
 		showPodcasts();
 		//System.out.println("Added Podcasts to Gui");
 	
 		// loop here to update the podcasts with a sleep
 		while (!programExiting){
 			int updateInterval=60;
 
 			// Add code here to get podcasts to do an update
 			for (int pcc=0; pcc < podcasts.size(); pcc++){
 				podcasts.get(pcc).downloadFeed();
 			}
 			
 			for (int psc=0; psc < progSettings.size(); psc++){
 				if (progSettings.get(psc).setting.equals("updateTimer")){
 					updateInterval=Integer.parseInt(progSettings.get(psc).value);
 				}
 			}
 
 			synchronized (syncObject){
 				syncObject.notify();
 			}			
 			
 			try {
 				// Put the thread to sleep
 				Thread.sleep((long)updateInterval*60000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/** This is used to start the podcast Threads, which populate the system with
 	 * the data from the podcasts. 
 	 */
 	public void showPodcasts(){
 		for (int pcc=00; pcc < podcasts.size(); pcc++){
 			Thread podcast = new Thread(podcasts.get(pcc));
 			podcast.start();
 			//System.out.println("Podcast Started: "+podcasts.get(pcc).getName());
 		}
 	}
 
 	public void saveSettings(){
 		settings.saveSettings(podcasts, downloads, progSettings);
 	}
 	
 	public URLDownloadList getDownloadList(){
 		return downloads;
 	}
 	
 	/** Adds a new podcast to the system, this is called from CommandPass when the 
 	 * user adds the new podcast
 	 * @param newFeed - Url to the new podcast
 	 */
 	public void addRssFeed(String newFeed){
 		Podcast newPodcast = new Podcast(newFeed, settings, treePane, cardPane, syncObject);
 		podcasts.add(newPodcast);
 		Thread podcastRunner = new Thread(newPodcast);
 		podcastRunner.start();
 	}
 	
 	public TreePane getTreePane(){
 		return treePane;
 	}
 
 	public void addDownload(String url) {
 		downloads.addDownload(url);
 	}
 
 }
