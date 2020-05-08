 package com.lanl.application.treePruner.applet;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import javax.swing.JLabel;
 
 import org.forester.archaeopteryx.ControlPanel;
 import org.forester.archaeopteryx.MainFrame;
 
 import com.lanl.application.treePruner.custom.data.WorkingSet;
 
 public class AutoSave {
 	private static long base;
 	private static String autoSaveTimeTEXT = "Last Autosaved at: ";
 	private  JLabel autoSaveTimeLabel= new JLabel(autoSaveTimeTEXT,JLabel.CENTER);
 	private static boolean autoSaved=false;
 	private static String autoSaveDateTime = "Not Saved Yet";
 	
 	public void addAutoSaveLabel(ControlPanel controlPanel){
 		autoSaveTimeLabel.setText(autoSaveTimeTEXT + autoSaveDateTime);
 		autoSaveTimeLabel.setVerticalTextPosition(JLabel.TOP);
 		autoSaveTimeLabel.setHorizontalTextPosition(JLabel.CENTER);
 		controlPanel.addLabel(autoSaveTimeLabel);
 		refreshAutoSaveLabel();
 	}
 	
 	public void refreshAutoSaveLabel(){
 		if(autoSaved){
 			autoSaveDateTime = currentDateTime();
 			autoSaveTimeLabel.setText(autoSaveTimeTEXT + autoSaveDateTime );
 		    refreshAutoSaveTimeInAllFrames();
 		}
 		else{}
 		
 	}
 	
	private void refreshAutoSaveTimeInAllFrames(){
 		for(MainFrame mf : SubTreePanel.mainFrames){
 			mf.get_main_panel().get_control_panel().controlPanelAdditions.autoSave.autoSaveTimeLabel.setText(autoSaveTimeTEXT + autoSaveDateTime);
 		}
 		SubTreePanel.mainAppletFrame.get_main_panel().get_control_panel().
 		controlPanelAdditions.autoSave.autoSaveTimeLabel.setText(autoSaveTimeTEXT + autoSaveDateTime);
 	}
 	
 	private static String currentDateTime() {
         String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
         Calendar cal = Calendar.getInstance();
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
         return sdf.format(cal.getTime());
 
     }
 	
 	private static boolean timeToAutoSave(){
 		if(base == 0){
 			base = System.currentTimeMillis();
 		}
 		long delay = System.currentTimeMillis() - base;
 		if(delay >= 600000){    //10 minutes =600000 ms
 		//if(delay >= 20000){
 			base=System.currentTimeMillis();
 			delay=0;
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	public static void doAutoSave(ControlPanelAdditions controlPanelAdditions, WorkingSet ws,TreePrunerCommunicationMessageWarningWindow warningWindow){
 		if(timeToAutoSave()){
 			String accToRemove = ws.getACCasString();   //acc ==accToRemove //get_rem_acc == toCommunicateWithServer && getACCasString
 			String returnedString="";
             
             if(ws.toCommunicateWithServer()){
             	 warningWindow = new TreePrunerCommunicationMessageWarningWindow("Your Action is being performed" +
                         " Please wait...","                                                  " +
                         		"                                                              ");
             	returnedString = controlPanelAdditions.saveToFileComm(accToRemove);
             	ws.copyAccToRememberAcc();
             //	ws.clear("");
             	
            // 	ws.clearSavedStuff();  // not saving // only remembering (remACC - autosave)
            // 	ws.copyStuffToSavedStuff();  // not saving // only remembering (remACC - autosave)
             	
             	//System.out.println("++++++++++++_________________"+savedACC.toString() + savedREMOVE_ALL.toString());
             	destroyWarningWindow(warningWindow);
             	autoSaved = true;
             }
             else if(!ws.toCommunicateWithServer()){
             	
             }
             else{
             	//Don't do anything if all arrays (keep, remove and revert) are empty
             }
             
             //now clear out the arrays for sequences selected for next session
                
          //   ws.clearListsForNextSession();   //This will clear out all the lists (node lists) which we dont want // Also we dont want the color nodes 
             //to be copied to the remove all nodes which will gray them.
             if(returnedString.equals("Can't open file")){
             	System.out.println("AUTO SAVE \n");
             	System.out.println(controlPanelAdditions.toString() + "\n Server reurned: Can't open file");
             }
             else if (returnedString.equals("File successfully opened and written")){
             	System.out.println("AUTO SAVE \n");
             	System.out.println(controlPanelAdditions.toString() + "\n Server reurned: File successfully opened and written");
             }
             
 		}
 		else{
 			autoSaved = false;
 		}
 	}
 	private static void destroyWarningWindow(TreePrunerCommunicationMessageWarningWindow warningWindow){
         if(warningWindow!=null){
         	warningWindow.close();
                 
         }
     }
 	
 	public static void resetAutoSave(){   //CALL from EXIT 
 		base = 0;
 		autoSaveDateTime = "Not Saved Yet";
 	}
 }
