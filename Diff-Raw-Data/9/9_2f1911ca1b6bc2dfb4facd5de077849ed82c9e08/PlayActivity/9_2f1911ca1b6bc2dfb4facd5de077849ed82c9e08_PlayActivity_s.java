 package se.chalmers.chessfeud;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import se.chalmers.chessfeud.constants.DbHandler;
 import se.chalmers.chessfeud.constants.Game;
 import se.chalmers.chessfeud.view.GameView;
 import android.app.Activity;
 import android.os.Bundle;
 
 public class PlayActivity extends Activity implements PropertyChangeListener{
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play);
 		
 		GameView gv = (GameView) findViewById(R.id.chessBoard);
 		String gameInfo = getIntent().getStringExtra("GameString");
 		int position = getIntent().getIntExtra("Position", -1);
 		if(gameInfo != null)
 			gv.setGameModel(gameInfo, position, this);
 		
 		
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if(event.getPropertyName().equals("Model")){
 			Game gameInfo = (Game)event.getOldValue();
 			String gameBoard = (String)event.getNewValue();
 			try{
				DbHandler dbh = new DbHandler();
 				dbh.newMove(gameInfo.getWhitePlayer(), gameInfo.getBlackPlayer(), gameBoard);
 			}catch(Exception e){//Add exception message TODO:
 			}
 		}
 	}
 }
