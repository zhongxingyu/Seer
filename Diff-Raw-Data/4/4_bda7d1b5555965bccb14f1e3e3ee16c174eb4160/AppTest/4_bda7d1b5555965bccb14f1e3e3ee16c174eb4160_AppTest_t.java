 package org.zoneproject.extractor.plugin.inseegeo;
 
 import java.util.ArrayList;
 import java.util.List;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.zoneproject.extractor.plugin.opencalais.openCalaisExtractor;
 import org.zoneproject.extractor.utils.Prop;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest 
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public AppTest( String testName )
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
         return new TestSuite( AppTest.class );
     }
 
 
     @org.junit.Test
     public void testApp1() {
         System.out.println("getCitiesResult");
         String city = "Toulon";
         ArrayList<Prop> expResult = new ArrayList<Prop>();
         expResult.add(new Prop("http://rdf.insee.fr/geo/Arrondissement","http://rdf.insee.fr/geo/2011/ARR_832",false));
         expResult.add(new Prop("http://rdf.insee.fr/geo/Pseudo-canton","http://rdf.insee.fr/geo/2011/CAN_8399",false));
         expResult.add(new Prop("http://rdf.insee.fr/geo/Departement","http://rdf.insee.fr/geo/2011/DEP_83",false));
         expResult.add(new Prop("http://rdf.insee.fr/geo/Region","http://rdf.insee.fr/geo/2011/REG_93",false));
         expResult.add(new Prop("http://rdf.insee.fr/geo/Pays","http://rdf.insee.fr/geo/2011/PAYS_FR",false));
         ArrayList<Prop> result = InseeSparqlRequest.getDimensions(city);
 
         assertEquals(result.size(), expResult.size());
        for(Prop p: expResult){
            assertTrue(result.contains(p));
        }
     }
 }
