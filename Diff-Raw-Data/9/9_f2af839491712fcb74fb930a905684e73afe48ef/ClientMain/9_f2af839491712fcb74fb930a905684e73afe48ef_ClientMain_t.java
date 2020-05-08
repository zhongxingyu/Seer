 /**
  * Main game client class. Allows the user to logon to the server. Basic
  * TCP client code based on code from CS283 homepage.
  */
 package cs283.catan;
 
 import java.awt.EventQueue;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.UIManager.*;
 
 /**
  * @author John
  *
  */
 public class ClientMain {
     
     /**
      * Enumeration representing the types of lobby messages
      *
      */
     public enum LobbyMessageType {
         AddUserToGame,
         RemoveUserFromGame,
         CreateGame
     }
 
     /**
      * Server socket address
      */
     private static InetSocketAddress serverAddress;
     
     /**
      * Timeout in milliseconds when attempting to connect to the server
      */
     private static final int CONNECT_TIMEOUT = 5000;
     
     /**
      * Maximum username length
      */
     private static final int MAX_USERNAME_LENGTH = 16;
     
     /**
      * Current username of the user logged in
      */
     private static String username;
     
     
     /**
      * TCP socket
      */
     private static Socket clientSocket;
     
     /**
      * Object input stream used to receive data from the socket
      */
     private static ObjectInputStream objInputStream;
     
     /**
      * Object output stream used to send data on the socket
      */
     private static ObjectOutputStream objOutputStream;
     
     
     /**
      * Map that will store the games currently in the lobby. The key is the
      * String name of the game, and the value is the String array of players 
      * in the game (should be size 4).
      */
     private static Map<String, String[]> lobbyGames;
     
     
     /**
      * Receiver thread object
      */
     private static Thread receiverThread = null;
     
     /**
      * Catan GUI object
      */
     private static CatanGUI gui = null;
     
     /**
      * Object that the main thread waits on for the mode to change
      */
     public static Object waitForModeChanged = new Object();
     
     
     /**
      * @param args
      */
     public static void main(String[] args) throws Exception {
         
         // Set the IP and port based on the command line arguments
         if (args.length != 2) {
             // Print the proper command line usage
             System.out.println("Usage: <IP_Address> <Port>");
             System.exit(0);
         }
        
         // Parse the IP address and port number
         try {
             serverAddress = 
                   new InetSocketAddress(args[0], Integer.parseInt(args[1]));      
             
             if (serverAddress.isUnresolved()) {
                 System.out.println("Unresolved host: " + args[0]);
                 System.exit(0);
             }
         } catch (NumberFormatException e) {
             System.out.println("Invalid port: " + args[1]);
             System.exit(0);
         } catch (IllegalArgumentException e) {
             System.out.println("Port not in range: " + args[1]);
             System.exit(0);
         }
     
         
         // Use a nicer look and feel for the GUI if available
         try {
             for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (Exception e) {
             // Leave default look and feel
         }
         
         // Logon loop
         boolean logonSuccessful = false;
         
         do {
             
             // Create the login attempt dialog
             JFrame frame = new JFrame("Login");
             frame.setUndecorated(true);
             frame.setVisible(true);
             frame.setAlwaysOnTop(true);
             frame.setLocationRelativeTo(null); // Centers dialog on screen
             
             username = JOptionPane.showInputDialog(frame, "Please enter your " +
                                                           "user name (" + 
                                                           "maximum of 16 " + 
                                                           "characters)");
             
             if (username == null) { // User canceled the login dialog, so exit
                 System.exit(0);
             }
             
             // Get rid of whitespace
             username = username.trim();
             
             if (!username.equals("")) {
                 if (username.length() <= MAX_USERNAME_LENGTH) {
                     String message = attemptLogon(username);
                     if (message.equals("Successfully logged on!")) {
                         logonSuccessful = true;
                     } else {
                         JOptionPane.showMessageDialog(frame, message);
                     }
                 } else {
                     JOptionPane.showMessageDialog(frame, 
                                                   "Username '" + username +
                                                   "' exceeds the 16 character" +
                                                   " limit. Please enter a" +
                                                   " valid username!");
                 }
             } else {
                 JOptionPane.showMessageDialog(frame, "Please enter a name");
             }
             
             frame.dispose();
         } while (!logonSuccessful);
         
         // Check to make sure the user is logged on
         if (logonSuccessful) {
             
             // Create the GUI
             EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     try {
                         gui = new CatanGUI();
                         gui.setUsername(username);
                         gui.getFrame().setVisible(true);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             });
             
            // Start the receiver thread
            receiverThread = new Thread(new ClientReceiver());
            receiverThread.start();
            
             
             // Wait for lobby mode to finish
             synchronized (waitForModeChanged) {
                 waitForModeChanged.wait();
             }
         }
 
         // Terminate the receiver thread (calling interrupt() may be unnecessary
         //                                since closing the socket should signal
         //                                to the thread to exit)
         if (receiverThread != null) {
             receiverThread.interrupt();
         }
         
         // Close the connection to the server
         clientSocket.close();
         
         // Wait for the receiver thread to end
         if (receiverThread != null) {
             receiverThread.join(1000);
         }
     }
 
     /**
      * Attempts to contact the server and logon with the given username
      * @param username
      * @return the message from the server
      */
     private static String attemptLogon(String username) {
         
         
         // Attempt to connect to the server
         String message = null;
         
         try {
             
             // If socket previously opened, close it
             if (clientSocket != null) {
                 clientSocket.close();
             }
             
             clientSocket = new Socket();
             
             // Attempt to connect to the server
             clientSocket.connect(serverAddress, CONNECT_TIMEOUT);
             
             // Send the username to the server
             objOutputStream = 
                          new ObjectOutputStream(clientSocket.getOutputStream());
             
             objOutputStream.writeObject(username);
             objOutputStream.flush();
             
             // Read the message from the server
             objInputStream =
                            new ObjectInputStream(clientSocket.getInputStream());
             
             message = (String) objInputStream.readObject();
             
         } catch (Exception e) {
             return "Unable to connect to game server: " + e.getMessage();
         }
        
         if (message == null) {
             message = "No server response.";
         }
         
         return message;
     }
     
     /**
      * Manage the lobby mode. TEMPORARY, WILL BE REPLACED BY GUI.
      */
     private static void lobbyMode() {
         Scanner scanner = new Scanner(System.in);
         
         try {
             
             String input;
             
             do {
                 input = scanner.nextLine();
                 
                 if (input.toLowerCase().equals("new game")) {
                     
                     System.out.print("Please enter the name of the game: ");
                     String gameName = scanner.nextLine();
                     sendLobbyMsg(LobbyMessageType.CreateGame, gameName);
                     
                 } else if (input.toLowerCase().equals("join game")) {
                     
                     System.out.print("Please enter the name of the game: ");
                     String gameName = scanner.nextLine();
                     sendLobbyMsg(LobbyMessageType.AddUserToGame, gameName);
                     
                 } else if (input.toLowerCase().equals("exit game")) {
                     
                     System.out.print("Please enter the name of the game: ");
                     String gameName = scanner.nextLine();
                     sendLobbyMsg(LobbyMessageType.RemoveUserFromGame, gameName);
                     
                 } else if (input.toLowerCase().equals("query")) {
                     // TEMPORARY CODE FOR DEBUGGING PURPOSES
                     objOutputStream.writeObject(new String("Query"));
                     objOutputStream.flush();
                     
                 }
                 
             } while (!input.toLowerCase().equals("quit"));
             
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         scanner.close();
     }
     
     /**
      * Print out the current state of the lobby. TEMPORARY, WILL BE REPLACED BY
      * GUI.
      */
     private static void printLobby() {
         System.out.println("==============Lobby Games===============\n");
         
         // Iterate through each game in the lobby and print it out
         for (Map.Entry<String, String[]> entry : lobbyGames.entrySet()) {
             System.out.println("Game: '" + entry.getKey() + "'");
             
             // Print out the players in the game
             for (int i = 0; i < entry.getValue().length; i++) {
                 System.out.print("Player " + (i + 1)  + ": ");
                 if (entry.getValue()[i] != null) {
                     System.out.print("'" + entry.getValue()[i] + "'");
                 } else {
                     System.out.print("N/A");
                 }
                 
                 if (i != entry.getValue().length - 1) {
                     System.out.print("    ");
                 }
                 
                 if (i % 2 == 1) {
                     System.out.println();
                 }
             }
             
             System.out.println("\n");
         }
         
         System.out.println("========================================");
     }
     
     /**
      * Sends a lobby message to the server requesting to perform some action
      * related to the game gameName.
      * @param msgType
      * @param gameName
      */
     
     public static void sendLobbyMsg(LobbyMessageType msgType, String gameName) 
                                      throws Exception {
         
         String msg = null;
         
         // Form the message depending on the message type
         switch (msgType) {
         case AddUserToGame:
             msg = "Join Game\n" + gameName + "\n" + username;
             break;
         case RemoveUserFromGame:
             msg = "Remove User from Game\n" + gameName + "\n" + username;
             break;
         case CreateGame:
             msg = "Create Game\n" + gameName + "\n" + username;
             break;
         }
         
         // Only send a message if one was created
         if (msg != null) {
             synchronized (objOutputStream) {
                 objOutputStream.writeObject(msg);
                 objOutputStream.flush();
             }
         }
     }
     
     
     /**
      * Class that handles the receiving of network data in a separate thread
      * @author John
      *
      */
     @SuppressWarnings("unchecked")
     private static class ClientReceiver implements Runnable {
         
         /**
          * Run the receiver thread
          */
         @Override
         public void run() {
             String message;
             
             while (!Thread.interrupted()) {
                 try {
                     message = (String) objInputStream.readObject();
                     
                     if (message.equals("Lobby")) {
                         lobbyGames = (Map<String, String[]>) 
                                 objInputStream.readObject();
                    
                         
                         EventQueue.invokeAndWait(new Runnable() {
                             public void run() {
                                 gui.updateLobby(lobbyGames);
                             }
                         });
 
                     } else if (message.equals("Game starting")) {
                         
                         // Game starting, confirm that client is ready to play
                         // TODO: May want to move where this confirmation
                         // message is sent to GUI thread
                         synchronized (objOutputStream) {
                             objOutputStream.writeObject("Begin Game");
                             objOutputStream.flush();
                         }
                         
                         EventQueue.invokeAndWait(new Runnable() {
                             public void run() {
                                 gui.switchToGameMode();
                             }
                         });
                         
                         break;
                         
                     } else {
                         System.out.println("Server response: " + message);
                     }
                 } catch (InterruptedIOException e) {
                     // The thread was interrupted, so exit the thread
                     break;
                 } catch (IOException e) {
                     // The server must have closed its connection, so exit
                     // the thread
                     System.out.println("Receiver can no longer receive from " +
                                        "server.");
                     
                     // TODO: Probably should make sure program exits at this
                     //       point
                     break;
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             
             System.out.println("Ending receiver thread");
         }
        
     }
     
 }
