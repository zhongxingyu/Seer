 package twelve.team;
 
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.WindowConstants;
 
 import twelve.team.Board.moveType;
 import twelve.team.Piece.Team;
 import twelve.team.Settings.GameType;
 
 public class NetworkGame extends Thread implements GameControllerListener{
 	
 	
 	public NetworkGame(GameController c, boolean isServer_, boolean enabled_){
 		controller = c;
 		isServer = isServer_;
 		enabled = enabled_;
 		controller.addGameControllerListener(this);
 	}
 	
 	public void setIpAndPort(String ip, int port){
 		this.ip = ip;
 		this.port = port;
 	}
 	
 	public void showConnectionSettings(boolean ipEnabled){
 		if(!enabled)
 			return;
 		NetworkPanel panel = new NetworkPanel(new JFrame(), true, this, ipEnabled);
 		panel.setVisible(true);
 	}
 	
 	public void run(){
 		if(!enabled)
 			return;
 		
 		String inputLine;
 		ServerSocket server = null;
 		Socket client = null;
 		if(isServer){
 			
 			//is the server
 			try {
 				//Start the server
 				server = new ServerSocket(port);
 				ready = true;
 				//wait for a connection
 				client = server.accept();
 				
 				out = new PrintWriter(client.getOutputStream(), true);
 				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 				
 				
 				//send initial board settings and welcome messages
 				Settings settings = controller.getSettings();
 				out.println("WELCOME");
 				out.println("INFO " + settings.boardWidth + " " + settings.boardHeight + " B " + settings.gameTimer);
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			//is the client
 			try {
 				client = new Socket(ip, port);
 				
 				out = new PrintWriter(client.getOutputStream(), true);
 				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		
 		try {
 			while((inputLine = in.readLine()) != null){
 				controller.debug(inputLine);
 				if(inputLine.equals("READY")){
 					//client is ready to start the game! LETS DO THIS!
 					out.println("BEGIN");
 					ready = true;
 					recievedOk = false;
 				} else if(inputLine.equals("OK") || inputLine.equals("WELCOME")){
 					recievedOk = true;
 				} else if(inputLine.equals("ILLEGAL")){
 					//uh oh, we screwed up
 					if(isServer){
 						out.println("WINNER");
 					}
 				} else if(inputLine.equals("TIME")){
 										
 				} else if(inputLine.equals("WINNER")){
					controller.endGame(false);//we win!
 					controller.incrementWins(localPlayer);
 				} else if(inputLine.equals("LOSER")){
 					// we lose :(
					controller.endGame(true);
 					controller.incrementWins(localPlayer == Team.BLACK ? Team.WHITE : Team.BLACK);
 				} else if(inputLine.startsWith("INFO")){
 					String[] params = inputLine.split(" ");
 					if(!recievedOk || params.length != 5){
 						controller.debug("Server sent bad INFO string");
 						out.println("ILLEGAL");
 						out.println("LOSER");
 					}	
 					ready = true;
 					
 					int cols = Integer.parseInt(params[1].trim());
 					int rows = Integer.parseInt(params[2].trim());
 					this.localPlayer = params[3].equals("W") ? Team.WHITE : Team.BLACK;
 					long timerTime = Long.parseLong(params[4]);
 					
 					Settings settings = new Settings();
 					settings.boardHeight = rows;
 					settings.boardWidth = cols;
 					settings.gameType = GameType.MULT_CLIENT;
 					settings.gameTimer = timerTime;
 					controller.updateSettings(settings);
 					controller.showBoard();
 					out.println("OK");
 					
 				} else if(inputLine.startsWith("A") || inputLine.startsWith("R") || inputLine.startsWith("S") || inputLine.startsWith("P")){
 					if(!recievedOk){
 						//client did not acknowledge last message
 						controller.debug("Client did not acknowledge last message");
 						out.println("ILLEGAL");
 						out.println("LOSER");
 					}
 					recievedOk = false;
 					out.println("OK");
 					if(!processInput(inputLine)){
 						ready = true;
 						controller.debug("Client move was invalid");
 						out.println("ILLEGAL");
 						out.println("LOSER");
 					}
 				} else {
 					controller.debug("Invalid input recieved from client");
 					out.println("ILLEGAL");
 					out.println("LOSER");
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try{
 				if(client != null)
 					client.close();
 				if(server != null)
 					server.close();
 			} catch(Exception e){
 				controller.debug("Couldnt close client and server.  Already closed?");
 			}
 			
 		}
 	}
 	
 	public boolean processInput(String input){
 		String[] moveStrings = input.split("\\+");
 		String[] moveString;
 		moveType type = moveType.ADVANCE;
 		Point start = new Point();
 		Point end = new Point();
 		
 		try{
 			for(int i=0;i<moveStrings.length;i++){
 				moveString = moveStrings[i].trim().split(" ");
 				if(moveString.length != 5){
 					controller.debug("Invalid Move String length: " + moveString.length);
 					return false;
 				}
 				
 				switch(moveString[0]){
 				case "A":
 					type = moveType.ADVANCE;
 					break;
 				case "R":
 					type = moveType.RETREAT;
 					break;
 				case "S":
 					type = moveType.SACRIFICE;
 					break;
 				default:
 					type = moveType.NONE;
 				}
 				start = new Point(Integer.parseInt(moveString[1]), Integer.parseInt(moveString[2]));
 				end = new Point(Integer.parseInt(moveString[3]), Integer.parseInt(moveString[4]));
 				controller.debug(moveString[0] + " " + (start.x-1) + " " + (start.y-1) + " " + (end.x-1) + " " + (end.y-1));
 				
 				boolean bool = controller.move(new Point(start.x-1, start.y-1), new Point(end.x-1, end.y-1), type);
 				if(!bool && i != moveStrings.length-1){
 					return false;
 				}
 				if(bool && i == moveStrings.length-1){
 					return false;
 				}
 			}
 			return true;
 		}
 		catch(Exception e){
 			controller.debug(e.getMessage());
 			controller.debug("Client had an invalid move");
 			return false;
 		}
 	}
 	
 	@Override
 	public void onNextTurn() {
 		// TODO Auto-generated method stub
 		if(!enabled)
 			return;
 		
 		while(!ready){
 			//busy loop while not ready
 		}
 		
 		//If current turn != localPlayer, the localPlayer just finished their turn
 		if(controller.getTurn() == localPlayer)
 			return;
 		if(ready){
 			String outputLine = "";
 			ArrayList<Move> moves = controller.getMoves();
 			for(int i=0;i < moves.size();i++){
 				Move move = moves.get(i);
 				char moveChar;
 				switch(move.type){
 				case ADVANCE:
 				case NONE:
 					moveChar = 'A';
 					break;
 				case PAIKA:
 					moveChar = 'P';
 					break;
 				case RETREAT:
 					moveChar = 'R';
 					break;
 				case SACRIFICE:
 					moveChar = 'S';
 					break;
 				default:
 					moveChar = 'A';
 					break;
 				
 				}
 				outputLine += moveChar + " " + (move.start.x+1) + " " 
 						+ (move.start.y+1) + " " + (move.end.x+1) + " " + (move.end.y+1);
 				if(i != moves.size()-1)
 					outputLine += " + ";
 			}
 			controller.debug(outputLine);
 			out.println(outputLine);
 		}
 	}
 
 	@Override
 	public void onTimeUp() {
 		// TODO Auto-generated method stub
 		if(!ready || !enabled)
 			return;
 		if(!isServer)
 			return;
 		out.println("TIME");
 		if(controller.getTurn() == localPlayer){
 			out.println("WINNER");
 			controller.endGame(true);
 		}
 		else{
 			out.println("LOSER");
 			controller.endGame(false);
 		}
 	}
 	
 	@Override
 	public void onGameWin(Team winner) {
 		if(!isServer)
 			return;
 		if(winner == null){
 			out.println("TIE");
 		} else if(winner == localPlayer){
 			out.println("LOSER");
 		} else {
 			out.println("WINNER");
 		}
 	}
 	
 	@Override
 	public void newGame() {
 				
 	}
 	
 	private PrintWriter out;
 	private BufferedReader in;
 	private String ip;
 	private int port;
 	
 	
 	
 	private boolean ready = false;
 	private boolean recievedOk = false;
 	private GameController controller;
 	private boolean isServer;
 	private boolean enabled;
 	public Team localPlayer = Team.WHITE;
 	
 	
 	
 }
 
 class NetworkPanel extends JDialog {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 621347222230937948L;
 	/**
      * Creates new form NetworkPanel
      */
 	public NetworkPanel(Frame parent, boolean modal, NetworkGame net){
 		this(parent, modal, net, false);
 	}
 	
     public NetworkPanel(Frame parent, boolean modal, NetworkGame net, boolean diableIp) {
         super(parent, modal);
         network = net;
         initComponents();
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     
     // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
     private void initComponents() {
 
         serverIpLabel = new JLabel();
         serverIp = new JTextField();
         portLabel = new JLabel();
         port = new JTextField();
         okButton = new JButton();
 
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
         serverIpLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         serverIpLabel.setText("Server IP:");
 
         portLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         portLabel.setText("Port:");
         if(disableIp){
         	serverIp.setEnabled(false);
         	serverIp.setEditable(false);
         }
 
         okButton.setText("Ok");
         okButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
             	String ip = serverIp.getText().trim();
             	String portString = port.getText();
             	network.setIpAndPort(ip, Integer.parseInt(portString));
             	dispose();
             }
         });
 
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(serverIpLabel)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(serverIp, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(portLabel)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(port, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(okButton)
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(serverIpLabel)
                     .addComponent(serverIp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(portLabel)
                     .addComponent(port, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(okButton))
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
         
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
     	int x = (dim.width - getWidth())/2;
     	int y = (dim.height - getHeight())/2;
     	setLocation(x,y);
     }// </editor-fold>                        
 
     // Variables declaration - do not modify
     private boolean disableIp;
     private JTextField port;
     private JButton okButton;
     private JLabel portLabel;
     private JTextField serverIp;
     private JLabel serverIpLabel;
     private NetworkGame network;
     // End of variables declaration                   
 }
