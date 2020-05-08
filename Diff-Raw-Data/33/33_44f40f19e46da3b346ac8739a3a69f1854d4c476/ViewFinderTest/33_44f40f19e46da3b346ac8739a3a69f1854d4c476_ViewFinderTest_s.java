 package org.thoughtworks.orc.internal.util;
 
import org.testng.annotations.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class ViewFinderTest {
     @Test
     public void test_read_template() throws Exception {
         final String test = new ViewFinder("/Users/twer/w3/wheel/wheel_MVC/src/test/resources/views/", "mst").readTemplate(new NamePair("some", "test"));
         assertEquals(test, "{{hello}}");
     }
 
 }
