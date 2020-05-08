 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 
 public class WServe implements Runnable {
 
 	private HashMap<Integer, WClientHandler> Users = new HashMap<Integer, WClientHandler>();;
 	private int id = 0;
 	private int defaultPort = 11111;
 
 	public WServe() {}
 
 	private int getNextID(){
 		return id++;
 	}
 
 	public void run() {
 		ServerSocket sock;
 
 		try {
 			sock = new ServerSocket(defaultPort);
 			System.out.println("Server started.");
 		}
 		catch (IOException e) {
 			System.err.println("Cannot open port " + defaultPort);
 			return;
 		}
 		while (true) {
 			try {
 				Socket client = sock.accept();
 				Integer newId = getNextID();
 				WClientHandler handler = new WClientHandler(this, client, new WUser(newId,"user"+newId));
 				Users.put(newId, handler);
 				new Thread(handler).start();
 			}
 			catch (Exception e) {
 				System.err.println("Eek! Server error " +e.getMessage());
 			}
 		}
 	}
 
 	public void broadcast(String message) {
 		for (WClientHandler u: Users.values()) {
 			u.sendMessage(message);
 			System.out.println("Broadcast: " + message);
 		}
 	}
 
 	public void broadcastTo(Integer id, String message) {
 		WClientHandler who = Users.get(id);
 		who.sendMessage(message);
 		System.out.println("Message to: "+who.getNick()+":"+message);
 	}
 
 	public void broadcastExcept(Integer id, String message) {
 		WClientHandler who = Users.get(id);
 		System.out.println(who.getNick() + " " + message);
 		for (WClientHandler u: Users.values()) {
 			if(u != who) {
 				u.sendMessage(message);
 			}
 		}
 	}
 
 	public void setName(WClientHandler client, String name) {
 		if (name != "" || name != null) {
 			client.iAm(name);
 			Users.put(client.getClientId(), client); //putting existing id overwrites previous entry
 		}
 		broadcastExcept(client.getClientId(), "User joined: " + client.getNick());
 	}
	
 	public String getUsers() {
 		String allUsers = "users ";
 		for (WClientHandler u: Users.values()) {
 			allUsers = allUsers + "#" + u.getClientId() + " " + u.getNick();
 		}
 		return allUsers;
 	}
 
 	public void removeUser(Integer id) {
 		try{
 			WClientHandler user = Users.remove(id);
 			if (user == null) return;
 			user.exit();
 			broadcast("User left: " + user.getNick());
 		} catch (Exception e) {
 			System.err.println("Eek! Remove user error: "+ e.getMessage());
 		}
 	}
 
 	public static void main(String[] args) {
 		System.out.println("Starting server.");
 		new Thread(new WServe()).start();
 	}
 }

