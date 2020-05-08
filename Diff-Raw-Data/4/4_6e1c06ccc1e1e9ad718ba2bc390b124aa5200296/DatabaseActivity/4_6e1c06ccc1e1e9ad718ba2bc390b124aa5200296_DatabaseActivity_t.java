 package com.github.barcodescanner.database;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import com.github.barcodescanner.R;
 import com.github.barcodescanner.activities.HelpActivity;
 import com.github.barcodescanner.activities.MainActivity;
 import com.github.barcodescanner.product.AddManuallyActivity;
 import com.github.barcodescanner.product.Product;
 import com.github.barcodescanner.product.ProductActivity;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.SparseBooleanArray;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AbsListView.MultiChoiceModeListener;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 @SuppressLint("Recycle")
 public class DatabaseActivity extends ListActivity {
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "DatabaseActivity";
 
 	private DatabaseHelper database;
 	private List<Product> items = new ArrayList<Product>();
 	private ListView list;
 	private EditText searchBar;
 	private String searchQuery;
 
 	private AlertDialog dialog;
 	
 	// These variables are a sort of "hack" to allow a dialog to "return" data.
 	private List<Product> productsToDelete;
 	private Integer productCount = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_database);
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 
 		setupDatabase();
 
 		setupDialogs();
 
 		list = (ListView) findViewById(android.R.id.list);
 
 		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
 		list.setMultiChoiceModeListener(new MultiChoiceModeListener() {
 
 			@Override
 			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
 			}
 
 			@Override
 			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 				switch (item.getItemId()) {
 				case R.id.context_menu_delete:
 					deleteSelectedItems();
 					mode.finish();
 					return true;
 				default:
 					return false;
 				}
 			}
 
 			@Override
 			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 				MenuInflater inflater = mode.getMenuInflater();
 				inflater.inflate(R.menu.menu_context, menu);
 				return true;
 			}
 
 			@Override
 			public void onDestroyActionMode(ActionMode mode) {
 			}
 
 			@Override
 			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 				return false;
 			}
 		});
 	}
 
 	/**
 	 * Gives the class access to the database.
 	 */
 	private void setupDatabase() {
 		DatabaseHelperFactory.init(this);
 		database = DatabaseHelperFactory.getInstance();
 	}
 
 	private void setupDialogs() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setPositiveButton(R.string.context_menu_dialog_ok, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				deleteItems();
 			}
 		});
 		builder.setNegativeButton(R.string.context_menu_dialog_cancel, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
				// We have to remember to clear and reset our "hack" values.
				productsToDelete.clear();
				productCount = 0;
 			}
 		});
 
 		dialog = builder.create();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		searchQuery = "";
 		updateSpecialAdapter();
 	}
 
 	private void deleteSelectedItems() {
 		productsToDelete = new ArrayList<Product>();
 
 		SparseBooleanArray checkedItems = list.getCheckedItemPositions();
 		if (checkedItems != null) {
 			for (int i = 0; i < checkedItems.size(); i++) {
 				if (checkedItems.valueAt(i) == true) {
 					productsToDelete.add((Product) list.getAdapter().getItem(checkedItems.keyAt(i)));
 					productCount++;
 				}
 			}
 			
 			// Display a different message depending on how many items we are deleting.
 			if (productCount > 1) {
 				dialog.setTitle(getString(R.string.context_menu_dialog_title_several, productCount));
 				dialog.setMessage(getString(R.string.context_menu_dialog_message_several));
 			} else {
 				dialog.setTitle(getString(R.string.context_menu_dialog_title_one));
 				dialog.setMessage(getString(R.string.context_menu_dialog_message_one));
 			}
 			
 			dialog.show();
 		}
 	}
 
 	private void deleteItems() {
 		String id = "";
 		String lastProductName = "";
 
 		for (Product product : productsToDelete) {
 			id = product.getBarcode();
 			lastProductName = product.getName();
 			database.removeProduct(id);
 		}
 
 		// Setup and show a toast to confirm for the user that the
 		// action succeeded.
 		Context context = getApplicationContext();
 		CharSequence text;
 		if (productCount > 1) {
 			text = getString(R.string.context_menu_toast_several_deleted, productCount);
 		} else {
 			text = getString(R.string.context_menu_toast_one_deleted, lastProductName);
 		}
 		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
 		toast.show();
 
 		// We have to remember to clear and reset our "hack" values. If only we could pass and return data from dialogs...
 		productsToDelete.clear();
 		productCount = 0;
 		updateSpecialAdapter();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu_database, menu);
 		setupSearch(menu);
 		return true;
 	}
 
 	private void setupSearch(Menu menu) {
 		searchBar = (EditText) menu.findItem(R.id.database_menu_search).getActionView();
 		searchBar.setEms(10);
 		searchBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		searchBar.setHint(R.string.database_search_hint);
 		searchBar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.action_search, 0, 0, 0);
 		searchQuery = "";
 
 		/**
 		 * Enabling Search Filter
 		 * */
 		searchBar.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
 				// When a user has changed the text in the search widget, we
 				// update the search query and the special adapter.
 				searchQuery = cs.toString();
 				updateSpecialAdapter();
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 				// Not used, but must be implemented.
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				// Not used, but must be implemented.
 			}
 		});
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		switch (item.getItemId()) {
 		case R.id.database_menu_search:
 			// This "hack" is to make sure that the keyboard shows up when the
 			// search bar gains focus. Oh, the things we have to do when we roll
 			// with our own widgets!
 			(new Handler()).postDelayed(new Runnable() {
 
 				public void run() {
 					searchBar.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
 							MotionEvent.ACTION_DOWN, 0, 0, 0));
 					searchBar.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
 							MotionEvent.ACTION_UP, 0, 0, 0));
 				}
 			}, 50);
 			return true;
 		case R.id.database_menu_create:
 			intent = new Intent(this, AddManuallyActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.database_menu_help:
 			intent = new Intent(this, HelpActivity.class);
 			startActivity(intent);
 			return true;
 		case android.R.id.home:
 			intent = new Intent(this, MainActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		ViewGroup currentRow = (ViewGroup) getListView().getChildAt(position);
 
 		TextView nameView = (TextView) currentRow.getChildAt(0);
 		TextView priceView = (TextView) currentRow.getChildAt(1);
 		TextView idView = (TextView) currentRow.getChildAt(3);
 
 		String productName = nameView.getText().toString();
 		Integer productPrice = Integer.parseInt(priceView.getText().toString());
 		String productId = idView.getText().toString();
 
 		Bundle productBundle = new Bundle();
 
 		productBundle.putString("productName", productName);
 		productBundle.putInt("productPrice", productPrice);
 		productBundle.putString("productId", productId);
 
 		Intent intent = new Intent(this, ProductActivity.class);
 		intent.putExtras(productBundle);
 		startActivity(intent);
 	}
 
 	/**
 	 * Updates the SpecialAdapter, which in turn updates the view of the list of
 	 * all the items in the database.
 	 */
 	private void updateSpecialAdapter() {
 		TextView emptyView = (TextView) findViewById(android.R.id.empty);
 
 		if (database.getProducts().size() == 0) {
 			emptyView.setText(R.string.database_empty_text);
 		} else {
 			emptyView.setText(R.string.database_search_result_empty);
 		}
 
 		items = filterList(database.getProducts(), searchQuery);
 		SpecialAdapter adapter = new SpecialAdapter(this, items);
 		list.setAdapter(adapter);
 	}
 
 	/**
 	 * A static class that helps the SpecialAdapter generate the database view.
 	 */
 	static class ViewHolder {
 		TextView name, price, id;
 	}
 
 	/**
 	 * A special adapter that generates the view that shows the items in the
 	 * database.
 	 */
 	private class SpecialAdapter extends BaseAdapter {
 		private LayoutInflater mInflater;
 
 		// The variable that will hold our text data to be tied to list.
 		private List<Product> data;
 
 		public SpecialAdapter(Context context, List<Product> items) {
 			mInflater = LayoutInflater.from(context);
 			this.data = items;
 		}
 
 		@Override
 		public int getCount() {
 			return data.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return data.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			// A ViewHolder keeps references to children views to avoid
 			// unnecessary calls to findViewById() on each row.
 			ViewHolder holder;
 
 			if (convertView == null) {
 				convertView = mInflater.inflate(R.layout.row, null);
 
 				holder = new ViewHolder();
 				holder.name = (TextView) convertView.findViewById(R.id.name);
 				holder.price = (TextView) convertView.findViewById(R.id.price);
 				holder.id = (TextView) convertView.findViewById(R.id.id);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			// Bind the data efficiently with the holder.
 			holder.name.setText(data.get(position).getName());
 			holder.price.setText("" + data.get(position).getPrice());
 			holder.id.setText(data.get(position).getBarcode());
 
 			return convertView;
 		}
 	}
 
 	private List<Product> filterList(List<Product> list, String s) {
 		if (s.equals("")) {
 			return list;
 		}
 		List<Product> newList = new ArrayList<Product>();
 		for (Product p : list) {
 			if (p.getName().toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH))) {
 				newList.add(p);
 			}
 		}
 		return newList;
 	}
 }
