 package GUI;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import main.BlasterCardListener;
 import main.Machine;
 import main.SystemAdministrator;
 import main.Tool;
 import main.User;
 
 public class LogInAnotherUserPanel extends ContentPanel {
 
 	private JButton saveButton;
 	private JButton goButton;
 	private JButton logOutUser;
 	private ButtonListener buttonListener;
 	private JTextField cwidField;
 	private String start = ";984000017";
 	private String error = "E?";
 
 	private JScrollPane scroller;
 	private JPanel selectionPanel;
 	private JPanel machines;
 	private JPanel availableTools;
 	private JPanel checkedOutTools;
 	private User user;
 	private User current;
 
 	public LogInAnotherUserPanel() {
 		// All the fonts are in ContentPanel.
 		super("Log In Another User");
 
 		current = Driver.getAccessTracker().getCurrentUser();
 
 		buttonListener = new ButtonListener();
 
 		JLabel cwidLabel = new JLabel("Enter CWID:");
 		cwidField = new JTextField();
 
 		cwidField.setFont(textFont);
 		cwidField.addActionListener(buttonListener);
 
 		cwidLabel.setFont(borderFont);
 
 		goButton = new JButton("Go");
 		goButton.setFont(buttonFont);
 		goButton.addActionListener(buttonListener);
 
 		selectionPanel = new JPanel(new GridLayout(1, 3));
 
 		machines = new JPanel();
 		availableTools = new JPanel();
 		checkedOutTools = new JPanel();
 
 		machines.setLayout(new GridLayout(0, 1));
 		availableTools.setLayout(new GridLayout(0, 1));
 		checkedOutTools.setLayout(new GridLayout(0, 1));
 
 		machines.setBorder(new TitledBorder("Select Machines"));
 		availableTools.setBorder(new TitledBorder("Check Out Tools"));
 		checkedOutTools.setBorder(new TitledBorder("Return Tools"));
 
 		selectionPanel.add(machines);
 		selectionPanel.add(availableTools);
 		selectionPanel.add(checkedOutTools);
 
 		logOutUser = new JButton("Log User Out");
 		logOutUser.setFont(buttonFont);
 		logOutUser.addActionListener(buttonListener);
 
 		scroller = new JScrollPane(selectionPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 
 		TitledBorder border = new TitledBorder("Selection Options");
 		border.setTitleFont(borderFont);
 		scroller.setBorder(border);
 
 		JPanel cwidPanel = new JPanel(new GridLayout(1, 3));
 
 		cwidPanel.add(cwidLabel);
 		cwidPanel.add(cwidField);
 		cwidPanel.add(goButton);
 
 		saveButton = new JButton("Save");
 		saveButton.setFont(buttonFont);
 		saveButton.addActionListener(buttonListener);
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.6;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 1;
 		add(cwidPanel, c);
 
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 0.0;
 		c.weighty = 0.1;
 		c.gridwidth = 1;
 		c.gridx = 1;
 		c.gridy = 2;
 		add(logOutUser, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weightx = 0.0;
 		c.weighty = 0.5;
 		c.gridwidth = 1;
 		c.gridx = 1;
 		c.gridy = 3;
 		add(scroller, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weightx = 0.0;
 		c.weighty = 0.1;
 		c.gridwidth = 1;
 		c.gridx = 1;
 		c.gridy = 4;
 		add(saveButton, c);
 
 		c.weighty = 0.1;
 		c.gridy = 5;
 		add(new JPanel(), c);
 	}
 
 	public void showMessage(String message) {
 		JOptionPane.showMessageDialog(this, message);
 	}
 
 	private void showMachines() {
 		machines.removeAll();
 		for ( Machine m : user.getCertifiedMachines() ) {
 			JCheckBox cb = new JCheckBox(m.getName() + " [" + m.getID() + "]");
 			cb.setHorizontalAlignment(JCheckBox.LEFT);
 			cb.setFont(borderFont);
 			machines.add(cb);
 		}
 	}
 
 	private void showaAvailableTools() {
 		availableTools.removeAll();
 		for ( Tool t : Driver.getAccessTracker().getTools()) {
 			JCheckBox cb = new JCheckBox(t.getName() + " [" + t.getUPC() + "]");
 			cb.setHorizontalAlignment(JCheckBox.LEFT);
 			cb.setFont(borderFont);
 			if (!user.getToolsCheckedOut().contains(t))
 				availableTools.add(cb);
 			if (t.isCheckedOut()) {
 				cb.setEnabled(false);
 			}
 		}
 	}
 
 	private void showCheckedOutTools() {
 		checkedOutTools.removeAll();
 		for ( Tool t : user.getToolsCheckedOut()) {
 			JCheckBox cb = new JCheckBox(t.getName() + " [" + t.getUPC() + "]");
 			cb.setHorizontalAlignment(JCheckBox.LEFT);
 			cb.setFont(borderFont);
 			checkedOutTools.add(cb);
 		}
 	}
 
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if ( e.getSource() == saveButton) {
 				if ( user == null ) {
 					showMessage("Please enter the user's CWID.");
 				}
 				else {
 					ArrayList<Machine> machinesSelected = new ArrayList<Machine>();
 
 					for ( int i = 0; i < machines.getComponentCount(); ++i ) {
 						JCheckBox cb = (JCheckBox) machines.getComponent(i);
 						if ( cb.isSelected() ) {
 							String s = cb.getText();
 							s = s.substring(s.indexOf('[') + 1, s.indexOf(']'));
 							for ( Machine m : Driver.getAccessTracker().getMachines() ) {
 								String ID = m.getID();
 								if ( s.equals(ID) ) {
 									machinesSelected.add(m);
 								}
 							}
 						}
 					}
 
 					ArrayList<Tool> availableToolsSelected = new ArrayList<Tool>();
 
 					for ( int i = 0; i < availableTools.getComponentCount(); ++i ) {
 						JCheckBox cb = (JCheckBox) availableTools.getComponent(i);
 						if ( cb.isSelected() ) {
 							String s = cb.getText();
 							s = s.substring(s.indexOf('[') + 1, s.indexOf(']'));
 							for ( Tool t : Driver.getAccessTracker().getTools() ) {
 								String UPC = t.getUPC();
 								if ( s.equals(UPC) ) {
 									availableToolsSelected.add(t);
 								}
 							}
 						}
 					}
 
 					ArrayList<Tool> checkedOutToolsSelected = new ArrayList<Tool>();
 
 					for ( int i = 0; i < checkedOutTools.getComponentCount(); ++i ) {
 						JCheckBox cb = (JCheckBox) checkedOutTools.getComponent(i);
 						if ( cb.isSelected() ) {
 							String s = cb.getText();
 							s = s.substring(s.indexOf('[') + 1, s.indexOf(']'));
 							for ( Tool t : Driver.getAccessTracker().getTools() ) {
 								String UPC = t.getUPC();
 								if ( s.equals(UPC) ) {
 									checkedOutToolsSelected.add(t);
 								}
 							}
 						}
 					}
 
 					for (Machine m : machinesSelected) {
 						user.useMachine(m);
 					}
 					user.getCurrentEntry().addMachinesUsed(machinesSelected);
 					for (Tool t : availableToolsSelected) {
 						user.checkoutTool(t);
 					}
 					user.getCurrentEntry().addToolsCheckedOut(availableToolsSelected);
 					user.returnTools(checkedOutToolsSelected);
 					user.getCurrentEntry().addToolsReturned(checkedOutToolsSelected);
 
 					Driver.getAccessTracker().setCurrentUser(current);
 
 				}
 				clearFields();
 				System.out.println(Driver.getAccessTracker().getCurrentUsers());
 
 			} else if ( e.getSource() == goButton || e.getSource() == cwidField ) {
 				String input = BlasterCardListener.strip(cwidField.getText());
 
 				user = Driver.getAccessTracker().processLogIn(input);
 				cwidField.setText(user.getFirstName() + " " + user.getLastName());
 
 				showMachines();
 				showaAvailableTools();
 				showCheckedOutTools();
 			} else if ( e.getSource() == logOutUser) {
 				if (user != null)  {
 					Driver.getAccessTracker().processLogOut(user.getCWID());
 					clearFields();
 					System.out.println(Driver.getAccessTracker().getCurrentUsers());
 				}
 			}
			
			Driver.getAccessTracker().setCurrentUser(current);
 		}
 	}
 
 	// Clears all the text fields to empty, and set the user null.
 	private void clearFields() {
 		cwidField.setText("");
 		user = null;
 		machines.removeAll();
 		availableTools.removeAll();
 		checkedOutTools.removeAll();
		
		Driver.getAccessTracker().setCurrentUser(current);
 	}
 }
