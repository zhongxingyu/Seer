 /*
  * (c) Copyright IBM Corp. 2000, 2002.
  * All Rights Reserved.
  */
 package org.eclipse.help.ui.internal.browser.win32;
 import java.io.*;
 import java.util.StringTokenizer;
 import org.eclipse.help.internal.ui.util.HelpWorkbenchException;
 import org.eclipse.help.internal.ui.win32.WebBrowser;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 /**
  * Application providing embeded Internet Explorer
  * The controlling commands are read from standard input
  * Commands and their parameters are separated using spaced
  * and should be provided one command per line.
  */
 public class IEHost implements Runnable {
 	public static final String SYS_PROPERTY_INSTALLURL = "installURL";
 	public static final String SYS_PROPERTY_STATELOCATION = "stateLocation";
 	public static final String CMD_CLOSE = "close";
 	public static final String CMD_DISPLAY_URL = "displayURL";
 	public static final String CMD_SET_LOCATION = "setLocation";
 	public static final String CMD_SET_SIZE = "setSize";
 	private static final String BROWSER_X = "browser.x";
 	private static final String BROWSER_Y = "browser.y";
 	private static final String BROWSER_WIDTH = "browser.w";
 	private static final String BROWSER_HEIGTH = "browser.h";
 	private static String installURL;
 	private static String stateLocation;
 	private Display display;
 	private Shell shell;
 	private WebBrowser webBrowser;
 	private IEResources ieResources;
 	private IEStore store;
 	boolean opened = false;
 	/**
 	 * Constructor
 	 */
 	private IEHost() {
 		display = new Display();
 		ieResources = new IEResources(installURL);
 		store = new IEStore(new File(stateLocation, ".iestore").toString());
 		store.restore();
 		createShell();
 		// Start command interpreter
 		Thread inputReader = new Thread(this);
 		inputReader.setDaemon(true);
 		inputReader.setName("IE Command Interpreter");
 		inputReader.start();
 	}
 	/**
 	 * Runs event loop for the display
 	 */
 	private void runUI() {
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 	}
 	/**
 	 * Entry point to the program
 	 * Command line arguments are not used.
 	 */
 	public static void main(String[] args) throws Throwable {
 		installURL = System.getProperty(SYS_PROPERTY_INSTALLURL);
 		if (installURL == null || installURL.length() <= 0) {
 			System.err.println("Property " + SYS_PROPERTY_INSTALLURL + " must be set.");
 			return;
 		}
 		stateLocation = System.getProperty(SYS_PROPERTY_STATELOCATION);
 		if (stateLocation == null || stateLocation.length() <= 0) {
 			System.err.println("Property " + SYS_PROPERTY_STATELOCATION + " must be set.");
 			return;
 		}
 		IEHost ie = new IEHost();
 		ie.runUI();
 	}
 	/**
 	 * Creates hosting shell.
 	 */
 	private void createShell() {
 		shell = new Shell();
 		shell.setImage(
 			ImageDescriptor
 				.createFromURL(ieResources.getImagePath("shellIcon"))
 				.createImage());
 		shell.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				Point location = shell.getLocation();
 				store.put(BROWSER_X, Integer.toString(location.x));
 				store.put(BROWSER_Y, Integer.toString(location.y));
 				Point size = shell.getSize();
 				store.put(BROWSER_WIDTH, Integer.toString(size.x));
 				store.put(BROWSER_HEIGTH, Integer.toString(size.y));
 				store.save();
 				shell.close();
 			}
 		});
 		shell.setText(ieResources.getString("browserTitle"));
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		shell.setLayout(layout);
 		createContents(shell);
 		// use saved location and size
 		int x = store.getInt(BROWSER_X);
 		int y = store.getInt(BROWSER_Y);
 		int w = store.getInt(BROWSER_WIDTH);
 		int h = store.getInt(BROWSER_HEIGTH);
 		if (w == 0 || h == 0) {
 			// use defaults
			w = 700;
			h = 500;
 		}
 		shell.setLocation(x, y);
 		shell.setSize(w, h);
 		shell.open();
 		opened = true;
 	}
 	/**
 	 * Populates shell control with toolbar and ActiveX IE.
 	 */
 	private Control createContents(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridData data = new GridData(GridData.FILL_BOTH);
 		composite.setLayoutData(data);
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		composite.setLayout(layout);
 		// Add a toolbar
 		ToolBar bar = new ToolBar(composite, SWT.FLAT);
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		//gridData.horizontalSpan = 3;
 		bar.setLayoutData(gridData);
 		ToolItem item;
 		// Add a button to navigate back
 		item = new ToolItem(bar, SWT.NONE);
 		item.setToolTipText(ieResources.getString("Previous_page"));
 		item.setImage(
 			ImageDescriptor
 				.createFromURL(ieResources.getImagePath("back_icon"))
 				.createImage());
 		item.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				webBrowser.back();
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		// Add a button to navigate forward
 		item = new ToolItem(bar, SWT.NONE);
 		item.setToolTipText(ieResources.getString("Next_page"));
 		item.setImage(
 			ImageDescriptor
 				.createFromURL(ieResources.getImagePath("forward_icon"))
 				.createImage());
 		item.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				webBrowser.forward();
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		// Add a button to print
 		item = new ToolItem(bar, SWT.NONE);
 		item.setToolTipText(ieResources.getString("Print_page"));
 		item.setImage(
 			ImageDescriptor
 				.createFromURL(ieResources.getImagePath("printer_icon"))
 				.createImage());
 		item.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				//webBrowser.print(true);
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		try {
 			webBrowser = new WebBrowser(composite);
 		} catch (HelpWorkbenchException hwe) {
 		}
 		return composite;
 	}
 	/**
 	 * Reads commands from standard input.
 	 */
 	public void run() {
 		BufferedReader reader = new BufferedReader((new InputStreamReader(System.in)));
 		// Run command loop
 		String line;
 		try {
 			while (null != (line = reader.readLine())) {
 				if (line.length() > 0) {
 					executeCommand(line);
 					while (System.in.available() <= 0) {
 						try {
 							Thread.currentThread().sleep(30);
 						} catch (InterruptedException ie) {
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			return;
 		}
 	}
 	/**
 	 * Command Inerpreter for commands.
 	 * Commands are passed on stdin one per line
 	 * Supported commands:
 	 * 	 close
 	 *   displayURL <url>
 	 *   setLocation <x> <y>
 	 *   setSize <width> <height>
 	 */
 	private void executeCommand(String line) {
 		StringTokenizer tokenizer = new StringTokenizer(line);
 		if (!tokenizer.hasMoreTokens())
 			return;
 		String command = tokenizer.nextToken();
 		String pars[] = new String[tokenizer.countTokens()];
 		for (int i = 0; i < pars.length; i++) {
 			pars[i] = tokenizer.nextToken();
 		}
 		if (CMD_DISPLAY_URL.equalsIgnoreCase(command)) {
 			if (pars.length >= 1 && pars[0] != null) {
 				display.syncExec(new DisplayURLCommand(pars[0]));
 				display.syncExec(new MakeVisible());
 			}
 		} else if (CMD_SET_LOCATION.equalsIgnoreCase(command)) {
 			if (pars.length >= 2 && pars[0] != null && pars[1] != null) {
 				try {
 					display.syncExec(
 						new SetLocationCommand(Integer.parseInt(pars[0]), Integer.parseInt(pars[1])));
 				} catch (NumberFormatException nfe) {
 				}
 			}
 		} else if (CMD_SET_SIZE.equalsIgnoreCase(command)) {
 			if (pars.length >= 2 && pars[0] != null && pars[1] != null) {
 				try {
 					display.syncExec(
 						new SetSizeCommand(Integer.parseInt(pars[0]), Integer.parseInt(pars[1])));
 				} catch (NumberFormatException nfe) {
 				}
 			}
 		} else if (CMD_CLOSE.equalsIgnoreCase(command)) {
 			display.syncExec(new CloseCommand());
 			return;
 		} else {
 			System.err.println("Unrecognized command");
 		}
 	}
 	class SetLocationCommand implements Runnable {
 		int x, y;
 		public SetLocationCommand(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 		public void run() {
 			shell.setLocation(x, y);
 		}
 	}
 	class SetSizeCommand implements Runnable {
 		int x, y;
 		public SetSizeCommand(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 		public void run() {
 			shell.setSize(x, y);
 		}
 	}
 	class CloseCommand implements Runnable {
 		public void run() {
 			shell.dispose();
 		}
 	}
 	class DisplayURLCommand implements Runnable {
 		String url;
 		public DisplayURLCommand(String url) {
 			this.url = url;
 		}
 		public void run() {
 			webBrowser.navigate(url);
 		}
 	}
 	class MakeVisible implements Runnable {
 		public void run() {
 			shell.setVisible(true);
 			shell.setMinimized(false);
 			shell.moveAbove(null);
 			shell.forceFocus();
 		}
 	}
 }
