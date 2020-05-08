 package edu.mines.alterego;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.util.Pair;
 
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
         CharacterDBHelper dbhelper = new CharacterDBHelper(this);
 
        ArrayList<Pair<Integer, String>> game_list = dbhelper.get_games(db);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 }
