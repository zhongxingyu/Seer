 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.transfer.gates;
 
 import java.rmi.RemoteException;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.topology.NeighborInformation;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.ui.Viewable;
 
 
 /**
  * Abstract base class for all gates forwarding messages to an other node
  * by using a lower layer. It is going 'down' in the context of a layered
  * stack, since it represents the way from FoG to a lower layer.
  */
 abstract public class DownGate extends AbstractGate
 {
 	private ForwardingElement nextHop;
 	
 	@Viewable("Network interface")
 	private NetworkInterface networkInterface;
 	
 	
 	public DownGate(FoGEntity pEntity, NetworkInterface pInterface, Description pDescription, Identity pOwner)
 	{
 		super(pEntity, pDescription, pOwner);
 		
 		networkInterface = pInterface;
 		
 		// knowing the next hop is not too important for most DownGates,
 		// but it is of great help for the GUI.
 		nextHop = networkInterface.getLowerLayerGUIRepresentation();
 	}
 
 	public NetworkInterface getLowerLayer()
 	{
 		return networkInterface;
 	}
 	
 	public abstract NeighborInformation getToLowerLayerID();
 
 	public ForwardingElement getNextNode()
 	{
 		return nextHop;
 	}
 	
 	public NetworkInterface getNetworkInterface()
 	{
 		return networkInterface;
 	}
 	
 	/**
 	 * Updates description to description of lower layer, if gate is a best effort gate.
 	 * 
 	 * @return true, if something changed; false, if everything constant
 	 */
 	public boolean refreshDescription()
 	{
 		if(getDescription().isBestEffort()) {
 			try {
				mLogger.log(this, "Updating description..");
				mLogger.log(this, "  ..old description: " + getDescription());
				mLogger.log(this, "  ..old description: " + networkInterface.getBus().getDescription());
 				setDescription(networkInterface.getBus().getDescription());
 				
 				return true;
 			}
 			catch(RemoteException exc) {
 				mLogger.err(this, "Can not refresh description from lower layer. State transition to ERROR.", exc);
 				switchToState(GateState.ERROR);
 			}
 			
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	protected void delete()
 	{
 		networkInterface = null;
 		nextHop = null;
 		
 		super.delete();
 	}
 
 }
