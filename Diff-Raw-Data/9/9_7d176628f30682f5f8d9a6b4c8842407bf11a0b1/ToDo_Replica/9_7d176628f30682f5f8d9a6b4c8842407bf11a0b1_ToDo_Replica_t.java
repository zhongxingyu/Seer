 package eecs.berkeley.edu.cs294;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ToDo_Replica extends Activity {
 	static public DatabaseHelper dh;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		final boolean customTitle = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 
 		dh = new DatabaseHelper(this); 
 
 		setContentView(R.layout.main);
 		if (customTitle)
 			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
 		
 		if(dh.select_user().size() == 0) {
 			Intent intent = new Intent(ToDo_Replica.this, StartScreen.class);
 			startActivity(intent);
 		}
 		
 		/* Timer code */
 		/********************************************************************/
 		Timer serverTimer = new Timer("serverTimer", true);
 		TimerTask serverTimerTask = new TimerTask() {
 			public void run() {
 				ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 				NetworkInfo netInfo = connManager.getActiveNetworkInfo();
 
 				if(netInfo == null) {
 					Log.d("DEBUG", "---------- No internet connection ----------");
 					// TODO: Warn the user
 				}
 
 				else if (netInfo.isConnected()) {
 					Log.d("DEBUG", "---------- Connected to internet ----------");
 					
 					while(dh.select_user().size() == 0);
 				}
 			}
 		};
 
 		// TODO: Need to be un-hardcoded
 		serverTimer.scheduleAtFixedRate(serverTimerTask, 40000, 10000);
 		/********************************************************************/
 
 
 		final TextView tv_custom_title = (TextView) findViewById(R.id.tv_custom_title);
 		if (tv_custom_title != null)
 			tv_custom_title.setText("ToDo");
 		/*
 		final ImageButton ib_custom_search = (ImageButton) findViewById(R.id.ib_custom_search);
 		if (ib_custom_search != null)
 		 */
 
 		final ImageButton ib_custom_add = (ImageButton) findViewById(R.id.ib_custom_add);
 		if (ib_custom_add != null) {
 			ib_custom_add.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(v.getContext(), Add.class);
 					startActivityForResult(intent, 0);
 				}
 			});
 		}
 
 		Button b_maps = (Button) findViewById(R.id.b_maps);
 		b_maps.setBackgroundResource(R.drawable.menu_maps);
 		b_maps.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), GoogleMaps.class);
 				startActivityForResult(intent, 0);
 			}
 		});
 
 		Button b_todo_lists = (Button) findViewById(R.id.b_todo_lists);
 		b_todo_lists.setBackgroundResource(R.drawable.menu_to_do_lists);
 		b_todo_lists.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), ToDo_Lists.class);
 				startActivityForResult(intent, 1);
 			}
 		});
 		Button b_groups = (Button) findViewById(R.id.b_groups);
 		b_groups.setBackgroundResource(R.drawable.menu_groups);
 		b_groups.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), Groups.class);
 				startActivityForResult(intent, 2);
 			}
 		});
 
 		ImageView iv_background = (ImageView) findViewById(R.id.iv_background);
 		iv_background.setBackgroundResource(R.drawable.chocobo);
 		
 		//TextView tv_cloud1 = (TextView) findViewById(R.id.tv_cloud1);
 		//tv_cloud1.setBackgroundResource(R.drawable.gradient);
 		/** Temp stuffs */
 		/*
 		Date date = new Date();
 		Log.d("ServerDEBUG", Long.toString(date.getTime()));
 		 */
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.main_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.m_settings:
 			startActivity(new Intent(this, MainMenu.class));
 			return true;
 		case R.id.m_feedback:
 			Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 			intent.setData(Uri.parse("mailto:" + "dicz.hack@gmail.com, filbert.hansel@gmail.com, hilfialkaff@gmail.com"));
 			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ToDo Feedback");
 			startActivity(intent); 
 			return true;
 		case R.id.m_invitations:
 			startActivity(new Intent(this, InvitationWindow.class));
 			return true;
 		}
 		return false;
 	}
 }
