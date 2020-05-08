 package org.esgi.java.grabbergui.view.gui.items;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import org.esgi.java.grabbergui.models.ProjectGrabber;
 import org.esgi.java.grabbergui.view.gui.lang.TR;
 
 /**
  * The projectsTabItem class is an object contained in the
  * ProjectPanelView class. This object is used to display the
  * grabber project.
  *  
  * @author gwadaboug
  */
 public class ProjectsTabItem extends AbstractTableModel {
 
 	private static final long serialVersionUID = 3794908225833880479L;
 	
 	//---------------------------------------------------------------------------------------------
 	// Public constants
 	//---------------------------------------------------------------------------------------------
 	public static final int DISPLAY_RUN   = 0;
 	public static final int DISPLAY_PAUSE = 1;
 	public static final int DISPLAY_END   = 2;
 	public static final int DISPLAY_ALL   = 3;
 	
 	//---------------------------------------------------------------------------------------------
 	// Private Variables 
 	//---------------------------------------------------------------------------------------------
 	private int statusToDisplay;
 	private ArrayList<ProjectGrabber> projectGrabberList;
 	private String[] column = {TR.toString("$PV_TAB_NAME"), 
 			TR.toString("$PV_TAB_STATUS"), TR.toString("$PV_TAB_FILES"),
 			TR.toString("$PV_TAB_SIZE"), TR.toString("$PV_TAB_PROGRESS")};
 	
 	//---------------------------------------------------------------------------------------------
 	// Constructor
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Init the items of the class
 	 */
 	public ProjectsTabItem() {
 		this.projectGrabberList = new ArrayList<ProjectGrabber>();
 		this.statusToDisplay = ProjectsTabItem.DISPLAY_ALL;
 	}
 
 	//---------------------------------------------------------------------------------------------
 	// Public methods
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Add a ProjectGrabber model into the table
 	 */
 	public void addProjectModel(ProjectGrabber project) {
 		this.projectGrabberList.add(project);
 	}
 
 	/**
 	 * Remove a ProjectGrabber (not implemented yet)
 	 */
 	public void removeProjectModel(ProjectGrabber project) {
 		this.projectGrabberList.remove(project);
 	}
 	
 	/**
 	 * Modify the status of the table, for choose which type of
 	 * ProjectGrabber will be showed
 	 */
 	public void setStatusToShow(int status) {
 		this.statusToDisplay = status; 
 	}
 	
 	public List<ProjectGrabber> getAllProjects()
 	{
 		return this.projectGrabberList;
 	}
 	
 	//---------------------------------------------------------------------------------------------
 	// Override methods
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Get all table column name
 	 */
 	@Override
 	public String getColumnName(int col) {
         return this.column[col];
     }
 
 	/**
 	 * Get the number of column
 	 */
 	@Override
 	public int getColumnCount() {
 		return this.column.length;
 	}
 
 	/**
 	 * Get the number of row there are in the table
 	 */
 	@Override
 	public int getRowCount() {
 		//if all project are displayed then the number of row is the size of
 		//the list of project
 		if (this.statusToDisplay == ProjectsTabItem.DISPLAY_ALL)
 			return this.projectGrabberList.size();
 		
 		//else we cover the table and search the project with the same status of us status
 		int count = 0;
 		for (ProjectGrabber project : this.projectGrabberList) {
 			if      ((this.statusToDisplay == ProjectsTabItem.DISPLAY_RUN)   && (project.getStatus() == ProjectGrabber.State.PROJECT_RUN))   count ++;
 			else if ((this.statusToDisplay == ProjectsTabItem.DISPLAY_PAUSE) && (project.getStatus() == ProjectGrabber.State.PROJECT_PAUSE)) count ++;
 			else if ((this.statusToDisplay == ProjectsTabItem.DISPLAY_END)   && (project.getStatus() == ProjectGrabber.State.PROJECT_END))   count ++;
 		}
 		return count;
 	}
 
 	/**
 	 * get the cell value with the X and Y coordinate 
 	 */
 	@Override
 	public Object getValueAt(int row, int column) {
 		ProjectGrabber project = this.getProjectAtRow(row);
 		
 		if (column == 0) return project.getName();
 		else if (column == 1) return project.getStatusStr();
 		else if (column == 2) return project.getdowloadedfiles();
 		else if (column == 3) {
			
			long dl = project.getDowloadedsize()/(1024); 
 			if ( dl < 1024)
 				return dl +" Ko";
 			else {
 				long percent = (dl*100)/1024;
 				return (float)percent /100 +" Mo";
 			}
			
 		}
 		else if (column == 4 )
 			return project.getProgress();
 		
 		return null;
 	}
 	
 	/**
 	 * get the project at the parameter row. the project returned depend of the
 	 * table status
 	 */
 	public ProjectGrabber getProjectAtRow(int row) {
 		// If all projects are displayed, then we return the project at the
 		// list index
 		if (this.statusToDisplay == ProjectsTabItem.DISPLAY_ALL)
 			return this.projectGrabberList.get(row);
 		
 		// Else we must cover the list, and increment a counter for find the
 		// row
 		int count = -1;
 		for (ProjectGrabber p : this.projectGrabberList) {
 			if      ((this.statusToDisplay == ProjectsTabItem.DISPLAY_RUN)   && (p.getStatus() == ProjectGrabber.State.PROJECT_RUN))   count ++;
 			else if ((this.statusToDisplay == ProjectsTabItem.DISPLAY_PAUSE) && (p.getStatus() == ProjectGrabber.State.PROJECT_PAUSE)) count ++;
 			else if ((this.statusToDisplay == ProjectsTabItem.DISPLAY_END)   && (p.getStatus() == ProjectGrabber.State.PROJECT_END))   count ++;
 			
 			if (count == row)
 				return p;
 		}
 		//if no row found, then we return null
 		return null;
 	}
 }
