 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.overview;
 
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.controller.GetRequirementsController;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.RequirementModel;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.ViewEventController;
 
 public class OverviewTable extends JTable
 {
 	private DefaultTableModel tableModel = null;
 	private boolean initialized;
 	/**
 	 * Sets initial table view
 	 * 
 	 * @param data	Initial data to fill OverviewTable
 	 * @param columnNames	Column headers of OverviewTable
 	 */
 	public OverviewTable(Object[][] data, String[] columnNames)
 	{
 		this.tableModel = new DefaultTableModel(columnNames, 0);
 		this.setModel(tableModel);

		this.getTableHeader().setReorderingAllowed(false);
 		this.setAutoCreateRowSorter(true);
		setFillsViewportHeight(true);

 		ViewEventController.getInstance().setOverviewTable(this);
 		initialized = false;
 		
 		/* Create double-click event listener */
 		this.addMouseListener(new MouseAdapter(){
 			public void mouseClicked(MouseEvent e){
 				if (e.getClickCount()==2){
 					ViewEventController.getInstance().editSelectedRequirement();
 				}
 			}
 		});
 	}
 	
 	/**
 	 * updates OverviewTable with the contents of the requirement model
 	 * 
 	 */
 	public void refresh() {
 		tableModel.setRowCount(0); //clear the table
 		
 		List<Requirement> requirements = RequirementModel.getInstance().getRequirements();
 		for (int i = 0; i < requirements.size(); i++) {
 			Requirement req = requirements.get(i);
 			if (!req.isDeleted()) {
 				tableModel.addRow(new Object[]{ req.getId(), 
 												req,
 												req.getRelease(),
 												req.getIteration(),
 												req.getType(),
 												req.getStatus(),
 												req.getPriority(),
 												req.getEstimate()
 												 }
 				);
 			}
 		}
 			
 	}
 	
 	/**
 	 * Overrides the isCellEditable method to ensure no cells are editable.
 	 */
 	@Override
 	public boolean isCellEditable(int row, int col)
 	{
 		return false;
 	}
 	
 	
 	/**
 	 * Overrides the paintComponent method to retrieve the requirements on the first painting.
 	 */
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		if(!initialized)
 		{
 			try 
 			{
 				GetRequirementsController.getInstance().retrieveRequirements();
 				initialized = true;
 			}
 			catch (Exception e)
 			{
 
 			}
 		}
 
 		super.paintComponent(g);
 	}
 }
