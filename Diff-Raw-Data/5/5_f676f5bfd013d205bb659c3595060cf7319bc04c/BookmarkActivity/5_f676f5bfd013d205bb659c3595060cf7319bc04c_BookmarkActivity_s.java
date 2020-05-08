 package com.novel.reader;
 
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.SparseArray;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.widget.LinearLayout;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.ads.AdView;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.kosbrother.fragments.MyBookmarkFragment;
 import com.novel.reader.util.Setting;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class BookmarkActivity extends SherlockFragmentActivity{
 
    
     private boolean                              alertDeleteBookmark;
     SharedPreferences                            settings;
     private final String                         alertKey   = "alertDeleteBookmark";
     private String[]                  CONTENT;
     private static ViewPager                 pager;
     private static FragmentStatePagerAdapter adapter;
 	private static BookmarkActivity mActivity;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Setting.setApplicationActionBarTheme(this);
         setContentView(R.layout.simple_titles);
         
         mActivity = BookmarkActivity.this;
         
         final ActionBar ab = getSupportActionBar();
         ab.setTitle(getResources().getString(R.string.my_bookmark));
         ab.setDisplayHomeAsUpEnabled(true);
 
         Resources res = getResources();
         CONTENT = res.getStringArray(R.array.bookmarks);
 
         adapter = new NovelPagerAdapter(getSupportFragmentManager());
 
         pager = (ViewPager) findViewById(R.id.pager);
         pager.setAdapter(adapter);
 
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             if (extras.getBoolean("IS_RECNET")) {
                 pager.setCurrentItem(1);
             }
         }
         TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
         indicator.setViewPager(pager);
 
         settings = getSharedPreferences(Setting.keyPref, 0);
         alertDeleteBookmark = settings.getBoolean(alertKey, true);
         AdViewUtil.setBannerAdView((LinearLayout) findViewById(R.id.adonView), this);
         if (alertDeleteBookmark)
             showArticleDeleteDialog();
     }
     
     class NovelPagerAdapter extends FragmentStatePagerAdapter {
     	
     	SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
     	
         public NovelPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             Fragment kk = new Fragment();
             if (position == 0) {
                 kk = MyBookmarkFragment.newInstance(MyBookmarkFragment.BOOKMARK_VIEW);
             } else if (position == 1) {
                 kk = MyBookmarkFragment.newInstance(MyBookmarkFragment.RECENT_READ_VIEW);
             }
             registeredFragments.put(position, kk);
             return kk;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return CONTENT[position % CONTENT.length];
         }
 
         @Override
         public int getCount() {
             return CONTENT.length;
         }
         
         public Fragment getRegisteredFragment(int position) {
             return registeredFragments.get(position);
         }
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         int itemId = item.getItemId();
         switch (itemId) {
         case android.R.id.home:
             finish();
             break;
         }
         return true;
     }
 
 
     
 
     private void showArticleDeleteDialog() {
         new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.reminder)).setIcon(R.drawable.ic_stat_notify)
                 .setMessage(getResources().getString(R.string.delete_bookmark_reminder))
                 .setPositiveButton(getResources().getString(R.string.do_not_reminder), new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface arg0, int arg1) {
                         settings.edit().putBoolean(alertKey, false).commit();
 
                     }
 
                 }).setNegativeButton(getResources().getString(R.string.reminder_next), new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface arg0, int arg1) {
 
                     }
 
                 }).show();
 
     }
 
 
     public void rotationHoriztion(int beganDegree, int endDegree, AdView view) {
         final float centerX = 320 / 2.0f;
         final float centerY = 48 / 2.0f;
         final float zDepth = -0.50f * view.getHeight();
 
         Rotate3dAnimation rotation = new Rotate3dAnimation(beganDegree, endDegree, centerX, centerY, zDepth, true);
         rotation.setDuration(1000);
         rotation.setInterpolator(new AccelerateInterpolator());
         rotation.setAnimationListener(new Animation.AnimationListener() {
             public void onAnimationStart(Animation animation) {
             }
 
             public void onAnimationEnd(Animation animation) {
             }
 
             public void onAnimationRepeat(Animation animation) {
             }
         });
         view.startAnimation(rotation);
     }
     
     @Override
     public void onStart() {
       super.onStart();
       EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
       super.onStop();
       EasyTracker.getInstance().activityStop(this);
     }
     
     public ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
 
 	      // Called when the action mode is created; startActionMode() was called
 	      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 	          // Inflate a menu resource providing context menu items
 	          MenuInflater inflater = mode.getMenuInflater();
 	          // Assumes that you have "contexual.xml" menu resources
 	          inflater.inflate(R.menu.contextual, menu);
 	          
 	          return true;
 	      }
 	
 	      // Called each time the action mode is shown. Always called after
 	      // onCreateActionMode, but
 	      // may be called multiple times if the mode is invalidated.
 	      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 	          return false; // Return false if nothing is done
 	      }
 	
 	      // Called when the user selects a contextual menu item
 	      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 	    	  MyBookmarkFragment fragment1 = (MyBookmarkFragment) ((NovelPagerAdapter) adapter).getRegisteredFragment(0);
         	  MyBookmarkFragment fragment2 = (MyBookmarkFragment) ((NovelPagerAdapter) adapter).getRegisteredFragment(1);
 	          switch (item.getItemId()) {
 	          case R.id.delete_articles:
 	        	  fragment1.deleteAndReload();
 	        	  fragment2.deleteAndReload();
 	        	  mode.finish();
 	              return true;
 	          default:
 	        	  fragment1.resetIsShowDeleteCallbackAction();
 	        	  fragment2.resetIsShowDeleteCallbackAction();
 	              return false;
 	          }
 	      }
 	
 	      // Called when the user exits the action mode
 	      public void onDestroyActionMode(ActionMode mode) {
	          // mActionMode = null;
 	
 	      }
 	};
 	private ActionMode mActionMode;
 	  
     public void showCallBackAction() {
     	mActionMode = mActivity.startActionMode(mActionModeCallback);  
     }
     
     public void closeActionMode(){
     	if (mActionMode!=null){
     		mActionMode.finish();
     		MyBookmarkFragment fragment1 = (MyBookmarkFragment) ((NovelPagerAdapter) adapter).getRegisteredFragment(0);
       	  	MyBookmarkFragment fragment2 = (MyBookmarkFragment) ((NovelPagerAdapter) adapter).getRegisteredFragment(1);
       	    fragment1.isShowDeleteCallbackAction = false;
       	    fragment2.isShowDeleteCallbackAction = false;
     	}
     }
     
 
 }
