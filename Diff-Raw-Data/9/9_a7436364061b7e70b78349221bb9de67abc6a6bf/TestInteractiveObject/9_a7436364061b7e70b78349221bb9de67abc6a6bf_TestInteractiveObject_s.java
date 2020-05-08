 package rsmg.model.object;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
import rsmg.model.Variables;
 import rsmg.model.object.item.HealthPack;
 import rsmg.model.object.item.Item;
 import rsmg.model.object.unit.PCharacter;
 
 /**
  * Tests the InteractiveObject class
  *
  */
 public class TestInteractiveObject {
 	
 	@BeforeClass
 	public static void beforeClass() {
 	}
 
 	@AfterClass
 	public static void afterClass() {
 	}
 
 	@Before
 	public void before() {
 	}
 
 	@After
 	public void after() {
 	}
 
 	@Test
 	public void testHasCollidedWith() {
         int charX = 0;
 		int notYIntersect = Variables.TILESIZE*2-Variables.CHARACTERHEIGHT-1;
 		Item i = new HealthPack(charX,notYIntersect);
 		
 		PCharacter character = new PCharacter(charX, notYIntersect, null);
 		assertTrue(i.hasCollidedWith(character));
 	}
 }
