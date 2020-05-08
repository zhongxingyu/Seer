 package de.strud.xmlparser;
 
 import de.strud.data.Document;
 import junit.framework.Assert;
 import org.junit.Test;
 
 import java.util.List;
 
 /**
  * User: strud
  */
 public class XMLParserTest {
 
     @Test
     public void testParsing() throws Exception {
        XMLParser parser = new XMLParser(this.getClass().getClassLoader().getResourceAsStream("enwiki_short.xml"));
         Assert.assertTrue("Parser should have entries.", !parser.isRead());
         List<Document> docs = parser.parse(1);
         Assert.assertNotNull("Documents cannot be null.", docs);
         Assert.assertTrue("Documents should have length > 0.", docs.size() > 0);
         Assert.assertNotNull("Text cannot be null.", docs.get(0).getText());
         Assert.assertNotNull("Title cannot be null.", docs.get(0).getTitle());
         Assert.assertNotNull("URL cannot be null.", docs.get(0).getUrl());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testParsingWithNull() throws Exception {
        new XMLParser(null);
     }
 }
