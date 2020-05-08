 package com.github.cman85.PvPTag;
 
 import org.bukkit.*;
 import org.bukkit.command.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.world.*;
 import org.bukkit.plugin.java.*;
 
 import java.util.*;
 import java.util.logging.*;
 
 public class PvPTag extends JavaPlugin implements Listener {
 
    HashMap<String, Long> safeTimes = new HashMap<String, Long>();
    HashMap<String, Long> deathTimes = new HashMap<String, Long>();
    private static Logger logger;
    private long SAFE_DELAY = 30000;
    private long DEATH_TP_DELAY = 30000;
    ChatColor nameTagColor;
    private boolean disableFlight = true;
    private DeathChestListener dcl;
    private TagAPEye tagApi;
    String version = "1.2";
    private Updater updater;
    private long lastLogout = System.currentTimeMillis();
 
    public void onEnable(){
       logger = getLogger();
       dcl = new DeathChestListener(this);
       manageConfig();
       getServer().getPluginManager().registerEvents(this, this);
       task();
    }
 
    void manageConfig(){
       Config.getInstance().enable(this);
       this.SAFE_DELAY = Config.getInstance().getConfig().getInt("Safe Time") * 1000;
       this.DEATH_TP_DELAY = Config.getInstance().getConfig().getInt("DeathTP Time") * 1000;
       DeathChest.CHEST_BREAK_DELAY = Config.getInstance().getConfig().getInt("Chest Time") * 1000;
       if(! Config.getInstance().getConfig().getBoolean("DeathTP Enabled")) this.DEATH_TP_DELAY = 0;
       PvPLoggerZombie.HEALTH = Config.getInstance().getConfig().getInt("PVPLogger Health");
       this.disableFlight = Config.getInstance().getConfig().getBoolean("Disable Flying");
       this.nameTagColor = Config.getInstance().parseNameTagColor();
       if(Config.getInstance().getConfig().getBoolean("DeathChest Enabled"))
          getServer().getPluginManager().registerEvents(dcl, this);
       if(Config.getInstance().getConfig().getBoolean("Use TagAPI") && getServer().getPluginManager().getPlugin("TagAPI") != null){
          this.tagApi = new TagEnabled(this);
       }else{
          this.tagApi = new TagDisabled();
       }
       getServer().getPluginManager().registerEvents(tagApi, this);
 
      if(Config.getInstance().getConfig().getBoolean("Auto update"))
          updater = new Updater(this, "pvp-tag", this.getFile(), Updater.UpdateType.DEFAULT, false);
    }
 
    public void onDisable(){
       Config.getInstance().disable();
       callSafeAllManual();
       dcl.breakAll();
    }
 
    public void task(){
       this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
          public void run(){
             resetNameTagsAuto();
          }
       }, 20L, 20L);
    }
 
    private void callSafeAllManual(){
       Iterator<String> iter = safeTimes.keySet().iterator();
       while(iter.hasNext()){
          String s = iter.next();
          iter.remove();
          callSafe(getServer().getPlayer(s));
       }
    }
 
    private void resetNameTagsAuto(){
       Iterator<String> iter = safeTimes.keySet().iterator();
       while(iter.hasNext()){
          String s = iter.next();
          Player player = getServer().getPlayer(s);
          if(player == null){
             iter.remove();
          }else if(isSafe(s)){
             iter.remove();
             player.sendMessage("§cYou are now safe.");
             refresh(player);
          }
       }
    }
 
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent e){
       if(e.getDamager() instanceof Snowball) e.setCancelled(true);
       if(! e.getEntity().getWorld().getName().equalsIgnoreCase("ArenaWorld")){
          if(e.getEntity() instanceof Player){
             Player hitter;
             Player hitted = (Player)e.getEntity();
             if(e.getDamager() instanceof Arrow){
                Arrow arrow = (Arrow)e.getDamager();
                if(arrow.getShooter() instanceof Player){
                   hitter = (Player)arrow.getShooter();
                }else{
                   return;
                }
             }else if(e.getDamager() instanceof Player){
                hitter = (Player)e.getDamager();
 
             }else if(e.getDamager() instanceof Zombie){
                if(PvPLoggerZombie.isPvPZombie((Zombie)e.getDamager())){
                   if(isSafe(hitted.getName())) e.setCancelled(true);
                }
                return;
             }else{
                return;
             }
             if(! e.isCancelled()){
                if(isSafe(hitted.getName())){
                   addUnsafe(hitted);
                }
                if(isSafe(hitter.getName())){
                   addUnsafe(hitter);
                }
                safeTimes.put(hitted.getName(), calcSafeTime(SAFE_DELAY));
                safeTimes.put(hitter.getName(), calcSafeTime(SAFE_DELAY));
             }else{
                if(! isSafe(hitted.getName()) && hitter.getInventory().getItemInHand() != null){
                   e.setCancelled(false);
                   safeTimes.put(hitted.getName(), calcSafeTime(SAFE_DELAY));
                   safeTimes.put(hitter.getName(), calcSafeTime(SAFE_DELAY));
                   if(isSafe(hitter.getName())){
                      addUnsafe(hitter);
                   }
                }
             }
 
          }
       }
 
    }
 
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
       if(cmd.getName().equalsIgnoreCase("callsafe") || cmd.getName().equalsIgnoreCase("csafe")){
          if(sender.isOp() || sender.hasPermission("pvptag.callsafe") || sender instanceof ConsoleCommandSender){
             if(args.length == 1){
                if(! args[0].equalsIgnoreCase("all")){
                   Player p = getServer().getPlayer(args[0]);
                   if(p == null){
                      sender.sendMessage("§cYou must specify an online player.");
                      return true;
                   }else{
                      if(! isSafe(p.getName())){
                         callSafe(p);
                         sender.sendMessage("§c" + p.getName() + " is no longer hittable.");
                         refresh(p);
                         return true;
                      }else{
                         sender.sendMessage("§c" + p.getName() + " was not hittable.");
                         return true;
                      }
                   }
                }else{
                   callSafeAllManual();
                }
             }else{
                sender.sendMessage("§cUsage: /callsafe [name] or /callsafe all");
             }
          }else{
             sender.sendMessage("§cYou must be an operator to use this command.");
          }
       }else if(cmd.getName().equalsIgnoreCase("callhit") || cmd.getName().equalsIgnoreCase("ch")){
          if(sender.isOp() || sender.hasPermission("pvptag.pvptag") || sender instanceof ConsoleCommandSender){
             Player p;
             if(args.length != 1)
                return false;
             else{
                p = getServer().getPlayer(args[0]);
                if(p == null){
                   sender.sendMessage("§cYou must specify an online player.");
                   return true;
                }
             }
 
             if(isSafe(p.getName())){
                p.damage(1);
                addUnsafe(p);
             }
          }
       }else if(cmd.getName().equalsIgnoreCase("pvptag")){
          if(sender.isOp() || sender.hasPermission("pvptag.callhit") || sender instanceof ConsoleCommandSender){
             if(args.length > 0){
                if(args[0].equalsIgnoreCase("reload")){
                   Config.getInstance().reload();
                   sender.sendMessage("§cSettings reloaded!");
                }else if(args[0].equalsIgnoreCase("setcolor")){
                   if(args.length == 2){
                      this.nameTagColor = ChatColor.getByChar(args[1]);
                      Config.getInstance().getConfig().set("NameTag Color", this.nameTagColor.getChar());
                      sender.sendMessage("§cColor changed to: " + this.nameTagColor + "this.");
                   }
                }else if(args[0].equalsIgnoreCase("save")){
                   Config.getInstance().saveYamls();
                   sender.sendMessage("§cConfig saved!");
                }
             }
          }
 
       }
       return true;
    }
 
    private long calcSafeTime(Long time){
       return System.currentTimeMillis() + time;
    }
 
    private void addUnsafe(Player p){
       safeTimes.put(p.getName(), calcSafeTime(SAFE_DELAY));
       p.sendMessage("§cYou can now be hit anywhere for at least " + (SAFE_DELAY / 1000) + " seconds!");
       p.setFlying(false);
       p.setAllowFlight(false);
       refresh(p);
    }
 
    private void callSafe(Player player){
       if(player != null){
          safeTimes.remove(player.getName());
          refresh(player);
          player.sendMessage("§cYou are now safe.");
       }
    }
 
    private void refresh(Player p){
       tagApi.refresh(p);
    }
 
    public boolean isSafe(String player){
       if(safeTimes.containsKey(player)){
          return (safeTimes.get(player) < System.currentTimeMillis());
       }
       return true;
    }
 
    public static void log(Level level, String message){
       logger.log(level, message);
    }
 
    @EventHandler
    public void onDeath(PlayerRespawnEvent e){
       safeTimes.remove(e.getPlayer().getName());
       deathTimes.put(e.getPlayer().getName(), calcSafeTime(DEATH_TP_DELAY));
    }
 
    @EventHandler(priority = EventPriority.HIGH)
    public void onTpEvent(PlayerTeleportEvent e){
       if(! isSafe(e.getPlayer().getName()) && ! e.getPlayer().isOp()){
          e.setCancelled(true);
          e.getPlayer().sendMessage(ChatColor.RED + "You cannot teleport until you are safe.");
       }else{
          if(deathTimes.containsKey(e.getPlayer().getName())){
             Long deathTime = deathTimes.get(e.getPlayer().getName());
             Long currTime = System.currentTimeMillis();
             if(deathTime > currTime){
                e.getPlayer().sendMessage("§cYou cannot teleport for " + (DEATH_TP_DELAY / 1000) + " seconds after dying. Time left: §6" + ((DEATH_TP_DELAY / 1000) - (deathTime / 1000 - currTime / 1000)));
                e.setCancelled(true);
             }else{
 
             }
          }
       }
    }
 
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
       if(PvPLoggerZombie.waitingToDie.contains(e.getPlayer().getName())){
          e.getPlayer().getInventory().clear();
          e.getPlayer().setHealth(0);
          e.getPlayer().updateInventory();
          PvPLoggerZombie.waitingToDie.remove(e.getPlayer().getName());
       }
       PvPLoggerZombie pz = PvPLoggerZombie.getByOwner(e.getPlayer().getName());
       if(pz != null){
          safeTimes.put(e.getPlayer().getName(), calcSafeTime(SAFE_DELAY));
          refresh(e.getPlayer());
          pz.despawnNoDrop(true, true);
       }
    }
 
    @EventHandler
    public void entityDeath(EntityDeathEvent e){
       if(e.getEntity() instanceof Zombie){
          PvPLoggerZombie pz = PvPLoggerZombie.getByZombie((Zombie)e.getEntity());
          if(pz != null){
             PvPLoggerZombie.waitingToDie.add(pz.getPlayer());
             pz.despawnDrop(true);
          }
       }
    }
 
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFlight(PlayerToggleFlightEvent e){
      if(disableFlight && ! isSafe(e.getPlayer().getName())){
          e.getPlayer().setFlying(false);
          e.getPlayer().setAllowFlight(false);
          e.setCancelled(true);
       }
    }
 
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
       if(! isSafe(e.getPlayer().getName())){
          lastLogout = System.currentTimeMillis();
          new PvPLoggerZombie(e.getPlayer().getName());
       }
    }
 
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreature(CreatureSpawnEvent e){
       if(e.getEntity() instanceof Zombie){
          if(System.currentTimeMillis() - lastLogout < 20){
             e.setCancelled(false);
          }else{
          }
       }
    }
 
    @EventHandler
    public void onChunk(ChunkUnloadEvent e){
       Chunk c = e.getChunk();
       for(Entity en : c.getEntities()){
          if(en.getType() == EntityType.ZOMBIE){
             Zombie z = (Zombie)en;
             if(PvPLoggerZombie.isPvPZombie(z)){
                PvPLoggerZombie pz = PvPLoggerZombie.getByZombie(z);
                pz.despawnDrop(true);
                pz.killOwner();
             }
          }
       }
    }
 
 }
