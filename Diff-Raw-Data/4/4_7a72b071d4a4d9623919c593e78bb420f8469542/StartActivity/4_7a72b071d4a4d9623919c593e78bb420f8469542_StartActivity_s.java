 package com.example.wecharades.views;
 
 
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
 
 import com.example.wecharades.R;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.presenter.SeparatedListAdapter;
 import com.example.wecharades.presenter.StartPresenter;
 
 /**
  * 
  * @author Alexander
  *
  */
 public class StartActivity extends GenericActivity {
 	protected static final String TAG = "StartScreen";
 	public final static String ITEM_TITLE = "title";
 	public final static String ITEM_CAPTION = "caption";
 
 	private StartPresenter presenter;
 
 	// Adapter for ListView Contents
 	private SeparatedListAdapter adapter;
 
 	// ListView Contents
 	private ListView gameListView;
 
 	private Button invitations;
 	private Button account;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState, new StartPresenter(this));
 		// Sets the View Layer
 		setContentView(R.layout.list_screen);
 		
 		// Get a reference to views
         gameListView = (ListView) findViewById(R.id.list);
 		
 		// Inflate Start screen header in the ListView
 		View header = LayoutInflater.from(this).inflate(R.layout.start_screen_header, gameListView, false);
 		gameListView.addHeaderView(header);
 		
 		invitations = (Button) findViewById(R.id.invitations);
 		account = (Button) findViewById(R.id.account);
 		
 		// Sets the presenter
 		presenter = (StartPresenter) super.getPresenter();
 
 		//TODO All this should probably be done in PRESENTER?
 		// Create the ListView Adapter
 		adapter = new SeparatedListAdapter(this);
 		
 		//Check if the user is logged in or saved in the cache
 		presenter.checkLogin();
 		
 	}
 
 	public void onStart(){
 		super.onStart();
 		
 		//TODO here the code for updating the view should be included.
 		presenter.update();
 		
 		// Set the adapter on the ListView holder //TODO Assign adapter in presenter?
 		gameListView.setAdapter(presenter.setAdapter(adapter));
         // Listen for Click events
         gameListView.setOnItemClickListener(new OnItemClickListener() {
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
         		Game game = (Game) adapter.getItem(position-1);
         		Intent intent = new Intent(getApplicationContext(), GameDashboardActivity.class);
         		intent.putExtra("Game", game);
         		startActivity(intent);
             }
         });
 	}
 	
 	/**
 	 * 
 	 * @param v
 	 */
 	public void onClickInvitations(View v) {
 		Intent intent = new Intent(this, InvitationActivity.class);
 		startActivity(intent);
 	}
 	
 	/**
 	 * 
 	 * @param v
 	 */
 	public void onClickHighscore(View v) {
 		Intent intent = new Intent(this, HighScoreActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Go to New Game screen
 	 * @param view
 	 */
 	public void onClickNewGame(View view) {
 		Intent intent = new Intent (this, NewGameActivity.class);
 		startActivity(intent);
 	}
 	
 	/**
 	 * Go to High Score screen
 	 * @param view
 	 */
 	public void onClickHighScore(View view) {
//		Intent intent = new Intent (this, HighScoreActivity.class);
//		startActivity(intent);
 	}
 
 
 	/**
 	 * Nothing happens so far...
 	 * @param view
 	 */
 	public void onClickAccount(View view) {
 		Intent intent = new Intent (getApplicationContext(), AccountActivity.class);
 		startActivity(intent);
 	}
 	
 	public void setAccountName(String user){
 		account.setText(user);
 	}
 
 	public void setInvitations(int nrInvites){
 		if (nrInvites != 0) {
 			invitations.setText("+" + nrInvites);
 		}
 	}
 
 	@Override
 	public TextView getTextArea() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
