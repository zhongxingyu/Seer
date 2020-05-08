 package herbstJennrichLehmannRitter.tests.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import herbstJennrichLehmannRitter.engine.exception.EngineCouldNotStartException;
 import herbstJennrichLehmannRitter.engine.factory.GameCardFactory;
 import herbstJennrichLehmannRitter.engine.factory.impl.GameCardFactoryImpl;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Player;
 import herbstJennrichLehmannRitter.engine.model.impl.PlayerImpl;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class ComplexCardTests {
 	
 	private GameCardFactory gameCardFactory;
 	
 	@Before
 	public void before() {
 		try {
 			this.gameCardFactory = new GameCardFactoryImpl();
 		} catch (EngineCouldNotStartException e) {
 			fail(e.getLocalizedMessage());
 		}
 	}
 
 	@Test
 	public void testAuferstehung() {
 		Card auferstehung = this.gameCardFactory.createCard("Auferstehung");
 		Player player1 = new PlayerImpl();
 		Player player2 = new PlayerImpl();
 		
 		auferstehung.getComplexCardAction().applyActionOnPlayer(player1, player2);
 		
 		assertEquals(32, player1.getTower().getActualPoints());
 	}
 	
 	@Test
 	public void testDieb() {
 		Card dieb = this.gameCardFactory.createCard("Dieb");
 		Player player1 = new PlayerImpl();
 		Player player2 = new PlayerImpl();
 		
 		dieb.getComplexCardAction().applyActionOnPlayer(player1, player2);
 		
 		assertEquals(20, player1.getMagicLab().getStock());
 		assertEquals(18, player1.getMine().getStock());
 		assertEquals(5, player2.getMagicLab().getStock());
 		assertEquals(10, player2.getMine().getStock());
 	}
 }
