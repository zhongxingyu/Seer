 package com.ouchadam.podcast.util;
 
 import com.ouchadam.podcast.Message;
 import com.ouchadam.podcast.parser.FeedParserFactory;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import static org.junit.Assert.fail;
 
 public class FeedParserHelper {
 
     private static final String TEST_FILE_PATH = "core/src/main/res/";
 
     private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
     private final DocumentBuilder builder;
 
     public FeedParserHelper() {
         try {
             builder = FACTORY.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             throw new RuntimeException("ParserConfigurationException");
         }
     }
 
     public List<Message> getArticle() {
         try {
             return FeedParserFactory.getParser(builder.parse(createFile()));
         } catch (SAXException e) {
             fail(e.getMessage());
         } catch (IOException e) {
             fail(e.getMessage());
         }
         fail("Parser failed");
         return null;
     }
 
     private File createFile() {
         return new File((TEST_FILE_PATH + "stuff_you_should_know.rss").toString());
     }
 
 }
