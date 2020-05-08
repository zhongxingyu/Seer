 package edu.upenn.cis.fruity;
 
 import edu.upenn.cis.fruity.database.DatabaseHandler;
 import edu.upenn.cis.fruity.database.FruitStand;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Allows the user to provide input for calculated revenue per item and total revenue
  */
 public class RevenueCalculationsActivity extends Activity {
 	public static final int RevenueCalculationsActivity_ID = 13;
 	
 	//TODO: include other	
 	private int numItems = 9;
 	private int numInputItems = numItems + 1; // 1 more for total revenue
 	
 	// apple = 0, pear = 1, orange = 2, banana = 3, grapes = 4, kiwi = 5, mixedBag = 6, smoothie = 7, granola = 8
 	private double revenueInput[] = new double[numItems];
 	private double expectedRevenue[] = new double[numItems];
 	private boolean[] correct = new boolean[numItems];
 	private double totalExpectedRevenue = 0.0;
 	private double precision = 0.001;
 	
 	private int[] itemIds = {R.id.revenue_apple, R.id.revenue_pear, R.id.revenue_orange,
 			R.id.revenue_banana, R.id.revenue_grapes, R.id.revenue_kiwi, R.id.revenue_mixedBag,
 			R.id.revenue_smoothie, R.id.revenue_granola};
 	private int[] numItemsPurchased = new int[numItems];
 	private double[] itemPrices = new double[numItems];
 	
 	private int correctColor = android.R.attr.editTextBackground;
 	private int incorrectColor = Color.YELLOW;
 	
 	private ParseInputData parser = new ParseInputData();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calculations_revenue);
 		
 		DatabaseHandler dh = DatabaseHandler.getInstance(this);
 		FruitStand currentStand = dh.getCurrentFruitStand();
 		long id = currentStand.id;
 		
 		SQLiteDatabase db = dh.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT item_name, sum(count) FROM Purchase " +
				"WHERE fruit_stand_id ="+id + " AND amount_cash > 0 GROUP BY item_name", null);
 		getNumItemsSold(c);
 		setNumItemsSold();
 
 		Cursor c2 = db.rawQuery("SELECT item_name, price FROM ProcessedInventoryItem " +
 				"WHERE fruit_stand_id ="+id + " GROUP BY item_name", null);
 		getItemPrices(c2);
 		setItemPrices();
 		
 		calculateExpectedRevenue();
 		
 		TextView numCorrectDisplay = (TextView)findViewById(R.id.num_correct_revenue_calculations);
 		numCorrectDisplay.setText("0/"+numInputItems);
 	}
 
 	public void getNumItemsSold(Cursor c){		
 		if(c.moveToFirst()){
 			for (int i = 0; i < c.getCount(); i++) {			  
 				String itemName = c.getString(0);
 	 			int num = Integer.parseInt(c.getString(1));
 
 	 			if(itemName.equals("apple")){
 	 				numItemsPurchased[0] = num;
 	 			}
 	 			else if(itemName.equals("pear")){
 	 				numItemsPurchased[1] = num;
 	 			}
 	 			else if(itemName.equals("orange")){
 	 				numItemsPurchased[2] = num;
 	 			}
 	 			else if(itemName.equals("banana")){
 	 				numItemsPurchased[3] = num;
 	 			}
 	 			else if(itemName.equals("grapes")){
 	 				numItemsPurchased[4] = num;
 	 			}
 	 			else if(itemName.equals("kiwi")){
 	 				numItemsPurchased[5] = num;
 	 			}
 	 			else if(itemName.equals("mixedBag")){
 	 				numItemsPurchased[6] = num;
 	 			}
 	 			else if(itemName.equals("frozenFruitBag")){
 	 				numItemsPurchased[7] = num;
 	 			}
 	 			else if(itemName.equals("granola")){
 	 				numItemsPurchased[8] = num;
 	 			}
 	 			else{ //TODO: other
 	 				
 	 			}
 	 			
 				c.moveToNext();
 			}
 		}
 		c.close();
 	}
 	
 	public void setNumItemsSold(){
 		int[] numItemIds = {R.id.num_bought_apple, R.id.num_bought_pear, R.id.num_bought_orange,
 				R.id.num_bought_banana, R.id.num_bought_grapes, R.id.num_bought_kiwi, R.id.num_bought_mixedBag,
 				R.id.num_bought_smoothie, R.id.num_bought_granola};
 		
 		TextView numItemsText;
 		for(int i = 0; i < numItems; i++){
 			numItemsText = (TextView)findViewById(numItemIds[i]);
 			numItemsText.setText("" + numItemsPurchased[i]);
 		}
 	}
 
 	public void getItemPrices(Cursor c){
 		if(c.moveToFirst()){
 			for (int i = 0; i < c.getCount(); i++) {			  
 				String itemName = c.getString(0);
 	 			double price = Double.parseDouble(c.getString(1));
 
 	 			if(itemName.equals("apple")){
 	 				itemPrices[0] = price;
 	 			}
 	 			else if(itemName.equals("pear")){
 	 				itemPrices[1] = price;
 	 			}
 	 			else if(itemName.equals("orange")){
 	 				itemPrices[2] = price;
 	 			}
 	 			else if(itemName.equals("banana")){
 	 				itemPrices[3] = price;
 	 			}
 	 			else if(itemName.equals("grapes")){
 	 				itemPrices[4] = price;
 	 			}
 	 			else if(itemName.equals("kiwi")){
 	 				itemPrices[5] = price;
 	 			}
 	 			else if(itemName.equals("mixedBag")){
 	 				itemPrices[6] = price;
 	 			}
 	 			else if(itemName.equals("frozenFruitBag")){
 	 				itemPrices[7] = price;
 	 			}
 	 			else if(itemName.equals("granola")){
 	 				itemPrices[8] = price;
 	 			}
 	 			else{ //TODO: other
 	 				
 	 			}
 	 			
 				c.moveToNext();
 			}
 		}
 	}
 	
 	public void setItemPrices(){
 		int[] priceIds = {R.id.price_calc_apple, R.id.price_calc_pear, R.id.price_calc_orange,
 				R.id.price_calc_banana, R.id.price_calc_grapes, R.id.price_calc_kiwi, R.id.price_calc_mixedBag,
 				R.id.price_calc_smoothie, R.id.price_calc_granola};
 		
 		TextView priceText;
 		for(int i = 0; i < numItems; i++){
 			priceText = (TextView)findViewById(priceIds[i]);
 			priceText.setText(parser.convertToCurrency(itemPrices[i]));
 		}
 	}
 	
 	public void calculateExpectedRevenue(){
 		for(int i = 0; i < numItems; i++){
 			expectedRevenue[i] = numItemsPurchased[i] * itemPrices[i];
 			totalExpectedRevenue +=expectedRevenue[i]; 
 		}
 	}
 	
 	public void onCheckRevenueCalculationsButtonClick(View v){
 		int numCorrect = 0;
 		EditText itemRevenueText;
 		// compare actual to expected revenue input
 		for(int i = 0; i < numItems; i++){
 			itemRevenueText = (EditText)findViewById(itemIds[i]);
 			revenueInput[i] = parser.parseItemPrice(itemRevenueText); // actual revenue input
 
 			if(Math.abs(revenueInput[i] - expectedRevenue[i]) < precision){
 				correct[i] = true;
 				numCorrect++;
 				itemRevenueText.setBackgroundColor(correctColor);
 			}
 			else{
 				correct[i] = false;		
 				itemRevenueText.setBackgroundColor(incorrectColor);
 			}
 		}
 		// compare actual to expected total revenue
 		EditText totalRevenueText = (EditText)findViewById(R.id.totalRevenue);
 		double totalRevenueInput = parser.parseItemPrice(totalRevenueText);
 		if(Math.abs(totalRevenueInput - totalExpectedRevenue) < precision){
 			numCorrect++;
 			totalRevenueText.setBackgroundColor(correctColor);
 		}
 		else{
 			totalRevenueText.setBackgroundColor(incorrectColor);
 		}
 		
 		TextView numCorrectDisplay = (TextView)findViewById(R.id.num_correct_revenue_calculations);
 		numCorrectDisplay.setText("" + numCorrect +"/"+numInputItems);
 		
 		if(numCorrect < numInputItems){
 			Toast toast = Toast.makeText(getApplicationContext(),
 					"Incorrect revenue calculations are highlighted in yellow.", Toast.LENGTH_SHORT);
 			toast.show();
 		}
 	}
 
 	public void onGoToProfitCalculationsButtonClick(View v){
 		Intent i = new Intent(this, ProfitCalculationsActivity.class);
 		i.putExtra("totalRevenue", ((Double)totalExpectedRevenue).toString());
 		startActivityForResult(i, RevenueCalculationsActivity_ID);
 	}
 }
