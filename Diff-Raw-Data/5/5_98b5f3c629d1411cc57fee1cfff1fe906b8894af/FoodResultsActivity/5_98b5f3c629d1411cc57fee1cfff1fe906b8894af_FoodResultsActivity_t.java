 package com.ISU.shoppingsidekick;
 
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.Database.API.Account;
 import com.Database.API.DatabaseAPI;
 import com.Database.API.Expiration;
 import com.Database.API.Food;
 import com.Database.API.Price;
 import com.Database.API.Review;
 
 public class FoodResultsActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_food_results);
 		final Account a = (Account) getIntent().getExtras().get("account");
 		
 		Bundle scanVal = null;
 		
 		Intent scannerValue = getIntent();
 		scanVal = scannerValue.getExtras();
 		TextView productName = (TextView) findViewById(R.id.productName);
 		TextView productBrand = (TextView) findViewById(R.id.productBrand);
 		TextView expInformation = (TextView) findViewById(R.id.expInformation);
 		TextView priceInformation = (TextView) findViewById(R.id.priceInformation);
 		TextView reviewInformation = (TextView) findViewById(R.id.reviewInformation);
 		String name = "";
 		String brand= "";
 		String id ="";
 		Food scannedFood = new Food();
 		if(scanVal != null){		
 			final String scanValue = scanVal.getString("scanID");
 			ExecutorService pool = Executors.newFixedThreadPool(3);
 			Callable task = new Callable(){
 				@Override
 				public Object call() throws Exception{
 					DatabaseAPI database = new DatabaseAPI();
 					
 					Food food = database.getFoodItemByID(scanValue);
 					if(food.getName() != null){
 						return food;
 					}
 					else{
 						return null;
 					}
 				}
 			};
 			Future<Food> future = pool.submit(task);
 			try {
 				scannedFood = future.get();
 				
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			if(scannedFood.getID() != null){
 				Expiration expirationInfo = scannedFood.getExpirationInformation();
 				Price priceInfo = scannedFood.getPriceInformation();
 				List<Review> reviewInfo = scannedFood.getReviewInformation();
 				
 				name = scannedFood.getName();
 				brand = scannedFood.getBrand();
 				id = scannedFood.getID();
 				productName.setText("Product: " + name);
 				
 				productBrand.setText("Product brand: " + brand);
 				
 				expInformation.setText("Average Expiration: " + expirationInfo.getAvgHours() + " Hours");
 				
 				priceInformation.setText("Average Price: " + "$" + priceInfo.getAvgPrice());
 				
				String str = "Reviews:\n\n";
 				for(int i = 0; i < 3 && i < reviewInfo.size(); i++)
 				{
 					Review itemToAdd = reviewInfo.get(i);
					str += itemToAdd != null ? itemToAdd.getReview() + "\n\n" : "";
 				}
 				reviewInformation.setText(str);
 				
 			}
 			else{
 				productName.setText("Item not found");
 				productBrand.setVisibility(View.INVISIBLE);
 				expInformation.setVisibility(View.INVISIBLE);
 				priceInformation.setVisibility(View.INVISIBLE);
 				reviewInformation.setVisibility(View.INVISIBLE);
 			}
 		}
 		
 		else{
 			productName.setText("Item not found");
 			productBrand.setVisibility(View.INVISIBLE);
 			expInformation.setVisibility(View.INVISIBLE);
 			priceInformation.setVisibility(View.INVISIBLE);
 			reviewInformation.setVisibility(View.INVISIBLE);
 		}	
         
         //home button
         Button goToFoodFinderBtn = (Button) findViewById(R.id.addItem);
         goToFoodFinderBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Thread thread = new Thread(){
 					@Override
 					public void run()
 					{
 						Account account = (Account) getIntent().getExtras().get("account");
 						final String scanValue = getIntent().getExtras().getString("scanID");
 						DatabaseAPI database = new DatabaseAPI();
 						Food food = database.getFoodItemByID(scanValue);
 						database.addFoodItemToUserTable(account.getUserID(), food);
 						Intent i = new Intent(FoodResultsActivity.this, HomeActivity.class);
 						i.putExtra("account", account);
 						startActivity(i);
 					}
 				};
 				thread.start();
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.food_results, menu);
 		return true;
 	}
 
 }
