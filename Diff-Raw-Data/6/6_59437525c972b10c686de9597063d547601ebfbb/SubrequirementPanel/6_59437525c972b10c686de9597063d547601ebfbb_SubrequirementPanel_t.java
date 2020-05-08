 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 
 /**
  * @author Justin Hess
  * @author Christopher Botaish
  */
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.tabs;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementSelector;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementSelectorListener;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementSelectorMode;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementViewMode;
 
 public class SubrequirementPanel extends JPanel implements RequirementSelectorListener, RequirementTab
 {
 	private boolean enabled;
 	private RequirementTabsPanel parentPanel;
 	private RequirementViewMode viewMode;
 	private Requirement activeRequirement;
 	private JTable subRequirementTable;
 	private JButton addNewButton;
 	private JButton removeButton;
 	private RequirementSelector existingReqSelector;
 	private DefaultTableModel tableModel;
 
 	/**
 	 * Constructor for the subrequirement panel.
 	 * @param requirementBeingEdited the current requirement being edited.
 	 */
 	public SubrequirementPanel(RequirementTabsPanel parentPanel, RequirementViewMode vm, Requirement requirementBeingEdited)
 	{
 		this.setLayout(new BorderLayout());
 		this.parentPanel = parentPanel;
 		this.viewMode = vm;
 		this.activeRequirement = requirementBeingEdited;
 		enabled = true;
 		existingReqSelector = new RequirementSelector(this, activeRequirement, RequirementSelectorMode.POSSIBLE_CHILDREN, false);
 		// Create new scroll pane for jtable
 		
 		JScrollPane scroll = new JScrollPane();
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		// Always show scroll bar
 
 		addNewButton = new JButton("Add New");
 		addNewButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ViewEventController.getInstance().createChildRequirement(activeRequirement.getId());				
 			}
 		});
 		
 		removeButton = new JButton("Remove Child");
 		removeButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int[] selectedObjects = subRequirementTable.getSelectedRows();
 				
 				for(int i = 0; i < selectedObjects.length; i++)
 				{
 					Requirement toBeRemoved = (Requirement)subRequirementTable.getValueAt(i, 0);
 					try {
 						toBeRemoved.setParentID(-1);
 					} catch (Exception e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					UpdateRequirementController.getInstance().updateRequirement(toBeRemoved);
 					ViewEventController.getInstance().refreshEditRequirementPanel(toBeRemoved);
 				}
 				existingReqSelector.refreshList();
 				refreshTable();
 			}
 		});
 		removeButton.setEnabled(false);
 
 		this.add(scroll, BorderLayout.CENTER); // Add scroll pane to panel
 
 		existingReqSelector.addButton(1,addNewButton);
 		existingReqSelector.addButton(0,removeButton);
 		this.add(existingReqSelector,BorderLayout.SOUTH);
 		
 		subRequirementTable = buildTable();
 		scroll.setViewportView(subRequirementTable);
 		
 		this.refreshTable();
 		
 		if(viewMode == RequirementViewMode.CREATING) enableChildren(false);
 	}
 
 	/**
 	 * Constructs the jtable that holds all of the subrequirements.
 	 * @return the subrequirement table.
 	 */
 	private JTable buildTable()
 	{
 		subRequirementTable = new JTable()
 		{
 			/**
 			 * Overrides the isCellEditable method to ensure no cells are editable.
 			 * 
 			 * @param row	row of OverviewTable cell is located
 			 * @param col	column of OverviewTable cell is located
 			 */
 			@Override
 			public boolean isCellEditable(int row, int col)
 			{
 				return false;
 			}
 		};
 
 		String[] columnNames = {"Name", "Description", "Priority", "Estimate"}; 		
 		tableModel = new DefaultTableModel(columnNames, 0);
 		subRequirementTable.setModel(tableModel);
 
 		subRequirementTable.getTableHeader().setReorderingAllowed(false);
 		subRequirementTable.setAutoCreateRowSorter(true);
 		subRequirementTable.setFillsViewportHeight(true);
 
 		subRequirementTable.getColumnModel().getColumn(0).setMaxWidth(240);
 		subRequirementTable.getColumnModel().getColumn(2).setMaxWidth(75);
 		subRequirementTable.getColumnModel().getColumn(3).setMaxWidth(75);
 		
 		/* Create double-click event listener */
 		subRequirementTable.addMouseListener(new MouseAdapter(){
 			public void mouseClicked(MouseEvent e){
 				
 				if(subRequirementTable.getRowCount() > 0)
 				{
 					int mouseY = e.getY();
 					Rectangle lastRow = subRequirementTable.getCellRect(subRequirementTable.getRowCount() - 1, 0, true);
 					int lastRowY = lastRow.y + lastRow.height;
 
 					if(mouseY > lastRowY) 
 					{
 						subRequirementTable.getSelectionModel().clearSelection();
 						repaint();
 					}
 				}
 				
 				if (e.getClickCount() == 2)
 				{
 					int[] selection = subRequirementTable.getSelectedRows();
 
 					if(selection.length != 1) return;
 					Requirement toEdit = (Requirement)subRequirementTable.getValueAt(selection[0], 0);
 					ViewEventController.getInstance().editRequirement(toEdit);
 				}
 			}
 		});
 		
 		subRequirementTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				removeButton.setEnabled(subRequirementTable.getSelectedRowCount() > 0 && enabled);
 			}		
 		});
 		
 		return subRequirementTable;
 	}
 
 	/**
 	 * Populates the subrequirement table with the sub requirements of the requirement being edited.
 	 */
 	private void refreshTable()
 	{
 		tableModel.setRowCount(0); //clear the table
 
 		List<Requirement> subRequirements = activeRequirement.getChildren();
 
 		for (int i = 0; i < subRequirements.size(); i++) {
 			Requirement req = subRequirements.get(i);
 			if (!req.isDeleted()) {
 				tableModel.addRow(new Object[]{ req,
 						req.getDescription(),
 						req.getPriority(),
 						req.getEstimate()
 				});
 			}
 		}
 	}
 
 	/**
 	 * Overrides the paintComponent method to retrieve the requirements on the first painting.
 	 * 
 	 * @param g	The component object to paint
 	 */
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		refreshTable();
 
 		super.paintComponent(g);
 	}
 
 	/**
 	 * Overrides the requirementSelected method to signal to refresh the table when the
 	 * subrequirements are updated.
 	 */
 	@Override
 	public void requirementSelected() {
 		refreshTable();
 	}
 	
 	/**
 	 * disable child panels
 	 * @param whether its enabled or not
 	 */
 	public void enableChildren(boolean enabled)
 	{
 		this.enabled = enabled;
		addNewButton.setEnabled(enabled && viewMode != RequirementViewMode.CREATING);
		removeButton.setEnabled(enabled && subRequirementTable.getSelectedRowCount() != 0 && viewMode != RequirementViewMode.CREATING);
		existingReqSelector.enableChildren(enabled && viewMode != RequirementViewMode.CREATING);
 	}
 
 	@Override
 	public boolean readyToRemove() {
 		return true;
 	}
 }
