 package fr.umlv.escape;
 
 import java.io.IOException;
 
 import fr.umlv.escape.file.IllegalFormatContentFile;
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.game.Game;
 import android.app.Activity;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 public class GameActivity extends Activity{
 	FrontApplication frontApplication;
 	Game game;
 	public static Context context;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		frontApplication = new FrontApplication(this);
 		this.game = Game.getTheGame();
 		this.game.setFrontApplication(frontApplication);
		setContentView(frontApplication);
 		try {
 			this.game.initializeGame(frontApplication);
 			this.game.startGame(getApplicationContext());
			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalFormatContentFile e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	
 	/**
 	 * Asynchronous task that initialize the application at the launching
 	 * and set the content view with the main menu
 	 */
 	private class LaunchApplication extends AsyncTask<Void, Integer, Void>{
 		@Override
 		protected Void doInBackground(Void... params) {
 			//Initialize here the game
 			for(int i=0; i<20; ++i){
 				try {
 					Thread.sleep(5); // SImule traitement
 					publishProgress(i*5);
 				} catch (InterruptedException e) {
 					break;
 				}
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 		}
 	}
 
 }
