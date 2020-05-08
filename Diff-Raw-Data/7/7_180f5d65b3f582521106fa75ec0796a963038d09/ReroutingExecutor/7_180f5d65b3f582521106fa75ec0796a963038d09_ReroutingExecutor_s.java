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
 package de.tuilmenau.ics.fog.application;
 
 import java.io.Serializable;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.events.Event;
 import de.tuilmenau.ics.fog.facade.events.ServiceDegradationEvent;
 import de.tuilmenau.ics.fog.packets.statistics.ReroutingTestAgent;
 import de.tuilmenau.ics.fog.scripts.RerouteScript;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.manager.Controller.RerouteMethod;
import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.SimpleName;
 
 public class ReroutingExecutor extends Application
 {
 	private Name mName = null;
 	private Service mServerSocket = null;
 	private LinkedList<Session> mSessions;
 	
 	public ReroutingExecutor(Node pNode, Identity pIdentity)
 	{
 		super(pNode.getHost(), pIdentity);
 	
 		mName = new SimpleName(new Namespace("rerouting"), pNode.toString());
 	}
 	
 	@Override
 	protected void started()
 	{
 		try {
 			Binding tBinding = getHost().bind(null, mName, getDescription(), getIdentity());
 			
 			// create object, which adds EchoService objects
 			// to each incoming connection
 			mServerSocket = new Service(false, null)
 			{
 				public void newConnection(Connection pConnection)
 				{
 					Session tSession = new ReroutingSession(false);
 					
 					// start event processing
 					tSession.start(pConnection);
 					
 					// add it to list of ongoing connections
 					if(mSessions == null) {
 						mSessions = new LinkedList<Session>();
 					}
 					mSessions.add(tSession);
 				}
 			};
 			mServerSocket.start(tBinding);
 		}
 		catch (NetworkException tExc) {
 			terminated(tExc);
 		}
 	}
 
 	@Override
 	public synchronized void exit()
 	{
 		if(isRunning()) {
 			if(mSessions != null) {
 				while(!mSessions.isEmpty()) {
 					mSessions.getFirst().closed();
 				}
 				mSessions.clear();
 			}
 			
 			mServerSocket.stop();
 			mServerSocket = null;
 			terminated(null);
 		}
 	}
 
 	@Override
 	public boolean isRunning()
 	{
 		return(mServerSocket != null);
 	}
 
 	/**
 	 * Session object handling the events for a connection.
 	 */
 	public class ReroutingSession extends Session implements Remote
 	{
 		private boolean mSource;
 		
 		public ReroutingSession(boolean pSource)
 		{
 			super(false, mLogger, null);
 			mSource = pSource;
 		}
 		
 		@Override
 		public boolean receiveData(Object pData)
 		{
 			return true;
 		}
 		
 		// called on error and normal shutdown
 		public void stop()
 		{
 			super.stop();
 			
 			if(mSessions != null) {
 				mSessions.remove(this);
 			}
 		}
 		
 		public void sendData(Serializable pData) throws RemoteException, NetworkException
 		{
 			getConnection().write(pData);
 		}
 		
 		@Override
 		protected void unknownEvent(Event event)
 		{
 			if(event instanceof ServiceDegradationEvent) {
				Logging.log(this, "Received Service Degradation Event");
 				ReroutingTestAgent tPacket = new ReroutingTestAgent();
 				tPacket.setSourceNode(RerouteScript.getCurrentInstance().getExperiment().getSource());
 				tPacket.setDestNode(RerouteScript.getCurrentInstance().getExperiment().getTarget());
 				tPacket.setBrokenType(RerouteScript.getCurrentInstance().getExperiment().getCurrentBrokenType());
 				tPacket.setRerouteMethod(RerouteMethod.GLOBAL);
 				try {
 					sendData(tPacket);
 				} catch (RemoteException tExc) {
 					mLogger.err(this, "Unable to send data after rerouting" + tPacket, tExc);
 				} catch (NetworkException tExc) {
 					mLogger.err(this, "Unable to send data after rerouting" + tPacket, tExc);
 				}
 			}
 		}
 		
 		@Override
 		public void connected()
 		{
 			mLogger.log(this, "Session is now connected");
 			try {
 				if(mSource) {
 					RerouteScript.getCurrentInstance().getExperiment().tell(null);
 				}
 			} catch (RemoteException tExc) {
 				mLogger.err(this, "Unable to notify rerouting executor about establishment of connection", tExc);
 			}
 		}
 	};
 }
