 package edu.upenn.cis.fruity;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import edu.upenn.cis.fruity.database.DatabaseHandler;
 import edu.upenn.cis.fruity.database.EndInventoryItem;
 import edu.upenn.cis.fruity.database.FruitStand;
 import edu.upenn.cis.fruity.database.ProcessedInventoryItem;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SalesPaymentActivity extends Activity {
 
 	public static final int SalesPaymentActivity_ID = 13;
 	private Double total = 0.0;
 	private FruitTuple[] purchasedItems;
 	private Integer numCoupons = 0;
 	private Integer numTradeIns = 0;
 	private Intent intent;
 	private FruitStand currStand;
 	
 	public enum PaymentType {
 		CASH, COUPON, TRADEIN
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		intent = getIntent();
 		DatabaseHandler dht = DatabaseHandler.getInstance(this);
 		currStand = dht.getCurrentFruitStand();
 		
 		ArrayList<FruitTuple> itemBuffer = new ArrayList<FruitTuple>();
 		ProcessedInventoryItem[] possItems = currStand.getProcessedInventoryItems(this);
 		
 		// Impossible on an actual application instance, but needed in testing
 		if (possItems != null) {
 		
 			for (ProcessedInventoryItem item : possItems) {
 				for (int i = 0; i < intent.getIntExtra(item.item_name, 0); i++) {
 					itemBuffer.add(new FruitTuple(item.item_name, item.price,
 							intent.getIntExtra(item.item_name, 0)));
 					total += item.price;
 				}
 			}
 
 			// Sort list in ascending order by price; comparator enforces this
 			// for custom
 			// FruitTuple type
 			Collections.sort(itemBuffer, new Comparator<FruitTuple>() {
 				public int compare(FruitTuple a, FruitTuple b) {
 					return (a.price > b.price ? 1 : a.price < b.price ? -1 : 0);
 				}
 			});
 
 			purchasedItems = (FruitTuple[]) itemBuffer
 					.toArray(new FruitTuple[itemBuffer.size()]);
 		
 		}
 		
 		setContentView(R.layout.activity_sales_payment);
 		updateViews();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	} 
 	
 	public void onPlusCouponButtonClick(View v) {
 		if (numTradeIns + numCoupons < purchasedItems.length && numCoupons < purchasedItems.length) {
 			total -= purchasedItems[numCoupons + numTradeIns].price;
 			numCoupons++;
 			updateViews();
 		}
 	}
 
 	public void onMinusCouponButtonClick(View v) {
 		if (numCoupons > 0) {
 			numCoupons--;
 			total += purchasedItems[numCoupons + numTradeIns].price;
 			updateViews();
 		}
 	}
 
 	public void onPlusTradeInButtonClick(View v) {
 		if (numTradeIns + numCoupons < purchasedItems.length && numTradeIns < purchasedItems.length) {
 			total -= purchasedItems[numCoupons + numTradeIns].price;
 			numTradeIns++;
 			updateViews();
 		}		
 	}
 	
 	public void onMinusTradeInButtonClick(View v) {
 		if (numTradeIns > 0) { 
 			numTradeIns--;
 			total += purchasedItems[numCoupons + numTradeIns].price;
 			updateViews();
 		}		
 	}
 	
 	private void updateViews() {
 		TextView tradeCounter = (TextView) findViewById(R.id.ASPtradeInCounter);
 		tradeCounter.setText("Trade-Ins: " + numTradeIns);
 		TextView couponCounter = (TextView) findViewById(R.id.ASPcouponCounter);
 		couponCounter.setText("Coupons: " + numCoupons);
 		TextView cashCounter = (TextView) findViewById(R.id.ASPcashCounter);
 		cashCounter.setText("Payment: " + java.text.NumberFormat.getCurrencyInstance().format(total));
 	}
 	
 	private String parseCustomer() {
 		String customer;
 		boolean isMale = intent.getBooleanExtra("isMale", false);
 		int age = intent.getIntExtra("ageCategory", 0);
 		
 		customer = isMale ? "Male " : "Female ";
 		
 		switch (age) {
 		case 1:
 			customer = customer + "K - 5";
 			break;
 		case 2:
 			customer = customer + "6 - 8";
 			break;
 		case 3: 
 			customer = customer + "9 - 12";
 			break;
 		case 4: 
 			customer = customer + "Staff";
 			break;
 		case 5:
 			customer = customer + "Parent";
 			break;
 		case 6:
 			customer = customer + "Other";
 			break;
 		}
 		
 		return customer;
 	}
 	
 	// The *best* way of dividing up the purchase into its various fruit-level components.
 	// Existing numCoupons and numTradeIns used as decrementing counters for total still available
 	// to apply to purchase.
 	public void submit() {
 		if (purchasedItems == null || purchasedItems.length == 0) return;
 		
 		String currItem = purchasedItems[0].name;
 		String customer = parseCustomer();
 		int incrCoupons = 0;
  		int incrTradeIns = 0;
  		double incrCash = 0.0;
  		
 		for (int i = 0; i < purchasedItems.length; i++) {
 			
 			if (numCoupons > 0) {
 				incrCoupons++;
 				numCoupons--;
 			} else if (numTradeIns > 0) {
 				incrTradeIns++;
 				numTradeIns--;
 			} else {
 				incrCash += purchasedItems[i].price;
 			}
 			
 			if (i + 1 == purchasedItems.length || !purchasedItems[i + 1].name.equals(purchasedItems[i].name)) {
 				int num = purchasedItems[i].amount;
 				currStand.addPurchase(this, currItem, num, incrCoupons, incrTradeIns, incrCash, customer);
 				
 				incrCoupons = 0;
 				incrTradeIns = 0;
 				incrCash = 0.0;
 				if (i + 1 != purchasedItems.length) currItem = purchasedItems[i+1].name;
 			}
 		}
 		
 		ParseInputData parser = new ParseInputData();
 		double donationAmount = parser.parseItemPrice((EditText)findViewById(R.id.donationInput));
		
		if (Math.abs(donationAmount) > 0.00001) {
			currStand.addPurchase(this, "donation", 1, 0, 0, donationAmount, customer);
		}
 	}
 
 	public void onFinishTransactionButtonClick(View view){
 		submit();
 		Toast toast = Toast.makeText(getApplicationContext(),
 				"Purchase Successful!", Toast.LENGTH_SHORT);
 		toast.show();
 	
 		Intent i = new Intent(this, SalesSummaryActivity.class);
 		startActivityForResult(i, SalesPaymentActivity_ID);
 		
 	}
 	
 	private class FruitTuple {
 		private String name;
 		private int amount;
 		private double price;
 		
 		
 		public FruitTuple(String f, double p, int amt) {
 			name = f;
 			price = p;
 			amount = amt;
 		}
 	}
 }
