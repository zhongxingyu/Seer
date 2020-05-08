 package nz.ac.auckland.netlogin.gui;
 
 import nz.ac.auckland.netlogin.NetLoginConnection;
 import nz.ac.auckland.netlogin.NetLoginPlan;
 import nz.ac.auckland.netlogin.NetLoginPreferences;
 import nz.ac.auckland.netlogin.PingListener;
 import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
 import nz.ac.auckland.netlogin.negotiation.PopulatedCredentialsCallback;
 import nz.ac.auckland.netlogin.util.SystemSettings;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.MenuDetectEvent;
 import org.eclipse.swt.events.MenuDetectListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import java.beans.EventHandler;
 import java.lang.reflect.Method;
 
 public class NetLoginGUI implements PingListener {
 
 	private Display display;
 	private Shell window;
 
 	private Composite disconnectedPanel;
 	private Composite connectingPanel;
 	private Composite connectedPanel;
 
 	private Label userLabel;
 	private Label planLabel;
 	private Label usageLabel;
 	private Label connectionErrorMessage;
 
 	private NetLoginPreferences preferences;
 
 	private Button connectButton;
 	private MenuItem loginMenuItem;
 	private MenuItem logoutMenuItem;
 
 	private LoginDialog loginDialog;
 	private AboutDialog aboutDialog;
 	private PreferencesDialog preferencesDialog;
 
 	private NetLoginConnection netLoginConnection;
 	private boolean connected = false;
 
 	private boolean useSystemTray = true;
 	private TrayItem trayIcon;
 
 	static String helpURL = "http://www.ec.auckland.ac.nz/docs/net-student.htm";
 	static String passwdChangeURL = "https://iam.auckland.ac.nz/password/change";
 
 	private Composite bodyPanel;
 	private StackLayout bodyPanelLayout;
 
 	public NetLoginGUI() {
 		initialize();
 		openWindow();
 		netLoginConnection.monitor();
 		run();
 	}
 
 	public NetLoginGUI(String upi, String password) {
 		initialize();
 		openWindow();
 		login(new PopulatedCredentialsCallback(upi, password));
 		minimizeWindow();
 		netLoginConnection.monitor();
 		run();
 	}
 
 	private void initialize() {
 		preferences = NetLoginPreferences.getInstance();
 		netLoginConnection = new NetLoginConnection(this);
 
 		loadLookAndFeel();
 		createWindow();
 		createDialogs();
 		createMenuBar();
 		initBody();
 		initTrayIcon();
 	}
 
 	private void createDialogs() {
 		loginDialog = new LoginDialog(window);
 		preferencesDialog = new PreferencesDialog(window, preferences);
 		aboutDialog = new AboutDialog(window);
 	}
 
 	private void loadLookAndFeel() {
 		SystemSettings.setSystemPropertyDefault("apple.laf.useScreenMenuBar", "true");
 		SystemSettings.setSystemPropertyDefault("com.apple.macos.useScreenMenuBar", "true"); // historical
 		SystemSettings.setSystemPropertyDefault("com.apple.mrj.application.apple.menu.about.name", "NetLogin");
 		SystemSettings.setSystemPropertyDefault("com.apple.mrj.application.live-resize", "true");
 	}
 
 	public void createWindow() {
 		display = new Display();
 
 		if (isSystemTraySupported()) {
 			// create an invisible parent window, this hides the task bar
 			Shell windowWrapper = new Shell();
 			window = new Shell(windowWrapper, SWT.CLOSE | SWT.TITLE);
 		} else {
         	window = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.MIN);
 		}
 
 		window.setText("NetLogin");
 
 		// if we're using the system tray, then close actually hides it
 		if (isSystemTraySupported()) {
 			window.addShellListener(new ShellAdapter() {
 				public void shellClosed(ShellEvent e) {
 					e.doit = false;
 					minimizeWindow();
 				}
 			});
 		}
 
 		window.setSize(270, 160);
 		window.setImages(Icons.getInstance().getWindowIcons());
 	}
 
 	private void run() {
 		while (!window.isDisposed()) {
 			if (!display.readAndDispatch()) display.sleep();
 		}
 		display.dispose();
 	}
 	
 	public void login() {
 		login(loginDialog);
 		netLoginConnection.monitor();
 	}
 
 	protected void login(CredentialsCallback callback) {
 		netLoginConnection.login(callback);
 	}
 
 	public boolean isSystemTraySupported() {
 		return useSystemTray && display.getSystemTray() != null;
 	}
 	
 	public void openWindow() {
 		window.setMinimized(false);
 		window.open();
 		window.forceActive();
 	}
 	
 	public void minimizeWindow() {
 		if (isSystemTraySupported()) {
 			window.setVisible(false);
 		} else {
 			window.setMinimized(true);
 		}
 	}
 
 	public void initBody() {
 		window.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
 		window.setLayout(SWTHelper.createMinimalGridLayout());
 
 		bodyPanel = new Composite(window, SWT.EMBEDDED);
 		bodyPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		bodyPanelLayout = new StackLayout();
 		bodyPanel.setLayout(bodyPanelLayout);
 
 		disconnectedPanel = new Composite(bodyPanel, SWT.EMBEDDED);
 		disconnectedPanel.setLayout(new GridLayout());
 		Label disconnectedLabel = SWTHelper.createStrongLabel(disconnectedPanel, "Not Connected");
 		disconnectedLabel.setAlignment(SWT.CENTER);
 		disconnectedLabel.setLayoutData(new GridData(GridData.FILL, GridData.END, true, true));
 		connectionErrorMessage = new Label(disconnectedPanel, SWT.NONE);
 		connectionErrorMessage.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
 		connectionErrorMessage.setAlignment(SWT.CENTER);
 
 		connectingPanel = new Composite(bodyPanel, SWT.EMBEDDED);
 		connectingPanel.setLayout(new GridLayout());
 		Label connectingLabel = SWTHelper.createStrongLabel(connectingPanel, "Connecting...");
 		connectingLabel.setAlignment(SWT.CENTER);
 		connectingLabel.setLayoutData(new GridData(GridData.FILL, GridData.END, true, true));
 		ProgressBar connectingProgress = new ProgressBar(connectingPanel, SWT.HORIZONTAL | SWT.INDETERMINATE);
 		connectingProgress.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, false, true));
 
 		connectedPanel = SWTHelper.createForm(bodyPanel);
 		((GridLayout)connectedPanel.getLayout()).makeColumnsEqualWidth = true;
 		SWTHelper.createFormLabel(connectedPanel, "NetID/UPI:");
 		userLabel = SWTHelper.createStrongLabel(connectedPanel, "");
 		userLabel.setLayoutData(SWTHelper.formLayoutData());
 		SWTHelper.createFormLabel(connectedPanel, "Internet Plan:");
 		planLabel = SWTHelper.createStrongLabel(connectedPanel, "");
 		planLabel.setLayoutData(SWTHelper.formLayoutData());
 		SWTHelper.createFormLabel(connectedPanel, "Used this month:");
 		usageLabel = SWTHelper.createStrongLabel(connectedPanel, "");
 		usageLabel.setLayoutData(SWTHelper.formLayoutData());
 
 		selectBodyPanel(disconnectedPanel);
 
 		Composite buttonPanel = SWTHelper.createButtonPanel(window);
		connectButton = SWTHelper.createButton(buttonPanel, "Disconnect");
 		connectButton.setToolTipText("Login to NetAccount");
        connectButton.pack();
        connectButton.setText("Connect");
 
 		connectButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!connected) {
 					login();
                 } else {
 					disconnect();
                 }
 			}
 		});
 	}
 
 	private void selectBodyPanel(Composite panel) {
 		assert disconnectedPanel == panel || connectingPanel == panel || connectedPanel == panel;
 		bodyPanelLayout.topControl = panel;
 		bodyPanel.layout();
 	}
 
 	public void disconnect() {
 		netLoginConnection.unmonitor();
 		netLoginConnection.logout();
 	}
 
 	public void connecting() {
 		display.asyncExec(new Runnable() {
 			public void run() {
 				connectButton.setEnabled(false);
 				connectionErrorMessage.setText("");
 				selectBodyPanel(connectingPanel);
 			}
 		});
 	}
 
 	public void connectionFailed(final String message) {
 		display.asyncExec(new Runnable() {
 			public void run() {
 				connectButton.setEnabled(true);
 				selectBodyPanel(disconnectedPanel);
 				if (message != null) connectionErrorMessage.setText(message);
 			}
 		});
 	}
 
 	public void connected(final String username, int ipUsage, NetLoginPlan plan) {
 		this.connected = true;
 		update(ipUsage, plan);
 		display.asyncExec(new Runnable() {
 			public void run() {
 				userLabel.setText(username);
 				selectBodyPanel(connectedPanel);
 
 				connectButton.setToolTipText("Disconnect from NetAccount");
 				connectButton.setText("Disconnect");
 				connectButton.setEnabled(true);
 
 				loginMenuItem.setEnabled(false);
 				logoutMenuItem.setEnabled(true);
 				updateTrayLabel();
 			}
 		});
 	}
 
 	public void disconnected() {
 		this.connected = false;
 		display.asyncExec(new Runnable() {
 			public void run() {
 				connectButton.setText("Connect");

 				selectBodyPanel(disconnectedPanel);
 
 				loginMenuItem.setEnabled(true);
 				logoutMenuItem.setEnabled(false);
 				updateTrayLabel();
 			}
 		});
 	}
 
 	public void update(final int ipUsage, final NetLoginPlan plan) {
 		display.asyncExec(new Runnable() {
 			public void run() {
 				float ipUsageMb = (float) (Math.round((ipUsage / 1024.0) * 100)) / 100;
 				usageLabel.setText("" + ipUsageMb + " MBs");
 				planLabel.setText(plan.toString());
 			}
 		});
 	}
 
 	private void createMenuBar() {
 		Menu menuBar = new Menu(window, SWT.BAR);
 		window.setMenuBar(menuBar);
 
 		Menu netLoginMenu = SWTHelper.createMenu(window, menuBar, "NetLogin", 'n');
 		loginMenuItem = SWTHelper.createMenuItem(netLoginMenu, "Login", 'l');
 		logoutMenuItem = SWTHelper.createMenuItem(netLoginMenu, "Logout", 'l');
 		logoutMenuItem.setEnabled(false);
 		SWTHelper.addMenuSeparator(netLoginMenu);
 		MenuItem preferencesMenuItem = SWTHelper.createMenuItem(netLoginMenu, "Preferences", 'r');
 		SWTHelper.addMenuSeparator(netLoginMenu);
 		MenuItem quitMenuItem = SWTHelper.createMenuItem(netLoginMenu, "Exit", 'x');
 
 		Menu servicesMenu = SWTHelper.createMenu(window, menuBar, "Services", 's');
 		MenuItem changePassMenuItem = SWTHelper.createMenuItem(servicesMenu, "Change Password", 'p');
 		MenuItem chargeRatesMenuItem = SWTHelper.createMenuItem(servicesMenu, "Show Charge Rates", 'c');
 
 		Menu helpMenu = SWTHelper.createMenu(window, menuBar, "Help", 'h');
 		MenuItem aboutMenuItem = SWTHelper.createMenuItem(helpMenu, "About", 'a');
 
 		loginMenuItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "login"));
 		logoutMenuItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "disconnect"));
 		preferencesMenuItem.addSelectionListener(EventHandler.create(SelectionListener.class, preferencesDialog, "open"));
 		changePassMenuItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "changePassword"));
 		quitMenuItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "quit"));
 		aboutMenuItem.addSelectionListener(EventHandler.create(SelectionListener.class, aboutDialog, "open"));
 		chargeRatesMenuItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "showChargeRates"));
 	}
 
 	public void showChargeRates() {
 		openURL(helpURL);
 	}
 
 	public void changePassword() {
 		openURL(passwdChangeURL);
 	}
 
 	public void quit() {
 		System.exit(0);
 	}
 
 	public static void openURL(String url) {
 		String osName = System.getProperty("os.name");
 		try {
 			if (osName.startsWith("Mac")) {// Mac OS
 				Class fileMgr = Class.forName("com.apple.eio.FileManager");
 				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
 				openURL.invoke(null, url);
 			} else if (osName.startsWith("Windows")) {// Windows
 				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
 			} else { // Unix or Linux
 				String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
 				String browser = null;
 				for (int count = 0; count < browsers.length && browser == null; count++) {
 					if (Runtime.getRuntime().exec(
 							new String[] { "which", browsers[count] })
 							.waitFor() == 0) {
 						browser = browsers[count];
 					}
 				}
 				if (browser == null) {
 					throw new Exception("Could not find web browser");
 				} else {
 					Runtime.getRuntime().exec(new String[] { browser, url });
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void updateTrayLabel() {
 		if (trayIcon == null) return;
 
 		String label = "Disconnected";
 		if (connected) label = "Connected";
 		trayIcon.setToolTipText(label);
 	}
 
 	private void initTrayIcon() {
 		if (!isSystemTraySupported()) return;
 
 		final Menu popup = new Menu(window, SWT.POP_UP);
 		MenuItem passwordItem = SWTHelper.createMenuItem(popup, "Change Password", 'p');
 		MenuItem rateItem = SWTHelper.createMenuItem(popup, "Show Charge Rates", 'c');
 		SWTHelper.addMenuSeparator(popup);
 		MenuItem openItem = SWTHelper.createMenuItem(popup, "Open NetLogin", 'o');
 		MenuItem exitItem = SWTHelper.createMenuItem(popup, "Exit", 'x');
 
 		rateItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "showChargeRates"));
 		passwordItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "changePassword"));
 		openItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "openWindow"));
 		exitItem.addSelectionListener(EventHandler.create(SelectionListener.class, this, "quit"));
 
 		Image trayIconImage = Icons.getInstance().getClosestIcon(24);
 
 		Tray tray = display.getSystemTray();
 		trayIcon = new TrayItem(tray, SWT.NONE);
 		trayIcon.setToolTipText("NetLogin");
 		trayIcon.setImage(trayIconImage);
 
 		trayIcon.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				openWindow();
 			}
 		});
 
 		trayIcon.addMenuDetectListener(new MenuDetectListener() {
 			public void menuDetected(MenuDetectEvent e) {
 				popup.setVisible(true);
 			}
 		});
 	}
 
 }
