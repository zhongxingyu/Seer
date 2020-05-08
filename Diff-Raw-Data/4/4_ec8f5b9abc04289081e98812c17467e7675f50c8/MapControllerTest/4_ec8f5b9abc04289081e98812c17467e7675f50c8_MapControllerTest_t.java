 package edina.eframework.gefcdemo.controllers.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.MalformedURLException;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import edina.eframework.gefcdemo.controllers.MapController;
import edina.eframework.gefcdemo.domain.ConfigParameters;
 import edina.eframework.gefcdemo.domain.SoilErosionWps;
 import edina.eframework.gefcdemo.helpers.MockServletContextWebContextLoader;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = MockServletContextWebContextLoader.class,
                       locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml",
                                    "file:src/main/webapp/WEB-INF/gefcdemo-servlet.xml"})
 public class MapControllerTest {
 
   @Autowired
   private MapController mapController;
   
   @Test
   public void testHandleMap() throws MalformedURLException {
    String viewName = mapController.handleMap( new SoilErosionWps(), new ConfigParameters() );
     assertEquals( "Unexpected view name returned from MapController.",
                   "gefcdemo", viewName );
   }
 }
