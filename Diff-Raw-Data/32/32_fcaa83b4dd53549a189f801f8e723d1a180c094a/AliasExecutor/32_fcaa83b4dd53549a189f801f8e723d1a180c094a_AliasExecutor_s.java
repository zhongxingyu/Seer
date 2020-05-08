 package com.mike724.sudoalias;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 
 public class AliasExecutor implements Runnable {
 
     private Alias alias;
     private String cmd;
     private CommandSender sender;
     private String pName;
 
     public AliasExecutor(Alias alias, String cmd, CommandSender sender, String pName) {
         this.alias = alias;
         this.cmd = cmd;
         this.sender = sender;
         this.pName = pName;
     }
 
     @Override
     public void run() {
         if (!sender.hasPermission(alias.getPermNode())) {
             sender.sendMessage(ChatColor.RED + "Sorry, you don't have permission to do that.");
             return;
         }
         String[] args = alias.getArgs(cmd);
         int argAmt = args.length;
         for (String command : alias.getCommandsToRun()) {
             if (command.startsWith("$wait:")) {
                 String[] data = command.split(":");
                 if (data.length != 2) {
                     continue;
                 }
                 long time = 1000l;
                 try {
                     time = Long.parseLong(data[1]);
                 } catch (NumberFormatException ex) {
                     SudoAlias.getInstance().getLogger().warning("Invalid long time value " + data[1] + " defaulting to 1000.");
                 }
                 try {
                     Thread.sleep(time);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             } else {
                 if (pName != null && !pName.isEmpty()) {
                     command = command.replace("$player", pName);
                 }
                 for (int i = 0; i < argAmt; i++) {
                     command = command.replace("$" + i, args[i]);
                 }
                 Server server = SudoAlias.getInstance().getServer();
                 if(alias.getRunAs() == AliasRunAs.CONSOLE) {
                    server.dispatchCommand(server.getConsoleSender(), command);
                 } else if(alias.getRunAs() == AliasRunAs.PLAYER) {
                    server.dispatchCommand(sender, command);
                 }
             }
         }
         String successMsg = alias.getSuccessMessage();
         if (!successMsg.isEmpty() && successMsg != null) {
             sender.sendMessage(successMsg);
         }
     }
 
 }
