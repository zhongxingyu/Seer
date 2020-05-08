 package tu.kom.uhg;
 
 import android.content.Context;
 import android.content.Intent;
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
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         context = getApplicationContext();
         duration = Toast.LENGTH_SHORT;
     }
 
     public void mapView(View v) {
         if (!this.getLocalClassName().equals("MainActivity") &&
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
 }
