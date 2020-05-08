 package GUI;
 
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import java.awt.GridLayout;
 import java.awt.BorderLayout;
 import java.awt.event.ActionListener;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import java.awt.Component;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JSplitPane;
 import javax.swing.JOptionPane;
 import javax.swing.JFileChooser;
 import java.awt.event.ActionEvent;
 import java.awt.CardLayout;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import java.io.File;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.JTextArea;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.UIManager;
 
 /**
 *
 * Authorship: Aaron Harris
 */
 
 public class PartsManagerPanel extends JPanel {
 	private JPanel pnlButtons;
 	private JPanel pnlView;
 	private JPanel pnlEdit;
 	private JPanel pnlAdd;
 	private JComboBox cbPart;
 	private String[] backupFields; // used for temporarily storing old field data in case a user wants to revert
 	private final JFileChooser fc;
 	private JTextField tfName;
 	
 	/**
 	 * Create the panel.
 	 */
 	public PartsManagerPanel() {
 		setLayout(new GridLayout(1, 1));
 		fc = new JFileChooser();
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		add(tabbedPane);
 		JPanel managerPanel = new JPanel();
 		tabbedPane.addTab("Part Manager", managerPanel);
 		managerPanel.setLayout(new BorderLayout(0, 0));
 		
 		JPanel pnlPartChooser = new JPanel();
 		managerPanel.add(pnlPartChooser, BorderLayout.NORTH);
 		
 		cbPart = new JComboBox();
 		cbPart.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent ie) {
 				viewPart((String) ie.getItem());
 			}
 		});
 		pnlPartChooser.add(cbPart);
 		
 		JButton btnNewPart = new JButton("New Part Type");
 		btnNewPart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				showAddPanel();
 			}
 		});
 		pnlPartChooser.add(btnNewPart);
 		
 		JPanel pnlForm = new JPanel();
 		pnlForm.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		managerPanel.add(pnlForm, BorderLayout.CENTER);
 		GridBagLayout gbl_pnlForm = new GridBagLayout();
 		gbl_pnlForm.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0};
 		gbl_pnlForm.columnWeights = new double[]{0.0, 0.0};
 //		gbl_pnlForm.columnWidths = new int[]{0, 0, 0};
 //		gbl_pnlForm.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
 //		gbl_pnlForm.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 //		gbl_pnlForm.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		pnlForm.setLayout(gbl_pnlForm);
 		
 		JLabel lblNumber = new JLabel("Part No:");
 		GridBagConstraints gbc_lblNumber = new GridBagConstraints();
 		gbc_lblNumber.anchor = GridBagConstraints.EAST;
 		gbc_lblNumber.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNumber.gridx = 0;
 		gbc_lblNumber.gridy = 0;
 		pnlForm.add(lblNumber, gbc_lblNumber);
 		
 		JSpinner spinner = new JSpinner();
 		spinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(0), null, new Integer(1)));
 		GridBagConstraints gbc_spinner = new GridBagConstraints();
 		gbc_spinner.anchor = GridBagConstraints.WEST;
 		gbc_spinner.insets = new Insets(0, 0, 5, 0);
 		gbc_spinner.gridx = 1;
 		gbc_spinner.gridy = 0;
 		pnlForm.add(spinner, gbc_spinner);
 		
 		JLabel lblName = new JLabel("Name:");
 		GridBagConstraints gbc_lblName = new GridBagConstraints();
 		gbc_lblName.anchor = GridBagConstraints.EAST;
 		gbc_lblName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblName.gridx = 0;
 		gbc_lblName.gridy = 1;
 		pnlForm.add(lblName, gbc_lblName);
 		
 		tfName = new JTextField();
 		GridBagConstraints gbc_tfName = new GridBagConstraints();
 		gbc_tfName.anchor = GridBagConstraints.WEST;
 		gbc_tfName.insets = new Insets(0, 0, 5, 0);
 		gbc_tfName.gridx = 1;
 		gbc_tfName.gridy = 1;
 		pnlForm.add(tfName, gbc_tfName);
 		tfName.setColumns(10);
 		
 		JLabel lblDescription = new JLabel("Description:");
 		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
 		gbc_lblDescription.anchor = GridBagConstraints.EAST;
 		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDescription.gridx = 0;
 		gbc_lblDescription.gridy = 2;
 		pnlForm.add(lblDescription, gbc_lblDescription);
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.insets = new Insets(0, 0, 5, 0);
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 2;
 		pnlForm.add(panel, gbc_panel);
 		panel.setLayout(new GridLayout(1, 1));
 		
 		JTextArea textArea = new JTextArea();
 		textArea.setFont(UIManager.getFont("Button.font"));
 		panel.add(textArea);
 		
 		JLabel lblImagePath = new JLabel("Image:");
 		GridBagConstraints gbc_lblImagePath = new GridBagConstraints();
 		gbc_lblImagePath.anchor = GridBagConstraints.EAST;
 		gbc_lblImagePath.insets = new Insets(0, 0, 0, 5);
 		gbc_lblImagePath.gridx = 0;
 		gbc_lblImagePath.gridy = 3;
 		pnlForm.add(lblImagePath, gbc_lblImagePath);
 		
 		JComboBox cbImges = new JComboBox();
 		GridBagConstraints gbc_cbImges = new GridBagConstraints();
 		gbc_cbImges.anchor = GridBagConstraints.WEST;
 		gbc_cbImges.gridx = 1;
 		gbc_cbImges.gridy = 3;
 		pnlForm.add(cbImges, gbc_cbImges);
 		
 		pnlButtons = new JPanel();
 		managerPanel.add(pnlButtons, BorderLayout.SOUTH);
 		pnlButtons.setLayout(new CardLayout(0, 0));
 		
 		pnlView = new JPanel();
 		pnlButtons.add(pnlView, "View Part Type");
 		
 		JButton btnEditPartType = new JButton("Edit Part Type");
 		btnEditPartType.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				editPart((String) cbPart.getSelectedItem());
 			}
 		});
 		pnlView.add(btnEditPartType);
 		
 		JButton btnDeletePartType = new JButton("Delete Part Type");
 		btnDeletePartType.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				deletePart((String) cbPart.getSelectedItem());
 			}
 		});
 		pnlView.add(btnDeletePartType);
 		
 		pnlEdit = new JPanel();
 		pnlButtons.add(pnlEdit, "Edit Part Type");
 		
 		JButton btnSaveChanges = new JButton("Save Changes");
 		btnSaveChanges.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 //				savePartEdit((String) cbPart.getSelectedItem());
 			}
 		});
 		pnlEdit.add(btnSaveChanges);
 		
 		JButton btnCnclChanges = new JButton("Cancel Changes");
 		btnCnclChanges.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancelEdit();
 				viewPart((String) cbPart.getSelectedItem());
 			}
 		});
 		pnlEdit.add(btnCnclChanges);
 		
 		pnlAdd = new JPanel();
 		pnlButtons.add(pnlAdd, "Add Part Type");
 		
 		JButton btnCreatePartType = new JButton("Create Part Type");
 		btnCreatePartType.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				createPart();
 			}
 		});
 		pnlAdd.add(btnCreatePartType);
 		
 		JButton btnClearFields = new JButton("Clear Fields");
 		btnClearFields.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				clearFields();
 			}
 		});
 		pnlAdd.add(btnClearFields);
 	}
 	
 	protected void showAddPanel() {
 		// change combo box to "Select Part..."
 		CardLayout cl = (CardLayout)(pnlButtons.getLayout());
         cl.show(pnlButtons, "Add Part Type");
         clearFields();
         enableFields();
 	}
 	
 	protected void viewPart(String part) {
 		CardLayout cl = (CardLayout)(pnlButtons.getLayout());
         cl.show(pnlButtons, "View Part Type");
         disableFields();
         // Show the part type items in form elements
 	}
 	
 	protected void editPart(String part) {
 		CardLayout cl = (CardLayout)(pnlButtons.getLayout());
         cl.show(pnlButtons, "Edit Part Type");
         // "back-up" the original values in case a user decides to cancel changes
         backupFields[0] = tfName.getText();
         enableFields();        
 	}
 	
 	protected void cancelEdit() {
 		if (backupFields[0] != null) tfName.setText(backupFields[0]);
 	}
 	
 	protected void createPart() {
 		String partName = tfName.getText();
 		// creates the part
 		viewPart(partName);		
 	}
 	
 	protected void deletePart(String part) {
         int choice = JOptionPane.showConfirmDialog(null,
         		"Are you sure you want to delete this part type?\nNote: the action cannot be undone.",
                 "Delete Part",
                 JOptionPane.YES_NO_OPTION);
         if (choice == 0) ; // delete part
         // remove the part option from cbPart
         // view the next item in the list, or no item if there are no items in the list
 	}
 	
 	protected void clearFields() {
 		tfName.setText("");
 	}
 	
 	protected void toggleFields() {
 		if (tfName.isEnabled()) tfName.setEnabled(false);
 		else tfName.setEnabled(true);
 	}
 	protected void enableFields() {
 		if (tfName.isEnabled()) toggleFields();
 	}
 	protected void disableFields() {
 		if (!tfName.isEnabled()) toggleFields();
 	}
 }
