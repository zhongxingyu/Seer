 package to.joe.j2mc.fun;
 
 import java.util.List;
 
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.fun.command.ClearInventoryCommand;
 import to.joe.j2mc.fun.command.ItemCommand;
 
 public class J2MC_Fun extends JavaPlugin implements Listener {
 
     public List<Integer> blockedForTrusted;
     public List<Integer> blockedForNormals;
     public List<Integer> summonBlackList;
     public List<Integer> summonWatchList;
 
     @Override
     public void onDisable() {
         this.getLogger().info("Fun module disabled.");
     }
 
     @Override
     public void onEnable() {
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
         this.blockedForTrusted = this.getConfig().getIntegerList("blockfortrusted");
         this.blockedForNormals = this.getConfig().getIntegerList("blockfornormals");
         this.summonBlackList = this.getConfig().getIntegerList("summonblacklist");
         this.summonWatchList = this.getConfig().getIntegerList("summonwatchlist");
         if (this.getConfig().getBoolean("OMGFREEFLIGHT")) {
             this.getServer().getPluginManager().registerEvents(this, this);
             for (final Player player : this.getServer().getOnlinePlayers()) {
                 this.setFlight(player);
             }
         }
         this.getCommand("ci").setExecutor(new ClearInventoryCommand(this));
         this.getCommand("item").setExecutor(new ItemCommand(this));
 
         this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
         this.getLogger().info("Fun module enabled!");
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent event) {
         this.setFlight(event.getPlayer());
     }
    
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
    	this.setFlight(event.getPlayer());
    }
 
     public void setFlight(Player player) {
         if (player.hasPermission("j2mc.fun.trusted")) {
             if (player.getGameMode() != GameMode.CREATIVE) {
                 player.setGameMode(GameMode.CREATIVE);
             }
         } else {
             if (player.getGameMode() == GameMode.CREATIVE) {
                 player.setGameMode(GameMode.SURVIVAL);
             }
             player.setAllowFlight(true);
         }
     }
 
 }
