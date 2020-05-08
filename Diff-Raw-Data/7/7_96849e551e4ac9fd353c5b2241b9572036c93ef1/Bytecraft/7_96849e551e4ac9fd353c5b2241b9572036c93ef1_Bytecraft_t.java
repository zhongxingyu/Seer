 package info.bytecraft;
 
 import info.bytecraft.api.*;
 import info.bytecraft.zones.Zone;
 import info.bytecraft.zones.Zone.Permission;
 import info.bytecraft.api.BytecraftPlayer.Flag;
 import info.bytecraft.api.event.CallEventListener;
 import info.bytecraft.api.math.Point;
 import info.bytecraft.commands.*;
 import info.bytecraft.database.*;
 import info.bytecraft.database.db.DBContextFactory;
 import info.bytecraft.listener.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.*;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.maxmind.geoip.LookupService;
 
 public class Bytecraft extends JavaPlugin
 {
     private HashMap<String, BytecraftPlayer> players;
     private static IContextFactory contextFactory;
     public static Logger LOGGER;
     
     private List<String> deathMessages;
     private List<String> quitMessages;
     private List<String> swordNames;
     private Map<String, List<String>> armorNames;
     
     private LookupService lookup = null;
     
     private List<Zone> zones;
     
     public void onLoad()
     {
         File folder = getDataFolder();
         reloadConfig();
         
         FileConfiguration config = getConfig();
         LOGGER = getLogger();
         contextFactory = new DBContextFactory(config, this);
         
         zones = new ArrayList<>();
         
         try{
             lookup = new LookupService(new File(folder, "GeoIPCity.dat"), 
                     LookupService.GEOIP_MEMORY_CACHE);
         }catch(IOException e){
             getLogger().warning("Could not find GeoIPCity.dat, is it in the correct folder?");
         }
         
         try(IContext ctx = createContext())
         {
             IMessageDAO dao = ctx.getMessageDAO();
             IZoneDAO zDao = ctx.getZoneDAO();
             this.deathMessages = dao.loadDeathMessages();
             this.quitMessages = dao.loadQuitMessages();
             
             zones = zDao.loadZones();
                         
             ILoreDAO lore  = ctx.getLoreDAO();
             
             this.swordNames = lore.getSwordNames();
             this.armorNames = Maps.newHashMap();
             armorNames.put("HELMET", lore.getArmorNames("HELMET"));
             armorNames.put("CHESTPLATE", lore.getArmorNames("CHESTPLATE"));
             armorNames.put("BOOTS", lore.getArmorNames("BOOTS"));
             armorNames.put("LEGGINGS", lore.getArmorNames("LEGGINGS"));
             
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
 
     public void onEnable()
     {
         players = Maps.newHashMap();
         for (Player delegate : Bukkit.getOnlinePlayers()) {
             try {
                 addPlayer(delegate, delegate.getAddress().getAddress());
             } catch (PlayerBannedException e) {}
         }
 
         registerEvents();
 
         getCommand("back").setExecutor(new BackCommand(this));
         getCommand("ban").setExecutor(new BanCommand(this));
         getCommand("bless").setExecutor(new BlessCommand(this));
         getCommand("brush").setExecutor(new BrushCommand(this));
         getCommand("clear").setExecutor(new ClearCommand(this));
         getCommand("cmob").setExecutor(new CreateMobCommand(this));
         getCommand("creative").setExecutor(new GameModeCommand(this, "creative"));
         getCommand("channel").setExecutor(new ChannelCommand(this));
         getCommand("chestlog").setExecutor(new ChestLogCommand(this));
         getCommand("fill").setExecutor(new FillCommand(this, "fill"));
         getCommand("force").setExecutor(new ForceCommand(this));
         getCommand("gamemode").setExecutor(new GameModeCommand(this, "gamemode"));
         getCommand("give").setExecutor(new GiveCommand(this));
         getCommand("god").setExecutor(new SayCommand(this, "god"));
         getCommand("hardwarn").setExecutor(new WarnCommand(this, "hardwarn"));
         getCommand("head").setExecutor(new HeadCommand(this));
         getCommand("home").setExecutor(new HomeCommand(this));
         getCommand("inv").setExecutor(new InventoryCommand(this));
         getCommand("item").setExecutor(new ItemCommand(this));
         getCommand("kick").setExecutor(new KickCommand(this));
         getCommand("kill").setExecutor(new KillCommand(this));
         getCommand("lot").setExecutor(new LotCommand(this));
         getCommand("makewarp").setExecutor(new WarpCreateCommand(this));
         getCommand("me").setExecutor(new ActionCommand(this));
         getCommand("message").setExecutor(new MessageCommand(this, "message"));
         getCommand("mute").setExecutor(new MuteCommand(this));
         getCommand("nuke").setExecutor(new NukeCommand(this));
         getCommand("pos").setExecutor(new PositionCommand(this));
         getCommand("reply").setExecutor(new MessageCommand(this, "reply"));
         getCommand("ride").setExecutor(new RideCommand(this, "ride"));
         getCommand("rideme").setExecutor(new RideCommand(this, "rideme"));
         getCommand("say").setExecutor(new SayCommand(this, "say"));
         getCommand("sell").setExecutor(new SellCommand(this));
         getCommand("summon").setExecutor(new SummonCommand(this));
         getCommand("smite").setExecutor(new SmiteCommand(this));
         //getCommand("support").setExecutor(new SupportCommand(this));
         getCommand("spawn").setExecutor(new SpawnCommand(this));
         getCommand("survival").setExecutor(new GameModeCommand(this, "survival"));
         getCommand("testfill").setExecutor(new FillCommand(this, "testfill"));
         getCommand("time").setExecutor(new TimeCommand(this));
         getCommand("tool").setExecutor(new ToolCommand(this));
         getCommand("town").setExecutor(new TownCommand(this));
         getCommand("tpblock").setExecutor(new TeleportBlockCommand(this));
         getCommand("teleport").setExecutor(new TeleportCommand(this));
         getCommand("tppos").setExecutor(new TeleportPosCommand(this));
         getCommand("user").setExecutor(new UserCommand(this));
         getCommand("vanish").setExecutor(new VanishCommand(this));
         getCommand("wallet").setExecutor(new WalletCommand(this));
         getCommand("warn").setExecutor(new WarnCommand(this, "warn"));
         getCommand("warp").setExecutor(new WarpCommand(this));
         getCommand("who").setExecutor(new WhoCommand(this));
         getCommand("zone").setExecutor(new ZoneCommand(this));
         
         PluginDescriptionFile pdf = getDescription();
         
         String version = pdf.getVersion();
         
         Bukkit.broadcastMessage(ChatColor.GREEN + "Successfuly loaded Bytecraft " 
         + ChatColor.GOLD + "v" + version + ChatColor.GREEN + "!");
     }
     
     public void onDisable()
     {
         for(BytecraftPlayer player: getOnlinePlayers()){
             this.removePlayer(player);
         }
         
     }
     
     public IContextFactory getContextFactory()
     {
         return contextFactory;
     }
     
     public IContext createContext()
     throws DAOException
     {
         return contextFactory.createContext();
     }
 
     private void registerEvents()
     {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(new BlessListener(this), this);
        pm.registerEvents(new BookShelfListener(this), this);
         pm.registerEvents(new ButtonListener(this), this);
         pm.registerEvents(new BytecraftPlayerListener(this), this);
         pm.registerEvents(new BytecraftBlockListener(this), this);
         pm.registerEvents(new CallEventListener(this), this);
         pm.registerEvents(new ChatListener(this), this);
         pm.registerEvents(new FillListener(this), this);
         pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new ItemFrameListener(this), this);
         pm.registerEvents(new PlayerLookupListener(this), this);
         pm.registerEvents(new PlayerPromotionListener(this), this);
         pm.registerEvents(new SelectListener(this), this);
         pm.registerEvents(new SignColorListener(), this);
         pm.registerEvents(new ZoneListener(this), this);
         
         //rare drop
        //pm.registerEvents(new RareDropListener(this), this);
         pm.registerEvents(new DamageListener(this), this);
     }
     
     // ========================================================
     // ================== Player Methods ======================
     // ========================================================
     
     public void refreshPlayer(BytecraftPlayer player)
     {
         try(IContext ctx = createContext()){
             IPlayerDAO dao = ctx.getPlayerDAO();
             dao.updatePlayTime(player);
             dao.loadFlags(player);
             player.setRank(dao.getRank(player));
             ChatColor color = player.getRank().getColor();
             
             if(player.hasFlag(Flag.NOBLE)){
                 if(player.getRank() == Rank.MEMBER || player.getRank() == Rank.SETTLER){
                     color = ChatColor.GOLD;
                 }
             }
             
             String name = color + player.getName();
             player.setDisplayName(name + ChatColor.WHITE);
             if(name.length() > 16){
                 player.setPlayerListName(name.substring(0, 15));
             }else{
                 player.setPlayerListName(name);
             }
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
     
     public void reloadPlayer(BytecraftPlayer player)
     {
         try{
             addPlayer(player.getDelegate(), player.getAddress().getAddress());
         }catch(PlayerBannedException e){
             player.kickPlayer(e.getMessage());
         }
     }
 
     public BytecraftPlayer getPlayer(Player player)
     {
         return getPlayer(player.getName());
     }
 
     public BytecraftPlayer getPlayer(String name)
     {
         return players.get(name);
     }
     
     public BytecraftPlayer getPlayerOffline(String name)
     {
         if(this.players.containsKey(name)){
             return players.get(name);
         }
         try(IContext ctx = createContext()){
             IPlayerDAO dao = ctx.getPlayerDAO();
             return dao.getPlayer(name);
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
 
     public BytecraftPlayer addPlayer(Player srcPlayer,  InetAddress addr)
             throws PlayerBannedException
     {
         if (players.containsKey(srcPlayer.getName())) {
             return players.get(srcPlayer.getName());
         }
         
         try (IContext ctx = createContext()){
             IPlayerDAO dao = ctx.getPlayerDAO();
             BytecraftPlayer player = dao.getPlayer(srcPlayer);
             
             if(player == null){
                 player = dao.createPlayer(srcPlayer);
             }
             
             if(dao.isBanned(player)){
                 throw new PlayerBannedException(ChatColor.RED + "You are not allowed on this server");
             }
             
             player.setFlag(Flag.HARDWARNED, false);
             player.setFlag(Flag.SOFTWARNED, false);
             player.setFlag(Flag.MUTE, false);
             
             IReportDAO reportDao = ctx.getReportDAO();
             List<PlayerReport> reports = reportDao.getReports(player);
             for(PlayerReport report: reports){
                 Date validUntil = report.getValidUntil();
                 if (validUntil == null) {
                     continue;
                 }
                 if (validUntil.getTime() < System.currentTimeMillis()) {
                     continue;
                 }
                 
                 if (report.getAction() == PlayerReport.Action.SOFTWARN) {
                     player.setFlag(Flag.SOFTWARNED, true);
                 }
                 else if (report.getAction() == PlayerReport.Action.HARDWARN) {
                     player.setFlag(Flag.HARDWARNED, true);
                 }else if(report.getAction() == PlayerReport.Action.MUTE){
                     player.setFlag(Flag.MUTE, true);
                 }
             }
             
             ChatColor color = player.getRank().getColor();
             if(player.hasFlag(Flag.NOBLE)){
                 if(player.getRank() == Rank.MEMBER || player.getRank() == Rank.SETTLER){
                     color = ChatColor.GOLD;
                 }
             }
             
             if(player.hasFlag(Flag.HARDWARNED) || player.hasFlag(Flag.SOFTWARNED)){
                 color = ChatColor.GRAY;
             }
             
             player.setDisplayName(color + player.getName() + ChatColor.WHITE);
             String name = color + player.getName();
             
             if(name.length() > 16){
                 player.setPlayerListName(name.substring(0, 15));
             }else{
                 player.setPlayerListName(name);
             }
             
             player.setIp(addr.getHostAddress());
             player.setHost(addr.getCanonicalHostName());
             
             if(lookup != null){
                 com.maxmind.geoip.Location loc = lookup.getLocation(player.getIp());
                 if(loc != null){
                     player.setCountry(loc.countryName);
                     player.setCity(loc.city);
                 }
             }
             
             ILogDAO logs = ctx.getLogDAO();
             logs.insertLogin(player, "login");
             
             players.put(player.getName(), player);
             
             Location loc = player.getLocation();
             
             if(loc != null){
                Zone zone = getZoneAt(loc.getWorld(), new Point(loc.getBlockX(), loc.getBlockZ()));
                if(zone != null){
                    player.sendMessage(ChatColor.RED + "[" + zone.getName() + "] "
                            + zone.getEnterMessage());
 
                    if (zone.hasFlag(Zone.Flag.PVP)) {
                        player.sendMessage(ChatColor.RED
                                + "[" + zone.getName() + "] "
                                + "Warning! This is a PVP zone! Other players can damage or kill you here.");
                    }
                    Permission perm  = zone.getUser(player);
                    if (perm != null) {
                        String permNotification = perm.getPermNotification();
                        player.sendMessage(ChatColor.RED + "[" + zone.getName()
                                + "] " + permNotification);
                    }
                }
             }
             
             return player;
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
 
     public void removePlayer(BytecraftPlayer player)
     {
         try(IContext ctx = createContext()){
             ctx.getPlayerDAO().updatePlayTime(player);
             ctx.getLogDAO().insertLogin(player, "logout");
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
         this.players.remove(player.getName());
     }
 
     public List<BytecraftPlayer> getOnlinePlayers()
     {
         List<BytecraftPlayer> playersList = new ArrayList<BytecraftPlayer>();
         for (BytecraftPlayer player : players.values()) {
             playersList.add(player);
         }
         return playersList;
     }
     
     public List<BytecraftPlayer> matchPlayer(String name)
     {
         List<BytecraftPlayer> players = Lists.newArrayList();
         for(BytecraftPlayer player: getOnlinePlayers()){
             String playerName = player.getName().toLowerCase();
             if(playerName.startsWith(name.toLowerCase())){
                 players.add(player);
             }
         }
         return players;
     }
     
     public BytecraftPlayer getBytecraftPlayerOffline(String name)
     {
         if(players.containsKey(name)){
             return players.get(name);
         }
         try(IContext ctx = createContext()){
             return ctx.getPlayerDAO().getPlayer(name);
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
 
     // ========================================================
     // ======================= Zones ==========================
     // ========================================================
     
     public List<Zone> getZones(String world)
     {
         List<Zone> zones = Lists.newArrayList();
         for(Zone zone: this.zones){
             if(zone.getWorld().equalsIgnoreCase(world)){
                 zones.add(zone);
             }
         }
         return zones;
     }
     
     public Zone getZoneAt(World world, Point p)
     {
         for(Zone zone: getZones(world.getName())){
             if(zone.contains(p)){
                 return zone;
             }
         }
         return null;
     }
     
     public Zone getZone(String name)
     {
         for(Zone zone: zones){
             if(zone.getName().equalsIgnoreCase(name)){
                 return zone;
             }
         }
         return null;
     }
     
     public void addZone(Zone zone)
     {
         this.zones.add(zone);
     }
     
     public void deleteZone(Zone zone)
     {
         this.zones.remove(zone);
     }
     
     // ========================================================
     // ===================== Other ============================
     // ========================================================
     public long getValue(Block block)
     {
         switch (block.getType()) {
         case STONE:
             return 1;
         case DIRT:
             return 1;
         case GRASS:
             return 1;
         case SAND:
             return 2;
         case IRON_ORE:
             return 30;
         case COAL_ORE:
             return 5;
         case LAPIS_ORE:
             return 5;
         case OBSIDIAN:
             return 50;
         case GOLD_ORE:
             return 50;
         case EMERALD_ORE:
             return 100;
         case DIAMOND_ORE:
             return 200;
         default:
             return 1;
         }
     }
     
     public Location getWorldSpawn(String name)
     {
         String loc = this.getConfig().getString("spawn." + name);
         
         if(loc == null || loc.equalsIgnoreCase("")){
             return Bukkit.getWorld(name).getSpawnLocation();
         }
         
         String[] args = loc.split(", ");
         
         Location location = new Location(Bukkit.getWorld(name), parseDouble(args[0]), parseDouble(args[1]), 
                 parseDouble(args[2]), parseFloat(args[3]), parseFloat(args[4]));
         
         return location;
     }
     
     private double parseDouble(String s)
     {
         try{
             return Double.parseDouble(s);
         }catch(NumberFormatException e){
             return 0.0;
         }
     }
     
     private float parseFloat(String s)
     {
         try{
             return Float.parseFloat(s);
         }catch(NumberFormatException e){
             return 0.0F;
         }
     }
 
     public void sendMessage(String string)
     {
         Bukkit.getConsoleSender().sendMessage(string);
     }
     
     public List<String> getQuitMessages()
     {
         return this.quitMessages;
     }
     
     public List<String> getDeathMessages()
     {
         return this.deathMessages;
     }
 
     public List<String> getSwordNames()
     {
         return swordNames;
     }
     
     public List<String> getArmorNames(String type)
     {
         return this.armorNames.get(type);
     }
     
 }
