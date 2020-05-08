 package com.example.todolist;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class MainActivity extends Activity {
 
 	private static final String TAG = "MainActivity";
 	private Context context = null;
 
 	private ArrayList<ToDoRow> todoRows = null;
 	private MyAdapter adapter = null;
 
 	private ListView myListView = null;
 	private EditText myEditText = null;
 	private final String FILENAME = "list.json";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		context = getApplicationContext();
 		setContentView(R.layout.activity_main);
 
 		// Get references to UI widgets
 		myListView = (ListView) findViewById(R.id.myListView);
 		myEditText = (EditText) findViewById(R.id.myEditText);
 
 		// Create the array list of to do items
 		todoRows = new ArrayList<ToDoRow>();
 		
 		// Create mock tasks
 //		todoRows.add(new ToDoRow("Feed the dog", true));
 //		todoRows.add(new ToDoRow("Go to walk", false));
 //		todoRows.add(new ToDoRow("Learn Android development", true));
 
 		// Create the adapter
 		adapter = new MyAdapter(this, todoRows);
 
 		// Bind the array adapter to the ListView
 		myListView.setAdapter(adapter);
 
 		// Restore data from internal storage
 		try {
 			restoreDataFromJSON(readJSON());
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}		
 		adapter.notifyDataSetChanged();
 		
 		// Bind the event handler to the button
 		Button b = (Button) findViewById(R.id.myButton);
 		b.setOnClickListener(buttonListener);
 	}
 
 	private OnClickListener buttonListener = new OnClickListener() {
 
 		public void onClick(View v) {
 
 			// do something when the button is clicked
 			Log.v(TAG, "Button clicked!");
 
 			// add a new task
 			String task = myEditText.getText().toString();
 
 			if (Util.isNullOrEmpty(task)) {
 
 				// task is empty; show toast
 				Util.showToast(context, "You cannot leave it blank.");
 			} else {
 				int index = 0;
 				todoRows.add(index, new ToDoRow(task));
 				adapter.notifyDataSetChanged();
 				myEditText.setText("");
 			}
 		}
 	};
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		Log.v(TAG, "onDestroy called!");
 		saveData();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.v(TAG, "onPause called!");
 		saveData();		
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.v(TAG, "onResume called!");
 		saveData();		
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Log.v(TAG, "onStop called!");
 		saveData();
 	}
 	
 	public String getJSON() throws IOException {
 		
 	  JSONArray json = new JSONArray();
 	  
 	  try {
 		  
 		for (ToDoRow row : todoRows) {
 	
 			JSONObject o = new JSONObject();
 			o.put("task", row.getTask());
 			o.put("checked", row.isChecked());
 			json.put(o);
 		}
 		
 		Log.v(TAG + " writeJSON()", json.toString());
 		
 	  } catch (JSONException e) {
 		  e.printStackTrace();
 	  }
 	  return json.toString();
 	}
 	
 	public void writeJSON() throws IOException {
 		
 		try {
 			
 			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);					
 			fos.write( getJSON().getBytes() );
 			fos.close();
 			
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
 	}
 	
 	public String readJSON() throws FileNotFoundException, IOException {
 		
 		// Constructs a new StringBuffer containing an empty String		
 		StringBuffer fileContent = new StringBuffer("");
 		
 		try {
 			
 			FileInputStream fis = openFileInput(FILENAME);
 
 			byte[] buffer = new byte[1024];
 			/* Reads a single byte from this stream and returns it as an integer 
 			* in the range from 0 to 255. Returns -1 if the end of the stream
 			* has been reached. Blocks until one byte has been read, the end of 
 			* the source stream is detected or an exception is thrown.
             */
			while ( fis.read(buffer) != -1 ) {
 			    fileContent.append(new String(buffer));
			}
			fis.close();			
 			
 		} catch (IOException e) {
 			// could be IOException or FileNotFoundException
 			e.printStackTrace();
 		}
 		return fileContent.toString();
 	}
 	
 	public void restoreDataFromJSON(String json) {
 		
 		todoRows.clear();
 		
 		try {
 			
 		  JSONArray data = new JSONArray(json);
 		
 	      for (int i = 0; i < data.length(); i++) {
 		 	  
 		        JSONObject o = data.getJSONObject(i);
 		        
 		        String task = o.getString("task");
 		        boolean checked = o.getBoolean("checked");
 		        
 		        ToDoRow row = new ToDoRow(task, checked);
 		        todoRows.add(row);
 		  }
 			
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 		}
 	}
 	
 	public void saveData() {
 
 		try {
 			writeJSON();
 		} catch (IOException e) {
 			
 			Log.e(TAG, "JSON writing failed.");
 			e.printStackTrace();
 		}
 	}
 }
