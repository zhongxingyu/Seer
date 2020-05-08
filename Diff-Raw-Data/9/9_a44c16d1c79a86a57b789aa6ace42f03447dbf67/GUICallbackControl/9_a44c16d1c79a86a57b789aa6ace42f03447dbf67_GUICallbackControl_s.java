 package ussr.remote.facade;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
import ussr.aGui.helpers.hintPanel.HintPanelTypes;
 import ussr.aGui.tabs.constructionTabs.AssignableControllersEditors;
 import ussr.builder.enumerations.SupportedModularRobots;
 import ussr.builder.enumerations.tools.ConstructionTools;
 import ussr.builder.simulationLoader.SimulationSpecification;
 
 /**
  * Provides GUI with  call backs from remote simulation.
  * @author Konstantinas
  */
 public interface GUICallbackControl extends Remote {
 
 	/**
 	 * Adapts Construct Robot tab to the tool chosen by user. To be more precise disables and enables relevant components of the tab. 
 	 * @param chosenTool,the tool chosen by the user in Construct Robot tab.
 	 */
 	public void adaptConstructRobotTabToChosenTool(ConstructionTools chosenTool)throws RemoteException;
 
 	/**
 	 * Populates the table in Assign behavior tab with labels of entity(module, connector and so on) selected in simulation environment.
 	 * @param labels, the string of labels separated by comma to populate the table with.
 	 */
 	public void updateTableWithLabels(String labels) throws RemoteException;
 	
 	/**
 	 * Sets the global ID of the module selected in simulation environment.
 	 * @param selectedModuleID, the global ID of the module selected in simulation environment.
 	 */
 	public void setSelectedModuleID(int selectedModuleID)throws RemoteException;
 	
 	/**
 	 * Adapts construct robot to the module type selected in simulation environment.
 	 * @param supportedModularRobot, the type of modular robot to adapt to.
 	 */
 	public void adaptConstructRobotTabToSelectedModuleType(SupportedModularRobots supportedModularRobot)throws RemoteException;
 	
 	/**
 	 * Updates the hint panel in the tab called Assign Behaviors by changing icon and text of it.
 	 * @param hintPanelTypes, the type of hint panel.
 	 * @param text, the text for hint panel to display.
 	 */
 	public void updateHintPanelAssignBehaviorsTab(HintPanelTypes hintPanelTypes, String text)throws RemoteException;
 	
 	/**
 	 * Calls back GUI in order to indicate new module addition in simulation environment.
 	 */
 	public void newModuleAdded()throws RemoteException;
 	
 	public SimulationSpecification getSimulationSpecification() throws RemoteException;
 	
 	public void newRobotLoaded(SimulationSpecification simulationSpecification)throws RemoteException;
 	
 	//public void adaptTabToModuleInSimulation()throws RemoteException;
 	
 	public String getDefaultConstructionModuleType()throws RemoteException;
 	
 	public Float getValueJSpinnerAtronSpeedRotateContinuous()throws RemoteException;
 	
 	public Integer getValuejSpinnerAtronRotateDegrees()throws RemoteException;	
 	
 	public  Integer getValueAtronNrsConnectors()throws RemoteException;
 	
 	
 	public  Float getValuejSpinnerOdinActuateContinuously()throws RemoteException;
 	
 	public int getSelectedjComboBoxMtranNrsActuators() throws RemoteException;
 	
 	public int getValuejSpinnerMtranRotateContinuously() throws RemoteException;
 	
 	public void setRemoteSimulationToNull()throws RemoteException;
 	
 	
 	public void setSelectedDefaultConstructionModule(Object defaultConstructionModuleType)throws RemoteException;
 	
 
 	
 }
