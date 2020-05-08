 /*
  * Copyright (C) 2012 ANDLABS. All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.andlabs.studiolounge;
 
 import android.content.ComponentName;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.widget.ImageView;
 import eu.andlabs.studiolounge.gcp.GCPService;
 import eu.andlabs.studiolounge.gcp.Lounge;
 import eu.andlabs.studiolounge.lobby.LoginManager;
 
 public class LoungeActivity extends FragmentActivity implements
 		OnPageChangeListener {
 	private static final float ALPHA_OFF = 0.3f;
 	private ViewPager mViewPager;
 	private Lounge mLounge;
 	private LoungeFragmentAdapter mAdapter;
 	private ImageView mLobbyIcon;
 	private ImageView mChatIcon;
 	private ImageView mStatsIcon;
 	private ImageView mAboutIcon;
 	private String mName;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// mLounge = new Lounge(this);
 
 		Log.i("Luc", "test");
 		setContentView(R.layout.main_pager);

 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mAdapter = new LoungeFragmentAdapter(getFragmentManager());
 		mViewPager.setAdapter(mAdapter);
 		mViewPager.setOnPageChangeListener(this);
 
 		mLobbyIcon = (ImageView) findViewById(R.id.ic_tab_lobby);
 		mChatIcon = (ImageView) findViewById(R.id.ic_tab_chat);
 		mStatsIcon = (ImageView) findViewById(R.id.ic_tab_stat);
 		mAboutIcon = (ImageView) findViewById(R.id.ic_tab_about);
 
 		 mName = LoginManager.getInstance(this).getUserId();
 	}
 
 	@Override
 	protected void onStart() {
 		Log.d("Lounge", "on START");
 		mLounge = GCPService.bind(this, mName);
 		super.onStart();
 	}
 
 	@Override
 	protected void onStop() {
 		Log.d("Lounge", "on STOP");
 		GCPService.unbind(this, mLounge);
 		super.onStop();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onPageScrollStateChanged(int arg0) {
 	}
 
 	@Override
 	public void onPageScrolled(int arg0, float arg1, int arg2) {
 	}
 
 	@Override
 	public void onPageSelected(int position) {
 		switch (position) {
 		case 0:
 			mLobbyIcon.setAlpha(1.0f);
 			mChatIcon.setAlpha(ALPHA_OFF);
 			mStatsIcon.setAlpha(ALPHA_OFF);
 			mAboutIcon.setAlpha(ALPHA_OFF);
 			break;
 
 		case 1:
 			mLobbyIcon.setAlpha(ALPHA_OFF);
 			mChatIcon.setAlpha(1.0f);
 			mStatsIcon.setAlpha(ALPHA_OFF);
 			mAboutIcon.setAlpha(ALPHA_OFF);
 
 			break;
 
 		case 2:
 			mLobbyIcon.setAlpha(ALPHA_OFF);
 			mChatIcon.setAlpha(ALPHA_OFF);
 			mStatsIcon.setAlpha(1.0f);
 			mAboutIcon.setAlpha(ALPHA_OFF);
 
 			break;
 
 		case 3:
 			mLobbyIcon.setAlpha(ALPHA_OFF);
 			mChatIcon.setAlpha(ALPHA_OFF);
 			mStatsIcon.setAlpha(ALPHA_OFF);
 			mAboutIcon.setAlpha(1.0f);
 
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 	public void hostGame(ComponentName hostComponent) {
 		this.mLounge.hostGame(hostComponent);
 	}
 	
 	public Lounge getLounge() {
 		return mLounge;
 	}
 }
