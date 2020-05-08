 package oving5;
 
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 public class Board {
 	//Size of the board.
 	public final int k;
 	//the board.
 	private Square[][] board;
 	
 	public Board(int k) {
 		this.k = k;
 		board = new Square[k][k];
 		//Initializing the board with squares.
 		for(int y=0; y<k; y++){
 			for(int x=0; x<k; x++){
 				board[y][x] = new Square(x, y);
 			}
 		}
 	}
 
 	
 	/**
 	 * Returns the given square.
 	 * @param x	Position x
 	 * @param y	Position y
 	 * @return
 	 */
 	public Square getSquare(int x, int y){
 		return board[y][x];
 	}
 	
 	/**
 	 * Count how many queens that are attcking the square.
 	 * @param s		The square that the method is checking
 	 * @return		number of attacking queens.
 	 */
 	public int queensAttackingSquare(Square s) {
 		//number of conflicts
 		int conflicts = 0;
 		//The squares X and Y position.
 		int sX = s.getPosX();
 		int sY = s.getPosY();
 		
 		for(int i=0; i<k; i++){
 			//row
 			if(board[sY][i].isQueen()) {
 				if(sX != i)
 					conflicts++;
 			}
 			//column
 			if(board[i][sX].isQueen()) {
 				if(sY != i)
 					conflicts++;
 			}
 			
 			if(i==0)
 				continue; //So that the queen isn't in attacking itself.
 			
 			//diagonals
 			if(!(sX+i>k-1)){ //Right
 				if(!(sY+i>k-1))//down
 					if(board[sY+i][sX+i].isQueen())
 						conflicts++;
 				if(!(sY-i<0))//up
 					if(board[sY-i][sX+i].isQueen())
 						conflicts++;
 			}
 			if(!(sX-i<0)){//Left
 				if(!(sY-i<0))//up
 					if(board[sY-i][sX-i].isQueen())
 						conflicts++;
 				if(!(sY+i>k-1))//down
 					if(board[sY+i][sX-i].isQueen())
 						conflicts++;
 			}
 		}
 		return conflicts;
 	}
 	
 	/**
 	 * Updates the number of queens attacking each square.
 	 */
 	public void updateSquares() {
 		for(Square[] s : board)
 			for(Square ss : s)
 				ss.setNumperOfQueensAttacking(queensAttackingSquare(ss));
 	}
 	
 	
 	public ArrayList<Square> getQueensInConflict() {
 		ArrayList<Square> list = new ArrayList<Square>();
 		
 		for(Square[] s : board)
 			for(Square ss : s)
				if(ss.isQueen())
					if(ss.getNumperOfQueensAttacking()>0)
						list.add(ss);
 		return list;
 	}
 	
 	/**
 	 * Adds all squares to a PriorityQueue that places them in order after how many queens that's attacking it.
 	 * @return
 	 */
 	public PriorityQueue<Square> getOrderedQueueOfSquares() {
 		PriorityQueue<Square> list = new PriorityQueue<Square>();
 		
 		for(Square[] s : board)
 			for(Square ss : s)
 				if(!ss.isQueen())
 					list.add(ss);
 		return list;
 	}
 	
 	/**
 	 * Check to se if the "puzzle" is solved. By checking if there is any queeens attacking each other.
 	 * @return	if the puzzle is solved.
 	 */
 	public boolean isSolved() {	
 		return getQueensInConflict().size()==0;
 	}
 	
 	/**
 	 * toString Method for printing.
 	 */
 	@Override
 	public String toString(){
 		String tmp = "";
 		for (int i = 0; i < k; i++) {
 			tmp += "\n";
 			for (int j = 0; j < k; j++) {
 				if(getSquare(i, j).isQueen())
 					tmp += " X ";
 				else
 					tmp += " O ";
 			}
 		}
 		return tmp;
 		
 	}
 }
