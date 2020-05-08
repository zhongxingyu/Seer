 package net.morodomi.lecture8;
 
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 
 public class Config {
	public static final String TWITTER_CONSUMER_KEY    = "DQIxumXA4O9agTfeFx8rw";
	public static final String TWITTER_CONSUMER_SECRET = "im91pITvGrJb72RVOPEy84O5cr45EIT0L0ZT9mW0uI";
 	public static final String TWITTER_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
 	public static final String TWITTER_AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
 	public static final String TWITTER_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
 	
 	public static final String PREFERENCES_NAME = "net.morodomi.lecture8";
 
 	public static CommonsHttpOAuthConsumer consumer;
 	public static OAuthProvider provider;
 }
