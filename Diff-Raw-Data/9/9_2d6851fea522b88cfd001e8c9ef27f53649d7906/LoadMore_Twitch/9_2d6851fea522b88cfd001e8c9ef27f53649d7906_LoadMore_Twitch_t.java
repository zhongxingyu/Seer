 package com.wwyz.loltv.loadMore;
 
 import java.util.List;
 
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.wwyz.loltv.FlashInstallerActivity;
 import com.wwyz.loltv.R;
 import com.wwyz.loltv.TwitchPlayer;
 import com.wwyz.loltv.R.drawable;
 import com.wwyz.loltv.feedManager.FeedManager_Twitch;
 
 public class LoadMore_Twitch extends LoadMore_Base implements
 SearchView.OnQueryTextListener {
 	@Override
 	public void Initializing() {
 		// Give a title for the action bar
 		abTitle = "Twitch LoL Streams";
 
 		// Give API URLs
 		API.add("https://api.twitch.tv/kraken/streams?game=League%20of%20Legends&limit=10");
 
 		// set a feed manager
 		feedManager = new FeedManager_Twitch();
 
 		// Show menu
 		setHasOptionsMenu(true);
 		setOptionMenu(true, false);
 
 		
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		
 		SearchView searchView = new SearchView(sfa.getSupportActionBar()
 				.getThemedContext());
 		searchView.setQueryHint("Search Twitch.tv");
 		searchView.setOnQueryTextListener(this);
 
 		menu.add(0,20,0,"Search")
				.setIcon(R.drawable.abs__ic_search)
 				.setActionView(searchView)
 				.setShowAsAction(
 						MenuItem.SHOW_AS_ACTION_IF_ROOM
 								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 
 		menu.add(0,0,0,"Refresh")
 				.setIcon(R.drawable.ic_refresh)
 				.setShowAsAction(
 						MenuItem.SHOW_AS_ACTION_IF_ROOM
 								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 
 //			Toast.makeText(this.getSherlockActivity(), videos.get(position),
 //					Toast.LENGTH_SHORT).show();
 
 			if (check()){
 				Intent i = new Intent(this.getSherlockActivity(), TwitchPlayer.class);
 				i.putExtra("video", videolist.get(position-1).getVideoId());
 				startActivity(i);
 
 			}else{
 				Intent i = new Intent(this.getSherlockActivity(), FlashInstallerActivity.class);
 				startActivity(i);
 			}
 	}
 
 	private boolean check() {
 		PackageManager pm = sfa.getPackageManager();
 		List<PackageInfo> infoList = pm
 				.getInstalledPackages(PackageManager.GET_SERVICES);
 		for (PackageInfo info : infoList) {
 			if ("com.adobe.flashplayer".equals(info.packageName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 		// start search activity
 		Intent intent = new Intent(sfa, LoadMore_Activity_Search_Twitch.class);
 		intent.putExtra("query", query);
 		startActivity(intent);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
