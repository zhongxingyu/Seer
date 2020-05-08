 package de.ololololo;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	// Static strings for exchanging data with bundles
 	public static String BUNDLE_TASK_ID = "bundle_task_id";
 	
 	//Data
 	private DataSource mDs;
 	private ArrayList<Task> mTaskList;
 	
 	ArrayAdapter<Task> mAdapter;
 	
 	//UI
 	Button mNewTaskButton;
 	ListView mList;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		mDs = DataSource.getInstance(); //also reads the file
 		
 		mTaskList = mDs.getTasks();
 		if (mTaskList == null){ //mTaskList must not be null for initUI()
 			mTaskList = new ArrayList<Task>();
 		}
 		initUI();
 	}
 	
 	@Override
 	protected void onPause() {
 		mDs.save();
 		super.onPause();
 	}
	
	@Override
	protected void onResume(){
		super.onResume();
		mAdapter.notifyDataSetChanged();
	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.menu_about:
 			final Intent intent = new Intent(this, AboutActivity.class);
 			startActivity(intent);
 			break;
 			
 		default:
 				break;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	private void initUI(){
 		//Button
 		mNewTaskButton = (Button) findViewById(R.id.button_add);
 		mNewTaskButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final Intent intent = new Intent(getApplicationContext(), EditActivity.class);
 				startActivity(intent);
 				
 			}
 		});
 		//List
 		ListView mList = (ListView) findViewById(R.id.list_main);
 		mAdapter = new ArrayAdapter<Task>(this, android.R.layout.simple_list_item_1, mTaskList);
 		mList.setAdapter(mAdapter);
 		mList.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				final Task tmpTask = (Task) parent.getItemAtPosition(position);
 				Builder builder = new Builder(MainActivity.this, AlertDialog.THEME_HOLO_DARK);
 			    builder.setMessage(R.string.dialog_message);
 			    builder.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mDs.removeTask(tmpTask.getId());
 						mAdapter.notifyDataSetChanged();
 						Toast.makeText(getApplicationContext(), tmpTask.getName() + " deleted.", Toast.LENGTH_SHORT).show();
 						
 					}
 				});
 				builder.setNeutralButton(R.string.dialog_neutal, new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						final Intent intent = new Intent(getApplicationContext(), EditActivity.class);
 						intent.putExtra(BUNDLE_TASK_ID, tmpTask.getId());
 						startActivity(intent);
 						mAdapter.notifyDataSetChanged();
 						
 					}
 				});
 			    builder.setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// Do nothing
 						
 					}
 				});
 				builder.show();
 				
 			}
 		});
 	}
 }
