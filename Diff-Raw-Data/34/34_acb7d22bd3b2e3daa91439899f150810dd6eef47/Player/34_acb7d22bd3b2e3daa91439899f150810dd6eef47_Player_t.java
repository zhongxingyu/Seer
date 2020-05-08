 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.rmi.AccessException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import com.ds.maze.ClientConnect;
 import com.ds.maze.GlobalInfoP2P;
 import com.ds.maze.Notify;
 import com.ds.maze.P2PBase;
 import com.ds.maze.PlayerInfoP2P;
 import com.ds.maze.PolicyFileLocator;
 
 
 @SuppressWarnings("serial")
 public class Player extends JFrame{
 	
 	private static Board board;
 	private static P2PBase changecord;
 	private static ConcurrentLinkedQueue<String> serverList;
 	private static String myKey;
 	private static String firstServerIp;
 	private static String myIp;
 	private HeartBeatSender hb;
 	
 	
 	public Player(){
 		
 		
 		/* 
 		 * This part of the code creates a registry for all peers. Invocation of the implementation methods would occur either remotely 
 		 * or locally depends on whether the peer is  the server or  client.
 		 */
 		System.setProperty("java.rmi.server.codebase", P2PBase.class.getProtectionDomain().getCodeSource().getLocation().toString());
 		System.setProperty("java.security.policy", PolicyFileLocator.getLocationOfPolicyFile());
 		
 		if(System.getSecurityManager() == null) {
 			System.setSecurityManager(new SecurityManager());
 		}
 		
 		try {
 			
 			//instantiate class variables
 			serverList=new ConcurrentLinkedQueue<String>();
 			myKey=new ClientKeygen().genKey();
 			if(firstServerIp == null)
 				firstServerIp=InetAddress.getLocalHost().getHostAddress();			
 			myIp=InetAddress.getLocalHost().getHostAddress();	        	
 			P2PBase engine = new PlayerMoveImplement(10);
 			P2PBase engineStub = (P2PBase)UnicastRemoteObject.exportObject(engine, 0);
 			Registry registry = LocateRegistry.createRegistry(9000);
 			registry.rebind(P2PBase.SERVICE_NAME, engineStub);
 			System.out.println("Created a registry at "+InetAddress.getLocalHost().getHostAddress()+" and port 9000");
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private static void makeConnectionSettings(String serverIP){
 		//Set system properties
 		Registry registry;
 		try {
 			changecord=null;
 			System.setProperty("java.security.policy", PolicyFileLocator.getLocationOfPolicyFile());
 			registry = LocateRegistry.getRegistry(serverIP,9000);
 			changecord= (P2PBase)registry.lookup(P2PBase.SERVICE_NAME);
 		} catch (AccessException e) {
 			
 			//Handle the connection loss exception
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			serverList.remove();
 			makeConnectionSettings(serverList.element());
 		} catch (NotBoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	public void startPlaying() {
		
 			
 			makeConnectionSettings(firstServerIp);
 			
 			ClientConnect connect=new ClientConnect(){
 
 				@Override
 				public void onSuccess(HashMap<String, Object> connect)
 						throws RemoteException {
 										
					System.out.println("I am here");
 					//Print the current state 
 					GlobalInfoP2P globalInfo=(GlobalInfoP2P)connect.get("GLOBALINFO");
 					
 					System.out.println("_________________________");
 					System.out.println("MY STATE"+"==>"+((PlayerInfoP2P)connect.get(myKey)).toString());
 					System.out.println("NO_OF_PLAYERS"+"==>"+globalInfo.getNumberOfplayers());
 					System.out.println("TREASURES ON OFFER ==> "+globalInfo.getSumOftreasures());
 					System.out.println("GRID WITH TREASURES");
 					
 					//get the list of peer IPs
 					
 					serverList=globalInfo.getPeerIPList();
 					int gridSize = globalInfo.getGridSize();
 					int[][] grid=globalInfo.getAtomicToIntGrid();
										
 					
 					System.out.println("_________________________");
 					
 					//Wait for a second before executing the moves
 					PlayerInfoP2P myinfo=(PlayerInfoP2P) connect.get(myKey);			
 					board = new Board(gridSize,myinfo.getxCord(),myinfo.getyCord(),grid);
 			        add(board);
 			        setTitle("Maze Game");
 			        setDefaultCloseOperation(EXIT_ON_CLOSE);
 			        setSize(gridSize*77, gridSize*78);
 			        setLocationRelativeTo(null);
 			        setVisible(true);
 			        setResizable(false);
 					updateBoard(connect);
 					System.out.println("Please start moving. Enter L/l for LEFT, R/r for RIGHT, U/u for UP and any key for DOWN ");
 					
 					//Once connection has been successfully established, start sending periodic heart beats to the server
 					hb=new HeartBeatSender();
 					
 					Thread heartbeat=new Thread(hb);   
 					heartbeat.setDaemon(true);
 					heartbeat.start();
 					
 				}
 
 				@Override
 				public void onFailure()
 						throws RemoteException {
 					System.out.println("The game has already started please try again later...");
 					System.exit(0);
 					
 					
 				}
 				
 				};
				
				try {
					UnicastRemoteObject.exportObject(connect);
					changecord.connectToServer(myKey,myIp,connect);
 		} catch (RemoteException e) {
			//serverList.remove();
			//makeConnectionSettings(serverList.element());
 			e.printStackTrace();
 		}
 			
 		
 	}
 	
 	
 	
 	public static void move(String move) throws InterruptedException {
 		
 		HashMap<String, Object> afterMove = null;
 		GlobalInfoP2P globalInfo=null;
 		int[][] gridAfterMove = null;
 		int gridSize;
 		try {
 			afterMove = changecord.moveToLocation(move,myKey);
 		} catch (RemoteException e) {
 			
 			changeServer();
 			try {
 				afterMove = changecord.moveToLocation(move,myKey);
 			} catch (RemoteException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}	
 			
 			e.printStackTrace();
 		}
  		
  		if(afterMove==null){
  			System.out.println("Game Over...");
  			System.exit(0);
  		}else if(afterMove.get(myKey)=="DISCONNECTED"){
  			System.out.println("Time out error. Game over!!");
  			System.exit(0);
  			
  		}else{
  			
  			globalInfo=(GlobalInfoP2P)afterMove.get("GLOBALINFO");
  			System.out.println("________AFTER MOVE__________");
 			System.out.println("MY STATE"+"==>"+afterMove.get(myKey));
 			System.out.println("NO_OF_PLAYERS"+"==>"+globalInfo.getNumberOfplayers());
 			System.out.println("GRID WITH TREASURES");
 			gridSize = globalInfo.getGridSize();
 			gridAfterMove=(int[][]) globalInfo.getAtomicToIntGrid();
 			for(int i=0;i<gridSize;i++){
 				for(int j=0;j<gridSize;j++){
 					System.out.print(gridAfterMove[i][j]+" ");
 				}
 				System.out.println("");
 			}
 			
 			
 			System.out.println("_______END OF AFTER MOVE________");
 			
  		}	
  		
  		updateBoard(afterMove);
 		
  		
  		
  		Thread.sleep(100);
 		
 	}
 	private static void updateBoard (HashMap<String, Object> afterMove){
 		int i =0, j = 0;
 		Set<String> keys = afterMove.keySet();
 		GlobalInfoP2P globalInfo=(GlobalInfoP2P)afterMove.get("GLOBALINFO");
 		int[][] gridAfterMove = (int[][]) globalInfo.getAtomicToIntGrid();
 		PlayerInfoP2P myinfo=(PlayerInfoP2P) afterMove.get(myKey);
 		int np = globalInfo.getNumberOfplayers();
 		int[][] cord = new int[np-1][2];
 		for(Object value : keys){
 			j = 0;
 			if(!((String)value).equals(myKey) && !(((String)value).equals("GLOBALINFO"))){
 				
 				PlayerInfoP2P cordinates=(PlayerInfoP2P) afterMove.get(value);
 				cord[i][j] = cordinates.getxCord();
 				j++;
 				cord[i][j] = cordinates.getyCord();
 				i++;
 			}
 		}
 		board.drawAgain(myinfo.getxCord(),myinfo.getyCord(),gridAfterMove,cord,np);
 		
 	}
 	
 	public static void main(String[] args) {
 		if(args.length > 0)
 			firstServerIp = args[0];
 
 		new Player().startPlaying();		
 	}
 	
 	private static void changeServer(){
 		
 		serverList.remove();
 		makeConnectionSettings(serverList.element());
 		try {
 			changecord.startBackup();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
 	}
 	
 	private  class HeartBeatSender extends Thread{
 		@Override
 		public void run(){
 			while(true){
 				Notify note;
 				try {
 					note = new NotifyImpl();
 					changecord.heartBeat(myKey,note);
 					Thread.sleep(200);
 					
 				} catch (InterruptedException e) {
 				} catch (RemoteException e) {
 					changeServer();					 
 				}
 			}
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	private static class NotifyImpl extends UnicastRemoteObject implements Notify{
 		protected NotifyImpl() throws RemoteException {
 			super();
 		}
 		
 		@Override
 		public void onSuccess(HashMap<String,Object> gameState) throws RemoteException {
 			
 			updateBoard(gameState);
 			
 		}
 		
 		@Override
 		public void onFailure(String crashedClient,HashMap<String,Object> gameState) throws RemoteException {
 			System.out.println(crashedClient+" crashed");
 			GlobalInfoP2P globalInfo=(GlobalInfoP2P)gameState.get("GLOBALINFO");
 			serverList=globalInfo.getPeerIPList();
 			
 		}
 		
 		
 	}
 	
 	public class Board extends JPanel  {
 		
 		private String location = "/image.png";
 		private int boardSize;
 		private Image image;
 		private int x = 0;
 		private int y = 0;
 		private int [][]grid;
 		private int [][] players;
 		private int numberPlayers;
 		private int gridSize;
 		
 		public Board(int boardSize,int x, int y, int[][] grid2) {
 			
 			setFocusable(true);
 			addKeyListener(new TAdapter());
 			setDoubleBuffered(true);
 			this.gridSize = boardSize;
 			this.boardSize=boardSize*75;
 			this.x = x;
 			this.y = y;
 			this.grid = grid2;
 			try {
 				image = ImageIO.read(getClass().getResource(location));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			
 		}
 		
 		
 		public void paint(Graphics g) {
 			super.paint(g);
 			Graphics2D g2d = (Graphics2D)g;
 			g2d.setColor(Color.RED);        
 			g2d.drawImage(image,40+y*75,40+x*75, this);
 			for( int i = 2; i <= boardSize+2; i=i+75 ){
 				g2d.drawLine(2, i, boardSize+2, i);
 				g2d.drawLine(i, 2, i, boardSize+2);
 			}
 			for(int k=0;k<gridSize;k++){
 				for(int j=0;j<gridSize;j++){
 					g.drawString(Integer.toString(grid[k][j]),20+j*75,20+k*75);					 
 				}	
 			}
 			if(numberPlayers > 1){
 				for(int k=0; k<(numberPlayers-1);k++){
 					int xCrd = players[k][0];
 					int yCrd = players[k][1];
 					g.drawString("P",20+yCrd*75,50+xCrd*75);
 					
 				}				 
 			}
 			Toolkit.getDefaultToolkit().sync();
 			g.dispose();
 		}
 		
 		
 		public void drawAgain(int newX, int newY,int [][] newGrd,int [][] newPlayers,int newNp) {
 			x = newX ;
 			y = newY ;
 			grid = newGrd;
 			players = newPlayers;
 			numberPlayers = newNp;			
 			repaint();  
 			
 		}
 		
 		private class TAdapter extends KeyAdapter {
 			
 			public void keyPressed(KeyEvent e) {
 				int key = e.getKeyCode();
 				switch(key) { 
 				 	case KeyEvent.VK_RIGHT: 
 				 		try {
 				 			Player.move("r");
 				 		} catch (InterruptedException e1) {
 				 			// TODO Auto-generated catch block
 				 			e1.printStackTrace();
 						} 
 				 		break; 
 						
 				 	case KeyEvent.VK_LEFT: 
 				 		try {
 				 			Player.move("l");
 				 		} catch (InterruptedException e1) {
 				 			// TODO Auto-generated catch block
 				 			e1.printStackTrace();
 				 		} 
 				 		break; 
 						
 				 	case KeyEvent.VK_UP: 
 				 		try {
 				 			Player.move("u");
 				 		} catch (InterruptedException e1) {
 				 			// TODO Auto-generated catch block
 				 			e1.printStackTrace();
 				 		} 
 				 		break; 
 						
 				 	case KeyEvent.VK_DOWN: 
 				 		try {
 				 			Player.move("d");
 				 		} catch (InterruptedException e1) {
 				 			// TODO Auto-generated catch block
 				 			e1.printStackTrace();
 				 		}
 				 		break; 
 				}
 			}
 			
 		}
 		
 	}
 	
 }
