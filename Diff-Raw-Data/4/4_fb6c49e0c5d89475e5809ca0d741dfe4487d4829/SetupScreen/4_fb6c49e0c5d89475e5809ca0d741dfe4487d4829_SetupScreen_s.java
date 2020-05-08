 package edu.wheaton.simulator.gui.screen;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import com.google.common.collect.ImmutableMap;
 
 import edu.wheaton.simulator.gui.BoxLayoutAxis;
 import edu.wheaton.simulator.gui.Gui;
 import edu.wheaton.simulator.gui.HorizontalAlignment;
 import edu.wheaton.simulator.gui.MaxSize;
 import edu.wheaton.simulator.gui.MinSize;
 import edu.wheaton.simulator.gui.PrefSize;
 import edu.wheaton.simulator.gui.SimulatorGuiManager;
 import edu.wheaton.simulator.simulation.Simulator;
 
 //TODO make sure that all information is actually being transmitted to simulation
 public class SetupScreen extends Screen {
 
 	private JTextField nameField;
 	private JTextField timeField;
 	
 	private JTextField widthField;
 	private JTextField heightField;
 
 	private String[] agentNames;
 	private JComboBox updateBox;
 
 	private ArrayList<JComboBox> agentTypes;
 	private ArrayList<JTextField> values;
 	private ArrayList<JButton> deleteButtons;
 	private ArrayList<JPanel> subPanels;
 
 	private JPanel conListPanel;
 	private JButton addConditionButton;
 
 	private static final long serialVersionUID = -8347080877399964861L;
 
 	public SetupScreen(final SimulatorGuiManager gm) {
 		super(gm);
 		this.setLayout(new GridBagLayout());
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		JPanel upperPanel = makeUpperPanel();
 		JPanel lowerPanel = makeLowerPanel();
 		conListPanel = makeConditionListPanel();
 		
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridheight = 3;
 		c.gridwidth = 3;
 		this.add(upperPanel,c);
 		
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridheight = 3;
 		c.gridwidth = 3;
 		this.add(lowerPanel,c);
 		
 		c.gridx = 0;
 		c.gridy = 7;
 		c.gridwidth = 4;
 		c.gridheight = GridBagConstraints.RELATIVE;
 		c.weighty = 1.0;
 		c.anchor = GridBagConstraints.PAGE_START;
 		this.add(conListPanel,c);
 		
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.anchor = GridBagConstraints.PAGE_END;
 		c.gridx = 2;
 		c.gridy = 8;
 		this.add(
 			Gui.makePanel(
 					Gui.makeButton("Revert",null,new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							load();
 						}}),
 						makeConfirmButton()
 					), c);
 
 		agentNames = new String[0];
 
 		agentTypes = new ArrayList<JComboBox>();
 		deleteButtons = new ArrayList<JButton>();
 		subPanels = new ArrayList<JPanel>();
 
 		agentTypes = new ArrayList<JComboBox>();
 		values = new ArrayList<JTextField>();
 	}
 
 	private JButton makeConfirmButton(){
 		return Gui.makeButton("Confirm",null,new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					SimulatorGuiManager gm = getGuiManager();
 					if (nameField.getText().equals(""))
 						throw new Exception("All fields must have input");
 					gm.setSimName(nameField.getText());
 
 					for (int i = 0; i < values.size(); i++)
 						if (values.get(i).getText().equals(""))
 							throw new Exception("All fields must have input.");
 
 					gm.setSimStepLimit(Integer.parseInt(timeField.getText()));
 					String str = (String)updateBox.getSelectedItem();
 
 					if (str.equals("Linear"))
 						gm.setSimLinearUpdate();
 					else if (str.equals("Atomic"))
 						gm.setSimAtomicUpdate();
 					else
 						gm.setSimPriorityUpdate(0, 50);
 
 					for (int i = 0; i < values.size(); i++) {
 						gm.setSimPopLimit(
 								(String)(agentTypes.get(i).getSelectedItem()), 
 								Integer.parseInt(values.get(i).getText())
 								);
 					}
 					load();
 				}
 				catch (NumberFormatException excep) {
 					JOptionPane.showMessageDialog(null,
 							"Width and Height fields must be integers greater than 0");
 				}
 				catch (Exception excep) {
 					JOptionPane.showMessageDialog(null, excep.getMessage());
 				} 
 			}
 		});
 	}
 
 	@Override
 	public void load() {
 		reset();
 		nameField.setText(getGuiManager().getSimName());
 		updateBox.setSelectedItem(getGuiManager().getCurrentSimUpdater());
 		widthField.setText(gm.getSimGridWidth().toString());
 		heightField.setText(gm.getSimGridHeight().toString());
 
 		SimulatorGuiManager gm = getGuiManager();
 		int stepLimit = gm.getSimStepLimit();
 		agentNames = Simulator.prototypeNames().toArray(agentNames);
 		timeField.setText(stepLimit + "");
 		//to prevent accidental starting simulation with time limit of 0
 		if (stepLimit <= 0) 
 			timeField.setText(10 + "");
 
 		ImmutableMap<String, Integer> popLimits = gm.getSimPopLimits();
 
 		if (popLimits.size() == 0) {
 			conListPanel.add(addConditionButton);
 		}
 		else {
 			int i = 0;
 			for (String p : popLimits.keySet()) {
 				addCondition();
 				agentTypes.get(i).setSelectedItem(p);
 
 				values.get(i).setText(popLimits.get(p) + "");
 				i++;
 			}
 		}
 		validate();
 	}
 	
 	private JPanel makeUpperPanel(){
 		JPanel upperPanel = Gui.makePanel(new GridBagLayout(), MaxSize.NULL, PrefSize.NULL, null);
 		
 		JLabel nameLabel = Gui.makeLabel("Name: ",MaxSize.NULL, HorizontalAlignment.LEFT);
 		nameField = Gui.makeTextField(gm.getSimName(), 25,new MaxSize(400,30),new MinSize(272,25));
 		nameField.setHorizontalAlignment(SwingConstants.LEFT);
 		JLabel widthLabel = Gui.makeLabel("Width: ", new MaxSize(200, 40), HorizontalAlignment.LEFT);
 		widthField = Gui.makeTextField("10", 5, new MaxSize(200, 40), new MinSize(100,25));
 		widthField.setHorizontalAlignment(SwingConstants.LEFT);
 		JLabel yLabel = Gui.makeLabel("Height: ", new MaxSize(200, 40), HorizontalAlignment.LEFT);
 		heightField = Gui.makeTextField("10", 5, new MaxSize(200, 40), new MinSize(100,25));
 		heightField.setHorizontalAlignment(SwingConstants.LEFT);
 		JLabel updateLabel = Gui.makeLabel("Update type: ",new MaxSize(100,40),HorizontalAlignment.LEFT);
 		updateBox = Gui.makeComboBox(new String[]{"Linear", "Atomic", "Priority"}, new MaxSize(200,40));
 		updateBox.setAlignmentY(LEFT_ALIGNMENT);
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.insets = new Insets(70,0,0,0);
 		upperPanel.add(nameLabel, c);
 
 		c.gridx = 1;
 		c.gridy = 0;
 		c.gridwidth = 3;
 		c.insets = new Insets(70,0,0,0);
 		upperPanel.add(nameField, c);
 		
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(0,0,0,0);
 		upperPanel.add(widthLabel,c);
 		
 		c.gridx = 1;
 		c.gridy = 1;
 		upperPanel.add(widthField,c);
 		
 		c.gridx = 2;
 		c.gridy = 1;
 		c.insets = new Insets(0,15,0,0);
 		upperPanel.add(yLabel,c);
 		
 		c.gridx = 3;
 		c.gridy = 1;
 		c.insets = new Insets(0,0,0,0);
 		upperPanel.add(heightField,c);
 		
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 2;
 		upperPanel.add(updateLabel,c);
 		
 		c.gridx = 2;
 		c.gridy = 2;
 		c.gridwidth = 2;
 		upperPanel.add(updateBox,c);
 		
 		return upperPanel;
 	}
 	
 	private JPanel makeLowerPanel(){
		JPanel lowerPanel = Gui.makePanel(new GridBagLayout(), MaxSize.NULL, PrefSize.NULL, null);
 		
 		JLabel conHeader = Gui.makeLabel("Ending Conditions",new PrefSize(300,100),HorizontalAlignment.CENTER );
 		JLabel timeLabel = Gui.makeLabel("Time Limit",new PrefSize(300,100),HorizontalAlignment.LEFT );
 		
 		timeField = Gui.makeTextField(null,15,new MaxSize(200,30),new MinSize(100,25));
 		timeField.setHorizontalAlignment(SwingConstants.RIGHT);
 		
 		JLabel agentTypeLabel = Gui.makeLabel("Agent Type",new PrefSize(300,30),HorizontalAlignment.LEFT);
 		JLabel valueLabel = Gui.makeLabel("Population Limit",new PrefSize(400,30),HorizontalAlignment.RIGHT);
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 1;
 		c.gridwidth = 3;
 		c.gridy = 0;
 		c.insets = new Insets(50,0,20,0);
 		lowerPanel.add(conHeader,c);
 		
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(0,0,20,0);
 		lowerPanel.add(timeLabel,c);
 		
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 3;
 		c.insets = new Insets(0,0,20,0);
 		lowerPanel.add(timeField,c);
 		
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 2;
 		c.insets = new Insets(0,0,0,120);
 		lowerPanel.add(agentTypeLabel,c);
 		
 		c.gridx = 2;
 		c.gridy = 2;
 		c.insets = new Insets(0,0,0,0);
 		lowerPanel.add(valueLabel,c);
 		
 		return lowerPanel;
 	}
 	
 	private JPanel makeConditionListPanel() {
 		JPanel conListPanel = Gui.makePanel(BoxLayoutAxis.Y_AXIS,null,null);
 		
 		addConditionButton = Gui.makeButton("Add Field",null,
 				new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addCondition();
 			}
 		});
 
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 3;
 		c.gridy = 0;
 		conListPanel.add(addConditionButton,c);
 		
 		return conListPanel;
 	}
 
 	private void addCondition() {
 		JComboBox newBox = Gui.makeComboBox(agentNames,new MaxSize(500,40));
 		newBox.setMinimumSize(new Dimension(200,25));
 		agentTypes.add(newBox);
 
 		JTextField newValue = Gui.makeTextField(null,25,new MaxSize(300,40),new MinSize(200,40));
 		values.add(newValue);
 
 		JButton newButton = Gui.makeButton("Delete",null,new DeleteListener());
 		deleteButtons.add(newButton);
 		newButton.setActionCommand(deleteButtons.indexOf(newButton) + "");
 
 		JPanel newPanel = Gui.makePanel( BoxLayoutAxis.X_AXIS,null,null);
 		newPanel.add(newBox);
 		newPanel.add(newValue);
 		newPanel.add(newButton);
 		subPanels.add(newPanel);
 
 		conListPanel.add(newPanel);
 		conListPanel.add(addConditionButton);
 		conListPanel.validate();
 
 		validate();	
 	}
 
 	private void reset() {
 		nameField.setText("");
 		conListPanel.removeAll();
 		agentTypes.clear();
 		values.clear();
 		deleteButtons.clear();
 		subPanels.clear();
 	}
 
 	private class DeleteListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e){
 			int n = Integer.parseInt(e.getActionCommand());
 			String str = (String) agentTypes.get(n).getSelectedItem();
 
 			if (str != null) 
 				getGuiManager().removeSimPopLimit(str);
 
 			conListPanel.remove(subPanels.get(n));
 			agentTypes.remove(n);
 			values.remove(n);
 			deleteButtons.remove(n);
 
 			for (int i = n; i < deleteButtons.size(); i++)
 				deleteButtons.get(i).setActionCommand(
 						(Integer.parseInt(deleteButtons.get(i).getActionCommand()) - 1) + "");
 
 			subPanels.remove(n);
 			validate();
 			repaint();
 		}
 	}
 }
