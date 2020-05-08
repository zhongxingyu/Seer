 package fr.umlv.escape;
 
 import fr.umlv.escape.editor.LevelAdapter;
 import fr.umlv.escape.game.Level;
 import fr.umlv.escape.game.Wave;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 
public class EditorActivity extends Activity implements OnItemClickListener{
 	private int state = 0;
 	private String backgroundName;
 	private Level level;
 	LevelAdapter levelAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editor_map);
 		this.backgroundName = "level1";
 		this.level = new Level("edited_level");
 		level.addWaveList(new Wave("empty_wave"));
 		level.addDelayList(0);
 	}
 
 	public void onPushButton(View v) {
 		switch(state){
 		case 0 : performState0(v); break;
 		case 1 : performState1(v); break;
 		case 2 : performState2(v); break;
 		case 3 : performState3(v); break;
 		}
 	}
 	
 	public void performState0(View v) {
 		ImageView map = (ImageView)findViewById(R.id.mapImageEditor);
 		switch(v.getId())
 		{
 			case R.id.map1Button : {
 			map.setImageResource(R.drawable.level1);
 			this.backgroundName = "level1";
 			break;
 			}
 			case R.id.map2Button : {
 			map.setImageResource(R.drawable.level2);
 			this.backgroundName = "level2";
 			break;
 			}
 			case R.id.map3Button : {
 			map.setImageResource(R.drawable.level3);
 			this.backgroundName = "level3";
 			break;
 			}
 			case R.id.nextButton : {
 			state= 1;
 			levelAdapter=new LevelAdapter(this,level);
 			setContentView(R.layout.editor_waves);
 			ListView list = (ListView) findViewById(R.id.waves_list_editor);
 			list.setAdapter(levelAdapter);
			list.setOnItemClickListener(this);
 			break;
 			}
 		}
 		v.invalidate();
 	}
 	
 	public void performState1(View v) {
 	    switch(v.getId())
 		{
 			case R.id.backButton : {
 				state = 0;
 				setContentView(R.layout.editor_map);
 				break;
 			}
 			case R.id.plus : {
 				System.out.println("addedddd : "+level.getWaveList().size());
 				level.getWaveList().add(new Wave("empty_wave"));
 				level.getDelayWaveList().add((long) 0);
 				levelAdapter.notifyDataSetChanged();
 				System.out.println("addedddd : "+level.getWaveList().size());
				break;
 			}
 			case R.id.moins : {
 				if(level.getWaveList().size()>1){					
 					level.getWaveList().remove(level.getWaveList().size()-1);
 					level.getDelayWaveList().remove(level.getWaveList().size()-1);
 				}
 				levelAdapter.notifyDataSetChanged();
				break;
 			}
 		}
 		v.invalidate();
 	}
 
 	public void performState2(View v) {
 	
 	}
 
 	public void performState3(View v) {
 	
 	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		System.out.println(arg0.getItemAtPosition(arg2));
	}
 }
