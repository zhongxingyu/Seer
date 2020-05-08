 import java.io.*;
 import java.net.*;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 public class Client extends Thread{
 
 	public boolean recv = true;
 	protected int portNum;
 	protected String hostName;
 	protected int gameType;
 	protected int timerLen;
 	protected int locate;
 	protected int numRows;
 	protected int numCols;
 	protected String color;
 	Socket clientSocket;
 	Fanorona newGame;
 		
     /**
 	@param location The game the client will command.
 	@param host The Hostname of the server.
 	@param port The Port the server is using.*/
 	public Client(int location, String host, int port) {
 //		hostName = host;
 //		portNum = port;
 		locate = location;
 		hostName = "127.0.0.1";
 		portNum = 11192;
 		try {
 			final Socket clientSocket = new Socket(InetAddress.getByName(hostName), portNum);
 		    /* assert:  Socket successfully created */
 			Thread tClient = new Thread(new Worker(clientSocket, "client", this));
 			tClient.start();
 		}
 		
 		catch (UnknownHostException e) {
 			System.err.println("Unable to locate host: "+hostName);
 		}
 		catch (IOException e) {
 		    System.err.println("Coundn't get I/O for the connection.");
 		    System.err.println(e.getMessage());
 		    System.exit(1);  // an error exit status
 		}
 	}
 
 	public void startGame() {
 		Fanorona newGame = new Fanorona(locate, gameType, numRows, numCols, timerLen);
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
 	
 	public void sendMove(Pair p, Pair q, String type){
 		p = transf2Cartesian(p);
 		q = transf2Cartesian(q);
 		if (type.equals("A")) {
 			try {
 				OutputStream outStream = clientSocket.getOutputStream();
 				Command move = new Command("capture_move", "A"+" "+p.getFirst()+" "+p.getSecond()+" "+q.getFirst()+" "+q.getSecond()+" ");
 				move.send(outStream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (type.equals("W")) {
 			try {
 				OutputStream outStream = clientSocket.getOutputStream();
 				Command move = new Command("capture_move", "W"+" "+p.getFirst()+" "+p.getSecond()+" "+q.getFirst()+" "+q.getSecond()+" ");
 				move.send(outStream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (type.equals("P")) {
 			try {
 				OutputStream outStream = clientSocket.getOutputStream();
 				Command move = new Command("paika_move", "P"+" "+p.getFirst()+" "+p.getSecond()+" "+q.getFirst()+" "+q.getSecond()+" ");
 				move.send(outStream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (type.equals("S")) {
 			try {
 				OutputStream outStream = clientSocket.getOutputStream();
 				Command move = new Command("sacrifice_move", "S"+" "+p.getFirst()+" "+p.getSecond()+" "+q.getFirst()+" "+q.getSecond()+" ");
 				move.send(outStream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void configGame(String content) {
 	    StringTokenizer sToken = new StringTokenizer(content, " ");
 	    numCols  = Integer.parseInt(sToken.nextToken());
 	    numRows = Integer.parseInt(sToken.nextToken());
 	    color  = sToken.nextToken();
 	    timerLen  = Integer.parseInt(sToken.nextToken());
 		
 	}
 
 	public void setEndCond(int end) {
 		if (end == 0) {
 			//WINNER;
 		}
 		if (end == 1) {
 			//TIE
 		}
 		if (end == 2) {
 			//LOSER
 		}
 		if (end == 3) {
 			//ILLLEGAL
 		}
 		if (end == 4) {
 			//TIME
 		}
 		
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
