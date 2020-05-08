 /*
  * OWLS2WSDLGui.java
  *
  * Created on 2. Oktober 2006, 10:32
  *
  * Copyright (C) 2007
  * German Research Center for Artificial Intelligence (DFKI GmbH) Saarbruecken
 * Hochschule f�r Technik und Wirtschaft (HTW) des Saarlandes
  * Developed by Oliver Fourman, Ingo Zinnikus, Matthias Klusch
  *
  * The code is free for non-commercial use only.
  * You can redistribute it and/or modify it under the terms
  * of the Mozilla Public License version 1.1  as
  * published by the Mozilla Foundation at
  * http://www.mozilla.org/MPL/MPL-1.1.txt
  *
  * Todo: JOptionPane for exceptions: http://www.javalobby.org/java/forums/t19012.html
  */
 
 package de.dfki.dmas.owls2wsdl.gui;
 
 import gr.zeus.ui.JConsole;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.LookAndFeel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.WindowConstants;
 
 import com.jgoodies.looks.LookUtils;
 import com.jgoodies.looks.Options;
 
 import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;
 
 //import java.awt.*;
 
 /**
  * 
  * @author Oliver Fourman
  */
 public class OWLS2WSDLGui extends JFrame { // implements WindowListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5327777439739515135L;
 
 	// Globals
 	protected static final Dimension PREFERRED_SIZE = LookUtils.IS_LOW_RESOLUTION ? new Dimension(
 			880, 660) : new Dimension(1040, 800);
 
 	/** Describes optional settings of the JGoodies Looks. */
 	private final Settings settings;
 
 	// OWLS2WSDL frame components
 
 	private JPanel contentPane;
 	private MainPane mainPane;
 	// private JMenuBar menuBar;
 	protected ToolBar toolbar;
 	protected StatusBar statusbar;
 	protected OutputPanel outputPanel;
 
 	// ActionListener
 	protected GUIActionListener actionListener;
 
 	// JConsole
 	public static JConsole jconsole;
 
 	/** Creates a new instance of OWLS2WSDLGui */
 	public OWLS2WSDLGui(Settings settings) {
 		// super("OWLS2WSDL Tool"); // window title
 		super(ResourceManager.getString("application.title"));
 		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // see
 																			// listener:
 																			// close
 																			// method
 
 		this.settings = settings; // JGoodies
 		this.actionListener = new GUIActionListener(this);
 
 		// WindowListener hinzufuegen (= Action Listener)
 		this.addWindowListener(this.actionListener);
 	}
 
 	private void configureUI() {
 		// UIManager.put("ToolTip.hideAccelerator", Boolean.FALSE);
 		Options.setDefaultIconSize(new Dimension(18, 18));
 		Options.setUseNarrowButtons(settings.isUseNarrowButtons());
 		Options.setTabIconsEnabled(settings.isTabIconsEnabled());
 		UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY,
 				settings.isPopupDropShadowEnabled());
 
 		// Swing Settings
 		LookAndFeel selectedLaf = settings.getSelectedLookAndFeel();
 
 		// Work around caching in MetalRadioButtonUI
 		JRadioButton radio = new JRadioButton();
 		radio.getUI().uninstallUI(radio);
 		JCheckBox checkBox = new JCheckBox();
 		checkBox.getUI().uninstallUI(checkBox);
 
 		try {
 			UIManager.setLookAndFeel(selectedLaf);
 			SwingUtilities.updateComponentTreeUI(this);
 		} catch (UnsupportedLookAndFeelException uslafe) {
 			System.out.println("UnsupportedLookAndFeelException: "
 					+ uslafe.getMessage());
 		} catch (Exception e) {
 			System.out.println("Can't change L&F: " + e);
 		}
 
 		// // FensterIcon
 		// String IconLocation = ResourceManager.getString("icon.IconImage");
 		// System.out.println("[i] set IconImage: " +
 		// getClass().getResource(IconLocation).toString());
 		// setIconImage( new
 		// ImageIcon(getClass().getResource(IconLocation)).getImage());
 
 	}
 
 	public MainPane getMainPane() {
 		return this.mainPane;
 	}
 
 	/**
 	 * Builds the <code>Mainframe</code> using Options from the Launcher.
 	 */
 	private void build() {
 
 		//
 		// ContentPane
 		//
 		contentPane = new JPanel(new BorderLayout());
 		contentPane.setPreferredSize(PREFERRED_SIZE);
 		this.setContentPane(contentPane);
 
 		//
 		// MENU
 		//
 
 		setJMenuBar(createMenuBuilder().buildMenuBar(settings, actionListener));
 		setIconImage(readImageIcon("owls2wsdl_icon.gif").getImage());
 
 		//
 		// TOOL-BAR
 		//
 		this.toolbar = new ToolBar(actionListener);
 		contentPane.add(toolbar, BorderLayout.NORTH);
 
 		//
 		// STATUS-BAR
 		//
 		this.statusbar = new StatusBar();
 		contentPane.add(statusbar, BorderLayout.SOUTH);
 
 		//
 		// CONTENT-PANE
 		//
 		this.mainPane = new MainPane();
 		this.getContentPane().add(mainPane.build(), BorderLayout.CENTER);
 
 		this.outputPanel = mainPane.outputPanel;
 
 		// servicePanel.buildPanel(frame);
 		// datatypePanel.buildPanel(frame);
 		this.pack();
 	}
 
 	/**
 	 * Creates and returns a builder that builds the menu. This method is
 	 * overriden by the full JGoodies Looks Demo to use a more sophisticated
 	 * menu builder that uses the JGoodies UI Framework.
 	 * 
 	 * @return the builder that builds the menu bar
 	 */
 	protected MenuBarView createMenuBuilder() {
 		return new MenuBarView();
 	}
 
 	public static final void centerWindow(Window win) {
 		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension winDim = win.getSize();
 
 		// System.out.println("[i] Screen: "+screenDim.width+"x"+screenDim.height);
 		// System.out.println("[i] WinDim: "+winDim.width+"x"+winDim.height);
 
		// wenn das Fenster gro�er als der Desktop ist, Fenster auf
 		// Desktopgroesse verkleinern
 		if (screenDim.width < winDim.width) {
 			win.setSize(screenDim.width, winDim.height);
 		}
 		if (screenDim.height < winDim.height) {
 			win.setSize(winDim.width, screenDim.height);
 		}
 		// Fenster zentrieren
 		int x = (screenDim.width - winDim.width) / 2;
 		int y = (screenDim.height - winDim.height) / 2;
 
 		System.out.println("[i] setLocation x=" + x + ", y=" + y);
 		win.setLocation(x, y);
 	}
 
 	public static void createAndShowGUI() {
 
 		if (OWLS2WSDLSettings.getInstance().getProperty("lang").equals("de")) {
 			ResourceManager.setBundle("de.dfki.dmas.owls2wsdl.gui.OWLS2WSDL",
 					"de", "DE");
 			ResourceManager.setDefaultBundle(
 					"de.dfki.dmas.owls2wsdl.gui.OWLS2WSDL", "en", "EN");
 		} else if (OWLS2WSDLSettings.getInstance().getProperty("lang")
 				.equals("en")) {
 			ResourceManager.setBundle("de.dfki.dmas.owls2wsdl.gui.OWLS2WSDL",
 					"en", "EN");
 		}
 		// ResourceManager.printResources();
 
 		Settings settings = createDefaultSettings();
 
 		final OWLS2WSDLGui instance = new OWLS2WSDLGui(settings);
 		settings.setSelectedLookAndFeel(Options.PLASTICXP_NAME);
 		instance.configureUI();
 
 		Runnable r1 = new Runnable() {
 			public void run() {
				SplashScreen.splash(instance, null);
				SplashScreen.disposeSplash();
 				try {
 					jconsole.setVisible(true);
 				} catch (java.lang.NullPointerException e) {
 					System.out.println("[e] JConsole not yet initiated.");
 				}
 				instance.setVisible(true);
 			}
 		};
 
 		Runnable r2 = new Runnable() {
 			public void run() {
 				// instance.configureUI();
 				instance.build();
 				instance.setSize(PREFERRED_SIZE);
 				// instance.centerWindow(instance);
 				instance.locateOnScreen(instance);
 				instance.pack(); // System.out.println("[.] pack window");
 
 				if (OWLS2WSDLSettings.getInstance().getProperty("JCONSOLE")
 						.equals("yes")) {
 					jconsole = new JConsole();
 					Dimension screenSize = instance.getToolkit()
 							.getScreenSize();
 					jconsole.setTitle("JConsole for OWLS2WSDL Tool");
 					jconsole.setSize(940, 300);
 					jconsole.setLocation(0, screenSize.height - 300);
 					jconsole.setAlwaysOnTop(false);
 					// jconsole.getConsole().startConsole();
 					JConsole.getConsole().startConsole();
 					// jconsole.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 					if (!jconsole.isVisible()) {
 						jconsole.setVisible(true);
 						jconsole.toBack();
 					}
 				}
 			}
 		};
 
 		java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
 				.newCachedThreadPool();
 
 		executor.execute(r1);
 		executor.execute(r2);
 	}
 
 	public static void main(String[] args) {
 		createAndShowGUI();
 	}
 
 	private static Settings createDefaultSettings() {
 		Settings settings = Settings.createDefault();
 
 		// Configure the settings here.
 
 		return settings;
 	}
 
 	// Helper Code
 	// **********************************************************************
 
 	/**
 	 * Looks up and returns an icon for the specified filename suffix.
 	 */
 	protected static ImageIcon readImageIcon(String filename) {
 		URL url = OWLS2WSDLGui.class.getClassLoader().getResource(
 				"images/" + filename);
 		return new ImageIcon(url);
 	}
 
 	/**
 	 * Locates the given component on the screen's center.
 	 */
 	protected void locateOnScreen(Component component) {
 		Dimension paneSize = component.getSize();
 		Dimension screenSize = component.getToolkit().getScreenSize();
 		component.setLocation((screenSize.width - paneSize.width) / 2,
 				(screenSize.height - paneSize.height) / 2 - 40);
 	}
 }
