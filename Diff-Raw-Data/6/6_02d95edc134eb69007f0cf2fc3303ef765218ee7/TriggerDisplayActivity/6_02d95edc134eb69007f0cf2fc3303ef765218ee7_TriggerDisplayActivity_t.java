 package teamwork.goodVibrations;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class TriggerDisplayActivity extends Activity {
 	
 	//private ArrayList <String> triggerArray = new ArrayList<String>();
 	private ArrayAdapter <String> triggerArrayAdapter;
 	private ListView listView;
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.trigger_tab);
        
         triggerArrayAdapter = new ArrayAdapter<String>(this, R.layout.trigger_list_item);
         listView = (ListView) findViewById(R.id.listViewTriggers);
         listView.setAdapter(triggerArrayAdapter);
         
         final Button buttonAdd = (Button) findViewById(R.id.addTrigger);
        buttonAdd.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
             	triggerArrayAdapter.add("NEW TRIGGER!");
             }
         });
     }
 
 }
