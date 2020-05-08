 package DerpyAI;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import sharedfiles.*;
 
 public class DerpyAI {
 	private Boolean myColor; //black is false, white is true. 
 	private ArrayList<Board> boardStore; //The current, and all previous boards
 	private ArrayList<Piece> takenPieces; //The pieces we took
 	public ArrayList<Piece> ourPieces; //Our Array of Pieces
 	public ArrayList<Piece> theirPieces; //Our Array of Pieces
 	private Board currentBoard; //currentBoard is the current chess board
 	public ArrayList<Point> ourPiecesPoints; //array of the locations of our pieces
 	public ArrayList<Point> theirPiecesPoints; //array of the locations of their pieces
 	private ArrayList<Move> allMoves;
 
 	//constructor
 	public DerpyAI(Boolean b, Board c){
 		myColor = b; 
 		boardStore = new ArrayList<Board>();
 		takenPieces = new ArrayList<Piece>();
 		ourPieces = new ArrayList<Piece>();
 		ourPieces = new ArrayList<Piece>();
 		currentBoard = c; 
 		theirPiecesPoints = new ArrayList<Point>();
		ourPiecesPoints = new ArrayList<Point>();
 		allMoves = new ArrayList<Move>();
 	}
 
 	///////////////////////////Board State Checks//////////////////////////////////////////
 
 	public void findTheirPieces(){ // Creates an array of their pieces
 	Piece[][] boardState = currentBoard.getBoardArray(); 
 	for(int i=0;i<8;i++){
 			for(int a=0;a<8;a++){ 
 				if (!(this.isPieceOurs(boardState[i][a]))) theirPieces.add(boardState[i][a]); 
 			}
 		}
 	}
 	
 	public void findTheirPiecesPoints(){ // Creates an array of their pieces' locations
 		Piece[][] boardState = currentBoard.getBoardArray(); 
 		for(int i=0;i<8;i++){
 				for(int a=0;a<8;a++){
 					Point currentPoint=new Point (i,a);
 					if (!(this.isPieceOurs(boardState[i][a]))) theirPiecesPoints.add(currentPoint); 
 				}
 			}
 		}
 	
 	public void findOurPieces(){ // Creates an array of our pieces
 		Piece[][] boardState = currentBoard.getBoardArray(); 
 		for(int i=0; i < 8; i++){
 			for(int a=0; a < 8; a++){ 
 				if (this.isPieceOurs(boardState[i][a])) ourPieces.add(boardState[i][a]); 
 			}
 		}
 	}
 	
 	public void findOurPiecesPoints(){ // Creates an array of our pieces' locations
 		Piece[][] boardState = currentBoard.getBoardArray(); 
 		for(int i=0; i < 8; i++){
 			for(int a=0; a < 8; a++){ 
 				Point currentPoint=new Point(i,a);
 				if (this.isPieceOurs(boardState[i][a])) ourPiecesPoints.add(currentPoint); 
 			}
 		}
 	}
 
 	//checks if a piece is ours
 	public boolean isPieceOurs(Piece p) {
 		if (this.myColor == p.getColor() && !(p instanceof Blank)){
 			return true;
 		}
 		else return false;
 	}
 	
 	//returns an arraylist of our pieces that are threatened by enemy an enemy piece
 	public ArrayList<Piece> enemyThreats(Board b){
 		ArrayList<Piece> ourThreatenedPieces = new ArrayList<Piece>();
 		for(int i = 0; i < 8; i++){
 			for(int j = 0; j <= 8; j++){
 				if(this.isPieceOurs(b.getBoardArray()[i][j])){
 					if(this.pieceIsThreatened(b.getBoardArray()[i][j])){
 						ourThreatenedPieces.add(b.getBoardArray()[i][j]);
 					}
 				}
 			}
 		}
 						
 		return ourThreatenedPieces;
 
 	}
 
 	//returns an arraylist of enemy pieces that we threaten
 	public ArrayList<Piece> ourThreats(Board b){
 		ArrayList<Piece> theirThreatenedPieces = new ArrayList<Piece>();
 		for(int i = 0; i < 8; i++){
 			for(int j = 0; j <= 8; j++){
 				if(!(this.isPieceOurs(b.getBoardArray()[i][j]))){
 					if(this.pieceIsThreatened(b.getBoardArray()[i][j])){
 						theirThreatenedPieces.add(b.getBoardArray()[i][j]);
 					}
 				}
 			}
 		}
 
 		return theirThreatenedPieces;
 	}
 
 	//asks if a piece is threatened
 	public boolean pieceIsThreatened(Piece p) {
 		boolean b = false; 
 		return b; 
 	}
 
 	//Returns if the king is in check
 	public boolean inCheck() {
 		
 		
 		boolean b = false;
 		
 		return b;
 	} 
 	
 	//makes a move to get out of check
 	public Board getOutOfCheck(Board b){
 		return b;
 	}
 	
 	public boolean pieceCanMoveToPosition(Piece piece, Point position) {
 		
 		int xPos = (int)position.getX();
 		int yPos = (int)position.getY();
 		
 		//We need to get the Piece object at that position
 		
 		
 		
		
 		return false;
 	}
 	
 	//uses provided board to make a move, returns a board with the move made
 	public Board makeMove(Board b){
 		
 		boardStore.add(b);
 		
 		Board boardWithPieceMoved = null;
 		
 		if (this.inCheck()){
 			//We're in check, call getOutOfCheck to get us a board where we're not in check
 			boardWithPieceMoved = this.getOutOfCheck(b);
 		}
 		else {
 
 
 		}
 		boardStore.add(boardWithPieceMoved);
 		currentBoard = boardWithPieceMoved;
 		if(this.inCheck())concedeGame(); //If we're still in check even after all that, there's no way out of check. Concede to the other player.
 		
 		return boardWithPieceMoved;
 	}
 	
 	public void concedeGame() {
 		System.out.println("DerpyAI has lost the game.");
 		System.exit(0); //Exit with Terminated status 0
 	}
 
 }
 
 
