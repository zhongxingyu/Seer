 package net.straininfo2.grs.idloader.bioproject.xmlparsing;
 
 import net.straininfo2.grs.idloader.bioproject.bindings.TypePackage;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
 
 public class DocumentChunkerTest {
 
     public static URL getBioProjectUrl() {
         return DocumentChunkerTest.class.getClassLoader().getResource("bioproject.xml");
     }
 
     /**
      * Utility function that reads the bioproject XML into binding objects.
      */
     public static List<TypePackage> parseBioProjectFile() throws
             JAXBException, SAXException, ParserConfigurationException, IOException {
 
         final List<TypePackage> packageList = new LinkedList<>();
         PackageProcessor processor = new PackageProcessor() {
             @Override
             public void processPackage(TypePackage nextPackage) {
                 packageList.add(nextPackage);
             }
 
             @Override
             public void endParsing() {
             }
         };
         URL xmlUrl = getBioProjectUrl();
         if (xmlUrl == null) {
             throw new RuntimeException("Unable to fetch bioproject.xml");
         }
         else {
             DocumentChunker.parseXmlFile(xmlUrl, processor);
             return packageList;
         }
     }
 
     /**
      * Pass a bioproject XML file to chunker, checks if chunking works.
      */
     @Test
     public void parseBioProjectXml() throws Exception {
         assertEquals(4, parseBioProjectFile().size());
     }
 
     private static class ProcessEnd implements PackageProcessor {
 
         private boolean done = false;
 
         @Override
         public void processPackage(TypePackage nextPackage) {
         }
 
         @Override
         public void endParsing() {
             done = true;
         }
 
         public boolean getDone() {
             return done;
         }
     }
     @Test
     public void testEndOfParsing() throws ParserConfigurationException, IOException, SAXException, JAXBException {
         ProcessEnd processor = new ProcessEnd();
         DocumentChunker.parseXmlFile(getBioProjectUrl(), processor);
         assertTrue(processor.getDone());
     }
 
 }
