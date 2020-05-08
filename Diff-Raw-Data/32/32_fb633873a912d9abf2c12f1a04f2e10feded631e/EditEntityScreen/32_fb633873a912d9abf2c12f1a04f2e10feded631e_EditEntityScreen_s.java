 
 /**
  * EditEntityScreen
  * 
  * Class representing the screen that allows users to edit properties of
  * grid entities, including triggers and appearance.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui.screen;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.gui.BoxLayoutAxis;
 import edu.wheaton.simulator.gui.FileMenu;
 import edu.wheaton.simulator.gui.Gui;
 import edu.wheaton.simulator.gui.HorizontalAlignment;
 import edu.wheaton.simulator.gui.IconGridPanel;
 import edu.wheaton.simulator.gui.MaxSize;
 import edu.wheaton.simulator.gui.PrefSize;
 import edu.wheaton.simulator.gui.ScreenManager;
 import edu.wheaton.simulator.gui.SimulatorFacade;
 import edu.wheaton.simulator.simulation.Simulator;
 
 public class EditEntityScreen extends Screen {
 
 	private static final long serialVersionUID = 4021299442173260142L;
 
 	private Boolean editing;
 
 	private Prototype prototype;
 
 	private JPanel cards;
 
 	private String currentCard;
 
 	private JPanel generalPanel;
 
 	private JTextField nameField;
 
 	private JColorChooser colorTool;
 
 	private ArrayList<JTextField> fieldNames;
 
 	private ArrayList<JTextField> fieldValues;
 
 	private ArrayList<JButton> fieldDeleteButtons;
 
 	private ArrayList<JPanel> fieldSubPanels;
 
 	private JButton addFieldButton;
 
 	private boolean[][] buttons;
 
 	private JPanel fieldListPanel;
 
 	private HashSet<Integer> removedFields;
 
 	private JButton nextButton;
 
 	private JButton previousButton;
 
 	private JButton finishButton;
 
 	private GridBagConstraints c;
 	
 	private TriggerScreen triggerScreen;
 
 	public EditEntityScreen(final SimulatorFacade gm) {
 		super(gm);
 		this.setLayout(new BorderLayout());
 		editing = false;
 		removedFields = new HashSet<Integer>();
 
 		nameField = new JTextField(25);
 
 		colorTool = Gui.makeColorChooser();
 
 		fieldNames = new ArrayList<JTextField>();
 		fieldValues = new ArrayList<JTextField>();
 
 		fieldSubPanels = new ArrayList<JPanel>();
 
 		fieldDeleteButtons = new ArrayList<JButton>();
 
 		buttons = new boolean[7][7];
 
 		currentCard = "General";
 
 		fieldListPanel = Gui.makePanel(BoxLayoutAxis.Y_AXIS, MaxSize.NULL,
 				PrefSize.NULL);
 
 		cards = new JPanel(new CardLayout());
 
 		generalPanel = new JPanel(new GridBagLayout());
 
 		addFieldButton = Gui.makeButton("Add Field",null,
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addField();
 			}
 		});
 
 		final IconGridPanel iconPanel = new IconGridPanel(gm);
 		initIconDesignObject(iconPanel);
 
 		JPanel colorPanel = Gui.makeColorChooserPanel(colorTool);
 
 		colorTool.getSelectionModel().addChangeListener( new ChangeListener(){
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				iconPanel.repaint();
 			}
 
 		});
 		
 		JLabel agentName = Gui.makeLabel("Agent Name: ",PrefSize.NULL, HorizontalAlignment.RIGHT);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = GridBagConstraints.PAGE_START;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 4;
 		generalPanel.add(
 				Gui.makeLabel("General Info",
 						PrefSize.NULL,
 						HorizontalAlignment.CENTER),c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		generalPanel.add(agentName, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 2;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		generalPanel.add(nameField, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 1.0;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 2;
 		generalPanel.add(colorPanel, c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 1.0;
 		c.gridx = 2;
 		c.gridy = 2;
 		c.gridwidth = 2;
 		generalPanel.add(iconPanel, c);
 

 		addField();

		// TODO make sure components line up

 		fieldSubPanels.get(0).setLayout(
 				new BoxLayout(fieldSubPanels.get(0), BoxLayout.X_AXIS));
 		fieldSubPanels.get(0).add(fieldNames.get(0));
 		fieldSubPanels.get(0).add(fieldValues.get(0));
 		fieldSubPanels.get(0).add(fieldDeleteButtons.get(0));
 
 		fieldListPanel.add(fieldSubPanels.get(0));
 		fieldListPanel.add(addFieldButton);
 		fieldListPanel.add(Box.createVerticalGlue());
 
 		cards.add(generalPanel, "General");
 		cards.add(makeFieldMainPanel(fieldListPanel), "Fields");
 		triggerScreen = new TriggerScreen(gm);
 		cards.add(triggerScreen, "Triggers");
 
 		finishButton = Gui.makeButton("Finish",null,
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (sendInfo()) {
 					Gui.getScreenManager().load(Gui.getScreenManager().getScreen("View Simulation"));
 					gm.saveAgent((String)nameField.getText());
 					reset();
 				}
 			}
 		});
 
 		previousButton = Gui.makeButton("Previous", null,
 				new PreviousListener());
 		nextButton = Gui.makeButton("Next", null, new NextListener());
 
 		this.add(new JLabel("Edit Entities", SwingConstants.CENTER),
 				BorderLayout.NORTH);
 		this.add(cards, BorderLayout.CENTER);
 
 		this.add(Gui.makePanel(
 				previousButton,
 				Gui.makeButton("Cancel",null,
 						new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						ScreenManager sm = getScreenManager();
 						sm.load(sm.getScreen("View Simulation"));
 						FileMenu fm = Gui.getFileMenu();
 						fm.setSaveSim(true);
 						if (!editing)
 							Prototype.removePrototype(nameField.getText());
 						reset();
 					}
 				}), finishButton, nextButton), 
 				BorderLayout.SOUTH );
 	}
 
 	private void initIconDesignObject(IconGridPanel iconPanel){
 		//Creates the icon design object.
 		iconPanel.setIcon(buttons);
 	}
 
 	private static JPanel makeFieldMainPanel(JPanel fieldListPanel){
 		JPanel fieldMainPanel = Gui.makePanel(new GridBagLayout(),MaxSize.NULL,PrefSize.NULL);
 
 		GridBagConstraints constraint = new GridBagConstraints();
 		constraint.gridx = 0;
 		constraint.gridy = 0;
 		constraint.gridwidth = 2;
 		fieldMainPanel.add(
 				Gui.makeLabel("Field Info",PrefSize.NULL,HorizontalAlignment.CENTER), 
 				constraint);
 
 		constraint = new GridBagConstraints();
 		constraint.gridx = 0;
 		constraint.gridy = 1;
 		constraint.gridwidth = 1;
 
 		JLabel fieldNameLabel = Gui.makeLabel("Field Name", PrefSize.NULL, HorizontalAlignment.LEFT);
 		fieldNameLabel.setAlignmentX(LEFT_ALIGNMENT);
 		fieldMainPanel.add(fieldNameLabel, constraint);
 
 		constraint.gridx = 1;
 		JLabel fieldValueLabel = Gui.makeLabel("Field Initial Value",
 				PrefSize.NULL, HorizontalAlignment.LEFT);
 		fieldValueLabel.setAlignmentX(LEFT_ALIGNMENT);
 		fieldMainPanel.add(fieldValueLabel, constraint);
 
 		constraint = new GridBagConstraints();
 		constraint.gridx = 0;
 		constraint.gridy = 2;
 		constraint.gridwidth = 3;
 		constraint.weighty = 1.0;
 		constraint.anchor = GridBagConstraints.PAGE_START;
 		fieldMainPanel.add(fieldListPanel, constraint);
 
 		return fieldMainPanel;
 	}
 
 	public void load(String str) {
 		reset();
 		prototype = gm.getPrototype(str);
 		nameField.setText(prototype.getName());
 		colorTool.setColor(prototype.getColor());
 
 		byte[] designBytes = prototype.getDesign();
 		byte byter = Byte.parseByte("0000001", 2);
 
 		for (int i = 0; i < 7; i++) 
 			for (int j = 0; j < 7; j++)
 				if ((designBytes[i] & (byter << j)) != Byte.parseByte("0000000", 2))
 					buttons[i][6-j] = true;
 
 		Map<String, String> fields = prototype.getCustomFieldMap();
 		int i = 0;
 		for (String s : fields.keySet()) {
 			addField();
 			fieldNames.get(i).setText(s);
 			fieldValues.get(i).setText(fields.get(s));
 			i++;
 		}
 	}
 
 	public void reset() {
 		prototype = null;
 		currentCard = "General";
 		((CardLayout) cards.getLayout()).first(cards);
 		nameField.setText("");
 		colorTool.setColor(Color.WHITE);
 		for (int x = 0; x < 7; x++) {
 			for (int y = 0; y < 7; y++) {
 				buttons[x][y] = false;
 			}
 		}
 		fieldNames.clear();
 		fieldValues.clear();
 		fieldDeleteButtons.clear();
 		fieldSubPanels.clear();
 		removedFields.clear();
 		fieldListPanel.removeAll();
 		fieldListPanel.add(addFieldButton);
 		triggerScreen.reset();
 		previousButton.setEnabled(false);
		//previousButton.setVisible(false);
 		nextButton.setEnabled(true);
		//nextButton.setVisible(true);
		finishButton.setEnabled(false);
		//finishButton.setVisible(false);
 		
 		FileMenu fm = Gui.getFileMenu();
 		fm.setSaveSim(false);
 	}
 
 	public boolean sendInfo() {
 		sendGeneralInfo();
 		prototype = triggerScreen.sendInfo();
 		return sendFieldInfo();
 	}
 
 	public boolean sendGeneralInfo() {
 		boolean toReturn = false;
 		try {
 			if (nameField.getText().equals("")) {
 				throw new Exception("Please enter an Agent name");
 			}
 			if (!editing) {
 				gm.createPrototype(nameField.getText(), colorTool.getColor(), generateBytes());
 				prototype = gm.getPrototype(nameField.getText());
 			} else {
 				prototype.setPrototypeName(prototype.getName(), nameField.getText());
 				prototype.setColor(colorTool.getColor());
 				prototype.setDesign(generateBytes());
 				Simulator.addPrototype(prototype);
 			}
 			toReturn = true;
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, e.getMessage());
 		}
 		return toReturn;
 	}
 
 	public boolean sendFieldInfo() {
 		boolean toReturn = false;
 		try {
 			for (int i = 0; i < fieldNames.size(); i++)
 				if (!removedFields.contains(i)
 						&& (fieldNames.get(i).getText().equals("")
 								|| fieldValues.get(i).getText().equals("")))
 					throw new Exception("All fields must have input");
 
 			for (int i = 0; i < fieldNames.size(); i++) {
 				if (removedFields.contains(i)
 						&& (prototype.hasField(fieldNames.get(i).getText())))
					prototype.removeField(fieldNames.get(i).toString());
 				else {
 					if (prototype.hasField(fieldNames.get(i).getText()))
 						prototype.updateField(fieldNames.get(i).getText(),
 								fieldValues.get(i).getText());
 					else {
 						try {
 							if (!removedFields.contains(i))
 								prototype.addField(fieldNames.get(i).getText(),
 										fieldValues.get(i).getText());
 						} catch (ElementAlreadyContainedException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			toReturn = true;
 		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
 		}
 		return toReturn;
 	}
 
 	public void setEditing(Boolean b) {
 		editing = b;
 	}
 
 	public Color getColor() {
 		return colorTool.getColor();
 	}
 
 	private void addField() {
 
 
 		JTextField newName = Gui.makeTextField(null,25,MaxSize.NULL,null);
 		fieldNames.add(newName);
 
 		JTextField newValue = Gui.makeTextField(null,25,MaxSize.NULL,null);
 		fieldValues.add(newValue);
 
 		JButton newButton = Gui.makeButton("Delete",null,
 				new DeleteFieldListener());
 		fieldDeleteButtons.add(newButton);
 		newButton.setActionCommand(fieldDeleteButtons.indexOf(newButton) + "");
 
 
 		JPanel newPanel = Gui.makePanel(BoxLayoutAxis.X_AXIS, null, null);
 		newPanel.add(newName);
 		newPanel.add(newValue);
 		newPanel.add(newButton);
 
 		fieldSubPanels.add(newPanel);
 		fieldListPanel.add(newPanel);
 		fieldListPanel.add(addFieldButton);
 		fieldListPanel.add(Box.createVerticalGlue());
 		validate();
 		repaint();
 	}
 
 	private byte[] generateBytes() {
 		String str = "";
 		byte[] toReturn = new byte[7];
 		for (int column = 0; column < 7; column++) {
 			for (int row = 0; row < 7; row++) {
 				if (buttons[column][row] == true)
 					str += "1";
 				else
 					str += "0";
 			}
 			str += ":";
 		}
 
 		String[] byteStr = str.substring(0, str.lastIndexOf(':')).split(":");
 		for (int i = 0; i < 7; i++)
 			toReturn[i] = Byte.parseByte(byteStr[i], 2);
 
 		return toReturn;
 	}
 
 	private class DeleteFieldListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			removedFields.add(Integer.parseInt(e.getActionCommand()));
 			fieldListPanel.remove(fieldSubPanels.get(Integer.parseInt(e
 					.getActionCommand())));
 			validate();
 		}
 	}
 
 	private class NextListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			CardLayout c1 = (CardLayout) cards.getLayout();
 			if (currentCard == "General") {
 				if (sendGeneralInfo()) {
 					previousButton.setEnabled(true);
 					//previousButton.setVisible(true);
 					c1.next(cards);
 					currentCard = "Fields";
 				}
 			} else if (currentCard == "Fields") {
 				if (sendFieldInfo()) {
 					triggerScreen.load(prototype);
 					c1.next(cards);
 					nextButton.setEnabled(false);
 					//nextButton.setVisible(false);
 					finishButton.setEnabled(true);
 					//finishButton.setVisible(true);
 					currentCard = "Triggers";
 				}
 			}
 			validate();
 		}
 	}
 
 	private class PreviousListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			CardLayout c1 = (CardLayout) cards.getLayout();
 			if (currentCard == "Fields") {
 				if (sendFieldInfo()) {
 					previousButton.setEnabled(false);
 					//previousButton.setVisible(false);
 					c1.previous(cards);
 					currentCard = "General";
 				}
 			} else if (currentCard == "Triggers") {
 					prototype = triggerScreen.sendInfo();
 					c1.previous(cards);
 					nextButton.setEnabled(true);
 					//nextButton.setVisible(true);
 					currentCard = "Fields";
 				}
 			validate();
 		}
 	}
 
 	@Override
 	public void load() {
 		reset();
 		addField();
 	}
 
 }
