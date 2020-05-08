 /**
  * Update
  *
  * Update status in Twitter platform.
  * Based on the code exposed by <a href="https://github.com/yusuke">Yusuke Yamamoto</a>
  * in <a href="https://github.com/yusuke/twitter4j/tree/master/twitter4j-examples/src/main/java/twitter4j/examples">twitter4J examples</a>
  *
  * Author: Ariel Gerardo Rios <mailto:arielgerardorios@gmail.com>
  */
 
 package twittercli.cli;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 
 import twittercli.commons.Constants;
 
 
 /** 
  * Update
  *
  * @author Ariel Gerardo Rios <mailto:arielgerardorios@gmail.com>
 */
 public class Update extends Object { 
 
     private Twitter twitter = null;
 
     public Update() {
        this.initialize();
    }

    /** 
     * Creates the Twitter object through the factory and sets credentials to
     * use against the API.
     *
    */
    private void initialize() { 
         this.twitter = new TwitterFactory().getInstance();
        this.twitter.setOAuthConsumer(Constants.OAUTH_KEY,
                Constants.OAUTH_SECRET);
     }
 
     /** 
      * Shows the help text.
      *
     */
     public static void help() { 
         System.out.println( Constants.PROJECT_NAME + " >> Usage: java " +
                 "Update [message]");
     }
 
     /** 
      * Obtains the authorization code to update status.
      *
     */
     private boolean authorize() { 
         try {
             // get request token.
             // this will throw IllegalStateException if access token is already
             // available
             RequestToken requestToken = twitter.getOAuthRequestToken();
             System.out.println("Got request token.");
             System.out.println("Request token: " + requestToken.getToken());
             System.out.println("Request token secret: " +
                     requestToken.getTokenSecret());
             AccessToken accessToken = null;
                                                                                  
             BufferedReader br = new BufferedReader(new
                     InputStreamReader(System.in));
             while (null == accessToken) {
                 System.out.println("Open the following URL and grant access " +
                         "to your account:");
                 System.out.println(requestToken.getAuthorizationURL());
                System.out.print("Enter the PIN (if available) and hit " +
                        "enter after you granted access.\n[PIN]: ");
                 String pin = null;
                 try {
                     pin = br.readLine();
                 } catch (IOException ioe) {
                     ioe.printStackTrace();
                     System.out.println("Failed to read the system input.");
                     return false;
                 }
                 try {
                     if (pin.length() > 0) {
                         accessToken = twitter.getOAuthAccessToken(requestToken,
                                 pin);
                     } else {
                         accessToken = twitter.getOAuthAccessToken(requestToken);
                     }
                 } catch (TwitterException te) {
                     if (401 == te.getStatusCode()) {
                        System.out.println("There was an exception getting " +
                                "the access tocken:");
                         te.printStackTrace();
                    } else {
                         return false;
                     }
                 }
             }
             System.out.println("Got access token.");
             System.out.println("Access token: " + accessToken.getToken());
             System.out.println("Access token secret: " +
                     accessToken.getTokenSecret());
         } catch (IllegalStateException ie) {
             // access token is already available, or consumer key/secret is
             // not set.
             if (!twitter.getAuthorization().isEnabled()) {
                System.out.println("OAuth consumer key/secret is not set:");
                ie.printStackTrace();
                 return false;
             }
         } catch (TwitterException te) {
            System.out.println("Unable to get authorization code:");
             te.printStackTrace();
             return false;
         }
 
         return true;
     }
 
     /** 
      * Performs all tasks to concrete the update.
      *
      * @return int value as operation result.
     */
     public int update(String message) {
         if (!this.authorize()) {
             System.out.println("Failed getting authorization.");
             return -1;
         }
                                                                     
         Status status = null;
         try {
             status = twitter.updateStatus(message);
         } catch (TwitterException te) {
             te.printStackTrace();
             System.out.println("Failed updating status: " + te.getMessage());
             return -1;
         }
         System.out.println("Successfully updated the status to [" +
                     status.getText() + "].");
         return 0;
     }
 
     /**
      * Usage: java Update [message]
      *
      * @param args message
      * @return int value as program result.
      */
     public static void main(String[] args) {
         if (args.length < 1) {
             Update.help();
             System.exit(-1);
         }
         
         Update u = new Update();
         System.exit(u.update(args[0]));
     }
 }
 
 // vim:ft=java:
