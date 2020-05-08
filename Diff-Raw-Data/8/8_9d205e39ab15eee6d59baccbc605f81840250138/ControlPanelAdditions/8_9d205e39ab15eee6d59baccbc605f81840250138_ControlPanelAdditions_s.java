 package com.lanl.application.treePruner.applet;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import org.forester.archaeopteryx.ControlPanel;
 import org.forester.archaeopteryx.MainFrame;
 import org.forester.atv.ATVapplet;
 
 
 import com.Extropia.net.JavaCGIBridge;
 import com.Extropia.net.JavaBridge;
 import com.Extropia.net.JavaBridgeTimeOutException;
 import com.Extropia.net.JavaCGIBridgeTimeOutException;
 
 import com.lanl.application.treePruner.custom.data.WorkingSet;
 
 public class ControlPanelAdditions {
 	
 	private WorkingSet ws = new WorkingSet();;
 	
 	private JButton save_to_file;
 	private JButton delete_from_db;
 	private JButton discard;
 	private JButton undo;
 	private JButton refresh;
 	public static String lastAction="";
 	public JLabel subTreeWindowHierarchy = new JLabel(subTreeWindowHierarchyTEXT,JLabel.CENTER);;
 	public static String subTreeWindowHierarchyTEXT ="Subtree: ";
 	static TreePrunerCommunicationMessageWarningWindow warningWindow;
 	public ControlPanel controlPanel;
 	public AutoSave autoSave = new AutoSave();
 	//public ControlPanelAdditions controlPanelExtras;
 	
 	public ControlPanelAdditions() {
 		
 	}
 	public ControlPanelAdditions(ControlPanel cp) {
 		this.controlPanel = cp;
 		save_to_file = new JButton("Save");
 		save_to_file
 				.setToolTipText("Save the net result of all pruning actions until now.");
 		delete_from_db = new JButton("Commit changes; finish session");
 		delete_from_db
 				.setToolTipText("Implement all pruning actions in your working set; close applet.");
 		discard = new JButton("Discard all");
 		discard
 				.setToolTipText("Discard all pruning actions and restore tree to initial state.");
 		refresh = new JButton("Refresh");
 		refresh.setToolTipText("Refreshes the tree in all open applet windows");
 		undo = new JButton("Discard recent");
 		undo
 				.setToolTipText("Discard all pruning actions since most recent save.");
 
 	}
 
 	public void addTreePrunerButtons() {
 		final JLabel spacer = new JLabel("");
 		final JLabel spacer2 = new JLabel("");
 		final JLabel spacer3 = new JLabel("");
 		controlPanel.addLabel(spacer2);
 		controlPanel.add_additional_JButton(refresh, controlPanel);
 		
 		controlPanel.addLabel(spacer);
 		controlPanel.addLabel(spacer2);
 		controlPanel.add_additional_JButton(save_to_file, controlPanel);
 		final JPanel discard_panel = new JPanel(new GridLayout(1, 2, 0, 0));
 		discard_panel.setBackground(controlPanel.getBackground());
 		controlPanel.addPanel(discard_panel);
 		controlPanel.add_additional_JButton(discard, discard_panel);
 		controlPanel.add_additional_JButton(undo, discard_panel);
 		controlPanel.add_additional_JButton(delete_from_db, controlPanel);
 		
 	//	controlPanel.addLabel(spacer3);
 
 	}
 	
 	public void addSubTreeWindowHierarchyLabel(){
 		String hierarchyNumber="1.";
 		for(int i = 0;i <= SubTreePanel.subTreeHierarchy.size()-1;i++){
 			hierarchyNumber+= SubTreePanel.subTreeHierarchy.get(i).toString();
 			if(i==SubTreePanel.subTreeHierarchy.size()-1){
 				
 			}
 			else{
 				hierarchyNumber+=".";
 			}
 		}
 		if(hierarchyNumber=="1."){
 			hierarchyNumber = "1";
 		}
 		subTreeWindowHierarchy.setText(subTreeWindowHierarchyTEXT + hierarchyNumber);
 		subTreeWindowHierarchy.setVerticalTextPosition(JLabel.TOP);
 		subTreeWindowHierarchy.setHorizontalTextPosition(JLabel.CENTER);
 		controlPanel.addLabel(subTreeWindowHierarchy);
 	}
 	public void callAutosaveToAdd(){
 		AutoSave.doAutoSave(this, ws, warningWindow);
 		autoSave.addAutoSaveLabel(controlPanel);
 	}
 	 public void callAutoSaveToRefresh(){
 		AutoSave.doAutoSave(this, ws, warningWindow);
 		autoSave.refreshAutoSaveLabel(); 
 	 }
 
 	public void addTreePrunerButtonFunctions(ActionEvent e){
 		if ( e.getSource() == refresh ) {
         	for(MainFrame o: SubTreePanel.mainFrames){
         		if(o!=null)
         			//paint all slave windows
         			o.repaintPanel();
         	}
         	//paint the master window
         	SubTreePanel.mainAppletFrame.repaintPanel();
         }
 		
 		//#######################
 		
 		else if ( e.getSource() == save_to_file ) {
 			String accToRemove = ws.getACCasString();   //acc ==accToRemove //get_rem_acc == toCommunicateWithServer && getACCasString
 			String returnedString="";
             
             if(ws.toCommunicateWithServer()){
             	 warningWindow = new TreePrunerCommunicationMessageWarningWindow("Your Action is being performed" +
                         " Please wait...","                                                  " +
                         		"                                                              ");
             	returnedString = saveToFileComm(accToRemove);
             	ws.copyAccToRememberAcc();
             //	ws.clear("");
             	
             	ws.clearSavedStuff();
             	ws.copyStuffToSavedStuff();
             	
             	//System.out.println("++++++++++++_________________"+savedACC.toString() + savedREMOVE_ALL.toString());
             	destroyWarningWindow();
             }
             else if(!ws.toCommunicateWithServer()){
             	
             }
             else{
             	//Don't do anything if all arrays (keep, remove and revert) are empty
             }
             
             //now clear out the arrays for sequences selected for next session
                
             ws.clearListsForNextSession();
             if(returnedString.equals("Can't open file")){
             	System.out.println("SAVE PRESSED \n");
             	System.out.println(this.toString() + "\n Server reurned: Can't open file");
             }
             else if (returnedString.equals("File successfully opened and written")){
             	System.out.println("SAVE PRESSED \n");
             	System.out.println(this.toString() + "\n Server reurned: File successfully opened and written");
             }
         }
         else if(e.getSource() == delete_from_db){
         	if(!SubTreePanel.mainFrames.isEmpty()|| SubTreePanel.mainFrames.size()>=1 || 
         			   SubTreePanel.sub_frame_count!=0){   //There are slave windows open
             	Object[] options = {"Yes",
     								"No"};
             	int n = JOptionPane.showOptionDialog(null,"Commiting changes will close your applet session \n  "
             												+ "Are you sure you would like to continue?",
             												"Terminate Interrupted",
             												JOptionPane.YES_NO_OPTION,
             												JOptionPane.QUESTION_MESSAGE,
             												null,
             												options,
             												options[1]);
             	
         	
             	if(n==JOptionPane.YES_OPTION){
 	           // 	ATVapplet atva = new ATVapplet();
 	           //     _atvappletframe=atva.get_applet_frame();  
             		String accToRemove = ws.getACCasString();
 	                String returnedString="";
 	                
 	                if (ws.toCommunicateWithServer()) {
 	                	warningWindow = new TreePrunerCommunicationMessageWarningWindow("Your Action is being performed" +
 	                            " Please wait...","                                                  " +
 	                            		"                                                              ");
 						returnedString=deleteFromDbComm(accToRemove);
 						ws.copyAccToRememberAcc();
 						destroyWarningWindow();
 						if(returnedString.equals("Accessions successfully deleted")){
 		                	JOptionPane.showMessageDialog( null, "Your Seqences were successfully deleted","Delete Confirmation",JOptionPane.INFORMATION_MESSAGE);
 		                	
 		                }
 		                else if (returnedString.equals("Nothing deleted")){
 		                	JOptionPane.showMessageDialog( null, "Your Seqences were not deleted.\n Please make sure " +
 		                			"that the sequences are not already deleted. \n " +
 		                			"Please contact flu@lanl.gov if your problem persists.","Delete Confirmation",JOptionPane.INFORMATION_MESSAGE );
 		                }
 						
 	                }
 	            	//SIGM START
 	                
             
                     for(MainFrame o : SubTreePanel.mainFrames){
 	                	if(o!=null){
 	                		o.closeOnDelete();
 	                	}
 	                }
 	                if(SubTreePanel.mainAppletFrame!=null){
 	                	SubTreePanel.mainAppletFrame.closeOnDelete();
 	                }
 		            
 		            SubTreePanel.clearListsOnClose();
 		 //           stored_levels.clear();
 	            
             /*
 		            System.out.println("9999999999999999999998888888888888888 "+ATVtreePanel.atvFrames);
 		            System.out.println("9999999999999999999998888888888888888 "+ATVtreePanel._phylogenies);
 		            System.out.println("9999999999999999999998888888888888888 "+ATVtreePanel._phylogenies_subtree);
 		            System.out.println("9999999999999999999998888888888888888 "+ATVtreePanel.sub_frame_count);
 		            System.out.println("9999999999999999999998888888888888888 "+ATVtreePanel._subtree_index);*/
 		            //if(_atvappletframe!=null){
 		            //	_atvappletframe.close();
 		            //}
             	}
             	else if(n==JOptionPane.NO_OPTION){ 
             		//dont do anything as user has clicked dont commit 
             		
             	}
 	        }
             else {   //Only the parent Window is open
             	String accToRemove = ws.getACCasString();
                 String returnedString="";
                 if (ws.toCommunicateWithServer()) {
                 	
                 	warningWindow = new TreePrunerCommunicationMessageWarningWindow("Your Action is being performed" +
                             " Please wait...","                                                  " +
                             		"                                                              ");
 					returnedString = deleteFromDbComm(accToRemove);
 					ws.copyAccToRememberAcc();
 					destroyWarningWindow();
 					if(returnedString.equals("Accessions successfully deleted")){
 	                	JOptionPane.showMessageDialog( null, "Your Seqences were successfully deleted","Delete Confirmation",JOptionPane.INFORMATION_MESSAGE);
 	                	
 	                }
 	                else if (returnedString.equals("Nothing deleted")){
 	                	JOptionPane.showMessageDialog( null, "Your Seqences were not deleted.\n Please make sure " +
 	                			"that the sequences are not already deleted. \n " +
 	                			"Please contact flu@lanl.gov if your problem persists.","Delete Confirmation",JOptionPane.INFORMATION_MESSAGE );
 	                }
 					
                 }
             	if(SubTreePanel.mainAppletFrame!=null){
             		SubTreePanel.mainAppletFrame.closeOnDelete();
                 }
             }
 	    }//end of delete from ws  //SIGMA END
         else if(e.getSource() == discard){
             String returnedString = "";
             warningWindow = new TreePrunerCommunicationMessageWarningWindow("Your Action is being performed" +
                     " Please wait...","                                                  " +
                     		"                                                              ");
             returnedString = discardComm();
             destroyWarningWindow();
             if(returnedString.equals("success")){
             	System.out.println("Discard PRESSED \n");
             	System.out.println(this.toString() + "\n Server reurned: file successfully deleted");
             }
             else if (returnedString.equals("fail")){
             	System.out.println("Discard PRESSED \n");
             	System.out.println(this.toString() + "\n Server reurned: Failed to delete the file because no file was present or permissions");
             }	
 			
             ws.clearAllLists();
             AutoSave.resetAutoSave();
             
            controlPanel.displayed_phylogeny_mightHaveChanged(true);
             ws.clear("rm_all");
             /*if(s.equals("Accessions successfully deleted")){
             	JOptionPane.showMessageDialog( null, "Your Seqences were successfully deleted","Delete Confirmation",JOptionPane.INFORMATION_MESSAGE);
             }
             else if (s.equals("Nothing deleted")){
             	JOptionPane.showMessageDialog( null, "Your Seqences were not deleted.\n Please make sure " +
             			"that the sequences are not already deleted. \n " +
             			"Please contact flu@lanl.gov if your problem persists.","Delete Confirmation",JOptionPane.INFORMATION_MESSAGE );
             }*/
         }
         else if(e.getSource() == undo){
         	 ws.clearAllLists();
 //             System.out.println("SAVED REMOVE ALL "+savedREMOVE_ALL.toString());
 //            System.out.println("SAVED ACC "+savedACC.toString());
              ws.resetRemoveALL();
              ws.resetACC();
         }
 		
 		
 		//########################
 		
 		
 		
 		
 	}
 	
 	public String saveToFileComm(String accToRemove){
 		/**
 		 * Note to programmer:
 		 * dont use controlPanel variable as call from AppletTerminate uses the empty constructor
 	 	 *	Beware of nullPointerException
 		 */
 		lastAction="save";
 		String returnedString = "";
 
 		//################
 		Vector returnedDataSet = null;
         URL u = null;
         Hashtable formVars = new Hashtable();
         String filename = AppletParams.filename;
         if(isCommunicationGET()){
         	JavaBridge jb = new JavaBridge();
         	String getURL = getSetupFileForGET();
         	try {
                 if (getURL.startsWith("http")) {
                      u = new URL(getURL); 
                 } else {
                      u = new URL( getURL);
                 }
                 jb.addFormValue(formVars,"comm", "on");
                 jb.addFormValue(formVars,
                         "mark_in_DB",accToRemove);
                 jb.addFormValue(formVars,
                             "filename",filename);
                 jb.addFormValue(formVars, "action", "save");  //BHB06--09
                 returnedDataSet = jb.getParsedData(u,formVars);
         	}
         	catch (NullPointerException e){            // Caught because the getURL is null when the Archae is run as TP/TD-Application
 				destroyWarningWindow();
 				System.out.println("Null Pointer Exception: " + e);
 			}
         	catch (MalformedURLException e) {
         		destroyWarningWindow();
                 System.out.println("Malformed URL Exception:" + e);
             } catch (JavaBridgeTimeOutException e) {   //BHB06--09
             	destroyWarningWindow();
                 System.out.println("JavaBridge Timed Out:" + e);  //BHB06--09
             }
 		}
 		else{
 			JavaCGIBridge jcb = new JavaCGIBridge();
 			String postURL =getSetupFileForPOST();
 			try {
 	            if (postURL.startsWith("http")) {
 	                 u = new URL(postURL); 
 	            } else {
 	                 u = new URL( postURL);
 	            }
 	            
 	            jcb.addFormValue(formVars,"comm", "on");
 	            jcb.addFormValue(formVars,
 	                "mark_in_DB",accToRemove);
 	            jcb.addFormValue(formVars,
 	                    "filename",filename);
 	            returnedDataSet = jcb.getParsedData(u,formVars);
 			}
 			catch (NullPointerException e){            // Caught because the postURL is null when the Archae is run as TP/TD-Application
 				destroyWarningWindow();
 				System.out.println("Null Pointer Exception: " + e);
 			}
 			catch (MalformedURLException e) {
 				destroyWarningWindow();
 				System.out.println("Malformed URL Exception:" + e);
 			} catch (JavaCGIBridgeTimeOutException e) {
 				destroyWarningWindow();
 				System.out.println("JavaCGIBridge Timed Out:" + e);
 			}
 		}
         if(!returnedDataSet.isEmpty()){
         	returnedString = (String)((Vector)returnedDataSet.elementAt(0)).elementAt(0);
         }
 	    System.out.println("returned string AT SaveToFile"+returnedString);
 
 		
 		//#################
 		
 		
 		return returnedString;
 	}
 	
 	public String deleteFromDbComm (String accToRemove){
 		/**
 		 * Note to programmer:
 		 * dont use controlPanel variable as call from AppletTerminate uses the empty constructor
 	 	 *	Beware of nullPointerException
 		 */
 		lastAction="delete";
 		String returnedString = "";
 		
 		//################
 		Vector returnedDataSet = null;
         URL u = null;
         Hashtable formVars = new Hashtable();
         String filename = AppletParams.filename;
         if(isCommunicationGET()){
         	JavaBridge jb = new JavaBridge();
         	String getURL = getSetupFileForGET();
         	try {
                 if (getURL.startsWith("http")) {
                      u = new URL(getURL); 
                 } else {
                      u = new URL( getURL);
                 }
                 jb.addFormValue(formVars,"comm", "on");
                 jb.addFormValue(formVars,
                     "delete_from_DB",accToRemove);
                 jb.addFormValue(formVars,
                         "filename",filename);
                 jb.addFormValue(formVars, "action", "commit");  //BHB06--09
                 returnedDataSet = jb.getParsedData(u,formVars);
         	}
         	catch (NullPointerException e){            // Caught because the getURL is null when the Archae is run as TP/TD-Application
 				destroyWarningWindow();
 				System.out.println("Null Pointer Exception: " + e);
 			}
         	catch (MalformedURLException e) {
         		destroyWarningWindow();
                 System.out.println("Malformed URL Exception:" + e);
             } catch (JavaBridgeTimeOutException e) {   //BHB06--09
             	destroyWarningWindow();
                 System.out.println("JavaBridge Timed Out:" + e);  //BHB06--09
             }
 		}
 		else{
 			JavaCGIBridge jcb = new JavaCGIBridge();
 			String postURL =getSetupFileForPOST();
 			try {
 	            if (postURL.startsWith("http")) {
 	                 u = new URL(postURL); 
 	            } else {
 	                 u = new URL( postURL);
 	            }
 	            
 	            jcb.addFormValue(formVars,"comm", "on");
 	            jcb.addFormValue(formVars,
 	                "delete_from_DB",accToRemove);
 	            jcb.addFormValue(formVars,
 	                    "filename",filename);
 	            returnedDataSet = jcb.getParsedData(u,formVars);
 			}
 			catch (NullPointerException e){            // Caught because the postURL is null when the Archae is run as TP/TD-Application
 				destroyWarningWindow();
 				System.out.println("Null Pointer Exception: " + e);
 			}
 			catch (MalformedURLException e) {
 				destroyWarningWindow();
 				System.out.println("Malformed URL Exception:" + e);
 			} catch (JavaCGIBridgeTimeOutException e) {
 				destroyWarningWindow();
 				System.out.println("JavaCGIBridge Timed Out:" + e);
 			}
 		}
         if(!returnedDataSet.isEmpty()){
         	returnedString = (String)((Vector)returnedDataSet.elementAt(0)).elementAt(0);
         }
 	    System.out.println("returned string AT SaveToFile"+returnedString);
 
 		
 		//#################
 
 		
 		return returnedString;
 	}
 	
 	public String discardComm(){
 		/**
 		 * Note to programmer:
 		 * dont use controlPanel variable as call from AppletTerminate uses the empty constructor
 	 	 *	Beware of nullPointerException
 		 */
 		lastAction="discard";
 		String returnedString ="";
 		
 		//################
 		Vector returnedDataSet = null;
         URL u = null;
         Hashtable formVars = new Hashtable();
         String filename = AppletParams.filename;
         if(isCommunicationGET()){
         	JavaBridge jb = new JavaBridge();
         	String getURL = getSetupFileForGET();
         	try {
                 if (getURL.startsWith("http")) {
                      u = new URL(getURL); 
                 } else {
                      u = new URL( getURL);
                 }
                 jb.addFormValue(formVars,"comm", "on");
                 jb.addFormValue(formVars,
                     "del_file","Delete filename");
                 jb.addFormValue(formVars,
                         "filename",filename);
                 jb.addFormValue(formVars, "action", "discard");  //BHB06--09
                 returnedDataSet = jb.getParsedData(u,formVars);
         	}
         	catch (NullPointerException e){            // Caught because the getURL is null when the Archae is run as TP/TD-Application
 				destroyWarningWindow();
 				System.out.println("Null Pointer Exception: " + e);
 			}
         	catch (MalformedURLException e) {
         		destroyWarningWindow();
                 System.out.println("Malformed URL Exception:" + e);
             } catch (JavaBridgeTimeOutException e) {   //BHB06--09
             	destroyWarningWindow();
                 System.out.println("JavaBridge Timed Out:" + e);  //BHB06--09
             }
 		}
 		else{
 			JavaCGIBridge jcb = new JavaCGIBridge();
 			String postURL =getSetupFileForPOST();
 			try {
 	            if (postURL.startsWith("http")) {
 	                 u = new URL(postURL); 
 	            } else {
 	                 u = new URL( postURL);
 	            }
 	            
 	            jcb.addFormValue(formVars,"comm", "on");
 	            jcb.addFormValue(formVars,
 	                "del_file","Delete filename");
 	            jcb.addFormValue(formVars,
 	                    "filename",filename);
 	            returnedDataSet = jcb.getParsedData(u,formVars);
 			}
 			catch (NullPointerException e){            // Caught because the postURL is null when the Archae is run as TP/TD-Application
 				destroyWarningWindow();
 				System.out.println("Null Pointer Exception: " + e);
 			}
 			catch (MalformedURLException e) {
 				destroyWarningWindow();
 				System.out.println("Malformed URL Exception:" + e);
 			} catch (JavaCGIBridgeTimeOutException e) {
 				destroyWarningWindow();
 				System.out.println("JavaCGIBridge Timed Out:" + e);
 			}
 		}
         if(!returnedDataSet.isEmpty()){
         	returnedString = (String)((Vector)returnedDataSet.elementAt(0)).elementAt(0);
         }
 	    System.out.println("returned string AT SaveToFile"+returnedString);
 
 		
 		//#################
 
 		
 		return returnedString;
 	}
 	
 	private String getSetupFileForPOST() {
         String postURL = null;
         try {
             URL u = new URL(AppletParams.codeBase,"ATVserver.cgi");
             postURL= u.toString();
         } catch (MalformedURLException e) {
             System.out.println("Malformed URL Exception:" + e);
         } 
         return postURL;
     } // end of getSetupFilePOST
 	
 	private String getSetupFileForGET() {
         String getURL = null;
         try {
         	URL u = new URL(AppletParams.URLprefix + "?"); //BHB06--09
             System.out.println("URL in getSetupFile in ATVControl" + u); //BHB06--09 //changed text
             getURL= u.toString();
         } catch (MalformedURLException e) {
             System.out.println("Malformed URL Exception:" + e);
         } 
         return getURL;
     } // end of getSetupFileGET
 	
 	private boolean isCommunicationGET(){
 		return(AppletParams.URLprefix.length()>0);
 	}
 	
 	private static void destroyWarningWindow(){
         if(warningWindow!=null){
         	warningWindow.close();
                 
         }
     }
 
 }
