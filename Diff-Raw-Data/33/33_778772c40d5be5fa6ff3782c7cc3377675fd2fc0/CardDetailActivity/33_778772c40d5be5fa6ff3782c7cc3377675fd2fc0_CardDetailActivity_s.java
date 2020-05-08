 package com.bubbletastic.prayercards;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 
 public class CardDetailActivity extends FragmentActivity implements CardDetailFragmentAccess {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_card_detail);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getOrCreateDetailsFragment();
     }
     
     private void getOrCreateDetailsFragment() {
     	
     	CardDetailFragment fragment = null;
     	if (getSupportFragmentManager().findFragmentByTag("0") != null) {
     		fragment = (CardDetailFragment) getSupportFragmentManager().findFragmentByTag("0");
     	} else {
     		Bundle arguments = new Bundle();
     		arguments.putInt(CardDetailFragment.ARG_ITEM_ID, getIntent().getIntExtra(CardDetailFragment.ARG_ITEM_ID, 0));
         	
     		fragment = new CardDetailFragment();
     		fragment.setArguments(arguments);
 
     		getSupportFragmentManager().beginTransaction()
     		.add(R.id.card_detail_container, fragment, "0")
     		.commit();
     	}
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, CardListActivity.class));
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
 	@Override
 	public void setActivityTitle(String title) {
 		setTitle(title);
 	}
 }
