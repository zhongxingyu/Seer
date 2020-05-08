 package ch9k.plugins.liteanalyzer;
 
 import ch9k.core.settings.Settings;
 import ch9k.plugins.TextAnalyzer;
 import ch9k.plugins.TextAnalyzerTester;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class LiteTextAnalyzerTest {    
     /**
      * Test of getSubject method, of class LiteTextAnalyzer.
      */
     @Test
     public void testGetSubject() {
        TextAnalyzer analyzer =
                new LiteTextAnalyzer(null, null, new Settings());
         String[] messages = {
             "Mushroom Mushroom Mushroom",
             "Badger Badger",
             "The a The a The a The a"
         };
         String[] subjects = analyzer.getSubjects(messages);
         assertEquals("mushroom", subjects[0]);
         assertEquals("badger", subjects[1]);
         assertEquals(2, subjects.length);
     }
 
     /**
      * Test of getFrequencyMap method, of class LiteTextAnalyzer.
      */
     @Test
     public void testGetFrequencyMap() {
         TextAnalyzerTester tester = new TextAnalyzerTester();
         tester.testGetFrequencyMap(
                 new LiteTextAnalyzer(null, null, new Settings()));
     }
 }
