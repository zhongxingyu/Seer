 package org.remus.test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.thrift.TException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
import org.remus.PeerInfo;
 import org.remus.plugin.PluginManager;
 import org.remus.thrift.NotImplemented;
 import org.remus.thrift.PeerInfoThrift;
 import org.remus.thrift.PeerType;
 
 public class NameTest {
 
 	PluginManager pm1, pm2;
 	
 	@Before public void setup() throws Exception {
 		Map initMap = new HashMap();
 		Map manMap = new HashMap();
 		Map manConfigMap = new HashMap();
 		manConfigMap.put("port", 17999);
 		manMap.put("server", manConfigMap);
 		initMap.put("org.remus.manage.WorkManager", manMap);
 		
 		//JSONBuilder init = new JSONBuilder();
 		//init.map().put('init', init.map().put( 'server', init.map() ));
 		
 		Map jsMap = new HashMap();
 		Map jsServerMap = new HashMap();
 		jsServerMap.put("port", 18001);
 		jsMap.put("server", jsServerMap);
 		initMap.put("org.remus.js.JSWorker", jsMap);
 		System.out.println(initMap);
 		pm1 = new PluginManager(initMap);
 		pm1.start();
 		
 		
 		Map initMap2 = new HashMap();		
 		Map manMap2 = new HashMap();
 		manConfigMap.put("host", "localhost");
 		manMap2.put("client", manConfigMap);		
 		initMap2.put("org.remus.manage.WorkClient", manMap2);
 		System.out.println(initMap2);		
 		pm2 = new PluginManager(initMap2);
 		pm2.start();
 
 	}
 
 	@Test public void testNaming() throws NotImplemented, TException {		
 		for (PeerInfoThrift peer : pm2.getManager().getPeers()) {
 			if (peer.peerType == PeerType.WORKER) {
 				System.out.println(peer.peerID + " " + peer.workTypes + " " + peer.host + ":" + peer.port);
				//RemusNet.Iface p = pm2.getPeer(peer.peerID);
				//System.out.println(p);
 			}
 		}
 	}
 	
 	
 	@After public void tearDown() {
 		pm1.close();
 		pm2.close();
 	}
 	
 }
