 package GUI;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 
 import main.Machine;
 import main.MachineComparator;
 import main.OracleConnection;
 import main.SystemAdministrator;
 import main.User;
 import main.Validator;
 
 public class AddUsersPanel extends ContentPanel {
 
 	private JButton saveButton;
 	private ButtonListener buttonListener;
 	private JTextField firstNameField;
 	private JTextField lastNameField;
 	private JTextField userIDField;
 	private JTextField emailField;
 	private JTextField departField;
 	private JPanel permissionsPanel;
 	private JScrollPane scroller;
 	private User addedUser;
 	private OracleConnection connection;
 	private JCheckBox selectAllBox;
 
 	public AddUsersPanel() {
 
 		super("Add a New User");
 		// Tries to connect to the Oracle database that contains student information.
 		// The Oracle database is a dummy one for now, and will need to
 		// change to the actual Mines database.
 		try {
 			connection = new OracleConnection();
 			connection.getConnection();
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 
 		buttonListener = new ButtonListener();
 
 		JLabel firstNameLabel = new JLabel("First Name:");
 		JLabel lastNameLabel = new JLabel("Last Name:");
 		JLabel userIDLabel = new JLabel("User CWID:");
 		JLabel emailLabel = new JLabel("Mines Email:");
 		JLabel departLabel = new JLabel("Dept. (i.e. EECS):");
 
 		firstNameLabel.setFont(textFont);
 		lastNameLabel.setFont(textFont);
 		userIDLabel.setFont(textFont);
 		emailLabel.setFont(textFont);
 		departLabel.setFont(textFont);
 
 		firstNameField = new JTextField();
 		lastNameField = new JTextField();
 		userIDField = new JTextField();
 		emailField = new JTextField();
 		departField = new JTextField();
 
 		firstNameField.setFont(textFont);
 		lastNameField.setFont(textFont);
 		userIDField.setFont(textFont);
 		emailField.setFont(textFont);
 		departField.setFont(textFont);
 
 		departField.addActionListener(buttonListener);
 		userIDField.addActionListener(buttonListener);
 
 		selectAllBox = new JCheckBox("Select All");
 		selectAllBox.setFont(textFont);
 		selectAllBox.setHorizontalAlignment(JCheckBox.CENTER);
 		selectAllBox.addActionListener(buttonListener);
 
 		permissionsPanel = new JPanel(new GridLayout(0, 2));
 		
 		// sorts the machines list
 		ArrayList<Machine> sorted = new ArrayList<Machine>();
 		sorted = Driver.getAccessTracker().getMachines();
 		Collections.sort(sorted, new MachineComparator());
 				
 		for (Machine m : sorted) {
 			JCheckBox cb = new JCheckBox(m.getName() + " [" + m.getID() + "]");
 			cb.setHorizontalAlignment(JCheckBox.LEFT);
 			cb.setFont(checkBoxFont);
 			permissionsPanel.add(cb);
 		}
 
 		scroller = new JScrollPane(permissionsPanel,
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 		TitledBorder border = new TitledBorder("Add Certifications");
 		border.setTitleFont(borderFont);
 		scroller.setBorder(border);
 
 		JPanel firstNamePanel = new JPanel(new GridLayout(1, 2));
 		JPanel lastNamePanel = new JPanel(new GridLayout(1, 2));
 		JPanel userIDPanel = new JPanel(new GridLayout(1, 2));
 
 		// Manually type the email and department.
 		JPanel emailPanel = new JPanel(new GridLayout(1, 2));
 		JPanel departmentPanel = new JPanel(new GridLayout(1, 2));
 
 		firstNamePanel.add(firstNameLabel);
 		firstNamePanel.add(firstNameField);
 
 		lastNamePanel.add(lastNameLabel);
 		lastNamePanel.add(lastNameField);
 
 		userIDPanel.add(userIDLabel);
 		userIDPanel.add(userIDField);
 		
 	    emailPanel.add(emailLabel);
 	    emailPanel.add(emailField);
 
 	    departmentPanel.add(departLabel);
 	    departmentPanel.add(departField);
 
 		JPanel dataPanel = new JPanel(new GridBagLayout());
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.2;
 		c.gridx = 0;
 		c.gridy = 0;
 		dataPanel.add(firstNamePanel, c);
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.2;
 		c.gridx = 0;
 		c.gridy = 1;
 		dataPanel.add(lastNamePanel, c);
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.2;
 		c.gridx = 0;
 		c.gridy = 2;
 		dataPanel.add(userIDPanel, c);
 
 		c.gridy = 3;
 		dataPanel.add(emailPanel, c);
 
 		c.gridy = 4;
 		dataPanel.add(departmentPanel, c);
 
 		saveButton = new JButton("Save");
 
 		saveButton.setFont(buttonFont);
 		saveButton.addActionListener(buttonListener);
 
 		// ///////////////////////////////////////////////////////////////////////////////////////////////
 		/********************
 		 * All weighty values should add up to 0.9
 		 * *********************************** All weightx values should add up
 		 * to 0.8
 		 **********************************/
 		// ///////////////////////////////////////////////////////////////////////////////////////////////
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.25;
 		c.gridx = 1;
 		c.gridy = 1;
 		add(dataPanel, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.5;
 		c.gridx = 1;
 		c.gridy = 2;
 		add(scroller, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.05;
 		c.gridx = 1;
 		c.gridy = 3;
 		add(selectAllBox, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.05;
 		c.gridx = 1;
 		c.gridy = 4;
 		c.gridwidth = 1;
 		add(saveButton, c);
 
 		c.weighty = 0.05;
 		c.gridy = 5;
 		add(new JPanel(), c);
 
 		scroller.setMaximumSize(new Dimension(scroller.getWidth(), scroller.getHeight()));
 		scroller.setMaximumSize(scroller.getPreferredSize());
 		scroller.getVerticalScrollBar().setUnitIncrement(13);
 
 	}
 
 	private boolean saveUser(ArrayList<Machine> machines) {
 		String firstName = firstNameField.getText();
 		String lastName = lastNameField.getText();
 		String cwid = userIDField.getText();
 		String email = emailField.getText();
 		String department = departField.getText();
 
 		SystemAdministrator admin = ((SystemAdministrator) Driver
 				.getAccessTracker().getCurrentUser());
 
 		// Gets the user with same CWID from the Oracle database if that user
 		// exists.
 		try {
 			addedUser = admin.loadNewUser(cwid, connection);
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 
 		if (addedUser == null) {
 			// If the user being added does not exist in Oracle database,
 			// ask the System Administrator if he/she still wants to add.
 			String message = "Our records show that the CWID " + cwid
 					+ " either does not exist or is not an active "
 					+ "student at Mines.\nDo you still want to add "
 					+ firstName + " " + lastName
 					+ " to the database?\n\nThis action is highly discouraged.";
 
 			if (confirm(message)) {
 				addedUser = new User(firstName, lastName, cwid, email, department);
 				if (admin.addUser(addedUser)) {
 					admin.updateCertifications(addedUser, machines);
 					return true;
 				} else {
 					clearFields();
 				}
 			}
 		} else {
 			if (firstName.equals(addedUser.getFirstName())
 					&& lastName.equals(addedUser.getLastName())) {
 				if (admin.addUser(addedUser)) {
 					admin.updateCertifications(addedUser, machines);
 					return true;
 				} else {
 					clearFields();
 				}
 			} else {
 				// If the name in Oracle database is different from what was
 				// entered, ask the System Administrator if this is the person he/she
 				// wants to add.
 				String message = "Our records show that the name associated with this CWID is "
 						+ addedUser.getFirstName()
 						+ " "
 						+ addedUser.getLastName()
 						+ ".\n"
 						+ "Is this the user you meant to add?\n\nClick Yes to add this user "
 						+ "to the database, or No/Cancel to go back.";
 				if (confirm(message)) {
 					if (admin.addUser(addedUser)) {
 						admin.updateCertifications(addedUser, machines);
 						return true;
 					} else {
 						clearFields();
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	// Clears the text fields to empty, and deselect the check box.
 	private void clearFields() {
 		firstNameField.setText("");
 		lastNameField.setText("");
 		userIDField.setText("");
 		emailField.setText("");
 		departField.setText("");
 
 		selectAllBox.setSelected(false);
 		for (int i = 0; i < permissionsPanel.getComponentCount(); ++i) {
 			((JCheckBox) permissionsPanel.getComponent(i)).setSelected(false);
 		}
 	}
 	
 	public boolean validFields() {
 		if (firstNameField.getText().equals("")	|| userIDField.getText().equals("")	|| lastNameField.getText().equals("") ) {
 			showMessage("Please fill in the user's name and CWID.");
 			return false;
 		} else if (!(Validator.isValidCWID(userIDField.getText()))) {
			showMessage("Please enter an " + Validator.CWID_LENGTH + "-digit CWID, numbers only.");
 			return false;
 		} else if ( !(Validator.isValidEmail(emailField.getText()) || emailField.getText().equals("")) ) {
 			showMessage("Please enter a valid email address");
 			return false;
 		} else if ( !(Validator.isValidDeptCode(departField.getText()) || departField.getText().equals("")) ) {
 			showMessage("Please enter a department code of length " + Validator.DEPT_CODE_LENGTH );
 			return false;
 		}
 		return true;
 	}
 	
 	public void toggleAll() {
 		if (selectAllBox.isSelected()) {
 			for (int i = 0; i < permissionsPanel.getComponentCount(); ++i) {
 				JCheckBox cb = (JCheckBox) permissionsPanel.getComponent(i);
 				cb.setSelected(true);
 			}
 		} else {
 			for (int i = 0; i < permissionsPanel.getComponentCount(); ++i) {
 				JCheckBox cb = (JCheckBox) permissionsPanel.getComponent(i);
 				cb.setSelected(false);
 			}
 		}
 	}
 
 	public void save() {
 		
 		ArrayList<String> added = new ArrayList<String>();
 		ArrayList<Machine> machines = new ArrayList<Machine>();
 
 		// Adding new user requires first name, last name, and CWID.
 		if ( !validFields() ) {
 			return;
 		}
 		
 		for (int i = 0; i < permissionsPanel.getComponentCount(); ++i) {
 			JCheckBox cb = (JCheckBox) permissionsPanel.getComponent(i);
 			if (cb.isSelected()) {
 				for (Machine m : Driver.getAccessTracker().getMachines()) {
 					String s = cb.getText();
 					s = s.substring(s.indexOf('[') + 1,s.indexOf(']'));
 					String ID = m.getID();
 					if (s.equals(ID)) {
 						added.add(m.getName() + " [" + ID + "]");
 						machines.add(m);
 					}
 				}
 			}
 		}
 		
 		String list = "";
 		for (String s : added) {
 			list += s + "\n";
 		}
 		String message = "Are you sure you want to give "
 				+ firstNameField.getText() + " " + lastNameField.getText()
 				+ " " + "these permissions?\n\n" + list;
 		
 		if (confirm(message) && saveUser(machines)) {
 			clearFields();
 		}
 	}
 	
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// Selects/deselects all the machines when selectAllBox is
 			// selected/deselected.
 			if (e.getSource() == selectAllBox) {
 				toggleAll();
 			} else if (e.getSource() == saveButton || e.getSource() == departField || e.getSource() == userIDField) {
 				save();
 			}
 		}
 	}
 }
