 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 
 public class Player {
 
 	private int maxDepth;
 	private int playerColor;
 	private int d1;
 	private int d2;
 	
 	public Player() {
 		
 		maxDepth = 2;
 		playerColor = Board.B;
 	}
 	
 	public Player(int maxDepth, int playerColor) {
 		
 		this.maxDepth = maxDepth;
 		this.playerColor = playerColor;
 	}
 	
 	public void roll() {
 		
 		this.d1 = (int)(Math.random()*6)+1;
 		this.d2 = (int)(Math.random()*6)+1;
 	}
 	
 	public Move[] inputMove(int d1, int d2, Board b) {
 		
 		Move[] moves;
 		String fr;
 		String t;
 		int from = -1;
 		int to = -1;
 		boolean gotit = false;
 		boolean dubs = (d1==d2);
 		boolean direction = false;
 		
 		/* to determine which dice was played */
 		boolean pD1 = false;
 		boolean pD2 = false;
 		
 		/* makes new Move array with length depending on
 		 * whether or not we have doubles 
 		 */
 		if(dubs)
 			moves = new Move[4];
 		else
 			moves = new Move[2];
 		
 		/* creates a temporary board so that we can 
 		 * display moves while a turn has not finished
 		 */
 		Board tempB = new Board(b);
 		
 		for(int i=0; i<moves.length; i++) { 
 			
 			InputStreamReader read = new InputStreamReader(System.in);
 			BufferedReader in = new BufferedReader(read);
 			
 			if(playerColor==Board.W)
 				System.out.println("White rolled "+d1+" and "+d2+" .");
 			else 
 				System.out.println("Black rolled "+d1+" and "+d2+" .");
 			
			Position[] tempP = tempB.getPositions();
 			
 			/* checking if we are in lastrun mode */
 			boolean lastrun = tempB.lastrun(playerColor);
 			
 			System.out.println("lastrun = "+lastrun);
 			
 			while(!gotit) {
 				
 				try {
 					
 					System.out.println("Enter where you want to move from :");
 					fr = in.readLine();
 					from = Integer.parseInt(fr);
 					System.out.println("Enter where you want to move to :");
 					t = in.readLine();
 					to = Integer.parseInt(t);
 					
 					/* checking if everyone is moving in the right direction */
 					direction = (((playerColor==Board.W) && ((to-from)>0)) || ((playerColor==Board.B) && ((to-from)<0))); 
 					
 					if((Math.abs(from-to) == d1) && direction && (!pD1 || dubs) || lastrun) {
 						
 						
 						/* making the move on the temporary board
 						 * for displaying purposes
 						 */
 						if(tempB.moveIsLegal(from, to, playerColor, d1, d2)) {
 							
 							if((!dubs && i==0)||(dubs && i!=3)) {
 								tempB.playMove(from, to, playerColor);
 								tempB.print();
 							}
 							pD1 = true;
 							gotit = true;
 						} else {
 							System.out.println("\nIllegal move, try again0. \n");
 						}
 						
 					} else if((Math.abs(from-to) == d2) && direction && (!pD2 || dubs) || lastrun)  {
 					
 						/* same as above */
 						if(tempB.moveIsLegal(from, to, playerColor, d1, d2)) {
 							
 							if((!dubs && i==0)||(dubs && i!=3)) {
 								tempB.playMove(from, to, playerColor);
 								tempB.print();
 							}
 							pD2 = true;
 							gotit = true;
 						} else {
 							System.out.println("\nIllegal move, try again1. \n");
 						}	
 					}else {
 					
 						System.out.println("\nIllegal move, try again2. \n");
 					}
 					
 				} catch(Exception e) {
 					
 					System.out.println("\nTry again3.\n");
 					//e.printStackTrace();
 				}
 			}
 			gotit = false;
 			moves[i] = new Move(from, to, playerColor);
 			if(tempB.isTerminal()) {
 				Move[] tempM = new Move[i+1];
 				for(int c=0; c<tempM.length; c++) {
 					tempM[c] = moves[c];
 				}
 				return tempM;
 			}
 		}
 			
 		return moves;
 	}
 	
 	public int getD1() {
 		
 		return this.d1;
 	}
 	
 	public int getD2() {
 			
 		return this.d2;
 	}
 	
 	public int getDepth() {
 		
 		return this.maxDepth;
 	}
 }
