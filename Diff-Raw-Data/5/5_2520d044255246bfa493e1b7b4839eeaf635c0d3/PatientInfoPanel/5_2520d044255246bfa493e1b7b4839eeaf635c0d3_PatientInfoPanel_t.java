 package hms.views;
 
 import java.text.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.text.*;
 
 import net.miginfocom.swing.MigLayout;
 
 import hms.util.Priority;
 import hms.models.*;
 
 public class PatientInfoPanel extends JPanel {
 	final private JLabel nameLabel = new JLabel("Name:");
 	final private JLabel phoneLabel = new JLabel("Phone:");
 	final private JLabel emailLabel = new JLabel("Email:");
 	final private JLabel healthcareNumberLabel = new JLabel("Health #:");
 	final private JLabel addressLabel = new JLabel("Address:");
 	final private JLabel birthdateLabel = new JLabel("Birthdate:");
 	final private JLabel genderLabel = new JLabel("Gender:");
 	final private JLabel priorityLabel = new JLabel("Priority:");
 	final private JLabel inHospitalLabel = new JLabel("In Hospital:");
 	
 	final private JLabel emergencyNameLabel = new JLabel("Name:");
 	final private JLabel emergencyPhoneLabel = new JLabel("Phone:");
 	final private JLabel emergencyEmailLabel = new JLabel("Email:");
 	
 	final private JLabel medicationsLabel = new JLabel("Medications:");
 	final private JLabel specialCareLabel = new JLabel("Special Care:");
 	final private JLabel historyLabel = new JLabel("History:");
 	final private JLabel commentsLabel = new JLabel("Comments:");
 	
 	final private JLabel wardLabel = new JLabel("Ward:");
 	final private JLabel roomLabel = new JLabel("Room:");
 	final private JLabel bedLabel = new JLabel("Bed:");
 	
 	final private JTextField nameField = new JTextField();
 	final private JTextField phoneField = new JTextField();
 	final private JTextField emailField = new JTextField();
 	final private JTextField healthcareNumberField = new JTextField();
 	final private JTextArea addressField = new JTextArea();
 	final private SimpleDateFormat birthdateFormat = new SimpleDateFormat("MM/dd/yyyy");
 	final private JFormattedTextField birthdateField = new JFormattedTextField(birthdateFormat);
 	final private JComboBox priorityDropdown = new JComboBox();
 	final private JCheckBox inHospitalCheckbox = new JCheckBox();
 	
 	final private JTextField emergencyNameField = new JTextField();
 	final private JTextField emergencyPhoneField = new JTextField();
 	final private JTextField emergencyEmailField = new JTextField();
 	
 	final private JTextArea medicationsField = new JTextArea();
 	final private JTextArea specialCareField = new JTextArea();
 	final private JTextArea historyField = new JTextArea();
 	final private JTextArea commentsField = new JTextArea();
 	
 	final private JRadioButton maleButton = new JRadioButton("Male");
 	final private JRadioButton femaleButton = new JRadioButton("Female");
 	final private ButtonGroup genderGroup = new ButtonGroup();
 	
 	final private JComboBox wardDropdown = new JComboBox();
 	final private JComboBox roomDropdown = new JComboBox();
 	final private JComboBox bedDropdown = new JComboBox();
 	
 	public PatientInfoPanel() {
 		initUI();
 	}
 	
 	/**
 	 * Initializes the components for this panel.
 	 */
 	private void initUI() {
 		genderGroup.add(maleButton);
 		genderGroup.add(femaleButton);
 		
 		wardDropdown.setModel(new DefaultComboBoxModel(Ward.getWardNames()));
 		
 		wardDropdown.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					roomDropdown.setModel(new DefaultComboBoxModel(Room.getRoomNumbers(wardDropdown.getSelectedIndex())));
 				}
 			}
 		});
 		
 		roomDropdown.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					bedDropdown.setModel(new DefaultComboBoxModel(Bed.getBedNumbers((Integer)roomDropdown.getSelectedItem())));
 				}
 			}
 		});
 		
 		priorityDropdown.addItem(Priority.HIGH);
 		priorityDropdown.addItem(Priority.MEDIUM);
 		priorityDropdown.addItem(Priority.LOW);
 		
 		this.setLayout(new MigLayout("fillx", "[label]rel[grow,fill][grow,fill]", "[]5[]"));
 		
 		addSeparator("General Information");
 		
 		this.add(nameLabel);
 		this.add(nameField, "span 2, wrap");
 		
 		this.add(phoneLabel);
 		this.add(phoneField, "span 2, wrap");
 		
 		this.add(healthcareNumberLabel);
 		this.add(healthcareNumberField, "span 2, wrap");
 		
 		this.add(birthdateLabel);
 		this.add(birthdateField, "span 2, wrap");
 		
 		this.add(emailLabel);
 		this.add(emailField, "span 2, wrap");
 		
 		this.add(addressLabel);
 		this.addressField.setRows(3);
 		this.add(addressField, "span 2, wrap");
 		
 		this.add(genderLabel);
 		this.add(maleButton);
 		this.add(femaleButton, "wrap");
 		
 		this.add(priorityLabel);
 		this.add(priorityDropdown, "span 2, wrap");
 		
 		this.add(inHospitalLabel);
 		this.add(inHospitalCheckbox, "span 2, wrap para");
 		
 		addSeparator("Emergency Contact Information");
 		
 		this.add(emergencyNameLabel);
 		this.add(emergencyNameField, "span 2, wrap");
 		
 		this.add(emergencyPhoneLabel);
 		this.add(emergencyPhoneField, "span 2, wrap");
 		
 		this.add(emergencyEmailLabel);
 		this.add(emergencyEmailField, "span 2, wrap para");
 		
 		addSeparator("Additional Information");
 		
 		this.add(medicationsLabel);
 		this.medicationsField.setRows(3);
 		this.add(medicationsField, "span 2, wrap");
 		
 		this.add(specialCareLabel);
 		this.specialCareField.setRows(3);
 		this.add(specialCareField, "span 2, wrap");
 		
 		this.add(historyLabel);
 		this.historyField.setRows(3);
 		this.add(historyField, "span 2, wrap");
 		
 		this.add(commentsLabel);
 		this.commentsField.setRows(3);
 		this.add(commentsField, "span 2, wrap para");
 		
 		addSeparator("Location");
 		
 		this.add(wardLabel);
 		this.add(wardDropdown, "span 2, wrap");
 		this.add(roomLabel);
 		this.add(roomDropdown, "span 2, wrap");
 		this.add(bedLabel);
 		this.add(bedDropdown, "span 2, wrap");
 		
 		setTextComponentBorders();
 	}
 	
 	/**
 	 * Sets all text component borders to the same style so we have a more unified look
 	 * across JTextFields and JTextAreas.
 	 */
 	private void setTextComponentBorders() {
 		for (Component comp : getComponents()) {
 			if (comp instanceof JTextComponent) {
 				JTextComponent textComp = (JTextComponent)comp;
 				textComp.setBorder(BorderFactory.createEtchedBorder());
 			}
 		}
 	}
 	
 	/**
 	 * Sets whether the text components on this panel are editable.
 	 * @param editable A boolean value to set whether the text components should be editable.
 	 */
 	public void setEditable(boolean editable) {
 		for (Component comp : getComponents()) {
 			if (comp instanceof JTextComponent) {
 				JTextComponent textComp = (JTextComponent)comp;
 				textComp.setEditable(editable);
 			}
 		}
 		maleButton.setEnabled(editable);
 		femaleButton.setEnabled(editable);
 		priorityDropdown.setEnabled(editable);
 		inHospitalCheckbox.setEnabled(editable);
 		wardDropdown.setEnabled(editable);
 		roomDropdown.setEnabled(editable);
 		bedDropdown.setEnabled(editable);
 	}
 	
 	/**
 	 * Clears all of the patient information from the panel.
 	 */
 	public void clearPatientInformation() {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				nameField.setText("");
 				phoneField.setText("");
 				emailField.setText("");
 				healthcareNumberField.setText("");
 				addressField.setText("");
 				birthdateField.setText("");
 				emergencyNameField.setText("");
 				emergencyPhoneField.setText("");
 				emergencyEmailField.setText("");
 				medicationsField.setText("");
 				specialCareField.setText("");
 				historyField.setText("");
 				commentsField.setText("");
 				maleButton.setSelected(false);
 				femaleButton.setSelected(false);
 				wardDropdown.setSelectedItem("Hospital");
 				roomDropdown.removeAllItems();
 				bedDropdown.removeAllItems();
 				priorityDropdown.setSelectedItem(Priority.HIGH);
 				inHospitalCheckbox.setSelected(false);
 			}
 		});
 	}
 	
 	/**
 	 * Loads the patient information into the form from the given patient.
 	 * @param patient The patient to take the information from.
 	 */
 	public void loadPatientInformation(Patient patient) {
 		final Patient finalPatient = patient;
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				nameField.setText(finalPatient.getName());
 				phoneField.setText(finalPatient.getPhoneNumber());
 				emailField.setText(finalPatient.getEmail());
 				healthcareNumberField.setText(finalPatient.getHealthcareNumber());
 				addressField.setText(finalPatient.getAddress());
 				birthdateField.setText(birthdateFormat.format(finalPatient.getBirthdate()));
 				emergencyNameField.setText(finalPatient.getEmergencyName());
 				emergencyPhoneField.setText(finalPatient.getEmergencyPhoneNumber());
 				emergencyEmailField.setText(finalPatient.getEmergencyEmail());
 				medicationsField.setText(finalPatient.getMedications());
 				specialCareField.setText(finalPatient.getSpecialCare());
 				historyField.setText(finalPatient.getHistory());
 				commentsField.setText(finalPatient.getComments());
 				if (finalPatient.getGender().equals("M")) {
 					maleButton.setSelected(true);
 					femaleButton.setSelected(false);
 				} else {
 					maleButton.setSelected(false);
 					femaleButton.setSelected(true);
 				}
 				wardDropdown.setSelectedIndex(finalPatient.getWard());
 				roomDropdown.addItem(finalPatient.getRoom());
 				roomDropdown.setSelectedItem(finalPatient.getRoom());
 				bedDropdown.addItem(finalPatient.getBed());
 				bedDropdown.setSelectedItem(finalPatient.getBed());
 				priorityDropdown.setSelectedItem(finalPatient.getPriority());
 				inHospitalCheckbox.setSelected(finalPatient.getInHospital());
 			}
 		});
 	}
 	
 	/**
 	 * Creates a new Patient object from the information present on the panel.
 	 * @return A Patient object corresponding to the information on the panel.
 	 */
 	public Patient patientFromInformation() throws ParseException {
 		return new Patient(healthcareNumberField.getText(),
 						   nameField.getText(),
 						   phoneField.getText(),
 						   emailField.getText(),
 						   maleButton.isSelected() ? "M" : "F",
 						   "",
 						   addressField.getText(),
 						   birthdateFormat.parse(birthdateField.getText()),
 						   medicationsField.getText(),
 						   specialCareField.getText(),
 						   historyField.getText(),
 						   commentsField.getText(),
 						   emergencyNameField.getText(),
 						   emergencyPhoneField.getText(),
 						   emergencyEmailField.getText(),
 						   inHospitalCheckbox.isSelected(),
 						   wardDropdown.getSelectedIndex(),
 						   (Integer)roomDropdown.getSelectedItem(),
 						   (Integer)bedDropdown.getSelectedItem(),
 						   (Priority)priorityDropdown.getSelectedItem());
 	}
 	
 	/**
 	 * Toggles the signifiers for required fields on the patient info panel.
 	 * @param showRequiredFields Boolean to determine whether the required fields should be signified
 	 */
 	public void signifyRequiredFields(boolean showRequiredFields) {
 		String signifier = "* ";
 		if (showRequiredFields) {
 			nameLabel.setText(signifier + nameLabel.getText());
 			healthcareNumberLabel.setText(signifier + healthcareNumberLabel.getText());
 			birthdateLabel.setText(signifier + birthdateLabel.getText());
 			bedLabel.setText(signifier + bedLabel.getText());
 		} else {
 			nameLabel.setText(nameLabel.getText().replaceFirst(signifier, ""));
 			healthcareNumberLabel.setText(healthcareNumberLabel.getText().replaceFirst(signifier, ""));
 			birthdateLabel.setText(birthdateLabel.getText().replaceFirst(signifier, ""));
 			bedLabel.setText(bedLabel.getText().replaceFirst(signifier, ""));
 		}
 	}
 	
 	/**
 	 * Validates the fields in the panel and returns a boolean value of whether
 	 * all the fields are valid. Also marks all invalid field labels in red.
 	 * @return true if the fields are all valid; false otherwise
 	 */
 	public boolean validateInformation() {
 		boolean validated = true;
 		
 		if (nameField.getText().trim().equals("")) {
 			nameLabel.setForeground(Color.RED);
 			validated = false;
 		} else {
 			nameLabel.setForeground(Color.BLACK);
 		}
 		
		if (healthcareNumberField.getText().equals("")){
 			healthcareNumberLabel.setForeground(Color.RED);
 			validated = false;
 		}
 		else {
 			healthcareNumberLabel.setForeground(Color.BLACK);
 		}
 		
 		try {
 			birthdateFormat.parse(birthdateField.getText());
 			birthdateLabel.setForeground(Color.BLACK);
 		} catch (ParseException pe) {
 			validated = false;
 			birthdateLabel.setForeground(Color.RED);
 		}
 
		if (bedDropdown.getSelectedItem() == null) {
 			bedLabel.setForeground(Color.RED);
 			validated = false;
 		}
 		else {
 			bedLabel.setForeground(Color.BLACK);
 		}
 		
 		return validated;
 	}
 	
 	/**
 	 * Creates a form separator.
 	 * @param label The label to use on the separator.
 	 */
 	private void addSeparator(String message) {
 		JLabel label = new JLabel(message);
 		Font font = label.getFont();
 		label.setFont(font.deriveFont(font.getStyle() ^ Font.BOLD));
 		this.add(label, "split, span, gapbottom 10");
 		this.add(new JSeparator(), "growx, wrap, gapbottom 10");
 	}
 }
