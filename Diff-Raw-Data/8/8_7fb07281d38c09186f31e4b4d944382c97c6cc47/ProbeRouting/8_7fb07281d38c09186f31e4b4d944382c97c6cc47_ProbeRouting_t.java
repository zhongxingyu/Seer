 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.ui.eclipse.commands.hierarchical;
 
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.eclipse.ui.commands.EclipseCommand;
 import de.tuilmenau.ics.fog.eclipse.ui.dialogs.SelectFromListDialog;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.DestinationApplicationProperty;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.ProbeRoutingProperty;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * In order to create simulations this class sends packets from one node to another randomly chosen node. Or from one 
  * packets are sent to all other nodes within the network. The last case is to iteratively walk through the nodes of a network
  * and send packets to all other nodes. In that case you can determine the stretch of your system.
  *  
  *
  */
 public class ProbeRouting extends EclipseCommand
 {
 	/**
 	 * Defines the node name which is used to send a packet to all nodes. 
 	 */
 	private static final String SEND_TO_ALL_ADDRESSES_OF_TARGET_NODE = "all nodes";
 	
 	/**
 	 * Stores a reference to the NMS instance.
 	 */
 	private NameMappingService mNMS = null;
 
 	/**
 	 * Stores the selection of the last call in order to ease the GUI handling.
 	 */
 	private static int sLastTargetSelection = -1;
 
 	private Node mNode = null;
 	
 	public ProbeRouting()
 	{
 		
 	}
 
 	/**
 	 * Initializes this Command.
 	 *
 	 * @param pSite the SWT site for this Command
 	 * @param pObject the object parameter
 	 */
 	@Override
 	public void execute(Object object)
 	{
 		if(object instanceof Node) {
 			mNode = (Node) object;
 		} else if(object instanceof HRMController) {
 			mNode = ((HRMController)object).getNode();
 		}
 		
 		if(mNode != null) {
 			/**
 			 * determine all nodes from the simulation
 			 */
 			LinkedList<Node> tNodeList = new LinkedList<Node>();
 			for(AutonomousSystem tAS : mNode.getAS().getSimulation().getAllAS()) {
 				for(Node tNode : tAS.getNodelist().values()) {
 					tNodeList.add(tNode);
 				}
 			}
 			
 			/**
 			 * Let the user select the target(s) for the probe packet
 			 */
 			// list possible targets
 			LinkedList<String> tPossibilities = new LinkedList<String>();
 			tPossibilities.add(SEND_TO_ALL_ADDRESSES_OF_TARGET_NODE);
 			for (Node tNode : tNodeList){
 				tPossibilities.add(tNode.getName());
 			}
 			// show dialog
 			int tTargetSelection = SelectFromListDialog.open(getSite().getShell(), "Target(s) selection", "To which target(s) should the probe packet be send?", (sLastTargetSelection >= 0 ? sLastTargetSelection : 1), tPossibilities);
 			if (tTargetSelection >= 0){
 				sLastTargetSelection = tTargetSelection;			
 				String tTargetName = tPossibilities.get(tTargetSelection);
 				Logging.log(this, "Selected target: " + tTargetName + "(" + tTargetSelection + ")");
 				// store target selection as string
 				
 				/**
 				 * Has user selected "all nodes"?
 				 */
 				// send to all nodes of the simulation?
 				boolean tSendToAllNodes = false;
 				if (tTargetName.equals(SEND_TO_ALL_ADDRESSES_OF_TARGET_NODE)){
 					tSendToAllNodes = true;
 				}
 	
 				/**
 				 * Get a reference to the naming-service
 				 */
 				try {
 					mNMS = HierarchicalNameMappingService.getGlobalNameMappingService(mNode.getAS().getSimulation());
 				} catch (RuntimeException tExc) {
 					mNMS = HierarchicalNameMappingService.createGlobalNameMappingService(mNode.getAS().getSimulation());
 				}
 	
 				// send to all nodes?
 				if(!tSendToAllNodes) {	
 					/**
 					 * We determine detailed data about the target node here.
 					 * This is the same as if the user would have directly selected this string. Thus, the two following operations are allowed - and eases the handling of our FoG GUI.
 					 */
 					// get a reference to the target node: this is possible for the GUI only
 					Node tTargetNode = tNodeList.get(tTargetSelection - 1 /* be aware of "all nodes" entry */);
 					
 					// get the recursive FoG layer
 					FoGEntity tFoGLayer = (FoGEntity) tTargetNode.getLayer(FoGEntity.class);
 					
 					// get the central FN of this node
 					Multiplexer tCentralFN = tFoGLayer.getCentralFN();
 
 					// get the name of the central FN
 					Name tTargetNodeName = tCentralFN.getName();
 	
 					/**
 					 * Send a probe-packet to each HRMID, which is found in the NMS instance. 
 					 */
					sendProbeConnectionRequest(tTargetNodeName, tTargetNode, 53, 1 * 1000);
 				} else {
 					for(Node tTargetNode : tNodeList) {
 						/**
 						 * We determine detailed data about the target node here.
 						 * This is the same as if the user would have directly selected this string. Thus, the following operation is allowed - and eases the handling of our FoG GUI.
 						 */
 						// get the recursive FoG layer
 						FoGEntity tFoGLayer = (FoGEntity) tTargetNode.getLayer(FoGEntity.class);
 						
 						// get the central FN of this node
 						Multiplexer tCentralFN = tFoGLayer.getCentralFN();
 
 						// get the name of the central FN
 						Name tTargetNodeName = tCentralFN.getName();
 		
 						/**
 						 * Send a probe-packet to each HRMID, which is found in the NMS instance. 
 						 */
						sendProbeConnectionRequest(tTargetNodeName, tTargetNode, 53, 1 * 1000);
 					}
 				}
 			}else{
 				Logging.log(this, "User canceled the target selection dialog");
 			}
 		}
 	}
 
 	/**
 	 * Send a HRM probe-packet to the defined target.
 	 * 
 	 * @param pTargetNodeName the name (e.g., HRMID) of the target
 	 * @param pTargetNode the target node (reference is only used for debugging purposes)
 	 * @param pDesiredDelay the desired delay
 	 * @param pDataRate the desired data rate
 	 */
 	private void sendProbeConnectionRequest(Name pTargetNodeName, Node pTargetNode, int pDesiredDelay, int pDataRate)
 	{
 		Logging.log(this, "\n\n\n############## Sending probe packet to " + pTargetNodeName + ", which belongs to node " + pTargetNode);
 		
 		// get the recursive FoG layer
 		FoGEntity tFoGLayer = (FoGEntity) mNode.getLayer(FoGEntity.class);
 		
 		// get the central FN of this node
 		Multiplexer tCentralFN = tFoGLayer.getCentralFN();
 		
 		// check if we have a valid NMS
 		if (mNMS == null){
 			Logging.err(this, "Reference to NMS is invalid, cannot send a packet to " + pTargetNodeName);
 			return;
 		}
 		
 		/**
 		 * Create QoS description
 		 */
 		Description tDesiredQoSValues = Description.createQoS(pDesiredDelay, pDataRate);
 		
 		// send a HRM probe-packet to each registered address for the given target name
 		try {
 			for(NameMappingEntry<?> tNMSEntryForTarget : mNMS.getAddresses(pTargetNodeName)) {
 				if(tNMSEntryForTarget.getAddress() instanceof HRMID) {
 					// get the HRMID of the target node
 					HRMID tTargetNodeHRMID = (HRMID)tNMSEntryForTarget.getAddress();
 					
 					Logging.log(this, "Found in the NMS the HRMID " + tTargetNodeHRMID.toString() + " for node " + pTargetNode);  
 					
 					if ((HRMConfig.DebugOutput.GUI_SHOW_RELATIVE_ADDRESSES) || (!tTargetNodeHRMID.isRelativeAddress())){
 						if(!tTargetNodeHRMID.isClusterAddress()){
 							/**
 							 * Connect to the destination node
 							 */
 							// create requirements with probe-routing property and DestinationApplication property
 							Description tConnectionReqs = tDesiredQoSValues.clone();
							tConnectionReqs.set(new ProbeRoutingProperty(tCentralFN.getName().toString(), tTargetNodeHRMID, pDesiredDelay, pDataRate, true));
 							tConnectionReqs.set(new DestinationApplicationProperty(HRMController.ROUTING_NAMESPACE));
 							// probe connection
 							Connection tConnection = null;
 							Logging.log(this, "\n\n\nProbing a connection to " + tTargetNodeHRMID + " with requirements " + tConnectionReqs);
 							tConnection = mNode.getLayer(null).connect(tTargetNodeHRMID, tConnectionReqs, mNode.getIdentity());
 							/**
 							 * Disconnect by closing the connection
 							 */
 							if(tConnection != null) {
 								Logging.log(this, "        ..found valid connection to " + tTargetNodeHRMID + "(" + pTargetNodeName + ")");
 								tConnection.close();
 							}else{
 								Logging.err(this, "Unable to connecto to " + tTargetNodeHRMID);
 							}
 						}else{
 							// cluster address
 						}
 
 						Logging.log(this, "############## PROBING END ###########"); 
 					}else{
 						Logging.log(this, "     ..address " + tTargetNodeHRMID + " is ignored because it is a relative one");
 					}
 				}else{
 					Logging.log(this, "Found in the NMS the unsupported address " + tNMSEntryForTarget.getAddress() + " for node " + pTargetNode);
 				}
 			}
 		} catch (RemoteException tExc) {
 			Logging.err(this, "Unable to determine addresses for node " + pTargetNode, tExc);
 		}
 	}
 	
 	/**
 	 * Returns a descriptive string about the object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mNode.toString();
 	}
 }
