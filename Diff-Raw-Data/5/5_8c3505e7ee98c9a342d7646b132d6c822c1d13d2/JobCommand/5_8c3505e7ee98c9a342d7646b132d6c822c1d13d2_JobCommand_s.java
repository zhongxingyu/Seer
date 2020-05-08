 package co.mcme.jobs.commands;
 
 import co.mcme.jobs.Job;
 import co.mcme.jobs.Jobs;
 import static co.mcme.jobs.Jobs.notRunningJobs;
 import static co.mcme.jobs.Jobs.runningJobs;
 import co.mcme.jobs.util.Util;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabExecutor;
 import org.bukkit.entity.Player;
 import org.bukkit.util.ChatPaginator;
 
 public class JobCommand implements TabExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if ((args.length > 0) && ((sender instanceof Player))) {
             Player player = (Player) sender;
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
                     player.sendMessage(ChatColor.GRAY + "Loaded " + co.mcme.jobs.files.Loader.loadActiveJobs() + " old job(s) from file.");
                     Util.info(runningJobs.size() + " of which are active.");
                     return true;
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
                     return true;
                 }
 
                 if (args.length >= 2 && player.hasPermission("jobs.run")) {
                     if (args[0].equalsIgnoreCase("start")) {
                         if (args[1] != null) {
                             String jobname = args[1];
                             boolean inviteOnly = false;
                             if (args.length == 3) {
                                 if (args[2].equalsIgnoreCase("private")) {
                                     inviteOnly = true;
                                 }
                             }
                             if (!runningJobs.containsKey(jobname)) {
                                 Jobs.storeJob(jobname, player.getName(), "new", inviteOnly);
                                 player.sendMessage(ChatColor.GRAY + "Successfully created the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                             } else {
                                 player.sendMessage(ChatColor.RED + "A job by that name is already running.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You must provide a job name.");
                         }
                         return true;
                     }
                     if (args[0].equalsIgnoreCase("stop")) {
                         if (args[1] != null) {
                             String jobname = args[1];
                             if (runningJobs.containsKey(jobname)) {
                                 Jobs.storeJob(jobname, player.getName(), "remove", runningJobs.get(jobname).isInviteOnly());
                                 player.sendMessage(ChatColor.GRAY + "Successfully closed the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                             } else {
                                 player.sendMessage(ChatColor.RED + "No job found by that name.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You must provide a job name.");
                         }
                         return true;
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
                     return true;
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
                                 player.sendMessage(ChatColor.RED + "You must provide the name of the job you would like to join.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "No jobs currently running.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "You do not have permission.");
                     }
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("warpto")) {
                     if (player.hasPermission("jobs.join")) {
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
                                 player.sendMessage(ChatColor.RED + "You must provide the name of the job you would like to warp to.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "No jobs currently running.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "You do not have permission.");
                     }
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("info")) {
                     if (args.length > 1) {
                         if (runningJobs.containsKey(args[1])) {
                             Job jobToJoin = runningJobs.get(args[1]);
                             player.sendMessage(Jobs.getJobInfo(jobToJoin));
                         } else if (notRunningJobs.containsKey(args[1])) {
                             Job jobToJoin = notRunningJobs.get(args[1]);
                             player.sendMessage(Jobs.getJobInfo(jobToJoin));
                         } else {
                             player.sendMessage(ChatColor.RED + "No job found by the name of `" + args[1] + "`");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "What job would you like to get info on?");
                     }
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("archive")) {
                     if (player.hasPermission("jobs.check")) {
                         if (notRunningJobs.size() > 0) {
                             StringBuilder out = new StringBuilder();
                             int pageNum = 1;
                             boolean first = true;
                             for (String jobName : notRunningJobs.keySet()) {
                                 Job job = notRunningJobs.get(jobName);
                                 if (!first) {
                                     out.append("\n");
                                 }
                                 out.append(ChatColor.AQUA).append(jobName).append(ChatColor.GRAY).append(" with ").append(job.getAdmin().getName()).append(" (").append(job.getWorkers().size()).append(")");
                                 if (first) {
                                     first = false;
                                 }
                             }
                             if (args.length > 1) {
                                 pageNum = Integer.valueOf(args[1]);
                             }
                             ChatPaginator.ChatPage page = ChatPaginator.paginate(out.toString(), pageNum, ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH, 8);
                             player.sendMessage(ChatColor.AQUA + "Job Archive Page: " + page.getPageNumber() + " of " + page.getTotalPages());
                             player.sendMessage(page.getLines());
                         } else {
                             player.sendMessage(ChatColor.GRAY + "No jobs found in archive.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "You don't have permission.");
                     }
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
         if (args[0].equalsIgnoreCase("archive")) {
             return new ArrayList();
         }
         if (args[0].equalsIgnoreCase("check")) {
             return new ArrayList();
         }
         if (args[0].equalsIgnoreCase("start")) {
             List<String> jobs = new ArrayList();
             jobs.addAll(Jobs.notRunningJobs.keySet());
             return jobs;
         }
         if (args[0].equalsIgnoreCase("info")) {
             List<String> jobs = new ArrayList();
             jobs.addAll(Jobs.runningJobs.keySet());
             jobs.addAll(Jobs.notRunningJobs.keySet());
             return jobs;
         } else {
             List<String> jobs = new ArrayList();
             jobs.addAll(Jobs.runningJobs.keySet());
             return jobs;
         }
     }
 }
