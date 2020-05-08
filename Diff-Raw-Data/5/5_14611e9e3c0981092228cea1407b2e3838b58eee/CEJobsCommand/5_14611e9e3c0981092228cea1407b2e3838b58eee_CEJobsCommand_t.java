 package me.ccattell.plugins.completeeconomy.commands;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import static me.ccattell.plugins.completeeconomy.CompleteEconomy.plugin;
 import me.ccattell.plugins.completeeconomy.database.CEQueryFactory;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Charlie
  */
 public class CEJobsCommand implements CommandExecutor {
 
     CEQueryFactory qf = new CEQueryFactory();
     public String prefix = plugin.configs.getJobConfig().getString("Jobs.Prefix");
     public String moduleName = ChatColor.BLUE + prefix + ChatColor.RESET + " ";
     public boolean JobsEnabled = plugin.configs.getJobConfig().getBoolean("Jobs.Enabled");
     public boolean DeleteOnQuit = plugin.configs.getJobConfig().getBoolean("Jobs.DeleteOnQuit");
     public String ReJoinPercent = plugin.configs.getJobConfig().getString("Jobs.ReJoinPercent");
     public String found_job;
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 
         HashMap<String, Object> seta = new HashMap<String, Object>();
         HashMap<String, Object> setw = new HashMap<String, Object>();
         if (cmd.getName().equalsIgnoreCase("jobs")) {
             // don't do anything unless it's our command
             if(JobsEnabled){
                Set<String> jobsList = plugin.configs.getJobList().getConfigurationSection("Jobs").getKeys(false);
 
                 Player player;
                 if (sender instanceof Player) {
                     player = (Player) sender;
                 } else {
                     sender.sendMessage(moduleName + "The jobs command cannot be used from the console!");
                     return true;
                 }
                 if (!sender.hasPermission("ce.jobs")) {
                     sender.sendMessage(moduleName + "You don't have permission to use jobs!");
                     return true;
                 } else {
                     if (args.length == 0) { // if args.length != 2 - incorrect number of arguments?
                         player.sendMessage(moduleName + "Incorrect number of arguments");
                         return true;
                     }
                     if (args[0].equalsIgnoreCase("list") && args.length == 1) {
                         player.sendMessage(moduleName + "Available jobs:");
                         for (String job : jobsList) {
                             player.sendMessage("    " + job);
                         }
                         return true;
                     } else if (args[0].equalsIgnoreCase("info") && args.length == 2) {
                         String job = args[1];
                         for (String jobs : jobsList) {
                             if (jobs.equalsIgnoreCase(job)) {
                                 found_job = jobs;
                                 break;
                             }
                         }
                         if(found_job.equals("")){
                             player.sendMessage(moduleName + "Could not find a job with that name, use /jobs list to find one!");
                             return true;
                         }
                         // check args[1] is in the jobs list
                        List<String> skillList = plugin.configs.getJobList().getStringList("Jobs." + found_job);
                         player.sendMessage(moduleName + "Job description for " + found_job + ":");
                         // loop thru list
                         for (String skill : skillList) {
                             //need to go one more layer deep and get info from skillsConfig on skills found associated with this job
                             // send message
                             player.sendMessage("    " + skill);
                         }
                         return true;
                     } else if (args[0].equalsIgnoreCase("join") && args.length == 2) {
                         String job = args[1];
                         for (String jobs : jobsList) {
                             if (jobs.equalsIgnoreCase(job)) {
                                 found_job = jobs;
                                 break;
                             }
                         }
                         if(found_job.equals("")){
                             player.sendMessage(moduleName + "Could not find a job with that name, use /jobs list to find one!");
                             return true;
                         }
                         String jobCheck = qf.checkPlayerJob(found_job, player.getName());
                         if(jobCheck.equalsIgnoreCase("active")){
                             player.sendMessage(moduleName + "You already have that job!");
                             return true;
                         }else if(jobCheck.equalsIgnoreCase("inactive")){
                             player.sendMessage(moduleName + "Rejoining that job!");
                             setw.put("player_name", player.getName());
                             setw.put("job", found_job);
                             seta.put("status", "active");
                             qf.doUpdate("CEJobs", seta, setw);
                         }else if(jobCheck.equalsIgnoreCase("none")){
                             player.sendMessage(moduleName + "Joining that job!");
                             seta.put("player_name", player.getName());
                             seta.put("job", found_job);
                             seta.put("status", "active");
                             qf.doInsert("CEJobs", seta);
                         }
                         return true;
                     } else if (args[0].equalsIgnoreCase("stats") && args.length == 1) {
                         HashMap<String, String> jobs_stats = qf.getPlayerJobs(player.getName());
                         player.sendMessage(moduleName + "Stats report for " + player.getName());
                         for (Map.Entry<String, String> entry : jobs_stats.entrySet()) {
                             String stats[] = entry.getValue().split(",");
                             player.sendMessage("    Lvl " + stats[0] + " " + entry.getKey() + " " + stats[1] + "/" + stats[2] + " XP");
                         }
                         return true;
                     } else if (args[0].equalsIgnoreCase("quit") && args.length == 2) {
                         String job = args[1];
                         for (String jobs : jobsList) {
                             if (jobs.equalsIgnoreCase(job)) {
                                 found_job = jobs;
                                 break;
                             }
                         }
                         if(found_job.equals("")){
                             player.sendMessage(moduleName + "Could not find a job with that name, use /jobs list to find one!");
                             return true;
                         }
                         if(DeleteOnQuit){
                             player.sendMessage(moduleName + "Deleting " + found_job);
                             setw.put("player_name", player.getName());
                             setw.put("job", found_job);
                             qf.doDelete("CEJobs", setw);
                         }else{
                             player.sendMessage(moduleName + "Quitting " + found_job);
                             setw.put("player_name", player.getName());
                             setw.put("job", found_job);
                             seta.put("status", "inactive");
                             qf.doUpdate("CEJobs", seta, setw);
                         }
                         return true;
                     } else {
                         player.sendMessage(moduleName + "Incorrect number of arguments");
                         // show them the proper usage
                         return false;
                     }
                 }
             }else{
                 sender.sendMessage(moduleName + "Jobs have been disabled");
             }
         }
         return true;
     }
 }
