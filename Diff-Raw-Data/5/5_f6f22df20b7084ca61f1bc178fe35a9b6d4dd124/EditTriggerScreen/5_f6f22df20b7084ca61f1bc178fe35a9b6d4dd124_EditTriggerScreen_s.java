 package edu.wheaton.simulator.gui;
 
 import java.awt.Dimension;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 public class EditTriggerScreen extends Screen {
 
 	private JButton addConditional;
 	
 	private JButton addBehavior;
 	
 	private JLabel title;
 	
 	public EditTriggerScreen(Manager sm) {
 		super(sm);
 		addConditional = new JButton();
 		addBehavior = new JButton();
 		title = new JLabel("Edit Trigger");
 		title.setPreferredSize(new Dimension(300, 100));
 		
 	}
 
 	@Override
 	public void load() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
