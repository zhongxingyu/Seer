 package com.github.groupENIGMA.journalEgocentrique;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 
 public class WriteNote extends Activity {
 	
 	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_write_note);
 	}
 	
 	public void sendNote(View view){
 		Intent intent = new Intent(getApplicationContext(), ListActivity.class);
 		EditText text = (EditText) findViewById(R.id.editNote);
 		String message = text.getText().toString();
 		intent.putExtra(EXTRA_MESSAGE, message);
 		startActivity(intent);
 	}
 
 }
