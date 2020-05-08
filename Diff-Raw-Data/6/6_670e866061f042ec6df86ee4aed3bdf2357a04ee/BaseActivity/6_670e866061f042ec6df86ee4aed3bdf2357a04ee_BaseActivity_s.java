 package edu.nkuresearch.securitychecker;
 
 import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
 
 public class BaseActivity extends SherlockFragmentActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
 		return super.onPrepareOptionsMenu(menu);
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
 }
