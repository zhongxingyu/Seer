 package com.example.gamecontrollerdatatransfer;
 
 import gc.common_resources.IPAddressFormatValidator;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class WifiConnectActivity extends ListActivity {
 
 	/** TODOS: 
 	 *  Confirmation msg for delete all;
 	 *  Fix scroll bar;
 	 *  Sort list ;
 	 */
 	private String ipAddress;
 	private EditText userInput;
 	private SharedPreferences preferences;
 	private SharedPreferences.Editor editor;
 	private ArrayList<String> listItems = new ArrayList<String>();
 	private ArrayAdapter<String> adapter;
 	private ListView lv = null;
 
 	public String getIpAddress() {
 
 		return ipAddress;
 	}
 
 	public void setIpAddress(String ipAddress) {
 		this.ipAddress = ipAddress;
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_wificonnect);
 
 		// to store previous IPs
 		preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		editor = preferences.edit();
 		userInput = (EditText) findViewById(R.id.user_input);
 
 		// to display as a list
 		adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_multiple_choice, listItems);
 
 		// setListAdapter(adapter);
 
 		lv = (ListView) findViewById(android.R.id.list);
 		lv.setAdapter(adapter);
 		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
 		for (String key : preferences.getAll().keySet()) {
 			listItems.add(key);
 			Log.d("preference key=", key); // only keys with true are shown
 		}
 		adapter.notifyDataSetChanged();
 
 		lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				String item = (String) lv.getItemAtPosition(position);
 				Toast.makeText(getApplicationContext(),
 						"You selected : " + item, Toast.LENGTH_SHORT).show();
 				userInput.setText(item);
 			}
 		});
 
 		Button buttonPlay = (Button) findViewById(R.id.play);
 		buttonPlay.setOnClickListener(buttonSendOnClickListener);
 		Button buttonAddToFav = (Button) findViewById(R.id.addToFavourites);
 		buttonAddToFav.setOnClickListener(buttonAddToFavourites);
 		Button buttonDeleteAll = (Button) findViewById(R.id.clearList);
 		buttonDeleteAll.setOnClickListener(buttonClearList);
 		Button buttonDeleteSelected = (Button) findViewById(R.id.delete);
 		buttonDeleteSelected.setOnClickListener(buttonDelSelected);
 	}
 
 	Button.OnClickListener buttonDelSelected = new Button.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			int len = lv.getCount();
 			SparseBooleanArray checked = lv.getCheckedItemPositions();
 			List<String> temp = new ArrayList<String>();
 			for (int i = 0; i < len; i++)
 				if (checked.get(i)) {
 					String item = (String) lv.getItemAtPosition(i);
 					temp.add(item);
 					Log.d("Delete this one", item);
 				}
 
 			for (String element : temp) {
 				listItems.remove(element);
				editor.remove(element).commit();
 			}
 			adapter.notifyDataSetChanged();
 			setListToDefault();
 		}
 	};
 
 	Button.OnClickListener buttonSendOnClickListener = new Button.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			IPAddressFormatValidator iadfv = new IPAddressFormatValidator();
 			if (iadfv.validate(userInput.getText().toString())) {
 				Intent k = new Intent(WifiConnectActivity.this, PlayActivity.class);
 				k.putExtra("ipAddress", userInput.getText().toString());
 				startActivity(k);
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"Incorrect IP address format", Toast.LENGTH_LONG)
 						.show();
 			}
 		}
 	};
 
 	Button.OnClickListener buttonAddToFavourites = new Button.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			// editor.clear();
 			IPAddressFormatValidator iadfv = new IPAddressFormatValidator();
 			if (iadfv.validate(userInput.getText().toString())) {
 				editor.putString(userInput.getText().toString(), "dummy");
 				editor.commit();
 				addItems(v);
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"Incorrect IP address format", Toast.LENGTH_LONG)
 						.show();
 			}
 		}
 	};
 
 	Button.OnClickListener buttonClearList = new Button.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					getApplicationContext());
 			/*
 			 * builder.setTitle("Are you sure?");
 			 * builder.setMessage(R.string.confirm_clear_list
 			 * ).setPositiveButton(R.string.ok, new
 			 * DialogInterface.OnClickListener() {
 			 * 
 			 * @Override public void onClick(DialogInterface dialog, int id) {
 			 * // User clicked OK, so save the mSelectedItems results somewhere
 			 * // or return them to the component that opened the dialog
 			 * 
 			 * } }).setNegativeButton(R.string.cancel, new
 			 * DialogInterface.OnClickListener() {
 			 * 
 			 * @Override public void onClick(DialogInterface dialog, int id) {
 			 * //NOP } }); builder.show();
 			 */
 			clearlistview();
 		}
 	};
 
 	public void addItems(View v) {
 
 		adapter.clear();
 		Map<String, ?> prefMap = preferences.getAll();
 		for (String key : prefMap.keySet()) {
 			listItems.add(key);
 			Log.d("preference key=", key); // only keys with true are shown
 		}
 		setListToDefault();
 		adapter.notifyDataSetChanged();
 	}
 
 	public void clearlistview() {
 		listItems.clear();
		editor.clear().commit();
 		adapter.notifyDataSetChanged();
 	}
 
 	public void setListToDefault() {
 		for (int i = 0; i < adapter.getCount(); i++) {
 			lv.setItemChecked(i, false);
 		}
 	}
 }
