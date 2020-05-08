 package com.ouchadam.podcast;
 
 import com.ouchadam.podcast.util.FeedParserHelper;
 import org.junit.Test;
import org.w3c.dom.Document;
 
 import static org.junit.Assert.assertNotNull;
 
 public class PodcastParserShould {
 
     @Test
     public void shouldReadRes() {
        Document doc = new FeedParserHelper().getDocFromInput();
 
        assertNotNull(doc);
     }
 
 }
