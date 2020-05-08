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
        double totalPre = total(doc);
		cutAllEmployees(doc);
         new File("outputs").mkdir();
         saveDocument(doc,output);
        Document cutDoc = loadDocument(output);
        double total = total(cutDoc);
        assertEquals(totalPre / 2.0, total, 0);
     }
 
 }
