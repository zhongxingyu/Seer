 package ussr.aGui.tabs.controllers;
 
 import java.awt.GridBagConstraints;
 import java.awt.Label;
 import java.rmi.RemoteException;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonModel;
 import javax.swing.JToggleButton;
 
 import ussr.aGui.designHelpers.hintPanel.HintPanelTypes;
 import ussr.aGui.enumerations.hintpanel.HintsAssignControllersTab;
 import ussr.aGui.tabs.constructionTabs.AssignControllerTab;
 import ussr.aGui.tabs.constructionTabs.AssignableControllers;
 
import ussr.builder.controllerAdjustmentTool.AssignControllerTool;
import ussr.builder.controllerAdjustmentTool.ControllerStrategy;
 import ussr.builder.enumerations.SupportedModularRobots;
 
 import ussr.builder.helpers.FileDirectoryHelper;
 
 public class AssignControllerTabController extends TabsControllers {
 
 	/**
 	 * Container for keeping all classes of controllers extracted from package "ussr.builder.controllerAdjustmentTool";
 	 */
 	private static  Vector<String> classesOfControllers ;
 
 	/**
 	 * Temporary container for keeping classes of controllers filtered out for specific modular robot from above package.
 	 */
 	private static  Vector<String> tempClassesOfControllers =  new Vector<String> ()  ;
 
 	/**
 	 * The name of the package where all behaviors are stored for interactive adjustment of controller.
 	 */
 	private static final String packageName = "ussr.builder.controllerAdjustmentTool";
 
 	/**
 	 * Loads all existing names of controllers from package ussr.builder.controllerAdjustmentTool and filters
 	 * out the ones for selected button (modular robot name).
 	 * @param radionButton, the radio button representing modular robot name.
 	 */
 	public static void jButtonGroupActionPerformed(javax.swing.AbstractButton radionButton){
 
 		
 
 		boolean modularRobotNameExists = false;
 		SupportedModularRobots[] supportedModularRobots = SupportedModularRobots.values();
 		for (int buttonTextItem=0;buttonTextItem<supportedModularRobots.length;buttonTextItem++){
 
 			if (radionButton.getText().equals(supportedModularRobots[buttonTextItem].getUserFriendlyName())){
 				String modularRobotName= SupportedModularRobots.getModularRobotSystemName(supportedModularRobots[buttonTextItem].getUserFriendlyName()).toString();
 				if (AssignControllerTab.getJToggleButtonEditValues().isSelected()){
 					updateList(AssignControllerTab.getJListAvailableControllers(),AssignableControllers.getAllUserFrienlyNamesForRobot(SupportedModularRobots.valueOf(modularRobotName)));
 				}else{
 					loadExistingControllers(AssignControllerTab.getJListAvailableControllers());
 					updateList(AssignControllerTab.getJListAvailableControllers(),filterOut(modularRobotName));
 				}
 				modularRobotNameExists =true;
 			}
 		}
 
 		if (modularRobotNameExists==false){
 			throw new Error ("Not supported modulal robot name: "+ radionButton.getText());
 		}
 		
 	}
 
 	/**
 	 * Extracts and loads the names of controllers existing in the package "ussr.builder.controllerAdjustmentTool" into jList
 	 * @param jList1, the component in GUI.
 	 */
 	public static void loadExistingControllers(javax.swing.JList jList1){
 
 		Class[] classes = null;
 		try {
 			classes = FileDirectoryHelper.getClasses(packageName);
 		} catch (ClassNotFoundException e) {
 			throw new Error ("The package named as: "+ packageName + "was not found in the directory ussr.builder.controllerAdjustmentTool");			
 		}		
 
 		/*Loop through the classes and take only controllers, but not the classes defining the tool*/
 		classesOfControllers = new Vector<String>();
 		for (int i=0; i<classes.length;i++){
			if (classes[i].toString().contains(AssignControllerTool.class.getName())||classes[i].toString().contains(ControllerStrategy.class.getName())){
 				//do nothing	
 			}else{
 				classesOfControllers.add(classes[i].toString().replace("class "+packageName+".", ""));
 			}
 		}			
 		updateList(jList1,classesOfControllers);
 		/*Update the list with newly loaded names of controllers*/
 	}
 
 
 	/**
 	 * Updates the list with the names of controllers.
 	 * @param jList1,the component in GUI. 
 	 * @param controllers, vector of controllers.
 	 */
 	@SuppressWarnings("serial")
 	public static void updateList(javax.swing.JList jList1,final Vector<String> controllers ){
 		jList1.setModel(new javax.swing.AbstractListModel() {
 			Object[] strings =  controllers.toArray();
 			public int getSize() { return strings.length; }
 			public Object getElementAt(int i) { return strings[i]; }
 		});		
 	}
 
 
 	/**
 	 * Filters out the names of controller for specific modular robot name.
 	 * @param modularRobotName, modular robot name.
 	 * @return tempClassesOfControllers, array of controllers for specific modular robot name.
 	 */
 	public static Vector<String> filterOut(String modularRobotName){
 		tempClassesOfControllers.removeAllElements();
 		for (int index=0; index<classesOfControllers.size();index++){
 			if (classesOfControllers.get(index).contains(modularRobotName)){
 				tempClassesOfControllers.add(classesOfControllers.get(index));
 			}
 		}
 		return tempClassesOfControllers;
 	}
 
 	/**
 	 * Initializes the tool for assigning controller chosen by user in GUI component. 
 	 * @param toggleButtonEditValuesIsSelected
 	 * @param jList1,the component in GUI. 
 	 */
 	public static void jListAvailableControllersMouseReleased(javax.swing.JList jList1) {
 		String canonicalName = packageName+"."+jList1.getSelectedValue();
 
 		if (AssignControllerTab.getJToggleButtonEditValues().isSelected()){
 			AssignableControllers assignableController = AssignableControllers.getControllerSystemName(jList1.getSelectedValue().toString());
 			canonicalName= assignableController.getClassName().getCanonicalName();
 			AssignControllerTab.getJPanelEditValue().removeAll();
 
 			GridBagConstraints gridBagConstraintsEditValue = new GridBagConstraints();
 			gridBagConstraintsEditValue.fill = GridBagConstraints.HORIZONTAL;
 			gridBagConstraintsEditValue.gridx = 0;
 			gridBagConstraintsEditValue.gridy = 0;	
 
 			AssignControllerTab.getJPanelEditValue().add(new Label(jList1.getSelectedValue().toString()),gridBagConstraintsEditValue);
 
 			gridBagConstraintsEditValue.fill = GridBagConstraints.HORIZONTAL;
 			gridBagConstraintsEditValue.gridx = 0;
 			gridBagConstraintsEditValue.gridy = 1;	
 
 			AssignControllerTab.getJPanelEditValue().add(assignableController.getValueEditor(),gridBagConstraintsEditValue);
 			AssignControllerTab.getJPanelEditValue().revalidate();
 			AssignControllerTab.getJPanelEditValue().repaint();
 
 		}
 		try {
 			builderControl.setAdjustControllerPicker(canonicalName);			
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ "AssignControllerTool" + ", due to remote exception");
 		}
 	}
 
 	/**
 	 * Adapts Assign Behaviors Tab to the the type of first module in simulation environment.
 	 * TODO MAKE IT MORE GENERIC BY MEANS OF IDENTIFYING THE LAST TYPE OF MODULE IN XML FILE
 	 * OR SOMETHING SIMILLAR.
 	 */
 	public static void adaptTabToModuleInSimulation(){
 		int amountModules =0;		
 		try {
 			amountModules =  builderControl.getIDsModules().size();
 		} catch (RemoteException e) {
 			throw new Error("Failed to identify amount of modules in simulation environment, due to remote exception");
 		}
 
 		if (amountModules>0){
 			/*	Adapt to first module*/
 			String modularRobotName ="";
 			try {
 				modularRobotName = builderControl.getModuleType(0);
 			} catch (RemoteException e) {
 				throw new Error ("Failed to identify the type of the first module in simulation environment, due to remote exception.");
 			}
 
 			if (modularRobotName.toUpperCase().contains(SupportedModularRobots.ATRON.toString())){
 				jButtonGroupActionPerformed(AssignControllerTab.getRadionButtonATRON());
 				AssignControllerTab.getRadionButtonATRON().setSelected(true);
 			} else if (modularRobotName.toUpperCase().contains(SupportedModularRobots.ODIN.toString())){
 				jButtonGroupActionPerformed(AssignControllerTab.getRadioButtonODIN());
 				AssignControllerTab.getRadioButtonODIN().setSelected(true);
 			} else if (modularRobotName.toUpperCase().contains(SupportedModularRobots.MTRAN.toString())){
 				jButtonGroupActionPerformed(AssignControllerTab.getRadioButtonMTRAN());
 				AssignControllerTab.getRadioButtonMTRAN().setSelected(true);
 			}else if(modularRobotName.toUpperCase().contains(SupportedModularRobots.CKBOT_STANDARD.toString())){
 				jButtonGroupActionPerformed(AssignControllerTab.getRadionButtonCKBOTSTANDARD());
 				AssignControllerTab.getRadionButtonCKBOTSTANDARD().setSelected(true);
 			}		
 		}
 	}
 
 
 	public static void updateHintPanel(HintPanelTypes hintPanelTypes,String text){
 		AssignControllerTab.getHintPanel().setType(hintPanelTypes);
 		AssignControllerTab.getHintPanel().setText(text);
 	}
 
 	/**
 	 * Changes the view of controller names 
 	 * @param toggleButtonEditValues
 	 */
 	public static void jToggleButtonEditValuesActionPerformed(JToggleButton toggleButtonEditValues) {
 		if (toggleButtonEditValues.isSelected()){
 			AssignControllerTab.getJPanelEditValue().setVisible(true);
 		}else{
 			AssignControllerTab.getJPanelEditValue().setVisible(false);
 		}	
 		
 		ButtonModel selectedButton = AssignControllerTab.getButtonGroup().getSelection();
 		AssignControllerTab.getButtonGroup().clearSelection();		
 		AssignControllerTab.getButtonGroup().setSelected(selectedButton, true);
 		
 		Enumeration<AbstractButton> buttonGroupButtons= AssignControllerTab.getButtonGroup().getElements();
 		
 		for (int buttonNr=0;buttonNr<AssignControllerTab.getButtonGroup().getButtonCount();buttonNr++){
 			AbstractButton button  = buttonGroupButtons.nextElement();
 			if (button.isSelected()){
 				jButtonGroupActionPerformed(button);
 			}
 		}	
 	}
 
 	public static void activateAssignmentTool(AssignableControllers assignableController){
 		try {
 			builderControl.setAdjustControllerPicker(assignableController.getClassName().getCanonicalName());			
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ "AssignControllerTool" + ", due to remote exception");
 		}
 	}		
 }
 
