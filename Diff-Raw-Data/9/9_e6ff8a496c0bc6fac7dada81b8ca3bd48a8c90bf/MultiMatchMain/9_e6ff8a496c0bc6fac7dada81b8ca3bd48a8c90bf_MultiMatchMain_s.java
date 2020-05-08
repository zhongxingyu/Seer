 package jothello;
 
 import java.awt.Point;
 
 public class MultiMatchMain {
 	public static void main(String[] args) {
 		Ai ai_1 = new Ai(Ai.MONTE_CARLO_TREE_SEARCH); // dark	
		Ai ai_2 = new Ai(Ai.MINIMAX_ALPHA_BETA); // light
 		int winner_1 = 0;
 		int tie = 0;
 		int winner_2 = 0;
 		int sim = 100;
 		for(int i=0;i<sim;i++) {
 			Jothello jothello = new Jothello();
 			do {	
 				//jothello.printBoard();
 				byte turn = jothello.getTurn();		
 				Point p = null;
 				if(turn == State.DARK) {
 					p = ai_1.selectMove(jothello);
 				}else{
 					p = ai_2.selectMove(jothello);
 				}					
 				jothello.putPiece(p.y, p.x);							
 			}while(jothello.whoWin() == State.NONE);			
 			if(jothello.whoWin() == State.DARK) 
 				winner_1++;
 			else if(jothello.whoWin() == State.LIGHT)
 				winner_2++;
 			else if(jothello.whoWin() == State.TIE)
 				tie++;	
 			System.out.println(winner_1 + " " + tie + " " + winner_2);
 		}
 		System.out.println("final :" + winner_1 + " " + tie + " " + winner_2);
 	}
 }
