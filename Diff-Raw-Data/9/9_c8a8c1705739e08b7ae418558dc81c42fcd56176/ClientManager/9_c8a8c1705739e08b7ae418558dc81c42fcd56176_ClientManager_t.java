 package server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 /**
  * Runs a new thread that detects any new clients and creates a Connector for
  * them. Also handles all input messages, and responds to them. Stores the
  * entire game in a World. Essentially is the master class that runs all
  * server-client communication.
  * 
  * @author Dylan
  * 
  */
 public class ClientManager extends Thread {
 	ServerSocket server;
 
 	ArrayList<Connector> connections;
 	
 	boolean hasPlant = false;
 	boolean hasMecha = false;
 
 	public ClientManager(ServerSocket server) {
 		this.server = server;
 
 		connections = new ArrayList<Connector>();
 	}
 
 	public void run() {
 		while (true) {
 			try {
 				Socket newClient = server.accept();
 				Connector newConnector = new Connector(newClient, this);
 				newConnector.start();
 				connections.add(newConnector);
 				
 				newConnector.sendMessage(MessageType.SERVER_MESSAGE, "Welcome to the server!");
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	public void cleanConnections() {
 		for (Connector client : connections) {
 			if (client.reciever.isClosed()) {
 				connections.remove(client);
 				System.out.println("Closed connection");
 			}
 		}
 	}
 
 	public void sendMessage(MessageType type, String message) {
 		for (Connector client : connections) {
 			client.sendMessage(type, message);
 		}
 		cleanConnections();
 	}
 
 	public void input(MessageType type, String message, Connector client) {
 		switch (type) {
 		case SERVER_MESSAGE:
 			Init.sendServerMessage("[USER " + client.id + "]: " + message);
 			System.out.println("[USER " + client.id + "]: " + message);
 			break;
 		case CONNECT:
 			Init.sendServerMessage("New client connected.");
 			int newClientType;
 			if (!hasPlant){
				newClientType = 0;	//Connect plant player
 				hasPlant = true;
			} else if (!hasMecha){
				newClientType = 1;	//Connect mecha player
				hasMecha = true;
 			} else {
 				newClientType = 2;	//Reject player
 			}
 			client.sendMessage(MessageType.CONNECT, "" + (char) newClientType + client.id);
 			break;
 		case ADD_TOWER:
 			int x = message.charAt(0);
 			int y = message.charAt(1);
 			int towerType = message.charAt(2);
 			Init.sendServerMessage("[USER " + client.id + "] adding tower at " + x + ", " + y);
 			if (Init.map.addTower(x, y, towerType)){
 				sendMessage(MessageType.ADD_TOWER, "" + (char) x + (char) y + (char) towerType);
 			} else {
 				Init.sendServerMessage("[USER " + client.id + "] Oops, you can't build a tower there!");
 			}
 			break;
 		case ADD_MONSTER:
 			System.out.println("monster - clientmanager");
 			x = message.charAt(0);
 			y = message.charAt(1);
 			int monsterType = message.charAt(2);
 			Init.sendServerMessage("[USER " + client.id + "] adding monster at " + x + ", " + y);
 			int monsterId = Init.getUniqueId();
 			try {
 			if (Init.map.addMonster(x, y, monsterType, monsterId)){
 				System.out.println("monster - clientmanager - Init.map.add... true");
 				sendMessage(MessageType.ADD_MONSTER, "" + (char) x + (char) y + (char) monsterType + Integer.toString(monsterId));
 			} else {
 				Init.sendServerMessage("[USER " + client.id + "] Oops, you can't spawn a monster there!");
 			}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			break;
 		}
 	}
 }
