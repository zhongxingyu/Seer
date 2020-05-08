 /* 
 *  Copyright 2012-2013 Coronastreet Networks 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 *  implied. See the License for the specific language governing 
 *  permissions and limitations under the License 
 */
 package org.coronastreet.gpxconverter;
 
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.prefs.Preferences;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPasswordField;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.JCheckBox;
 
 
 public class MainWindow {
 
 	private JFrame frmStravaGpxConverter;
 	private JTextField txtSourceFile;
 	private JFileChooser fc;
 	private JTextArea statusTextArea;
 	protected String newline = "\n";
 	private JTextField loginVal;
 	private JPasswordField passwordVal;
 	private JTextField txtActivityName;
 	private String authToken;
 	private Preferences prefs;
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainWindow window = new MainWindow();
 					window.frmStravaGpxConverter.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MainWindow() {
 		initialize();
 	}
 
 	private void setPref(String name, String value) {
		if (value == null) value = "";
 		prefs.put(name, value);
 	}
 	
 	private void setPref(String name, boolean value) { 
 		prefs.putBoolean(name, value);
 	}
 	
 	private boolean getBoolPref(String name) {
 		return prefs.getBoolean(name, false);
 	}
 	
 	private String getPref(String name) {
		return prefs.get(name, "");
 	}
 	
 	private void delPref(String name) {
 		prefs.remove(name);
 	}
 	
 	protected void statusLog(String actionDescription) {
         statusTextArea.append(actionDescription + newline);
         statusTextArea.setCaretPosition(statusTextArea.getDocument().getLength()-1);
     }
 	
 
 	
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		
 		Preferences userRoot = Preferences.userRoot();
 	    prefs = userRoot.node( "com/coronastreet/gpxconverter/settings" );
 		  
 		frmStravaGpxConverter = new JFrame();
 		frmStravaGpxConverter.getContentPane().setFont(new Font("Tahoma", Font.BOLD, 11));
 		frmStravaGpxConverter.setTitle("GPX Importer for Strava & RideWithGPS");
 		frmStravaGpxConverter.setBounds(100, 100, 441, 447);
 		frmStravaGpxConverter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmStravaGpxConverter.getContentPane().setLayout(null);
 		
 		
 		fc = new JFileChooser(getPref("LastLocation"));
 		
 		JLabel lblThisToolConverts = new JLabel("Upload Ride Data from a GPX file with HR and Cadence data.");
 		lblThisToolConverts.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblThisToolConverts.setHorizontalAlignment(SwingConstants.CENTER);
 		lblThisToolConverts.setBounds(10, 11, 405, 14);
 		frmStravaGpxConverter.getContentPane().add(lblThisToolConverts);
 		
 		txtSourceFile = new JTextField();
 		txtSourceFile.setBounds(24, 36, 286, 20);
 		frmStravaGpxConverter.getContentPane().add(txtSourceFile);
 		txtSourceFile.setColumns(10);
 		
 		statusTextArea = new JTextArea();
 		statusTextArea.setEditable(false);
 		statusTextArea.setColumns(100);
 		statusTextArea.setRows(100);
 		statusTextArea.setLineWrap(true);
 		JScrollPane statusScroller = new JScrollPane(statusTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		statusScroller.setBounds(10, 275, 405, 112);
 		frmStravaGpxConverter.getContentPane().add(statusScroller);
 		
 		JLabel lblSourceGpxFile = new JLabel("Source GPX File");
 		lblSourceGpxFile.setBounds(24, 57, 111, 14);
 		frmStravaGpxConverter.getContentPane().add(lblSourceGpxFile);
 		
 		JButton btnNewButton = new JButton("Find Src");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				fc.setFileFilter(new GPXFilter());
 				int returnVal = fc.showDialog(frmStravaGpxConverter, "Choose Source");
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					txtSourceFile.setText(fc.getSelectedFile().getPath());
 					File f = new File("");
 					fc.setSelectedFile(f);
 					setPref("LastLocation", fc.getCurrentDirectory().getPath());
 				}
 			}
 		});
 		
 		btnNewButton.setBounds(320, 34, 95, 23);
 		frmStravaGpxConverter.getContentPane().add(btnNewButton);
 		
 		loginVal = new JTextField();
 		loginVal.setBounds(100, 80, 210, 20);
 		frmStravaGpxConverter.getContentPane().add(loginVal);
 		loginVal.setColumns(10);
 		loginVal.setText(getPref("lastLoginVal"));
 		
 		passwordVal = new JPasswordField();
 		passwordVal.setBounds(100, 110, 210, 20);
 		frmStravaGpxConverter.getContentPane().add(passwordVal);
 		passwordVal.setColumns(10);
 		passwordVal.setText(getPref("lastPasswordVal"));
 		
 		final JCheckBox chckbxSave = new JCheckBox("Save?");
 		chckbxSave.setToolTipText("Note: This saves your password in Cleartext in the Registry on Windows, and in a File on Unix systems.");
 		chckbxSave.setBounds(320, 109, 97, 23);
 		frmStravaGpxConverter.getContentPane().add(chckbxSave);
 		if (getBoolPref("lastPasswordSave")) {
 			chckbxSave.setSelected(true);
 		}
 		
 		JLabel lblLogin = new JLabel("Login:");
 		lblLogin.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblLogin.setBounds(45, 82, 51, 14);
 		frmStravaGpxConverter.getContentPane().add(lblLogin);
 		
 		JLabel lblNewLabel = new JLabel("Password:");
 		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNewLabel.setBounds(24, 111, 72, 14);
 		frmStravaGpxConverter.getContentPane().add(lblNewLabel);
 		
 		JRadioButton typeIsRide = new JRadioButton("Ride");
 		typeIsRide.setBounds(131, 175, 57, 23);
 		frmStravaGpxConverter.getContentPane().add(typeIsRide);
 		if (getPref("lastType").equals("Ride")) { typeIsRide.setSelected(true); }
 		
 		JRadioButton typeIsRun = new JRadioButton("Run");
 		typeIsRun.setBounds(190, 175, 57, 23);
 		frmStravaGpxConverter.getContentPane().add(typeIsRun);
 		if (getPref("lastType").equals("Run")) { typeIsRun.setSelected(true); }
 		
 		JRadioButton typeIsHike = new JRadioButton("Hike");
 		typeIsHike.setBounds(249, 175, 71, 23);
 		frmStravaGpxConverter.getContentPane().add(typeIsHike);
 		if (getPref("lastType").equals("Hike")) { typeIsHike.setSelected(true); }
 		
 		ButtonGroup eventType = new ButtonGroup();
 		eventType.add(typeIsHike);
 		eventType.add(typeIsRun);
 		eventType.add(typeIsRide);
 		
 		typeIsHike.addActionListener(new TypeAction());
 		typeIsRun.addActionListener(new TypeAction());
 		typeIsRide.addActionListener(new TypeAction());
 		
 		JLabel lblAltimeter = new JLabel("Altimeter:");
 		lblAltimeter.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblAltimeter.setBounds(10, 201, 86, 14);
 		frmStravaGpxConverter.getContentPane().add(lblAltimeter);
 		
 		JRadioButton altYes = new JRadioButton("Yes");
 		altYes.setBounds(131, 197, 57, 23);
 		frmStravaGpxConverter.getContentPane().add(altYes);
 		if (getPref("lastAltimeter").equals("Yes")) { altYes.setSelected(true); }
 		
 		JRadioButton altNo = new JRadioButton("No");
 		altNo.setBounds(190, 197, 57, 23);
 		frmStravaGpxConverter.getContentPane().add(altNo);
 		if (getPref("lastAltimeter").equals("No")) { altNo.setSelected(true); }
 		
 		ButtonGroup altimeterAvail = new ButtonGroup();
 		altimeterAvail.add(altYes);
 		altimeterAvail.add(altNo);
 		
 		altYes.addActionListener(new DeviceAction());
 		altNo.addActionListener(new DeviceAction());
 		
 		JLabel lblActivityType = new JLabel("Activity Type:");
 		lblActivityType.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblActivityType.setBounds(10, 179, 86, 14);
 		frmStravaGpxConverter.getContentPane().add(lblActivityType);
 		
 		JLabel lblActivityName = new JLabel("Activity Name:");
 		lblActivityName.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblActivityName.setBounds(10, 148, 86, 14);
 		frmStravaGpxConverter.getContentPane().add(lblActivityName);
 		
 		txtActivityName = new JTextField();
 		txtActivityName.setBounds(100, 147, 315, 20);
 		frmStravaGpxConverter.getContentPane().add(txtActivityName);
 		txtActivityName.setColumns(10);
 		
 		JLabel lblUploadTo = new JLabel("Upload To:");
 		lblUploadTo.setToolTipText("Note: Email and Password must match for each service.");
 		lblUploadTo.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblUploadTo.setBounds(10, 222, 86, 14);
 		frmStravaGpxConverter.getContentPane().add(lblUploadTo);
 		
 		final JCheckBox chckbxStrava = new JCheckBox("Strava");
 		chckbxStrava.setToolTipText("Note: Email and Password must match for each service.");
 		chckbxStrava.setBounds(131, 218, 90, 23);
 		frmStravaGpxConverter.getContentPane().add(chckbxStrava);
 		
 		final JCheckBox chckbxRidewithgps = new JCheckBox("RideWithGPS");
 		chckbxRidewithgps.setToolTipText("Note: Email and Password must match for each service.");
 		chckbxRidewithgps.setBounds(223, 218, 123, 23);
 		frmStravaGpxConverter.getContentPane().add(chckbxRidewithgps);
 		
 		final JButton btnConvertIt = new JButton("Upload Data");
 		btnConvertIt.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setPref("lastLoginVal", loginVal.getText());
 				
 				if (chckbxSave.isSelected()) {
 					setPref("lastPasswordSave", true);
 					setPref("lastPasswordVal", new String(passwordVal.getPassword()));
 				} else {
 					delPref("lastPasswordVal");
 					setPref("lastPasswordSave", false);
 				}
 				Converter c = new Converter();
 				c.setInFile(txtSourceFile.getText());
 				c.setAuthToken(authToken);
 				c.setActivityName(txtActivityName.getText());
				c.setActivityType(getPref("lastType"));
 				c.setStatusTextArea(statusTextArea);
 				c.setPassword(new String(passwordVal.getPassword()));
 				c.setEmail(loginVal.getText());
				if (getPref("altimeterEval").startsWith("Yes")) {
 				  c.setHasAltimeter(true);
 				}
 					
 				if (chckbxRidewithgps.isSelected()) {
 					c.setDoRWGPS(true);
 				}
 				if (chckbxStrava.isSelected()) {
 					c.setDoStrava(true);
 				}
 				//c.setOutFile(txtDestFile.getText());
 				statusLog("Starting Conversion and Upload...");
 				Thread t = new Thread(c, "Converter");
 				// Set the button the thread knows to enable when it's done. (yeah...lame way to do this...)
 				//btnConvertIt.setEnabled(false);
 				t.start();
 			}
 		});
 		btnConvertIt.setBounds(144, 247, 131, 23);
 		frmStravaGpxConverter.getContentPane().add(btnConvertIt);
 	}
 	
 	public class TypeAction implements ActionListener { 
 		public void actionPerformed(ActionEvent e) {
			setPref("lastType", e.getActionCommand());
 		}
 	}
 	
 	public class DeviceAction implements ActionListener { 
 		public void actionPerformed(ActionEvent e) {
			setPref("lastAltimeter", e.getActionCommand());
 		}
 	}
 }
