/*******************************************************************************
 * Copyright (c) 2013 WPI-Suite
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Team Rolling Thunder
 ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.buttons;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 import javax.swing.border.EtchedBorder;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 
 public class RequirementButtonsPanel extends JPanel{
 	
 	// initialize the main view toolbar buttons
 		private JButton createButton = new JButton("Create Requirement");
 		
 		private final JButton createIterationButton = new JButton("Create Iteration");
 	
 	public RequirementButtonsPanel(){
 		setBorder(BorderFactory.createTitledBorder("Create")); // add a border so you can see the panel
 		
 		SpringLayout toolbarLayout = new SpringLayout();
 		this.setLayout(toolbarLayout);
 		
 		
 
 		// the action listener for the Create Requirement Button
 		createButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// bring up a create requirement pane if not in Multiple Requirement Editing Mode
 				if (!ViewEventController.getInstance().getOverviewTable().getEditFlag()) {
 					ViewEventController.getInstance().createRequirement();
 				}
 			}
 		});		
 		
 		//action listener for the Create Iteration Button
 		createIterationButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				//if (!ViewEventController.getInstance().getOverviewTable().getEditFlag()) {
 					ViewEventController.getInstance().createIteration();
 				}
 		//	}
 		});
 		
 		toolbarLayout.putConstraint(SpringLayout.NORTH, createButton, 5,SpringLayout.NORTH, this);
 		toolbarLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, createButton, 0, SpringLayout.HORIZONTAL_CENTER, this);
 		
 		toolbarLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, createIterationButton, 0, SpringLayout.HORIZONTAL_CENTER, this);
 		toolbarLayout.putConstraint(SpringLayout.NORTH, createIterationButton, 5, SpringLayout.SOUTH, createButton);
 		
 		this.add(createButton);
 		this.add(createIterationButton);
 	}
 	public JButton getCreateButton() {
 		return createButton;
 	}
 
 	public JButton getCreateIterationButton() {
 		return createIterationButton;
 	}
 
 	
 }
