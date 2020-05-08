 package com.caa.ui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.caa.bspace.BSpaceUser;
 import com.caa.bspace.R;
 
 public class BSpaceMobileActivity extends Activity implements OnClickListener {
     /** Called when the activity is first created. */
 	static BSpaceUser user;
 	final Handler myHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			Button submit = (Button) findViewById(R.id.loginsubmit);
 			
 			
 			ProgressBar mProgress = (ProgressBar) findViewById(R.id.loginprogress);
 			mProgress.setVisibility(ProgressBar.GONE);
 			if(user.classes.size() == 0)
 			{
 				Toast.makeText(BSpaceMobileActivity.this.getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
			
				submit.setEnabled(true);
				submit.setClickable(true);
				submit.setVisibility(Button.VISIBLE);
 			}
 			else
 			{
 
 				 Intent myIntent = new Intent(BSpaceMobileActivity.this, ClassViewActivity.class);
 			     BSpaceMobileActivity.this.startActivity(myIntent);
			     
			     Message msg2 = revisibleHandler.obtainMessage();
			     revisibleHandler.sendMessageDelayed(msg2, 1000);
 
 			}

		}
	};
	
	final Handler revisibleHandler = new Handler() {
		public void handleMessage(Message msg) {
			Button submit = (Button) findViewById(R.id.loginsubmit);
			
 			submit.setEnabled(true);
 			submit.setClickable(true);
 			submit.setVisibility(Button.VISIBLE);
 		}
 	};
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         ProgressBar mProgress = (ProgressBar) findViewById(R.id.loginprogress);
 		mProgress.setVisibility(ProgressBar.GONE);
         
         Button button = (Button)findViewById(R.id.loginsubmit);
         button.setOnClickListener(this);
     }
 
    public void onResume(Bundle savedInstanceState)
    {
    	Button submit = (Button) findViewById(R.id.loginsubmit);
		submit.setEnabled(true);
		submit.setClickable(true);
		submit.setVisibility(Button.VISIBLE);
    	
    }
    
 	@Override
 	public void onClick(View arg0) {	
 		ProgressBar mProgress = (ProgressBar) findViewById(R.id.loginprogress);
 		
 		Button submit = (Button) findViewById(R.id.loginsubmit);
 		submit.setEnabled(false);
 		submit.setClickable(false);
 		submit.setVisibility(Button.GONE);
 		
 		// DERP this doesn't work
 		mProgress.setVisibility(ProgressBar.VISIBLE);
 		
 		
 		Thread t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				String username = ((TextView)findViewById(R.id.loginentry)).getText().toString();
 				String userpassword = ((TextView)findViewById(R.id.passwordentry)).getText().toString();
 				
 		    	BSpaceMobileActivity.user = new BSpaceUser(username,
 						userpassword);
 		    	Message msg = myHandler.obtainMessage();
 				myHandler.sendMessage(msg);
 			}
 		});
 		
 		t.start();
 
 		
 	}
 }
