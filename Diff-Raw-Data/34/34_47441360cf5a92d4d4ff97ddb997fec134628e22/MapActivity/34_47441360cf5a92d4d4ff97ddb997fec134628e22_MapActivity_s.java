 package edu.mines.alterego;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.TileOverlay;
 import com.google.android.gms.maps.model.TileOverlayOptions;
 
 public class MapActivity extends Activity {
 	
 	GoogleMap map;
 	Context context = this ;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mapactivity);
 
         map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
         map.setMapType(GoogleMap.MAP_TYPE_NONE);
         TileOverlayOptions opts = new TileOverlayOptions();
         opts.tileProvider(new CustomMapTileProvider(getAssets()));
 
         TileOverlay overlay = map.addTileOverlay(opts);
  
         // Creating onLongClickListener for user to add marker to map      
         map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
 			
 			@Override
 			public void onMapLongClick(final LatLng position) {
 				LayoutInflater li = LayoutInflater.from(context);
				final View v = li.inflate(R.layout.create_new_marker_dialogue, null);
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setView(v);
				builder.setCancelable(false);
				builder.setPositiveButton("Add Player", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
				
						
 						EditText mTitle = (EditText) v.findViewById(R.id.marker_title);
 						map.addMarker(new MarkerOptions()
 								.title(mTitle.getText().toString())
 								.position(position)
 								.draggable(true)
 								);
 						
 						
 					}
 				});
 				
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 						
 					}
 				});
 				
				AlertDialog alert = builder.create();
 				alert.show();
 				
 			}
 		});
     
     }
 }
 
