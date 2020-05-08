 package se.chalmers.dat255.risk;
 
 import android.view.Menu;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.backends.android.AndroidApplication;
 
 
 public class MainActivity extends AndroidApplication {
 
 	@Override
 	protected void onCreate(android.os.Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		 initialize(new Game(), false);
 	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
 
 }
