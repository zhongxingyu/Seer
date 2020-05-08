 package fr.youchuzz;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxStatus;
 
 import fr.youchuzz.core.API;
 import fr.youchuzz.core.Chuzz;
 import fr.youchuzz.core.ChuzzAdapter;
 
 public class HomeActivity extends BaseActivity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_home);
 		
 		aq = new AQuery(this);
 		aq.id(R.id.home_create).clicked(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent myIntent = new Intent(v.getContext(), CreateActivity.class);
 				startActivity(myIntent);
 			}
 		});
 		
 		API.getInstance().getChuzzs(this, "onChuzzsLoaded");
 	
 		modalLoad("", getString(R.string.home_loading));
 	}
 	
 	public void onChuzzsLoaded(String url, JSONArray json, AjaxStatus status)
 	{
 		endModalLoad();
 		if(json == null)
 		{
 			error("Error while getting chuzzs list : err. " + status.getCode());
 		}
 		else
 		{
 			ArrayList<Chuzz> chuzzsList = new ArrayList<Chuzz>();
 			
 			Log.i("yc", "Retrieved " + Integer.toString(json.length()) + " chuzz(s).");
 			for(int i = 0; i < json.length(); i++)
 			{
 				JSONObject jsonChuzz = new JSONObject();
 				try {
 					jsonChuzz = json.getJSONObject(i);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				Chuzz chuzz = new Chuzz();
 				chuzz.id = getInt(jsonChuzz, "chuzz_id");
 				chuzz.title = getString(jsonChuzz, "title");
 				chuzz.nbVoters = getInt(jsonChuzz, "voters");
 				chuzz.creationDate = getString(jsonChuzz, "creation");
 				chuzz.imageUrl = getString(jsonChuzz, "img");
 
 				chuzzsList.add(chuzz);
 			}
 	
 			ListView lv = (ListView) findViewById(R.id.home_chuzzs);
 			ChuzzAdapter adapter = new ChuzzAdapter(this, R.layout.item_home_chuzz, chuzzsList);
 			lv.setAdapter(adapter);
 		}
 	}
 }
