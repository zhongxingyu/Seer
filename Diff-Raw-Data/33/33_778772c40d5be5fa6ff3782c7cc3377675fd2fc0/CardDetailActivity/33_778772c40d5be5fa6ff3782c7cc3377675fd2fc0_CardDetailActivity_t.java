 package com.bubbletastic.prayercards;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
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
        	Intent intent = new Intent(this, CardListActivity.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.push_in_from_left, R.anim.push_out_to_right);
            finish();
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
 	@Override
 	public void setActivityTitle(String title) {
 		setTitle(title);
 	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.push_in_from_left, R.anim.push_out_to_right);
	}
 }
