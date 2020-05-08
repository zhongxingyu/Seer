 /**
  * LICENSING
  * 
  * This software is copyright by sunkid <sunkid@iminurnetz.com> and is
  * distributed under a dual license:
  * 
  * Non-Commercial Use:
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Commercial Use:
  *    Please contact sunkid@iminurnetz.com
  */
 package com.iminurnetz.bukkit.plugin.armageddon;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.block.BlockCanBuildEvent;
 import org.bukkit.event.entity.EntityEvent;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.PluginManager;
 
 import com.iminurnetz.bukkit.plugin.BukkitPlugin;
 import com.iminurnetz.bukkit.plugin.armageddon.arsenal.Grenade;
 import com.iminurnetz.bukkit.plugin.armageddon.arsenal.Gun;
 import com.iminurnetz.bukkit.plugin.armageddon.arsenal.Grenade.Type;
 import com.iminurnetz.bukkit.plugin.armageddon.listeners.ArmageddonBlockListener;
 import com.iminurnetz.bukkit.plugin.armageddon.listeners.ArmageddonEntityListener;
 import com.iminurnetz.bukkit.plugin.armageddon.listeners.ArmageddonPlayerListener;
 import com.iminurnetz.bukkit.plugin.armageddon.listeners.MoveCraftListener;
 import com.iminurnetz.bukkit.plugin.armageddon.tasks.EntityTracker;
 import com.iminurnetz.bukkit.plugin.armageddon.tasks.WaterTracker;
 import com.iminurnetz.bukkit.plugin.util.MessageUtils;
 import com.iminurnetz.bukkit.util.BlockLocation;
 import com.iminurnetz.bukkit.util.InventoryUtil;
 import com.iminurnetz.bukkit.util.MaterialUtils;
 
 public class ArmageddonPlugin extends BukkitPlugin {
 
     private Hashtable<BlockLocation, Cannon> cannons;
     private Hashtable<String, PlayerSettings> playerSettings;
     private List<TrackedLivingEntity> trackedEntities;
     private Hashtable<Integer, WaterTracker> waterTrackers;
 
     private int stunnerTaskId = -1;
 
     private ArmageddonConfiguration config;
     private ArmageddonPermissionHandler permissionHandler;
 
     private Hashtable<Integer, Grenade> grenadesFired;
     private List<Location> nuclearExplosions;
 
     private Hashtable<Integer, Integer> bulletsFired;
     private Hashtable<Integer, Block> blockShotAt;
 
     public static final int CANNON_FILE_VERSION = 2;
 
     @Override
     public void enablePlugin() throws Exception {
 
         grenadesFired = new Hashtable<Integer, Grenade>();
         trackedEntities = new ArrayList<TrackedLivingEntity>();
         waterTrackers = new Hashtable<Integer, WaterTracker>();
 
         nuclearExplosions = new ArrayList<Location>();
 
         bulletsFired = new Hashtable<Integer, Integer>();
         blockShotAt = new Hashtable<Integer, Block>();
 
         config = new ArmageddonConfiguration(this);
         permissionHandler = new ArmageddonPermissionHandler(this);
 
         loadCannonsFile();
 
         PluginManager pm = getServer().getPluginManager();
 
         ArmageddonBlockListener blockListener = new ArmageddonBlockListener(this);
 
         pm.registerEvent(Event.Type.BLOCK_DISPENSE, blockListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Monitor, this);
 
         ArmageddonPlayerListener playerListener = new ArmageddonPlayerListener(this);
 
         pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, playerListener, Priority.Monitor, this);
 
         ArmageddonEntityListener entityListener = new ArmageddonEntityListener(this);
 
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.ENTITY_INTERACT, entityListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.PROJECTILE_HIT, entityListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.CREEPER_POWER, entityListener, Priority.Highest, this);
 
         if (pm.getPlugin("MoveCraft") != null) {
             MoveCraftListener moveCraftListener = new MoveCraftListener();
             pm.registerEvent(Event.Type.CUSTOM_EVENT, moveCraftListener, Priority.Monitor, this);
         }
 
         log("enabled.");
     }
 
     @Override
     public void onDisable() {
         if (stunnerTaskId > 0) {
             log("Cancelling task " + stunnerTaskId);
             getServer().getScheduler().cancelTask(stunnerTaskId);
             stunnerTaskId = -1;
         }
 
         for (Integer i : waterTrackers.keySet()) {
             getServer().getScheduler().cancelTask(i);
         }
 
         saveCannonsFile();
         log("disabled.");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         final Player player;
         if (sender instanceof Player) {
             player = (Player) sender;
         } else {
             sender.sendMessage("This command is only accessible in-game");
             return false;
         }
 
         if (command.getName().equals("cb")) {
             if (!permissionHandler.canConfigure(player)) {
                 MessageUtils.send(player, ChatColor.RED, "You are not allowed to use this command!");
                 return true;
             }
 
             if (args.length == 0) {
                 showCannonData(player);
                 return true;
             } else if (args.length > 3) {
                 return false;
             }
 
             if (playerSettings.get(player.getName()) == null) {
                 playerSettings.put(player.getName(), new PlayerSettings(getDefaultCannon(), getDefaultGun()));
             }
 
             Cannon cannon = playerSettings.get(player.getName()).getCannon();
             boolean success = true;
 
             if (args.length == 3) {
                 try {
                     cannon.setFuse(Integer.parseInt(args[2]));
                 } catch (NumberFormatException e) {
                     success = false;
                 }
             }
 
             if (args.length > 1) {
                 try {
                     cannon.setVelocity(Double.parseDouble(args[1]));
                 } catch (NumberFormatException e) {
                     success = false;
                 }
             }
 
             try {
                 cannon.setAngle(Double.parseDouble(args[0]));
             } catch (NumberFormatException e) {
                 success = false;
             }
 
             if (!success) {
                 return false;
             }
 
             MessageUtils.send(player, ChatColor.RED, "Your settings were changed:");
             MessageUtils.send(player, ChatColor.RED, cannon.toString());
             return true;
         }
 
         return false;
     }
 
     @SuppressWarnings("unchecked")
     private void loadCannonsFile() {
 
         cannons = new Hashtable<BlockLocation, Cannon>();
         playerSettings = new Hashtable<String, PlayerSettings>();
 
         int fileVersion;
 
         File cache = getCannonsFile();
         if (!cache.exists()) {
             return;
         }
 
         FileInputStream fis;
         ObjectInputStream in;
         try {
             fis = new FileInputStream(cache);
             in = new ObjectInputStream(fis);
         } catch (IOException e) {
             log(Level.SEVERE, "Cannot load cached cannons and settings, starting from scratch", e);
             return;
         }
 
         try {
             fileVersion = in.readInt();
         } catch (IOException e) {
             log("Unknown cannon file version. Upgrading!");
             fileVersion = 0;
         }
 
         try {
             cannons = (Hashtable<BlockLocation, Cannon>) in.readObject();
             if (fileVersion < CANNON_FILE_VERSION) {
                 for (BlockLocation location : cannons.keySet()) {
                     Cannon c = safeClone(cannons.get(location));
                     cannons.put(location, c);
                 }
             }
         } catch (Exception e) {
             log(Level.SEVERE, "Cannon file corrupted; using default cannon settings");
             cannons = new Hashtable<BlockLocation, Cannon>();
         }
 
         try {
             playerSettings = (Hashtable<String, PlayerSettings>) in.readObject();
             if (fileVersion < CANNON_FILE_VERSION) {
                 for (String player : playerSettings.keySet()) {
                     PlayerSettings s = playerSettings.get(player);
                     Cannon c = safeClone(s.getCannon());
                     playerSettings.put(player, new PlayerSettings(c, getDefaultGun()));
                 }
             }
         } catch (Exception e) {
             log(Level.SEVERE, "Cannon file corrupted; using default player settings");
             playerSettings = new Hashtable<String, PlayerSettings>();
         }
 
         try {
             in.close();
             fis.close();
         } catch (IOException e) {
             log(Level.SEVERE, "Cannot properly close cannons file", e);
         }
     }
 
     private Cannon safeClone(Cannon cannon) {
         return new Cannon(cannon.getAngle(), cannon.getVelocity(), cannon.getFuse());
     }
 
     private File getCannonsFile() {
         return new File(this.getDataFolder(), "cannons.ser");
     }
 
     private void saveCannonsFile() {
         File cache = getCannonsFile();
 
         File dataDir = cache.getParentFile();
         if (!dataDir.exists()) {
             dataDir.mkdirs();
         }
 
         try {
             FileOutputStream fos = new FileOutputStream(cache);
             ObjectOutputStream out = new ObjectOutputStream(fos);
             out.writeInt(CANNON_FILE_VERSION);
             out.writeObject(cannons);
             out.writeObject(playerSettings);
             out.close();
             fos.close();
         } catch (Exception e) {
             log(Level.SEVERE, "Cannot cache cannons and settings", e);
         }
     }
 
     public Cannon getCannon(Block block, boolean createIfNotExist) {
         BlockLocation location = new BlockLocation(block);
         synchronized (cannons) {
             if (!cannons.containsKey(location) && createIfNotExist) {
                 Cannon cannon = getDefaultCannon();
                 cannons.put(location, cannon);
             }
 
             return cannons.get(location);
         }
     }
 
     public Cannon getCannon(Player player) {
         PlayerSettings settings = getPlayerSettings(player, true);
         return settings.getCannon();
     }
 
     public PlayerSettings getPlayerSettings(Player player, boolean alertPlayer) {
         if (playerSettings == null) {
             log(Level.SEVERE, "player settings corrupted, starting over");
             playerSettings = new Hashtable<String, PlayerSettings>();
         }
 
         synchronized (playerSettings) {
             String name = player.getName();
             if (!playerSettings.containsKey(name)) {
                 playerSettings.put(name, new PlayerSettings(getDefaultCannon(), getDefaultGun()));
                 if (alertPlayer) {
                     MessageUtils.send(player, ChatColor.RED, "Default cannon settings used!\n" + getHelpText());
                 }
             }
             return playerSettings.get(name);
         }
     }
 
     public String getHelpText() {
         return "Use '/cb [angle [velocity [fuse]]]' to configure your settings!\n" + "Display a cannon's settings by left-clicking it bare handed\n" + "Toggle dispensers/cannons by left-clicking with red stone dust\n" + "Set a cannon to your settings by left-clicking it with a torch\n";
     }
 
     public Cannon getDefaultCannon() {
         return new Cannon(getConfig().getAngle(), getConfig().getVelocity(), getConfig().getFuse());
     }
 
     public Gun getDefaultGun() {
         return ArmageddonConfiguration.DEFAULT_GUN;
     }
 
     public void showCannonData(Player player) {
         Cannon cannon = getCannon(player);
         MessageUtils.send(player, cannon.toString());
     }
 
     public ArmageddonConfiguration getConfig() {
         return config;
     }
 
     public ArmageddonPermissionHandler getPermissionHandler() {
         return permissionHandler;
     }
 
     public void removeCannon(Block block) {
         BlockLocation location = new BlockLocation(block);
         cannons.remove(location);
     }
 
     public boolean isCannon(Block block) {
         BlockLocation location = new BlockLocation(block);
         return cannons.containsKey(location);
     }
 
     public void registerGrenade(Entity projectile, Grenade grenade) {
         grenadesFired.put(projectile.getEntityId(), grenade);
     }
 
     public boolean isGrenade(Entity projectile) {
         return projectile != null && grenadesFired.containsKey(projectile.getEntityId());
     }
 
     public List<TrackedLivingEntity> getTrackedEntities() {
         return trackedEntities;
     }
 
     public TrackedLivingEntity getTrackedEntity(LivingEntity entity) {
         TrackedLivingEntity victim = new TrackedLivingEntity(entity);
         if (trackedEntities.contains(victim)) {
             return trackedEntities.get(trackedEntities.indexOf(victim));
         }
         return null;
     }
 
     public boolean doCancelIfNeccessary(Event event) {
         if (trackedEntities.size() == 0 || !(event instanceof Cancellable)) {
             return false;
         }
 
         LivingEntity entity = null;
         if (event instanceof PlayerEvent) {
             entity = ((PlayerEvent) event).getPlayer();
         } else if (event instanceof EntityEvent && ((EntityEvent) event).getEntity() instanceof LivingEntity) {
             entity = (LivingEntity) ((EntityEvent) event).getEntity();
         } else {
             return false;
         }
 
         TrackedLivingEntity victim = getTrackedEntity(entity);
         if (victim == null) {
             return false;
         }
 
         if (victim.isStunned()) {
             ((Cancellable) event).setCancelled(true);
             if (entity instanceof Player && !(event instanceof PlayerMoveEvent)) {
                 MessageUtils.send((Player) entity, ChatColor.RED, "No can do, you're stunned!");
             } else if (event instanceof PlayerMoveEvent) { // shouldn't have to do this!
                 ((Cancellable) event).setCancelled(false);
                 ((PlayerMoveEvent) event).setTo(victim.getLocation());
             }
 
             return true;
         } else if (victim.isSnared()) {
             log(entity + " is snared");
             if (entity instanceof Player && (event instanceof PlayerTeleportEvent || event instanceof PlayerPortalEvent)) {
                 ((Cancellable) event).setCancelled(true);
                 MessageUtils.send((Player) entity, ChatColor.RED, "No can do, you're snared!");
                 return true;
             } else if (event instanceof PlayerMoveEvent) {
 
             }
         }
 
         if (victim.isDoused()) {
 
         }
 
         return false;
     }
 
     public Grenade getGrenade(Entity projectile) {
         if (!isGrenade(projectile)) {
             return ArmageddonConfiguration.DEFAULT_GRENADE;
         }
 
         return grenadesFired.get(projectile.getEntityId());
     }
 
     public boolean adjustInventoryAndUsage(Inventory inventory, UsageTracker settings, Material material, int uses) {
         if (settings.getUsage(material) <= 0) {
             settings.setUsage(material, uses);
         }
 
         settings.use(material);
 
         if (settings.getUsage(material) == 0) {
             if (inventory instanceof PlayerInventory) {
                 InventoryUtil.removeItemNearItemHeldInHand((PlayerInventory) inventory, material);
             } else {
                 inventory.removeItem(new ItemStack(material, 1));
             }
             if (material.name().endsWith("_BUCKET")) {
                 inventory.addItem(new ItemStack(Material.BUCKET, 1));
             }
             return true;
         }
 
         return false;
     }
 
     public void removeGrenade(Entity entity) {
         grenadesFired.remove(entity);
     }
 
     public void explodeGrenade(Entity entity) {
         if (!isGrenade(entity)) {
             return;
         }
 
         float yield = 0;
         boolean setFire = false;
 
         Grenade grenade = getGrenade(entity);
         if (grenade.getClusterSize() == 0) {
             return;
         }
 
         Location loc = entity.getLocation();
         // log("Action: " + action.getType() + ", " + action.getYield());
         removeGrenade(entity);
 
         Location locClone = loc.clone();
 
         World world = entity.getWorld();
 
         switch (grenade.getType()) {
             case WATER_BALLOON:
                 if (grenade.getClusterSize() == 1) {
                     Set<Block> before = convertSurfaceBlocks(loc, grenade);
 
                     if (before.size() != 0) {
                         WaterTracker tracker = new WaterTracker(this, before);
                         int trackerId = getServer().getScheduler().scheduleSyncDelayedTask(this, tracker, 120);
                         tracker.setId(trackerId);
                         waterTrackers.put(trackerId, tracker);
                     }
                 }
 
             case SNARE:
             case STUN:
                 if (grenade.getClusterSize() == 1) {
                     strike(entity, grenade);
                 }
                 break;
 
             case TNT:
             case EXPLOSIVE:
                 yield = grenade.getYield();
                 break;
 
             case MOLOTOV:
                 yield = grenade.getYield();
                 setFire = true;
                 break;
 
             case NUCLEAR:
                 yield = grenade.getYield();
                 setFire = true;
                 locClone.setPitch(0);
                 locClone.setYaw(0);
                 nuclearExplosions.add(locClone);
                 break;
 
             case SPIDER_WEB:
                 if (grenade.getClusterSize() == 1) {
                     convertSurfaceBlocks(loc, grenade);
                 }
                 break;
 
             default:
                 return;
         }
 
         if (grenade.getClusterSize() > 1) {
             Random random = new Random((long) (loc.lengthSquared() * loc.getPitch() * loc.getYaw()));
             Grenade g = new Grenade(grenade.getType(), 1, grenade.getYield());
             for (int n = 0; n < grenade.getClusterSize(); n++) {
                 locClone.setPitch(-45);
                 locClone.setYaw(random.nextFloat() * 360);
                 Snowball cb = world.spawn(loc, Snowball.class);
                 cb.setVelocity(locClone.getDirection().multiply(random.nextDouble() * .6));
                 registerGrenade(cb, g);
             }
             yield = 0;
             setFire = false;
         }
 
         world.createExplosion(entity.getLocation(), yield, setFire);
 
     }
 
     private Set<Block> convertSurfaceBlocks(Location loc, Grenade grenade) {
         Material material = grenade.getType() == Type.WATER_BALLOON ? Material.WATER : Material.WEB;
         Set<Block> changedSet = new HashSet<Block>();
         for (Block block : getSurfaceBlocks(loc, grenade.getYield())) {
             if (MaterialUtils.isWater(block.getRelative(BlockFace.DOWN).getType())) {
                 continue;
             }
 
             BlockCanBuildEvent e = new BlockCanBuildEvent(block, material.getId(), true);
             getServer().getPluginManager().callEvent(e);
             if (e.isBuildable()) {
                 changedSet.add(block);
                 block.setType(material);
             }
 
         }
 
         return changedSet;
     }
 
     public void strike(Entity entity, Grenade grenade) {
         float yield = grenade.getYield();
         synchronized (trackedEntities) {
             for (Entity e : entity.getNearbyEntities(yield, yield, yield)) {
                 if (!(e instanceof LivingEntity)) {
                     continue;
                 }
 
                 TrackedLivingEntity victim = new TrackedLivingEntity((LivingEntity) e);
                 if (trackedEntities.contains(victim)) {
                     // log("Now with more stunning!");
                     victim = trackedEntities.get(trackedEntities.indexOf(victim));
                 } else {
                     trackedEntities.add(victim);
                 }
 
                 log("Inflicting " + grenade.getType() + " on " + e);
                 switch (grenade.getType()) {
                     case SNARE:
                         victim.snare(config.getStunTime());
                         break;
 
                     case STUN:
                         victim.stun(config.getStunTime());
                         break;
 
                     case WATER_BALLOON:
                         // victim.douse(config.getStunTime());
                         break;
                 }
             }
 
             if (trackedEntities.size() != 0 && stunnerTaskId == -1) {
                 stunnerTaskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new EntityTracker(this), 0, 1);
                 log("Scheduled new task " + stunnerTaskId);
             }
         }
     }
 
     // return all surface blocks in a sphere with diameter yield around location
     private Set<Block> getSurfaceBlocks(Location location, float yield) {
         Set<Block> blocks = new HashSet<Block>();
         Block hitBlock = location.getBlock();
         addSurfaceBlocks(hitBlock, location, yield, blocks, new HashSet<Block>());
         return blocks;
     }
 
     // recursively add surface blocks to a set if they are within distance
     private void addSurfaceBlocks(final Block block, Location location, float distance, Set<Block> blocks, Set<Block> visited) {
         if (visited.contains(block) || block.getLocation().distance(location) > distance) {
             return;
         }
 
         searchUpDown(block, location, distance, blocks, visited, BlockFace.DOWN);
         searchUpDown(block, location, distance, blocks, visited, BlockFace.UP);
         visited.add(block);
 
         for (BlockFace face : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
             Block b = block.getRelative(face);
             addSurfaceBlocks(b, location, distance, blocks, visited);
         }
 
     }
 
     private void searchUpDown(final Block block, Location location, float distance, Set<Block> blocks, Set<Block> visited, BlockFace direction) {
         Material m = block.getType();
         Block b = block;
         boolean didSearch = false;
 
         while (m == Material.AIR && b.getLocation().distance(location) < distance) {
             b = b.getRelative(direction);
             m = b.getType();
             visited.add(b);
             didSearch = true;
         }
 
         if (m != Material.AIR && didSearch) {
             blocks.add(b.getRelative(direction.getOppositeFace()));
         }
     }
 
     public void goNuclear(Location location, List<Block> blocks) {
         if (nuclearExplosions.remove(location)) {
             for (Block target : blocks) {
                 for (BlockFace dir : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN)) {
                     Block neighbor = target.getRelative(dir);
                     if (!blocks.contains(neighbor)) {
                         if (ArmageddonConfiguration.MELTABLE.contains(neighbor.getType())) {
                             neighbor.setType(Material.LAVA);
                         } else if (ArmageddonConfiguration.BURNABLE.contains(neighbor.getType())) {
                             neighbor.setType(Material.FIRE);
                         }
                     }
                 }
             }
         }
     }
 
     public void removeWaterTracker(int trackerId) {
         waterTrackers.remove(trackerId);
     }
 
     public void addBlockFlow(Block from, Block to) {
         synchronized (waterTrackers) {
             for (Integer i : waterTrackers.keySet()) {
                 waterTrackers.get(i).addBlock(from, to);
             }
         }
     }
 
     public Gun getGun(Player player) {
         Gun gun = getDefaultGun();
         if (getConfig().isGunItem(player.getItemInHand().getType())) {
             PlayerInventory inv = player.getInventory();
             if (inv.getHeldItemSlot() < 8) {
                 ItemStack i = inv.getItem(inv.getHeldItemSlot() + 1);
                 if (i != null) {
                     gun = getConfig().getGun(i.getType());
                 }
             }
 
             if ((gun.getType() == getDefaultGun().getType() || !permissionHandler.canShoot(player, gun)) && inv.getHeldItemSlot() > 0) {
                 ItemStack i = inv.getItem(inv.getHeldItemSlot() - 1);
                 if (i != null) {
                     gun = getConfig().getGun(i.getType());
                 }
             }
 
             // out of ammo
             if (gun.getType() == getDefaultGun().getType()) {
                playerSettings.get(player.getName()).setGun(gun);
                 return gun;
             }
 
            PlayerSettings settings = getPlayerSettings(player, false);

             // see if we reloaded
             Gun playerGun = settings.getGun();
             if (playerGun != null && playerGun.getShotsFired() == 0) {
                 playerGun = gun;
             }
 
             if (permissionHandler.canShoot(player, gun)) {
                 return gun;
             }
         }
         return getDefaultGun();
     }
 
     public void registerGunShot(Entity entity, Gun gun, Block block) {
         bulletsFired.put(entity.getEntityId(), gun.getDamage());
         blockShotAt.put(entity.getEntityId(), block);
     }
 
     public boolean isBullet(Entity projectile) {
         return bulletsFired.containsKey(projectile.getEntityId());
     }
 
     public void removeBullet(Entity projectile) {
         removeBullet(projectile.getEntityId());
     }
 
     public void removeBullet(Integer i) {
         bulletsFired.remove(i);
         blockShotAt.remove(i);
     }
 
     public Block getBlockShotAt(Entity projectile) {
         return blockShotAt.get(projectile.getEntityId());
     }
 
     public int getBulletDamage(Entity projectile) {
         if (isBullet(projectile)) {
             return bulletsFired.get(projectile.getEntityId());
         }
         return 0;
     }
 
 }
