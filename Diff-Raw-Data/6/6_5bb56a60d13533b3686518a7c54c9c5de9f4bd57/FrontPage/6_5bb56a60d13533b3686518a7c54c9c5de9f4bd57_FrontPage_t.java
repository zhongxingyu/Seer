 package bufferings.unyatter;
 
 import org.slim3.controller.Navigation;
 
 import scenic3.ScenicPage;
 import scenic3.annotation.ActionPath;
 import scenic3.annotation.Default;
 import scenic3.annotation.Page;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 
 @Page("/")
 public class FrontPage extends ScenicPage {
 
   private static final String OAUTH_VERIFIER_KEY = "oauth_verifier";
 
   private static final String REQUEST_TOKEN_KEY = "RequestToken";
 
   private static final String TWITTER_KEY = "Twitter";
 
   private static final String SCREEN_NAME_KEY = "ScreenName";
 
   private static final String ACCESS_TOKEN_SECRET_KEY = "AccessTokenSecret";
 
   private static final String ACCESS_TOKEN_KEY = "AccessToken";
 
   private static final String UNYATTER_URL = "http://unyatter.appspot.com/";
 
   private static final String[] UNYATTER_TWEETS =
     {
       "(」・ω・)」うー(／・ω・)／にゃー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー #unyatter ",
       "(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー(／・ω・)／にゃー(」・ω・)」うー #unyatter ",
      "(」・ω・)」うー(／・ω・)／にゃー(／・ω・)／にゃー(／・ω・)／にゃー(／・ω・)／にゃー(／・ω・)／にゃー(／・ω・)／にゃー(／・ω・)／にゃー(／・ω・)／にゃー(／・ω・)／にゃー #unyatter " };
 
   @Default
   public Navigation index() {
     if (isLoggedIn()) {
       return forward("/index.jsp");
     } else {
       return forward("/login.jsp");
     }
   }
 
   @ActionPath("oauth")
   public Navigation oauth() throws TwitterException {
     Twitter twitter = new TwitterFactory().getInstance();
     RequestToken requestToken = twitter.getOAuthRequestToken();
     sessionScope(TWITTER_KEY, twitter);
     sessionScope(REQUEST_TOKEN_KEY, requestToken);
     return redirect(requestToken.getAuthorizationURL());
   }
 
   @ActionPath("callback")
   public Navigation callback() throws Exception {
     try {
       String verifier = requestScope(OAUTH_VERIFIER_KEY);
       Twitter twitter = removeSessionScope(TWITTER_KEY);
       RequestToken requestToken = removeSessionScope(REQUEST_TOKEN_KEY);
      AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
       saveAccessToken(accessToken);
       return redirect("/");
     } catch (Exception e) {
       clearAccessToken();
       throw e;
     }
   }
 
   @ActionPath("logout")
   public Navigation logout() {
     clearAccessToken();
     return redirect("/");
   }
 
   @ActionPath("tweet")
   public Navigation tweet() throws TwitterException {
     Twitter twitter = new TwitterFactory().getInstance();
     twitter.setOAuthAccessToken(loadAccessToken());
 
     ResponseList<Status> statuses = twitter.getUserTimeline();
     for (String item : UNYATTER_TWEETS) {
       boolean tweeted = false;
       for (int i = 0; i < 10; i++) {
         Status status = statuses.get(i);
         String text = status.getText();
         if (text.startsWith(item)) {
           tweeted = true;
           break;
         }
       }
       if (!tweeted) {
         twitter.updateStatus(item + UNYATTER_URL);
         return forward("/tweet.jsp");
       }
     }
     throw new IllegalStateException();
   }
 
   void saveAccessToken(AccessToken accessToken) {
     sessionScope(ACCESS_TOKEN_KEY, accessToken.getToken());
     sessionScope(ACCESS_TOKEN_SECRET_KEY, accessToken.getTokenSecret());
     sessionScope(SCREEN_NAME_KEY, accessToken.getScreenName());
   }
 
   AccessToken loadAccessToken() {
     String accessToken = sessionScope(ACCESS_TOKEN_KEY);
     String accessTokenSecret = sessionScope(ACCESS_TOKEN_SECRET_KEY);
     return new AccessToken(accessToken, accessTokenSecret);
   }
 
   void clearAccessToken() {
     removeSessionScope(ACCESS_TOKEN_KEY);
     removeSessionScope(ACCESS_TOKEN_SECRET_KEY);
     removeSessionScope(SCREEN_NAME_KEY);
   }
 
   boolean isLoggedIn() {
     return (sessionScope(ACCESS_TOKEN_KEY) != null);
   }
 }
