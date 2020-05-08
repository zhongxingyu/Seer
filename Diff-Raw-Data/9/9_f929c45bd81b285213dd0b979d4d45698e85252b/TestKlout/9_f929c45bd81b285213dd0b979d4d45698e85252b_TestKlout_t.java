 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jKlout2;
 
 import java.io.IOException;
 import java.io.InputStream;
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class TestKlout {
 
     @Test
     public void testShowUser() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jKlout2/showUser.json");
         String myJson = IOUtils.toString(is, "UTF-8");
         Assert.assertNotNull("Could not read json file", myJson);
     }
     
 }
