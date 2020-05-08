 package fr.umlv.escape;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 
 import fr.umlv.escape.editor.EditedLevel;
 import fr.umlv.escape.file.IllegalFormatContentFile;
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.game.Game;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Layout;
 import android.view.Display;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 
 public class GameActivity extends Activity implements OnItemSelectedListener{
 	FrontApplication frontApplication;
 	Game game;
 	public static Context context;
 	Spinner spinner_level;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Display display = getWindowManager().getDefaultDisplay();
 		int width = display.getWidth();  // deprecated
 		int height = display.getHeight();
 		
 		frontApplication = new FrontApplication(this,width,height);
 		this.game = Game.getTheGame();
 		setContentView(R.layout.levels_menu);
 		
 		spinner_level = (Spinner) findViewById(R.id.edited_level_select);
 		
 		File dir = getCacheDir();
 		File[] editedlevels = dir.listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				return true;
 			}
 		});
		System.out.println(spinner_level);
 		ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, android.R.layout.simple_spinner_dropdown_item,editedlevels);
 		spinner_level.setAdapter(adapter);
 		spinner_level.setOnItemSelectedListener(this);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		
 		game.stop();
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
 	
 	public void launchLevel(View view){
 		String[] levelsNames=null;
 		
 		switch(view.getId())
 		{
 		case R.id.level1Button:{
 			levelsNames = new String[3];
 			levelsNames[0]="level1";
 			levelsNames[1]="level2";
 			levelsNames[2]="level3"; 
 			break;
 		}
 		case R.id.level2Button: {
 			levelsNames = new String[2];
 			levelsNames[0]="level2";
 			levelsNames[1]="level3"; 
 			break;
 		}
 		case R.id.level3Button: {
 			levelsNames = new String[1];
 			levelsNames[0]="level3"; 
 			break;
 		}
 		default:
 			return;
 		}
 		
 		try {
 			this.game.initializeGame(this,frontApplication,levelsNames);	
 			this.game.startGame(getApplicationContext());
 			setContentView(frontApplication);
 		} catch (IOException e) {
 			onDestroy();
 		} catch (IllegalFormatContentFile e) {
 			onDestroy();
 		}	
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
 		String[] levelsNames=new String[0]; 
 		levelsNames[0] = (String) parent.getItemAtPosition(pos);
 		try {
 			this.game.initializeGame(this,frontApplication,levelsNames);	
 			this.game.startGame(getApplicationContext());
 			setContentView(frontApplication);
 		} catch (IOException e) {
 			onDestroy();
 		} catch (IllegalFormatContentFile e) {
 			onDestroy();
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub	
 	}
 }
