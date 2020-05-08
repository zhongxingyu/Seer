 package edu.berkeley.cs160.smartnature;
 
 import java.util.Date;
 import java.util.ArrayList;
 
 import edu.berkeley.cs160.smartnature.PlotScreen.PlantAdapter;
 import edu.berkeley.cs160.smartnature.StartScreen.GardenAdapter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.ScaleAnimation;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ImageView;
 import android.widget.RadioButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.ZoomControls;
 
 public class PlantScreen extends ListActivity implements View.OnTouchListener, View.OnClickListener, AdapterView.OnItemClickListener {
 	
 	AlertDialog dialog;
 
 	static EntryAdapter adapter;
 
 	int gardenID, plotID, plantID; 
 	
 	LinearLayout entries;
 	TextView dateText;
 	TextView text;
 	
 
 	String name; 
 	EditText entryText;
 	ImageView addImage;
 	TextView plantTextView;
 	Button addEntryButton, backButton;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 
 		super.onCreate(savedInstanceState);
 
 
 		super.onCreate(savedInstanceState);
 
 		entries = (LinearLayout) findViewById(R.id.entryTextLayout);
 		
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null && extras.containsKey("name")) {
 			name = extras.getString("name"); 
 			gardenID = extras.getInt("gardenID");
 			plotID = extras.getInt("plotID");
 			plantID = extras.getInt("plantID");
 			setTitle(name);
 		} else {
 			showDialog(0);
 		}
 		
 		setContentView(R.layout.plant);
 		initMockData();
 		getListView().setOnItemClickListener(PlantScreen.this);
 		
 		entryText = (EditText) findViewById(R.id.entryText);
		addImage = (ImageView) findViewById(R.id.addImage);
 		addEntryButton = (Button) findViewById(R.id.addEntryButton);
 		plantTextView = (TextView) findViewById(R.id.plantTextView);
 		plantTextView.setText(name);
 
 		
 		addImage.setOnClickListener(new OnClickListener() {
 			@Override
       public void onClick(View v) {
 				//TODO
 				// Call Deepti's Picture dialog
       }
     });
 
 
 		
 
 		//Button addPicButton = (Button) findViewById(R.id.addPicButton);
 		Button addEntryButton = (Button) findViewById(R.id.addEntryButton);
 
 		
 
 
 		addEntryButton.setOnClickListener(new OnClickListener() {
 			@Override
       public void onClick(View v) {
 
 				//TODO
 				// Call Deepti's entry dialog
 
 				
 				StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants().get(plantID).addEntry(entryText.getText().toString());
 				adapter.notifyDataSetChanged(); //refresh ListView
       }
 
 
     });
     
 	}
 	
 	/*
 	public void loadEntries(){
 		for (Entry e: StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants().get(plantID).getEntries()){
 			
 			text = new TextView(PlantScreen.this);
 			text.setTextColor(0xFF000000); //black
 			text.setText(e.getName());
 			
 			dateText = new TextView(PlantScreen.this);
 			dateText.setTextColor(0xFFCCCCCC); //black
 			dateText.setText(e.getDate());
 			
 			entries.addView(dateText);
 			entries.addView(text);
 		}
 	}
 	*/
 	
 	public void initMockData() {
 
 		//mockPlant.addEntry("Entry 1");
 		//mockPlant.addEntry("Entry 2");
 		adapter = new EntryAdapter(this, R.layout.list_item, StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants().get(plantID).getEntries() );
 		setListAdapter(adapter);
 	}
 
 
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.garden_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.m_home:
 				finish();
 				break;
 			case R.id.m_resetzoom:
 				break;
 			case R.id.m_share:
 				startActivity(new Intent(this, ShareGarden.class));
 				break;
 			case R.id.m_showlabels:		
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		System.out.println("touched");
 		return false;
 	}
 	
 	@Override
 	public void onClick(View view) {
 		System.out.println("clicked");
 	}
 
 
 	class EntryAdapter extends ArrayAdapter<String> {
 
 		private ArrayList<String> items;
 		private LayoutInflater li;
 		
 		public EntryAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
 			super(context, textViewResourceId, items);
 			li = ((ListActivity) context).getLayoutInflater();
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null)
 				v = li.inflate(R.layout.plant_list, null);
 			String s = items.get(position);
 			((TextView) v.findViewById(R.id.plant_name)).setText(s);
 			return v;
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 	}
 	
 	
 }
