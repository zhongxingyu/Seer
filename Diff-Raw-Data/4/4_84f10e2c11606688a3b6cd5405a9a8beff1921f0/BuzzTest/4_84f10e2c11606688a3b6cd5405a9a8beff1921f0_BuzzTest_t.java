 package com.google.buzz;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.Assert;
 import com.google.buzz.Buzz;
 import com.google.buzz.model.BuzzContent;
 import com.google.buzz.model.BuzzFeed;
 import com.google.buzz.model.BuzzFeedEntry;
 
 
 
 public class BuzzTest
 {
 
 
  @Before public void initBuzzClient()
                           throws Exception
  {
    if (buzz==null) {
      buzz = new Buzz();
      buzz.setConsumerForScope(Config.buzzConsumerKey,
                               Config.buzzConsumerSecret,
                               Buzz.BUZZ_SCOPE_WRITE);
      buzz.setTokenWithSecret(Config.buzzToken, Config.buzzTokenSecret);
    }
  }
 
  @Test public void testPublish() throws Exception
  {
    String userId="@me";
    BuzzContent buzzContent = new BuzzContent();
    buzzContent.setText("test message");
    buzzContent.setType("text/plain");
    BuzzFeedEntry post = buzz.createPost(userId,buzzContent,null);
    Assert.assertTrue(post!=null);
  }
 
 
/*
 *   see http://code.google.com/p/google-buzz-api/issues/detail?id=134
  @Test public void testReadAndReshare() throws Exception
  {
    BuzzFeed feed = buzz.getPosts("@me",BuzzFeed.Type.CONSUMPTION);
    if (feed.getEntries().size() == 0) {
     System.err.println("No posts to reshare");
    } else {
     BuzzFeedEntry feedEntry = feed.getEntries().get(0);
     BuzzFeedEntry reshareEntry = buzz.resharePost("@me",
                                         feedEntry.getId(),"reshare message");
     Assert.assertTrue(reshareEntry!=null);
    }
  }
*/
 
  private Buzz buzz=null;
 
 }
