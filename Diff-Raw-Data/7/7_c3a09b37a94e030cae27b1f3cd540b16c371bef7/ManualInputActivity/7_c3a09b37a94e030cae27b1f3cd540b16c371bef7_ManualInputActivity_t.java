 package billsplit.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import billsplit.engine.Item;
 import billsplit.engine.Transaction;
 
 import com.billsplit.R;
 import com.billsplit.R.layout;
 import com.billsplit.R.menu;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class ManualInputActivity extends Activity {
 
 	private ItemAdapter adapter;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_manual_input);
 		
 		
 		adapter = new ItemAdapter(this,R.layout.item_description_price_row, Transaction.current.getItems(), Transaction.current.getItemsBools());
 		//adapter = new ItemAdapter(this,android.R.layout.simple_list_item_1, items);
 		 ListView items = (ListView) findViewById(R.id.manual_input_itemlist);
 		 items.setAdapter(adapter);
 		 
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.manual_input, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void btnAdd_clicked(View view){
 		EditText description= (EditText)this.findViewById(R.id.manual_input_txtDescription);
 		EditText price= (EditText)this.findViewById(R.id.manual_input_txtPrice);
 		
		double itemPrice = 0.0; 
		if ((price != null) && (price.getText().toString()=="")) {
			itemPrice = Double.valueOf(price.getText().toString());
		}
		
		Item item = new Item(description.getText().toString(), itemPrice);
 		
 		Transaction.current.addItem(item);
 		
 		adapter = new ItemAdapter(this,R.layout.item_description_price_row, Transaction.current.getItems(), Transaction.current.getItemsBools());
 		//adapter = new ItemAdapter(this,android.R.layout.simple_list_item_1, items);
 		 ListView items = (ListView) findViewById(R.id.manual_input_itemlist);
 		 items.setAdapter(adapter);
 	//	items.add(item);
 		adapter.notifyDataSetChanged();
 	//	Toast.makeText(getApplicationContext(), item.getName(), Toast.LENGTH_LONG).show();
 	}
 	
 	public void btnDone_clicked(View view)
 	{
 		/*
 		Transaction newTransaction = new Transaction(Transaction.current.getParticipants(),items);
 		Transaction.current = newTransaction;*/
 		finish();
 	}
 
 }
