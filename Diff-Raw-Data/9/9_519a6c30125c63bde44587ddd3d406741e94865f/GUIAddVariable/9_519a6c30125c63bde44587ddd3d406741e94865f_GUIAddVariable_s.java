 package gui;
import internalformatting.Unit;
 import internalformatting.Variable;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 
 import storage.Saver;
import storage.UnitDatabase;
 import storage.VariableDatabase;
 
 /**
  * Another GUI to learn a bit about gui making,
  * and to make a slightly different way to format
  * what we want in our GUI
  * 
  * @author May Camp
  * @author Michelle Len
  * @author Jonathan Tan
  */
 public class GUIAddVariable extends JPanel {
 
 	private static final long serialVersionUID = 6187284211221392126L;
 
 	private NewtonsToolboxPanel middlePanel;
 
 	private NewtonsToolboxPanel namePanel;
 	private NewtonsToolboxPanel unitPanel;
 	private NewtonsToolboxPanel infoPanel;
 	private NewtonsToolboxPanel tagPanel;
 	private NewtonsToolboxPanel addVariableButtonPanel;
 
 	private JLabel nameLabel   = new JLabel("Name: ");
 	private JLabel unitLabel   = new JLabel("Units: ");
 	private JLabel infoLabel   = new JLabel("Info: ");
 	private JLabel tagLabel    = new JLabel("Tags: ");
 
 	private JTextField nameTextField   = new JTextField(57);
 	private JTextField unitTextField   = new JTextField(57); // Change to drop down later
 	private JTextArea infoTextArea     = new JTextArea(17, 57);
 	private JTextArea tagTextArea      = new JTextArea(4, 57);
 
 	private JScrollPane infoScrollbar;
 	private JScrollPane tagScrollbar;
 
 	private JButton createUnitButton = new JButton("Create Unit");
 	private JButton addVariableButton = new JButton("Add Variable");
 
	private JComboBox unitComboBox;
 	
 	public GUIAddVariable() {
 
 		//////////////NOTE TO SELF
 		//need to add a scrollpanel to the units drop down units thing (JComboBox)
 		//need to make action listener for button at bottom to save variables
 		//need to delete unneeded setOpaque thing
 		//need to reorganize
 		//might want to change color of units JComboBox, as it is a gray-ish color
 
 		setSize(720,480);
 
		unitComboBox = new JComboBox();
 		for(int i=0;i<GUIMain.UNITS.getSize();i++){
 			unitComboBox.addItem(GUIMain.UNITS.get(i).getName());
 		}
 
 		middlePanel = new NewtonsToolboxPanel();
 		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
 		middlePanel.setSize(720, 480);
 		add(BorderLayout.NORTH, middlePanel);
 
 		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
 
 		unitLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		unitLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
 
 		infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		infoLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
 
 		tagLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		tagLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
 
 		nameTextField.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		nameTextField.setAlignmentY(Component.CENTER_ALIGNMENT);
 
 		unitComboBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		unitComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);
 		
 		createUnitButton.setAlignmentX(RIGHT_ALIGNMENT);
 		createUnitButton.setAlignmentY(CENTER_ALIGNMENT);
 
 		infoTextArea.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		infoTextArea.setAlignmentY(Component.CENTER_ALIGNMENT);
 
 		tagTextArea.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		tagTextArea.setAlignmentY(Component.CENTER_ALIGNMENT);
 
 		addVariableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 		addVariableButton.setAlignmentY(Component.CENTER_ALIGNMENT);
 
 		// Scroll-bar for the Info JTextArea
 		infoScrollbar = new JScrollPane(infoTextArea);
 		infoScrollbar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		infoScrollbar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
 		// Scroll-bar for the Info JTextArea
 		tagScrollbar = new JScrollPane(tagTextArea);
 		tagScrollbar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		tagScrollbar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
 		namePanel   = new NewtonsToolboxPanel();
 		unitPanel   = new NewtonsToolboxPanel();
 		infoPanel   = new NewtonsToolboxPanel();
 		tagPanel    = new NewtonsToolboxPanel();
 
 		addVariableButtonPanel = new NewtonsToolboxPanel();
 
 		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
 		unitPanel.setLayout(new BoxLayout(unitPanel, BoxLayout.X_AXIS));
 		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
 		tagPanel.setLayout (new BoxLayout(tagPanel,  BoxLayout.X_AXIS));
 
 		addVariableButtonPanel.setLayout(new BoxLayout(addVariableButtonPanel, BoxLayout.X_AXIS));
 
 		//wrap words and lines and make sure you can't edit it
 		infoTextArea.setLineWrap(true);
 		infoTextArea.setWrapStyleWord(true);
 		//wrap words and lines and make sure you can't edit it
 		tagTextArea.setLineWrap(true);
 		tagTextArea.setWrapStyleWord(true);
 
 		namePanel.add(nameLabel);
 		namePanel.add(Box.createRigidArea(new Dimension(12,0)));
 		namePanel.add(nameTextField);
 
 		unitPanel.add(unitLabel);
 		unitPanel.add(Box.createRigidArea(new Dimension(16,0)));
 		unitPanel.add(unitComboBox);
 		unitPanel.add(createUnitButton);
 
 		infoPanel.add(infoLabel);
 		infoPanel.add(Box.createRigidArea(new Dimension(24,0)));
 		infoPanel.add(infoScrollbar);
 
 		tagPanel.add(tagLabel);
 		tagPanel.add(Box.createRigidArea(new Dimension(17,0)));
 		tagPanel.add(tagScrollbar);
 
 		addVariableButtonPanel.add(addVariableButton);
 
 		middlePanel.add(namePanel);
 		middlePanel.add(unitPanel);
 		middlePanel.add(infoPanel);
 		middlePanel.add(tagPanel);
 		middlePanel.add(addVariableButtonPanel);
 
 		setOpaque(false);
 		middlePanel.setOpaque(false);//THIS THING makes it not opaque
 
 		namePanel.setOpaque(false);
 		unitPanel.setOpaque(false);
 		infoPanel.setOpaque(false);
 		tagPanel.setOpaque(false);
 
 		addVariableButton.addActionListener(new addVariableButtonListener());
 		
 		addVariableButtonPanel.setOpaque(false);
 
 	}
 	public class NewtonsToolboxPanel extends JPanel {
 		// Included to suppress Eclipse Warning
 		private static final long serialVersionUID = 2571010965858865585L;
 	}
 
 	//Button Listener Classes:
 	class addVariableButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			String variableName = nameTextField.getText();
 			String variableInfo = infoTextArea.getText();
 			//String variableUnit = unitTextField.getText();
 			String[] tagsTemp = tagTextArea.getText().split(",");
 
 			Variable newVariable = new Variable(variableName);
 			newVariable.setInfo(variableInfo);
 			newVariable.setUnit(GUIMain.UNITS.get(unitComboBox.getSelectedIndex()));
 			for(int i=0;i<tagsTemp.length;i++){
 				newVariable.addTag(tagsTemp[i].toLowerCase());
 			}
 			((VariableDatabase)GUIMain.VARIABLES).addVariable(newVariable);
 			Saver.saveVars(GUIMain.VARIABLES);
 
 			nameTextField.setText("");
 			unitTextField.setText("");
 			infoTextArea.setText("");
 			tagTextArea.setText("");
 		}
 	}
 	
 	class createUnitButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			middlePanel.removeAll();
 			GUIMain.updateUI();
 			middlePanel.add(new GUIAddUnit());
 		}
 	}
 
 }
