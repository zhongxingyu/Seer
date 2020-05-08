 package tk.blackwolf12333.grieflog.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 
 import tk.blackwolf12333.grieflog.GriefLog;
 import tk.blackwolf12333.grieflog.utils.searching.ArgumentParser;
 
 public class UndoSerializer {
 	ArrayList<ArgumentParser> arguments = new ArrayList<ArgumentParser>();
 
 	public ArrayList<ArgumentParser> getArguments() {
 		return arguments;
 	}
 	
 	public void save() {
 		GriefLog plugin = (GriefLog) Bukkit.getPluginManager().getPlugin("GriefLog");
 		File undo = new File(plugin.getDataFolder(), "undo.dat");
 		try {
 			if(!undo.exists()) {
 				undo.getParentFile().mkdirs();
 				undo.createNewFile();
 			}
 			FileOutputStream fileOut = new FileOutputStream(undo);
 			ObjectOutputStream out = new ObjectOutputStream(fileOut);
 			out.writeObject(arguments);
 			out.close();
 			fileOut.close();
 		} catch (IOException i) {
 			i.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void load() {
 		GriefLog plugin = (GriefLog) Bukkit.getPluginManager().getPlugin("GriefLog");
 		try {
 			FileInputStream fileIn = new FileInputStream(new File(plugin.getDataFolder(), "undo.dat"));
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			this.arguments = (ArrayList<ArgumentParser>) in.readObject();
 			in.close();
 			fileIn.close();
 		} catch (IOException i) {
			if(i instanceof FileNotFoundException) {
				return;
			}
 			i.printStackTrace();
 		} catch (ClassNotFoundException c) {
 			c.printStackTrace();
 		}
 	}
 }
