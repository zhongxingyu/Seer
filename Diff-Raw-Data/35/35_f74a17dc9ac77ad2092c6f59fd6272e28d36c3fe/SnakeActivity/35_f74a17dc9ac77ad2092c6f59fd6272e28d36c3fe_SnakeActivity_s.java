 package pl.edu.amu.wmi.lsm.snake;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 
 public class SnakeActivity extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// mojsaidas
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	}
 
 	public void onClick(View view) {
 
 		Intent i = new Intent(this, ActivityGameBoard.class);
             //Intent i = new Intent(this, ActivityNewGame.class);
 		//i.setClass(view.getContext(), ActivityNewGame.class);
 		startActivity(i);
 	}
 
 	public void onClick2(View view) {
 
 		Intent i = getIntent();
 		i.setClass(view.getContext(), ActivityContinueGame.class);
 		startActivity(i);
 	}
 
 	public void onClick3(View view) {
 
 		Intent i = getIntent();
 		i.setClass(view.getContext(), ActivityViewHighScores.class);
 		startActivity(i);
 	}
 
 	public void onClick4(View view) {
 
 		Intent i = getIntent();
 		i.setClass(view.getContext(), ActivityViewSettings.class);
 		startActivity(i);
 	}
 
 	public void onClick5(View view) {
 
 		Intent i = getIntent();
 		i.setClass(view.getContext(), ActivityViewHighScores.class);
 		startActivity(i);
 	}
 }
