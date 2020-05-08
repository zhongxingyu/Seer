 import java.util.ArrayList;
 
 
 
 public class ComputerPlayer extends Player {
 
 	public ComputerPlayer(String n, Intersection.Piece p) {
 		super(n, p);
 		// TODO Auto-generated constructor stub
 	}
 
 	public void promptMove(GoEngine g) {
 		
 		int row = 0;
 		int col = 0;
 		boolean placed = false;
 		while(placed != true) {
 			int leftSpace = 0;
 			int rightSpace = 0;
 			int topSpace = 0;
 			int bottomSpace = 0;
 			for(int i = 0; i < 81; i++){
 				if(g.board.getContents(row, col) != Intersection.Piece.EMPTY 
 						&& g.board.getContents(row, col) != piece) {
 					for(int j = row + 1; j <= 8; j++){
 						if(g.board.getContents(j, col) == Intersection.Piece.EMPTY) {
 							bottomSpace++;
 						} else {
 							break;
 						}
 					}
					for(int j = row - 1; j >= 0; j++){
 						if(g.board.getContents(j, col) == Intersection.Piece.EMPTY) {
 							topSpace++;
 						} else {
 							break;
 						}
 					}
 					for(int j = col + 1; j <= 8; j++){
 						if(g.board.getContents(row, j) == Intersection.Piece.EMPTY) {
 							rightSpace++;
 						} else {
 							break;
 						}
 					}
					for(int j = col - 1; j >= 0; j++){
 						if(g.board.getContents(j, col) == Intersection.Piece.EMPTY) {
 							leftSpace++;
 						} else {
 							break;
 						}
 					}
 					if(leftSpace >= rightSpace && leftSpace >= topSpace && leftSpace >= bottomSpace){
 						placed = g.board.placePiece(row, col - 1, piece);
 					} else if(rightSpace >= leftSpace && rightSpace >= topSpace && rightSpace >= bottomSpace){
 						placed = g.board.placePiece(row, col + 1, piece);
 					} else if(topSpace >= rightSpace && topSpace >= leftSpace && topSpace >= bottomSpace){
 						placed = g.board.placePiece(row - 1, col, piece);
 					} else if(bottomSpace >= rightSpace && bottomSpace >= topSpace && bottomSpace >= leftSpace){
 						placed = g.board.placePiece(row + 1, col, piece);
 					}
 				}
 				if(placed || (col == 8 && row == 8)){
 					break;
 				}
 				if(col >= 8){
 					col = 0;
 					row++;
 				} else {
 					col++;
 				}
 			}
 		}
 	}
 }
