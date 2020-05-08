 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view;
 
 import java.awt.Component;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.overview.OverviewPanel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.overview.OverviewTable;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.EditRequirementPanel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.NewRequirementPanel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanel;
 
 
 /**
  * Provides an interface for interaction with the main GUI elements
  * All actions on GUI elements should be conducted through this controller.
  */
 
 public class ViewEventController {
 	private static ViewEventController instance = null;
 	private MainView main = null;
 	private ToolbarView toolbar = null;
 	private OverviewTable overviewTable = null;
 	private ArrayList<EditRequirementPanel> listOfEditingPanels = new ArrayList<EditRequirementPanel>();
 	
 	/**
 	 * Sets the OverviewTable for the controller
 	 * @param overviewTable a given OverviewTable
 	 */
 	public void setOverviewTable(OverviewTable overviewTable) {
 		this.overviewTable = overviewTable;
 	}
 
 	/**
 	 * Default constructor for ViewEventController.  Is protected to prevent instantiation.
 	 */
 	protected ViewEventController() {}
 
 	/**
 	 * Returns the singleton instance of the vieweventcontroller.
 	 * @return The instance of this controller.
 	 */
 	public static ViewEventController getInstance() {
 		if (instance == null) {
 			instance = new ViewEventController();
 		}
 		return instance;
 	}
 
 	/**
 	 * Sets the main view to the given view.
 	 * @param main2 the main view to be set as active.
 	 */
 	public void setMainView(MainView mainview) {
 		main = mainview;
 	}
 
 	/**
 	 * Sets the toolbarview to the given toolbar
 	 * @param tool2 the toolbar to be set as active.
 	 */
 	public void setToolBar(ToolbarView toolbar) {
 		this.toolbar = toolbar;
 	}
 
 	/**
 	 * Opens a new tab for the creation of a requirement.
 	 */
 	public void createRequirement() {
 		NewRequirementPanel newReq = new NewRequirementPanel();
 		main.addTab("New Req.", null, newReq, "New Requirement");
 		main.invalidate(); //force the tabbedpane to redraw.
 		main.repaint();
 		main.setSelectedComponent(newReq);
 	}
 	
 	/**
 	 * Opens a child requirement panel to create the child requirement for the given parent.
 	 * @param parentID
 	 */
 	public void createChildRequirement(int parentID) {
 		NewRequirementPanel newReq = new NewRequirementPanel(parentID);
 		main.addTab("Add Child Req.", null, newReq, "Add Child Requirement");
 		main.invalidate(); //force the tabbedpane to redraw.
 		main.repaint();
 		main.setSelectedComponent(newReq);
 	}
 	/**
 	 * Opens a new tab for the editing of a requirement
 	 */
 	public void editRequirement(Requirement toEdit)
 	{
 		EditRequirementPanel editPanel = new EditRequirementPanel(toEdit);
 		
 		StringBuilder tabName = new StringBuilder();
 		tabName.append(toEdit.getId()); 
 		tabName.append(". ");
 		int subStringLength = toEdit.getName().length() > 6 ? 7 : toEdit.getName().length();
 		tabName.append(toEdit.getName().substring(0,subStringLength));
 		if(toEdit.getName().length() > 6) tabName.append("..");
 		
 		main.addTab(tabName.toString(), null, editPanel, toEdit.getName());
 		this.listOfEditingPanels.add(editPanel);
 		main.invalidate();
 		main.repaint();
 		main.setSelectedComponent(editPanel);
 	}
 	
 	
 	/**
 	 * Toggles the Overview Table multiple requirement editing mode
 	 */
 	public void toggleEditingTable(){
		if (this.overviewTable.getEditFlag()) {
			this.overviewTable.getCellEditor().stopCellEditing();
 		}
		this.overviewTable.setEditFlag(!this.overviewTable.getEditFlag());
 	}
 	
 	
 	/** 
 	 * @return overviewTable
 	 */
 	public OverviewTable getOverviewTable(){
 		return overviewTable;
 		
 	}
 	
 	/**
 	 * Removes the tab for the given JComponent
 	 */
 	
 	public void removeTab(JComponent comp)
 	{
 		if(comp instanceof EditRequirementPanel)
 		{
 			if(!((EditRequirementPanel)comp).readyToRemove()) return;
 			this.listOfEditingPanels.remove(comp);
 			
 		}
 		main.remove(comp);
 	}
 
 	/**Tells the table to update its listings based on the data in the requirement model
 	 * 
 	 */
 	public void refreshTable() {
 		overviewTable.refresh();
 	}
 	
 	/**
 	 * Returns an array of the currently selected rows in the table.
 	 * @return the currently selected rows in the table
 	 */
 	public int[] getTableSelection()
 	{
 		return overviewTable.getSelectedRows();
 	}
 	
 	/**
 	 * Assigns all currently selected rows to the backlog.
 	 */
 	public void assignSelectionToBacklog()
 	{
 		int[] selection = overviewTable.getSelectedRows();
 		
 		// Set to false to indicate the requirement is being newly created
 		boolean created = false;
 		
 		for(int i = 0; i < selection.length; i++)
 		{
 			Requirement toSendToBacklog = (Requirement)overviewTable.getValueAt(selection[i], 1);
 			toSendToBacklog.setIteration("Backlog", created);
 			UpdateRequirementController.getInstance().updateRequirement(toSendToBacklog);
 		}
 		
 		this.refreshTable();
 	}
 	
 	/**
 	 * Edits the currently selected requirement.  If more than 1 requirement is selected, does nothing.
 	 */
 	public void editSelectedRequirement()
 	{
 		int[] selection = overviewTable.getSelectedRows();
 
 		if(selection.length != 1) return;
 		
 		Requirement toEdit = (Requirement)overviewTable.getValueAt(selection[0],1);
 		
 		EditRequirementPanel exists = null;
 		
 		for(EditRequirementPanel panel : listOfEditingPanels)
 		{
 			if(panel.getRequirementBeingEdited() == toEdit)
 			{
 				exists = panel;
 				break;
 			}
 		}	
 		
 		if(exists == null)
 		{
 			editRequirement(toEdit);
 		}
 		else
 		{
 			main.setSelectedComponent(exists);
 		}
 	}
 
 	/**
 	 * Closes all of the tabs besides the overview tab in the main view.
 	 */
 	public void closeAllTabs() {
 
 		int tabCount = main.getTabCount();
 		
 		for(int i = tabCount - 1; i >= 0; i--)
 		{
 			Component toBeRemoved = main.getComponentAt(i);
 
 			if(toBeRemoved instanceof OverviewPanel) continue;
 			
 			if(toBeRemoved instanceof RequirementPanel)
 			{
 				if(!((RequirementPanel)toBeRemoved).readyToRemove()) break;
 				this.listOfEditingPanels.remove(toBeRemoved);
 			}
 			
 			main.removeTabAt(i);
 		}
 		
 		main.repaint();
 	}
 
 	/**
 	 * Closes all the tabs except for the one that was clicked.
 	 * 
 	 * @param indexOfTab Index of the tab that was clicked
 	 */
 	public void closeOthers(int indexOfTab) {
 		int tabCount = main.getTabCount();
 		Component compAtIndex = main.getComponentAt(indexOfTab);
 		
 		for(int i = tabCount - 1; i >= 0; i--)
 		{
 			Component toBeRemoved = main.getComponentAt(i);
 			
 			if(toBeRemoved instanceof OverviewPanel) continue;
 			
 			if(toBeRemoved == compAtIndex) continue;
 			
 			if(toBeRemoved instanceof RequirementPanel)
 			{
 				if(!((RequirementPanel)toBeRemoved).readyToRemove()) break;
 					this.listOfEditingPanels.remove(toBeRemoved);
 			}
 
 			main.removeTabAt(i);
 		}
 		main.repaint();
 		
 	}
 }
