 package net.m_kawato.tabletpos;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public class OrderInputHelper implements AdapterView.OnItemSelectedListener {
     public static final String TAG = "OrderInputHelper";
     private Activity activity;
     private Globals globals;
 
     public OrderInputHelper(Activity activity, Globals globals) {
         this.activity = activity;
         this.globals = globals;
     }
 
     // Event handler for spinners
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         Log.d(TAG, String.format("onItemSelected: id=%x, position=%d", parent.getId(), position));
         switch (parent.getId()) {
         case R.id.spn_route:
            if (globals.selectedRoute != position) {
                globals.selectedRoute = position;
                globals.selectedPlace = 0;
                updatePlaceSelector();
            }
             break;
         case R.id.spn_place:
             globals.selectedPlace = position;
             break;
         }
     }
     @Override
     public void onNothingSelected(AdapterView<?> parent) {
         Log.d(TAG, "onNothingSelected");
     }
 
     // Build Spinner for route selection
     public void buildRouteSelector() {
         Log.d(TAG, "buildRouteSelector");
         ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item);
         routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         for (String routeCode: globals.routes) {
             String routeName = globals.routeName.get(routeCode);
             routeAdapter.add(String.format("%s (%s)", routeName, routeCode));
         }
         Spinner spnRoute = (Spinner) activity.findViewById(R.id.spn_route);
         spnRoute.setAdapter(routeAdapter);
         spnRoute.setSelection(globals.selectedRoute);
         spnRoute.setOnItemSelectedListener(this);    
     }
 
     // Build Spinner for place selection
     public void buildPlaceSelector() {
         Log.d(TAG, "buildPlaceSelector");
         Spinner spnPlace = (Spinner) activity.findViewById(R.id.spn_place);
         spnPlace.setOnItemSelectedListener(this);
         updatePlaceSelector();
     }
     
     // Update list of Spinner for place selection
     private void updatePlaceSelector() {
         Log.d(TAG, "updatePlaceSelector");
         Spinner spnPlace = (Spinner) activity.findViewById(R.id.spn_place);
         ArrayAdapter<String> placeAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item);
         placeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         List<String> places = globals.places.get(globals.getSelectedRouteCode());
         for (String placeCode: places) {
             String placeName = globals.placeName.get(placeCode);
             placeAdapter.add(String.format("%s (%s)", placeName, placeCode));
         }
         spnPlace.setAdapter(placeAdapter);
         spnPlace.setSelection(globals.selectedPlace);
     }
 }
