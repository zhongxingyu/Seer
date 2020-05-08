 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import java.awt.BorderLayout;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.Box;
 import java.awt.Component;
 import java.awt.Color;
 import javax.swing.JTextArea;
 import javax.swing.JList;
 import javax.swing.JScrollBar;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 import java.awt.SystemColor;
 import javax.swing.UIManager;
 import java.awt.Font;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 
 public class NurseEnterInformation {
 
 	
 	
 	public JFrame frame;
 	private JTextField temperatureBox;
 	private JTextField weightBox;
 	private JTextField heightBox;
 	private JTextField bloodPressureTopBox;
 	private JTextField bloodPressureBotBox;
 	private JTextField bloodSugarBox;
 	private JTextField symptomsBox;
 	private JComboBox dayComboBox = new JComboBox();
 	private JComboBox monthComboBox = new JComboBox();
 	private JComboBox yearComboBox = new JComboBox();
 	private JTextField nameBox;
 	private JTextField patientIDBox;
 	private JLabel lblUserExists = new JLabel("User Exists");
 	private JLabel lblUserNotFound = new JLabel("User not Found");
 	private double temperature, weight, height, bloodPressureTop, bloodPressureBot, bloodSugar;
 	private String symptoms, name; 
 	private int ID;
 	/**
 	 * Launch the application.
 	 */
 
 
 	/**
 	 * Create the application.
 	 */
	public NurseEnterInformation() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.getContentPane().setForeground(UIManager.getColor("Menu.selectionForeground"));
 		frame.getContentPane().setBackground(UIManager.getColor("text"));
 		frame.setBounds(100, 100, 500, 500);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 		
 		JButton btnGoBack = new JButton("<-  Go Back");
 		btnGoBack.setFont(new Font("Consolas", Font.PLAIN, 11));
 		btnGoBack.setBackground(Color.LIGHT_GRAY);
 		btnGoBack.setBounds(10, 11, 110, 23);
 		frame.getContentPane().add(btnGoBack);
 		
 		JButton btnEnter = new JButton("Enter");
 		btnEnter.setForeground(SystemColor.textHighlight);
 		btnEnter.setBackground(Color.LIGHT_GRAY);
 		btnEnter.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 				
 		
 				
 				if(temperatureBox.getText().length() > 0)
 				{
 					temperature = Double.parseDouble(temperatureBox.getText());
 				}
 				else 
 					temperature = 0;
 				
 				if(weightBox.getText().length() > 0)
 				{
 					weight = Double.parseDouble(weightBox.getText());
 				}
 				else 
 					weight = 0;
 				
 				if(heightBox.getText().length() > 0)
 				{
 					height = Double.parseDouble(heightBox.getText());
 				}
 				else 
 					height = 0;
 				
 				if(bloodPressureTopBox.getText().length() > 0)
 				{
 					bloodPressureTop = Double.parseDouble(bloodPressureTopBox.getText());
 				}
 				else 
 					bloodPressureTop = 0;
 				
 				if(bloodPressureBotBox.getText().length() > 0)
 				{
 					bloodPressureBot = Double.parseDouble(bloodPressureBotBox.getText());
 				}
 				else 
 					bloodPressureBot = 0;
 				
 				if(bloodSugarBox.getText().length() > 0)
 				{
 					bloodSugar = Double.parseDouble(bloodSugarBox.getText());
 				}
 				else 
 					bloodSugar = 0;
 				
 				if(symptomsBox.getText().length() > 0)
 				{
 					symptoms = symptomsBox.getText();
 				}
 				else 
 					symptoms = "";
 				
 				
 				int day = Integer.parseInt(dayComboBox.getSelectedItem().toString());
 				int month = Integer.parseInt(monthComboBox.getSelectedItem().toString());
 				int year = Integer.parseInt(yearComboBox.getSelectedItem().toString());
 				
 				if(nameBox.getText().length() > 0)
 				{
 					name = nameBox.getText();
 					Patient tempPatient;
 					User tempUser;
 					if(Globals.userDatabase.searchUserName(name) != null)
 					{
 						tempUser = Globals.userDatabase.searchUserName(name);
 						
 						if(tempUser instanceof Patient)
 						{
 						
 							Visit newVisit = new Visit(month, day, year, bloodPressureTop,
 							bloodPressureBot, bloodSugar, weight, height, temperature, symptoms);
 							tempPatient = (Patient) tempUser;
 							tempPatient.getMedicalRecord().addVisit(newVisit);
 							
 						}
 						
 					}
 					
 				}
 				else	
 				{
 					if(patientIDBox.getText().length() > 0)
 					{
 						ID = Integer.parseInt(patientIDBox.getText().toString());
 						Patient tempPatient;
 						User tempUser;
 						if(Globals.userDatabase.searchUserID(ID) != null)
 						{
 							tempUser = Globals.userDatabase.searchUserID(ID);
 							if(tempUser instanceof Patient)
 							{
 								Visit newVisit = new Visit(month, day, year, bloodPressureTop,
 								bloodPressureBot, bloodSugar, weight, height, temperature, symptoms);
 								tempPatient = (Patient) tempUser;
 								tempPatient.getMedicalRecord().addVisit(newVisit);
 								
 							}
 						}
 					}
 				}
 				
 				
 			}
 		});
 		btnEnter.setBounds(325, 388, 89, 23);
 		frame.getContentPane().add(btnEnter);
 		
 		temperatureBox = new JTextField();
 		temperatureBox.setBounds(366, 170, 46, 20);
 		frame.getContentPane().add(temperatureBox);
 		temperatureBox.setColumns(10);
 		
 		weightBox = new JTextField();
 		weightBox.setBounds(366, 201, 46, 20);
 		frame.getContentPane().add(weightBox);
 		weightBox.setColumns(10);
 		
 		heightBox = new JTextField();
 		heightBox.setBounds(366, 232, 46, 20);
 		frame.getContentPane().add(heightBox);
 		heightBox.setColumns(10);
 		
 		bloodPressureTopBox = new JTextField();
 		bloodPressureTopBox.setBounds(366, 263, 46, 20);
 		frame.getContentPane().add(bloodPressureTopBox);
 		bloodPressureTopBox.setColumns(10);
 		
 		bloodPressureBotBox = new JTextField();
 		bloodPressureBotBox.setBounds(366, 294, 46, 20);
 		frame.getContentPane().add(bloodPressureBotBox);
 		bloodPressureBotBox.setColumns(10);
 		
 		bloodSugarBox = new JTextField();
 		bloodSugarBox.setBounds(366, 325, 46, 20);
 		frame.getContentPane().add(bloodSugarBox);
 		bloodSugarBox.setColumns(10);
 		
 		JLabel temperatureLabel = new JLabel("Temperature (F)");
 		temperatureLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		temperatureLabel.setBounds(262, 173, 94, 14);
 		frame.getContentPane().add(temperatureLabel);
 		
 		JLabel weightLabel = new JLabel("Weight (lbs)");
 		weightLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		weightLabel.setBounds(262, 204, 73, 14);
 		frame.getContentPane().add(weightLabel);
 		
 		JLabel heightLabel = new JLabel("Height (inches)");
 		heightLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		heightLabel.setBounds(262, 235, 94, 14);
 		frame.getContentPane().add(heightLabel);
 		
 		JLabel bloodPressureTopLabel = new JLabel("Blood Pressure Top");
 		bloodPressureTopLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		bloodPressureTopLabel.setBounds(262, 266, 116, 14);
 		frame.getContentPane().add(bloodPressureTopLabel);
 		
 		JLabel bloodSugarLabel = new JLabel("Blood Sugar (mg/dL)");
 		bloodSugarLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		bloodSugarLabel.setBounds(262, 328, 135, 14);
 		frame.getContentPane().add(bloodSugarLabel);
 		
 		JLabel bloodPressureBotLabel = new JLabel("Blood Pressure Bot");
 		bloodPressureBotLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		bloodPressureBotLabel.setBounds(262, 297, 116, 14);
 		frame.getContentPane().add(bloodPressureBotLabel);
 		
 		JLabel lblSymptoms = new JLabel("Symptoms");
 		lblSymptoms.setBounds(11, 297, 74, 14);
 		frame.getContentPane().add(lblSymptoms);
 		String symptoms = lblSymptoms.getText();
 		
 		
 		monthComboBox.setForeground(UIManager.getColor("Menu.foreground"));
 		monthComboBox.setBackground(SystemColor.text);
 		monthComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		monthComboBox.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"}));
 		monthComboBox.setToolTipText("1, 2 ,3\r\n");
 		monthComboBox.setBounds(62, 215, 37, 20);
 		frame.getContentPane().add(monthComboBox);
 
 		dayComboBox.setBackground(SystemColor.text);
 		dayComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		dayComboBox.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"}));
 		dayComboBox.setToolTipText("1, 2 ,3\r\n");
 		dayComboBox.setBounds(109, 215, 37, 20);
 		frame.getContentPane().add(dayComboBox);
 		
 
 		yearComboBox.setBackground(SystemColor.text);
 		yearComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		yearComboBox.setModel(new DefaultComboBoxModel(new String[] {"2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022"}));
 		yearComboBox.setToolTipText("1, 2 ,3\r\n");
 		yearComboBox.setBounds(156, 215, 58, 20);
 		frame.getContentPane().add(yearComboBox);
 		
 		
 		JLabel lblDate = new JLabel("Date");
 		lblDate.setBounds(6, 217, 46, 14);
 		frame.getContentPane().add(lblDate);
 		
 		symptomsBox = new JTextField();
 		symptomsBox.setBounds(11, 325, 135, 20);
 		frame.getContentPane().add(symptomsBox);
 		symptomsBox.setColumns(10);
 		
 		JLabel lblMonth = new JLabel("Month");
 		lblMonth.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblMonth.setBounds(62, 204, 37, 14);
 		frame.getContentPane().add(lblMonth);
 		
 		JLabel lblDay = new JLabel("Day");
 		lblDay.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblDay.setBounds(109, 204, 29, 14);
 		frame.getContentPane().add(lblDay);
 		
 		JLabel lblYear = new JLabel("Year");
 		lblYear.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblYear.setBounds(156, 204, 29, 14);
 		frame.getContentPane().add(lblYear);
 		
 		nameBox = new JTextField();
 		nameBox.setBounds(208, 71, 94, 20);
 		frame.getContentPane().add(nameBox);
 		nameBox.setColumns(10);
 		
 		JLabel lblPatientNameOr = new JLabel("Patient Name");
 		lblPatientNameOr.setForeground(SystemColor.textHighlight);
 		lblPatientNameOr.setFont(new Font("Monospaced", Font.BOLD, 11));
 		lblPatientNameOr.setBounds(109, 74, 94, 14);
 		frame.getContentPane().add(lblPatientNameOr);
 		
 		JLabel lblPatientId = new JLabel("Patient ID");
 		lblPatientId.setForeground(SystemColor.textHighlight);
 		lblPatientId.setFont(new Font("Monospaced", Font.BOLD, 11));
 		lblPatientId.setBounds(109, 111, 89, 14);
 		frame.getContentPane().add(lblPatientId);
 		
 		patientIDBox = new JTextField();
 		patientIDBox.setColumns(10);
 		patientIDBox.setBounds(208, 108, 94, 20);
 		frame.getContentPane().add(patientIDBox);
 		
 		JLabel lblEnterPatientName = new JLabel("Enter Patient Name\r\n or Patient ID");
 		lblEnterPatientName.setBounds(169, 37, 201, 23);
 		frame.getContentPane().add(lblEnterPatientName);
 		lblUserExists.setForeground(Color.GREEN);
 		lblUserExists.setFont(new Font("Consolas", Font.ITALIC, 11));
 		
 		
 		lblUserExists.setBounds(325, 111, 89, 14);
 		frame.getContentPane().add(lblUserExists);
 		lblUserExists.setVisible(false);
 		
 		lblUserNotFound.setForeground(new Color(220, 20, 60));
 		lblUserNotFound.setFont(new Font("Consolas", Font.ITALIC, 11));
 		lblUserNotFound.setBounds(332, 111, 103, 14);
 		frame.getContentPane().add(lblUserNotFound);
 		lblUserNotFound.setVisible(false);
 		
 		JButton btnCheck = new JButton("Check");
 		btnCheck.setBounds(324, 70, 73, 23);
 		frame.getContentPane().add(btnCheck);
 		
 		JButton btnViewInformation = new JButton("view information");
 		btnViewInformation.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				frame.dispose();
				PatientDisplayInformation window  = new PatientDisplayInformation();
                window.frame.setVisible(true);
 			}
 		});
 		btnViewInformation.setBounds(29, 388, 89, 23);
 		frame.getContentPane().add(btnViewInformation);
 		btnCheck.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg1) 
 			{
 				String tempName;
 				int tempID;
 				if(nameBox.getText().length() > 0)
 				{
 					tempName = nameBox.getText();
 					if(Globals.userDatabase.searchUserName(tempName) != null)
 					{
 						lblUserNotFound.setVisible(false);
 						lblUserExists.setVisible(true);
 					}
 					else 
 					{
 						lblUserExists.setVisible(false);
 						lblUserNotFound.setVisible(true);
 					}
 				}
 				else	
 				{
 					if(patientIDBox.getText().length() > 0)
 					{
 						
 						tempID = Integer.parseInt(patientIDBox.getText().toString());
 						
 						if(Globals.userDatabase.searchUserID(tempID) != null)
 						{
 							lblUserNotFound.setVisible(false);
 							lblUserExists.setVisible(true);
 						}
 						else 
 						{
 							lblUserExists.setVisible(false);
 							lblUserNotFound.setVisible(true);
 						}
 					}
 				}
 				
 				
 			}
 		});
 		
 		
 		
 		
 	}
 }
 
 	
