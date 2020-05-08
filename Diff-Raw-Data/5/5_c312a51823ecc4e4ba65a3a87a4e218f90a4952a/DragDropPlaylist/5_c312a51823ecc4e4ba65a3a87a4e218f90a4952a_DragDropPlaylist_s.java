 package com.example.stereoplayer;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.ClipData;
 import android.content.ClipDescription;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.View;
 import android.view.View.DragShadowBuilder;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class DragDropPlaylist extends Activity {
 
 	private ArrayList<HashMap<String, String>> allSonglist;	// input from the caller
 	private ArrayList<HashMap<String, String>> newSongList;	// send this to middle man
 	private List<String> droppedList;	// a list for displaying songs in newSongList
 	
 	private ListView listSource;
 	private ListView listTarget;
 	private LinearLayout targetLayout;
 	private ArrayAdapter<String> targetAdapter;
 	
 	private EditText nameEdit;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dragdropplaylist);
 		listSource = (ListView) findViewById(R.id.listView1);
 		listTarget = (ListView) findViewById(R.id.listView2);
 		targetLayout = (LinearLayout)findViewById(R.id.targetlayout);
 		nameEdit = (EditText)findViewById(R.id.editText1);
 		
 		// get raw list
 		Intent intent = getIntent();
 		String[] rawPlaylist = intent.getStringArrayExtra("rawPlaylist");
 		allSonglist = getListFromIntent( rawPlaylist );
 		newSongList = new ArrayList<HashMap<String, String>>();
 
 		listSource.setTag("listSource");
 		listTarget.setTag("listTarget");
 		targetLayout.setTag("targetLayout");
 		
 		listSource.setAdapter(new SimpleAdapter( this, allSonglist, R.layout.dragdropsourcelistview, new String[] { "Song","Artist" },
 				new int[] { R.id.textView1, R.id.textView2}));
 		
 		listSource.setOnItemLongClickListener(new sourceListItemLongClickListener());
 		listTarget.setOnItemLongClickListener(new targetListItemLongClickListener());
 		
 		droppedList = new ArrayList<String>();			
 		targetAdapter = new ArrayAdapter<String>(this,
 	              android.R.layout.simple_list_item_1, droppedList);
 		
 		listTarget.setAdapter(targetAdapter);
 
 		listSource.setOnDragListener(new MyDragEventListener());
 		targetLayout.setOnDragListener(new MyDragEventListener());
 
 	}
 	
 	/**
 	 * send the created list
 	 */
 	public void sendList (View view) {
 		if(!newSongList.isEmpty()) {
 			String[] list = new String [newSongList.size()];
 			for(int i = 0; i < list.length; i++)
 				list[i] = newSongList.get(i).get("ID");
 			sendCurrentPlayListToDE2(list);
 			Intent resultIntent = new Intent();
 			//resultIntent.putExtra("volume", volume);
 			//resultIntent.putExtra("FromLoading", playlist);
 			setResult(Activity.RESULT_OK, resultIntent);
 			//tcp_timer.cancel();
 			finish();
 		}
 	}
 	
 	/**
 	 * save the list on android
 	 */
 	public void saveNewList (View view) {
		MyApplication app = (MyApplication) DragDropPlaylist.this.getApplication();
		app.new SocketSend().execute("Z");
 		String name = nameEdit.getEditableText().toString();
 		if(name.compareTo("") == 0)
 			name = "newlist1";
 		String[] list = new String [newSongList.size()];
 		for(int i = 0; i < list.length; i++)
 			list[i] = newSongList.get(i).get("ID");
 		saveList( name, list );
 	}
 	
 	public void loadListFromFile (View view) {
 		String name = nameEdit.getEditableText().toString();
 		if(name.compareTo("") == 0)
 			return;
 		String[] list = loadList(name);
 		if(list == null)
 			return;
 		while(!newSongList.isEmpty())
 			newSongList.remove(0);
 		while(!droppedList.isEmpty())
 			droppedList.remove(0);
 		for(int i = 0; i < list.length; i++) {
 			boolean found = false;
 			for(int position = 0; position < allSonglist.size() && !found; position++) {
 				if(allSonglist.get(position).get("ID").compareTo(list[i]) == 0) {
 					HashMap<String, String> song = new HashMap<String, String>();
 					song.put("ID", allSonglist.get(position).get("ID"));
 					song.put("Song", allSonglist.get(position).get("Song"));
 					song.put("Artist", allSonglist.get(position).get("Artist"));
 					newSongList.add(song);
 					droppedList.add(allSonglist.get(position).get("Song") + "\n" + allSonglist.get(position).get("Artist"));	//here
 					found = true;
 				}
 			}
 		}
 		targetAdapter.notifyDataSetChanged();
 	}
 	
 	private static class MyDragShadowBuilder extends View.DragShadowBuilder {
 		private static Drawable shadow;
 
 		public MyDragShadowBuilder(View v) {
 			super(v);
 			shadow = new ColorDrawable(Color.LTGRAY);
 		}
 
 		@Override
 		public void onProvideShadowMetrics (Point size, Point touch){
 			int width = getView().getWidth();
 			int height = getView().getHeight();
 
 			shadow.setBounds(0, 0, width, height);
 			size.set(width, height);
 			touch.set(width / 2, height / 2);
 		}
 
 		@Override
 		public void onDrawShadow(Canvas canvas) {
 			shadow.draw(canvas);
 		}
 
 	}
 
 	class sourceListItemLongClickListener implements OnItemLongClickListener {
 		@Override
 		public boolean onItemLongClick(AdapterView<?> l, View v,
 				int position, long id) {
 			Log.i("drag","sourceListItemLongClickListener");
 			Log.i("drag", allSonglist.get(position).toString());
 			//Selected item is passed as item in dragData
 			ClipData.Item item = new ClipData.Item(allSonglist.get(position).toString());
 
 			String[] clipDescription = {ClipDescription.MIMETYPE_TEXT_PLAIN};
 			ClipData dragData = new ClipData((CharSequence)v.getTag(),
 					clipDescription,
 					item);
 			DragShadowBuilder myShadow = new MyDragShadowBuilder(v);
 
 			v.startDrag(dragData, //ClipData
 					myShadow,  //View.DragShadowBuilder
 					position,  //Object myLocalState
 					0);    //flags
 			return true;
 		}
 	}
 	
 	class targetListItemLongClickListener implements OnItemLongClickListener {
 		@Override
 		public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id) {		
 			droppedList.remove(position);
 			newSongList.remove(position);
 			targetAdapter.notifyDataSetChanged();
 			
 			Log.i("drag", "size of new list: " + newSongList.size());
 			Log.i("drag", newSongList.toString());
 			return true;
 		}
 	}
 
 	protected class MyDragEventListener implements View.OnDragListener {
 		@Override
 		public boolean onDrag(View v, DragEvent event) {
 			final int action = event.getAction();
 			String commentMsg;
 			switch(action) {
 			case DragEvent.ACTION_DRAG_STARTED:
 				//All involved view accept ACTION_DRAG_STARTED for MIMETYPE_TEXT_PLAIN
 				if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
 					return true; //Accept
 				} else {
 					return false; //reject
 				}
 			case DragEvent.ACTION_DRAG_ENTERED:
 				return true;
 			case DragEvent.ACTION_DRAG_LOCATION:
 				return true;
 			case DragEvent.ACTION_DRAG_EXITED:
 				return true;
 			case DragEvent.ACTION_DROP:
 				//If apply only if drop on buttonTarget
 				if(v == targetLayout) {
 					int position = (Integer) event.getLocalState();
 					
 					HashMap<String, String> song = new HashMap<String, String>();
 					song.put("ID", allSonglist.get(position).get("ID"));
 					song.put("Song", allSonglist.get(position).get("Song"));
 					song.put("Artist", allSonglist.get(position).get("Artist"));
 					newSongList.add(song);
 					
 					String tmp = song.get("Song") + "\n" + song.get("Artist");
 					droppedList.add(tmp);
 					targetAdapter.notifyDataSetChanged();
 					
 					commentMsg = "Dropped item";
 					Log.i("drag", commentMsg);
 					return true;
 				} else {
 					return false;
 				}
 			case DragEvent.ACTION_DRAG_ENDED:
 				if (event.getResult()){
 					commentMsg = v.getTag() + " : ACTION_DRAG_ENDED - success." + " size of new list: " + newSongList.size();
 					Log.i("drag", commentMsg);
 					Log.i("drag", newSongList.toString());
 
 				} else {
 					commentMsg = v.getTag() + " : ACTION_DRAG_ENDED - fail.";
 					Log.i("drag", commentMsg);
 				}
 				return true;
 			default: //unknown case
 				commentMsg = v.getTag() + " : UNKNOWN !!!";
 				Log.i("drag", commentMsg);
 				return false;
 			}
 		} 
 	}
 	
 	private ArrayList<HashMap<String, String>> getListFromIntent(String[] raw) {
 		ArrayList<HashMap<String, String>> lis = new ArrayList<HashMap<String, String>>();
 		for (int i = 0; i + 5 <= raw.length; i += 5) 
 		{
 			HashMap<String,String> item = new HashMap<String,String>(); 
 			item.put( "ID", raw[i]);
 			item.put( "Song", raw[i + 1]);
 			item.put( "Artist", raw[i + 2]);
 			lis.add( item );
 		}
 		return lis;
 	}
 	
 	private void saveList( String name, String[] str ) {
 		FileOutputStream fos = null;
 		
 		try {
 			fos = openFileOutput( name, Context.MODE_PRIVATE );
 			
 			for ( int i = 0; i < str.length; i++ ) {
 				fos.write( str[i].getBytes() );
 				fos.write( ".".getBytes() );
 			}
 		} 
 		catch (FileNotFoundException e) {
 			Log.i( "Exception", "File: " + name + " is not found." );
 		} 
 		catch (IOException e) {
 			Log.i( "Exception", "str.getBytes() threw an IOException for file: " + name + "." );
 		}
 		finally {
 			try {
 				fos.close();
 			} 
 			catch (IOException e) {
 				Log.i( "Exception", "Failed close file: " + name + "." );
 			}
 		}
 	}
 	
 	/**
 	 * Sends a playList to DE2
 	 * Precondition: playList != null
 	 * @return 0 if successful, otherwise -1
 	 */
 	private int sendCurrentPlayListToDE2 ( String[] playList ) {
 		MyApplication app = (MyApplication) getApplication();
 		InputStream in = null;
 		try {
 			in = app.sock.getInputStream();
 		} 
 		catch (IOException e1) {
 			Log.i( "Exception", "app.sock.getInputStream() failed in sendCurrentPlayListToDE2" );
 			return -1;
 		}
 		
 		int listLength = playList.length;
 		Log.i( "list", "Start sending playList" );
 		//new SocketSend().execute( Integer.toString( ( Integer.toString( listLength ) ).length() ) );
 		//Log.i( "list", "Sending: " +  Integer.toString( ( Integer.toString( listLength ) ).length() ) );
 		app.new SocketSend().execute( Integer.toString( listLength ) );
 		Log.i( "list", "Sending: " +  Integer.toString( listLength ) );
 		
 		for ( int i = 0; i < listLength; i++ ) {
 			if ( i != 0 && i % 30 == 0 )	// for HandShake
 			{
 				try {
 					byte buf[] = new byte[AdvancedMainActivity.ONE_BYTE];
 										
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
 					String msg = new String(buf, 0, AdvancedMainActivity.ONE_BYTE, "US-ASCII");
 					
 					Log.i( "list", "Message got is: " + msg );
 					
 					if ( msg.compareTo( "H" ) != 0 ) {
 						Log.i( "list", "Invalid message came from DE2 in sendCurrentPlayListToDE2" );
 						return -1;
 					}
 					Log.i( "list", "valid message came from DE2 in sendCurrentPlayListToDE2" );
 				} 
 				catch (IOException e) {
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
 	
 	/** 
 	 * Loads a list of string from the internal storage
 	 * @param name	file name
 	 */
 	private String[] loadList( String name )	{
 		FileInputStream fis = null;
 		try {
 			fis = openFileInput( name );
 			
 			byte[] buf = new byte[AdvancedMainActivity.MAX_BYTES];
 			String temp = new String();
 			int i;
 			
 			int bytesRead;
 			while ( (bytesRead = fis.read( buf )) != -1 ) {
 				Log.i( "playList", "bytesRead is: " + bytesRead );
 				temp = temp.concat( new String( buf, 0, bytesRead, "US-ASCII" ));			
 			}
 			Log.i( "playList", "bytesRead is: " + bytesRead );
 			
 			String[] str = temp.split("\\.");
 			
 			for ( i = 0; i < str.length; i++)
 				Log.i( "ss", "str[" + i + " ]: " + str[i] );
 			
 			return str;
 		} 
 		catch (FileNotFoundException e) {
 			Log.i( "Exception", "File: " + name + " is not found." );
 		} 
 		catch (IOException e) {
 			Log.i( "Exception", "fis.read() threw an IOException for file: " + name + "." );
 		}
 		finally {
 			try {
 				fis.close();
 			} 
 			catch (IOException e) {
 				Log.i( "Exception", "Failed close file: " + name + "." );
 			}
 		}	
 		return null;
 	}
 } 
