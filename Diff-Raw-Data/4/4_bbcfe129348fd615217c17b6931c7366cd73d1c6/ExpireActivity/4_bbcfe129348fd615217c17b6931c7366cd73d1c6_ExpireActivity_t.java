 package com.openfridge;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 
 //TODO Add the name of the item to the activity    ZH
 //TODO Make the add to list push to shopping list  EL
 //TODO Change add to list to add to shopping list  EL
 //TODO Log action (Thrown/Eaten)                   EL
 //TODO Thrown/Eaten should delete from web         EL
 //TODO Pass item info to edit/postpone             ZH
 //TODO Make edit/postpone button link to activity  ZH
 //TODO Make cancel button work                     ZH
 
 public class ExpireActivity extends Activity {
 	
 	private Intent expirationList, itemEdit;
 
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         expirationList = new Intent(this, ExpirationListActivity.class);
         itemEdit = new Intent(this, ItemEditActivity.class);
 
         setContentView(R.layout.expire);
 	}
 	
 	public void EditPostponeClick(View view){
 		startActivity(itemEdit);
 	}
 	
 	public void DoneClick(View view){
 		startActivity(expirationList);
 	}
 	
 	public void CancelClick(View view){
		finish();
 	}
 }
