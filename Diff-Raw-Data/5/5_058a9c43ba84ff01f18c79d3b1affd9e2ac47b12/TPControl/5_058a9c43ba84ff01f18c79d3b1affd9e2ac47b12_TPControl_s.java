 package nu.nerd.tpcontrol;
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 public class TPControl extends JavaPlugin implements Listener {
     Logger log = Logger.getLogger("Minecraft");
     
     
     //private final TPControlListener cmdlistener = new TPControlListener(this);
     public final Configuration config = new Configuration(this);
     
     private HashMap<String,User> user_cache = new HashMap<String, User>();
     
     @Override
     public void onEnable(){
         log = this.getLogger();
         //Listen to events
         PluginManager pm = getServer().getPluginManager();
 	pm.registerEvents(this, this);
         
         //Load config
         File config_file = new File(getDataFolder(), "config.yml");
         if(!config_file.exists()) {
             getConfig().options().copyDefaults(true);
             saveConfig();
         }
         config.load();
         
         //TODO: Can we get away with async?
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
                 for(User u : user_cache.values()) {
                     u.save();
                 }
             }
         }, config.SAVE_INTERVAL*20, config.SAVE_INTERVAL*20);
         
         log.info("TPControl has been enabled!");
     }
     
     @Override
     public void onDisable(){
         log.info("TPControl has been disabled.");
     }
     
     public Player getPlayer(String name) {
         Player[] players = getServer().getOnlinePlayers();
 
         Player found = null;
         String lowerName = name.toLowerCase();
         int delta = Integer.MAX_VALUE;
         for (Player player : players) {
             if (ChatColor.stripColor(player.getName()).toLowerCase().startsWith(lowerName)) {
                 int curDelta = player.getName().length() - lowerName.length();
                 if (curDelta < delta) {
                     found = player;
                     delta = curDelta;
                 }
                 if (curDelta == 0) break;
             }
         }
         return found;
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteract(PlayerInteractEvent event) { 
         if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
             return;
         }
         if (event.getClickedBlock().getType() == Material.WALL_SIGN) {
             Sign s = (Sign) event.getClickedBlock().getState();
             Player p = event.getPlayer();
             if (event.getItem() != null && event.getItem().getType() == Material.DIAMOND_HOE) {
                 // Registering a sign
                 if (s.getLines().length > 1) {
                     if (!s.getLine(0).isEmpty() && p.hasPermission("tpcontrol.level.admin") && s.getLine(0).equalsIgnoreCase("[WARP]")) {
                         s.setLine(3, ChatColor.GREEN + "." + ChatColor.BLACK);
                         s.update();
                         p.sendMessage("Warp sign created.");
                         return;
                     }
                 }
             }
             if (s.getLines().length > 1) {
                 if (!s.getLine(3).isEmpty() && s.getLine(3).equalsIgnoreCase(ChatColor.GREEN + "." + ChatColor.BLACK)) {
                     if (!s.getLine(1).isEmpty() && s.getLine(1).matches("\\s*-*\\d+\\s*,\\s*-*\\d+\\s*,\\s*-*\\d+\\s*")) {
                         String[] sCo = s.getLine(1).split(",");
                         // Change our strings to ints, remember to remove spaces because people can be stupid about fomatting
                         Location tp = new Location(p.getWorld(), Integer.parseInt(sCo[0].trim()), Integer.parseInt(sCo[1].trim()), Integer.parseInt(sCo[2].trim()));
                         p.teleport(tp);
                     } else {
                         p.sendMessage("Warp sign incorrect.");
                     }
                 }
             }
         }
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
         //
         // /tp
         //
         if (command.getName().equalsIgnoreCase("tp")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
             Player p1 = (Player)sender;
             if(!canTP(p1)) {
                 messagePlayer(p1, "You do not have permission.", ChatColor.RED);
                 return true;
             }
             
             if(args.length != 1) {
                 messagePlayer(p1, "Usage: /tp <player>", ChatColor.GOLD);
                 return true;
             }
             
             Player p2 = getPlayer(args[0]);
             if(p2 == null) {
                 messagePlayer(p1, "Couldn't find player "+ args[0], ChatColor.RED);
                 return true;
             }
             
             User u2 = getUser(p2);
             String mode;
             
             if(canOverride(p1, p2)) {
                 mode = "allow";
             } else {
                 mode = u2.getCalculatedMode(p1); //Get the mode to operate under (allow/ask/deny)
             }
             
             
             if (mode.equals("allow")) {
                 messagePlayer(p1, "Teleporting you to " + p2.getName() + ".", ChatColor.GREEN);
                 teleport(p1, p2);
             } 
             else if (mode.equals("ask")) {
                 messagePlayer(p1, "A request has been sent to " + p2.getName() + ".", ChatColor.GREEN);
                 u2.lodgeRequest(p1);
             } 
             else if (mode.equals("deny")) {
                 messagePlayer(p1, p2.getName() + " has teleportation disabled.", ChatColor.RED);
             }
             return true;
         }
         //
         // /tppos [world] x y z
         //
         else if (command.getName().equalsIgnoreCase("tppos")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
             
             if (args.length < 3 || args.length > 4) {
                 sender.sendMessage("Invalid paramaters. Syntax: /tppos [world] x y z");
                 return true;
             }
             
             Player p = (Player)sender;
             World w = null;
             if (args.length == 4) {
                 w = getServer().getWorld(args[0]);
                 args = new String[] { args[1], args[2], args[3] };
                 if (w == null) {
                     sender.sendMessage(ChatColor.RED + "An invalid world was provided.");
                     return true;
                 }
             }
             else {
                 w = p.getWorld();
             }
             
             double x, y, z;
             
             try {
                 x = Double.parseDouble(args[0]);
                 y = Double.parseDouble(args[1]);
                 z = Double.parseDouble(args[2]);
             } catch (NumberFormatException ex) {
                 sender.sendMessage("Invalid paramaters. Syntax: /tppos [world] x y z");
                 return true;
             }
             
             p.teleport(new Location(w, x, y, z));
             
             return true;
         }
         //
         // /tphere <player>
         //
         else if (command.getName().equalsIgnoreCase("tphere")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
             
             
             Player p2 = (Player)sender;
             
             if(!canTP(p2)) {
                 messagePlayer(p2, "You do not have permission.", ChatColor.RED);
                 return true;
             }
             
             if(args.length != 1) {
                 messagePlayer(p2, "Usage: /tphere <player>", ChatColor.GOLD);
                 return true;
             }
             
             Player p1 = getPlayer(args[0]);
             if(p1 == null) {
                 messagePlayer(p2, "Couldn't find player "+ args[0], ChatColor.RED);
                 return true;
             }
             
             if(canTP(p2) && canOverride(p2, p1)) {
                 messagePlayer(p1, p2.getName() + " teleported you to them.", ChatColor.GREEN);
                 messagePlayer(p2, "Teleporting " + p1.getName() + " to you.", ChatColor.GREEN);
                 teleport(p1, p2);
                 return true;
             } else {
                 messagePlayer(p2, "You do not have permission.", ChatColor.RED);
                 return true;
             }
         }
         //
         // /tpmode allow|ask|deny
         //
         else if (command.getName().equalsIgnoreCase("tpmode")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
             Player p2 = (Player)sender;
             
             if(!canTP(p2)) {
                 messagePlayer(p2, "You do not have permission.", ChatColor.RED);
                 return true;
             }
             
             User u2 = getUser(p2);
             
             if(args.length != 1 || (!args[0].equals("allow") &&
                                     !args[0].equals("ask") &&
                                     !args[0].equals("deny"))) {
                 messagePlayer(p2, "Usage: /tpmode allow|ask|deny", ChatColor.GOLD);
                messagePlayer(p2, "Your are currently in *" + u2.getMode() + "* mode.", ChatColor.GOLD);
             } else {
                 u2.setMode(args[0]);
                messagePlayer(p2, "You are now in *"+args[0]+"* mode.", ChatColor.GOLD);
             }
             return true;
         }
         //
         // /tpallow
         //
         else if (command.getName().equalsIgnoreCase("tpallow")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
             Player p2 = (Player)sender;
             
             if(!canTP(p2)) {
                 messagePlayer(p2, "You do not have permission." , ChatColor.RED);
                 return true;
             }
             
             User u2 = getUser(p2);
 
             //Check the field exists...
             if(u2.last_applicant == null) {
                 messagePlayer(p2, "Error: No one has attempted to tp to you lately!", ChatColor.RED);
                 return true;
             }
             
             //Check it hasn't expired
             Date t = new Date();
             if(t.getTime() > u2.last_applicant_time + 1000L*config.ASK_EXPIRE) {
                 messagePlayer(p2, "Error: /tp request has expired!", ChatColor.RED);
                 return true;
             }
             
             
             Player p1 = getPlayer(u2.last_applicant);
             
             if(p1 == null) {
                 messagePlayer(p2, "Error: "+u2.last_applicant+" is no longer online.");
                 return true;
             }
             
             u2.last_applicant = null;
             messagePlayer(p1, "Teleporting you to " + p2.getName() + ".", ChatColor.GREEN);
             messagePlayer(p2, "Teleporting " + p1.getName() + " to you.", ChatColor.GREEN);
             teleport(p1, p2);
             
             
             return true;
         }
         //
         // /tpdeny
         //
         else if (command.getName().equalsIgnoreCase("tpdeny")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
             
             Player p2 = (Player)sender;
             
             if(!canTP(p2)) {
                 messagePlayer(p2, "You do not have permission.", ChatColor.RED);
                 return true;
             }
             
             User u2 = getUser(p2);
 
             if(u2.last_applicant == null) {
                 messagePlayer(p2, "Error: No one has attempted to tp to you lately!", ChatColor.RED);
                 return true;
             }
             
             
             messagePlayer(p2, "Denied a request from "+u2.last_applicant+".", ChatColor.RED);
             messagePlayer(p2, "Use '/tpblock "+u2.last_applicant+"' to block further requests", ChatColor.RED);
             u2.last_applicant = null;
             
             return true;
         }
         //
         // /tpfriend <player>
         //
         else if (command.getName().equalsIgnoreCase("tpfriend")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
             Player p2 = (Player)sender;
             
             if(!canTP(p2)) {
                 messagePlayer(p2, "You do not have permission.", ChatColor.RED);
                 return true;
             }
             
             User u2 = getUser(p2);
             if(args.length != 1) {
                 messagePlayer(p2, "Usage: /tpfriend <player>", ChatColor.GOLD);
                 return true;
             }
             if(u2.addFriend(args[0])) {
                 messagePlayer(p2, args[0] + " added as a friend.", ChatColor.GREEN);
             } else {
                 messagePlayer(p2, "Error: " + args[0] + " is already a friend.", ChatColor.GOLD);
             }
             return true;
         }
         //
         // /tpunfriend <player>
         //
         else if (command.getName().equalsIgnoreCase("tpunfriend")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
 
             Player p2 = (Player)sender;
             
             if(!canTP(p2)) {
                 messagePlayer(p2, "You do not have permission.", ChatColor.RED);
                 return true;
             }
             
             User u2 = getUser(p2);
             if(args.length != 1) {
                 messagePlayer(p2, "Usage: /tpunfriend <player>", ChatColor.RED);
                 return true;
             }
             if(u2.delFriend(args[0])) {
                 messagePlayer(p2, args[0] + " removed from friends.", ChatColor.GREEN);
             } else {
                 messagePlayer(p2, "Error: " + args[0] + " not on friends list.", ChatColor.GOLD);
             }
             return true;
         }
         //
         // /tpblock <player>
         //
         else if (command.getName().equalsIgnoreCase("tpblock")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
             Player p2 = (Player)sender;
             
             if(!canTP(p2)) {
                 messagePlayer(p2, "You do not have permission.", ChatColor.RED);
                 return true;
             }
             
             User u2 = getUser(p2);
             if(args.length != 1) {
                 messagePlayer(p2, "Usage: /tpblock <player>", ChatColor.GOLD);
                 return true;
             }
             if(u2.addBlocked(args[0])) {
                 messagePlayer(p2, args[0] + " was blocked from teleporting to you.", ChatColor.GREEN);
             } else {
                 messagePlayer(p2, "Error: " + args[0] + " is already blocked.", ChatColor.GOLD);
             }
             return true;
         }
         //
         // /tpunblock <player>
         //
         else if (command.getName().equalsIgnoreCase("tpunblock")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This cannot be run from the console!");
                 return true;
             }
 
             Player p2 = (Player)sender;
             
             if(!canTP(p2)) {
                 messagePlayer(p2, "You do not have permission.", ChatColor.RED);
                 return true;
             }
             
             User u2 = getUser(p2);
             if(args.length != 1) {
                 messagePlayer(p2, "Usage: /tpunblock <player>", ChatColor.GOLD);
                 return true;
             }
             if(u2.delBlocked(args[0])) {
                 messagePlayer(p2, args[0] + " was unblocked from teleporting to you.", ChatColor.GREEN);
             } else {
                 messagePlayer(p2, "Error: " + args[0] + " is not blocked.", ChatColor.GOLD);
             }
             return true;
         }
         return false;
     }
     
     //Pull a user from the cache, or create it if necessary
     private User getUser(Player p) {
         User u = user_cache.get(p.getName().toLowerCase());
         if(u == null) {
             u = new User(this, p);
             user_cache.put(p.getName().toLowerCase(), u);
         }
         return u;
     }
     
     //Checks if p1 can override p2
     private boolean canOverride(Player p1, Player p2) {
         for(int j = config.GROUPS.size() - 1; j >= 0; j--){
             String g = config.GROUPS.get(j);
             if(p2.hasPermission("tpcontrol.level."+g)) {
                 return false;
             }
             else if (p1.hasPermission("tpcontrol.level."+g)) {
                 return true;
             }
         }
         return false;
     }
     
     private boolean canTP(Player p1) {
         for(String g : config.GROUPS) {
             if(g.equals(config.MIN_GROUP) && p1.hasPermission("tpcontrol.level."+g)) {
                 return true;
             }
             else if(p1.hasPermission("tpcontrol.level."+g)) {
                 return true;
             }
         }
         return false;
     }
     
     public void messagePlayer(Player p, String m) {
         messagePlayer(p, m, ChatColor.GRAY);
     }
     
     public void messagePlayer(Player p, String m, ChatColor color) {
         p.sendMessage(ChatColor.GRAY + "[TP] " + color + m);
     }
     
     private void teleport(Player p1, Player p2) {
         p1.teleport(p2.getLocation());
         if (p2.isFlying()) {
             p1.setFlying(true);
         }
     }
 }
