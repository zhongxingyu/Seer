 /*
  * 
  */
 package tzer0.TweakedCycle;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * Time and weather controls!
  *
  * @author TZer0
  */
 public class TweakedCycle extends JavaPlugin {
     public PermissionHandler permissions;
     private LinkedList<Schedule> schedList;
     Configuration conf;
     final String[] states = {"normal", "day", "night", "dusk", "dawn", "real"};
     boolean broadcast;
     int shiftTime;
 
     public void onDisable() {
         schedList = new LinkedList<Schedule>();
         getServer().getScheduler().cancelTasks(this);
         System.out.println("TweakedCycle disabled.");
     }
 
     public void onEnable() {
         conf = getConfiguration();
         broadcast = conf.getBoolean("broadcast", true);
         reloadWorlds();
         PluginDescriptionFile pdfFile = this.getDescription();
         setupPermissions();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         shiftTime = conf.getInt("shiftTime", 0);
         getServer().getScheduler().scheduleAsyncDelayedTask(this, new InitialLoader(), 600);
         
     }
     public void reloadWorlds() {
         String mode;
         schedList = new LinkedList<Schedule>();
         getServer().getScheduler().cancelTasks(this);
         schedList.clear();
         for (World tmp : getServer().getWorlds()) {
             mode = conf.getString("worlds." + tmp.getName(), "0");
             Schedule sched = new Schedule(mode, tmp, this);
             schedList.add(sched);
         } 
     }
     
     public int getRes(String world) {
         int res = conf.getInt("resolution." + world.toLowerCase(), conf.getInt("resolution.default", 15));
         if (res < 0) {
             res = 1;
         }
         return res;
     }
     
     public void setRes(String world, int res) {
         conf.setProperty("resolution." + world.toLowerCase(), res);
         conf.save();
     }
     
     public boolean validMode(int mode) {
         return mode < states.length && mode >= 0;
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
         if (commandLabel.equalsIgnoreCase("tc") || commandLabel.equalsIgnoreCase("tweakedcycle")) {
             if (sender instanceof Player) {
                 if (permissions != null) {
                     if (!permissions.has((Player) sender, "tweakedcycle.admin")) {
                         sender.sendMessage(ChatColor.RED + "You do not have access to this command.");
                         return true;
                     }
                 } else {
                     if (!((Player) sender).isOp()) {
                         sender.sendMessage(ChatColor.RED + "You do not have access to this command.");
                         return true; 
                     }
                 }
             }
             int i;
             int l = args.length;
             if (l == 0) {
                 sender.sendMessage(ChatColor.GREEN + "TweakedCycle by TZer0 (TZer0.jan@gmail.com)");
                 sender.sendMessage(ChatColor.YELLOW+"All commands start with /tweakedcycle or /tc");
                 sender.sendMessage(ChatColor.YELLOW+"General usage:");
                 sender.sendMessage(ChatColor.YELLOW+"(r)eload - reloads worlds");
                 sender.sendMessage(ChatColor.YELLOW+"(l)ist - gives you a list of worlds");
                 sender.sendMessage(ChatColor.YELLOW+"(s)et worldname mode(0,1,2,3,4,5/normal,day,night,dusk,dawn,real OR schedule)");
                 sender.sendMessage(ChatColor.YELLOW+"(l)ist(s)chedules [#] - lists avilable schedules");
                 sender.sendMessage(ChatColor.YELLOW+"(d)elete(s)chedule name - deletes a schedule");
                 sender.sendMessage(ChatColor.YELLOW+"(n)ew(s)chedule name sched - creates a new schedule");
                 sender.sendMessage(ChatColor.YELLOW+"(s)ched(r)es [world] [res] - schedres-settings");
                 sender.sendMessage(ChatColor.YELLOW+"(b)road(c)ast - warns players about time-changes");
                 sender.sendMessage(ChatColor.YELLOW+"(s)hift(t)ime");
             } else if (args[0].equalsIgnoreCase("shifttime") || args[0].equalsIgnoreCase("st")) {
                 if (l == 2) {
                     int ntime = 0;
                     try {
                         ntime = Integer.parseInt(args[1]);
                     } catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED + "Invalid number format");
                         return true;
                     }
                     shiftTime = ntime;
                     conf.setProperty("shiftTime", ntime);
                     conf.save();
                 }
                 sender.sendMessage(ChatColor.GREEN + String.format("Time is shifted by %d hours.", conf.getInt("shiftTime", 0)));
             } else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
                 int page;
                 if (l == 2) {
                     page = Integer.parseInt(args[1]);
                 } else {
                     page = 0;
                 }
                 int limit = Math.min(schedList.size(), (page+1)*10);
                 sender.sendMessage(String.format(ChatColor.YELLOW+"Showing worlds from %d to %d", page*10+1, limit));
                 for (i = page*10; i < limit; i++) {
                     sender.sendMessage(ChatColor.YELLOW+"Name: " + schedList.get(i).world.getName() + ", mode: " + schedList.get(i).getMode());
                 }
                 if ((page+1)*10 < limit) {
                     sender.sendMessage(String.format(ChatColor.YELLOW+"/tc list %d to see the next page", page+1 ));
                 }
             } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
                 reloadWorlds();
                 sender.sendMessage(ChatColor.GREEN+"Done.");
             } else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("s")) {
                 boolean error = false;
                 String worldname = "";
                 String schedname = "";
                 if (l >= 3) {
                     worldname = args[1];
                     for (int j = 2; j < l-1; j++) {
                         worldname += " "+args[j];
                     }
                     schedname = args[l-1];
                 } else {
                     error = true;
                 }
                 if (error) {
                     sender.sendMessage(ChatColor.RED+"Requires a world-name and a schedule/time of day");
                 } else {
                     Schedule current = null;
                     for (Schedule sched : schedList) {
                         if (sched.world.getName().equalsIgnoreCase(worldname)) {
                             current = sched;
                             break;
                         }
                     }
                     if (current != null) {
                         current.setMode(schedname);
                         conf.setProperty("worlds."+current.world.getName(), schedname);
                         conf.save();
                         sender.sendMessage(ChatColor.GREEN+"Done.");
                     } else {
                         sender.sendMessage(ChatColor.RED+String.format("Invalid world name, %s",worldname));
                     }
                 }
             } else if (args[0].equalsIgnoreCase("newschedule") || args[0].equalsIgnoreCase("ns")) {
                 if (l == 3) {
                     String translated = args[2].replace(":1,", ",").replaceAll("day", "1")
                     .replaceAll("night", "2").replaceAll("dusk", "3").replaceAll("dawn", "4").replaceAll("real", "5");
                     if (checkMode(translated)) {
                         conf.setProperty("modes."+args[1], translated);
                         conf.save();
                         sender.sendMessage(ChatColor.GREEN + "Saved!");
                         for (Schedule tmp : schedList) {
                             if (tmp.schedname.equalsIgnoreCase(args[1])) {
                                 tmp.setMode(translated);
                             }
                         }
                     } else {
                         sender.sendMessage(ChatColor.RED + "Invalid mode.");
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "Requires a name and a schedule.");
                 }
             } else if (args[0].equalsIgnoreCase("deleteschedule") || args[0].equalsIgnoreCase("ds")) {
                 if (l == 2) {
                     if (conf.getString("modes." + args[1].toLowerCase()) != null) {
                         conf.removeProperty("modes." + args[1].toLowerCase());
                         conf.save();
 
                     } else {
                         sender.sendMessage(ChatColor.RED + "Requires a name");
                     }
                 }
             } else if (args[0].contentEquals("listschedules") || args[0].equalsIgnoreCase("ls")) {
 
                 if (conf.getKeys("modes.") != null) {
                     List<String> keys = conf.getKeys("modes.");
                     int page = 0;
                     if (l == 2) {
                         page = toInt(args[1]);
                     }
                     if (page < keys.size()) {
                         sender.sendMessage(ChatColor.GREEN + "Saved schedules:");
                         for (int j = page*10; j < Math.min((page+1)*10, keys.size()); j++) {
                             sender.sendMessage(ChatColor.YELLOW + String.format("%s - %s", keys.get(j), 
                                     humanReadable(conf.getString("modes."+keys.get(j)))));
                         }
                         sender.sendMessage(ChatColor.GREEN + String.format("/tc ls %d for next page", page+1));
                     } else {
                         sender.sendMessage("No more schedules");
                     }
                 }
             } else if (args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("bc")) {
                 broadcast = !broadcast;
                 if (broadcast) {
                     sender.sendMessage(ChatColor.GREEN + "Time- and weather-change warnings have been turned on!");
                 } else {
                     sender.sendMessage(ChatColor.GREEN + "Time- and weather-change warnings have been turned off!");
                 }
                 conf.setProperty("broadcast", broadcast);
                 conf.save();
             } else if (args[0].equalsIgnoreCase("schedres") || args[0].equalsIgnoreCase("sr")) {
                 if (l == 1) {
                     sender.sendMessage(ChatColor.GREEN + "Schedule-resolutions (in secs):");
                     sender.sendMessage(ChatColor.YELLOW + String.format("Default-resolution: %d", conf.getInt("resolution.default", 15)));
                     for (Schedule sched : schedList) {
                         sender.sendMessage(ChatColor.YELLOW + String.format("%s: %d", sched.world.getName(), sched.schedRes));
                     }
                     
                 } else if (l == 3) {
                     int newRes = toInt(args[2]);
                     if (newRes >= 1) {
                         String cmp = args[1].toLowerCase();
                         boolean found = false;
                         Schedule rel = null;
                         for (Schedule sched : schedList) {
                             if (sched.world.getName().equalsIgnoreCase(cmp)) {
                                 rel = sched;
                                 found = true;
                                 break;
                             }
                         }
                         if (cmp.equalsIgnoreCase("default")) {
                             found = true;
                         }
                         if (!found) {
                             sender.sendMessage(ChatColor.RED + "No such world: " + args[1]);
                             return true;
                         }
                         sender.sendMessage(ChatColor.GREEN + "Resolution set to " + newRes);
                         conf.setProperty("resolution." + cmp, newRes);
                         conf.save();
                         if (rel != null) {
                             rel.reload();
                         }
                    } else if (newRes == 0) {
                        if (args[1].equalsIgnoreCase("default")) {
                            sender.sendMessage(ChatColor.RED + "Default resolution cannot be set to default... nice try though.");
                            return true;
                        }
                        conf.removeProperty("resolution."+args[1].toLowerCase());
                        conf.save();
                        sender.sendMessage(ChatColor.GREEN + "Resolution set to default");
                     } else {
                         sender.sendMessage(ChatColor.RED + "Resolution must be at least 1 s");
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "To see resolutions: /tc sr, to set: /tc sr worldname res");
                 }
             }
             return true;
         }
         return false;
 
     }
     public String humanReadable(String in) {
         in = "," + in;
         in = in.replace(",0", ":cont").replaceAll(",1", ",day").replaceAll(",2", ",night").replaceAll(",3", ",dusk").replaceAll(",4", ",dawn");
         return in.substring(1);
     }
 
     public int translateState(String in) {
         for (int i = 0; i < states.length; i++) {
             if (states[i].equalsIgnoreCase(in)) {
                 return i;
             }
         }
         return -1;
     }
     class Schedule extends Thread {
         World world;
         boolean []reset;
         int []storm, thunder, modes, lengths; 
         int remaining, current, task, schedRes;
         String schedname;
         boolean newstate;
         TweakedCycle plugin;
         public Schedule(String mode, World world, TweakedCycle plugin) {
             task = -1;
             this.world = world;
             this.plugin = plugin;
             reload();
             setMode(mode);
         }
 
         public void configWeather(String weather, int i) {
             if (weather.contains("s")) {
                 storm[i] = 2; 
             } else if (weather.contains("d")) {
                 storm[i] = 1;
             } else {
                 storm[i] = 0;
             }
             if (weather.contains("t")) {
                 thunder[i] = 2;
             } else if (weather.contains("c")) {
                 thunder[i] = 1;
             } else {
                 thunder[i] = 0;
             }
         }
 
         public void setMode(String newMode) {
             newstate = true;
             String out = "";
             String weather = "";
             String mode = "";
             String []split = newMode.toLowerCase().split("-");
             if (split.length == 2) {
                 weather = split[1];
                 mode = split[0];
             } else {
                 mode = newMode;
             }
             int i = translateState(mode);
             if (i == -1) {
                 i = toInt(mode,-1);
             }
             if (i != -1) {
                 modes = new int[1];
                 lengths = new int[1];
                 reset = new boolean[1];
                 storm = new int[1];
                 thunder = new int[1];
                 configWeather(weather, 0);
                 reset[0] = false;
                 modes[0] = i;
                 if (i == 5) {
                     setRes(world.getName(), 1);
                 }
                 if (i <= 2) {
                     lengths[0] = (int) 15./schedRes;
                 } else {
                     lengths[0] = (int) 60./schedRes;
                 }
                 if (lengths[0] < 1) {
                     lengths[0] = 1;
                 }
                 schedname = newMode;
             } else{
                 if (checkInt(mode)) {
                     out = mode;
                     int temp = toInt(out);
                     if (temp > states.length || temp < 0) {
                         temp = 0;
                     }
                     schedname = states[temp];
                 } else {
                     out = conf.getString("modes." + mode);
                     if (out == null) {
                         out = "0";
                         schedname = "Undefined sched";
                     } else {
                         schedname = mode;
                     }
                 }
                 makeSettingsArrays(out);
             }
             remaining = 0;
             current = -1;
             setTime(true);
         }
         
         public void reload() {
             if (task != -1) {
                 getServer().getScheduler().cancelTask(task);
             }
             schedRes = getRes(world.getName());
             task = getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, schedRes*30, schedRes*30);
         }
         
         public int[] makeSettingsArrays(String input) {
             String []split = input.split(",");
             String []values;
             modes = new int[split.length];
             reset = new boolean[split.length];
             lengths = new int[split.length];
             thunder = new int[split.length];
             storm = new int[split.length];
             for (int i = 0; i < split.length; i++) {
                 values = split[i].split(":");
                 if (values.length >= 1) {
                     String weather = "";
                     String []split2 = split[i].split("-");
                     if (split2.length == 2) {
                         values[0] = split2[0];
                         weather = split2[1];
                     }
                     configWeather(weather, i);
                     reset[i] = false;
                     lengths[i] = 1;
                     int tmp = translateState(values[0]);
                     if (tmp == -1) {
                         modes[i] = toInt(values[0]);                        
                     } else {
                         modes[i] = tmp;
                     }
                     if (values.length == 2) {
                         lengths[i] = toInt(values[1].replace("r", ""));
                         reset[i] = values[1].contains("r");
                     }
                     if (lengths[i] <= 0) {
                         lengths[i] = 1;
                     }
                 } else {
                     modes[i] = 0;
                     lengths[i] = 1;
                     reset[i] = false;
                     thunder[i] = 0;
                     storm[i] = 0;
                 }
             }
             return modes;
         }
         public void run() {
             setTime(false);
         }
 
         public void setTime(boolean force) {
             if (remaining <= 0) {
                 current = (current + 1)%modes.length;
                 remaining = lengths[current];
                 force = true;
             }
             if (modes[current] == 5) {
                 force = true;
             }
             if (thunder[current] == 2) {
                 world.setThundering(true);
             } else if (thunder[current] == 1) {
                 world.setThundering(false);
             }
             if (storm[current] == 2) {
                 world.setStorm(true);
             } else if (storm[current] == 1){
                 world.setStorm(false);
             }
             if (broadcast && checkRemaining(remaining*schedRes, world.getName())) {
                 String ns = "";
                 int next = (current+1)%modes.length;
                 if (storm[next] == 2) {
                     if (thunder[next] == 2 && thunder[next] != thunder[current]) {
                         ns += ChatColor.BLUE + "Thunderstorm ";
                     } else if (thunder[next] == 1 && thunder[next] != thunder[current]) {
                         ns += ChatColor.BLUE + "Storm ";
                     }
                 } else if (storm[next] == 1 && storm[next] != storm[current]) {
                     ns += ChatColor.YELLOW + "Clear ";
                 }
                 if (modes[current] != modes[next] && modes[next] != 0) {
                     if (modes[next] != 0 && !(ns.length() != 0) && !ns.equalsIgnoreCase("") 
                             && !ns.equalsIgnoreCase(ChatColor.YELLOW+"clear ")) {
                         ns += ChatColor.YELLOW + "and ";
                     }
                     if (modes[next] == 1) {
                         ns += ChatColor.YELLOW + "Day";
                     } else if (modes[next] == 2) {
                         ns += ChatColor.BLUE + "Night";
                     } else if (modes[next] == 3) {
                         ns += ChatColor.GOLD + "Dusk";
                     } else if (modes[next] == 4) {
                         ns += ChatColor.GOLD + "Dawn";
                     }   
                 }
                 if (((thunder[next] != 0 && thunder[next] != thunder[current] )
                         || (storm[next] != 0 && storm[next] != storm[current]) 
                         || (modes[next] != 0 && modes[current] != modes[(current+1)%modes.length]) )
                         && (!ns.equalsIgnoreCase(""))) {
                     for (Player pl : getServer().getOnlinePlayers()) {
                         if (pl.getWorld() == world) {
                             pl.sendMessage(String.format("%s %sin %d seconds!", ns, ChatColor.YELLOW, remaining*schedRes));
                         }
                     }
                 }
             }
             remaining--;
             if (!force && !reset[current] && !newstate) {
                 return;
             }
             newstate = false;
             if (modes[current] ==  1) {
                 world.setTime(5775);
             } else if (modes[current] == 2) {
                 world.setTime(17775);
             } else if (modes[current] == 3) {
                 world.setTime(11975);
             } else if (modes[current] == 4) {
                 world.setTime(22900);
             } else if (modes[current] == 5) {
                 Calendar cal = Calendar.getInstance();
                 world.setTime((long) ((cal.get(Calendar.HOUR)*1000 + cal.get(Calendar.MINUTE)*16.66 + cal.get(Calendar.SECOND)*0.277)+4000)%24000 + shiftTime*1000);
             }
         }
         public String getMode() {
             return schedname;
         }
     }
     public boolean checkRemaining(int in, String world) {
         for (int i = 1; i < 5; i*=2) {
             if (in <= i*15 && in > i*15-getRes(world)) {
                 return true;
             }
         }
         return false;
     }
     public boolean checkMode(String in) {
         char chars[] = in.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             char c = chars[i];
             if (!(Character.isDigit(c) || c == ',' || c == ':' || c == 'r' || c == 's' || c == 't' || c == 'd' || c == 'c' || c == '-')) {
                 return false;
             }
         }
         return true;
     }
 
     public int toInt(String in) {
         int out = 0;
         if (checkInt(in) && !in.equalsIgnoreCase("")) {
             out = Integer.parseInt(in);
         }
         return out;
     }
     
     public int toInt(String in, int def) {
         if (checkInt(in) && !in.equalsIgnoreCase("")) {
             def = Integer.parseInt(in);
         }
         return def;
     }
     /**
      * Check if the string is valid as an int (accepts signs).
      *
      * @param in The string to be checked
      * @return boolean Success
      */
     public boolean checkInt(String in) {
         char chars[] = in.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             if (!(Character.isDigit(chars[i]))) {
                 return false;
             }
         }
         return true;
     }
     /**
      * Basic Permissions-setup, see more here: https://github.com/TheYeti/Permissions/wiki/API-Reference
      */
     private void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (this.permissions == null) {
             if (test != null) {
                 this.permissions = ((Permissions) test).getHandler();
             } else {
                 System.out.println(ChatColor.YELLOW
                         + "Permissons not detected - defaulting to OP!");
             }
         }
     }
     class InitialLoader extends Thread {
         public void start() {
             reloadWorlds();
         }
     }
 }
