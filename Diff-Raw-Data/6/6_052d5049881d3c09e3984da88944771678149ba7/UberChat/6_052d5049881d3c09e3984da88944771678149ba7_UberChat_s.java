 package net.daboross.bukkitdev.uberchat;
 
 import net.daboross.bukkitdev.uberchat.commandexecutors.ColorizorExecutor;
 import net.daboross.bukkitdev.uberchat.commandexecutors.MeExecutor;
 import net.daboross.bukkitdev.uberchat.commandexecutors.MsgExecutor;
 import net.daboross.bukkitdev.uberchat.commandexecutors.ReplyExecutor;
 import net.daboross.bukkitdev.uberchat.commandexecutors.ShoutExecutor;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * UberChat Plugin Made By DaboRoss
  *
  * @author daboross
  */
 public final class UberChat extends JavaPlugin {
 
     @Override
     public void onEnable() {
         registerEvents();
         assignCommands();
         getLogger().info("UberChat Fully Enabled");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("UberChat Fully Disabled");
     }
 
     private void registerEvents() {
         PluginManager pm = this.getServer().getPluginManager();
         UberChatListener uberChatListener = new UberChatListener();
         pm.registerEvents(uberChatListener, this);
     }
 
     private void assignCommands() {
        PluginCommand colorizor = getCommand("colorizor");
        if (colorizor != null) {
            colorizor.setExecutor(new ColorizorExecutor());
         }
         PluginCommand me = getCommand("me");
         if (me != null) {
             me.setExecutor(new MeExecutor());
         }
         PluginCommand msg = getCommand("msg");
         if (msg != null) {
             msg.setExecutor(new MsgExecutor());
         }
         PluginCommand reply = getCommand("reply");
         if (reply != null) {
             reply.setExecutor(new ReplyExecutor());
         }
         PluginCommand shout = getCommand("shout");
         if (shout != null) {
             shout.setExecutor(new ShoutExecutor());
         }
     }
 }
