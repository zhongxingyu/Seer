 /**
  * 
  */
 package cytoscape.actions;
 
 import java.awt.event.ActionEvent;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.util.CytoscapeAction;
 
 import cytoscape.dialogs.plugins.PluginUpdateDialog;
 
 import cytoscape.plugin.DownloadableInfo;
 import cytoscape.plugin.PluginInfo;
 import cytoscape.plugin.PluginManager;
 import cytoscape.plugin.ManagerException;
 import cytoscape.plugin.PluginStatus;
 
 import java.util.List;
import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 public class PluginUpdateAction extends CytoscapeAction {
 	public PluginUpdateAction() {
 		super("Update Plugins");
 		setPreferredMenu("Plugins");
 
 		if (PluginManager.usingWebstartManager()) {
 			setEnabled(false);
 		}
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		PluginUpdateDialog Dialog = new PluginUpdateDialog(Cytoscape
 				.getDesktop());
		ArrayList<String> XmlIncorrect = new ArrayList<String>();
		ArrayList<String> BadXml = new ArrayList<String>();
		
 		if (!PluginManager.usingWebstartManager()) {
 			boolean updateFound = false;
 			PluginManager Mgr = PluginManager.getPluginManager();
 			// Find updates
 			for (DownloadableInfo Current : Mgr.getDownloadables(PluginStatus.CURRENT)) {
 
 			// Configure JTask Dialog Pop-Up Box
 //			JTaskConfig jTaskConfig = new JTaskConfig();
 //			jTaskConfig.setOwner(Cytoscape.getDesktop());
 //			jTaskConfig.displayCloseButton(false);
 //			jTaskConfig.displayStatus(true);
 //			jTaskConfig.setAutoDispose(true);
 //			jTaskConfig.displayCancelButton(false);
 
 				
 				try {
 					List<DownloadableInfo> Updates = Mgr.findUpdates(Current);
 					if (Updates.size() > 0) {
 						Dialog.addCategory(Current.getCategory(), Current,
 								Updates);
 						updateFound = true;
 					}
 				} catch (org.jdom.JDOMException jde) {
//					System.err.println("Failed to retrieve updates for "
//							+ Current.getName() + ", XML incorrect at "
//							+ Current.getDownloadableURL());
 					System.err.println(jde.getMessage());
					XmlIncorrect.add(Current.toString());
 					// jde.printStackTrace();
 				} catch (java.io.IOException ioe) {
 					System.err.println("Failed to read XML file for "
 							+ Current.getName() + " at "
 							+ Current.getDownloadableURL());
 					ioe.printStackTrace();
					BadXml.add(Current.toString());
 				}
 
 			}
			if (XmlIncorrect.size() > 0) {
				// show option pane warning message?
			}
			
 			if (updateFound) {
 				Dialog.setVisible(true);
 			} else {
 				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 						"No updates available for currently installed plugins.",
 						"Plugin Updates", JOptionPane.INFORMATION_MESSAGE);
 			}
 		} else {
 			JOptionPane
 					.showMessageDialog(
 							Cytoscape.getDesktop(),
 							"Plugin updates are not available when using Cytoscape through webstart",
 							"Plugin Update", JOptionPane.INFORMATION_MESSAGE);
 		}
 	}
 
 }
