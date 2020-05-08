 package com.playphone.sdk.example;
 
 import com.playphone.multinet.MNDirect;
 import com.playphone.multinet.MNDirectUIHelper;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class PostScoreActivity extends CustomTitleActivity implements OnClickListener{
 	
 	EditText editInput;
 	TextView txtResult;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.post_score);
         
         // set the breadcrumbs text
      	TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
      	txtBreadCrumbs.setText("Home > Leaderboards > Details > Update");
      		
         
         Button btnUpload = (Button) findViewById(R.id.btnUpload);
         btnUpload.setOnClickListener(this);
         
         editInput = (EditText) findViewById(R.id.editInput);
         txtResult = (TextView) findViewById(R.id.txtResult);
         
         editInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 if (hasFocus) {
                     getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                 }
                 else
                 {
                 	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                 	imm.hideSoftInputFromWindow(editInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                 }
             }
         });
         
         
     }
     
     
     @Override
 	protected void onPause() {
 		super.onPause();
 		MNDirectUIHelper.setHostActivity(null);
 	}
  
 	@Override
 	protected void onResume() {
 		super.onResume();
 		MNDirectUIHelper.setHostActivity(this);
 	}
 
 
 	@Override
 	public void onClick(View arg0) {
 		editInput.clearFocus();
 		MNDirect.postGameScore(Long.valueOf(editInput.getText().toString()));
 		txtResult.setText("Updated your score of " + editInput.getText().toString() + " on the Leaderboards");
		
 	}
 
 }
