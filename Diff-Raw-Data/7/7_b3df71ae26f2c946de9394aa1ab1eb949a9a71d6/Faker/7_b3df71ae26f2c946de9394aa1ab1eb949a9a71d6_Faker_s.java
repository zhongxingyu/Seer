 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl.debug;
 
 import java.util.Random;
 
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.ClusterLeaderHandler;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PositionUpdateHandler;
 
 import org.olsr.plugin.pud.ClusterLeader;
 import org.olsr.plugin.pud.PositionUpdate;
 import org.springframework.beans.factory.annotation.Required;
 
 public class Faker {
 	private ClusterLeaderHandler clusterLeaderHandler;
 
 	/**
 	 * @param clusterLeaderHandler
 	 *          the clusterLeaderHandler to set
 	 */
 	@Required
 	public final void setClusterLeaderHandler(ClusterLeaderHandler clusterLeaderHandler) {
 		this.clusterLeaderHandler = clusterLeaderHandler;
 	}
 
 	private PositionUpdateHandler positionUpdateHandler;
 
 	/**
 	 * @param positionUpdateHandler
 	 *          the positionUpdateHandler to set
 	 */
 	@Required
 	public final void setPositionUpdateHandler(PositionUpdateHandler positionUpdateHandler) {
 		this.positionUpdateHandler = positionUpdateHandler;
 	}
 
 	public enum MSGTYPE {
 		PU, CL
 	}
 
 	private boolean firstFake = true;
 
 	/**
 	 * @return the firstFake
 	 */
 	public final boolean isFirstFake() {
 		return this.firstFake;
 	}
 
 	/**
 	 * @param firstFake
 	 *          the firstFake to set
 	 */
 	public final void setFirstFake(boolean firstFake) {
 		this.firstFake = firstFake;
 	}
 
 	/* locations in byte array */
 	static private final int UplinkMessage_v4_olsrMessage_v4_originator_network = 10;
 	static private final int UplinkMessage_v4_olsrMessage_v4_originator_node = 11;
 	static private final int UplinkMessage_v4_clusterLeader_originator_network = 8;
 	static private final int UplinkMessage_v4_clusterLeader_originator_node = 9;
 	static private final int UplinkMessage_v4_clusterLeader_clusterLeader_network = 12;
 	static private final int UplinkMessage_v4_clusterLeader_clusterLeader_node = 13;
 
 	public void fakeit(Gateway gateway, long utcTimestamp, MSGTYPE type, Object msg) {
 		if (!this.firstFake) {
 			return;
 		}
 
 		Random random = new Random();
 		int randomRange = 100;
 		byte[] clmsg = null;
 		byte[] pumsg = null;
 		int initialNetwork = 0;
 		byte initialNode = 1;
 		if (type == MSGTYPE.PU) {
 			pumsg = ((PositionUpdate) msg).getData();
 			initialNetwork = pumsg[UplinkMessage_v4_olsrMessage_v4_originator_network];
 			initialNode = pumsg[UplinkMessage_v4_olsrMessage_v4_originator_node];
 		} else /* if (type == MSGTYPE.CL) */{
 			clmsg = ((ClusterLeader) msg).getData();
 			initialNetwork = clmsg[UplinkMessage_v4_clusterLeader_originator_network];
 			initialNode = clmsg[UplinkMessage_v4_clusterLeader_originator_node];
 		}
 
 		boolean firstNode = true;
 		int network = initialNetwork;
 		int networkMax = network + 2;
 		byte node = initialNode;
 		int nodeCount = 0;
 		int nodeCountMax = 6;
 		byte clusterLeaderNode = node;
 		while (network <= networkMax) {
 			node = initialNode;
 			clusterLeaderNode = node;
 			nodeCount = 0;
 			while (nodeCount < nodeCountMax) {
 				if (!firstNode) {
 					boolean skipNode = ((network == (initialNetwork + 1)) && (node == initialNode));
 					if (!skipNode) {
 						/*
 						 * Position Update Message
 						 */
 						if (type == MSGTYPE.PU) {
 							byte[] pumsgClone = pumsg.clone();
 							/* olsr originator */
 							pumsgClone[UplinkMessage_v4_olsrMessage_v4_originator_network] = (byte) network;
 							pumsgClone[UplinkMessage_v4_olsrMessage_v4_originator_node] = node;
 
 							PositionUpdate pu = new PositionUpdate(pumsgClone, pumsgClone.length);
 							this.positionUpdateHandler.handlePositionMessage(gateway, utcTimestamp + random.nextInt(randomRange), pu);
 						}
 
 						/*
 						 * Cluster Leader Message
 						 */
 						else /* if (type == MSGTYPE.CL) */{
 							byte[] clmsgClone = clmsg.clone();
 							/* originator */
 							clmsgClone[UplinkMessage_v4_clusterLeader_originator_network] = (byte) network;
 							clmsgClone[UplinkMessage_v4_clusterLeader_originator_node] = node;
 
 							/* clusterLeader */
 							clmsgClone[UplinkMessage_v4_clusterLeader_clusterLeader_network] = (byte) network;
 							clmsgClone[UplinkMessage_v4_clusterLeader_clusterLeader_node] = clusterLeaderNode;
 
 							ClusterLeader cl = new ClusterLeader(clmsgClone, clmsgClone.length);
 							this.clusterLeaderHandler.handleClusterLeaderMessage(gateway, utcTimestamp + random.nextInt(randomRange),
 									cl);
 						}
 					}
 				} else {
 					firstNode = false;
 				}
 
 				node++;
 				if (node == 0) {
 					node++;
 				}
 				nodeCount++;
 			}
 			network++;
 		}
 
 		/* add an extra standalone node */
 
 		node = initialNode;
 
 		/*
 		 * Position Update Message
 		 */
 		if (type == MSGTYPE.PU) {
 			byte[] pumsgClone = pumsg.clone();
 			// olsr originator
 			pumsgClone[UplinkMessage_v4_olsrMessage_v4_originator_network] = (byte) network;
 			pumsgClone[UplinkMessage_v4_olsrMessage_v4_originator_node] = node;
 
 			PositionUpdate pu = new PositionUpdate(pumsgClone, pumsgClone.length);
 			this.positionUpdateHandler.handlePositionMessage(gateway, utcTimestamp + random.nextInt(randomRange), pu);
 		}
 
 		/*
 		 * Cluster Leader Message
 		 */
 		else /* if (type == MSGTYPE.CL) */{
 			byte[] clmsgClone = clmsg.clone();
 			// originator
 			clmsgClone[UplinkMessage_v4_clusterLeader_originator_network] = (byte) network;
 			clmsgClone[UplinkMessage_v4_clusterLeader_originator_node] = node;
 
 			// clusterLeader
 			clmsgClone[UplinkMessage_v4_clusterLeader_clusterLeader_network] = (byte) (network - 1);
 			clmsgClone[UplinkMessage_v4_clusterLeader_clusterLeader_node] = (byte) (clusterLeaderNode + nodeCountMax - 1);
 
 			ClusterLeader cl = new ClusterLeader(clmsgClone, clmsgClone.length);
 			this.clusterLeaderHandler.handleClusterLeaderMessage(gateway, utcTimestamp + random.nextInt(randomRange), cl);
 		}
 	}
 }
