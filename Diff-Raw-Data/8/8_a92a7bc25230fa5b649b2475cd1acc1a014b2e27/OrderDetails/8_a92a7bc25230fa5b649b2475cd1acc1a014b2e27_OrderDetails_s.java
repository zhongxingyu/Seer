 package com.ordrupapp.ordrup;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.GridLayout;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class OrderDetails extends Activity {
 
 
 	GridLayout layout;
 	int orderNumber, currentTableIndex;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_order_details);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 
 			currentTableIndex = extras.getInt("tableIndex");
 			orderNumber = extras.getInt("orderNumber");
 			
 			((TextView)findViewById(R.id.order_details_title)).setText("Order " + (orderNumber + 1));
 			
 			getOrderItems((View)findViewById(R.id.order_details_list));
 
 			//Make the button invisible and unclickable if the order has been submitted
 			if (sessionInfo.getInstance().getTables().get(currentTableIndex).getOrders().get(orderNumber).wasSubmitted()){
 				((Button)findViewById(R.id.order_details_submit_order_button)).setClickable(false);
 				((Button)findViewById(R.id.order_details_submit_order_button)).setVisibility(Button.INVISIBLE);
 			}
 
 
 
 		}
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
 		//		getMenuInflater().inflate(R.menu.order_details, menu);
 		//		return true;
 		//	}
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.settings_menu, menu);
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
 		case R.id.settings_logoff:
 			sessionInfo.getInstance().clear();
 			Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
 			startActivity(intent);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void getOrderItems(View view){
 		sessionInfo mySession = sessionInfo.getInstance();
 
 		GridLayout layout = (GridLayout) view.findViewById(R.id.order_details_list);
 
 		//clear the view
 		layout.removeAllViews();
 
 		layout.setColumnCount(3);
 
 		//declare the fields
 		TextView orderItemName;
 		Button removeItem;
 		TextView orderItemNotes;
 		TextView status;
 		String orderItemNotesString;
 
 		View.OnClickListener btnHandler = new View.OnClickListener() {
 			public void onClick(View v) {
 				int orderItemIndex = Integer.parseInt(v.getTag().toString());
 				sessionInfo.getInstance().getTables().get(currentTableIndex).getOrders().get(orderNumber).getOrderItems().remove(orderItemIndex);
 				((Button)v).setClickable(false);
 				((Button)v).setVisibility(Button.INVISIBLE);
 				v.invalidate();
 
				//removing last orderItem causes an error
 
 			}
 		};
 
 		for (int i = 0; i < mySession.getTables().get(currentTableIndex).getOrders().get(orderNumber).getOrderItemCount(); i++){
 			
 			//add the remove button if the order has not been submitted
 			if (!mySession.getTables().get(currentTableIndex).getOrders().get(orderNumber).wasSubmitted()){
 				
 				removeItem = new Button(view.getContext());
 				removeItem.setText("-");
 				removeItem.setOnClickListener(btnHandler);
 				removeItem.setTag(i);
 				removeItem.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
 				layout.addView(removeItem);
 			}
 
 			//get the name
 			orderItemName = new TextView(view.getContext());
 			orderItemName.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
 			orderItemName.setText (
 					mySession.getTables()
 					.get(currentTableIndex)
 					.getOrders()
 					.get(orderNumber)
 					.getOrderItems()
 					.get(i).getName());
 			layout.addView(orderItemName);
 
 			//get the notes
 			orderItemNotesString = mySession.getTables()
 					.get(currentTableIndex)
 					.getOrders()
 					.get(orderNumber)
 					.getOrderItems()
 					.get(i)
 					.getNotes();
 			
 			orderItemNotes = new TextView(view.getContext());
 			orderItemNotes.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
 			
 			System.out.println("Notes: " + orderItemNotesString);
 			
 			//add notes if any
 			if (!orderItemNotesString.isEmpty()) {
 				orderItemNotes.setText(" - " + orderItemNotesString);
 			}
 			
 			layout.addView(orderItemNotes);
 
 			
 			if (mySession.getTables().get(currentTableIndex).getOrders().get(orderNumber).wasSubmitted()){
 				status = new TextView(view.getContext());
 				status.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
 				status.setText (" - " +
 						mySession.getTables()
 						.get(currentTableIndex)
 						.getOrders()
 						.get(orderNumber)
 						.getOrderItems()
 						.get(i).getStatus());
 				layout.addView(status);
 			}
 
 
 		}
 
 	}
 
 	public void submitOrder(View view){
 		System.out.println(currentTableIndex + " " + orderNumber);
 		boolean submitted = sessionInfo.getInstance().getTables().get(currentTableIndex).getOrders().get(orderNumber).submitOrder();
 
 		//check failure state
 		if(!submitted){
 			Toast.makeText(getApplicationContext(), "Error: Failed to submit order", Toast.LENGTH_LONG).show();
 		}
 		else{
 			Toast.makeText(getApplicationContext(), "Success: Order Submitted", Toast.LENGTH_LONG).show();
 			refresh();
 		}
 		
 		
 	}
 	
 	public void refresh(){
 		Intent intent = getIntent();
 	    finish();
 	    startActivity(intent);
 	}
 
 }
