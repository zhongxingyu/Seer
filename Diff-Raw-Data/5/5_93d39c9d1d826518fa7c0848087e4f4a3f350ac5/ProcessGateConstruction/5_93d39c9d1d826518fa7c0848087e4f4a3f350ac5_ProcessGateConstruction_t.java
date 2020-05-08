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
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.Gate;
 import de.tuilmenau.ics.fog.transfer.Gate.GateState;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.ui.Viewable;
 
 
 public abstract class ProcessGateConstruction extends Process
 {
 	public ProcessGateConstruction(ForwardingNode base, AbstractGate replacementFor, Identity owner)
 	{
 		super(base, owner);
 		
 		mReplacementFor = replacementFor;
 	}
 	
 	protected abstract AbstractGate newGate(FoGEntity entity) throws NetworkException;
 	
 	public Gate create() throws NetworkException
 	{
 		ForwardingNode tBase = getBase();
 		
 		// synch for test&set of down gates
 		synchronized(tBase) {
 			// create gate
 			mGate = newGate(getBase().getEntity());
 			
 			// assign gate a local ID
 			if(mReplacementFor != null) {
 				// check if gate is still available
 				if(mReplacementFor.isOperational() || (mReplacementFor.getState() == GateState.INIT)) {
 					if(tBase.replaceGate(mReplacementFor, mGate)) {
 						mLogger.log(this, "Gate " +mGate +" created at " +tBase +" as replacement for " +mReplacementFor);
 						mReplacementFor.shutdown();
 					} else {
 						mLogger.err(this, "Was not able to replace " +mReplacementFor +". Register it as new gate.");
 						
 						tBase.registerGate(mGate);
 					}
 				} else {
 					// init in order to be able to switch to delete
 					mGate.initialise();
					mGate.shutdown();
					mGate = null;
					
					// invalidate the process
					mReplacementFor = null;
 					
 					throw new NetworkException(this, "Gate " +mReplacementFor +" that should be replaced is not operational. Terminating the construction of a replacement.");
 				}
 			} else {
 				tBase.registerGate(mGate);
 				
 				mLogger.log(this, "gate " +mGate +" created at " +tBase);
 			}
 		}
 		
 		// switch it to init state
 		mGate.initialise();
 		
 		// start terminate timer for timeout till update
 		restartTimer();
 		return mGate;
 	}
 	
 	public void update(GateID reverseGateNumberAtPeer, Name peerNodeRoutingName, Identity peerIdentity)
 	{
 		FoGEntity tNode = getBase().getEntity();
 		
 		// check access permissions
 		if(mPeerIdentity == null) {
 			mPeerIdentity = peerIdentity;
 		} else {
 			if(!mPeerIdentity.equals(peerIdentity)) {
 				mLogger.err(this, "Access not permitted for " +peerIdentity +". Peer identity is " +mPeerIdentity +".");
 				return;
 			}
 		}
 		
 		// lazy creation?
 		if((mGate == null) && (getState() == ProcessState.STARTING)) {
 			mLogger.log(this, "Update called before create. Doing implicit creation.");
 			
 			try {
 				create();
 			}
 			catch (NetworkException tExc) {
 				mLogger.err(this, "Can not create the gate implicitly. Abording update call.", tExc);
 				return;
 			}
 		}
 		
 		// switch to check timer mode
 		setState(ProcessState.OPERATING);
 		restartTimer();
 		
 		// store reverse gate
 		// if reverse gate is not established, reference is null. Then we just
 		// have a one way down gate
 		mGate.setReverseGateID(reverseGateNumberAtPeer);
 		mGate.setRemoteDestinationName(peerNodeRoutingName);
 
 		// inform the routing service if the peer name is known
 		if(peerNodeRoutingName != null) {
 			try {
 				tNode.getTransferPlane().registerLink(getBase(), mGate);
 			}
 			catch (NetworkException exc) {
 				mLogger.err(this, "Failed to register link " +mGate +" at " +getBase() +" at routing service.", exc);
 			}
 		}
 		// else: hidden gate; do not inform RS
 	}
 
 	@Override
 	public boolean isChangableBy(Identity changer)
 	{
 		boolean allowed = super.isChangableBy(changer);
 		
 		if(!allowed) {
 			if(mPeerIdentity != null) {
 				allowed = mPeerIdentity.equals(changer);
 			} else {
 				allowed = true;
 			}
 		}
 		
 		return allowed;
 	}
 
 	@Override
 	public boolean check()
 	{
 		if(mGate != null) {
 			// gate ok?
 			return (mGate.getState() == GateState.OPERATE);
 		} else {
 			// something wrong since create was not called or terminate does not stop timer
 			mLogger.err(this, "Internal error: Gate " +mGate +" not available.");
 			return false;
 		}
 	}
 	
 	public GateID getGateNumber() throws NetworkException
 	{
 		if(mGate != null) return mGate.getGateID();
 		else {
 			if(mReplacementFor != null) return mReplacementFor.getGateID();
 			else throw new NetworkException(this, "No gate number available. Call create before.");
 		}
 	}
 
 	@Override
 	protected void finished()
 	{
 		// deleting gate
 		mLogger.log(this, "removing gate " +mGate);
 		
 		if(mGate != null) {
 			synchronized(getBase()) {
 				mGate.shutdown();
 				
 				getBase().unregisterGate(mGate);
 				mGate = null;
 			}
 		}
 		
 		super.finished();
 	}
 	
 
 	@Viewable("Gate")
 	protected AbstractGate mGate;
 	
 	@Viewable("Peer identity")
 	private Identity mPeerIdentity;
 	
 	private AbstractGate mReplacementFor;
 }
