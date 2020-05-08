 package com.example.wecharades.views;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.wecharades.R;
 import com.example.wecharades.SeparatedListAdapter;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.presenter.StartPresenter;
 
 /**
  * 
  * @author Alexander
  *
  */
 public class StartActivity extends Activity {
 	
 	protected static final String TAG = "StartScreen";
 	public final static String ITEM_TITLE = "title";
 	public final static String ITEM_CAPTION = "caption";
 
 	private StartPresenter presenter;
 
 	// Adapter for ListView Contents
 	private SeparatedListAdapter adapter;
 
 	// ListView Contents
 	private ListView gameListView;
 
 	private TextView displayUser; 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		
 		// Sets the View Layer
 		setContentView(R.layout.start_screen);
 		
		// Gat a reference to the dispalyUser field
		displayUser = (TextView) findViewById(R.id.user_display);
		
 		// Get a reference to the ListView holder
         gameListView = (ListView) this.findViewById(R.id.game_list);
 		
 		// Inflate Start screen header in the ListView
 		View header = LayoutInflater.from(this).inflate(R.layout.start_screen_header, gameListView, false);
 		gameListView.addHeaderView(header);
 		
 		
	//---------	
 		// Sets the presenter
 		presenter = new StartPresenter (this);
 		
 		//Check if the user is logged in or saved in the cache
 		//presenter.checkLogin();		
 
 		//TODO All this should probably be done in PRESENTER?
 		// Create the ListView Adapter
 		adapter = new SeparatedListAdapter(this);
 
 	}
 
	public void onStart(Bundle savedStateBundle){
 		super.onStart();
 		
 		//TODO here the code for updating the view should be included.
 		presenter.update();
 		
 		// Set the adapter on the ListView holder //TODO Assign adapter in presenter?
 		gameListView.setAdapter(presenter.setAdapter(adapter));
         // Listen for Click events
         gameListView.setOnItemClickListener(new OnItemClickListener() {
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
         		Game item = (Game) adapter.getItem(position-1);
         		Intent intent = new Intent(getApplicationContext(), GameDashboardActivity.class);
         		intent.putExtra("GameId", item);
             }
         });
 	}
 
 	/**
 	 * Logout and go back to login screen
 	 * @param view
 	 */
 	public void onClickLogout(View view) {
 		//TODO some of this code should probably be moved to the presenter.
 		presenter.logOut();
 		//Redirecting to LoginActivity
 //		Intent login = new Intent(getApplicationContext(), LoginActivity.class);
 //		login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 //		startActivity(login);
 		// Closing start screen
 		finish();
 	}
 
 	public Map<String, ?> createItem(String title, String caption){
 		Map<String, String> item = new HashMap<String, String>();
 		item.put(ITEM_TITLE, title);
 		item.put(ITEM_CAPTION, caption);
 		return item;
 	}
 
 	/**
 	 * Go to New Game screen
 	 * @param view
 	 */
 	public void onClickNewGame(View view) {
 		Button b = (Button) view;
 		//presenter.showToast(getApplicationContext(), b.getText().toString());
 		Intent intent = new Intent (getApplicationContext(), NewGameScreen.class);
 		Toast.makeText(getApplicationContext(), b.getText().toString(), Toast.LENGTH_SHORT).show();
 		startActivity(intent);
 	}
 
 	/**
 	 * Nothing happens so far...
 	 * @param view
 	 */
 	public void onClickAccount(View view) {
 		Log.d("Clicked", "Account");
 		Button b = (Button) view;
 		Toast.makeText(getApplicationContext(), b.getText().toString(), Toast.LENGTH_SHORT).show();
 		Intent intent = new Intent (getApplicationContext(), GameDashboardActivity.class);
 		startActivity(intent);
 	}
 	
 	public void setDisplayName(String user){
 		displayUser.setText(user);
 	}
 }
