 package net.krinsoft.jobsuite.commands;
 
 import net.krinsoft.jobsuite.Job;
 import net.krinsoft.jobsuite.JobCore;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.List;
 
 /**
  * @author krinsdeath
  */
 public class JobAddItemCommand extends JobCommand {
 
     public JobAddItemCommand(JobCore instance) {
         super(instance);
         setName("JobSuite: Add Item");
         setCommandUsage("/job additem [type[:data]] [amount]");
         addCommandExample("/job additem stone 64 -- Request 64 stone");
         addCommandExample("/job additem 17:1 32  -- Request 32 pine wood logs");
         addCommandExample("/job additem 263:1 64 -- Request 64 charcoal");
         setArgRange(2, 2);
         addKey("jobsuite additem");
         addKey("job additem");
         addKey("js additem");
         addKey("jobsuite ai");
         addKey("job ai");
         addKey("js ai");
         setPermission("jobsuite.additem", "Adds a required job item.", PermissionDefault.TRUE);
     }
 
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         Job job = manager.getQueuedJob(sender.getName());
         if (job != null) {
             Material type;
             byte data = 0;
             try {
                 String arg = args.get(0);
                 if (arg.contains(":")) {
                     arg = args.get(0).split(":")[0];
                     data = Byte.parseByte(args.get(0).split(":")[1]);
                 }
                 type = Material.getMaterial(Integer.parseInt(arg));
             } catch (NumberFormatException e) {
                 type = Material.matchMaterial(args.get(0));
             } catch (ArrayIndexOutOfBoundsException e) {
                 error(sender, "Something went wrong with your input.");
                 return;
             }
             if (type == null) {
                 error(sender, "Unknown item type.");
                 return;
             }
            if (type == Material.PISTON_STICKY_BASE) {
                data = 7;
            }
             int amount;
             try {
                 amount = Integer.parseInt(args.get(1));
             } catch (NumberFormatException e) {
                 error(sender, "Error parsing argument: expected number");
                 return;
             }
             if (type.getMaxStackSize() < amount) {
                 error(sender, "Please split the items up into multiple entries.");
                 return;
             }
             ItemStack item = new ItemStack(type, amount, (short) 0, data);
             int id = job.addItem(item);
            message(sender, "Item added at index '" + id + "': " + item.getType().toString());
             message(sender, "View item info: " + ChatColor.DARK_AQUA + "/job info this " + id);
             message(sender, "Add an enchantment: " + ChatColor.DARK_AQUA + "/job addenchant " + id + " [enchantment] [level]");
             message(sender, "Remove this item: " + ChatColor.DARK_AQUA + "/job remitem " + id);
             message(sender, "Add more items: " + ChatColor.DARK_AQUA + "/job additem [type] [amount]");
             message(sender, "Post the job: " + ChatColor.GOLD + "/job post");
         } else {
             error(sender, "You aren't currently making a job.");
         }
     }
 }
