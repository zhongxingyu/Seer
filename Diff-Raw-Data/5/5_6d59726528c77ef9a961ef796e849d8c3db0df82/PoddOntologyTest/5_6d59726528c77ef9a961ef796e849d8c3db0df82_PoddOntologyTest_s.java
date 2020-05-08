 /**
  * 
  */
 package com.github.podd.ontology.test;
 
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 
 /**
 * Parameterized test to validate that PODD ontologies.
  * 
  * @author kutila
  * 
  */
 @Ignore
 @RunWith(value = Parameterized.class)
 public class PoddOntologyTest
 {
     
     // ----------- parameters for junit test----------------
     private String filename;
     private String mimeType;
     private int statementCount;
     
     /**
      * Parameterized constructor
      * 
      * @param number
      */
     public PoddOntologyTest(final String filename, final String mimeType, final int statementCount)
     {
         this.filename = filename;
         this.mimeType = mimeType;
         this.statementCount = statementCount;
     }
     
     @Parameters
     public static Collection<Object[]> data()
     {
         final Object[][] data =
                 new Object[][] { 
                        { "/ontologies/dcTerms.owl", "application/rdf+xml", 29 },
                         { "/ontologies/poddBase.owl", "application/rdf+xml", 284 },
                         { "/ontologies/poddUser.owl", "application/rdf+xml", 225 }, 
                         { "/ontologies/poddScience.owl", "application/rdf+xml", 1124 },
                         { "/ontologies/poddPlant.owl", "application/rdf+xml", 77 },
                         { "/ontologies/poddAnimal.owl", "application/rdf+xml", 171 }, 
                 };
         return Arrays.asList(data);
     }
     
     /**
      * Test the ontology can be parsed into RDF and contains the expected
      * number of statements. 
      */
     @Test
     public void testParseOntology() throws Exception
     {
         final InputStream inputStream = this.getClass().getResourceAsStream(this.filename);
         Assert.assertNotNull("Null resource " + this.filename, inputStream);
         
         final RDFFormat format = RDFFormat.forMIMEType(this.mimeType);
         final RDFParser rdfParser = Rio.createParser(format);
         final StatementCollector collector = new StatementCollector();
         rdfParser.setRDFHandler(collector);
         
         rdfParser.parse(inputStream, "http://purl.org/podd/ns/XYZ");
         
         Assert.assertEquals("Incorrect number of statements for " + this.filename, this.statementCount, collector
                 .getStatements().size());
     }
     
 }
