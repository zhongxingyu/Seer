 package org.spoutcraft.launcher.modpacks;
 
 import java.io.File;
 import java.util.Map;
 
 import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.PlatformUtils;
 
 public class InstalledModsYML {
 
 	private static final String	INSTALLED_MODS_YML	= "installedMods.yml";
	public static File					installedModsYML		= new File(PlatformUtils.getWorkingDirectory(), INSTALLED_MODS_YML);
 
 	public static boolean setInstalledModVersion(String modName, String version) {
 		Configuration modsConfig = new Configuration(installedModsYML);
 		modsConfig.load();
 		modsConfig.setProperty(getModPath(modName), version);
 		return modsConfig.save();
 	}
 
 	public static String getInstalledModVersion(String modName) {
 		Configuration modsConfig = new Configuration(installedModsYML);
 		modsConfig.load();
 		return (String) modsConfig.getProperty(getModPath(modName));
 	}
 
 	private static String getModPath(String modName) {
 		return String.format("mods.%s", modName);
 	}
 
 	public static boolean removeMod(String modName) {
 		Configuration modsConfig = new Configuration(installedModsYML);
 		modsConfig.load();
 		modsConfig.removeProperty(getModPath(modName));
 		return modsConfig.save();
 	}
 
 	public static Map<String, String> getInstalledMods() {
 		Configuration modsConfig = new Configuration(installedModsYML);
 		modsConfig.load();
 		return (Map<String, String>) modsConfig.getProperty("mods");
 	}
 }
