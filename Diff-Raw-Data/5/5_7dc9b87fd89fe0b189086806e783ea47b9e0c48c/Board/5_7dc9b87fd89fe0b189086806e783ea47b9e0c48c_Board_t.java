 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.HashSet;
 
 public class Board {
 	
 	public static final byte W = 1;
 	public static final byte B = -1;
 	public static final byte EMPTY = 0;
 	
 	/* the backgammon board */
 	private Position[] positions;
 	
 	/* the last move that was played
 	 * on this board */
 	private Move lastMove;
 	
 	/* the last player that played on this board */
 	private byte lastColorPlayed;
 	
 	/* the value of the board. this is not
 	 * the value of the heuristic, it is the
 	 * value that is pushed from leaf boards(nodes)
 	 * to non-leaf boards while minimax is
 	 * being executed */
 	private int value;
 	
 	/* the values of the two dice that were
 	 * last played on the board */
 	private byte d1Pl;
 	private byte d2Pl;
 	
 	public Board() {
 		
 		lastMove = new Move();
 		
 		/* edit this to change who plays first */
 		lastColorPlayed = B;
 		
 		value = 0;
 
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
 		this.value = board.value;
 		this.positions = new Position[28];
 		this.d1Pl = board.d1Pl;
 		this.d2Pl = board.d2Pl;
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
 	
 	public int getValue() {
 		return value;
 	}
 	
 	public byte getd1Pl() {
 		return d1Pl;
 	}
 	
 	public byte getd2Pl() {
 		return d2Pl;
 	}
 	
 	public void setLastMove(Move lastMove) {
 		
 		this.lastMove.setFrom(lastMove.getFrom());
 		this.lastMove.setTo(lastMove.getTo());
 		this.lastMove.setCol(lastMove.getCol());
 	}
 	
 	public void setLastColPlayed(byte lastCol) {
 		
 		this.lastColorPlayed = lastCol;
 	}
 	
 	public void setPositions(Position[] pst) {
 		
 		for(int i=0; i<28; i++) {
 			
 			this.positions[i] = new Position(pst[i]);
 		}
 	}
 	
 	public void setValue(int value) {
 		this.value = value;
 	}
 	
 	public void setd1Pl(byte d1Pl) {
 		this.d1Pl = d1Pl;
 	}
 	
 	public void setd2Pl(byte d2Pl) {
 		this.d2Pl = d2Pl;
 	}
 
 	public void playMove(byte from, byte to, byte col) {
 		
 		/* checking who wants to make the move, and manipulating
 		 * the board accordingly. 
 		 * 
 		 * in the case below, the black pill in position [to] 
 		 * is eaten, so we store it in position [25] the 'else'
 		 * statement is written with the same logic.
 		 */
 		if(col==W) {
 	
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
 		if(positions[from].getNum()==0 && from!=0 && from !=25) {
 			positions[from].setCol(EMPTY);
 		}
 		positions[to].incr();
 		positions[to].setCol(col);
 		
 		lastMove = new Move(from, to, col);
 		lastColorPlayed = col;
 	}
 	
 	public boolean lastrun(byte col) {
 		
 		/* the board is checked to see if the player
 		 * with color 'col' is in lastrun mode. 
 		 * (the gathering phase of the game) 
 		 */
 		
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
 	
 	public boolean moveIsLegal(byte from, byte to, byte col, byte d1, byte d2) {
 		
 		/* various legal checks (hopefully) in order of importance */
 		
 		boolean direction = (((col==Board.W) && ((to-from)>0)) || ((col==Board.B) && ((to-from)<0))); 
 		
 		if(!direction && to!=27) {
 			return false;
 		}
 		
 		if(from==to) {
 			return false;
 		}
 		
 		if(from==26 || from==27) {
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
 		
 		if((col==W && positions[0].getNum()>0) && from != 0) {
 			return false;
 		}
 		
 		if((col==B && positions[25].getNum()>0) && from !=25) {
 			return false;
 		}
 		
 		if((col==W && to==27) || (col==B && to==26)) {
 			return false;
 		}
 		
 		if((col==W && to==25) || (col==B && to==0)) {
 			return false;
 		}
 		
 		if((to==26 && !this.lastrun(W))|| (to==27 && !this.lastrun(B))) {
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
 	
 	public Move[] movegen(byte d1,byte d2, byte col) {
 		
 		/* this generates an array of moves on the current board.
 		 * the moves need not be legal. this method is 
 		 * called only for single(non-double) dice rolls.
 		 */
 		
 		int counter = 0;
 		
 		Move[] moves;
 		
 		Set<Integer> froms = new HashSet<Integer>();
 		
 		/* counting how many positions contain
 		 * pills of our given color , and 
 		 * adding those positions to a set 
 		 */
 		for(int i=0; i<26; i++) {
 			if(positions[i].getCol()==col && positions[i].getNum()>0) {
 				counter++;
 				froms.add(i);
 			}
 		}
 		
 		/* the moves to be sent to getChildren()
 		 * are 2 per position if
 		 * we have a regular roll
 		 */
 		moves = new Move[counter*2];
 		
 		Iterator<Integer> it = froms.iterator();
 		int i = 0;
 		int temp = 0;
 		
         while (it.hasNext()) {
         	
 			if(col==W) {
         		
 				temp = it.next();
         		
 				if(temp+d1>24) {
 					moves[i] = new Move((byte)temp, (byte)26, col);
 					i++;
 				} else {
 					moves[i] = new Move((byte)temp, (byte)(temp+d1), col);
 	        		i++;
 				}
 				
 				if(temp+d2>24) {
 					moves[i] = new Move((byte)temp, (byte)26, col);
 					i++;
 				} else {
 					moves[i] = new Move((byte)temp, (byte)(temp+d2), col);
 	        		i++;
 				}
         	} else if(col==B) {
         		
         		temp = it.next();
         		
         		if(temp-d1<1) {
         			moves[i] = new Move((byte)temp, (byte)27, col);
         			i++;
         		} else {
         			moves[i] = new Move((byte)temp, (byte)(temp-d1), col);
 	        		i++;
         		}
         		
         		if(temp-d2<1) {
         			moves[i] = new Move((byte)temp, (byte)27, col);
         			i++;
         		} else {
         			moves[i] = new Move((byte)temp, (byte)(temp-d2), col);
 	        		i++;
         		}
         	}
         }
 		return moves;
 	}
 	
 	public Move[] movegen(byte dice, byte col) {
 		
 		Move[] moves;
 		
 		Set<Integer> froms = new HashSet<Integer>();
 		
 		int counter = 0;
 		
 		for(int i=0; i<26; i++) {
 			if(positions[i].getCol()==col && positions[i].getNum()>0) {
 				counter++;
 				froms.add(i);
 			}
 		}
 		
 		if(counter!=0) 
 			moves = new Move[counter];
 		else 
 			return null;
 		
 		Iterator<Integer> it = froms.iterator();
 		int i=0;
 		int temp =0;
 		
 		while(it.hasNext()) {
 			if(col==W) {
 				
 				temp = it.next();
 				
 				if(temp+dice>24) {
 					moves[i] = new Move((byte)temp, (byte)26, col);
 					i++;
 				} else {
 					moves[i] = new Move((byte)temp, (byte)(temp+dice), col);
 					i++;
 				}
 			} else if(col==B) {
 				
 				temp = it.next();
 				
 				if(temp-dice<1) {
 					moves[i] = new Move((byte)temp, (byte)27, col);
 					i++;
 				} else { 
 					moves[i] = new Move((byte)temp, (byte)(temp-dice), col);
 					i++;
 				}
 			}
 		}
 		return moves;
 	}
 	
 	public ArrayList<Board> getChildren(byte d1, byte d2, byte col) {
 				
 		
 		ArrayList<Board> children = new ArrayList<Board>();
 		
 		Set<Board> childSet = new HashSet<Board>();
 		
 		boolean dubs = (d1==d2);
 		
 		if(!dubs) {
 			
 			ArrayList<Board> temp = new ArrayList<Board>();
 			
 			/* movegen() fills the moves array with possible
 			 * moves(not necessarily legal ones). 
 			 */
 			Move[] moves = movegen(d1, d2, col);
 			
 			for(int i=0; i<moves.length; i++) {
 			
 				if(moveIsLegal(moves[i].getFrom(), moves[i].getTo(), col, d1, d2)) {
 					Board child = new Board(this);
 					child.playMove(moves[i].getFrom(), moves[i].getTo(), col);
 					if(child.isTerminal()) {
 						childSet.add(child);
 					} else {
 						temp.add(child);
 					}
 				}
 			}
 		
 			ArrayList<Board> temp2;
 			
 			for(Board child : temp) {
 					
 				if(Math.abs(child.getLastMove().getFrom()-child.getLastMove().getTo())==d1) {
 					
 					temp2 = child.getChildren(d2, col);
 					if(!temp2.isEmpty()) {
 						for(Board tempchild : temp2) {
 							childSet.add(tempchild);
 						}
 					} else {
 						childSet.add(child);
 					}
 				} else if(Math.abs(child.getLastMove().getFrom()-child.getLastMove().getTo())==d2) {
 					
 					temp2 = child.getChildren(d1, col);
 					if(!temp2.isEmpty()) {
 						for(Board tempchild : temp2) {
 							childSet.add(tempchild);
 						}
 					} else {
 						childSet.add(child);
 					}
 				} else {
 					
 					if(col==W) {
 						if((child.getLastMove().getFrom()+d1) > 24 && child.getLastMove().getFrom()+d2 > 24) {
 							if(d1<d2) {
 								
 								temp2 = child.getChildren(d1, col);
 								if(!temp2.isEmpty()) {
 									for(Board tempchild : temp2) {
 										childSet.add(tempchild);
 									}
 								} else {
 									childSet.add(child);
 								}
 							} else {
 								
 								temp2 = child.getChildren(d2, col);
 								if(!temp2.isEmpty()) {
 									for(Board tempchild : temp2) {
 										childSet.add(tempchild);
 									}
 								} else {
 									childSet.add(child);
 								}
 							}
 						} else if(child.getLastMove().getFrom()+d1 == 25) {
 							
 							temp2 = child.getChildren(d2, col);
 							if(!temp2.isEmpty()) {	
 								for(Board tempchild : temp2) {
 									childSet.add(tempchild);
 								}
 							} else {
 								childSet.add(child);
 							}
 						} else if(child.getLastMove().getFrom()+d2 == 25) {
 							
 							temp2 = child.getChildren(d1, col);
 							if(!temp2.isEmpty()) {
 								for(Board tempchild : temp2) {
 									childSet.add(tempchild);
 								}
 							} else {
 								childSet.add(child);
 							}
 						}
 					} else if(col==B) {
 						if((child.getLastMove().getFrom()-d1) < 0 && child.getLastMove().getFrom()-d2 < 0) {
 							if(d1<d2) {
 								
 								temp2 = child.getChildren(d1, col);
 								if(!temp2.isEmpty()) {
 									for(Board tempchild : temp2) {
 										childSet.add(tempchild);
 									}
 								} else {
 									childSet.add(child);
 								}
 							} else {
 								
 								temp2 = child.getChildren(d2, col);
 								if(!temp2.isEmpty()) {
 									for(Board tempchild : temp2) {
 										childSet.add(tempchild);
 									}
 								} else {
 									childSet.add(child);
 								}
 							}
 						} else if(child.getLastMove().getFrom()-d1 == 0) {
 							
 							temp2 = child.getChildren(d2, col);
 							if(!temp2.isEmpty()) {
 								for(Board tempchild : temp2) {
 									childSet.add(tempchild);
 								}
 							} else {
 								childSet.add(child);
 							}
 						} else if(child.getLastMove().getFrom()-d2 == 0) {
 							
 							temp2 = child.getChildren(d1, col);
 							if(!temp2.isEmpty()) {
 								for(Board tempchild : temp2) {
 									childSet.add(tempchild);
 								}
 							} else {
 								childSet.add(child);
 							}
 						}
 					}
 				}
 			}
 		} else {
 			
 			ArrayList<Board> t1, t2, t3, t4;
 						
 			t1 = getChildren(d1, col);
 			
 			for(Board temp1 : t1) {
 				
 				if(temp1.isTerminal()) {
 					childSet.add(temp1);
 					break;
 				}
 				
 				t2 = temp1.getChildren(d1, col);
 				if(t2.isEmpty()) {
 					childSet.add(temp1);
 					continue;
 				}
 			
 				for(Board temp2 : t2) {
 					
 					if(temp2.isTerminal()) {
 						childSet.add(temp2);
 						break;
 					}
 					
 					t3 = temp2.getChildren(d1, col);
 					if(t3.isEmpty()) {
 						childSet.add(temp2);
 						continue;
 					}
 					
 					for(Board temp3 : t3) {
 						
 						if(temp3.isTerminal()) {
 							childSet.add(temp3);
 							break;
 						}
 					
 						t4 = temp3.getChildren(d1, col);
 						if(t4.isEmpty()) {
 							childSet.add(temp3);
 							continue;
 						}
 						
 						for(Board child : t4) {
 							
 							childSet.add(child);
 						}
 					}
 				}
 			}
 			
 			
 		}
 		Iterator<Board> it = childSet.iterator();
 		
 		while(it.hasNext()) {
 			children.add(it.next());
 		}
 		
 		return children; 
 	} 
 	
 	public ArrayList<Board> getChildren(byte dice, byte col) {
 		
 		ArrayList<Board> children = new ArrayList<Board>();
 		
 		Move[] moves = movegen(dice, col);
 		
 		if(moves==null) {
 			System.out.println("\n\n\nNULL\n\n\n");
 			return null;
 		}
 		
 		for(int i=0; i<moves.length; i++) {
 			
 			if(moveIsLegal(moves[i].getFrom(), moves[i].getTo(), col, dice, dice)) {
 				Board child = new Board(this);
 				child.playMove(moves[i].getFrom(), moves[i].getTo(), col);
 				children.add(child);
 			}
 		}
 		
 		return children;
 	}
 	
 	public int evaluate() {
 		
 		int bsum = 0;
 		int wsum = 0;
 		
 		for(int i=1; i<25; i++) {
 			
 			if(positions[i].getNum()>1 && positions[i].getCol()==B) {
 				bsum += 4;
 			} else if(positions[i].getNum()==1 && positions[i].getCol()==B) {
 				bsum -= 3;
 			}
 			
 			if(positions[i].getNum()>1 && positions[i].getCol()==W) {
 				wsum += 4;
 			} else if(positions[i].getNum()==1 && positions[i].getCol()==W) {
 				wsum -= 3;
 			}
 		}
 		
 		if(positions[0].getNum()>0) {
 			wsum -= positions[0].getNum()*4;
 			bsum += positions[0].getNum()*4;
 		}
 		
 		if(positions[26].getNum()>0) {
 			wsum += positions[26].getNum()*5;
 		}
 		
 		if(positions[25].getNum()>0) {
 			bsum -= positions[25].getNum()*4;
 			wsum += positions[25].getNum()*4;
 		}
 		
 		if(positions[27].getNum()>0) {
 			bsum += positions[27].getNum()*5;
 		}
 		
 		return bsum - wsum;
 	}
 	
 public int evaluate_modie() {
 		
 		int bsum = 0;
 		int wsum = 0;
 		
 		for(int i=1; i<25; i++) {
 			
 			if(positions[i].getNum()>1 && positions[i].getCol()==B) {
 				bsum += 6;
 			} else if(positions[i].getNum()==1 && positions[i].getCol()==B) {
 				bsum -= 5;
 			}
 			
 			if(positions[i].getNum()>1 && positions[i].getCol()==W) {
 				wsum += 6;
 			} else if(positions[i].getNum()==1 && positions[i].getCol()==W) {
 				wsum -= 5;
 			}
 		}
 		
 		if(positions[0].getNum()>0) {
 			wsum -= positions[0].getNum()*4;
 			bsum += positions[0].getNum()*4;
 		}
 		
 		if(positions[26].getNum()>0) {
 			wsum += positions[26].getNum()*5;
 		}
 		
 		if(positions[25].getNum()>0) {
 			bsum -= positions[25].getNum()*4;
 			wsum += positions[25].getNum()*4;
 		}
 		
 		if(positions[27].getNum()>0) {
 			bsum += positions[27].getNum()*5;
 		}
 		
 		return bsum - wsum;
 	}
 	
 	public boolean isTerminal() {
 		
 		if(positions[26].getNum()==15 || positions[27].getNum()==15) {
 		//if(positions[26].getNum()==5 || positions[27].getNum()==5) {	
 			return true;
 		}
 		return false;
 	}
 	
 	/*------------------Utilities-&-Overrides------------------*/
 	
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
 	
 	private void printHelp(int i) {
 	
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
 	
 	public boolean equals(Object o){
 	    
 	
 	    if(o == null) {
 	    	return false;
 	    }
 	    if(!(o instanceof Board)) {
 	    	return false;
 	    }
 
 	    Board b = (Board)o;
 		
 		for(int i=0; i<28; i++) {
 			if((this.positions[i].getCol() != b.positions[i].getCol()) || (this.positions[i].getNum() != b.positions[i].getNum())) {
 				return false;
 			}
 		}
 	    return true;
 	  }
 	
 	/*public int hashCode() {  
 		
 		int hc = 0;
 		int eval = this.evaluate();
 		
 		for(int i=0; i<28; i++) {
 			
 			hc += ((this.positions[i].getNum()&this.positions[i].getCol())*(this.positions[i].getNum()^127)&eval)+i*eval*this.positions[i].getCol();
 		}
 		
 	    return hc;
 	}*/ 
 	
 	public int hashCode() {
 		
 		int hc = 5381;
 		int num ,col;	
 		for(int i=0; i<28; i++) { 
 			col = this.positions[i].getCol();
 			num = this.positions[i].getNum();
 			if(col == B) 
 				col = 3;
			else if(col == EMPTY)
 				col = 1;
			else if(col == W)
 				col = 5;
 			hc +=   ((hc << 5 )  + hc  ) +  num + i*col*3;  
 		}
 		return hc;
 	} 
 }
