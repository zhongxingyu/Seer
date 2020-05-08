 package fr.aumgn.diamondrush.listeners;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import fr.aumgn.bukkitutils.geom.Vector;
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.event.players.DRPlayerJoinEvent;
 import fr.aumgn.diamondrush.event.players.DRPlayerQuitEvent;
 import fr.aumgn.diamondrush.event.players.DRSpectatorJoinEvent;
 import fr.aumgn.diamondrush.event.team.DRTeamSpawnSetEvent;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.Team;
 
 public class GameListener implements Listener {
 
     private final Game game;
     private boolean handleMove = false;
     private Set<Player> playersInSpawn = new HashSet<Player>();
 
     public GameListener(DiamondRush diamondRush) {
         this.game = diamondRush.getGame();
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
 
         if (game.contains(player)) {
             game.updatePlayer(player);
             Team team = game.getTeam(player);
             initPlayer(team, player);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onJoinGame(DRPlayerJoinEvent event) {
         Player player = event.getPlayer();
         Team team = event.getTeam();
         if (team.getTotem() != null) {
             Vector pos = team.getTotem().getTeleportPoint();
             player.teleport(pos.toLocation(game.getWorld()));
         }
         initPlayer(team, player);
     }
 
     private void initPlayer(Team team, Player player) {
         String teamName = team.getTeamName(player);
         player.setDisplayName(teamName);
         if (teamName.length() > 16) {
             player.setPlayerListName(teamName.substring(0, 16));
         } else {
             player.setPlayerListName(teamName);
         }
         if (team.getSpawn() != null) {
             Location spawnLoc = team.getSpawn().getMiddle().
                     toLocation(game.getWorld());
             player.setCompassTarget(spawnLoc);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onQuitGame(DRPlayerQuitEvent event) {
         Player player = event.getPlayer();
         // Ugly..
        String name = ChatColor.stripColor(player.getDisplayName());
        player.setDisplayName(name);
        player.setPlayerListName(name);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onSpectatorJoin(DRSpectatorJoinEvent event) {
         Player player = event.getPlayer();
         if (game.contains(player)) {
             event.setCancelled(true);
             player.sendMessage(ChatColor.RED + "Vous êtes déjà dans la partie.");
         }
     }
 
     @EventHandler
     public void onRespawn(PlayerRespawnEvent event) {
         Player player = event.getPlayer();
         if (!game.contains(player)) {
             return;
         }
 
         event.setRespawnLocation(game.getRespawnFor(player));
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onSpawnSetEvent(DRTeamSpawnSetEvent event) {
         for (Player player : event.getTeam().getPlayers()) {
             Location target = event.getRegion().getMiddle()
                     .toLocation(game.getWorld());
             player.setCompassTarget(target);
         }
     }
 
     @EventHandler
     public void onMove(PlayerMoveEvent event) {
         if (!handleMove) {
             return;
         }
 
         Player player = event.getPlayer();
         if (!playersInSpawn.contains(player)) {
             return;
         }
 
         if (!game.contains(player)) {
             playersInSpawn.remove(player);
             handleMove = playersInSpawn.size() > 0;
         }
 
         Location from = event.getFrom();
         Location to = event.getTo();
         if (from.getBlockX() == to.getBlockX()
                 && from.getBlockY() == to.getBlockY()
                 && from.getBlockZ() == to.getBlockZ()
                 && to.getWorld().equals(game.getWorld())) {
             return;
         }
 
         Vector toPos = new Vector(to);
         if (!game.getTeam(player).getSpawn().contains(toPos)) {
             playersInSpawn.remove(player);
             handleMove = playersInSpawn.size() > 0;
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onDamage(EntityDamageEvent event) {
         Entity targetEntity = event.getEntity();
 
         if (!(targetEntity instanceof Player)) {
             return;
         }
 
         Player target = (Player) targetEntity;
         if (playersInSpawn.contains(target)) {
             event.setCancelled(true);
             return;
         }
 
         if (!(event instanceof EntityDamageByEntityEvent)) {
             return;
         }
 
         Entity damagerEntity = ((EntityDamageByEntityEvent) event).getDamager();
         if (!(damagerEntity instanceof Player)) {
             return;
         }
 
         Player damager = (Player) damagerEntity;
         if ((game.contains(target) != game.contains(damager))) {
             event.setCancelled(true);
         }
     }
 }
