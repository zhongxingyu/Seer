 package com.robonobo.mina.network.eon;
 
 import static com.robonobo.mina.external.node.EonEndPoint.*;
 
 import java.io.IOException;
 import java.net.*;
 import java.nio.ByteBuffer;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.robonobo.common.async.PushDataReceiver;
 import com.robonobo.common.util.NetUtil;
 import com.robonobo.core.api.proto.CoreApi.EndPoint;
 import com.robonobo.core.api.proto.CoreApi.Node;
 import com.robonobo.eon.*;
 import com.robonobo.mina.external.HandoverHandler;
 import com.robonobo.mina.external.MinaException;
 import com.robonobo.mina.external.node.*;
 import com.robonobo.mina.instance.MinaInstance;
 import com.robonobo.mina.message.proto.MinaProtocol.PublicDetails;
 import com.robonobo.mina.network.*;
 
 public class EonEndPointMgr implements EndPointMgr {
 	private static final int LISTENER_EON_PORT = 23;
 	private static final int LOCAL_LISTENER_EON_PORT = 17;
 	private static final int HANDOVER_EON_PORT = 1;
 	private MinaInstance mina;
 	private EonEndPoint myListenEp;
 	private EonEndPoint gatewayEp;
 	private EonEndPoint natTraversalEp;
 	private EonEndPoint myAllegedPublicEndpoint;
 	private EonEndPoint publicDetailsSource;
 	private boolean natTraversalDecided;
 	private Log log;
 	private EONManager eonMgr;
 	private SEONConnection listenConn;
 	private LocalNodesListener localNodesListener;
 	private HandoverListener handoverListener;
 	private HandoverHandler handoverHandler;
 	boolean localListenerReady = false;
 
 	public EonEndPointMgr() {
 	}
 
 	public void start() throws Exception {
 		log = mina.getLogger(getClass());
 		String myAddr = mina.getConfig().getLocalAddress();
 		if (myAddr == null)
 			throw new MinaException("No local IP address defined");
 		InetAddress myAddress = InetAddress.getByName(myAddr);
 		int udpPortToListen = mina.getConfig().getListenUdpPort();
 		myListenEp = new SeonEndPoint(myAddress, udpPortToListen, LISTENER_EON_PORT);
 		eonMgr = new EONManager("mina", mina.getExecutor(), udpPortToListen);
 		eonMgr.setMaxOutboundBps(mina.getConfig().getMaxOutboundBps());
 		if (mina.getConfig().getGatewayAddress() != null)
 			gatewayEp = new SeonEndPoint(InetAddress.getByName(mina.getConfig().getGatewayAddress()), mina.getConfig()
 					.getGatewayUdpPort(), LISTENER_EON_PORT);
 		// If we're public or have a gateway, we don't need to bugger about with NAT traversal
 		if ((localAddrIsPublic()) || gatewayEp != null)
 			natTraversalDecided = true;
 		log.info("Starting Eon endpoint on " + myListenEp);
 		eonMgr.start();
 		listenConn = eonMgr.createSEONConnection();
 		listenConn.addListener(new IncomingConnectionListener());
 		listenConn.bind(LISTENER_EON_PORT);
 		if (mina.getConfig().getLocateLocalNodes()) {
 			localNodesListener = new LocalNodesListener();
 			localNodesListener.start();
 		}
 		handoverListener = new HandoverListener();
 		handoverListener.start();
 	}
 
 	public void stop() {
 		handoverListener.stop();
 		if (localNodesListener != null)
 			localNodesListener.stop();
 		log.info("Shutting down Eon endpoint on " + myListenEp);
 		listenConn.close();
 		eonMgr.stop();
 	}
 
 	public boolean canConnectTo(Node node) {
 		// If I am public, I can connect to anyone, by asking them to connect to me
 		if(localAddrIsPublic() || gatewayEp != null)
 			return true;
 		for (EndPoint ep : node.getEndPointList()) {
 			if (isEonUrl(ep.getUrl())) {
 				EonEndPoint eonEp = parse(ep.getUrl());
 				if (eonEp instanceof SeonNatTraversalEndPoint) {
 					// Only connect if I support nat traversal
 					if (natTraversalEp != null)
 						return true;
 				} else if (eonEp instanceof SeonEndPoint)
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public ControlConnection connectTo(Node node, List<EndPoint> alreadyTriedEps, boolean indirectAllowed) {
 		// Go through the endpoints of this node. If there is an eon endpoint,
 		// connect to that, otherwise return null.
 		nextEp: for (EndPoint ep : node.getEndPointList()) {
 			if (isEonUrl(ep.getUrl())) {
 				if (alreadyTriedEps != null) {
 					for (EndPoint triedEp : alreadyTriedEps) {
 						if (triedEp.equals(ep))
 							continue nextEp;
 					}
 				}
 				EonEndPoint theirEp = EonEndPoint.parse(ep.getUrl());
 				try {
 					if (!(theirEp instanceof SeonEndPoint))
 						continue;
 					if ((theirEp instanceof SeonNatTraversalEndPoint) && (natTraversalEp != null) && indirectAllowed) {
 						// Only connect if we are behind a NAT too
 						if (natTraversalEp == null)
 							continue;
 						if (indirectAllowed) {
 							// We're traversing NATs at both ends, and we are the initiating node, so we send a 'NAT
 							// seed'. These packets contain no data, but open up the port on our NAT so that when their
 							// connection arrives, it's allowed through
 							eonMgr.sendNATSeed(new InetSocketAddress(theirEp.getAddress(), theirEp.getUdpPort()));
 							// Now we just return null - CCMgr.makeCCTo will then send a ReqConn, which should get
 							// through our NAT
 							return null;
 						}
 						// If we get here, we're doing NAT traversal and have received the ReqConn - just connect as per
 						// normal, their NAT seed should have opened the NAT and allowed us to connect
 					}
 					log.info("Connecting to node " + node.getId() + " on " + theirEp.getUrl());
 					SEONConnection newConn = eonMgr.createSEONConnection();
 					EonSocketAddress theirSockAddr = new EonSocketAddress(theirEp.getAddress(), theirEp.getUdpPort(),
 							theirEp.getEonPort());
 					newConn.connect(theirSockAddr);
 					EonSocketAddress mySockAddr = newConn.getLocalSocketAddress();
 					EonEndPoint myEp = new SeonEndPoint(mySockAddr.getAddress(), mySockAddr.getUdpPort(),
 							mySockAddr.getEonPort());
 					EonConnectionFactory scm = new EonConnectionFactory(eonMgr, mina);
 					ControlConnection cc = new ControlConnection(mina, node, myEp.toMsg(), theirEp.toMsg(), newConn,
 							scm);
 					return cc;
 				} catch (EONException e) {
 					log.error("Error creating eon control connection to " + theirEp);
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 
 	public EndPoint getPublicEndPoint() {
 		if (gatewayEp != null)
 			return gatewayEp.toMsg();
 		if (localAddrIsPublic())
 			return myListenEp.toMsg();
 		if (natTraversalEp != null)
 			return natTraversalEp.toMsg();
 		return null;
 	}
 
 	private boolean localAddrIsPublic() {
 		return !myListenEp.getAddress().isSiteLocalAddress();
 	}
 
 	public EndPoint getLocalEndPoint() {
 		return myListenEp.toMsg();
 	}
 
 	public EndPoint getEndPointForTalkingTo(Node node) {
 		if (node.getLocal())
 			return myListenEp.toMsg();
		if (gatewayEp != null)
			return gatewayEp.toMsg();
		if (localAddrIsPublic())
			return myListenEp.toMsg();
		return null;
 	}
 
 	public void setMina(MinaInstance mina) {
 		this.mina = mina;
 	}
 
 	public void locateLocalNodes() {
 		// Make sure our listener is listening before we send out our advert
 		while (!localListenerReady) {
 			try {
 				synchronized (this) {
 					wait(100);
 				}
 			} catch (InterruptedException ignore) {
 			}
 		}
 		try {
 			DatagramSocket sock = new DatagramSocket();
 			int locatorPort = mina.getConfig().getLocalLocatorUdpPort();
 			log.debug("Locating local nodes on UDP port " + locatorPort);
 			EonSocketAddress sourceEp = new EonSocketAddress(myListenEp.getAddress(), myListenEp.getUdpPort(),
 					myListenEp.getEonPort());
 			EonSocketAddress destEp = new EonSocketAddress("255.255.255.255", locatorPort, LOCAL_LISTENER_EON_PORT);
 			Node myLocalNodeDesc = mina.getNetMgr().getLocalNodeDesc();
 			ByteBuffer payload = ByteBuffer.wrap(myLocalNodeDesc.toByteArray());
 			DEONPacket pkt = new DEONPacket(sourceEp, destEp, payload);
 			ByteBuffer buf = ByteBuffer.allocate(8192);
 			pkt.toByteBuffer(buf);
 			buf.flip();
 			byte[] pktBytes = new byte[buf.limit()];
 			System.arraycopy(buf.array(), 0, pktBytes, 0, buf.limit());
 			sock.setBroadcast(true);
 			sock.send(new DatagramPacket(pktBytes, pktBytes.length, InetAddress.getByName("255.255.255.255"),
 					locatorPort));
 			sock.close();
 		} catch (Exception e) {
 			log.error("Caught " + e.getClass().getName() + " when locating local nodes: " + e.getMessage());
 		}
 	}
 
 	@Override
 	public boolean natTraversalDecided() {
 		return natTraversalDecided;
 	}
 
 	@Override
 	public boolean advisePublicDetails(PublicDetails details, EndPoint source) {
 		if (natTraversalDecided)
 			return false;
 		EonEndPoint newEp = null;
 		for (String epUrl : details.getUrlList()) {
 			if (isEonUrl(epUrl))
 				newEp = parse(epUrl);
 		}
 		if (newEp == null)
 			return false;
 		EonEndPoint sourceEp = parse(source.getUrl());
 		// If we haven't been given public details, store these
 		if (myAllegedPublicEndpoint == null) {
 			myAllegedPublicEndpoint = newEp;
 			publicDetailsSource = sourceEp;
 			return false;
 		}
 		// If this is the same IP address as before, ignore it as it doesn't tell us anything
 		if (sourceEp.getAddress().equals(publicDetailsSource.getAddress()))
 			return false;
 		// We can traverse if our udp port on the NAT doesn't change between destination host
 		if ((myAllegedPublicEndpoint.getUdpPort() == newEp.getUdpPort())) {
 			log.info("It appears my NAT supports traversal - w00t");
 			natTraversalEp = new SeonNatTraversalEndPoint(newEp.getAddress(), newEp.getUdpPort(), LISTENER_EON_PORT);
 		}
 		natTraversalDecided = true;
 		return (natTraversalEp != null);
 	}
 
 	@Override
 	public void configUpdated() {
 		eonMgr.setMaxOutboundBps(mina.getConfig().getMaxOutboundBps());
 	}
 
 	@Override
 	public void setHandoverHandler(HandoverHandler handler) {
 		handoverHandler = handler;
 	}
 
 	private class LocalNodesListener implements PushDataReceiver {
 		private DEONConnection conn;
 
 		public void start() {
 			conn = eonMgr.createDEONConnection();
 			conn.setDataReceiver(this);
 			try {
 				conn.bind(LOCAL_LISTENER_EON_PORT);
 			} catch (EONException e) {
 				log.fatal("ERROR: caught EONException while listening for local nodes", e);
 				return;
 			}
 			localListenerReady = true;
 			log.debug("Listening for local nodes");
 		}
 
 		public void stop() {
 			conn.close();
 		}
 
 		@Override
 		public void receiveData(ByteBuffer data, Object metadata) throws IOException {
 			try {
 				Node otherNode = Node.parseFrom(data.array());
 				if (!otherNode.getLocal()) {
 					log.error("Received local node advert with non-local descriptor: " + new String(data.array()));
 					return;
 				}
 				if (otherNode.getId().equals(mina.getMyNodeId().toString()))
 					return; // This is just me
 				else {
 					if (!mina.getCCM().haveRunningOrPendingCCTo(otherNode.getId())) {
 						log.debug("Received local node request from " + otherNode.getId());
 						mina.getCCM().makeCCTo(otherNode, null);
 					}
 				}
 			} catch (InvalidProtocolBufferException e) {
 				log.error("Error: XML parse error when receiving local node advert", e);
 			}
 		}
 
 		@Override
 		public void providerClosed() {
 			// Do nothing
 		}
 	}
 
 	private class HandoverListener implements PushDataReceiver {
 		private DEONConnection conn;
 
 		public void start() {
 			conn = eonMgr.createDEONConnection();
 			conn.setDataReceiver(this);
 			try {
 				conn.bind(HANDOVER_EON_PORT);
 			} catch (EONException e) {
 				log.fatal("ERROR: caught EONException while listening for local nodes", e);
 				return;
 			}
 
 		}
 
 		public void stop() {
 			conn.close();
 		}
 
 		@Override
 		public void receiveData(ByteBuffer data, Object metadata) throws IOException {
 			EonSocketAddress theirSockAddr = (EonSocketAddress) metadata;
 			if (!NetUtil.getLocalInetAddresses(true).contains(theirSockAddr.getAddress())) {
 				log.error("SECURITY ERROR: Illegal handover attempt from non-local address "
 						+ theirSockAddr.getAddress().getHostAddress());
 				return;
 			}
 			String msg = new String(data.array(), data.arrayOffset(), data.remaining());
 			String retMsg;
 			if (handoverHandler == null)
 				retMsg = "1:No handler was available to accept the handover - perhaps rbnb is running in console mode?";
 			else
 				retMsg = handoverHandler.gotHandover(msg);
 			log.debug("Received handover msg '" + msg + "', sending response: " + retMsg);
 			try {
 				conn.sendTo(theirSockAddr, retMsg.getBytes());
 			} catch (EONException e) {
 				log.error("Error sending handover response", e);
 			}
 		}
 
 		@Override
 		public void providerClosed() {
 			// Do nothing
 		}
 	}
 
 	private class IncomingConnectionListener implements SEONConnectionListener {
 		private EonConnectionFactory connectionFactory;
 
 		IncomingConnectionListener() {
 			connectionFactory = new EonConnectionFactory(eonMgr, mina);
 		}
 
 		public void onClose(EONConnectionEvent event) {
 			log.warn("Eon listener closing on " + myListenEp);
 		}
 
 		public void onNewSEONConnection(EONConnectionEvent event) {
 			SEONConnection conn = (SEONConnection) event.getConnection();
 			EonSocketAddress localSockAddr = conn.getLocalSocketAddress();
 			EonEndPoint myEp = new SeonEndPoint(localSockAddr.getAddress(), localSockAddr.getUdpPort(),
 					localSockAddr.getEonPort());
 			EonSocketAddress remoteSockAddr = conn.getRemoteSocketAddress();
 			EonEndPoint theirEp = new SeonEndPoint(remoteSockAddr.getAddress(), remoteSockAddr.getUdpPort(),
 					remoteSockAddr.getEonPort());
 			RemoteNodeHandler handler = new RemoteNodeHandler(mina, conn, myEp.toMsg(), theirEp.toMsg(),
 					connectionFactory);
 			handler.handle();
 		}
 	}
 }
