 package generalChess;
 
 import sharedfiles.Board;
 import sharedfiles.Piece;
 
 import java.awt.Point;
 import java.util.ArrayList;
 public class maxTestClass {
 	public static void main(String[] args) {
 		v1Bobby a=new v1Bobby(new Board(), true);
 		
 		
 		
		a.move(3,7,3,0);
		a.move(3,1,7,3);
		a.move(7,0,3,7);
		a.move(4,1,7,4);
 		a.printBoard();
 		v1Bobby enemy=new v1Bobby(a.getB(), false);
		System.out.println(enemy.numDefenders(3,0));
		System.out.println(enemy.kMoves());
		System.out.println(enemy.kMoves().get(0).size());
		System.out.println(enemy.takeIfPossible(enemy.kMoves()));
		enemy.stopCheck();
 		a.getB();
 		enemy.printBoard();
 		
 		
 		
 		/*
 		System.out.println(enemy.numDefenderValue(5,5));
 		System.out.println(a.numDefenderValue(5,5));
 		a.randomMove();
 		//a.move(4,7,4,3);
 		
 		
 		a.printBoard();
 		a.bestPieceGetOutOfDanger();
 		
 		//a.move(7,7,3,4);
 		a.printBoard();
 		a.newBestPieceToTake();
 		a.printBoard();
 		
 		a.printBoard();
 		System.out.println(a.takeIfPossible(a.allMoves().get(0)));
 		
 		a.printBoard();
 		System.out.println(a.numDefenders(1,4));
 		System.out.println(a.numDefenderValue(1,4));
 		System.out.println(a.bMovesDefending());
 		a.randomMove();
 		a.printBoard();
 		a.randomMove();
 		v1Bobby x=new v1Bobby(a.getB(), false);
 		x.move(4,1,4,2);
 		x.printBoard();
 		System.out.println(a.takeIfPossible(a.allMoves().get(15)));
 		System.out.println(x.numDefenders(4, 2));
 	
 		
 		System.out.println(a.allMoves());
 		*/
 		}
 	
 }
