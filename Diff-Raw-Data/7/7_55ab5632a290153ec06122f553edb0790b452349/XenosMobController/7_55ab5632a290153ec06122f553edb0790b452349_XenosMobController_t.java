 package net.xenosmc.mob;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.world.ChunkUnloadEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 public class XenosMobController extends JavaPlugin {
 
     private HashMap<String, SpawnPoint> spawnPoints = new HashMap<String, SpawnPoint>();
     private BukkitTask scheduler = null;
     private LinkedList<SpawnPoint> enabled = new LinkedList<SpawnPoint>();
     private SpawnPoint spawningPoint = null;
 
     public void onEnable() {
         saveDefaultConfig();
         getConfig().options().copyDefaults(true);
         saveConfig();
         reloadConfig();
         this.getServer().getPluginManager().registerEvents(new EntityListener(this), this);
         this.getServer().getPluginCommand("xmc").setExecutor(new CommandHandler(this));
 
         this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
             @Override
             public void run() {
                 SpawnPoint next = enabled.pollFirst();
                 if (next == null) {
                     return; // bail if no enabled spawn points.
                 }
                 enabled.add(next);
                 if (getServer().getOnlinePlayers().length < next.getMinPlayers()) {
                     return; // bail early if there's not enough players online.
                 }
 
                 if (next.getLocation().getChunk().isLoaded()) {
                     if (!next.hasMob()) {
                         if (next.getLastDeathTime() + (next.getRespawnTimer() * 1000) < System.currentTimeMillis()) {
                             int rad = next.getSpawnRadiusSquared();
                             int count = 0;
                             for (Player p: getServer().getOnlinePlayers()) {
                                 double dist = p.getLocation().distanceSquared(next.getLocation());
                                 if (dist <= rad) {
                                     count++;
                                 }
                             }
                            debug(count + " players within " + rad + "(sq) [need min. " + next.getMinPlayers() + "]");
                             if (count >= next.getMinPlayers()) {
                                 spawnMob(next);
                             }
                         }
                     }
                 }
 
             }
         }, 1, 1);
     }
 
     public void spawnMob(SpawnPoint spawnPoint) {
         spawningPoint = spawnPoint;
         Entity entity = spawnPoint.getLocation().getWorld().spawnEntity(spawnPoint.getLocation(), spawnPoint.getEntityType());
         spawningPoint = null;
         entity.setMetadata("spawnPoint", new FixedMetadataValue(this, spawnPoint.getName()));
         if (entity instanceof LivingEntity) {
             ((LivingEntity) entity).setMaxHealth(spawnPoint.getHealth());
             ((LivingEntity) entity).setHealth(spawnPoint.getHealth());
         }
         ((LivingEntity) entity).setRemoveWhenFarAway(false);
         spawnPoint.setEntityId(entity.getUniqueId());
     }
 
     @Override
     public void reloadConfig() {
         super.reloadConfig();
         spawnPoints.clear();
         enabled.clear();
         ConfigurationSection pointCfg = getConfig().getConfigurationSection("spawn-points");
         if (pointCfg == null) {
             pointCfg = getConfig().createSection("spawn-points");
         }
         ConfigurationSection c;
         for (String pointName: pointCfg.getKeys(false)) {
             SpawnPoint p = new SpawnPoint(pointName);
             c = pointCfg.getConfigurationSection(pointName);
             p.setLocation(locationFromString(c.getString("location")));
             p.setEntityType(EntityType.valueOf(c.getString("entity-type")));
             p.setEnabled(c.getBoolean("enabled"));
             p.setHealth(c.getDouble("health"));
             p.setDamage(c.getDouble("damage"));
             p.setMinPlayers(c.getInt("min-players", 1));
             p.setSpawnRadius(c.getInt("spawn-radius", 30));
             p.setRespawnTimer(c.getLong("respawn-timer"));
             spawnPoints.put(p.getName().toLowerCase(), p);
             if (p.isEnabled()) {
                 enabled.add(p);
             }
         }
     }
 
     public String locationToString(Location loc) {
         if (loc == null) {
             return null;
         }
         return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
     }
 
     private Location locationFromString(String location) {
         if (location == null) {
             return null;
         }
         String[] parts = location.split(";");
         World w =getServer().getWorld(parts[0]);
         Location loc = null;
         try {
             loc = new Location(w, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
             if (parts.length >= 5) {
                 loc.setYaw(Float.parseFloat(parts[4]));
             }
             if (parts.length == 6) {
                 loc.setPitch(Float.parseFloat(parts[5]));
             }
         } catch (IllegalArgumentException ex) {
             return null;
         }
         return loc;
     }
 
     public HashMap<String, SpawnPoint> getSpawnPoints() {
         return spawnPoints;
     }
 
     public void enableSpawnPoint(SpawnPoint p) {
         p.setEnabled(true);
         enabled.add(p);
         getConfig().getConfigurationSection("spawn-points").getConfigurationSection(p.getName()).set("enabled", true);
         saveConfig();
     }
 
     public void disableSpawnPoint(SpawnPoint p) {
         p.setEnabled(false);
         enabled.remove(p);
         getConfig().getConfigurationSection("spawn-points").getConfigurationSection(p.getName()).set("enabled", false);
         saveConfig();
     }
 
     public void removeSpawnPoint(SpawnPoint p) {
         if (enabled.contains(p)) {
             enabled.remove(p);
         }
         getSpawnPoints().remove(p.getName().toLowerCase());
         getConfig().getConfigurationSection("spawn-points").set(p.getName(), null);
         saveConfig();
     }
 
 
     public void debug(String s) {
         if (getConfig().getBoolean("debug")) {
             getLogger().info(s);
         }
     }
 
     public void addSpawnPoint(SpawnPoint p) {
         getSpawnPoints().put(p.getName().toLowerCase(), p);
         if (p.isEnabled()) {
             enabled.add(p);
         }
         ConfigurationSection sect = getConfig().getConfigurationSection("spawn-points").createSection(p.getName());
         sect.set("enabled", p.isEnabled());
         sect.set("health", p.getHealth());
         sect.set("damage", p.getDamage());
         sect.set("location", locationToString(p.getLocation()));
         sect.set("respawn-timer", p.getRespawnTimer());
         sect.set("min-players", p.getMinPlayers());
         sect.set("entity-type", p.getEntityType().name());
         saveConfig();
     }
 
     public SpawnPoint getSpawningPoint() {
         return spawningPoint;
     }
 }
