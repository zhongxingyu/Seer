 /*
  * SplashScreen.java
  *
  * Created on 22. Juli 2007, 16:45
  *
  * Due working with Java 1.5 we need an extra SplashScreen class.
  *
  * Copyright (C) 2007
  * German Research Center for Artificial Intelligence (DFKI GmbH) Saarbruecken
  * Hochschule fuer Technik und Wirtschaft (HTW) des Saarlandes
  * Developed by Oliver Fourman, Ingo Zinnikus, Matthias Klusch
  *
  * The code is free for non-commercial use only.
  * You can redistribute it and/or modify it under the terms
  * of the Mozilla Public License version 1.1  as
  * published by the Mozilla Foundation at
  * http://www.mozilla.org/MPL/MPL-1.1.txt
  */
 
 package de.dfki.dmas.owls2wsdl.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.border.Border;
 import javax.swing.border.CompoundBorder;
 //import java.net.URI;
 //import java.net.URISyntaxException;
 
 /**
  * 
  * @author Oliver Fourman Shows SplashScreen while loading main application.
  */
 public class SplashScreen extends JDialog { // JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -15463214786899914L;
 	// singleton
 	private static SplashScreen instance;
 	private static final int w = 650;
 	private static final int h = 400;
 	JButton closeButton;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param parent
 	 *            the parent of the window.
 	 * @param image
 	 *            the splash image.
 	 */
 	private SplashScreen(JFrame owner, URL imageURL1, URL imageURL2,
 			URL infoTextURL, String release) { // throws URISyntaxException {
 		// super("OWLS2WSDL SplashScreen");
 		super(owner, "OWLS2WSDL SplashScreen", true);
 		setUndecorated(true);
 		Image image1 = Toolkit.getDefaultToolkit().createImage(imageURL1);
 		Image image2 = Toolkit.getDefaultToolkit().createImage(imageURL2);
 
 		ImageIcon imageIcon1 = new ImageIcon(image1.getScaledInstance(210, 171,
 				Image.SCALE_SMOOTH));
 		ImageIcon imageIcon2 = new ImageIcon(image2.getScaledInstance(210, 49,
 				Image.SCALE_SMOOTH));
 		JLabel logo = new JLabel(imageIcon1, JLabel.LEFT);
 		JLabel participants = new JLabel(imageIcon2, JLabel.LEFT);
 
 		// ViewComponent logoComponent = new ViewComponent();
 		// logoComponent.setImage(imageURL);
 		// logoComponent.scale(240);
 		// logoComponent.validate();
 		// logoComponent.revalidate();
 		// logoComponent.updateUI();
 
 		JPanel parentPanel = new JPanel(new BorderLayout());
 		Border myborder = new CompoundBorder(
 				BorderFactory.createRaisedBevelBorder(),
 				BorderFactory.createEmptyBorder(5, 5, 5, 5));
 		parentPanel.setBorder(myborder);
 		parentPanel.setSize(w, h);
 		parentPanel.setPreferredSize(new Dimension(w, h));
 
 		MediaTracker mt = new MediaTracker(parentPanel);
 		mt.addImage(image1, 0);
 		mt.addImage(image2, 1);
 		try {
 			mt.waitForID(0);
 		} catch (InterruptedException ie) {
 		}
 
 		JPanel logoPanel = new JPanel(new BorderLayout());
 		logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
 		logoPanel.add(logo, BorderLayout.NORTH);
 		logoPanel.add(participants, BorderLayout.CENTER);
 
 		JTextPane licencePane = new JTextPane();
 		licencePane.setBackground(parentPanel.getBackground());
 
 		licencePane.setFont(new Font("Dialog", Font.PLAIN, 9));
 		licencePane.setEditable(false);
 		licencePane.setEnabled(false);
 		licencePane.setDisabledTextColor(Color.BLACK);
 
 		String releaseString = "";
 		if (release != null) {
 			releaseString = "Release: " + release + "\n\n";
 		}
 
 		licencePane.setText(releaseString + "Copyright 2007\n\n"
 				+ "Oliver Fourman, " + "Matthias Klusch and Ingo Zinnikus\n"
 				+ "German Research Center for Artificial Intelligence\n"
 				+ "Released under the Mozilla Public Licence MPL 1.1\n" + "\n");
 
 		logoPanel.add(licencePane, BorderLayout.SOUTH);
 
 		JPanel creditsPanel = new JPanel(new BorderLayout());
 
 		JPanel creditsLabelPanel = new JPanel();
 		creditsLabelPanel
 				.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
 		creditsLabelPanel.add(new JLabel("Java OWLS2WSDL Tool"));
 		creditsPanel.add(creditsLabelPanel, BorderLayout.NORTH);
 
 		JTextArea licence = new JTextArea();
 		// licence.setBackground(new java.awt.Color(246, 246, 246));
 		licence.setBackground(Color.WHITE);
 		licence.setEditable(false);
 		licence.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
 		licence.setFont(new java.awt.Font("Dialog", Font.PLAIN, 11));
 
 		// Problems reading Text from Jar archive...
 
 		// try {
 		// System.out.println("LOAD PATH: "+infoTextURL.getPath());
 		// //System.out.println("LOAD URI : "+infoTextURL.toURI());
 		// File infoFile = new File(infoTextURL.getPath());
 		// System.out.println("LOAD FILE: "+infoFile.getAbsolutePath());
 		// FileReader fr = new FileReader(infoFile);
 		// char[] temp = new char[(int) infoFile.length()];
 		// fr.read(temp);
 		// String info = new String(temp);
 		// //System.out.println(info);
 		// licence.setText(info);
 		// fr.close();
 		// }
 		// catch ( IOException e ) {
 		// System.out.println( "Fehler beim Lesen der Datei: "+infoTextURL );
 		// licence.setText( "Fehler beim Lesen der Datei: "+infoTextURL );
 		// }
 
 		InputStream is = SplashScreen.class.getResourceAsStream("/info.txt");
 		StringBuffer sbuffer = new StringBuffer();
 		try {
 			byte[] buf = new byte[4096];
 			int len;
 			while ((len = is.read(buf)) > 0) {
 				sbuffer.append(new String(buf, 0, len));
 			}
 			is.close();
 			licence.setText(sbuffer.toString());
 		} catch (IOException ioe) {
 			System.out.println("Fehler beim Lesen der Datei: " + infoTextURL);
 			ioe.printStackTrace();
 			licence.setText("Fehler beim Lesen der Datei: " + infoTextURL);
 		}
 
 		licence.setCaretPosition(0);
 		creditsPanel.add(new JScrollPane(licence,
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
 
 		JPanel creditsCmdsPanel = new JPanel();
 		closeButton = new JButton("Close Splash Screen");
 		closeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		creditsCmdsPanel.add(closeButton);
 
 		creditsPanel.add(creditsCmdsPanel, BorderLayout.SOUTH);
 
 		parentPanel.add(logoPanel, BorderLayout.WEST);
 		parentPanel.add(creditsPanel, BorderLayout.CENTER);
 
 		parentPanel.updateUI();
 		setContentPane(parentPanel);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		// System.out.println("DIM: "+d);
 		setSize(w, h);
 		setLocation((d.width - w) / 2, (d.height - h) / 2);
 	}
 
 	/**
 	 * Open's a splash window using the specified image.
 	 * 
 	 * @param JFrame
 	 *            The splash image.
 	 */
 	public static void splash(JFrame owner, String release) {
 		// URL imageURL = SplashScreen.class.getResource("splash.gif");
		URL imageURL1 = SplashScreen.class.getClassLoader()
				.getResource("images/owls2wsdl-logo.jpg");
		URL imageURL2 = SplashScreen.class.getClassLoader()
				.getResource("images/project-participants.jpg");
		URL infoTextURL = SplashScreen.class.getClassLoader().getResource("info.txt");
		System.out.println("[i] Loaded resources: " + imageURL1 + ", " + imageURL2 + ", " + infoTextURL);
 
 		if (instance == null && imageURL1 != null && imageURL2 != null
 				&& infoTextURL != null) {
 			System.out
 					.println("[i] Swing into the SPLASH Screen instance-method!");
 			// Create the splash image
 			instance = new SplashScreen(owner, imageURL1, imageURL2,
 					infoTextURL, release);
 			instance.pack();
 			instance.setVisible(true);
 		} else if (instance != null && imageURL1 != null && imageURL2 != null
 				&& infoTextURL != null) {
 			instance.setVisible(true);
 		}
 	}
 
 	/**
 	 * Closes the splash window.
 	 */
 	public static void disposeSplash() {
 		if (instance != null) {
 			// instance.getOwner().dispose();
 			instance.dispose();
 			instance = null;
 		}
 	}
 
 	public static void hideSplash() {
 		instance.setVisible(false);
 	}
 
 	/**
 	 * Invokes the main method of the provided class name.
 	 */
 	public static void invokeMainApp() {
 		String[] args = new String[0];
 		try {
 			Class.forName("de.dfki.dmas.owls2wsdl.gui.OWLS2WSDLGui")
 					.getMethod("main", new Class[] { String[].class })
 					.invoke(null, new Object[] { args });
 		} catch (Exception e) {
 			InternalError error = new InternalError(
 					"Failed to invoke main method");
 			error.initCause(e);
 			throw error;
 		}
 	};
 
 	public static void main(String[] args) {
 		SplashScreen.splash(null, null);
 		SplashScreen.invokeMainApp();
 		SplashScreen.disposeSplash();
 	}
 
 }
