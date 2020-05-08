 package com.balk.bovespatracker;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.balk.bovespatracker.StockData;
 import com.balk.bovespatracker.DBHelper;
 
 public class BovespaTracker extends Activity {
 
 	private static final String TAG = "BovespaTracker";
 	EditText mStockFromDialog;
 	Context mContext;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.i(TAG, "Inicio BovespaTracker");
     	mContext = this;
     	
     	super.onCreate(savedInstanceState);
     	
     	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     	
         setContentView(R.layout.activity_main);
 
         new addStocktoLayoutFromDBTask().execute();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.add_stock:
             Log.i(TAG, "Clicked on menu Add stock");
             getStockNameDialog();
             return true;
         case R.id.refresh:
         	Log.i(TAG, "Clicked on menu Refresh");
         	new refreshStockDataTask().execute();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void getStockNameDialog() {
     	//Mapping UI
         View stockDialog = LayoutInflater.from(this).inflate(R.layout.stock_name_dialog, null);
         mStockFromDialog = (EditText) stockDialog.findViewById(R.id.stockEditText);
 
         // Creating and shows the new channel dialog
         Builder newStockDialog = new AlertDialog.Builder(this);
         newStockDialog.setTitle(getString(R.string.enter_stock_symbol))
             .setView(stockDialog)
             .setPositiveButton(getString(R.string.ok), saveNewStockListener )
             .setNegativeButton(getString(R.string.cancel),cancelNewStockListener )
             .show();
     }
     
     //Called when OK button is clicked on dialog
     DialogInterface.OnClickListener saveNewStockListener = new DialogInterface.OnClickListener() {
     	public void onClick(DialogInterface dialog, int whichButton) {
     		String stockSymbol = mStockFromDialog.getText().toString();
     		
     		// Removing new line that might be entered by user
     		stockSymbol = stockSymbol.replaceAll("\\\\n", ""); 
 
     		// Check if stock symbol seems valid
     		Log.i(TAG, "stockSymbol length = " + stockSymbol.length());
     		if(4 > stockSymbol.length() || stockSymbol.length() > 8) {
 				Toast.makeText(getApplicationContext(),
 	    				"Stock " + stockSymbol + " seems invalid",
 	    				Toast.LENGTH_LONG).show();   			
     			return;
     		}
     		
     		Log.i(TAG, "onClick Save, stockSymbol = " + stockSymbol);
     		
     		try {
     			new addStocktoLayoutTask().execute(stockSymbol);
 
     		} catch (Exception e) {
 				e.printStackTrace();
 			}
 
     	}
     };
 
     //Called when Cancel button is clicked on dialog
     DialogInterface.OnClickListener cancelNewStockListener = new DialogInterface.OnClickListener() {
     	public void onClick(DialogInterface dialog, int whichButton) {
     		Log.i(TAG, "onClick Cancel" );
     	}
     };
 
 
 	
 	public void addStockToLayout(LayoutInflater inflater, StockData stockData) {
 		
 		TableLayout mainTableLayout = (TableLayout) findViewById(R.id.mainTableLayout);
 		View stockRow = inflater.inflate(R.layout.stockrow, null);
 		
 		TextView stockNameView = (TextView) stockRow.findViewById(R.id.stockName);
 		stockNameView.setText(stockData.getStockName());
 		TextView stockSymbolView = (TextView) stockRow.findViewById(R.id.stockSymbol);
 		stockSymbolView.setText(stockData.getStockSymbol());
 		TextView stockPriceView = (TextView) stockRow.findViewById(R.id.stockPrice);
 		stockPriceView.setText(stockData.getStockPrice());
 		TextView stockVariationView = (TextView) stockRow.findViewById(R.id.stockVariation);
 		stockVariationView.setText(stockData.getStockVariation());
 		
 		if(stockData.getStockVariation().startsWith("-")) {
 			stockVariationView.setTextColor(Color.RED);
 		}
 		
 		mainTableLayout.addView(stockRow);
       
 	}
 
 	private class addStocktoLayoutTask extends AsyncTask<String, Integer, StockData> {
 
 		@Override
 		protected StockData doInBackground(String... stockSymbols) {
 			StockData stockData = new StockData();
 			
 	        try {
 	        	stockData = stockData.getStockDataFromSymbol(stockSymbols[0]);
 	        } catch (Exception e) {
 	        	e.printStackTrace();
 	        }
 	        
 			return stockData;
 		}		
 		
 		protected void onPostExecute(StockData stockData) {
 		    DBHelper dbHelper = new DBHelper(mContext);
 	        SQLiteDatabase db = dbHelper.getWritableDatabase();
 	        
 			// DO stuff here ( it's UI thread )
 			if(stockData != null) {
 				//stockData.debugStockDataObj();
 				addStockToLayout(getLayoutInflater(), stockData);
 				dbHelper.addStockToDB(db, stockData);
 
 				Toast.makeText(getApplicationContext(),
 	    				"Added stock " + stockData.getStockSymbol() + " to watch list",
 	    				Toast.LENGTH_LONG).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 	    				"Couldn't add stock to watch list",
 	    				Toast.LENGTH_LONG).show();
 			}
 			
 			db.close();
 		}
 		
 	}
 	
 	private class addStocktoLayoutFromDBTask extends AsyncTask<Void, Void, StockData[]> {
 
 		@Override
 		protected StockData[] doInBackground(Void... params) {
 			StockData[] stockDataArray = null;			
 		    DBHelper dbHelper = new DBHelper(mContext);
 	        SQLiteDatabase db = dbHelper.getWritableDatabase();
 	        
 	        try {
 	        	stockDataArray = dbHelper.getAllStocks(db);
 	        } catch (Exception e) {
 	        	e.printStackTrace();
 	        }
 	        
 	        db.close();
 			return stockDataArray;
 		}
 		
 		protected void onPostExecute(StockData[] stockDataArray) {
 			TableLayout mainTableLayout = (TableLayout) findViewById(R.id.mainTableLayout);
 			mainTableLayout.removeAllViewsInLayout();
 			
 			// DO stuff here ( it's UI thread )
 			for(StockData stockData : stockDataArray) {
 				if(stockData != null) {
 					//stockData.debugStockDataObj();
 					addStockToLayout(getLayoutInflater(), stockData);
 				}
 			}
 		}
 
 	}
 
 	private class refreshStockDataTask extends AsyncTask<Void, Void, Integer> {
 		@Override
 		protected Integer doInBackground(Void... params) {
 		    DBHelper dbHelper = new DBHelper(mContext);
 	        SQLiteDatabase db = dbHelper.getWritableDatabase();
 	    	StockData[] allStocks = dbHelper.getAllStocks(db);
 	    	StockData stockUpdated = new StockData();
	    	
	    	// Check if we have at least one stock on allStocks[]
	    	if(allStocks.length == 0) { 
	    		db.close();
				return -1;
	    	}
	    	
 	    	for(StockData stock : allStocks) {
 	    		try {
 					stockUpdated = stockUpdated.getStockDataFromSymbol(stock.getStockSymbol());
 					// If stock price or variation have changed, update stock data
 					if (!(stock.getStockPrice().equals(stockUpdated.getStockPrice())) || 
 						!(stock.getStockVariation().equals(stockUpdated.getStockVariation()))) {
 						dbHelper.updateStockData(db, stockUpdated);
 						Log.i(TAG, "Updated price data for stock " + stockUpdated.getStockSymbol());
 					} else {
 						Log.i(TAG, "Price data for stock " + stockUpdated.getStockSymbol() + " didn't change");
 					}
 				} catch (Exception e) {
 					Log.e(TAG, "Could not adquire StockData from YQL for stock " + stock.getStockSymbol());
 					e.printStackTrace();
					db.close();
					return -2;					
 				}
 	    	}
	    	
 	    	db.close();
 			return 0;
 		}
 		
 		@Override
 		protected void onPostExecute(Integer param) {
 			// DO stuff here ( it's UI thread )
			
			// Check for errors
			switch(param) {
			// No stock to update
			case -1:
				Toast.makeText(getApplicationContext(), "No stock to update. Please add at least one", Toast.LENGTH_LONG).show();
				return;
			case -2:
				Toast.makeText(getApplicationContext(), "Could not refresh stock data for all stocks. Please try again", Toast.LENGTH_LONG).show();
				return;
			default:
				Toast.makeText(getApplicationContext(), "Stock prices updated", Toast.LENGTH_LONG).show();
			}
			
 			Log.i(TAG, "Calling addStocktoLayoutFromDBTask() from refreshStockDataTask()");
 			new addStocktoLayoutFromDBTask().execute();
 		}
 
 	}
 	
 }
