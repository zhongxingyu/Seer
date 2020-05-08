 package com.openfridge;
 
 import android.app.Activity;
import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public class ItemEditActivity extends Activity {
	private Intent expirationList;
 
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
        expirationList = new Intent(this, ExpirationListActivity.class);
        
         setContentView(R.layout.item_edit);
         
         Spinner spinner = (Spinner) findViewById(R.id.common_items_dropdown);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.Common_items, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
     }
 	
 	public void doneEditClick(View view){
 		//if (getIntent().getAction().equals("com.openfridge.expirationList")) {
		startActivity(expirationList);
 	}
 }
