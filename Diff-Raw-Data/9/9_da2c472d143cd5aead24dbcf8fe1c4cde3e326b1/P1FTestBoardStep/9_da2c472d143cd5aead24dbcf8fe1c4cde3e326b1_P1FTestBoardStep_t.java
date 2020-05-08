 package fr.xebia.training.jbehave.part1.finished;
 
 import junit.framework.Assert;
 
 import org.jbehave.core.annotations.Given;
 import org.jbehave.core.annotations.Then;
 import org.jbehave.core.annotations.When;
 
 import fr.xebia.training.tdd.gameoflife.Board;
 import fr.xebia.training.tdd.gameoflife.Coordinate;
 import fr.xebia.training.tdd.gameoflife.Universe;
 
 public class P1FTestBoardStep {
 
 	private Universe board;
 
 	@Given("an empty board of size $size") 
 	public void givenAGame(int size) {
 		Coordinate[] initialState = new Coordinate[]{
                 new Coordinate(0, 0)
         };
 		board = new Board(size,  initialState );
 	}
 	
	@When("we play the first round")
 	public void whenPlayARound() {
 		board.update();
 	}
 	
 	@Then("no cell is alive")
 	public void thenNoCellIsAlive() {
 		Assert.assertEquals(0, board.liveCellsCount());
 	}
 	
 	
 }
