 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Random;
 
 import javax.swing.JFrame;
 
 
 public class GameLogic {
 	
 	private ActivePlayerPanel ActivePlayer;
 	private GameBoardPanel GameBoard;
 	private OtherPlayerPanel OtherPlayers;
 	private ChatPanel ChatPanel;
 	private GameStatusPanel StateOfGamePanel;
 	private MainGamePanel MainGamePanel;
 	private JFrame tFrame;
 	
 	ArrayList<GamePiece> allP;
 	
 	GamePiece thief;
 	ArrayList<GamePiece> road;
 	ArrayList<GamePiece> verts;
 	
 	ArrayList<String> loseR,winR;
 	HashMap<String, ArrayList<String>> aiResponses;
 	
 	ArrayList<Player> playerList;
 	Player activeP;
 	int actPNum = 0;
 	
 	ArrayList<Integer> hexRolls;
 	ArrayList<Resource> resDistr;
 	ArrayList<Resource> portOrder;
 	ArrayList<Vertex> SClocs;
 	ArrayList<Edge> edges;
 	
 	
 	public GameLogic(){
 		thief = new GamePiece();
 		thief.type = 3;
 		thief.player = -1;
 		
 		road = new ArrayList<GamePiece>();
 		verts = new ArrayList<GamePiece>(54);
 		
 		playerList = new ArrayList<Player>();
 		loseR = new ArrayList<String>();
 		winR = new ArrayList<String>();
 		aiResponses= new HashMap<String, ArrayList<String>>();
 
 		//Hard coding players in interest of getting stuff done
 		playerList.add(new Player("Shamino", "mylittlepony"));
 		playerList.add(new Player("YOLO", "yourmom"));
 		playerList.add(new Player("Kurios", "coonapp"));
 		playerList.add(new Player("Ghandi", "nukeseverywhere"));
 		
 		
 		playerList.get(0).color = new Color(255, 128, 0); // Player 1 = Orange
 		playerList.get(1).color = new Color(128, 0, 255); // Player 2 = Purple
 		playerList.get(2).color = new Color(0, 255, 255); // Player 3 = Cyan
 		playerList.get(3).color = new Color(102, 51,  0); // Player 4 = Brown
 		
 		playerList.get(0).pNum = 0;
 		playerList.get(1).pNum = 1;
 		playerList.get(2).pNum = 2;
 		playerList.get(3).pNum = 3;
 		
 		activeP = playerList.get(0);
 				
 		hexRolls = new ArrayList<Integer>();
 		hexRolls.add(11);
 		hexRolls.add(12);
 		hexRolls.add(9);
 		hexRolls.add(4);
 		hexRolls.add(6);
 		hexRolls.add(5);
 		hexRolls.add(10);
 		hexRolls.add(null);
 		hexRolls.add(3);
 		hexRolls.add(11);
 		hexRolls.add(4);
 		hexRolls.add(8);
 		hexRolls.add(8);
 		hexRolls.add(10);
 		hexRolls.add(9);
 		hexRolls.add(3);
 		hexRolls.add(5);
 		hexRolls.add(2);
 		hexRolls.add(6);
 		
 		resDistr = new ArrayList<Resource>();
 		// ordered from left to right, top to bottom.
 		resDistr.add(new Resource("sheep"));
 		resDistr.add(new Resource("wood"));
 		resDistr.add(new Resource("wheat"));
 		resDistr.add(new Resource("brick"));
 		resDistr.add(new Resource("iron"));
 		resDistr.add(new Resource("brick"));
 		resDistr.add(new Resource("wood"));
 		resDistr.add(new Resource("desert"));
 		resDistr.add(new Resource("sheep"));
 		resDistr.add(new Resource("wheat"));
 		resDistr.add(new Resource("sheep"));
 		resDistr.add(new Resource("wheat"));
 		resDistr.add(new Resource("brick"));
 		resDistr.add(new Resource("wood"));
 		resDistr.add(new Resource("wood"));
 		resDistr.add(new Resource("iron"));
 		resDistr.add(new Resource("iron"));
 		resDistr.add(new Resource("wheat"));
 		resDistr.add(new Resource("sheep"));
 		
 		portOrder = new ArrayList<Resource>();
 		// ordered from top left corner, clockwise.
 		portOrder.add(new Resource("any"));
 		portOrder.add(new Resource("sheep"));
 		portOrder.add(new Resource("any"));
 		portOrder.add(new Resource("any"));
 		portOrder.add(new Resource("brick"));
 		portOrder.add(new Resource("wood"));
 		portOrder.add(new Resource("any"));
 		portOrder.add(new Resource("wheat"));
 		portOrder.add(new Resource("iron"));
 		
 		SClocs = new ArrayList<Vertex>();
 		// ordered from left to right, top to bottom.
 		// vertex's parameter order starts from above or top left, clockwise
 		// v shaped triangle
 		SClocs.add(new Vertex(-2-0, -1, 0));
 		SClocs.add(new Vertex(-1, -2-1, 1));
 		SClocs.add(new Vertex(-1, -1, 2));
 		// ^ shaped triangle
 		SClocs.add(new Vertex(-2-0, 0, -1));
 		SClocs.add(new Vertex(-1, 1, 0));
 		SClocs.add(new Vertex(-2-1, 2, 1));
 		SClocs.add(new Vertex(-1, -1, 2));
 		// v shaped triangle
 		SClocs.add(new Vertex(-1, 0, 3));
 		SClocs.add(new Vertex(0, 1, 4));
 		SClocs.add(new Vertex(1, 2, 5));
 		SClocs.add(new Vertex(2, -2-2, 6));
 		// ^ shaped triangle
 		SClocs.add(new Vertex(-1, 3, -2-8));
 		SClocs.add(new Vertex(0, 4, 3));
 		SClocs.add(new Vertex(1, 5, 4));
 		SClocs.add(new Vertex(2, 6, 5));
 		SClocs.add(new Vertex(-2-2, -1, 6));
 		// v shaped triangle
 		SClocs.add(new Vertex(-2-8, 3, 7));
 		SClocs.add(new Vertex(3, 4, 8));
 		SClocs.add(new Vertex(4, 5, 9));
 		SClocs.add(new Vertex(5, 6, 10));
 		SClocs.add(new Vertex(6, -1, 11));
 		// ^ shaped triangle
 		SClocs.add(new Vertex(-1, 7, -1));
 		SClocs.add(new Vertex(3, 8, 7));
 		SClocs.add(new Vertex(4, 9, 8));
 		SClocs.add(new Vertex(5, 10, 9));
 		SClocs.add(new Vertex(6, 11, 10));
 		SClocs.add(new Vertex(-1, -2-3, 11));
 		// v shaped triangle
 		SClocs.add(new Vertex(-1, 7, -1));
 		SClocs.add(new Vertex(7, 8, 12));
 		SClocs.add(new Vertex(8, 9, 13));
 		SClocs.add(new Vertex(9, 10, 14));
 		SClocs.add(new Vertex(10, 11, 15));
 		SClocs.add(new Vertex(11, -2-3, -1));
 		// ^ shaped triangle
 		SClocs.add(new Vertex(7, 12, -2-7));
 		SClocs.add(new Vertex(8, 13, 12));
 		SClocs.add(new Vertex(9, 14, 13));
 		SClocs.add(new Vertex(10, 15, 14));
 		SClocs.add(new Vertex(11, -1, 15));
 		// v shaped triangle
 		SClocs.add(new Vertex(-2-7, 12, -1));
 		SClocs.add(new Vertex(12, 13, 16));
 		SClocs.add(new Vertex(13, 14, 17));
 		SClocs.add(new Vertex(14, 15, 18));
 		SClocs.add(new Vertex(15, -1, -2-4));
 		// ^ shaped triangle
 		SClocs.add(new Vertex(12, 16, -1));
 		SClocs.add(new Vertex(13, 17, 16));
 		SClocs.add(new Vertex(14, 18, 17));
 		SClocs.add(new Vertex(15, -2-4, 18));
 		// v shaped triangle
 		SClocs.add(new Vertex(-1, 16, -2-6));
 		SClocs.add(new Vertex(16, 17, -1));
 		SClocs.add(new Vertex(17, 18, -2-5));
 		SClocs.add(new Vertex(18, -1, -1));
 		// ^ shaped triangle
 		SClocs.add(new Vertex(16, -1, -2-6));
 		SClocs.add(new Vertex(17, -2-5, -1));
 		SClocs.add(new Vertex(18, -1, -1));
 		
 		edges = new ArrayList<Edge>();
 		edges.add(new Edge(0,3)); // /
 		edges.add(new Edge(0,4)); // \
 		edges.add(new Edge(1,4)); // /
 		edges.add(new Edge(1,5)); // \
 		edges.add(new Edge(2,5)); // /
 		edges.add(new Edge(2,6)); // \
 
 		edges.add(new Edge(3,7)); // |
 		edges.add(new Edge(4,8)); // |
 		edges.add(new Edge(5,9)); // |
 		edges.add(new Edge(6,10)); // |
 
 		edges.add(new Edge(7,11)); // /
 		edges.add(new Edge(7,12)); // \
 		edges.add(new Edge(8,12)); // /
 		edges.add(new Edge(8,13)); // \
 		edges.add(new Edge(9,13)); // /
 		edges.add(new Edge(9,14)); // \
 		edges.add(new Edge(10,14)); // /
 		edges.add(new Edge(10,15)); // \
 
 		edges.add(new Edge(11,16)); // |
 		edges.add(new Edge(12,17)); // |
 		edges.add(new Edge(13,18)); // |
 		edges.add(new Edge(14,19)); // |
 		edges.add(new Edge(15,20)); // |
 
 		edges.add(new Edge(16,21)); // /
 		edges.add(new Edge(16,22)); // \
 		edges.add(new Edge(17,22)); // /
 		edges.add(new Edge(17,23)); // \
 		edges.add(new Edge(18,23)); // /
 		edges.add(new Edge(18,24)); // \
 		edges.add(new Edge(19,24)); // /
 		edges.add(new Edge(19,25)); // \
 		edges.add(new Edge(20,25)); // /
 		edges.add(new Edge(20,26)); // \
 
 		edges.add(new Edge(21,27)); // |
 		edges.add(new Edge(22,28)); // |
 		edges.add(new Edge(23,29)); // |
 		edges.add(new Edge(24,30)); // |
 		edges.add(new Edge(25,31)); // |
 		edges.add(new Edge(26,32)); // |
 
 		edges.add(new Edge(27,33)); // \
 		edges.add(new Edge(28,33)); // /
 		edges.add(new Edge(28,34)); // \
 		edges.add(new Edge(29,34)); // /
 		edges.add(new Edge(29,35)); // \
 		edges.add(new Edge(30,35)); // /
 		edges.add(new Edge(30,36)); // \
 		edges.add(new Edge(31,36)); // /
 		edges.add(new Edge(31,37)); // \
 		edges.add(new Edge(32,37)); // /
 
 		edges.add(new Edge(33,38)); // |
 		edges.add(new Edge(34,39)); // |
 		edges.add(new Edge(35,40)); // |
 		edges.add(new Edge(36,41)); // |
 		edges.add(new Edge(37,42)); // |
 
 		edges.add(new Edge(38,43)); // \
 		edges.add(new Edge(39,43)); // /
 		edges.add(new Edge(39,44)); // \
 		edges.add(new Edge(40,44)); // /
 		edges.add(new Edge(40,45)); // \
 		edges.add(new Edge(41,45)); // /
 		edges.add(new Edge(41,46)); // \
 		edges.add(new Edge(42,46)); // /
 
 		edges.add(new Edge(43,47)); // |
 		edges.add(new Edge(44,48)); // |
 		edges.add(new Edge(45,49)); // |
 		edges.add(new Edge(46,50)); // |
 
 		edges.add(new Edge(47,51)); // \
 		edges.add(new Edge(48,51)); // /
 		edges.add(new Edge(48,52)); // \
 		edges.add(new Edge(49,52)); // /
 		edges.add(new Edge(49,53)); // \
 		edges.add(new Edge(50,53)); // /
 
 		ActivePlayer = null;
 		GameBoard = null;
 		OtherPlayers = null;
 		ChatPanel = null;
 		StateOfGamePanel = null;
 		
 		loseR.add("gg");
 		loseR.add("well played");
 		winR.add("gg no re");
 		aiResponses.put("lose", loseR);//when to check for this?
 		aiResponses.put("win", winR);//continue for other responses
 		//appropriateResponse, so gamelogic.aiResponses("win")
 		
 		
 			
 	}
 
 	public String getActivePlayer() {
 		return activeP.name;
 	}
	public ArrayList<Vertex> usableCityLocations(Player p)
 	{
		return null;
		//yep.
 		
 	}
	public ArrayList<Integer> useableRoadLocations(Player P)
 	{
		ArrayList<Integer> returnArray=new ArrayList<Integer>();
		for(GamePiece p:verts)
 		{
			if((!(p==null))&&(p.type==1||p.type==2)&&playerList.get(p.player).equals(P))
 			{
				//if(gl.resDistr.get(gl.SClocs.get(p.location).resIndex1).type==0)Lr+=getProb(gl.hexRolls.get(gl.SClocs.get(p.location).resIndex1))*p.type;
				//at this point the game piece is a city. therefore any adjacent edge is
				//a viable road location.
				for(Edge e:edges)
				{
					if((e.vertIndex1==p.location||e.vertIndex2==p.location)&&!(road.get(edges.indexOf(e))==null))
					{
						returnArray.add(edges.indexOf(e));
					}
					
				}
 				
 				
 				
 				
 			}
 		}
		return returnArray;
		
 		
 	}
 	public void appropriateResponse(String key,String sendingAI)
 	{
 		//random chance to do or not do goes here
 		//zipp wants to EAT THE CODE
 		if(true)
 		{
 			//pick a random response based on key here
 			String response=aiResponses.get(key).get(0);//change the get(0) to random number from 0 to arraylistlength-1
 			newChatMessage(sendingAI+": "+response);
 			//crap, needs to write to chat
 		}
 		
 	}
 
 	public void newChatMessage(String string) {
 		ChatPanel.addMessage(string);
 	}
 
 	public void setPanels(GameBoardPanel g,
 			GameStatusPanel s, ActivePlayerPanel a,
 			OtherPlayerPanel o, ChatPanel c, MainGamePanel m) {
 		this.ActivePlayer = a;
 		this.GameBoard = g;
 		this.OtherPlayers = o;
 		this.ChatPanel = c;
 		this.StateOfGamePanel = s;
 		this.MainGamePanel = m;
 		
 		
 	}
 
 	public void startTrade(int activePlayerNum) {
 		MainGamePanel.setVisible(false);
 		tFrame = new JFrame("Trade Window");
 		TradeWindow tPanel = new TradeWindow(activePlayerNum, this);
 		tFrame.setBounds(100, 100, 500, 400);
 		tFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		tFrame.add(tPanel);
 		tFrame.setVisible(true);
 		
 	}
 	
 	public void startAiTrade(int aiPlayerNum, int[] offers){
 		MainGamePanel.setVisible(false);
 		tFrame = new JFrame("Trade Window");
 		TradeWindow tPanel = new TradeWindow(aiPlayerNum, this);
 		tPanel.aiOffer(aiPlayerNum, offers);
 		
 		tFrame.setBounds(100, 100, 500, 400);
 		tFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		tFrame.add(tPanel);
 		tFrame.setVisible(true);
 	}
 	
 	public void endTrade(){
 		tFrame.dispose();
 		tFrame = null;
 		MainGamePanel.setVisible(true);
 	}
 
 	/**
 	 * Method to send a trade request to an AI player
 	 */
 	public void sendTrade() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void endTurn() {
 		if(actPNum == 3) actPNum = 0;
 		else actPNum++;
 		updateEverything();				
 		
 	}
 
 	private void updateEverything() {
 		GameBoard.updateLogic(this);
 		ActivePlayer.update(this);
 		OtherPlayers.update(this);
 		ChatPanel.update(this);
 		StateOfGamePanel.update(this);
 	}
 	
 }
