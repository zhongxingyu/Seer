 package com.example.example.masterdetail_tabs;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 
 public class ItemListActivity extends FragmentActivity
     implements ItemListFragment.Callbacks {
 
  private boolean mTwoPane;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
    // THIS IS ALWAYS NULL. Why?
    if (findViewById(R.id.item_detail_container) != null) {
      mTwoPane = true;

      ((ItemListFragment) getSupportFragmentManager()
          .findFragmentById(R.id.item_list))
          .setActivateOnItemClick(true);
    }

     ActionBar actionBar = getActionBar();
     actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
     ActionBar.Tab tab1 = actionBar.newTab();
     tab1.setText("Tab1");
     tab1.setTabListener(new TabListener<Tab1Fragment>(this, "tab1", Tab1Fragment.class));
     actionBar.addTab(tab1);
 
     ActionBar.Tab tab2 = actionBar.newTab();
     tab2.setText("Tab2");
     tab2.setTabListener(new TabListener<Tab2Fragment>(this, "tab2", Tab2Fragment.class));
     actionBar.addTab(tab2);
   }
 
   @Override
   public void onItemSelected(String id) {
     if (mTwoPane) {
       Bundle arguments = new Bundle();
       arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
       ItemDetailFragment fragment = new ItemDetailFragment();
       fragment.setArguments(arguments);
       getSupportFragmentManager().beginTransaction()
           .replace(R.id.item_detail_container, fragment)
           .commit();
 
     } else {
       Intent detailIntent = new Intent(this, ItemDetailActivity.class);
       detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
       startActivity(detailIntent);
     }
   }
 
   public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
     private Fragment mFragment;
     private final Activity mActivity;
     private final String mTag;
     private final Class<T> mClass;
 
     public TabListener(Activity activity, String tag, Class<T> clz) {
       mActivity = activity;
       mTag = tag;
       mClass = clz;
     }
 
     public void onTabSelected(Tab tab, FragmentTransaction ft) {
       if (mFragment == null) {
         mFragment = Fragment.instantiate(mActivity, mClass.getName());
         ft.add(android.R.id.content, mFragment, mTag);
       } else {
         ft.attach(mFragment);
       }
     }
 
     public void onTabUnselected(Tab tab, FragmentTransaction ft) {
       if (mFragment != null) {
         ft.detach(mFragment);
       }
     }
 
     public void onTabReselected(Tab tab, FragmentTransaction ft) {
     }
   }
 
 }
