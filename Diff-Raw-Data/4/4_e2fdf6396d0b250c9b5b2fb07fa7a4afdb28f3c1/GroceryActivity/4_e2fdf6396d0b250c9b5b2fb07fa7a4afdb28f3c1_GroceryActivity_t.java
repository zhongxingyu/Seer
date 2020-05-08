 package com.aj3.kiss;
 
 import java.util.List;
 
 import android.os.Bundle;
 import android.view.Menu;
 import com.aj3.kiss.R;
 
 public class GroceryActivity extends ItemListActivity {
 	public static final String NAME = "grocery";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_grocery);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		DatabaseHelper db = new DatabaseHelper(this);
		List<ListItem> listItems = db.getGrocery();
 		db.close();
 		
 		this.displayList(listItems);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.grocery, menu);
 		return true;
 	}
 
 }
