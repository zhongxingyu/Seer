 package pl.edu.amu.wmi.lsm.snake;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
import android.widget.Toast;
 
 public class SnakeActivity extends Activity {
    private final String KEY_COL = "color";
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// mojsaidas
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	}
 
 	public void onClick(View view) {
            Intent j = getIntent();
            Bundle bundle = j.getExtras();
            String value = bundle.getString(KEY_COL);
 
 		Intent i = new Intent(this, ActivityGameBoard.class);
                i.putExtra(KEY_COL, value);
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();

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
