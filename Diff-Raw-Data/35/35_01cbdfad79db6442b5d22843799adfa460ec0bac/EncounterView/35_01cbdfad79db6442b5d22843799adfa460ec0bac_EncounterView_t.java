 package com.cs2340.spacetrader;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EncounterView extends Activity 
 {
 	private TextView encounterTitle;
 	private TextView encounterDescription;
 	private Button action1;
 	private Button action2;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_encounter);
         
         encounterTitle = (TextView) findViewById(R.id.encounter_title);
         encounterDescription = (TextView) findViewById(R.id.encounter_description);
         action1 = (Button) findViewById(R.id.encounter_action1);
         action2 = (Button) findViewById(R.id.encounter_action2);
         
         setupEncounter();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
         getMenuInflater().inflate(R.menu.menu_options, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) 
     {
         switch (item.getItemId()) {
             case R.id.save_game:
             	item.setEnabled(false);
             	item.setTitle("Saving");
             	SaveState state = new SaveState(GameSetup.thePlayer, GameSetup.theMap);
             	if (MemoryService.saveGame(state, this))
         		{
             		Toast.makeText(this, "Game saved successfully", Toast.LENGTH_SHORT).show();
         		}
             	else
             	{
             		Toast.makeText(this, "Failed to save game", Toast.LENGTH_SHORT).show();
             	}
             	item.setTitle("Save");
             	item.setEnabled(true);
                 return true;
             case R.id.quit_game:
             	Intent intent = new Intent(this, Launcher.class);
             	startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     /**
      * Populates fields with this particular encounter
      * Including the encounter's title, description (directions), and options
      */
     public void setupEncounter()
     {
     	/* Fill in */
    	encounterTitle.setText("Pirates!");
    	encounterDescription.setText("You have encountered pirates! Do you wish to fight or flee?"); 
    	action1.setText("Fight");
    	action2.setText("Flee");
     }
     
     /**
      * Consequences of user's choice of action1
      */
     public void fight(View view)
     {
     	GameSetup.thePlayer.getship().fight();
     	startPlanetView(view);
     }
     
     /**
      * Consequences of user's choice of action2
      */
     public void flee(View view)
     {
     	GameSetup.thePlayer.getship().flee();
     	startPlanetView(view);
     }
     
     /**
      * Changes activity to Planet
      * @param view PlanetView
      */
     public void startPlanetView(View view)
     {
     	Intent intent = new Intent(EncounterView.this, PlanetView.class);
     	startActivity(intent);
     }
 }
