 package at.roadrunner.android.activity;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import at.roadrunner.android.model.Delivery;
 import at.roadrunner.android.model.Item;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 import com.markupartist.android.widget.actionbar.R;
 
 public class Items extends ListActivity {
 	private ProgressDialog _progressDialog = null;
 	private Delivery _delivery = null;
 	private ArrayList<Item> _items = null;
 	private ItemAdapter _adapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// get the selected delivery
 		_delivery = (Delivery) getIntent().getSerializableExtra("Delivery");
 		if (_delivery == null) {
 			return;
 		}
 		
 		setLayout();
 		
 		_adapter = new ItemAdapter(this, R.layout.row_item_deliveries);
 		setListAdapter(_adapter);
 		
 		registerForContextMenu(getListView());
 		
 		// Runnable to fill the items in a Thread
 		Runnable showItems = new Runnable() {
 			@Override
 			public void run() {
 				getItems();
 			}
 		};
 
 		// start a new Thread to get the items
 		new Thread(null, showItems, "getItemsOfDelivery").start();
 
 		// show the Progressbar
 		_progressDialog = ProgressDialog.show(this, getString(R.string.app_progress_pleasewait),getString(R.string.app_progress_retdata), true);
 	}
 	
 	private Intent createMapIntent() {
 		Intent mapIntent = new Intent(this, DeliveryMap.class);
 		mapIntent.putExtra("Delivery", _delivery);
 		return mapIntent;
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 	  super.onConfigurationChanged(newConfig);
 	  setLayout();
 	}
 	
 	private void getItems() {
 		_items = _delivery.getItems();
 		runOnUiThread(updateActivity);
 	}
 	
 	private void setLayout() {
 		setContentView(R.layout.activity_items);
 		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.items_title);
 		actionBar.setHomeAction(new IntentAction(this, new Intent(this, Roadrunner.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), R.drawable.ic_title_home_default));
 		actionBar.addAction(new IntentAction(this, createMapIntent(), R.drawable.ic_title_map_default));
 	}
 	
 	/*
 	 * fill the adapter with the items of the list
 	 */
 	private final Runnable updateActivity = new Runnable() {
 
 		@Override
 		public void run() {
 			if (_items != null) {
 				_adapter.notifyDataSetChanged();
 				for (int i = 0; i < _items.size(); i++) {
 					_adapter.add(_items.get(i));
 				}
 			}
 
 			_adapter.notifyDataSetChanged();
 			_progressDialog.dismiss();
 		}
 	};
 	
 	/*
 	 * ItemAdapter
 	 */
 	private class ItemAdapter extends ArrayAdapter<Item> {
 
 		public ItemAdapter(Context context, int textViewResourceId) {
 			super(context, textViewResourceId);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			if (view == null) {
 				LayoutInflater layInf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = layInf.inflate(R.layout.row_item_items, null);
 			}
 
 			Item item = _items.get(position);
 			if (item != null) {
 				// get Views
 				TextView txtName = (TextView) view.findViewById(R.id.items_name);
 				TextView txtTempText = (TextView) view.findViewById(R.id.items_temp_text);
 				TextView txtTemp = (TextView) view.findViewById(R.id.items_temp);
 				ImageView imgStatus = (ImageView) view.findViewById(R.id.items_status);
 				
 				// set values of Views
 				txtName.setTextColor( (item.isLoaded() ? Color.BLACK : Color.GRAY) );
 				txtTempText.setTextColor( (item.isLoaded() ? Color.BLACK : Color.GRAY) );
 				txtTemp.setTextColor( (item.isLoaded() ? Color.BLACK : Color.GRAY) );
 				txtName.setText(item.getName());
 				txtTemp.setText(item.getMinTempString() + " - " + item.getMaxTempString());
 				imgStatus.setImageResource( (item.isLoaded() ? R.drawable.ic_item_truck : R.drawable.ic_item_house) );
 			}
 
 			return view;
 		}
 	}
 }
