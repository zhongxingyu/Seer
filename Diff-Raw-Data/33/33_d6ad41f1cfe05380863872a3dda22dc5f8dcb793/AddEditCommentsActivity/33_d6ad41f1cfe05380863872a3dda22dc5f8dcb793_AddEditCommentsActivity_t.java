 package ca.ualberta.team2recipefinder.views;
 
 import ca.ualberta.team2recipefinder.R;
 import ca.ualberta.team2recipefinder.R.layout;
 import ca.ualberta.team2recipefinder.R.menu;
 import ca.ualberta.team2recipefinder.model.Ingredient;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AddEditCommentsActivity extends Activity {
 
	private final int EDIT = 3;
	private final int ADD = 2;
 		
 	int mode = ADD;
 	
 	TextView txtComment;
 	Button btnDelete;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_edit_comments);
 		
 		txtComment = (TextView) findViewById(R.id.txtComment);
 		
 		TextView lblAddComment = (TextView) findViewById(R.id.lblAddComment);
 		btnDelete = (Button) findViewById(R.id.btnDelete);
 		
 		if (savedInstanceState == null) {
 			Bundle extras = getIntent().getExtras();
 			
 			if (extras != null) {
 				String screenMode = extras.getString("mode");				
 								
 				if (screenMode.equals("edit")) {
 					mode = EDIT;
 					
 					txtComment.setText(extras.getString("type"));
 				}
 				else {
 					mode = ADD;
 				}
 			}
 			else {
 				mode = ADD;
 			}
 		}
 		
		if (mode == EDIT) {				
 			lblAddComment.setText(getString(R.string.edit_comment));
 		}
 		else {
 			btnDelete.setVisibility(View.INVISIBLE);
 			
 			lblAddComment.setText(getString(R.string.add_comment));
 		}
 		
 		Button btnOK = (Button) findViewById(R.id.btnOk);
 		btnOK.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// validate fields					
 				String comment = new String(txtComment.getText().toString());
 				
 				if (comment.length() > 0) {				
 					Intent intent = new Intent();
 					intent.putExtra("result", comment);
 					setResult(RESULT_OK, intent);
 					finish();
 				}
 				else {
 					Toast.makeText(AddEditCommentsActivity.this, getString(R.string.missing_fields), 
 							   Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 		
 		btnDelete.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent();
 				intent.putExtra("deleted", "yes");
 				setResult(RESULT_OK, intent);
 				finish();
 			}			
 		});
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.add_edit_comments, menu);
 		return true;
 	}
 
 }
