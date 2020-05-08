 package ussr.aGui.tabs.controllers;
 
 
 import java.rmi.RemoteException;
 
 import javax.swing.AbstractButton;
 import javax.swing.JComboBox;
 import javax.swing.JToggleButton;
 
 import ussr.builder.enumerations.ConstructionTools;
 import ussr.builder.enumerations.SupportedModularRobots;
 import ussr.aGui.enumerations.hintpanel.HintsConstructRobotTab;
 import ussr.aGui.tabs.constructionTabs.ConstructRobotTab;
 
 
 /**
  * Controls events of Construct Robot Tab  and underlying logic of simulator.
  * @author Konstantinas
  */
 public class ConstructRobotTabController extends TabsControllers{
 
 	/**
 	 *  The name of modular robot chosen in GUI.
 	 */
 	private  static SupportedModularRobots chosenMRname;
 	
 	/**
 	 * Adds initial construction module according to selected module type and adapts the Tab to modular robot type.
 	 * @param button, button selected in the group of radio button.
 	 */
 	public static void jButtonGroupActionPerformed(AbstractButton button ) {
 
 		String chosenModularRobot = button.getText();			
 		//chosenMRname = SupportedModularRobots.valueOf(chosenModularRobot.toUpperCase());
 		chosenMRname = SupportedModularRobots.getModularRobotSystemName(chosenModularRobot);
 		
 		/*Adapt Tab components to chosen modular robot */
 		adaptTabToChosenMR(chosenMRname,false);
 
 		try {
 			/*Add initial construction module*/
 			builderControl.addInitialConstructionModule(chosenMRname);
 		} catch (RemoteException e1) {
 			throw new Error("Failed to add initial construction module due to remote exception");
 		}
 
 		try {
 			/*Set default construction tool to be "On selected  connector"*/
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called " + ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR + " , due to remote exception");
 		}
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.MODULAR_ROBOT_CHOSEN.getHintText());
 	}
 
 
 	/**
 	 * Adapt the tab to chosen modular robot.
 	 * @param chosenMRname, modular robot name chosen in button group.
 	 * @param moduleSelectedFlag, flag for identifying selection of module in simulation environment.
 	 */
 	private static void adaptTabToChosenMR(SupportedModularRobots chosenMRname, boolean moduleSelectedFlag){
 
 		/*Disable and enable components*/
 		if (moduleSelectedFlag){//do nothing
 		}else{
 			ConstructRobotTab.setRadioButtonsEnabled(false);
 			ConstructRobotTab.getJComboBoxEntity().setEnabled(true);
 			ConstructRobotTab.setEnabledFirstModuleToolBar(true);
 			ConstructRobotTab.setEnabledGenericToolBar(true);
 			ConstructRobotTab.setEnabledConstructionToolsToolBar(true);
 		}
 
 		/*Adapt tab to chosen modular robot*/
 		switch(chosenMRname){
 		case ATRON:
 			ConstructRobotTab.adaptTabToATRON();
 			break;
 		case MTRAN:
 			ConstructRobotTab.adaptTabToMTRAN();
 			break;
 		case ODIN:
 			ConstructRobotTab.adaptTabToOdin();
 			break;
 		case CKBOT_STANDARD:
 			ConstructRobotTab.adaptTabToCKBOTSTANDARD();
 			break;
 		}	
 	}
 
 	/**
 	 * Default chosen entity for operations on existing modules or robot. 
 	 */
 	private static String chosenItem ="Module" ;
 
 	/**
 	 * Initializes the tool for deleting module or robot with the mouse selecting.
 	 */
 	public static void jButtonDeleteActionPerformed() {
 		if (chosenItem.equalsIgnoreCase("Module")){
 			try {
 				builderControl.setRemoveModulePicker();
 			} catch (RemoteException e) {
 				throw new Error("Failed to initialize picker called Remove Module, due to remote exception");
 			}
 		}else if (chosenItem.equalsIgnoreCase("Robot")){
 			//TODO SUPPORT ROBOT DELETION
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.DELETE.getHintText()); 
 	}
 
 	/**
 	 * Initializes the tool for moving module or robot with the mouse.
 	 */
 	public static void jButtonMoveActionPerformed() {	
 		if (chosenItem.equalsIgnoreCase("Module")){
 			try {
 				builderControl.setMoveModulePicker();
 			} catch (RemoteException e) {
 				throw new Error("Failed to initialize picker called Move Module, due to remote exception");
 			}
 		}else if (chosenItem.equalsIgnoreCase("Robot")){
 			//TODO Support moving robot 
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.MOVE.getHintText());
 	}
 
 	/**
 	 * Colors connectors of modules with color coding. 
 	 * @param toggleButtonColorConnetors 
 	 */
 	public static void jButtonColorConnectorsActionPerformed(JToggleButton toggleButtonColorConnetors) {	
 		try {
 			if (toggleButtonColorConnetors.isSelected()){
 				builderControl.colorConnectorsModules();
 			}else{
 				builderControl.restoreOriginalColorsConnectors();
 			}
 		} catch (RemoteException e) {
 			throw new Error("Failed color connectors of modules with color coding, due to remote exception");
 		}	
 	}
 
 	/**
 	 * Enables or disables the components in  toolbar because of missing implementation for robot.
 	 * TODO WILL CHANGE WITH ADDDING ROBOT SUPPORT.
 	 * @param jComboBoxEntity
 	 */
 	public static void jComboBoxEntityActionPerformed(JComboBox jComboBoxEntity) {
 		chosenItem = jComboBoxEntity.getSelectedItem().toString();
 		if (chosenItem.equalsIgnoreCase("Module") ){
 			//ConstructRobotTab.setEnabledGenericToolBar(true);
 		}else if(chosenItem.equalsIgnoreCase("Robot")){
 			//TODO  Support robot deletion, moving and coloring of connectors.
 			//ConstructRobotTab.setEnabledGenericToolBar(false);
 		}		
 	}
 
 	/**
 	 * Initializes the tool for rotating modules selected in simulation environment with opposite rotation. 
 	 * @param jmeSimulation, the physical simulation.     
 	 */	
 	public static void jButtonOppositeRotationActionPerformed() {
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.MODULE_OPPOSITE_ROTATION);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initialize picker called "+ ConstructionTools.MODULE_OPPOSITE_ROTATION.toString()+ " , due to remote exception");
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.OPPOSITE_ROTATION.getHintText());
 	}
 
 	/**
 	 * Standard rotation of the module chosen in GUI.
 	 */
 	public static String chosenStandardRotation;
 
 	/**
 	 * Initializes the tool for rotating initial module selected in simulation environment with standard rotations chosen in GUI. 
 	 * @param comboBoxStandardRotations, the GUI component.  
 	 */	
 	public static void jComboBoxStandardRotationsActionPerformed(JComboBox comboBoxStandardRotations) {
 		chosenStandardRotation = comboBoxStandardRotations.getSelectedItem().toString(); 
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.STANDARD_ROTATIONS, chosenStandardRotation);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ ConstructionTools.STANDARD_ROTATIONS.toString()+ ", due to remote exception");
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.STANDARD_ROTATIONS.getHintText()+ chosenStandardRotation +".");
 	}
 
 	/**
 	 * Initializes the tool for adding new modules on selected connectors of the module in interest in simulation environment. 	
 	 */
 	public static void jButtonOnSelectedConnectorActionPerformed() {
 		ConstructRobotTab.adaptToconstructionToolSelected();		
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called " + ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR.toString() + " , due to remote exception");
 		}		
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.ON_SELECTED_CONNECTOR.getHintText()); 
 	}
 
 	/**
 	 * Connector number chosen by user in GUI.
 	 */
 	private static int chosenConnectorNr;
 
 	/**
 	 * Initializes the tool for adding new modules on connector chosen in combo box(GUI) by user. Later user selects the module to apply it to.
 	 * @param comboBoxNrConnectorsConstructionTool, JComboBox containing the number of connectors.
 	 */
 	public static void jComboBoxNrConnectorsConstructionToolActionPerformed(JComboBox comboBoxNrConnectorsConstructionTool) {
 		ConstructRobotTab.adaptToconstructionToolSelected();
 		chosenConnectorNr = Integer.parseInt(comboBoxNrConnectorsConstructionTool.getSelectedItem().toString());
 		refreshSelectionColorConnectors();
 
 		try {
 			builderControl.colorConnectorsModules();
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.ON_CHOSEN_CONNECTOR_NR,chosenConnectorNr);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initiate picker called "+ ConstructionTools.ON_CHOSEN_CONNECTOR_NR.toString()+ ", due to remote exception");
 		}
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.ON_CHOSEN_CONNECTOR_NR.getHintText()+ chosenConnectorNr); 
 	}
 
 	/**
 	 * Refreshes selections of button for coloring connectors of modules.
 	 */
 	public static void refreshSelectionColorConnectors(){
 		ConstructRobotTab.getJToggleButtonColorConnetors().setSelected(true);
 		jButtonColorConnectorsActionPerformed(ConstructRobotTab.getJToggleButtonColorConnetors());
 	}
 
 	/**
 	 * Colors connectors of modules in case new module was added to simulation environment.
 	 */
 	public static void newModuleAdded(){
 		if (ConstructRobotTab.getJToggleButtonColorConnetors().isSelected()){
 			try {
 				builderControl.colorConnectorsModules();
 			} catch (RemoteException e) {
 				throw new Error("Failed to color connectors of modules, due to remote exception.");
 			}
 		}
 	}
 
 	/**
 	 * Initializes the tool for adding new modules to all connectors of selected module.
 	 */
 	public static void jButtonConnectAllModulesActionPerformed() {
 		ConstructRobotTab.adaptToconstructionToolSelected();
 
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.NEW_MODULES_ON_ALL_CONNECTORS);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ ConstructionTools.NEW_MODULES_ON_ALL_CONNECTORS.toString() + ", due to remote exception");
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.ON_ALL_CONNECTORS.getHintText());
 
 	}
 
 
 
 	/**
 	 * Initializes the tool for moving new module from connector to another connector.
 	 */
 	public static void jButtonJumpFromConnToConnectorActionPerformed() {
 		ConstructRobotTab.adaptToconstructionToolSelected();		
 
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.MOVE_MODULE_FROM_CON_TO_CON,0);
 		} catch (RemoteException e) {
 			throw  new Error ("Failed to initialize picker named as "+ ConstructionTools.MOVE_MODULE_FROM_CON_TO_CON.toString()+ " ,due to remote exception");
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.JUMP_FROM_CON_TO_CON.getHintText()); 
 	}
 
 	/**
 	 * Adapts Construct Robot Tab to the the type modular robot is simulation environment.
 	 * @param supportedModularRobot, supported modular robot.
 	 */
 	public static void adaptTabToSelectedModule(SupportedModularRobots supportedModularRobot){
 		ConstructRobotTab.setRadioButtonsEnabled(true);
 
 		switch(supportedModularRobot){
 		case ATRON:
 			ConstructRobotTab.getRadionButtonATRON().setSelected(true);	
 			break;
 		case ODIN:
 			ConstructRobotTab.getRadionButtonODIN().setSelected(true);
 			break;
 		case MTRAN:
 			ConstructRobotTab.getRadioButtonMTRAN().setSelected(true);
 			break;
 		case CKBOT_STANDARD:
 			ConstructRobotTab.getRadionButtonCKBOTSTANDARD().setSelected(true);
 			break;
 		default: throw new Error("Modular robot named as "+ supportedModularRobot.toString() + "is not supported yet" );
 		}	
 		adaptTabToChosenMR(supportedModularRobot,true);
 		ConstructRobotTab.setRadioButtonsEnabled(false);
 		//constructionToolSelected();HERE
 
 	}
 
 	
 	/**
 	 * Adapts Construct Robot Tab to the the type of first module in simulation environment.
 	 */
 	public static void adaptTabToModuleInSimulation(){
 		int amountModules =0;		
 		try {
 			amountModules =  builderControl.getIDsModules().size();
 		} catch (RemoteException e) {
 			throw new Error("Failed to identify amount of modules in simulation environment, due to remote exception");
 		}
 
 		if (amountModules>2){
 			/*Adapt to first module type*/
 			String modularRobotName ="";
 			try {
 				modularRobotName = builderControl.getModuleType(0);
 			} catch (RemoteException e) {
 				throw new Error ("Failed to identify the type of the first module in simulation environment, due to remote exception.");
 			}
 			
			switch(SupportedModularRobots.getConsistentMRName(modularRobotName)){
 			
			}
 			
 			
 			if (modularRobotName.toUpperCase().contains(SupportedModularRobots.ATRON.toString())){
 				adaptTabToChosenMR(SupportedModularRobots.ATRON,false);
 				chosenMRname = SupportedModularRobots.ATRON;
 			} else if (modularRobotName.toUpperCase().contains(SupportedModularRobots.ODIN.toString())){
 				adaptTabToChosenMR(SupportedModularRobots.ODIN,false);
 				chosenMRname = SupportedModularRobots.ODIN;
 			} else if (modularRobotName.toUpperCase().contains(SupportedModularRobots.MTRAN.toString())){
 				adaptTabToChosenMR(SupportedModularRobots.MTRAN,false);
 				chosenMRname = SupportedModularRobots.MTRAN;
			}else if(modularRobotName.toUpperCase().contains(SupportedModularRobots.CKBOT_STANDARD.toString())){
 				adaptTabToChosenMR(SupportedModularRobots.CKBOT_STANDARD,false);
 				chosenMRname = SupportedModularRobots.CKBOT_STANDARD;
 			}else{
 				throw new Error ("Modular robot type "+modularRobotName+ "is not supported yet" );
 			}
 			ConstructRobotTab.adaptToconstructionToolSelected();	
 
 
 			try {
 				/*Set default construction tool to be "On selected  connector"*/
 				builderControl.setConstructionToolSpecPicker(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR);
 			} catch (RemoteException e) {
 				throw new Error("Failed to initate picker called " + ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR + " , due to remote exception");
 			}
 		}
 
 	}	
 
 	/**
 	 * Removes current robots in simulation environment and enables tab elements for constructing new robot.
 	 */
 	public static void jButtonStartNewRobotActionPerformed() {
 		ConstructRobotTab.setVisibleFirstModuleOperations(true);
 		ConstructRobotTab.setRadioButtonsEnabled(true);
 		ConstructRobotTab.setEnabledAllToolBars(false);	
 		ConstructRobotTab.getButtonGroupModularRobots().clearSelection();
 		ConstructRobotTab.getJToggleButtonColorConnetors().setSelected(false);
 
 		try {
 			builderControl.removeAllModules();
 		} catch (RemoteException e) {
 			throw new Error("Failed to to remove all modules, due to remote exception.");
 		}		
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.START_NEW_ROBOT.getHintText());
 	}
 
 	/**
 	 * Initializes the tool for varying the properties of modules (or types of modules in Odin case) selected in simulation environment.
 	 * Is specific to each modular robot.
 	 */
 	public static void jButtonVariateModulePropertiesActionPerformed() {	
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.VARIATE_MODULE_OR_PROPERTIES);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ ConstructionTools.VARIATE_MODULE_OR_PROPERTIES.toString() + ", due to remote exception");
 		}	
 		/*if (chosenMRname.equals(SupportedModularRobots.ODIN)){
 			ConstructRobotTab.setEnabledConstructionToolsToolBar(false);
 		}*/
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.VARIATE_MODULE_PROPERTIES.getHintText());
 	}
 
 	/**
 	 * Initializes the tool for changing module rotation with each selection of module in simulation environment.
 	 */
 	public static void jButtonStandardRotationsLoopActionPerformed() {
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.AVAILABLE_ROTATIONS);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called"+ConstructionTools.AVAILABLE_ROTATIONS.toString() +", due to remote exception");
 		}
 
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintsConstructRobotTab.AVAILABLE_ROTATIONS.getHintText());
 	}
 
 	/**
 	 * Adapts Construct Robot tab to the tool chosen by user. To be more precise disables and enables relevant components of the tab. 
 	 * @param chosenTool,the tool chosen by the user in Construct Robot tab.
 	 */
 	public static void adaptConstructRobotTabToChosenTool(ConstructionTools chosenTool)throws RemoteException{
 
 		switch(chosenTool){
 		case MOVE_MODULE_FROM_CON_TO_CON:
 			//ConstructRobotTab.setEnabledButtonsArrows(true);	
 			break;
 		case NEW_MODULE_ON_SELECTED_CONNECTOR:
 			ConstructRobotTab.adaptToconstructionToolSelected();
 			break;			
 		default: throw new Error ("The tool named as "+ chosenTool.toString() + " is not supported yet.");
 		}
 	}
 
 	/**
 	 * The global ID of the module selected in simulation environment.
 	 */
 	private static int selectedModuleID;
 
 	/**
 	 * Sets the global ID of the module selected in simulation environment.
 	 * @param selectedModuleID, the global ID of the module selected in simulation environment.
 	 */
 	public static void setSelectedModuleID(int selectedModuleID){
 		ConstructRobotTabController.selectedModuleID = selectedModuleID;
 	}
 }
