 /**
  * Programmers: Chase McCowan & Ed Broxson 
  * Date: 02/20/2013 
  * Purpose: Build GUI using GridBagLayout and handle answer checking
  */
 package layout;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DecimalFormat;
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class GaseousDiffusion {
 
     private final static boolean shouldFill = true;
     private final static boolean RIGHT_TO_LEFT = false;
     private static int height, width, box1 = 0, box2 = 0;
     private static String periodic, help, helpContent;
     private static double time1, time2;
     private static float rate1, rate2, mw1, mw2;
     protected static JTable table;
     private static GasChamber bt = new GasChamber();
     private static JDialog dialog, helpDialog;
     private static JLabel imageLabel;
     private static JPanel periodicPanel;
     private static JTextArea helpTextArea;
     private static DecimalFormat format = new DecimalFormat("#,##0.000");
 
     /**
      * Build components for GUI and add listeners
      */
     public static void addComponentsToPane(Container pane) {
         if (RIGHT_TO_LEFT) {
             pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         }
         //declare variables 
         JPanel periodHelpPanel, knownComboPanel, unknownComboPanel, sliderPanel,
                 tablePanel, boxPanel, checkResetButtonPanel, resultPanel, 
                 correctPanel, pauseLabelPanel;
         JButton periodButton, helpButton, goButton,
                 resetButton, checkAnswerButton;
         JLabel gasDiffuseLabel, enterLabel, knownComboBoxLabel,
                 unknownComboBoxLabel, tempLabel, pauseLabel, sigfigLabel;
         final JLabel correctLabel, correctLabel2, incorrectLabel, incorrectLabel2;
         JScrollPane jsp;
         final JComboBox knownComboBox, unknownComboBox;
         JList <String> knownList, unknownList;
         final JSlider slider;
 
         //make raisedBevel border 
         Border raisedBevel = BorderFactory.createRaisedSoftBevelBorder();
 
         //set pane layout to grid bag layout 
         pane.setLayout(new GridBagLayout());
 
         //set pane color 
         pane.setBackground(new Color(12, 66, 116));
 
         //make grid bag constraints object 
         GridBagConstraints c = new GridBagConstraints();
 
         //natural height, maximum width 
         if (shouldFill) {
             c.fill = GridBagConstraints.HORIZONTAL;
         }
 
         ///***************************TITLE**************************
         //title label 
         gasDiffuseLabel = new JLabel("Graham's Law");
 
         //set font for label 
         gasDiffuseLabel.setFont(new Font("Verdana", Font.BOLD, 50));
 
         //set foreground color for label 
         gasDiffuseLabel.setForeground(Color.white);
 
         //set grid bag layout constraints 
         c.insets = new Insets(10, 10, 10, 10);
         c.ipadx = 0;
         c.ipady = 0;
         c.weightx = 0.5;
         c.weighty = 0;
         c.gridwidth = 15;
         c.gridheight = 1;
         c.fill = GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 0;
 
         //add panel and contraints to pane 
         pane.add(gasDiffuseLabel, c);
 
         ///******PERIODIC TABLE BUTTON AND HELP BUTTON***************
 
         //make grid bag constraints object 
         GridBagConstraints periodicGBC = new GridBagConstraints();
 
         //panel for periodButton and helpButton 
         periodHelpPanel = new JPanel();
 
         //set panel// color 
         periodHelpPanel.setBackground(new Color(203, 228, 38));
 
         //set border for button 
         periodHelpPanel.setBorder(raisedBevel);
 
         //make buttons 
         periodButton = new JButton("Periodic Table");
         helpButton = new JButton("Help");
 
         //set grid bag layout constraints 
         //set fill 
         periodicGBC.fill = GridBagConstraints.HORIZONTAL;
 
         //set padding 
         periodicGBC.insets = new Insets(0, 0, 0, 0);
 
         //set weight 
         periodicGBC.weightx = 0.5;
 
         //set grid position 
         periodicGBC.gridx = 9;
         periodicGBC.gridy = 0;
 
         //set height and width of grid cell 
         periodicGBC.ipadx = 0;
         periodicGBC.ipady = 0;
 
         //set anchor point within cell 
         periodicGBC.anchor = GridBagConstraints.EAST;
 
         //add buttons to panel 
         periodHelpPanel.add(periodButton);
         periodHelpPanel.add(helpButton);
 
         //add panel and contraints to pane 
         pane.add(periodHelpPanel, periodicGBC);
 
         ///*******DRAWING START BOX, RACE BOX, GATE AND FINISH*********
 
         //make grid bag constraints object 
         GridBagConstraints boxGBC = new GridBagConstraints();
 
         //make boxPanel and set layout to gridbaglayout 
         boxPanel = new JPanel();
 
         boxPanel.setBackground(new Color(12, 66, 116));
 
 
         //initialize bt and make it focusable  
         bt.init();
         bt.setFocusable(true);
 
         //add bt object to boxPanel 
         boxPanel.add(bt);
 
         // initialize particleFill from GasChamber using variables from 
          // combo boxes
         bt.particleFill(0, 0);
 
         //set grid bag layout constraints 
         boxGBC.insets = new Insets(10, 10, 10, 10);
         boxGBC.ipadx = 500;                                              
         boxGBC.ipady = 200;                                              
         boxGBC.weightx = 0.5;
         boxGBC.gridwidth = 15;                                            
         boxGBC.gridheight = 1;                         
         boxGBC.fill = GridBagConstraints.BOTH;
         boxGBC.gridx = 0;
         boxGBC.gridy = 1;
 
         //add panel and contraints to pane 
         pane.add(boxPanel, boxGBC);
 
         ///*****KNOWN COMBO BOX***********
 
         //make grid bag constraints object 
         GridBagConstraints knownGBC = new GridBagConstraints();
 
         //new jpanel for the known molecules combobox 
         knownComboPanel = new JPanel(new BorderLayout());
 
         //set panel color 
         knownComboPanel.setBackground(new Color(203, 228, 38));
 
         //set border for button 
         knownComboPanel.setBorder(raisedBevel);
 
         //create known combo box list label 
         knownComboBoxLabel = new JLabel("Select First Gas: ");
 
         //set font for label 
         knownComboBoxLabel.setFont(new Font("Verdana", Font.BOLD, 15));
 
         //set foreground color for label 
         knownComboBoxLabel.setForeground(new Color(12, 66, 116));
 
         //create array of list data for JList 
         String[] knownComboBoxListData = {"Helium (He)",
             "Neon (Ne)", "Argon (Ar)",};
 
         //create combobox and add list data to it 
         knownComboBox = new JComboBox(knownComboBoxListData);
 
         //create a courses list 
         knownList = new JList<String>(knownComboBoxListData);
 
         //set visible amount of rows in JList to 1 
         knownList.setVisibleRowCount(1);
 
 
 
         //set grid bag layout constraints 
         knownGBC.fill = GridBagConstraints.NONE;
         knownGBC.insets = new Insets(0, 0, 0, 0);
         knownGBC.weightx = 0.5;
         knownGBC.gridwidth = 1;
         knownGBC.gridx = 0;
         knownGBC.gridy = 3;
         knownGBC.ipadx = 30;
         knownGBC.ipady = 0;
 
         //add combo box label to panel 
         knownComboPanel.add(knownComboBoxLabel, BorderLayout.NORTH);
 
         //add combo box label to panel 
         knownComboPanel.add(knownComboBox, BorderLayout.SOUTH);
 
         //add panel to pane 
         pane.add(knownComboPanel, knownGBC);
 
         ///*********UNKNOWN/KNOWN COMBO BOX***************
 
         //make grid bag constraints object 
         GridBagConstraints unknownGBC = new GridBagConstraints();
 
         //create jpanel for the unknown molecules combobox 
         unknownComboPanel = new JPanel(new BorderLayout());
 
         //set panel color 
         unknownComboPanel.setBackground(new Color(203, 228, 38));
 
         //set border for button 
         unknownComboPanel.setBorder(raisedBevel);
 
         //create known combo box list label
         unknownComboBoxLabel = new JLabel("Select Second Gas: ");
 
         //set font for label
         unknownComboBoxLabel.setFont(new Font("Verdana", Font.BOLD, 15));
 
         //set foreground color for label
         unknownComboBoxLabel.setForeground(new Color(12, 66, 116));
 
         //create array of list data for JList
         String[] unknownComboBoxListData = {"Helium (He)",
             "Neon (Ne)", "Argon (Ar)", "Unknown 1", "Unknown 2", "Unknown 3"};
 
         //create combobox and add data to it
         unknownComboBox = new JComboBox(unknownComboBoxListData);
 
         //create a list
         unknownList = new JList<String>(unknownComboBoxListData);
 
         //set visible amount of rows in combo box to 1
         unknownList.setVisibleRowCount(1);
 
         //set grid bag layout constraints
         unknownGBC.fill = GridBagConstraints.NONE;
         unknownGBC.insets = new Insets(10, 0, 10, 10);
         unknownGBC.weightx = 0.5;
         unknownGBC.gridx = 1;
         unknownGBC.gridy = 3;
         unknownGBC.ipadx = 30;
         unknownGBC.ipady = 0;
 
         //add combo box label to panel
         unknownComboPanel.add(unknownComboBoxLabel, BorderLayout.NORTH);
 
         //add combo box to panel
         unknownComboPanel.add(unknownComboBox, BorderLayout.SOUTH);
 
         //add panel to pane
         pane.add(unknownComboPanel, unknownGBC);
 
         ///*******TEMPERATURE/REACTION RATE +- SLIDER**************
 
         //make grid bag constraints object
         GridBagConstraints tempGBC = new GridBagConstraints();
 
         //make new panel
         sliderPanel = new JPanel(new BorderLayout());
 
         //set panel color
         sliderPanel.setBackground(new Color(12, 66, 116));
 
         //make slider
         slider = new JSlider();
 
         //set slider color
         slider.setBackground(new Color(12, 66, 116));
 
         //make temperature label
         tempLabel = new JLabel("< - Temperature + >");
 
         //set font for label
         tempLabel.setFont(new Font("Verdana", Font.BOLD, 15));
 
         //set foreground color for label
         tempLabel.setForeground(Color.white);
 
         //set foreground color for slider
         slider.setForeground(Color.white);
 
         //set slider constraints
         slider.setSize(100, 20);
         slider.setValue(85);
         slider.setMinimum(10);
         slider.setMaximum(160);
         slider.setMajorTickSpacing(25);
         slider.setMinorTickSpacing(5);
         slider.setPaintTicks(true);
 
         //set grid bag layout constraints
         tempGBC.fill = GridBagConstraints.HORIZONTAL;
         tempGBC.insets = new Insets(0, 10, 0, 0);
         tempGBC.weightx = 150;
         tempGBC.gridwidth = 1;
         tempGBC.gridx = 2;
         tempGBC.gridy = 3;
         tempGBC.ipadx = 0;
         tempGBC.ipady = 0;
 
         //add slider to panel
         sliderPanel.add(tempLabel, BorderLayout.NORTH);
         sliderPanel.add(slider, BorderLayout.CENTER);
 
         //add button to pane
         pane.add(sliderPanel, tempGBC);
 
         ///****************GO BUTTON********************
 
         //make grid bag constraints object
         GridBagConstraints goGBC = new GridBagConstraints();
 
         //create go button
         goButton = new JButton("GO");
 
         //set button color
         goButton.setBackground(new Color(203, 228, 38));
 
         //set font for label
         goButton.setFont(new Font("Verdana", Font.BOLD, 20));
 
         //set foreground color for label
         goButton.setForeground(Color.darkGray);
 
         //set border for button
         goButton.setBorder(raisedBevel);
 
         //set grid bag layout constraints
         goGBC.insets = new Insets(0, 0, 0, 30);
         goGBC.fill = GridBagConstraints.VERTICAL;
         goGBC.weightx = 50;
         goGBC.weighty = 0;
         goGBC.gridwidth = 2;
         goGBC.gridx = 9;
         goGBC.gridy = 3;
         goGBC.ipadx = 30;
         goGBC.ipady = 0;
 
         //add button to pane
         pane.add(goButton, goGBC);
 
         ///****************TABLE**********************
 
         //make grid bag constraints object
         GridBagConstraints tableGBC = new GridBagConstraints();
 
         //create panel for table
         tablePanel = new JPanel(new BorderLayout());
 
         //set panel color
         tablePanel.setBackground(new Color(12, 66, 116));
 
         //create label for table
         enterLabel = new JLabel("Use the data in the table to "
                 + "compute the Rate and Molecular Weight...");
 
         //set font for label
         enterLabel.setFont(new Font("Verdana", Font.BOLD, 20));
 
         //set foreground color for label
         enterLabel.setForeground(Color.white);
 
         //create arrays for column headings and table data
         String[] colHeading = {"Chosen Molecules", "Time (seconds)", "Rate (meters/second)", "Molecular Weight (MW)"};
         String[][] data = {{"", "", "", ""},{"", "", "", ""}};
 
         //create table
         table = new JTable(new MyTableModel(data, colHeading));
 
         //make tool tip
         table.setToolTipText("Calculate rate and molecular weight using "
                 + "Graham's Law and insert into table");
 
         //make table not enabled to be edited
         table.editCellAt(0, 2);
         table.editCellAt(0, 3);
         table.editCellAt(1, 2);
         table.editCellAt(1, 3);
 
         //create scrollpane for table
         jsp = new JScrollPane(table);
 
         //set grid bag layout constraints
         tableGBC.fill = GridBagConstraints.BOTH;
         tableGBC.insets = new Insets(10, 10, 10, 10);
         tableGBC.weightx = 0;
         tableGBC.weighty = 0;
         tableGBC.gridx = 0;
         tableGBC.gridy = 5;
         tableGBC.gridwidth = 15;
         tableGBC.ipadx = 50;
         tableGBC.ipady = 30;
 
         //add label to panel
         tablePanel.add(enterLabel, BorderLayout.NORTH);
 
         //add scrollpane to panel
         tablePanel.add(jsp);
 
         //add panel to pane
         pane.add(tablePanel, tableGBC);
 
         String choice1 = knownComboBox.getSelectedItem().toString();
         String choice2 = unknownComboBox.getSelectedItem().toString();
 
         time1 = GasChamber.getTime1();
         time2 = GasChamber.getTime2();
 
         table.setValueAt(choice1, 0, 0);
         table.setValueAt(choice2, 1, 0);
 
         ///*****************CORRECT AND INCORRECT LABELS******************
 
         correctLabel = new JLabel("1st Row Correct");
         correctLabel2 = new JLabel("2nd Row Correct");
         //make grid bag constraints object
         GridBagConstraints correctGBC = new GridBagConstraints();
 
         resultPanel = new JPanel(new BorderLayout());
         correctPanel = new JPanel();
         correctPanel.setLayout(new BoxLayout(correctPanel, BoxLayout.Y_AXIS));
 
         //set panel color
         resultPanel.setBackground(new Color(12, 66, 116));
         correctPanel.setBackground(new Color(12, 66, 116));
 
         //make correct labels
         correctLabel.setFont(new Font("Verdana", Font.BOLD, 20));
         correctLabel.setForeground(Color.GREEN);
         correctLabel.setVisible(false);
 
         correctLabel2.setFont(new Font("Verdana", Font.BOLD, 20));
         correctLabel2.setForeground(Color.GREEN);
         correctLabel2.setVisible(false);
 
         //make incorrect labels
         incorrectLabel = new JLabel("1st Row Incorrect");
         incorrectLabel2 = new JLabel("2nd Row Incorrect");
 
         incorrectLabel.setFont(new Font("Verdana", Font.BOLD, 20));
         incorrectLabel.setForeground(Color.RED);
         incorrectLabel.setVisible(false);
 
         incorrectLabel2.setFont(new Font("Verdana", Font.BOLD, 20));
         incorrectLabel2.setForeground(Color.RED);
         incorrectLabel2.setVisible(false);
 
         //set grid bag layout constraints
         correctGBC.anchor = GridBagConstraints.WEST;
         correctGBC.insets = new Insets(0, 20, 0, 0);
         correctGBC.weightx = 1;
         correctGBC.gridx = 2;
         correctGBC.gridy = 7;
         correctGBC.gridwidth = 1;
         correctGBC.ipadx = 0;
         correctGBC.ipady = 0;
 
         correctPanel.add(correctLabel);
         correctPanel.add(incorrectLabel);
         correctPanel.add(correctLabel2);
         correctPanel.add(incorrectLabel2);
 
         resultPanel.add(correctPanel, BorderLayout.CENTER);
 
         resultPanel.setVisible(true);
 
         pane.add(resultPanel, correctGBC);
 
         ///*****************PAUSE LABEL***********************************
         
         //make grid bag constraints object
         GridBagConstraints pauseGBC = new GridBagConstraints();
 
         //make pauseLabelPanel
         pauseLabelPanel = new JPanel(new BorderLayout());
         
         //set panel color
         pauseLabelPanel.setBackground(new Color(12, 66, 116));
         
         //make pauseLabel
         pauseLabel = new JLabel("**Pause and play the simulation at any time"
                 + " using spacebar.");
         sigfigLabel = new JLabel("***Enter rate using 3 significant figures."
                 + "  Enter molecular weight using 2 significant figures.");
 
         //set font color to white
         pauseLabel.setForeground(Color.WHITE);
         sigfigLabel.setForeground(Color.WHITE);
 
         //set grid bag layout constraints
         pauseGBC.anchor = GridBagConstraints.EAST;
         pauseGBC.insets = new Insets(0, 20, 0, 0);
         pauseGBC.weightx = 1;
         pauseGBC.gridx = 0;
         pauseGBC.gridy = 7;
         pauseGBC.gridwidth = 2;
         pauseGBC.ipadx = 0;
         pauseGBC.ipady = 0;
 
         //add pause and sigfig labels to pause panel
         pauseLabelPanel.add(pauseLabel,BorderLayout.NORTH);
         pauseLabelPanel.add(sigfigLabel,BorderLayout.SOUTH);
         
         //add pauseLabel to pane
         pane.add(pauseLabelPanel, pauseGBC);
 
         ///*****************CHECK ANSWER AND RESET BUTTONS****************
 
         //make grid bag constraints object
         GridBagConstraints answerGBC = new GridBagConstraints();
 
         //create panel for reset and answer buttons
         checkResetButtonPanel = new JPanel();
 
         //set panel color
         checkResetButtonPanel.setBackground(new Color(12, 66, 116));
 
         //create reset and answer buttons
         resetButton = new JButton("Reset");
         checkAnswerButton = new JButton("Check Answer");
 
         //set grid bag layout constraints
         answerGBC.anchor = GridBagConstraints.CENTER;
         answerGBC.insets = new Insets(0, 0, 0, 0);
         answerGBC.weightx = 1;
         answerGBC.gridx = 9;
         answerGBC.gridy = 7;
         answerGBC.gridwidth = 2;
         answerGBC.ipadx = 0;
         answerGBC.ipady = 0;
 
         //add buttons to panel
         checkResetButtonPanel.add(resetButton);
         checkResetButtonPanel.add(checkAnswerButton);
 
         //add panel to pane
         pane.add(checkResetButtonPanel, answerGBC);
         
         /**LISTENERS*/
         // listener for check answer button 
         checkAnswerButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                if (table.isEditing()){
                    table.getCellEditor().stopCellEditing();
                }
                 //get the values from user input cells and compare them to 
                 // correct values
                 try{
                     
                 rate1 = Float.parseFloat(table.getValueAt(0, 2).toString());
                 rate2 = Float.parseFloat(table.getValueAt(1, 2).toString());
                 mw1 = Float.parseFloat(table.getValueAt(0, 3).toString());
                 mw2 = Float.parseFloat(table.getValueAt(1, 3).toString());
                 
                 }
                 
                 catch(NumberFormatException nfe){
                     
                     JOptionPane.showMessageDialog(null, "Please enter a value"
                             + " in all rate and molecular weight table cells"
                             + " before clicking check answer button.","Answers",JOptionPane.INFORMATION_MESSAGE);
                     
                 }
                 
 
                 correctLabel.setVisible(false);
                 correctLabel2.setVisible(false);
                 incorrectLabel.setVisible(false);
                 incorrectLabel2.setVisible(false);
                 
                 if ((rate1 < ((bt.getVel1() / .02) * 1.002) && rate1 > ((bt.getVel1() / .02) * .998)) && ((mw1 < (bt.getMw1() * 1.02) && mw1 > (bt.getMw1() * .98)))) {
                     correctLabel.setVisible(true);
                 } else {
                     incorrectLabel.setVisible(true);
                 }
                 if ((rate2 < ((bt.getVel2() / .02) * 1.002) && rate2 > ((bt.getVel2() / .02) * .998)) && ((mw2 < (bt.getMw2() * 1.02) && mw2 > (bt.getMw2() * .98)))) {
                     correctLabel2.setVisible(true);
                 } else {
                     incorrectLabel2.setVisible(true);
                 }
 
                 JOptionPane.showMessageDialog(null, "The Rate you entered "
                         + "for selection one, " + table.getValueAt(0, 0)
                         + ", is: " + rate1 + "   the answer is: " + (bt.getVel1() / .02)
                         + "\nThe MW you entered is: " + mw1 + " the answer is: "
                         + (bt.getMw1() * 1.000) + "\nThe Rate you entered for selection "
                         + "two, " + table.getValueAt(1, 0) + ", is: " + rate2
                         + "   the answer is: " + (bt.getVel2() / .02)
                         + "\nThe MW you entered is: " + mw2 + " the answer is: "
                         + (bt.getMw2() * 1.000));
 
                 //focus on start/race box
                 bt.requestFocus();
             }
         });
         
         // listener to open the gate and start the race 
         goButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 bt.setGateOpen(true);
                 bt.requestFocus();
             }
         });
         
         //create a listener for the help button 
         helpButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 //make string title variable 
                 help = "Help Readme";
 
                 //make string variable for help content 
                 helpContent = "This program is designed to show you\n"
                         + "the concept of diffusion.\n\nInstructions: \n"
                         + "Select from the drop down boxes the two elements\n"
                         + "you want to see displayed in the simulation box.\n\n"
                         + "The colors and size of the particles in the box\n"
                         + "will change to represent the elements you've\n"
                         + "chosen.  \n\nAfter one particle of each element"
                         + " has crossed\nthe finish line the table will display"
                         + "the time in\nseconds for each element particle.\n\n"
                         + "Using the periodic table, available by clicking "
                         + "the\nPeriodic Table button, and the information in "
                         + "the table\nat the bottom of the screen you will be "
                         + "able to \ncalculate the rate of speed in meters per "
                         + "second\nand the molecular weight of each particle\n"
                         + "and type them into the table.\n\nYou can then "
                         + "check your answers by using the check\nanswer "
                         + "button. If the answers are correct you'll see "
                         + "\n'Correct' show if the answers are incorrect "
                         + "you'll\nhave the chance to change your answers and "
                         + "\nrecheck 2 more times.\n\nGood Luck!";
 
                 //make text field for content 
                 helpTextArea = new JTextArea(helpContent);
 
                 //set font for text area 
                 helpTextArea.setFont(new Font("Verdana", Font.BOLD, 12));
 
                 //set foreground color for label 
                 helpTextArea.setForeground(Color.white);
 
                 //set background for text area 
                 helpTextArea.setBackground(new Color(12, 66, 116));
 
                 //set size of text area 
                 helpTextArea.setSize(400, 550);
 
                 //set text area to word wrap 
                 helpTextArea.setWrapStyleWord(true);
 
                 //make empty border for padding 
                 Border emptyBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
 
                 //set border for text area 
                 helpTextArea.setBorder(emptyBorder);
                 
                 //set text area to not editable
                 helpTextArea.setEditable(false);
 
                 //make joptionpane object 
                 helpDialog = new JDialog(helpDialog, help);
 
                 //set layout for dialog 
                 helpDialog.setLayout(new BorderLayout());
 
                 //add textfield to helpDialog 
                 helpDialog.add(helpTextArea, BorderLayout.CENTER);
 
                 //set dialog to resizable 
                 helpDialog.setResizable(false);
 
                 //get width and height of text area 
                 int helpWidth = helpTextArea.getWidth();
                 int helpHeight = helpTextArea.getHeight();
 
                 //set dialog size 
                 helpDialog.setSize(helpWidth, helpHeight);
 
                 //make dialog visible 
                 helpDialog.setVisible(true);
             }
         });
         
         //listener to start mix using particleFill method from GasChamber  
         knownComboBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(!Element.isGateOpen()){
                 //set box1 to get the selected index 
                 box1 = knownComboBox.getSelectedIndex();
                 bt.particleFill(box1, box2);
 
                 //declares choice1 var. and sets value to selected item 
                 String choice1 = knownComboBox.getSelectedItem().toString();
 
                 //sets value at 0,0 in table to choice1 
                 table.setValueAt(choice1, 0, 0);
                 
                 }
                 bt.requestFocus();
             }
         });
         
         //create a listener for the periodic button 
         periodButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //make icon object and reference the periodic table image 
                 final Icon icon = new javax.swing.ImageIcon(getClass().getResource("periodictablesmall.png"));
 
                 //make string title variable 
                 periodic = "Periodic Table";
 
                 //make width and height the icon's width and height 
                 width = icon.getIconWidth();
                 height = icon.getIconHeight();
 
                 //make jpanel for periodic table image 
                 periodicPanel = new JPanel();
 
                 //set size of periodicPanel to match image 
                 periodicPanel.setSize(width, height);
 
                 //make image label 
                 imageLabel = new JLabel(icon);
 
                 //make jdialog with title 
                 dialog = new JDialog(dialog, periodic);
 
                 //set size of dialog to match image 
                 dialog.setSize(width, height);
 
                 //add imagelabel to dialog 
                 dialog.add(imageLabel);
 
                 //set dialog to visible 
                 dialog.setVisible(true);
             }
         });
         
         // listener for reset button 
         resetButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 bt.setGateOpen(false);
                 knownComboBox.setSelectedIndex(0);
                 unknownComboBox.setSelectedIndex(0);
                 clearTable(table);
                 Element.setIsWinner(false);
                 Element.setIsSecondWinner(false);
                 slider.setValue(85);
                 bt.setFRate(85);
                 String choice1 = knownComboBox.getSelectedItem().toString();
                 String choice2 = unknownComboBox.getSelectedItem().toString();
                 time1 = GasChamber.getTime1();
                 time2 = GasChamber.getTime2();
                 table.setValueAt(choice1, 0, 0);
                 table.setValueAt(choice2, 1, 0);
                 correctLabel.setVisible(false);
                 correctLabel2.setVisible(false);
                 incorrectLabel.setVisible(false);
                 incorrectLabel2.setVisible(false);
                 bt.requestFocus();
             }
         });
         
         // listener to update frameRate in GasChamber 
         slider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 int value = slider.getValue();
                 bt.setFRate(value);
                 bt.requestFocus();
             }
         });
         
         // listener to start mix using particleFill method from GasChamber 
         unknownComboBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(!Element.isGateOpen()){
                 //set box2 to get the selected index
                 box2 = unknownComboBox.getSelectedIndex();
                 bt.particleFill(box1, box2);
 
                 //declares choice2 var. and sets value to selected item
                 String choice2 = unknownComboBox.getSelectedItem().toString();
                 //sets value at 1,0 in table to choice2
                 table.setValueAt(choice2, 1, 0);
                 
                 }
                 bt.requestFocus();
             }
         });
     }
 
     /**
      * clears table of user input data
      */
     public static void clearTable(final JTable table) {
         for (int i = 0; i < table.getRowCount(); i++) {
             for (int j = 0; j < table.getColumnCount(); j++) {
                 table.setValueAt("", i, j);
             }
         }
     }
     
     public void setTableTime1(int id){
         if(id == 0){
             // pulls value of time1 from GasChamber  
             time1 = GasChamber.getTime1();
             // sets value at 0,1 in table to time1  
             table.setValueAt(format.format(time1), 0, 1);
         }
         else{
             // pulls value of time2 from GasChamber   
             time2 = GasChamber.getTime2();
             // sets value at 1,1 in table to time2 
             table.setValueAt(format.format(time2), 1, 1);
         }
     }
 }
