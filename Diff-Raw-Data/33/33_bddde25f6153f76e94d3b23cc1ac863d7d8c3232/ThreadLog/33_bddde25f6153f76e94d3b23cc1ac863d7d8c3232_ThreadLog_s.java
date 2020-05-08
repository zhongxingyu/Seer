 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import IHM.ManagementFrame;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.ref.WeakReference;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Elodie
  */
 public class ThreadLog extends Thread {
 
 	InetAddress serverAddress;
 	Socket socketCommand;
 	int serverPort;
 
 	private final static String ERROR_BOX = "ERROR B";
 	private final static String ERROR_PALETTE = "ERROR P";
 	private final static String ERROR_PRINT = "ERROR A";
 	private final static String ERROR_WAREHOUSE = "ERROR W";
 	private final static String ERROR_PALETTE_QUEUE = "ERROR Q";
 	private final static String ERROR_BOX_REFUSED = "ERROR R";
 	private final static String ERROR_GAME_OVER = "GAME OVER";

 	public enum ERROR {
		BOX, PALETTE, PRINT, PALETTE_QUEUE, BOX_REFUSED
 	};
         
 	private WeakReference<LogReceiver> listener;
 
         /**
          * constructor of thread
          * creates the command socket
          * @throws IOException 
          */
 	public ThreadLog() throws IOException {
 		serverAddress = InetAddress.getLocalHost();
 		serverPort = 30035;
 		//socket creation
 		socketCommand = new Socket(serverAddress, serverPort);
 	}
 
         /**
          * sets the log receiver
          * @param listener listener
          */
 	public void setLogReceiver(LogReceiver listener) {
 		this.listener = new WeakReference<>(listener);
 	}
 
 	public interface LogReceiver {
 
 		public void onReveiveLog(String log);
 
 		public void onReceiveError(ERROR error);
 		
 		public void onGameOver();
 	}
 
         /**
          * the action of the thread
          */
 	@Override
 	public void run() {
 		//* Listening on socket
 		BufferedReader in;
 		boolean openedSocket = true;
 		while (openedSocket) {
 			try {
 				in = new BufferedReader(new InputStreamReader(socketCommand.getInputStream()));
 				while (in.ready()) {
 					String message_distant = in.readLine();
 					if (listener != null && listener.get() != null) {
 						switch (message_distant) {
 							case ERROR_BOX: {
 								listener.get().onReceiveError(ERROR.BOX);
 								break;
 							}
 							case ERROR_PALETTE: {
 								listener.get().onReceiveError(ERROR.PALETTE);
 								break;
 							}
 							case ERROR_PALETTE_QUEUE: {
 								listener.get().onReceiveError(ERROR.PALETTE_QUEUE);
 								break;
 							}
 							case ERROR_PRINT: {
 								listener.get().onReceiveError(ERROR.PRINT);
 								break;
 							}
 							case ERROR_BOX_REFUSED: {
 								listener.get().onReceiveError(ERROR.BOX_REFUSED);
 								break;
 							}
 							case ERROR_GAME_OVER: {
 								listener.get().onGameOver();
 								break;
 							}
 							default: {
 								listener.get().onReveiveLog(message_distant);
 								break;
 							}
 						}
 					}
 				}
 			} catch (IOException ex) {
 				Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
 				openedSocket = false;
 			}
 		}
 	}
 }
