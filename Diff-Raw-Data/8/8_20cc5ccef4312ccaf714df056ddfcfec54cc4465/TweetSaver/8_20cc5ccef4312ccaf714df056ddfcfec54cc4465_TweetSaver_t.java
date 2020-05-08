 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tweetSaver;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import twitter4j.HashtagEntity;
 import twitter4j.StallWarning;
 import twitter4j.Status;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 import twitter4j.URLEntity;
 import twitter4j.conf.ConfigurationBuilder;
 
 /**
  *
  * @author Lars
  */
 public class TweetSaver {
 
     private final Timer timer = new Timer();
     private boolean timePassed = false;
     private int timeSecondsInterval = 6;
     private Map<String, Integer> cCodes = new HashMap<>();
     private int totalAmount = 0; //counts the amount of tweets per file
     private int totalIntervalAmount = 0;
 
     /**
      * runs the methods which are necessary to collect the data
      */
     public void init() {
         startTimer();
         CollectData();
     }
 
     /**
      * Creates a new timer that sets the value timePassed to true. This task is
      * repeated in a ten-minute interval
      */
     private void startTimer() {
         System.out.println("Timer gestartet");
         timer.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                 timePassed = true;
                 System.out.println(timeSecondsInterval + " seconds passed");
             }
         }, timeSecondsInterval * 1000, timeSecondsInterval * 1000);
     }
 
     /**
      * This Method connects to the Twitterstream and saves the collected data of
      * a ten minute interval in an object of TweetInterval. This object is added
      * to a List of TweetIntervals whenever the ten minutes have passed.
      * Important: before you can use this method you have to replace the x with
      * values of your tokens and keys you get from your twitter developeraccount
      */
     private void CollectData() {
 
         /*
          * Authetication - the are x placeholders for my privat auth. keys - 
          * replace with the keys/ tokens twitter offers when creating a new
          * developer account
          */
         ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("xxx");
        cb.setOAuthConsumerSecret("xxx");
        cb.setOAuthAccessToken("xxx-xxx");
        cb.setOAuthAccessTokenSecret("xxx");
 
         //Getting in Twitter Stream..
         TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
         StatusListener listener = new StatusListener() {
             /*Counter the files that has been created to avoid naming conflicts*/
             int fileNumber = 1;
             TweetInterval ti = new TweetInterval();
             List<TweetInterval> tweetList = new LinkedList<>();
             FileSaver fileSaver = new FileSaver();
 
             @Override
             public void onStatus(Status status) {
 
                 //Array for checking the source of the Tweets
                 String source[] = new String[5];
                 source[0] = "android";
                 source[1] = "iphone";
                 source[2] = "blackberry";
                 source[3] = "windows";
                 source[4] = "other";
 
                 //add the saved tweets of one interval to the List of intervals
                 if (timePassed == true) {
                     tweetList.add(ti);
                     totalIntervalAmount++;
                     ti = new TweetInterval();
                     timePassed = false;
                     System.out.println("Tweetliste erfolgreich hinzugefügt");
                 }
 
                 //if the tweets of one day are collected save them to a json file
                 if (tweetList.size() > 5) {
                     System.out.println("Aufruf savte2json");
                     try {
                         fileSaver.saveToJson(fileNumber, tweetList, totalAmount, totalIntervalAmount);
                     } catch (IOException ex) {
                         Logger.getLogger(TweetSaver.class
                                 .getName()).log(Level.SEVERE, null, ex);
                     }
                     fileNumber++;
                     tweetList.clear();
                     totalAmount = 0;
                     totalIntervalAmount = 0;
                 }
 
                 //only save Tweets with Geolocation & Country Code information included
                 if (status.getGeoLocation() != null && status.getPlace() != null) {
 
                     //save the ID of the tweets
                     ti.setID(status.getId());
 
                     //save the username of the tweeter
                     ti.setusername(status.getUser().getScreenName());
 
                     //save the url to the tweet
                     ti.setUrl("https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId());
 
                     //save source(IPhone,Android..) 
                     for (int i = 0; i < 5; i++) {
                         if (i < 4 && status.getSource().contains(source[i])) {
                             ti.setSource(i + 1);
                             break;
                         } else if (i > 3) {
                             ti.setSource(5);
                         }
                     }
 
                     //save the Geolocation coordinates
                     ti.setLatitude(status.getGeoLocation().getLatitude());
                     ti.setLongtitude(status.getGeoLocation().getLongitude());
 
                     //search and save the HashTags
                     HashtagEntity hte[] = new HashtagEntity[status.getHashtagEntities().length];
                     hte = status.getHashtagEntities();
                     StringBuilder sBuilder = new StringBuilder();
                     if (status.getHashtagEntities() != null) {
                         for (int i = 0; status.getHashtagEntities().length > i; i++) {
                             if (i < 1) {
                                 sBuilder.append(hte[i].getText());
                             } else {
                                 sBuilder.append(" ,").append(hte[i].getText());
                             }
                         }
                     } else {
                         sBuilder.append("none");
                     }
                     ti.setHashtags(sBuilder.toString());
                     //clear StringBuilder 
                     sBuilder.delete(0, sBuilder.length());
 
                     //save the Links
                     URLEntity ue[] = new URLEntity[status.getURLEntities().length];
                     ue = status.getURLEntities();
                     if (status.getURLEntities() != null) {
                         for (int i = 0; status.getURLEntities().length > i; i++) {
                             if (i < 1) {
                                 sBuilder.append(ue[i].getURL());
                             } else {
                                 sBuilder.append(", ").append(ue[i].getURL());
                             }
                         }
                     } else {
                         sBuilder.append("none");
                     }
                     ti.setLinks(sBuilder.toString());
                     sBuilder.delete(0, sBuilder.length());
 
                     //save the date 
                     ti.setTimeStamp(status.getCreatedAt());
 
                     //save the followers
                     ti.setFollowers(status.getUser().getFollowersCount());
 
                     //save the countrycodes
                     System.out.println(status.getPlace().getCountryCode());
                     ti.setcCode(status.getPlace().getCountryCode());
 
                     //raise the counter
                     totalAmount++;
                 }
             }
 
             @Override
             public void onDeletionNotice(StatusDeletionNotice sdn) {
             }
 
             @Override
             public void onTrackLimitationNotice(int i) {
                 System.out.println("limitationNotice:" + i);;
             }
 
             @Override
             public void onScrubGeo(long l, long l1) {
                 System.out.println("ScrubGeo " + l + l1);
             }
 
             @Override
             public void onStallWarning(StallWarning sw) {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public void onException(Exception excptn) {
                 excptn.printStackTrace();
             }
         };
         twitterStream.addListener(listener);
         twitterStream.sample();
     }
 }
