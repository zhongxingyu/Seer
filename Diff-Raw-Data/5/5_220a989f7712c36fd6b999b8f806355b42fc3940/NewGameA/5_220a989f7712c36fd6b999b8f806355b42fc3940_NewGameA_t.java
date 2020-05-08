 package dominion.android;
 
 import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class NewGameA extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.newgame);
         
 		Button series = (Button) findViewById(R.id.startGame);
 		series.setOnClickListener(new OnClickListener() {
 			public void onClick(View clicked) {
 				Toast.makeText(NewGameA.this, "Starting game...", Toast.LENGTH_SHORT).show();

 				// First collect the player names.
 				ArrayList<String> players = new ArrayList<String>();
 				int[] editTextIds = new int[] { R.id.player1, R.id.player2, R.id.player3, R.id.player4 };
 				
 				for(int i = 0; i < editTextIds.length; i++) {
 					TextView et = (TextView) findViewById(editTextIds[i]);
 					String name = et.getText().toString();
 					if(name != null && name.length() > 0) {
 						players.add(name);
 					}
 				}
 				
 				if (players.size() < 2) {
 					Toast t = Toast.makeText(NewGameA.this, "You must have at least 2 players.", Toast.LENGTH_SHORT);
 					t.show();
 					return;
 				}
 				
 				StringBuffer sb = new StringBuffer();
 				sb.append("Players: ");
 				for(String p : players) {
 					sb.append(p);
 					sb.append(" ");
 				}
 				
 				Intent service = new Intent(NewGameA.this.getApplicationContext(), GameService.class);
 				service.putExtra("players", players);
 				Log.i(Constants.TAG, "Launching service.");
 				startService(service);
 				
 				Log.i(Constants.TAG, "Service started, launching DominionA.");
 				Intent i = new Intent(NewGameA.this.getApplicationContext(), DominionA.class);
 				startActivity(i);
 			}
 		});
     }
 }
