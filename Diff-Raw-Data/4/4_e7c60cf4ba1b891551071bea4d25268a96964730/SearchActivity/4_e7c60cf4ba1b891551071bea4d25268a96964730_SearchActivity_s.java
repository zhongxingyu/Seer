 package com.gui.taptobuy.activity;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.gui.taptobuy.Entities.Category;
 import com.gui.taptobuy.Entities.Product;
 import com.gui.taptobuy.Entities.ProductForAuction;
 import com.gui.taptobuy.Entities.ProductForSale;
 import com.gui.taptobuy.customadapter.CategoriesCustomListAdapter;
 import com.gui.taptobuy.customadapter.ItemCustomListAdapter;
 import com.gui.taptobuy.datatask.Main;
 import com.gui.taptobuy.datatask.ImageDownload;
 import com.gui.taptobuy.phase1.R;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchActivity extends Activity implements OnClickListener   {
 
 	private String[] sortingOptions;
 	private Button categories;
 	private Button cart;
 	private Button search;
 	private Button signIn;
 	private Button signOut;
 	private Button myTap;	
 	private Spinner sorter;
 	private EditText searchET;
 	//private boolean searchDone;
 	public static ArrayList<Product> searchResultItems;
 	private ListView itemsList;
 	private LayoutInflater layoutInflator;
 	/////////////////////////////////////////////
 
 	//private Item item1, item2, item3,item4,item5,item6,item7,item8;
 	private Product item;
 	private ImageView pic;
 	/////////////////////////////////////////////////
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);	
 		setContentView(R.layout.search);
 		//////////////////////////////////////////////////
 		new searchProductsTask().execute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");//searchET.getText().toString());
 
 		categories = (Button)findViewById(R.id.bCategories);
 		cart = (Button)findViewById(R.id.bCart);
 		search = (Button)findViewById(R.id.bSearch);
 		signOut = (Button)findViewById(R.id.bSignOut);
 		myTap = (Button)findViewById(R.id.bMyTap);			
 		signIn = (Button)findViewById(R.id.bSignIn);
 		searchET= (EditText) findViewById(R.id.searchText);
 		itemsList = (ListView)findViewById(R.id.itemsListView);
 		this.layoutInflator = LayoutInflater.from(this);
 
 		signIn.setOnClickListener(this);
 		categories.setOnClickListener(this);		
 		cart.setOnClickListener(this);
 		search.setOnClickListener(this);
 		signOut.setOnClickListener(this);
 		myTap.setOnClickListener(this);	
 
 		if(Main.signed){
 			signIn.setVisibility(View.GONE);		
 		}
 		else{
 			signOut.setVisibility(View.GONE);
 			myTap.setVisibility(View.GONE);
 		}
 
 		//setting the spinner
 		sortingOptions = getResources().getStringArray(R.array.sortBy);   
 		sorter = (Spinner) findViewById (R.id.SortSpinner);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice, sortingOptions);
 		sorter.setAdapter(adapter);
 
 
 
 		// setting action for when an sorting instance is selected
 		sorter.setOnItemSelectedListener(new OnItemSelectedListener(){	
 			@Override
 			public void onItemSelected(AdapterView<?> arg0,View arg1,int arg2, long arg3) 
 			{
 				int index = arg0.getSelectedItemPosition();
 				switch (index)
 				{						
 				case 0: // by name
 					//supongo que decirle al db y obtenerlo sorteado luego settiar el view de nuevo con los
 					// items ya sorted...
 					break;
 				case 1: // by price
 					break;
 				case 2: // by brand
 				}						
 			}
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {}	        	
 		});
 
 		/////////////////////// check above define products
 		// setItems(itemsOnSale);
 	}//end of on create method
 
 	@Override
 	public void onClick(View v) { 
 
 		final Dialog dialog = new Dialog(SearchActivity.this);
 
 		dialog.setContentView(R.layout.login_dialog);
 		dialog.setTitle("Sign in");
 
 		final EditText usernameET = (EditText) dialog.findViewById(R.id.etNameToLogin);
 		final EditText passwordET = (EditText) dialog.findViewById(R.id.etPasswordToLogin);        
 		Button btnSignIn = (Button) dialog.findViewById(R.id.bSignIn);
 
 		switch (v.getId())
 		{		
 		case R.id.bCart:			
 			if(!Main.signed){
 
 				btnSignIn.setOnClickListener(new View.OnClickListener() {
 
 					public void onClick(View v) 
 					{			                
 						String username = usernameET.getText().toString();
 						String password = passwordET.getText().toString();
 						if(username.equals("") || password.equals("")){
 							Toast.makeText(SearchActivity.this, "Error, you must provide userID & password", Toast.LENGTH_SHORT).show();			
 						}	
 						//new SignInTaskFromCartBtn().execute(username,password);   
 					}
 				});    
 				dialog.show();				
 			}
 			else{
 				startActivity(new Intent(this, CartActivity.class));
 			}
 			break;
 
 
 
 		case R.id.bCategories:
 
 			startActivity(new Intent(this, CategoryActivity.class));   		
 			break;
 
 		case R.id.bMyTap:
 			startActivity(new Intent(this, MyTapActivity.class));  
 			break;
 
 		case R.id.bSearch:
 			//itemsList.setAdapter(new ItemCustomListAdapter(this,this.pic,this.layoutInflator, this.itemsOnSale));
 			new searchProductsTask().execute("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");//searchET.getText().toString());
 			break;
 
 		case R.id.bSignIn:		
 			btnSignIn.setOnClickListener(new View.OnClickListener() {
 
 				public void onClick(View v) 
 				{	          
 					String username = usernameET.getText().toString();
 					String password = passwordET.getText().toString();
 					if(username.equals("") || password.equals("")){
 						Toast.makeText(SearchActivity.this, "Error, you must provide userID & password", Toast.LENGTH_SHORT).show();			
 					}	
 					//new SignInTaskFromSignInBtn().execute(username,password);
 				}
 			});    
 			dialog.show();				
 			break;
 
 		case R.id.bSignOut:
 			Main.signed = false;
 			Intent home = new Intent(this, SignInActivity.class);
 			home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(home);			
 
 			break;
 		}
 	}
 
 	private void signInDisabler()
 	{
 		Main.signed = true;
 		signIn.setVisibility(View.GONE);
 		signOut.setVisibility(View.VISIBLE);
 		myTap.setVisibility(View.VISIBLE);
 	}
 
 	public static class MyViewItem {
 		public TextView productName, sellerUserName, priceAndShiping,bidsAmount,timeRemaining;
 		public RatingBar sellerRating;
 		public TextView buyItNow;
 		public ImageView itemPic;
 		public Product item;
 		public CheckBox check;
 		public Button cartBuy;
 		public Button cartRemove;
 	}	
 	private ArrayList<Product> getSearchItems(String searchString){
 		HttpClient httpClient = new DefaultHttpClient();
 		String searchDir = Main.hostName +"/search/" + "aaaaaaaaaaaaaaaaaaaaaaaaa";
 		HttpGet get = new HttpGet(searchDir);
 		get.setHeader("content-type", "application/json");
 		try
 		{
 			HttpResponse resp = httpClient.execute(get);
 			if(resp.getStatusLine().getStatusCode() == 200){
 				String jsonString = EntityUtils.toString(resp.getEntity());
 				JSONArray searchResultArray = (new JSONObject(jsonString)).getJSONArray("results");
 				searchResultItems = new ArrayList<Product>();
 
 				JSONObject searchElement = null;
 				JSONObject jsonItem = null;
 				Product anItem = null;
 
 				for(int i=0; i<searchResultArray.length();i++){
 					searchElement = searchResultArray.getJSONObject(i);
 					jsonItem = searchElement.getJSONObject("item");
 					if(searchElement.getBoolean("forBid")){
 						anItem = new ProductForAuction(jsonItem.getInt("id"), jsonItem.getString("title"), jsonItem.getString("timeRemaining"), 
 								jsonItem.getDouble("shippingPrice"), jsonItem.getString("imgLink"),  jsonItem.getString("sellerUsername"), 
 								jsonItem.getDouble("sellerRate"),  jsonItem.getDouble("startinBidPrice"),  jsonItem.getDouble("currentBidPrice"),  jsonItem.getInt("totalBids"));
 					}
 					else{
 						anItem = new ProductForSale(jsonItem.getInt("id"), jsonItem.getString("title"), jsonItem.getString("timeRemaining"), 
 								jsonItem.getDouble("shippingPrice"), jsonItem.getString("imgLink"),  jsonItem.getString("sellerUsername"), 
 								jsonItem.getDouble("sellerRate"), jsonItem.getInt("remainingQuantity"), jsonItem.getDouble("instantPrice"));
 					}
 					searchResultItems.add(anItem);
 				}
 
 			}
 			else{
 				Log.e("JSON","search json could not be downloaded.");
 			}
 		}
 		catch(Exception ex)
 		{
 			Log.e("Search","Error!", ex);
 		}
 		return searchResultItems;
 	}
 
 	private class searchProductsTask extends AsyncTask<String,Void,ArrayList<Product>> {
 		public  int downloadadImagesIndex = 0;
 		protected ArrayList<Product> doInBackground(String... params) {
 			return getSearchItems(params[0]);//get search result
 		}
 		protected void onPostExecute(ArrayList<Product> searchResultItems ) {
 			//download images
 			for(Product itm: searchResultItems){
 				new DownloadImageTask().execute(itm.getImgLink());
 			}
 			itemsList.setAdapter(new ItemCustomListAdapter(SearchActivity.this,SearchActivity.this.pic,SearchActivity.this.layoutInflator, searchResultItems));
 		}			
 		private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 
 			protected Bitmap doInBackground(String... urls) {
 				return ImageDownload.downloadImage(urls[0]);
 			}
 			protected void onPostExecute(Bitmap result) {
<<<<<<< HEAD
 				itemsList.invalidateViews();
=======
>>>>>>> refs/remotes/origin/master
 				searchResultItems.get(downloadadImagesIndex++).setImg(result);
 			}
 		}
 	}
 
 }
