 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import util.RichUtil;
 import models.*;
 
 public class BasicTest extends UnitTest {
 
     @Test
     public void aVeryImportantThingToTest() {
         assertEquals(2, 1 + 1);
     }
 
     @Test
     public void testMapJson() {
    	Map map = MapGenerator.generateTestMap();
    	System.out.println(RichUtil.mapToJson(map));
     }
 }
