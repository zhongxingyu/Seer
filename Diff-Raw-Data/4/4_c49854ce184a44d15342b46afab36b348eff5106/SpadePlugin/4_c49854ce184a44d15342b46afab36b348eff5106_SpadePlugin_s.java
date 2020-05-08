 package net.nexisonline.spade;
 
 import java.util.HashMap;
 
 import net.nexisonline.spade.chunkproviders.ChunkProviderFlatGrass;
 import net.nexisonline.spade.chunkproviders.ChunkProviderMountains;
 import net.nexisonline.spade.chunkproviders.ChunkProviderStock;
 import net.nexisonline.spade.commands.SetWorldGenCommand;
 import net.nexisonline.spade.commands.TP2WorldCommand;
 
 import org.bukkit.ChunkProvider;
 import org.bukkit.World.Environment;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Sample plugin for Bukkit
  *
  * @author Dinnerbone
  */
 public class SpadePlugin extends JavaPlugin {
     private final SpadeWorldListener worldListener = new SpadeWorldListener(this);
 	private HashMap<String,ChunkProvider> chunkProviders = new HashMap<String,ChunkProvider>();
 
     public void onEnable() {
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.WORLD_LOAD, worldListener, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.WORLD_SAVE, worldListener, Event.Priority.Monitor, this);
         
         // Register our commands
         getCommand("setworldgen").setExecutor(new SetWorldGenCommand(this));
         getCommand("tpw").setExecutor(new TP2WorldCommand(this));
         
         registerChunkProviders();
 
         // Load World Settings
         worldListener.loadWorlds();
         
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         
         
     }
     private void registerChunkProviders() {
 		chunkProviders.put("stock", new ChunkProviderStock());
 		chunkProviders.put("flatgrass", new ChunkProviderFlatGrass());
 		chunkProviders.put("mountains", new ChunkProviderMountains());
 	}
 	public void onDisable() {
     }
     
 	public void loadWorld(String worldName, String cmName, String cpName) {
 		ChunkProvider cp = chunkProviders.get(cpName);
		getServer().createWorld(worldName, Environment.NORMAL, null, cp);
 	}
 
 }
