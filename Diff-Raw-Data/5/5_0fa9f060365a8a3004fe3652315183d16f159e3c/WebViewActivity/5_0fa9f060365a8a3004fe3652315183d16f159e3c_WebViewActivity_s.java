 //
 //  Copyright (c) 2013 Financial Business Systems, Inc. All rights reserved.
 //
 //  Licensed under the Apache License, Version 2.0 (the "License");
 //  you may not use this file except in compliance with the License.
 //  You may obtain a copy of the License at
 //
 //  http://www.apache.org/licenses/LICENSE-2.0
 //
 //  Unless required by applicable law or agreed to in writing, software
 //  distributed under the License is distributed on an "AS IS" BASIS,
 //  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //  See the License for the specific language governing permissions and
 //  limitations under the License.
 //
 
 package com.sparkplatform.ui;
 
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 import com.sparkplatform.api.SparkAPI;
 import com.sparkplatform.api.SparkAPIClientException;
 import com.sparkplatform.api.SparkSession;
 
 public class WebViewActivity extends Activity {
 	
 	// class vars *************************************************************
 	
 	private static final String TAG = "WebViewActivity";
 
     // instance vars **********************************************************
     
     private SparkAPI sparkClient;
     private boolean loginHybrid;
     
     // interface **************************************************************
     
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_web_view);
 		
 	    this.sparkClient = SparkAPI.getInstance();
 	    
 		Intent intent = getIntent();
 		loginHybrid = intent.getBooleanExtra(UIConstants.EXTRA_LOGIN_HYBRID, true);
 
 		WebView webView = (WebView) findViewById(R.id.webview);
 		WebSettings webSettings = webView.getSettings();
 		webSettings.setJavaScriptEnabled(true);
 		webView.setWebViewClient(new SparkWebViewClient());
 					    
 		webView.loadUrl(SparkAPI.sparkOpenIdLogoutURL);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_web_view, menu);
 		return true;
 	}
 	
 	private class SparkWebViewClient extends WebViewClient
 	{
 		public void onPageFinished (WebView view, String url)
 		{
 			if(url.equals(SparkAPI.sparkOpenIdLogoutURL))
 			{
 				String loginURL = loginHybrid ? 
 						sparkClient.getSparkHybridOpenIdURLString() : 
 						sparkClient.getSparkOpenIdAttributeExchangeURLString();
 				view.loadUrl(loginURL);
 			}
 			else
 				findViewById(R.id.webViewProgressBar).setVisibility(View.GONE);
 		}
 
 		public boolean shouldOverrideUrlLoading (WebView view, String url)
 		{
 			Log.d(TAG, "loadUrl>" + url);
 			
 			String openIdSparkCode = null;
 		    if(loginHybrid && (openIdSparkCode = SparkAPI.isHybridAuthorized(url)) != null)
 		    {
 				   Log.d(TAG, "openIdSparkCode>" + openIdSparkCode);
 				   new OAuth2PostTask().execute(openIdSparkCode);	   				   
 		    	   return true;
 		    }
 		    else if(!loginHybrid)
 		    {
 		    	try
 		    	{
 		    		if(sparkClient.openIdAuthenticate(url) != null)
 		    		{
 		    			processAuthentication((SparkSession)sparkClient.getSession(), url);
 
 		    			Intent intent = new Intent(getApplicationContext(), MyAccountActivity.class);
 		    			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		    			startActivity(intent);
 		    			return true;
 		    		}
 		    	}
 		    	catch(SparkAPIClientException e)
 		    	{
 		    		Log.e(TAG,"SparkApiClientException", e);
 		    	}
 		    	return false;
 		    }
 
 			return false;
 		}
 	}
 	
 	 private class OAuth2PostTask extends AsyncTask<String, Void, SparkSession> {
 	     protected SparkSession doInBackground(String... openIdSparkCode) {
 	    	 SparkSession session = null;
 	    	 try
 	    	 {
 	    		 session = sparkClient.hybridAuthenticate(openIdSparkCode[0]);
 	    	 }
 	    	 catch(SparkAPIClientException e)
 	    	 {
 	    		 Log.e(TAG, "SparkApiClientException", e);
 	    	 }
 	    	 
 	    	 return session;
 	     }
 	     
 	     protected void onPostExecute(SparkSession sparkSession) {	    	 
	    	if(sparkSession != null)
 	    	{
 	    		processAuthentication(sparkSession, null);
 	    		
 	    		Intent intent = new Intent(getApplicationContext(), ViewListingsActivity.class);
 	    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	    		startActivity(intent);	  
 	    	}
 	    	else
 	    	{
	    		 ActivityHelper.errorDialog("OAuth2 error", null, WebViewActivity.this, findViewById(R.id.webViewProgressBar));
 	    	}
 		 }
 	 }
 	 
 	 private void processAuthentication(SparkSession session, String url)
 	 {
 		SecurePreferences p = new SecurePreferences(this,UIConstants.SPARK_PREFERENCES, SparkAPI.getConfiguration().getApiSecret(), false);
 		 
 		if(session.getOpenIdToken() != null)
 		{
 			p.put(UIConstants.AUTH_OPENID, session.getOpenIdToken());
 			
 			List<NameValuePair> params = SparkAPI.getURLParams(url);
 			String value = SparkAPI.getParameter(params, "openid.ax.value.id");
 			if(value != null)
 				p.put(UIConstants.PROPERTY_OPENID_ID, value);
 			value = SparkAPI.getParameter(params, "openid.ax.value.friendly");
 			if(value != null)
 				p.put(UIConstants.PROPERTY_OPENID_FRIENDLY, value);
 			value = SparkAPI.getParameter(params, "openid.ax.value.first_name");
 			if(value != null)
 				p.put(UIConstants.PROPERTY_OPENID_FIRST_NAME, value);
 			value = SparkAPI.getParameter(params, "openid.ax.value.middle_name");
 			if(value != null)
 				p.put(UIConstants.PROPERTY_OPENID_MIDDLE_NAME, value);
 			value = SparkAPI.getParameter(params, "openid.ax.value.last_name");
 			if(value != null)
 				p.put(UIConstants.PROPERTY_OPENID_LAST_NAME, value);
 			value = SparkAPI.getParameter(params, "openid.ax.value.email");
 			if(value != null)
 				p.put(UIConstants.PROPERTY_OPENID_EMAIL, value);
 		}
 		else
 		{
 			p.put(UIConstants.AUTH_ACCESS_TOKEN, session.getAccessToken());
 			p.put(UIConstants.AUTH_REFRESH_TOKEN, session.getRefreshToken());
 		}		 
 	 }
 }
