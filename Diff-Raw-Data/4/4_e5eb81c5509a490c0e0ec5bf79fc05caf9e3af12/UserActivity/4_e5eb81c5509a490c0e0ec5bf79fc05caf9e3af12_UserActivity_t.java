 package com.lunatech.example.sietse.StackSearch;
 
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
import android.view.View;
 
 public class UserActivity extends Activity implements UserListFragment.Callbacks {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.user_list);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     public void onUserSelected(long rowid) {
        final UserDetailFragment detailFragment = getDetailFragment();
 
       if (detailFragment != null && detailFragment.isResumed())
           detailFragment.switchUser(rowid);
        else {
           final Intent intent = new Intent(this, UserDetailActivity.class);
           intent.putExtra(UserDetailActivity.USER_ID, rowid);
           startActivity(intent);
        }
     }
 
    private UserDetailFragment getDetailFragment() {
       return (UserDetailFragment) getFragmentManager().findFragmentById(R.id.user_detail_container);
    }
 
    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                finish();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
    @Override
    protected void onSaveInstanceState(Bundle outState) {
       super.onSaveInstanceState(outState);
 
       Log.wtf("UserActivity", "onSaveInstanceState");
    }
 
    @Override
    protected void onDestroy() {
       super.onDestroy();
 
       Log.wtf("UserActivity", "onDestroy");
    }
 }
