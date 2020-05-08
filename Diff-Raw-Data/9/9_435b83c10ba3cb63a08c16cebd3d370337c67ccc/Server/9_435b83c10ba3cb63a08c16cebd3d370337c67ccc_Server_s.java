 import java.awt.EventQueue;
 import java.io.*;
 import java.net.*;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 public class Server extends Thread{
 	
 	public boolean recv = true;
 	protected int portNum;
 	protected int locate;
 	protected int timerLen;
 	protected int gameType;
 	protected int numRows;
 	protected int numCols;
 	protected String color;
 	ServerSocket servSock;
 	Socket input;
 	Fanorona newGame;
 	
     /**
 	@param f The game the client will command.
 	@param port The port for the server to listen on.*/
 	public Server(int location, int type, int Rows, int Cols, int timerLength, int port) {
 //		portNum = port;
 		portNum = 11192;
 		locate = location;
 		numRows = Rows;
 		numCols = Cols;
 		gameType = type;
 		timerLen = timerLength;
 		color = "W";
 		try {
 			servSock = new ServerSocket(portNum);
 		    }
 		catch (IOException e) {
 			System.err.println("Socket Bind failed.: " + e.getMessage());
 			e.printStackTrace();
 		}   
 		
 		try {
 			Socket input = servSock.accept();
 			Thread t = new Thread(new Worker(input, "server", this));
 			t.start();				
 		}
 		catch (IOException e) {
 			System.err.println("Socket Accept failed.");
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			System.exit(1);  // an error exit status
 			return;
 		}
 		catch (Exception e) {
 			System.exit(1);
 			System.err.println("Accept failed.");
 			e.printStackTrace();
 		}
 	}
 	public void startGame() {
 		Fanorona newGame = new Fanorona(locate, gameType, numRows, numCols, timerLen);
 	}
 	
 	public String getConfig() {
 		String conf;
 		conf = numCols + " " + numRows + " " + color + " " + timerLen;
  		return conf;
 	}
 	
 	public String getGameConfig() {
 		String config = null;
 		config = getConfig();
 		return config;
 	}
 	
 	public void sendMove(Pair p, Pair q, String type){
 		p = transf2Cartesian(p);
 		q = transf2Cartesian(q);
 		if (type.equals("A")) {
 			try {
 				OutputStream outStream = input.getOutputStream();
 				Command move = new Command("capture_move", "A"+p.getFirst()+p.getSecond()+q.getFirst()+q.getSecond());
 				move.send(outStream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (type.equals("W")) {
 			try {
 				OutputStream outStream = input.getOutputStream();
 				Command move = new Command("capture_move", "W"+p.getFirst()+p.getSecond()+q.getFirst()+q.getSecond());
 				move.send(outStream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (type.equals("P")) {
 			try {
 				OutputStream outStream = input.getOutputStream();
 				Command move = new Command("paika_move", "P"+p.getFirst()+p.getSecond()+q.getFirst()+q.getSecond());
 				move.send(outStream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (type.equals("S")) {
 			try {
 				OutputStream outStream = input.getOutputStream();
 				Command move = new Command("sacrifice_move", "S"+p.getFirst()+p.getSecond()+q.getFirst()+q.getSecond());
 				move.send(outStream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public Pair transf2Cartesian(Pair p1) {
 		Pair fixed;
 		int x = p1.getFirst();
 		int y = p1.getSecond();
 		x++;
 		y = newGame.board.getRows()-(p1.getSecond())+1;
 		fixed = new Pair(x,y);
 		return fixed;
 	}
 	
 	public Pair transf2Matrix(Pair p1) {
 		Pair fixed;
 		int x = p1.getFirst();
 		int y = p1.getSecond();
 		x--;
 		y = newGame.board.getRows()-(p1.getSecond())-1;
 		fixed = new Pair(x,y);
 		return fixed;
 	}
 
 	public void movePiece(String content) {
 		Pair p1, p2;
 		String moveType;
 	    StringTokenizer moves = new StringTokenizer(content, "+");
 	    LinkedList<String> moveList = new LinkedList<String>();
 	    for (int i=0; i < moves.countTokens()-1; i++) {
 	    	moveList.add(moves.nextToken());
 	    }
 	    for (String move : moveList) {
 	    	StringTokenizer parts = new StringTokenizer(move, " ");
 	    	moveType = parts.nextToken();
 	    	p1 = new Pair(Integer.parseInt(parts.nextToken()),Integer.parseInt(parts.nextToken()));
 	    	p1 = transf2Matrix(p1);
 	    	p2 = new Pair(Integer.parseInt(parts.nextToken()),Integer.parseInt(parts.nextToken()));
 	    	p2 = transf2Matrix(p2);
 	    	newGame.board.move(p1, p2, moveType);
 			if ((moveType != "P") && (moveType != "S")) {
 				newGame.board.activateRemovables(p1, p2);
 			}
 			if (moveType == "A") {
 				newGame.board.removeRemovables(0); //0 for A; 1 for W
 			}
 			else if (moveType == "W") {
 				newGame.board.removeRemovables(1); //0 for A; 1 for W
 			}
 	    }
 	}
 	public void run() {
 		while (true) {
 			
 		}
 	}
 }
