 package com.cliche818.stockmarketv2;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.R.id;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Main extends ListActivity implements OnClickListener {
 	
 	//debug constants
 	private static final String TAG = "stockmarketv2";
 	
 	// input variables
 	EditText setSymbol;
 	EditText setNoOfStocks;
 
 	// output variables
 	// these symbols allow any methods in this class to use them
 	TextView companyNameOut;
 	TextView symbolOut;
 	TextView priceOut;
 	TextView changePercentageOut;
 	TextView bankAccountOut;
 	TextView assetAccountOut;
 	TextView updateOut;
 	Button getQuote;
 	Button insertSimulation;
 	Button refreshSimulation;
 	Button saveToPortfolio ;
 	
 	//database variable
 	private StockDBAdapter sDbHelper;
 	
 	//declare globalToast
 	private ErrorToast mToast;
 	private YahooCommunicator mYahooCommunicator;
 	
 	//shared preference name
 	private static final String PREFS_NAME = "MyPrefsFile";
 	
 	//menu variables
 	private static final int HELP_ID = Menu.FIRST;
 	private static final int ABOUT_ID = 2;
 	private static final int DELETE_ID = 3;
 	private static final int MOREINFO_ID = 4;
 	
 	//CONSTANTS
 	private static final String NOTVALIDSTOCKPRICE = "0.00";
 	private static final BigDecimal ZERO = new BigDecimal (0);
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mm a 'on' yyyy-MM-dd");
 	
 	// Database
 	SQLiteDatabase db;
 	Cursor dbCursor;
 	
 	// Lists
 	ListView portfolio;
 	ListView gameAsset;
 	ListAdapter adapter;
 	
 	// Stored variables
 	String lastTicker ;
 	
 	//class variables to make my life easy
 	String stockCompanyName = "N/A";
 	String stockSymbol = "N/A";
 	String stockQuote = "N/A";
 	String stockChangePercentage = "N/A";
 	
 	//class variable
 	BigDecimal bankAccountBigDecimal;
 	BigDecimal assetAccountBigDecimal;
 	BigDecimal newAssetTotalBigDecimal;
 	
 	/*
 	 * This method is the startup method, all buttons are created and linked here
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// Intilize database connection variable
 		db = (new SQLiteExpandedHelper(this)).getWritableDatabase();
 		
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
 		//-----------------------------------------Start of Setting up Views for the App-----------------------------------------------------
 		// known as inflate, shows the stuff in our XML file
 		setContentView(R.layout.main);
 		
 		//global toast
 		mToast = new ErrorToast (getApplicationContext());
 		mYahooCommunicator = new YahooCommunicator(this);
 		
 		// connect reference variables with our view objects
 		setSymbol = (EditText) findViewById(R.id.setSymbol);
 		setNoOfStocks = (EditText) findViewById(R.id.setNoOfStocks);
 		setNoOfStocks.setEnabled(false);
 		
 		companyNameOut = (TextView) findViewById(R.id.companyNameOutput);
 		symbolOut = (TextView) findViewById(R.id.stockSymbolOutput);
 		priceOut = (TextView) findViewById(R.id.stockPriceOutput);
 		changePercentageOut = (TextView) findViewById(R.id.stockChangePercentageOutput);
 		
 		bankAccountOut = (TextView) findViewById(R.id.bankAccountOutput);
 		assetAccountOut = (TextView) findViewById(R.id.assetAccountOutput);
 		updateOut = (TextView) findViewById (R.id.updateOutput);
 
 		getQuote = (Button) findViewById(R.id.get_quote_button);
 		saveToPortfolio = (Button) findViewById(R.id.save_to_portfolio_button) ;
 		
 		insertSimulation = (Button) findViewById(R.id.insert_button);
 		insertSimulation.setEnabled(false);
 
 		refreshSimulation = (Button) findViewById(R.id.refreshSimulation);
 		
 		//database setup
 		sDbHelper = new StockDBAdapter(this);
 		sDbHelper.open();
 		fillData();
 		registerForContextMenu(getListView());
 		
 		//creating shared preferences (to save user money/cash account)
 		SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 		
 		String bankAccountString = userAccount.getString("bankAccount", "100000.00");
 		bankAccountBigDecimal = new BigDecimal(bankAccountString);
 		bankAccountOut.setText("Cash: " + currencyFormat(bankAccountBigDecimal));
 		
 		String assetAccountString = userAccount.getString("assetAccount", "0");
 		assetAccountBigDecimal = new BigDecimal(assetAccountString);
 		assetAccountOut.setText("Assets: " + currencyFormat(assetAccountBigDecimal));
 		
 		//Setting up Tabs
 		final TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
 		tabHost.setup();
 		
 		//Setting up Get Quote Module
 		TabHost.TabSpec stockQuoteScreen = tabHost.newTabSpec("StockQuoteTab");
 		stockQuoteScreen.setContent(R.id.stockQuote);
 		stockQuoteScreen.setIndicator("Stock Quote", getResources().getDrawable(android.R.drawable.ic_menu_search));
 		tabHost.addTab(stockQuoteScreen);
 		
 		//Setting up Portfolio
 		TabHost.TabSpec portfolioScreen = tabHost.newTabSpec("Portfolio");
 		portfolioScreen.setContent(R.id.portfolio);
 		portfolioScreen.setIndicator("Portfolio", getResources().getDrawable(R.drawable.ic_action_copy));
 		tabHost.addTab(portfolioScreen);
 		
 		//Setting up Simulation Module
 		TabHost.TabSpec simulationScreen = tabHost.newTabSpec("simulationTab");
 		simulationScreen.setContent(R.id.simulation);
 		simulationScreen.setIndicator("Simulation", getResources().getDrawable(R.drawable.ic_action_paste));
 		tabHost.addTab(simulationScreen);
 		
 		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
 			RelativeLayout rl = (RelativeLayout)tabHost.getTabWidget().getChildAt(i);
 			ImageView iv = (ImageView)rl.getChildAt(0);
 			iv.setPadding(0, 0, 0, 15);
 		 }
 		
 		tabHost.setCurrentTab(0);
 		
 		//check for internet initially
 		if (!isInternet()){
 			mToast.showErrorMessage("Internet is OFF!");
 		}
 		
 		//-----------------------------------------End of Setting up Views for the App-----------------------------------------------------
 
 		getQuote.setOnClickListener(this);
 		
 		insertSimulation.setOnClickListener(this);
 		
 		refreshSimulation.setOnClickListener(this);
 
 		// ------------------------------------------- PORTFOLIO STUFF -----------------------------------------------//
 		saveToPortfolio.setOnClickListener(this) ;
 
 		// Get portfolio variables
 		portfolio = (ListView) findViewById(R.id.pfList);
 		
 		// Prep the portfolio lists
 		dbCursor = db.rawQuery("SELECT * FROM portfolio WHERE 1=1", null);
 		adapter = new SimpleCursorAdapter(this,	R.layout.portfolio_item, dbCursor, new String[] {"Ticker"},	new int[] {R.id.pfliTicker});
 		portfolio.setAdapter(adapter);
 		// ------------------------------------------- END PORTFOLIO STUFF -----------------------------------------------//
 	}
 	
 	/*
 	 * This method is the startup method, all buttons are created and linked here
 	 * @param menu the "menu" where I play my options (sell stock)
 	 * @param v the view that this method was called on 
 	 */
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         if (v.getId()== id.list) {
             AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
             
             //setting the title
             //first select linear layout then the TextView for the company's stock symbol
             LinearLayout selectedLinearLayout = ((LinearLayout) info.targetView);
             TextView selectedStock = (TextView) selectedLinearLayout.findViewById(R.id.stockText);
             menu.setHeaderTitle(selectedStock.getText());
             
             menu.add(0, DELETE_ID, 0, R.string.menu_delete);
             menu.add(0, MOREINFO_ID, 0, R.string.menu_moreinfo);
         }
     }
 
     /*
      * Option to delete/sell ALL stock from simulation mode
      */
     @Override
     public boolean onContextItemSelected(final MenuItem item) {
     	
     	final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
     	final Cursor cur = sDbHelper.fetchStock(info.id);
     	
         switch(item.getItemId()) {
             case DELETE_ID:
             	
             	final Dialog sellDialog = new Dialog(Main.this);
             	sellDialog.setContentView (R.layout.sellpage);
             	sellDialog.setTitle("Selling " + cur.getString(1));
             	sellDialog.setCancelable(true);
             	sellDialog.show();
             	
             	Button sellButton = (Button) sellDialog.findViewById(R.id.sell);
             	Button sellAllButton = (Button) sellDialog.findViewById(R.id.sellAll);
             	final EditText noToSellInput = (EditText) sellDialog.findViewById(R.id.noToSell);
             	
             	//before even giving the ability to sell (the stock MUST be updated)
             	if (!mYahooCommunicator.refreshOne (cur, sellDialog) )
             	{
             		mToast.showErrorMessage("Busy refreshing!");
             		cur.close();
             		sellDialog.dismiss();
             	}	
             	
             	sellAllButton.setOnClickListener (new View.OnClickListener (){
         			@Override
         				public void onClick (View v){
 
 		                    
 		                    //before deleting stock, it must first be "sold"
 		                    //operations to change the user's bankAccount information
 		                    //Cursor cur = sDbHelper.fetchStock(info.id);
		        			BigDecimal stockQuoteBigDecimal = new BigDecimal (cur.getString(2));
 		        			BigDecimal noOfStocksBigDecimal = new BigDecimal (cur.getString(4));
 		        			
 		        			stockQuoteBigDecimal = stockQuoteBigDecimal.multiply(noOfStocksBigDecimal);
 		        			bankAccountBigDecimal = bankAccountBigDecimal.add(stockQuoteBigDecimal);
 		        			assetAccountBigDecimal = assetAccountBigDecimal.subtract(stockQuoteBigDecimal);
 		        			
 		        			//forgot to set our "global/stored" bank account string, bug fix
 		        			SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 		        			SharedPreferences.Editor editor = userAccount.edit();
 		        			
 		        			editor.putString ("bankAccount", bankAccountBigDecimal.toString());
 		        			bankAccountOut.setText("Cash: " + currencyFormat(bankAccountBigDecimal));
 		        			
 		        			editor.putString ("assetAccount", assetAccountBigDecimal.toString());
 		        			assetAccountOut.setText("Assets: " + currencyFormat(assetAccountBigDecimal));
 		        			
 		                    editor.commit();
 		        			
 		                    sDbHelper.deleteStock(info.id);
 		                    fillData();
 		                    cur.close();
 		                    sellDialog.dismiss();
         			}
         			});
             	
             	sellButton.setOnClickListener(new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						
 	                    
 						String noToSellString = noToSellInput.getText().toString();
 						
 						if (noToSellString.length() == 0) {
 							mToast.showErrorMessage("A number is required to continue");
 	        			}	
 						
 						else {
 		                    //before deleting stock, it must first be "sold"
 		                    //operations to change the user's bankAccount information
 							
 		                    
		        			BigDecimal stockQuoteBigDecimal = new BigDecimal (cur.getString(2));
 		        			BigDecimal noOfStocksBigDecimal = new BigDecimal (cur.getString(4));
 		        			
 		        			BigDecimal noToSellBigDecimal = new BigDecimal (noToSellString);
 		        			BigDecimal remainingStocksBigDecimal = noOfStocksBigDecimal.subtract(noToSellBigDecimal);
 		        			
 		        			
 		        			
 		        			//user entered an invalid number (more shares than what he/she has)
 		        			if (remainingStocksBigDecimal.compareTo(ZERO) == -1)
 		        			{
 		        				mToast.showErrorMessage("An invalid number was entered!");
 		        			}
 		        			
 		        			//user wants to sell ALL
 		        			else if (remainingStocksBigDecimal.compareTo(ZERO) == 0)
 		        			{
 		        				stockQuoteBigDecimal = stockQuoteBigDecimal.multiply(noOfStocksBigDecimal);
 			        			bankAccountBigDecimal = bankAccountBigDecimal.add(stockQuoteBigDecimal);
 			        			assetAccountBigDecimal = assetAccountBigDecimal.subtract(stockQuoteBigDecimal);
 			        			
 			        			//forgot to set our "global/stored" bank account string, bug fix
 			        			SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 			        			SharedPreferences.Editor editor = userAccount.edit();
 			        			
 			        			editor.putString ("bankAccount", bankAccountBigDecimal.toString());
 			        			bankAccountOut.setText("Cash: " + currencyFormat(bankAccountBigDecimal));
 			        			
 			        			editor.putString ("assetAccount", assetAccountBigDecimal.toString());
 			        			assetAccountOut.setText("Assets: " + currencyFormat(assetAccountBigDecimal));
 			        			
 			                    editor.commit();
 			        			
 			                    sDbHelper.deleteStock(info.id);
 			                    fillData();
 			                    cur.close();
 			                    sellDialog.dismiss();
 		        			}
 		        			
 		        			//user wants to sell SOME
 		        			else
 		        			{
 		        				stockQuoteBigDecimal = stockQuoteBigDecimal.multiply(noToSellBigDecimal);
 			        			bankAccountBigDecimal = bankAccountBigDecimal.add(stockQuoteBigDecimal);
 			        			assetAccountBigDecimal = assetAccountBigDecimal.subtract(stockQuoteBigDecimal);
 			        			
 			        			//forgot to set our "global/stored" bank account string, bug fix
 			        			SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 			        			SharedPreferences.Editor editor = userAccount.edit();
 			        			
 			        			editor.putString ("bankAccount", bankAccountBigDecimal.toString());
 			        			bankAccountOut.setText("Cash: " + currencyFormat(bankAccountBigDecimal));
 			        			editor.putString ("assetAccount", assetAccountBigDecimal.toString());
 			        			assetAccountOut.setText("Assets: " + currencyFormat(assetAccountBigDecimal));
 			        			
 			                    editor.commit();
 		        				
 		        				
 		        				sDbHelper.updateStock(cur.getInt(0), cur.getString(1), cur.getString(2), cur.getString(3), remainingStocksBigDecimal.toString());
 		        				fillData();
 		        				cur.close();
 		        				sellDialog.dismiss();
 		        			}
 						}
 					}
 				});
             	break;
             	
             case MOREINFO_ID:
             	mYahooCommunicator.moreInfo(cur);
             	break;
 
         }
         return super.onContextItemSelected(item);
     }
 	
     /*
      * Creates the options menu for the app
      * @param menu the "menu" where I placed my options (help and about)
      */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		//menu.add(0, HELP_ID, 0, R.string.menu_insert);
 		menu.add(1, ABOUT_ID, 1, R.string.menu_insert2);
 		return result;
 	}
 
 
 	/*
      * Sets the behavior for what each of my options (help and about does) 
      * @param menu the "menu" where I placed my options (help and about)
      */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()){
 		case HELP_ID:
 			return true;
 		case ABOUT_ID:
 			final Dialog aboutDialog = new Dialog(Main.this);
 			aboutDialog.setContentView (R.layout.about);
 			aboutDialog.setTitle("About");
 			aboutDialog.setCancelable(true);
 			
 			//setup text (about message)
 			TextView aboutText = (TextView) aboutDialog.findViewById(R.id.aboutWindow);
 			aboutText.setText("Written by Fangbo and Jeff.\n\nShoutout to StackOverflow and Frank\n");
 			
 			//do the exit button
 			Button aboutButton = (Button) aboutDialog.findViewById(R.id.aboutButton);
 			aboutButton.setOnClickListener (new View.OnClickListener (){
 			@Override
 				public void onClick (View v){
 					aboutDialog.dismiss();
 			}
 			});
 			aboutDialog.show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 
 
 
 	/*
 	 * The function sees if the app has access to either wifi internet or mobile internet
 	 */
 	protected boolean isInternet(){
 		ConnectivityManager connectmgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		
 		
 		//facts: getNetworkInfo (1) is for WIFI internet
 		//		 getNetworkInfo (0) is for MOBILE internet 
 		
 		if (connectmgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
 				connectmgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED){
 			return true;
 		}
 		else{
 			mToast.showErrorMessage("There is no internet, disengage!");
 			return false;
 		}
 		
 	}
 	
 	/*
 	 * This function gets data by accepting a symbol (ex. amd)
 	 * returns a string that needs to be parsed outside
 	 */
 	protected String getStockInfo( String symbolInput){
 		
 		URL url;
 		String stockTxt = "";
 		try {
 				// getting info from Yahoo Finance API [meat of the program]
 				url = new URL(
 						"http://download.finance.yahoo.com/d/quotes.csv?s="
 								+ symbolInput + ".to" +"&f=sl1p2n");
 				
 				//!!!!!!added .to TSX stocks only!!!!!!!!!!!//
 				
 				/*
 				 * s = stock symbol
 				 * l1 = last trade (price only)
 				 * p2 = change in percent
 				 * n = name of company
 				 */
 
 				InputStream stream = url.openStream();
 				
 				//convert stream to string
 				//reason to use bufferedReader is so there are more functions to use: readLine()
 				BufferedReader r = new BufferedReader(new InputStreamReader(stream));
 				
 				
 				stockTxt = r.readLine();
 
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return stockTxt;
 	}
 	
 	/*
 	 * This method adds the stock to the database
 	 * @param noOfStocksString a number of stocks to "buy" in string form
 	 */
 	private int createStock(String noOfStocksString){
 
 		//operation to change the user's bank account (buying)
 		BigDecimal stockQuoteBigDecimal = new BigDecimal (stockQuote);
 		BigDecimal noOfStocksBigDecimal = new BigDecimal (noOfStocksString);
 		
 		stockQuoteBigDecimal = stockQuoteBigDecimal.multiply(noOfStocksBigDecimal);
 		
 		
 		//check if there the user has enough cash....don't want cash account to go below 0
 		if (stockQuoteBigDecimal.compareTo(bankAccountBigDecimal) == 1){
 			mToast.showErrorMessage("You don't have enough money!");
 			return -1;
 		}
 			
 		else{
 			bankAccountBigDecimal = bankAccountBigDecimal.subtract(stockQuoteBigDecimal);
 			assetAccountBigDecimal = assetAccountBigDecimal.add(stockQuoteBigDecimal);
 			
 			//forgot to set our "global" bank account string, bug fix
 			SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 			SharedPreferences.Editor editor = userAccount.edit();
 			
 			editor.putString ("bankAccount", bankAccountBigDecimal.toString());
 			bankAccountOut.setText("Cash: " + currencyFormat(bankAccountBigDecimal));
 			
 			editor.putString ("assetAccount", assetAccountBigDecimal.toString());
 			assetAccountOut.setText("Assets: " + currencyFormat(assetAccountBigDecimal));
 			
 			editor.commit();
 			
 			// add the stock to database
 			long insertMsg = sDbHelper.createStock(stockSymbol, stockQuote, stockQuote, noOfStocksString);
 			if (insertMsg == -1){
 				mToast.showErrorMessage("You had already added this stock symbol before!");
 
 			}
 			
 			mToast.showErrorMessage("Successfully inserted the stock symbol!");
 
 			
 			fillData();
 			
 			return 0;
 		}
 		
 		
 	}
 	
 	/*
 	 * This method displays everything in the database in the ListView "list"
 	 */
 	private void fillData(){
 		// Get all of the notes from the database and create the item list
         Cursor c = sDbHelper.fetchAllStocks();
         startManagingCursor(c);
                 
         String[] from = new String[] { StockDBAdapter.STOCK_KEY, StockDBAdapter.STOCK_PRICE_NEW, StockDBAdapter.STOCK_PRICE_OLD, StockDBAdapter.STOCK_NUM };
         int[] to = new int[] { R.id.stockText, R.id.stockText2, R.id.stockText3, R.id.stockText4 };
         
         // Now create an array adapter and set it to display using our row
         SimpleCursorAdapter stocks =
             new SimpleCursorAdapter(this, R.layout.stocks_row, c, from, to);
         
         //ListView simulationList = (ListView) findViewById(R.id.simulationListView);
         //simulationList.setAdapter (stocks);
         this.setListAdapter(stocks);
 	}
 	
 	
 	/*
 	 * Changes the format of BigDecimal to a 2 decimal String form (for outputting in text box)
 	 */
 	public String currencyFormat(BigDecimal n) {
 	    return NumberFormat.getCurrencyInstance(Locale.CANADA).format(n);
 	}
 
 	/*
 	 * hides the keyboard (used after entering something in the EditText)
 	 */
 	public void hideKeyboard () {
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(setSymbol.getWindowToken(), 0);
 	}
 	
 	// PORTFOLIO FUNCTIONS
 	// Checks if the given ticker already exists in the database (true for yes false for no)
 	protected boolean existInDB(String ticker){
 		if (ticker != null && ticker != "") {
 			dbCursor = db.rawQuery("SELECT _id FROM portfolio WHERE Ticker = \"" + ticker + "\"", null) ;
 			return (dbCursor.getCount()>0) ;
 		} else {
 			Toast dbError = Toast.makeText(Main.this,
 					"There was no stock ticker to search.",
 					Toast.LENGTH_LONG);
 			dbError.show();
 			return false ;
 		}
 	}
 	
 	// Adds a ticker to the portfolio db (true for okay false for error)
 	protected boolean insertInDB(String ticker) {
 		if (ticker != null && ticker != "") {
 			ContentValues values = new ContentValues();
 	        values.put("Ticker", ticker);
 			if (-1 != db.insert("portfolio", "_id", values)) {
 				// update the portfolio page upon success
 				dbCursor = db.rawQuery("SELECT * FROM portfolio WHERE 1=1", null);
 				adapter = new SimpleCursorAdapter(this,	R.layout.portfolio_item, dbCursor, new String[] {"Ticker"},	new int[] {R.id.pfliTicker});
 				portfolio.setAdapter(adapter);
 				return true ;
 			} else {
 				return false ;
 			}
 		} else {
 			Toast dbError = Toast.makeText(Main.this,
 					"There was no stock ticker to add.",
 					Toast.LENGTH_LONG);
 			dbError.show();
 			return false ;
 		}
 	}
 	
 	// Delete button handler for portfolio view
 	public void portfolioDelete(View v) {
 		// get row layout object
 		LinearLayout row = (LinearLayout)v.getParent();
 		// get label from layout
         TextView ticker = (TextView)row.getChildAt(0);
         // da query
         if (db.delete("portfolio", "Ticker = \"" + ticker.getText().toString() + "\"", null) > 0) {
 			Toast dbInform = Toast.makeText(Main.this,
 					ticker.getText().toString() + " removed.",
 					Toast.LENGTH_LONG);
 			dbInform.show();
         } else {
 			Toast dbError = Toast.makeText(Main.this,
 					"Unable to delete " + ticker.getText().toString() + ".",
 					Toast.LENGTH_LONG);
 			dbError.show();
         }
         // reset layout so it actually shows
 		dbCursor = db.rawQuery("SELECT * FROM portfolio WHERE 1=1", null);
 		adapter = new SimpleCursorAdapter(this,	R.layout.portfolio_item, dbCursor, new String[] {"Ticker"},	new int[] {R.id.pfliTicker});
 		portfolio.setAdapter(adapter);
 	}
 
 	/*
 	 * This method is a switch statement to listen for what button was clicked and call its corresponding method
 	 * @param v the view that the button was pressed in
 	 */
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId())
 		{
 		case R.id.get_quote_button:
 			getQuoteButtonOnClick();
 			break;
 		case R.id.insert_button:
 			insertSimulationOnClick();
 			break;
 		case R.id.refreshSimulation:
 			refreshSimulationOnClick();
 			break;
 		case R.id.save_to_portfolio_button:
 			saveToPortfolioOnClick();
 			break;
 
 		
 		}
 	}
 	
 	/*
 	 * This method is linked to the Get Stock Price button
 	 * Gets the stock information (price and etc)
 	 */
 	public void getQuoteButtonOnClick() {
 		
 		hideKeyboard();
 		
 		//disable the getQuoteButton
 		getQuote.setText("Getting Stock Quote");
 		getQuote.setEnabled(false);
 		
 		//being diligent in checking for Internet every time
 		isInternet();
 		
 		String symbolInput = setSymbol.getText().toString();
 		
 		if (symbolInput.length() == 0) {
 			mToast.showErrorMessage("A stock symbol is required to continue");
 			
 			//re-enable getQuote button
 			getQuote.setText("Get Stock Quote");
 			getQuote.setEnabled(true);
 		}
 
 		else if (!mYahooCommunicator.getStockQuote(symbolInput)){
 			getQuoteButtonAftermath(null);	
 		}
 	}
 	
 	public void getQuoteButtonAftermath (String stockTxt){
 		
 		
 		if (stockTxt == null)
 		{
 			mToast.showErrorMessage("Busy refreshing!");
 			getQuote.setText("Get Stock Quote");
 			getQuote.setEnabled(true);
 			return;
 		}
 		
 		//require a debug message here
 		Log.d(TAG, stockTxt);
 		
 		if (stockTxt.length() == 0)
 		{
 			mToast.showErrorMessage("There is no Internet, can't get data!");			
 			getQuote.setText("Get Stock Quote");
 			getQuote.setEnabled(true);
 			return;
 		}
 		
 		
 		String[] tokens = stockTxt.split(",");
 
 		stockSymbol = tokens[0];
 		stockQuote = tokens[1];
 		stockChangePercentage = tokens[2];
 		stockCompanyName = tokens[3];
 
 		// parse the individual tokens, taking out "" and .to for stock symbol
 		stockSymbol = stockSymbol.substring(1,stockSymbol.length() - 4);
 		stockChangePercentage = stockChangePercentage.substring(1,stockChangePercentage.length() - 3);
 		stockCompanyName = stockCompanyName.substring(1, stockCompanyName.length() - 1);
 		
 		// checking if a correct stock symbol was entering
 		// looking to see if stock price is 0.00, which is not possible
 		if ( stockQuote.compareTo(NOTVALIDSTOCKPRICE) == 0){
 			
 			mToast.showErrorMessage("A invalid stock symbol was entered");
 			
 			stockQuote = "Stock Quote: N/A";
 			stockChangePercentage = "Percent Change: N/A";
 			stockCompanyName = "Company Name: N/A";
 			
 			companyNameOut.setText (stockCompanyName);
 			symbolOut.setText(stockSymbol + " is not a valid stock symbol in the TSX");
 			priceOut.setText(stockQuote);
 			changePercentageOut.setText(stockChangePercentage);
 			
 			//only now is it possible to add stock symbols to database
 			insertSimulation.setEnabled(false);
 			setNoOfStocks.setEnabled(false);
 			
 			
 			
 		}
 		//correct stock quote was entered
 		else {
 			
 			companyNameOut.setText ("Company Name: " + stockCompanyName);
 			symbolOut.setText("Stock Symbol: " + stockSymbol);
 			priceOut.setText("Stock Quote: " + stockQuote);
 			changePercentageOut.setText("Percent Change: " + stockChangePercentage + "%");
 			
 		}
 
 		// Keeping ticker value
 		lastTicker = setSymbol.getText().toString() ;
 		
 		// Show save to portfolio button
 		if (existInDB(lastTicker)) {
 			saveToPortfolio.setText("Already in portfolio") ;
 			saveToPortfolio.setClickable(false) ;
 		} else {
 			saveToPortfolio.setText("Save to Portfolio") ;
 			saveToPortfolio.setClickable(true) ;
 		}
 		saveToPortfolio.setVisibility(View.VISIBLE) ;			
 		
 		//allow the user to get other stocks again
 		getQuote.setText("Get Stock Quote");
 		getQuote.setEnabled(true);
 		
 		//only now is it possible to add stock symbols to database
 		insertSimulation.setEnabled(true);
 		setNoOfStocks.setEnabled(true);
 	
 	}
 	
 	/*
 	 * This method is linked to the Buy button
 	 * Inserts (buys) the stock that is seen in the Stock Quote page
 	 */
 	public void insertSimulationOnClick() {
 		hideKeyboard();
 		
 		String noOfStocksString = setNoOfStocks.getText().toString();
 		
 		//check that a number is indeed entered
 		if (noOfStocksString.length() == 0) {
 			mToast.showErrorMessage("A number is required to continue");
 		}	
 		else{
 			if (createStock(noOfStocksString) == 0)
 			{
 				TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
 				tabHost.setCurrentTab(2);
 			}
 		}
 	}
 	
 	/*
 	 * This method is linked to the Refresh button
 	 * Refreshes the data in the simulation mode by updating each stock's current price
 	 */
 	public void refreshSimulationOnClick() {
 		//refreshStocksAsync refreshTask = new refreshStocksAsync();
 		refreshSimulation.setText("Refreshing");
 		refreshSimulation.setEnabled(false);
 		//refreshTask.execute();
 		Cursor cur = sDbHelper.fetchAllStocks();
 		
 		if (cur.isAfterLast())
 		{
 			refreshSimulation.setText("Refresh");
 			refreshSimulation.setEnabled(true);
 			return;
 		}		
 		else if (cur.moveToFirst())
 		{
 			//always set this global counting variable to 0
 			newAssetTotalBigDecimal = new BigDecimal ("0.0");
 			mYahooCommunicator.refreshAll(cur);
 			return;
 		}
 		
 		
 		
 		
 	}
 	
 	public void refreshALLAftermath (String stockTxt, Cursor cur)
 	{
 		if (stockTxt.length() == 0)
 		{
 			mToast.showErrorMessage("There is no Internet, can't refresh!");
 			refreshSimulation.setText("Refresh");
 			refreshSimulation.setEnabled(true);
 			cur.close();
 			return;
 		}
 		
 		
 		String[] tokens = stockTxt.split(",");
 		
 		//keep a new asset total so it can be update when refresh finishes
 		//operation to change the user's bank account (buying)
 		BigDecimal stockQuoteBigDecimal = new BigDecimal (tokens[1]);
 		BigDecimal noOfStocksBigDecimal = new BigDecimal (cur.getString(4));
 				
 		stockQuoteBigDecimal = stockQuoteBigDecimal.multiply(noOfStocksBigDecimal);
 		newAssetTotalBigDecimal = newAssetTotalBigDecimal.add(stockQuoteBigDecimal);
 		
 		//since I know new stock quote is the 2nd token
 		sDbHelper.updateStock(cur.getInt(0), cur.getString(1), tokens[1], cur.getString(3), cur.getString(4));
 		
 		if (cur.moveToNext())
 		{
 			mYahooCommunicator.refreshAll(cur);
 			return;
 		}
 		
 		fillData();
 		refreshSimulation.setText("Refresh");
 		refreshSimulation.setEnabled(true);
 		cur.close();
 		
 		
 		//actually updating the asset number
 		SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = userAccount.edit();
 		
 		editor.putString ("assetAccount", newAssetTotalBigDecimal.toString());
 		editor.commit();
 		assetAccountOut.setText("Assets: " + currencyFormat(newAssetTotalBigDecimal));
 		
 		//set the time for when it was updated
 		updateOut.setText("Updated at: " + dateFormat.format(new Date()) );
 	}
 	
 	public void refreshOneAftermath (String stockTxt, Cursor cur, Dialog sellDialog){
 		Log.d(TAG, stockTxt);
 		
 		//check if Internet cuts off and getting no data
 		if (stockTxt.length() == 0)
 		{
 			mToast.showErrorMessage("There is no Internet, can't sell!");
 			sellDialog.dismiss();
 			cur.close();
 			return;
 		}
 		
 		String[] tokens = stockTxt.split(",");
 		//since I know stock quote is the 2nd token
 		sDbHelper.updateStock(cur.getInt(0), cur.getString(1), tokens[1], cur.getString(3), cur.getString(4));
 		return;
 	}
 	
 	/*
 	 * Global Toast used to display error messages
 	 */
 	
 	public void saveToPortfolioOnClick() {
 		// Report if no ticker inserted
 		if (lastTicker == null || lastTicker == "") {
 			Toast dbError = Toast.makeText(Main.this,
 					"There was no stock ticker to add.",
 					Toast.LENGTH_LONG);
 			dbError.show();
 			return ;
 		// Report if already exist
 		} else if (existInDB(lastTicker)) {
 			Toast dbError = Toast.makeText(Main.this,
 					"This company is already in your portfolio!",
 					Toast.LENGTH_LONG);
 			dbError.show();
 			return ;
 		} else {
 			// Report if error
 			if (!insertInDB(lastTicker)) {
 				Toast dbError = Toast.makeText(Main.this,
 						"I can't seem to add this to the portfolio, please restart the application and try again.",
 						Toast.LENGTH_LONG);
 				dbError.show();
 				return ;
 			}
 			// User feedback
 			saveToPortfolio.setText("Done") ;
 			saveToPortfolio.setClickable(false) ;
 		}
 	}
 	
 }
