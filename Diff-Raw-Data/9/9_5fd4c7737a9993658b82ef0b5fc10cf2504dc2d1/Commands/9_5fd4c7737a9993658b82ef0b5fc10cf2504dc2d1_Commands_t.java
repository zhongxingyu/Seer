 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nationsmc.chunkrefresh.commands;
 
 import com.nationsmc.chunkrefresh.ChunkRefresh;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Koo
  */
 public class Commands implements CommandExecutor {
 
     public ChunkRefresh cr;
 
     public Commands(ChunkRefresh plugin) {
         cr = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         Player player = (Player) sender;
         World world = player.getWorld();
         Location loc;
         Chunk chunk;
         List<Entity> entities;
         if (cmd.getName().equalsIgnoreCase("cr")) {
             loc = player.getLocation();
             chunk = world.getChunkAt(loc);
             if (args.length < 1) {
                 /*
                  * If LESS then 1 argument.
                  */
                 player.sendMessage("/cr <save {Amount} | run {all}>");
                 return true;
             } else if (args.length == 1) {
                 /*
                  * If ONLY 1 argument.
                  */
                 if ("save".equalsIgnoreCase(args[0])) {
                     cr.save(chunk);
                     player.sendMessage("[ChunkRefresh] Chunk saved.");
                     return true;
                 } else if ("run".equalsIgnoreCase(args[0])) {
                     if (!cr.playersPresent(chunk, player)) {
                         if (cr.checkSave(chunk)) {
                             cr.sendAway(player, chunk);
                             cr.run(chunk);
                             return true;
                         } else {
                             player.sendMessage("[ChunkRefresh] Chunk is not saved!");
                             return true;
                         }
                     } else {
                         player.sendMessage("[ChunkRefresh] Players present");
                         return true;
                     }
                 } else if("setTime".equalsIgnoreCase(args[0])){
                     Long time = new Date().getTime();
                     cr.getPool().add(chunk, time);
                     try {
                         cr.getPool().flush();
                     } catch (IOException ex) {
                         player.sendMessage("Failed to save data!");
                         return true;
                     }
                     return true;                    
                 }else{
                     player.sendMessage("/cr <save {Amount} | run {all}>");
                     return true;
                 }
             } else if (args.length > 1) {
                 /*
                  * If more then 1 argument.
                  */
                 if ("save".equalsIgnoreCase(args[0])) {
                     if (cr.isNumeric(args[1])) {
                         if (args[2] != null && args[3] != null) {
                             if (args[2].equalsIgnoreCase("n")) {
                                 if (args[3].equalsIgnoreCase("e")) {
                                     cr.saveMore(chunk, Integer.parseInt(args[1]), "ne");
                                     player.sendMessage("Saved chunks");
                                     return true;
                                 } else if (args[3].equalsIgnoreCase("w")) {
                                     cr.saveMore(chunk, Integer.parseInt(args[1]), "nw");
                                     player.sendMessage("Saved chunks");
                                     return true;
                                 } else {
                                     player.sendMessage("That is not a valid direction.");
                                    player.sendMessage("/cr save " + args[1] + " " + args[2] + " <e|w>");
                                     return true;
                                 }
                             } else if (args[2].equalsIgnoreCase("s")) {
                                 if (args[3].equalsIgnoreCase("e")) {
                                     cr.saveMore(chunk, Integer.parseInt(args[1]), "se");
                                     player.sendMessage("Saved chunks");
                                     return true;
                                 } else if (args[3].equalsIgnoreCase("w")) {
                                     cr.saveMore(chunk, Integer.parseInt(args[1]), "sw");
                                     player.sendMessage("Saved chunks");
                                     return true;
                                 } else {
                                     player.sendMessage("That is not a valid direction.");
                                     player.sendMessage("/cr save " + args[1] + args[2] + " <e|w>");
                                     return true;
                                 }
                             } else {
                                 player.sendMessage("That is not a valid direction.");
                                 player.sendMessage("/cr save " + args[1] + " <n|s> <e|w>");
                                 return true;
                             }
                         } else {
                             player.sendMessage("Need a direction.");
                             player.sendMessage("/cr save " + args[1] + " <n|s> <e|w>");
                             return true;
                         }
                     } else {
                         player.sendMessage("Numerical amounts only.");
                         return true;
                     }
                 } else if ("run".equalsIgnoreCase(args[0])) {
                     if ("all".equalsIgnoreCase(args[1])) {
                         cr.runAll();
                         cr.sendAway(player, chunk);
                         return true;
                     } else {
                         player.sendMessage("Invalid Argument.");
                         player.sendMessage("/cr run {all}");
                        return true;
                     }
                 } else {
                    player.sendMessage("/cr <save {Amount (N | E | S | W)}  | run {all} | setTime>");
                    player.sendMessage("{}=Optional");
                    player.sendMessage("()=Required");
                     return true;
                 }
             }
         }
         return false;
     }
 }
