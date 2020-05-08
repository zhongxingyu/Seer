 package hms.Views;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JLabel;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JRadioButton;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import java.text.ParseException;
 import java.util.Date;
 import java.awt.Color;
 
 import javax.swing.border.EtchedBorder;
 import javax.swing.text.MaskFormatter;
 import javax.swing.JFormattedTextField;
 import javax.swing.JComboBox;
 
 import hms.models.*;
 
 public class PatientView {
 
 	public JFrame frmPatient;
 	private JTextField textFieldPatientTelephoneNumber;
 	private JTextField textFieldPatientName;
 	private JTextField textFieldPatientEmail;
 	private JTextField textFieldPatientHealthCareNumber;
 	private JTextField textField;//Emergency Contact Telephone Number
 	private JTextField textField_2;//Emergency Contact Name
 	private JTextField textField_1;//Emergenct Contact email
 	private JRadioButton rdbtnMale;
 	private JRadioButton rdbtnFemale;
 	private JFormattedTextField formattedTextFieldBirthdate;
 	private JTextPane textPaneMedications;
 	private JTextPane textPanePatientAddress;
 	private JTextPane textPane_1;//Special Care Information
 	private JTextPane textPaneHistory;
 	private JTextPane textPaneComments;
 	public static boolean isNew = false;
 
 	/**
 	 */
 	public PatientView() {
		initialize(false);
 
 		centreWindow(frmPatient);
 	}
 
 	public PatientView(String[] row){
		initialize(true);
 		centreWindow(frmPatient);
 		if(row.length == 15){
 			if(row[0]!= null)
 				textFieldPatientHealthCareNumber.setText(row[0]);
 			if(row[1]!= null)
 				textFieldPatientName.setText(row[1]);
 			if(row[2]!= null)
 				textFieldPatientTelephoneNumber.setText(row[2]);
 			if(row[3]!= null)
 				textFieldPatientEmail.setText(row[3]);
 			if(row[4]!= null){
 				if(row[4].equals("M")){
 					rdbtnFemale.setSelected(false);
 					rdbtnMale.setSelected(true);
 				}else{
 					rdbtnFemale.setSelected(true);
 					rdbtnMale.setSelected(false);
 				}
 			}
 			//if(row[5]!= null)//TODO if treatment is added
 			if(row[6]!= null)
 				textPanePatientAddress.setText(row[6]);
 			if(row[7]!= null){
 				{
 					String str = "";
 					str += row[7].substring(8,10);
 					str += ".";
 					str += row[7].substring(5,7);
 					str += ".";
 					str += row[7].substring(0,4);
 					formattedTextFieldBirthdate.setText(str);
 				}
 			}
 			if(row[8]!= null)
 				textPaneMedications.setText(row[8]);
 			if(row[9]!= null)
 				textPane_1.setText(row[9]);
 			if(row[10]!= null)
 				textPaneHistory.setText(row[10]);
 			if(row[11]!= null)
 				textPaneComments.setText(row[11]);
 			//TODO add in rest if implemented
 
 		}
 	}
 	
 	public void createPatient(int unused){
 		//TODO test to see if patient is in database
 		
 		
 		
 		Date BirthDate = null;
 		String STR = formattedTextFieldBirthdate.getText();
 		if(STR.length() == 10){
 			String day   = STR.substring(0, 2);
 			String month = STR.substring(3, 5);
 			String year  = STR.substring(6, 10);
 			BirthDate = new Date( Integer.parseInt(year)-1900 , Integer.parseInt(month)-1 , Integer.parseInt(day) );
 		}				
 		Patient temp = new Patient(
 				textFieldPatientHealthCareNumber.getText(),
 				textFieldPatientName.getText(),
 				textFieldPatientTelephoneNumber.getText(), 
 				textFieldPatientEmail.getText(),
 				rdbtnMale.isSelected()? "M":"F",
 						null,//no field for this
 						textPanePatientAddress.getText(),
 						BirthDate,
 						textPaneMedications.getText(),
 						textPane_1.getText(),
 						textPaneHistory.getText(),
 						textPaneComments.getText(),
 						null,//
 						null,//
 						null);//for iteration 2 maybe? TODO
 		//textFieldPatientHealthCareNumber.setText(BirthDate.toString());
 		try{
 			temp.delete();
 			temp.create();
 		}catch(Exception e1){
 			return;
 		}
 	}
 
 	public void createPatient(){
 		//TODO test to see if patient is in database
 		Date BirthDate = null;
 		String STR = formattedTextFieldBirthdate.getText();
 		if(STR.length() == 10){
 			String day   = STR.substring(0, 2);
 			String month = STR.substring(3, 5);
 			String year  = STR.substring(6, 10);
 			BirthDate = new Date( Integer.parseInt(year)-1900 , Integer.parseInt(month)-1 , Integer.parseInt(day) );
 		}				
 		Patient temp = new Patient(
 				textFieldPatientHealthCareNumber.getText(),
 				textFieldPatientName.getText(),
 				textFieldPatientTelephoneNumber.getText(), 
 				textFieldPatientEmail.getText(),
 				rdbtnMale.isSelected()? "M":"F",
 						null,//no field for this
 						textPanePatientAddress.getText(),
 						BirthDate,
 						textPaneMedications.getText(),
 						textPane_1.getText(),
 						textPaneHistory.getText(),
 						textPaneComments.getText(),
 						null,//
 						null,//
 						null);//for iteration 2 maybe? TODO
 		//textFieldPatientHealthCareNumber.setText(BirthDate.toString());
 		try{
 			temp.create();
 		}catch(Exception e1){
 			return;
 		}
 	}
 
 
 	public static void centreWindow(JFrame frame) {
 		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
 		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
 		frame.setLocation(x, y);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize(boolean isNew) {
 		this.isNew = isNew;
 		frmPatient = new JFrame();
 		frmPatient.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.png")));
 		frmPatient.setAlwaysOnTop(true);
 		frmPatient.setTitle("Patient");
 		frmPatient.setBounds(100, 100, 730, 623);
 		frmPatient.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		JLabel lblName = new JLabel("Name");
 
 		JLabel lblTelephoneNumber = new JLabel("Telephone Number");
 
 		textFieldPatientTelephoneNumber = new JTextField();
 		textFieldPatientTelephoneNumber.setColumns(10);
 
 		JButton btnSaveAndClose = new JButton("Save and Close");
 		//		public Patient(String healthcare_number, String name, String phone_number, String email,
 		//				String gender, String treatment, String address, Date birthdate, String medications, 
 		//				String special_care, String history, String comments, String emerg_name, 
 		//				String emerg_phone_number, String emerg_email)
 		btnSaveAndClose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//TODO test to see if patient is in database
 				if(PatientView.isNew)
 					createPatient();
 				else createPatient(0);
 				frmPatient.dispose();//TODO
 			}
 		});
 
 		textFieldPatientName = new JTextField();
 		textFieldPatientName.setColumns(10);
 
 		JLabel lblEmail = new JLabel("Email");
 
 		textFieldPatientEmail = new JTextField();
 		textFieldPatientEmail.setColumns(10);
 
 		rdbtnMale = new JRadioButton("Male");
 		rdbtnMale.setSelected(true);
 		rdbtnFemale = new JRadioButton("Female");
 
 		rdbtnMale.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				rdbtnFemale.setSelected(!rdbtnMale.isSelected());
 			}
 		});
 		rdbtnFemale.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				rdbtnMale.setSelected(!rdbtnFemale.isSelected());
 			}
 		});
 
 		JLabel lblAddress = new JLabel("Address");
 
 		JLabel lblHealthCareNumber = new JLabel("Health Care Number");
 
 		textFieldPatientHealthCareNumber = new JTextField();
 		textFieldPatientHealthCareNumber.setColumns(10);
 
 		JPanel panelSpecialCareInformation = new JPanel();
 		panelSpecialCareInformation.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 
 		textPanePatientAddress = new JTextPane();
 
 		JPanel panel = new JPanel();
 		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 
 		JLabel label = new JLabel("Emergency Contact");
 
 		JLabel label_1 = new JLabel("Telephone Number");
 
 		textField = new JTextField();
 		textField.setColumns(10);
 
 		JLabel label_2 = new JLabel("Name");
 
 		JLabel label_3 = new JLabel("Email");
 
 		textField_2 = new JTextField();
 		textField_2.setColumns(10);
 
 		textField_1 = new JTextField();
 		textField_1.setColumns(10);
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 				gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 								.addGroup(gl_panel.createSequentialGroup()
 										.addComponent(label, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
 										.addContainerGap())
 										.addGroup(gl_panel.createSequentialGroup()
 												.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
 														.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 														.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE))
 														.addPreferredGap(ComponentPlacement.RELATED)
 														.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
 																.addComponent(textField)
 																.addComponent(textField_1, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
 																.addPreferredGap(ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
 																.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
 																.addPreferredGap(ComponentPlacement.RELATED)
 																.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE)
 																.addContainerGap(84, Short.MAX_VALUE))))
 				);
 		gl_panel.setVerticalGroup(
 				gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 						.addGap(6)
 						.addComponent(label)
 						.addPreferredGap(ComponentPlacement.UNRELATED)
 						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 								.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(label_2))
 								.addGap(6)
 								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 										.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 												.addComponent(label_1)
 												.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 												.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 														.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 														.addComponent(label_3)))
 														.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 		panel.setLayout(gl_panel);
 
 		JButton btnClose = new JButton("Close");
 		btnClose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				frmPatient.dispose();
 			}
 		});
 
 		JLabel lblBirthdate = new JLabel("Birthdate");
 		MaskFormatter birthdateMaskFormatter = null;
 		try {
 			birthdateMaskFormatter = new MaskFormatter("##.##.####");
 		} catch (ParseException e1) {
 			e1.printStackTrace();
 		}
 
 		formattedTextFieldBirthdate = new JFormattedTextField(birthdateMaskFormatter);
 
 		JLabel lblExDdmmyyyy = new JLabel("ex: dd.MM.YYYY");
 		JLabel lblBirthdate1 = new JLabel("Birthdate");
 		MaskFormatter roomNumberMaskFormatter = null;
 		try {
 			roomNumberMaskFormatter = new MaskFormatter("####");
 		} catch (ParseException e1) {
 			e1.printStackTrace();
 		}
 
 		JPanel panel_1 = new JPanel();
 		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 
 		JButton buttonSave = new JButton("Save");
 		buttonSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(PatientView.isNew)
 					createPatient();
 				else 
 					createPatient(0);
 			}
 		});
 		GroupLayout groupLayout = new GroupLayout(frmPatient.getContentPane());
 		groupLayout.setHorizontalGroup(
 				groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 						.addContainerGap(34, Short.MAX_VALUE)
 						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 								.addGroup(groupLayout.createSequentialGroup()
 										.addComponent(buttonSave, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addComponent(btnSaveAndClose)
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addComponent(btnClose, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE))
 										.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
 												.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 														.addGroup(groupLayout.createSequentialGroup()
 																.addComponent(lblName, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
 																.addPreferredGap(ComponentPlacement.UNRELATED)
 																.addComponent(textFieldPatientName, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE)
 																.addPreferredGap(ComponentPlacement.RELATED))
 																.addGroup(groupLayout.createSequentialGroup()
 																		.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 																				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 																						.addComponent(lblAddress)
 																						.addComponent(lblHealthCareNumber, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 																						.addComponent(lblTelephoneNumber, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 																						.addPreferredGap(ComponentPlacement.RELATED)
 																						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 																								.addComponent(textPanePatientAddress, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE)
 																								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 																										.addComponent(textFieldPatientHealthCareNumber, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
 																										.addComponent(textFieldPatientTelephoneNumber)))))
 																										.addGap(21)
 																										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 																												.addComponent(lblEmail, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
 																												.addComponent(lblBirthdate1, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
 																												.addPreferredGap(ComponentPlacement.RELATED)
 																												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 																														.addComponent(lblExDdmmyyyy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																														.addComponent(textFieldPatientEmail, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
 																														.addGroup(groupLayout.createSequentialGroup()
 																																.addComponent(rdbtnMale)
 																																.addGap(18)
 																																.addComponent(rdbtnFemale, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 																																.addComponent(formattedTextFieldBirthdate))
 																																.addGap(106))
 																																.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																																.addComponent(panelSpecialCareInformation, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
 																																.addComponent(panel_1, 0, 0, Short.MAX_VALUE))
 																																.addGap(24))
 				);
 		groupLayout.setVerticalGroup(
 				groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 						.addGap(29)
 						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(textFieldPatientName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(rdbtnMale)
 								.addComponent(rdbtnFemale)
 								.addComponent(lblName))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 										.addComponent(lblHealthCareNumber)
 										.addComponent(textFieldPatientHealthCareNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 										.addComponent(lblBirthdate1)
 										.addComponent(formattedTextFieldBirthdate, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 												.addComponent(textPanePatientAddress, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 												.addComponent(lblAddress)
 												.addComponent(lblExDdmmyyyy))
 												.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 												.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 														.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 																.addComponent(textFieldPatientTelephoneNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 																.addComponent(lblTelephoneNumber))
 																.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 																		.addComponent(lblEmail)
 																		.addComponent(textFieldPatientEmail, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 																		.addPreferredGap(ComponentPlacement.RELATED)
 																		.addComponent(panel, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
 																		.addPreferredGap(ComponentPlacement.RELATED)
 																		.addComponent(panelSpecialCareInformation, GroupLayout.PREFERRED_SIZE, 171, GroupLayout.PREFERRED_SIZE)
 																		.addPreferredGap(ComponentPlacement.RELATED)
 																		.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 																		.addGap(18)
 																		.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 																				.addComponent(btnSaveAndClose)
 																				.addComponent(btnClose)
 																				.addComponent(buttonSave))
 																				.addGap(93))
 				);
 
 		JLabel lblAssignPatientTo = new JLabel("Assign Patient to Room");
 
 		JLabel lblBed = new JLabel("Beds");
 
 		JFormattedTextField formattedTextFieldBeds = new JFormattedTextField();
 		formattedTextFieldBeds.setEditable(false);
 
 		JLabel lblWard = new JLabel("Wards");
 
 		JComboBox comboBoxWard = new JComboBox();
 
 		JLabel lblRoom = new JLabel("Rooms");
 
 		JComboBox comboBoxRoom = new JComboBox();
 		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
 		gl_panel_1.setHorizontalGroup(
 				gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblAssignPatientTo)
 								.addGroup(gl_panel_1.createSequentialGroup()
 										.addComponent(lblWard, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addComponent(comboBoxWard, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
 										.addGap(12)
 										.addComponent(lblRoom, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addComponent(comboBoxRoom, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
 										.addGap(18)
 										.addComponent(lblBed)
 										.addGap(10)
 										.addComponent(formattedTextFieldBeds, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)))
 										.addGap(18))
 				);
 		gl_panel_1.setVerticalGroup(
 				gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 						.addComponent(lblAssignPatientTo)
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblWard)
 								.addComponent(comboBoxWard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblRoom)
 								.addComponent(comboBoxRoom, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblBed)
 								.addComponent(formattedTextFieldBeds, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 								.addContainerGap(15, Short.MAX_VALUE))
 				);
 		panel_1.setLayout(gl_panel_1);
 
 		JLabel lblMedications = new JLabel("Medications");
 
 		JLabel lblMedicalInformation = new JLabel("Medical Information");
 
 		textPaneMedications = new JTextPane();
 
 		textPane_1 = new JTextPane();
 
 		JLabel lblSpecialCareInformation = new JLabel("Special Care Information");
 
 		textPaneHistory = new JTextPane();
 
 		textPaneComments = new JTextPane();
 
 		JLabel lblHistory = new JLabel("History");
 
 		JLabel lblComments = new JLabel("Comments");
 		GroupLayout gl_panelSpecialCareInformation = new GroupLayout(panelSpecialCareInformation);
 		gl_panelSpecialCareInformation.setHorizontalGroup(
 				gl_panelSpecialCareInformation.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panelSpecialCareInformation.createSequentialGroup()
 						.addGap(38)
 						.addGroup(gl_panelSpecialCareInformation.createParallelGroup(Alignment.TRAILING)
 								.addComponent(lblMedications)
 								.addComponent(lblHistory))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(gl_panelSpecialCareInformation.createParallelGroup(Alignment.LEADING)
 										.addComponent(textPaneMedications, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE)
 										.addComponent(textPaneHistory, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
 										.addGroup(gl_panelSpecialCareInformation.createParallelGroup(Alignment.TRAILING)
 												.addComponent(lblComments)
 												.addComponent(lblSpecialCareInformation, GroupLayout.PREFERRED_SIZE, 157, GroupLayout.PREFERRED_SIZE))
 												.addPreferredGap(ComponentPlacement.RELATED)
 												.addGroup(gl_panelSpecialCareInformation.createParallelGroup(Alignment.LEADING)
 														.addComponent(textPaneComments, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE)
 														.addComponent(textPane_1, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE))
 														.addContainerGap(32, Short.MAX_VALUE))
 														.addGroup(gl_panelSpecialCareInformation.createSequentialGroup()
 																.addContainerGap()
 																.addComponent(lblMedicalInformation, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
 																.addContainerGap(520, Short.MAX_VALUE))
 				);
 		gl_panelSpecialCareInformation.setVerticalGroup(
 				gl_panelSpecialCareInformation.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panelSpecialCareInformation.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(lblMedicalInformation)
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addGroup(gl_panelSpecialCareInformation.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblMedications)
 								.addComponent(textPaneMedications, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 								.addComponent(textPane_1, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblSpecialCareInformation))
 								.addPreferredGap(ComponentPlacement.UNRELATED)
 								.addGroup(gl_panelSpecialCareInformation.createParallelGroup(Alignment.LEADING)
 										.addComponent(textPaneComments, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 										.addComponent(textPaneHistory, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 										.addComponent(lblHistory)
 										.addComponent(lblComments))
 										.addGap(22))
 				);
 		panelSpecialCareInformation.setLayout(gl_panelSpecialCareInformation);
 		frmPatient.getContentPane().setLayout(groupLayout);
 	}
 }
