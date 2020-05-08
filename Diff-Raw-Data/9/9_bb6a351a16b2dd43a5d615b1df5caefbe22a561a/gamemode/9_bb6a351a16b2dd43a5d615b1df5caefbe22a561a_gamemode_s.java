 package com.Top_Cat.CODMW.gamemodes;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.minecraft.server.EntityItem;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkitcontrib.BukkitContrib;
 
 import com.Top_Cat.CODMW.main;
 import com.Top_Cat.CODMW.team;
 import com.Top_Cat.CODMW.Killstreaks.Killstreaks;
 import com.Top_Cat.CODMW.Killstreaks.killstreak;
 import com.Top_Cat.CODMW.objects.Reason;
 import com.Top_Cat.CODMW.objects.grenade;
 import com.Top_Cat.CODMW.objects.player;
 import com.Top_Cat.CODMW.sql.Achievement;
 import com.Top_Cat.CODMW.sql.Stat;
 
 public class gamemode {
     
     main plugin;
     ArrayList<ArrayList<Location>> spawns = new ArrayList<ArrayList<Location>>();
     public HashMap<Arrow, Location> ploc = new HashMap<Arrow, Location>();
     public HashMap<Arrow, Location> floc = new HashMap<Arrow, Location>();
     ArrayList<String> lossmesssages = new ArrayList<String>();
     ArrayList<String> winmesssages = new ArrayList<String>();
     public Random generator = new Random();
     Location d1, d2, d3, d4;
     private int t1, t2, t3, t4;
     boolean dl = false;
     public int time = 0;
     int gamelength = 600;
     int scorelimit = 0;
     Timer t = new Timer();
     public int melee = 0;
     public int respawntime = 0;
     
     public gamemode(main instance) {
         plugin = instance;
         
         setup();
         t4 = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new tele(), 4L, 4L);
         
         lossmesssages.add("They have won the battle but not the war!");
         lossmesssages.add("We have made a lot of mistakes you are going to regret");
         lossmesssages.add("God. You're such Gigs");
         winmesssages.add("Good job seals");
         winmesssages.add("You will receive cake for your victory here!");
         
         gamelength = plugin.getVarValue("gamelength", 600);
         melee = plugin.getVarValue("melee", 0);
         respawntime = plugin.getVarValue("respawntime", 0);
         scheduleGame();
     }
     
     public void sendMessage(team t, String s, Player exclude) {
         for (Player p : plugin.players.keySet()) {
             if ((t == team.BOTH || t == plugin.p(p).getTeam()) && p != exclude) {
                 p.sendMessage(s);
             }
         }
     }
     
     public void sendMessage(team t, String s) {
         sendMessage(t, s, null);
     }
     
     public class tick implements Runnable {
         @Override
         public void run() { try { tick(); } catch (Exception e) { e.printStackTrace(); } }
     }
     
     public class tickone implements Runnable {
         @Override
         public void run() { time++; }
     }
     
     public class tickfast implements Runnable {
         @Override
         public void run() { try { tickfast(); } catch (Exception e) { e.printStackTrace(); } }
     }
     
     public boolean spawnCheck(Location i) {
         return ((d1 == null || i.distance(d1) > 7) && (d2 == null || i.distance(d2) > 7) && (d3 == null || i.distance(d3) > 7) && (d4 == null || i.distance(d4) > 7));
     }
     
     public void scheduleGame() {
        t.schedule(new startgame(), plugin.getVarValue("waittime", 55) * 1000);
     }
 
     public class startgame extends TimerTask {
 
         @Override
         public void run() {
             beginGame();
         }
 
     }
     
     public void beginGame() {
         if (plugin.activeGame == false) {
             if (plugin.players.size() >= plugin.minplayers) {
                 sendMessage(team.BOTH, plugin.d + "9Game starting in 5 seconds!");
                 plugin.r.countdowns();
                 plugin.activeGame = true;
             } else {
                 scheduleGame();
             }
         }
     }
     
     // BELOW ARE INTERFACES
     
     public void setup() {};
     
     public void spawnPlayer(Player p, boolean start) {
         player _p = plugin.p(p);
         _p.spawn = System.currentTimeMillis();
         _p.clearinv();
         _p.setinv();
         
         if (!start) {
             int slot = 3;
             for (Killstreaks s : _p.last.keySet()) {
                 _p.giveItem(slot++, new ItemStack(s.getMat(), _p.last.get(s)));
             }
         }
         
         spawnTele(_p, p, start);
         _p.dead = false;
     }
     
     public Location spawnTele(player _p, Player p, boolean start) { return null; };
     
     public void startGame() {
         sendMessage(team.BOTH, plugin.d + "9Game starting now!");
         for (Player p : plugin.players.keySet()) {
             plugin.p(p).resetScore();
             spawnPlayer(p, true);
         }
         t1 = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new tick(), 40L, 40L);
         t2 = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new tickone(), 20L, 20L);
         t3 = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new tickfast(), 2L, 2L);
     }
     
     public void onWin(team winners, player lastkill, player lastdeath) {
         destroy();
         plugin.loadmap();
         
         for (player i : plugin.players.values()) {
             i.dead = false;
         }
         
         if (lastdeath != null) {
             lastdeath.p.getInventory().setHelmet(new ItemStack(Material.WOOD, 1));
             lastdeath.s.incStat(Stat.LAST_DEATH);
         }
         if (lastkill != null) {
             lastkill.s.incStat(Stat.LAST_KILL);
         }
         
        sendMessage(team.BOTH, plugin.d + "9Game ended, game will resume on '" + plugin.gm.toString().toLowerCase() + "_" + plugin.currentMap.time + "' in 60 seconds");
     }
     
     public void onKill(player attacker, player defender, Location l) {
         if (dl) {
             d1 = l;
             d3 = attacker.p.getLocation();
         } else {
             d2 = l;
             d4 = attacker.p.getLocation();
         }
         dl = !dl;    
     }
     
     @SuppressWarnings("unchecked")
     public void destroy() {
         plugin.getServer().getScheduler().cancelTask(t1);
         plugin.getServer().getScheduler().cancelTask(t2);
         plugin.getServer().getScheduler().cancelTask(t3);
         plugin.getServer().getScheduler().cancelTask(t4);
         try {
             BukkitContrib.getAppearanceManager().resetAllCloaks();
         } catch (NoClassDefFoundError e) {
             //This happens on server stop
         }
         for (killstreak i : (ArrayList<killstreak>) plugin.ks.clone()) {
             i.destroy();
         }
         plugin.ks.clear();
         plugin.activeGame = false;
         t.cancel();
     }
     
     public void afterDeath(Player p) {}
     
     @SuppressWarnings("unchecked")
     public void tick() {
         for (killstreak i : (ArrayList<killstreak>) plugin.ks.clone()) {
             i.tick();
         }
         for (player p : plugin.players.values()) {
             int life = (int) (System.currentTimeMillis() - p.spawn);
             if (life > 300000) {
                 p.s.awardAchievement(Achievement.BEAR_GRYLLS);
             } else if (life > 180000) {
                 p.s.awardAchievement(Achievement.CAMPER);
             } else if (life > 120000) {
                 p.s.awardAchievement(Achievement.HIDING);
             }
             if (p.htime < System.currentTimeMillis() && p.h < 20) {
                 p.incHealth(-3, null, Reason.NONE, null);
             }
             p.p.setHealth(p.h);
         }
     }
     
     @SuppressWarnings("unchecked")
     public void tickfast() {
         for (killstreak i : (ArrayList<killstreak>) plugin.ks.clone()) {
             i.tickfast();
         }
         for (grenade i : (ArrayList<grenade>) plugin.g.clone()) {
             i.explode();
         }
         for (player p : plugin.players.values()) {
             if (!p.dropped && p.time_todrop < System.currentTimeMillis()) {
                 afterDeath(p.p);
                 if (p.todrop > 0) {
                     plugin.currentWorld.dropItem(p.dropl, new ItemStack(Material.FEATHER, p.todrop));
                     p.todrop = 0;
                 }
                 p.dropped = true;
             }
             if (p.dead && p.tospawn < System.currentTimeMillis()) {
                 spawnPlayer(p.p, false);
             }
         }
         List<Entity> r2 = new ArrayList<Entity>();
         for (Entity i : plugin.currentWorld.getEntities()) {
             if (i instanceof Arrow) {
                 Location l = i.getLocation();
                 if (!floc.containsKey(i)) {
                     floc.put((Arrow) i, i.getLocation());
                 }
                 if (ploc.containsKey(i)) {
                     if (l.distance(ploc.get(i)) < 0.1) {
                         r2.add(i);
                         floc.remove(i);
                     }
                 }
                 ploc.put((Arrow) i, l);
             } else if (i instanceof Item) {
                 int itemId = ((EntityItem)((CraftEntity)i).getHandle()).itemStack.id;
                 if (!plugin.playerListener.allowed_pickup.contains(Material.getMaterial(itemId)) && !Killstreaks.table2.keySet().contains(Material.getMaterial(itemId)) && itemId != 332) {
                     r2.add(i);
                 }
             } else if (i instanceof Creature && !(i instanceof Wolf)) {
                 r2.add(i);
             }
             for (Entity j : r2) {
                 j.remove();
             }
         }
     }
     
     public void printScore(Player p, team t) {};
     
     public void playermove(PlayerMoveEvent event) {};
     
     public void playerpickup(PlayerPickupItemEvent event, Material pickedup) {};
     
     public class tele extends TimerTask {
         
         @Override
         public void run() {
             for (Player i : plugin.totele) {
                 jointele(i);
             }
             plugin.totele.clear();
         }
         
     }
     
     public void playerquit(PlayerQuitEvent event) {
         String nick = event.getPlayer().getDisplayName();
         player p = plugin.p(event.getPlayer());
         if (p != null) {
             nick = p.nick;
         }
         event.setQuitMessage(plugin.d + "9" + plugin.leave_msg.replaceAll("\\$nick", nick));
     }
     
     public void playerjoin(PlayerJoinEvent event) {
         plugin.setDoors();
         plugin.totele.add(event.getPlayer());
         plugin.clearinv(event.getPlayer());
         String nick = event.getPlayer().getDisplayName();
         ResultSet r = plugin.sql.query("SELECT * FROM cod_players WHERE username = '" + event.getPlayer().getDisplayName() + "'");
         try {
             if (r.first()) {
                 nick = r.getString("nick");
             } else {
                 int id = plugin.sql.update("INSERT INTO cod_players (username, nick) VALUES ('" + event.getPlayer().getDisplayName() + "', '" + event.getPlayer().getDisplayName() + "')");
                 plugin.sql.update("INSERT INTO cod_stats VALUES ('" + id + "', '0', '1000')");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         event.setJoinMessage(plugin.d + "9" + plugin.join_msg.replaceAll("\\$nick", nick));
         event.getPlayer().sendMessage(plugin.d + "9" + plugin.welcome_msg);
         event.getPlayer().setHealth(20);
     }
     
     public void jointele(Player p) {};
     
     public boolean canHit(LivingEntity a, LivingEntity d) { return false; };
     
     public String getClaymoreText(Player p) { return "^^ CLAYMORE ^^"; };
     
     public player getTopPlayer(team t) { return null; }
     
 }
