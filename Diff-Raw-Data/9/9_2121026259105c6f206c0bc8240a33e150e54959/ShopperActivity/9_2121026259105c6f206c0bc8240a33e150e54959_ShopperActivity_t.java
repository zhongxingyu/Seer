 package com.remifayolle.android.shopper;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class ShopperActivity extends SherlockListActivity {
 
 	private ShopperDbAdapter mDbHelper;
 	private ImageButton mAddButton;
 	private EditText mInput;
 
     private static final int DELETE_ID = Menu.FIRST;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // init data
         mDbHelper = new ShopperDbAdapter(this);
         mDbHelper.open();
         fillData();
         mDbHelper.close();
         
         registerForContextMenu(getListView());
         
         mInput = (EditText) findViewById(R.id.editor);
         mInput.setOnEditorActionListener(new OnEditorActionListener(){
         	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         		if (actionId == EditorInfo.IME_ACTION_DONE) {
         			mAddButton.performClick();
                     return true;
                 }
 				return false;
 			}
 		});
         
         mAddButton = (ImageButton) findViewById(R.id.add_button);
         mAddButton.setOnClickListener(new OnClickListener(){
         	public void onClick(View v) {
         		if(mInput.getText().length()>0)
         		{
         			// add new object to DB
         			mDbHelper.createItem(mInput.getText().toString());
 
         			// clean edit text
         			mInput.setText(null);
         		
         			// update list
         			fillData();
         		}
         	};
         });
         
     }
 
     
 
     @Override
 	protected void onPause() {
 		mDbHelper.close();
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	protected void onResume() {
         mDbHelper.open();
 		super.onResume();
 	}
 
 	@Override
 	protected void onRestart() {
         
 		// init data
         mDbHelper = new ShopperDbAdapter(this);
         mDbHelper.open();
         fillData();
         mDbHelper.close();
         
 		super.onRestart();
 	}
 	
 
 	private void fillData() {
 		// Get all of the rows from the database and create the item list
        Cursor itemCursor = mDbHelper.fetchAllItems();
        //startManagingCursor(itemCursor); //TODO: use CursorLoader
 
         // Create an array to specify the fields we want to display in the list
         String[] from = new String[]{ShopperDbAdapter.KEY_DESC};
         
         // Create an array of the fields we want to bind those fields to (in this case just text1)
         int[] to = new int[]{R.id.item_text};
         
         // Now create a simple cursor adapter and set it to display
         SimpleCursorAdapter items = new SimpleCursorAdapter(this, R.layout.item, itemCursor, from, to, 0);
         setListAdapter(items);
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		
 		return true;
 	}
 
 	
 	/**
 	 * Create contextual menu on list items
 	 */
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.add(0, DELETE_ID, 0, R.string.one_delete);
     }
 
 
     /**
      * Contextual menu on item
      */
     @Override
     public boolean onContextItemSelected(android.view.MenuItem item) {
         switch(item.getItemId()) {
             case DELETE_ID:
                 AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                 mDbHelper.deleteItem(info.id);
                 fillData();
                 return true;
         }
         return super.onContextItemSelected(item);
     }
 
 
     /**
      * Click on item
      */
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// get TextView to update
 		//TextView text = (TextView) v.findViewById(R.id.item_text);
 		
 		// get info on item to update
 		Cursor c = mDbHelper.fetchAllItems();
 		c.moveToPosition(position);
 		
 		mDbHelper.updateItem(c.getInt(c.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ROWID)), c.getString(c.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)), true);
		c.close();
 		
 		super.onListItemClick(l, v, position, id);
 	}
 
     
     
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
             case R.id.menu_delete:
                 clearList();
                 return true;
             case R.id.menu_share:
             	shareList();
             	return true;
         }
 
         return super.onMenuItemSelected(featureId, item);
     }
 
 
 
 	private void clearList() {
 		// Get all of the rows from the database and create the item list
         Cursor itemCursor = mDbHelper.fetchAllItems();
         if (itemCursor.moveToFirst()) {
     		mDbHelper.deleteItem(itemCursor.getLong(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ROWID)));
         	while (itemCursor.moveToNext()) {
         		mDbHelper.deleteItem(itemCursor.getLong(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ROWID)));
         	}
         }
        itemCursor.close();
         
         // Refresh the list
 		fillData();
 	}
 	
 	
 
 	private void shareList() {
 		StringBuilder toShare = new StringBuilder(getString(R.string.share_begin));
         Cursor itemCursor = mDbHelper.fetchAllItems();
         if (itemCursor.moveToFirst()) {
         	int isdone = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ISDONE));
         	if(isdone==0)
     		{
         		toShare.append("\n-");
     			toShare.append(itemCursor.getString(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)));
     		}
         	while (itemCursor.moveToNext()) {
         		isdone = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_ISDONE));
             	if(isdone==0)
         		{
             		toShare.append("\n-");
             		toShare.append(itemCursor.getString(itemCursor.getColumnIndexOrThrow(ShopperDbAdapter.KEY_DESC)));
         		}
         	}
         }
        itemCursor.close();
 		
     	Intent shareIntent = new Intent(Intent.ACTION_SEND);
     	shareIntent.setType("text/plain");
     	shareIntent.putExtra(Intent.EXTRA_TEXT,toShare.toString());
     	startActivity(Intent.createChooser(shareIntent,getString(R.string.share_menu_title)));
 	}
 }
