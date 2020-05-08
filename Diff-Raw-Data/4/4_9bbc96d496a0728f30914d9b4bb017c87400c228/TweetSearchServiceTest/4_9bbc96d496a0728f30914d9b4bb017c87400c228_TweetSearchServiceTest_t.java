 package de.hypoport.twitterwall.twitter;
 
 import de.hypoport.twitterwall.config.TwitterConfiguration;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.TwitterException;
 
import java.util.logging.Logger;

 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Mockito.mock;
 
 
 @Test
 public class TweetSearchServiceTest {
 
   private TwitterService tweetSearchService;
 
   @BeforeMethod
   public void setup() {
     TwitterService.logger = mock(Logger.class);
 
     tweetSearchService = new TwitterService();
     tweetSearchService.configuration = mock(TwitterConfiguration.class);
   }
 
   @Test (enabled = false)
   public void simple_Suche_mit_ConsumerKey_und_ConsumerSecret_auth() throws TwitterException {
 
     QueryResult queryResult = tweetSearchService.searchTweets(new Query("html5"));
 
     assertThat(queryResult).isNotNull();
     assertThat(queryResult.getTweets()).isNotEmpty();
   }
 }
