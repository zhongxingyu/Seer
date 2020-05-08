 package dcll.jfri.projetConvertisseur;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 
 public class MenuTest {
 	
 	protected Menu menu;
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() {
 		menu = new Menu();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testCheminSource() throws Exception {
 		assertEquals(null, menu.getCheminSource());
 	}
 	
 	@Test
 	public void testAction() throws Exception {
 		assertEquals(-1, menu.getAction());
 	}
 	
 	@Test
 	public void testCheminResultat() throws Exception {
 		assertEquals(null, menu.getCheminResultat());
 	}
 
 }
