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
 package de.tuilmenau.ics.fog.packets;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ClientFN;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ConnectionEndPoint;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ServerFN;
 import de.tuilmenau.ics.fog.transfer.manager.Process;
 import de.tuilmenau.ics.fog.transfer.manager.ProcessConnection;
 import de.tuilmenau.ics.fog.transfer.manager.ProcessGateCollectionConstruction;
 import de.tuilmenau.ics.fog.transfer.manager.Process.ProcessState;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Viewable;
 
 
 /**
  * Used as payload in signaling messages to build up a connection.
  */
 public class PleaseOpenConnection extends SignallingRequest
 {
 	
 	private static final long serialVersionUID = 1488705063865574155L;
 	
 	/**
 	 * Initial connection request.
 	 * 
 	 * @param pCallbackName The name of the connection demanding callback.
 	 * 
 	 * @param pSendersProcess The the constructing process of the sender of this
 	 * connection request.
 	 * 
 	 * @param pDescription The requirements for the connection.
 	 */
 	public PleaseOpenConnection(String pCallbackName, ProcessGateCollectionConstruction pSendersProcess, Description pDescription, boolean pToApplication)
 	{
 		super(pSendersProcess != null ? pSendersProcess.getID() : 0);
 		
 		mConnectionInitiatorName = pCallbackName;
 		if(mConnectionInitiatorName == null) {
 			mConnectionInitiatorName = "?*";
 		}
 		incCounter(Index.INSTANCE_COUNTER);
 		mSendersProcessNumber = pSendersProcess != null ? pSendersProcess.getID() : 0;
 		mReceiversProcessNumber = 0;
 		
 		setDescription(pDescription);
 		
 		if(pSendersProcess != null) {
 			mSendersRouteUpToHisClient = pSendersProcess.getRouteUpToClient();
 			
 			ForwardingNode tFN = pSendersProcess.getBase(); 
 			mPeerRoutingName = tFN.getEntity().getRoutingService().getNameFor(tFN);
 		}
 		mToApplication = pToApplication;
 		//Logging.log(this, "Create PleaseOpenConnection1 towards: " + mPeerRoutingName);
 	}
 	
 	/**
 	 * Connection request as response to an received one.
 	 * 
 	 * @param pPredecessor The connection request preceding this one.
 	 * 
 	 * @param pSendersProcess The the constructing process of the sender of this
 	 * connection request.
 	 * 
 	 * @param pDescription The (new) requirements for the connection.
 	 */
 	private PleaseOpenConnection(PleaseOpenConnection pPredecessor, ProcessGateCollectionConstruction pSendersProcess, Description pDescription)
 	{
 		super(pSendersProcess != null ? pSendersProcess.getID() : 0);
 		
 		mConnectionInitiatorName = pPredecessor.mConnectionInitiatorName;
 		if(mConnectionInitiatorName == null) {
 			mConnectionInitiatorName = "?*";
 		}
 		incCounter(Index.INSTANCE_COUNTER);
 		mSendersProcessNumber = pSendersProcess != null ? pSendersProcess.getID() : 0;
 		mReceiversProcessNumber = pPredecessor.mSendersProcessNumber;
 		
 		mSendersRouteUpToHisClient = pSendersProcess.getRouteUpToClient();
 		
 		ForwardingNode tFN = pSendersProcess.getBase(); 
 		mPeerRoutingName = tFN.getEntity().getRoutingService().getNameFor(tFN);
 		setDescription(pDescription);
 		//Logging.log(this, "Create PleaseOpenConnection2 towards: " + mPeerRoutingName);
 	}
 	
 	/**
 	 * Short constructor for a dummy signaling message. The signal workflow
 	 * will create a connection end point locally and do not send any
 	 * signaling answers.
 	 * 
 	 * @param pDescription Requirements for the connection end point.
 	 */
 	public PleaseOpenConnection(Description pDescription)
 	{
 		this(null, null, pDescription, true);
 		
 		mIsLocalDummy = true;
 		//Logging.log(this, "Create PleaseOpenConnection3 towards: " + mPeerRoutingName);
 	}
 	
 	/**
 	 * @return The reference number to the constructing
 	 * process of the sender of this connection request.
 	 */
 	protected int getSendersProcessNumber()
 	{
 		return getProcessNumber();
 	}
 	
 	/**
 	 * @return The reference number to the constructing
 	 * process of the receiver of this connection request.
 	 */
 	protected int getReceiversProcessNumber()
 	{
 		return mReceiversProcessNumber;
 	}
 	
 	@Override
 	public boolean execute(ForwardingNode pFN, Packet pPacket, Identity pRequester)
 	{
 		pFN.getEntity().getLogger().log(this, "execute open connection request on " +pFN + " from reverse node process " +getSendersProcessNumber());
 		
 		Process tProcess = null;
 		try {
 			if(!(pFN instanceof Multiplexer)) {
 				throw new NetworkException("open connection request can only be executed at multiplexing FNs\n   -> current FN is: " + pFN + "\n    -> signaling packet: " + pPacket);
 			}
 			if(pPacket == null) {
 				throw new NetworkException("missing packet argument to execute open connection request");
 			}
 			
 			Route tPacketReturnRoute = pPacket.getReturnRoute();
 			if(tPacketReturnRoute == null || tPacketReturnRoute.isEmpty()) {
				Logging.warn(this, "Found missing return route for connect() packet: " + pPacket);
				//Logging.err(this, "  ..source is: " + pPacket.getSenderAuthentication());
				Logging.warn(this, "  ..source is: " + mPeerRoutingName);
				Logging.warn(this, "  ..connection initiator: " + mConnectionInitiatorName);
				Logging.warn(this, "  ..forward route is: " + pPacket.getRoute());
 				throw new NetworkException("missing packet return route");
 			}
 			
 			tProcess = pFN.getEntity().getProcessRegister().getProcess(pFN, pRequester, getReceiversProcessNumber());
 			if(tProcess != null) {
 				// Returning (maybe requirement-changing) connection request.
 				
 				if(tProcess.getState() == ProcessState.CLOSING) {
 					throw new NetworkException("Connection process " +tProcess +" was closed.");
 				}
 
 				if(tProcess instanceof ProcessGateCollectionConstruction) {
 					ProcessGateCollectionConstruction mReceiversProcess = (ProcessGateCollectionConstruction) tProcess;
 					
 					// Check base FN identity.
 					if(mReceiversProcess.getBase() == pFN) {
 						
 						/* *****************************************************
 						 *  Use and complete the known instance of
 						 *  ProcessConnection.
 						 ******************************************************/
 						
 						mReturnRouteFromBaseFN = tPacketReturnRoute;
 						
 						Packet tRes = completeConnectionProcess(mReceiversProcess, pRequester);
 						signAndSend(pFN, tRes);
 						
 					} else {
 						throw new NetworkException("found referenced process " +tProcess.getID() + " starts at different forwarding node" );
 					}
 					
 				} else {
 					// Wrong type origin process.
 					throw new NetworkException("process " +tProcess.getID() + " (" +tProcess.getClass().getSimpleName() + ") is no instance of class ProcessSocketConstruction needed for open connection request execution" );
 				}
 			} else {
 				// Initial connection request.
 				mReturnRouteFromBaseFN = tPacketReturnRoute;
 				
 				if(mToApplication) {
 					if(pFN instanceof ServerFN) {
 						ServerFN tServerFN = (ServerFN) pFN;
 						
 						if(Config.Connection.SERVER_REDIRECT_TO_MULTIPLEXER) {
 							
 							// Start the connection socket at central multiplexer.
 							Multiplexer mNewBaseFN = redirectToMultiplexer(tServerFN);
 		
 							if(mNewBaseFN != null) {
 								pFN = mNewBaseFN;
 							} else {
 								pFN.getEntity().getLogger().log(this, "can not redirect from server " +pFN + " to central multiplexer");
 							}
 							
 						}
 						
 						/* *************************************************************
 						 *  Create and start new instance of ProcessConnection.
 						 **************************************************************/
 						ProcessConnection tProcessConn = createAndStartConnectionProcess(pFN, pRequester);
 						tProcess = tProcessConn;
 						if(tProcess == null) {
 							throw new NetworkException("Connection process creation failed.");
 						}
 						
 						// inform app about new connection
 						ConnectionEndPoint tCEP = new ConnectionEndPoint(tServerFN.getName(), pFN.getEntity().getLogger(), pPacket.getAuthentications());
 						ClientFN tFN = tProcessConn.getEndForwardingNode();
 						
 						tCEP.setForwardingNode(tFN);
 						tFN.setConnectionEndPoint(tCEP);
 						tServerFN.addNewConnection(tCEP);
 						
 						// are we just a dummy?
 						// -> send packet to connection end point
 						if(mIsLocalDummy) {
 							pPacket.getRoute().addLast(tProcessConn.getRouteUpToClient());
 							pFN.handlePacket(pPacket, null);
 						}
 						
 					} else {
 						// Connection request can also arrive at a
 						// MultiplexerGate that is no ServerGate!
 						// -> Need to know target service. -> Workaround.
 						// -> TODO Search for relevant server and try to connect.
 						
 						throw new NetworkException("Destination forwarding node " +pFN +" is not attached to a higher layer, source of request is " + pRequester.getName());
 					}
 				} else {
 					/* *************************************************************
 					 *  Create and start new instance of ProcessConnection.
 					 **************************************************************/
 					tProcess = createAndStartGateProcess(pFN, pFN, pRequester);
 					if(tProcess == null) {
 						throw new NetworkException("Connection process creation failed.");
 					}
 				}
 			}
 			incCounter(Index.POSITIVE_EXECUTION_COUNTER);
 			return true;
 		}
 		catch(NetworkException ne) {
 			// Log the error.
 			pFN.getEntity().getLogger().err(this, "Error during execution of open request on " +pFN, ne);
 			
 			// send error reply back
 			if(pFN != null) {
 				//TODO Differenciation between internal and real network exception
 				// needed to prevent posting internals to remote system.
 				Packet packet = new Packet(pPacket.getReturnRoute(), new OpenConnectionResponse(this, getReceiversProcessNumber(), ne));
 				
 				signAndSend(pFN, packet);
 			}
 			
 			if(tProcess != null && tProcess.getState() != null && !tProcess.isFinished()) {
 				// De-construct existing elements.
 				tProcess.terminate(ne);
 			}
 			
 			incCounter(Index.NEGATIVE_EXECUTION_COUNTER);
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * Create and start the connection process.
 	 * 
 	 * @param pReceiveCallback The local {@link ReceiveCallback} instance to use.
 	 * @param pPacket The packet containing the connection request.
 	 * 
 	 * @return The created local connection process.
 	 * @throws NetworkException on error
 	 */
 	@SuppressWarnings("unused")
 	private ProcessConnection createAndStartConnectionProcess(ForwardingNode pBaseFN, Identity pRequester) throws NetworkException
 	{
 		// Create construction process.
 		ProcessConnection tReceiversProcess = new ProcessConnection(pBaseFN, null, getDescription(), pRequester);
 		
 		// Create and register client FN.
 		tReceiversProcess.start();
 		mReceiversProcessNumber = tReceiversProcess.getID();
 		
 		if(Config.Connection.LAZY_REQUEST_RECEIVER && !mIsLocalDummy) {
 			// Socket-path will not be created before next handshake arrives. 
 			
 			// Send an answer.
 			Description tReturnDescription = getDescription().calculateDescrForRemoteSystem();
 
 			Packet packet = new Packet(mReturnRouteFromBaseFN, new PleaseOpenConnection(this, tReceiversProcess, tReturnDescription));
 			
 			signAndSend(pBaseFN, packet);
 			
 		} else {
 			// Build up socket path.
 			Packet answer = completeConnectionProcess(tReceiversProcess, pRequester);
 			
 			if(!mIsLocalDummy) {
 				signAndSend(tReceiversProcess.getBase(), answer);
 			}
 		}
 		
 		if(Config.Connection.TERMINATE_WHEN_IDLE) {
 			tReceiversProcess.activateIdleTimeout();
 		}
 		
 		if(Config.Connection.SEND_KEEP_ALIVE_MESSAGES_WHEN_IDLE) {
 			tReceiversProcess.activateKeepAlive();
 		}
 
 		return tReceiversProcess;
 	}
 	
 	/**
 	 * Create and start the connection process.
 	 * 
 	 * @param pReceiveCallback The local {@link ReceiveCallback} instance to use.
 	 * @param pPacket The packet containing the connection request.
 	 * 
 	 * @return The created local connection process.
 	 * @throws NetworkException on error
 	 */
 	private ProcessGateCollectionConstruction createAndStartGateProcess(ForwardingNode pBaseFN, ForwardingNode pOutgoingFN, Identity pRequester) throws NetworkException
 	{
 		// Create construction process.
 		ProcessGateCollectionConstruction tReceiversProcess = new ProcessGateCollectionConstruction(pBaseFN, pOutgoingFN, getDescription(), pRequester);
 				
 		// Create and register client FN.
 		tReceiversProcess.disableHorizontal();
 		tReceiversProcess.start();
 		mReceiversProcessNumber = tReceiversProcess.getID();
 
 		// Build up socket path.
 		Packet answer = completeConnectionProcess(tReceiversProcess, pRequester);
 		signAndSend(tReceiversProcess.getBase(), answer);
 
 		if(Config.Connection.TERMINATE_WHEN_IDLE) {
 			tReceiversProcess.activateIdleTimeout();
 		}
 
 		return tReceiversProcess;
 	}
 	
 	/**
 	 * Call an existing process to (re)create the socket-path.
 	 * 
 	 * @param pPacket The packet containing the connection request.
 	 * 
 	 * @throws NetworkException on error
 	 */
 	private Packet completeConnectionProcess(ProcessGateCollectionConstruction pReceiversProcess, Identity pRequester) throws NetworkException
 	{
 		if(pReceiversProcess == null || pReceiversProcess.getState() == null || pReceiversProcess.isFinished()) {
 			throw new NetworkException("Connection process failed.");
 		}
 		
 		/* *********************************************************************
 		 * (Re-)Build socket path.
 		 **********************************************************************/
 		pReceiversProcess.recreatePath(getDescription(), mReturnRouteFromBaseFN);
 		
 		if(mSendersRouteUpToHisClient != null) {
 			
 			/* *****************************************************************
 			 * Path from remote base FN to remote client FN available so
 			 * horizontal tunnel gate can be updated with route and consequently
 			 * the own process can be finished.
 			 * -> Reply with an OpenConnectionResponse.
 			 ******************************************************************/
 			
 			// Update the route the client leaving gate should use.
 			pReceiversProcess.updateRoute(mReturnRouteFromBaseFN, mSendersRouteUpToHisClient, mPeerRoutingName, pRequester);
 			
 			// Send an answer;
 			ForwardingNode tFN = pReceiversProcess.getBase(); 
 			Name tLocalServiceName = tFN.getEntity().getRoutingService().getNameFor(tFN);
 			
 			Packet packet = new Packet(mReturnRouteFromBaseFN, new OpenConnectionResponse(this, pReceiversProcess, tLocalServiceName));
 			return packet;
 			
 		} else {
 			
 			/* *****************************************************************
 			 * No path from remote base FN to remote client FN available so
 			 * can not update route for horizontal tunnel gate and consequently
 			 * not finish the own process as well.
 			 * -> Reply with an additional PleaseOpenConnection.
 			 ******************************************************************/
 
 			Description tReturnDescription = getDescription().calculateDescrForRemoteSystem();
 
 			Packet packet = new Packet(mReturnRouteFromBaseFN, new PleaseOpenConnection(this, pReceiversProcess, tReturnDescription));
 			return packet;
 		}
 	}
 	
 	/**
 	 * Try to replace the actual starting point by central multiplexer.
 	 * 
 	 * @return {@code true} if redirection could be initialized and
 	 * {@code false} if former base and routes will still be used.
 	 */
 	private Multiplexer redirectToMultiplexer(ForwardingNode mBaseFN)
 	{
 		FoGEntity tEntity = mBaseFN.getEntity();
 		Multiplexer tMux = tEntity.getCentralFN();
 		
 		if(tMux == mBaseFN) {
 			tEntity.getLogger().debug(this, "server-self-redirection skipped");
 			return null;
 		}
 		
 		Name tMuxName = tMux.getEntity().getRoutingService().getNameFor(tMux);
 		if(tMux == mBaseFN) {
 			tEntity.getLogger().warn(this, "unknown central multiplexer");
 			return null;
 		}
 		
 		// Packets return route should run through local central
 		// MultiplexerGate. The path from ServerGate to central
 		// MultiplexerGate has to be removed from packets return
 		// route to guide the response-packet from MultiplexerGate
 		// to remote system.
 		
 		Route tReturnRoute = new Route(mReturnRouteFromBaseFN);
 		
 		RouteSegment tRouteSegment	= tReturnRoute.removeFirst();
 		if(tRouteSegment == null) {
 			// No reverse route.
 			return null;
 		}
 		
 		if(!(tRouteSegment instanceof RouteSegmentPath)) {
 			// No local internal path in reverse route.
 			return null;
 		}
 		
 		// Get to know the first path in packets return route.
 		RouteSegmentPath tReturnRouteFirstPath = new RouteSegmentPath((RouteSegmentPath)tRouteSegment);
 		if(tReturnRouteFirstPath.isEmpty()) {
 			// Internal path in reverse route is empty.
 			return null;
 		}
 		
 		
 		// Get to know the internal route from server to multiplexer.
 		Route tRouteToMux = null;
 		try {
 			tRouteToMux = mBaseFN.getEntity().getTransferPlane().getRoute(mBaseFN, tMuxName, null, null);
 		} catch (NetworkException e) {}
 		
 		if(tRouteToMux == null) {
 			// Unknown route from server to multiplexer.
 			return null;
 		}
 		
 		tRouteSegment = null;
 		RouteSegmentPath tPathToMux = new RouteSegmentPath();
 		
 		while(!tRouteToMux.isEmpty() && (tRouteSegment = tRouteToMux.removeFirst()) != null)
 		{
 			if(tRouteSegment instanceof RouteSegmentPath) {
 				tPathToMux.addAll((RouteSegmentPath) tRouteSegment);
 			} else {
 				// Internal route segment is no path segment.
 				return null;
 			}
 		}
 		
 		if(tPathToMux.isEmpty()) {
 			// No internal path from server to central multiplexer.
 			return null;
 		}
 		
 		// Remove the path from server to multiplexer from return path.
 		while(!tReturnRouteFirstPath.isEmpty() && !tPathToMux.isEmpty())
 		{
 			if(!tReturnRouteFirstPath.removeFirst().equals(tPathToMux.removeFirst())) {
 				// Different path to mux or server is connected to other mux.
 				return null;
 			}
 		}
 		
 		if(!tPathToMux.isEmpty()) {
 			// Path to mux longer than internal reverse path from server to mux.
 			return null;
 		}
 		
 		if(!tReturnRouteFirstPath.isEmpty()) {
 			tReturnRoute.addFirst(tReturnRouteFirstPath);
 		}
 		
 		// Switch base from server to mux.
 		mReturnRouteFromBaseFN = tReturnRoute;
 		
 		return tMux;
 	}
 	
 	/**
 	 * @return The description for the demanded Connection including
 	 * functional requirements in well-defined order.
 	 */
 	protected Description getDescription()
 	{
 		return mDescription;
 	}
 	
 	/**
 	 * @param pDescription The description for the demanded connection to set.
 	 * May include functional requirements in well-defined order.
 	 */
 	private void setDescription(Description pDescription)
 	{
 		mDescription = pDescription;
 		if(mDescription == null) {
 			mDescription = new Description();
 		}
 	}
 	
 	/**
 	 * @return The route starting at senders base FN and ending at his client FN.
 	 */
 	protected Route getSendersRouteUpToHisClient()
 	{
 		return mSendersRouteUpToHisClient;
 	}
 	
 	/**
 	 * @param pSendersRouteUpToHisClient The route starting at senders base FN
 	 * and ending at his client FN.
 	 */
 	public void setSendersRouteUpToHisClient(Route pSendersRouteUpToHisClient)
 	{
 		this.mSendersRouteUpToHisClient = pSendersRouteUpToHisClient;
 	}
 	
 	@Override
 	public String toString()
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append(super.toString());
 		sb.append('(');
 		sb.append("[S_Proc: ");
 		sb.append(mSendersProcessNumber);
 		sb.append(']');
 		sb.append("[S_RouteUp: ");
 		sb.append(mSendersRouteUpToHisClient);
 		sb.append(']');
 		sb.append("[R_Proc: ");
 		sb.append(mReceiversProcessNumber);
 		sb.append(']');
 
 /*		sb.append("[R_FNName: ");
 		sb.append(mBaseFNName);
 		sb.append(']');
 		sb.append("[R_Svc: ");
 		sb.append(mServiceName);
 		sb.append(']');
 */
 		sb.append("[Descr: ");
 		sb.append(mDescription);
 		sb.append(']');
 		sb.append(')');
 		return sb.toString();
 	}
 	
 	/**
 	 * @param pClientName The name of the client that instantiated the signal(s).
 	 * 
 	 * @return Total number of instances created at given node.
 	 * 
 	 * <br/>As every static field relative to concrete JavaVM and ClassLoader.
 	 */
 	public static long getInstanceCounter(String pClientName)
 	{
 		return getCounter(pClientName, Index.INSTANCE_COUNTER);
 	}
 	
 	/**
 	 * @param pClientName The name of the client that instantiated the signal(s).
 	 * 
 	 * @return Total number of executions called at given node returned true.
 	 * 
 	 * <br/>As every static field relative to concrete JavaVM and ClassLoader.
 	 */
 	public static long getExecutedPositiveCounter(String pNodeName)
 	{
 		return getCounter(pNodeName, Index.POSITIVE_EXECUTION_COUNTER);
 	}
 	
 	/**
 	 * @param pClientName The name of the client that instantiated the signal(s).
 	 * 
 	 * @return Total number of executions called at given node returned false.
 	 * 
 	 * <br/>As every static field relative to concrete JavaVM and ClassLoader.
 	 */
 	public static long getExecutedNegativeCounter(String pNodeName)
 	{
 		return getCounter(pNodeName, Index.NEGATIVE_EXECUTION_COUNTER);
 	}
 	
 	/**
 	 * @param pClientName The name of the client that instantiated the signal(s).
 	 * @param pIndex The Index given by {@link Index#INSTANCE_COUNTER},
 	 * {@link Index#POSITIVE_EXECUTION_COUNTER} and
 	 * {@link Index#NEGATIVE_EXECUTION_COUNTER}.
 	 * 
 	 * @return The counter per node and index.
 	 */
 	private static long getCounter(String pNodeName, Index pIndex)
 	{
 		if(pNodeName == null) {
 			pNodeName = "?*";
 		}
 		if(pIndex != null && sCounterMap != null) {
 			long[] tCounterArray = sCounterMap.get(pNodeName);
 			if(tCounterArray != null) {
 				return tCounterArray[pIndex.ordinal()];
 			}
 		}
 		return 0L;
 	}
 	
 	/**
 	 * Increments counter relative to the name of the client that
 	 * instantiated the Connection.
 	 * 
 	 * @param pIndex The Index given by {@link Index#INSTANCE_COUNTER},
 	 * {@link Index#POSITIVE_EXECUTION_COUNTER} and
 	 * {@link Index#NEGATIVE_EXECUTION_COUNTER}.
 	 */
 	private void incCounter(Index pIndex)
 	{
 		if(pIndex != null) {
 			long[] tCounterArray = null;
 			if(sCounterMap == null) {
 				sCounterMap = new HashMap<String, long[]>();
 			} else {
 				tCounterArray = sCounterMap.get(mConnectionInitiatorName);
 			}
 			if(tCounterArray == null) {
 				tCounterArray = new long[]{0, 0, 0};
 				sCounterMap.put(mConnectionInitiatorName, tCounterArray);
 			}
 			tCounterArray[pIndex.ordinal()]++;
 			
 		}
 	}
 	
 	public String getConnectionInitiatorName()
 	{
 		return mConnectionInitiatorName;
 	}
 	
 	
 	/* *************************************************************************
 	 * Members
 	 **************************************************************************/
 	
 	
 	/** The route starting at senders base FN and ending at his client FN. */
 	@Viewable("Senders route up to its client")
 	private Route mSendersRouteUpToHisClient;
 	
 	/**
 	 * The route starting at receivers (local) {@code mBaseFN} and ending at
 	 * senders base FN to answer this request.
 	 */
 	@Viewable("Return route from base FN")
 	private Route mReturnRouteFromBaseFN;
 	
 	/** 
 	 * The description for the demanded Connection.
 	 * May include functional requirements in well-defined order.
 	 */
 	@Viewable("Description")
 	private Description mDescription;
 	
 	/** Receivers related process number. */
 	@Viewable("Receivers process number")
 	private int mReceiversProcessNumber;
 	
 	/** Senders related process number just for gui. */
 	@Viewable("Senders process number")
 	private int mSendersProcessNumber;
 	
 	/** Name of connection initiating client. */
 	@Viewable("Connection initiator name")
 	private String mConnectionInitiatorName = null;
 	
 	/** Routing name of the responding entity */
 	@Viewable("Peer routing name")
 	private Name mPeerRoutingName;
 	
 	@Viewable("To application")
 	private boolean mToApplication;
 	
 	@Viewable("Local dummy for creating connection")
 	private boolean mIsLocalDummy = false;
 	
 	
 	/* *************************************************************************
 	 * Static fields
 	 **************************************************************************/
 	
 	
 	private static enum Index {
 		/** Index of the total number of instances created. */
 		INSTANCE_COUNTER,
 		/** Index of the total number of executions returned true. */
 		POSITIVE_EXECUTION_COUNTER,
 		/** Index of the total number of executions returned false. */
 		NEGATIVE_EXECUTION_COUNTER;
 	}
 	
 	/** Map with counter for instances and positive and negative executions. */
 	private static volatile Map<String, long[]> sCounterMap;
 }
