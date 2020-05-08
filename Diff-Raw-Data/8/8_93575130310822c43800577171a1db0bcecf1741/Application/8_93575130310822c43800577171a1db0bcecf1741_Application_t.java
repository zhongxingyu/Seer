 package mkpc.app;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.*;
 import java.util.*;
 import java.util.Timer;
 
 import javax.swing.*;
 
 import mkpc.comm.MKCommunication;
 import mkpc.comm.MKCommunicationDelegate;
 import mkpc.fligthcontrol.MKCameraControlWindow;
 import mkpc.log.LogSystem;
 import mkpc.maps.ActivityIndicator;
 import mkpc.maps.MKWaypointEditorWindow;
 import mkpc.maps.MapViewController;
 import mkpc.model3D.MKopter3DViewController;
 import mkpc.webcam.MKWebcamWindow;
 
 import org.jdesktop.application.SingleFrameApplication;
 
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
  * <br ><b>Projekt:</b><br>Fotolandschaft der FH-Erfurt mithilfe eines Mikrokopters
  * @author Kristof Friess
  * @version 0.1.0.0
  * 
  */
 public class Application extends SingleFrameApplication implements MKCommunicationDelegate
 {
 	private static Application sharedApplication;
     private JPanel topPanel;
     private JMenuBar topMenu;
     private JMenu mp_file;
     private JMenu mp_windows;
     private JMenuItem mp_about;
     private JMenuItem mp_exit;
     //private JMenuItem mp_bluetoothDevice;
     private JMenuItem mp_settings;
     private JMenuItem mp_helpSite;
     private JButton btn_makePhoto;
     private JButton btn_connectCopter;
     private JButton btn_automaticFly;
     private JButton btn_sendCommand;
     private MapViewController mapViewController;
     private JTextPane consolePane;
     private JScrollPane consoleScrollPane;
     private JCheckBoxMenuItem cbmp_console;
     private JCheckBoxMenuItem cbmp_3DViewWindow;
     private JCheckBoxMenuItem cbmp_webcamWindow;
     private JCheckBoxMenuItem cbmp_waypointEditor;
     private JMenu mp_help;
     private MKopter3DViewController viewController3D = null;
     
     // Settings
     private MKSettingsWindow settingsWindow;
     
     // WebCam
     private MKWebcamWindow mKWebcamWindow;
     private ActivityIndicator webcamStarted;
     
     // waypoint editor
     private MKWaypointEditorWindow waypointEditor;
     private JTextField txt_commandInput;
 
     // Serial Communication
     public MKCommunication serialComm = null;
     
     // button animation timer
     java.util.Timer buttonTimer = null;
     
     @Override
     protected void startup() 
     {
     	{
     		getMainFrame().addWindowFocusListener(new WindowFocusListener() {
     			public void windowLostFocus(WindowEvent evt) {
     				mainFrameWindowLostFocus(evt);
     			}
     			public void windowGainedFocus(WindowEvent evt) {
     				mainFrameWindowGainedFocus(evt);
     			}
     		});
     	}
     	// load settings
     	this.setUpLogSystem();
     	{
     		getMainFrame().addWindowListener(new WindowAdapter() {
     			public void windowClosing(WindowEvent evt) {
     				mainFrameWindowClosing(evt);
     			}
     		});
     	}

     	// setup gui
     	{
     		topMenu = new JMenuBar();
     		getMainFrame().setJMenuBar(getTopMenu());
     		getMainFrame().setSize(732, 560);
     		getMainFrame().setResizable(false);
     		{
     			mp_file = new JMenu();
     			topMenu.add(mp_file);
     			//mp_file.setName("mp_file");
     			mp_file.setText(LogSystem.getMessage("menuFile"));
     			mp_file.setName("mp_file");
     			{
     				mp_settings = new JMenuItem();
     				mp_file.add(mp_settings);
     				//mp_settings.setName("mp_settings");
     				mp_settings.setText(LogSystem.getMessage("menuItemSettings"));
     				mp_settings.setName("mp_settings");
     				mp_settings.addActionListener(new ActionListener() {
     					public void actionPerformed(ActionEvent evt) {
     						mp_settingsActionPerformed(evt);
     					}
     				});
     			}
     			/* Insert in the Future
     			{
     				mp_bluetoothDevice = new JMenuItem();
     				mp_file.add(mp_bluetoothDevice);
     				//mp_bluetoothDevice.setName("mp_bluetoothDevice");
     				mp_bluetoothDevice.setText(LogSystem.getMessage("menuItemBluetoothDevice"));
     				mp_bluetoothDevice.setName("mp_bluetoothDevice");
     				mp_bluetoothDevice.addActionListener(new ActionListener() {
     					public void actionPerformed(ActionEvent evt) {
     						mp_bluetoothDeviceActionPerformed(evt);
     					}
     				});
     			}
     			*/
     			{
     				mp_exit = new JMenuItem();
     				mp_file.add(mp_exit);
     				//mp_exit.setName("mp_exit");
     				mp_exit.setText(LogSystem.getMessage("menuItemExit"));
     				mp_exit.setName("mp_exit");
     				mp_exit.addActionListener(new ActionListener() {
     					public void actionPerformed(ActionEvent evt) {
     						mp_exitActionPerformed(evt);
     					}
     				});
     			}
     		}
     		{
     			mp_windows = new JMenu();
     			topMenu.add(mp_windows);
     			//mp_windows.setName("mp_windows");
     			mp_windows.setText(LogSystem.getMessage("menuWindows"));
     			{
     				cbmp_webcamWindow = new JCheckBoxMenuItem();
     				mp_windows.add(cbmp_webcamWindow);
     				//cbmp_webcamWindow.setName("cbmp_webcamWindow");
     				cbmp_webcamWindow.setText(LogSystem.getMessage("menuItemWebcamWindow"));
     				cbmp_webcamWindow.setName("cbmp_webcamWindow");
     				cbmp_webcamWindow.addActionListener(new ActionListener() {
     					public void actionPerformed(ActionEvent evt) {
     						cbmp_webcamWindowActionPerformed(evt);
     					}
     				});
     			}
     			{
     				cbmp_3DViewWindow = new JCheckBoxMenuItem();
     				mp_windows.add(cbmp_3DViewWindow);
     				cbmp_3DViewWindow.setName("cbmp_3DViewWindow");
     				cbmp_3DViewWindow.setText(LogSystem.getMessage("menuItem3DViewWindow"));
     				cbmp_3DViewWindow.addActionListener(new ActionListener() {
     					public void actionPerformed(ActionEvent evt) {
     						cbmp_3DViewWindowActionPerformed(evt);
     					}
     				});
     			}
     			{
     				cbmp_console = new JCheckBoxMenuItem();
     				mp_windows.add(cbmp_console);
     				cbmp_console.setName("cbmp_console");
     				cbmp_console.setText(LogSystem.getMessage("menuItemConsole"));
     				cbmp_console.setSelected(true);
     				cbmp_console.addActionListener(new ActionListener() 
     				{
     					public void actionPerformed(ActionEvent evt) 
     					{
     						cbmp_consoleActionPerformed(evt);
     					}
     				});
     			}
     			{
     				cbmp_waypointEditor = new JCheckBoxMenuItem();
     				mp_windows.add(cbmp_waypointEditor);
     				cbmp_waypointEditor.setName("cbmp_waypointEditor");
     				cbmp_waypointEditor.setText(LogSystem.getMessage("menuItemWaypointEditor"));
     				cbmp_waypointEditor.addActionListener(new ActionListener() {
     					public void actionPerformed(ActionEvent evt) {
     						cbmp_waypointEditorActionPerformed(evt);
     					}
     				});
     			}
     		}
     		{
     			mp_help = new JMenu();
     			topMenu.add(mp_help);
     			//mp_help.setName("mp_help");
     			mp_help.setText(LogSystem.getMessage("menuHelp"));
     			{
     				mp_helpSite = new JMenuItem();
     				mp_help.add(mp_helpSite);
     				//mp_helpSite.setName("mp_helpSite");
     				mp_helpSite.setText(LogSystem.getMessage("menuItemHelpSite"));
     			}
     			{
     				mp_about = new JMenuItem();
     				mp_help.add(mp_about);
     				//mp_about.setName("mp_about");
     				mp_about.setText(LogSystem.getMessage("menuItemAbout"));
     			}
     		}
     	}
         topPanel = new JPanel();
         topPanel.setSize(700, 500);
         topPanel.setName("topPanel");
         topPanel.setLayout(null);
         topPanel.setPreferredSize(new java.awt.Dimension(700, 500));
         {
         	consoleScrollPane = new JScrollPane();
         	topPanel.add(consoleScrollPane);
 			consoleScrollPane.setBounds(0, 361, 726, 150);
 			consoleScrollPane.setAutoscrolls(true);
 			{
 				consolePane = new JTextPane();
 				consoleScrollPane.setViewportView(consolePane);
 	        	consolePane.setName("consolePane");
 			}
         }
         {
         	webcamStarted = new ActivityIndicator(new Point(400,0));
         	topPanel.add(webcamStarted);
         }
         {
         	mapViewController = new MapViewController(new Rectangle(0, 0, 500, 360));
         	topPanel.add(mapViewController);
         	mapViewController.setBounds(0, 0, 500, 360);
         }
         {
         	btn_connectCopter = new JButton();
         	topPanel.add(btn_connectCopter);
         	btn_connectCopter.setBounds(510, 7, 206, 26);
         	btn_connectCopter.setName("btn_connectCopter");
        	btn_connectCopter.setText(LogSystem.getMessage("buttonCopterConnect"));
         	btn_connectCopter.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					btn_connectCopterActionPerformed(evt);
 				}
 			});
         }
         {
         	btn_automaticFly = new JButton();
         	topPanel.add(btn_automaticFly);
         	btn_automaticFly.setBounds(510, 33, 206, 26);
         	btn_automaticFly.setName("btn_automaticFly");
         	btn_automaticFly.setText(LogSystem.getMessage("buttonAutomaticFly"));
         }
         {
         	btn_makePhoto = new JButton();
         	topPanel.add(btn_makePhoto);
        	btn_makePhoto.setVisible(false);
         	btn_makePhoto.setBounds(510, 59, 206, 26);
         	btn_makePhoto.setName("btn_makePhoto");
         	btn_makePhoto.setText(LogSystem.getMessage("buttonMakePhoto"));
         	btn_makePhoto.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					btn_makePhotoActionPerformed(evt);
 				}
 			});
         }
         {
 			txt_commandInput = new JTextField();
 			topPanel.add(txt_commandInput);
 			txt_commandInput.setBounds(510, 85, 206, 20);
 			txt_commandInput.setName("txt_commandInput");
 		}
         {
         	btn_sendCommand = new JButton();
         	topPanel.add(btn_sendCommand);
         	btn_sendCommand.setBounds(510, 111, 206, 26);
         	btn_sendCommand.setName("btn_sendCommand");
         	btn_sendCommand.setText(LogSystem.getMessage("buttonSendCommand"));
         	btn_sendCommand.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					btn_sendCommandActionPerformed(evt);
 				}
 			});
         }
         show(topPanel);
         
         // set log textpane
         LogSystem.setTextPane(consolePane);
         
         // set shared application
         sharedApplication = this;
         
         // set user application settings
         MKSettings.loadSettingsFile("settings.xml");
         MKSettings.loadApplicationSettings();
         
         
         // test load cameraControll
         MKCameraControlWindow cameraControl = new MKCameraControlWindow();
         this.show(cameraControl);
     }
 
     public static Application sharedApplication()
     {
     	return sharedApplication;
     }
     
     /**
      * @return The TopMenu of the Application
      */
     public JMenuBar getTopMenu() {
     	return topMenu;
     }
     
     /**
      * @return The TopPanel of the Application
      */
     public JPanel getTopPanel() {
     	return topPanel;
     }
     
     /**
      * @return The WebcamItem of the Application
      */
     public JCheckBoxMenuItem getWebcamMenuItem()
     {
     	return cbmp_webcamWindow;
     }
     
     /**
      * @return the waypoint menu item of the application
      */
     public JCheckBoxMenuItem getWaypointMenuItem()
     {
     	return cbmp_waypointEditor;
     }
     
     public MKopter3DViewController getViewController3D()
     {
     	return viewController3D;
     }
     
     /**
      * LogSystem create log-files and write it into a console (TextPane)
      */
     public void setUpLogSystem()
     {
     	LogSystem.setLogPath("log");
     	LogSystem.addLog("Application did start!");
     }
     
     /**
      * @return the MapViewController of the Application
      */
     public MapViewController getMapViewController()
     {
     	return this.mapViewController;
     }
     
     public MKWebcamWindow getWebcamFrame()
     {
     	return mKWebcamWindow;
     }
     
     public MKWaypointEditorWindow getWaypointEditor()
     {
     	return waypointEditor;
     }
     
     /**
      * Show and Unshow the console
      * @param evt ActionEvent
      */
     public void cbmp_consoleActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("cbmp_console.actionPerformed, event="+evt);
     	if(cbmp_console.isSelected())
     	{
     		LogSystem.CLog("not selected");
     		mkpc.log.LogSystem.setTextPane(null);
     		Rectangle newFrame = topPanel.getBounds();
     		newFrame.height = newFrame.height + consolePane.getBounds().height;
     		Rectangle mainFrame = getMainFrame().getBounds();
     		mainFrame.height = mainFrame.height + consolePane.getBounds().height;
     		getMainFrame().setBounds(mainFrame);
     		topPanel.setBounds(newFrame);
     		topPanel.add(consoleScrollPane);
     	}
     	else
     	{	
     		LogSystem.CLog("Selected");
     		mkpc.log.LogSystem.setTextPane(consolePane);
     		Rectangle newFrame = topPanel.getBounds();
     		newFrame.height = newFrame.height - consolePane.getBounds().height;
     		topPanel.setBounds(newFrame);
     		topPanel.remove(consoleScrollPane);
     		
     		Rectangle mainFrame = getMainFrame().getBounds();
     		mainFrame.height = mainFrame.height - consolePane.getBounds().height;
     		getMainFrame().setBounds(mainFrame);
     	}
     }
     
     /**
      * Show and unshow the webcam window (JFrame)
      * @param evt
      */
     private void cbmp_webcamWindowActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("cbmp_webcamWindow.actionPerformed, event="+evt);
     	if(cbmp_webcamWindow.isSelected())
     	{
     		//webcamStarted.setVisible(true);
             //webcamStarted.startAnimation();
     		mKWebcamWindow = new MKWebcamWindow();
         	mKWebcamWindow.setVisible(true);
         	//webcamStarted.stopAnimation();
         	//webcamStarted.setVisible(false);
     	}
     	else
     	{
     		mKWebcamWindow.close();
     		mKWebcamWindow.setVisible(false);
     	}
     }
     
     /**
      * Load the waypoint editor to select waypoint style
      * or remove waypoints, change the order
      * @param evt
      */
     private void cbmp_waypointEditorActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("cbmp_waypointEditor.actionPerformed, event="+evt);
     	if(cbmp_waypointEditor.isSelected())
     	{
     		if(waypointEditor == null)
     		{
     			waypointEditor = new MKWaypointEditorWindow();
     		}
         	waypointEditor.setVisible(true);
     	}
     	else
     	{
     		waypointEditor.setVisible(false);
     	}
     }
     
     public void showWaypointEditor()
     {
     	if(waypointEditor == null)
 		{
 			waypointEditor = new MKWaypointEditorWindow();
 		}
     	waypointEditor.setVisible(true);
     	cbmp_waypointEditor.setSelected(true);
     }
     
     public void unshowWaypointEditor()
     {
     	waypointEditor.setVisible(false);
     	cbmp_waypointEditor.setSelected(false);
     }
     
     // close and save settings functions
     private void mainFrameWindowClosing(WindowEvent evt) 
     {
     	LogSystem.CLog("mainFrame.windowClosed, event="+evt);
     	MKSettings.saveApplicationSettings();
     	LogSystem.writeToLogFile("Application close currectly!");
     }
     
     private void mp_exitActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("mp_exit.actionPerformed, event="+evt);
     	MKSettings.saveApplicationSettings();
     	LogSystem.writeToLogFile("Application close currectly!");
     	System.exit(1);
     }
     
     /* Insert in the Future
     private void mp_bluetoothDeviceActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("mp_bluetoothDevice.actionPerformed, event="+evt);
     	BDeviceSearchWindow bluetoothDeviceSearchWindow = new BDeviceSearchWindow();
     	bluetoothDeviceSearchWindow.setLocationRelativeTo(getMainFrame());
     	bluetoothDeviceSearchWindow.setDelegate(getMainFrame());
 		getMainFrame().setEnabled(false);
 		bluetoothDeviceSearchWindow.setVisible(true);
     }
     */
     private void cbmp_3DViewWindowActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("cbmp_3DViewWindow.actionPerformed, event="+evt);
     	
     	if(cbmp_3DViewWindow.isSelected())
     	{
     		this.show3DViewController();
     	}
     	else
     	{
     		this.unshow3DViewController();
     	}
     }
     
     public void show3DViewController()
     {
     	Properties prop = System.getProperties( );
 		StringBuffer str = new StringBuffer(prop.getProperty("os.name"));
 		
 		if(str.indexOf("Windows") != -1)
 		{
 			//kopter3DView = new MKopter3DView();
         	//.add(kopter3DView);
         	//kopter3DView.setBounds(502, 177, 220, 180);
 			if(viewController3D == null)
 			{
 				viewController3D = new MKopter3DViewController();
 				topPanel.add(viewController3D);
 	        	viewController3D.setBounds(502, 177, 220, 180);
 			}
 
         	cbmp_3DViewWindow.setSelected(true);
         	viewController3D.setVisible(true);
         	viewController3D.start();
         	topPanel.revalidate();
 		}
 		else
 		{
 			LogSystem.addLog(LogSystem.getMessage("3DViewError001"));
 		}  
     }
     
     public void unshow3DViewController()
     {
     	viewController3D.stop();
     	viewController3D.setVisible(false);
     	cbmp_3DViewWindow.setSelected(false);
     	topPanel.revalidate();
     }
     
     private void mainFrameWindowLostFocus(WindowEvent evt) 
     {
     	LogSystem.CLog("mainFrame.windowLostFocus, event="+evt);
     	if(this.getMainFrame().getState() == JFrame.ICONIFIED)
     	{
     		if(waypointEditor != null) waypointEditor.setState(JFrame.ICONIFIED);
     	}
     }
     
     private void mainFrameWindowGainedFocus(WindowEvent evt) 
     {
     	LogSystem.CLog("mainFrame.windowGainedFocus, event="+evt);
     	
     	if(this.getMainFrame().getState() == JFrame.NORMAL)
     	{
     		if(waypointEditor != null) waypointEditor.setState(JFrame.NORMAL);
     	}
     }
     
     private void mp_settingsActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("mp_settings.actionPerformed, event="+evt);
     	if(this.settingsWindow == null)
     	{
     		this.settingsWindow = new MKSettingsWindow();
     		settingsWindow.setLocationRelativeTo(getMainFrame());
     		this.show(this.settingsWindow);
     	}
     }
     
     
     
     private void btn_sendCommandActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("btn_sendCommand.actionPerformed, event="+evt);
     	if( serialComm != null && serialComm.isPortOpen())
     	{
     		serialComm.sendCommand(txt_commandInput.getText());
     		/*int[] values = new int[16];
     		for ( int i = 0; i < 16; ++i)
     		{
     			if(i==0)
     				values[i] = 1;
     			else
     				values[i] = 0;
     		}
     		serialComm.sendCommand('b', 't', values); 
     		// #bt=M======================X>
     		 * */
     	}
     	else
     	{
     		LogSystem.addLog("Please open connection at first.");
     	}
     }
     private void btn_makePhotoActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("btn_makePhoto.actionPerformed, event="+evt);
     	if( serialComm != null && serialComm.isPortOpen())
     	{
     		btn_makePhoto.setText(LogSystem.getMessage("buttonMakePhotoInProgress"));
     		
     		int[] values = new int[12];
     		for ( int i = 0; i < 12; ++i)
     		{
     			if(i==0)
     				values[i] = 125;
     			else
     				values[i] = 0;
     		}
     		serialComm.sendCommand('b', 'y', values); 
     		new Thread(new Runnable()
     		{
 				public void run() 
 				{
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					int[] values = new int[12];
 		    		for ( int i = 0; i < 12; ++i)
 		    		{
 		    				values[i] = -125;
 		    		}
 		    		serialComm.sendCommand('b', 'y', values); 
 					try {
 						Thread.sleep(4000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					btn_makePhoto.setEnabled(true);
 					btn_makePhoto.setText(LogSystem.getMessage("buttonMakePhoto"));
 				}
 			}).start();
     	}
     	else
     	{
     		LogSystem.addLog("Please open connection at first.");
     	}
     }
 
 //>> Copter connection Button, Delegate calls, and button animation
     private void btn_connectCopterActionPerformed(ActionEvent evt) 
     {
     	LogSystem.CLog("btn_connectCopter.actionPerformed, event="+evt);
     	if( serialComm == null )
     	{
     		serialComm = new MKCommunication();
     	}
     	
     	if(!serialComm.isPortOpen())
     	{
     		if(buttonTimer == null)
     		{
     			buttonTimer = new java.util.Timer();
     		}
     		
     		btn_connectCopter.setEnabled(false);
     		buttonTimer.schedule(new MKCopterConnectButtonAnimation(), 10, 200);
     		if(MKSettings.hasValueForKey("comPort"))
     		{
     			serialComm.openComPort(MKSettings.getValueForKey("comPort"), this);
     		}
     		else
     		{
     			serialComm.openComPort("COM1", this);
     		}
     	}
     }
     
 	@Override
 	public void communicationDidOpenCOMPort() 
 	{
 		LogSystem.addLog("COMPort has open.");
 	}
 
 	@Override
 	public void communicationDidFailOpenCOMPort(String error) 
 	{
 		LogSystem.addLog(error);
 		buttonTimer.cancel();
 		buttonTimer = null;
 		btn_connectCopter.setEnabled(true);
 		btn_connectCopter.setText("btnConnectCopter");
 	}
 
 	@Override
 	public void communicationDidOpenCopterConnection() 
 	{
 		LogSystem.addLog("Copter connected!");
 		buttonTimer.cancel();
 		buttonTimer = null;
 		btn_connectCopter.setEnabled(true);
 		btn_connectCopter.setText("btnCopterConnected");
 	}
 
 	@Override
 	public void communicationDidFailOpenCopterConnection(String error) 
 	{
 		LogSystem.addLog(error);
 		buttonTimer.cancel();
 		buttonTimer = null;
 		btn_connectCopter.setEnabled(true);
 		btn_connectCopter.setText("btnConnectCopter");
 	}
 	
 	class MKCopterConnectButtonAnimation extends java.util.TimerTask
 	{
 		int position;
 		@Override
 		public void run() 
 		{
 			String dots = "";
 			for (int i = 0; i < position; ++i)
 			{
 				dots = dots + ".";
 			}
 			++position;
 			if(position > 6)
 				position = 0;
 			
 			btn_connectCopter.setText("Search "+dots);
 		}
 		
 	}
 }
