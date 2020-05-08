 package org.reprap.gui;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Properties;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SwingConstants;
 
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
 public class Preferences extends javax.swing.JDialog {
 	private JButton jButtonOK;
 	private JButton jButtonCancel;
 	private JComboBox geometry;
 	private JLabel jLabel2;
 	private JLabel jLabel4;
 	private JLabel jLabel8;
 	private JPanel jPanelExtruders;
 	private JLabel jLabel10;
 	private JLabel jLabel9;
 	private JTextField motorTorque1;
 	private JTextField motorTorque2;
 	private JTextField motorTorque3;
 	private JTextField motorAddress1;
 	private JTextField motorAddress2;
 	private JTextField motorAddress3;
 	private JLabel jLabel7;
 	private JLabel jLabel6;
 	private JLabel jLabel5;
 	private JLabel jLabel3;
 	private JPanel jPanelMotors;
 	private JTextPane jTextPane1;
 	private JTextField serialPort;
 	private JLabel jLabel1;
 	private JPanel jPanelGeneral;
 	private JTabbedPane jTabbedPane1;
 	
 	private String [][] geometries =
 	{
			{ "cartesian", "Cartensian" },
			{ "nullcartesian", "Null cartensian" }
 	};
 	
 	/**
 	 * Auto-generated main method to display this JDialog
 	 */
 	public static void main(String[] args) {
 		JFrame frame = new JFrame();
 		Preferences inst = new Preferences(frame);
 		inst.setVisible(true);
 	}
 	
 	public void loadPreferences() {
 		try {
 			Properties props = new Properties();
 			URL url = ClassLoader.getSystemResource("reprap.properties");
 			props.load(url.openStream());
 			
 			serialPort.setText(props.getProperty("Port"));
 			motorAddress1.setText(props.getProperty("Axis1Address"));
 			motorAddress2.setText(props.getProperty("Axis2Address"));
 			motorAddress3.setText(props.getProperty("Axis3Address"));
 			motorTorque1.setText(props.getProperty("Axis1Torque"));
 			motorTorque2.setText(props.getProperty("Axis2Torque"));
 			motorTorque3.setText(props.getProperty("Axis3Torque"));
 			
 			String geometryName = props.getProperty("Geometry");
 			for(int i = 0; i < geometries.length; i++)
 				if (geometries[i][0].compareToIgnoreCase(geometryName) == 0)
 					geometry.setSelectedIndex(i);
 			
 			// Fall back to some defaults
 			if (motorTorque1.getText().length() == 0) motorTorque1.setText("33");
 			if (motorTorque2.getText().length() == 0) motorTorque2.setText("33");
 			if (motorTorque3.getText().length() == 0) motorTorque3.setText("33");
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(null, "Loading preferences: " + ex);
 			ex.printStackTrace();
 		}
 		
 	}
 
 	public void savePreferences() {
 		try {
 			Properties props = new Properties();
 			URL url = ClassLoader.getSystemResource("reprap.properties");
 			props.load(url.openStream());
 			
 			props.setProperty("Port", serialPort.getText());
 			props.setProperty("Axis1Address", motorAddress1.getText());
 			props.setProperty("Axis2Address", motorAddress2.getText());
 			props.setProperty("Axis3Address", motorAddress3.getText());
 			props.setProperty("Axis1Torque", motorTorque1.getText());
 			props.setProperty("Axis2Torque", motorTorque2.getText());
 			props.setProperty("Axis3Torque", motorTorque3.getText());
 			props.setProperty("Geometry", geometries[geometry.getSelectedIndex()][0]);
 			
 			OutputStream output = new java.io.FileOutputStream(url.getPath());
 			props.store(output, "Reprap properties http://reprap.org/");
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(null, "Saving preferences: " + ex);
 			ex.printStackTrace();
 		}
 	}
 	
 	public Preferences(JFrame frame) {
 		super(frame);
 		initGUI();
 		loadPreferences();
 	}
 	
 	private void initGUI() {
 		try {
 			{
 				jButtonOK = new JButton();
 				getContentPane().add(jButtonOK);
 				jButtonOK.setText("OK");
 				jButtonOK.setBounds(308, 238, 77, 28);
 				jButtonOK.addMouseListener(new MouseAdapter() {
 					public void mouseClicked(MouseEvent evt) {
 						jButtonOKMouseClicked(evt);
 					}
 				});
 			}
 			{
 				jButtonCancel = new JButton();
 				getContentPane().add(jButtonCancel);
 				jButtonCancel.setText("Cancel");
 				jButtonCancel.setBounds(217, 238, 77, 28);
 				jButtonCancel.addMouseListener(new MouseAdapter() {
 					public void mouseClicked(MouseEvent evt) {
 						jButtonCancelMouseClicked(evt);
 					}
 				});
 			}
 			{
 				jTabbedPane1 = new JTabbedPane();
 				getContentPane().add(jTabbedPane1);
 				jTabbedPane1.setBounds(7, 7, 378, 224);
 				{
 					jPanelGeneral = new JPanel();
 					jTabbedPane1.addTab("General", null, jPanelGeneral, null);
 					jPanelGeneral.setPreferredSize(new java.awt.Dimension(373, 198));
 					jPanelGeneral.setLayout(null);
 					{
 						jLabel1 = new JLabel();
 						jPanelGeneral.add(jLabel1);
 						jLabel1.setText("Serial port");
 						jLabel1.setBounds(7, 14, 84, 28);
 					}
 					{
 						serialPort = new JTextField();
 						jPanelGeneral.add(serialPort);
 						serialPort.setBounds(91, 14, 154, 28);
 					}
 					{
 						jTextPane1 = new JTextPane();
 						jPanelGeneral.add(jTextPane1);
 						jTextPane1.setText("For linux use a number such as \"0\", \"1\" or alternatively use the full path to your serial device.  For Windows use \"COM1\", \"COM2\" etc.");
 						jTextPane1.setBounds(91, 49, 273, 63);
 						jTextPane1.setEnabled(false);
 						jTextPane1.setEditable(false);
 						jTextPane1.setOpaque(false);
 					}
 				}
 				{
 					jPanelMotors = new JPanel();
 					jTabbedPane1.addTab("Axes", null, jPanelMotors, null);
 					jPanelMotors.setLayout(null);
 					{
 						String [] geometryList = new String[geometries.length];
 						for(int i = 0; i < geometries.length; i++)
 							geometryList[i] = geometries[i][1];
 						ComboBoxModel geometryModel = new DefaultComboBoxModel(
 								geometryList);
 						geometry = new JComboBox();
 						jPanelMotors.add(geometry);
 						geometry.setModel(geometryModel);
 						geometry.setBounds(91, 14, 182, 28);
 					}
 					{
 						jLabel2 = new JLabel();
 						jPanelMotors.add(jLabel2);
 						jLabel2.setText("Geometry");
 						jLabel2.setBounds(14, 14, 63, 28);
 					}
 					{
 						jLabel3 = new JLabel();
 						jPanelMotors.add(jLabel3);
 						jLabel3.setText("Motor 1 (X)");
 						jLabel3.setBounds(14, 81, 84, 28);
 					}
 					{
 						jLabel4 = new JLabel();
 						jPanelMotors.add(jLabel4);
 						jLabel4.setText("Motor 2 (Y)");
 						jLabel4.setBounds(14, 109, 84, 28);
 					}
 					{
 						jLabel5 = new JLabel();
 						jPanelMotors.add(jLabel5);
 						jLabel5.setText("Motor 3 (Z)");
 						jLabel5.setBounds(14, 137, 84, 28);
 					}
 					{
 						jLabel6 = new JLabel();
 						jPanelMotors.add(jLabel6);
 						jLabel6.setText("Address");
 						jLabel6.setBounds(105, 56, 63, 28);
 						jLabel6.setHorizontalAlignment(SwingConstants.CENTER);
 					}
 					{
 						jLabel7 = new JLabel();
 						jPanelMotors.add(jLabel7);
 						jLabel7.setText("Max torque");
 						jLabel7.setBounds(182, 56, 77, 28);
 					}
 					{
 						motorAddress3 = new JTextField();
 						jPanelMotors.add(motorAddress3);
 						motorAddress3.setBounds(112, 140, 49, 21);
 					}
 					{
 						motorAddress1 = new JTextField();
 						jPanelMotors.add(motorAddress1);
 						motorAddress1.setBounds(112, 84, 49, 21);
 					}
 					{
 						motorAddress2 = new JTextField();
 						jPanelMotors.add(motorAddress2);
 						motorAddress2.setBounds(112, 112, 49, 21);
 					}
 					{
 						motorTorque1 = new JTextField();
 						jPanelMotors.add(motorTorque1);
 						motorTorque1.setBounds(182, 84, 35, 21);
 					}
 					{
 						motorTorque3 = new JTextField();
 						jPanelMotors.add(motorTorque3);
 						motorTorque3.setBounds(182, 140, 35, 21);
 					}
 					{
 						motorTorque2 = new JTextField();
 						jPanelMotors.add(motorTorque2);
 						motorTorque2.setBounds(182, 112, 35, 21);
 					}
 					{
 						jLabel8 = new JLabel();
 						jPanelMotors.add(jLabel8);
 						jLabel8.setText("%");
 						jLabel8.setBounds(224, 140, 21, 21);
 					}
 					{
 						jLabel9 = new JLabel();
 						jPanelMotors.add(jLabel9);
 						jLabel9.setText("%");
 						jLabel9.setBounds(224, 84, 21, 21);
 					}
 					{
 						jLabel10 = new JLabel();
 						jPanelMotors.add(jLabel10);
 						jLabel10.setText("%");
 						jLabel10.setBounds(224, 112, 21, 21);
 					}
 				}
 				{
 					jPanelExtruders = new JPanel();
 					jTabbedPane1.addTab(
 						"Extruders",
 						null,
 						jPanelExtruders,
 						null);
 				}
 			}
 			{
 				getContentPane().setLayout(null);
 				this.setTitle("RepRap Preferences");
 			}
 			setSize(400, 300);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void jButtonOKMouseClicked(MouseEvent evt) {
 		// Update all preferences
 		savePreferences();
 		dispose();
 	}
 	
 	private void jButtonCancelMouseClicked(MouseEvent evt) {
 		// Close without saving
 		dispose();
 	}
 	
 }
