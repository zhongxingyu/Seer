 /**
  * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.ui;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.action.AddTaskAction;
 import com.bigpupdev.synodroid.adapter.BookmarkMenuAdapter;
 import com.bigpupdev.synodroid.utils.BookmarkDBHelper;
 import com.bigpupdev.synodroid.utils.BookmarkMenuItem;
 
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Message;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.DownloadListener;
 import android.webkit.WebChromeClient;
 import android.webkit.WebIconDatabase;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 /**
  * This activity displays a help page
  * 
  * @author Steve Garon (synodroid at gmail dot com)
  */
 @SuppressLint("SetJavaScriptEnabled")
 public class BrowserFragment extends SynodroidFragment {
 	private ImageButton bookmark_btn = null;
 	private ImageButton stop_btn = null;
 	private EditText url_text = null;
 	private ImageView url_favicon = null;
 	private WebView myWebView = null;
 	private String default_url = "http://www.google.com";
 	private BookmarkMenuAdapter adapter = null;
 	private Comparator<BookmarkMenuItem> comp = null;
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// ignore orientation change
 		super.onConfigurationChanged(newConfig);
 	}
 
 	/**
 	 * Activity creation
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		try {
 			if (((Synodroid) getActivity().getApplication()).DEBUG)	Log.v(Synodroid.DS_TAG, "BrowserFragment: Creating Browser fragment");
 		} catch (Exception ex) {/* DO NOTHING */}
 		View browser = inflater.inflate(R.layout.browser, null, false);
 		
 		String curBrowserUrl = ((Synodroid)getActivity().getApplication()).getBrowserUrl();
 		comp = new Comparator<BookmarkMenuItem>() {
             public int compare(BookmarkMenuItem arg0, BookmarkMenuItem arg1) {
             	try{
                     return arg0.title.compareTo(arg1.title);
             	}
             	catch (Exception e){
             		return 0;
             	}
             }
         };
 
 		WebIconDatabase.getInstance().open(getActivity().getDir("icons", FragmentActivity.MODE_PRIVATE).getPath());
 		View secMenu = ((BrowserActivity)getActivity()).getSlidingMenu().getSecondaryMenu();
 		adapter = new BookmarkMenuAdapter(getActivity());
         HashMap<String, String> map = getUrlsFromDB();
         for (Map.Entry<String, String> entry : map.entrySet()){
         	adapter.add(new BookmarkMenuItem(entry.getValue(), entry.getKey(), null));
         }
         adapter.sort(comp);
 		final ListView menuList = (ListView) secMenu.findViewById(R.id.lvBookmarks);
         menuList.setAdapter(adapter);
         menuList.setOnItemClickListener(new ListView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view, int position,
 					long id) {
 				BookmarkMenuItem menuListSelectedItem = (BookmarkMenuItem) menuList.getItemAtPosition(position);
             	((BrowserActivity)getActivity()).getSlidingMenu().showContent(true);
 				myWebView.loadUrl(menuListSelectedItem.url);
 			}
         
         });
         menuList.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapterView, View clickedView,
 					int position, long arg3) {
 				final BookmarkMenuItem menuListSelectedItem = (BookmarkMenuItem) menuList.getItemAtPosition(position);
 				
 				ConfirmDialog dialog = new ConfirmDialog();
 	        	Runnable ok = new Runnable(){
 					@Override
 					public void run() {
 						deleteFromDB(menuListSelectedItem.url);
 						
 						HashMap<String, String>bookmarks = getUrlsFromDB();
 						adapter.clear();
 				        for (Map.Entry<String, String> entry : bookmarks.entrySet()){
 				        	adapter.add(new BookmarkMenuItem(entry.getValue(), entry.getKey(), null));
 				        }
 				        adapter.sort(comp);
 				        adapter.notifyDataSetChanged();
 					}
 	            };
 
 	        	dialog.Confirm(getActivity(), getActivity().getText(R.string.confirm_remove_bookmark).toString(), menuListSelectedItem.url, getActivity().getText(R.string.button_cancel).toString(), getActivity().getText(R.string.button_ok).toString(), ok, ConfirmDialog.empty);
 	        	
 				return true;
 			}
         
         });
 		myWebView = (WebView) browser.findViewById(R.id.webview);
 		url_favicon = (ImageView) browser.findViewById(R.id.favicon);
 		stop_btn = (ImageButton) browser.findViewById(R.id.stop);
 		bookmark_btn = (ImageButton) browser.findViewById(R.id.bookmark);
 		url_text = (EditText) browser.findViewById(R.id.url);
 		url_text.setOnEditorActionListener(new OnEditorActionListener(){
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 	        	String url = url_text.getText().toString();
 				if (!url.equals("")){
 					if (!url.contains(".")){
 						try {
 							url = "http://www.google.com/m?q=" + URLEncoder.encode(url, "utf-8");
 						} catch (UnsupportedEncodingException e) {}
 					}
 					if (!url.contains("://")){
 						url = "http://" + url;
 					}
 					
 					myWebView.loadUrl(url);	
 					url_text.clearFocus();
 				}
 	            return true;
 		    }
 			
 		});
 		url_text.setOnFocusChangeListener(new OnFocusChangeListener(){
 
 			@Override
 			public void onFocusChange(View view, boolean hasFocus) {
 				if (!hasFocus){
 					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(url_text.getWindowToken(), 0);
 					imm.hideSoftInputFromWindow(url_text.getWindowToken(), 0);
 				}
 				
 			}
 			
 		});
 		bookmark_btn.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View clickedView) {
 				Activity a = getActivity();
 				try{
 					if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"BrowserFragment: Add bookark button selected.");
 				}catch (Exception ex){/*DO NOTHING*/}
 				
 				WebView webView = (WebView) getView().findViewById(R.id.webview);
 				String cur_url = webView.getUrl();
 				
 				if (cur_url != null){
 					HashMap<String, String> bookmarks = getUrlsFromDB();
 					
 					if (!bookmarks.containsKey(cur_url)){
 						bookmark_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_resethome));
 						//Save to DB
 						saveToDB(webView.getTitle(), cur_url);
 						
 						Crouton.makeText(getActivity(), getText(R.string.add_bookmark), Synodroid.CROUTON_CONFIRM).show();
 					}
 					else{
 						bookmark_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_sethome));
 						//Delete from DB
 						deleteFromDB(cur_url);
 						
 						Crouton.makeText(getActivity(), getText(R.string.del_bookmark), Synodroid.CROUTON_ALERT).show();
 					}
 					
 					bookmarks = getUrlsFromDB();
 					adapter.clear();
 			        for (Map.Entry<String, String> entry : bookmarks.entrySet()){
 			        	adapter.add(new BookmarkMenuItem(entry.getValue(), entry.getKey(), null));
 			        }
 			        adapter.sort(comp);
 			        adapter.notifyDataSetChanged();
 				}
 			}
 			
 		});
 		
 		stop_btn.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View clickedView) {
 				myWebView.stopLoading();				
 			}
 			
 		});
 		
 		final ProgressBar Pbar = (ProgressBar) browser.findViewById(R.id.browser_progress);
 		MyWebViewClient webViewClient = new MyWebViewClient();
 		MyDownloadListener downloadListener = new MyDownloadListener();
 		MyWebChromeClient webChromeClient = new MyWebChromeClient();
 		webChromeClient.setPB(Pbar);
 		
 		myWebView.setWebViewClient(webViewClient);
 		myWebView.setDownloadListener(downloadListener);
 		myWebView.setWebChromeClient(webChromeClient);
 		myWebView.setOnTouchListener(new View.OnTouchListener() { 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 			           switch (event.getAction()) { 
 			               case MotionEvent.ACTION_DOWN: 
 			               case MotionEvent.ACTION_UP: 
 			                   if (!v.hasFocus()) { 
 			                       v.requestFocus(); 
 			                   } 
 			                   break; 
 			           } 
 			           return false; 
 			        }
 			});
 		
 		WebSettings webSettings = myWebView.getSettings();
 		webSettings.setJavaScriptEnabled(true);
 		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
 		webSettings.setUseWideViewPort(true);
 		webSettings.setLoadWithOverviewMode(true);
 		
 		if (curBrowserUrl != null){
 			myWebView.loadUrl(curBrowserUrl);
 		}
 		else{
 			myWebView.loadUrl(default_url);
 		}	
 		registerForContextMenu(myWebView);
 		return browser;
 	}
 	
 	@Override
 	public void onDestroy(){
 		WebIconDatabase.getInstance().close();
 	    super.onDestroy();
 	}
 	
 	
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
 		ConfirmDialog dialog = new ConfirmDialog();
     	WebView.HitTestResult hitTestResult = myWebView.getHitTestResult();
 		final String okUrl = hitTestResult.getExtra();
     	Runnable ok = new Runnable(){
 			@Override
 			public void run() {
 				AddTaskAction addTask = new AddTaskAction(Uri.parse(okUrl), true, false);
 				Synodroid app = (Synodroid) getActivity().getApplication();
 				app.executeAsynchronousAction(BrowserFragment.this, addTask, false);
 			}
         };
 
     	dialog.Confirm(getActivity(), getActivity().getText(R.string.confirm_download).toString(), okUrl, getActivity().getText(R.string.button_cancel).toString(), getActivity().getText(R.string.button_ok).toString(), ok, ConfirmDialog.empty);
 	
 	}
 	public HashMap<String, String> getUrlsFromDB(){
 		HashMap<String, String> map = new HashMap<String, String>();
 		BookmarkDBHelper mDbHelper = new BookmarkDBHelper(getActivity());
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		// Define a projection that specifies which columns from the database
 		// you will actually use after this query.
 		String[] projection = {
 			BookmarkDBHelper.BookmarkEntry.COLUMN_NAME_TITLE,
 		    BookmarkDBHelper.BookmarkEntry.COLUMN_NAME_URL};
 
 		// How you want the results sorted in the resulting Cursor
 		String sortOrder =
 			BookmarkDBHelper.BookmarkEntry.COLUMN_NAME_TITLE + " DESC";
 
 		Cursor c = db.query(
 			BookmarkDBHelper.BookmarkEntry.TABLE_NAME,  // The table to query
 		    projection,                               // The columns to return
 		    null,                                // The columns for the WHERE clause
 		    null,                            // The values for the WHERE clause
 		    null,                                     // don't group the rows
 		    null,                                     // don't filter by row groups
 		    sortOrder                                 // The sort order
 		    );
 		
 	
 		for (int i = 0; i < c.getCount(); i++){
 			c.moveToPosition(i);
 			String itemTitle = c.getString(c.getColumnIndexOrThrow(BookmarkDBHelper.BookmarkEntry.COLUMN_NAME_TITLE));
 			String itemUrl = c.getString(c.getColumnIndexOrThrow(BookmarkDBHelper.BookmarkEntry.COLUMN_NAME_URL));
 			map.put(itemUrl, itemTitle);
 		}
 		
 		db.close();
 		return map;
 	}
 	
 	public void deleteFromDB(String url){
 		BookmarkDBHelper mDbHelper = new BookmarkDBHelper(getActivity());
 
 		// Gets the data repository in write mode
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		// Define 'where' part of query.
 		String selection = BookmarkDBHelper.BookmarkEntry.COLUMN_NAME_URL + " LIKE ?";
 		// Specify arguments in placeholder order.
 		String[] selectionArgs = { url };
 		// Issue SQL statement.
 		db.delete(BookmarkDBHelper.BookmarkEntry.TABLE_NAME, selection, selectionArgs);
 
 		db.close();
 	}
 	
 	public void saveToDB(String title, String url){
 		BookmarkDBHelper mDbHelper = new BookmarkDBHelper(getActivity());
 
 		// Gets the data repository in write mode
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 
 		// Create a new map of values, where column names are the keys
 		ContentValues values = new ContentValues();
 		values.put(BookmarkDBHelper.BookmarkEntry.COLUMN_NAME_TITLE, title);
 		values.put(BookmarkDBHelper.BookmarkEntry.COLUMN_NAME_URL, url);
 
 		// Insert the new row, returning the primary key value of the new row
 		db.insert(BookmarkDBHelper.BookmarkEntry.TABLE_NAME, null, values);
 
 		db.close();
 	}
 	
 	@Override
 	public void handleMessage(Message msgP) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public class MyWebChromeClient extends WebChromeClient{
 		private ProgressBar pb = null;
 		private boolean shouldUpdate = true;
 		
 		public void setPB(ProgressBar mPB){
 			pb = mPB;
 		}
 		
 		public void onProgressChanged(WebView view, int progress){
 			if(progress < 100 && pb.getVisibility() == ProgressBar.GONE){
 	        	pb.setVisibility(ProgressBar.VISIBLE);
 	        }
 	        
 	        pb.setProgress(progress);
 	        
 	        if(progress == 100) {
 	        	pb.setVisibility(ProgressBar.GONE);
 	        	((BrowserActivity)getActivity()).updateRefreshStatus(false);
 				shouldUpdate = true;
 				stop_btn.setVisibility(View.GONE);
 	        }
 	        else if (shouldUpdate){
 				((BrowserActivity)getActivity()).updateRefreshStatus(true);
 				shouldUpdate = false;
 				stop_btn.setVisibility(View.VISIBLE);
 			}
 	    }
 		
 		@Override
 		public void onReceivedIcon(WebView view, Bitmap icon){				
 			url_favicon.setImageBitmap(icon);
 		}
 	}
 	
 	public class MyDownloadListener implements DownloadListener{
 
 		@Override
 		public void onDownloadStart(String url, String userAgent,
 				String contentDisposition, String mimetype, long contentLength) {
 			try{
 				if (((Synodroid)getActivity().getApplication()).DEBUG)Log.d(Synodroid.DS_TAG, "Downloading URL: " + url);
 			} catch (Exception e){}
 			
 			if (url.startsWith("http://magnet/")){
 				url = url.replace("http://magnet/", "magnet:");
 			}
 			else if (url.startsWith("https://magnet/")){
 				url.replace("https://magnet/", "magnet:");
 			}
 			
 			ConfirmDialog dialog = new ConfirmDialog();
         	final String okUrl = url;
         	Runnable ok = new Runnable(){
 				@Override
 				public void run() {
 					AddTaskAction addTask = new AddTaskAction(Uri.parse(okUrl), true, false);
 					Synodroid app = (Synodroid) getActivity().getApplication();
 					app.executeAsynchronousAction(BrowserFragment.this, addTask, false);
 				}
             };
 
         	dialog.Confirm(getActivity(), getActivity().getText(R.string.confirm_download).toString(), url, getActivity().getText(R.string.button_cancel).toString(), getActivity().getText(R.string.button_ok).toString(), ok, ConfirmDialog.empty);
 		}
 		
 	}
 	
 	public class MyWebViewClient extends WebViewClient {
 		
 		public MyWebViewClient() {
 			super();
 			// start anything you need to
 		}
 		
 		@Override  
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {  
 		    boolean shouldOverride = false;  
 		    if (url.startsWith("http://magnet/")){
 				url = url.replace("http://magnet/", "magnet:");
 			}
 			else if (url.startsWith("https://magnet/")){
 				url.replace("https://magnet/", "magnet:");
 			}
 	        
 	        if (url.startsWith("magnet:")){
 	        	ConfirmDialog dialog = new ConfirmDialog();
 	        	final String okUrl = url;
 	        	Runnable ok = new Runnable(){
 					@Override
 					public void run() {
 						AddTaskAction addTask = new AddTaskAction(Uri.parse(okUrl), true, false);
 						Synodroid app = (Synodroid) getActivity().getApplication();
 						app.executeAsynchronousAction(BrowserFragment.this, addTask, false);
 					}
 	            };
 
 	        	dialog.Confirm(getActivity(), getActivity().getText(R.string.confirm_download).toString(), url, getActivity().getText(R.string.button_cancel).toString(), getActivity().getText(R.string.button_ok).toString(), ok, ConfirmDialog.empty);
 	        	
 				view.stopLoading();
 				shouldOverride = true;
 	        }
 		    return shouldOverride;  
 		} 
 		
 		public void onPageStarted(WebView view, String url, Bitmap favicon) {
 			// Do something to the urls, views, etc.
 			try{
 				if (((Synodroid)getActivity().getApplication()).DEBUG)Log.d(Synodroid.DS_TAG, "Loading URL: " + url);
 				((Synodroid)getActivity().getApplication()).setBrowserUrl(url);
 			} catch (Exception e){}
 			
 			if (favicon != null){
 				url_favicon.setImageBitmap(favicon);
 			}
 			else{
 				url_favicon.setImageDrawable(getResources().getDrawable(R.drawable.ic_browser));
 			}
 			
 			url_text.setText(url);
 			
 			HashMap<String, String> bookmarks = getUrlsFromDB();
 			
 		 	if (bookmark_btn != null && url != null){
 		 		if (bookmarks.containsKey(url)){
 					bookmark_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_resethome));
 				}
 				else{
 					bookmark_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_sethome));
 				}	
 		 	}
 		}
 	}
 }
