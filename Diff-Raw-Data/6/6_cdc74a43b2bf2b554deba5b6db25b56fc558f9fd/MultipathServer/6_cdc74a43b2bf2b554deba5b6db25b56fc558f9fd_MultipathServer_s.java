 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - App
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.app.multipath;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.application.Application;
 import de.tuilmenau.ics.fog.application.InterOpIP;
 import de.tuilmenau.ics.fog.application.Service;
 import de.tuilmenau.ics.fog.application.Session;
 import de.tuilmenau.ics.fog.application.InterOpIP.Transport;
 import de.tuilmenau.ics.fog.application.interop.ConnectionEndPointUDPProxy;
 import de.tuilmenau.ics.fog.exceptions.InvalidParameterException;
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Host;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.ui.Viewable;
 import de.tuilmenau.ics.fog.util.SimpleName;
 
 
 /**
  * Server, which forwards multipath data to an external destination.
  * The data is relayed to an IP based destination. For this purpose,
  * it uses a UDPServerProxy.
  *  
  */
 public class MultipathServer extends Application implements Connection
 {
 	public static final boolean DEBUG_SCTP_IO = false;
 	public static final boolean DEBUG_PACKETS = false;
 	public static final boolean DEBUG_PACKETS_DATA = false;
 	public static final String NAME_MULTIPATH_IP_BRIDGE_SERVER = "ServerBridge";
 	public static final Namespace NAMESPACE_MULTIPATH = new Namespace("mp");
 	
 	
 	public MultipathServer(Host pLocalHost, Identity pOwnIdentity, String pDestinationIp, int pDestinationPort, Transport pIpTransport)
 	{
 		super(pLocalHost, pOwnIdentity);
 		
 		mDestinationIp = pDestinationIp;
 		mDestinationPort = pDestinationPort;
 		mTransport = pIpTransport;
 				
 		try {
 			mServerName = SimpleName.parse(NAMESPACE_MULTIPATH + "://" + NAME_MULTIPATH_IP_BRIDGE_SERVER);
 		} catch (InvalidParameterException tExc) {
 			getLogger().err(this, "Invalid Name parameter", tExc);
 		}
 		
 		mIpDestination = mDestinationIp + ":" + mDestinationPort + "(" + pIpTransport + ")"; 
 	}
 	
 	@Override
 	protected void started() 
 	{
 		// create FoG connection towards IP destination
 		try {
 			if (mTransport ==  InterOpIP.Transport.UDP){
 				mClientCEP2IP = new ConnectionEndPointUDPProxy(mLogger, mDestinationIp, mDestinationPort, 0);
 				mClientCEP2IP.start(this);
 			}else
 			{
 				mLogger.err(this, "TCP is not supported by SCTP encapsulation");
 				//mCEP2IP = new ConnectionEndPointTCPProxy(mLogger, pDestinationIp, pDestinationPort);
 			}
 		} catch (Exception tExc) {
 			mLogger.err(this, "Unable to create CEP towards SCTP destination because \"" + tExc.getMessage() + "\"", tExc);
 		} 
 		
 		// provide the service within the FoG network
 		try {
 			Binding tBinding = getHost().bind(null, mServerName, getDescription(), getIdentity());
 			
 			// per incoming connection
 			mServerSocket = new Service(false, null)
 			{
 				public void newConnection(Connection pConnection)
 				{
 					Session tSession = new SctpClientSession();
 					
 					// start event processing
 					tSession.start(pConnection);
 					
 					// add it to list of ongoing connections
 					mSctpClientSessions.add(tSession);
 				}
 			};
 			mServerSocket.start(tBinding);
 		}
 		catch (NetworkException tExc) {
 			terminated(tExc);
 		}
 	}
 
 	@Override
 	public boolean isRunning() 
 	{
 		return(mServerSocket != null);
 	}
 	
 	private boolean receivedSCTP(byte[] pPayload) 
 	{
 		boolean tResult = true;
 		
 		if (DEBUG_PACKETS) {
 			mLogger.log(this, "Received SCTP data of " + pPayload.length + " bytes");
 		}
 		if (DEBUG_PACKETS_DATA) {
 			mLogger.log(this, "Received SCTP data " + pPayload.toString());
 		}
 		
 		mReceivedSctpPackets++;
 		mReceivedSctpBytes +=  pPayload.length;
 		
 		if (MultipathServer.DEBUG_PACKETS_DATA)
 			SCTP.parsePacket(pPayload);
 
 		int tStreamId = SCTP.getStreamIdFromPacket(pPayload);
 		if (MultipathServer.DEBUG_SCTP_IO) {
 			getLogger().log(this, "Got " + pPayload.length + " bytes, destination port " + SCTP.getDestinationPort(pPayload) + ", stream ID " + tStreamId + ", type " + SCTP.getChunkType(pPayload));
 		}
 
 		synchronized(mSctpClientSessions) {
 			if (mSctpClientSessions.size() > 0) {
 				for (Session tSession: mSctpClientSessions) {
 					// TODO: filter stream ID and select right connection
 					if (tSession instanceof SctpClientSession) {
 						SctpClientSession tSctpClientSession = (SctpClientSession)tSession;
 						tResult &= tSctpClientSession.sendtoMultipathClient(pPayload);				
 					}
 					break; //TODO: at the moment we answer via the first connection
 				}
 			}else
 				mLogger.err(this, "No valid FoG connection towards Multipath client");
 		}
 		
 		return tResult;
 	}
 
 	private boolean sendSCTP(byte[] pPayload)
 	{
 		boolean tResult = false;
 		
 		if (DEBUG_PACKETS) {
 			mLogger.log(this, "Sending SCTP data of " + pPayload.length + " bytes");
 		}
 		if (DEBUG_PACKETS_DATA) {
 			mLogger.log(this, "Sending SCTP data " + pPayload.toString());
 		}
 
 		mSentSctpPackets++;
 		mSentSctpBytes +=  pPayload.length;
 
 		tResult = mClientCEP2IP.receiveData(pPayload);
 
 		return tResult;		
 	}
 	
 	public int countReceivedSctpBytes()
 	{
 		return mReceivedSctpBytes;
 	}
 	
 	public int countReceivedSctpPackets()
 	{
 		return mReceivedSctpPackets;
 	}
 
 	public int countSentSctpBytes()
 	{
 		return mSentSctpBytes;
 	}
 	
 	public int countSentSctpPackets()
 	{
 		return mSentSctpPackets;
 	}
 	
 	public int getListenerPort()
 	{
 		return mClientCEP2IP.getLocalPort();
 	}
 	
 	public String getIpDestination()
 	{
 		return mIpDestination;
 	}
 	
 	public synchronized void exit()
 	{
 		if(isRunning()) {
 			if(mSctpClientSessions != null) {
 				while(!mSctpClientSessions.isEmpty()) {
 					mSctpClientSessions.getFirst().closed();
 				}
 				mSctpClientSessions.clear();
 			}
 			
 			mServerSocket.stop();
 			mServerSocket = null;
 			terminated(null);
 		}
 	}
 
 	/**
 	 * Session object handling the events for a connection.
 	 */
 	class SctpClientSession extends Session
 	{
 		public SctpClientSession()
 		{
 			super(false, mLogger, null);
 		}
 		
 		@Override
 		public boolean receiveData(Object pData)
 		{
 			boolean tResult = false;
 			
 			if (!(pData instanceof byte[]))
 			{
 				getLogger().warn(this, "Malformed data from multipath client: " + pData);
 				return false;
 			}
 			
 			byte[] tPacketFromMultipathClient = (byte[])pData;
 			
 			if (DEBUG_PACKETS) {
 				getLogger().log(this, "Received from multipath client a packet of " + tPacketFromMultipathClient.length + " bytes");
 			}
 
 			sendSCTP(tPacketFromMultipathClient);
 
 			return tResult;
 		}
 		
 		public boolean sendtoMultipathClient(Serializable pData)
 		{
 			boolean tResult = false;
 			
 			Connection tConnection = getConnection();
 			if(tConnection != null) {
 				try {
 					if(pData instanceof Serializable) {
 						tConnection.write((Serializable) pData);
 					} else {
 						tConnection.write(pData.toString());
 					}
 					tResult = true;
 				}
 				catch(NetworkException tExc) {
 					getLogger().err(this, "Sending SCTP packet to multipath client failed for: " + pData, tExc);
 				}
 			} else {
 				getLogger().warn(this, "No socket towards multipath client available");
 			}
 			
 			return tResult;
 		}
 		
 		public void closed()
 		{
 			super.closed();
 			if(mSctpClientSessions != null) {
 				mSctpClientSessions.remove(this);
 			}
 		}
 	};
 
 	
 	@Viewable("Received SCTP data [bytes]")
 	private int mReceivedSctpBytes = 0;
 	
 	@Viewable("Received SCTP packets")
 	private int mReceivedSctpPackets = 0;
 
 	@Viewable("Sent SCTP data [bytes]")
 	private int mSentSctpBytes = 0;
 	
 	@Viewable("Sent SCTP packets")
 	private int mSentSctpPackets = 0;
 	
 	@Viewable("IP destination")
 	private String mIpDestination = null;
 
 	private ConnectionEndPointUDPProxy mClientCEP2IP = null;
 	private SimpleName mServerName = null;
 	private String mDestinationIp = null;
 	private int mDestinationPort = 0;
 	private Transport mTransport = Transport.UDP;
 	private Service mServerSocket = null;
 	
 	/**
 	 * One client session per connect from MultipathClient. 
 	 */
 	private LinkedList<Session> mSctpClientSessions = new LinkedList<Session>();
 
 	@Override
 	public void registerListener(EventListener observer) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean unregisterListener(EventListener observer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void connect() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isConnected() {
 		return isRunning();
 	}
 
 	@Override
 	public Name getBindingName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public LinkedList<Signature> getAuthentications() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Description getRequirements() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void write(Serializable pData) throws NetworkException {
 		// incoming UDP encapsulation data
 		if (pData instanceof byte[]){
 			byte[] tSCTPData = (byte[])pData;
 			
 //			if (DEBUG_PACKETS) {
 //				mLogger.log(this, "write()-received SCTP data of " + tSCTPData.length + " bytes");
 //			}
 //			if (DEBUG_PACKETS_DATA) {
 //				mLogger.log(this, "write()-received SCTP data " + tSCTPData.toString());
 //			}
 
 			receivedSCTP(tSCTPData);
 		}else{
 			getLogger().warn(this, "Malformed received SCTP packet from UDP listener " + pData);
 		}
 	}
 
 	@Override
 	public Object read() throws NetworkException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public OutputStream getOutputStream() throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public InputStream getInputStream() throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void close() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 }
 
