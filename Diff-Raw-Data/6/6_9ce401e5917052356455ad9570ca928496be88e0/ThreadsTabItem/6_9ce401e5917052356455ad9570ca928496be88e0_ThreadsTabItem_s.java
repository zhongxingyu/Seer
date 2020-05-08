 package org.esgi.java.grabbergui.view.gui.items;
 
 import javax.swing.table.AbstractTableModel;
 
 import org.esgi.java.grabbergui.models.ProjectGrabber;
 import org.esgi.java.grabbergui.view.gui.lang.TR;
 
 public class ThreadsTabItem extends AbstractTableModel {
 	
 	private static final long serialVersionUID = 6312355165956874881L;
 	
 	
 	//---------------------------------------------------------------------------------------------
 	// Private variables
 	//---------------------------------------------------------------------------------------------
 	private ProjectGrabber project;
 	private String[] column = {"$SV_TAB_URL"};
 	
 	//---------------------------------------------------------------------------------------------
 	// Constructor
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Construct the ThreadsTabItem object 
 	 */
 	public ThreadsTabItem() {
 		this.project = null;
 	}
 	//---------------------------------------------------------------------------------------------
 	// Getters and Setters
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Get the current project displayed
 	 */
 	public ProjectGrabber getProjectDisplayed()
 	{
 		return this.project;
 	}
 	
 	/**
 	 * Set the project to display
 	 */
 	public void setProjectToDisplay(ProjectGrabber project)
 	{
 		this.project = project;
 	}
 	//---------------------------------------------------------------------------------------------
 	// Public methods
 	//---------------------------------------------------------------------------------------------
 	
 	//---------------------------------------------------------------------------------------------
 	// Override methods
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Get all table column name
 	 */
 	@Override
 	public String getColumnName(int column) {
 		return TR.toString(this.column[column]);
 	}
 	
 	/**
 	 * Return the number of column
 	 */
 	@Override
 	public int getColumnCount() {
 		return this.column.length;
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	public int getRowCount() {
 		if (this.project == null)
 			return 0;
 		//TODO: change in the projectGrabber the type of list
 		return this.project.getTodownload().size();
 	}
 	
 	
 	@Override
 	public Object getValueAt(int row, int column) {
		if (row > 50) 
 			return null;
		if (column == 0) return project.getHistory().toArray()[project.getHistory().size() -row + 1];
 		return null;
 	}
 	
 	//---------------------------------------------------------------------------------------------
 	// Private methods
 	//---------------------------------------------------------------------------------------------
 }
