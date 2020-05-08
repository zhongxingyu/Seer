 /*
  * 	This file is part of ASage.
  *
  *    ASage is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    ASage is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with ASage.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   Copyright 2012 Giovanni Visentini 
  */
 
 package com.wise;
 
 import java.io.ByteArrayOutputStream;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.transform.Result;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import android.app.Fragment;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.TextView;
 
 /**
  * class that show the rss content
  * @author Giovanni Visentini
  * */
 public class RssViewFragment extends Fragment {
 
 	private final static String TAG="RssView";
 	public final static String RSS_URL = "url";
 
 	private URL feedXml;
 	private Transformer mRss2Html;
 	private WebView browser;
 
 	private static String ERROR_MESSAGE[];
 	private static int ERROR_URL=0;
 	private static int ERROR_XML=1;
 	private TextView errorMessage;
 	private View errorView;
 	
 	/**
 	 * @see android.app.Fragment#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if(ERROR_MESSAGE==null){
 			ERROR_MESSAGE=getResources().getStringArray(R.array.errorMessageString);
 		}
 		
 		if (savedInstanceState != null){
 			try {
 				feedXml = new URL(savedInstanceState.getString(RSS_URL));
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}//try-catch
 		}//if
 
 		try {
 			mRss2Html = TransformerFactory.newInstance().newTransformer(
 					new StreamSource(this.getResources().openRawResource(
 							R.raw.rss2html)));
 		} catch (TransformerConfigurationException e) {
 			Log.d(TAG, e.toString());
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			Log.d(TAG, e.toString());
 			e.printStackTrace();
 		}//try-catch
 
 	}// onCreate
 
 	/**
 	 * @see android.app.Fragment#onStart()
 	 */
 	/*we use onStart for be secure that the webView is initialized */
 	public void onStart() {
 		super.onStart();
 		browser.setWebViewClient(new WebClientManager());
 	}
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.rss_view, container, false);
         
         browser = (WebView) v.findViewById(R.id.browser);
         errorMessage = (TextView) v.findViewById(R.id.errorText);
         errorView = v.findViewById(R.id.errorView);
         
         showBrowser();
         
         return v;
     }
 
     public void showError(int messageId, Object... paramiter){
     	errorMessage.setText(String.format(ERROR_MESSAGE[messageId],paramiter));
     	errorView.setVisibility(View.VISIBLE);
     	browser.setVisibility(View.GONE);
     }
 
     public void showBrowser(){
     	errorView.setVisibility(View.GONE);
     	browser.setVisibility(View.VISIBLE);
     }
     
     public void viewRss(String url){
     	try {
 			feedXml = new URL(url);
 			new LoadRss().execute(feedXml);
 		} catch (MalformedURLException e) {
 			showError(ERROR_URL,url);
 		}
     	
     	
     }
     
 	/**
 	 * @see android.app.Fragment#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// save the feed
 		if(feedXml!=null)
 			outState.putString(RSS_URL, feedXml.toString());
 	}
 
 	/**
 	 *class that load the xml and convert it into an html file
 	 */
 	private class LoadRss extends AsyncTask<URL, Void, Boolean> {
 
 		//buffer for the downloaded page
 		private ByteArrayOutputStream page = new ByteArrayOutputStream();
 
 		@Override
 		protected Boolean doInBackground(URL... arg0) {
 			
 			Result html = new StreamResult(page);
 
 			try {
 				StreamSource s = new StreamSource(feedXml.openStream());
 				mRss2Html.transform(s, html);
 			} catch (Exception e) {
 				Log.e(TAG, "TransformerException", e);
 				return false;
 			}
 
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean loaded) {
 			Log.d(TAG, "Page Loaded:" + loaded);
 			if(loaded){
 				String html = page.toString();
				if(!html.isEmpty())
 					browser.loadData(page.toString(), "text/html", "utf8");
				else
 					showError(ERROR_XML,feedXml.toString());
 			}
 		}
 
 	}
 
 	/**
 	 *class that manage the browser 
 	 *used for open a link into the default browser instead of using the fragment
 	 */
 	private class WebClientManager extends WebViewClient {
 
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			Log.d(TAG, "Open link: " + url);
 
 			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 			view.getContext().startActivity(i);
 
 			return true;
 		}
 
 	}
 
 }
