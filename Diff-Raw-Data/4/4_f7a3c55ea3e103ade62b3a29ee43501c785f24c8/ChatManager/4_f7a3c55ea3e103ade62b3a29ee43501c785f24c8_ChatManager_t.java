 package org.peerbox.chat;
 
 import org.peerbox.friend.Friend;
 import org.peerbox.friend.FriendManager;
 import org.peerbox.rpc.RPCEvent;
 import org.peerbox.rpc.RPCHandler;
 import org.peerbox.rpc.RPCResponseListener;
 import org.peerbox.rpc.ServiceRequestListener;
 
 public class ChatManager implements ServiceRequestListener {
 	protected RPCHandler rpc;
 	protected FriendManager friendMan;
 	
 	public ChatManager(RPCHandler rpc, FriendManager friendMan) {
 		this.rpc = rpc;
 		this.friendMan = friendMan;
 		this.rpc.registerServiceListener("chat", this);
 	}
 	
 	public void sendMessage(final String alias, String message) {
 		Friend friend = friendMan.getFriend(alias);
		if (friend == null || !friend.isAlive()) {
 			System.out.println("No friend " + alias);
			return;
 		}
 		rpc.sendRequest(friend.getNetworkAddress(), "chat", message, new RPCResponseListener() {
 			@Override
 			public void onResponseReceived(RPCEvent event) {
 				
 			}
 
 			@Override
 			public void onTimeout() {
 				System.out.println("Message to " + alias + " failed");
 			}
 		});
 	}
 	
 	@Override
 	public void onRequestRecieved(RPCEvent e) {
 		Friend friend = null;
 		for (Friend f : friendMan.getAllFriends()) {
 			if (f.getNetworkAddress().equals(e.getSenderURI())) {
 				friend = f;
 			}
 		}
 		String alias;
 		if (friend != null) {
 			alias = friend.getAlias();
 		} else {
 			alias = e.getSenderURI().toString();
 		}
 		e.respond("");
 		System.out.println(alias + " says: " + e.getDataString());
 	}
 
 }
