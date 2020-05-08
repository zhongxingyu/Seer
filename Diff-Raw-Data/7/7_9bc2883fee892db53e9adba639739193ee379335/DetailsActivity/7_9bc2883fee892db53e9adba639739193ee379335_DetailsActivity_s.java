 package com.HuskySoft.metrobike.ui;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 import com.HuskySoft.metrobike.R;
 import com.HuskySoft.metrobike.backend.Leg;
 import com.HuskySoft.metrobike.backend.Route;
 import com.HuskySoft.metrobike.backend.Step;
 
 /**
  * @author mengwan, Xinyun Chen
  * 
  */
 public class DetailsActivity extends Activity {
 
     /**
      * Results from the search.
      */
     private ArrayList<Route> routes = null;
 
     /**
      * Current route that should be displayed on the map.
      */
     private int currRoute = -1;
     
     /**
      * TextView to show direction details.
      */
     private TextView directions;
     
     /**
      * TextView to show source address.
      */
     private TextView start;
     
     /**
      * TextView to show destination address.
      */
     private TextView destination;
 
     /**
      * onCreate function of DetailsActivity class Display the details of metroBike search.
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_details);
         directions = (TextView) findViewById(R.id.directions);
         start = (TextView) findViewById(R.id.directionsStart);
         destination = (TextView) findViewById(R.id.directionsDest);
         ActionBar actionBar = this.getActionBar();
         actionBar.setTitle("Details");
         @SuppressWarnings("unchecked")
         List<Route> recievedRoutes = (ArrayList<Route>) getIntent().getSerializableExtra(
                 "List of Routes");
         if (recievedRoutes != null) {
             routes = (ArrayList<Route>) recievedRoutes;
             currRoute = (Integer) getIntent().getSerializableExtra("Current Route Index");
             // if there exists route, show the details
             this.setDetails();
         }
     }
 
     /**
      * onCreate menu option of DetailsActivity.
      * @param menu the menu to be inflated.
      * @return boolean for showing whether the action has been successfully done.
      */
     @Override
     public final boolean onCreateOptionsMenu(final Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_menu, menu);
         return true;
     }
 
     /**
      * This method will be called when user click buttons in the setting menu.
      * 
      * @param item
      *            the menu item that user will click
      * @return true if user select an item
      */
     @Override
     public final boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
         case R.id.action_settings:
             // user click the setting button, start the settings activity
             Intent intent = new Intent(this, SettingsActivity.class);
             startActivity(intent);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * 
      * @param view
      *            : the view of the button onClick function of the go to
      *            Navigate button
      */
     public final void goToNavigate(final View view) {
         // Do something in response to button
         Intent intent = new Intent(this, NavigateActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         intent.putExtra("List of Routes", (Serializable) routes);
         intent.putExtra("Current Route Index", currRoute);
         startActivity(intent);
     }
 
     /**
      * 
      * @param view
      *            : the view of the button onClick function of the return to
      *            search page button
      */
     public final void goToSearchPage(final View view) {
         // Do something in response to button
         Intent intent = new Intent(this, SearchActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
     }
 
     /**
      * 
      * @param view
      *            : the view of the button onClick function for the return to
      *            result page button
      */
 
     public final void goToResults(final View view) {
         // Do something in response to button
         Intent intent = new Intent(this, ResultsActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         intent.putExtra("List of Routes", (Serializable) routes);
         intent.putExtra("Current Route Index", currRoute);
         startActivity(intent);
     }
 
     /**
      * Helper function that sets up the details of a route.
      */
     private void setDetails() {
         // sets up details
         Route displayRoute = routes.get(currRoute);
         List<Leg> legs = displayRoute.getLegList();
         int count = 1;
         start.append(legs.get(0).getStartAddress());
         for (int i = 0; i < legs.size(); i++) {
             Leg curLeg = legs.get(i);
             List<Step> steps = curLeg.getStepList();
             for (int j = 0; j < steps.size(); j++) {
                 Step s = steps.get(j);
                 String ss = s.getHtmlInstruction().replaceAll("\\<.*?>", "");
                 directions.append("\nStep " + count + "   " + ss);
                 count++;
             }
         }
         destination.append(legs.get(legs.size() - 1).getEndAddress());
     }
 }
