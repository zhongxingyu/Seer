 package seg.project.checkers;
 
 import java.util.ArrayList;
 
 
 public class CheckerBoard  {
 	private CheckerSquare[][] grid; 
 	private ArrayList<CheckerSquare> redPieces;
 	private ArrayList<CheckerSquare> blackPieces;
 	public CheckerBoard(){
 		redPieces = new ArrayList<CheckerSquare>(12);
 		blackPieces = new ArrayList<CheckerSquare>(12);
 		grid = new CheckerSquare[8][8];
 		for(int i =0; i < 8;i++){
 			for(int u = 0;u<8;u++){
 				if(i%2==0){
 					if(u%2!=0)
 						if(i>=5){
 							grid[i][u]= new CheckerSquare(this,i,u,false);
 							redPieces.add(grid[i][u]);
 						}
 						else if(i <3){
 							grid[i][u] = new CheckerSquare(this,i,u,true);
 							blackPieces.add(grid[i][u]);
 							
 						}
 					}
 				
 				else{
 					if(u%2 == 0){
 						if(i>=5){
 							grid[i][u]= new CheckerSquare(this,i,u,false);
 							redPieces.add(grid[i][u]);
 						}else if(i <3){
 							grid[i][u] = new CheckerSquare(this,i,u,true);
 							blackPieces.add(grid[i][u]);
 						}
 					}
 				}
 			}
 		}
 	}
 		/*
 		for(int i =0; i < 8;i++){
 			for(int u = 0;u<8;u++){
 				CheckerSquare temp = new CheckerSquare(this,i,u, false, false);
 				temp.setIcon(new ImageIcon(temp.getImage()));
 				grid[i][u]= temp;
 				add(temp);
 			}
 		}
 		fillGameBoard();
 				
 	}
 	private void fillGameBoard(){
 		
 	}
 	
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// I will fill this out
 	}
 	*/
 	public boolean validateMove(int oldX, int oldY, int newX, int newY) {
 		boolean red = (grid[newX][newY].isBlack());
 		boolean piece = grid[newX][newY] == null;
 
 		if (red || red != piece)
 			return false;
 
 		/*
 		 * If this is a valid non-jump move, then make the move and return true.
 		 * Making the move requires: (0) picking up the piece, (1) putting it
 		 * down on the new square, and (2) promoting it to a new king if necessary.
 		 */
 
 		if (isValidNonjump(oldX, oldY, newX, newY)) {
 			piece = grid[oldX][oldY] == null;
 			grid[newX][newY]= grid[oldY][oldX];
 			grid[oldX][oldY] = null;
 			crownKing(newX, newY);
 			return true;
 		}
 
 		/*
 		 * If this is a valid jump move, then make the move and return true.
 		 * Making the move requires: (0) picking up the piece, (1) putting it
 		 * down on the new square, and (2) promoting the new piece to a king if necessary
 		 */
 		if (isValidjump(oldX, oldY, newX, newY)) {
 			piece = grid[oldX][oldY] == null;
 			grid[newX][newY]= grid[oldY][oldX];
 			grid[oldX][oldY] = null;
 			
 			crownKing(newX, newY);
 			return true;
 		}
 
 		// The move is invalid, so return false
         return false;
 		/*
 		 * Make sure to check: 1. the move is diagonal 2. it is only one square
 		 * if it is a move 3. a friendly piece is not in the way 4. If it is a
 		 * jump 5. If a piece blocks the jump
 		 */
 	}
 
 	private boolean isValidNonjump(int oldX, int oldY, int newX, int newY) {
 		int xPos = newX - oldX;
 		int yPos = newY - oldY;
 
 		// Return false if the move is not a move to an adjacent row and column,
 		if (Math.abs(xPos) != 1)
 			return false;
 		if (Math.abs(yPos) != 1)
 			return false;
 
 		if(grid[oldX][oldY] == null)
 			return false;
 		// Return true if this is a King
 		if (grid[oldX][oldY].isKing())
 			return true;
 
 		// The piece is not a king. Return value of the piece moves forward
 		return ((!grid[oldX][oldY].isBlack()) && yPos > 0)
 				|| (grid[oldX][oldY].isBlack() && yPos < 0);
 	}
 
 	private boolean isValidjump(int oldX, int oldY, int newX, int newY) {
 		int xPos = newX - oldX;
 		int yPos = newY - oldY;
 
 		//........................................
 		if(grid[oldX][oldY] == null)
 			return false;
 		// Return true if this is a King which can go in any direction
 		if (grid[oldX][oldY].isKing())
 			return true;
 
 		// The piece is not a king. Return value of the piece moves forward
 		return ((!grid[oldX][oldY].isBlack()) && yPos > 0)
 				|| (grid[oldX][oldY].isBlack() && yPos < 0);
 	}
 	private void crownKing(int newX, int newY){
		if ((newY == 7) && !grid[newX][newY].isBlack()) {
 			grid[newX][newY].setKing(true);
 			return;
 		}
 
		if ((newY == 0) && grid[newX][newY].isBlack()) {
 			grid[newX][newY].setKing(true);
 			return;
 		}
 	}
 	public boolean performMove(int oldX, int oldY, int newX, int newY){
 		// Fill me out
 		boolean valid = validateMove(oldX, oldY, newX, newY);
 		// Preform all moves
 		return false;
 		
 	}
 	public void removePiece(int x, int y) {
 		if (grid[x][y]!= null) {
 			grid[x][y] = null;
 		} else
 			throw new NullPointerException("No piece Found");
 	}
 	public ArrayList<CheckerSquare> getBlackPieces() {
 		return blackPieces;
 	}
 	public ArrayList<CheckerSquare> getRedPieces() {
 		return redPieces;
 	}
 	public CheckerSquare[][] getGrid() {
 		// TODO Auto-generated method stub
 		return grid;
 	}
 	
 	
 	
 	// Old generation code
 	/*
 	 * 
 	 */
 }
