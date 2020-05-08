 package edina.eframework.gefcdemo.controllers.test;
 
import static org.junit.Assert.assertEquals;

 import org.junit.Test;
 
 import edina.eframework.gefcdemo.controllers.MapController;
 
 public class MapControllerTest {
 
   @Test
   public void testHandleMap() {
     MapController mapController = new MapController();
     String viewName = mapController.handleMap();
     assertEquals( "Unexpected view name returned from MapController.",
                   "gefcdemo", viewName ); 
   }
 }
