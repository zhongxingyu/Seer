 package net.krinsoft.jobsuite;
 
 import com.fernferret.allpay.AllPay;
 import com.fernferret.allpay.commons.GenericBank;
 import com.pneumaticraft.commandhandler.CommandHandler;
 import net.krinsoft.jobsuite.commands.*;
 import net.krinsoft.jobsuite.listeners.ServerListener;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author krinsdeath
  */
 public class JobCore extends JavaPlugin {
     private JobManager manager;
     private CommandHandler commands;
     private GenericBank bank;
 
     private boolean debug = false;
 
     @Override
     public void onEnable() {
         // let's see how long it takes to start up!
         long time = System.currentTimeMillis();
 
         // validate allpay
         validateAllPay();
 
         // generate a default config if it doesn't exist already
         if (!new File(getDataFolder(), "config.yml").exists()) {
             getConfig().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/config.yml")));
             getConfig().options().copyDefaults(true);
             saveConfig();
         }
         // check debug mode
         debug = getConfig().getBoolean("plugin.debug");
 
         // register the economy listener
         getServer().getPluginManager().registerEvents(new ServerListener(this), this);
 
         initializeManager();
         initializeCommands();
         getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 manager.persist();
             }
         }, 180L * 20L, 180L * 20L); // save every 3 minutes (180 seconds)
         getLogger().info("JobSuite initialized! (" + (System.currentTimeMillis() - time) + "ms)");
     }
 
     @Override
     public void onDisable() {
         long time = System.currentTimeMillis();
         getServer().getScheduler().cancelTasks(this);
         manager.close();
         getLogger().info("JobSuite disabled successfully. (" + (System.currentTimeMillis() - time) + "ms)");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         List<String> allArgs = new ArrayList<String>(Arrays.asList(args));
         allArgs.add(0, label);
         return commands.locateAndRunCommand(sender, allArgs);
     }
 
     public void debug(String message) {
         if (debug) {
             getLogger().info("[Debug] " + message);
         }
     }
 
     private void initializeManager() {
         manager = new JobManager(this);
         manager.load();
     }
 
     private void initializeCommands() {
         PermissionHandler handler = new PermissionHandler();
         commands = new CommandHandler(this, handler);
         // base help command
         commands.registerCommand(new JobBaseCommand(this));
 
         // JOB CREATION
         commands.registerCommand(new JobMakeCommand(this));
         commands.registerCommand(new JobDescriptionCommand(this));
         commands.registerCommand(new JobRewardCommand(this));
         commands.registerCommand(new JobAddItemCommand(this));
         commands.registerCommand(new JobRemoveItemCommand(this));
         commands.registerCommand(new JobAddEnchantmentCommand(this));
         commands.registerCommand(new JobRemoveEnchantmentCommand(this));
         commands.registerCommand(new JobListItemCommand(this));
         commands.registerCommand(new JobPostCommand(this));
         commands.registerCommand(new JobQuitCommand(this));
 
         // ADMINISTRATIVE
         commands.registerCommand(new JobLockCommand(this));
         commands.registerCommand(new JobUnlockCommand(this));
         commands.registerCommand(new JobFinishCommand(this));
         commands.registerCommand(new JobCancelCommand(this));
         commands.registerCommand(new JobClaimCommand(this));
         commands.registerCommand(new JobListCommand(this));
         commands.registerCommand(new JobInfoCommand(this));
 
         // BASIC FUNCTIONS
         //commands.registerCommand(new JobReloadCommand(this));
         //commands.registerCommand(new JobFlushCommand(this));
     }
 
     public JobManager getJobManager() {
         return manager;
     }
 
     public boolean validateAllPay() {
         if (bank != null) { return true; }
         AllPay handle = new AllPay(this, "[JobSuite] ");
         if ((bank = handle.loadEconPlugin()) != null) {
             bank.toggleReceipts(false);
             debug("Economy hooked.");
         }
         return false;
     }
 
     public GenericBank getBank() {
         if (validateAllPay()) {
             return this.bank;
         } else {
             debug("Couldn't find a valid economy plugin.");
             return null;
         }
     }
 
 }
