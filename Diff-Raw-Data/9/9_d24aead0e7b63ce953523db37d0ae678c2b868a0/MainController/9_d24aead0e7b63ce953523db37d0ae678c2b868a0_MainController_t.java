 package ch.bli.mez.controller;
 
 import ch.bli.mez.view.MainView;
 
 /**
  * MainController startet das GUI und setzt die einzelne GUIs zusammen
  * 
  * @author dave
  * @version 1.0
  */
 public class MainController {
 
 	private final EmployeeController employeeController;
 	// private final TimeMgmtController timeMgmtController;
 	// private final ReportingController reportingController;
	   private final ManagementController managementController;
 	private final MainView mainView;
 
 	public MainController() {
 	 this.managementController = new ManagementController();
 		this.mainView = new MainView();
 		this.employeeController = new EmployeeController();
 		setPanels();
 	}
 
 	/**
 	 * Macht das GUI sichtbar
 	 */
 	public void showView() {
 		mainView.pack();
 		mainView.setVisible(true);
 	}
 
 	/**
 	 * Setzt die einzelnen Panels in der Mainframe, ursprünglich
 	 * "Mitarbeiter verwalten", "Zeiten erfassen", "Auswertungen" und
 	 * "Verwaltung"
 	 */
 	private void setPanels() {
 		this.mainView.setEmployeePanel(this.employeeController.getView());
 		this.mainView.setManagementPanel(this.managementController.getView());
 		// hier sollen die "Zeiten erfassen", "Auswertungen" und "Verwaltung"
 		// Panels dem Mainframe hinzugefügt werden (internerKommentar)
 	}
 }
