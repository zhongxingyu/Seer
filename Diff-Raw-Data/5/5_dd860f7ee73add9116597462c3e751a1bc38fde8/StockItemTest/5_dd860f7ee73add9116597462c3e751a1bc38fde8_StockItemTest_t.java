 package ee.ut.math.tvt.BSS;
 
import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 
 public class StockItemTest {
 	private StockItem item1;
 	@Before
 	public void setUp() {
 		long id = 10;
 		item1 = new StockItem(id, "testItem", "testDescription", 12.0, 5);
 	}
 	
 	@Test
 	public void testClone() {
 		StockItem item2 = (StockItem) item1.clone();
 	    assertEquals(item2.getId(), 10, 0.0001);
 	    assertEquals(item2.getName(), "testItem");
 	    assertEquals(item2.getPrice(), 12.0, 0.001);
 	    assertEquals(item2.getQuantity(), 5);
 	    assertEquals(item2.getDescription(), "testDescription");
 	}
 	
 	@Test
 	public void testGetColumn() {
		long id = ((Long) item1.getColumn(0)).longValue();
 	    assertEquals(id, 10, 0.0001);
 	    String name = (String) item1.getColumn(1);
 	    assertEquals(name, "testItem");
 	    double price = (double) item1.getColumn(2);
 	    assertEquals(price, 12.0, 0.001);
 	    int quantity = (int) item1.getColumn(3);
 	    assertEquals(quantity, 5);
 	}
 	@Test (expected = RuntimeException.class) 
 	public void testGetColumnException() {
 	    String other = (String) item1.getColumn(4);
 	}
 
 }
