 package ted;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.w3c.dom.Element;
 
 import ted.ui.TimedOptionPane;
 import ted.ui.addshowdialog.AddShowDialog;
 import ted.ui.editshowdialog.EditShowDialog;
 import ted.ui.messaging.MessengerCenter;
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 /**
  * TED: Torrent Episode Downloader (2005 - 2007)
  * 
  * This is the mainwindow of ted
  * It shows all the shows with their urls, status and more and includes menus
  * and buttons for the user to interact with ted.
  * 
  * @author Roel
  * @author Joost
  *
  * ted License:
  * This file is part of ted. ted and all of it's parts are licensed
  * under GNU General Public License (GPL) version 2.0
  * 
  * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
  *
  */
 public class TedMainDialog extends javax.swing.JFrame implements ActionListener 
 {
 	/****************************************************
 	 * GLOBAL VARIABLES
 	 ****************************************************/
 	private static final long serialVersionUID = 3722636937353936684L;
 
 	private static final double tedVersion = 0.95;
 	
 	// menu images
 	private ImageIcon tedProgramIcon = new ImageIcon(getClass().getClassLoader().getResource("icons/icon-ted2.png")); //$NON-NLS-1$
 	private ImageIcon tedIdleIcon    = new ImageIcon(getClass().getClassLoader().getResource("icons/icon-ted.gif")); //$NON-NLS-1$
 	private ImageIcon tedActiveIcon  = new ImageIcon(getClass().getClassLoader().getResource("icons/icon-active-ted.gif")); //$NON-NLS-1$
 	private final Font SMALL_FONT    = new Font("Dialog",0,10);
 	
 	private JLabel label_count;
 	private JScrollPane jScrollPane1;
 	
 	private TedTable          serieTable;	
 	private TedCounter        tCounter;	
 	private TedLogDialog      tLog;	
 	private TedParseHandler   tParseHandler;	
 	private TedMainToolBar    TedToolBar;
 	private TedTablePopupMenu ttPopupMenu;
 	private TedMainMenuBar    tMenuBar;
 	private TedTrayIcon       tedTray;
 		
 	private JPanel jStatusPanel;	
 	private JPanel jPanel1;
 
 	private boolean osHasTray = TedSystemInfo.osSupportsTray();
 	private boolean stopParsing = false;
 	private boolean isParsing = false;
 	
 	private boolean uiInitialized = false;
 	
 	private MessengerCenter messengerCenter;
 	
 
   	/****************************************************
 	 * CONSTRUCTORS
 	 ****************************************************/
 	/**
 	 * Constructs a new TedMainDialog
 	 * @param userWantsTray Flag if the user wants ted to add a trayicon
 	 */
 	public TedMainDialog(boolean userWantsTray, boolean saveInLocalDir) 
 	{
 		//super();
 		this.osHasTray = this.osHasTray && userWantsTray;
 		
 		// set if user wants to save / read files from local dir instead of users dir
 		TedSystemInfo.setSaveInLocalDir(saveInLocalDir);
 		
 		// check if the java version is correct
 		if (TedSystemInfo.isSupportedJava())
 		{
 			initGUI();
 		}
 		else
 		{
 			// show dialog that asks user to download latest javaversion
 			JOptionPane.showMessageDialog(null, Lang.getString("TedMainDialog.DialogJavaVersion1") + " (" + TedSystemInfo.getJavaVersion() + ") "+ Lang.getString("TedMainDialog.DialogJavaVersion2") + " \n" + //$NON-NLS-1$
 					Lang.getString("TedMainDialog.DialogJavaVersion3") + " " + TedSystemInfo.MINIMUM_JAVA + ".\n" +
 					Lang.getString("TedMainDialog.DialogJavaVersion4"));
 		}
 	}
 	
 	
 	/****************************************************
 	 * LOCAL METHODS
 	 ****************************************************/
 	private void initGUI() 
 	{
 		uiInitialized = false;
 		// set look and feel to system default
 		try
 		{
 			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
 		} catch (Exception e1)
 		{
 			TedLog.error(e1, Lang.getString("TedMainDialog.ErrorLookAndFeel")); //$NON-NLS-1$
 			SwingUtilities.updateComponentTreeUI( this );
 		}
 		
 		// load config file
 		TedIO tcio = new TedIO();
 		
 		try 
 		{
 			tcio.GetConfig();
 		} 
 		catch (FileNotFoundException e) 
 		{
 			// if no config: generate one and show config dialog
 			Lang.setLanguage(TedConfig.getLocale());
 			// config file is not found, ask user to input preferences
 			JOptionPane.showMessageDialog(null, Lang.getString("TedMainDialog.DialogStartup1") + "\n" + //$NON-NLS-1$
 					Lang.getString("TedMainDialog.DialogStartup2") + "\n" +  //$NON-NLS-1$
 					Lang.getString("TedMainDialog.DialogStartup3")); //$NON-NLS-1$
 			
 			// set initial size of maindialog
 			this.setSize(350, 500);
 			
 			// Get the screen size
 		    Toolkit toolkit = Toolkit.getDefaultToolkit();
 		    Dimension screenSize = toolkit.getScreenSize();
 			int x = (screenSize.width - this.getWidth()) / 2;
 		    int y = (screenSize.height - this.getHeight()) / 2;
 
 		    //Set the new frame location
 		    this.setLocation(x, y);
 			
 			new TedConfigDialog(this, false, true);
 		}
 		
 		Lang.setLanguage(TedConfig.getLocale());
 			
 		
 		
 		try 
 		{
 			// main layout
 			BoxLayout thisLayout = new BoxLayout(
 				this.getContentPane(),
 				javax.swing.BoxLayout.Y_AXIS);
 			this.getContentPane().setLayout(thisLayout);
 
 			// init menubar	
 			tMenuBar = new TedMainMenuBar(this);
 			setJMenuBar(tMenuBar);
 			
 			// fill panel on window
 			jPanel1 = new JPanel();
 			this.getContentPane().add(jPanel1);
 			jPanel1.setMaximumSize(new java.awt.Dimension(32767, 20));
 			
 
 			// add toolbar to panel
 			TedToolBar = new TedMainToolBar(this);
 			jPanel1.add(TedToolBar);
 			//jPanel1.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
 			
 			// add scrollpane to panel
 			jScrollPane1 = new JScrollPane();
 			this.getContentPane().add(jScrollPane1);
 			
 			//	add context menu for table
 			this.ttPopupMenu = new TedTablePopupMenu(this);
 			this.getContentPane().add(ttPopupMenu);
 			
 			// add table to scrollpanel
 			serieTable = new TedTable(this, ttPopupMenu);
 			jScrollPane1.setViewportView(serieTable);		
 			
 			// status bar
 			jStatusPanel = new JPanel();
 			GridLayout jStatusPanelLayout = new GridLayout(1, 1);
 			jStatusPanelLayout.setColumns(3);
 			jStatusPanelLayout.setHgap(5);
 			jStatusPanelLayout.setVgap(5);
 			jStatusPanel.setFont(this.SMALL_FONT);
 			jStatusPanel.setLayout(jStatusPanelLayout);
 			getContentPane().add(jStatusPanel);
 			jStatusPanel.setPreferredSize(new java.awt.Dimension(455, 18));
 			jStatusPanel.setMaximumSize(new java.awt.Dimension(32767, 18));
 
 			label_count = new JLabel();
 			jStatusPanel.add(label_count);
 			FlowLayout label_countLayout = new FlowLayout();
 			label_count.setLayout(label_countLayout);
 			label_count.setFont(this.SMALL_FONT);
 			label_count.setBounds(10, 0, 189, 14);
 			label_count.setPreferredSize(new java.awt.Dimension(424, 17));		
 			
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 		
 		this.setStatusString(Lang.getString("TedMain.LoadingConfig"));
 		
 		// register for mac os quit, preferences and about dialog items in menu
 		if (TedSystemInfo.osIsMac())
 		{
 			new TedMainMacListener(this);				
 		}		
 		
 		this.addWindowListener(new WindowAdapter() 
 			{
 				public void windowClosing(WindowEvent evt) 
 				{
 					tLog.setVisible(false);
 					rootWindowClosing(evt);
 				}
 				public void windowIconified(WindowEvent evt)
 				{
 					tLog.setVisible(false);
 					rootWindowIconified(evt);
 				}
 				public void windowDeiconified(WindowEvent evt)
 				{
 					if(!tLog.getIsClosed())
 					{
 						tLog.setVisible(true);
 					}
 					
 					rootWindowDeiconified(evt);
 				}
 			}
 		);
 		
 		// Save the window settings after resize.
 		this.addComponentListener(new ComponentAdapter()
 			{
 				public void componentResized(ComponentEvent evt)
 				{
 					saveConfig(false);
 				}
 			}
 		);
 		
 		tLog = TedLogDialog.getInstance();
 		TedLog.debug(Lang.getString("TedMainDialog.LogTedStarted")); //$NON-NLS-1$
 		
 		tParseHandler = new TedParseHandler(this);
 		
 		// add title and icon to window
 		this.setTitle(Lang.getString("TedMainDialog.WindowTitle")); //$NON-NLS-1$
 		this.setIconImage(tedProgramIcon.getImage());
 		
 		// load the config files
 		serieTable.setSeries(tcio.GetShows());
 		
 		// set size and position of ted
 		this.setSize(TedConfig.getWidth(), TedConfig.getHeight());
 		this.setLocation(TedConfig.getX(), TedConfig.getY());
 		this.setMinimumSize(new java.awt.Dimension(320, 320));
 		
 		// only if the os is supported by the trayicon program
 		// currently supports windows, linux and solaris		
 		this.osHasTray = this.osHasTray && TedConfig.isAddSysTray();
 		
 		if (osHasTray)
 		{
 			try
 			{
 				tedTray = new TedTrayIcon(this, tedIdleIcon);
 				
 			}
 			catch (Exception e)
 			{
 				TedLog.error(e, "Error while adding tray icon. Disabling tray in config");
 				this.osHasTray = false;
 				TedConfig.setAddSysTray(false);
 				TedConfig.setStartMinimized(false);
 			}
 		}
 		if (TedConfig.isStartMinimized())
 		{
 			if (this.osHasTray)
 			{
 				this.setVisible(false);
 			}
 			else
 			{
 				this.setVisible(true);
 				this.toBack();
 			}
 		}
 		else
 		{
 			this.setVisible(true);
 		}
 		
 		tCounter = new TedCounter(this);
 	
 		// reset all previous saved statusinformation of all shows
 		this.resetStatusOfAllShows(true);
 		
 		// set buttons according to selected row	
 		this.updateButtonsAndMenu();
 		
 		this.messengerCenter = new MessengerCenter(this);
 		
 		// if the shows.xml file does not exist download it
 		File f = new File(TedIO.XML_SHOWS_FILE); //$NON-NLS-1$
 		TedIO tio = new TedIO();
 		if(!f.exists())
 		{
 			this.setStatusString(Lang.getString("TedMain.CheckingNewShows"));
 			tio.downloadXML(this, TedConfig.getTimeOutInSecs(), -1);
 		}
 		// check to see if there is a new shows.xml file available
 		else if (TedConfig.isAutoUpdateFeedList() || TedConfig.askAutoUpdateFeedList())
 		{
 			this.setStatusString(Lang.getString("TedMain.CheckingNewShows"));
 			tio.checkNewXMLFile(this, false, serieTable);
 		}
 		
 		// Check if the file is now actually present on the user's system.
		if(tio.checkForShowsXML())
		{
			tio.UpdateShow(this, true, this.serieTable);
		}
 		
 		// start the counter
 		tCounter.start();
 		
 		if (TedConfig.isCheckVersion())
 		{
 			this.isNewTed(false);
 		}
 		
 		uiInitialized = true;
 	}
 	
 	/**
 	 * Update all GUI elements
 	 * Mostly called when the locale of ted is changed
 	 */
 	public void updateGUI()
 	{
 		// only if UI is initialized
 		if (uiInitialized)
 		{
 			Lang.setLanguage(TedConfig.getLocale());
 			
 			this.TedToolBar.updateText();
 			this.serieTable.updateText();
 			this.ttPopupMenu.updateText();
 			tMenuBar.updateText();
 			
 			if (!this.isParsing)
 			{
 				this.resetStatusOfAllShows(true);
 			}
 			
 			if (tedTray != null)
 			{
 				this.tedTray.updateText();
 			}
 			
 			this.repaint();
 		}
 	}
 	
 	/**
 	 * Updates the buttons and menu of ted according to if something is selected
 	 * in the serie table
 	 */
 	public void updateButtonsAndMenu()
 	{
 		int row;
 		boolean statusDelete = false;
 		boolean statusEdit = false;
 		row = this.serieTable.getSelectedRow();
 		if (row < 0)
 		{
 			statusDelete = false;
 			statusEdit = false;
 			this.tMenuBar.setNothingSelected();
 		}
 		else if (!this.isParsing)
 		{
 			statusEdit = true;
 			statusDelete = true;
 			this.tMenuBar.setSomethingSelected();
 		}
 		else
 		{
 			statusEdit = true;
 			statusDelete = false;
 		}
 		
 		this.TedToolBar.setEditButtonStatus(statusEdit);
 		this.TedToolBar.setDeleteButtonStatus(statusDelete);
 	}
 
 	private void rootWindowClosing(WindowEvent evt) 
 	{		
 		// if user has tray to minimize to
 		if (this.osHasTray)
 		{
 			this.setVisible(false);
 		}
 		// else we shut down ted
 		else
 		{
 			this.quit();
 		}
 	}
 	
 	private void rootWindowIconified(WindowEvent evt)
 	{
 		
 	}
 	
 	private void rootWindowDeiconified(WindowEvent evt)
 	{
 	}
 		
 	/**
 	 * Set the current trayicon of ted
 	 * @param icon Icon to be set
 	 */
 	private void setIcon(ImageIcon icon) 
 	{
 		if (osHasTray)
 		{
 			tedTray.setIcon(icon);
 		}	
 	}
 	
 	/****************************************************
 	 * PUBLIC METHODS
 	 ****************************************************/
 	
 	/**
 	 * Adds a show to the table
 	 * @param currentSerie Show to add
 	 */
 	public void addSerie(TedSerie newSerie) 
 	{
 		serieTable.addSerie(newSerie);
 		
 		// if it is the day to start checking the serie again
 		newSerie.updateShowStatus();
 		
 		// if the serie is not paused
 		if(newSerie.isCheck())
 		{
 			
 			// parse new show
 			TedParseHandler handler = new TedParseHandler(newSerie, this);
 			handler.start();
 		}
 	}
 		
 	/**
 	 * Parses all shows listed in the table of ted
 	 */
 	public void parseShows() 
 	{
 		if(!isParsing)
 		{
 			// first check if ted is up to date
 			if(TedConfig.getTimesParsedSinceLastCheck() == 5)
 			{
 				if(TedConfig.isCheckVersion())
 				{
 					isNewTed(false);
 				}
 				
 				isNewPredefinedShowsXML(false);
 				TedConfig.setTimesParsedSinceLastCheck(0);
 			}
 			else
 			{
 				TedConfig.setTimesParsedSinceLastCheck(TedConfig.getTimesParsedSinceLastCheck()+1);
 			}
 				
 			tParseHandler = new TedParseHandler(this);
 					
 			int rows = serieTable.getRowCount();
 			for (int i = 0; i < rows ; i++)
 			{
 				TedSerie serie = serieTable.getSerieAt(i);
 				
 				tParseHandler.addParseThread(serie, false);
 			}
 	
 			tParseHandler.start();
 		}
 	}
 	
 	/**
 	 * Update the countertext in the mainwindow
 	 * @param count Number of minutes left to next parserrround
 	 */
 	public void updateCounter(int count) 
 	{
 		if (count == 0)
 		{
 			this.label_count.setText(Lang.getString("TedMainDialog.StatusBarChecking")); //$NON-NLS-1$
 		}
 		else if (count == 1)
 		{
 			this.label_count.setText(Lang.getString("TedMainDialog.StatusBarLessThan1Minute")); //$NON-NLS-1$
 		}
 		else
 		{
 			this.label_count.setText(Lang.getString("TedMainDialog.StatusBarNextCheckInStart") + " " + count + " " + Lang.getString("TedMainDialog.StatusBarNextCheckInEnd")); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		this.repaint();
 		
 	}
 	
 	/**
 	 * Alert the user of an error that happened while running ted
 	 * @param header Header of the errormessage
 	 * @param message Body of the errormessage
 	 * @param details Details that are posted in the logwindow only
 	 */
 	public void displayError(String header, String message, String details)
 	{
 		this.messengerCenter.displayError(header, message);
 		
 		TedLog.error(message+"\n"+details); //$NON-NLS-1$
 	}
 	
 	/**
 	 * Show the user that ted found an episode of a specific show
 	 * @param header Header of the message
 	 * @param message Body of the message
 	 * @param details Details that are posted to the logwindow only
 	 */
 	public void displayHurray(String header, String message, String details)
 	{
 		this.messengerCenter.displayHurray(header, message);
 		TedLog.debug(message+"\n"+details); //$NON-NLS-1$
 	}
 		
 	public void actionPerformed(ActionEvent e)
 	{
 		// handles all the events on the ted mainwindow
 		String action = e.getActionCommand();
 		
 		if(action.equals("Preferences...")) //$NON-NLS-1$
 		{
 			new TedConfigDialog(this, true, true);
 		}
 		else if(action.equals("Log")) //$NON-NLS-1$
 		{
 			// show log
 			tLog.setIsClosed(false);
 			tLog.setLines(tLog.getMaxLines()); //Log is created before reading the config
 			tLog.resetAction();
 			tLog.setVisible(true);
 		}
 		else if(action.equals("Delete")) //$NON-NLS-1$
 		{
 			serieTable.DeleteSelectedShow();
 		}
 		else if(action.equals("New")) //$NON-NLS-1$
 		{
 			AddShowDialog asd = new AddShowDialog(this);
 		}
 		else if(action.equals("Exit")) //$NON-NLS-1$
 		{
 			this.quit();
 		}
 		else if(action.equals("Edit")) //$NON-NLS-1$
 		{
 			// get the selected show and open a episode dialog for it
 			int pos = serieTable.getSelectedRow();
 			if (pos >= 0)
 			{
 				TedSerie selectedserie = serieTable.getSerieAt(pos);
 				new EditShowDialog(this, selectedserie, false);
 			}
 		}
 		else if (action.equals("parse selected")) //$NON-NLS-1$
 		{			
 			// parse only the selected show, regardles of the status it has
 			int pos = serieTable.getSelectedRow();
 			
 			if (pos >= 0)
 			{
 				TedSerie selectedserie = serieTable.getSerieAt(pos);
 				TedParseHandler handler = new TedParseHandler(selectedserie, this, true);
 				handler.start();
 			}
 			
 		}
 		else if (action.equals("setstatuscheck")) //$NON-NLS-1$
 		{
 			serieTable.setSelectedStatus(TedSerie.STATUS_CHECK);
 			this.saveShows();
 		}
 		else if (action.equals("setstatuspause")) //$NON-NLS-1$
 		{
 			serieTable.setSelectedStatus(TedSerie.STATUS_PAUSE);
 			this.saveShows();
 		}
 		else if (action.equals("setstatushold")) //$NON-NLS-1$
 		{
 			serieTable.setSelectedStatus(TedSerie.STATUS_HOLD);
 			this.saveShows();
 		}
 		else if (action.equals("setstatusdisabled"))
 		{
 			serieTable.setSelectedStatus(TedSerie.STATUS_DISABLED);
 			this.saveShows();
 		}
 		else if (action.equals("checkupdates")) //$NON-NLS-1$
 		{
 			this.isNewTed(true);
 		}
 		else if (action.equals("checkRSS")) //$NON-NLS-1$
 		{
 			this.isNewPredefinedShowsXML(true);
 		}
 		else if (action.equals("help")) //$NON-NLS-1$
 		{
 			// try to open the ted documentation website
 			try 
 			{
 				BrowserLauncher.openURL("http://www.ted.nu/documentation/"); //$NON-NLS-1$
 			} 
 			catch (IOException ep) 
 			{
 				// error launching ted website
 				// TODO: add error message
 				System.out.println(Lang.getString("TedMainDialog.LogErrorWebsite")); //$NON-NLS-1$
 				ep.printStackTrace();
 			}	
 		}
 		else if (action.equals("opensite")) //$NON-NLS-1$
 		{
 			// try to open the ted website
 			try 
 			{
 				BrowserLauncher.openURL("http://www.ted.nu/"); //$NON-NLS-1$
 			} 
 			catch (IOException ep) 
 			{
 				// error launching ted website
 				// TODO: add error message
 				System.out.println(Lang.getString("TedMainDialog.LogErrorWebsite")); //$NON-NLS-1$
 				ep.printStackTrace();
 			}			
 		}
 		else if (action.equals("Donate")) //$NON-NLS-1$
 		{
 			// try to open the ted website
 			try 
 			{
 				BrowserLauncher.openURL("http://www.ted.nu/donate.php"); //$NON-NLS-1$
 			} 
 			catch (IOException ep) 
 			{
 				// error launching ted website
 				// TODO: add error message
 				System.out.println(Lang.getString("TedMainDialog.LogErrorWebsite")); //$NON-NLS-1$
 				ep.printStackTrace();
 			}			
 		}
 		else if (action.equals("buydvd")) //$NON-NLS-1$
 		{
 			TedXMLParser parser = new TedXMLParser();
 			Element nl = parser.readXMLFromFile(TedIO.XML_SHOWS_FILE);
 			Vector locations = parser.getAmazonURLs(nl);
 			
 			if(locations.size()==3)
 			{
 				// try to open the amazon.com website
 				try 
 				{
 					int rows = serieTable.getRowCount();
 					if (rows > 0)
 					{
 						// loop through all the shows and put names in address
 						
 						String names = "";
 						for (int i = 0; i < rows ; i++)
 						{
 							String spacer = "|";
 							if (i == rows-1)
 							{
 								spacer = "";
 							}
 							TedSerie serie = serieTable.getSerieAt(i);
 							String name = URLEncoder.encode("\""+serie.getName()+"\""+spacer, "UTF-8");
 							names += name;
 						}
 						
 						BrowserLauncher.openURL(locations.get(0)+names+locations.get(1)); //$NON-NLS-1$
 						
 					}
 					else
 					{
 						BrowserLauncher.openURL((String)locations.get(2)); //$NON-NLS-1$
 					}
 				} 
 				catch (IOException ep) 
 				{
 					// error launching ted website
 					// TODO: add error message
 					System.out.println("Error opening amazon.com website"); //$NON-NLS-1$
 					ep.printStackTrace();
 				}			
 			}
 			else
 			{
 				TedLog.error("shows_clean.xml file is corrupt");
 			}
 		}
 		else if (action.equals("buyDVDselectedshow"))
 		{
 			// try to open the amazon.com website
 			
 			// get selected showname
 			TedSerie selectedSerie = serieTable.getSelectedShow();
 			
 			if (selectedSerie != null)
 			{
 				String name = selectedSerie.getName();
 				this.openBuyLink(name);	
 			}
 		}
 		
 		
 		else if(action.equals("PressAction")) //$NON-NLS-1$
 		{
 			this.setVisible(true);
 			this.toFront();
 			if(!tLog.getIsClosed())
 			{
 				tLog.setVisible(true);
 			}
 		}
 		else if(action.equals("Help")) //$NON-NLS-1$
 		{
 			// show logwindow
 			tLog.setVisible(true);
 		}
 		else if(action.equals("Parse")) //$NON-NLS-1$
 		{
 			// parse all shows
 			this.parseShows();
 			//this.tCounter.setCount(TedConfig.getRefreshTime());
 		}
 		else if(action.equals("stop parsing")) //$NON-NLS-1$
 		{
 			tParseHandler.stopParsing();
 			this.TedToolBar.setParseButtonStatus(false);
 			this.TedToolBar.setParseButtonText(Lang.getString("TedMainDialog.ButtonCheckShowsStopping"));
 			this.resetStatusOfAllShows(true);
 		}
 		else if(action.equals("About ted")) //$NON-NLS-1$
 		{
 			this.showAboutDialog();
 		}
 		else if(action.equals("Export")) //$NON-NLS-1$
 		{
 			Vector series = serieTable.getSeries();
 			new TedXMLWriter(series);
 		}
 		else if(action.equals("synchronize")) //$NON-NLS-1$
 		{
 			TedIO tio = new TedIO();
 			tio.UpdateShow(this, false, serieTable);
 			serieTable.fireTableDataChanged();
 		}
 		else if(action.equals("translate"))
 		{
 			TedTranslateDialog trans = new TedTranslateDialog();
 			trans.setVisible(true);
 		}
 		else if(action.equals("language"))
 		{
 			this.openTranslationLink();
 		}
 		else if(action.equals("toggleautoschedule"))
 		{
 			TedSerie selectedSerie = serieTable.getSelectedShow();
 			
 			if (selectedSerie != null)
 			{
 				selectedSerie.setUseAutoSchedule(!selectedSerie.isUseAutoSchedule());
 			}
 		}
 	}
 	
 	public void showAboutDialog() 
 	{
 		new TedAboutDialog(tedVersion);	
 	}
 	
 	public void showPreferencesDialog()
 	{
 		new TedConfigDialog(this, true,true);
 	}
 	
 	public void quit()
 	{
 		// close ted
 		this.saveShows();
 		this.saveConfig(false);
 		System.exit(0);
 	}	
 
 	/**
 	 * Saves the shows that are in displayed in the ted window
 	 */
 	public void saveShows() 
 	{
 		TedIO tcio = new TedIO();
 		tcio.SaveShows(serieTable.getSeries());	
 		this.repaint();
 	}
 	
 	/**
 	 * Save the configuration of ted
 	 * @param resetTime Parse interval
 	 */
 	public void saveConfig(boolean resetTime) 
 	{
 		TedIO tcio = new TedIO();
 		
 		// set the current width, heigth and position of the window
 		TedConfig.setWidth(this.getWidth());
 		TedConfig.setHeight(this.getHeight());
 		TedConfig.setX(this.getX());
 		TedConfig.setY(this.getY());
 		
 		// save
 		tcio.SaveConfig();
 		
 		this.updateGUI();
 		
 		// notify counter of refreshtime change
 		if (resetTime && tCounter.isAlive())
 		{
 			tCounter.updateRefreshTime();
 		}
 		
 	}	
 	
 	/**
 	 * Check if there is a new version of ted available
 	 * @param show Show the user if he has the current version
 	 */
 	public void isNewTed(boolean show)
 	{
 		// check the website if there is a new version availble
 		TedIO tio = new TedIO();
 		double currentVersion = tio.checkNewTed(TedMainDialog.tedVersion);
 		
 		if (currentVersion > TedMainDialog.tedVersion)
 		{
 			String message = Lang.getString("TedMainDialog.DialogNewVersion1Begin")+ " (" + currentVersion + ") "+ Lang.getString("TedMainDialog.DialogNewVersion1End") + //$NON-NLS-1$ //$NON-NLS-2$
             								"\n" + Lang.getString("TedMainDialog.DialogNewVersion2")+ " " +  TedMainDialog.tedVersion + "\n" +  //$NON-NLS-1$
             								Lang.getString("TedMainDialog.DialogNewVersion3");
 			String title = Lang.getString("TedMainDialog.DialogNewVersionHeader");
 			
 			int answer = TimedOptionPane.showTimedOptionPane(null, message, title, "", 30000, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, Lang.getYesNoLocale(), Lang.getYesNoLocale()[0]);
 			
 			if (answer == JOptionPane.YES_OPTION)
 			{	
 				// launch ted website in browser
 				try 
 				{
 					BrowserLauncher.openURL("http://www.ted.nu/download.php"); //$NON-NLS-1$
 				} 
 				catch (IOException e) 
 				{
 					// error launching ted website
 				}
 				return;
 			}
 		}
 		else if (show)
 		{
 			JOptionPane.showMessageDialog(null, Lang.getString("TedMainDialog.DialogLatestVersionBegin")
 					 + " ("+ TedMainDialog.tedVersion + ") " + 
 					 Lang.getString("TedMainDialog.DialogLatestVersionEnd")); //$NON-NLS-1$ //$NON-NLS-2$
 			return;
 		}
 	}
 	
 	public void isNewPredefinedShowsXML(boolean show)
 	{
 		TedIO tio = new TedIO();
 		tio.checkNewXMLFile(this, show, serieTable);
 	}
 	
 	/**
 	 *  Reset all statusmessages for all series in the window to the default
 	 * @param resetCheck Reset status messages of shows with status CHECK?
 	 */
 	private void resetStatusOfAllShows(Boolean resetCheck)
 	{
 		int rows = serieTable.getRowCount();
 		for (int i = 0; i < rows ; i++)
 		{
 			TedSerie serie = serieTable.getSerieAt(i);
 			// only reset the statusstring if the status of the show is not check
 			if (serie.isCheck())
 			{
 				serie.resetStatus(resetCheck);
 			}
 			else
 			{
 				serie.resetStatus(true);
 			}
 		}
 		this.repaint();		
 	}
 	
 		
 	/****************************************************
 	 * GETTERS & SETTERS
 	 ****************************************************/
 	
 	/**
 	 * Set the GUI elements to parsing, disable delete button and menuitem
 	 * and change parsing button to "stop parsing"
 	 */
 	public void setStatusToParsing()
 	{
 		this.isParsing = true;
 		this.setStopParsing(false);
 		
 		// set icon
 		this.setIcon(tedActiveIcon);
 		
 		// disable delete buttons and menu items
 		updateButtonsAndMenu();
 		
 		this.tMenuBar.setParsing();		
 		this.ttPopupMenu.setParsing();
 		this.TedToolBar.setParsing();
 	}
 	
 	/**
 	 * Set the status of the GUI to idle. So ted is not parsing
 	 */
 	public void setStatusToIdle()
 	{
 		this.isParsing = false;
 		
 		// set icon
 		this.setIcon(tedIdleIcon);
 		this.resetStatusOfAllShows(false);
 		
 		// enable buttons and menu items
 		this.tMenuBar.setIdle();
 		this.ttPopupMenu.setIdle();
 		this.TedToolBar.setIdle();
 		updateButtonsAndMenu();	
 	}
 	
 	/**
 	 * @return Wheter the user has clicked the stop-parsing button
 	 */
 	public boolean getStopParsing()
 	{
 		return this.stopParsing;
 	}
 	
 	public void setStopParsing(boolean b)
 	{
 		this.stopParsing = b;
 	}
 		
 	/**
 	 * Update the status bar with a new text
 	 * @param status
 	 */
 	public void setStatusString(String status)
 	{
 		this.label_count.setText(status);
 	}
 
 
 
 
 	/**
 	 * Open buy dvd website of amazon.com for specific show
 	 * @param name name of the show
 	 */
 	public void openBuyLink(String name) 
 	{
 		TedXMLParser parser = new TedXMLParser();
 		Element nl = parser.readXMLFromFile(TedIO.XML_SHOWS_FILE);
 		Vector locations = parser.getAmazonURLs(nl);
 		
 		if(locations.size()==3)
 		{
 			try 
 			{
 				// open search for dvds
 				name = URLEncoder.encode(name, "UTF-8");
 				
 				BrowserLauncher.openURL(locations.get(0)+name+locations.get(1)); //$NON-NLS-1$
 			} 
 			catch (Exception ep) 
 			{
 				// error launching ted website
 				// TODO: add error message
 				System.out.println(Lang.getString("TedLog.AmazonError")); //$NON-NLS-1$
 				ep.printStackTrace();
 			}	
 		}
 		else
 		{
 			TedLog.error(Lang.getString("TedLog.ShowListCorrupt"));
 		}
 		
 	}
 	
 	/**
 	 * Open website where the latest translations can be donwloaded
 	 * @param name name of the show
 	 */
 	public void openTranslationLink() 
 	{
 		try 
 		{
 			BrowserLauncher.openURL("http://www.ted.nu/wiki/index.php/Latest_translations"); //$NON-NLS-1$
 		} 
 		catch (Exception ep) 
 		{
 			// error launching ted website
 			System.out.println("Error while opening language website"); //$NON-NLS-1$
 		}	
 		
 	}	
 	
 	/** @return Whether the current OS has an active tray icon
 	  */
 	public boolean osHasTray()
 	{
 		return this.osHasTray;
 	}
 	
 	/**
 	 * @return The current tray icon
 	 */
 	public TedTrayIcon getTrayIcon()
 	{
 		return this.tedTray;
 	} 
 	
 	public TedTable getSerieTable()
 	{
 		return this.serieTable;
 	}
 	
 	public TedTablePopupMenu getPopupMenu()
 	{
 		return this.ttPopupMenu;
 	}
 
 
 	public void updateAllSeries() 
 	{
 		// update status of all series if table is available
 		if (this.getSerieTable() != null)
 		{
 			this.getSerieTable().updateAllSeries();
 			this.updateGUI();
 		}		
 	}
 }
