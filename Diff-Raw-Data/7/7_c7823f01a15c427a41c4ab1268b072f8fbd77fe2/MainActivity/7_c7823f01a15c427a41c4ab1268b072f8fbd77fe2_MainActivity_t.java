 package com.example.simplemusicplayer;
 
 import java.util.ArrayList;
 
 import playlist.IPlaylist;
 import database.Database;
 
 
 
 
 
 import mainmodel.Model;
 import musicplayer.IPlayingSong;
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	//I just inserted this comment to make sure it pushed
 //	private ArrayList<String> songList = new ArrayList<String>();
 //	private ReadSongs readSong;
 	//List (interface) = ArrayList (impl)
 	//private IPlayingSong player = new AndroidPlayingSong();
 	
 	private TextView textDatabaseContents;
 	//private Button buttonRemove;
 	private Button buttonPlay;
 	private Button buttonPause;
 	//private Button buttonVolume;
 	//private TextView textSongTitle;
 	private Button buttonNext;
 	private Button buttonDB;
 	
 	private Database db;
     private IPlayingSong ps;
     private IPlaylist pl; 
 	
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         db = new AndroidDatabase(this);
         ps = new AndroidPlayingSong(db);
         pl = new AndroidPlaylist();
    	Model.create(ps, pl, db);
         
 //      AndroidDatabase database = new AndroidDatabase(this);    
  //     database.open();
         textDatabaseContents = (TextView) findViewById(R.id.textDatabaseContents);
         buttonPlay = (Button) findViewById(R.id.buttonPlay);
         buttonDB = (Button) findViewById(R.id.buttonDB);
         buttonPause = (Button) findViewById(R.id.buttonPause);
         buttonNext = (Button) findViewById(R.id.buttonNext);
         buttonPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
         	   play_song();
            }
         });
         buttonPause.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		pause_song();
         	}
         });
 
         buttonNext.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		playNext();
         	}
         });
         buttonDB.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		 fillDataText();
         	}
         });
        
         
         
         //readSong = new ReadSongs();
         //songList = readSong.getPlayList();
         
         
         //Log.d("MainActivity", "SongList size is " + songList.size());	
         
        
               
     //    playlist = new AndroidPlaylist();
         
     }
     
     public void play_song() {
     	Log.d("MainActivity", "Called play song");
     	
     	Model.get().first();
     }
     public void pause_song() {
     	Model.get().play_pause();
     	
     	
     }
     public void playNext() {
     	Model.get().next();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	Model.get().close();
     }
     
     private void fillDataText() {
         StringBuilder str = new StringBuilder();
     	
     	// Get all of the notes from the database and create the item list
     	AndroidDatabase mDbHelper = new AndroidDatabase(this);
     	mDbHelper.open();
         Cursor c = mDbHelper.getAll();
         startManagingCursor(c);
         
         c.moveToLast();
         str.append("Total Entries in DB: " + c.getCount());
         while(!c.isBeforeFirst()) {
         	str.append("\n\nIndex: ");
         	str.append(c.getInt(c.getColumnIndexOrThrow(AndroidDatabase.COLUMN_ID)));
         	
         	str.append("\nFilename: ");
         	str.append(c.getString(c.getColumnIndexOrThrow(AndroidDatabase.COLUMN_FILENAME)));
         
         	str.append("\nStartSec: ");
         	str.append(c.getLong(c.getColumnIndexOrThrow(AndroidDatabase.COLUMN_STARTSEC)));
         	
         	str.append("\nEndSec: ");
         	str.append(c.getLong(c.getColumnIndexOrThrow(AndroidDatabase.COLUMN_ENDSEC)));
         	
         	str.append("\nTimestamp: ");
         	str.append(c.getString(c.getColumnIndexOrThrow(AndroidDatabase.COLUMN_TIMESTAMP)));
         	
         	str.append("\n\n");
         	textDatabaseContents.setText(str.toString());
         	
         	c.moveToPrevious();
         	
         }
         
         //c.close();
         mDbHelper.close();
         
     }
     
 }
