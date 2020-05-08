 package com.bexkat.feedlib;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.ShareActionProvider;
 
 public class ItemDetailFragment extends SherlockFragment {
 	Activity mActivity;
 	Uri uri;
 	String title;
 	
 	private static final String TAG = "ItemDetailFragment";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		mActivity = activity;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.item_detail, container,
 				false);
 		return v;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.menu_item_detail, menu);
 		MenuItem actionItem = menu.findItem(R.id.menu_item_share);
 		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
 				.getActionProvider();
 		actionProvider
 				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
 		actionProvider.setShareIntent(createShareIntent(title, uri));
 	}
 
 	static public Intent createShareIntent(String title, Uri uri) {
 		Intent shareIntent = new Intent(Intent.ACTION_SEND);
 
 		shareIntent.setType("text/plain");
 		shareIntent
 				.putExtra(Intent.EXTRA_SUBJECT, R.string.share_subject);
 		shareIntent.putExtra(Intent.EXTRA_TEXT, "Article on " + title + ": " + uri.toString());
 		return shareIntent;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return false;
 	}
 
 	public void updateState(Bundle b) {
 		TextView tv = (TextView) mActivity.findViewById(R.id.item_content);
 		tv.setText(b.getString("content"));
 		tv = (TextView) mActivity.findViewById(R.id.item_pubdate);
 		tv.setText(b.getString("pubDate"));
 		tv = (TextView) mActivity.findViewById(R.id.item_title);
 		tv.setText(b.getString("title"));
 		title = b.getString("title");
 		uri = Uri.parse(b.getString("URI"));
 	}
 }
