 package ca.ryerson.scs.rus.messenger;
 
 import ca.ryerson.scs.rus.MenuActivity;
 import ca.ryerson.scs.rus.R;
 import ca.ryerson.scs.rus.SplashActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class FriendsListActivity extends Activity implements OnClickListener {
 	
 	private Button btnMessages, btnBack;
 	private final String MESSAGES_STRING = "ca.ryerson.scs.rus.messenger.MESSAGES_LIST";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.friends_list);
 		
 	btnMessages = (Button) findViewById(R.id.BtnMessages);
	//btnBack = (Button) findViewById(R.id.BtnBack);
 	
 	btnMessages.setFocusable(true);
 	btnBack.setFocusable(true);
 	
 	btnMessages.setOnClickListener(this);
 	btnBack.setOnClickListener(this);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (v == btnBack) {
 			if (SplashActivity.DEBUG) {
 				Log.i(MenuActivity.TAG, "Back Button");
 			}
 			// TODO: Make it go back to the main page while finishing all other activities
 			
 		} else if (v == btnMessages) {
 			if (SplashActivity.DEBUG) {
 				Log.i(MenuActivity.TAG, "Messages Button");
 			}
 			finish();
 			startActivity(new Intent(MESSAGES_STRING));
 		}
 	}
 
 }
