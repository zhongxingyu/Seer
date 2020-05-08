 package com.example.digplay;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 public class GameplanManagerActivity extends Activity implements OnItemClickListener, OnClickListener {
 	private ListView gameplanLV;
 	private ListView playbookLV;
 	private ArrayList<String> playbookPlays;
 	private ArrayList<String> gameplanPlays;
 	private Button delete;
 	private boolean deletePressed;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.gameplan_manager);
 	    playbookPlays = new ArrayList<String>();
 	    gameplanPlays = new ArrayList<String>();
 	    playbookPlays.add("play 1");
 	    playbookPlays.add("play 2");
 	    playbookPlays.add("play 3");
 	    setListViews();
 	    delete = (Button) findViewById(R.id.gm_delete);
 	    delete.setOnClickListener(this);
 	    deletePressed = false;
 	}
 	private void setListViews() {
 		gameplanLV = (ListView) findViewById(R.id.gm_gameplan);
 		playbookLV = (ListView) findViewById(R.id.gm_playbook);
 		ArrayAdapter<String> playbookAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,playbookPlays);  
 		ArrayAdapter<String> gameplanAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,gameplanPlays); 
 		playbookLV.setAdapter(playbookAdapter);
 		gameplanLV.setAdapter(gameplanAdapter);
 		playbookLV.setOnItemClickListener(this);
 		gameplanLV.setOnItemClickListener(this);
 		playbookLV.setBackgroundColor(Color.LTGRAY);
 		gameplanLV.setBackgroundColor(Color.LTGRAY);
 	}
 	
 	public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
 		if(!deletePressed){
 			String playSelected = (String) adapter.getItemAtPosition(position);
			gameplanPlays.add(playSelected);
 			ArrayAdapter<String> thisAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,gameplanPlays);
 			gameplanLV.setAdapter(thisAdapter);
		}else{
 			gameplanPlays.remove(position);
 			ArrayAdapter<String> thisAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,gameplanPlays);
 			gameplanLV.setAdapter(thisAdapter);
 		}
 	}
 	public void onClick(View v) {
 		if(!deletePressed){
 			delete.setBackgroundColor(Color.RED);
 			deletePressed = true;
 		}else{
 			delete.setBackgroundColor(Color.LTGRAY);
 			deletePressed = false;
 		}
 	}
 }
