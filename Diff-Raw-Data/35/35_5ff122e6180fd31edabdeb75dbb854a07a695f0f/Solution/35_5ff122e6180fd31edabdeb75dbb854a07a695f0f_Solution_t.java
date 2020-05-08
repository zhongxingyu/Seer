 package tictactoeai;
 
 import java.io.*;
 import java.util.*;
 
 public class Solution {
 	public static void main(String[] args) {
 		Scanner s = new Scanner(System.in);
 		String minimaxVer = s.nextLine();
 		String opponentVer = s.nextLine();
 		StringBuilder log = new StringBuilder();
 		Board board = new Board();
 		Player opp, minimax;
 		if(minimaxVer.equals("win")){
 			minimax = new MinimaxWin();
 		}
 		else{
 			minimax = new MinimaxTie();
 		}
 		if(opponentVer=="simple"){
 			opp = new SimplePlayer(2);
 		}
 		else{
 			opp = new HumanPlayer(2);
 		}
 		String state,commentary;
 		while(!board.isOver()){
 			minimax.makeMove(board);
 			commentary = "----------------\n";
 			//commentary += "Player "+minimax.getType()+" adds a X to position ";
 			System.out.println(commentary);
 			state = board.toString();
 			System.out.println(state);
			if(board.isOver()){
				break;
			}
 			System.out.println(commentary);
 			log.append(state);
 			opp.makeMove(board);
 			state = board.toString();
 			System.out.println(state);
 		}
 		if(board.isOver())
 			System.out.println("is Over!");
 		try{
 			// Create file
 			String playerType1 = minimax.getType();
 			String playerType2 = opp.getType();
 			FileWriter fstream = new FileWriter("log_"+playerType1+"_vs_+"+playerType2+".txt");
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(log.toString());
 			
 			//Close the output stream
 			out.close();
 		}catch (Exception e){//Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 }
