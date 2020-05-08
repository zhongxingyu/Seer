 package com.example.anr_pad;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.view.WindowManager;
 
 public class MainActivity extends SherlockFragmentActivity  implements ActionBar.TabListener {
 
     private RunnerFragment runnerFrag;
     private CorpFragment corpFrag;
     int firstTime;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		final ActionBar ab = getSupportActionBar();
 
         // set defaults for logo & home up
         ab.setDisplayHomeAsUpEnabled(false);
         ab.setDisplayUseLogoEnabled(false);
 
         // create a couple of simple fragments as placeholders
         final int MARGIN = 16;
         runnerFrag = new RunnerFragment(getResources().getColor(
                 R.color.android_green), 1f, MARGIN, MARGIN / 2, MARGIN, MARGIN);
         corpFrag = new CorpFragment(getResources().getColor(
                 R.color.honeycombish_blue), 2f, MARGIN / 2, MARGIN, MARGIN,
                 MARGIN);
 
        ab.addTab(ab.newTab().setText("Runner").setTabListener(this));
        ab.addTab(ab.newTab().setText("Corp").setTabListener(this));
        showTabsNav();

         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         ft.add(R.id.root, runnerFrag);
         ft.add(R.id.root, corpFrag);
         ft.commit();
         firstTime = 0;
 	}
 
     private void showTabsNav() {
         ActionBar ab = getSupportActionBar();
         if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
             ab.setDisplayShowTitleEnabled(false);
             ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         }
     }
 
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
         switch (tab.getPosition()) {
         case 0:
             ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,
                                    android.R.anim.fade_in,android.R.anim.fade_out);
             ft.show(runnerFrag);
             break;
         case 1:
             ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,
                                    android.R.anim.fade_in,android.R.anim.fade_out);
             ft.show(corpFrag);
             break;
         }
     }
 
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
         switch (tab.getPosition()) {
         case 0:
             ft.hide(runnerFrag);
             break;
         case 1:
             ft.hide(corpFrag);
             break;
         }
     }
 
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
         // FIXME implement this
     }
 }
