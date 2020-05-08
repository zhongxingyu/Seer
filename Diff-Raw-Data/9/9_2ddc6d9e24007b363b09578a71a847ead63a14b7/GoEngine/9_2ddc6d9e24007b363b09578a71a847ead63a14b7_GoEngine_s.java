 import javax.swing.*;
 import java.awt.*;
 //import java.awt.geom.*;
 import java.util.Scanner;
 
 public class GoEngine {
 	public DisplayArea display;
 	public Board board;
 	private Player playerBlack;
 	private Player playerWhite;
 	public static int turnCount;
 
 	public Scanner in = new Scanner(System.in);
 
 	public static void main(String[] args) {
 		GoEngine engine = new GoEngine();
 		engine.createWindow();
 		engine.playGo();
 		turnCount = 1;
 	}
 
 	public GoEngine() {
 		playerBlack = new RandomAI("Black", Intersection.Piece.BLACK);
 		playerWhite = new TerritoryDenialAI("White", Intersection.Piece.WHITE);
 		board = new Board();
 		display = new DisplayArea();
 		display.setBoard(board);
 
 	}
 
 	// Set up the window
 	public void createWindow() {
 		JFrame frame = new JFrame("GoEngine");
 		frame.setBackground(Color.white);
 		frame.setSize(500, 500);
 		frame.setContentPane(display);
 		frame.addWindowListener(new ExitListener());
 		frame.setVisible(true);
 	}
 
 	// Main game loop
 	public void playGo() {
 		while (true) { // TODO This will need termination eventually, but
 						// players can't pass yet
 			//System.out.println("b");
 			playerBlack.promptMove(this);
 			turnCount++;
 			display.repaint();
 			//System.out.println("w");
 			playerWhite.promptMove(this);
 			turnCount++;
 			display.repaint();
 			board.calculateScores(playerBlack, playerWhite);
 			System.out.println("B:"+playerBlack.score+" W:"+playerWhite.score);
 		}
 	}
 }
