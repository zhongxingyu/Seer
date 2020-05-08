 package com.example.stereoplayer;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RatingBar;
 import android.widget.SimpleAdapter;
 import android.widget.SimpleAdapter.ViewBinder;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class AdvancedMainActivity extends Activity
 {
 	/* Set this to true if debugging saving and loading play list and/or rating list*/
 	boolean Debug = false;
 
 	/* Constants */
 	final static int ONE_BYTE = 1;
 	final static int MAX_BYTES = 255;
 	final static int dragDrop = 3;
 
 	/* Variables */	
 	private MyApplication app;
 	private ArrayList<String[]> mainPlaylist;
 	private String[] rawPlaylist;
 	private TCPReadTimerTask tcp_task;
 	private int songIndex;
 	private int songVolume;
 	private int currentSongPositionInTime;
 	private ArrayList<HashMap<String, String>> list;
 
 	public class TCPReadTimerTask extends TimerTask 
 	{		
 		public void run() 
 		{
 			MyApplication app = (MyApplication) getApplication();
 			if (app.sock != null && app.sock.isConnected() && !app.sock.isClosed()) 
 			{
 				try 
 				{
 					InputStream in = app.sock.getInputStream();
 					int bytes_avail = in.available();
 
 					if (bytes_avail > 0) 
 					{						
 						byte buf[] = new byte[ONE_BYTE];
 						in.read(buf);
 						String msg = new String(buf, 0, ONE_BYTE, "US-ASCII");
 						String data = new String();
 
 						if (msg.compareTo("M") == 0 || msg.compareTo("I") == 0 || msg.compareTo("O") == 0 )
 						{							
 							while ( in.available() == 0 );
 							bytes_avail = in.available();
 
 							byte buffer[] = new byte[ONE_BYTE];
 							in.read(buffer);
 							int numBytesOfNumber = buffer[0];
 
 							buffer = new byte[numBytesOfNumber];
 
 							while( in.available() < numBytesOfNumber );
 							in.read(buffer);
 							String temp = new String( buffer, 0, numBytesOfNumber, "US-ASCII" );
 
 							int numBytesOfData = Integer.parseInt( temp );
 
 							buffer = new byte[ONE_BYTE];
 							for ( int i = 0; i < numBytesOfData; )
 							{
 								if ( in.available() > 0 )
 								{
 									in.read(buffer);
 									data = data.concat( new String(buffer, 0, ONE_BYTE, "US-ASCII") );
 									i++;
 								}
 							}
 						}
 
 						final String command = new String(msg);
 						final String message = new String( data );
 
 						Log.i("AdvancedMain", "Handshake - Commands is: " + command);
 						Log.i("AdvancedMain", "Handshake - message is: " + message);
 
 						if (msg.compareTo("M") == 0)
 						{
 							Log.i("Shuffle", "String data is: " + data);
 							Log.i("Shuffle", "String message is: " + message);
 						}
 
 						runOnUiThread(new Runnable() 
 						{
 							public void run() 
 							{								
 								TextView volumeT = (TextView) findViewById(R.id.viewText1);
 								TextView text = (TextView) findViewById(R.id.viewText2);
 
 								if ( command.compareTo( "P" ) == 0 )
 								{
 									text.setText("Playing: " + mainPlaylist.get(songIndex)[1]);
 								}
 								else if (command.compareTo("p") == 0) 
 								{
 									text.setText("Paused");
 								} 
 								else if (command.compareTo("S") == 0) 
 								{
 									text.setText("Stopped");
 									currentSongPositionInTime = 0;
 								} 
 								else if (command.compareTo("U") == 0) 
 								{
 									volumeT.setText("Volume = " + Integer.toString(songVolume));
 								} 
 								else if (command.compareTo("D") == 0) 
 								{
 									volumeT.setText("Volume = " + Integer.toString(songVolume));
 								}
 								else if (command.compareTo("O") == 0) 
 								{
 									currentSongPositionInTime = Integer.parseInt( message );
 
 									updateProgressBar();
 
 									int songLength = Integer.parseInt( mainPlaylist.get(songIndex)[4] );
 									updateTime( currentSongPositionInTime, songLength );
 									
 									text.setText("Playing: " + mainPlaylist.get(songIndex)[1]);
 
 									Log.i("Prog", "currentSongPosition is: " + currentSongPositionInTime );
 								}
 								else if (command.compareTo("M") == 0) 
 								{
 									Log.i( "AdvancedMain", "Successfully reached here" );
 
 									ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
 									pb.setProgress( 0 );
 
 									songIndex = Integer.parseInt( message );
 									Log.i("indexNumber", Integer.toString(songIndex));
 									text.setText("Playing: " + mainPlaylist.get(songIndex)[1] );
 
 									int songLength = Integer.parseInt( mainPlaylist.get(songIndex)[4] );
 									setupTime( songLength );
 								}
 								else if ( command.compareTo( "I" ) == 0 )
 								{
 									songIndex = Integer.parseInt( message );
 									Log.i("indexNumber", Integer.toString(songIndex));
 									
 									updateProgressBar();
 									int songLength = Integer.parseInt( mainPlaylist.get(songIndex)[4] );
 									setupTime( songLength );
 									updateTime( currentSongPositionInTime, songLength );
 									//text.setText("Playing: " + mainPlaylist.get(songIndex)[1]);
 								}
 								else
 								{
 
 								}
 							}
 						});
 					}
 
 					/* Debugging purpose */
 					if ( Debug )
 					{
 						String[] list = new String[100];
 						for ( int i = 0; i < 100; i++ )
 						{
 							list[i] = Integer.toString( i );
 						}
 
 						/* playlist */
 						Log.i( "list", "Before save playList" );
 						savePlayList( "playList.txt", list );
 						Log.i( "list", "After save playList and Before load PlayList" );
 
 						list = loadPlayList( "playList.txt" );
 						Log.i( "list", "After Load playList" );
 
 						for ( int i = 0; i < list.length; i++ )
 							Log.i( "list", list[i] );
 
 						sendCurrentPlayListToDE2( list );
 
 						/* rating */
 						for ( int i = 0; i < 100; i++ )
 						{
 							list[i] = Integer.toString( 99-i );
 						}
 
 						Log.i( "list", "Before save saveRating" );
 						saveRating( list );
 						Log.i( "list", "After save saveRating and Before load saveRating" );
 
 						list = loadRating( );
 						Log.i( "list", "After Load saveRating" );
 
 						for ( int i = 0; i < list.length; i++ )
 							Log.i( "list", list[i] );
 
 						Debug = false;
 					}
 				} 
 				catch (IOException e) 
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		setContentView(R.layout.advanced_main);
 
 		app = (MyApplication)AdvancedMainActivity.this.getApplication();
 		songVolume = 0;
 
 		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
 		.detectDiskReads().detectDiskWrites().detectNetwork()
 		.penaltyLog().build());
 
 		Intent intent = getIntent();
 		rawPlaylist = intent.getStringArrayExtra("rawPlaylist");
 		songVolume = intent.getIntExtra("volume", 4);
 		currentSongPositionInTime = intent.getIntExtra("progress", 0);
 		if (mainPlaylist == null) initializeList(rawPlaylist);
 		overridePendingTransition(R.anim.slide_upward, R.anim.slide_upward);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		
 		app.new SocketSend().execute( "W" );
 		
 		/* Testing */
 		TCPClearTask tcpClearTask = new TCPClearTask();
 		Timer tcp_timer2 = new Timer();
 		tcp_timer2.schedule( tcpClearTask, 0 );
 		
 		tcp_task = new TCPReadTimerTask();
 		Timer tcp_timer = new Timer();
 		tcp_timer.schedule(tcp_task, 3000, 200);
 
 		getIndex();
 	}
 
 	@Override
 	public void onPause()
 	{
 		tcp_task.cancel();
 		super.onPause();
 
 	}
 
 	public void openDragDropPlaylist(View view)
 	{
 		Intent intent = new Intent(this, DragDropPlaylist.class);
 		intent.putExtra("rawPlaylist", rawPlaylist);
 
 		startActivityForResult(intent, dragDrop);
 	}
 
 	@Override 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) 
 	{   
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (requestCode == dragDrop && resultCode == Activity.RESULT_OK)
 		{
 			Toast.makeText(this, "backFromDrag", Toast.LENGTH_SHORT).show();
 			String name = data.getStringExtra("playlistName");
 			String[] result = loadList(name);
 			if (result != null)
 			{
 				Toast.makeText(this, "playlist test loaded", Toast.LENGTH_SHORT).show();
 				for (int i =0; i < result.length; i++)
 					Log.i("load",result[i]);
 				//initializeListViewFromDragDrop(result);
 				currentSongPositionInTime = 0;
 				TextView text = (TextView) findViewById(R.id.viewText2);
 				text.setText( "Stopped" );
 			}
 		}
 		else if (resultCode == Activity.RESULT_CANCELED)
 			Toast.makeText(this, "action cancelled", Toast.LENGTH_SHORT).show();
 	}
 
 	public void initializeList(String[] playlist)
 	{
 
 		Log.i("AdvancedMain", "SimpleMain - initializing Song List");
 		mainPlaylist = new ArrayList<String[]>();
 
 		for (int i = 0; i + 5 <= playlist.length; i += 5) 
 		{
 			String[] newSong = new String[5];
 			newSong[0] = playlist[i];
 			newSong[1] = playlist[i + 1];
 			newSong[2] = playlist[i + 2];
 			newSong[3] = playlist[i + 3];
 			newSong[4]	= playlist[i + 4];
 			mainPlaylist.add(newSong);
 		}
 
 		Log.i("AdvancedMain", "SimpleMain - Finished initializing Song List");
 		
 		loadPlaylistIntoListView(playlist);
 	}
 
 	public void playPauseSong(View view) {
 		app.new SocketSend().execute("P");
 	}
 
 	public void stopSong(View view) {
 		app.new SocketSend().execute("S");
 	}
 
 	public void nextSong(View view) {
 		app.new SocketSend().execute("N");
 	}
 
 	public void prevSong(View view) {
 		app.new SocketSend().execute("L");
 	}
 
 	public void upVolume(View view) {
 		app.new SocketSend().execute("U");
 	}
 
 	public void downVolume(View view) {
 		app.new SocketSend().execute("D");
 	}
 
 	/* Gets the current song index from DE2 */
 	public void getIndex() {
 		app.new SocketSend().execute("I");
 	}
 
 	/* Toggles mode from Order-Mode/Shuffle-Mode */
 	public void onClickPlayOrderMode( View view )
 	{
 		boolean isShuffle = ((ToggleButton) view).isChecked();
 
 		if ( isShuffle )
 			app.new SocketSend().execute("H");
 		else
 			app.new SocketSend().execute("h");
 	}
 
 	/* Toggles mode from Repeat-One-Song-Mode/Repeat-Whole-List-Mode */
 	public void onClickPlayRepeatMode( View view )
 	{
 		boolean isRepeatOneSong = ((ToggleButton) view).isChecked();
 
 		if ( isRepeatOneSong )
 			app.new SocketSend().execute("R");
 		else
 			app.new SocketSend().execute("r");
 	}
 
 	/* Sends a playList to DE2
 	 * returns 0 if successful, otherwise -1
 	 * Pre: playList != null
 	 */
 	public int sendCurrentPlayListToDE2( String[] playList )
 	{
 		MyApplication app = (MyApplication) getApplication();
 		InputStream in = null;
 		try 
 		{
 			in = app.sock.getInputStream();
 		} 
 		catch (IOException e1) 
 		{
 			Log.i( "Exception", "app.sock.getInputStream() failed in sendCurrentPlayListToDE2" );
 			return -1;
 		}
 
 		int listLength = playList.length;
 		Log.i( "list", "Start sending playList" );
 		//new SocketSend().execute( Integer.toString( ( Integer.toString( listLength ) ).length() ) );
 		//Log.i( "list", "Sending: " +  Integer.toString( ( Integer.toString( listLength ) ).length() ) );
 		app.new SocketSend().execute( Integer.toString( listLength ) );
 		Log.i( "list", "Sending: " +  Integer.toString( listLength ) );
 
 		for ( int i = 0; i < listLength; i++ )
 		{
 			if ( i != 0 && i % 30 == 0 )	// for HandShake
 			{
 				try 
 				{
 					byte buf[] = new byte[ONE_BYTE];
 
 					app.new SocketSend().execute( "H" );
 					Log.i( "list", "Sending H" );
 
 
 					/* Android loopback mode purpose */
 					/*
 					String msg = new String();
 					while ( msg.compareTo( "H" ) != 0 )
 					{
 						if ( in.available() > 0 )
 						{
 							in.read( buf );
 							msg = new String(buf, 0, ONE_BYTE, "US-ASCII");
 						}
 					}
 					 */
 
 					/* Real Purpose */
 					while ( in.available() == 0 );
 					Log.i( "list", "Got message from DE2" );
 
 					in.read( buf );
 					String msg = new String(buf, 0, ONE_BYTE, "US-ASCII");
 
 					Log.i( "list", "Message got is: " + msg );
 
 					if ( msg.compareTo( "H" ) != 0 )
 					{
 						Log.i( "list", "Invalid message came from DE2 in sendCurrentPlayListToDE2" );
 						return -1;
 					}
 					Log.i( "list", "valid message came from DE2 in sendCurrentPlayListToDE2" );
 				} 
 				catch (IOException e) 
 				{
 					Log.i( "Exception", "IOException failed in sendCurrentPlayListToDE2" );
 					return -1;
 				}		
 			}
 
 			//new SocketSend().execute( Integer.toString( playList[i].length() ) );
 			//Log.i( "list", "Sending: " +  Integer.toString( playList[i].length() ) );
 			app.new SocketSend().execute( playList[i] );
 			Log.i( "list", "Sending: " +  playList[i] );
 		}
 		Log.i( "list", "Done sending playList" );
 		return 0;
 	}
 
 	/* Wrapper function for storing rating list */
 	public void saveRating( String[] rating )
 	{
 		saveList( "Rating.txt", rating );
 	}
 
 	/* Wrapper function for loading rating list */
 	public String[] loadRating()
 	{
 		return loadList( "Rating.txt" );
 	}
 
 	/* Wrapper function for saving play list */
 	public void savePlayList( String name, String[] str )
 	{
 		saveList( name, str );
 	}
 
 	/* Wrapper function for loading play list */
 	public String[] loadPlayList( String name )
 	{
 		return loadList( name );
 	}
 
 	/* Stores a list of string into internal storage as a file
 	 * @name is the name of the file
 	 * @str is the list of strings to store
 	 * Pre: Str != null
 	 */
 	public void saveList( String name, String[] str )
 	{
 		FileOutputStream fos = null;
 
 		try 
 		{
 			fos = openFileOutput( name, Context.MODE_PRIVATE );
 
 			for ( int i = 0; i < str.length; i++ )
 			{
 				fos.write( str[i].getBytes() );
 				fos.write( ".".getBytes() );
 			}
 		} 
 		catch (FileNotFoundException e) 
 		{
 			Log.i( "Exception", "File: " + name + " is not found." );
 		} 
 		catch (IOException e) 
 		{
 			Log.i( "Exception", "str.getBytes() threw an IOException for file: " + name + "." );
 		}
 		finally
 		{
 			try 
 			{
 				fos.close();
 			} 
 			catch (IOException e) 
 			{
 				Log.i( "Exception", "Failed close file: " + name + "." );
 			}
 		}
 	}
 
 	/* Loads a list of string from the internal storage
 	 * @name is the name of the file
 	 */
 	public String[] loadList( String name )
 	{
 
 		FileInputStream fis = null;
 		try 
 		{
 			fis = openFileInput( name );
 
 			byte[] buf = new byte[MAX_BYTES];
 			String temp = new String();
 			int i;
 
 			int bytesRead;
 			while ( (bytesRead = fis.read( buf )) != -1 )
 			{
 				Log.i( "playList", "bytesRead is: " + bytesRead );
 				temp = temp.concat( new String( buf, 0, bytesRead, "US-ASCII" ));			
 			}
 			Log.i( "playList", "bytesRead is: " + bytesRead );
 
 			String[] str = temp.split("\\.");
 
 			for ( i = 0; i < str.length; i++)
 				Log.i( "ss", "str[" + i + " ]: " + str[i] );
 
 			return str;
 		} 
 		catch (FileNotFoundException e) 
 		{
 			Log.i( "Exception", "File: " + name + " is not found." );
 		} 
 		catch (IOException e) 
 		{
 			Log.i( "Exception", "fis.read() threw an IOException for file: " + name + "." );
 		}
 		finally
 		{
 			try 
 			{
 				fis.close();
 			} 
 			catch (IOException e) 
 			{
 				Log.i( "Exception", "Failed close file: " + name + "." );
 			}
 		}
 
 		return null;
 	}
 
 	public void updateProgressBar()
 	{
 		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
 		int songLength = Integer.parseInt( mainPlaylist.get(songIndex)[4] );
 		int currentProgress = (int) ( ((double) currentSongPositionInTime) / (double) songLength * 100.0 );
 		pb.setProgress( currentProgress );
 	}
 
 	/* Initializes the max time of the current song */
 	public void setupTime( int songLength )
 	{
 		TextView MaxTimeMin = (TextView) findViewById( R.id.textView5 );
 		TextView MaxTimeSec = (TextView) findViewById( R.id.textView7 );
 
 		int maxMin = songLength / 60;
 		int maxSec = songLength % 60;
 
 		String maxSecStr = Integer.toString( maxSec );
 		if ( maxSec < 10 )
 			maxSecStr = "0" + maxSecStr;
 
 		MaxTimeMin.setText( Integer.toString( maxMin ) );
 		MaxTimeSec.setText( maxSecStr );
 		
 		updateTime( 0, songLength );
 	}
 
 	/* Updates the current time of the current song */
 	public void updateTime( int currentSongPositionInTime, int songLength )
 	{
 		TextView currTimeMin = (TextView) findViewById( R.id.textView1 );
 		TextView currTimeSec = (TextView) findViewById( R.id.textView3 );
 
 		if ( currentSongPositionInTime > songLength )
 			return;
 
 		int currMin = currentSongPositionInTime / 60;
 		int currSec = currentSongPositionInTime % 60;
 
 		String currSecStr = Integer.toString( currSec );
 		if ( currSec < 10 )
 			currSecStr = "0" + currSecStr;
 
 		currTimeMin.setText( Integer.toString( currMin ) );
 		currTimeSec.setText( currSecStr );
 	}
 
 	public void openPiano(View view)
 	{
 		Intent intent = new Intent(this, Piano.class);
 		//intent.putExtra("rawPlaylist", rawPlaylist);
 
 		startActivity(intent);
 	}
 
 	public void initializeListViewFromDragDrop(String[] result)
 	{
 		ArrayList<String[]> newPlaylist = new ArrayList<String[]>();
 		String[] newRawPlaylist = new String [result.length * 5];
 		int iterator = 0;
 		
 		for (int i = 0; i < result.length; i++)
 		{
 			for (int j = 0; j < mainPlaylist.size(); j++)
 			{
 				if (mainPlaylist.get(j)[0].compareTo(result[i])==0)
 				{
 					newRawPlaylist[iterator] = mainPlaylist.get(j)[0];
 					newRawPlaylist[iterator+1] = mainPlaylist.get(j)[1];
 					newRawPlaylist[iterator+2] = mainPlaylist.get(j)[2];
 					newRawPlaylist[iterator+3] = mainPlaylist.get(j)[3];
 					newRawPlaylist[iterator+4] = mainPlaylist.get(j)[4];
 					iterator+=5;
 				}
 			}
 		}
 		
 		loadPlaylistIntoListView(newRawPlaylist);
 	}
 	
 	public void loadPlaylistIntoListView(String[] playlist)
 	{
 		list = new ArrayList<HashMap<String, String>>();
 		int songCount = playlist.length / 5;
 		String[] mSongs = new String[songCount];
 		String[] mArtists = new String[songCount];
 		String[] mRatings = new String[songCount];
 		String[] mId = new String[songCount];
 		String[] mLengths = new String[songCount];
 		
 		int currentIndex = 0;
 		
		for (int iterator = 0; iterator + 5 <= playlist.length; iterator += 5) 
 		{
 			mId[currentIndex] = playlist[iterator];
 			mSongs[currentIndex] = playlist[iterator + 1];
 			mArtists[currentIndex] = playlist[iterator + 2];
 			mRatings[currentIndex] = playlist[iterator + 3];
 			mLengths[currentIndex]	= playlist[iterator + 4];
 			currentIndex++;
 		}
 		
 		for(int i=0; i<mSongs.length; i++)
 		{
 			HashMap<String,String> item = new HashMap<String,String>();
 			item.put( "Song", mSongs[i]);
 			item.put( "Artist",mArtists[i] );
 			item.put("Rating", mRatings[i]);
 			list.add( item );
 		}
 		
 		SimpleAdapter adapter = new SimpleAdapter( this, list, R.layout.mylistview1, new String[] { "Song","Artist","Rating" },
 				new int[] { R.id.textView1, R.id.textView2, R.id.rating1 });
 		adapter.setViewBinder(new MyBinder());
 		
 		ListView listView = (ListView) findViewById(R.id.listView);
 		listView.setAdapter(adapter);
 		
 	}
 
 	class MyBinder implements ViewBinder
 	{
 
 		@Override
 		public boolean setViewValue(View arg0, Object arg1, String arg2) {
 			if (arg0 instanceof RatingBar)
 			{
 				//String stringval = (String) arg1;
				Log.i("arg1", (String) arg1);
 				float i = Float.parseFloat((String)arg1);
				
 				RatingBar bar = (RatingBar) arg0;
 				bar.setRating(i);
 				return true;
 			}
 			return false;
 		}
 	}
 	
 	
 	public class TCPClearTask extends TimerTask 
 	{		
 		public void run() 
 		{
 			MyApplication app = (MyApplication) getApplication();
 			if (app.sock != null && app.sock.isConnected() && !app.sock.isClosed()) 
 			{
 				try 
 				{
 					InputStream in = app.sock.getInputStream();
 					int bytes_avail = in.available();
 
 					while ( true )
 					{
 						if (bytes_avail > 0) 
 						{						
 							Log.i( "Clear", Integer.toString( bytes_avail ) );
 							
 							byte buf[] = new byte[ONE_BYTE];
 							in.read(buf);
 							String msg = new String(buf, 0, ONE_BYTE, "US-ASCII");
 							
 							Log.i( "Clear", msg );
 							
 							if ( msg.compareTo( "W" ) == 0 )
 								break;
 						}
 					}
 				} 
 				catch (IOException e) 
 				{
 					Log.i( "Exception", "TCPClearTask Failed." );
 				}
 			}
 		}
 	}
 }
