 package com.cesarandres.ps2link;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.cesarandres.ps2link.base.BaseActivity;
 import com.cesarandres.ps2link.base.BaseFragment.FragmentCallbacks;
 import com.cesarandres.ps2link.fragments.FragmentFriendList;
 import com.cesarandres.ps2link.fragments.FragmentKillList;
 import com.cesarandres.ps2link.fragments.FragmentMembersList;
 import com.cesarandres.ps2link.fragments.FragmentMembersOnline;
 import com.cesarandres.ps2link.fragments.FragmentProfile;
 import com.cesarandres.ps2link.fragments.FragmentStatList;
 import com.cesarandres.ps2link.module.ObjectDataSource;
 
 /**
  * Created by cesar on 6/16/13.
  */
 public class ActivityOutfit extends BaseActivity implements FragmentCallbacks {
 
 	private ObjectDataSource data;
 	private SectionsPagerAdapter mSectionsPagerAdapter;
 	private ViewPager mViewPager;
 	private String outfitId;
 	private static final int ONLINE = 0;
 	private static final int MEMBERS = 1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_outfit);
 
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 		mViewPager = (ViewPager) findViewById(R.id.outfitPager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		findViewById(R.id.buttonFragmentUpdate).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				String tag;
 				Fragment fragment;
 				try{
 					switch (mViewPager.getCurrentItem()) {
 					case ONLINE:
						tag = ApplicationPS2Link.makeFragmentName(R.id.outfitPager, ONLINE);
 						fragment = ((FragmentMembersOnline) getSupportFragmentManager().findFragmentByTag(tag));
 						((FragmentMembersOnline) fragment).downloadOutfitMembers();
 						break;
 					case MEMBERS:
						tag = ApplicationPS2Link.makeFragmentName(R.id.outfitPager, MEMBERS);
 						fragment = ((FragmentMembersList) getSupportFragmentManager().findFragmentByTag(tag));
 						((FragmentMembersList) fragment).downloadOutfitMembers();
 						break;
 					default:
 						break;
 					}
 				}catch(Exception e){
 					Toast.makeText(getApplicationContext(), "There was a problem trying to refresh. Please try again.", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 
 		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
 
 			@Override
 			public void onPageSelected(int arg0) {
 				switch (arg0) {
 				case ONLINE:
 					findViewById(R.id.toggleShowOffline).setVisibility(View.GONE);
 					break;
 				case MEMBERS:
 					findViewById(R.id.toggleShowOffline).setVisibility(View.VISIBLE);
 					break;
 				default:
 					findViewById(R.id.toggleShowOffline).setVisibility(View.VISIBLE);
 					break;
 				}
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 
 			}
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 
 			}
 		});
 
 		Bundle extras = getIntent().getExtras();
 		String activityMode = "";
 		if (extras != null) {
 			activityMode = extras.getString(ApplicationPS2Link.ACTIVITY_MODE_KEY);
 		}
 		outfitId = extras.getString("PARAM_0");
 
 		setData(new ObjectDataSource(this));
 		data.open();
 
 		Button titleBack = (Button) findViewById(R.id.buttonFragmentTitle);
 		titleBack.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				navigateUp();
 			}
 		});
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		data.close();
 	}
 
 	public ObjectDataSource getData() {
 		return data;
 	}
 
 	public void setData(ObjectDataSource data) {
 		this.data = data;
 	}
 
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Fragment fragment = null;
 			switch (position) {
 			case ONLINE:
 				fragment = new FragmentMembersOnline();
 				break;
 			case MEMBERS:
 				fragment = new FragmentMembersList();
 				break;
 			default:
 				break;
 			}
 			Bundle args = new Bundle();
 			args.putString("PARAM_0", outfitId);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case ONLINE:
 				return "ONLINE";
 			case MEMBERS:
 				return "MEMBERS";
 			default:
 				return "ONLINE";
 			}
 		}
 	}
 
 	@Override
 	public void onItemSelected(String id, String args[]) {
 		Class newActivityClass = ActivityContainerSingle.getActivityByMode(id);
 		if (newActivityClass != null) {
 		} else {
 			newActivityClass = ActivityContainerSingle.class;
 		}
 		Intent intent = new Intent(this, newActivityClass);
 		if (args != null && args.length > 0) {
 			for (int i = 0; i < args.length; i++) {
 				intent.putExtra("PARAM_" + i, args[i]);
 			}
 		}
 		intent.putExtra(ApplicationPS2Link.ACTIVITY_MODE_KEY, id);
 		startActivity(intent);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			finish();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
