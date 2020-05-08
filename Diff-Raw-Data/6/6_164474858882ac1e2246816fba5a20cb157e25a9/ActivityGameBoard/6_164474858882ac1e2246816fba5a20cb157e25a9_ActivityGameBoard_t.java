 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package pl.edu.amu.wmi.lsm.snake;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Color;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author AnitkA
  */
 public class ActivityGameBoard extends Activity {
    private SharedPreferences sp;
    private final String SHARED_PREFERENCE = "Settings";
    private final String KEY_LANG = "lang";
    private final String KEY_COL = "color";
    private final String KEY_SOUND = "sound";
    private final String KEY_IS_PAUSE = "isPause";
    private MediaPlayer mediaPlayer;
 
 	public void onCreate(Bundle savedInstanceState) {
                 View view = this.findViewById(android.R.id.content);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gameboard);
                 sp = this.getApplicationContext().getSharedPreferences(SHARED_PREFERENCE, Activity.MODE_PRIVATE);
 
             Intent j = getIntent();
             Bundle bundle = j.getExtras();
             String value = bundle.getString(KEY_COL);
 
//                  String tmp = sp.getString(KEY_COL, "");
//                  tmp = loadPreferences(KEY_COL);
//                Toast.makeText(getApplicationContext(), value ,Toast.LENGTH_SHORT).show();
 
                 //String[] colors = sp.getString(KEY_COL, "0;255;0").split(";");
                 String[] colors = value.split(";");
                 int[] kolory = null;
                 try {
                     kolory = convertStringArraytoIntArray(colors);
                 } catch (Exception ex) {
                     Logger.getLogger(ActivityGameBoard.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 view.setBackgroundColor(Color.rgb(kolory[0],kolory[1],kolory[2]));
 
                 //Boolean tmp = sp.getBoolean(KEY_SOUND, );
                 //Toast.makeText(getApplicationContext(), tmp.toString() ,Toast.LENGTH_SHORT).show();
                 if(sp.getBoolean(KEY_SOUND, true)) //dzwiek
                 {
                     mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.maintheme);
                     mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 }
                 else  //bez dzwieku
                 {
 
                 }
 	}
 	
         @Override
         public void onPause() {
             super.onPause();  // Always call the superclass method first
             Editor ed = sp.edit();
             ed.putBoolean(KEY_IS_PAUSE, true);
             ed.commit();
             mediaPlayer.pause();
         }
 
         @Override
         public void onResume() {
             super.onResume();  // Always call the superclass method first
             Editor ed = sp.edit();
             ed.putBoolean(KEY_IS_PAUSE, false);
             ed.commit();
             mediaPlayer.start();
         }
 
         public void onClickPause(View view) {
             if (!sp.getBoolean(KEY_IS_PAUSE, false)) onPause();
             else onResume();
 	}
 
 	public void onClickExit(View view) { //exit
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                             Intent i = new Intent(ActivityGameBoard.this, SnakeActivity.class);
                             //i.setClass(view.getContext(), SnakeActivity.class);
                             startActivity(i); 
                             //ActivityGameBoard.this.finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                             dialog.cancel();
                        }
                    });
             AlertDialog alert = builder.create();
             alert.show();
 	}
 
 	public void onClickLeft(View view) {
 
 	}
 
 	public void onClickRight(View view) {
 
 	}
 
         private void alertExit() {
 
         }
 
         private String loadPreferences(String key) {
             SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
             String loadedString = sharedPreferences.getString(key, "");
             return loadedString;
         }
 
 public int[] convertStringArraytoIntArray(String[] sarray) throws Exception {
     if (sarray != null) {
     int intarray[] = new int[sarray.length];
     for (int i = 0; i < sarray.length; i++) {
     intarray[i] = Integer.parseInt(sarray[i]);
     }
     return intarray;
     }
     return null;
     }
 }
