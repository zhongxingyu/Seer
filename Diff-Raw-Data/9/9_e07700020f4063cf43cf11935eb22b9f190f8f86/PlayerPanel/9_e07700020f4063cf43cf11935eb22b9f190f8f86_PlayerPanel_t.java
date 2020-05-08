 package admin.playertab;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 
 import sun.awt.VerticalBagLayout;
 
 import admin.AdminUtils;
 import admin.MainFrame;
 import admin.StatusPanel;
 import admin.data.GameData;
 
 import data.Contestant;
 import data.InvalidFieldException;
 import data.User;
 
 
 /**
  * TODO: Doc
  * @author kevin
  *
  */
 public class PlayerPanel extends JPanel implements ChangeListener,
 		MouseListener {
 
 	// input fields:
 	private JLabel labelName;
 	private JTextField tfFirstName;
 	private JTextField tfLastName;
 
 	private JLabel labelID;
 	private JTextField tfID;
 	private JButton btnGenID;
 
 	private JLabel labelWeekly;
 	private JComboBox<Contestant> cbWeeklyPick;
 	private JLabel labelUltimate;
 	private JComboBox<Contestant> cbUltPick;
 
 	// etc
 	private JLabel labelPts;
 	private JButton btnSave;
 
 	// / Table fields
 	private PlayerTableModel tableModel;
 	private JTable table;
 	private JTableHeader header;
 
 	// Bottom buttons
 	private JButton btnAddNew;
 	private JButton btnDelete;
 	private PlayerFieldsPanel playerFields;
 	
 	// Vars
 	private boolean isNewUser;
 	
 	// has the PlayerFields changed?
 	private boolean fieldsChanged;
 
 	public PlayerPanel() {
 		super();
 
 		setLayout(new BorderLayout(5, 5));
 
 		// ////////////////////////////
 		// Top Panel:
 		// ////////////////////////////
 		labelName = new JLabel("Name:");
 		tfFirstName = new JTextField();
 		tfLastName = new JTextField();
 
 		labelID = new JLabel("User ID:");
 		tfID = new JTextField();
 		btnGenID = new JButton("Generate ID");
 
 		labelWeekly = new JLabel("Weekly Pick:");
 		cbWeeklyPick = new JComboBox<Contestant>();
 
 		labelUltimate = new JLabel("Ultimate Pick:");
 		cbUltPick = new JComboBox<Contestant>();
 
 		playerFields = new PlayerFieldsPanel(labelName, tfFirstName,
 				tfLastName, labelID, tfID, btnGenID, labelWeekly, cbWeeklyPick,
 				labelUltimate, cbUltPick);
 		// add the mouse listener to all components.
 		for (Component c : playerFields.getComponents()) {
 			c.addMouseListener(this);
 		}
 
 		// right side!
 		labelPts = new JLabel("Points: 0");
 		btnSave = new JButton("Save");
 
 		// ////////////////////////////
 		// Mid
 		// ////////////////////////////
 		List<User> users = 
 				AdminUtils.uncastListToCast(
 						GameData.getCurrentGame().getAllUsers(), 
 						new User()
 					);
 		tableModel = new PlayerTableModel(users);
 		table = new JTable(tableModel);
 		header = table.getTableHeader();
 
 		// ////////////////////////////
 		// Bottom
 		// ////////////////////////////
 		btnAddNew = new JButton("Add");
 		btnDelete = new JButton("Delete");
 
 		// build the two panes
 		// setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		setLayout(new BorderLayout(5, 5));
 		buildTopPanel();
 		buildTablePanel();
 		buildBottomPanel();
 		
 		buildActions();
 		
 		// init the GUI components
 		
 		refreshContestantCBs();
 		
 		if (users.size() > 0) {
 			setPanelUser(users.get(0), false);
 			table.setRowSelectionInterval(0, 0);
 		} else {
 			setPanelUser(null, true);
 		}
 	}
 
 	/**
 	 * Builds the top panel including all the editable information
 	 */
 	private void buildTopPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout(10, 10));
 
 		// this does not need to be referenced else where, only for layout
 		JPanel rightPane = new JPanel();
 		BoxLayout b = new BoxLayout(rightPane, BoxLayout.Y_AXIS);
 		rightPane.setLayout(b);
 		rightPane.add(Box.createVerticalStrut(32));
 		rightPane.add(labelPts);
 		rightPane.add(Box.createVerticalGlue());
 		rightPane.add(btnSave);
 		rightPane.add(Box.createVerticalStrut(32));
 
 		// add all components on top:
 		panel.add(playerFields, BorderLayout.CENTER);
 		panel.add(rightPane, BorderLayout.LINE_END);
 
 		add(panel, BorderLayout.PAGE_START);
 
 		// add the mouse listener to all components.
 		for (Component c : panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 
 		for (Component c : rightPane.getComponents())
 			c.addMouseListener(this);
 	}
 
 	/**
 	 * Builds the panel containing the JTable
 	 */
 	private void buildTablePanel() {
 		JPanel panel = new JPanel();
 
 		// settings:
 		header.setReorderingAllowed(false); // no moving.
 		table.setColumnSelectionAllowed(true);
 		table.setRowSelectionAllowed(true);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		header.addMouseListener(tableModel.new SortColumnAdapter());
 
 		TableCellRenderer renderer = new TableCellRenderer() {
 
 			JLabel label = new JLabel();
 
 			@Override
 			public JComponent getTableCellRendererComponent(JTable table,
 					Object value, boolean isSelected, boolean hasFocus,
 					int row, int column) {
 
 				Color c = null;
 				if (table.isRowSelected(row)) {
 					c = AdminUtils.getThemeTableHighlight();
 				} else {
 					c = UIManager.getColor("Table.background");
 				}
 				label.setBackground(c);
 
 				label.setOpaque(true);
 				label.setText("" + value);
 
 				return label;
 			}
 
 		};
 		table.setDefaultRenderer(Object.class, renderer);
 
 		JScrollPane scroll = new JScrollPane(table);
 
 		panel.setLayout(new BorderLayout(5, 5));
 		panel.add(scroll, BorderLayout.CENTER);
 
 		add(panel, BorderLayout.CENTER);
 
 		// add the mouse listener to all components.
 		for (Component c : scroll.getComponents()) {
 			c.addMouseListener(this);
 		}
 	}
 
 	private void buildBottomPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 
 		panel.add(btnAddNew);
 		panel.add(btnDelete);
 
 		add(panel, BorderLayout.PAGE_END);
 		// add the mouse listener to all components.
 		for (Component c : panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 	}
 
 	public void seasonStarted(){
 		tfID.setEnabled(false);
 		tfFirstName.setEnabled(false);
 		tfLastName.setEnabled(false);
 		btnGenID.setEnabled(false);
 		btnAddNew.setEnabled(false);
 		btnDelete.setEnabled(false);
 	}
 	
 	/**
 	 * Loads the contestants in the ComboBoxes from the GameData.
 	 */
 	private void refreshContestantCBs() {
 		GameData g = (GameData) GameData.getCurrentGame();
 		
 		if (g == null) {
 			return;
 		}
 		
 		List<Contestant> cons = AdminUtils.vectorToCastList(
 				g.getActiveContestants(), 
 				new Contestant()
 			);
 		//cons = AdminUtils.noNullList(cons);
 
 		cbWeeklyPick.removeAllItems();
 		cbUltPick.removeAllItems();
 
 		for (Contestant c : cons) {
 			cbWeeklyPick.addItem(c);
 			cbUltPick.addItem(c);
 		}
 	}
 	
 	/**
 	 * Currently used to check if a tab is changed, and if its changed to the
 	 * PlayerPanel, it will modify ComboBoxes.
 	 */
 	@Override
 	public void stateChanged(ChangeEvent e) {
 
 		Object obj = e.getSource();
 		if (obj instanceof JTabbedPane) // tab clicked
 		{	
 
 			JTabbedPane tab = (JTabbedPane) obj;
 	
 			if (tab.getSelectedIndex() != 2)
 				return;
 	
 			refreshContestantCBs();
 			return;
 		}
 	}
 	
 	/**
 	 * Returns a new user represented by the data in the User fields.
 	 * @return New User
 	 * @throws InvalidFieldException thrown if any field is invalid.
 	 */
 	private User getUser() throws InvalidFieldException {
 		
 		User u = new User();
 		String uID = tfID.getText();
 		
 		u.setID(uID);
 		
 		u.setFirstName(tfFirstName.getText().trim());
 		u.setLastName(tfLastName.getText().trim());
 		
 		int item = cbUltPick.getSelectedIndex();
 		u.setUltimatePick(cbUltPick.getItemAt(item));
 		
 		item = cbWeeklyPick.getSelectedIndex();
 		u.setWeeklyPick(cbWeeklyPick.getItemAt(item));
 		
 		return u;
 	}
 	
 	private void setPanelUser(User u, boolean newUser) {
 		isNewUser = newUser;
 		
 		tfID.setEnabled(newUser);
 		btnGenID.setEnabled(newUser);
 		
 		if (newUser || u == null) {
 			// set default values
 			tfID.setText("");
 			tfFirstName.setText("First Name");
 			tfLastName.setText("Last Name");
			
			GameData g = GameData.getCurrentGame();
			if (g.getAllContestants().size() > 0) {
				cbUltPick.setSelectedIndex(0);
				cbWeeklyPick.setSelectedIndex(0);
			}
			
 			labelPts.setText(Integer.toString(0));
 			
 			return;
 		}
 		
 		tfID.setText(u.getID());
 		
 		tfFirstName.setText(u.getFirstName());
 		tfLastName.setText(u.getLastName());
 		
 		labelPts.setText(Integer.toString(u.getPoints()));
 		
 		boolean[] bools = new boolean[] {false, false};
 		
 		@SuppressWarnings("unchecked")
 		JComboBox<Contestant>[] cbs = new JComboBox[2];
 		cbs[0] = cbUltPick; cbs[1] = cbWeeklyPick;
 		
 		// iterate through combo boxes setting indexes as necessary
 		for (int i = 0; i < cbUltPick.getItemCount(); i++) {
 			
 			for (int j = 0; j < cbs.length; j++) {
 				if (bools[j]) {
 					Contestant c = cbs[j].getItemAt(i);
 					
 					if (u.getID().equals(c.getID())) { 
 						// contestant is correct
 						cbs[j].setSelectedIndex(i);
 						bools[j] = true;
 					}	
 				}
 			}
 			
 			if (bools[0] && bools[1]) 
 				break; // break if both are set
 		}
 	}
 	
 	private void setExceptionError(InvalidFieldException e) {
 		MainFrame mf = MainFrame.getRunningFrame();
 		
 		switch (e.getField()) {
 		case USER_ID:
 			mf.setStatusErrorMsg("Invalid ID (must be between 2 and 7 chars" +
 					" long, followed by numbers)", tfID);
 			break;
 		case USER_FIRST:
 			mf.setStatusErrorMsg("Invalid First Name (must be alphabetic" +
 					", 1-20 characters)", tfFirstName);
 			break;
 		case USER_LAST:
 			mf.setStatusErrorMsg("Invalid Last Name (must be alphabetic" +
 					", 1-20 characters)", tfLastName);
 			break;
 		case USER_ULT_PICK:
 			mf.setStatusErrorMsg("Invalid Ultimate Pick", cbUltPick);
 			break;
 		case USER_WEEKLY_PICK:
 			mf.setStatusErrorMsg("Invalid Weekly Pick", cbWeeklyPick);
 			break;
 		default:
 			mf.setStatusErrorMsg("Unknown problem with fields");	
 		}
 	}
 	
 	private void saveUser() {
 		
 		User user = null;
 		try {
 			user = getUser();
 		} catch (InvalidFieldException e) {
 			setExceptionError(e);
 			return;
 		} // end catch block
 		
 		tableModel.updateUser(user);
 		
 		int row = tableModel.getRowByUser(user);
 		if (row >= 0) // select a row
 			table.setRowSelectionInterval(row, row);
 
 		isNewUser = false;
 	}
 	
 	private void buildActions() {
 		btnAddNew.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (fieldsChanged) {
 					saveUser();
 				}
 				
 				fieldsChanged = false;
 				setPanelUser(null, true);
 			}
 			
 		});
 		
 		btnSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 				// make sure they want to save initially.
 				if (isNewUser) {
 					int response = JOptionPane.showConfirmDialog(null,
 						"Would you like to save a new selected user? You can " +
 						"not change ID after first save.",
 						"Delete User",
 						JOptionPane.YES_NO_OPTION);
 					if(response == JOptionPane.NO_OPTION){
 						return;
 					}
 				}
 				
 				if (fieldsChanged) {
 					saveUser();
 				}
 				
 				fieldsChanged = false;
 			}
 		});
 		
 		btnDelete.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int response = JOptionPane.showConfirmDialog(null,
 						"Would you like to delete currently selected user?","Delete User",
 								JOptionPane.YES_NO_OPTION);
 				if(response == JOptionPane.YES_OPTION){
 					User u = null;
 					try {
 						u = getUser();
 					} catch (InvalidFieldException ex) {
 						if (ex.getField() == InvalidFieldException.Field.USER_ID) {
 							MainFrame.getRunningFrame().setStatusErrorMsg("Can not delete User (invalid ID)", tfID);
 							return;
 						}
 					}
 
 					GameData g = GameData.getCurrentGame();
 					User t = g.getUser(u.getID());
 					int row = tableModel.getRowByUser(t);
 					boolean selRow = (table.getRowCount() > 1);
 					tableModel.removeUser(t);
 					if (selRow) {
 						row %= table.getRowCount();
 						table.setRowSelectionInterval(row, row);
 					} else {
 						btnAddNew.doClick();
 					}
 				}
 			}
 		});
 		
 		btnGenID.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				User u = new User();
 				try {
 					u.setFirstName(tfFirstName.getText().trim());
 					u.setLastName(tfLastName.getText().trim());
 				} catch (InvalidFieldException ex) {
 					setExceptionError(ex);
 					return;
 				}
 				GameData g = GameData.getCurrentGame();
 				List<data.Person> userList = 
 						AdminUtils.vectorToCastList(g.getAllUsers(), 
 								(data.Person)(new User())); // cast the user to a person.. lol
 				
 				String id = AdminUtils.generateID(u, userList);
 				
 				tfID.setText(id);
 			}
 		});
 		
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
 
 			public void valueChanged(ListSelectionEvent le) {
 				 int row = table.getSelectedRow();
 				 if (row < 0) return;
 				 
 				 User u = tableModel.getByRow(row);
 			     
 				 if (u != null){
 					 setPanelUser(u, false); 
 				 }
 			}
 		});
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		Component c = e.getComponent();
 		MainFrame mf = MainFrame.getRunningFrame();
 		if (c == labelName || c == tfFirstName || c == tfLastName) {
 			mf.setStatusMsg("First and Last name must be alphabetic");
 		} else if (c == labelID || c == tfID) {
 			mf.setStatusMsg("ID must be 2-7 chars long and may end with numbers");
 		} else if (c == btnGenID) {
 			mf.setStatusMsg("Click to auto-generate ID from first and last name");
 		} else if (c == labelWeekly || c == cbWeeklyPick) {
 			mf.setStatusMsg("Select Weekly pick");
 		} else if (c == labelUltimate || c == cbUltPick) {
 			mf.setStatusMsg("Select Ultimate Winner");
 		} else if (c == btnAddNew) {
 			mf.setStatusMsg("Add a new User to system");
 		} else if (c == btnDelete) {
 			mf.setStatusMsg("Remove selected User from system");
 		} else if (c == table || c == header) {
 			mf.setStatusMsg("Click Heading to sort by column");
 		}
 		// System.out.println("MouseEntered: " + c.toString());
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// Currently unused stubs.
 		return;
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 		Component c = me.getComponent();
 		if (c == tfFirstName || c == tfLastName || c == tfID || 
 				c == cbUltPick || c == cbWeeklyPick) {
 			fieldsChanged = true;
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// Currently unused stubs.
 		return;
 	}
 }
