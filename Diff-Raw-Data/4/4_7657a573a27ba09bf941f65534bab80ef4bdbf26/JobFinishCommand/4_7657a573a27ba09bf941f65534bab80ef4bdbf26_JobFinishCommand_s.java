 package net.krinsoft.jobsuite.commands;
 
 import net.krinsoft.jobsuite.Job;
 import net.krinsoft.jobsuite.JobCore;
 import net.krinsoft.jobsuite.JobItem;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.List;
 
 /**
  * @author krinsdeath
  */
 public class JobFinishCommand extends JobCommand {
 
     public JobFinishCommand(JobCore instance) {
         super(instance);
         setName("JobSuite: Finish Job");
         setCommandUsage("/job finish [job id]");
         setArgRange(1, 1);
         addKey("jobsuite finish");
         addKey("job finish");
         addKey("js finish");
         setPermission("jobsuite.finish", "Finishes the specified job.", PermissionDefault.TRUE);
     }
 
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         try {
             Job job = manager.getJob(Integer.parseInt(args.get(0)));
            if (job != null) {
                if (job.hasRequiredItems(sender) && !job.isFinished()) {
                     message(sender, "You have all of the required items.");
                     if (sender instanceof Player) {
                         for (JobItem jItem : job.getItems()) {
                             ((Player)sender).getInventory().remove(jItem.getItem());
                         }
                     }
                     manager.finishJob(sender, job);
                     if (sender instanceof Player) {
                         message(sender, "You've been rewarded for your work! " + plugin.getBank().getFormattedAmount((Player)sender, job.getReward(), -1) + ".");
                     }
                 } else {
                     message(sender, "You do not yet have all of the required items.");
                 }
             } else {
                 error(sender, "Couldn't find a matching job.");
             }
         } catch (NumberFormatException e) {
             error(sender, "Error parsing argument: expected number");
         }
     }
 }
