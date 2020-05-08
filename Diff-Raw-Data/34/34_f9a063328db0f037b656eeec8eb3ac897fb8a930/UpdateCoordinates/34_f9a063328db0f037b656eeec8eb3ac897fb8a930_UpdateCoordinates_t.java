 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 
 public class UpdateCoordinates implements Runnable {
 	public void run() {
 		Socket connection = null;
 		try {
 			System.out.println("Updating coordinates");
 			int id, x , y, listPosition;
 			// gets server address and attempts to establish a connection
			InetAddress address = InetAddress.getByName("localhost");
 			connection = new Socket(address, 8149);
 			BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
 			ObjectOutputStream outputStream = new ObjectOutputStream(bos);
 			BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
 			ObjectInputStream inputStream = new ObjectInputStream(bis);
 			//ObjectOutputStream outputStream = new ObjectOutputStream(bos);
 			
 			// tell server we want to update player coords
 			outputStream.writeObject("update");
 			outputStream.flush();
 						
 			int c = inputStream.readInt();
 			// one is accepted connection from server
 			if (c == 1) {
 				while (true) {
 					Thread.sleep(100); // adjust this
 					
 					if (Main.exitRequest) { // check if a reason exists to continue
 						connection.close();
 						return;
 					}
 					
 					// System.out.println("Sending coordinates to server..");
 					id = Player.getId();
 					listPosition = Player.listPosition;
 					x = Player.onlinePlayers.get(listPosition).x;
 					y = Player.onlinePlayers.get(listPosition).y;
 					Player player = new Player(id, String.valueOf(listPosition), x, y);
 					outputStream.writeObject(player);
 					outputStream.flush();
 					// System.out.println("Coordinates sent = " + x + "  " + y);
 				}
 			}
 			
 		}
 		// socketexception thrown when connection terminates
 		catch (Exception e) {
			e.printStackTrace();
		} 
 		finally {
 			try {
 				connection.close();
 			} catch (Exception e) {
 				//e.printStackTrace();
 			}
 		}
 	}
 
 }
