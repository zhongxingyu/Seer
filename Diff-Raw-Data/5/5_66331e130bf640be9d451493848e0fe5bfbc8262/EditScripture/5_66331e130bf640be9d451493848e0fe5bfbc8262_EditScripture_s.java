 package team.kyb.database;
 
 
 
 import team.kyb.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class EditScripture extends Activity {
 	
 	private long rowID;
 	private EditText passage;
 	private EditText book;
 	private EditText chapter;
 	private EditText verse;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState); 
 		setContentView(R.layout.scripture_edit);	
 		
 		passage = (EditText) findViewById(R.id.passagetext);
 		book = (EditText) findViewById(R.id.booktext);
 		chapter = (EditText) findViewById(R.id.chaptertext);
 		verse = (EditText) findViewById(R.id.versetext);
 		
 		Bundle extras = getIntent().getExtras();
 		
 		// if there are extras, use them to populate the EditTexts
 		if (extras != null){
 			rowID = extras.getLong("row_id");
 			passage.setText(extras.getString("passage"));
 			book.setText(extras.getString("book"));
 			chapter.setText(extras.getString("chapter"));
 			verse.setText(extras.getString("verse"));
 			
 		} // end if extras
 		
 		// set event listener for the Save Scripture Button
 		Button saveScriptureButton = (Button) findViewById(R.id.saveScriptureButton);
 		saveScriptureButton.setOnClickListener(saveScriptureButtonClicked);
 		
 	}; // end of public void onCreate(Bundle savedInstanceState) {
 		
 	//responds to event generated when user clicks the Save button
 	OnClickListener saveScriptureButtonClicked = new OnClickListener(){
 		
 		@Override
 		public void onClick(View v){
 			if(passage.getText().length() == 0){
 				AlertDialog.Builder vpassage = new AlertDialog.Builder(EditScripture.this);
 				
 				//set dialog title and message, and provide the button to dismiss
 				vpassage.setTitle(R.string.vpassageTitle);
 				vpassage.setMessage(R.string.vpassageMessage);
 				vpassage.setPositiveButton(R.string.vButton, null);
 				vpassage.show();
 			} else if (book.getText().length() == 0){
 				AlertDialog.Builder vbook = new AlertDialog.Builder(EditScripture.this);
 				
 				//set dialog title and message, and provide the button to dismiss
 				vbook.setTitle(R.string.vbookTitle);
 				vbook.setMessage(R.string.vbookMessage);
 				vbook.setPositiveButton(R.string.vButton, null);
 				vbook.show();				
 			} else if (chapter.getText().length() == 0){
 				AlertDialog.Builder vchapter = new AlertDialog.Builder(EditScripture.this);
 				
 				//set dialog title and message, and provide the button to dismiss
 				vchapter.setTitle(R.string.vbookTitle);
				vchapter.setMessage(R.string.vbookMessage);
 				vchapter.setPositiveButton(R.string.vButton, null);
 				vchapter.show();				
 			} else if (verse.getText().length() == 0){
 				AlertDialog.Builder vverse = new AlertDialog.Builder(EditScripture.this);
 				
 				//set dialog title and message, and provide the button to dismiss
 				vverse.setTitle(R.string.vbookTitle);
				vverse.setMessage(R.string.vbookMessage);
 				vverse.setPositiveButton(R.string.vButton, null);
 				vverse.show();				
 			} else {
 				
 				AsyncTask<Object, Object, Object> saveScriptureTask = 
 						new AsyncTask<Object, Object, Object>() {
 
 					@Override
 					protected Object doInBackground(Object... params) { 
 						saveScripture(); 
 						return null;
 					} 
 
 					@Override
 					protected void onPostExecute(Object result) { 
 						finish(); 
 					} 
 				}; 
 
 				// save the scripture to the database using a separate thread
 				saveScriptureTask.execute((Object[]) null); 				
 				
 			}
 			
 		}
 	};  // end of 	OnClickListener saveScriptureButtonClicked = new OnClickListener(){
 	
 	private void saveScripture(){
 		
 		//get DatabaseConnector to interact with the SQLite database
 		DatabaseConnector databaseConnector = new DatabaseConnector(this);
 		
 		if (getIntent().getExtras() == null){
 			//insert the scripture info into the database
 			databaseConnector.insertScripture(
 					passage.getText().toString(), 
 					book.getText().toString(), 
 					Integer.parseInt(chapter.getText().toString()), 
 					Integer.parseInt(verse.getText().toString())
 			);
 		} else {
 			//update the scripture info in the database based on row id
 			databaseConnector.updateScripture(
 					rowID, 
 					passage.getText().toString(), 
 					book.getText().toString(), 
 					Integer.parseInt(chapter.getText().toString()), 
 					Integer.parseInt(verse.getText().toString())
 			);
 			
 		}
 		
 	} // end of private void saveScripture(){
 	
 	
 
 
 	
 } // end of public class AddEditScripture extends Activity {
