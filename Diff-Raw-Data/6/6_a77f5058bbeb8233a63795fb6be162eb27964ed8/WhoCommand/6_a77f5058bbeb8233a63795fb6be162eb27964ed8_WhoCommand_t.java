 package info.bytecraft.commands;
 
 import static org.bukkit.ChatColor.*;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
import info.bytecraft.api.Rank;
 import info.bytecraft.api.BytecraftPlayer.Flag;
 
 public class WhoCommand extends AbstractCommand
 {
 
     public WhoCommand(Bytecraft instance)
     {
         super(instance, "who");
     }
 
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
         if (args.length == 0) {
             StringBuilder sb = new StringBuilder();
             String delim = "";
             int playerCounter = 0;
             for (BytecraftPlayer other : plugin.getOnlinePlayers()) {
                 if (other.hasFlag(Flag.INVISIBLE)) {
                    if(player.getRank() != Rank.ELDER && player.getRank() != Rank.PRINCESS){
                        continue;
                    }
                 }
                 sb.append(delim);
                 sb.append(other.getDisplayName());
                 delim = ChatColor.WHITE + ", ";
                 playerCounter++;
             }
             player.sendMessage(GRAY + "************" + DARK_PURPLE
                     + "Player List" + GRAY + "************");
             player.sendMessage(sb.toString().trim());
             player.sendMessage(GRAY + "************" + GOLD + playerCounter
                     + " player(s) online" + GRAY + "*****");
         }
         else if (args.length == 1) {
             if (!player.isAdmin())
                 return true;
             List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[0]);
             if (cantidates.size() != 1) {
                 return true;
             }
 
             BytecraftPlayer target = cantidates.get(0);
             whoOther(player.getDelegate(), target);
         }
         return true;
     }
 
     public boolean handleOther(Server server, String[] args)
     {
         ConsoleCommandSender sender = server.getConsoleSender();
         if (args.length == 0) {
             StringBuilder sb = new StringBuilder();
             String delim = "";
             int playerCounter = 0;
             for (BytecraftPlayer other : plugin.getOnlinePlayers()) {
                 sb.append(delim);
                 sb.append(other.getDisplayName());
                 delim = ChatColor.WHITE + ", ";
                 playerCounter++;
             }
             sender.sendMessage(GRAY + "************" + DARK_PURPLE
                     + "Player List" + GRAY + "************");
             sender.sendMessage(sb.toString().trim());
             sender.sendMessage(GRAY + "************" + GOLD + playerCounter
                     + " player(s) online" + GRAY + "*****");
         }
         else if (args.length == 1) {
             List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[0]);
             if (cantidates.size() != 1) {
                 return true;
             }
 
             BytecraftPlayer target = cantidates.get(0);
             whoOther(sender, target);
         }
         return true;
     }
 
     public void whoOther(CommandSender player, BytecraftPlayer whoPlayer)
     {
         int x = whoPlayer.getLocation().getBlockX();
         int y = whoPlayer.getLocation().getBlockY();
         int z = whoPlayer.getLocation().getBlockZ();
 
         player.sendMessage(DARK_GRAY + "******************** " + DARK_PURPLE
                 + "PLAYER INFO" + DARK_GRAY + " ********************");
         player.sendMessage(GOLD + "Player: " + GRAY
                 + whoPlayer.getDisplayName());
         player.sendMessage(GOLD + "World: " + GRAY
                 + whoPlayer.getWorld().getName());
         player.sendMessage(GOLD + "Coords: " + GRAY + x + ", " + y + ", " + z);
         player.sendMessage(GOLD + "Channel: " + GRAY
                 + whoPlayer.getChatChannel());
         player.sendMessage(GOLD + "Wallet: " + GRAY + whoPlayer.getBalance()
                 + " bytes.");
         player.sendMessage(GOLD + "Health: " + GRAY + whoPlayer.getHealth());
         player.sendMessage(GOLD + "Country: " + GRAY + whoPlayer.getCountry());
         player.sendMessage(GOLD + "City: " + GRAY + whoPlayer.getCity());
         player.sendMessage(GOLD + "IP Address: " + GRAY + whoPlayer.getIp());
         player.sendMessage(GOLD + "Port: " + GRAY
                 + whoPlayer.getAddress().getPort());
         player.sendMessage(GOLD + "Gamemode: " + GRAY
                 + whoPlayer.getGameMode().toString().toLowerCase());
         player.sendMessage(GOLD + "Level: " + GRAY + whoPlayer.getLevel());
         player.sendMessage(DARK_GRAY
                 + "******************************************************");
     }
 }
