 package org.game.advwars;
 
 import dataconnectors.DBAndroidConnector;
 import dataconnectors.DBCheckPlayerName;
 import dataconnectors.SaveGUIData;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class ChangePlayerName extends Activity implements OnClickListener
 {
 	private String playerName = null;
 	private String oldPlayerName = null;
 	private EditText newPlayerName = null;
 	private GUIGameValues changePlayerGGV = new GUIGameValues();
 	private SaveGUIData sgd = new SaveGUIData();
 	
 	protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.change_player_name);
         
         // Set up text box and button
         newPlayerName = (EditText) findViewById(R.id.change_player_name_textbox);
         Button changePlayerName = (Button) findViewById(R.id.change_player_name_button);
         
         // Default text box text
         newPlayerName.setText("");
         
         changePlayerName.setOnClickListener(this);
     }
 	
 	@Override
 	public void onClick(View v)
 	{
		boolean cancelPlayerNameEntry = true;
 		DBAndroidConnector dbc = new DBAndroidConnector();
 		playerName = newPlayerName.getText().toString();
 		
 		changePlayerGGV = sgd.loadGGVData();
 		oldPlayerName = changePlayerGGV.getPlayerName();
 		
 		DBCheckPlayerName checkPlayer = new DBCheckPlayerName();
 		
 		if(checkPlayer.playerNameIsValid(playerName))
			cancelPlayerNameEntry = false;
		
		if(playerName.length() > 15)
 			cancelPlayerNameEntry = true;
 
		if (!playerName.equals("") && !playerName.equals("VALUE_NOT_SET") && !cancelPlayerNameEntry)
 		{
 			changePlayerGGV.setPlayerName(playerName);
 			dbc.executeQuery("update PlayerSettings set Player_Name = '" + playerName + "' where Player_Name = '" + oldPlayerName + "';");
 			dbc.executeQuery("update Scores set Player_Name = '" + playerName + "' where Player_Name = '" + oldPlayerName + "';");
 			dbc.closeDB();
 			sgd.saveGGVData(changePlayerGGV);
 
 			Intent i = new Intent(this, MainMenu.class);
 			i.putExtra("ggv", changePlayerGGV);
 			startActivity(i);
 		}
 		else
 		{
 			Intent i2 = new Intent(this, InvalidPlayerNameEntered.class);
 			startActivity(i2);
 		}
 	}
 }
