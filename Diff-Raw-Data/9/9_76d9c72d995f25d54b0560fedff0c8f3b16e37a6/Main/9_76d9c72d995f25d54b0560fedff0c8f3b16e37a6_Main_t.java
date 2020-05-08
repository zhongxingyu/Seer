 package me.EnderSlayerHD.com;
 
 import java.io.File;
 
 import me.EnderSlayerHD.com.util.Basics;
 import me.EnderSlayerHD.com.util.Chatevent;
 import me.EnderSlayerHD.com.util.Disablemobspawn;
 import me.EnderSlayerHD.com.util.Disablerain;
 import me.EnderSlayerHD.com.util.Droppickupitems;
 import me.EnderSlayerHD.com.util.Joingame;
 import me.EnderSlayerHD.com.util.Timeday;
 import me.EnderSlayerHD.com.util.Worldinteract;
 import me.EnderSlayerHD.com.util.playerrespawn;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerToggleFlightEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin implements Listener{
 
 	
 	@Override
 public void onEnable() {
 	File config = new File(getDataFolder()+File.separator+"config.yml");
 	if (!config.exists()){
 		this.getLogger().info("Generating config.yml...");
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 	}
 	getConfig().options().copyDefaults(true);
     saveDefaultConfig();
     getServer().getPluginManager().registerEvents(this, this);
     getServer().getPluginManager().registerEvents(new Inv(this), this);
     getServer().getPluginManager().registerEvents(new IconMenu(this), this);
     getServer().getPluginManager().registerEvents(new Playerjoin(this), this);
     getServer().getPluginManager().registerEvents(new Disablerain(this), this);
     getServer().getPluginManager().registerEvents(new Disablemobspawn(this), this);
     getServer().getPluginManager().registerEvents(new Droppickupitems(this), this);
     getServer().getPluginManager().registerEvents(new Timeday(this), this);
     getServer().getPluginManager().registerEvents(new Worldinteract(this), this);
     getServer().getPluginManager().registerEvents(new playerrespawn(this), this);
     getServer().getPluginManager().registerEvents(new Chatevent(this), this);
     getServer().getPluginManager().registerEvents(new Joingame(this), this);
     Bukkit.getMessenger().registerOutgoingPluginChannel(this,  "BungeeCord");
     getCommand("setspawn").setExecutor(new Basics(this));
     getCommand("spawn").setExecutor(new Basics(this));
     getCommand("gmc").setExecutor(new com.gmail.firework4lj.commands.Basics(this));
     getCommand("gms").setExecutor(new com.gmail.firework4lj.commands.Basics(this));
     getCommand("msg").setExecutor(new com.gmail.firework4lj.commands.Basics(this));
     getLogger().info("==============================================");
    getLogger().info("Hub has been enabled!");
    getLogger().info("Owned by Firework4lj.");
     getLogger().info("Plugin created by firework4lj and _EnderSlayerHD.");
     getLogger().info("==============================================");
     int seconds = 4800;
     Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
     	public void run(){
     		Bukkit.getServer().getWorld("world").setTime(0);
     		Bukkit.broadcastMessage(ChatColor.RED+"Keep this server running! Donate for packages at http://enderdragonnetwork.buycraft.net/category/0");
     	}
     }, 0, (seconds));
   }
 	@Override
 public void onDisable() {
     getLogger().info("==============================================");
    getLogger().info("Hub has been disabled!");
	getLogger().info("Owned by Firework4lj.");
 	getLogger().info("Plugin created by firework4lj and _EnderSlayerHD.");
 	getLogger().info("==============================================");
 }
   @EventHandler(priority=EventPriority.HIGH)
   public void onPlayerJoin(PlayerJoinEvent event) {
     Player p = event.getPlayer();
 
     if (getConfig().getString("FasterWalkingSpeed").equalsIgnoreCase("true"))
       p.setWalkSpeed(0.25F);
   }
   
   @EventHandler
   public void onEntityDamage(EntityDamageEvent e)
   {
     if (((e.getEntity() instanceof Player)) && (e.getCause() == EntityDamageEvent.DamageCause.FALL) || (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)){
       e.setCancelled(true);
     }else if(((e.getEntity() instanceof Player)) && (e.getCause() == EntityDamageEvent.DamageCause.VOID)){
     Player p = (Player) e.getEntity();
     Location spawn = new Location(Bukkit.getServer().getWorld("world"), getConfig().getDouble("spawn.x"), getConfig().getDouble("spawn.y"), getConfig().getDouble("spawn.z"));
     p.teleport(spawn);
     p.setHealth(20);
     }
   }
 
   @EventHandler
   public void onMove(PlayerMoveEvent event){
     if ((event.getPlayer().getGameMode() != GameMode.CREATIVE) && (event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)){
       event.getPlayer().setAllowFlight(true);
       event.getPlayer().setExp(1.0F);
     }
   }
   
   @EventHandler
   public void onFly(PlayerToggleFlightEvent event){
     Player player = event.getPlayer();
     if (player.getGameMode() != GameMode.CREATIVE) {
       event.setCancelled(true);
       player.setAllowFlight(false);
       player.setFlying(false);
       player.setVelocity(player.getLocation().getDirection().multiply(1.6D).setY(1.0D));
       player.setExp(0.0F);
 
       if (getConfig().getString("TakeoffSound").equalsIgnoreCase("true")) {
         player.getLocation().getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1.0F, -5.0F);
       }
       if (getConfig().getString("CloudEffect").equalsIgnoreCase("true"))
         for (Player p : Bukkit.getOnlinePlayers())
           try {
             ParticleEffects.CLOUD.sendToPlayer(p, player.getLocation(), 1.0F, 1.0F, 1.0F, 1.0F, 40);
             player.setExp(0.0F);
           } catch (Exception e) {
             e.printStackTrace();
           }
     }
   }
 }
