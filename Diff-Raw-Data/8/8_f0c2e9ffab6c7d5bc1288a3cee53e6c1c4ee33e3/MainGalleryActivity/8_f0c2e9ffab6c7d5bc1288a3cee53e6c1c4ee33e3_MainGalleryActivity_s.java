 package com.atlast.MegaLike;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Collections;
 import java.util.Vector;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.atlast.MegaLike.FacebookLogic.Link;
 import com.atlast.MegaLike.FacebookLogic.Photo;
 import com.atlast.MegaLike.Lib.Extra;
 import com.atlast.MegaLike.Lib.SessionManager;
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.FacebookError;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.viewpagerindicator.TabPageIndicator;
 
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.Html;
 import android.text.Spanned;
 import android.util.Log;
 import android.widget.Toast;
 
 public class MainGalleryActivity extends SherlockFragmentActivity {
 	private static final int ACTIVITY_REQUEST_CODE = 0;
 	private FragmentStatePagerAdapter mFragmentAdapter;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add("Search").setIcon(R.drawable.ic_action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		menu.add("Clear cache").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		menu.add("Logout").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getTitle().equals("Search"))
 			onSearchRequested();
 		if (item.getTitle().equals("Clear cache"))
 			clearCache();
 		if (item.getTitle().equals("Logout"))
 			logout();
 		return true;
 	}
 
 	private void clearCache() {
 		ImageLoader.getInstance().clearDiscCache();
 		ImageLoader.getInstance().clearMemoryCache();
 		Toast.makeText(this, "Cleared cache", Toast.LENGTH_SHORT).show();
 	}
 
 	private void logout() {
 		Extra.mAsyncRunner = new AsyncFacebookRunner(Extra.mFacebook);
 		Extra.mAsyncRunner.logout(getApplicationContext(), new RequestListener() {
 			public void onComplete(String response, Object state) {
 				SessionManager.clear(MainGalleryActivity.this);
 				startLoginActivity();
 				finish();
 			}
 
 			public void onIOException(IOException e, Object state) {
 			}
 
 			public void onFileNotFoundException(FileNotFoundException e, Object state) {
 			}
 
 			public void onMalformedURLException(MalformedURLException e, Object state) {
 			}
 
 			public void onFacebookError(FacebookError e, Object state) {
 			}
 		});
 	}
 
 	private void startLoginActivity() {
 		Intent intent = new Intent(this, LoginActivity.class);
 		startActivity(intent);
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.maingallery);
 		Log.d("TAG", "Access Token: " + Extra.mFacebook.getAccessToken());
 
 		mFragmentAdapter = new MegalikeAdapter(getSupportFragmentManager());
 
 		ViewPager pager = (ViewPager) findViewById(R.id.maingallery_pager);
 		pager.setAdapter(mFragmentAdapter);
 
 		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.maingallery_indicator);
 		indicator.setViewPager(pager);
 		
 		new ParseFacebookDataTask().execute();
 	}
 
 	private void redrawUI() {
 		mFragmentAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		setIntent(intent);
 		handleIntent(intent);
 	}
 
 	private void handleIntent(Intent intent) {
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			final Intent searchIntent = new Intent(getApplicationContext(), SearchableActivity.class);
 			searchIntent.putExtra(SearchManager.QUERY, query);
 			searchIntent.setAction(Intent.ACTION_SEARCH);
 			startActivityForResult(searchIntent, ACTIVITY_REQUEST_CODE);
 		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
 			String uid = intent.getDataString();
 			Extra.CURRENT_FRIEND_UID = uid;
 			new ParseFacebookDataTask().execute();
 		}
 	}
 
 	private class ParseFacebookDataTask extends AsyncTask<Void, Void, String> {
 		private ProgressDialog dialog = new ProgressDialog(MainGalleryActivity.this);
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
			dialog.setMessage("Downloading data from Facebook.");
 			dialog.show();
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			parseImageUrls();
 			parseLinks();
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if (dialog.isShowing())
 				dialog.dismiss();
 			redrawUI();
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == ACTIVITY_REQUEST_CODE)
 			new ParseFacebookDataTask().execute();
 	}
 
 	private void parseImageUrls() {
 		if (!Extra.CURRENT_FRIEND_UID.equals(Extra.sCurrentlyDisplayedImagesUID)) {
 			Extra.sBigImageUrls.clear();
 			Extra.sThumbImageUrls.clear();
 			Vector<Photo> photos = Extra.mFacebookData.getPhotosAll(Extra.CURRENT_FRIEND_UID);
 			Collections.sort(photos);
 			for (Photo photo : photos) {
 				Extra.sBigImageUrls.add(photo.bigSrc);
 				Extra.sThumbImageUrls.add(photo.thumbSrc);
 			}
 			Extra.sCurrentlyDisplayedImagesUID = Extra.CURRENT_FRIEND_UID;
 		}
 	}
 
 	private void parseLinks() {
 		if (!Extra.CURRENT_FRIEND_UID.equals(Extra.sCurrentlyDisplayedLinksUID)) {
 			Extra.sLinks.clear();
 			Extra.sStatuses.clear();
 			Extra.sLinks = Extra.mFacebookData.getLinks(Extra.CURRENT_FRIEND_UID);
 			Collections.sort(Extra.sLinks);
 			for (Link link : Extra.sLinks) {
 				if (link.url != null && link.linkTitle != null) {
 					String linkUrl = link.url.replaceAll("gdata.youtube.com/feeds/api/videos/", "www.youtube.com/watch?v=");
 					Extra.sStatuses.add(Html.fromHtml(link.status + "\n<a href=" + linkUrl + ">" + link.linkTitle + "</a>"));
 				} else
 					Extra.sStatuses.add(Html.fromHtml(link.status));
 			}
 			Extra.sCurrentlyDisplayedLinksUID = Extra.CURRENT_FRIEND_UID;
 		}
 	}
 
 	private static class MegalikeAdapter extends FragmentStatePagerAdapter {
 		public MegalikeAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		public int getItemPosition(Object object) {
 			return POSITION_NONE;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			if (position == 0)
 				return PhotosTabFragment.newInstance(position % Extra.TAB_CONTENT.length);
 			else
 				return StatusesTabFragment.newInstance();
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return Extra.TAB_CONTENT[position % Extra.TAB_CONTENT.length].toUpperCase();
 		}
 
 		@Override
 		public int getCount() {
 			return Extra.TAB_CONTENT.length;
 		}
 	}
 }
