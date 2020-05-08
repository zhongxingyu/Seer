 package com.vivianhhuang.briefly;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 
 import com.vivianhhuang.briefly.R;
 
 public class DeleteGroupActivity extends Activity {
 	
 	public static final String DELETE = "DELETE";
 	public static final String CANCEL = "CANCEL";
 	private static int id;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_delete_group);
 		Intent intent = getIntent();
 		id = intent.getIntExtra(GroupActivity.GROUPID, -1);
		Log.v("PLZ", "" + id);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return true;
 	}
 
 	public void delete(View view) {
 		Intent returnIntent = new Intent(this, GroupActivity.class);
 		returnIntent.putExtra(DELETE, true);
 		returnIntent.putExtra(GroupActivity.GROUPID, id);
 		setResult(RESULT_OK, returnIntent);
 		finish();
 	}
 	
 	public void cancel(View view) {
 		Intent cancelIntent = new Intent();
		cancelIntent.putExtra(CANCEL, false);
 		setResult(RESULT_OK, cancelIntent);
 		finish();
 	}
 }
