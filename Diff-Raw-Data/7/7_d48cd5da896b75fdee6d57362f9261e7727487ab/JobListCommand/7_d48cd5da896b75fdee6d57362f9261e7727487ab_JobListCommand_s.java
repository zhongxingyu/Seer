 package net.krinsoft.jobsuite.commands;
 
 import net.krinsoft.jobsuite.Job;
 import net.krinsoft.jobsuite.JobCore;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.List;
 
 /**
  * @author krinsdeath
  */
 public class JobListCommand extends JobCommand {
 
     public JobListCommand(JobCore instance) {
         super(instance);
         setName("JobSuite: List Jobs");
         setCommandUsage("/job list [page]");
         setArgRange(0, 1);
         addKey("jobsuite list");
         addKey("job list");
         addKey("js list");
        setPermission("jobsuite.listjobs", "Lists active jobs.", PermissionDefault.TRUE);
     }
 
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         int page = 1;
         if (args.size() > 0) {
             try {
                 page = Integer.parseInt(args.get(0));
             } catch (NumberFormatException e) {
                 page = 1;
             }
         }
         List<Job> jobs = manager.getJobs();
         int pages = jobs.size() / 6;
         int bound = jobs.size();
         if (page >= pages) {
             page = pages;
         } else {
             bound = (page * 6) + 6;
         }
         if (page < 0) {
             page = 1;
         }
         if (jobs.size() == 0) {
             message(sender, "There are no jobs available.");
             return;
         }
         message(sender, "Job List [" + (page+1) + "/" + (pages+1) + "] - Total: " + jobs.size());
         message(sender, "Lock: " + ChatColor.AQUA + "/job lock [job id]");
         for (int i = page * 6; i < bound; i++) {
             Job job = jobs.get(i);
             if (!job.isFinished()) {
                message(sender, ChatColor.GOLD + "[ID: " + job.getId() + "] " + job.getName() + ChatColor.WHITE + ": " + ChatColor.AQUA + job.getDescription());
             }
         }
     }
 }
