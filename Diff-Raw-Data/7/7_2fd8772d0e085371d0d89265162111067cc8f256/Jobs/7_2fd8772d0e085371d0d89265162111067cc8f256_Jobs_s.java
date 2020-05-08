 package co.mcme.jobs;
 
 import co.mcme.jobs.commands.JobAdminCommand;
 import co.mcme.jobs.util.Util;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Jobs extends JavaPlugin implements Listener {
 
     private final Logger log = Logger.getLogger("Minecraft");
     static Configuration conf;
     public static HashMap<String, Job> runningJobs = new HashMap();
     public static HashMap<String, Job> notRunningJobs = new HashMap();
     public static ArrayList<World> protected_worlds = new ArrayList();
     public static HashMap<Job, World> opened_worlds = new HashMap();
     public static HashMap<Job, Long> timedout_waiting = new HashMap();
     public static boolean debug = false;
 
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
         setupConfig();
         debug = getConfig().getBoolean("general.debug");
         getCommand("jobadmin").setExecutor(new JobAdminCommand());
         getServer().getPluginManager().registerEvents(this, this);
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 Util.debug("Starting cleanup");
                 Cleanup.scheduledCleanup();
             }
         }, 0, (5 * 60) * 20);
     }
 
     @Override
     public void onDisable() {
         for (Job job : runningJobs.values()) {
             try {
                 job.writeToFile();
             } catch (IOException ex) {
                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public void setupConfig() {
         conf = getConfig();
         getConfig().options().copyDefaults(true);
         saveConfig();
         protected_worlds = (ArrayList) getConfig().getStringList("protect_worlds");
         loadJobs();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if ((args.length > 0) && ((sender instanceof Player))) {
             Player player = (Player) sender;
             if (label.equalsIgnoreCase("job")) {
                 if (args.length > 0) {
                     if (args[0].equalsIgnoreCase("reload") && player.hasPermission("jobs.reload")) {
                         for (Job job : runningJobs.values()) {
                             try {
                                 job.writeToFile();
                             } catch (IOException ex) {
                                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                         runningJobs = new HashMap();
                         player.sendMessage(ChatColor.GRAY + "Loaded " + co.mcme.jobs.files.Loader.loadJobs() + " old job(s) from file.");
                         Util.info(runningJobs.size() + " of which are active.");
                     }
                     if (args[0].equalsIgnoreCase("write") && player.hasPermission("jobs.write")) {
                         if (runningJobs.containsKey(args[1])) {
                             Job writing = runningJobs.get(args[1]);
                             try {
                                 writing.writeToFile();
                             } catch (IOException ex) {
                                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                     }
 
                     if (args.length == 2 && player.hasPermission("jobs.run")) {
                         if (args[0].equalsIgnoreCase("start")) {
                             if (args[1] != null) {
                                 String jobname = args[1];
                                 if (notRunningJobs.containsKey(jobname)) {
                                     storeJob(jobname, player.getName(), "reopen");
                                     player.sendMessage(ChatColor.GRAY + "Successfully reopend the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                                 } else {
                                     storeJob(jobname, player.getName(), "new");
                                     player.sendMessage(ChatColor.GRAY + "Successfully created the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "You must provide a job name.");
                             }
                         }
                         if (args[0].equalsIgnoreCase("stop")) {
                             if (args[1] != null) {
                                 String jobname = args[1];
                                 if (runningJobs.containsKey(jobname)) {
                                     storeJob(jobname, player.getName(), "remove");
                                     player.sendMessage(ChatColor.GRAY + "Successfully closed the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                                 } else {
                                     player.sendMessage(ChatColor.RED + "No job found by that name.");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "You must provide a job name.");
                             }
                         }
                     }
                     if (args[0].equalsIgnoreCase("check")) {
                         if (player.hasPermission("jobs.check")) {
                             if (runningJobs.size() > 0) {
                                 StringBuilder out = new StringBuilder();
                                 out.append(ChatColor.GRAY).append("Running Jobs:");
                                 for (String jobName : runningJobs.keySet()) {
                                     Job job = runningJobs.get(jobName);
                                     out.append("\n").append(ChatColor.AQUA).append(jobName).append(ChatColor.GRAY).append(" with ").append(job.getAdmin().getName()).append(" (").append(job.getWorkers().size()).append(")");
                                 }
                                 player.sendMessage(out.toString());
                             } else {
                                 player.sendMessage(ChatColor.GRAY + "No jobs currently running.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You don't have permission.");
                         }
                     }
                     if (args[0].equalsIgnoreCase("join")) {
                         if (player.hasPermission("jobs.join")) {
                             if (runningJobs.size() > 0) {
                                 if (args.length > 1) {
                                     if (runningJobs.containsKey(args[1])) {
                                         Job jobToJoin = runningJobs.get(args[1]);
                                         if (jobToJoin.addWorker(player)) {
                                             player.sendMessage(ChatColor.GRAY + "You have joined the job " + ChatColor.AQUA + jobToJoin.getName());
                                         } else {
                                             player.sendMessage(ChatColor.RED + "You cannot be added to that job.");
                                         }
                                     } else {
                                         player.sendMessage(ChatColor.RED + "No job ruuning by the name of `" + args[1] + "`");
                                     }
                                 } else {
                                     player.sendMessage(ChatColor.RED + "What job would you like to join?");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "No jobs currently running.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You do not have permission.");
                         }
                     }
                     if (args[0].equalsIgnoreCase("warpto")) {
                         if (runningJobs.size() > 0) {
                             if (args.length > 1) {
                                 if (runningJobs.containsKey(args[1])) {
                                     Job jobToJoin = runningJobs.get(args[1]);
                                     player.teleport(jobToJoin.getWarp());
                                     player.sendMessage(ChatColor.GRAY + "Warped to " + ChatColor.AQUA + jobToJoin.getName());
                                 } else {
                                     player.sendMessage(ChatColor.RED + "No job ruuning by the name of `" + args[1] + "`");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "What job would you like to warp to?");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "No jobs currently running.");
                         }
                     }
                     if (args[0].equalsIgnoreCase("info")) {
                         if (runningJobs.size() > 0) {
                             if (args.length > 1) {
                                 if (runningJobs.containsKey(args[1])) {
                                     Job jobToJoin = runningJobs.get(args[1]);
                                     player.sendMessage(getJobInfo(jobToJoin));
                                 } else if (notRunningJobs.containsKey(args[1])) {
                                     Job jobToJoin = notRunningJobs.get(args[1]);
                                     player.sendMessage(getJobInfo(jobToJoin));
                                 } else {
                                     player.sendMessage(ChatColor.RED + "No job ruuning by the name of `" + args[1] + "`");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "What job would you like to get info on?");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "No jobs currently running.");
                         }
                     }
                     if (args[0].equalsIgnoreCase("archive")) {
                         if (player.hasPermission("jobs.check")) {
                             if (notRunningJobs.size() > 0) {
                                 StringBuilder out = new StringBuilder();
                                 out.append(ChatColor.GRAY).append("Running Jobs:");
                                 for (String jobName : notRunningJobs.keySet()) {
                                     Job job = notRunningJobs.get(jobName);
                                     out.append("\n").append(ChatColor.AQUA).append(jobName).append(ChatColor.GRAY).append(" with ").append(job.getAdmin().getName()).append(" (").append(job.getWorkers().size()).append(")");
                                 }
                                 player.sendMessage(out.toString());
                             } else {
                                 player.sendMessage(ChatColor.GRAY + "No jobs currently running.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You don't have permission.");
                         }
                     }
                 }
             }
         }
         return true;
     }
 
     public void storeJob(String jobname, String admin, String status) {
         Player p = Bukkit.getPlayer(admin);
         if (status.equalsIgnoreCase("new")) {
             Location adminloc = Bukkit.getPlayer(admin).getLocation();
             int newx = (int) adminloc.getX();
             int newy = (int) adminloc.getY();
             int newz = (int) adminloc.getZ();
             adminloc.setX(newx);
             adminloc.setY(newy);
             adminloc.setZ(newz);
             Job newjob = new Job(jobname, admin, true, adminloc, adminloc.getWorld().getName());
             runningJobs.put(jobname, newjob);
             try {
                 newjob.writeToFile();
             } catch (IOException ex) {
                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
             }
             opened_worlds.put(newjob, adminloc.getWorld());
             for (Player targetP : Bukkit.getOnlinePlayers()) {
                targetP.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + admin + " has started a new job called '" + jobname + "'");
                targetP.playSound(targetP.getLocation(), Sound.ENDERDRAGON_DEATH, 100, 100);
             }
         }
         if (status.equalsIgnoreCase("reopen")) {
             if (notRunningJobs.containsKey(jobname)) {
                 Job oldjob = notRunningJobs.get(jobname);
                 oldjob.setAdmin(Bukkit.getOfflinePlayer(admin));
                 oldjob.setStatus(true);
                 runningJobs.put(oldjob.getName(), oldjob);
                 notRunningJobs.remove(jobname);
                 opened_worlds.put(oldjob, oldjob.getWorld());
                 try {
                     oldjob.writeToFile();
                 } catch (IOException ex) {
                     Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
         if (status.equalsIgnoreCase("remove")) {
             if (runningJobs.containsKey(jobname)) {
                 Job oldjob = runningJobs.get(jobname);
                 oldjob.setStatus(false);
                 notRunningJobs.put(oldjob.getName(), oldjob);
                 runningJobs.remove(jobname);
                 opened_worlds.remove(oldjob);
                 try {
                     oldjob.writeToFile();
                 } catch (IOException ex) {
                     Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     private void loadJobs() {
         Util.info("Loaded " + co.mcme.jobs.files.Loader.loadJobs() + " old job(s) from file.");
         for (Job job : runningJobs.values()) {
             getServer().getPluginManager().registerEvents(job, this);
         }
         Util.info(runningJobs.size() + " of which are active.");
     }
 
     @EventHandler
     public void onPlace(BlockPlaceEvent event) {
         World happenedin = event.getBlock().getWorld();
         if (protected_worlds.contains(happenedin) && !event.getPlayer().hasPermission("jobs.ignorestatus")) {
             if (opened_worlds.containsValue(happenedin)) {
                 OfflinePlayer toCheck = Bukkit.getOfflinePlayer(event.getPlayer().getName());
                 for (Job job : opened_worlds.keySet()) {
                     if (job.getWorld().equals(happenedin)) {
                         event.setCancelled(!job.isWorking(toCheck));
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void onBreak(BlockBreakEvent event) {
         World happenedin = event.getBlock().getWorld();
         if (protected_worlds.contains(happenedin) && !event.getPlayer().hasPermission("jobs.ignorestatus")) {
             if (opened_worlds.containsValue(happenedin)) {
                 OfflinePlayer toCheck = Bukkit.getOfflinePlayer(event.getPlayer().getName());
                 for (Job job : opened_worlds.keySet()) {
                     if (job.getWorld().equals(happenedin)) {
                         event.setCancelled(!job.isWorking(toCheck));
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent event) {
         if (runningJobs.size() > 0) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "The is a job running! Use /job check to find out what it is!");
         }
     }
 
     private String getJobInfo(Job job) {
         StringBuilder out = new StringBuilder();
         out.append(ChatColor.GRAY).append(job.getName()).append("\n");
         out.append("Started by: ").append(ChatColor.AQUA).append(job.getAdmin().getName()).append("\n").append(ChatColor.GRAY);
         out.append("Started on: ").append(ChatColor.AQUA).append(new Date(job.getRunningSince()).toGMTString()).append("\n").append(ChatColor.GRAY);
         out.append("Location: ").append(ChatColor.AQUA).append(job.getWorld().getName()).append(" (x: ").append(job.getWarp().getX()).append(", y: ").append(job.getWarp().getY()).append(", z: ").append(job.getWarp().getZ()).append(")").append("\n").append(ChatColor.GRAY);
         String status = (job.getStatus()) ? ChatColor.GREEN + "OPEN" : ChatColor.RED + "CLOSED";
         out.append("Status: ").append(status);
         return out.toString();
     }
 
     public String jobExists(String name) {
         String out = "never";
         if (runningJobs.containsKey(name)) {
             out = "active";
         }
         if (notRunningJobs.containsKey(name)) {
             out = "dormant";
         }
         if (!(notRunningJobs.containsKey(name) || runningJobs.containsKey(name))) {
             out = "never";
         }
         return out;
     }
 
     static void scheduleAdminTimeout(Job job) {
         Long time = System.currentTimeMillis();
         timedout_waiting.put(job, time);
     }
 
     public static void disableJob(Job job) {
         if (runningJobs.containsValue(job)) {
             job.setStatus(false);
             notRunningJobs.put(job.getName(), job);
             runningJobs.remove(job.getName());
             opened_worlds.remove(job);
             try {
                 job.writeToFile();
             } catch (IOException ex) {
                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public static void enableJob(Job job) {
         if (notRunningJobs.containsValue(job)) {
             job.setStatus(true);
             runningJobs.put(job.getName(), job);
             notRunningJobs.remove(job.getName());
             opened_worlds.put(job, job.getWorld());
             try {
                 job.writeToFile();
             } catch (IOException ex) {
                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
