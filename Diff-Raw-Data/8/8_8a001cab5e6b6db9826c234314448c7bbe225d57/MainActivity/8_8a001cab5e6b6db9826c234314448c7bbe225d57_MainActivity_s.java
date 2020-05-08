 package com.arcao.sayitlouder;
 
 import java.util.ArrayList;
 import java.util.List;
 
import com.arcao.sayitloud.R;

 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
 
 	private static final String PREFERENCES_MESSAGE_PREFIX = "message_";
 	private static final String APP_NAME = "SayItLoud";
 
 	private TextView messageText;
 	private ListView list;
 	private Button buttonShow;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		messageText = (TextView) findViewById(R.id.message);
 		list = (ListView) findViewById(R.id.list);
 		buttonShow = (Button) findViewById(R.id.show);
 
 		list.setOnItemClickListener(this);
 		list.setOnItemLongClickListener(this);
 
 		messageText.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				buttonShow.setEnabled(s.length() > 0);
 			}
 		});
 
 		loadMessages();
 	}
 
 	public void buttonShow_onClick(View view) {
 		Intent intent = new Intent(this, DisplayActivity.class);
 		intent.putExtra(DisplayActivity.INTENT_EXTRA_MESSAGE, messageText.getText().toString());
 
 		addMessage(messageText.getText().toString());
 
 		startActivity(intent);
 	}
 
 	private void addMessage(String text) {
 		@SuppressWarnings("unchecked")
 		ArrayAdapter<String> adapter = (ArrayAdapter<String>) list.getAdapter();
 		adapter.remove(text);
 		adapter.insert(text, 0);
 
 		saveMessages();
 	}
 
 	private void loadMessages() {
 		List<String> messages = new ArrayList<String>();
 
 		SharedPreferences settings = getSharedPreferences(APP_NAME, MODE_PRIVATE);
 
 		int i = 0;
 		String message = null;
 		while ((message = settings.getString(PREFERENCES_MESSAGE_PREFIX + i, null)) != null) {
 			messages.add(message);
 			i++;
 		}
 
 		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages));
 	}
 
 	private void saveMessages() {
 		SharedPreferences settings = getSharedPreferences(APP_NAME, MODE_PRIVATE);
 		Editor editor = settings.edit();
 
 		@SuppressWarnings("unchecked")
 		ArrayAdapter<String> adapter = (ArrayAdapter<String>) list.getAdapter();
 
 		for (int i = 0; i < adapter.getCount(); i++) {
 			editor.putString(PREFERENCES_MESSAGE_PREFIX + i, adapter.getItem(i));
 		}
 
 		// remove next if exist
 		editor.remove(PREFERENCES_MESSAGE_PREFIX + adapter.getCount());
 
 		editor.commit();
 
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 		@SuppressWarnings("unchecked")
 		ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
 		String message = adapter.getItem(position);
 		adapter.remove(message);
 
 		Toast.makeText(this, getResources().getString(R.string.item_deleted, message), Toast.LENGTH_LONG).show();
 
 		saveMessages();
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		String message = parent.getItemAtPosition(position).toString();
 
 		messageText.setText(message);
 	}
 }
