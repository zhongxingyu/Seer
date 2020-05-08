 package org.tjug.chess.game;
 //
 //  This class represents a chess board
 //
 import java.util.ArrayList;
 import java.util.List;
 
 public class Board {
 	private IChessPiece.Color lastPlayer = IChessPiece.Color.WHITE;
 	private List<IChessPiece> removedPieces = new ArrayList<IChessPiece>();
 	private IChessPiece[][] board = new IChessPiece[8][8];
 
 	void destroy(){
 		lastPlayer = null;
 		removedPieces.clear();
 		removedPieces = null;
 		
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++)
 				board[i][j]=null;
 			
 		board = null;
 	}
 
 	public void setPiece(Coordinate coord, ChessPiece chessPiece){
 		board[coord.getX()][coord.getY()] = chessPiece;
 	}
 	
 	IChessPiece.Color getLastPlayer(){
 		return lastPlayer;
 	}
 	
     IChessPiece.Color getActivePlayer(){
 		if (IChessPiece.Color.BLACK == lastPlayer)
 			return IChessPiece.Color.WHITE;
 		else
 			return IChessPiece.Color.BLACK;
 	}
 	public void clearBoard(){
 		for(int y=0; y<8; y++){
 			for(int x=0; x<8; x++){
 				board[x][y] = null;
 			}
 		}
 	}
 	
 	public void initialize(){
 		clearBoard();
 		setup(IChessPiece.Color.WHITE);
 		setup(IChessPiece.Color.BLACK);
 		removedPieces.clear();
 		lastPlayer = null;
 		
 		log(this.toString());
 	}
 	
 	void move(String strMove){
 		try{
 			Move move = new Move(strMove);
 			IChessPiece piece = getPiece(move.getOrigin());
 			log("Piece : " + piece);
 			IChessPiece removedPiece = null;
 			
 			// test driven development
 			//    players must alternate turns
 			if(piece.getColor()==lastPlayer){
 				throw new RuntimeException("Player out of order.");
 			}
 			piece.move(move, this);
 			
 			// sometimes pawns change ...
 			//   sneaky little things ...
 			if (piece instanceof Pawn)
 				piece = getPiece(move.getOrigin());
 			
 			removedPiece = board[move.getDestination().getX()][move.getDestination().getY()];
 			
 			board[move.getDestination().getX()][move.getDestination().getY()] = piece;
 			board[move.getOrigin().getX()][move.getOrigin().getY()] = null;
 			lastPlayer = piece.getColor();
 
 			log("Valid move - " + move);
 			if (null!=removedPiece){
 				log("Chess piece removed: " + removedPiece.toString());
 				removedPieces.add(removedPiece);
 			}
 			log(this.toString());
 			
 		}catch(RuntimeException r){
 			err(r.getMessage());
 		}
 	}
 
 	public IChessPiece getPiece(Coordinate coordinate){
 		return board[coordinate.getX()][coordinate.getY()];
 	}
 	
     boolean clearPath(Coordinate origin, Coordinate destination){
 		//int x1, x2, y1, y2;
 		//
 		// paths can only be vertical | horizontal | positive diagonal | negative diagonal.
 		
 		// vertical move
 		if (origin.getX() == destination.getX()){
 			int direction = (0>(origin.getY() - destination.getY())) ? 1 : -1;
 			
 			for (int y = origin.getY()+direction; y!=destination.getY(); y+=direction){
 				if (board[origin.getX()][y] != null)
 					return false;
 			}
 			
 			return true;
 		}
 			
 		// Horizontal move
 		if (origin.getY() == destination.getY()){
 			int direction = (0>(origin.getX() - destination.getX())) ? 1 : -1;
 			
 			for (int x = origin.getX()+direction; x!=destination.getX(); x+=direction){
 				if (board[x][origin.getY()] != null)
 					return false;
 			}
 			
 			return true;
 		}
 		
 		
 	    // diagonal 
 		long absX = Math.abs(origin.getX() - destination.getX());
 		long absY = Math.abs(origin.getY() - destination.getY());
 		
 		// must be the correct slope 45 degrees ...
 		if (absX!=absY){
 			return false;
 		}
 		
 		int directionX = (0>(origin.getX()-destination.getX())) ? 1 : -1;
 		int directionY = (0>(origin.getY()-destination.getY())) ? 1 : -1;
 		
 		for (int x=origin.getX()+directionX, y=origin.getY()+directionY; x!=destination.getX(); x+=directionX, y+=directionY ){
 			if (board[x][y] != null)
 				return false;
 		}
 	    
 	    return true;
 	}
 	
 	private void setup(IChessPiece.Color color)
 	{
 		int file = color == IChessPiece.Color.BLACK ? 0 : 7;
 		
 		board[file][0] = new Rook(color);
 		board[file][1] = new Knight(color); 
 		board[file][2] = new Bishop(color); 
		board[file][3] = new King(color); 
		board[file][4] = new Queen(color); 
 		board[file][5] = new Bishop(color); 
 		board[file][6] = new Knight(color); 
 		board[file][7] = new Rook(color);
 		file = (file==7) ? 6 : 1;
 		board[file][0] = new Pawn(color); 
 		board[file][1] = new Pawn(color); 
 		board[file][2] = new Pawn(color); 
 		board[file][3] = new Pawn(color); 
 		board[file][4] = new Pawn(color); 
 		board[file][5] = new Pawn(color); 
 		board[file][6] = new Pawn(color); 
 		board[file][7] = new Pawn(color); 
 	}
 	
 	@Override
 	public String toString(){
 		StringBuffer sb1 = new StringBuffer();
 		StringBuffer sb2;
 		String headerLine = "   -----A-------------B-------------C-------------D-------------E-------------F-------------G-------------H-----\n";
 		String line = "  |-------------------------------------------------------------------------------------------------------------|\n";
 		sb1.append(headerLine);
 		for(int i=0; i<8; i++){
 			sb2 = new StringBuffer();
 			for(int j=0; j<8; j++){
 				String piece;
 				piece = (null==board[i][j])? "" : board[i][j].toString();
 				piece = " | " + piece + "            ".substring(0, 10-piece.length());
 				piece = (0==j ? Integer.toString(8-i) : " ")+ piece;
 				sb2.append(piece);
 			}
 			sb2.append("|\n");
 			sb1.append(sb2.toString());
 			if (7>i) sb1.append(line);
 		}
 		sb1.append(headerLine);
 		
 		return sb1.toString(); 
 	}
 	
 	public String getUpdatedBoard(){
 		StringBuffer sb = new StringBuffer();
 		
 		for (int i=0; i<8; i++){
 			for (int j=0; j<8; j++){
 				sb.append( (null==board[i][j])?"":(board[i][j]).toString());
 				if(j<7) sb.append("|");
 			}
 			if(i<7) sb.append("*");
 		}
 		
 		return sb.toString(); 
 	}
 
 	void log(String msg){
 		System.out.println(msg);
 	}
 	
 	void err(String msg){
 		System.err.println(msg);
 	}
 }
