 package com.github.CubieX.ScubaDoo;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SDCommandHandler implements CommandExecutor
 {
    private ScubaDoo plugin = null;
    private SDConfigHandler cHandler = null;
 
    public SDCommandHandler(ScubaDoo plugin, SDConfigHandler cHandler) 
    {
       this.plugin = plugin;
       this.cHandler = cHandler;
    }
 
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
       Player player = null;
       if (sender instanceof Player) 
       {
          player = (Player) sender;
       }
             
       if (cmd.getName().equalsIgnoreCase("scuba"))
      { // If the player typed /scuba then do the following... (can be run from console also)
          if (args.length == 0)
          { //no arguments, so help will be displayed
             return false;
          }
          if (args.length==1)
          {
             if (args[0].equalsIgnoreCase("version")) // argument 0 is given and correct
             {            
                sender.sendMessage(ChatColor.AQUA + "This server is running " + plugin.getDescription().getName() + " version " + plugin.getDescription().getVersion());
                               
                return true;
             }
             if (args[0].equalsIgnoreCase("reload")) // argument 0 is given and correct
             {            
               if(sender.hasPermission("teamadvantage.admin"))
                {                        
                   cHandler.reloadConfig(sender);
                   return true;
                }
                else
                {
                   sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
                }
             }
             if (args[0].equalsIgnoreCase("help")) // argument 0 is given and correct
             {            
                sender.sendMessage(ChatColor.AQUA + ScubaDoo.logPrefix + "HILFE: Ein oder mehrere Glasbloecke in den Helm-Slot legen.");
                sender.sendMessage(ChatColor.AQUA + ScubaDoo.logPrefix + "HILFE: Zum Atmen jeweils kurz SCHLEICHEN druecken.");
                               
                return true;
             }
          }
          else
          {
             sender.sendMessage(ChatColor.YELLOW + "Invalid argument count.");
          }                
 
       }         
       return false; // if false is returned, the help for the command stated in the plugin.yml will be displayed to the player
    }
 }
