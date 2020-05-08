 package org.remus.manage;
 
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.transport.TSocket;
 import org.remus.PeerInfo;
 import org.remus.RemusManager;
 import org.remus.plugin.PluginInterface;
 import org.remus.plugin.PluginManager;
 import org.remus.thrift.BadPeerName;
 import org.remus.thrift.NotImplemented;
 import org.remus.thrift.PeerInfoThrift;
 import org.remus.thrift.PeerType;
 import org.remus.thrift.RemusNet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WorkClient extends RemusManager {
 
 	private Logger logger;
 	String server;
 	int port;
 	private PluginManager plugManager;
 
 	@Override
 	public PeerInfo getPeerInfo() {
 		PeerInfo p = new PeerInfo();
 		p.name = "Work Client";
 		p.peerType = PeerType.MANAGER;
 		return p;
 	}
 
 	@Override
 	public void init(Map params) throws Exception {
 		logger = LoggerFactory.getLogger(WorkClient.class);
 		server = (String) params.get("host");
 		port =  Integer.parseInt(params.get("port").toString());
 	}
 
 	
 	@Override	
 	public void start(PluginManager pluginManager) throws Exception {
 		this.plugManager = pluginManager;
 		for (PluginInterface pi : pluginManager.getPlugins()) {
 			PeerInfo info = pi.getPeerInfo();
			info.setPeerID(UUID.randomUUID().toString());
			info.setHost(Util.getDefaultAddress());
			info.setPort(pluginManager.addLocalPeer(info.peerID, (RemusNet.Iface) pi));
 			addPeer(info);
 		}
 	}
 
 	@Override
 	public void stop() {
 		
 	}
 
 	@Override
 	public void scheduleRequest() throws NotImplemented, TException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addPeer(PeerInfoThrift info) throws BadPeerName, TException, NotImplemented {
 		TSocket transport = new TSocket(server, port);
 		TBinaryProtocol protocol = new TBinaryProtocol(transport);
 		transport.open();
 		RemusNet.Client client = new RemusNet.Client(protocol);
 
 		client.addPeer(info);
 
 		transport.close();
 	}
 
 
 	@Override
 	public void delPeer(String peerName) throws TException, NotImplemented {
 		TSocket transport = new TSocket(server, port);
 		TBinaryProtocol protocol = new TBinaryProtocol(transport);
 		transport.open();
 		transport.open();
 		RemusNet.Client client = new RemusNet.Client(protocol);
 
 		client.delPeer(peerName);
 
 		transport.close();	}
 
 
 	@Override
 	public List<PeerInfoThrift> getPeers() throws TException, NotImplemented {
 		logger.info("Getting peers from " + server + ":" + port);
 		TSocket transport = new TSocket(server, port);
 		TBinaryProtocol protocol = new TBinaryProtocol(transport);
 		transport.open();
 		RemusNet.Client client = new RemusNet.Client(protocol);
 		List<PeerInfoThrift> out = client.getPeers();
 		transport.close();
 		return out;
 	}
 
 
 }
