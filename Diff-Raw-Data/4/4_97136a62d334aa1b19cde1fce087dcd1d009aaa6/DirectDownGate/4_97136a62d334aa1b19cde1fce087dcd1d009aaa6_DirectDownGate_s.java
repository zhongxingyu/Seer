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
 
 import java.io.Serializable;
 import java.util.NoSuchElementException;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.Config.Simulator.SimulatorMode;
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.properties.DedicatedQoSReservationProperty;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.packets.PleaseCloseConnection;
 import de.tuilmenau.ics.fog.packets.PleaseOpenDownGate;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.topology.NeighborInformation;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.topology.ILowerLayer.SendResult;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.manager.Process;
 import de.tuilmenau.ics.fog.transfer.manager.Controller.BrokenType;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Viewable;
 
 
 /**
  * Represents gate which forwards messages to another node by using a lower
  * layer ID (e.g. a MAC address for a bus).
  */
 public class DirectDownGate extends DownGate
 {
 	public DirectDownGate(ForwardingNode pFN, int localProcessNumber, FoGEntity entity, NetworkInterface networkInterface, NeighborInformation toLowerLayerID, Description description, Identity owner)
 	{
 		super(entity, networkInterface, description, owner);
 
 		if(description != null){
 			DedicatedQoSReservationProperty tProp = (DedicatedQoSReservationProperty)description.get(DedicatedQoSReservationProperty.class);
 			if(tProp != null){
 				mDedicatedQoSReservation = true;
 				if(tProp.isBidirectional()){
 					mBidirectionalQoSReservation = true;
 				}
 			}
 		}
 		
 		mLocalProcessNumber = localProcessNumber;
 		mToLowerLayerID = toLowerLayerID;
 		mFN = pFN;
 		
 		networkInterface.attachDownGate(this);
 	}
 	
 	@Override
 	protected void init()
 	{
 		if((mToLowerLayerID == null) || (getLowerLayer() == null)) {
 			setState(GateState.ERROR);
 		}
 	}
 	
 	@Override
 	protected void setLocalPartnerGateID(GateID pReverseGateID)
 	{
 		super.setLocalPartnerGateID(pReverseGateID);
 		
 		if(pReverseGateID != null) {
 			switchToState(GateState.OPERATE);
 		} else {
 			switchToState(GateState.ERROR);
 		}
 	}
 	
 	public void handlePacket(Packet packet, ForwardingElement lastHop)
 	{
 		if(packet.isTraceRouting()){
 			Logging.log(this, "TRACEROUTE-Sending packet: " + packet);
 		}
 
 		boolean tDeleteMe = false;
 		if(mDedicatedQoSReservation){
 			Serializable tPacketPayload = packet.getData();
 			if(tPacketPayload instanceof PleaseCloseConnection){
 				mLogger.log(this, "Have to disappear because connection is to be closed.");
 				tDeleteMe = true;
 			}
 				
 			//mLogger.log(this, "Forwarding with QoS reservation the packet: " + packet);	
 		}
 		
 		packet.addToDownRoute(getGateID());
 		if (packet.traceBackwardRoute()) {
 			if (isReverseGateAvailable()) {
 				packet.addReturnRoute(getReverseGateID());
 			} else {
 				packet.returnRouteBroken();
 			}
 		}
 		
 		boolean invisible = packet.isInvisible();
 
 		// send packet to lower layer
 		NetworkInterface ll = getLowerLayer();
 		if(ll != null) {
 			if(!invisible) incMessageCounter();
 			
 			SendResult res = ll.sendPacketTo(mToLowerLayerID, packet, this);
 			if(packet.isTraceRouting()){
 				if(res != SendResult.OK){
 					Logging.err(this, "TRACEROUTE-Failed (res=" + res.toString() + ") to send packet: " + packet);
 				}
 			}
 
 			// Error during transmission?
 			// Do not do any recovery for invisible packets.
 			if((res != SendResult.OK) && !invisible) {
 				String msg = "Cannot send packet " +packet +" to " +mToLowerLayerID +" due to " +res;
 				if(Config.Simulator.MODE == SimulatorMode.FAST_SIM) {
 					// do not report it in batch mode as warning, since it might be intended by scenario
 					mLogger.log(this, msg);
 				} else {
 					mLogger.warn(this, msg);
 				}
 				
 				// maybe gate already closed during error recovery? 
 				if((getState() != GateState.SHUTDOWN) && (getState() != GateState.DELETED)) {
 					switchToState(GateState.ERROR);
 				}
 				
 				// inform controller
 				try {
 					if (packet.getReturnRoute().getFirst() instanceof RouteSegmentPath) {
 						((RouteSegmentPath)packet.getReturnRoute().getFirst()).removeFirst();
 						((RouteSegmentPath)packet.getReturnRoute().getFirst()).removeFirst();
 					}
 				}
 				catch (NoSuchElementException e) {
 					mEntity.getLogger().err(this, "Could not modify return route", e);
 				}
 				mEntity.getController().handleBrokenElement(convertError(res), getLowerLayer(), packet, this);
 			}
 		} else {
 			if(!invisible) {
 				// gate maybe closed
 				mLogger.err(this, "No network interface given. Dropping packet " +packet);
 			}
 			// else: ignore error due to invisible packet
 			packet.dropped(this);
 		}
 		
 		if(tDeleteMe){
 			mLogger.log(this, "Deleting myself");
 			shutdown();
 			delete();
 			Process tParentProcess = getEntity().getProcessRegister().getProcess(mFN, getOwner(), mLocalProcessNumber);
 			
 			mLogger.log(this, "Terminating parent process...");
			tParentProcess.terminate(new NetworkException(this, this + " should be removed now, process termination required"));
 		}
 	}
 	
 	@Override
 	public void refresh()
 	{
 		NetworkInterface ll = getLowerLayer();
 		
 		if(ll != null) {
 			Name addr = mEntity.getRoutingService().getNameFor(ll.getMultiplexerGate());
 			
 			Packet tReq = new Packet(new PleaseOpenDownGate(mLocalProcessNumber, getGateID(), addr, getDescription()));
 			if(getEntity().getAuthenticationService().sign(tReq, getEntity().getIdentity())) {
 				handlePacket(tReq, null);
 			} else {
 				mLogger.err(this, "Can not send refresh signaling message since signature for " +getEntity().getIdentity() +" can not be created. (owner of gate = " +getOwner() +")");
 			}
 		} else {
 			delete();
 		}
 	}
 
 	public NeighborInformation getToLowerLayerID()
 	{
 		return mToLowerLayerID;
 	}
 	
 	private BrokenType convertError(SendResult result)
 	{
 		switch(result) {
 		case LOWER_LAYER_BROKEN:
 			return BrokenType.BUS;
 			
 		case NEIGHBOR_NOT_REACHABLE:
 			return BrokenType.NODE;
 			
 		case NEIGHBOR_NOT_KNOWN:
 			// TODO: implement
 			
 		case UNKNOWN_ERROR:
 		default:
 			return BrokenType.UNKNOWN;
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		String tResult = super.toString();
 		
 		if(!getDescription().isBestEffort()){
 			tResult += " (QoS)";
 		}
 		
 		return tResult;
 	}
 
 	ForwardingNode mFN = null;
 	
 	@Viewable("Local process number")
 	private int mLocalProcessNumber = -1;
 	
 	@Viewable("Lower layer name")
 	private NeighborInformation mToLowerLayerID;
 	
 	@Viewable("Dedicated QoS reservation")
 	private boolean mDedicatedQoSReservation = false;
 	
 	@Viewable("Bidirectional QoS reservation")
 	private boolean mBidirectionalQoSReservation = false;	
 }
