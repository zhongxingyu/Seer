 package net.krinsoft.jobsuite;
 
 import net.krinsoft.jobsuite.db.Database;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author krinsdeath
  */
 public class JobManager {
     private final JobCore plugin;
     private Database database;
 
     private Map<Integer, Job> jobs = new HashMap<Integer, Job>();
     private List<Job> claims = new ArrayList<Job>();
 
     private int nextJob;
 
     public JobManager(JobCore instance) {
         this.plugin = instance;
     }
 
     public void load() {
         database = new Database(plugin);
         nextJob = plugin.getConfig().getInt("jobs.total", 0);
     }
 
     public void close() {
         persist();
         database.close();
     }
 
     public void persist() {
         PreparedStatement schema = database.prepare("REPLACE INTO jobsuite_schema (id, NEXT_ID) VALUES (?, ?);");
         PreparedStatement jobStatement = database.prepare("REPLACE INTO jobsuite_base (job_id, owner, name, description, expiry, reward, locked_by, finished, claimed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
         PreparedStatement itemStatement = database.prepare("REPLACE INTO jobsuite_items (job_id, item_entry, enchantment_entry, type, amount, enchanted) VALUES (?, ?, ?, ?, ?, ?);");
         PreparedStatement enchStatement = database.prepare("REPLACE INTO jobsuite_enchantments (job_id, enchantment_entry, item_entry, enchantment, power) VALUES (?, ?, ?, ?, ?);");
         try {
             schema.setInt(1, 1);
             schema.setInt(2, nextJob);
             schema.executeUpdate();
             jobStatement.getConnection().setAutoCommit(false);
             for (Map.Entry<Integer, Job> entry : jobs.entrySet()) {
                 Job job = entry.getValue();
                 if (job.isExpired()) {
                     continue;
                 }
                 jobStatement.setInt(1, job.getId());
                 jobStatement.setString(2, job.getOwner());
                 jobStatement.setString(3, job.getName());
                 jobStatement.setString(4, job.getDescription());
                 jobStatement.setLong(5, job.getDuration());
                 jobStatement.setDouble(6, job.getReward());
                 jobStatement.setString(7, job.getLock());
                 jobStatement.setBoolean(8, job.isFinished());
                 jobStatement.setBoolean(9, job.isClaimed());
                 jobStatement.executeUpdate();
                 for (JobItem item : job.getItems()) {
                     itemStatement.setInt(1, job.getId());
                     itemStatement.setInt(2, item.getId());
                     itemStatement.setInt(3, item.hashCode());
                     itemStatement.setString(4, item.getItem().getType().toString());
                     itemStatement.setInt(5, item.getItem().getAmount());
                     itemStatement.setBoolean(6, item.getItem().getEnchantments().size() > 0);
                     itemStatement.executeUpdate();
                     for (Map.Entry<Enchantment, Integer> ench : item.getItem().getEnchantments().entrySet()) {
                         enchStatement.setInt(1, job.getId());
                         enchStatement.setInt(2, item.hashCode());
                        enchStatement.setInt(3, ench.getKey().getId());
                        enchStatement.setInt(4, item.getId());
                         enchStatement.setInt(5, ench.getValue());
                         enchStatement.executeUpdate();
                     }
                 }
             }
             schema.getConnection().commit();
         } catch (SQLException e) {
             plugin.getLogger().warning("An SQLException occurred: " + e.getMessage());
         }
     }
 
     public List<Job> getJobs(CommandSender sender) {
         List<Job> temp = new ArrayList<Job>();
         for (Job job : jobs.values()) {
             if (job.getLock() == null || job.getLock().equals(sender.getName()) || job.getOwner().equals(sender.getName())) {
                 temp.add(job);
             }
         }
         return temp;
     }
 
     /**
      * Gets the specified Job by its ID
      * @param id The ID of the job we're fetching
      * @return The Job and all of its details, otherwise null
      */
     public Job getJob(int id) {
         return jobs.get(id);
     }
 
     public boolean addJob(Job job) {
         Job j = jobs.get(job.getId());
         if (j != null && j.equals(job)) {
             plugin.getLogger().finest("Duplicate job: '" + job.getName() + "'@'" + job.getId() + "'");
             return false;
         } else {
             plugin.getLogger().finer("Job '" + job.getName() + "'@'" + job.getId() + "' registered.");
             jobs.put(job.getId(), job);
             return true;
         }
     }
 
     /**
      * Registers the specified job
      * @param owner The owner of the job
      * @return true if the job is valid and added, otherwise false
      */
     public boolean addJob(String owner) {
         if (owner == null) { return false; }
         Job job = queued.get(owner);
         if (job == null) { return false; }
         jobs.put(job.getId(), job);
         queued.remove(owner);
         plugin.getConfig().set("jobs.total", nextJob);
         return true;
     }
 
     /**
      * Attempts to cancel the specified job.
      * @param sender The person who is issuing the command.
      * @param job The job we're attempting to cancel.
      * @return true if the job was successfully canceled, otherwise false.
      */
     public boolean cancelJob(CommandSender sender, Job job) {
         if (!(job != null && (job.getOwner().equals(sender.getName()) || sender.hasPermission("jobsuite.admin.cancel") || job.isExpired()))) {
             return false;
         }
         Job cancel = jobs.remove(job.getId());
         if (cancel != null) {
             if (sender instanceof Player) {
                 plugin.getBank().give((Player) sender, cancel.getReward(), -1);
             }
             PreparedStatement basePrep = database.prepare("DELETE FROM jobsuite_base WHERE job_id = ? ;");
             PreparedStatement itemPrep = database.prepare("DELETE FROM jobsuite_items WHERE job_id = ? ;");
             PreparedStatement enchPrep = database.prepare("DELETE FROM jobsuite_enchantments WHERE job_id = ? ;");
             try {
                 basePrep.setInt(1, cancel.getId());
                 basePrep.executeUpdate();
                 itemPrep.setInt(1, cancel.getId());
                 itemPrep.executeUpdate();
                 enchPrep.setInt(1, cancel.getId());
                 enchPrep.executeUpdate();
             } catch (SQLException e) {
                 plugin.getLogger().warning("An SQLException occurred: " + e.getMessage());
             }
         }
         return cancel != null;
     }
 
     public void finishJob(CommandSender sender, Job job) {
         moveToClaims(job);
         if (!(sender instanceof ConsoleCommandSender)) {
             plugin.getBank().give((Player) sender, job.getReward(), -1);
         }
     }
 
     public void moveToClaims(Job job) {
         if (!job.isFinished()) {
             Player owner = plugin.getServer().getPlayer(job.getOwner());
             if (owner != null) {
                 owner.sendMessage(ChatColor.GREEN + "[Job] One of your jobs has been completed: " + ChatColor.AQUA + "/job claim");
             }
             job.finish();
             claims.add(job);
             if (job.getOwner().equals("CONSOLE")) {
                 claim(job);
             }
         }
     }
 
     public List<Job> getClaimableJobs(CommandSender sender) {
         List<Job> jobs = new ArrayList<Job>();
         for (Job job : claims) {
             if (job.getOwner().equals(sender.getName())) {
                 jobs.add(job);
             }
         }
         return jobs;
     }
 
     public Job getClaimableJob(CommandSender sender, int id) {
         for (Job job : getClaimableJobs(sender)) {
             if (job.getId() == id) {
                 return job;
             }
         }
         return null;
     }
 
     public void claim(Job job) {
         if (claims.contains(job)) {
             job.claim();
             claims.remove(job);
             jobs.remove(job.getId());
         }
     }
 
     /////////////////
     // JOB QUEUING //
     /////////////////
 
     private Map<String, Job> queued = new HashMap<String, Job>();
 
     /**
      * Gets a job that is currently being created by the player's name
      * @param player The player whose job we're fetching
      * @return The job associated with the player, or null
      */
     public Job getQueuedJob(String player) {
         return queued.get(player);
     }
 
     /**
      * Adds a job to the queue unless the player already has one queued
      * @param player The player responsible for the job
      * @param name The job's name
      * @return true if the job is added, otherwise false (the player has one queued or an object was null)
      */
     public boolean addQueuedJob(String player, String name) {
         if (queued.get(player) == null) {
             if (player == null || name == null || name.length() == 0) {
                 return false;
             }
             Job job = new Job(player, name, ++nextJob);
             queued.put(player, job);
             return true;
         }
         return false;
     }
 
     /**
      * Attempts to remove the specified player's queued job
      * @param player The player whose job we're removing
      * @return true if the job existed and was removed, otherwise false
      */
     public boolean removeQueuedJob(String player) {
         return queued.remove(player) != null;
     }
 
 }
