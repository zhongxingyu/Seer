 package edu.ilstu;
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import edu.ilstu.RemoveStudentGUI.cancelListener;
 
 
 /**
  * Creates the interface to add a student to the list
  * @author John, Rachel, Corbin
  *
  */
 public class AddStudentGUI extends JFrame 
 {
 	//assets of the first name panel
 	private JPanel fNamePanel; 
 	private JLabel fNameLabel;
 	private JTextField fNameField;
 	private String fName;
 	
 	//assets of the last name panel
 	private JPanel lNamePanel;
 	private JLabel lNameLabel;
 	private JTextField lNameField;
 	private String lName;
 	
 	private JPanel buttonPanel;
 	private JButton addButton;
 	private JButton cancelButton;
 	
 	private JPanel genderPanel;
 	private ButtonGroup genderButtonGroup;
 	private JRadioButton male;
 	private JRadioButton female;
 	
 	private Group addStudentList;
 	private File currentFile;
 	
 	private String gender;
 	
 	/**
 	 * Default Constructor
 	 */
 	public AddStudentGUI(Group studentList)
 	{
 		//sets up frame
     	super("Add a Student");
 		setSize(800, 450);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setLayout(new GridLayout(0,1));
 		
 		addStudentList = studentList;
 		
 		//builds panels
 		buildFirstNamePanel();
 		buildLastNamePanel();
 		buildButtonPanel();
 		buildGenderPanel();
 		
 		//adds panels
 		add(fNamePanel);
 		add(lNamePanel);
 		add(genderPanel);
 		add(buttonPanel);
 		
 		pack();
 		setVisible(true);
 	}
 	
 	/**
 	 * creates first name panel
 	 */
 	public void buildFirstNamePanel()
 	{
 		//sets up first name assets
 		fNamePanel = new JPanel();
 		fNamePanel.setLayout(new FlowLayout());
 		fNameLabel = new JLabel("First Name: ");
 		fNameField = new JTextField(15);
 		
 		//adds first name assets
 		fNamePanel.add(fNameLabel);
 		fNamePanel.add(fNameField);
 	}
 	
 	/**
 	 * creates last name panel
 	 */
 	public void buildLastNamePanel()
 	{
 		//sets up last name panel
 		lNamePanel = new JPanel();
 		lNamePanel.setLayout(new FlowLayout());
 		lNameLabel = new JLabel("Last Name: ");
 		lNameField = new JTextField(15);
 		
 		//adds last name assets
 		lNamePanel.add(lNameLabel);
 		lNamePanel.add(lNameField);
 	}
 	
 	public void buildGenderPanel()
 	{
 		genderPanel = new JPanel();
 		genderPanel.setLayout(new FlowLayout());
 		
 		male = new JRadioButton("male");
 		female = new JRadioButton("female");
 		
 		genderButtonGroup = new ButtonGroup();
 		genderButtonGroup.add(male);
 		genderButtonGroup.add(female);
 		
 		genderPanel.add(male);
 		genderPanel.add(female);
 	}
 	
 	public void buildButtonPanel()
 	{
 		buttonPanel = new JPanel();
 		buttonPanel.setLayout(new FlowLayout());
 		
 		addButton = new JButton("Add");
 		addButton.addActionListener(new addStudentListener());
 		
 		cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(new cancelListener());
 		
 		buttonPanel.add(addButton);
 		buttonPanel.add(cancelButton);
 	}
 	
     private class cancelListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
         {	
     		dispose();
         }
     }
     
     private class addStudentListener  implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
         {	
     		fName = fNameField.getText();
     		lName = lNameField.getText();
     		gender = "";
     		
     		if(male.isSelected())
     		{
     			gender = "M";
     		}
     		else if(female.isSelected())
     		{
     			gender = "F";
     		}
     		else
     		{
     			gender = null;
     		}
     		
     		addStudentList.addStudent(fName, lName, gender);
     		dispose();
         }
     } 
 }
