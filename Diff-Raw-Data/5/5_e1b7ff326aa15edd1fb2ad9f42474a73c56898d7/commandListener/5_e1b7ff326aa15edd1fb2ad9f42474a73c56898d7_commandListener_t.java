 package com.codisimus.plugins.quizblock.listeners;
 
 import com.codisimus.plugins.quizblock.Quiz;
 import com.codisimus.plugins.quizblock.QuizBlock;
 import com.google.common.collect.Sets;
 import java.util.HashSet;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.material.Door;
 
 /**
  * Executes Player Commands
  * 
  * @author Codisimus
  */
 public class CommandListener implements CommandExecutor {
     private static enum Action { HELP, MAKE, LINK, UNLINK, DELETE, MSG, LIST, RL }
     private static enum BlockType { RIGHT, DOOR, WRONG }
     private static final HashSet TRANSPARENT = Sets.newHashSet(
             (byte)8, (byte)9, (byte)10, (byte)11, (byte)51);
     
     /**
      * Listens for QuizBlock commands to execute them
      * 
      * @param sender The CommandSender who may not be a Player
      * @param command The command that was executed
      * @param alias The alias that the sender used
      * @param args The arguments for the command
      * @return true always
      */
     @Override
     public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
         //Cancel if the command is not from a Player
         if (!(sender instanceof Player)) {
             if (args.length > 0 && args[0].equals("rl"))
                 rl(null);
             
             return true;
         }
         
         Player player = (Player)sender;
 
         //Display the help page if the Player did not add any arguments
         if (args.length == 0) {
             sendHelp(player);
             return true;
         }
         
         Action action;
         
         try {
             action = Action.valueOf(args[0].toUpperCase());
         }
         catch (Exception notEnum) {
             sendHelp(player);
             return true;
         }
         
         //Cancel if the Player does not have permission to use the command
         if (!QuizBlock.hasPermission(player, args[0]) && !args[0].equals("help")) {
             player.sendMessage("You do not have permission to do that.");
             return true;
         }
         
         //Execute the correct command
         switch (action) {
             case HELP:
                 sendHelp(player);
                 return true;
                 
             case MAKE:
                 if (args.length == 2)
                     make(player, args[1]);
                 else
                     sendHelp(player);
                 
                 return true;
                 
             case LINK:
                 if (args.length == 3) {
                     BlockType blockType;
         
                     try {
                         blockType = BlockType.valueOf(args[1].toUpperCase());
                     }
                     catch (Exception notEnum) {
                         sendHelp(player);
                         return true;
                     }
                     
                     link(player, args[2], blockType);
                 }
                 else
                     sendHelp(player);
                 
                 return true;
                 
             case UNLINK:
                 if (args.length == 1)
                     unlink(player);
                 else
                     sendHelp(player);
                 
                 return true;
                 
             case DELETE:
                 switch (args.length) {
                     case 1: delete(player, null); return true;
                     case 2: delete(player, args[1]); return true;
                     default: sendHelp(player); return true;
                 }
                 
             case MSG:
                 if (args.length < 3) {
                     sendHelp(player);
                     return true;
                 }
                 
                 String msg = "";
                 for (int i = 2; i < args.length; i++)
                     msg = msg.concat(args[i].concat(" "));
                 
                 BlockType blockType;
         
                 try {
                     blockType = BlockType.valueOf(args[1].toUpperCase());
                 }
                 catch (Exception notEnum) {
                     sendHelp(player);
                     return true;
                 }
                     
                 msg(player, args[2], blockType, msg);
                 return true;
                 
             case LIST:
                 if (args.length == 1)
                     list(player);
                 else
                     sendHelp(player);
                 
                 return true;
                 
             case RL:
                 if (args.length == 1)
                     rl(player);
                 else
                     sendHelp(player);
                 
                 return true;
                 
             default: sendHelp(player); return true;
         }
     }
     
     /**
      * Creates a new Quiz of the given name at the given Player's Location
      * 
      * @param player The Player creating the Quiz
      * @param name The name of the Quiz being created (must not already exist)
      */
     private static void make(Player player, String name) {
         //Cancel if the Quiz already exists
         if (QuizBlock.findQuiz(name) != null) {
             player.sendMessage("A Quiz named "+name+" already exists.");
             return;
         }
         
         QuizBlock.quizes.add(new Quiz(name, player.getLocation()));
         player.sendMessage("Quiz "+name+" made!");
         QuizBlock.save();
     }
     
     /**
      * Links the target Block to the specified Quiz
      * 
      * @param player The Player linking the Block they are targeting
      * @param name The name of the Quiz the Block will be linked to
      * @param type The BlockType that the Block will be linked as
      */
     private static void link(Player player, String name, BlockType type) {
         Block block = player.getTargetBlock(TRANSPARENT, 10);
         
         //Cancel if the Block is already linked to a Quiz
         Quiz quiz = QuizBlock.findQuiz(block);
         if (quiz != null) {
             player.sendMessage("That Block is already linked to Quiz "+quiz.name+".");
             return;
         }
         
         //Cancel if the Quiz with the given name does not exist
         quiz = QuizBlock.findQuiz(name);
        if (quiz == null) {
             player.sendMessage("Quiz "+name+" does not exsist.");
             return;
         }
         
         //Link the Block as the given type
         switch (type) {
             case RIGHT:
                 quiz.rightBlocks.add(block);
                 player.sendMessage("Succesfully linked as right block of "+name+"!");
                 break;
                 
             case DOOR:
                 switch (block.getType()) {
                     case WOOD_DOOR: //Fall through
                     case WOODEN_DOOR: //Fall through
                     case IRON_DOOR: //Fall through
                     case IRON_DOOR_BLOCK:
                         if (((Door)block.getState().getData()).isTopHalf())
                             block = block.getRelative(BlockFace.DOWN);
 
                         break;
 
                     default: break;
                 }
                 
                 quiz.doorBlocks.add(block);
                 player.sendMessage("Succesfully linked as door block of "+name+"!");
                 break;
                 
             case WRONG:
                 quiz.wrongBlocks.add(block);
                 player.sendMessage("Succesfully linked as wrong block of "+name+"!");
                 break;
                 
             default: sendHelp(player); return;
         }
         
         QuizBlock.save();
     }
     
     /**
      * Unlinks the target Block from the specified Warp
      * 
      * @param player The Player unlinking the Block they are targeting
      */
     private static void unlink(Player player) {
         //Cancel if the Block the Player is targeting is not linked to a Quiz
         Block block = player.getTargetBlock(TRANSPARENT, 10);
         Quiz quiz = QuizBlock.findQuiz(block);
         if (quiz == null) {
             player.sendMessage("Target Block is not linked to a Quiz");
             return;
         }
         
         if (!quiz.doorBlocks.remove(block))
             if (!quiz.rightBlocks.remove(block))
                 quiz.wrongBlocks.remove(block);
         
         player.sendMessage("Target Block has been unlinked from Quiz "+quiz.name+"!");
         QuizBlock.save();
     }
     
     /**
      * Deletes the specified Quiz
      * If a name is not provided, the Quiz of the target Block is deleted
      * 
      * @param player The Player deleting the Quiz
      * @param name The name of the Quiz to be deleted
      */
     private static void delete(Player player, String name) {
         Quiz quiz = null;
         
         if (name == null) {
             //Find the Warp that will be modified using the target Block
             quiz = QuizBlock.findQuiz(player.getTargetBlock(TRANSPARENT, 10));
             
             //Cancel if the Warp does not exist
             if (quiz == null ) {
                 player.sendMessage("Target Block is not linked to a Quiz");
                 return;
             }
         }
         else {
             //Find the Warp that will be modified using the given name
             quiz = QuizBlock.findQuiz(name);
             
             //Cancel if the Warp does not exist
             if (quiz == null ) {
                 player.sendMessage("Quiz "+quiz.name+" does not exsist.");
                 return;
             }
         }
         
         quiz.doorBlocks.clear();
         quiz.rightBlocks.clear();
         quiz.wrongBlocks.clear();
         QuizBlock.quizes.remove(quiz);
         
         player.sendMessage("Quiz "+name+" deleted.");
         QuizBlock.save();
     }
     
     /**
      * Modifies the message of the specified Quiz
      * If a name is not provided, the Quiz of the target Block is modified
      * 
      * @param player The Player modifying the Quiz
      * @param name The name of the Quiz to be modified
      * @param type The BlockType that the message will be linked to
      * @param msg The new message
      */
     private static void msg(Player player, String name, BlockType type, String msg) {
         //Cancel if the Quiz with the given name does not exist
         Quiz quiz = QuizBlock.findQuiz(name);
        if (quiz == null) {
             player.sendMessage("Quiz "+name+" does not exsist.");
             return;
         }
 
         //Set the message for the given BlockType
         switch (type) {
             case RIGHT:
                 quiz.right = QuizBlock.format(msg);
                 player.sendMessage("'Right' message for "+quiz.name+" is now '"+quiz.right+"'");
                 break;
                 
             case WRONG:
                 quiz.wrong = QuizBlock.format(msg);
                 player.sendMessage("'Wrong' message for "+quiz.name+" is now '"+quiz.wrong+"'");
                 break;
                 
             default: sendHelp(player); return;
         }
         
         QuizBlock.save();
     }
     
     /**
      * Displays a list of current Quiz
      * 
      * @param player The Player requesting the list
      */
     private static void list(Player player) {
         String quizList = "Current Quizes:  ";
         
         //Concat the name of each Quiz
         for (Quiz tempQuiz: QuizBlock.quizes)
             quizList = quizList.concat(tempQuiz.name+", ");
         
         player.sendMessage(quizList.substring(0, quizList.length() - 2));
     }
     
     /**
      * Reloads QuizBlock data
      * 
      * @param player The Player reloading the data 
      */
     private static void rl(Player player) {
         QuizBlock.quizes.clear();
         QuizBlock.save = true;
         QuizBlock.loadData();
         QuizBlock.pm = QuizBlock.server.getPluginManager();
         
         System.out.println("[QuizBlock] reloaded");
         if (player != null)
             player.sendMessage("QuizBlock reloaded");
         return;
     }
     
     /**
      * Displays the QuizBlock Help Page to the given Player
      *
      * @param player The Player needing help
      */
     private static void sendHelp(Player player) {
         player.sendMessage("§e     QuizBlock Help Page:");
         player.sendMessage("§2/quiz make [Name]§b Makes Quiz at target location");
         player.sendMessage("§2/quiz link right [Name]§b Links target Block with Quiz");
         player.sendMessage("§2/quiz link door [Name]§b Links target Block with Quiz");
         player.sendMessage("§2/quiz link wrong [Name]§b Links target Block with Quiz");
         player.sendMessage("§2/quiz msg right [Name] [msg]§b Sets right msg for Quiz");
         player.sendMessage("§2/quiz msg wrong [Name] [msg]§b Sets wrong msg for Quiz");
         player.sendMessage("§2/quiz unlink§b Unlinks target Block from Quiz");
         player.sendMessage("§2/quiz delete (Name)§b Deletes Quiz and unlinks Block");
         player.sendMessage("§2/quiz list§b Lists all Quizes");
         player.sendMessage("§2/quiz rl§b Reloads QuizBlock plugin");
     }
 }
