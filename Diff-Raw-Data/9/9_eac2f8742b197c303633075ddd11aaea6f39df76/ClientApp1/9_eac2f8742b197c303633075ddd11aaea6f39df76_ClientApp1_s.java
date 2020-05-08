 package org.peer.client;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.plaf.SliderUI;
 
 import org.base.peerserver.RFCIndexNode;
 import org.base.rsserver.PeerListNode;
 import org.common.DataKeyConstants;
 import org.peer.server.PeerServerApp;
 
 
 public class ClientApp1 {
 
 	public static void main(String[] args) {
 		
 		List <PeerListNode> peerList;
 		PeerServerApp peerServerApp = new PeerServerApp(1200);
 		peerServerApp.start();
 		System.out.println("After calling start");
 		ClientFunction peer = new ClientFunction("peer2.txt","10.139.75.19");
		peer.registerPeer(1200);
 		//peer.pQueryFunc();
 		while(true){
 			try {
 				Thread.sleep(1800000);
 				peer.keepAliveFunc();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		//peerList = peer.getPeerList();
 		//peer.RFCIndexFunc(peerList.get(rand(peerList.size())));
 		
 		//peer.leaveFunc();
 	}
 public static int rand(int size){
 	Random random = new Random();
 	int randomint = random.nextInt(size);
 	return randomint;
 }
 }
