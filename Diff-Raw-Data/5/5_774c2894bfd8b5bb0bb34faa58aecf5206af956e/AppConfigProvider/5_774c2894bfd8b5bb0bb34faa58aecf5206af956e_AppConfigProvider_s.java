 package tesla.app.command.provider;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import tesla.app.command.Command;
 
 public class AppConfigProvider {
 
 	public static final String APP_RHYTHMBOX = "rhythmbox";
 	public static final String APP_AMAROK = "amarok";
 	
 	public String appName = "amarok";
 	
 	public AppConfigProvider(String appName) {
 		this.appName = appName;
 	}
 	
 	public String getCommand(String key) throws Exception {
 		
 		// These commands will be extracted from
 		// a database of application configurations
 		
 		if (appName.equals(APP_RHYTHMBOX)) {
 			return rhythmBoxCommand(key);
 		}
 		else {
 			return amarokCommand(key);
 		}
 	}
 	
 	public Map<String, String> getSettings(String key) {
 		Map<String, String> settings = null;
 		if (appName.equals(APP_RHYTHMBOX)) {
 			settings = rhythmboxSettings(key);
 		}
 		else {
 			settings = amarokSettings(key);
 		}
 		return settings;
 	}
 	
 	public Map<String, String> rhythmboxSettings(String key) {
 		Map<String, String> settings = new HashMap<String, String>();
 		if (key.equals(Command.VOL_CURRENT)) {
 			settings.put("MIN", "0.0");
 			settings.put("MAX", "1.0");
 		}
 		return settings;
 	}
 	
 	public Map<String, String> amarokSettings(String key) {
 		Map<String, String> settings = new HashMap<String, String>();
 		if (key.equals(Command.VOL_CURRENT)) {
 			settings.put("MIN", "0.0");
 			settings.put("MAX", "100.0");
 		}
 		return settings;
 	}
 	
 	private String rhythmBoxCommand(String key) throws Exception {
 		String out = "";
 		if (key.equals(Command.PLAY)) {
 			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player playPause false";
 		}
 		else if (key.equals(Command.PAUSE)) {
 			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player playPause false";
 		}
 		else if (key.equals(Command.PREV)) {
 			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player previous";
 		}
 		else if (key.equals(Command.NEXT)) {
 			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player next";
 		}
 		else if (key.equals(Command.VOL_CHANGE)) {
 			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player setVolume %f";
 		}
 		else if (key.equals(Command.VOL_MUTE)) {
 			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player setVolume 0.0";
 		}
 		else if (key.equals(Command.VOL_CURRENT)) {
 			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getVolume";
 		}
 		else {
 			throw new Exception("Command not implemented");
 		}
 		return out;
 	}
 
 	private String amarokCommand(String key) throws Exception {
 		String out = "";
 		if (key.equals(Command.PLAY)) {
			out = "qdbus org.kde.amarok /Player Play";
 		}
 		else if (key.equals(Command.PAUSE)) {
 			out = "qdbus org.kde.amarok /Player Pause";
 		}
 		else if (key.equals(Command.PREV)) {
			out = "qqdbus org.kde.amarok /Player Prev";
 		}
 		else if (key.equals(Command.NEXT)) {
 			out = "qdbus org.kde.amarok /Player Next";
 		}
 		else if (key.equals(Command.VOL_CHANGE)) {
 			out = "qdbus org.kde.amarok /Player VolumeSet %i";
 		}
 		else if (key.equals(Command.VOL_MUTE)) {
 			out = "qdbus org.kde.amarok /Player VolumeSet 0";
 		}
 		else if (key.equals(Command.VOL_CURRENT)) {
 			out = "qdbus org.kde.amarok /Player VolumeGet";
 		}
 		else {
 			throw new Exception("Command not implemented");
 		}
 		return out;
 	}
 }
