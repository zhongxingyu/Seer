 //
 // Created       : 2006 Jun 14 (Wed) 18:29:38 by Harold Carr.
// Last Modified : 2008 May 24 (Sat) 15:26:29 by Harold Carr.
 //
 
 package client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Component;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 public class QueryPanel
 {
     private final JPanel      queryPanel;
     private final ButtonGroup buttonGroup;
 
     // These should be final
     private       JRadioButton baseRadioButton;
     private       JTextField   baseSubjectTextField;
     private       JTextField   baseProperyTextField;
     private       JTextField   baseValueTextField;
 
     private       JTextField  selectedSubjectTextField;
     private       JTextField  selectedPropertyTextField;
     private       JTextField  selectedValueTextField;
 
     private       int         triplePanelID = 0;
 
     QueryPanel()
     {
 	buttonGroup = new ButtonGroup();
 	queryPanel = new JPanel();
 	queryPanel.setLayout(new BoxLayout(queryPanel, 
 					      BoxLayout.PAGE_AXIS));
 	addToVerticalPanel(makeTriplePanel());
     }
 
     private void addToVerticalPanel(JPanel triplePanel)
     {
	Main.getSwingView().addTriplePanel(queryPanel, triplePanel);
     }
 
     public void removeFromVerticalPanel(JPanel triplePanel)
     {
 	Component[] components = queryPanel.getComponents();
 	for (int i = 0; i < components.length; i++) {
 	    if (triplePanel == components[i]) {
 		queryPanel.remove(components[i]);
 		break;
 	    }
 	}
 	addToVerticalPanel(null);
     }
 
     JTextField getSubjectTextField()  { return selectedSubjectTextField; }
     JTextField getPropertyTextField() { return selectedPropertyTextField; }
     JTextField getValueTextField()    { return selectedValueTextField; }
     JPanel     getPanel()             { return queryPanel; }
 
     //////////////////////////////////////////////////
 
     private JPanel makeTriplePanel()
     {
 	final JPanel       triplePanel   = new JPanel();
 	final JButton      leftButton;
 	final JRadioButton radioButton       = new JRadioButton();
 	final JMenuBar     subjectJMenuBar;
 	final JTextField   subjectTextField  = new JTextField();
 	final JMenuBar     propertyJMenuBar;
 	final JTextField   propertyTextField = new JTextField();
 	final JMenuBar     valueJMenuBar;
 	final JTextField   valueTextField    = new JTextField();
 
 	triplePanel.setName("triplePanel-" + ++triplePanelID);
 
 	buttonGroup.add(radioButton); // Only one can be selected.
 
 	// The latest created is always selected.
 	radioButton.setEnabled(true);
 	radioButton.setSelected(true);
 	selectedSubjectTextField  = subjectTextField;
 	selectedPropertyTextField = propertyTextField;
 	selectedValueTextField    = valueTextField;
 
 	if (queryPanel.getComponentCount() == 0) {
 	    leftButton = new JButton("+");
 	    leftButton.setText("+");
 	    leftButton.addMouseListener(new MouseAdapter() {
 		public void mouseClicked(MouseEvent e) {
 		    addToVerticalPanel(makeTriplePanel());
 		    //window.pack();
 		}});
 	    baseRadioButton      = radioButton;
 	    baseSubjectTextField = subjectTextField;
 	    baseProperyTextField = propertyTextField;
 	    baseValueTextField   = valueTextField;
 	} else {
 	    leftButton = new JButton("-");
 	    leftButton.setText("-");
 	    leftButton.addMouseListener(new MouseAdapter() {
 		public void mouseClicked(MouseEvent e) {
 		    removeFromVerticalPanel(triplePanel);
 		    buttonGroup.remove(radioButton);
 		    baseRadioButton.setSelected(true);
 		    selectedSubjectTextField  = baseSubjectTextField;
 		    selectedPropertyTextField = baseProperyTextField;
 		    selectedValueTextField    = baseValueTextField;
 		}});
 	}
 
 	radioButton.addMouseListener(new MouseAdapter() {
 	    public void mouseClicked(MouseEvent e) {
 		selectedSubjectTextField  = subjectTextField;
 		selectedPropertyTextField = propertyTextField;
 		selectedValueTextField    = valueTextField;
 	    }});
 		
 	subjectTextField.setText(Main.qsubject);
 	propertyTextField.setText(Main.qproperty);
 	valueTextField.setText(Main.qvalue);
 	subjectJMenuBar  = 
 	    makeJMenuBar(Main.qvalue,    valueTextField,
 			 Main.qsubject,  subjectTextField,
 			 Main.qproperty, propertyTextField);
 	propertyJMenuBar =
 	    makeJMenuBar(Main.qsubject,  subjectTextField,
 			 Main.qproperty, propertyTextField,
 			 Main.qvalue,    valueTextField);
 	valueJMenuBar    =
 	    makeJMenuBar(Main.qproperty, propertyTextField,
 			 Main.qvalue,    valueTextField,
 			 Main.qsubject,  subjectTextField);
 
 	Main.getSwingView().queryPanelLayout(
             triplePanel,
 	    leftButton, radioButton,
 	    subjectJMenuBar, subjectTextField,
 	    propertyJMenuBar, propertyTextField,
 	    valueJMenuBar, valueTextField);
 
 	return triplePanel;
     }
 
     private JMenuBar makeJMenuBar(final String     leftText,
 				  final JTextField leftTextField,
 				  final String     thisText,
 				  final JTextField thisTextField,
 				  final String     rightText,
 				  final JTextField rightTextField)
     {
 	final ActionListener clearCommand = new ActionListener() {
             public void actionPerformed(ActionEvent event) {
 		thisTextField.setText(thisText);
 		Main.getMainPanel().doQuery(true);
                 }
             };
 
 	final ActionListener showAllCommand = new ActionListener() {
             public void actionPerformed(ActionEvent event) {
 		Main.getMainPanel()
 		    .doQuery(true,
 			     Main.qsubject, Main.qproperty,
 			     Main.qvalue, thisText);
 	    }
 	};
 
 	final ActionListener moveLeftCommand = new ActionListener() {
             public void actionPerformed(ActionEvent event) {
 		final String text = thisTextField.getText();
 		thisTextField.setText(thisText);
 		leftTextField.setText(text);
 		Main.getMainPanel().doQuery(true);
 	    }
 	};
 
 	final ActionListener moveRightCommand = new ActionListener() {
             public void actionPerformed(ActionEvent event) {
 		final String text = thisTextField.getText();
 		thisTextField.setText(thisText);
 		rightTextField.setText(text);
 		Main.getMainPanel().doQuery(true);
 	    }
 	};
 
 	final JMenuBar menuBar = new JMenuBar();
 	final JMenu menu = new JMenu();
 	menu.setText("v");
 	menuBar.add(menu);
 
 	JMenuItem menuItem;
 
 	menuItem = new JMenuItem(Main.clear);
 	menuItem.addActionListener(clearCommand);
 	menu.add(menuItem);
 
 	menuItem = new JMenuItem(Main.showAll);
 	menuItem.addActionListener(showAllCommand);
 	menu.add(menuItem);
 
 	menuItem = new JMenuItem(Main.shiftRight);
 	menuItem.addActionListener(moveRightCommand);
 	menu.add(menuItem);
 
 	menuItem = new JMenuItem(Main.shiftLeft);
 	menuItem.addActionListener(moveLeftCommand);
 	menu.add(menuItem);
 
 	return menuBar;
     }
 }
 
 // End of file.
