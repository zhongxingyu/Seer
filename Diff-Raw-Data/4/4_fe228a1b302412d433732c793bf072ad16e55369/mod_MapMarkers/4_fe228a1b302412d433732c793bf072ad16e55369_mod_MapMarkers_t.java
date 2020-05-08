 package btwmod.livemap;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 import net.minecraft.src.MathHelper;
 
 import btwmods.CommandsAPI;
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.io.Settings;
 
 public class mod_MapMarkers implements IMod {
 
 	private Settings data;
 	private Gson gson;
 	
 	private Map<String, Marker> mapMarkers = new LinkedHashMap<String, Marker>();
 	private CommandMarker commandMarker = null;
 	
 	private File markerFile = new File(ModLoader.modDataDir, "markers.txt");
 	public int maxMarkersPerDimension = 3;
 	public long markerCooldownMinutes = 30;
 
 	@Override
 	public String getName() {
 		return "Map Markers";
 	}
 
 	@Override
 	public void init(Settings settings, Settings data) throws Exception {
 		this.data = data;
 		
 		gson = new GsonBuilder()
 			.setPrettyPrinting()
 			.enableComplexMapKeySerialization()
 			.create();
 
 
		if (settings.hasKey("markerFile")) {
			markerFile = new File(settings.get("markerFile"));
 		}
 		
 		maxMarkersPerDimension = settings.getInt("maxMarkersPerDimension", maxMarkersPerDimension);
 		markerCooldownMinutes = settings.getLong("markerCooldownMinutes", markerCooldownMinutes);
 		
 		loadMarkers(data);
 		saveMarkers(false);
 		
 		CommandsAPI.registerCommand(commandMarker = new CommandMarker(this), this);
 	}
 
 	@Override
 	public void unload() throws Exception {
 		CommandsAPI.unregisterCommand(commandMarker);
 	}
 	
 	public Marker getMarker(String username, int dimension, int markerIndex) {
 		return mapMarkers.get(username.toLowerCase() + "_" + dimension + "_" + markerIndex);
 	}
 	
 	public void setMarker(Marker marker) {
 		setMarker(marker, true);
 	}
 	
 	public void setMarker(Marker marker, boolean setCooldown) {
 		if (marker != null) {
 			mapMarkers.put(marker.username.toLowerCase() + "_" + marker.dimension + "_" + marker.markerIndex, marker);
 			
 			if (setCooldown)
 				data.setLong("markers", "lastset_" + marker.username.toLowerCase(), System.currentTimeMillis() / 1000L);
 		}
 	}
 	
 	public boolean removeMarker(String username, int dimension, int markerIndex) {
 		return mapMarkers.remove(username.toLowerCase() + "_" + dimension + "_" + markerIndex) != null;
 	}
 	
 	private void loadMarkers(Settings data) {
 		int count = data.getInt("markers", "count", 0);
 		for (int i = 0; i < count; i++) {
 			setMarker(Marker.fromSettings(data, "markers", i), false);
 		}
 	}
 	
 	public Marker[] getMarkers() {
 		return mapMarkers.values().toArray(new Marker[mapMarkers.size()]);
 	}
 	
 	public Marker[] getMarkers(String username) {
 		List<Marker> list = new ArrayList<Marker>();
 		for (Marker marker : mapMarkers.values()) {
 			if (marker.username.equalsIgnoreCase(username))
 				list.add(marker);
 		}
 		return list.toArray(new Marker[list.size()]);
 	}
 	
 	public boolean canSetMarker(String username) {
 		return getMarkerCooldownRemaining(username) > 0;
 	}
 	
 	public long getMarkerCooldownRemaining(String username) {
 		if (markerCooldownMinutes <= 0)
 			return 0L;
 		
 		long lastMarkerSet = data.getLong("markers", "lastset_" + username.toLowerCase(), -1L) * 1000L;
 		long now = System.currentTimeMillis();
 
 		return now < lastMarkerSet ? 0L : MathHelper.ceiling_double_int((double)Math.max(0L, (markerCooldownMinutes * 60000L) - (System.currentTimeMillis() - lastMarkerSet)) / 1000D);
 	}
 	
 	public void saveMarkers() {
 		saveMarkers(true);
 	}
 	
 	public void saveMarkers(boolean saveData) {
 		if (saveData) {
 			data.setInt("markers", "count", mapMarkers.size());
 			
 			int i = 0;
 			for (Marker marker : mapMarkers.values()) {
 				marker.toSettings(data, "markers", i++);
 			}
 			
 			try {
 				data.saveSettings();
 			} catch (IOException e) {
 				ModLoader.outputError(e, getName() + " failed (" + e.getClass().getSimpleName() + ") to save to the data file: " + e.getMessage());
 			}
 		}
 		
 		if (markerFile != null) {
 			JsonArray arr = new JsonArray();
 			JsonObject obj;
 			for (Marker marker : mapMarkers.values()) {
 				obj = new JsonObject();
 				obj.addProperty("username", marker.username);
 				obj.addProperty("markerIndex", marker.markerIndex);
 				obj.addProperty("type", marker.type.toString().toLowerCase());
 				obj.addProperty("dimension", marker.dimension);
 				obj.addProperty("x", marker.x);
 				obj.addProperty("z", marker.z);
 				
 				if (marker.z >= 0)
 					obj.addProperty("y", marker.y);
 				
 				if (marker.getDescription() != null)
 					obj.addProperty("description", marker.getDescription());
 				
 				arr.add(obj);
 			}
 			
 			try {
 				FileWriter jsonWriter = new FileWriter(markerFile);
 				jsonWriter.write(gson.toJson(arr));
 				jsonWriter.close();
 			}
 			catch (Throwable e) {
 				ModLoader.outputError(getName() + " failed to write to " + markerFile.getPath() + ": " + e.getMessage());
 			}
 		}
 	}
 }
