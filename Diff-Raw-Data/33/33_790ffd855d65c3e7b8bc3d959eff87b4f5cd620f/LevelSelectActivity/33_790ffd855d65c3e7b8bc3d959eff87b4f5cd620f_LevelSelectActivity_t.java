 package com.android.mcameron.singletrack;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 
 public class LevelSelectActivity extends Activity {
 	GridView gridView;
 	
 	static final String[] numbers = new String[] { 
 			"1",  "2",  "3",  "4",  "5",
 			"6",  "7",  "8",  "9",  "10",
 			"11", "12", "13", "14", "15",
 			"16", "17", "18", "19", "20"};
 	
 	private String levelPack;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	
 		setContentView(R.layout.levelselect);
 		
 		// Get the level pack that is passed to the activity
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			levelPack = extras.getString("PACK_ID");
 		}
 		else {
 			levelPack = "01";
 		}
 	
 		gridView = (GridView) findViewById(R.id.grid);
 		gridView.setAdapter(new ButtonAdapter(this));	
 	}
 	
 	/**
 	 * An adapter for my button so that I can place it inside the custom GridView
 	 * 
 	 * @author markcameron
 	 */
 	public class ButtonAdapter extends BaseAdapter {
 		private Context mContext; 
 		
 		// Gets the context so it can be used later  
 		public ButtonAdapter(Context c) {  
 			mContext = c;  
 		}  
 		 
 		@Override
 		public int getCount() {
 			Levels levels = new Levels();
 			levels.setLevelPack(levelPack);
 			ArrayList<float[]> levelList = levels.getLevels();
 	        
 			return levelList.size() - 1;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			MenuButton btn;  
 			if (convertView == null) {  
 				// if it's not recycled, initialize some attributes  
 				btn = new MenuButton(mContext);  
 				// Set the font face because I cannot pass it to the Button constructor
 				btn.setTextSize(30f);
 				btn.setGravity(0x11);
 				btn.setTextColor(Color.WHITE);
 				btn.setTypeface(null, Typeface.BOLD);
 			}  
 			else {  
 				btn = (MenuButton) convertView;  
 			}  
 
 			Globals globals = (Globals) getApplicationContext();
 			AppPreferences appPrefs = new AppPreferences(mContext);
 //			Log.d("Counting", "current pack: "+ globals.getCurrentPack());
 			int levelState = appPrefs.getLevelState(globals.getCurrentPack(), String.format("%02d", position + 1));
 //			Log.d("Counting", Integer.toString(position) +" | "+ Integer.toString(levelState));
 			
			if (levelState == Globals.LEVEL_DISABLED && position < 2) {
				Log.d("Counting", "setState position: "+ position +" Level State: "+ levelState);
				appPrefs.setLevelState(globals.getCurrentPack(), String.format("%02d", position + 1), Globals.LEVEL_ENABLED);
 			}
			levelState = appPrefs.getLevelState(globals.getCurrentPack(), String.format("%02d", position + 1));
			Log.d("Counting", "setEnabled position: "+ position +" Level State: "+ levelState);
 			if (position > 1) {
 				btn.setEnabledFromLevelState(levelState);
 			}
 
 			btn.setText(numbers[position]);
 			// Set the button to use my custom black background
 			btn.setBackgroundResourceFromLevelState(levelState);
 			btn.setId(position);
 			
 		    // Set the onclicklistener so that pressing the button fires an event  
 		    // We will need to implement this onclicklistner.  
 		    btn.setOnClickListener(new MyOnClickListener(position));  
 	  
 			return btn;
 		}
 		
 		/**
 		 * OnClickListener for the button clicks inside the Custom GridView
 		 * 
 		 * @author markcameron
 		 */
 	    class MyOnClickListener implements OnClickListener {  
 	    	private final int position;  
   
 	    	public MyOnClickListener(int position) {  
 	    		this.position = position;  
 	    	}
   
 	    	@Override
 			public void onClick(View v) {  
 	    		// Open the level corresponding to the level selected in the GridView
 	    		Intent StartGameIntent = new Intent(LevelSelectActivity.this, Options.class);
 				StartGameIntent.putExtra("LEVEL_ID", numbers[position]);
 				Globals globals = (Globals) getApplicationContext();
 				globals.setCurrentLevel(numbers[position]);
         		startActivity(StartGameIntent);
 	    	}  
 	    }
 	}
 	
 	public void onRestart() {
 		super.onRestart();
 		Intent currentIntent = (Intent) getIntent();
 		finish();
 		startActivity(currentIntent);
 	}
 
 }// class
