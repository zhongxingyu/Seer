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
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.gui.BoxLayoutAxis;
 import edu.wheaton.simulator.gui.Gui;
 import edu.wheaton.simulator.gui.HorizontalAlignment;
 import edu.wheaton.simulator.gui.IconGridPanel;
 import edu.wheaton.simulator.gui.MaxSize;
 import edu.wheaton.simulator.gui.PrefSize;
 import edu.wheaton.simulator.gui.ScreenManager;
 import edu.wheaton.simulator.gui.SimulatorFacade;
 
 public class EditEntityScreen extends Screen {
 
 	private static final long serialVersionUID = 4021299442173260142L;
 
 	private Boolean editing;
 
 	private Prototype agent;
 
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
 
 	private ArrayList<JTextField> triggerNames;
 
 	private ArrayList<JTextField> triggerPriorities;
 
 	private ArrayList<JTextField> triggerConditions;
 
 	private ArrayList<JTextField> triggerResults;
 
 	private ArrayList<JButton> triggerDeleteButtons;
 
 	private ArrayList<JPanel> triggerSubPanels;
 
 	private JButton addTriggerButton;
 
 	private JPanel triggerListPanel;
 
 	private HashSet<Integer> removedFields;
 
 	private HashSet<Integer> removedTriggers;
 
 	private JButton nextButton;
 
 	private JButton previousButton;
 
 	private JButton finishButton;
 
 	private GridBagConstraints c;
 
 	public EditEntityScreen(final SimulatorFacade gm) {
 		super(gm);
 		this.setLayout(new BorderLayout());
 		editing = false;
 		removedFields = new HashSet<Integer>();
 		removedTriggers = new HashSet<Integer>();
 
 		nameField = new JTextField(25);
 		//nameField.setPreferredSize(new Dimension(400, 40));
 		//nameField.setMinimumSize(new Dimension(200, 40));
 
 		colorTool = Gui.makeColorChooser();
 
 		fieldNames = new ArrayList<JTextField>();
 		fieldValues = new ArrayList<JTextField>();
 
 		fieldSubPanels = new ArrayList<JPanel>();
 		triggerSubPanels = new ArrayList<JPanel>();
 
 		triggerNames = new ArrayList<JTextField>();
 		triggerPriorities = new ArrayList<JTextField>();
 		triggerConditions = new ArrayList<JTextField>();
 		triggerResults = new ArrayList<JTextField>();
 
 		fieldDeleteButtons = new ArrayList<JButton>();
 		triggerDeleteButtons = new ArrayList<JButton>();
 
 		buttons = new boolean[7][7];
 
 		currentCard = "General";
 
 		fieldListPanel = Gui.makePanel(BoxLayoutAxis.Y_AXIS, MaxSize.NULL,
 				PrefSize.NULL);
 
 		triggerListPanel = Gui.makePanel(BoxLayoutAxis.Y_AXIS, MaxSize.NULL,
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
 
 		addTriggerButton = Gui.makeButton("Add Trigger",null,
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addTrigger();
 			}
 		});
 
 		final IconGridPanel iconPanel = new IconGridPanel(gm);
 		iconPanel.setPreferredSize(new Dimension(500, 500));
 		//iconPanel.setAlignmentX(RIGHT_ALIGNMENT);
 		initIconDesignObject(iconPanel);
 
 		JPanel colorPanel = Gui.makeColorChooserPanel(colorTool);
 		Dimension maxSize = colorPanel.getMaximumSize();
 		maxSize.height += 50;
 		colorPanel.setMaximumSize(maxSize);
 
 		colorTool.getSelectionModel().addChangeListener( new ChangeListener(){
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				iconPanel.repaint();
 			}
 
 		});
 
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = GridBagConstraints.PAGE_START;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 4;
 		generalPanel.add(
 				Gui.makeLabel("General Info",
 						new PrefSize(300,80),
 						HorizontalAlignment.CENTER),c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		generalPanel.add(
 				new JLabel("Agent Name: ",SwingConstants.RIGHT),c);
 
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 2;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.ipadx = 250;
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
 		c.ipadx = 500;
 		c.ipady = 500;
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
 
 		addTrigger();
 
 		triggerSubPanels.get(0).setLayout(
 				new BoxLayout(triggerSubPanels.get(0), BoxLayout.X_AXIS));
 		triggerSubPanels.get(0).add(triggerNames.get(0));
 		triggerSubPanels.get(0).add(triggerPriorities.get(0));
 		triggerSubPanels.get(0).add(triggerConditions.get(0));
 		triggerSubPanels.get(0).add(triggerResults.get(0));
 		triggerSubPanels.get(0).add(triggerDeleteButtons.get(0));
 		triggerSubPanels.get(0).setAlignmentX(CENTER_ALIGNMENT);
 
 		triggerListPanel.add(triggerSubPanels.get(0));
 		triggerListPanel.add(addTriggerButton);
 		triggerListPanel.add(Box.createVerticalGlue());
 		triggerListPanel.setPreferredSize(new Dimension(750, 350));
 
 		cards.add(generalPanel, "General");
 		cards.add(makeFieldMainPanel(fieldListPanel), "Fields");
 		cards.add(makeTriggerMainPanel(triggerListPanel), "Triggers");
 
 		finishButton = Gui.makeButton("Finish",null,
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (sendInfo()) {
 					Screen update = Gui.getScreenManager().getScreen("View Simulation");
 					update.load();
 					Gui.getScreenManager().update(update);
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
 						sm.update(sm.getScreen("View Simulation"));
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
 
 	private static JPanel makeTriggerMainPanel(JPanel triggerListPanel) {
 		JPanel triggerLabelsPanel = Gui.makePanel(BoxLayoutAxis.X_AXIS,
 				MaxSize.NULL, PrefSize.NULL);
 		triggerLabelsPanel.add(Box.createHorizontalGlue());
 		triggerLabelsPanel.add(Gui.makeLabel("Trigger Name", new PrefSize(130,
 				30), HorizontalAlignment.LEFT));
 		triggerLabelsPanel.add(Gui.makeLabel("Trigger Priority", new PrefSize(
 				180, 30), null));
 		triggerLabelsPanel.add(Gui.makeLabel("Trigger Condition",
 				new PrefSize(300, 30), null));
 		triggerLabelsPanel.add(Gui.makeLabel("Trigger Result", new PrefSize(
 				300, 30), null));
 		triggerLabelsPanel.add(Box.createHorizontalGlue());
 		triggerLabelsPanel.setAlignmentX(CENTER_ALIGNMENT);
 
 		JPanel triggerBodyPanel = Gui.makePanel(BoxLayoutAxis.Y_AXIS,
 				MaxSize.NULL, PrefSize.NULL);
 		triggerBodyPanel.add(triggerLabelsPanel);
 		triggerBodyPanel.add(triggerListPanel);
 
 		JPanel triggerMainPanel = Gui.makePanel(new BorderLayout(),
 				MaxSize.NULL, PrefSize.NULL);
 		triggerMainPanel.add(Gui.makeLabel("Trigger Info", new PrefSize(300,
 				100), HorizontalAlignment.CENTER), BorderLayout.NORTH);
 		triggerMainPanel.add(triggerBodyPanel, BorderLayout.CENTER);
 		return triggerMainPanel;
 	}
 
 	private static JPanel makeFieldMainPanel(JPanel fieldListPanel){
 		JPanel fieldMainPanel = Gui.makePanel(new GridBagLayout(),MaxSize.NULL,PrefSize.NULL);
 
 		GridBagConstraints constraint = new GridBagConstraints();
 		constraint.gridx = 0;
 		constraint.gridy = 0;
 		constraint.gridwidth = 2;
 		fieldMainPanel.add(
 				Gui.makeLabel("Field Info",new PrefSize(300,100),HorizontalAlignment.CENTER), 
 				constraint);
 
 		constraint = new GridBagConstraints();
 		constraint.gridx = 0;
 		constraint.gridy = 1;
 		constraint.gridwidth = 1;
 
		JLabel fieldNameLabel = Gui.makeLabel("Field Name", new PrefSize(263,
 				30), HorizontalAlignment.LEFT);
 		fieldNameLabel.setAlignmentX(LEFT_ALIGNMENT);
 		fieldMainPanel.add(fieldNameLabel, constraint);
 
 		constraint.gridx = 1;
 		JLabel fieldValueLabel = Gui.makeLabel("Field Initial Value",
				new PrefSize(263, 30), HorizontalAlignment.LEFT);
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
 		agent = gm.getPrototype(str);
 		nameField.setText(agent.getName());
 		colorTool.setColor(agent.getColor());
 
 		byte[] designBytes = agent.getDesign();
 		byte byter = Byte.parseByte("0000001", 2);
 
 		for (int i = 0; i < 7; i++) 
 			for (int j = 0; j < 7; j++)
 				if ((designBytes[i] & (byter << j)) != Byte.parseByte("0000000", 2))
 					buttons[i][6-j] = true;
 
 		Map<String, String> fields = agent.getCustomFieldMap();
 		int i = 0;
 		for (String s : fields.keySet()) {
 			addField();
 			fieldNames.get(i).setText(s);
 			fieldValues.get(i).setText(fields.get(s));
 			i++;
 		}
 
 		List<Trigger> triggers = agent.getTriggers();
 		int j = 0;
 		for (Trigger t : triggers) {
 			addTrigger();
 			triggerNames.get(j).setText(t.getName());
 			triggerConditions.get(j).setText(t.getConditions().toString());
 			triggerResults.get(j).setText(t.getBehavior().toString());
 			triggerPriorities.get(j).setText(t.getPriority() + "");
 			j++;
 		}
 	}
 
 	public void reset() {
 		agent = null;
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
 		triggerNames.clear();
 		triggerPriorities.clear();
 		triggerConditions.clear();
 		triggerResults.clear();
 		triggerDeleteButtons.clear();
 		triggerSubPanels.clear();
 		removedTriggers.clear();
 		triggerListPanel.removeAll();
 		triggerListPanel.add(addTriggerButton);
 		previousButton.setEnabled(false);
 		//previousButton.setVisible(false);
 		nextButton.setEnabled(true);
 		//nextButton.setVisible(true);
 		finishButton.setEnabled(false);
 		//finishButton.setVisible(false);
 	}
 
 	public boolean sendInfo() {
 		sendGeneralInfo();
 
 		return (sendFieldInfo() && sendTriggerInfo());
 	}
 
 	public boolean sendGeneralInfo() {
 		boolean toReturn = false;
 		try {
 			if (nameField.getText().equals("")) {
 				throw new Exception("Please enter an Agent name");
 			}
 			if (!editing) {
 				//TODO signature of create prototype needs to not take a grid
 				gm.createPrototype(nameField.getText(), colorTool.getColor(), generateBytes());
 				agent = gm.getPrototype(nameField.getText());
 			} else {
 				agent.setPrototypeName(agent.getName(), nameField.getText());
 				agent.setColor(colorTool.getColor());
 				agent.setDesign(generateBytes());
 				Prototype.addPrototype(agent);
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
 						&& (agent.hasField(fieldNames.get(i).getText())))
 					agent.removeField(fieldNames.get(i).toString());
 				else {
 					if (agent.hasField(fieldNames.get(i).getText()))
 						agent.updateField(fieldNames.get(i).getText(),
 								fieldValues.get(i).getText());
 					else {
 						try {
 							if (!removedFields.contains(i))
 								agent.addField(fieldNames.get(i).getText(),
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
 
 	public boolean sendTriggerInfo() {
 		boolean toReturn = false;
 		try {
 			for (int j = 0; j < triggerNames.size(); j++) {
 				if (!removedTriggers.contains(j)
 						&& (triggerNames.get(j).getText().equals("")
 								|| triggerConditions.get(j).getText().equals("")
 								|| triggerResults.get(j).getText().equals("")))
 					throw new Exception("All fields must have input");
 
 				if (Integer.parseInt(triggerPriorities.get(j).getText()) < 0)
 					throw new Exception("Priority must be greater than 0");
 			}
 
 			for (int i = 0; i < triggerNames.size(); i++) {
 				if (removedTriggers.contains(i)
 						&& (agent.hasTrigger(triggerNames.get(i).getText())))
 					agent.removeTrigger(triggerNames.get(i).getText());
 				else {
 					if (agent.hasTrigger(triggerNames.get(i).getText()))
 						agent.updateTrigger(triggerNames.get(i).getText(),
 								generateTrigger(i));
 					else
 						agent.addTrigger(generateTrigger(i));
 				}
 			}
 			toReturn = true;
 		} catch (NumberFormatException e) {
 			JOptionPane.showMessageDialog(null,
 					"Priorities field must be an integer greater than 0.");
 			e.printStackTrace();
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
 
 
 		JTextField newName = Gui.makeTextField(null,25,new MaxSize(300,40),null);
 		fieldNames.add(newName);
 
 		JTextField newValue = Gui.makeTextField(null,25,new MaxSize(300,40),null);
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
 
 	private void addTrigger() {
 		JTextField newName = Gui.makeTextField(null, 25, new MaxSize(200, 40),
 				null);
 		triggerNames.add(newName);
 
 		JTextField newPriority = Gui.makeTextField(null,15,new MaxSize(150,40),null);
 		triggerPriorities.add(newPriority);
 
 		JTextField newCondition = Gui.makeTextField(null,50,new MaxSize(300,40),null);
 		triggerConditions.add(newCondition);
 
 		JTextField newResult = Gui.makeTextField(null,50,new MaxSize(300,40),null);
 		triggerResults.add(newResult);
 
 		JButton newButton = Gui.makeButton("Delete",null,
 				new DeleteTriggerListener());
 		newButton.setActionCommand(triggerDeleteButtons.indexOf(newButton)
 				+ "");
 		triggerDeleteButtons.add(newButton);
 
 		JPanel newPanel = Gui.makePanel(BoxLayoutAxis.X_AXIS,null,null);
 		newPanel.add(newName);
 		newPanel.add(newPriority);
 		newPanel.add(newCondition);
 		newPanel.add(newResult);
 		newPanel.add(newButton);
 
 		triggerSubPanels.add(newPanel);
 		triggerListPanel.add(newPanel);
 		triggerListPanel.add(addTriggerButton);
 		triggerListPanel.add(Box.createVerticalGlue());
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
 
 	private Trigger generateTrigger(int i) {
 		return new Trigger(triggerNames.get(i).getText(),
 				Integer.parseInt(triggerPriorities.get(i).getText()),
 				SimulatorFacade.makeExpression(triggerConditions.get(i).getText()),
 				SimulatorFacade.makeExpression(triggerResults.get(i).getText()));
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
 
 	private class DeleteTriggerListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			removedTriggers.add(Integer.parseInt(e.getActionCommand()));
 			triggerListPanel.remove(triggerSubPanels.get(Integer.parseInt(e
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
 				previousButton.setEnabled(false);
 				//previousButton.setVisible(false);
 				c1.previous(cards);
 				currentCard = "General";
 			} else if (currentCard == "Triggers") {
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
 		addTrigger();
 	}
 
 }
