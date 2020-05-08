 package btwmod.livemap;
 
 import btwmods.Util;
 import btwmods.io.Settings;
 
 public class Marker {
 	
 	public enum TYPE {
 		HOME, POINT;
 		
 		public static String[] tabCompletion = new String[] { "home" };
 		
 		public String asHumanReadable() {
 			return this.toString().toLowerCase();
 		}
 	};
 	
 	public final String username;
 	public final int markerIndex;
 	public final TYPE type;
 
 	public final int dimension;
 	public final int x;
 	public final int y;
 	public final int z;
 	
 	private String description = null;
 	
 	public void setDescription(String description) {
 		if (description != null && description.trim().length() == 0)
 			description = null;
 		
 		this.description = description;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public Marker(String username, int markerIndex, TYPE type, int dimension, int x, int z) {
 		this(username, markerIndex, type, dimension, x, -1, z);
 	}
 	
 	public Marker(String username, int markerIndex, TYPE type, int dimension, int x, int y, int z) {
 		this.username = username;
 		this.markerIndex = markerIndex;
 		this.type = type;
 		this.dimension = dimension;
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 	
 	public void toSettings(Settings data, String section, int index) {
 		data.set(section, index + "username", username);
 		data.setInt(section, index + "markerIndex", markerIndex);
 		data.set(section, index + "type", type.toString());
 		data.setInt(section, index + "dimension", dimension);
 		data.setInt(section, index + "x", x);
 		data.setInt(section, index + "y", y);
 		data.setInt(section, index + "z", z);
 		
		if (description == null)
			data.removeKey(section, index + "description");
		else
 			data.set(section, index + "description", description);
 	}
 	
 	public static Marker fromSettings(Settings data, String section, int index) {
 		String username = data.get(section, index + "username");
 		
 		if (username != null && username.trim().length() != 0
 			&& data.hasKey(section, index + "type")
 			&& data.isInt(section, index + "markerIndex")
 			&& data.isInt(section, index + "dimension")
 			&& data.isInt(section, index + "x")
 			&& data.isInt(section, index + "z")) {
 			
 			TYPE type;
 			try {
 				type = TYPE.valueOf(data.get(section, index + "type"));
 			}
 			catch (IllegalArgumentException e) {
 				return null;
 			}
 			
 			int markerIndex = data.getInt(section, index + "markerIndex", 0);
 			int dimension = data.getInt(section, index + "dimension", 0);
 			
 			if (markerIndex >= 0 && Util.getWorldNameFromDimension(dimension) != null) {
 				int x = data.getInt(section, index + "x", 0);
 				int y = Math.max(-1, data.getInt(section, index + "y", -1));
 				int z = data.getInt(section, index + "z", 0);
 				
 				Marker newMarker = new Marker(username, markerIndex, type, dimension, x, y, z);
 				newMarker.setDescription(data.get(section, index + "description"));
 				
 				return newMarker;
 			}
 		}
 		
 		return null;
 	}
 }
