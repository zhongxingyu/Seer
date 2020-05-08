 package ReactorEE.Networking;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import ReactorEE.simulator.GUIRefresher;
 import ReactorEE.simulator.PlantController;
 import ReactorEE.simulator.ReactorUtils;
 import ReactorEE.sound.Music;
 import ReactorEE.swing.MultiplayerMainGUI;
 
 public class HandshakeRequest 
 {
 	/**
 	 * Handles the initial handshaking procedure with the other player's computer.
 	 * Sends an expected message and waits for a reply. If the reply is also expected, 
 	 * the game state listener is started and provided with the IP of the other player.
 	 * The rest of the game is then initialised, and the connection closed. 
 	 * @param HostIP The IP of the other computer, supplied from the user in the GUI.
 	 * @throws IOException 
 	 * @throws UnknownHostException 
 	 */
 	public void run(String HostIP) throws UnknownHostException, IOException				
 	{
 		Socket socket = new Socket(InetAddress.getByName(HostIP), SocketUtil.HANDSHAKE_PORT_NO);
 		SocketUtil.write(socket, "ANCHOVY");						
 		PlantController plantController = new PlantController(new ReactorUtils());
 		
 		if(SocketUtil.readString(socket).equalsIgnoreCase("ANCHOVY FREE"))	
 		{
 			GamestateListener listen = new GamestateListener(HostIP, plantController);
			PlantController controller = new PlantController(new ReactorUtils());
 			MultiplayerMainGUI view = new MultiplayerMainGUI(plantController,HostIP);
 			//no step looper needed as stepping is synchronized through GamestateListener
 			Music.changeGameContext("game");
			controller.setStepLooper(new GUIRefresher(controller, view));
 			listen.start();
 		}
 		socket.close();												
 	}	
 }
