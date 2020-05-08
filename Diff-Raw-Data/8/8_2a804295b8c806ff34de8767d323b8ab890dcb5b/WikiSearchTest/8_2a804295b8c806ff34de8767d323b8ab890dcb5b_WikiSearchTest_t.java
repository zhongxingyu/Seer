 import edu.gwu.raminfar.iauthor.Utils;
import edu.gwu.raminfar.iauthor.core.Sentence;
 import edu.gwu.raminfar.iauthor.nlp.NlpService;
 import edu.gwu.raminfar.wiki.WikiPage;
 import edu.gwu.raminfar.wiki.WikiSearch;
 import org.junit.Test;
 
 import java.io.IOException;
 
 /**
  * Author: Amir Raminfar
  * Date: Oct 21, 2010
  */
 public class WikiSearchTest {
 
     @Test
     public void middleEast() throws IOException {
         WikiSearch search = new WikiSearch("middle east");
         for (WikiPage page : search.parseResults()) {
             System.out.println(page.getUrl() + ": " + page.content());
         }
     }
 
     @Test
     public void computerScience() throws IOException {
         WikiSearch search = new WikiSearch("Computer Science");
         WikiPage first = search.parseResults().iterator().next();
        for (String s : NlpService.detectedSentences(first.content())) {
            Sentence sentence = NlpService.tagSentence(s);
            System.out.println(s);
            System.out.println(sentence);
        }
     }
 }
