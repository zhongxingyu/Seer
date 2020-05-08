 package herbstJennrichLehmannRitter.tests.model;
 
 import static org.junit.Assert.assertEquals;
 import herbstJennrichLehmannRitter.engine.model.Player;
 import herbstJennrichLehmannRitter.engine.model.impl.DataImpl;
 import herbstJennrichLehmannRitter.engine.model.impl.PlayerImpl;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DataTests {
 	
 	private Player player = new PlayerImpl();
 	private DataImpl data = new DataImpl();
 	
 	@Before
 	public void before() {
 		this.player = new PlayerImpl();
 		this.data = new DataImpl();
 	}
 	
 	@Test
 	public void testOwnPlayer() {
 		this.data.setOwnPlayer(this.player);
		assertEquals(data.getOwnPlayer(), this.player);
 	}
 	
 	@Test
 	public void testEnemyPlayer() {
		data.setEnemyPlayer(this.player);
		assertEquals(data.getEnemyPlayer(), this.player);
 	}
 
 }
