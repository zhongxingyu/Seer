 package net.tailriver.agoraguide;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 public class AgoraGuideActivity extends AgoraActivity implements OnClickListener {
 	@Override
 	public void onPreInitialize() {
 		setContentView(R.layout.main);
 	}
 
 	@Override
 	public void onPostInitialize(Bundle savedInstanceState) {
		// TODO provides appropriate icons
 		ViewGroup vg = ((ViewGroup) findViewById(R.id.main_buttons));
 		for (int i = 0, max = vg.getChildCount(); i < max; i++) {
 			View v = vg.getChildAt(i);
 			v.setOnClickListener(this);
 		}
 
 		// update alarm
 		for (EntrySummary summary : Favorite.values()) {
 			Favorite.setFavorite(summary, true);
 		}
 	}
 
 	public void onClick(View v) {
 		try {
 			jumpNextActivity(v.getId());
 		} catch (UnsupportedOperationException e) {
 			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		try {
 			jumpNextActivity(item.getItemId());
 			return true;
 		} catch (UnsupportedOperationException e) {
 			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
 			return false;
 		}
 	}
 
 	private final void jumpNextActivity(int resID) {
 		Context applicationContext = getApplicationContext();
 		Intent intent = new Intent();
 		switch (resID) {
 		case R.id.button_search_keyword:
 		case R.id.menuSearchKeyword:
 			intent.setClass(applicationContext, SearchActivity.class);
 			intent.putExtra(IntentExtra.SEARCH_TYPE, SearchActivity.SearchType.Keyword);
 			break;
 		case R.id.button_search_schedule:
 		case R.id.menuSearchSchedule:
 			intent.setClass(applicationContext, SearchActivity.class);
 			intent.putExtra(IntentExtra.SEARCH_TYPE, SearchActivity.SearchType.Schedule);
 			break;
 		case R.id.button_search_area:
 		case R.id.menuSearchArea:
 			intent.setClass(applicationContext, AreaSearchIndexActivity.class);
 			break;
 		case R.id.button_search_favorite:
 		case R.id.menuFavorites:
 			intent.setClass(applicationContext, SearchActivity.class);
 			intent.putExtra(IntentExtra.SEARCH_TYPE, SearchActivity.SearchType.Favorite);
 			break;
 		case R.id.button_information:
 		case R.id.menu_credits:
 			intent.setClass(applicationContext, CreditsActivity.class);
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 		startActivity(intent);
 	}
 }
