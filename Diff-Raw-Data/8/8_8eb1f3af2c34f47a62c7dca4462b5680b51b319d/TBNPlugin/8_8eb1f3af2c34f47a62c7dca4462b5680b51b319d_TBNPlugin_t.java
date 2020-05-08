 package fr.aumgn.tobenamed;
 
import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.google.gson.Gson;
 import com.google.gson.stream.JsonReader;
 
 import fr.aumgn.bukkit.command.Commands;
 import fr.aumgn.tobenamed.command.GeneralCommands;
 import fr.aumgn.tobenamed.command.InfoCommands;
 import fr.aumgn.tobenamed.command.JoinStageCommands;
 import fr.aumgn.tobenamed.command.SpectatorsCommands;
 import fr.aumgn.tobenamed.listeners.GameListener;
 import fr.aumgn.tobenamed.listeners.RegionsListener;
 import fr.aumgn.tobenamed.listeners.SpectatorsListener;
 
 public class TBNPlugin extends JavaPlugin {
 
     public void onEnable() {
         TBN.init(this);
 
         PluginManager pm = Bukkit.getPluginManager();
         pm.registerEvents(new GameListener(), this);
         pm.registerEvents(new RegionsListener(), this);
         pm.registerEvents(new SpectatorsListener(), this);
 
         Commands.register(new GeneralCommands());
         Commands.register(new InfoCommands());
         Commands.register(new JoinStageCommands());
         Commands.register(new SpectatorsCommands());
 
         getLogger().info("Enabled.");
     }
 
     public void onDisable() {
         getLogger().info("Disabled.");
     }
 
     public TBNConfig loadTBNConfig() throws IOException {
         File folder = getDataFolder();
         if (!folder.exists()) {
             folder.mkdirs();
         }
 
         File file = new File(getDataFolder(), "config.json");
         Gson gson = TBN.getGson();
         TBNConfig config;
         if (file.exists()) {
             JsonReader reader = new JsonReader(new FileReader(file));
             config = gson.fromJson(reader, TBNConfig.class);
             reader.close();
         } else {
             config = new TBNConfig();
             file.createNewFile();
         }
         // This ensures user file is updated with newer fields. 
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(gson.toJson(config, TBNConfig.class));
         writer.close();
 
         return config;
     }
 }
