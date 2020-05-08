 package edu.ucsb.cs.cs185.moneysaver;
 
 import java.util.Comparator;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MoneySaverActivity extends Activity {
 	private static final String TRANS_NAME = "TRANS_NAME";
 	private static final String TRANS_AMOUNT = "TRANS_AMOUNT";
 	private static final String TRANS_DESCRIPTION = "TRANS_DESCRIPTION";
 	private static final String TRANS_DATE = "TRANS_DATE";
 	private static final String TRANS_ID = "TRANS_ID";
 	
 	private static final int TRANSACTION_KEY = 1421; 
 	
 	
 	private ListView lv_transaction_list;
 	
 	private TextView tv_amount;
 	
 	private DataBaseWrapper m_database;
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		Intent i;
     	switch(item.getItemId())
     	{
     		case R.id.home:    			
     			return true;
     		case R.id.tran:
     			i = new Intent(this, Transactions.class);
     			startActivityForResult(i, TRANSACTION_KEY);
     			return true;
     		case R.id.pie:
     			i = new Intent(this, Charts.class);
     			startActivity(i);
     			finish();
     			return true;
     		case R.id.settings:
     			i = new Intent(this, Settings.class);
     			startActivity(i);
     			finish();
     			return true;
     		case R.id.help:
     			i = new Intent(this, Help.class);
     			startActivity(i);
     			finish();
     			return true;    			
     		default:
     			return true;	
     	}
 	}
 	
 	/*
 	 * setting up the context menu for the ListView menus to display
 	 * when they're long clicked
 	 */
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		  super.onCreateContextMenu(menu, v, menuInfo);
 		  MenuInflater inflater = getMenuInflater();
 		  inflater.inflate(R.menu.transaction_menu, menu);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         
         //get list from database
     	List<Transaction> trans_list = m_database.getAllTransactions();
     	ArrayAdapter<Transaction> adapter  = new ArrayAdapter<Transaction>(MoneySaverActivity.this,
     			android.R.layout.simple_list_item_1, trans_list);
     	// sort adapter
     	adapter.sort(new Comparator<Transaction>(){
     		public int compare(Transaction trans1, Transaction trans2){
     			if(trans1.getId() > trans2.getId())
     				return -1;
     			return 1;
     		};    	    		
     	});
 	    
 	    switch (item.getItemId()) {
 	    	case R.id.transaction_info:
 	        	
 	            //Code example from:
 	        	//http://developmentality.wordpress.com/2009/10/31/android-dialog-box-tutorial/
 	          	AlertDialog.Builder builder = new AlertDialog.Builder(MoneySaverActivity.this);
 	        	AlertDialog alert;
 	        	builder.setCancelable(true);
 	        	//builder.setIcon(R.drawable.dialog_question);
 	        	builder.setTitle(adapter.getItem((int)info.id).toStringFull());
 	        	builder.setInverseBackgroundForced(true);
 	        	builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 	        	  @Override
 	        	  public void onClick(DialogInterface dialog, int which) {
 	        	    dialog.dismiss();
 	        	  }
 	        	});
 	        	
 	        	alert = builder.create();
 	        	alert.show();
 	        	
 	    		return true;
 	        case R.id.transaction_edit:
 	        	//calls Transactions.java with fields to pre-populate
 	        	EditTransaction(adapter.getItem((int)info.id));
 	        	
 	        	
 	            return true;
 	        case R.id.transaction_delete:
 	        	
 	        	m_database.deleteTransaction(adapter.getItem((int)info.id));
 	        	updateList(false);
 	        	
 	            return true;
 	        default:
 	            return super.onContextItemSelected(item);
 	    }
 	}
 	
 	/*
 	 * Start activity for result:
 	 * 	send default data to populate options
 	 */
 	public void EditTransaction(Transaction transaction) {
 		Intent intent = new Intent(this, Transactions.class);
 		intent.putExtra(TRANS_NAME, transaction.getName());
     	intent.putExtra(TRANS_AMOUNT, transaction.getValue());
     	intent.putExtra(TRANS_DESCRIPTION, transaction.getCategory());
     	intent.putExtra(TRANS_DATE, transaction.getDate());
     	long id = transaction.getId();
     	intent.putExtra(TRANS_ID, id);
     	
     	startActivityForResult(intent, TRANSACTION_KEY);
 	}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home);
         
         //create database here
         m_database = new DataBaseWrapper(getApplicationContext());
         
         ActionBar actionbar = getActionBar();
         actionbar.setDisplayShowHomeEnabled(false);
         actionbar.setDisplayShowTitleEnabled(false);
         
         tv_amount = (TextView) findViewById(R.id.tv_amount);
 
         
         // Will be used for displaying transactions
         lv_transaction_list = (ListView) findViewById(R.id.lv_transaction_list);
         
         //Setting the listView for a floating Context Menu
         registerForContextMenu(lv_transaction_list);
 
         /*
          * Creates a dialog box when an item in the list view is clicked
          * and shows a full description of the item
          */
         /*
         lv_transaction_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
 
             
             //get list from database
         	List<Transaction> trans_list = m_database.getAllTransactions();
         	ArrayAdapter<Transaction> adapter  = new ArrayAdapter<Transaction>(MoneySaverActivity.this,
         			android.R.layout.simple_list_item_1, trans_list);
         	// sort adapter
         	adapter.sort(new Comparator<Transaction>(){
         		public int compare(Transaction trans1, Transaction trans2){
         			if(trans1.getId() > trans2.getId())
         				return -1;
         			return 1;
         		};    	    		
         	});
         	
             //Code example from:
         	//http://developmentality.wordpress.com/2009/10/31/android-dialog-box-tutorial/
           	AlertDialog.Builder builder = new AlertDialog.Builder(MoneySaverActivity.this);
         	AlertDialog alert;
         	builder.setCancelable(true);
         	//builder.setIcon(R.drawable.dialog_question);
         	builder.setTitle(adapter.getItem(position).toStringFull());
         	builder.setInverseBackgroundForced(true);
         	builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
         	  @Override
         	  public void onClick(DialogInterface dialog, int which) {
         	    dialog.dismiss();
         	  }
         	});
         	
         	alert = builder.create();
         	alert.show();
           }
         });
 		*/
 
         updateList(false);
         displayBalance();
     }
     
     public void displayBalance() {
     	float balance = m_database.getBalance();
     	
     	String balance_str = "$" + String.format("%.3g%n", balance);
     	
     	tv_amount.setText(balance_str);
     }
     /*
      * If show_all = true, will populate ListView with all transactions
      * else shows 5 most recent transactions
      */
     public void updateList(boolean show_all) {
     	List<Transaction> trans_list = m_database.getAllTransactions();
     	ArrayAdapter<Transaction> adapter;
     	
     	if(trans_list.size() > 5 && !show_all){
     		adapter = new ArrayAdapter<Transaction>(this,
         			android.R.layout.simple_list_item_1, trans_list.subList(trans_list.size()-5, trans_list.size()));
         }else{
         	adapter = new ArrayAdapter<Transaction>(this,
         			android.R.layout.simple_list_item_1, trans_list);
         }
     	
     	//Sort the transactions to have the most recent on top
     	//Sorting by id, but might want to sort by date
     	adapter.sort(new Comparator<Transaction>(){
     		public int compare(Transaction trans1, Transaction trans2){
     			if(trans1.getId() > trans2.getId())
     				return -1;
     			return 1;
     		};    	    		
     	});
     	
     	lv_transaction_list.setAdapter(adapter);
     	
     	displayBalance();
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
    	
    	Bundle extras = data.getExtras();
    	
     	if(resultCode == RESULT_OK) {
     		if (requestCode == TRANSACTION_KEY) {
     			String name = extras.getString(TRANS_NAME);
     			float amount = extras.getFloat(TRANS_AMOUNT);
     			String description = extras.getString(TRANS_DESCRIPTION);
     			String date = extras.getString(TRANS_DATE);
     			long id = (long)extras.getLong(TRANS_ID);
     			
     			Transaction returned_transaction = null;
     			
     			returned_transaction = new Transaction(id, name, amount, description, date);
     			/*
     			 * Determine if editting or it exists
     			 */
     			Toast.makeText(MoneySaverActivity.this, "In Home: Added Transaction: " + name + " amount: " + amount + " on: " + date, Toast.LENGTH_SHORT).show();
     			
     			//this will just update transaction if already exists (based on id != -1)
     			m_database.insertTransaction(returned_transaction);
     			
 
     			updateList(false);
     		}
     	}
     }
     
     public void buttonClick(View view)
     {
     	Button b_clicked = (Button) view;
     	int id = b_clicked.getId();
     	
     	switch(id) {
     		case R.id.b_show_all:
     			updateList(true);
     			
     			break;
     		
     		default:
     	}
     }
  
 }
