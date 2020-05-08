 package edu.calpoly.catchmeifyoucan;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class GameOverPage extends Activity implements OnClickListener {
 	
	TextView buttonGameOver;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game_over_page);
		buttonGameOver = (TextView)findViewById(R.id.button_game_over);
 		buttonGameOver.setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_game_over_page, menu);
 		return true;
 	}
 
 	public void onClick(View btnClicked) {
 		if(btnClicked == buttonGameOver){
 			Intent i = new Intent(this, MainPage.class);
 			startActivity(i);
 			finish();
 		}
 	}
 
 }
