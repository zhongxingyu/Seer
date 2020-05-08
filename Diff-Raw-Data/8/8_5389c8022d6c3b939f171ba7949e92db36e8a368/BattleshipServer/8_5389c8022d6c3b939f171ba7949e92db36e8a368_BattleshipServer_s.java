 package battlechallenge.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * The Class BattleshipServer.
  */
 public class BattleshipServer extends Thread {
 
 	/** The socket. */
 	private ServerSocket socket;
 
 	/** The manager. */
 	private GameManager manager;
 
 	/**
 	 * Instantiates a new battleship server.
 	 * 
 	 * @param port
 	 *            the port
 	 * @param manager
 	 *            the manager
 	 */
 	public BattleshipServer(int port, GameManager manager) {
 		if (manager == null || port < 0)
 			throw new IllegalArgumentException();
 		this.manager = manager;
 		try {
 			socket = new ServerSocket(port);
 			this.start();
 		} catch (IOException e) {
 			// TODO: Handle server cannot open port
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Thread#run()
 	 */
 	public void run() {
 		System.out.println("Server started on port:" + socket.getLocalPort());
 		new GameManagerWindow();
 		while (true) {
 			try {
 				manager.addPlayer(socket.accept());
 			} catch (IOException e) {
 				// TODO Handle error opening client socket 
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * The main method.
 	 * 
 	 * args[0] is port number
 	 * 
 	 * @param args
 	 *            the arguments
 	 */
 	public static void main(String[] args) {
		int port = 3002;
 		if (args.length > 0)
 			try {
 				port = Integer.parseInt(args[0]);
 			} catch (NumberFormatException e) {
 			}
 		new BattleshipServer(port, new GameManager());
 	}
 
 }
