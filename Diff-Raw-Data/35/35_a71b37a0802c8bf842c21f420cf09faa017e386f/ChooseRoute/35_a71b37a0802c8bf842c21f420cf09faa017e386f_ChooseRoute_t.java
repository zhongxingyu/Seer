 package activities;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import data.DataBaseAccessor;
 
 import utils.Location;
 import utils.Controller;
 import utils.Routes;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import batmanmustdie.src.R;
 
 public class ChooseRoute extends Activity{
 	
 	Context c = getBaseContext();
 	
 	private final String TAG = "ChooseRoute";
 	
 	Controller controller = Controller.getInstance();
 	
 	List<Routes> prevRoutes = new ArrayList<Routes>();
 	int routeId = 0;
 	
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	Log.d(TAG,"Got to ChooseRoute activity");
     	setContentView(R.layout.chooseroutes);
     	
     	final TextView start = (TextView)findViewById(R.id.startLocationEditor);
 		final TextView end = (TextView)findViewById(R.id.endLocationEditor);
 		final ListView viewPrevRoutes = (ListView) findViewById(R.id.savedRoutesListView);
     	Button goButton = (Button)findViewById(R.id.goButton);
     	goButton.setOnClickListener(new OnClickListener() {
     		public void onClick(View view) {
     			 //Disabled for now, must be tested later
     			 //getRoads will probably be called from mainActivity. No need for it here other than checking that the entered locations are correct
     			String startLocation = start.getText().toString();
     			String endLocation = end.getText().toString();
     		
     			 List<Location> edLocs = controller.getRoads(startLocation, endLocation);
     			 //insert start and destination into the database
 //    			DataBaseAccessor.getInstance(c).insertLocations(edLocs);
     			 
    			 // Not needed, just save the values in the database
    			 //prevRoutes.add(new Routes(startLocation, endLocation, routeId));
    			 
     			 
     			// Go back to mainActivity
     			Intent i = new Intent();
     			i.putExtra("start", startLocation);
     			i.putExtra("end", endLocation);
    			routeId++; // not sure why this is needed, Oleg
                 setResult(RESULT_OK, i);
                 finish();
     		}
     	});
     	Button switchButton = (Button) findViewById(R.id.switchButton);
     	switchButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String temp = start.getText().toString();
 				start.setText(end.getText().toString());
 				end.setText(temp);
 			}
 		});
     	
     	/*Why doesnt this work?
    	 * 
     	 */
    	// test
    	prevRoutes.add(new Routes("start", "end", routeId));
		 prevRoutes.add(new Routes("start", "end2", routeId));
    	if (prevRoutes != null) { 
     		Log.d("ChooseRoute", "Trying to display Previous routes");
     		ArrayList<String> a1 = new ArrayList<String>();
     		for (Routes r: prevRoutes){
     			a1.add(r.getStart()+" - "+r.getEnd());
     		}
     		ArrayAdapter<String> a2 = new ArrayAdapter<String>(this, R.layout.testlayout, a1);
     		viewPrevRoutes.setAdapter(a2);
     	}
     }
 	
 }
