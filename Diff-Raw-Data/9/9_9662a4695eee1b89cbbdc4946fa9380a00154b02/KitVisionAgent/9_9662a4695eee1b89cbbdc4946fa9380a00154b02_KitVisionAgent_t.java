 package agents;
 import gui.GUI_KitStand;
 
 import java.util.*;
 
 import state.FactoryState;
 
 import agents.include.*;
 import agents.interfaces.*;
 
 public class KitVisionAgent extends Agent implements KitVision {
 
     /*** Data Structures **/
 
    	KitRobot kitRobot;
 	KitStand kitStand;
 	GUI_KitStand guiKitStand;
 	public boolean inspecting;
 	public boolean pass = true;
 	public boolean failInspection = false;
 	String name;
 	Map<String,Integer> missingParts = new HashMap<String,Integer>();
 	List<List<Part>> masterListOfMissingParts;	
 
     /*** Constructor **/
     
     public KitVisionAgent(String name, KitStand stand) {
     	super();
     	this.name = name;
         inspecting = false;
         kitStand = stand;
         masterListOfMissingParts = new ArrayList<List<Part>>();
     }
 
     /*** Messages **/
 
     /* 
      *  Message to have vision inspect kit
      *  Source: KitRobot
      */ 
     public void msgKitReadyForInspection()
     {
     	inspecting = true;
     	print("received message that kit is ready for inspection");
     	stateChanged();
     }
     
     /* receives kit configuration from GUI to mess up the kit config so that
      * the inspection fails
      */
     public void msgBreakKit(Map<String,Integer> brokenConfig){
     	print("received message to break kit");
     	if (brokenConfig == null){
     		System.out.println("ERROR: brokenConfig is null");
     	}else{
     		System.out.println("BrokenConfig: " + brokenConfig);
     	}
  
     	List <Part> incompleteParts = new ArrayList<Part>();
     	for (String partType : brokenConfig.keySet()){
     		int qty = brokenConfig.get(partType);
     		for (int i = 0; i < qty; i++){
     			Part p = new Part(partType);
     			incompleteParts.add(p);
     		}
     	}
     	masterListOfMissingParts.add(incompleteParts);
     }
 
     public boolean pickAndExecuteAnAction() {
 
         if(inspecting)
         {
 	    	inspecting = false;
         	pass = inspectKit();
            kitRobot.visionLock().release();
         	return true;
         }
         return false;
     }
 
     /*** Actions ***/
 
     public boolean inspectKit()
     {
 		List<Part> incompleteParts = new ArrayList<Part>();
     	Kit kit = kitStand.inspectKit();
     	boolean passed = true;
     	
     	DoInspectKit();
     	    	
     	if (masterListOfMissingParts.size() > 0){
     		incompleteParts = masterListOfMissingParts.remove(0);
     		kit.parts.clear();
     		for (Part p : incompleteParts){
     			kit.parts.add(p);
     		}
     		if (guiKitStand != null)
     		DoGiveUpdatedPartsListToGUIKitStand(kit.parts);
     	}
     	
         for (String partType : kit.config.keySet()){
         	int qty = kit.config.get(partType);
      
         	for (Part part : kit.parts){
         		if (part.name.equals(partType)){
         			qty--;
         		}        		
         	}
         	
         	if (qty > 0){
         		missingParts.put(partType,qty);        		
         		passed = false;
         	}
         }
                 
         stateChanged();
         if(passed){
         	print("Inspection passed");
         	return true;
         }
         else{
         	print("Inspection failed.  Missing parts");
         	FactoryState.out.println("Kit FAILED inspection");
         	return false;
         }
     }
     
     public Map<String,Integer> getMissingParts(){
     	return missingParts;
     }
     
     //extra
     public void setKitRobot(KitRobot r){
         kitRobot = r;
     }
     public String getName(){ return name; }
     
     public boolean getInspectionResults() { return pass; }
     
     public void setGUIKitStand(GUI_KitStand stand){
     	guiKitStand = stand;
     }
     
     public void printConfigs(Map<String,Integer> broken, Map<String,Integer> expected){
     	print("Expected Configuration:  ");
         for (String partType : expected.keySet()){
         	int qty = expected.get(partType);
         	print(qty + " of " + partType);
         }
         print("Actual(Broken) Configuration:  ");
         for (String partType : broken.keySet()){
         	int qty = broken.get(partType);
         	print(qty + " of " + partType);
         }
     }
     
     //gui
     private void DoInspectKit()
     {
     	//print("gui camera inspected kit");
     	if(kitRobot.getGUIKitStand() != null){
 	    	kitRobot.getGUIKitStand().camera.DoTakePicture();
 	    	try {
 				  Thread.sleep(2000);    // delay 2 seconds
 				}
 			catch (Exception e) {}
     	}
     }
     public void DoGiveUpdatedPartsListToGUIKitStand(List<Part> newParts){
     	guiKitStand.DoBreakInspectionKit(newParts);
     }
 
 
 }
