 package net.roguedraco.lang;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Lang {
 	
 	private static FileConfiguration lang = null;
 	private File langFile = null;
 	private JavaPlugin plugin;
 	
 	public Lang(JavaPlugin plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void setupLanguage() {
 		if (langFile == null) {
			langFile = new File("plugins/InfoButton/", "lang.yml");
 		}
 		lang = YamlConfiguration.loadConfiguration(langFile);
 
 		// Look for defaults in the jar
 		InputStream defConfigStream = plugin.getResource("lang.yml");
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration
 					.loadConfiguration(defConfigStream);
 			lang.setDefaults(defConfig);
 		}
 	}
 	
 	public void saveLanguage() {
 		if (lang == null || langFile == null) {
 			return;
 		}
 		try {
 			lang.save(langFile);
 		} catch (IOException ex) {
 			plugin.getLogger().log(Level.SEVERE,
 					"Could not save config to " + langFile, ex);
 		}
 	}
 	
 	public static String get(String path) {
 		String val = lang.getString(path);
 		return parseColours(val);
 	}
 	
 	public static String parseColours(String str) {
 		if(str == null) {
 			str = "";
 		}
 		Pattern color_codes = Pattern.compile("&([0-9A-Fa-fKkLlOoMmNn])");
 		Matcher find_colors = color_codes.matcher(str);
 		while (find_colors.find()) {
 		 str = find_colors.replaceFirst(new StringBuilder().append(ChatColor.COLOR_CHAR).append(find_colors.group(1)).toString());
 		 find_colors = color_codes.matcher(str);
 		}
 		return str;
 	}
 }
