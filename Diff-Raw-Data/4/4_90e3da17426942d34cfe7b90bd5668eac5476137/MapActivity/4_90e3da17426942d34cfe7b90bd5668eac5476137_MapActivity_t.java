 package edu.mines.alterego;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.TileOverlayOptions;
 
 public class MapActivity extends Activity {
 
 	GoogleMap map;
 	Context context = this;
 	int count = 0;
 	
	RadioGroup markerGroup;
	RadioButton markerButton;
 	int markerId = -1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.mapactivity);
 
 		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
 				.getMap();
 		map.setMapType(GoogleMap.MAP_TYPE_NONE);
 		TileOverlayOptions opts = new TileOverlayOptions();
 		opts.tileProvider(new CustomMapTileProvider(getAssets()));
 
 		// Creating onLongClickListener for user to add marker to map
 		map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
 
 			@Override
 			public void onMapLongClick(final LatLng position) {
 				Log.i("AlterEgos::MapAct::onMapLogClick", "Long Click Occurred on Map");
 				
 				LayoutInflater mapLayI = LayoutInflater.from(context);
 				final View mapV = mapLayI.inflate(R.layout.new_marker_dialog, null);
 				AlertDialog.Builder addMarker = new AlertDialog.Builder(context);
 				
 				addMarker.setView(mapV);
 				addMarker.setTitle("Add Marker");
 				addMarker.setCancelable(false);
 				addMarker.setPositiveButton("Create", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 //						Log.i("AlterEgos::MapAct::onMapLongClick::onClick", "AlertDialog Created");
 						AlertDialog groupDialog = (AlertDialog) dialog;
 						
 						markerGroup = (RadioGroup) groupDialog.findViewById(R.id.add_marker_group);
 //						Log.i("AlterEgos::MapAct::onMapLongClick::onClick::markerGroup", " markerGroup " + markerGroup);
 //						Log.i("AlterEgos::MapAct::onMapLongClick::onClick::markerId::BEFORE::", " markerId Before " + markerId);
 //						
 //					    // get selected radio button from radioGroup
 					    markerId = markerGroup.getCheckedRadioButtonId();
 //						Log.i("AlterEgos::MapAct::onMapLongClick::onClick::markerId::AFTER::", " markerId After " + markerId);
 						
 					    
 						// find the radio button by returned id
 					    markerButton = (RadioButton) groupDialog.findViewById(markerId);
 //					    Log.i("AlterEgos::MapAct::onMapLongClick::onClick::markerButton::", " markerButton " + markerButton);
 //						Toast.makeText(MapActivity.this,
 //							markerButton.getText(), Toast.LENGTH_SHORT).show();
 						
 						switch (markerId) {
 							case R.id.add_marker_player:
 								addPlayer(position);
 								break;
 							case R.id.add_marker_treasure:
 								addTreasure(position);
 								break;
 							case R.id.add_marker_enemy:
 								addEnemy(position);
 								break;
 							default:
 								Toast.makeText(MapActivity.this, "Nothing Selected", Toast.LENGTH_SHORT).show();
 								break;
 						}
 					}
 				});
 				
 				addMarker.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.cancel();
 
 							}
 						});
 
 				AlertDialog markerAlert = addMarker.create();
 				markerAlert.show();
 
 				
 			}
 		});
 
 	}
 	
 	public void addPlayer(final LatLng position) {
 
 		LayoutInflater mLayI = LayoutInflater.from(context);
 		final View mV = mLayI.inflate(R.layout.new_player_marker_dialog, null);
 		AlertDialog.Builder addPlayer = new AlertDialog.Builder(context);
 		addPlayer.setView(mV);
 		addPlayer.setTitle("Create Player");
 		addPlayer.setCancelable(false);
 		addPlayer.setPositiveButton("Create",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog,
 							int which) {
 						// grab user input for marker name
 						EditText mTitle = (EditText) mV
 								.findViewById(R.id.marker_player_title);
 
 						float mColor;
 
 						
 						switch(count) {
 							case 0:
 								mColor = BitmapDescriptorFactory.HUE_RED;
 								break;
 							case 1:
 								mColor = BitmapDescriptorFactory.HUE_ORANGE;
 								break;
 							case 2:
 								mColor = BitmapDescriptorFactory.HUE_YELLOW;
 								break;
 							case 3:
 								mColor = BitmapDescriptorFactory.HUE_GREEN;
 								break;
 							case 4:
 								mColor = BitmapDescriptorFactory.HUE_BLUE;
 								break;
 							case 5:
 								mColor = BitmapDescriptorFactory.HUE_CYAN;
 								break;
 							case 6:
 								mColor = BitmapDescriptorFactory.HUE_VIOLET;
 								break;
 							case 7:
 								mColor = BitmapDescriptorFactory.HUE_MAGENTA;
 								break;
 							case 8:
 								mColor = BitmapDescriptorFactory.HUE_AZURE;
 								break;
 							case 9:
 								mColor = BitmapDescriptorFactory.HUE_ROSE;
 								count = -1;
 								break;
 							default:
 								mColor = BitmapDescriptorFactory.HUE_RED;
 								break;
 						}		
 					
 
 						if (mTitle.getText().toString().equals("")) {
 							Toast name = Toast.makeText(MapActivity.this, "Required: Player Name", Toast.LENGTH_SHORT);
 							name.show();
 						} else {
 							map.addMarker(new MarkerOptions()
 									.title(mTitle.getText().toString())
 									.position(position)
 									.icon(BitmapDescriptorFactory
 											.defaultMarker(mColor))
 									.draggable(true));
 							count++;
 						}
 					}
 
 				});
 
 		addPlayer.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog,
 							int which) {
 						dialog.cancel();
 
 					}
 				});
 
 		AlertDialog alert = addPlayer.create();
 		alert.show();
 
 	}
 	
 	public void addTreasure(final LatLng position) {
 		LayoutInflater tLayI = LayoutInflater.from(context);
 		final View tV = tLayI.inflate(R.layout.new_treasure_marker_dialog, null);
 		AlertDialog.Builder addTreasure = new AlertDialog.Builder(context);
 		addTreasure.setView(tV);
 		addTreasure.setTitle("Create Treasure");
 		addTreasure.setCancelable(false);
 		addTreasure.setPositiveButton("Create",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog,
 							int which) {
 						// grab user input for marker name
 						EditText mTitle = (EditText)tV
 								.findViewById(R.id.marker_treasure_title);
 
 						if (mTitle.getText().toString().equals("")) {
 							Toast name = Toast.makeText(MapActivity.this, "Required: Treasure Title", Toast.LENGTH_SHORT);
 							name.show();
 						} else {
 							map.addMarker(new MarkerOptions()
 									.title(mTitle.getText().toString())
 									.position(position)
 									.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_treasure))
 									.draggable(true));
 						}
 					}
 
 				});
 
 		addTreasure.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog,
 							int which) {
 						dialog.cancel();
 
 					}
 				});
 
 		AlertDialog alert = addTreasure.create();
 		alert.show();
 	}
 	
 	public void addEnemy(final LatLng position) {
 		LayoutInflater eLayI = LayoutInflater.from(context);
 		final View eV = eLayI.inflate(R.layout.new_enemy_marker_dialog, null);
 		AlertDialog.Builder addEnemy = new AlertDialog.Builder(context);
 		addEnemy.setView(eV);
 		addEnemy.setTitle("Create Enemy");
 		addEnemy.setCancelable(false);
 		addEnemy.setPositiveButton("Create",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog,
 							int which) {
 						// grab user input for marker name
 						EditText mTitle = (EditText)eV
 								.findViewById(R.id.marker_enemy_title);
 
 						if (mTitle.getText().toString().equals("")) {
 							Toast name = Toast.makeText(MapActivity.this, "Required: Enemy Type/Name", Toast.LENGTH_SHORT);
 							name.show();
 						} else {
 							map.addMarker(new MarkerOptions()
 									.title(mTitle.getText().toString())
 									.position(position)
 									.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_skull))
 									.draggable(true));
 						}
 					}
 
 				});
 
 		addEnemy.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog,
 							int which) {
 						dialog.cancel();
 
 					}
 				});
 
 		AlertDialog alert = addEnemy.create();
 		alert.show();
 	}
 }
