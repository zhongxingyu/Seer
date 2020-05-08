 package nl.q42.hue2.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import nl.q42.hue2.PHUtilitiesImpl;
 import nl.q42.hue2.PresetsDataSource;
 import nl.q42.hue2.R;
 import nl.q42.hue2.Util;
 import nl.q42.hue2.dialogs.ErrorDialog;
 import nl.q42.hue2.dialogs.PresetRemoveDialog;
 import nl.q42.hue2.models.Bridge;
 import nl.q42.hue2.models.Preset;
 import nl.q42.hue2.views.ColorButton;
 import nl.q42.hue2.views.FeedbackSwitch;
 import nl.q42.javahueapi.HueService;
 import nl.q42.javahueapi.models.FullConfig;
 import nl.q42.javahueapi.models.Group;
 import nl.q42.javahueapi.models.Light;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class LightsActivity extends Activity {
 	// It takes extremely long for the server to update its data, so this interval is reasonable
 	private final static long REFRESH_INTERVAL = 5000;
 	
 	// startActivityForResult identifiers
 	private final static int ACTIVITY_LIGHT = 1;
 	private final static int ACTIVITY_GROUP = 2;
 	
 	private boolean connected = false;
 	
 	private Bridge bridge;
 	private HueService service;
 	
 	private HashMap<String, Light> lights = new HashMap<String, Light>();
 	private HashMap<String, ArrayList<View>> lightViews = new HashMap<String, ArrayList<View>>();
 	
 	private HashMap<String, Group> groups = new HashMap<String, Group>();
 	private HashMap<String, View> groupViews = new HashMap<String, View>();
 	
 	private TextView messageView;
 	private LinearLayout resultContainer;
 	private LinearLayout groupResultList;
 	private LinearLayout lightResultList;
 	private ImageButton refreshButton;
 	private ProgressBar loadingSpinner;
 	
 	// Database operations are simple, so they can be run in UI thread
 	private PresetsDataSource datasource;
 	private HashMap<String, ArrayList<Preset>> lightPresets;
 	private HashMap<String, ArrayList<Preset>> groupPresets;
 	
 	private Timer refreshTimer = new Timer();
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lights);
 		setTitle(R.string.app_name);
 		
 		// Set up loading UI elements		
 		ActionBar ab = getActionBar();
 		ab.setCustomView(R.layout.loader);
 		ab.setDisplayShowCustomEnabled(true);
 		ab.setDisplayShowHomeEnabled(false);
 		
 		RelativeLayout loadingLayout = (RelativeLayout) ab.getCustomView();
 		
 		loadingSpinner = (ProgressBar) loadingLayout.findViewById(R.id.loader_spinner);
 		refreshButton = (ImageButton) loadingLayout.findViewById(R.id.loader_refresh);
 		
 		messageView = (TextView) findViewById(R.id.lights_message);
 		
 		resultContainer = (LinearLayout) findViewById(R.id.lights_result_container);
 		groupResultList = (LinearLayout) findViewById(R.id.lights_groups_list);
 		lightResultList = (LinearLayout) findViewById(R.id.lights_lights_list);
 		
 		refreshButton.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				refreshState(true);
 			}
 		});
 		
 		// Open color preset database
 		datasource = new PresetsDataSource(this);
 		datasource.open();
 		
 		// Check if bridge info was passed
 		if (getIntent().hasExtra("bridge")) {
 			bridge = (Bridge) getIntent().getSerializableExtra("bridge");
 			Util.setLastBridge(this, bridge);
 		} else if (Util.getLastBridge(this) != null) {
 			bridge = Util.getLastBridge(this);
 		} else {
 			// No last bridge saved and no passed bridge, return to bridge search activity
 			Intent searchIntent = new Intent(this, LinkActivity.class);
 			searchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(searchIntent);
 			return;
 		}
 		
 		// Load presets (fast enough to do on UI thread for now)
 		lightPresets = datasource.getLightPresets(bridge);
 		groupPresets = datasource.getGroupPresets(bridge);
 		
 		// Set up bridge info
 		service = new HueService(bridge.getIp(), Util.getDeviceIdentifier(this));
 		
 		// Loading lights
 		if (savedInstanceState == null) {
 			refreshState(true);
 		} else {
 			lights = (HashMap<String, Light>) savedInstanceState.getSerializable("lights");
 			groups = (HashMap<String, Group>) savedInstanceState.getSerializable("groups");
 			connected = savedInstanceState.getBoolean("connected");
 			
 			if (connected) {
 				populateViews();
 			} else {
 				refreshState(true);
 			}
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.lights, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_bridges) {
 			Util.setLastBridge(this, null);
 			
 			Intent searchIntent = new Intent(this, LinkActivity.class);
 			searchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(searchIntent);
 			return true;
 		} else if (item.getItemId() == R.id.menu_new_group) {
 			// Check if the group limit has been reached (group 16 always returns empty lights list, so ignore)
 			if (groups.size() >= 1 + 15) {
 				ErrorDialog.show(getFragmentManager(), R.string.dialog_too_many_groups_title, R.string.dialog_too_many_groups);
 			} else {
 				Intent groupIntent = new Intent(LightsActivity.this, GroupActivity.class);
 				groupIntent.putExtra("lights", lights);
 				startActivityForResult(groupIntent, ACTIVITY_GROUP);
 			}
 			
 			return true;
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		
 		state.putSerializable("lights", lights);
 		state.putSerializable("groups", groups);
 		state.putBoolean("connected", connected);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		datasource.close();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		refreshTimer.cancel();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		startRefreshTimer();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		String id = data.getStringExtra("id");
 		boolean colorChanged = data.getBooleanExtra("colorChanged", false);
 		
 		// If user was picking colors and then cancelled, restore original colors for individual light or entire group
 		if (resultCode == RESULT_CANCELED && colorChanged) {
 			ArrayList<String> lightsToRestore = new ArrayList<String>();
 			
 			if (requestCode == ACTIVITY_GROUP) {
 				lightsToRestore.addAll(groups.get(id).lights);
 			} else {
 				lightsToRestore.add(id);
 			}
 			
 			for (String lid : lightsToRestore) {
 				Light light = lights.get(lid);
 				
 				if (light.state.colormode.equals("ct")) {
 					setLightColorCT(lid, light.state.ct, light.state.bri, light.state.on);
 				} else if (light.state.colormode.equals("hs")) {
 					setLightColorHS(lid, light.state.hue, light.state.sat, light.state.bri, light.state.on);
 				} else {
 					setLightColorXY(lid, light.state.xy, light.state.bri, light.state.on);
 				}
 			}
 		} else if (resultCode == RESULT_OK) {
 			String name = data.getStringExtra("name");
 			String mode = data.getStringExtra("mode");
 			float[] xy = data.getFloatArrayExtra("xy");
 			int ct = data.getIntExtra("ct", 153);
 			int bri = data.getIntExtra("bri", 0);
 			
 			if (requestCode == ACTIVITY_LIGHT) {
 				Light light = lights.get(id);
 				
 				if (data.getBooleanExtra("addPreset", false)) {
 					if (mode.equals("ct")) {
 						addLightPresetCT(id, ct, bri);
 					} else {
 						addLightPresetXY(id, xy, bri);
 					}
 				}
 				
 				if (!light.name.equals(name)) {
 					setLightName(id, name);
 				}
 				
 				if (colorChanged) {
 					if (mode.equals("ct")) {
 						setLightColorCT(id, ct, bri, true);
 					} else {
 						setLightColorXY(id, xy, bri, true);
 					}
 				}
 			} else if (requestCode == ACTIVITY_GROUP) {
 				ArrayList<String> lights = (ArrayList<String>) data.getSerializableExtra("lights");
 				
 				// If id was given, an existing group was edited, otherwise a new one is to be created
 				if (id != null) {
 					Group group = groups.get(id);
 					
 					if (data.getBooleanExtra("remove", false)) {
 						removeGroup(id);
 					} else {
 						if (data.getBooleanExtra("addPreset", false)) {
 							if (mode.equals("ct")) {
 								addGroupPresetCT(id, ct, bri);
 							} else {
 								addGroupPresetXY(id, xy, bri);
 							}
 						}
 						
 						if (!group.name.equals(name)) {
 							setGroupName(id, name);
 						}
 						
 						if (colorChanged) {
 							if (mode.equals("ct")) {
 								setGroupColorCT(id, ct, bri);
 							} else {
 								setGroupColorXY(id, xy, bri);
 							}
 						}
 						
 						if (!group.lights.equals(lights)) {
 							setGroupLights(id, lights);
 						}
 					}
 				} else {
 					createGroup(name, lights);
 				}
 			}
 		}
 	}
 	
 	private void startRefreshTimer() {
 		refreshTimer = new Timer();
 		refreshTimer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				resultContainer.post(new Runnable() {
 					@Override
 					public void run() {
 						refreshState(false);
 					}
 				});
 			}
 		}, REFRESH_INTERVAL, REFRESH_INTERVAL);
 	}
 	
 	/**
 	 * Enable/disable all switches (use while executing actions or refreshing state)
 	 */
 	private Timer indicatorTimer = new Timer();
 	private void setActivityIndicator(boolean enabled, boolean forced) {		
 		if (enabled) {
 			// Tasks shorter than 300 ms don't warrant a visual loading indicator
 			if (!forced) {
 				indicatorTimer = new Timer();
 				indicatorTimer.schedule(new TimerTask() {
 					@Override
 					public void run() {
 						refreshButton.post(new Runnable() {
 							@Override
 							public void run() {
 								refreshButton.setVisibility(View.GONE);
 								loadingSpinner.setVisibility(View.VISIBLE);
 							}
 						});
 					}
 				}, 300);
 			} else {
 				refreshButton.setVisibility(View.GONE);
 				loadingSpinner.setVisibility(View.VISIBLE);
 			}
 		} else {
 			indicatorTimer.cancel();
 			
 			refreshButton.setVisibility(View.VISIBLE);
 			loadingSpinner.setVisibility(View.GONE);
 		}
 	}
 	
 	/**
 	 * Reflect local lights and groups state in UI
 	 */
 	private void refreshViews() {
 		if (configurationChanged()) {
 			repopulateViews();
 		} else {
 			refreshGroups();
 			refreshLights();
 		}
 	}
 	
 	/**
 	 * Force all views to not just be refreshed, but be recreated
 	 */
 	private void repopulateViews() {
 		lightViews.clear();
 		lightResultList.removeAllViews();
 		
 		groupViews.clear();
 		groupResultList.removeAllViews();
 		
 		populateViews();
 	}
 	
 	/**
 	 * Check if groups were modified externally
 	 */
 	private boolean configurationChanged() {
 		// Cross-check groups
 		for (String id : groups.keySet()) if (!groupViews.containsKey(id)) return true;
 		for (String id : groupViews.keySet()) if (!groups.containsKey(id)) return true;
 		
 		// Cross-check lights
 		for (String id : lights.keySet()) if (!lightViews.containsKey(id)) return true;
 		for (String id : lightViews.keySet()) if (!lights.containsKey(id)) return true;
 		
 		return false;
 	}
 	
 	private void refreshGroups() {
 		for (final String id : groupViews.keySet()) {
 			View view = groupViews.get(id);
 			Group group = groups.get(id);
 			
 			((TextView) view.findViewById(R.id.lights_group_name)).setText(group.name);
 			
 			// Add preset buttons - if there are any presets	
 			if (groupPresets.containsKey(id)) {
 				LinearLayout presetsView = (LinearLayout) view.findViewById(R.id.lights_group_presets);
 				presetsView.removeAllViews();
 				
 				for (final Preset preset : groupPresets.get(id)) {
 					ColorButton presetBut = (ColorButton) getLayoutInflater().inflate(R.layout.lights_preset_button, presetsView, false);
 					
 					if (preset.color_mode.equals("xy")) {
 						presetBut.setColor(PHUtilitiesImpl.colorFromXY(preset.xy, null));
 					} else {
 						presetBut.setColor(Util.temperatureToColor(1000000 / (int) preset.ct));
 					}
 					
 					presetBut.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							if (preset.color_mode.equals("xy")) {
 								setGroupColorXY(id, preset.xy, preset.brightness);
 							} else {
 								setGroupColorCT(id, (int) preset.ct, preset.brightness);
 							}
 						}
 					});
 					
 					presetBut.setOnLongClickListener(new OnLongClickListener() {
 						@Override
 						public boolean onLongClick(View v) {
 							PresetRemoveDialog.newInstance(preset).show(getFragmentManager(), "dialog_remove_preset");
 							return true;
 						}
 					});
 					
 					presetsView.addView(presetBut);
 				}
 			}
 			
 			if (groupPresets.containsKey(id) && groupPresets.get(id).size() > 0) {
 				view.findViewById(R.id.lights_group_scroller).setVisibility(View.VISIBLE);
 			} else {
 				view.findViewById(R.id.lights_group_scroller).setVisibility(View.GONE);
 			}
 		}
 	}
 	
 	private void refreshLights() {
 		for (final String id : lightViews.keySet()) {
 			ArrayList<View> views = lightViews.get(id);
 			Light light = lights.get(id);
 			
 			for (View view : views) {
 				view.setEnabled(light.state.reachable);
 				
 				TextView nameView = (TextView) view.findViewById(R.id.lights_light_name);
 				nameView.setText(light.name);
 				nameView.setTextColor(light.state.reachable ? Color.WHITE : Color.GRAY);
 				
 				// Set background of light icon to light color
 				final View colorView = view.findViewById(R.id.lights_light_color);
 				colorView.setBackgroundColor(light.state.reachable ? Util.getRGBColor(light) : Color.BLACK);
 				
 				// Set switch
 				FeedbackSwitch switchView = (FeedbackSwitch) view.findViewById(R.id.lights_light_switch);
 				switchView.setEnabled(light.state.reachable);
 				switchView.setCheckedCode(light.state.reachable && light.state.on);
 				
 				// Add preset buttons - if there are any presets	
 				LinearLayout presetsView = (LinearLayout) view.findViewById(R.id.lights_light_presets);
 				presetsView.removeAllViews();
 					
 				if (lightPresets.containsKey(id)) {
 					for (final Preset preset : lightPresets.get(id)) {
 						ColorButton presetBut = (ColorButton) getLayoutInflater().inflate(R.layout.lights_preset_button, presetsView, false);
 						
 						if (preset.color_mode.equals("xy")) {
 							presetBut.setColor(PHUtilitiesImpl.colorFromXY(preset.xy, light.modelid));
 						} else {
 							presetBut.setColor(Util.temperatureToColor(1000000 / (int) preset.ct));
 						}
 						
 						presetBut.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View v) {
 								if (preset.color_mode.equals("xy")) {
 									setLightColorXY(id, preset.xy, preset.brightness, true);
 								} else {
 									setLightColorCT(id, (int) preset.ct, preset.brightness, true);
 								}
 							}
 						});
 						
 						presetBut.setOnLongClickListener(new OnLongClickListener() {
 							@Override
 							public boolean onLongClick(View v) {
 								PresetRemoveDialog.newInstance(preset, lights.get(id)).show(getFragmentManager(), "dialog_remove_preset");
 								return true;
 							}
 						});
 						
 						presetsView.addView(presetBut);
 					}
 				}
 				
 				if (lightPresets.containsKey(id) && lightPresets.get(id).size() > 0) {
 					view.findViewById(R.id.lights_light_scroller).setVisibility(View.VISIBLE);
 				} else {
 					view.findViewById(R.id.lights_light_scroller).setVisibility(View.GONE);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Download fresh copy of light state from bridge
 	 */
 	private void refreshState(final boolean flush) {
 		final HashMap<String, Group> oldGroups = groups;
 		
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override
 			protected void onPreExecute() {
 				// Empty state
 				if (flush) {					
 					lights.clear();
 					lightViews.clear();
 					lightResultList.removeAllViews();
 					
 					groups.clear();
 					groupViews.clear();
 					groupResultList.removeAllViews();
 					
 					resultContainer.setVisibility(View.INVISIBLE);
 					
 					if (!connected) {
 						messageView.setVisibility(View.VISIBLE);
 						messageView.setText(R.string.lights_connecting);
 					}
 					
 					setActivityIndicator(true, true);
 				}
 			}
 			
 			@Override
 			protected Boolean doInBackground(Void... params) {
 				try {
 					FullConfig cfg = service.getFullConfig();
 					
 					lights = new HashMap<String, Light>(cfg.lights);
 					groups = new HashMap<String, Group>(cfg.groups);
 					
 					// Add pseudo group with all lights
 					Group allGroup = new Group();
 					allGroup.name = getString(R.string.lights_group_all);
 					allGroup.lights = new ArrayList<String>(lights.keySet());
 					groups.put("0", allGroup);
 					
 					return true;
 				} catch (Exception e) {
 					return false;
 				}
 			}
 
 			@Override
 			protected void onPostExecute(Boolean success) {
 				messageView.setVisibility(success ? View.GONE : View.VISIBLE);
 				messageView.setText(R.string.lights_not_connected);
 				
 				if (success) {
 					connected = true;
 					
 					// Check if groups changed internally
 					boolean groupChanged = false;
 					for (String id : oldGroups.keySet()) {
 						if (!groups.containsKey(id)) continue;
 						
 						if (groups.get(id).lights.hashCode() != oldGroups.get(id).lights.hashCode() ||
 							!groups.get(id).name.equals(oldGroups.get(id).name)) {
 							groupChanged = true;
 						}
 						
 						// If the server reports a group as empty, ignore it and keep the local list
 						if (groups.get(id).lights.size() == 0 && oldGroups.get(id).lights.size() != 0) {
 							groups.get(id).lights = oldGroups.get(id).lights;
 						}
 					}
 					
 					if (flush) {
 						populateViews();
 						resultContainer.setVisibility(View.VISIBLE);
 					} else if (groupChanged) {
 						repopulateViews();
 					} else {
 						refreshViews();
 					}
 				} else if (!success && flush) {
 					connected = false;
 				}
 				
 				setActivityIndicator(false, true);
 			}
 		}.execute();
 	}
 	
 	private void populateViews() {
 		populateGroupList();
 		populateGroups();
 		refreshViews();
 	}
 	
 	private void populateGroupList() {
 		View lastView = null;
 		
 		// Show group header only when there's actually multiple groups
 		findViewById(R.id.lights_groups_header).setVisibility(groups.size() > 1 ? View.VISIBLE : View.GONE);
 		
 		// Sort groups by id
 		ArrayList<String> groupIds = new ArrayList<String>();
 		groupIds.addAll(groups.keySet());
 		Util.sortNumericallyIfPossible(groupIds);
 		
 		for (final String id : groupIds) {
 			Group group = groups.get(id);
 			
 			// Create view
 			lastView = addGroupListView(groupResultList, id, group);
 			
 			// Associate view with group
 			groupViews.put(id, lastView);
 		}
 		
 		if (lastView != null && groups.size() > 1) {
 			lastView.findViewById(R.id.lights_group_divider).setVisibility(View.INVISIBLE);
 			lastView.findViewById(R.id.lights_group_divider).setBackgroundColor(Color.rgb(87, 87, 87));
 		} else {
 			lastView.findViewById(R.id.lights_group_divider).setBackgroundColor(Color.rgb(51, 181, 229));
 		}
 	}
 	
 	private void populateGroups() {
 		// Sort groups by id
 		ArrayList<String> groupIds = new ArrayList<String>();
 		groupIds.addAll(groups.keySet());
 		Util.sortNumericallyIfPossible(groupIds);
 		
 		// Build sorted list of all lights 
 		ArrayList<String> otherLights = new ArrayList<String>();
 		otherLights.addAll(lights.keySet());
 		Util.sortNumericallyIfPossible(otherLights);
 		
 		// For each group, add a header and the lights		
 		for (final String id : groupIds) {
 			Group group = groups.get(id);
 			if (id.equals("0")) continue;
 			
 			otherLights.removeAll(group.lights);
 			
 			// Create view
 			addGroupView(groupResultList, id, group);
 		}
 		
 		// Create group with any remaining lights
 		if (otherLights.size() > 0) {
 			Group otherGroup = new Group();
 			otherGroup.lights = otherLights;
 			
 			if (otherLights.size() == lights.size()) {
 				otherGroup.name = getString(R.string.lights_group_other_only);
 			} else {
 				otherGroup.name = getString(R.string.lights_group_other);
 			}
 			
 			addGroupView(groupResultList, null, otherGroup);
 		}
 	}
 	
 	private View addGroupView(ViewGroup container, final String id, final Group group) {
 		View view = getLayoutInflater().inflate(R.layout.lights_group_container, container, false);
 		
 		((TextView) view.findViewById(R.id.lights_group_container_title)).setText(group.name);
 		LinearLayout lightList = (LinearLayout) view.findViewById(R.id.lights_group_container_list);
 		
 		// Only show header if there are custom groups
 		view.findViewById(R.id.lights_group_container_header).setVisibility(groups.size() > 1 ? View.VISIBLE : View.GONE);
 		
 		// Sort lights in group by id
 		ArrayList<String> lightIds = new ArrayList<String>();
 		lightIds.addAll(group.lights);
 		Util.sortNumericallyIfPossible(lightIds);
 		
 		// Create and add view for all lights
 		View lastView = null;
 		
 		for (final String lid : lightIds) {
 			Light light = lights.get(lid);
 			
 			// Create view
 			lastView = addLightView(lightList, lid, light);
 			
 			// Associate view with light
 			if (!lightViews.containsKey(lid)) {
 				lightViews.put(lid, new ArrayList<View>());
 			}
 			lightViews.get(lid).add(lastView);
 		}
 		
 		if (lastView != null) {
 			lastView.findViewById(R.id.lights_light_divider).setVisibility(View.INVISIBLE);
 		}
 		
 		container.addView(view);
 		
 		return view;
 	}
 	
 	private View addGroupListView(ViewGroup container, final String id, final Group group) {
 		View view = getLayoutInflater().inflate(R.layout.lights_group, container, false);
 		
 		// Highlight all group if it's the only one
 		TextView nameView = ((TextView) view.findViewById(R.id.lights_group_name));
 		if (groups.size() == 1) {
 			nameView.setTextColor(Color.rgb(51, 181, 229));
 			nameView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
 		} else {
 			nameView.setTextColor(Color.WHITE);
 			nameView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7);
 		}
 		
 		// Set group edit event handler
 		view.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent groupIntent = new Intent(LightsActivity.this, GroupActivity.class);
 				groupIntent.putExtra("id", id);
 				groupIntent.putExtra("group", groups.get(id));
 				groupIntent.putExtra("lights", lights);
 				groupIntent.putExtra("service", service);
 				startActivityForResult(groupIntent, ACTIVITY_GROUP);
 			}
 		});
 		
 		// Set on/off button event handlers
 		OnClickListener listener = new OnClickListener() {
 			@Override
 			public void onClick(final View v) {
 				final boolean checked = v.getId() == R.id.lights_group_on;
 				
 				asyncUpdate(new AsyncCallbacks() {			
 					@Override
 					public Object doUpdate() throws Exception {
 						service.turnGroupOn(id, checked);
 						return null;
 					}
 					
 					@Override
 					public void updateState(Object result) {
 						for (String lid : groups.get(id).lights) {
 							lights.get(lid).state.on = checked;
 						}
 					}
 				});
 			}
 		};
 		
 		view.findViewById(R.id.lights_group_on).setOnClickListener(listener);
 		view.findViewById(R.id.lights_group_off).setOnClickListener(listener);
 		
 		container.addView(view);
 		
 		return view;
 	}
 	
 	private View addLightView(ViewGroup container, final String id, final Light light) {
 		View view = getLayoutInflater().inflate(R.layout.lights_light, container, false);
 		
 		// Set light edit event handler
 		view.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {				
 				Intent lightIntent = new Intent(LightsActivity.this, LightActivity.class);
 				lightIntent.putExtra("id", id);
 				lightIntent.putExtra("light", lights.get(id));
 				lightIntent.putExtra("service", service); // Used for color preview requests
 				startActivityForResult(lightIntent, ACTIVITY_LIGHT);
 			}
 		});
 		
 		// Set switch event handler
 		final FeedbackSwitch switchView = (FeedbackSwitch) view.findViewById(R.id.lights_light_switch);
 		switchView.setCheckedCode(light.state.on);
 		switchView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton view, final boolean checked) {				
 				turnLightOn(id, checked);
 			}
 		});
 		
 		container.addView(view);
 		
 		return view;
 	}
 	
 	private void addLightPresetCT(final String id, final float ct, final int bri) {
 		int db_id = datasource.insertPreset(bridge.getSerial(), id, null, ct, bri);
 		
 		if (!lightPresets.containsKey(id)) {
 			lightPresets.put(id, new ArrayList<Preset>());
 		}
 		lightPresets.get(id).add(new Preset(db_id, id, null, ct, bri));
 		
 		refreshViews();
 	}
 	
 	private void addLightPresetXY(final String id, final float[] xy, final int bri) {
 		int db_id = datasource.insertPreset(bridge.getSerial(), id, null, xy, bri);
 		
 		if (!lightPresets.containsKey(id)) {
 			lightPresets.put(id, new ArrayList<Preset>());
 		}
 		lightPresets.get(id).add(new Preset(db_id, id, null, xy, bri));
 		
 		refreshViews();
 	}
 	
 	private void addGroupPresetCT(final String id, final float ct, final int bri) {
 		int db_id = datasource.insertPreset(bridge.getSerial(), null, id, ct, bri);
 		
 		if (!groupPresets.containsKey(id)) {
 			groupPresets.put(id, new ArrayList<Preset>());
 		}
 		groupPresets.get(id).add(new Preset(db_id, null, id, ct, bri));
 		
 		refreshViews();
 	}
 	
 	private void addGroupPresetXY(final String id, final float[] xy, final int bri) {
 		int db_id = datasource.insertPreset(bridge.getSerial(), null, id, xy, bri);
 		
 		if (!groupPresets.containsKey(id)) {
 			groupPresets.put(id, new ArrayList<Preset>());
 		}
 		groupPresets.get(id).add(new Preset(db_id, null, id, xy, bri));
 		
 		refreshViews();
 	}
 	
 	public void removeColorPreset(Preset preset) {
 		datasource.removePreset(preset);
 		
 		if (preset.light != null) {
 			lightPresets.get(preset.light).remove(preset);
 		} else {
 			groupPresets.get(preset.group).remove(preset);
 		}
 		
 		refreshViews();
 	}
 	
 	private void setGroupColorXY(final String id, final float[] xy, final int bri) {
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.setGroupXY(id, xy, bri);;
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				for (String lid : groups.get(id).lights) {
 					Light light = lights.get(lid);
 					
 					light.state.on = true;
 					light.state.colormode = "xy";
 					light.state.xy = xy;
 					light.state.bri = bri;
 				}
 			}
 		});
 	}
 	
 	private void setGroupColorCT(final String id, final int ct, final int bri) {
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.setGroupCT(id, ct, bri);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				for (String lid : groups.get(id).lights) {
 					Light light = lights.get(lid);
 					
 					light.state.on = true;
 					light.state.colormode = "ct";
 					light.state.ct = ct;
 					light.state.bri = bri;
 				}
 			}
 		});
 	}
 	
 	private void setGroupLights(final String id, final ArrayList<String> lights) {
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.setGroupLights(id, lights);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				groups.get(id).lights = lights;
 				repopulateViews();
 			}
 		});
 	}
 	
 	private void setGroupName(final String id, final String name) {		
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.setGroupName(id, name);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				groups.get(id).name = name;
 				repopulateViews();
 			}
 		});
 	}
 	
 	public void removeGroup(final String id) {
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.removeGroup(id);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				groups.remove(id);
 				
 				groupPresets.remove(id);
 				datasource.removePresetsGroup(id);
 				
 				repopulateViews();
 			}
 		});
 	}
 	
 	private void setLightName(final String id, final String name) {
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.setLightName(id, name);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				lights.get(id).name = name;
 			}
 		});
 	}
 	
 	public void createGroup(final String name, final List<String> lights) {		
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				return service.createGroup(name, lights);
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				Group group = new Group();
 				group.name = name;
 				group.lights = new ArrayList<String>(lights);
 				groups.put(String.valueOf(result), group);
 				
 				repopulateViews();
 			}
 		});
 	}
 	
 	private void setLightColorCT(final String id, final int ct, final int bri, final boolean on) {		
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.setLightCT(id, ct, bri, on);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				Light light = lights.get(id);
 				light.state.on = on;
 				light.state.colormode = "ct";
 				light.state.ct = ct;
 				light.state.bri = bri;
 			}
 		});
 	}
 	
 	private void setLightColorHS(final String id, final int hue, final int sat, final int bri, final boolean on) {
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.setLightHS(id, hue, sat, bri, on);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				Light light = lights.get(id);
 				light.state.on = on;
 				light.state.colormode = "hs";
 				light.state.hue = hue;
 				light.state.sat = sat;
 				light.state.bri = bri;
 			}
 		});
 	}
 	
 	private void setLightColorXY(final String id, final float[] xy, final int bri, final boolean on) {
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.setLightXY(id, xy, bri, on);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				Light light = lights.get(id);
 				light.state.on = on;
 				light.state.colormode = "xy";
 				light.state.xy = xy;
 				light.state.bri = bri;
 			}
 		});
 	}
 	
 	private void turnLightOn(final String id, final boolean on) {
 		asyncUpdate(new AsyncCallbacks() {			
 			@Override
 			public Object doUpdate() throws Exception {
 				service.turnLightOn(id, on);
 				return null;
 			}
 			
 			@Override
 			public void updateState(Object result) {
 				Light light = lights.get(id);
 				light.state.on = on;
 			}
 		});
 	}
 	
 	private interface AsyncCallbacks {
 		public Object doUpdate() throws Exception;
 		public void updateState(Object result);
 	}
 	
 	private void asyncUpdate(final AsyncCallbacks callback) {
 		new AsyncTask<Void, Void, Object>() {
 			@Override
 			protected void onPreExecute() {
 				setActivityIndicator(true, false);
 			}
 			
 			@Override
 			protected Object doInBackground(Void... params) {
 				try {					
 					return callback.doUpdate();
 				} catch (Exception e) {
 					return e;
 				}
 			}
 			
 			@Override
 			protected void onPostExecute(Object result) {
 				setActivityIndicator(false, false);
 				
 				// Set successful, update state
 				if (!(result instanceof Exception)) {
 					callback.updateState(result);
 				} else {
 					ErrorDialog.showNetworkError(getFragmentManager());
 				}
 				
 				refreshViews();
 			}
 		}.execute();
 	}
 }
