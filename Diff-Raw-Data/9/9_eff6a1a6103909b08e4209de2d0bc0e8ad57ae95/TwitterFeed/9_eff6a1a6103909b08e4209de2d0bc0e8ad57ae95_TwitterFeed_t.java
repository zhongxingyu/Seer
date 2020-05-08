 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hackeurope.feedspeak;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.lang.StringUtils;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.User;
 import twitter4j.conf.ConfigurationBuilder;
 import twitter4j.*;
 import twitter4j.api.*;
 
 /**
  *
  * @author Calum
  */
 public class TwitterFeed {
     
     private static void initLogFile() {
         try {
             Handler fileHandler = new FileHandler("/tmp/log");
             Logger.getLogger("").addHandler(fileHandler);
 
         } catch (IOException ex) {
             Logger.getLogger(TwitterFeed.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(TwitterFeed.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public static List<String> getTweets(){
         initLogFile();
         Logger.getLogger(TwitterFeed.class.getName()).log(Level.INFO, "getting tweets");
         
         ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true)
         .setOAuthConsumerKey("aTR1FAEsR0hAj9w47ko9Tg")
         .setOAuthConsumerSecret("XDSn1TTobWDBy46gZAfm6ya2kYkmli30B2vD2ixxpMA")
         .setOAuthAccessToken("1110145321-wYAlo6r3Oms2gHMlK8yRyqfPTd93oWCXqbU4M8C")
         .setOAuthAccessTokenSecret("WUlVHwaAis8eln16vWSajb4yTsPo0vaGs0PjgHmUo");
         
         TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
         
         List<String> packagedStatus = new LinkedList<String>();
         try {
             User user = twitter.verifyCredentials();
             
             List<Status> statuses = twitter.getHomeTimeline();
             
             for (Status status : statuses) {
                 String currentStatus = status.getText();
                 
                 String encodedStatus = speechEncodeTweet(currentStatus);
                 packagedStatus.add(status.getUser().getScreenName() + " says: " + encodedStatus);
             }
 
         } catch (TwitterException ex) {
             Logger.getLogger(TwitterFeed.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return packagedStatus;
     }
     
     public static String speechEncodeTweet(String currentStatus)
     {
         //Replace "#" with utterable word "hashtag"
         String first = currentStatus.replace("#", "hash tag ");
         
         //Replaces links with "link"
         String encodedTweet = first.replaceAll("(?:<\\w+.*?>|[^=!:'\"/]|^)((?:https?://|www\\.)[-\\w]+(?:\\.[-\\w]+)*(?::\\d+)?(?:/(?:(?:[~\\w\\+%-]|(?:[,.;@:][^\\s$]))+)?)*(?:\\?[\\w\\+%&=.;:-]+)?(?:\\#[\\w\\-\\.]*)?)(?:\\p{P}|\\s|<|$)", "link");
          
         return encodedTweet;
 //        String[] words = encodedTweet.split(" ");
 //        
 //        for (int i = 0; i< words.length; i++)
 //        {
 //            if (words[i].matches("^[\\.\\/]+$"))
 //                words[i] = "link";
 //        }
 //        for (int i = 0; i< words.length; i++)
 //        {
 //            encodedTweet += words[i] + " ";
 //        }
 //        StringUtils.
         
     }
 }
