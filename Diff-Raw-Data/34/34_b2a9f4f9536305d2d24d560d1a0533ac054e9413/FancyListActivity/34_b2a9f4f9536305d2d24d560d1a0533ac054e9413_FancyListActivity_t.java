 package ch.shibastudio.test;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
import android.widget.Toast;
 
 public class FancyListActivity extends Activity {
     ListView lv;
    FancyAdapter fAdapter;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         lv = (ListView)findViewById(R.id.list);
         
         ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
         HashMap<String, String> map;
         
        fAdapter = new FancyAdapter(getApplicationContext());
         boolean flag = false;
         for(int i=0; i<1000; i++){
         	CharacterInfos cInfos = new CharacterInfos();
         	map = new HashMap<String, String>();
         	String charact;
         	String inf1 = "" +i;
         	if(flag){
         		charact = "A";
         	}else{
         		charact = "B";
         	}
         	// SimpleAdapter
             map.put("character", charact);
             map.put("info1", inf1);
             map.put("info2", "...");
             map.put("favicon", String.valueOf(R.drawable.favoritedisabled));
             listItem.add(map);
             
             // FancyAdapter
             cInfos.character = charact;
             cInfos.info1 = inf1;
             cInfos.info2 = "test FancyAdapter";
             cInfos.favorite = false;
             fAdapter.addEntry(cInfos);
             
             flag = !flag;
         }
          
         //SimpleAdapter mSchedule = new SimpleAdapter (this.getBaseContext(), listItem, R.layout.listrow, new String[] {"character", "info1", "info2", "favicon"}, new int[] {R.id.character, R.id.info1, R.id.info2, R.id.favicon});
         //lv.setAdapter(mSchedule);
         
         lv.setAdapter(fAdapter);
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CharacterInfos ci = (CharacterInfos)fAdapter.getItem(position);
				if(null != ci){
					Toast.makeText(getApplicationContext(), "" +ci.info1, Toast.LENGTH_SHORT).show();
				}
			}
		});
     }
 }
