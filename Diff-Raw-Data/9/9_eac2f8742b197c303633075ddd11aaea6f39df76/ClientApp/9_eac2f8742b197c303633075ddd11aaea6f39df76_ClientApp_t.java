 package org.peer.client;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.Random;
 
 import org.base.peerserver.RFCIndexNode;
 import org.base.rsserver.PeerListNode;
 import org.common.DataKeyConstants;
 import org.peer.server.PeerServerApp;
 
 
 public class ClientApp {
 
 	public static void main(String[] args) {
 		
 		List <PeerListNode> peerList;
 		//PeerServerApp peerServerApp = new PeerServerApp();
 		//peerServerApp.start();
 		System.out.println("After calling start");
 		ClientFunction peer = new ClientFunction("peer1.txt","10.139.75.19");
		//peer.registerPeer(1010);
 		peer.pQueryFunc();
 		peer.keepAliveFunc();
 		peerList = peer.getPeerList();
 		//PeerListNode node = peerList.get(rand(peerList.size()));
 		PeerListNode node = peerList.get(0);
 		System.out.println("Connecting to " + node.getHostName()+"port no:"+node.getPortNumber());
 		peer.RFCIndexFunc(node);
 		List<RFCIndexNode> rfcList = peer.getRfcIndexList();
 		String rfcTitle = rfcList.get(2).getRfcTitle();
 		int rfcNo = rfcList.get(2).getRfcNumber();
 		System.out.println("RFC title " + rfcTitle + "  rfc no " + rfcNo);
 		byte [] fileData = peer.GetRFCFunc(node, rfcNo, rfcTitle);
 		if(fileData != null){
 			System.out.println("File size: " + fileData.length);
 		}
 		
 		peer.leaveFunc();
 	}
 public static int rand(int size){
 	Random random = new Random();
 	int randomint = random.nextInt(size);
 	return randomint;
 }
 
 }
