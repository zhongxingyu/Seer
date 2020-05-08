 package beans;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.primefaces.model.map.LatLng;
 
 public class TestMapBean {
 
 	@Test
 	public void testMapBean() {
 		MapBean map = new MapBean();
 
 		assertEquals(new LatLng(41.381542, 2.122893), map.getCenter());
 		assertEquals(600, map.getWidth());
 		assertEquals(400, map.getHeight());
 		assertEquals(15, map.getZoom());
 	}
 
 }
