 package tu.kom.uhg;
 
 import java.util.ArrayList;
 
 import com.google.gson.Gson;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.Toast;
 
 /**
  * Created by Kroshka on 11.06.13.
  */
 public class GenericActivity extends FragmentActivity {
     Context context;
     int duration;
     CharSequence text;
     Toast toast;
     String UserNickname = "Xena die Kriegerprinzessin";
     public final static String NICKNAME = "com.tud.uhg.NICKNAME";
     public final static int ACTIVATION_DISTANCE = 100;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         context = getApplicationContext();
         duration = Toast.LENGTH_SHORT;
     }
 
     public void mapView(View v) {
         if (//!this.getLocalClassName().equals("MainActivity") &&
     		!this.getLocalClassName().equals("MapActivity")){
         	//call the login class
             Intent intent = new Intent(this, MapActivity.class);
             startActivity(intent);
         	finish();
         }
     }
 
     public void settingsView(View v) {
         if (!this.getLocalClassName().equals("SettingsActivity")){
             //call the login class
             Intent intent = new Intent(this, SettingsActivity.class);
             intent.putExtra(NICKNAME, UserNickname);
             startActivity(intent);
 	        if (!this.getLocalClassName().equals("MainActivity") &&
 	    		!this.getLocalClassName().equals("MapActivity")){
 	        	finish();
 	        }
         }
     }
 
     public void gamesView(View v) {
         if (!this.getLocalClassName().equals("GamesActivity")){
             //call the login class
             Intent intent = new Intent(this, GamesActivity.class);
             startActivity(intent);
 	        if (!this.getLocalClassName().equals("MainActivity") &&
 	    		!this.getLocalClassName().equals("MapActivity")){
 	        	finish();
 	        }
         }
     }
 
     public void buddiesView(View v) {
         if (!this.getLocalClassName().equals("BuddiesActivity")){
             //call the login class
             Intent intent = new Intent(this, BuddiesActivity.class);
             startActivity(intent);
 	        if (!this.getLocalClassName().equals("MainActivity") &&
 	    		!this.getLocalClassName().equals("MapActivity")){
 	        	finish();
 	        }
         }
     }
 
     public void scoresView(View v) {
         if (!this.getLocalClassName().equals("ScoresActivity")){
             //call the login class
             Intent intent = new Intent(this, ScoresActivity.class);
             startActivity(intent);
             if (!this.getLocalClassName().equals("MainActivity") &&
 	    		!this.getLocalClassName().equals("MapActivity")){
 	        	finish();
             }
         }
     }
 
     public void meView(View v) {
         if (!this.getLocalClassName().equals("MeActivity")){
             //call the login class
             Intent intent = new Intent(this, MeActivity.class);
             startActivity(intent);           
             if (!this.getLocalClassName().equals("MainActivity") &&
 	    		!this.getLocalClassName().equals("MapActivity")){	
 	        	finish();
             }
         }
     }
     
    public void addScore (String gameName, String date, Integer score) {
 		//read scores from sharedPrefs
 		SharedPreferences prefs = getSharedPreferences("scores", MODE_PRIVATE);
 		String scoresStr = prefs.getString("scores", "");
 		//decode gson string to the scores object
 		Gson gson = new Gson();
 		Score scores = gson.fromJson(scoresStr, Score.class);
 		//append the new score
 		scores.append(gameName, date, score);
 		//encode back & save back to prefs
 		Editor prefsEditor = prefs.edit();
 		gson = new Gson();
 		String json = gson.toJson(scores);
 		prefsEditor.putString("scores", json);
 		prefsEditor.commit();
     }
     
 	public class Score {
 		public ArrayList<Game> games = new ArrayList<Game>();
 		public ArrayList<String> gameNames = new ArrayList<String>();
 		
 		public Score addGame (String newGameName, double headRatio, double armsRatio, double legsRatio){
 			Game game = new Game(headRatio, armsRatio, legsRatio);
 			gameNames.add(newGameName);
 			games.add(game);
 			return this;
 		}
 		
 		public Score append (String gameName, String date, Integer score){
 			Integer gameNumber = gameNames.indexOf(gameName);
 			/*
 			Game game = games.get(gameNumber);
 			game.addScore(date, score);
 			games.add(gameNumber, game);
 			*/
 			games.get(gameNumber).addScore(date, score);
 
 			return this;
 		}
 		
 		public class Game{
 			public ArrayList<String> dates = new ArrayList<String>();
 			public ArrayList<Integer> scores = new ArrayList<Integer>();
 			public ArrayList<Double> ratio = new ArrayList<Double>();//head, arms, legs
 			
 			public Game(double headRatio, double armsRatio, double legsRatio){
 				ratio.add(headRatio);
 				ratio.add(armsRatio);
 				ratio.add(legsRatio);
 			}
 			
 			public void addScore (String newDate, Integer newScore){
 				dates.add(newDate);
 				scores.add(newScore);
 			}
 			public Integer getTotalScore () {
 				Integer result = 0;
 				if(scores.size() > 0) {
 					for(int i = 0; i < scores.size(); i++) {
 						result += scores.get(i);
 					}
 				}				 
 				return result;
 			}
 			
 			public Double getHeadScore () {
 				return getTotalScore() * ratio.get(0);
 			}
 			public Double getArmsScore () {
 				return getTotalScore() * ratio.get(1);
 			}
 			public Double getLegsScore () {
 				return getTotalScore() * ratio.get(2);
 			}
 		}
 	}
 }
