 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.jp.notifications;
 
 import android.app.FragmentTransaction;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
 import eu.trentorise.smartcampus.jp.Config;
 import eu.trentorise.smartcampus.jp.R;
 
 public class BroadcastNotificationsActivity extends FeedbackFragmentActivity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.empty_layout_jp);
 		
 		getSupportFragmentManager().popBackStack("notifications", FragmentManager.POP_BACK_STACK_INCLUSIVE);
 
 //		ActionBar.Tab tab = getSupportActionBar().newTab().setText(R.string.tab_broadcast);
 //		tab.setTabListener(new TabListener<BroadcastNotificationsFragment>(this, Config.PLAN_NEW_FRAGMENT_TAG,
 //				BroadcastNotificationsFragment.class, Config.mainlayout));
 //		getSupportActionBar().addTab(tab);
 
 //		tab = getSupportActionBar().newTab().setText(R.string.tab_news);
 //		tab.setTabListener(new TabListener<NewsFragment>(this, Config.MY_JOURNEYS_FRAGMENT_TAG, NewsFragment.class,
 //				Config.mainlayout));
 //		getSupportActionBar().addTab(tab);
 
 		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD)
 			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 		
 		android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
 		SherlockFragment fragment = new BroadcastNotificationsFragment();
 		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 		fragmentTransaction.replace(Config.mainlayout, fragment);
 		fragmentTransaction.commit();
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getSupportActionBar().setHomeButtonEnabled(true);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSupportActionBar().setTitle(R.string.title_broadcast);
 	}
 
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			onBackPressed();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 
 	@Override
 	public String getAppToken() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 	@Override
 	public String getAuthToken() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 }
