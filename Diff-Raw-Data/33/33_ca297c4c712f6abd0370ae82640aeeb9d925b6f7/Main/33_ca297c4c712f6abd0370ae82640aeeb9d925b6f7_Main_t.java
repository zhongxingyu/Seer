 package edu.ntnu.brunson.manager;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import edu.ntnu.brunson.cards.Deck;
 import edu.ntnu.brunson.cards.Pile;
import edu.ntnu.brunson.player.Phase1Player;
 import edu.ntnu.brunson.preflop.*;
 import java.io.PrintStream;
 import java.io.File;
 
 public class Main {
 
 	private Main(){};
 	
	private void run(){
		GameManager gameManager = new GameManager();
		gameManager.addPlayer(new Phase1Player(100, 50, 25, 50));
		gameManager.addPlayer(new Phase1Player(100, 50, 25, 50));
		gameManager.addPlayer(new Phase1Player(100, 50, 25, 50));
		gameManager.addPlayer(new Phase1Player(100, 50, 25, 50));
		gameManager.playGames(5);
	}
	
 	public static void main(String[] args) {
		Main main = new Main();
		main.run();
 	}
 }
