 package com.mediware.gui.doctor;
 
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import com.mediware.arch.IO;
 import com.mediware.arch.mData;
 import com.mediware.arch.Enums.mType;
 import com.mediware.arch.Enums.partition;
 
 @SuppressWarnings("serial")
 public class PatientReport extends JPanel implements ActionListener {
 	private JTextField textFieldDOB;
 	private JTextField textFieldFirstName;
 	private JTextField textFieldMiddleName;
 	private JTextField textFieldLastName;
 	private JTextField textFieldHeight;
 	private JTextField textFieldWeight;
 	private JButton btnTakeVitals;
 	private JButton btnViewHistory;
 	private JButton btnComments;
 	private JButton btnViewProfile;
 	private JButton btnCancel;
 	private IO io;
 
 	/**
 	 * Create the panel.
 	 * @param cndIO 
 	 */
 	public PatientReport(IO cndIO, String []labels) {
 		
 		this.io = cndIO;
 		
 		setBorder(new TitledBorder(null, "Patient Menu", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0, 133, 61, 98, 51, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JLabel lblName = new JLabel("Name:");
 		GridBagConstraints gbc_lblName = new GridBagConstraints();
 		gbc_lblName.anchor = GridBagConstraints.EAST;
 		gbc_lblName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblName.gridx = 1;
 		gbc_lblName.gridy = 1;
 		add(lblName, gbc_lblName);
 		
 		textFieldFirstName = new JTextField();
 		textFieldFirstName.setEditable(false);
 		GridBagConstraints gbc_textFieldFirstName = new GridBagConstraints();
 		gbc_textFieldFirstName.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldFirstName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldFirstName.gridx = 2;
 		gbc_textFieldFirstName.gridy = 1;
 		add(textFieldFirstName, gbc_textFieldFirstName);
 		textFieldFirstName.setColumns(10);
 		
 		textFieldMiddleName = new JTextField();
 		textFieldMiddleName.setEditable(false);
 		GridBagConstraints gbc_textFieldMiddleName = new GridBagConstraints();
 		gbc_textFieldMiddleName.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldMiddleName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldMiddleName.gridx = 3;
 		gbc_textFieldMiddleName.gridy = 1;
 		add(textFieldMiddleName, gbc_textFieldMiddleName);
 		textFieldMiddleName.setColumns(10);
 		
 		textFieldLastName = new JTextField();
 		textFieldLastName.setEditable(false);
 		GridBagConstraints gbc_textFieldLastName = new GridBagConstraints();
 		gbc_textFieldLastName.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldLastName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldLastName.gridx = 4;
 		gbc_textFieldLastName.gridy = 1;
 		add(textFieldLastName, gbc_textFieldLastName);
 		textFieldLastName.setColumns(10);
 		
 		JLabel lblFirst = new JLabel("First");
 		lblFirst.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		GridBagConstraints gbc_lblFirst = new GridBagConstraints();
 		gbc_lblFirst.insets = new Insets(0, 0, 5, 5);
 		gbc_lblFirst.gridx = 2;
 		gbc_lblFirst.gridy = 2;
 		add(lblFirst, gbc_lblFirst);
 		
 		JLabel lblMiddle = new JLabel("Middle");
 		lblMiddle.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		GridBagConstraints gbc_lblMiddle = new GridBagConstraints();
 		gbc_lblMiddle.insets = new Insets(0, 0, 5, 5);
 		gbc_lblMiddle.gridx = 3;
 		gbc_lblMiddle.gridy = 2;
 		add(lblMiddle, gbc_lblMiddle);
 		
 		JLabel lblLast = new JLabel("Last");
 		lblLast.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		GridBagConstraints gbc_lblLast = new GridBagConstraints();
 		gbc_lblLast.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLast.gridx = 4;
 		gbc_lblLast.gridy = 2;
 		add(lblLast, gbc_lblLast);
 		
 		JLabel lblDOB = new JLabel("Date of Birth:");
 		GridBagConstraints gbc_lblDOB = new GridBagConstraints();
 		gbc_lblDOB.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDOB.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;
 		gbc_lblDOB.gridx = 1;
 		gbc_lblDOB.gridy = 3;
 		add(lblDOB, gbc_lblDOB);
 		
 		textFieldDOB = new JTextField();
 		textFieldDOB.setEditable(false);
 		GridBagConstraints gbc_textFieldDOB = new GridBagConstraints();
 		gbc_textFieldDOB.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldDOB.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldDOB.gridx = 2;
 		gbc_textFieldDOB.gridy = 3;
 		add(textFieldDOB, gbc_textFieldDOB);
 		textFieldDOB.setColumns(10);
 		
 		JLabel lblHeight = new JLabel("Height:");
 		GridBagConstraints gbc_lblHeight = new GridBagConstraints();
 		gbc_lblHeight.anchor = GridBagConstraints.EAST;
 		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
 		gbc_lblHeight.gridx = 3;
 		gbc_lblHeight.gridy = 3;
 		add(lblHeight, gbc_lblHeight);
 		
 		textFieldHeight = new JTextField();
 		textFieldHeight.setEditable(false);
 		GridBagConstraints gbc_textFieldHeight = new GridBagConstraints();
 		gbc_textFieldHeight.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldHeight.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldHeight.gridx = 4;
 		gbc_textFieldHeight.gridy = 3;
 		add(textFieldHeight, gbc_textFieldHeight);
 		textFieldHeight.setColumns(10);
 		
 		JLabel lblWeight = new JLabel("Weight:");
 		GridBagConstraints gbc_lblWeight = new GridBagConstraints();
 		gbc_lblWeight.anchor = GridBagConstraints.EAST;
 		gbc_lblWeight.insets = new Insets(0, 0, 5, 5);
 		gbc_lblWeight.gridx = 3;
 		gbc_lblWeight.gridy = 4;
 		add(lblWeight, gbc_lblWeight);
 		
 		textFieldWeight = new JTextField();
 		textFieldWeight.setEditable(false);
 		GridBagConstraints gbc_textFieldWeight = new GridBagConstraints();
 		gbc_textFieldWeight.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldWeight.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldWeight.gridx = 4;
 		gbc_textFieldWeight.gridy = 4;
 		add(textFieldWeight, gbc_textFieldWeight);
 		textFieldWeight.setColumns(10);
 		
 		btnTakeVitals = new JButton("Take Vitals");
 		GridBagConstraints gbc_btnTakeVitals = new GridBagConstraints();
 		gbc_btnTakeVitals.insets = new Insets(0, 0, 5, 5);
 		gbc_btnTakeVitals.gridx = 2;
 		gbc_btnTakeVitals.gridy = 6;
 		add(btnTakeVitals, gbc_btnTakeVitals);
 		btnTakeVitals.addActionListener (this);
 		
 		btnViewHistory = new JButton("View History");
 		GridBagConstraints gbc_btnViewHistory = new GridBagConstraints();
 		gbc_btnViewHistory.insets = new Insets(0, 0, 5, 5);
 		gbc_btnViewHistory.gridx = 2;
 		gbc_btnViewHistory.gridy = 7;
 		add(btnViewHistory, gbc_btnViewHistory);
 		btnViewHistory.addActionListener (this);
 		
 		btnComments = new JButton("Send Message");
 		GridBagConstraints gbc_btnComments = new GridBagConstraints();
 		gbc_btnComments.insets = new Insets(0, 0, 5, 5);
 		gbc_btnComments.gridx = 2;
 		gbc_btnComments.gridy = 8;
 		add(btnComments, gbc_btnComments);
 		btnComments.addActionListener (this);
 		
 		btnViewProfile = new JButton("View/Edit Profile");
 		GridBagConstraints gbc_btnViewProfile = new GridBagConstraints();
 		gbc_btnViewProfile.insets = new Insets(0, 0, 5, 5);
 		gbc_btnViewProfile.gridx = 2;
 		gbc_btnViewProfile.gridy = 9;
 		add(btnViewProfile, gbc_btnViewProfile);
 		btnViewProfile.addActionListener (this);
 		
 		btnCancel = new JButton("Cancel");
 		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
 		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
 		gbc_btnCancel.gridx = 4;
 		gbc_btnCancel.gridy = 9;
 		add(btnCancel, gbc_btnCancel);
 		btnCancel.addActionListener (this);
 
 		//Set for the textfields
 		//			0			1				2				3				4				5
 		// {oc1.getFname(), oc1.getMname(), oc1.getLname(), oc1.getHeight(), oc1.getWeight(), oc1.getDob()};
 		
 		textFieldFirstName.setText(labels[0]);
 		textFieldMiddleName.setText(labels[1]);
 		textFieldLastName.setText(labels[2]);
 		textFieldHeight.setText(labels[3]);
 		textFieldWeight.setText(labels[4]);
 		textFieldDOB.setText(labels[5]);
 		
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		// Check which button was clicked on
 		if (event.getSource() == btnTakeVitals)
 		{	// Patient Search button was clicked
 			int[] intParams = {1};
 			String[] stringParams = new String[0];
 			mData messageData = new mData(intParams, stringParams);
 			partition[] subscribers = {partition.CND};
 			io.createMessageToSend(partition.CND, subscribers, messageData, mType.cndDisplayPatientVitalsPanel);
         }
 		else if (event.getSource() == btnViewHistory)
 		{	// Messages / Alerts button was clicked
 			int[] intParams = new int[0];
 			String[] stringParams = new String[1];
 			mData messageData = new mData(intParams, stringParams);
 			partition[] subscribers = {partition.CND};
 			io.createMessageToSend(partition.CND, subscribers, messageData, mType.cndDisplayPatientHealthHistory);
         }
 		else if (event.getSource() == btnComments)
 		{	// Messages / Alerts button was clicked
 			int[] intParams = {1};
 			String[] stringParams = new String[0];
 			mData messageData = new mData(intParams, stringParams);
 			partition[] subscribers = {partition.CND};
 			io.createMessageToSend(partition.CND, subscribers, messageData, mType.cndDisplaySendMessage);
         }
 		else if (event.getSource() == btnViewProfile)
 		{	// Messages / Alerts button was clicked
 			int[] intParams = {1};
 			String[] stringParams = new String[0];
 			mData messageData = new mData(intParams, stringParams);
 			partition[] subscribers = {partition.SYS};
 			io.createMessageToSend(partition.SYS, subscribers, messageData, mType.sysPatientEditProfileRequest);
         }
 		else if (event.getSource() == btnCancel)
 		{	// Messages / Alerts button was clicked
 			int[] intParams = new int[0];
 			String[] stringParams = new String[0];
 			mData messageData = new mData(intParams, stringParams);
			partition[] subscribers = {partition.SYS};
			io.createMessageToSend(partition.CND, subscribers, messageData, mType.sysGoToMenu);
         }
 		
 	}
 }
