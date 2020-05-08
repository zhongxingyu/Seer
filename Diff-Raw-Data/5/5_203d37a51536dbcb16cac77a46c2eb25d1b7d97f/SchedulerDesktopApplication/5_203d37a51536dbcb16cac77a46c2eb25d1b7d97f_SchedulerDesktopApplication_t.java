 /**
  * 
  */
 package org.promasi.client_swing.gui.desktop.application.Scheduler;
 
 import java.awt.BorderLayout;
 import java.io.IOException;
 
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import org.joda.time.DateTime;
 import org.promasi.client_swing.gui.GuiException;
 import org.promasi.client_swing.gui.desktop.IDesktop;
 import org.promasi.client_swing.gui.desktop.application.ADesktopApplication;
 import org.promasi.client_swing.gui.desktop.application.QuickStartButton;
 import org.promasi.game.IGame;
 import org.promasi.game.company.DepartmentMemento;
 import org.promasi.game.company.EmployeeMemento;
 import org.promasi.game.company.IDepartmentListener;
 
 import org.promasi.utilities.file.RootDirectory;
 
 
 /**
  * @author alekstheod
  *
  */
 public class SchedulerDesktopApplication extends ADesktopApplication implements ISchedulerApplication, IDepartmentListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * 
 	 */
 	public static final String CONST_APPNAME = "Scheduler";
 	
 	/**
 	 * 
 	 */
 	public static final String CONST_APP_ICON = "gantt.png";
 	
 	/**
 	 * 
 	 */
 	private QuickStartButton _quickButton;
 	
 	/**
 	 * 
 	 */
 	private JPanel _internalPanel;
 	
 	/**
 	 * 
 	 * @param game
 	 * @throws GuiException
 	 * @throws IOException
 	 */
 	public SchedulerDesktopApplication( IGame game, IDesktop desktop ) throws GuiException, IOException{
 		super(CONST_APPNAME, RootDirectory.getInstance().getImagesDirectory() + CONST_APP_ICON);
 		if( game == null){
 			throw new GuiException("Wrong argument game == null");
 		}
 		
 		if( desktop == null ){
 			throw new GuiException("Wrong argument desktop == null");
 		}
 		
 		setLayout(new BorderLayout());
 		_internalPanel = new JPanel();
 		_internalPanel.setLayout(new BorderLayout());
 		add(_internalPanel, BorderLayout.CENTER);
 		_internalPanel.add( new SchedulerJPanel( game , this, desktop), BorderLayout.CENTER);
 		_quickButton = new QuickStartButton(this, desktop); 
 		desktop.addQuickStartButton(_quickButton);
 		game.addDepartmentListener(this);
 	}
 
 	@Override
 	public void setPanel(JPanel panel) {
 		if( panel != null ){
 			_internalPanel.removeAll();
 			_internalPanel.add(panel, BorderLayout.CENTER);
			_internalPanel.invalidate();
 			_internalPanel.repaint();
 		}
 	}
 
 	@Override
 	public void employeeDischarged(String director,
 			DepartmentMemento department, EmployeeMemento employee,
 			DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void employeeHired(String director, DepartmentMemento department,
 			EmployeeMemento employee, DateTime dateTime) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void tasksAssigned(String director, DepartmentMemento department,
 			DateTime dateTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				_quickButton.showPopupNotifier( "Tasks assigned");
 			}
 		});
 	}
 
 	@Override
 	public void tasksAssignFailed(String director,
 			DepartmentMemento department, DateTime dateTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				_quickButton.showPopupNotifier( "Assign tasks failed");
 			}
 		});
 	}
 
 	@Override
 	public void departmentAssigned(String director,
 			DepartmentMemento department, DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
