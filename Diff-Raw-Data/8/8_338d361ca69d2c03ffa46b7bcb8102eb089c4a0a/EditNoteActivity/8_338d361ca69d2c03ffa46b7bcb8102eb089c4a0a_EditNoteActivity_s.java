 package com.wesleyreisz.teaching.android.class4.notetaker;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class EditNoteActivity extends Activity {
 	public static final int RESULT_DELETE = -500;
 	private boolean isInEditMode = true;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_note);
 		
 		final Button btnSave = (Button)findViewById(R.id.btnNote);
 		final Button btnCancel = (Button)findViewById(R.id.btnCancel);
 		final EditText txtTitle = (EditText)findViewById(R.id.txtTitle);
 		final EditText txtNotes = (EditText)findViewById(R.id.txtNotes);
 		final TextView txtDate = (TextView)findViewById(R.id.txtLastUpdated);
 		
 		Serializable extra = getIntent().getSerializableExtra("note");
 		if(extra!=null){
 			Note note = (Note)extra;
 			txtTitle.setText(note.getTitle());
 			txtNotes.setText(note.getNote());
 			
 			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 			txtDate.setText(formatter.format(note.getDate()));
 			
 			isInEditMode=false;
 			txtTitle.setEnabled(false);
 			txtNotes.setEnabled(false);
 			btnSave.setText("Edit");
 			
 		}
 		
 		btnCancel.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent returnIntent = new Intent();
 				setResult(RESULT_CANCELED,returnIntent);
 				finish();
 			}
 		});
 		
 		btnSave.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				if(isInEditMode){
 					isInEditMode=false;
 					Note note = new Note(
 						txtTitle.getText().toString(),
 						txtNotes.getText().toString(),
 						Calendar.getInstance().getTime()
 					);
 					
 					Intent returnIntent = new Intent();
 					returnIntent.putExtra("note", note);
 					setResult(RESULT_OK, returnIntent);
 					finish();
 					
 				}else{
 					isInEditMode=true;
 					
 					txtTitle.setEnabled(true);
 					txtNotes.setEnabled(true);
 					btnSave.setText("Save");
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.are_you_sure_you_want_to_delete_this_item_it_can_t_be_undone_);
 		builder.setTitle("Confirm Delete");
 		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Intent returnIntent = new Intent();
 				
 				setResult(RESULT_DELETE, returnIntent);
 				finish();
 			}
 		});
 		
 		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		builder.create().show();
 		return true;
 	}
 
 
 }
