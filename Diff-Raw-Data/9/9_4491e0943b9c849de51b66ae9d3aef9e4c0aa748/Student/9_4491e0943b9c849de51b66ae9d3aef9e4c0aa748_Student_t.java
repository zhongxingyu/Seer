 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 
 public class Student extends JFrame implements ActionListener {
 	private final int WINDOW_WIDTH = 900, WINDOW_HEIGHT = 500;
 
 	private JButton addButton,removeButton,saveButton,exitButton,compareButton;
 	private JPanel selectPanel,functionPanel,classButtons;
 	private JComboBox subjectBox,numberBox,degreeBox,degreeBox2;
 	private JList studentClasses;
 	private DefaultListModel<String> transcript;
 	private FileReader studentWriter;
 	private String hashedUser;
 	private Toolkit toolkit = Toolkit.getDefaultToolkit();
 
 	public Student(){
 		//Window Attributes
 		setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
 		setTitle("Academic Major Selection");
 
 		//Center the Program Window
 		Dimension dim = new Dimension(toolkit.getScreenSize());
 		setLocation((int)dim.getWidth()/2 - WINDOW_WIDTH/2,(int)dim.getHeight()/2 - WINDOW_HEIGHT/2);
 
 		//do not allow resizing of window
 		setResizable(false);
 
 		studentWriter = new FileReader();
 
 		//create arrays for classes
 		//input file reader here
 		String[] subjectArray = null;
 		String[] degreeArray = null;
 		String[] degreeArray2 = null;
 		try {
 			subjectArray = setFirstDropDownBox();
 			degreeArray = setDegreeDropDownBox(false); 
 			degreeArray2 = setDegreeDropDownBox(true);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		String[] numberArray = {"100","200","300","400","500"};
 		
 
 
 		//create boxes for dropdown
 		subjectBox = new JComboBox(subjectArray);
 		subjectBox.addActionListener(this);
 		numberBox = new JComboBox(numberArray);
 		degreeBox = new JComboBox(degreeArray);
 		degreeBox2 = new JComboBox(degreeArray2);
 
 		//creates a panel for our dropdown panels
 		selectPanel = new JPanel();
 		selectPanel.setLayout(new GridLayout(1,2,25,10));
 		selectPanel.add(subjectBox);
 		selectPanel.add(numberBox);
 
 		//places dropdowns into another panel for formatting
 		JPanel dropdownBoxes = new JPanel();
 
 		//add dropdown boxes to select panel
 		JPanel degreePanel = new JPanel();
 		degreePanel.add(new JLabel("Select Degree"));
 		degreePanel.add(degreeBox);
 		JPanel degreePanel2 = new JPanel();
 		degreePanel2.add(new JLabel(" If Dual Major :"));
 		degreePanel2.add(degreeBox2);
 		dropdownBoxes.add(degreePanel,BorderLayout.CENTER);
 		dropdownBoxes.add(degreePanel2,BorderLayout.CENTER);
 		dropdownBoxes.add(selectPanel,BorderLayout.CENTER);
 
 		//create add and remove function buttons
 		addButton = new JButton("Add Class ===>");
 		removeButton = new JButton("<=== Remove Class");
 		compareButton = new JButton("Run Comparison");
 
 		//place add and remove buttons into 2 panels for formatting
 		JPanel classButtons = new JPanel();
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new GridLayout(2,1,0,25));
 		buttons.add(addButton);
 		buttons.add(removeButton);
 
 		//add our buttons to main panel
 		classButtons.add(buttons,BorderLayout.CENTER);
 
 		//demo list for our list box
 		transcript = new DefaultListModel<String>();
 		//transcript.addElement("CSC305");
 		//transcript.addElement("MTH212");
 
 		//Create panels for our output list
 		//output list attributes
 		studentClasses = new JList(transcript);
 		studentClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		studentClasses.setLayoutOrientation(JList.VERTICAL);
 		studentClasses.setVisibleRowCount(-1);
 		JScrollPane scrollableList = new JScrollPane(studentClasses);
 		//places main functions save and exit into a panel
 		functionPanel = new JPanel();
 		JPanel saveExitButtons = new JPanel();
 		saveButton = new JButton("SAVE");
 		exitButton = new JButton("EXIT");
 
 		//place in another panel for formatting
 		functionPanel.setLayout(new GridLayout(2,1,0,25));
 		functionPanel.add(saveButton);
 		functionPanel.add(exitButton);
 		functionPanel.add(compareButton);
 		saveExitButtons.add(functionPanel,BorderLayout.CENTER);
 
 		
 		//Call for Instantiation of instructions
 		String[] instructions = instructionInit();
 		JTextArea selectStep = new JTextArea(instructions[0]);
 		selectStep.setEditable(false);
 		selectStep.setOpaque(false);
 		selectStep.setFont(new Font("SERIF",Font.BOLD, 16));
 		
 		JTextArea addStep = new JTextArea(instructions[1]);
 		addStep.setEditable(false);
 		addStep.setOpaque(false);
 		addStep.setFont(new Font("SERIF",Font.BOLD, 16));
 		
 		JTextArea checkStep = new JTextArea(instructions[2]);
 		checkStep.setEditable(false);
 		checkStep.setOpaque(false);
 		checkStep.setFont(new Font("SERIF",Font.BOLD, 16));
 		
 		JTextArea submitStep = new JTextArea(instructions[3]);
 		submitStep.setEditable(false);
 		submitStep.setOpaque(false);
 		submitStep.setFont(new Font("SERIF",Font.BOLD, 16));
 		
 		//Grid for the entire Main Window. "" represents blank grid spaces.
 		// grid adds left to right, top to bottom in a 3x3 grid. 1 2 3
 		//                                                       4 5 6
 		//                                                       7 8 9
 		getContentPane().setLayout(new GridLayout(3,3));
 		getContentPane().add(selectStep);
 		getContentPane().add(addStep);
 		getContentPane().add(checkStep);
 		getContentPane().add(dropdownBoxes);
 		getContentPane().add(classButtons);
 		getContentPane().add(scrollableList);
 		getContentPane().add(new JLabel(""));
 		getContentPane().add(submitStep);
 		getContentPane().add(saveExitButtons);
 
 		//add the Action Listener to all Buttons and list
 		addButton.addActionListener(this);
 		removeButton.addActionListener(this);
 		saveButton.addActionListener(this);
 		exitButton.addActionListener(this);
 		compareButton.addActionListener(this);
 
 		try {
 			setSecondDropDownBox();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 		//show the window
 		setVisible(true);
 	}
 	
 	private String[] instructionInit() {
 		// TODO Auto-generated method stub
 		String[] instructions = new String[4];
 		
 		//first instruction paragraph
 		//for dropdown boxes
 		instructions[0] = 	"\n         Step 1: Select your degree. \n" + 
 							"\n         Step 2: Select a class + course #";
 		//Second instruction paragraph
 		// for add and remove buttons
 		instructions[1] = 	"\n Step 3: Click add to add classes. \n";
 		
 		//Third instruction paragraph
 		//Removing and checking classes
 		instructions[2] = 	"\n Step 4: Check if your classes are correct! \n" +
 							"\n If you need to remove classes, select them, \n" +
 							" then Click the Remove Button. ";
 		
 		instructions[3] =   "\n Step 5: When your class list is fully built \n" +
 							"click the SAVE button. \n" +
 							"Then click run comparison.";
 		
 		return instructions;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		//What button is pressed?
 		if(e.getActionCommand() == addButton.getText()){
 			System.out.println("Add button Pressed");
 			if(classAlreadyThereCheck()){
 				System.out.println("You've already added that class.");
 			}
 			else{
 				transcript.addElement(new StringBuilder("").append(subjectBox.getSelectedItem().toString()).append(" ").append(numberBox.getSelectedItem()).toString());
 			}
 
 			String[] sortedClasses = sortClasses(transcript.toArray());
 			transcript.clear();
 			for(int i = 0; i < sortedClasses.length;i++){
 				transcript.addElement(sortedClasses[i]);
 			}
 		}else if(e.getActionCommand() == removeButton.getText()){
 			System.out.println("Remove button Pressed");
 			int s = studentClasses.getSelectedIndex();
 			if(s!=-1){
 				transcript.remove(s);	
 			}
 		}else if(e.getActionCommand() == saveButton.getText()){
 			System.out.println("Save button Pressed");
 			Object[] takenClassArray = new Object[transcript.getSize()]; //Makes an array of objects
 			System.out.println("Taken classes: " + takenClassArray.toString());
 			takenClassArray = transcript.toArray();//Copies classes into array
 
 			try {
 				//Calls FileWriter to save the array of classes to a file of the user's hashedName
 				studentWriter.writeUserSave(hashedUser, takenClassArray, transcript.getSize());
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}else if(e.getActionCommand() == exitButton.getText()){
 			System.out.println("Exit button Pressed");
 			System.exit(0);
 		}else if(e.getActionCommand() == compareButton.getText()){
 			System.out.println("Comparison button Pressed");
 			CompareEngine compare = null;
 			try {
 				compare = new CompareEngine(degreeBox.getSelectedItem().toString());
 				DefaultListModel<String> copytranscript = new DefaultListModel<String>();
 				for(int i=0; i<transcript.getSize();i++){
 					copytranscript.addElement(transcript.get(i));
 				}
 				ArrayList<String> recommendList1 = compare.compare(copytranscript);
 				ArrayList<String> recommendList2 = new ArrayList<String>();
 				if(degreeBox2.getSelectedItem().toString()!=""){
 					compare.resetReqs(degreeBox2.getSelectedItem().toString());
 					
 					copytranscript.clear();
 					for(int i=0; i<transcript.getSize();i++){
 						copytranscript.addElement(transcript.get(i));
 					}
 					recommendList2 = compare.compare(copytranscript);
 				}
 				recommendList1 = compare.DoubleMajor(recommendList1, recommendList2);
 				ResultsGUI results = new ResultsGUI(recommendList1);
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}else if(((String)subjectBox.getSelectedItem()).length() == 3){
 			System.out.println("New Subject Selected");
 			try {
 				setFirstDropDownBox();
 				setSecondDropDownBox();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 
 	}
 
 	String[] setFirstDropDownBox() throws IOException{
 		int i=0;
 		String[] someArray = studentWriter.fetchFiles("//catalogs");
 
 		String[] someSubjectArray = new String[someArray.length];
 		while(i<someArray.length){
 			someSubjectArray[i] = someArray[i].substring(0,someArray[i].lastIndexOf("."));
 			i++;
 		}
 		return someSubjectArray;
 	}
 
 	void setSecondDropDownBox() throws IOException{
 		int j=0;
 
 		String[][] someArrayTwoD = studentWriter.fetchCatalog(studentWriter.fetchFiles("//catalogs"));
 
 		String[] numberArray = new String[someArrayTwoD[subjectBox.getSelectedIndex()].length-1];
 		for(int i=0; i<numberArray.length; i++){
 			numberArray[i] = someArrayTwoD[subjectBox.getSelectedIndex()][i+1];
 		}	
 
 		//selectPanel.remove(subjectBox);
 		selectPanel.remove(numberBox);
 
 		//subjectBox = new JComboBox(subjectArray);
 		numberBox = new JComboBox(numberArray);
 
 		//selectPanel.add(subjectBox);
 		selectPanel.add(numberBox);
 		selectPanel.validate();
 	}
 
 	String[] setDegreeDropDownBox(boolean addNothingOption) throws IOException{
 		if(addNothingOption){
 			int i=0;
 			String[] someArray = studentWriter.fetchFiles("//requirements");
 	
			String[] someDegreeArray = new String[someArray.length+1];
			someDegreeArray[0] = "";
 			while(i<someArray.length){
				
				someDegreeArray[i+1] = someArray[i].substring(0,someArray[i].lastIndexOf("."));
 				i++;
 			}
 			return someDegreeArray;
 		}else{
 			int i=0;
 			String[] someArray = studentWriter.fetchFiles("//requirements");
 	
 			String[] someDegreeArray = new String[someArray.length];
 			while(i<someArray.length){
 				someDegreeArray[i] = someArray[i].substring(0,someArray[i].lastIndexOf("."));
 				i++;
 			}
 			return someDegreeArray;
 		}
 	}
 	/**
 	 *This method checks to see if the class trying to be added is already in the list of taken classes.
 	 *If so, returns true. If not, returns false.
 	 */
 	boolean classAlreadyThereCheck(){
 		Object[] classesTakenObj = new Object[transcript.getSize()];
 		String[] classesTakenStr = new String[transcript.getSize()]; 
 		classesTakenObj = transcript.toArray();//Puts elements of transcript into array of objects
 
 		//For loop below copies the object array into the String array as strings
 		for(int j=0; j<transcript.getSize(); j++){
 			classesTakenStr[j] = classesTakenObj[j].toString();
 		}
 
 		//someClass is a String containing the current class trying to be added
 		String someClass = new StringBuilder("").append(subjectBox.getSelectedItem().toString()).append(" ").append(numberBox.getSelectedItem()).toString();
 		for(int i=0; i<transcript.getSize(); i++){
 			if(classesTakenStr[i].compareTo(someClass) == 0){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Sets variable equal to the current user's hashed name.
 	 * Used to send to the filewriter so it knows where to save a student's classes
 	 * @param someHash (the user name hashed)
 	 */
 	 public void setHashName(String someHash){
 		hashedUser = someHash;
 	}
 
 	public void loadStudentClasses() throws FileNotFoundException{
 		 String[] tempLoadClass = null;
 		 tempLoadClass = studentWriter.readLoadClasses(hashedUser);
 
 		 for(int i=0; i<tempLoadClass.length; i++){
 			 transcript.addElement(tempLoadClass[i]);
 		 }
 	}
 
 	private String[] sortClasses(Object[] someTranscript){
 		//Object[] objectList = new Object[someTranscript.length];
 		String[] stringList = new String[someTranscript.length];
 
 
 		//change objects to strings.
 		for (int k = 0; k < stringList.length;k++){
 			stringList[k] = someTranscript[k].toString();
 		}
 
 
 
 		if (someTranscript.length <= 1){
 			return stringList;
 		}
 
 		int i = 0;
 		while(i< stringList.length){
 		String smallest = stringList[i];
 		int smallestIndex = i;
 			int j = i;
 			while(j < stringList.length){
 				if(stringList[j].compareTo(smallest) <= 0){
 					smallest = stringList[j];
 					smallestIndex = j;
 				}
 				j++;
 			}
 
 			String temp;
 			temp = stringList[i];
 			stringList[i] = stringList[smallestIndex];
 			stringList[smallestIndex] = temp;
 			i++;
 		}
 
 		return stringList;
 	}
 
 }
