 package admin.contestanttab;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 
 import admin.FileDrop;
 import admin.GameDataDependant;
 import admin.MainFrame;
 import admin.Utils;
 import data.Contestant;
 import data.GameData;
 import data.InvalidFieldException;
 
 public class ContestantPanel extends JPanel implements MouseListener, GameDataDependant {
 
 	private static final long serialVersionUID = 1L;
 	private JButton imgDisplay;
 	private String imgPath;
 
 	private ContestantFieldsPanel paneEditFields;
 	// container for top stuff
 	private JButton btnCastOff;
 	private JButton btnSaveCont;
 	
 	private JLabel labelName;
 	// TODO: Refactor to something more obvious?
 	private JLabel labelCastOff;
 	private JLabel labelCastStatus;
 	private JLabel labelTribe;
 	
 	private JTextField tfFirstName;
 	private JTextField tfLastName;
 	private JComboBox<String> cbTribe;
 	
 	private JTextField tfContID;
 	private JLabel labelID;
 
 	private JTable table;
 	private ContestantTableModel tableModel; 
 	private JTableHeader header;
 	
 	private JButton btnAddCont;
 	private JButton btnDeleteCont;
 	
 	// vars:
 	private boolean isNewContestant;
 	private boolean fieldsChanged = false;
 	
 	private static final String CAST_OFF_TEXT = "Cast Off";
	private static final String UNDO_CAST_TEXT = "Undo Cast Off";
 	
 	private static String DEFAULT_PICTURE = "res/test/defaultpic.png";
 	private static int IMAGE_MAX_DIM = 75;
 	
 	
 	public ContestantPanel(){
 		super();
 		
 		//////////////////////////////
 		// Top Panel:
 		//////////////////////////////
 		// TODO: Better Test picture
 		imgDisplay = new JButton();
 		updateContPicture(DEFAULT_PICTURE); //apparently images have to be .png and alphanumeric
 		
 		
 		// Edit fields:
 		labelName = new JLabel("Name:");
 		tfFirstName = new JTextField(20);
 		tfLastName = new JTextField(20);
 		
 		labelCastOff = new JLabel("Cast off:");
 		// TODO: FIx the init of this.. :>
 		labelCastStatus = new JLabel("-");
 		
 		labelTribe = new JLabel("Tribe:");
 		cbTribe = new JComboBox<String>(GameData.getCurrentGame().getTribeNames());
 		
 		labelID = new JLabel("ID:");
 		tfContID = new JTextField(2);
 		
 		// holds all the fields
 		paneEditFields = new ContestantFieldsPanel(labelName, tfFirstName, 
 					tfLastName, labelID, tfContID, labelCastOff, 
 					labelCastStatus, labelTribe, cbTribe);
 		// add the mouse listener to all components.
 		for (Component c: paneEditFields.getComponents()) {
 			c.addMouseListener(this);
 		}
 		
 		
 		// buttons:
 		btnCastOff = new JButton("Cast Off");
 		/* check to stop casting off before start */
 		if (!GameData.getCurrentGame().isSeasonStarted())
 			btnCastOff.setEnabled(false);
 		
 		btnSaveCont = new JButton("Save");
 		
 		//////////////////////////////
 		// Mid
 		//////////////////////////////
 		List<Contestant> cons = GameData.getCurrentGame().getAllContestants();
 		tableModel = new ContestantTableModel(cons);
 		table = new JTable(tableModel);
 		header = table.getTableHeader();
 		
 		//////////////////////////////
 		// Bottom
 		//////////////////////////////
 		btnAddCont = new JButton("Add");
 		btnDeleteCont = new JButton("Delete");
 		
 		// build the two panes
 		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		setLayout(new BorderLayout(5, 5));
 		buildTopPanel();
 		buildTablePanel();
 		buildBottomPanel();
 		
 		buildActions();
 		
 		refreshGameFields();
 		
 		if (cons.size() > 0) {
 			setPanelContestant(cons.get(0), false);
 			table.setRowSelectionInterval(0, 0);
 		} else {
 			setPanelContestant(null, true);
 		}	
 	}
 	
 	/**
 	 * Builds the top panel including all the editable information
 	 */
 	private void buildTopPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout(10, 10));
 		
 		// this does not need to be referenced else where, only for layout
 		JPanel paneButtons = new JPanel();
 		GridLayout bl = new GridLayout(2, 1);
 		paneButtons.setLayout(bl);
 			
 		paneButtons.add(btnCastOff);
 		
 		paneButtons.add(btnSaveCont);
 		
 		// add all components on top:
 		panel.add(imgDisplay, BorderLayout.LINE_START);
 		panel.add(paneEditFields, BorderLayout.CENTER);
 		panel.add(paneButtons, BorderLayout.LINE_END);
 		
 		add(panel, BorderLayout.PAGE_START);
 		
 		// add the mouse listener to all components.
 		for (Component c: panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 		
 		for (Component c: paneButtons.getComponents())
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
 	            
 				if (table.isRowSelected(row)) {
 					label.setBackground(Utils.getThemeTableHighlight());
 					label.setForeground(Utils.getThemeBG());
 				} else {
 					label.setBackground(UIManager.getColor("Table.background"));
 					label.setForeground(UIManager.getColor("Table.foreground"));
 				}
 				
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
  		for (Component c: scroll.getComponents()) {
  			c.addMouseListener(this);
  		}
 	}
 	
 	private void buildBottomPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		
 		panel.add(btnAddCont);
 		panel.add(btnDeleteCont);
 		
 		add(panel, BorderLayout.PAGE_END);
 		// add the mouse listener to all components.
 		for (Component c: panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 	}
 	
 	/**
 	 * Updates the image displayed to have the path associated, helper method
 	 * <br>
 	 * <b>Note:</b> Pictures must be PNG format.
 	 * @param path Path to new image.
 	 */
 	//apparently images have to be .png and alphanumeric
 	private void updateContPicture(String path) {
 		// don't update if its already correct!
 		if (imgPath == path) {
 			return;
 		}
 		
 		try {
 			Image img = ImageIO.read(new File(path));
 			if(img==null)
 				throw new IOException();
 			
 			// TODO: Make this scale more approriately using Image's resolution/aspect ratio
 			// scale the image!
 			if (img.getWidth(null) > IMAGE_MAX_DIM ||
 					img.getHeight(null) > IMAGE_MAX_DIM) {
 				img = img.getScaledInstance(Math.min(IMAGE_MAX_DIM, img.getWidth(null)),
 						Math.min(IMAGE_MAX_DIM, img.getHeight(null)), 
 						Image.SCALE_SMOOTH);
 			}
 			
 			// NO IO errors occured if getting here:
 			ImageIcon imgD = new ImageIcon(img);
 			imgDisplay.setIcon(imgD);
 			imgPath = path;
 		} catch (IOException e) {
 			System.out.println("Exception loading image for contestant " +
 					"picture [" + path + "]");
 			imgDisplay.setIcon(null);
 			imgDisplay.setText("Could not load: " + path);
 		}
 		
 	}
 	
 	/**
 	 * gets the current information with the current contestant, will update 
 	 * from the fields associated.
 	 * @return Current contestant loaded
 	 * @throws InvalidFieldException Thrown on any bad fields passed
 	 */
 	private Contestant getContestant() throws InvalidFieldException {
 		Contestant c = new Contestant();
 	
 		c.setID(tfContID.getText());
 		c.setFirstName(tfFirstName.getText().trim());
 		c.setLastName(tfLastName.getText().trim());
 		c.setTribe((String)cbTribe.getSelectedItem());
 		c.setPicture(imgPath);
 		
 		return c;
 	}
 	
 	private void setPanelIsActive(boolean active, int week) {
 		if (active) {
 			labelCastOff.setText("Active");
 			btnCastOff.setText(CAST_OFF_TEXT);
 		} else {
 			labelCastOff.setText("Week " + week);
 			btnCastOff.setText(UNDO_CAST_TEXT);
 		}
		btnCastOff.setEnabled(!active);
 	}
 	
 	private void setPanelContestant(Contestant c, boolean newContestant) {
 		isNewContestant = newContestant;
 		
 		if (fieldsChanged) {
 			System.out.println("Player panel changing, fields modified.");
 			saveContestant();
 			fieldsChanged = false;
 		}
 		
 		tfContID.setEnabled(isNewContestant);
 		
 		if (newContestant || c == null) {
 			// set default values
 			tfContID.setText("");
 			tfFirstName.setText("First Name");
 			tfLastName.setText("Last Name");
 			
 			cbTribe.setSelectedIndex(0);
 			
 			setPanelIsActive(true, -1);
 			
 			updateContPicture(DEFAULT_PICTURE);
 			
 			return;
 		}
 		
 		tfFirstName.setText(c.getFirstName());
 		tfLastName.setText(c.getLastName());
 		
 		setPanelIsActive(c.isCastOff(), c.getCastDate());
 		
 		cbTribe.setSelectedItem(c.getTribe());
 		
 		tfContID.setText(c.getID());
 		
 		updateContPicture(c.getPicture());
 	}
 	
 	private void saveContestant() {
 		Contestant con = null;
 		try {
 			con = getContestant();
 		} catch (InvalidFieldException e) {
 			setExceptionError(e);
 			return;
 		} // end catch block
 		
 		tableModel.updateContestant(con);
 		
 		isNewContestant = false;
 		fieldsChanged = false;
 		
 		int row = tableModel.getRowByContestant(con);
 		if (row >= 0 && table.getSelectedRow() != row) // select a row
 			table.setRowSelectionInterval(row, row);
 	}
 	
 	/**
 	 * Sets the error infromation based on an exception!
 	 * @param e Exception with the information necessary
 	 */
 	private void setExceptionError(InvalidFieldException e) {
 		MainFrame mf = MainFrame.getRunningFrame();
 		
 		switch (e.getField()) {
 		case CONT_ID:
 			mf.setStatusErrorMsg("Invalid ID (must be 2 alphanumeric " +
 					"characters long)", 
 					tfContID);
 			break;
 		case CONT_ID_DUP:
 			mf.setStatusErrorMsg("Invalid ID (in use)", tfContID);
 			break;
 		case CONT_FIRST:
 			mf.setStatusErrorMsg("Invalid First Name (must be alphabetic" +
 					", 1-20 characters)",
 					tfFirstName);
 			break;
 		case CONT_LAST:
 			mf.setStatusErrorMsg("Invalid Last Name (must be alphabetic" +
 					", 1-20 characters)",
 					tfLastName);
 			break;
 		case CONT_TRIBE: // how you did this is beyond me..
 			mf.setStatusErrorMsg("Invalid Tribe selection", cbTribe);
 			break;
 		default:
 			mf.setStatusErrorMsg("Unknown problem with fields");	
 		}
 	}
 
 	private void buildActions() {
 		btnAddCont.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (fieldsChanged) {
 					saveContestant();
 				}
 				
 				fieldsChanged = false;
 				setPanelContestant(null, true);
 			}
 		});
 		
 		btnSaveCont.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (isNewContestant) {
 					int response = JOptionPane.showConfirmDialog(null,
 							"Would you like to save a new selected " +
 							"contestant? You can not change ID after saveing.",
 							"Delete Contestant?",
 							JOptionPane.YES_NO_OPTION);
 					if(response == JOptionPane.NO_OPTION){
 						return;
 					}
 				}
 				
 				if (fieldsChanged) {
 					saveContestant();
 				}
 				
 				fieldsChanged = false;
 			}
 			
 		});
 		
 		imgDisplay.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 				int ret = fc.showOpenDialog(null);
 				if(ret==JFileChooser.APPROVE_OPTION){
 					//File f = fc.getSelectedFile();
 					updateContPicture(fc.getSelectedFile().getAbsolutePath());
 				}	
 			}
 			
 		});
 		
 		btnCastOff.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String s = ((JButton) e.getSource()).getText();
 				if(s.equals("Cast Off")){
 					// can't cast off someone already off.
 					/*if (activeCon.isCastOff())
 						return;
 					
 						activeCon.toCastOff();
 						labelCastStatus.setText("Week " + activeCon.getCastDate());
 					}
 					else{
 						activeCon.undoCast();
 						labelCastStatus.setText("Active");
 						bCastOff.setText("Cast Off");
 					}	*/
 				}
 			}
 		});
 		
 		btnDeleteCont.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// ask the admin for input on whether to delete or not
 				int response = JOptionPane.showConfirmDialog(null,
 						"Would you like to delete currently selected " +
 						"Contestant?",
 						"Delete Contestant?",
 						JOptionPane.YES_NO_OPTION);
 				
 				if(response == JOptionPane.YES_OPTION){
 					// user said they want to delete contestant
 					Contestant c = null;
 					try {
 						c = getContestant();
 					} catch (InvalidFieldException ex) {
 						if (ex.getField() == InvalidFieldException.Field.CONT_ID) {
 							MainFrame.getRunningFrame()
 								.setStatusErrorMsg("Can not delete Contestant"+
 										" (invalid ID)", 
 										tfContID);
 							return;
 						}
 					}
 	
 					GameData g = GameData.getCurrentGame();
 					Contestant t = g.getContestant(c.getID());
 					int row = tableModel.getRowByContestant(t);
 					boolean selRow = (table.getRowCount() > 1);
 					tableModel.removeContestant(t);
 					if (selRow) {
 						row %= table.getRowCount();
 						table.setRowSelectionInterval(row, row);
 					} else {
 						btnAddCont.doClick();
 					}
 				}
 			}
 		});
 		
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
 
 			int oldRow = -1; // breaks an infinite loop since setPanelUser fires this event
 			
 			public void valueChanged(ListSelectionEvent le) {
 				 int row = table.getSelectedRow();
 				 if (row < 0 || oldRow == row) return;
 				 oldRow = row;
 				 
 				 Contestant c = tableModel.getByRow(row);
 			     
 				 if (c != null){
 					 if (fieldsChanged) {
 						 saveContestant();
 						 fieldsChanged = false;
 					 }
 					 
 					 setPanelContestant(c, false); 
 				 }
 			}
 		});
 		
 		new FileDrop( this, new FileDrop.Listener(){   
 			public void filesDropped( java.io.File[] files ){   
 				updateContPicture(files[0].getAbsolutePath());
 			}  
 		});
 		
 		FocusAdapter fa = new FocusAdapter() {
 			JTextField src;
 			
 			public void focusGained(FocusEvent evt) {
 				src = (JTextField)evt.getComponent();
 				
 				SwingUtilities.invokeLater( new Runnable() {
     				@Override
     				public void run() {
     					src.selectAll();		
     				}
     			});
     	    }
 		};
 		
 		List<JTextField> tfArr = Arrays.asList(tfContID, tfFirstName, 
 				tfLastName);
 		for (JTextField tf: tfArr) {
 			tf.addFocusListener(fa);
 		}
 	}
 
 	public void seasonStarted(){
 		btnAddCont.setEnabled(false);
 		btnCastOff.setEnabled(true);
 		btnDeleteCont.setEnabled(false);
 		tfLastName.setEnabled(false);
 		tfFirstName.setEnabled(false);
 		tfContID.setEnabled(false);
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		return;
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		Component c = e.getComponent();
 		MainFrame mf = MainFrame.getRunningFrame();
 		
 		if (c == labelName || c == tfFirstName || c == tfLastName) {
 			mf.setStatusMsg("First and Last name must be alphabetic");
 		} else if (c == labelID || c == tfContID) {
 			mf.setStatusMsg("ID must be two characters long and alpha-numeric");
 		} else if (c == labelTribe || c == cbTribe) {
 			mf.setStatusMsg("Select a tribe");
 		} else if (c == imgDisplay) {
 			mf.setStatusMsg("Click to select image");
 		} else if (c == table) {
 			mf.setStatusMsg("Click row to edit contestant");
 		} else if (c == btnAddCont) {
 			mf.setStatusMsg("Click to add new contestant");
 		} else if (c == btnSaveCont) {
 			mf.setStatusMsg("Click to save contestant data");
 		}
 		//System.out.println("MouseEntered: " + c.toString());
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		mouseEntered(e);
 	}
 
 	// unused
 	@Override
 	public void mousePressed(MouseEvent e) {
 		Component c = e.getComponent();
 		if (c == tfContID || c == tfFirstName || c == tfLastName ||
 				c == cbTribe || c == table || c == btnCastOff) {
 			fieldsChanged = true;
 			btnSaveCont.setEnabled(true);
 		}
 	}
 
 	// unused
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		return;
 	}
 
 	/**
 	 * Refreshes all values associated with GameData reference.
 	 * <br> Currently:
 	 * - Tribe combobox
 	 * - Table
 	 * @see GameDataDependant.refreshGameFields
 	 */
 	@Override
 	public void refreshGameFields() {
 		GameData g = GameData.getCurrentGame();
 		
 		// tribe combobox
 		String[] newTribes = g.getTribeNames();
 		cbTribe.removeAllItems();
 		for (String s: newTribes) {
 			cbTribe.addItem(s);
 		}
 		
 		tableModel.fireTableDataChanged();
 	}
 
 }
