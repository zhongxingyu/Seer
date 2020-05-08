 package DerpyAI;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Random;
 
 import sharedfiles.*;
 
 public class DerpyAI {
 	private Boolean myColor; //black is false, white is true. 
 	private ArrayList<Board> boardStore; //The current, and all previous boards
 	private ArrayList<DerpyPiece> takenPieces; //The pieces we took
 	public ArrayList<DerpyPiece> ourPieces; //Our Array of Pieces
 	public ArrayList<DerpyPiece> theirPieces; //Our Array of their Pieces
 	private DerpyBoard currentBoard; //currentBoard is the current chess board
 	public ArrayList<Point> ourPiecesPoints; //array of the locations of our pieces
 	public ArrayList<Point> theirPiecesPoints; //array of the locations of their pieces
 	private ArrayList<Move> allMoves;
 
 	//constructor
 	public DerpyAI(Boolean b, Board c){
 		myColor = b; 
 		boardStore = new ArrayList<Board>();
 		takenPieces = new ArrayList<DerpyPiece>();
 		ourPieces = new ArrayList<DerpyPiece>();
 		ourPieces = new ArrayList<DerpyPiece>();
 		currentBoard = (DerpyBoard)c; 
 		theirPiecesPoints = new ArrayList<Point>();
 		ourPiecesPoints = new ArrayList<Point>();
 		allMoves = new ArrayList<Move>();
 	}
 
 	///////////////////////////Board State Checks//////////////////////////////////////////
 
 	public void findTheirPieces(){ // Creates an array of their pieces
 	DerpyPiece[][] boardState = currentBoard.getBoardArray(); 
 	for(int i=0;i<8;i++){
 			for(int a=0;a<8;a++){ 
 				if (!(this.isPieceOurs(boardState[i][a]))) theirPieces.add(boardState[i][a]); 
 			}
 		}
 	}
 	
 	public void findTheirPiecesPoints(){ // Creates an array of their pieces' locations
 		DerpyPiece[][] boardState = currentBoard.getBoardArray(); 
 		for(int i=0;i<8;i++){
 				for(int a=0;a<8;a++){
 					Point currentPoint=new Point (i,a);
 					if (!(this.isPieceOurs(boardState[i][a]))) theirPiecesPoints.add(currentPoint); 
 				}
 			}
 		}
 	
 	public void findOurPieces(){ // Creates an array of our pieces
 		DerpyPiece[][] boardState = currentBoard.getBoardArray(); 
 		for(int i=0; i < 8; i++){
 			for(int a=0; a < 8; a++){ 
 				if (this.isPieceOurs(boardState[i][a])) ourPieces.add(boardState[i][a]); 
 			}
 		}
 	}
 	
 	public void findOurPiecesPoints(){ // Creates an array of our pieces' locations
 		DerpyPiece[][] boardState = currentBoard.getBoardArray(); 
 		for(int i=0; i < 8; i++){
 			for(int a=0; a < 8; a++){ 
 				Point currentPoint=new Point(i,a);
 				if (this.isPieceOurs(boardState[i][a])) ourPiecesPoints.add(currentPoint); 
 			}
 		}
 	}
 
 	//checks if a piece is ours
 	public boolean isPieceOurs(DerpyPiece p) {
 		if (this.myColor == p.getColor() && !(p instanceof DerpyBlank)){
 			return true;
 		}
 		else return false;
 	}
 	
 	//returns an arraylist of our pieces that are threatened by an enemy piece
 	public ArrayList<DerpyPiece> enemyThreats(DerpyBoard b){
 		ArrayList<DerpyPiece> ourThreatenedPieces = new ArrayList<DerpyPiece>();
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
 	public ArrayList<DerpyPiece> ourThreats(DerpyBoard b){
 		ArrayList<DerpyPiece> theirThreatenedPieces = new ArrayList<DerpyPiece>();
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
 	//checks if the defender is more valuable than attacker, and returns true if the defender
 	// is worth more. If this method returns true, we should make the move. 
 	public boolean makeTrade(DerpyPiece attacker, DerpyPiece defender){
 		if(defender instanceof DerpyKing){
 			return true;
 		}
 		if(defender instanceof DerpyQueen){
 			if (attacker instanceof DerpyKing){
 				return false;
 				}
 			else{ 
 				return true;
 			}
 		}
 		if(defender instanceof DerpyRook){
 			if(attacker instanceof DerpyQueen || attacker instanceof DerpyKing){
 				return false;
 			}
 			else{
 				return true;
 			}
 			
 		}
 		if(defender instanceof DerpyBishop || defender instanceof DerpyKnight){
 			if(attacker instanceof DerpyRook || attacker instanceof DerpyQueen || attacker instanceof DerpyKing){
 				return false;
 			}
 			else{
 				return true;
 			}
 		}
 		
 		if(defender instanceof DerpyPawn)
 			if(attacker instanceof DerpyRook || attacker instanceof DerpyQueen || attacker instanceof DerpyKing || attacker instanceof DerpyBishop || attacker instanceof DerpyKnight){
 				return false; 
 				}
 		else{
 			return true;
 		}
 		return false; 
 	}
 	
 	public boolean pieceIsProtected(DerpyPiece p){
 		DerpyPiece d = (DerpyPiece)p;
		for(Piece a:ourPieces){
 			if(this.pieceCanMoveToPosition(a, d.getLocation())){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	//asks if a piece is threatened
 	public boolean pieceIsThreatened(DerpyPiece p) {
 		DerpyPiece d = (DerpyPiece)p;
 		for(DerpyPiece a : theirPieces) {
 			if(this.pieceCanMoveToPosition(a, d.getLocation())){
 				if(this.makeTrade(a,p)){
 					return true;
 				}
 				else if(this.pieceIsProtected(p)){
 					return false;
 				}
 				else{
 					return true;
 				}
 			}
 		}
 		return false; 
 	}
 
 	//Returns if the king is in check
 	public boolean inCheck() {
 		for(int i=0;i<=ourPieces.size();i++){
 			if(ourPieces.get(i) instanceof DerpyKing){
 				if(this.pieceIsThreatened(ourPieces.get(i))){
 					return true;
 				}
 			}
 		}
 		return false;
 	} 
 	
 	//returns an arraylist of points a piece can move to
 	public ArrayList<Point> movablePoints(DerpyPiece p){
 		ArrayList<Point> listOfPoints=new ArrayList();
 		for(int i=0;i<8;i++){
 			for(int j=0;j<8;j++){
 				Point moveTo=new Point(i,j);
 				if(this.pieceCanMoveToPosition(p, moveTo)){
 					listOfPoints.add(moveTo);
 				}
 			}
 		}
 		return listOfPoints;
 	}
 	//returns an arraylist of pieces threatening this piece p if it is theirs
 		public ArrayList<DerpyPiece> threateningPiecesToThem(DerpyPiece p){
 			ArrayList<DerpyPiece> threats = new ArrayList<DerpyPiece>();
 			for(DerpyPiece a: ourPieces ){
 				if(this.pieceCanMoveToPosition(a, p.getLocation())){
 					threats.add(a);
 				}
 			}
 			return threats;
 		}
 	
 	//returns an arraylist of pieces threatening this piece p if it is ours
 	public ArrayList<DerpyPiece> threateningPiecesToUs(DerpyPiece p){
 		ArrayList<DerpyPiece> threats = new ArrayList<DerpyPiece>();
 		for(DerpyPiece a: theirPieces ){
 			if(this.pieceCanMoveToPosition(a, p.getLocation())){
 				threats.add(a);
 			}
 		}
 		return threats;
 	}
 	
 	//returns an arraylist of points that can be occupied to block theirs from capturing ours
 		public ArrayList<Point> findBlockablePoints(DerpyPiece ours, DerpyPiece theirs){
 			ArrayList<Point> points=new ArrayList<Point>();
 			if((theirs instanceof DerpyKnight || theirs instanceof DerpyPawn) || theirs instanceof DerpyKing){
 				return points;
 			}
 			if(theirs.getLocation().distance(ours.getLocation())<1.5){
 				return points;
 			}
 			if(theirs instanceof DerpyRook || theirs instanceof DerpyQueen){
 				if(theirs.getLocation().getX()==ours.getLocation().getX()){
 					if(theirs.getLocation().getY()>ours.getLocation().getY()){
 						for(double i=theirs.getLocation().getY(); i>=ours.getLocation().getY(); i--){
 							Point ourPoint=new Point((int)i,((int)theirs.getLocation().getY()));
 							points.add(ourPoint);
 						}
 					}
 					if(theirs.getLocation().getY()<ours.getLocation().getY()){
 						for(double i=theirs.getLocation().getY(); i<=ours.getLocation().getY(); i++){
 							Point ourPoint=new Point((int)i,((int)theirs.getLocation().getY()));
 							points.add(ourPoint);
 						}
 					}
 				}
 				if(theirs.getLocation().getY()==ours.getLocation().getY()){
 					if(theirs.getLocation().getX()>ours.getLocation().getX()){
 						for(double i=theirs.getLocation().getX(); i>=ours.getLocation().getX();i--){
 							Point ourPoint=new Point((int)i, ((int)theirs.getLocation().getY()));
 							points.add(ourPoint);
 						}
 					}
 					if(theirs.getLocation().getX()<ours.getLocation().getX()){
 						for(double i=theirs.getLocation().getX(); i<=ours.getLocation().getX();i++){
 							Point ourPoint=new Point((int)i, ((int)theirs.getLocation().getY()));
 							points.add(ourPoint);
 						}
 					}
 				}
 				
 			}
 			
 			if(theirs instanceof DerpyBishop || theirs instanceof DerpyQueen){
 				if(theirs.getLocation().getX()>ours.getLocation().getX()){
 					if(theirs.getLocation().getY()<ours.getLocation().getX()){
 						for(double i=theirs.getLocation().getX();i>=ours.getLocation().getX(); i--){
 							for(double j=theirs.getLocation().getY(); j<=ours.getLocation().getY();j++){
 								Point ourPoint=new Point((int)i, (int)j);
 								points.add(ourPoint);
 							}
 						}
 					}
 					if(theirs.getLocation().getY()>ours.getLocation().getY()){
 						for(double i=theirs.getLocation().getX(); i>=ours.getLocation().getX(); i--){
 							for(double j=theirs.getLocation().getY(); j>=ours.getLocation().getY();j--){
 								Point ourPoint=new Point ((int)i, (int)j);
 								points.add(ourPoint);
 								
 							}
 						}
 					}
 				}
 				if(theirs.getLocation().getX()<ours.getLocation().getX()){
 					if(theirs.getLocation().getY()<ours.getLocation().getX()){
 						for(double i=theirs.getLocation().getX();i<=ours.getLocation().getX();i++){
 							for(double j=theirs.getLocation().getY();j<=ours.getLocation().getY();j++){
 								Point ourPoint=new Point((int)i, (int)j);
 								points.add(ourPoint);
 							}
 						}
 					}
 					if(theirs.getLocation().getY()>ours.getLocation().getX()){
 						for(double i=theirs.getLocation().getX();i<=ours.getLocation().getX();i++){
 							for(double j=theirs.getLocation().getY();j>=ours.getLocation().getY();j--){
 								Point ourPoint=new Point((int)i, (int)j);
 								points.add(ourPoint);
 							}
 						}
 					}
 				}
 			}
 			return points;
 		}
 	
 	//makes a move to get out of check
 	public DerpyBoard getOutOfCheck(Board b){
 		//tries to move the king out of check
 		for(int i=0;i<ourPieces.size();i++){
 			if(ourPieces.get(i) instanceof DerpyKing){
 				ArrayList<Point> listOfPoints=this.movablePoints(ourPieces.get(i));
 				for(int j=0;j<listOfPoints.size();j++){
 					if(this.pieceCanMoveToPosition(ourPieces.get(i), listOfPoints.get(j))){
 						return this.movePiece(ourPieces.get(i), listOfPoints.get(j));
 					}
 				}
 			}
 		}
 		//tries to take the threatening piece
 		for(int i=0;i<ourPieces.size();i++){
 			if(ourPieces.get(i) instanceof DerpyKing){
 				DerpyPiece ourKing=ourPieces.get(i);
 				if(this.threateningPiecesToUs(ourKing).size()==1){
 					DerpyPiece threat=this.threateningPiecesToUs(ourKing).get(0);
 					if(this.threateningPiecesToThem(threat).size()>=1){
 						DerpyPiece taker=this.threateningPiecesToThem(threat).get(0);
 						return this.movePiece(taker, threat.getLocation());
 					}
 				}
 				
 			}
 		}
 		for(int i=0; i<ourPieces.size();i++){
 			if(ourPieces.get(i) instanceof DerpyKing){
 				DerpyPiece ourKing=ourPieces.get(i);
 				ArrayList<DerpyPiece> threats=threateningPiecesToUs(ourKing);
 				if(threats.size()==0){
 					ArrayList<Point> betweenSpaces=this.findBlockablePoints(ourKing, threats.get(0));
 					for(Point p:betweenSpaces){
 						for(DerpyPiece c:ourPieces){
 							if(this.pieceCanMoveToPosition(c, p)){
 								return this.movePiece(c,p);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		this.concedeGame();
 		return currentBoard;
 	}
 	
 	public boolean pieceCanMoveToPosition(DerpyPiece piece, Point position) {
 		
 		int xPos = (int)position.getX();
 		int yPos = (int)position.getY();
 		
 		if(piece instanceof DerpyKing){
 			if(piece.getLocation().distanceSq(position)==1 || piece.getLocation().distanceSq(position)==2){
 				DerpyBoard testBoard=this.movePiece(piece, position);
 				if(!(this.inCheck())){
 				return true;
 			}
 		}
 		//We need to get the Piece object at that position
 		
 		//Iterate through each Piece to figure out whether there's a piece at that position, or is it blank?
 		
 		DerpyPiece targetPiece = null;
 		for(Piece p : ourPieces) {
 			DerpyPiece d = (DerpyPiece)p;
 			Point piecePosition = d.getLocation();
 		}
 		
 		
 		
 		return false;
 	}
 	
 	//uses provided board to make a move, returns a board with the move made
 	
 	public DerpyBoard movePiece(DerpyPiece p, Point mL){
 		DerpyBoard theBoard = currentBoard; 
 		//Point oL = p.getLocation(); //This will access the instance data in the piece class that contain its location. 
 		//p.changeLocation(mL); //This will change the instance data above to the new location and erase the piece from its prior location. 
 		//theBoard = theBoard.updateLocations(); //This will have the board update its array locations; could potentially just be a function of changeLocation() but for now I have it as a separate method. 
 		
 		return theBoard; 
 	}
 	
 	public DerpyBoard makeMove(Board b){
 		
 		boardStore.add(b);
 		
 		DerpyBoard boardWithPieceMoved = null;
 		
 		if (this.inCheck()){
 			//We're in check, call getOutOfCheck to get us a board where we're not in check
 			boardWithPieceMoved = this.getOutOfCheck(b);
 		}
 		else {
 			DerpyPiece randomPiece = null;
 			Point randomLocation = null;
 			for(;;) {
 				//ourPieces is the array containing the array of all of our pieces
 				Random r = new Random();
 				//We're just going to temporarily randomly select a piece and move it forward, if we can
 				int randomIndex = r.nextInt(ourPieces.size()-1)+1;
 				randomPiece = ourPieces.get(randomIndex);
 				
 				randomLocation = new Point(r.nextInt(7)+1,r.nextInt(7)+1);
 				if(this.pieceCanMoveToPosition(randomPiece, randomLocation)) {
 					break;
 				}
 				
 			}
 			randomPiece.changeLocation(randomLocation);
 			boardWithPieceMoved = new DerpyBoard((DerpyBoard)b); //Copy the b board
 			
 			//boardWithPieceMoved.arr[1][1] = "WX";
 			
 
 		}
 		boardStore.add(boardWithPieceMoved);
 		currentBoard = boardWithPieceMoved;
 		if(this.inCheck())concedeGame(); //If we're still in check even after all that, there's no way out of check. Concede to the other player.
 		
 		return boardWithPieceMoved;
 	}
 	
 	public void concedeGame() {
 		System.out.println("DerpyAI has lost the game.");
 		System.exit(0); //Exit with terminated status 0
 	}
 
 }
 
 
