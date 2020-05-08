 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class MineSweeperTest {
 
 	private MineSweeper mineSweeper;
 
 	@Before
 	public void setUp() {
 		mineSweeper = new MineSweeper();
 	}
 
 	@Test
 	public void revealingABoardWithoutAMineShouldWork() {
 
		String input = "0";
 		String expectedOutput = "0";
 
 		String actualOutput = mineSweeper.reveal(input);
 
 		Assert.assertEquals("Could not reveal the hints properly", expectedOutput, actualOutput);
 
 	}
 
 	@Test
 	public void revealingASingleMineShouldWork() {
 
 		String input = "*";
 		String expectedOutput = "*";
 
 		String actualOutput = mineSweeper.reveal(input);
 
 		Assert.assertEquals("Could not reveal the hints properly", expectedOutput, actualOutput);
 
 	}
 
 }
