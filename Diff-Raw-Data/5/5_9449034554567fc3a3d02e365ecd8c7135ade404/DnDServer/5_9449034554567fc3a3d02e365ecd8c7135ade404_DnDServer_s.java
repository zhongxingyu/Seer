 package kryonet.server;
 
 import enums.Enums.RaceType;
 import gui.util.Database;
 import gui.util.managers.AccountManager;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import kryonet.DnDNetwork;
 import kryonet.DnDNetwork.CreateAccount;
 import kryonet.DnDNetwork.CreateCharacter;
 import kryonet.DnDNetwork.DisconnectCharacter;
 import kryonet.DnDNetwork.GetCharacters;
 import kryonet.DnDNetwork.Login;
 import kryonet.DnDNetwork.LoginCharacter;
 import kryonet.DnDNetwork.Message;
 import kryonet.DnDNetwork.Register;
 import kryonet.DnDNetwork.UpdateNames;
 import kryonet.client.DnDClient;
 import objects.dndcharacter.DnDCharacter;
 import objects.dndcharacter.classes.DnDClass.ClassType;
 
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 import com.esotericsoftware.minlog.Log;
 
 public class DnDServer {
 	Server server;
 	
 	public DnDServer() throws IOException {
 		
 		if(!DnDClient.testing)
 			Database.getInstance().queryAccountsAndSave();
 		
 		server = new Server() {
 			protected Connection newConnection() {
 				return new DnDConnection();
 			}
 		};
 		
 		DnDNetwork.register(server);
 		
 		server.addListener(new Listener() {
 			public void received (Connection c, Object object) {
 				DnDConnection connection = (DnDConnection)c;
 								
 				if (object instanceof Register) {
 					System.out.println("New Connection From: " + connection.getRemoteAddressTCP());
 				} else
 				
 				if (object instanceof Login) {
 					checkLogin(connection, (Login)object);
 				} else
 					
 				if(object instanceof CreateAccount) {
 					checkCreateAccount(connection, (CreateAccount)object);
 				} else
 					
 				if(object instanceof CreateCharacter) {
 					checkCreateCharacter(connection, (CreateCharacter)object);
 				} else
 				
 				if(object instanceof GetCharacters) {
 					getCharacters(connection, (GetCharacters)object);
 				} else
 				
 				if(object instanceof Message) {
 					sendMessage(connection, (Message)object);
 				} else
 				
 				if(object instanceof LoginCharacter) {
 					loginCharacter(connection, (LoginCharacter)object);
 				} else
 				
 				if(object instanceof DisconnectCharacter) {
 					disconnectCharacter(connection);
 				} 
 			}
 		});
 		
 		server.bind(DnDNetwork.port);
 		server.start();
 		
 		JFrame frame = new JFrame("DnD Server");
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosed (WindowEvent evt) {
 				server.stop();
 				Database.getInstance().closeConnection();
 			}
 		});
 		frame.getContentPane().add(new JLabel("Close to stop the DnD server."));
 		frame.setSize(320, 200);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 	
 	private void checkCreateAccount(DnDConnection connection, CreateAccount create) {
 		if(AccountManager.getInstance().validateNewAccount(create.username, create.password, create.email)) {
 			create.accepted = true;
 			System.out.println("Connection : " + connection.getRemoteAddressTCP() + " - Create Valid: " + create.username + ":" + create.password + ":" + create.email);
 		} else {
 			String error = AccountManager.getInstance().getErrorMessage();
 			create.accepted = false;
 			create.error = error;
 			System.out.println("Connection : " + connection.getRemoteAddressTCP() + " - CREATE INVALID: " + create.username + ":" + create.password + ":" + create.email + ":" + error);
 		}
 		server.sendToTCP(connection.getID(), create);
 	}
 	
 	private void checkCreateCharacter(DnDConnection connection, CreateCharacter create) {
 		if(AccountManager.getInstance().validateNewCharacter(create.username, create.name, create.dnd_class, create.dnd_race)) {
 			create.accepted = true;
 			System.out.println("Connection : " + connection.getRemoteAddressTCP() + " - Create Valid: " + create.name + ":" + create.dnd_class + ":" + create.dnd_race);
 		} else {
 			String error = AccountManager.getInstance().getErrorMessage();
 			create.accepted = false;
 			create.error = error;
 			System.out.println("Connection : " + connection.getRemoteAddressTCP() + " - CREATE INVALID: " + create.name + ":" + create.dnd_class + ":" + create.dnd_race + ":" + error);
 		}
 		server.sendToTCP(connection.getID(), create);
 	}
 
 	private void updateNames () {
 		// Collect the names for each connection.
 		Connection[] connections = server.getConnections();
 		ArrayList<String> names = new ArrayList<String>(connections.length);
 		for (int i = connections.length - 1; i >= 0; i--) {
 			DnDConnection connection = (DnDConnection)connections[i];
 			if(connection.characterName != null)
 				names.add(connection.characterName);
 		}
 		// Send the names to everyone.
 		UpdateNames updateNames = new UpdateNames();
 		updateNames.names = (String[])names.toArray(new String[names.size()]);
 		server.sendToAllTCP(updateNames);
 	}
 
 	private void loginCharacter(DnDConnection connection, LoginCharacter character) {
 		connection.characterName = character.name;
 		Message chatMessage = new Message();
 		chatMessage.text = connection.characterName + " has connected.";
 		server.sendToAllExceptTCP(connection.getID(), chatMessage);
 		updateNames();
 	}
 
 	private void sendMessage(DnDConnection connection, Message chatMessage) {
 		// Ignore the object if the chat message is invalid.
 		String message = chatMessage.text;
 		if (message == null) return;
 		message = message.trim();
 		if (message.length() == 0) return;
 		// Prepend the connection's name and send to everyone.
 		chatMessage.text = connection.characterName + ": " + message;
 		server.sendToAllTCP(chatMessage);
 	}
 
 	private void getCharacters(DnDConnection connection, GetCharacters character) {
 		List<DnDCharacter> characters = new ArrayList<DnDCharacter>();
 		if(!DnDClient.testing) {
 			ResultSet results = AccountManager.getInstance().getAccountCharacters(character.name);
 					
 			try {
 				while(results.next()) {
 					String name = results.getString("name");
 					String class_type = results.getString("class_type");
 					String race_type  = results.getString("race_type");
 					
 					ClassType classType = ClassType.valueOf(class_type);
 					RaceType raceType = RaceType.valueOf(race_type);
 				
 					DnDCharacter dndCharacter = new DnDCharacter(name, classType, raceType, null);
 					characters.add(dndCharacter);
 
 					System.out.println("Connection : " + connection.getRemoteAddressTCP() + " - Character: " + dndCharacter);
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			character.results = characters;
 		} else {
 			DnDCharacter dndCharacter = new DnDCharacter("Test", ClassType.FIGHTER, RaceType.HUMAN, null);
 			characters.add(dndCharacter);
 			character.results = characters;
 		}
 		server.sendToTCP(connection.getID(), character);
 	}
 
 	private void checkLogin(DnDConnection connection, Login login) {
 		if(isUserLoggedIn(login)) {
 			login.accepted = false;
 			login.errorMessage = "User Is Already Logged In";
 			System.out.println("Connection : " + connection.getRemoteAddressTCP() + " - Invalid Login: " + login.username + ":" + login.password);
 		} else if(AccountManager.getInstance().validateLogin(login.username, login.password)){
 			connection.userName = login.username;
 			login.accepted = true;
 			System.out.println("Connection : " + connection.getRemoteAddressTCP() + " - Login: " + login.username + ":" + login.password);
 		} else {
 			login.accepted = false;
 			login.errorMessage = "Invalid Username / Password";
 			System.out.println("Connection : " + connection.getRemoteAddressTCP() + " - Invalid Login: " + login.username + ":" + login.password);
 		}
 		server.sendToTCP(connection.getID(), login);
 	}	
 
 	private void disconnectCharacter(DnDConnection connection) {
 		// Announce to everyone that someone (with a registered name) has left.
 		Message chatMessage = new Message();
 		chatMessage.text = connection.characterName + " has disconnected.";
 		server.sendToAllExceptTCP(connection.getID(), chatMessage);
 		connection.characterName = null;
 		updateNames();
 	}
 	
 	private boolean isUserLoggedIn(Login login) {
		for(Connection connection : server.getConnections()) {
			if(((DnDConnection) connection).userName.equals(login.username)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static class DnDConnection extends Connection {
 		String userName;
 		String characterName;
 	}
 	
 	public static void main(String[] args) throws IOException {
 		Log.set(Log.LEVEL_DEBUG);
 		new DnDServer();
 	}
 }
