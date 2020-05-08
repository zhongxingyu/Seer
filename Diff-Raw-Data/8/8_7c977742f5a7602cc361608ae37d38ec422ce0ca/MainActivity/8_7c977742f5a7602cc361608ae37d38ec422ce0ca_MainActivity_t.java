 /*
  * Copyright (C) 2013 Moritz Heindl <lenidh[at]gmail[dot]com>
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.lenidh.android.holochron.ui;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.ViewPager;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import de.lenidh.android.holochron.App;
 import de.lenidh.android.holochron.R;
 import de.lenidh.android.holochron.adapters.LapArrayAdapter;
 import de.lenidh.android.holochron.adapters.LapPagerAdapter;
 import de.lenidh.android.holochron.controls.DigitalDisplay;
 import de.lenidh.libzeitmesser.stopwatch.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MainActivity extends SherlockFragmentActivity implements Display, SharedPreferences.OnSharedPreferenceChangeListener {
 	
 	// Widgets
 	private Button btnState;
 	private Button btnExtra;
 	private DigitalDisplay display;
 
 	private LapArrayAdapter elapsedTimeArrayAdapter;
 	private LapArrayAdapter lapTimeArrayAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 
 
 		/* content view */
 
 		if(App.getThemePreference().equals(getString(R.string.pref_value_theme_dark))) {
			setDarkContentView();
 		} else {
 			setContentView(R.layout.activity_main);
 		}
 
 
 
 		/* state button */
 
 		this.btnState = (Button)this.findViewById(R.id.btnState);
 		this.btnState.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				onStartStop();
 			}
 		});
 
 
 
 		/* extra button */
 
 		this.btnExtra = (Button)this.findViewById(R.id.btnExtra);
 		this.btnExtra.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				onResetRecord();
 			}
 		});
 
 
 
 		/* display */
 
 		this.display = (DigitalDisplay)this.findViewById(R.id.digitalDisplay1);
 
 
 
 		/* elapsed time Adapter */
 
 		List<Lap> elapsedTimeItems = App.getWatch().getLapContainer().toList();
 		this.elapsedTimeArrayAdapter = new LapArrayAdapter(this, App.getWatch().getLapContainer(), elapsedTimeItems, LapArrayAdapter.Mode.elapsedTime);
 
 
 
 		/* lap time Adapter */
 
 		List<Lap> lapTimeItems = App.getWatch().getLapContainer().toList(LapContainer.Order.lapTime);
 		this.lapTimeArrayAdapter = new LapArrayAdapter(this, App.getWatch().getLapContainer(), lapTimeItems, LapArrayAdapter.Mode.lapTime);
 
 
 
 		/* lap pages */
 
 		// Find pager view.
 		ViewPager lapPager = (ViewPager) this.findViewById(R.id.lapPager);
 		ArrayList<LapListFragment> pages = new ArrayList<LapListFragment>();
 
 		// Check if page was already created and create page if necessary.
 		LapListFragment elapsedTimeListFragment = (LapListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + lapPager.getId() + ":0");
 		if(elapsedTimeListFragment == null) elapsedTimeListFragment = new LapListFragment();
 		elapsedTimeListFragment.setListAdapter(this.elapsedTimeArrayAdapter);
 		pages.add(elapsedTimeListFragment);
 
 		// Check if page was already created and create page if necessary.
 		LapListFragment lapTimeListFragment = (LapListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + lapPager.getId() + ":1");
 		if(lapTimeListFragment == null) lapTimeListFragment = new LapListFragment();
 		lapTimeListFragment.setListAdapter(this.lapTimeArrayAdapter);
 		pages.add(lapTimeListFragment);
 
 		// Create the page adapter.
 		LapPagerAdapter lapPagerAdapter = new LapPagerAdapter(getSupportFragmentManager());
 		lapPagerAdapter.setPages(pages);
 
 		// Add page adapter to pager.
 		lapPager.setAdapter(lapPagerAdapter);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		App.getWatch().removeDisplay(this);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		updateTime();
 		updateLaps();
 		updateState();
 		App.getWatch().addDisplay(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		this.getSupportMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_item_settings:
 				this.startActivity(new Intent(this, SettingsActivity.class));
 				return true;
 			case R.id.menu_item_about:
 				this.startActivity(new Intent(this, AboutActivity.class));
 				return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		App.updateThemePreference();
 		finish();
 	}
 
 	@Override
 	public void updateLaps() {
 		this.lapTimeArrayAdapter.notifyDataSetChanged();
 		this.elapsedTimeArrayAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void updateState() {
 		this.runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				if (App.getWatch().isRunning()) {
 					btnState.setText(R.string.stop);
 					btnExtra.setText(R.string.lap);
 				} else {
 					btnState.setText(R.string.start);
 					btnExtra.setText(R.string.reset);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void updateTime() {
 		this.runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				display.setTime(App.getWatch().getElapsedTime());
 			}
 		});
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		/*if(this.getString(R.string.pref_key_lap_appearance).equals(key)) {
 			this.updatePages();
 		}/* else if(this.getString(R.string.pref_key_volume_buttons).equals(key)) {
 
 		} else if(this.getString(R.string.pref_key_theme).equals(key)) {
 
 		}*/
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 			case KeyEvent.KEYCODE_VOLUME_DOWN:
 				this.onVolumeDown();
 				return true;
 			case KeyEvent.KEYCODE_VOLUME_UP:
 				this.onVolumeUp();
 				return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
	private void setDarkContentView() {
 		setTheme(R.style.AppTheme_Dark);
 
		setContentView(R.layout.activity_main);
 
 		LinearLayout tile = (LinearLayout)this.findViewById(R.id.tile);
 		View hView = this.findViewById(R.id.hSeparator);
 		View vView = this.findViewById(R.id.vSeparator);
 
 		tile.setBackgroundResource(R.drawable.tile_shape_dark);
 		hView.setBackgroundResource(R.color.watch_button_separator_color_dark);
 		vView.setBackgroundResource(R.color.watch_button_separator_color_dark);
 	}
 
 	private void onStartStop() {
 		if(App.getWatch().isRunning()) {
 			App.getWatch().stop();
 		} else {
 			App.getWatch().start();
 		}
 	}
 
 	private void onResetRecord() {
 		if(App.getWatch().isRunning()) {
 			App.getWatch().record();
 		} else {
 			App.getWatch().reset();
 		}
 	}
 
 	private void onVolumeDown() {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		String volumeKeyPref = preferences.getString(this.getString(R.string.pref_key_volume_buttons), this.getString(R.string.pref_value_volume_buttons_ignore));
 
 		if(this.getString(R.string.pref_value_volume_buttons_use).equals(volumeKeyPref)) {
 			this.onResetRecord();
 		} else if(this.getString(R.string.pref_value_volume_buttons_inverse).equals(volumeKeyPref)) {
 			this.onStartStop();
 		}
 	}
 
 	private void onVolumeUp() {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		String volumeKeyPref = preferences.getString(this.getString(R.string.pref_key_volume_buttons), this.getString(R.string.pref_value_volume_buttons_ignore));
 
 		if(this.getString(R.string.pref_value_volume_buttons_use).equals(volumeKeyPref)) {
 			this.onStartStop();
 		} else if(this.getString(R.string.pref_value_volume_buttons_inverse).equals(volumeKeyPref)) {
 			this.onResetRecord();
 		}
 	}
 }
