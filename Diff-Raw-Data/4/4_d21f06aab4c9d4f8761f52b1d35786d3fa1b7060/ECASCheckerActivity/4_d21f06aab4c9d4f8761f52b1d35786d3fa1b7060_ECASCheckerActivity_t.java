 package com.oscd.ecas;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class ECASCheckerActivity extends Activity {
 
 	WebView myWebView;
 	ProgressDialog dialog;
 	String URL = "file:///android_asset/main.html";
 
 	SharedPreferences prefs = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(initWebView());
 		Log.d("OnCreate", "On Create initiated");
 
 	}
 	
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		setContentView(initWebView());
 		Log.d("onResume", "onResume initiated");
 
 	}	
 	
 	
 	
     @Override
     // Create the menu based on the XML defintion
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu_option, menu);
         return true;
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	        switch (item.getItemId()) {
             case R.id.help:
             	SendEmail();
                 break;
 
 	        }
 		return true;
 	  }
 	
 	    private void SendEmail() {
 	        String body = getString(R.string.body);
 
 	        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 	        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { getString(R.string.support) });
 	        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.help));
 	        emailIntent.setType("plain/text");
 	        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
 	        startActivity(emailIntent);
 	    }
 	 
 	 
 	
 
 	public boolean isOnline() {
 		ConnectivityManager cm =
 				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	public void SaveJson(String json) {
 		Log.d("JSON", json);
 		if (json.matches("remember.*")) {
 			Log.d("JSON", "I selected remember");
 
 			
 		    Context mContext = getApplicationContext();
 		    prefs = mContext.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
 		    
 			Editor editor = prefs.edit();
 			editor.putString("identifierType", GetMatch("identifierType", json));
 			editor.putString("identifier", GetMatch("identifier", json));
 			editor.putString("surname", GetMatch("surname", json));
 
 			editor.putString("dobDay", GetMatch("dobDay", json));
 			editor.putString("dobYear", GetMatch("dobYear", json));
 			editor.putString("dobMonth", GetMatch("dobMonth", json));
 
 			editor.putString("countryOfBirth", GetMatch("countryOfBirth", json));
 			editor.putBoolean("saved", true);
 			editor.commit();
 
 		}
 	}
 
 	public String GetMatch(String match, String str) {
 		String myreturn = "-1";
 		String Regxp;
 		if (match.equals("countryOfBirth")) {
 			Regxp = match + "=(\\d+)";
 
 		} else {
 			Regxp = match + "=(.*?)\\&";
 		}
 		//Log.d("Regxp ", Regxp);
 
 		Pattern patt = Pattern.compile(Regxp);
 		Matcher m = patt.matcher(str);
 		if (m.find()) {
 			myreturn = m.group(1);
 		}
 		return myreturn;
 
 	}
 
 	private WebView initWebView() {
 		
 	    Context mContext = getApplicationContext();
 	    prefs = mContext.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
 	    
 		myWebView = new WebView(this);
 
 		myWebView.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				super.onPageFinished(view, url);
 				if (url.toString().equals(URL)) {
 
 					if (prefs.getBoolean("saved", false)) {
 						// In here we know that the user has saved
 
 						Log.d("DEBUG SHARED", "Value is  set");
 
 						view.loadUrl("javascript:$('#surnameLabel').val(\""
 								+ prefs.getString("surname", null) + "\");");
 
 						view.loadUrl("javascript:$('#idNumberLabel').val(\""
 								+ prefs.getString("identifier", null) + "\");");
 						view.loadUrl("javascript:$(\"#idTypeLabel option[value='"
 								+ prefs.getString("identifierType", null)
 								+ "']\").prop('selected',true);");
 
 						// day of birth
 						view.loadUrl("javascript:$(\"#dobDay option[value='"
 								+ prefs.getString("dobDay", null)
 								+ "']\").prop('selected',true);");
 						view.loadUrl("javascript:$(\"#dobYear option[value='"
 								+ prefs.getString("dobYear", null)
 								+ "']\").prop('selected',true);");
 						view.loadUrl("javascript:$(\"#dobMonth option[value='"
 								+ prefs.getString("dobMonth", null)
 								+ "']\").prop('selected',true);");
 						// place of birth
						view.loadUrl("javascript:$(\"#cobLabel option[value='" 
								+ prefs.getString("countryOfBirth", null)								
								+"']\").prop('selected',true);");
 
 
 
 					} else {
 						Log.d("DEBUG SHARED", "Value is NOT  set");
 					}
 				}
 			}
 
 		});
 
 		myWebView.getSettings().setJavaScriptEnabled(true);
 		myWebView.getSettings().setSavePassword(false);
 		myWebView.getSettings().setPluginsEnabled(false);
 		myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
 		myWebView.addJavascriptInterface(this, "activity");
 
 		// Scale
 		myWebView.setInitialScale(0);
 		myWebView.setWebChromeClient(new MyJavaScriptChromeClient());
 
 		// load content
 		myWebView.loadUrl(URL);
 
 		return myWebView;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// Check if the key event was the Back button and if there's history
 		if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
 			myWebView.goBack();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private class MyJavaScriptChromeClient extends WebChromeClient {
 		@Override
 		public boolean onJsAlert(WebView view, String url, String message,
 				final JsResult result) {
 			// handle Alert event, here we are showing AlertDialog
 			new AlertDialog.Builder(ECASCheckerActivity.this)
 			.setTitle("Message !")
 			.setMessage(message)
 			.setPositiveButton(android.R.string.ok,
 					new AlertDialog.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 						int which) {
 					// do your stuff
 					result.confirm();
 				}
 			}).setCancelable(false).create().show();
 			return true;
 		}
 	}
 
 }
