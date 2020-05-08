import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 

 public class AlienShotTest {
 
 	
 	AlienShot as;
 	BattleFieldElement element;
 	
 	@Before
 	public void init(){
 		as = new AlienShot(2,1);
 		element = new AlienShot(3,1);
 	}
 	@Test
 	public void testMove() {
		Assert.assertEquals("at the start x must be: 1", 1, element.getX());
		Assert.assertEquals("at the start x must be: 3", 3, element.getY());
 		element.move(4,5);
		Assert.assertEquals("After move x must be: 1", 1, element.getX());
		Assert.assertEquals("After move x must be: 4", 4, element.getY());
 	}
 
 	@Test
 	public void testGetXOffset() {
		Assert.assertEquals("X offset can be 0 in Empty", 0, as.getXOffset());
 	}
 
 	@Test
 	public void testGetYOffset() {
 		BattleField bf = null;
 		try {
 			bf = new BattleField("es-in.txt");
 		} catch (IllegalElementException e) {
 			e.printStackTrace();
 		} catch (IllegalPositionException e) {
 			e.printStackTrace();
 		}
 		bf.setRows(2);
		Assert.assertEquals("Verify is return is correct", 2-(2+1), as.getYOffset());
 	}
 
 	@Test
 	public void testToString() {
		Assert.assertEquals("String returned must be: S", "S", as.toString());
 	}
 
 	@Test
 	public void testAlienShot() {
		Assert.assertEquals("The v must be: 3", 3, element.getY());
		Assert.assertEquals("The h must be: 1", 1, element.getX());
 		AlienShot as = new AlienShot(2,1);
		Assert.assertEquals("The moved must be 0 after new declaration of AlienShot", 0, as.getMoved());
 	}
 
 	@Test
 	public void testGetSetMoved() {
 		as.setMoved(10);
		Assert.assertEquals("The moved must be 10", 10, as.getMoved());
 	}
 
 }
