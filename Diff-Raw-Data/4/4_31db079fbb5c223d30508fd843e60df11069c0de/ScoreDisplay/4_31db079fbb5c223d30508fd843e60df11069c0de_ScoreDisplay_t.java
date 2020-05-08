 package nl.hr.minor.jjs.pogo;
 
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 
 public class ScoreDisplay {
 
 	private Paint _p;
 	private Map<Integer, Player> _playerlist;
 	private String _scoreText;
 	private Player _player;
 	private Activity _gameActivity;
 	
 	public ScoreDisplay(Map<Integer, Player> playerlist, Context mainScreen) {
 		
 		_playerlist = playerlist;
 		_gameActivity = (Activity) mainScreen;
 		
 		_p = new Paint();
 		_p.setColor(Color.WHITE);
 		_p.setAlpha(200);
 		_p.setTextSize(18);
 		
 	}
 	
 	public void draw(Canvas c){
 		
 		_scoreText = "";
 		
 		// Display scores
 		// Todo: Change sorting order. For some reason they don't order correctly (4-1-2-3 instead of 1-2-3-4)
 		for(Integer playerIndex : _playerlist.keySet()){
 			
 			_player = _playerlist.get(playerIndex);
 			_scoreText += "Player " + _player._id + ": " + _player.getScore() + " - ";
 		}
 		
		// Remove last 2 chars (- ) from scoreText
		_scoreText = _scoreText.substring(0, _scoreText.length()-2);
		
		// Draw score
 		c.drawText(_scoreText, 10, 460, _p);
 	}
 	
 	public void showFinalScore(){
 		Intent h = new Intent(ContextHolder.getInstance().getContext(), ScoreScreen.class);
 		
 		for(Integer playerIndex : _playerlist.keySet()){
 			
 			_player = _playerlist.get(playerIndex);
 			h.putExtra("score"+_player._id, Integer.toString(_player.getScore()));
 		}
 		
 		_gameActivity.finish();
 		ContextHolder.getInstance().getContext().startActivity(h);
 	}
 
 }
