 package net.larry1123.fly.commands;
 
 import net.canarymod.Canary;
 import net.canarymod.Translator;
 import net.canarymod.api.GameMode;
 import net.canarymod.api.Server;
 import net.canarymod.api.entity.living.humanoid.HumanCapabilities;
 import net.canarymod.api.entity.living.humanoid.Player;
 import net.canarymod.api.world.blocks.CommandBlock;
 import net.canarymod.api.world.position.Location;
 import net.canarymod.chat.MessageReceiver;
 import net.canarymod.hook.player.TeleportHook;
 import net.larry1123.fly.permissions.PermissionsMan;
 import net.larry1123.fly.task.FlyTime;
 import net.larry1123.util.api.chat.FontTools;
 import net.larry1123.util.api.plugin.commands.Command;
 import net.larry1123.util.api.plugin.commands.CommandData;
 import net.larry1123.util.api.time.StringTime;
 import net.visualillusionsent.utils.LocaleHelper;
 
 public class CFly implements Command {
 
     private final CommandData command;
     private final CommandMan commandman;
     private boolean loaded = false;
 
     // This is to save servers! Warning do not set speed too high in anyway!
     private static float maxSpeed;
 
     public CFly(CommandMan commandman, String[] aliases) {
         maxSpeed = commandman.getPlugin().getConfigMan().getMainConfig().getMaxSpeed();
         this.commandman = commandman;
         command = new CommandData( //
                 aliases, //
                 new String[]{PermissionsMan.commandNode,}, //
                 "Allows you or someone else to fly!", //
                 /**
                  * fly
                  * fly Speed
                  * fly Player
                  * fly Player Time
                  * fly Player Speed
                  * fly Player Speed Time
                  */
                 "/fly [Player|Speed] [Speed|Time] [Time]" //
         );
         command.setMax(4);
     }
 
     @Override
     public void execute(MessageReceiver caller, String[] parameters) {
         if ((caller instanceof Server || caller instanceof CommandBlock) && parameters.length == 1) {
             // Makes sure Server commands and CommandBlock commands at lest have one parameter
             caller.message("Must Provide Player");
             return;
         }
         Player player;
         float speed = 0;
         long time = 0;
         if (caller instanceof Player) {
             if (parameters.length == 1) {
                 // fly
                 player = (Player) caller;
                 if (!player.getCapabilities().mayFly()) {
                     speed = (float) 0.05;
                 } else {
                    speed = 0;
                 }
             } else if (parameters.length == 2) {
                 if (Canary.getServer().getPlayer(parameters[1]) != null) {
                     // fly Player
                     player = Canary.getServer().getPlayer(parameters[1]);
                     if (!player.getCapabilities().mayFly()) {
                         speed = (float) 0.05;
                     } else {
                         speed = 0;
                     }
                 } else {
                     // fly Speed
                     player = (Player) caller;
                     try {
                         if (parameters[1].toLowerCase().endsWith("D".toLowerCase())) {
                             throw new NumberFormatException();
                         }
                         speed = Float.parseFloat(parameters[1]);
                     } catch (NumberFormatException e) {
                         String[] parts = new String[]{parameters[1]};
                         time = StringTime.millisecondsFromString(parts);
                         if (time != 0) {
                             speed = (float) 0.05;
                             caller.message(FontTools.RED + "Given Speed is NaN!");
                         }
                     }
                 }
             } else {
                 if (Canary.getServer().getPlayer(parameters[1]) != null) {
                     player = Canary.getServer().getPlayer(parameters[1]);
                     try {
                         if (parameters[2].toLowerCase().endsWith("D".toLowerCase())) {
                             throw new NumberFormatException();
                         }
                         speed = Float.parseFloat(parameters[2]);
                     } catch (NumberFormatException e) {
                         //
                     }
                 } else {
                     player = (Player) caller;
                     try {
                         if (parameters[1].toLowerCase().endsWith("D".toLowerCase())) {
                             throw new NumberFormatException();
                         }
                         speed = Float.parseFloat(parameters[1]);
                     } catch (NumberFormatException e) {
                         //
                     }
                 }
                 boolean speeded = false;
                 try {
                     // fly Player Speed
                     if (parameters[2].toLowerCase().endsWith("D".toLowerCase())) {
                         throw new NumberFormatException();
                     }
                     speed = Float.parseFloat(parameters[2]);
                     speeded = true;
                 } catch (NumberFormatException e) {
                     speed = (float) 0.05;
                     // fly Player Time
                     // We know that it is only Time stuff left, or well we can hope
                     String[] parts = new String[parameters.length - 2];
                     int y = 0;
                     for (int i = 2; i <= parameters.length - 1; i++) {
                         parts[y++] = parameters[i];
                     }
                     time = StringTime.millisecondsFromString(parts);
                 }
                 if (speeded) {
                     // fly Player Speed Time
                     String[] parts = new String[parameters.length - 3];
                     int y = 0;
                     for (int i = 3; i <= parameters.length - 1; i++) {
                         parts[y++] = parameters[i];
                     }
                     time = StringTime.millisecondsFromString(parts);
                 }
             }
         } else {
             player = Canary.getServer().getPlayer(parameters[1]);
             if (parameters.length >= 3) {
                 boolean speeded = false;
                 try {
                     if (parameters[2].toLowerCase().endsWith("D".toLowerCase())) {
                         throw new NumberFormatException();
                     }
                     speed = Float.parseFloat(parameters[2]);
                     speeded = true;
                 } catch (NumberFormatException e) {
                     speed = (float) 0.05;
                     if (parameters.length != 4) {
                         caller.message(FontTools.RED + "Given Speed is NaN!");
                     } else {
                         // fly Player Time
                         // We know that it is only Time stuff left, or well we can hope
                         String[] parts = new String[parameters.length - 2];
                         int y = 0;
                         for (int i = 2; i <= parameters.length - 1; i++) {
                             parts[y++] = parameters[i];
                         }
                         time = StringTime.millisecondsFromString(parts);
                     }
                 }
                 if (speeded) {
                     // fly Player Speed Time
                     String[] parts = new String[parameters.length - 3];
                     int y = 0;
                     for (int i = 3; i <= parameters.length - 1; i++) {
                         parts[y++] = parameters[i];
                     }
                     time = StringTime.millisecondsFromString(parts);
                 }
             }
         }
         if (player == null) {
             caller.message(FontTools.RED + "Player not found!");
             return;
         }
         if (caller != player) {
             // Stop people from controlling and admin, but allow other admins to troll
             if (player.isAdmin()) {
                 if (caller instanceof Server) {
                     // Yes server we fallow you!
                 } else if (caller instanceof CommandBlock) {
                     // Stop here admin is being let out
                     // TODO add way for admins to toggle this
                     return;
                 } else if (caller instanceof Player) {
                     if (!((Player) caller).isAdmin()) {
                         caller.message(FontTools.RED + "You can not affect an Admin!!!");
                     }
                 }
             }
             if (!PermissionsMan.allowFlyOther(caller)) {
                 caller.message(FontTools.RED + "You do not have Permission to allow others to fly!");
                 return;
             }
         }
         if (!PermissionsMan.canFly(player)) {
             String message = " not allowed to fly";
             if (caller != player) {
                 if (caller instanceof Player) {
                     if (PermissionsMan.allowFlyOtherOverRide(caller)) {
                         caller.message(player.getName() + message + ", but you can still affect this player.");
                     } else {
                         caller.message(FontTools.RED + player.getName() + " is" + message);
                         return;
                     }
                 } else {
                     if (caller instanceof Server) {
                         caller.message(player.getName() + " may not fly! But Oh Mighty Server you may still affect this player.");
                     }
                 }
             } else {
                 caller.message(FontTools.RED + "You are" + message);
                 return;
             }
         }
         // Limit to at lest Maxspeed for Safety
         if (speed > maxSpeed) {
             speed = maxSpeed;
             // No one is allowed over the max not even admins!
         }
         // Limit speed to the max the player can go.
         float playerMaxSpeed = PermissionsMan.getMaxSpeedByCaller(player);
         if (speed > playerMaxSpeed) {
             if (!PermissionsMan.allowFlyOtherOverRide(caller)) {
                 if (caller instanceof Player) {
                     speed = playerMaxSpeed;
                     if (caller != player) {
                         caller.message(player.getName() + " can not fly that fast. Setting Speed to " + speed);
                     } else {
                         caller.message("You can not fly that fast. Setting Speed to " + speed);
                     }
                 }
                 // Need to limit speed to what the Caller can set
                 if (speed > PermissionsMan.getMaxSpeedByCaller(caller)) {
                     speed = PermissionsMan.getMaxSpeedByCaller(caller);
                     if (caller != player) {
                         caller.message("You can not let others fly that fast. Setting Speed to " + speed);
                     }
                 }
             } else {
                 if (caller != player) {
                     caller.message("Please know that " + speed + " is faster then what this player is allowed, " + playerMaxSpeed + " is this player's max");
                     // Allow some people to have full power!
                 }
             }
         }
         // SetUp Done
         HumanCapabilities capabilities = player.getCapabilities();
         if (!capabilities.mayFly()) {
             if (speed == 0) {
                 // TODO add way for up/down flying?
                 return;
             }
             if (!player.getMode().equals(GameMode.CREATIVE)) {
                 // Starts flying if not Flying and not in Creative
                 capabilities.setMayFly(true);
                 capabilities.setFlying(true);
                 capabilities.setFlySpeed(speed);
                 player.updateCapabilities();
                 String message;
                 if (speed > 0.5) {
                     message = " may fly now, with the speed of " + FontTools.RED + capabilities.getFlySpeed();
                 } else {
                     message = " may fly now, with the speed of " + capabilities.getFlySpeed();
                 }
                 if (time > 0) {
                     message += FontTools.RESET + ", for " + time;
                 }
                 player.message("You" + message);
                 if (caller != player) {
                     caller.message(player.getName() + message);
                 }
                 FlyTime.addPlayerFor(player, time);
             }
         } else {
             if (!player.getMode().equals(GameMode.CREATIVE) && speed == 0) {
                 capabilities.setFlying(false);
                 capabilities.setMayFly(false);
                 capabilities.setFlySpeed((float) 0.05);
                 player.updateCapabilities();
                 Location loc = player.getLocation();
                 loc.setY(player.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()) + 1);
                 player.teleportTo(loc, TeleportHook.TeleportCause.MOVEMENT);
                 String message = " may no longer fly.";
                 player.message("You" + message);
                 if (caller != player) {
                     caller.message(player.getName() + message);
                 }
                 FlyTime.removePlayer(player);
             } else if (player.getMode().equals(GameMode.CREATIVE) || speed != 0) {
                 // Sets the Speed a Creative Player is flying at
                 if (speed == capabilities.getFlySpeed()) return;
                 capabilities.setFlySpeed(speed);
                 player.updateCapabilities();
                 String message;
                 if (speed > 0.5) {
                     message = " flight speed is now " + FontTools.RED + capabilities.getFlySpeed();
                 } else {
                     message = " flight speed is now " + capabilities.getFlySpeed();
                 }
                 player.message("Your" + message);
                 if (caller != player) {
                     player.message(player.getName() + message);
                 }
             }
         }
     }
 
     @Override
     public CommandData getCommandData() {
         return command;
     }
 
     @Override
     public LocaleHelper getTranslator() {
         return Translator.getInstance();
     }
 
     @Override
     public boolean isForced() {
         return false;
     }
 
     @Override
     public boolean isloaded() {
         return loaded;
     }
 
     @Override
     public void setloadded(boolean loadedness) {
         loaded = loadedness;
     }
 
 }
