 package com.norcode.bukkit.livestocklock.commands;
 
 import com.norcode.bukkit.livestocklock.LivestockLock;
 import com.norcode.bukkit.livestocklock.OwnedAnimal;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 
 public class ListCommand extends BaseCommand {
     public ListCommand(LivestockLock plugin) {
         super(plugin, "list", new String[] {
             "List your claimed animals.",
             "/<command> list - list all of your animals.",
             "/<command> list <player> - list all of <player>'s animals."
         });
     }
 
     @Override
     public boolean onCommand(CommandSender sender, String label, LinkedList<String> args) {
         String owner = null;
         if (args.size() == 1) {
             if (!sender.hasPermission("livestocklock.claimforothers")) {
                 sender.sendMessage("Sorry, you don't have permission to view other people's claims.");
                 return true;
             }
             List<Player> matches = plugin.getServer().matchPlayer(args.peek());
             if (matches.size() != 1) {
                 sender.sendMessage("Unknown Player: " + args.peek());
                 return true;
             }
             owner = matches.get(0).getName();
         } else {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("This command must be run by a player.");
                 return true;
             }
             owner = ((Player) sender).getName();
         }
         List<String> accessList = plugin.getAccessList(owner);
         List<OwnedAnimal> ownedAnimals = new LinkedList<OwnedAnimal>();
         for (UUID uuid: plugin.getOwnedAnimalIDs(owner)) {
            ownedAnimals.add(plugin.getOwnedAnimal(uuid));
         }
         sender.sendMessage(ChatColor.GOLD + "Claimed Animals for " + owner);
         if (ownedAnimals.size() == 0) {
             sender.sendMessage(owner + " has no animals!");
             return true;
         }
         for (OwnedAnimal oa: ownedAnimals) {
             sender.sendMessage(" - " + oa.getEntityType().name());
         }
         sender.sendMessage(ChatColor.GOLD + "Trust List for " + owner);
         if (ownedAnimals.size() == 0) {
             sender.sendMessage(owner + " has no trust list!");
             return true;
         }
         for (String s: accessList) {
             sender.sendMessage(" - " + s);
         }
         return true;
     }
 
     @Override
     public List<String> onTabComplete(CommandSender sender, String label, LinkedList<String> args) {
         return null;
     }
 }
