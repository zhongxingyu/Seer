 package fr.umlv.escape;
 
 import fr.umlv.escape.editor.EditorLevelActivity;
import fr.umlv.escape.editor.EditorMapActivity;
 import fr.umlv.escape.editor.EditorWaveActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 
 public class EditorActivity extends Activity implements OnClickListener{
 //	private int state = 0;
 //	private String backgroundName;
 //	private Bitmap background;
 //	private Level level;
 //	LevelAdapter levelAdapter;
 //	ArrayAdapter<String> arrayAdapter;
 	//private FrontBuilder frontBuilder;
 
 	ImageView map;
 	int HEIGHT;
 	int WIDTH;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.choose_builder);
 		
 		final Button button_wave_builder = (Button) findViewById(R.id.button_wave_builder);
 		final Button button_level_builder = (Button) findViewById(R.id.button_level_builder);
 		button_wave_builder.setOnClickListener(this);
 		button_level_builder.setOnClickListener(this);
 		
 		/*button_wave_builder.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplicationContext(), EditorWaveActivity.class);
 				startActivity(intent);
 			}
 		});
 		
 		button_level_builder.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getBaseContext(), EditorLevelActivity.class);
 				startActivity(intent);
 
 			}
 		});*/
 		
 //		this.backgroundName = "level1";
 //		this.level = new Level("edited_level");
 //		level.addWaveList(new Wave("empty_wave"));
 //		level.addDelayList(0);
 //		WIDTH = display.getWidth();  // deprecated
 //		HEIGHT = display.getHeight();
 		//this.frontBuilder = new FrontBuilder(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent intent;
 		switch (v.getId()) {
 		case R.id.button_wave_builder:
 			System.out.println("WAVE BUTTON");
 			 intent = new Intent(this, EditorWaveActivity.class);
 			startActivity(intent);
 			break;
 		case R.id.button_level_builder:
 			System.out.println("LEVEL BUTTON");
			intent = new Intent(this, EditorMapActivity.class);
 			startActivity(intent);
 			break;
 		default:
 			break;
 		}
 	}
 
 //	public void onPushButton(View v) {
 //		switch(state){
 //		case 0 : performState0(v); break;
 //		case 1 : performState1(v); break;
 //		}
 //	}
 //	
 //	public void performState0(View v) {
 //		map = (ImageView)findViewById(R.id.mapImageEditor);
 //
 //		switch(v.getId())
 //		{
 //		case R.id.map1Button : {
 //			background = BitmapFactory.decodeResource(this.getResources(), R.drawable.level1);
 //			map.setImageBitmap(background);
 //			this.backgroundName = "level1";
 //			break;
 //		}
 //		case R.id.map2Button : {
 //			background = BitmapFactory.decodeResource(this.getResources(), R.drawable.level2);
 //			map.setImageBitmap(background);
 //			this.backgroundName = "level2";
 //			break;
 //		}
 //		case R.id.map3Button : {
 //			background = BitmapFactory.decodeResource(this.getResources(), R.drawable.level3);
 //			map.setImageBitmap(background);
 //			this.backgroundName = "level3";
 //			break;
 //		}
 //		case R.id.nextButton : {
 //			if(background==null){
 //				return;
 //			}
 //			state= 1;
 //			levelAdapter = new LevelAdapter(this,level);
 //			setContentView(R.layout.editor_waves);
 //			ListView list = (ListView) findViewById(R.id.waves_list_editor);
 //			list.setAdapter(levelAdapter);
 //			list.setOnItemClickListener(this);
 //			break;
 //		}
 //		}
 //		v.invalidate();
 //	}
 //	
 //	public void performState1(View v) {
 //	    switch(v.getId())
 //		{
 //			case R.id.backButton : {
 //				state = 0;
 //				setContentView(R.layout.editor_map);
 //				break;
 //			}
 //			case R.id.plus : {
 //				level.getWaveList().add(new Wave("empty_wave"));
 //				level.getDelayWaveList().add((long) 0);
 //				levelAdapter.notifyDataSetChanged();
 //				break;
 //			}
 //			case R.id.moins : {
 //				if(level.getWaveList().size()>1){					
 //					level.getWaveList().remove(level.getWaveList().size()-1);
 //					level.getDelayWaveList().remove(level.getWaveList().size()-1);
 //				}
 //				levelAdapter.notifyDataSetChanged();
 //				break;
 //			}
 //		}
 //		v.invalidate();
 //	}
 //	
 //	public void performState2(View v) {
 //		v.invalidate();
 //	}
 //
 //	@Override
 //	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 //		//setContentView(frontBuilder);
 //		//frontBuilder.buildWave(background,(Wave)arg0.getItemAtPosition(arg2));
 //		setContentView(R.layout.builder_wave);
 //		performState2(findViewById(R.layout.builder_wave));
 //	}
 
 }
