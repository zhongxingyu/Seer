 package com.example.sextoncalculator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class BrowseActivity extends ListActivity {
 	// protected Spinner spinner;
 	protected FoodData foodData;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		foodData = new FoodData(this);
 		// spinner = (Spinner) findViewById(R.id.spinner);
 		if (foodData.count() == 0) {
 			foodData.load();
 		}
 		Cursor cursor = foodData.all(this);
 		@SuppressWarnings("deprecation")
 		SimpleCursorAdapter foodCursorAdapter = new SimpleCursorAdapter(this,
 				R.layout.activity_browse_row, cursor, new String[] {
 						foodData.NAME, foodData.PRICE }, new int[] {
 						R.id.itemName_textView, R.id.itemPrice_textView });
 		setListAdapter(foodCursorAdapter);
 		// foodCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		setContentView(R.layout.activity_browse);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_browse, menu);
 		return true;
 	}
 
 	public void homeActivity(View view) {
 		Intent intent = new Intent(this, HomeActivity.class);
 		startActivity(intent);
 	}
 
 	public void checkoutActivity(View view) {
 		Intent intent = new Intent(this, CheckoutActivity.class);
 		startActivity(intent);
 	}
 
 	public void increaseQuantity(View v) {
 		RelativeLayout layout = (RelativeLayout) v.getParent();
 		TextView itemQuantity = (TextView) layout
 				.findViewById(R.id.itemQuantity_textView);
 		int quantity = Integer.parseInt(itemQuantity.getText().toString());
 		itemQuantity.setText(String.valueOf(quantity + 1));
 	}
 
 	public void decreaseQuantity(View v) {
 		RelativeLayout layout = (RelativeLayout) v.getParent();
 		TextView itemQuantity = (TextView) layout
 				.findViewById(R.id.itemQuantity_textView);
 		int quantity = Integer.parseInt(itemQuantity.getText().toString());
 		if (quantity > 0) {
 			itemQuantity.setText(String.valueOf(quantity - 1));
 		}
 	}
 
 	public List<FoodItem> generateFoodList() {
 		List<FoodItem> foodList = new ArrayList<FoodItem>();
 		TextView itemName;
 		TextView itemPrice;
 		TextView itemQuantity;
 		String name;
 		double price;
 		int quantity;
 		ListView list = getListView();
 		for (int i = 0; i < list.getChildCount(); i++) {
 			itemName = (TextView) list.getChildAt(i).findViewById(
 					R.id.itemName_textView);
 			itemPrice = (TextView) list.getChildAt(i).findViewById(
 					R.id.itemPrice_textView);
 			itemQuantity = (TextView) list.getChildAt(i).findViewById(
 					R.id.itemQuantity_textView);
 			name = itemName.getText().toString();
 			price = Double.parseDouble(itemPrice.getText().toString());
 			quantity = Integer.parseInt(itemQuantity.getText().toString());
			if (quantity > 0) {
				foodList.add(new FoodItem(name, price, quantity));
			}
 		}
 		return foodList;
 	}
 
 	public void calculateTotal(View v) {
 		List<FoodItem> foodList = generateFoodList();
 		RelativeLayout layout = (RelativeLayout) v.getParent();
 		TextView totalPrice = (TextView) layout
 				.findViewById(R.id.totalPrice_textView);
 		double total = 0;
		for (int i = 0; i < foodList.size(); i++) {
 			total += foodList.get(i).getPrice() * foodList.get(i).getQuantity();
 		}
 		totalPrice.setText("Total Price: $" + String.valueOf(total));
 	}
 }
