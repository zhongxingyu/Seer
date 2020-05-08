 package edu.ilstu;
 
 import java.awt.GridLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JCheckBox;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Description: GroupCreatorGUI that generates the GUI that allows a user to
  * select the various ways of creating a group.
  * @author Rachel A Schifano
  */
 
 public class GroupCreatorSortPanel extends JPanel {
 
 	// Radio buttons for group number sorting
 	private JRadioButton studentsPerGroup;
 	private JRadioButton groups;
 	
 	// Checkbox for optional gender sorting
 	private JCheckBox gender;
 	
 	// Buttongroup variable will reference an object to group the radio buttons and checkbox
 	private ButtonGroup bg_sort;
 	
 	private GroupListApp groupListApp;
 	
 	
 	/**
 	 * Constructor
 	 */
 	public GroupCreatorSortPanel() {
		groupListApp = new GroupListApp();
 		
 		// GridLayout manager with three rows one column
 		setLayout(new GridLayout(3,1));
 		
 		studentsPerGroup = new JRadioButton("Number of students per group."); // radio button
 		studentsPerGroup.addActionListener(new StudentNumberListener());
 		
 		groups = new JRadioButton("Number of groups."); // radio button
 		groups.addActionListener(new GroupNumberListener());
 		
 		gender = new JCheckBox("Gender."); // check box
 		gender.addActionListener(new GenderListener());
 		
 	    // Group the radio buttons.
 	    bg_sort = new ButtonGroup();
 	    bg_sort.add(studentsPerGroup);
 	    bg_sort.add(groups);
 	    
 	    // Adds border around panel
 	    setBorder(BorderFactory.createTitledBorder("Sort"));
 	   
 	    // Add buttons to panel.
 	    add(studentsPerGroup);
 	    add(groups);
 	    add(gender);
 	}
 	
 	
 	/**
 	 * Private method that takes the number of students, parses the String to an int,
 	 * and passes it through the ** method.
 	 */
 	private class StudentNumberListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			String studentNumber = JOptionPane.showInputDialog(null,
 					"Enter the number of students.");
 			
 			int studentNum = Integer.parseInt(studentNumber);
 			
 			groupListApp.setGroupSize(studentNum);
 			
 		}
 	}
 	
 	/**
 	 * Private method that takes the number of groups, parses the String to an int,
 	 * and passes it through the ** method.
 	 */
 	private class GroupNumberListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			String groupNumber = JOptionPane.showInputDialog(null,
 					"Enter the number of groups.");
 			
 			int groupNum = Integer.parseInt(groupNumber);
 			
 			groupListApp.setNumGroups(groupNum);
 		}
 	}
 	
 	private class GenderListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			
 			boolean gender = true;
			
 			groupListApp.setBiased(gender);
 			
 		}
 		
 	}
 	
 	
 }
