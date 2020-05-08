 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import se.chalmers.dryleafsoftware.androidrally.GameConfigurationActivity;
 import se.chalmers.dryleafsoftware.androidrally.IO.IOHandler;
 import se.chalmers.dryleafsoftware.androidrally.game.GameSettings;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
 
 /**
  * This is the activity which will simply start the game.
  * 
  * @author
  * 
  */
 public class GameActivity extends AndroidApplication {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
 		cfg.useGL20 = false;
 		// Turn off these to save battery
 		cfg.useAccelerometer = false;
 		cfg.useCompass = false;
 		
 		Intent config = getIntent();
 		
 		int bots = config.getIntExtra(GameConfigurationActivity.BOTS_INTENT_EXTRA, 7);
 		int hours = config.getIntExtra(GameConfigurationActivity.HOURS_INTENT_EXTRA, 24);
 		int card = config.getIntExtra(GameConfigurationActivity.CARD_TIME_INTENT_EXTRA, 45);
 				
 		GameSettings settings = new GameSettings(1, bots, hours, card);
 		GameSettings.setCurrentSettings(settings);
		initialize(new GdxGame(IOHandler.getNewID(), true), cfg);
 	}
 }
