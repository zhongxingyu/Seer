 package net.solutinno.websearch;
 
 import android.app.Activity;
 import android.content.Intent;
import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 
 import net.solutinno.util.SoftKeyboardHelper;
 import net.solutinno.websearch.data.SearchEngine;
 import net.solutinno.websearch.data.SearchEngineCursor;
 
 import java.util.UUID;
 
 public class MainActivity extends ActionBarActivity {
 
     int mActiveFragment = 0;
 
     Menu mMainMenu;
     DrawerLayout mDrawerLayout;
     boolean mHasDrawerLayout;
 
     ListFragment mListFragment;
     DetailFragment mDetailFragment;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mListFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_list);
         mListFragment.RegisterSelectItemListener(mSelectItemListener);
 
         mDetailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_detail);
 
         mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
         mHasDrawerLayout = mDrawerLayout != null;
 
         if (mHasDrawerLayout) {
             mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
             mDrawerLayout.setDrawerListener(mDrawerListener);
             mListFragment.RegisterSelectItemListener(mDetailFragment);
             mDetailFragment.SetDetailController(mDetailController);
 
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int orientation = getResources().getConfiguration().orientation;
             final View detail = mDrawerLayout.getChildAt(1);
            if (detail != null && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                 ViewGroup.LayoutParams layoutParams = detail.getLayoutParams();
                 if (layoutParams != null) {
                     layoutParams.width = (metrics.widthPixels / 3) * 2;
                     detail.setLayoutParams(layoutParams);
                 }
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.clear();
         if (mMainMenu == null) mMainMenu = menu;
         if (mActiveFragment == 0) {
             getSupportActionBar().setIcon(R.drawable.ic_launcher);
             getSupportActionBar().setTitle(R.string.app_name);
             getMenuInflater().inflate(R.menu.main, mMainMenu);
         }
         else {
             getMenuInflater().inflate(R.menu.detail, mMainMenu);
         }
         return super.onCreateOptionsMenu(mMainMenu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId())
         {
             case R.id.action_add:
                 Add();
                 break;
             default:
                 if (mHasDrawerLayout) {
                     mDetailFragment.onOptionsItemSelected(item);
                 }
                 break;
         }
         return true;
     }
 
     private Activity getActivity() {
         return this;
     }
 
     ListFragment.SelectItemListener mSelectItemListener = new ListFragment.SelectItemListener() {
         @Override
         public void onSelectItem(UUID id) {
             if (mHasDrawerLayout) {
                 mDrawerLayout.openDrawer(GravityCompat.END);
             }
             else {
                 Intent detail = new Intent(getBaseContext(), DetailActivity.class);
                 detail.putExtra(SearchEngineCursor.COLUMN_ID, id.toString());
                 startActivityForResult(detail, 0);
             }
         }
     };
 
     DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
 
         @Override
         public void onDrawerOpened(View view) {
             mActiveFragment = 1;
             onCreateOptionsMenu(mMainMenu);
             getSupportActionBar().setDisplayHomeAsUpEnabled(true);
             getSupportActionBar().setHomeButtonEnabled(true);
         }
 
         @Override
         public void onDrawerClosed(View view) {
             mDetailFragment.ClearFields();
             mActiveFragment = 0;
             onCreateOptionsMenu(mMainMenu);
             mListFragment.getLoaderManager().getLoader(0).forceLoad();
             getSupportActionBar().setDisplayHomeAsUpEnabled(false);
             getSupportActionBar().setHomeButtonEnabled(false);
             SoftKeyboardHelper.CloseSoftKeyboard(getActivity());
         }
 
         @Override
         public void onDrawerSlide(View view, float v) {
         }
 
         @Override
         public void onDrawerStateChanged(int i) {
         }
     };
 
     DetailFragment.DetailController mDetailController = new DetailFragment.DetailController() {
         @Override
         public void OnDetailFinish(int mode, SearchEngine engine) {
 
             mDrawerLayout.closeDrawer(GravityCompat.END);
 
             switch (mode)
             {
                 case DetailFragment.MODE_CANCEL:
                     break;
                 case DetailFragment.MODE_UPDATE:
                     break;
                 case DetailFragment.MODE_DELETE:
                     break;
             }
         }
     };
 
     private void Add() {
         if (mHasDrawerLayout) {
             mDetailFragment.onSelectItem(null);
             mDrawerLayout.openDrawer(GravityCompat.END);
         }
         else {
             Intent detail = new Intent(this, DetailActivity.class);
             startActivity(detail);
         }
     }
 }
