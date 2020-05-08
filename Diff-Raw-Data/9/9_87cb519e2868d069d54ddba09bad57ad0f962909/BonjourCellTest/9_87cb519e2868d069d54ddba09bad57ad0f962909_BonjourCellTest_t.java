 package IUT.BoBot.SmartCells;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class BonjourCellTest {
 
 	@Test
 	public void testBonjour() {
		BonjourCell cell = new BonjourCell("Bonjour");
 		assertEquals("Bonjour!", cell.answer());
 	}
 	
 	@Test
 	public void testNotBonjour() {
 		BonjourCell cell = new BonjourCell("au revoir");
 		assertEquals(null, cell.answer());
 	}
 
 }
