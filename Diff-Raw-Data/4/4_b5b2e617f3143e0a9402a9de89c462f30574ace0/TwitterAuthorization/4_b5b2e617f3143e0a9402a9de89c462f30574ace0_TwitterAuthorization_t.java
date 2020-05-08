 package client;
 import exceptions.AccessAccountFailedException;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import util.ConsoleReader;
 
 public class TwitterAuthorization {
 	
 	private final static String OPEN_URL_MESSAGE = "Open the following URL and grant access to your account:";
 	private final static String ENTER_PIN_MESSAGE = "Enter the PIN from the Website (if available) or just hit enter. [PIN]:";
 
 	//TODO Remove before commitment
	private final static String O_AUTH_CONSUMER_KEY = "*********************";
	private final static String O_AUTH_CONSUMER_SECRET = "******************************************";
 	
 	private AccessToken accessToken = null;
 	
 	public TwitterAuthorization(Twitter twitter) throws AccessAccountFailedException {
 		twitter.setOAuthConsumer(O_AUTH_CONSUMER_KEY, O_AUTH_CONSUMER_SECRET);
 		
 		try {
 			accessToken = requestAccessToken(twitter);
 		} catch (TwitterException e) {
 			if (401 == e.getStatusCode()) {
 				throw new AccessAccountFailedException("Unable to get access twitter account.");
 			} else {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private AccessToken requestAccessToken(Twitter twitter) throws TwitterException {
 		String pin;
 		AccessToken accessToken = null;
 		RequestToken requestToken = twitter.getOAuthRequestToken();
 		
 		while (accessToken == null) {
 			System.out.println(OPEN_URL_MESSAGE);
 			System.out.println(requestToken.getAuthorizationURL());
 			
 			pin = ConsoleReader.readLine(ENTER_PIN_MESSAGE);
 			
 			if (pin.length() > 0) {
 				accessToken = twitter.getOAuthAccessToken(requestToken, pin);
 			} else {
 				accessToken = twitter.getOAuthAccessToken();
 			}
 		}
 		
 		return accessToken;
 	}
 
 	public String getOAuthConsumerKey() {
 		return O_AUTH_CONSUMER_KEY;
 	}
 
 	public String getOAuthConsumerSecret() {
 		return O_AUTH_CONSUMER_SECRET;
 	}
 
 	public AccessToken getAccessToken() {
 		return accessToken;
 	}
 }
