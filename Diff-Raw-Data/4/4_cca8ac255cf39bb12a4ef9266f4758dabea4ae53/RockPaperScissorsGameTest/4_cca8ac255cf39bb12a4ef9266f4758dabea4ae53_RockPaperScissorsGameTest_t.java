 package gr.zapantis.rockpaperscissors;
 
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
 
 public class RockPaperScissorsGameTest {
 	
 	@Test
 	public void createGame() {
		RockPaperScissorsGame rockPaperScissorsGame = new RockPaperScissorsGame();
		assertNotNull(rockPaperScissorsGame);
 	}
 
 }
