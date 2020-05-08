 
 package GUI;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListSelectionModel;
 
 public class Student extends JFrame implements ActionListener {
 	private final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 300;
 	
 	private JButton addButton,removeButton,saveButton,exitButton;
 	private JPanel selectPanel,functionPanel,classButtons;
 	private JComboBox subjectBox,numberBox;
 	private JList studentClasses;
 	private DefaultListModel transcript;
 	
 	
 	
 	public Student(){
 		//Window Attributes
 		setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
 		setTitle("Academic Major Selection");
 		
 		//do not allow resizing of window
 		setResizable(false);
 		
 		//create arrays for classes
 		//input file reader here
 		String[] subjectArray = {"CSC","MTH","ENG","THE","PHY"};
 		String[] numberArray = {"100","200","300","400","500"};
 		
 		//create boxes for dropdown
 		subjectBox = new JComboBox(subjectArray);
 		numberBox = new JComboBox(numberArray);
 		
 		//creates a panel for our dropdown panels
 		selectPanel = new JPanel();
 		selectPanel.setLayout(new GridLayout(1,2,25,0));
 		selectPanel.add(subjectBox);
 		selectPanel.add(numberBox);
 		
 		//places dropdowns into another panel for formatting
 		JPanel dropdownBoxes = new JPanel();
 		
 		//add dropdown boxes to select panel
 		dropdownBoxes.add(selectPanel,BorderLayout.CENTER);
 		
 		//create add and remove function buttons
 		addButton = new JButton("Add Class ===>");
 		removeButton = new JButton("<=== Remove Class");
 		
 		//place add and remove buttons into 2 panels for formatting
 		JPanel classButtons = new JPanel();
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new GridLayout(2,1,0,25));
 		buttons.add(addButton);
 		buttons.add(removeButton);
 		
 		//add our buttons to main panel
 		classButtons.add(buttons,BorderLayout.CENTER);
 	
 		//demo list for our list box
 		transcript = new DefaultListModel();
 		transcript.addElement("CSC305");
 		transcript.addElement("MTH212");
 		
 		//Create panels for our output list
 		//output list attributes
 		studentClasses = new JList(transcript);
 		studentClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		studentClasses.setLayoutOrientation(JList.VERTICAL);
 		studentClasses.setVisibleRowCount(-1);
 		
 		//places main functions save and exit into a panel
 		functionPanel = new JPanel();
 		JPanel saveExitButtons = new JPanel();
 		saveButton = new JButton("SAVE");
 		exitButton = new JButton("EXIT");
 		
 		//place in another panel for formatting
 		functionPanel.setLayout(new GridLayout(2,1,0,25));
 		functionPanel.add(saveButton);
 		functionPanel.add(exitButton);
 		saveExitButtons.add(functionPanel,BorderLayout.CENTER);
 		
 		//Grid for the entire Main Window. "" represents blank grid spaces.
 		// grid adds left to right, top to bottom in a 3x3 grid. 1 2 3
 		//                                                       4 5 6
 		//                                                       7 8 9
 		getContentPane().setLayout(new GridLayout(3,3));
 		getContentPane().add(new JLabel(""));
 		getContentPane().add(new JLabel(""));
 		getContentPane().add(new JLabel(""));
 		getContentPane().add(dropdownBoxes);
 		getContentPane().add(classButtons);
 		getContentPane().add(studentClasses);
 		getContentPane().add(new JLabel(""));
 		getContentPane().add(new JLabel(""));
 		getContentPane().add(saveExitButtons);
 		
 		//add the Action Listener to all Buttons and list
 		addButton.addActionListener(this);
 		removeButton.addActionListener(this);
 		saveButton.addActionListener(this);
 		exitButton.addActionListener(this);
 		
 		//show the window.
 		setVisible(true);
 		
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		
 		//What button is pressed?
 		if(e.getActionCommand() == addButton.getText()){
 			System.out.println("Add button Pressed");
 			transcript.addElement(new StringBuilder("").append(subjectBox.getSelectedItem()).append(numberBox.getSelectedItem()));
 			
 		}else if(e.getActionCommand() == removeButton.getText()){
 			System.out.println("Remove button Pressed");
 			int s = studentClasses.getSelectedIndex();
			transcript.remove(s);
 		}else if(e.getActionCommand() == saveButton.getText()){
 			System.out.println("Save button Pressed");
 			
 		}else if(e.getActionCommand() == exitButton.getText()){
 			System.out.println("Exit button Pressed");
 			
 		}
 			
 	}
 }
