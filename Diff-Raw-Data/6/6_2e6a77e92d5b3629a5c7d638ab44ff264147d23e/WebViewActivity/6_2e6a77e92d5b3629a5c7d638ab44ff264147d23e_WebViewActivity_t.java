 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.ui;
 
 import org.gots.R;
 
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class WebViewActivity extends SherlockFragment {
 	private ProgressDialog pd;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		WebView mWebView = new WebView(getActivity());
 		mWebView.setWebViewClient(new HelloWebViewClient());
 		mWebView.getSettings().setJavaScriptEnabled(true);
 
 		Bundle bundle = this.getArguments();
 		String url = bundle.getString("org.gots.seed.url");

 		mWebView.loadUrl(url);
 
 		pd = ProgressDialog.show(getActivity(), "", getResources().getString(R.string.gots_loading), true);
 		pd.setCanceledOnTouchOutside(true);
 		return mWebView;
 	}
 
 	private class HelloWebViewClient extends WebViewClient {
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			view.loadUrl(url);
 			return true;
 		}
 
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			super.onPageFinished(view, url);
			if (pd.isShowing())
				pd.dismiss();
 		}
 
 	}
 }
