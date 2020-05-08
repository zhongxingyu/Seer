 package fake.domain.adamlopresto.goshop;
 
 import android.app.ListActivity;
 import android.app.LoaderManager;
 import android.content.ContentValues;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import fake.domain.adamlopresto.goshop.contentprovider.GoShopContentProvider;
 import fake.domain.adamlopresto.goshop.tables.ItemAisleDetailView;
 import fake.domain.adamlopresto.goshop.tables.ItemAisleTable;
 import fake.domain.adamlopresto.goshop.tables.ItemsTable;
 
 public class ItemDetailActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 	
 	private RadioButton mNeed;
 	private RadioButton mPurchased;
 	private RadioButton mHave;
 	private EditText mName;
 	private EditText mQuantity;
 	private EditText mUnits;
 	private EditText mNotes;
 	private EditText mPrice;
 	private EditText mCategory;
 	private Button addAisle;
 
 	private long listId;
 
 	private Uri uri;
 	
 	private SimpleCursorAdapter adapter;
 	
 	private boolean deleting = false;
 	
 	private static final int DELETE_ID = Menu.FIRST + 1;
 
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.item_edit);
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		View header = View.inflate(this, R.layout.item_edit_header, null);
 		getListView().addHeaderView(header, null, false);
 		
 		View footer = View.inflate(this, R.layout.item_edit_footer, null);
 		getListView().addFooterView(footer, null, false);
 	
 		mNeed      = (RadioButton) header.findViewById(R.id.item_detail_need);
 		mPurchased = (RadioButton) header.findViewById(R.id.item_detail_purchased);
 		mHave      = (RadioButton) header.findViewById(R.id.item_detail_have);
 		mName      = (EditText)    header.findViewById(R.id.item_detail_name);
 		mQuantity  = (EditText)    header.findViewById(R.id.item_detail_quantity);
 		mUnits     = (EditText)    header.findViewById(R.id.item_detail_units);
 		mNotes     = (EditText)    header.findViewById(R.id.item_detail_notes);
 		mPrice     = (EditText)    header.findViewById(R.id.item_detail_price);
 		mCategory  = (EditText)    header.findViewById(R.id.item_detail_category);
 		
 		addAisle   = (Button)      footer.findViewById(R.id.add_aisle);
 		
 		addAisle.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				addAisle();
 			}
 		});
 
 		listId = MainListActivity.list;
 
 		Bundle extras = getIntent().getExtras();
 
 		// Check from the saved Instance
 		uri = (bundle == null) ? null : (Uri) bundle
 				.getParcelable(GoShopContentProvider.CONTENT_ITEM_TYPE);
 		
 		String[] from = new String[] {ItemAisleDetailView.COLUMN_STORE_NAME, ItemAisleDetailView.COLUMN_AISLE_NAME};
 		int[] to = new int[] {android.R.id.text1, android.R.id.text2};
 
 		adapter = new SimpleCursorAdapter(this, R.layout.item_aisle_row, null, from,
 				to, 0);
 
 		setListAdapter(adapter);
 		registerForContextMenu(getListView());
 		
 
 		// Or passed from the other activity
 		if (extras != null) {
 			uri = extras.getParcelable(GoShopContentProvider.CONTENT_ITEM_TYPE);
 
 			if (uri != null) {
 				fillData(uri);
 			} else {
 				mName.setText(extras.getString("name"));
 			}
 		}
 	}
 
 	private void addAisle() {
 		saveState();
 		startActivityForResult(new Intent(this, StoreAisleSelection.class), 1);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (requestCode == 1) {
 			if(resultCode == RESULT_OK){
 				ContentValues values = new ContentValues(2);
 				values.put(ItemAisleTable.COLUMN_ITEM, uri.getLastPathSegment());
 				values.put(ItemAisleTable.COLUMN_AISLE, data.getExtras().getLong("aisle"));
 				getContentResolver().insert(GoShopContentProvider.ITEM_AISLE_URI, values);
 				getLoaderManager().restartLoader(0, null, this);
 			} else if(resultCode == RESULT_CANCELED){
 				Toast.makeText(this, 
 						"Canceled",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(this, 
 						"Unexpected result code: "+resultCode,
 						Toast.LENGTH_LONG).show();
 				
 			}
 		} else {
 				Toast.makeText(this, 
 						"Unexpected request code: "+requestCode,
 						Toast.LENGTH_LONG).show();
 			
 		}
 	}
 
 	private void fillData(Uri uri) {
 		String[] projection = { ItemAisleDetailView.COLUMN_ITEM_NAME,
 				ItemAisleDetailView.COLUMN_QUANTITY, ItemAisleDetailView.COLUMN_UNITS,  ItemAisleDetailView.COLUMN_NOTES, 
 				ItemAisleDetailView.COLUMN_PRICE, ItemAisleDetailView.COLUMN_CATEGORY, ItemAisleDetailView.COLUMN_STATUS,
 				ItemAisleDetailView.COLUMN_LIST
 		};
 		Cursor cursor = getContentResolver().query(uri, projection, null, null,
 				null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 
 			String status = cursor.getString(cursor
 					.getColumnIndexOrThrow(ItemAisleDetailView.COLUMN_STATUS));
 
 			if ("P".equals(status)){
 				mPurchased.setChecked(true);
 			} else if ("H".equals(status)){
 				mHave.setChecked(true);
 			} else {
 				mNeed.setChecked(true);
 			}
 			
 			mName.setText(cursor.getString(cursor
 					.getColumnIndexOrThrow(ItemAisleDetailView.COLUMN_ITEM_NAME)));
 			mQuantity.setText(cursor.getString(cursor
 					.getColumnIndexOrThrow(ItemAisleDetailView.COLUMN_QUANTITY)));
 			mUnits.setText(cursor.getString(cursor
 					.getColumnIndexOrThrow(ItemAisleDetailView.COLUMN_UNITS)));
 			mNotes.setText(cursor.getString(cursor
 					.getColumnIndexOrThrow(ItemAisleDetailView.COLUMN_NOTES)));
 			mPrice.setText(cursor.getString(cursor
 					.getColumnIndexOrThrow(ItemAisleDetailView.COLUMN_PRICE)));
 			mCategory.setText(cursor.getString(cursor
 					.getColumnIndexOrThrow(ItemAisleDetailView.COLUMN_CATEGORY)));
 			listId = cursor.getInt(cursor.getColumnIndexOrThrow(ItemAisleDetailView.COLUMN_LIST));
 
 			// Always close the cursor
 			cursor.close();
 			
 			getLoaderManager().restartLoader(0, null, this);
 			
 		}
 	}
 
	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		//saveState();
 		outState.putParcelable(GoShopContentProvider.CONTENT_ITEM_TYPE, uri);
 	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		uri = state.getParcelable(GoShopContentProvider.CONTENT_ITEM_TYPE);
	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		saveState();
 	}
 
 	private void saveState() {
 		
 		if (deleting){
 			if (uri != null){
 				getContentResolver().delete(uri, null, null);
 			}
 			return;
 		}
 
 		String name = mName.getText().toString();
 
 		// Only save if there's a name
 		if (TextUtils.isEmpty(mName.getText().toString())){
 			return;
 		}
 
 		ContentValues values = new ContentValues();
 		values.put(ItemsTable.COLUMN_NAME, name.trim());
 
 		//values.put(ItemsTable.COLUMN_STATUS, ((String) mStatus.getSelectedItem()).substring(0,1));
 		
 		if (mNeed.isChecked()){
 			values.put(ItemsTable.COLUMN_STATUS, "N");
 		} else if (mPurchased.isChecked()){
 			values.put(ItemsTable.COLUMN_STATUS, "P");
 		} else if (mHave.isChecked()){
 			values.put(ItemsTable.COLUMN_STATUS, "H");
 		}
 
 		try {
 			values.put(ItemsTable.COLUMN_QUANTITY, Double.parseDouble(mQuantity.getText().toString()));
 		} catch (NumberFormatException e) {
 			values.putNull(ItemsTable.COLUMN_QUANTITY);
 		}
 
 		values.put(ItemsTable.COLUMN_UNITS, mUnits.getText().toString().trim());
 		values.put(ItemsTable.COLUMN_NOTES, mNotes.getText().toString().trim());
 
 		try {
 			values.put(ItemsTable.COLUMN_PRICE, Double.parseDouble(mPrice.getText().toString()));
 		} catch (NumberFormatException e){
 			values.putNull(ItemsTable.COLUMN_PRICE);
 		}
 		values.put(ItemsTable.COLUMN_CATEGORY, mCategory.getText().toString().trim());
 
 		if (uri == null) {
 			// New todo
 			values.put(ItemsTable.COLUMN_LIST, listId);
 			uri = getContentResolver().insert(GoShopContentProvider.ITEM_URI, values);
 			//makeToast("Created new item: "+todoUri.toString()+" "+GoShopContentProvider.CONTENT_URI+" "+values);
 
 		} else {
 			// Update todo
 			getContentResolver().update(uri, values, null, null);
 			//makeToast("Updated: "+todoUri.toString());
 		}
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_detail, menu);		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This is called when the Home (Up) button is pressed
 			// in the Action Bar.
 			Intent parentActivityIntent = new Intent(this, MainListActivity.class);
 			parentActivityIntent.addFlags(
 					Intent.FLAG_ACTIVITY_CLEAR_TOP |
 					Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(parentActivityIntent);
 			finish();
 			return true;
 		case R.id.menu_delete:
 			deleting = true;
 			finish();
 			return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 /*
 	private void makeToast(String msg) {
 		Toast.makeText(this, msg,
 				Toast.LENGTH_LONG).show();
 
 	}*/
 	
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		if (uri == null){
 			return null;
 		}
 		String id = uri.getLastPathSegment();
 		String[] projection = new String[] {ItemAisleDetailView.COLUMN_ID, ItemAisleDetailView.COLUMN_STORE_NAME, ItemAisleDetailView.COLUMN_AISLE_NAME};
 		CursorLoader cursorLoader = new CursorLoader(this, GoShopContentProvider.ITEM_AISLE_URI, 
 				projection, ItemAisleDetailView.COLUMN_ITEM+"=?", new String[]{id}, null);
 		return cursorLoader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
 		adapter.swapCursor(c);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		adapter.swapCursor(null);
 		
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		long id = info.id;
 		Uri uri = Uri.parse(GoShopContentProvider.ITEM_AISLE_URI + "/"+ id);
 
 		switch (item.getItemId()) {
 
 		case DELETE_ID:
 			getContentResolver().delete(uri, null, null);
 			//fillData();
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 }
