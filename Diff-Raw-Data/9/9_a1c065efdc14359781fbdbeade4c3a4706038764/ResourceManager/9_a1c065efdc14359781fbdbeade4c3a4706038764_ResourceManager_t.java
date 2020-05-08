 package net.worldoftomorrow.eventcontrol;
 
 import java.io.File;
 
 public class ResourceManager {
 	
 	public static final char sepchar = File.separatorChar;
 	
 	public static File geti18nFile(String language) {
 		String path = EventControl.instance().getDataFolder().getPath() +
 				sepchar + "lang" + sepchar + language;
 		File langfile = new File(path);
 		if(Validate.isValidFile(langfile)) {
 			return langfile;
 		} else {
 			return null;
 		}
 	}
 }
