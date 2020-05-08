 package com.ouchadam.podcast;
 
 import com.ouchadam.podcast.util.FeedParserHelper;
 import org.junit.Test;
 
 import static org.junit.Assert.assertNotNull;
 
 public class PodcastParserShould {
 
     @Test
     public void shouldReadRes() {
 
        assertNotNull(new FeedParserHelper().getArticle());

     }
 
 }
