 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.HashSet;
 
 public class Board {
 	
 	public static final int W = 1;
 	public static final int B = -1;
 	public static final int EMPTY = 0;
 	
 	private Position[] positions;
 	
 	private Move lastMove;
 	
 	private int lastColorPlayed;
 	
 	public Board() {
 		
 		lastMove = new Move();
 		lastColorPlayed = B;
 		positions = new Position[28];
 		for(int i=0; i<28; i++) {
 			positions[i] = new Position();
 		}	
 		
 		/* setting standard backgammon starting positions
 		 */
 		positions[1].setCol(W);
 		positions[1].setNum((byte)2);
 		positions[6].setCol(B);
 		positions[6].setNum((byte)5);
 		positions[8].setCol(B);
 		positions[8].setNum((byte)3);
 		positions[12].setCol(W);
 		positions[12].setNum((byte)5);
 		positions[13].setCol(B);
 		positions[13].setNum((byte)5);
 		positions[17].setCol(W);
 		positions[17].setNum((byte)3);
 		positions[19].setCol(W);
 		positions[19].setNum((byte)5);
 		positions[24].setCol(B);
 		positions[24].setNum((byte)2);
 		
 		/* we keep the eaten pills in [0] for white, and [25] for
 		 * black. also, we keep the pills that have exited the board 
 		 * in [26] for white, and [27] for black
 		 */ 
 		positions[0].setCol(W);
 		positions[25].setCol(B);
 		positions[26].setCol(W);
 		positions[27].setCol(B);
 	}
 	
 	public Board(Board board) {
 		
 		this.lastMove = board.lastMove;
 		this.lastColorPlayed = board.lastColorPlayed;
 		this.positions = new Position[28];
 		for(int i=0; i<28; i++) {
 			positions[i] = new Position(board.positions[i]);
 		}
 	}
 	
 	
 	public Move getLastMove() {
 		
 		return lastMove;
 	}
 	
 	public int getLastColPlayed() {
 		
 		return lastColorPlayed;
 	}
 	
 	public Position[] getPositions() {
 		
 		return positions;
 	}
 	
 	public void setLastMove(Move lastMove) {
 		
 		this.lastMove.setFrom(lastMove.getFrom());
 		this.lastMove.setTo(lastMove.getTo());
 		this.lastMove.setCol(lastMove.getCol());
 	}
 	
 	public void setLastColPlayed(int lastCol) {
 		
 		this.lastColorPlayed = lastCol;
 	}
 	
 	public void setPositions(Position[] pst) {
 		
 		for(int i=0; i<28; i++) {
 			
 			this.positions[i] = new Position(pst[i]);
 		}
 	}
 
 
 	public void playMove(int from, int to, int col) {
 		
 		/* checking who wants to make the move, and manipulating
 		 * the board accordingly
 		 */
 		if(col==W) {
 			/* in the case below, the black pill in position [to] is
 			 * eaten, so we store it in position [25] the 'else'
 			 * statement is written with the same logic
 			 */
 			if(positions[to].getCol()==B) {
 				positions[to].decr();
 				positions[25].incr();
 			}
 			
 		} else {
 			if(positions[to].getCol()==W) {
 				positions[to].decr();
 				positions[0].incr();
 			}
 		}
 		
 		positions[from].decr();
 		if(positions[from].getNum()==0) {
 			positions[from].setCol(EMPTY);
 		}
 		positions[to].incr();
 		positions[to].setCol(col);
 		
 		lastMove = new Move(from, to, col);
 		lastColorPlayed = col;
 	}
 	
 	public boolean lastrun(int col) {
 		
 		boolean lastrun = true;
 		if(col==W) {
 			for(int j=0; j<19; j++) {
 				
 				if(positions[j].getCol()==W && positions[j].getNum()>0) {
 					lastrun = false;
 					break;
 				}
 			}
 		} else if(col== B) {
 			for(int j=25; j>6; j--) {
 				
 				if(positions[j].getCol()==Board.B && positions[j].getNum()>0) {
 					lastrun = false;
 					break;
 				}
 			}
 		}
 		return lastrun;
 	}
 	
 	public boolean moveIsLegal(int from, int to, int col, int d1, int d2) {
 		
 		boolean direction = (((col==Board.W) && ((to-from)>0)) || ((col==Board.B) && ((to-from)<0))); 
 		
 		if(!direction) {
 			return false;
 		}
 		
 		if((from < 0) || (to<0) || (from>27) || (to>27)) {
 			return false;
 		}
 		
 		if(col==W) {
 			if(positions[to].getCol()==B && positions[to].getNum()>1) {
 				return false;
 			}
 		} else {
 			if(positions[to].getCol()==W && positions[to].getNum()>1) {
 				return false;
 			}
 		}
 		
 		if(positions[from].getCol() != col) {
 			return false;
 		}
 		
		if(((Math.abs(to-from)!=d1) && (Math.abs(to-from)!=d2)) && !this.lastrun(col)) {
 			return false;
 		}
 		
 		if(col==W && positions[0].getNum()>0 && from != 0) {
 			return false;
 		}
 		
 		if(col==B && positions[25].getNum()>0 && from !=25) {
 			return false;
 		}
 		
 		if((col==W && to==27) || (col==B && to==26)) {
 			return false;
 		}
 		
 		int farthestWhite = -1;
 		
 		if(to==26) {
 			if(from<19) {
 				return false;
 			}
 			for(int i=19; i<25; i++) {
 				if(positions[i].getCol()==W) {
 					farthestWhite = i;
 					break;
 				}
 			}
 		}
 		
 		int farthestBlack = -1;
 		
 		if(to==27) {
 			if(from>6) {
 				return false;
 			}
 			for(int i=6; i>0; i--) {
 				if(positions[i].getCol()==B) {
 					farthestBlack = i;
 					break;
 				}
 			}
 		}
 		
 		if(to==26 && from!=farthestWhite && (25-from)!=d1 && (25-from)!=d2) {
 			return false;
 		}
 		
 		if(to==27 && from!=farthestBlack && from!=d1 && from!=d2) {
 			return false;
 		}
 		
 		if(to==26 && from == farthestWhite) {
 			if((25-from)>d1 && (25-from)>d2) {
 				return false;
 			}
 		}
 		
 		if(to==27 && from == farthestBlack) {
 			if(from>d1 && from>d2) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	public Move[] movegen(int d1,int d2, int col) {
 		
 		int counter = 0;
 		
 		Move[] moves;
 		
 		boolean dubs = (d1==d2);
 		
 		Set<Integer> froms = new HashSet<Integer>();
 		
 		/* counting how many positions contain
 		 * pills of our given color , and 
 		 * adding those positions to a set */
 		for(int i=0; i<26; i++) {
 			if(positions[i].getCol()==col) {
 				counter++;
 				froms.add(i);
 			}
 		}
 		
 		/* the moves to be sent to getChildren()
 		 * can't be more than 4 per position if
 		 * we have a regular roll, and 4 per 
 		 * position if we have dubs */
 		if(!dubs)
 			moves = new Move[counter*2];
 		else
 			moves = new Move[counter];
 		
 		Iterator<Integer> it = froms.iterator();
 		int i = 0;
 		int temp = 0;
 		
         while (it.hasNext()) {
         	if(!dubs) {
 	        	if(col==W) {
 	        		temp = it.next();
 	        		moves[i] = new Move(temp, temp+d1, col);
 	        		i++;
 	        		moves[i] = new Move(temp, temp+d2, col);
 	        		i++;
 	        	} else if(col==B) {
 	        		temp = it.next();
 	        		moves[i] = new Move(temp, temp-d1, col);
 	        		i++;
 	        		moves[i] = new Move(temp, temp-d2, col);
 	        		i++;
 	        	}
         	} else {
         		if(col==W) {
         			temp = it.next();
         			moves[i] = new Move(temp, temp+d1, col);
         			i++;
         			
         		} else if(col==B) {
         			temp = it.next();
         			moves[i] = new Move(temp, temp-d1, col);
         			i++;
         		}
         	}
         }
 		return moves;
 	}
 	
 	public ArrayList<Board> getChildren(int d1, int d2, int col) {
 		
 		ArrayList<Board> children = new ArrayList<Board>();
 		
 		Move[] moves = movegen(d1, d2, col);
 		
 		for(int i=0; i<moves.length; i++) {
 		
 			if(moveIsLegal(moves[i].getFrom(), moves[i].getTo(), col, d1, d2)) {
 				Board child = new Board(this);
 				child.playMove(moves[i].getFrom(), moves[i].getTo(), col);
 				children.add(child);
 			}
 		}
 		
 		return children; 
 	} 
 	
 	public boolean isTerminal() {
 		
 		if(positions[26].getNum()==15 || positions[27].getNum()==15) {
 		//if(positions[26].getNum()==5 || positions[27].getNum()==5) {	
 			return true;
 		}
 		return false;
 	}
 	
 	public void print() {
 		
 		System.out.println(" 12  11  10   9   8   7   6   5   4   3   2   1");
 		for(int i=12; i>0; i--) {
 			printHelp(i);
 		}
 		
 		System.out.println("\n|\t\t\t\t\t\t|");
 		
 		for(int i=13; i<25; i++) {
 			printHelp(i);
 		}
 		System.out.println("\n 13  14  15  16  17  18  19  20  21  22  23  24");
 		
 		System.out.println("\n");
 		System.out.println("                0   25");
 		System.out.println("eaten pills : <w"+positions[0].getNum()+
 						   "> <b"+positions[25].getNum()+">");
 		System.out.println("pills out : <w"+positions[26].getNum()+
 						   "> <b"+positions[27].getNum()+">");
 		System.out.println("             26  27");
 		
 	}
 	
 	public void printHelp(int i) {
 	
 		if(positions[i].getCol()==EMPTY) {
 				System.out.print("<  >");
 		} else {
 			if(positions[i].getCol()==W) {
 				System.out.print("<w"+positions[i].getNum()+">");
 			} else {
 				System.out.print("<b"+positions[i].getNum()+">");
 			}
 		}	
 	}
 }
