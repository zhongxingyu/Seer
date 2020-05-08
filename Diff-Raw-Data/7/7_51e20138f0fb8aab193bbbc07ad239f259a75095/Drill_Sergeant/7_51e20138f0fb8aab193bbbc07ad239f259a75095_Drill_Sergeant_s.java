 package ui;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import java.awt.Font;
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 import javax.swing.AbstractListModel;
 import javax.swing.border.BevelBorder;
 import javax.swing.UIManager;
 import javax.swing.JScrollPane;
 import javax.swing.border.TitledBorder;
 import javax.swing.JSeparator;
 import javax.swing.JComboBox;
 import javax.swing.JSpinner;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JProgressBar;
 import javax.swing.JTextField;
 import javax.swing.JOptionPane; //Added manually.
 import javax.swing.JDialog;		//Added manually.
 import javax.swing.SwingConstants;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 import javax.swing.border.EtchedBorder;
 import javax.swing.JRadioButton;
 import java.awt.Component;
 import javax.swing.JToggleButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Toolkit;
 import javax.swing.JTextPane;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.DropMode;
 import javax.swing.DefaultComboBoxModel;
 
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 
 import javax.swing.JInternalFrame;
 import javax.swing.JDesktopPane;
 import javax.swing.JLayeredPane;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import data.Exercise;
 import data.Workout;
 import data.XMLSaxParser;
 
 public class Drill_Sergeant {
 
 	private JFrame 		frmDrillSergeant;
 	private Preview 	frmPreview = new Preview();
 	private JList 		listWorkout;
 	private JTextField 	txtCurrent;
 	private JTextField 	txtCurrentSet;
 	private JTextField 	txtTotalSets;
 	private JTextField 	txtRepCount;
 	private JTextField 	txtSetTimeLeft;
 	private JTextField 	txtTotalTimeLeft;
 	private JTextField 	txtNext;
 	private JTextField 	txtWorkout;
 	private JButton		btnPreview;
 	private JComboBox 	cbName;
 	private JComboBox 	cbSets;
 	private JComboBox 	cbReps;
 	private JComboBox 	cbBetweenMin;
 	private JComboBox 	cbBetweenSec;
 	private JComboBox 	cbAfterMin;
 	private JComboBox 	cbAfterSec;
 	private CardLayout 	cardlayout = new CardLayout();
 	private JPanel 		cards = new JPanel(cardlayout);
 	private ImageIcon 	dialogIcon;
 	private Workout 	newWorkout;
 	private Workout[] 	workouts = new Workout[50];		//Stores the array of Workout objects, creating a "workout list".
 	private String 		workoutName = new String();
 	private XMLSaxParser handler = new XMLSaxParser();
 	
 	//************************************************************
 	// main
 	//		Launch the application.
 	//************************************************************
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Drill_Sergeant window = new Drill_Sergeant();
 					window.frmDrillSergeant.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 
 	//************************************************************
 	// Drill_Sergeant
 	//		Initialize the contents of the frame.
 	//************************************************************
 	public Drill_Sergeant() {
 		//Main Application Frame
 		frmDrillSergeant = new JFrame();
 		frmDrillSergeant.setName("DrillSergeant");
 		frmDrillSergeant.setIconImage(Toolkit.getDefaultToolkit().getImage(Drill_Sergeant.class.getResource("/ui/resources/stopwatch.png")));
 		frmDrillSergeant.setResizable(false);
 		frmDrillSergeant.setTitle("Drill Sergeant");
 		frmDrillSergeant.setBounds(100, 100, 520, 600);
 		frmDrillSergeant.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmDrillSergeant.getContentPane().setLayout(null);
 		
 		//The main card panel containing all of the "cards" (screens) in the main application frame.
 		cards.setBounds(0, 0, 514, 551);
 		frmDrillSergeant.getContentPane().add(cards);
 		
 		//=============================================================================================================
 		// Card 1 - Title Screen
 		//=============================================================================================================
 		JPanel card1 = new JPanel();
 		card1.setBackground(UIManager.getColor("Tree.dropLineColor"));
 		cards.add(card1, "card1");
 		card1.setLayout(null);
 
 		//-------------
 		// Images
 		//-------------
 		JLabel lblTitleImage = new JLabel("");
 		lblTitleImage.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/dstitle2.png")));
 		lblTitleImage.setBounds(109, 35, 300, 301);
 		card1.add(lblTitleImage);
 		
 		//-------------
 		// Buttons
 		//-------------
 		//Open
 		JButton btnOpen = new JButton("Open Workout");
 		btnOpen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
					parseXML("config/user_config.xml");
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				swapView("card2");
 			}
 		});
 		btnOpen.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/document-open-folder_24x24.png")));
 		btnOpen.setBackground(UIManager.getColor("Tree.dropLineColor"));
 		btnOpen.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnOpen.setBounds(109, 360, 132, 74);
 		card1.add(btnOpen);
 		
 		//New
 		JButton btnNew = new JButton("New Workout");
 		btnNew.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//swapView("card3");
 				dialogIcon = new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/document-new-8_24x24.png"));
 				String temp = (String) JOptionPane.showInputDialog(frmDrillSergeant, "Please enter a name for the workout:", "Name", JOptionPane.INFORMATION_MESSAGE, dialogIcon, null, workoutName);
 				newWorkout = new Workout(temp);
 				String workoutName = newWorkout.getName();
 				if (workoutName != null) {
 					txtWorkout.setText(workoutName);
 					swapView("card3");
 				}
 			}
 		});
 		btnNew.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/document-new-8_24x24.png")));
 		btnNew.setBackground(UIManager.getColor("Tree.dropLineColor"));
 		btnNew.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnNew.setBounds(277, 360, 132, 74);
 		card1.add(btnNew);
 		
 		//View Journal
 		JButton btnJournal = new JButton("View Journal");
 		btnJournal.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 			}
 		});
 		btnJournal.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/accessories-dictionary_24x24.png")));
 		btnJournal.setBackground(UIManager.getColor("Tree.dropLineColor"));
 		btnJournal.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnJournal.setBounds(109, 458, 132, 74);
 		card1.add(btnJournal);
 		
 		//Settings
 		JButton btnSettings = new JButton("Settings");
 		btnSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				swapView("card5");
 			}
 		});
 		btnSettings.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/configure-3_24x24.png")));
 		btnSettings.setBackground(UIManager.getColor("Tree.dropLineColor"));
 		btnSettings.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnSettings.setBounds(277, 458, 132, 74);
 		card1.add(btnSettings);
 		
 		JLabel label = new JLabel("");
 		label.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/textureAlum.jpg")));
 		label.setBounds(0, 0, 514, 551);
 		card1.add(label);
 		
 		//=============================================================================================================
 		// Card 2 - Select Workout
 		//=============================================================================================================
 		JPanel card2 = new JPanel();
 		card2.setLayout(null);
 		cards.add(card2, "card2");
 		
 		//-------------
 		// Borders
 		//-------------
 		JPanel panelTitleBorderCard2 = new JPanel();
 		panelTitleBorderCard2.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		panelTitleBorderCard2.setToolTipText("");
 		panelTitleBorderCard2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select a Workout", TitledBorder.CENTER, TitledBorder.TOP, null, UIManager.getColor("windowBorder")));
 		panelTitleBorderCard2.setBounds(10, 27, 496, 355);
 		card2.add(panelTitleBorderCard2);
 		panelTitleBorderCard2.setLayout(null);
 		
 		JScrollPane scrollWorkout = new JScrollPane();
 		scrollWorkout.setBounds(6, 16, 484, 332);
 		panelTitleBorderCard2.add(scrollWorkout);
 		
 		//-------------
 		// List
 		//-------------
 		listWorkout = new JList();
 		listWorkout.setDragEnabled(true);
 		scrollWorkout.setViewportView(listWorkout);
 		listWorkout.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		listWorkout.setFont(new Font("Cordia New", Font.PLAIN, 16));
 		listWorkout.setBorder(new BevelBorder(BevelBorder.LOWERED, UIManager.getColor("InternalFrame.resizeIconShadow"), UIManager.getColor("InternalFrame.borderLight"), null, null));
 		listWorkout.setModel(new AbstractListModel() {
 			String[] values = new String[] {"LIST ITEM 1", "LIST ITEM 2", "LIST ITEM 3", "LIST ITEM 4", "LIST ITEM 5", "LIST ITEM 6", "LIST ITEM 7", "LIST ITEM 8", "LIST ITEM 9", "LIST ITEM 10", "LIST ITEM 11", "LIST ITEM 12", "LIST ITEM 13", "LIST ITEM 14", "LIST ITEM 15"};
 			public int getSize() {
 				return values.length;
 			}
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		
 		//-------------
 		// Buttons
 		//-------------
 		//Open
 		JButton btnOpen2 = new JButton("   Open");
 		btnOpen2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int selectedIndex = listWorkout.getSelectedIndex();
 				selectWorkout(handler, selectedIndex, "btnOpen2");
 			}
 		});
 		btnOpen2.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/document-open-folder_32x32.png")));
 		btnOpen2.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnOpen2.setBounds(10, 408, 132, 74);
 		card2.add(btnOpen2);
 		
 		//Delete
 		JButton btnDelete = new JButton("   Delete");
 		btnDelete.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		btnDelete.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/edit-delete-2.png")));
 		btnDelete.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnDelete.setBounds(372, 408, 132, 74);
 		card2.add(btnDelete);
 		
 		//Edit
 		JButton btnEdit = new JButton("   Edit");
 		btnEdit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int selectedIndex = listWorkout.getSelectedIndex();
 				selectWorkout(handler, selectedIndex, "btnEdit");
 			}
 		});
 		btnEdit.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/edit-4.png")));
 		btnEdit.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnEdit.setBounds(192, 408, 132, 74);
 		card2.add(btnEdit);
 		
 		//Back
 		JButton btnBack = new JButton("  Back");
 		btnBack.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				swapView("card1");
 			}
 		});
 		btnBack.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/go-previous-7_32x32.png")));
 		btnBack.setBounds(10, 505, 100, 35);
 		card2.add(btnBack);
 		
 		//=============================================================================================================
 		// Card 3 - Edit Workout
 		//=============================================================================================================
 		JPanel card3 = new JPanel();
 		cards.add(card3, "card3");
 		card3.setLayout(null);
 		
 		//-------------
 		// Borders
 		//-------------
 		JPanel panelTitleBorderCard3 = new JPanel();
 		panelTitleBorderCard3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Edit Workout", TitledBorder.CENTER, TitledBorder.TOP, null, null));
 		panelTitleBorderCard3.setBounds(10, 11, 494, 392);
 		card3.add(panelTitleBorderCard3);
 		panelTitleBorderCard3.setLayout(null);
 		
 		//-------------
 		// Combo Boxes
 		//-------------
 		//Name
 		cbName = new JComboBox();
 		cbName.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JComboBox cbExercises = (JComboBox)arg0.getSource();
 				String selected = (String)cbExercises.getSelectedItem();
 				System.out.print(selected);
 				if (selected == "**Custom**") {
 					selected = (String) JOptionPane.showInputDialog(cards, "Please enter a name for the custom exercise:", "Name", JOptionPane.INFORMATION_MESSAGE);
 					System.out.print(selected);
 					cbExercises.setEditable(true);
 					cbExercises.setSelectedItem(selected);
 					cbExercises.addItem(selected);
 					cbExercises.setEditable(false);
 				}
 			}
 		});
 		cbName.setModel(new DefaultComboBoxModel(new String[] {"--------------------------Select an exercise to add-------------------------", "**Custom**", "Crunches", "Lunges", "Pushups", "Pullups", "Situps"}));
 		cbName.setName("");
 		cbName.setBounds(78, 96, 352, 34);
 		panelTitleBorderCard3.add(cbName);
 		
 		//Sets
 		cbSets = new JComboBox();
 		cbSets.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if ((String)cbSets.getSelectedItem() == "1") {
 					cbBetweenMin.setEnabled(false);
 					cbBetweenSec.setEnabled(false);
 					cbBetweenMin.setSelectedItem("0");
 					cbBetweenSec.setSelectedItem("0");
 				} else {
 					cbBetweenMin.setEnabled(true);
 					cbBetweenSec.setEnabled(true);
 				}
 			}
 		});
 		cbSets.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"}));
 		cbSets.setEditable(true);
 		cbSets.setBounds(212, 160, 50, 20);
 		panelTitleBorderCard3.add(cbSets);
 		
 		//Reps
 		cbReps = new JComboBox();
 		cbReps.setModel(new DefaultComboBoxModel(new String[] {"5", "10", "15", "20", "25", "30", "35", "40", "45", "50"}));
 		cbReps.setEditable(true);
 		cbReps.setBounds(212, 219, 50, 20);
 		panelTitleBorderCard3.add(cbReps);
 		
 		//Rest time between sets
 		cbBetweenMin = new JComboBox();
 		cbBetweenMin.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "2", "3", "4", "5"}));
 		cbBetweenMin.setEditable(true);
 		cbBetweenMin.setEnabled(false); //Initialize to false since "Sets" is initialized to 1.
 		cbBetweenMin.setBounds(212, 281, 40, 20);
 		panelTitleBorderCard3.add(cbBetweenMin);
 		
 		cbBetweenSec = new JComboBox();
 		cbBetweenSec.setModel(new DefaultComboBoxModel(new String[] {"0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"}));
 		cbBetweenSec.setEditable(true);
 		cbBetweenSec.setEnabled(false); //Initialize to false since "Sets" is initialized to 1.
 		cbBetweenSec.setBounds(282, 281, 40, 20);
 		panelTitleBorderCard3.add(cbBetweenSec);
 		
 		//Rest time after exercise
 		cbAfterMin = new JComboBox();
 		cbAfterMin.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "2", "3", "4", "5"}));
 		cbAfterMin.setEditable(true);
 		cbAfterMin.setBounds(212, 347, 40, 20);
 		panelTitleBorderCard3.add(cbAfterMin);
 		
 		cbAfterSec = new JComboBox();
 		cbAfterSec.setModel(new DefaultComboBoxModel(new String[] {"0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"}));
 		cbAfterSec.setEditable(true);
 		cbAfterSec.setBounds(282, 347, 40, 20);
 		panelTitleBorderCard3.add(cbAfterSec);
 		
 		//-------------
 		// Text
 		//-------------
 		JLabel lblNameOfWorkout = new JLabel("Name:");
 		lblNameOfWorkout.setFont(new Font("Tahoma", Font.BOLD, 12));
 		lblNameOfWorkout.setBounds(10, 44, 59, 14);
 		panelTitleBorderCard3.add(lblNameOfWorkout);
 		
 		//Workout Name
 		txtWorkout = new JTextField();
 		txtWorkout.setText("My_Workout_A");
 		txtWorkout.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		txtWorkout.setBounds(78, 34, 352, 34);
 		panelTitleBorderCard3.add(txtWorkout);
 		txtWorkout.setColumns(10);
 		
 		//Exercises
 		JLabel lblExercise = new JLabel("Exercises:");
 		lblExercise.setBounds(10, 105, 58, 14);
 		panelTitleBorderCard3.add(lblExercise);
 		lblExercise.setFont(new Font("Tahoma", Font.BOLD, 12));
 		
 		//Sets
 		JLabel lblSets = new JLabel("Sets:");
 		lblSets.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblSets.setBounds(133, 162, 69, 14);
 		panelTitleBorderCard3.add(lblSets);
 		lblSets.setFont(new Font("Tahoma", Font.BOLD, 12));
 		
 		//Reps/Set
 		JLabel lblNewLabel = new JLabel("Reps/Set:");
 		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNewLabel.setBounds(133, 221, 69, 14);
 		panelTitleBorderCard3.add(lblNewLabel);
 		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
 		
 		//Rest Time Between Sets
 		JLabel lblTimeBetweenSets = new JLabel("Rest Time Between Sets:");
 		lblTimeBetweenSets.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblTimeBetweenSets.setBounds(43, 281, 159, 14);
 		panelTitleBorderCard3.add(lblTimeBetweenSets);
 		lblTimeBetweenSets.setFont(new Font("Tahoma", Font.BOLD, 12));
 		
 		JLabel lblBetweenMin = new JLabel("min");
 		lblBetweenMin.setBounds(256, 284, 16, 14);
 		panelTitleBorderCard3.add(lblBetweenMin);
 		
 		JLabel lblBetweenSec = new JLabel("sec");
 		lblBetweenSec.setBounds(326, 284, 16, 14);
 		panelTitleBorderCard3.add(lblBetweenSec);
 		
 		//Rest Time After Exercise
 		JLabel lblRestTime = new JLabel("Rest Time After This Exercise:");
 		lblRestTime.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblRestTime.setBounds(22, 347, 180, 14);
 		panelTitleBorderCard3.add(lblRestTime);
 		lblRestTime.setFont(new Font("Tahoma", Font.BOLD, 12));
 		
 		JLabel lblAfterMin = new JLabel("min");
 		lblAfterMin.setBounds(256, 354, 16, 14);
 		panelTitleBorderCard3.add(lblAfterMin);
 		
 		JLabel lblAfterSec = new JLabel("sec");
 		lblAfterSec.setBounds(326, 354, 16, 14);
 		panelTitleBorderCard3.add(lblAfterSec);
 		
 		//-------------
 		// Buttons
 		//-------------
 		//Add
 		JButton btnAdd = new JButton(" Add");
 		btnAdd.setBounds(386, 336, 98, 39);
 		panelTitleBorderCard3.add(btnAdd);
 		btnAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Exercise newExercise = new Exercise();
 				if (cbName.getSelectedIndex() == 0) {
 					JOptionPane.showMessageDialog(cards, "Please select an exercise.", "Notification", JOptionPane.ERROR_MESSAGE);
 					cbName.requestFocus();
 					return;
 				}
 				newExercise.setName((String)cbName.getSelectedItem());
 				newExercise.setGraphicURL("URL GOES HERE");
 				newExercise.setSets((String)cbSets.getSelectedItem());
 				newExercise.setReps((String)cbReps.getSelectedItem());
 				newExercise.setRestBetween((String)cbBetweenMin.getSelectedItem(), (String)cbBetweenSec.getSelectedItem());
 				newExercise.setRestAfter((String)cbAfterMin.getSelectedItem(), (String)cbAfterSec.getSelectedItem());
 				newWorkout.addExercise(newExercise);
 				int previewIndex = frmPreview.add(newExercise);
 				newExercise.setPosition(previewIndex);
 				ImageIcon okIcon = new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/dialog-ok-apply-5_32x32.png"));
 				JOptionPane.showMessageDialog(cards, "Exercise added!", "Notification", JOptionPane.INFORMATION_MESSAGE, okIcon);
 			}
 		});
 		btnAdd.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/edit-add-2_16x16.png")));
 		btnAdd.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 		//Cancel
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int choice = JOptionPane.showConfirmDialog(cards, "Are you sure you want to cancel? All work will be lost.", "Cancel", JOptionPane.YES_NO_OPTION);
 				if (choice == JOptionPane.YES_OPTION) {
 					resetEditScreen();
 					swapView("card1");
 				} else {
 					//Do nothing except close dialog box.
 				}
 			}
 		});
 		btnCancel.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/dialog-cancel-3.png")));
 		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnCancel.setBounds(10, 439, 132, 74);
 		card3.add(btnCancel);
 		
 		//Preview
 		btnPreview = new JButton("Preview");
 		btnPreview.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				//swapView("card6");
 				togglePreviewWindow();
 			}
 		});
 		btnPreview.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/format-list-ordered_24x24.png")));
 		btnPreview.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnPreview.setBounds(193, 439, 132, 74);
 		card3.add(btnPreview);
 		
 		//Finished
 		JButton btnSave = new JButton("Finished");
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Object[] options = { "Save and Exit", "Save and Begin Workout", "Cancel" };
 				int choice = JOptionPane.showOptionDialog(cards, 
 						"Do you want to exit to the Main screen or begin this workout?\n(Press Cancel to stay on this screen)", 
 						null, 
 						JOptionPane.YES_NO_CANCEL_OPTION, 
 						JOptionPane.QUESTION_MESSAGE, 
 						null, options, options[0]);
 				
 				if (choice == JOptionPane.NO_OPTION) {
 					newWorkout.save(handler);
 					newWorkout.load();
 					swapView("card4");
 				} else if (choice == JOptionPane.YES_OPTION) {
 					newWorkout.save(handler);
 					swapView("card1");
 				} else if (choice == JOptionPane.CANCEL_OPTION) {
 					//do nothing except close dialog box
 				}
 			}
 		});
 		btnSave.setIcon(null);
 		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnSave.setBounds(372, 439, 132, 74);
 		card3.add(btnSave);
 		
 		//=============================================================================================================
 		// Card 4 - Active Workout
 		//=============================================================================================================
 		JPanel card4 = new JPanel();
 		cards.add(card4, "card4");
 		card4.setLayout(null);
 		
 		//-------------
 		// Text
 		//-------------
 		//Workout Name
 		JLabel lblWorkoutTitle = new JLabel("Name Of Workout");
 		lblWorkoutTitle.setForeground(new Color(102, 102, 102));
 		lblWorkoutTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
 		lblWorkoutTitle.setHorizontalAlignment(SwingConstants.CENTER);
 		lblWorkoutTitle.setBounds(10, 11, 487, 26);
 		card4.add(lblWorkoutTitle);
 		
 		//Current Exercise
 		JLabel lblCurrentExercise = new JLabel("Current:");
 		lblCurrentExercise.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblCurrentExercise.setHorizontalAlignment(SwingConstants.LEFT);
 		lblCurrentExercise.setBounds(10, 72, 52, 17);
 		card4.add(lblCurrentExercise);
 		
 		txtCurrent = new JTextField();
 		txtCurrent.setHorizontalAlignment(SwingConstants.LEFT);
 		txtCurrent.setForeground(new Color(0, 153, 0));
 		txtCurrent.setFont(new Font("Tahoma", Font.BOLD, 24));
 		txtCurrent.setText("PULL-UPS");
 		txtCurrent.setEditable(false);
 		txtCurrent.setBounds(75, 48, 429, 57);
 		card4.add(txtCurrent);
 		txtCurrent.setColumns(10);
 		
 		//Next Exercise
 		JLabel lblUpNext = new JLabel("Up Next:");
 		lblUpNext.setHorizontalAlignment(SwingConstants.LEFT);
 		lblUpNext.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblUpNext.setBounds(12, 123, 60, 17);
 		card4.add(lblUpNext);
 		
 		txtNext = new JTextField();
 		txtNext.setHorizontalAlignment(SwingConstants.LEFT);
 		txtNext.setEnabled(false);
 		txtNext.setText("CRUNCHES");
 		txtNext.setForeground(new Color(102, 102, 102));
 		txtNext.setFont(new Font("Tahoma", Font.BOLD, 14));
 		txtNext.setEditable(false);
 		txtNext.setColumns(10);
 		txtNext.setBounds(75, 116, 253, 32);
 		card4.add(txtNext);
 		
 		//Current Set
 		JLabel lblSet = new JLabel("Set:");
 		lblSet.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblSet.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblSet.setBounds(117, 192, 46, 14);
 		card4.add(lblSet);
 		
 		txtCurrentSet = new JTextField();
 		txtCurrentSet.setEditable(false);
 		txtCurrentSet.setForeground(new Color(0, 153, 0));
 		txtCurrentSet.setFont(new Font("Tahoma", Font.BOLD, 24));
 		txtCurrentSet.setHorizontalAlignment(SwingConstants.CENTER);
 		txtCurrentSet.setText("1");
 		txtCurrentSet.setBounds(173, 167, 60, 57);
 		card4.add(txtCurrentSet);
 		txtCurrentSet.setColumns(10);
 		
 		JLabel lblOf = new JLabel("of");
 		lblOf.setHorizontalAlignment(SwingConstants.CENTER);
 		lblOf.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblOf.setBounds(248, 182, 20, 27);
 		card4.add(lblOf);
 		
 		//Total Sets
 		txtTotalSets = new JTextField();
 		txtTotalSets.setForeground(new Color(0, 153, 0));
 		txtTotalSets.setFont(new Font("Tahoma", Font.BOLD, 24));
 		txtTotalSets.setHorizontalAlignment(SwingConstants.CENTER);
 		txtTotalSets.setText("5");
 		txtTotalSets.setEditable(false);
 		txtTotalSets.setColumns(10);
 		txtTotalSets.setBounds(279, 167, 60, 57);
 		card4.add(txtTotalSets);
 		
 		//Rep Count
 		JLabel lblRepCount = new JLabel("Rep Count:");
 		lblRepCount.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblRepCount.setBounds(89, 233, 80, 20);
 		card4.add(lblRepCount);
 		
 		txtRepCount = new JTextField();
 		txtRepCount.setForeground(new Color(0, 153, 0));
 		txtRepCount.setHorizontalAlignment(SwingConstants.CENTER);
 		txtRepCount.setText("10");
 		txtRepCount.setFont(new Font("Tahoma", Font.BOLD, 48));
 		txtRepCount.setEditable(false);
 		txtRepCount.setBounds(44, 253, 155, 52);
 		card4.add(txtRepCount);
 		txtRepCount.setColumns(10);
 		
 		//Set Time Left
 		JLabel lblNextSetStarts = new JLabel("Next Set Starts In:");
 		lblNextSetStarts.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblNextSetStarts.setBounds(337, 233, 120, 20);
 		card4.add(lblNextSetStarts);
 		
 		txtSetTimeLeft = new JTextField();
 		txtSetTimeLeft.setForeground(new Color(0, 153, 0));
 		txtSetTimeLeft.setHorizontalAlignment(SwingConstants.CENTER);
 		txtSetTimeLeft.setText("1:30");
 		txtSetTimeLeft.setFont(new Font("Tahoma", Font.BOLD, 48));
 		txtSetTimeLeft.setEditable(false);
 		txtSetTimeLeft.setColumns(10);
 		txtSetTimeLeft.setBounds(317, 253, 155, 52);
 		card4.add(txtSetTimeLeft);
 		
 		//Full Workout Progress
 		JLabel lblProgress = new JLabel("Full Workout Progress");
 		lblProgress.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblProgress.setHorizontalAlignment(SwingConstants.CENTER);
 		lblProgress.setHorizontalTextPosition(SwingConstants.CENTER);
 		lblProgress.setBounds(175, 316, 164, 20);
 		card4.add(lblProgress);
 		
 		//Total Time Remaining
 		JLabel lblTimeRemaining = new JLabel("Total Time Remaining:");
 		lblTimeRemaining.setHorizontalTextPosition(SwingConstants.CENTER);
 		lblTimeRemaining.setHorizontalAlignment(SwingConstants.LEFT);
 		lblTimeRemaining.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		lblTimeRemaining.setBounds(270, 380, 105, 20);
 		card4.add(lblTimeRemaining);
 		
 		txtTotalTimeLeft = new JTextField();
 		txtTotalTimeLeft.setForeground(new Color(102, 102, 102));
 		txtTotalTimeLeft.setHorizontalAlignment(SwingConstants.CENTER);
 		txtTotalTimeLeft.setFont(new Font("Tahoma", Font.BOLD, 16));
 		txtTotalTimeLeft.setText("hh:mm:ss");
 		txtTotalTimeLeft.setEditable(false);
 		txtTotalTimeLeft.setBounds(376, 380, 96, 26);
 		card4.add(txtTotalTimeLeft);
 		txtTotalTimeLeft.setColumns(10);
 		
 		//-------------
 		// Progress Bars
 		//-------------
 		JProgressBar progressBar = new JProgressBar();
 		progressBar.setBounds(44, 341, 428, 37);
 		card4.add(progressBar);
 		
 		//-------------
 		// Buttons
 		//-------------
 		//Start
 		JButton btnStart = new JButton("START");
 		btnStart.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/stopwatch_start.png")));
 		btnStart.setForeground(new Color(102, 102, 102));
 		btnStart.setFont(new Font("Tahoma", Font.BOLD, 18));
 		btnStart.setBounds(175, 417, 164, 123);
 		card4.add(btnStart);
 		
 		//End Workout
 		JButton btnEnd = new JButton("End Workout");
 		btnEnd.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/dialog-disable_16x16.png")));
 		btnEnd.setBounds(384, 503, 120, 37);
 		card4.add(btnEnd);
 		
 		//Restart Workout
 		JButton btnRestart = new JButton("Restart Workout");
 		btnRestart.setBounds(10, 503, 120, 37);
 		card4.add(btnRestart);
 		
 		//=============================================================================================================
 		// Card 5 - Settings
 		//=============================================================================================================
 		JPanel card5 = new JPanel();
 		card5.setName("card5");
 		cards.add(card5, "card5");
 		card5.setLayout(null);
 		
 		JPanel panelTitleBorderCard5 = new JPanel();
 		panelTitleBorderCard5.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Settings", TitledBorder.CENTER, TitledBorder.TOP, null, null));
 		panelTitleBorderCard5.setBounds(41, 33, 434, 439);
 		card5.add(panelTitleBorderCard5);
 		panelTitleBorderCard5.setLayout(null);
 		
 		//-------------
 		// Text
 		//-------------
 		JLabel lblSound = new JLabel("Sound:");
 		lblSound.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/player-volume_32x32.png")));
 		lblSound.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblSound.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblSound.setBounds(113, 29, 88, 44);
 		panelTitleBorderCard5.add(lblSound);
 		
 		//-------------
 		// Buttons
 		//-------------
 		//Sound On/Off radio toggle
 		JRadioButton rdbtnOn = new JRadioButton("On");
 		rdbtnOn.setSelected(true);
 		rdbtnOn.setBounds(216, 40, 39, 23);
 		panelTitleBorderCard5.add(rdbtnOn);
 		
 		JRadioButton rdbtnOff = new JRadioButton("Off");
 		rdbtnOff.setBounds(257, 40, 46, 23);
 		panelTitleBorderCard5.add(rdbtnOff);
 		
 		//Back
 		JButton btnBack_card5 = new JButton("  Back");
 		btnBack_card5.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				swapView("card1");
 			}
 		});
 		btnBack_card5.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/go-previous-7_32x32.png")));
 		btnBack_card5.setBounds(41, 494, 100, 35);
 		card5.add(btnBack_card5);
 		
 		//=============================================================================================================
 		// Card 6 - Preview
 		//=============================================================================================================
 		JPanel card6 = new JPanel();
 		card6.setName("");
 		cards.add(card6, "card6");
 		card6.setLayout(null);
 		
 		//-------------
 		// Borders
 		//-------------
 		JScrollPane scrollExercises = new JScrollPane();
 		scrollExercises.setBorder(new TitledBorder(null, "Preview", TitledBorder.CENTER, TitledBorder.TOP, null, null));
 		scrollExercises.setBounds(10, 30, 494, 380);
 		card6.add(scrollExercises);
 
 		//-------------
 		// Lists
 		//-------------
 		JList listExercises = new JList();
 		scrollExercises.setViewportView(listExercises);
 		
 		//-------------
 		// Buttons
 		//-------------
 		//Back
 		JButton button = new JButton("  Back");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				swapView("card3");
 			}
 		});
 		button.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/go-previous-7_32x32.png")));
 		button.setBounds(10, 444, 132, 74);
 		card6.add(button);
 		
 		//Delete
 		JButton button_1 = new JButton("   Delete");
 		button_1.setIcon(new ImageIcon(Drill_Sergeant.class.getResource("/ui/resources/edit-delete-2.png")));
 		button_1.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		button_1.setBounds(372, 444, 132, 74);
 		card6.add(button_1);
 		
 		//=============================================================================================================
 		// Menu Bar
 		//=============================================================================================================
 		JMenuBar menuBar = new JMenuBar();
 		frmDrillSergeant.setJMenuBar(menuBar);
 		
 		JMenu mFile = new JMenu("File");
 		menuBar.add(mFile);
 		
 		JMenu mHelp = new JMenu("Help");
 		menuBar.add(mHelp);
 		
 		JMenuItem mitemUserGuide = new JMenuItem("User Guide");
 		mHelp.add(mitemUserGuide);
 		
 		JMenuItem mitemAbout = new JMenuItem("About");
 		mHelp.add(mitemAbout);
 	} 
 	//End of initialize()
 	
 	
 	//************************************************************
 	// swapView
 	//		Change the current screen of the program.
 	// 		Param: cardName - the name of the card to be switched to
 	//************************************************************
 	public void swapView(String cardName) {
 	      cardlayout.show(cards, cardName);
 	}
 	
 	
 	//************************************************************
 	// resetEditScreen
 	//		Clear all fields on the Edit Workout screen.
 	//************************************************************
 	public void resetEditScreen() {
 		//
 		//TO DO
 		//
 	}
 	
 	
 	//************************************************************
 	// togglePreviewWindow
 	//		Show/hide the workout preview window.
 	//************************************************************
 	public void togglePreviewWindow() {
 		if (frmPreview.isVisible() == false) {
 			frmPreview.setLocationRelativeTo(frmDrillSergeant);
 			frmPreview.setVisible(true);
 		} else {
 			frmPreview.setVisible(false);
 		}
 	}
 	
 	
 	//************************************************************
 	// mdPrint
 	//		Print any string output to a message dialog.
 	//		Param: 	outputString - the string to be displayed in the message box
 	//				component - the component on which to display the message box. 
 	//************************************************************
 	public void mdPrint(String outputString, Component component) {
 		JOptionPane.showMessageDialog(component, outputString, "Debug", JOptionPane.INFORMATION_MESSAGE, null);
 	}
 	
 	
 	//************************************************************
 	// parseXML
 	//		Parse the xml file containing data for all the workouts.
 	//		Param: 	uri - the URI of the xml file.
 	//************************************************************
 	public void parseXML(String uri) throws Exception {
     	//Create a "parser factory" for creating SAX parsers
     	SAXParserFactory spfac = SAXParserFactory.newInstance();
 	
     	//Now use the parser factory to create a SAXParser object
     	SAXParser sp = spfac.newSAXParser();
 	
     	//Create an instance of this class; it defines all the handler methods
     	//handler = new XMLSaxParser();
 	
     	//Finally, tell the parser to parse the input and notify the handler
     	sp.parse(uri, handler);
     	handler.readList();
     	displayWorkoutList(handler);
 	}
 	
 	//************************************************************
 	// displayWorkoutList
 	//		Display the list of workouts parsed from the xml file
 	//		in the list object on the Workout Select screen.
 	//************************************************************
 	public void displayWorkoutList(XMLSaxParser handler) {
 		ArrayList<String> listItems = new ArrayList<String>(handler.getListItems());
 		listWorkout.setListData(listItems.toArray());
 	}
 	
 	//************************************************************
 	// selectWorkout
 	//		This method is called when the user highlights a workout
 	//		from the list of workouts and presses either the "Open"
 	//		or "Edit" button.
 	//************************************************************
 	public void selectWorkout(XMLSaxParser theHandler, int index, String callingBtnName) {
 		//Load the workout.
 		System.out.println(index);
 		newWorkout = new Workout(""); 				//Create a new empty workout so the selected saved workout can be copied into it.
 		newWorkout = theHandler.getWorkout(index);	//Copy the saved workout into the new workout.
 		System.out.println(newWorkout.toString());
 		txtWorkout.setText(newWorkout.getName());
 		
 		for (int i=0; i<newWorkout.getExerciseListSize(); i++) {
 			int previewIndex = frmPreview.add(newWorkout.getExercise(i));
 			newWorkout.getExercise(i).setPosition(previewIndex);
 		}
 		
 		//Switch to either the "Active Workout" or "Edit Workout"
 		if (callingBtnName == "btnOpen2") {
 			swapView("card4");
 		} else if (callingBtnName == "btnEdit") {
 			swapView("card3");
 		}
 	}
 	
 }
