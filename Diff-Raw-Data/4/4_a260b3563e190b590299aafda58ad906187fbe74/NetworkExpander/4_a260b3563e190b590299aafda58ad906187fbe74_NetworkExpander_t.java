 package cytoscape.data.webservice.ui;
 
 import giny.model.Node;
 import giny.view.NodeView;
 
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.data.webservice.CyWebServiceEvent;
 import cytoscape.data.webservice.DatabaseSearchResult;
 import cytoscape.data.webservice.NetworkImportWebServiceClient;
 import cytoscape.data.webservice.WebServiceClient;
 import cytoscape.data.webservice.WebServiceClientManager;
 import cytoscape.data.webservice.CyWebServiceEvent.WSEventType;
 import cytoscape.data.webservice.WebServiceClientManager.ClientType;
 import cytoscape.layout.CyLayouts;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import ding.view.NodeContextMenuListener;
 
 public class NetworkExpander implements PropertyChangeListener, NodeContextMenuListener {
 	
 	// This map manages list of expander-compatible clients.
 	// Key is the display name, and value is client ID.
 	private Map<String, String> clientMap;
 	
 	private JMenu menu;
 	
 	private String defLayout;
 	
 
 	public NetworkExpander() {
 		
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(this);
 		defLayout = CytoscapeInit.getProperties().getProperty("expanderDefaultLayout");
 		if(defLayout == null) {
 			defLayout = "force-directed";
 		}
 		
 		clientMap = new HashMap<String, String>();
 		final List<WebServiceClient> clients = WebServiceClientManager.getAllClients();
 		
 		menu = new JMenu("Get neighbors of selected node(s) from Web Services");
 		
 		for ( WebServiceClient client : clients ) { 
 			
 			if(client.isCompatibleType(ClientType.NETWORK)) {
 				addMenuItem(client, menu);
 				clientMap.put(client.getDisplayName(), client.getClientID());
 			}
 
 		}
 	}
 
 	private void addMenuItem(WebServiceClient client, JMenu menu) {
 		JMenuItem jmi = new JMenuItem(new AbstractAction(client.getDisplayName()) {
 			public void actionPerformed(ActionEvent e) {
 
 				System.out.println("Start Expanding network: " + e.getActionCommand() );
 				CyWebServiceEvent evt = new CyWebServiceEvent(clientMap.get(e.getActionCommand()), WSEventType.SEARCH_DATABASE, buildQuery(), WSEventType.EXPAND_NETWORK);
 //				WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(evt);
 				SearchTask task = new SearchTask(evt);
 
 				// Configure JTask Dialog Pop-Up Box
 				final JTaskConfig jTaskConfig = new JTaskConfig();
 				jTaskConfig.setOwner(Cytoscape.getDesktop());
 				jTaskConfig.displayCloseButton(true);
 				jTaskConfig.displayStatus(true);
 				jTaskConfig.setAutoDispose(true);
 
 				// Execute Task in New Thread; pops open JTask Dialog Box.
 				TaskManager.executeTask(task, jTaskConfig);
 				
 			}
 		});
 		
 		menu.add(jmi);
 		
 	}
 	
 	private String buildQuery() {
 		StringBuilder builder = new StringBuilder();
 		Set<Node> selectedNodes =  Cytoscape.getCurrentNetwork().getSelectedNodes();
 		for(Node node: selectedNodes) {
 			builder.append(node.getIdentifier() + " ");
 		}
 		
 		return builder.toString();
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		
 		Object resultObj = evt.getNewValue();
		if(evt.getPropertyName().equals(CyWebServiceEvent.WSResponseType.SEARCH_FINISHED.toString()) && ((DatabaseSearchResult)resultObj).getNextMove().equals(WSEventType.EXPAND_NETWORK)) {
 			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@Got search result from: " 
 					+ evt.getSource() + ", Num result = " + evt.getNewValue() + ", Source name = " + evt.getOldValue());
 			String message[] = {
 					((DatabaseSearchResult)resultObj).getResultSize() + " interactions found." ,
 					"Do you want to add these interactions to " + Cytoscape.getCurrentNetwork().getTitle() + "?"
 				};
 				int value = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), message, "Expand network", JOptionPane.YES_NO_OPTION
 				);
 				if (value == JOptionPane.YES_OPTION) {
 					CyWebServiceEvent evt2 = new CyWebServiceEvent(evt.getOldValue().toString(), WSEventType.EXPAND_NETWORK, ((DatabaseSearchResult)resultObj).getResult()); 
 					try {
 						WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(evt2);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 		} else if (evt.getPropertyName().equals(Cytoscape.NETWORK_MODIFIED) && evt.getSource() instanceof NetworkImportWebServiceClient){
 			
 			String message[] = {
 					"Neighbours loaded.",
 					"Do you want to layout the network now?"
 				};
 				int value = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), message, "Expansion complete", JOptionPane.YES_NO_OPTION
 				);
 				if (value == JOptionPane.YES_OPTION) {
 					CyLayouts.getLayout(defLayout).doLayout();
 				}
 
 		} else if(evt.getPropertyName().equals(Cytoscape.PREFERENCES_UPDATED)) {
 			defLayout = CytoscapeInit.getProperties().getProperty("expanderDefaultLayout");
 			if(defLayout == null) {
 				defLayout = "force-directed";
 			}
 		}
 		
 	}
 	
 	/**
 	 * @param nodeView The clicked NodeView
 	 * @param menu popup menu to add the Bypass menu
 	 */
 	public void addNodeContextMenuItems(NodeView nodeView, JPopupMenu menu) {
 
 		if (menu == null)
 			menu = new JPopupMenu();
 
 		menu.add(this.menu);
 	}
 	
 	
 	class SearchTask implements Task {
 
 		private CyWebServiceEvent evt;
 		
 		private TaskMonitor taskMonitor;
 		
 		public SearchTask(CyWebServiceEvent evt) {
 			this.evt = evt;
 		}
 		
 		public String getTitle() {
 			// TODO Auto-generated method stub
 			return "Expanding Network";
 		}
 
 		public void halt() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void run() {
 			taskMonitor.setStatus("Loading neighbours...");
 			taskMonitor.setPercentCompleted(-1);
 			// this even will load the file
 			try {
 				WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(evt);
 			} catch (Exception e) {
 				taskMonitor.setException(e, "Failed to load neighbours.");
 				return;
 			}
 			taskMonitor.setPercentCompleted(100);
 			
 			Cytoscape.getDesktop().setFocus(Cytoscape.getCurrentNetwork().getIdentifier());
 			
 			String curNetID = Cytoscape.getCurrentNetwork().getIdentifier();
 			
 			Cytoscape.getNetworkView(curNetID).setVisualStyle(Cytoscape.getVisualMappingManager().getVisualStyle().getName());
 			Cytoscape.getNetworkView(curNetID).redrawGraph(false, true);
 			
 		}
 
 		public void setTaskMonitor(TaskMonitor arg0)
 				throws IllegalThreadStateException {
 			this.taskMonitor = arg0;
 			
 		}
 		
 	}
 	
 }
