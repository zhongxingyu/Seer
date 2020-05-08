 package player;
 import mixmeta4.*;
 import game.EricNSRD;
 import game.EricNSRM;
 import game.EricNSRT;
 import game.EricSRA;
 import game.EricSUA;
 
 import java.awt.*;
 import java.util.*;
 import samplePlayers.*;
 
 /**
 * This is the main class for initiating games between two players. You can specify the players, 
  * board configuration, type of game, starting clock time, time per move, and output type.
  * (For multiple players, use PlayCF.)
  */
 public class Play {
   
   public static void main(String[] args) {
 
     Player red = new EricSUA(true);
 //    Player red = new Hal(true);
     Player black = new Hal(false);
     
     int redwins = 0;
     int blackwins = 0;
     
     String setup =""+
       "..wnqknw..\n"+
       "._epepep_.\n"+
       "__________\n"+
       "__..__..__\n"+
       "__________\n"+
       "._epepep_.\n"+
       "..wnqknw..\n";
 
 
     //    Game game = new Game("Finish First",setup,red,black);
     //    Game game = new Game("Finish First",setup,red,black,60,10);
     //    Game game = new Game("TakeAll",setup,red,black);
 	
 	
 	//	Game game = new Game("CheX",setup,red,black, 100, 12, "terminal");
 
 	// the following runs game automatically without using gui
 	Game game = new Game("CheX",setup,red,black, 60, 6, "terminal");
	for(int x = 0; x < 1; x++) {
 		
 		while (!game.gameIsOver()) game.step();
 		if(game.blackWon()) {
 			blackwins++;
 		}
 		if(game.redWon()) {
 			redwins++;
 		}
 		game = new Game("CheX",setup,red,black, 60, 6, "terminal");
 		System.out.printf(red.name + " won: %d\n" + black.name + " won: %d", redwins, blackwins);
 	}
 	
 	System.out.printf(red.name + " won: %d\n" + black.name + " won: %d", redwins, blackwins);
 	
 
  }
 }
