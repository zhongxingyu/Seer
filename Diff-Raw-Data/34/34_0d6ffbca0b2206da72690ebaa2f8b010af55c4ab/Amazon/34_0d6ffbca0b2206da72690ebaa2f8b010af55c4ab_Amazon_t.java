 package ubco.ai.games;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.regex.Pattern;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import net.n3.nanoxml.IXMLElement;
 
 import ubco.ai.*;
 import ubco.ai.connection.*;
 import ubco.ai.games.*;
 
 /**
  * for testing purposes only. An GUI Amazon client for manual playing 
  * 
  * @author yongg
  *
  */
 
 public class Amazon extends JFrame implements GamePlayer{
 
     private GameClient gameClient;
     
     private GameBoard board = null;
  
     private boolean isPlayerA = false;
     private boolean gameStarted = false;
     
     private ArrayList<String[]> roomList = null;
     private GameRoom currentRoom = null; 
     
     private int roomID = -1;
     
     public String usrName = null;
     
     
     /**
      * Constructor 
      * @param args
      */
 	public static void main(String[] args) {
 		String arg0 = "SnozamaHumanTest", arg1 = "wearethebest";
 		boolean bot = false;
 		
 		if (args.length > 0) {
 			arg0 = args[0];
 		}
 		if (args.length > 1) {
 			arg1 = args[1];
 		}
 		if(args.length > 2 && args[2].equals("true")){
 	       bot = true;
 		}
 	    else{
 	    	bot = false;
 	    }
 	    
 		Amazon game = new Amazon(arg0, arg1, bot);
     }
     
     
 	/**
 	 * Constructor
 	 * @param name
 	 * @param passwd
 	 * @param gamebot
 	 */
     public Amazon(String name, String passwd, boolean gamebot){  
 	
 	   this.usrName = name;
 		
        gameClient = new GameClient(name, passwd, this);	   
        currentRoom = gameClient.roomList.get(0); 
        roomID = currentRoom.roomID;
 	   gameClient.joinGameRoom(currentRoom.roomName);
 	   
 	   
 	   this.setSize(500, 500);
 	   setup();
        this.setVisible(true); 
 	}
 	
 	
 	/**
 	 * Implements the GamePlayer interface. All the messages after the user login
 	 * will be forwarded to this method by the GameClient.
 	 * 
 	 * See GameMessage.java and MessageFormat.java  
 	 * 
 	 * @param msg the GameMeesage instance that hold the message type and the action message in XML 
 	 * 				format.     
 	 */
 	public boolean handleMessage(GameMessage msg){
 		
         IXMLElement xml = ServerMessage.parseMessage(msg.msg); 
         String type = xml.getAttribute("type", "WRONG!");
         System.out.println(msg);
         
         if(type.equals(GameMessage.ACTION_ROOM_JOINED)){
         	onJoinRoom(xml);
         }
         else if (type.equals(GameMessage.ACTION_GAME_START)){
         	this.gameStarted = true;
         	
     		IXMLElement usrlist = xml.getFirstChildNamed("usrlist");
     		int ucount = usrlist.getAttribute("ucount", -1);
     		
     		Enumeration ch = usrlist.enumerateChildren();
     		while(ch.hasMoreElements()){
     			
     			System.out.println("game start!!!!!!");
     			
     			IXMLElement usr = (IXMLElement)ch.nextElement();
     			int id = usr.getAttribute("id", -1); 
     			String name = usr.getAttribute("name", "nnn");
     			
     			if(!name.equalsIgnoreCase(usrName)){
     				continue;
     			}
     				
     			String role = usr.getAttribute("role", "W");
     			if(role.equalsIgnoreCase("W")){
     				isPlayerA = true;
     			}
     			else{
     				isPlayerA = false;
     			}
     			
     			board.init(isPlayerA);
     			
     			//System.out.println("Name = " + name + ", ID = " + id + "isPlayerA =  " + isPlayerA);
     		}
         	
         	
         	System.out.println("Game Start: " + msg.msg);
         }
         else if(type.equals(GameMessage.ACTION_MOVE)){
             this.handleOpponentMove(xml); 
         }        
  
 		return true;
 	}
 	
 	
 	 
 
 	//handle the response of joining a room
 	private void onJoinRoom(IXMLElement xml){
 		IXMLElement usrlist = xml.getFirstChildNamed("usrlist");
 		int ucount = usrlist.getAttribute("ucount", -1);
 		
 		Enumeration ch = usrlist.enumerateChildren();
 		while(ch.hasMoreElements()){
 			IXMLElement usr = (IXMLElement)ch.nextElement();
 			int id = usr.getAttribute("id", -1); 
 			String name = usr.getAttribute("name", "NO!");  
 		}
 	 
 	}
 	
  
 
 	
 	//handle the event that the opponent makes a move. 
 	private void handleOpponentMove(IXMLElement xml){
 		System.out.println("Opp Move");
 		if(!gameStarted){
 			return;
 		} 
     	
         IXMLElement c1 = xml.getFirstChildNamed("queen");
         String qmove = c1.getAttribute("move", "default");
         
         IXMLElement c2 = xml.getFirstChildNamed("arrow");
         String amove = c2.getAttribute("move", "defalut");
               
 		if(isPlayerA){
 			board.playerAMove = false;
 		}
 		else
 		{
 			board.playerAMove = true;
 		}
 		
 		int qX = 0;
 		int qY = 0;
 				
 		char c = qmove.charAt(3);
 		qX = c - 97; 
 		qY = Integer.parseInt(qmove.substring(4,5));
 		
 		int qfx = 0;
 		int qfy = 0;
 		c = qmove.charAt(0);
 		qfx = c - 97;
 		qfy = Integer.parseInt(qmove.substring(1, 2));
 		
 		
 		int aX = 0;
 		int aY = 0;
 		c = amove.charAt(0);
 		aX = c - 97;
 		aY = Integer.parseInt(amove.substring(1, amove.length()));
 	    board.markPosition(qX, qY, aX, aY, qfx, qfy,true); 
 	    
 	}
 		 
 	
 	//set up the game board
 	private void setup(){
 		Container contentPane = this.getContentPane();
 		contentPane.setLayout(new  BorderLayout());
 		 
 		contentPane.add(Box.createVerticalGlue()); 
 		
 		board = createGameBoard();
 		
 		contentPane.add(board,  BorderLayout.CENTER);
 	}
 	
 
 	private GameBoard createGameBoard(){
 		return new GameBoard(this);
 	}
 	
 	/**
 	 * 
 	 * @return true if white
 	 */
     //public boolean isFirstPlayer(){
     //	return isPlayerA;
     //}
 	
     /**
      * handle a move of this player --- send the info to the server.
      * @param x queen row index 
      * @param y queen col index
      * @param arow arrow row index
      * @param acol arrow col index
      * @param qfr queen original row
      * @param qfc queen original col
      */
 	public void playerMove(int x, int y, int arow, int acol, int qfr, int qfc){		
 		this.sendToServer(GameMessage.ACTION_MOVE,  roomID, x,  y, arow, acol, qfr, qfc);		
 	}
     
 	/**
 	 * Send a message to the server. This is a specific method for the XOXO game  
 	 * 
 	 * @param action 
 	 * @param roomID
 	 * @param posX
 	 * @param posY
 	 * @param arow arrow row index
      * @param acol arrow col index
      * @param qfr queen original row
      * @param qfc queen original col
 	 */
 	public void sendToServer(String action, int roomID, int posX, int posY, 
 			 int arow, int acol, int qfr, int qfc){
 		//String msg = "%xt%cosc322%" +  "game-message" + 
 		//	     "%" + roomID + "%"+  action + "%" + posX + "%" + posY + "%";
 		
 		String actionMsg = "<action type='" +  action + "'>";
 
 		Character c = new Character((char) (97 + qfr));
 		
 		actionMsg = actionMsg + "<queen move='" + c.charValue() + String.valueOf(qfc) + "-";  
 		
 		c = new Character((char)(97 + posX)); 
 		
 		actionMsg = actionMsg + c.charValue() + String.valueOf(posY) 
 		              + "'>" + "</queen> ";
 		
 		c = new Character((char) (97 + arow));
 		
 		actionMsg = actionMsg + "<arrow move='" + c.charValue() + String.valueOf(acol) + 
 		      "'>" + "</arrow>";
 		
 		 
 		actionMsg = actionMsg + "</action>";
 
 		System.out.println(actionMsg);
 		
 		String msg = ServerMessage.compileGameMessage(GameMessage.MSG_GAME, roomID, actionMsg);
 	   	
 	   gameClient.sendToServer(msg, true);	 
 	}
 	
 
 	/**
 	 * The game board
 	 * 
 	 * @author yongg
 	 *
 	 */
 	public class GameBoard extends JPanel{
 		
 		private  int rows = 10;
 		private  int cols = 10; 
 		
 		int width = 500;
 		int height = 500;
 		int cellDim = width / 10; 
 		int offset = width / 20;
 		
 		int posX = -1;
 		int posY = -1;
 	
 		int r = 0;
 		int c = 0;
 		  
 		
 		Amazon game = null; 
 	    private BoardGameModel gameModel = null;
 		
 		boolean playerAMove;
 		
 		public GameBoard(Amazon game){
 	        this.game = game;	       
 	        gameModel = new BoardGameModel(this.rows, this.cols);
 	      	        
 	        //if(!game.isGamebot){
 	        	addMouseListener(new  GameEventHandler());
 	        //}
 		}
 		
 		
 		public void init(boolean isPlayerA){
 	        String tagS = null;
 	        String tagO = null;
 	        
 	        tagS = BoardGameModel.POS_MARKED_SELF;
 	        tagO = BoardGameModel.POS_MARKED_OPP;;
 	        
 	        if(game.isPlayerA){
 	        	gameModel.gameBoard[0][3] = tagS;
 	        	gameModel.gameBoard[0][6] = tagS;
	        	gameModel.gameBoard[3][0] = tagS;
	        	gameModel.gameBoard[3][9] = tagS;
 	        	
	        	gameModel.gameBoard[6][0] = tagO;
	        	gameModel.gameBoard[6][9] = tagO;
 	        	gameModel.gameBoard[9][3] = tagO;
 	        	gameModel.gameBoard[9][6] = tagO;
 	        }
 	        else{
  
 	        	gameModel.gameBoard[0][3] = tagO;
 	        	gameModel.gameBoard[0][6] = tagO;
	        	gameModel.gameBoard[3][0] = tagO;
	        	gameModel.gameBoard[3][9] = tagO;
 	        	
	        	gameModel.gameBoard[6][0] = tagS;
	        	gameModel.gameBoard[6][9] = tagS;
 	        	gameModel.gameBoard[9][3] = tagS;
 	        	gameModel.gameBoard[9][6] = tagS;
 	        }
 			
 		}
 		
 		
 		/**
 		 * repaint the part of the board
 		 * @param qrow queen row index
 		 * @param qcol queen col index 
 		 * @param arow arrow row index
          * @param acol arrow col index
          * @param qfr queen original row
          * @param qfc queen original col
 		 */
 		public boolean markPosition(int qrow, int qcol, int arow, int acol, 
 				  int qfr, int qfc, boolean  opponentMove){						
 			
 			boolean valid = gameModel.positionMarked(qrow, qcol, arow, acol, qfr, qfc, opponentMove);
 			repaint();						
 			return valid;
 		}
 		
 		// JCmoponent method
 		protected void paintComponent(Graphics gg){
 			Graphics g = (Graphics2D) gg;
 	 
 			//super.paintComponents(g);
 			
 			for(int i = 0; i <= rows; i++){
 				g.drawLine(i * cellDim + offset, offset, i * cellDim + offset, rows * cellDim + offset);
 				g.drawLine(offset, i*cellDim + offset, cols * cellDim + offset, i*cellDim + offset);
 			}
 			
 			for(int r = 0; r < 10; r++){
 			  for(int c = 0; c < 10; c++){
 				
 					posX = c * cellDim + offset;
 					//posY = r * cellDim + offset;
 					
 					posY = (9 - r) * cellDim + offset;
 					
 				if(gameModel.gameBoard[r][c].equalsIgnoreCase(BoardGameModel.POS_AVAILABLE)){
 					g.clearRect(posX + 1, posY + 1, 48, 48);					
 				}
 				  
 
 			if(game.isPlayerA && gameModel.gameBoard[r][c].equalsIgnoreCase(
 					  BoardGameModel.POS_MARKED_SELF)){
 				   g.drawOval(posX, posY, 50, 50);
 			}
 			else if (!game.isPlayerA && gameModel.gameBoard[r][c].equalsIgnoreCase(
 					BoardGameModel.POS_MARKED_SELF)){
 				g.fillOval(posX, posY, 50, 50);
 				
 				//g.drawLine(posX, posY, posX + 50, posY + 50);
 				//g.drawLine(posX, posY + 50, posX + 50, posY);
 			}
 			else if(game.isPlayerA && gameModel.gameBoard[r][c].equalsIgnoreCase(
 					  BoardGameModel.POS_MARKED_OPP)){
 				g.fillOval(posX, posY, 50, 50);
 			}
 			else if(!game.isPlayerA && gameModel.gameBoard[r][c].equalsIgnoreCase(
 					  BoardGameModel.POS_MARKED_OPP)){
 				g.drawOval(posX, posY, 50, 50);
 			}
 			else if (gameModel.gameBoard[r][c].equalsIgnoreCase(
 					  BoardGameModel.POS_MARKED_ARROW)) {
 				g.drawLine(posX, posY, posX + 50, posY + 50);
 				g.drawLine(posX, posY + 50, posX + 50, posY);
 			}
 			
 		    }
 		  }
 			
 		}//method
 		
 		//JComponent method
 		public Dimension getPreferredSize() {
 		        return new Dimension(500,500);
 		 }
 
 		/**
 		 * Handle mouse events
 		 * 
 		 * @author yongg
 		 */
 		public class GameEventHandler extends MouseAdapter{
 			 
 			    int counter = 0;
 			    
 			    int qrow = 0;
 			    int qcol = 0;
 			
 			    int qfr = 0;
 			    int qfc = 0;
 			    
 			    int arow = 0;
 			    int acol = 0; 
 			
 	            public void mousePressed(MouseEvent e) {
 	            	
 	            	if(!gameStarted){
 	            		//return; 
 	            	}
 	            	
                     int x = e.getX();
                     int y = e.getY();
 	            
                     
                     if(((x - offset) < -5) || ((y - offset) < -5)){
                     	return;
                     }
                     
                     int row = (y - offset) / cellDim;                        
                     int col = (x - offset) / cellDim;
                     
                     if(counter == 0){
                     	qfr = row;
                     	qfc = col;
                     	
                     	qfr = 9 - qfr;
                     	counter++;
                     }
                     else if(counter ==1){
                     	qrow = row;
                     	qcol = col;
                     	
                     	qrow = 9 - qrow;
                     	counter++;
                     }
                     else if (counter == 2){
                     	arow = row;
                     	acol = col;
                     	
                     	arow = 9 - arow;
                     	counter++;
                     }
                     
                     if(counter == 3){
                       counter = 0; 	
                       boolean validMove = markPosition(qrow, qcol, arow, acol, qfr, qfc, false); // update itself
 	               
                       if(validMove){
                     	game.playerMove(qrow, qcol, arow, acol, qfr, qfc); //to server
                       }
                       
                       qrow = 0;
                       qcol = 0;
                       arow = 0;
                       acol = 0;
                       
                     }
 	            }			 
 		 }//end of GameEventHandler		
 	
 	}//end of GameBoard  
 
 
 	public boolean handleMessage(String msg) throws Exception {
 		System.out.println("Time Out ------ " + msg); 
 		return true;
 	}
 	
 }//end of Amazon
