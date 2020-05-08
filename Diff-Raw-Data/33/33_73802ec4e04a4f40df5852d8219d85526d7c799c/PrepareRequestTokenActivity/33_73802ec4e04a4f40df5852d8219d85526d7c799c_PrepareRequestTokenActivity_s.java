 package com.gurkensalat.android.xingsync.oauth;
 
 import java.net.URLEncoder;
 
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
 
 import org.androidannotations.annotations.EActivity;
 import org.androidannotations.annotations.sharedpreferences.Pref;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 
 import com.gurkensalat.android.xingsync.keys.XingOAuthKeys;
 
 @EActivity
 public class PrepareRequestTokenActivity extends Activity
 {
 	private static Logger LOG = LoggerFactory.getLogger(PrepareRequestTokenActivity.class);
 
 	private OAuthConsumer consumer;
 	private OAuthProvider provider;
 
 	@Pref
 	SyncPrefs_ syncPrefs;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		LOG.info("onCreate()");
 		super.onCreate(savedInstanceState);
 		try
 		{
 			System.setProperty("debug", "true");
 			this.consumer = new CommonsHttpOAuthConsumer(XingOAuthKeys.CONSUMER_KEY, XingOAuthKeys.CONSUMER_SECRET);
 			this.provider = new CommonsHttpOAuthProvider(XingOAuthKeys.REQUEST_URL + "?scope="
 			        + URLEncoder.encode(XingOAuthKeys.SCOPE, XingOAuthKeys.ENCODING), XingOAuthKeys.ACCESS_URL,
 			        XingOAuthKeys.AUTHORIZE_URL);
 		}
 		catch (Exception e)
 		{
 			LOG.error("Error creating consumer / provider", e);
 		}
 
 		LOG.info("Starting task to retrieve request token.");
 		new OAuthRequestTokenTask(this, consumer, provider).execute();
 	}
 
 	/**
 	 * Called when the OAuthRequestTokenTask finishes (user has authorized the
 	 * request token). The callback URL will be intercepted here.
 	 */
 	@Override
 	public void onNewIntent(Intent intent)
 	{
 		LOG.info("onNewIntent()");
 		super.onNewIntent(intent);
 		final Uri uri = intent.getData();
 		if (uri != null && uri.getScheme().equals(XingOAuthKeys.OAUTH_CALLBACK_SCHEME))
 		{
 			LOG.info("Callback received : " + uri);
 			LOG.info("Retrieving Access Token");
 			// TODO Avoid passing the current prefs into access task
 			SharedPreferences prefs = syncPrefs.getSharedPreferences();
 			new RetrieveAccessTokenTask(this, consumer, provider, prefs).execute(uri);
 			finish();
 		}
 	}
 
 }
