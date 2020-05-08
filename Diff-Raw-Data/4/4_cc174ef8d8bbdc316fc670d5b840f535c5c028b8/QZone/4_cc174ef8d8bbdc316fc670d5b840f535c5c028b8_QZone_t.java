 package me.DDoS.Quarantine.zone;
 
 import me.DDoS.Quarantine.QLeaderboard;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import me.DDoS.Quarantine.QRewards;
 import me.DDoS.Quarantine.util.QUtil;
 import me.DDoS.Quarantine.player.QZonePlayer;
 import me.DDoS.Quarantine.Quarantine;
 import me.DDoS.Quarantine.player.QLobbyPlayer;
 import me.DDoS.Quarantine.player.QPlayer;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityCombustEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 
 /**
  *
  * @author DDoS
  */
 public class QZone {
 
     private QMainRegion region;
     private String zoneName;
     private Location lobby;
     private Location entrance;
     private int defaultMoney;
     private int maxNumOfPlayers;
     private int mobCheckTaskID;
     private boolean clearDrops;
     private boolean oneTimeKeys;
     private Map<Integer, Integer> kit;
     private List<QSubZone> subZones;
     private Map<CreatureType, QRewards> mobRewards;
     private QLeaderboard leaderboard;
     //
     private final Map<String, QPlayer> players = new HashMap<String, QPlayer>();
     private final Map<String, Integer> deadPlayers = new HashMap<String, Integer>();
 
     public QZone(QMainRegion region, String zoneName, Location lobby, Location entrance, int defaultMoney, int maxNumOfPlayers, boolean clearDrops, boolean oneTimeKeys,
             List<QSubZone> subZones, Map<Integer, Integer> kit, Map<CreatureType, QRewards> mobRewards, Quarantine plugin, World world, long interval) {
 
         this.region = region;
         this.zoneName = zoneName;
         this.lobby = lobby;
         this.entrance = entrance;
         this.defaultMoney = defaultMoney;
         this.maxNumOfPlayers = maxNumOfPlayers;
         this.clearDrops = clearDrops;
         this.oneTimeKeys = oneTimeKeys;
         this.kit = kit;
         this.subZones = subZones;
         this.mobRewards = mobRewards;
 
         if (QLeaderboard.USE) {
 
             leaderboard = new QLeaderboard(zoneName);
 
         }
 
         startMobCheckTask(plugin, interval);
 
     }
 
     public String getName() {
 
         return zoneName;
 
     }
 
     public void disconnectLB() {
 
         if (leaderboard == null) {
 
             return;
 
         }
 
         leaderboard.disconnect();
 
     }
 
     public QLeaderboard getLB() {
 
         return leaderboard;
 
     }
 
     public boolean checkForPlayer(String playerName) {
 
         return players.containsKey(playerName);
 
     }
 
     public Location getLobby() {
 
         return lobby;
 
     }
 
     public Location getEntrance() {
 
         return entrance;
 
     }
 
     public void setLobby(Location lobby) {
 
         this.lobby = lobby;
 
     }
 
     public boolean setEntrance(Location entrance) {
 
         if (!isInZone(entrance)) {
 
             return false;
 
         }
 
         this.entrance = entrance;
         return true;
 
     }
 
     public int getDefaultMoney() {
 
         return defaultMoney;
 
     }
 
     public Map<Integer, Integer> getKit() {
 
         return kit;
 
     }
 
     public boolean tellMoney(Player player) {
 
         QPlayer qPlayer = players.get(player.getName());
 
         if (qPlayer == null) {
 
             return false;
 
         }
 
         qPlayer.tellMoney();
         return true;
 
     }
 
     public boolean tellKeys(Player player) {
 
         QPlayer qPlayer = players.get(player.getName());
 
         if (qPlayer == null) {
 
             return false;
 
         }
 
         qPlayer.tellKeys();
         return true;
 
     }
 
     public boolean tellScoreAndRank(Player player) {
 
         QPlayer qPlayer = players.get(player.getName());
 
         if (qPlayer == null) {
 
             return false;
 
         }
 
         qPlayer.tellScoreAndRank();
         return true;
 
     }
 
     public boolean tellTopFive(Player player) {
 
         QPlayer qPlayer = players.get(player.getName());
 
         if (qPlayer == null) {
 
             return false;
 
         }
 
         qPlayer.tellTopFive();
         return true;
 
     }
 
     public boolean passPlayerTeleportEvent(PlayerTeleportEvent event) {
 
         QPlayer qPlayer = players.get(event.getPlayer().getName());
 
         if (qPlayer != null) {
 
             if (qPlayer.teleportLeave(event)) {
 
                 players.remove(event.getPlayer().getName());
 
                 if (players.isEmpty()) {
 
                     removeAllMobs();
 
                 }
             }
 
             return true;
 
         }
 
         return false;
 
     }
 
     public boolean passCreatureSpawnEvent(CreatureSpawnEvent event) {
 
         if (isInZone(event.getLocation())) {
 
             if (!event.getSpawnReason().equals(SpawnReason.CUSTOM)) {
 
                 event.setCancelled(true);
 
             }
 
             return true;
 
         }
 
         return false;
 
     }
 
     public boolean passEntityDeathEvent(LivingEntity entity, EntityDeathEvent event) {
 
         for (QSubZone subZone : subZones) {
 
             if (subZone.removeAndSpawnNewEntity(entity)) {
 
                 if (clearDrops) {
 
                     event.getDrops().clear();
 
                 }
 
                 Player player = getKiller(event.getEntity());
 
                 if (player != null) {
 
                     QPlayer qPlayer = players.get(player.getName());
 
                     if (qPlayer == null) {
 
                         return true;
 
                     }
 
                     if (!qPlayer.isZonePlayer()) {
 
                         return true;
 
                     }
 
                     QZonePlayer qzPlayer = (QZonePlayer) qPlayer;
                     CreatureType creature = QUtil.getEntityCreatureType(entity);
 
                     if (creature != null) {
 
                         QRewards rew = mobRewards.get(creature);
                         qzPlayer.addMoney(rew.getRandomMoneyAmount());
                         qzPlayer.addScore(rew.getScoreReward());
 
                     }
                 }
 
                 return true;
 
             }
         }
 
         return false;
 
     }
 
     public boolean passPlayerDeathEvent(Player player, EntityDeathEvent event) {
 
         QPlayer qPlayer = players.get(player.getName());
 
         if (qPlayer != null) {
 
             qPlayer.dieLeave(event);
             players.remove(player.getName());
 
             if (players.isEmpty()) {
 
                 removeAllMobs();
 
             }
 
             return true;
 
         }
 
         return false;
     }
 
     public boolean passPlayerRespawnEvent(PlayerRespawnEvent event, Quarantine plugin) {
 
         if (!deadPlayers.containsKey(event.getPlayer().getName())) {
 
             return false;
 
         }
 
         Player player = event.getPlayer();
         event.setRespawnLocation(lobby);
        QUtil.tell(player, "You lost.");
         QUtil.tell(player, "You may leave the lobby by teleporting away.");
         player.giveExp(deadPlayers.get(player.getName()));
         deadPlayers.remove(player.getName());
 
         return true;
 
     }
 
     public boolean passEntityCombustEvent(EntityCombustEvent event) {
 
         if (event.getEntity() instanceof LivingEntity) {
 
             LivingEntity ent = (LivingEntity) event.getEntity();
 
             for (QSubZone subZone : subZones) {
 
                 if (subZone.containsMob(ent)) {
 
                     event.setCancelled(true);
                     return true;
 
                 }
             }
         }
 
         return false;
 
     }
     
     public boolean passChunkUnloadEvent(ChunkUnloadEvent event) {
         
         if (!isInZone(event.getChunk())) {
             
             return false;
             
         }
         
         if (!players.isEmpty()) {
             
             event.setCancelled(true);
             
         }
 
         return true;
         
     }
 
     public boolean passPlayerInteractEvent(PlayerInteractEvent event) {
 
         if (!players.containsKey(event.getPlayer().getName())) {
 
             return false;
 
         }
         
         if (!event.hasBlock()) {
             
             return true;
             
         }
 
         QPlayer qPlayer = players.get(event.getPlayer().getName());
 
         if (!qPlayer.isZonePlayer()) {
 
             return true;
 
         }
 
         QZonePlayer qzPlayer = (QZonePlayer) qPlayer;
 
         if (!checkForSign(event.getClickedBlock())) {
 
             if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                     && !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
 
                 return true;
 
             }
 
             if (event.getClickedBlock().getType() != Material.STONE_BUTTON) {
 
                 return true;
 
             }
 
             if (!handleLock(qzPlayer, event.getClickedBlock())) {
 
                 event.setCancelled(true);
 
             }
 
             return true;
 
         }
 
         Sign sign = (Sign) event.getClickedBlock().getState();
 
         if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 
             return true;
 
         }
 
         if (sign.getLine(0).equalsIgnoreCase("[Quarantine]")) {
 
             handleZoneSign(qzPlayer, sign);
 
         }
 
         return true;
 
     }
 
     private boolean handleLock(QZonePlayer player, Block block) {
 
         Sign sign = getSignNextTo(block);
 
         if (sign != null) {
 
             return true;
 
         }
 
         if (sign.getLine(0).equalsIgnoreCase("[Quarantine]") && sign.getLine(1).equalsIgnoreCase("Key Lock")) {
 
             if (!player.useKey(sign.getLine(2), oneTimeKeys)) {
 
                 QUtil.tell(player.getPlayer(), "You need to purchase the key '" + sign.getLine(2) + "' to open this door.");
                 return false;
 
             }
         }
 
         return true;
 
     }
 
     private void handleZoneSign(QZonePlayer player, Sign sign) {
 
         if (sign.getLine(1).equalsIgnoreCase("Buy Item")) {
 
             String[] sa = sign.getLine(2).split("-");
             player.buyItem(Integer.parseInt(sa[0]), Integer.parseInt(sa[1]), Integer.parseInt(sa[2]));
             return;
 
         }
 
         if (sign.getLine(1).equalsIgnoreCase("Sell Item")) {
 
             String[] sa = sign.getLine(2).split("-");
             player.sellItem(Integer.parseInt(sa[0]), Integer.parseInt(sa[1]), Integer.parseInt(sa[2]));
             return;
 
         }
 
         if (sign.getLine(1).equalsIgnoreCase("Buy Key")) {
 
             player.addKey(sign.getLine(2), Integer.parseInt(sign.getLine(3)));
             return;
 
         }
         
         if (sign.getLine(1).equalsIgnoreCase("Enchantment")) {
 
             String[] sa = sign.getLine(2).split("-");
             player.addEnchantment(Integer.parseInt(sa[0]), Integer.parseInt(sa[1]), Integer.parseInt(sa[2]));
 
         }
     }
 
     private QPlayer getPlayer(Player player) {
 
         if (!players.containsKey(player.getName())) {
 
             return new QLobbyPlayer(player, this);
 
         }
 
         return players.get(player.getName());
 
     }
 
     public void joinPlayer(Player player) {
 
         if (players.size() >= maxNumOfPlayers) {
 
             QUtil.tell(player, "The zone is full.");
             return;
 
         }
 
         QPlayer qPlayer = getPlayer(player);
 
         if (qPlayer != null) {
 
             if (qPlayer.join()) {
 
                 players.put(player.getName(), qPlayer);
                 spawnStartingMobs();
 
             }
         }
     }
 
     public boolean enterPlayer(Player player) {
 
         QPlayer qPlayer = players.get(player.getName());
 
         if (qPlayer != null) {
 
             qPlayer.enter();
 
             if (!qPlayer.isZonePlayer()) {
 
                 players.remove(player.getName());
                 QZonePlayer qzPlayer = new QZonePlayer(qPlayer);
                 players.put(player.getName(), qzPlayer);
 
             }
 
             return true;
 
         }
 
         return false;
 
     }
 
     public boolean leavePlayer(Player player) {
 
         QPlayer qPlayer = players.get(player.getName());
 
         if (qPlayer != null) {
 
             if (qPlayer.leave()) {
 
                 players.remove(player.getName());
 
                 if (players.isEmpty()) {
 
                     removeAllMobs();
 
                 }
             }
 
             return true;
 
         }
 
         return false;
     }
 
     public void removeAllPlayers() {
 
         for (QPlayer player : players.values()) {
 
             player.forceLeave();
             removeAllMobs();
 
         }
     }
 
     public void saveLocations(FileConfiguration config) {
 
         ConfigurationSection configSec1 = config.getConfigurationSection("Zones." + zoneName);
 
         configSec1.set("lobby.x", lobby.getX());
         configSec1.set("lobby.y", lobby.getY());
         configSec1.set("lobby.z", lobby.getZ());
         configSec1.set("lobby.yaw", lobby.getYaw());
         configSec1.set("lobby.pitch", lobby.getPitch());
 
         configSec1.set("entrance.x", entrance.getX());
         configSec1.set("entrance.y", entrance.getY());
         configSec1.set("entrance.z", entrance.getZ());
         configSec1.set("entrance.yaw", entrance.getYaw());
         configSec1.set("entrance.pitch", entrance.getPitch());
 
         try {
 
             config.save("plugins/Quarantine/config.yml");
 
         } catch (IOException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't save config.");
             Quarantine.log.info("[Quarantine] Error message: " + ex.getMessage());
 
         }
     }
 
     public void reloadMobs() {
 
         for (QSubZone subzone : subZones) {
 
             subzone.removeAllMobs();
             subzone.spawnStartingMobs();
 
         }
     }
 
     public boolean isInZone(Location location) {
 
         return region.containsLocation(location);
 
     }
 
     public boolean isInZone(Chunk chunk) {
 
         return region.containsChunk(chunk);
 
     }
 
     private void spawnStartingMobs() {
 
         for (QSubZone subZone : subZones) {
 
             if (!subZone.hasMobs()) {
 
                 subZone.spawnStartingMobs();
 
             }
         }
     }
 
     private void removeAllMobs() {
 
         for (QSubZone subZone : subZones) {
 
             if (subZone.hasMobs()) {
 
                 subZone.removeAllMobs();
 
             }
         }
     }
 
     private void startMobCheckTask(Quarantine plugin, long interval) {
 
         mobCheckTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 
             @Override
             public void run() {
 
                 for (QSubZone subZone : subZones) {
 
                     subZone.checkForDeadMobs();
 
                 }
 
                 Quarantine.log.info("[Quarantine] Finished checking mobs.");
 
             }
         }, interval, interval);
 
         Quarantine.log.info("[Quarantine] Started mob check task for zone: " + zoneName);
 
     }
 
     public void stopMobCheckTask(Server server) {
 
         server.getScheduler().cancelTask(mobCheckTaskID);
 
         Quarantine.log.info("[Quarantine] Stopped mob check task for zone: " + zoneName);
 
     }
 
     private Sign getSignNextTo(Block block) {
 
         if (checkForSign(block.getRelative(BlockFace.UP))) {
 
             return (Sign) block.getRelative(BlockFace.UP).getState();
 
         }
 
         if (checkForSign(block.getRelative(BlockFace.DOWN))) {
 
             return (Sign) block.getRelative(BlockFace.DOWN).getState();
 
         }
 
         if (checkForSign(block.getRelative(BlockFace.EAST))) {
 
             return (Sign) block.getRelative(BlockFace.EAST).getState();
 
         }
 
         if (checkForSign(block.getRelative(BlockFace.WEST))) {
 
             return (Sign) block.getRelative(BlockFace.WEST).getState();
 
         }
 
         if (checkForSign(block.getRelative(BlockFace.NORTH))) {
 
             return (Sign) block.getRelative(BlockFace.NORTH).getState();
 
         }
 
         if (checkForSign(block.getRelative(BlockFace.SOUTH))) {
 
             return (Sign) block.getRelative(BlockFace.SOUTH).getState();
 
         }
 
         return null;
 
     }
 
     private boolean checkForSign(Block block) {
 
         switch (block.getType()) {
 
             case WALL_SIGN:
                 return true;
 
             case SIGN_POST:
                 return true;
 
             default:
                 return false;
 
         }
     }
 
     private Player getKiller(Entity ent) {
 
         Player player;
 
         EntityDamageEvent e1 = ent.getLastDamageCause();
         EntityDamageByEntityEvent e2 = (e1 instanceof EntityDamageByEntityEvent) ? (EntityDamageByEntityEvent) e1 : null;
         Entity damager = (e2 != null) ? e2.getDamager() : null;
         player = (e2 != null && damager instanceof Player) ? (Player) damager : null;
 
         if (player == null) {
 
             LivingEntity shooter = (e2 != null && damager instanceof Projectile) ? ((Projectile) damager).getShooter() : null;
             player = (shooter != null && shooter instanceof Player) ? (Player) shooter : null;
 
         }
 
         return player;
 
     }
 
     public void registerDeadPlayer(String playerName, int amount) {
 
         deadPlayers.put(playerName, amount);
 
     }
 }
