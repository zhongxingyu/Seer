 package org.softlang.company.tests;
 
 import static org.softlang.company.features.Cut.*;
 import static org.softlang.company.features.Total.*;
 import static org.softlang.company.features.Serialization.*;
 
 import org.w3c.dom.Document;
 
 import java.io.File;
 
import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class SerializationTest {
 
     private static String sampleCompany =
         "inputs"
         + File.separator
         + "sampleCompany.xml";
     private static String output =
         "outputs"
         + File.separator
         + "output.xml";
 
     @Test
     public void testDeserialization() throws Exception {
         loadDocument(sampleCompany);
     }
 
     @Test
     public void testSerialization() throws Exception {
         Document doc = loadDocument(sampleCompany);
         new File("outputs").mkdir();
         saveDocument(doc,output);
         Document loadedDoc = loadDocument(output);
        double total = total(loadedDoc);
        assertEquals(399747, total, 0);
     }
 
 }
