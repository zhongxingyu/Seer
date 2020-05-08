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
 package de.tuilmenau.ics.fog.transfer.manager;
 
 import java.rmi.RemoteException;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.properties.DatarateProperty;
 import de.tuilmenau.ics.fog.facade.properties.DedicatedQoSReservationProperty;
 import de.tuilmenau.ics.fog.facade.properties.NonFunctionalRequirementsProperty;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.packets.PleaseOpenDownGate;
 import de.tuilmenau.ics.fog.packets.SignallingRequest;
 import de.tuilmenau.ics.fog.topology.NeighborInformation;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.DirectDownGate;
 import de.tuilmenau.ics.fog.transfer.gates.ReroutingGate;
 
 
 /**
  * This process is responsible for creating, checking and de-constructing
  * a DownGate on a host. The DownGate represents a connection via a lower
  * layer to some other transfer service instance.
  */
 public class ProcessDownGate extends ProcessGateConstruction
 {
 	private static final double MAX_NUMBER_RETRIES_SIGNALING = 4;
 	
 	
 	public ProcessDownGate(ForwardingNode pBase, NetworkInterface pInterface, NeighborInformation tLowerLayerID, Description pRequirements, Identity pOwner, ReroutingGate pBackup)
 	{
 		super(pBase, pBackup, pOwner);
 		
 		mInterface = pInterface;
 		mLowerLayerID = tLowerLayerID;
 		
 		if(pRequirements != null) {
 			mRequirements = pRequirements.clone();
 		} else {
 			mRequirements = null;
 		}
 	}
 	
 	/**
 	 * Requests reverse gate from peer
 	 */
 	public void signal(Name ownRoutingServiceName)
 	{
 		boolean tUnidirectionalQoSReservation = false;
 		
 		getLogger().log(this, "Signaling creation of reverse DownGate with requirements: " + mRequirements);
 		
 		/**
 		 * Check if a unidirectional QoS reservation was executed -> abort creation of reverse gate
 		 */
 		DedicatedQoSReservationProperty tQoSReservationProp = (DedicatedQoSReservationProperty)mRequirements.get(DedicatedQoSReservationProperty.class);
 		if(tQoSReservationProp != null){
 			if(!tQoSReservationProp.isBidirectional()){
 				tUnidirectionalQoSReservation = true;
 			}
 		}
 
 		/**
		 * Trigger creation of a reverse gate without special QoS reservations
 		 */
 		if(tUnidirectionalQoSReservation){
 			getLogger().log(this, "Unidirectional QoS reservation, removing QoS attributes from original requirements set.");
 			Description tReducedRequirements = new Description();
 			for(Property tProp: mRequirements){
 				if(!(tProp instanceof NonFunctionalRequirementsProperty)){
 					tReducedRequirements.set(tProp);
 				}else if(tProp instanceof NonFunctionalRequirementsProperty){
 					NonFunctionalRequirementsProperty tNonFuncProp = (NonFunctionalRequirementsProperty)tProp;
 					if(tNonFuncProp.isBE()){
 						tReducedRequirements.set(tProp);
 					}
 				}
 			}
			tReducedRequirements.set(new DedicatedQoSReservationProperty(false) /* important to tell the DownGate that it belongs to an explicit QoS reservation and should be deleted when it gets deprecated */);
 			mRequirements = tReducedRequirements;
 		}
 		
 		if(mRequest == null) {
 			try {
 				mRequest = new PleaseOpenDownGate(this, ownRoutingServiceName, mRequirements);
 			}
 			catch(NetworkException exc) {
 				getLogger().err(this, "Can not prepare signaling message.", exc);
 				return;
 			}
 		}
 		
 		Packet tPacket = new Packet(mRequest);
 		getBase().getEntity().getAuthenticationService().sign(tPacket, getOwner());
 		mInterface.sendPacketTo(mLowerLayerID, tPacket, null);
 		
 		// event for re-send signaling message
 		IEvent event = new IEvent() {
 			@Override
 			public void fire()
 			{
 				if(!isFinished()) {
 					if(!isOperational()) {
 						getLogger().warn(this, "Timeout for re-sending the signaling message.");
 						
 						signal(null);
 					}
 				}
 			}
 		};
 		
 		// do it after sending the message. If we are not executed in the event loop, we would risk to be executed immediately otherwise.
 		getTimeBase().scheduleIn(Config.PROCESS_STD_TIMEOUT_SEC / (MAX_NUMBER_RETRIES_SIGNALING +1.0d), event);
 	}
 
 	protected AbstractGate newGate(FoGEntity entity) throws NetworkException
 	{
 		// Check capabilities of bus
 		try {
 			Description tBusCapab = mInterface.getBus().getDescription();
 			
 			if(tBusCapab != null) {
 				mRequirements = tBusCapab.deriveRequirements(mRequirements);
 			}
 			// else stick to the original requirements
 		}
 		catch(RemoteException tExc) {
 			throw new NetworkException("Can not get the description of the lower layer.", tExc);
 		}
 		
 		// Reserve resources in lower layer
 		if(mRequirements != null) {
 			DatarateProperty datarateUsage = (DatarateProperty) mRequirements.get(DatarateProperty.class);
 			if(datarateUsage != null) {
 				// is it really a requirement?
 				if(!datarateUsage.isBE()) {
 					// reserve bandwidth
 					try {
 						getLogger().log(this, "Modifying available data rate by " + (- datarateUsage.getMax()) + " kbit/s for bus " + mInterface.getBus());
 						mInterface.getBus().modifyAvailableDataRate(-datarateUsage.getMax());
 						
 						getLogger().log(this, "Refreshing gates for  bus " + mInterface.getBus());
 						mInterface.refreshGates();
 					}
 					catch(RemoteException exc) {
 						throw new NetworkException(this, "Can not reserve resources at lower layer.", exc);
 					}
 				}
 			}
 		}
 
 		// Create gate
 		DirectDownGate tRes = new DirectDownGate(getID(), entity, mInterface, mLowerLayerID, mRequirements, getOwner());
 		
 		if(Config.Connection.TERMINATE_WHEN_IDLE) {
 			if(mRequirements != null) {
 				if(!mRequirements.isBestEffort()) {
 					tRes.startCheckForIdle();
 				}
 			}
 		}
 		
 		return tRes;
 	}
 	
 	@Override
 	protected void finished()
 	{
 		if((mGate != null) && (mRequirements != null)) {
 			// release resources of lower layer
 			DatarateProperty datarateUsage = (DatarateProperty) mRequirements.get(DatarateProperty.class);
 			if(datarateUsage != null) {
 				// is it really a requirement?
 				if(!datarateUsage.isBE()) {
 					// free bandwidth
 					try {
 						mInterface.getBus().modifyAvailableDataRate(+datarateUsage.getMax());
 						
 						mInterface.refreshGates();
 					}
 					catch(RemoteException exc) {
 						getLogger().err(this, "Can not free resources at lower layer.", exc);
 					}
 				}
 			}
 		}
 		
 		super.finished();
 	}
 	
 	private NetworkInterface mInterface;
 	private NeighborInformation mLowerLayerID;
 	private Description mRequirements;
 	private SignallingRequest mRequest;
 }
