 /*
  Dubsar Dictionary Project
  Copyright (C) 2010-11 Jimmy Dee
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package com.dubsar_dictionary.Dubsar;
 
import android.annotation.SuppressLint;
 import android.os.Bundle;
// import android.util.Log;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class FAQActivity extends DubsarActivity {
 
 	WebView mWebView=null;
 
	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState, R.layout.faq);
 		
 		mWebView = (WebView)findViewById(R.id.faq_web_view);
         mWebView.getSettings().setJavaScriptEnabled(true);
 		
 		if (savedInstanceState != null) {
 			mWebView.restoreState(savedInstanceState);
 		}
 		else {
			String html = "<html><body style=\"background-color: #e0e0ff;\"><h1 style=\"color: #1c94c4; text-align: center; margin-top: 2ex; font: bold 18pt sans-serif\">" + 
					getString(R.string.loading_faq) + "</h1></body></html>";
			mWebView.loadData(html, "text/html", "utf-8");
			// Log.i(getString(R.string.app_name), "Initial FAQ HTML: " + html);
 		}
 		
         mWebView.setWebViewClient(new WebViewClient(){
             public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 view.loadUrl(url);
                 return true;
             }
             
             public void onPageFinished(WebView view, String url) {
             	String faqUrl = getString(R.string.faq_url);
             	if (url == null || !url.equals(faqUrl)) {
             		mWebView.loadUrl(faqUrl);
             	}
             }
         });
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		mWebView.saveState(outState);
 	}
 
 }
