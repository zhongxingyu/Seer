 package net.morodomi.lecture8;
 
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 
 public class Config {
	public static final String TWITTER_CONSUMER_KEY    = "";
	public static final String TWITTER_CONSUMER_SECRET = "";
 	public static final String TWITTER_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
 	public static final String TWITTER_AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
 	public static final String TWITTER_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
 	
 	public static final String PREFERENCES_NAME = "net.morodomi.lecture8";
 
 	public static CommonsHttpOAuthConsumer consumer;
 	public static OAuthProvider provider;
 }
