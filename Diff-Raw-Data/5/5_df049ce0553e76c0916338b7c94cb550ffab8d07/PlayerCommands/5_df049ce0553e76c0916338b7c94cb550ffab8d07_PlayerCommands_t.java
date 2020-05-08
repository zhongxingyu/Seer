 package fr.noogotte.useful_commands.command;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.aumgn.bukkitutils.util.Util;
 
 @NestedCommands(name = "useful")
 public class PlayerCommands extends UsefulCommands {
 
     @Command(name = "gamemode", flags = "cs", min = 0, max = 1)
     public void gamemode(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
         boolean force = args.hasFlags();
         GameMode gameMode = null;
         if (args.hasFlag('c')) {
             gameMode = GameMode.CREATIVE;
         } else {
             gameMode = GameMode.SURVIVAL;
         }
 
         for (Player target : targets) {
             if (force && target.getGameMode() == gameMode) {
                 continue;
             }
 
             if (target.getGameMode() == GameMode.CREATIVE) {
                 target.setGameMode(GameMode.SURVIVAL);
             } else {
                 target.setGameMode(GameMode.CREATIVE);
             }
 
             target.sendMessage(ChatColor.GREEN
                     + "Vous êtes maintenant en "
                     + ChatColor.AQUA + target.getGameMode());
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN
                         + "Vous avez mis "
                         + ChatColor.AQUA + target.getName()
                         + ChatColor.GREEN + " en "
                         + ChatColor.AQUA +  target.getGameMode());
             }
         }
     }
 
     @Command(name = "heal", flags = "hf", min = 0, max = 1)
     public void heal(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
         boolean health = !args.hasFlags() || args.hasFlag('h');
         boolean food = !args.hasFlags() || args.hasFlag('f');
 
         for (Player target : targets) {
             if (health) {
                 target.setHealth(20);
             }
             if (food) {
                 target.setFoodLevel(20);
             }
 
             target.sendMessage(ChatColor.YELLOW
                     + "Vous voilà soigné et nourri");
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN
                         + "Vous avez soigné et nourri "
                         + ChatColor.BLUE + target.getName());
             }
         }
     }
 
     @Command(name = "kick", flags = "o", min = 1, max = -1)
     public void kick(Player player, CommandArgs args) {
         String reason = args.get(1, -1);
         boolean dontKickOps = !args.hasFlag('o');
 
         for (Player target : args.getPlayers(0)) {
             if (dontKickOps && target.isOp()) {
                 player.sendMessage(ChatColor.RED + target.getName()
                         + " est OP vous ne pouvez pas le kicker.");
             } else {
                 target.kickPlayer(reason);
                 Util.broadcast(ChatColor.AQUA + target.getName()
                         + ChatColor.GREEN + " a été kické par "
                         + ChatColor.AQUA + player.getName());
             }
         }
     }
 
     @Command(name = "tell", min = 2, max = -1)
     public void tell(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0);
         String message = args.get(1, -1);
 
         StringBuilder receivers = new StringBuilder();
         for (Player target : targets) {
             target.sendMessage(ChatColor.ITALIC.toString()
                     + ChatColor.AQUA
                     + "De " + player.getDisplayName()
                    + ChatColor.WHITE +  " " + message);
 
             receivers.append(target.getDisplayName());
             receivers.append(" ");
            
            System.out.println("[MSG] de " + player.getName() +  " a " + target.getName() + ": " + message);
         }
 
        player.sendMessage(ChatColor.ITALIC.toString()
                 + ChatColor.AQUA + "A " + receivers + ":");
         player.sendMessage("  " + message);
     }
 
     @Command(name = "fly", min = 0, max = 1)
     public void fly(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
         for (Player target : targets) {
             if (target.isFlying()) {
                 target.setAllowFlight(false);
                 target.setFlying(false);
             } else {
                 target.setAllowFlight(true);
                 target.setFlying(true);
             }
             if (target.isFlying()) {
                 player.sendMessage(ChatColor.GREEN
                         + " Vous pouvez désormais voler !");
             } else {
                 player.sendMessage(ChatColor.GREEN
                         + " Vous ne pouvez plus voler !");
             }
 
             if (!player.equals(target)) {
                 if (target.isFlying()) {
                     player.sendMessage(ChatColor.GOLD + target.getName()
                             + ChatColor.GREEN + " peut maintenant voler !");
                 } else {
                     player.sendMessage(ChatColor.GOLD + target.getName()
                             + ChatColor.GREEN + " ne peut plus voler !");
                 }
             }
         }
     }
 
     @Command(name = "kill", min = 0, max = 1)
     public void kill(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
         for (Player target : targets) {
             target.setHealth(0);
             player.sendMessage(ChatColor.GREEN + "Vous vous êtes suicidés !");
 
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN + "Vous avez tué "
                         + ChatColor.WHITE + target.getName());
             }
         }
     }
 
     @Command(name = "effect", min = 1, max = 3)
     public void effect(Player player, CommandArgs args) {
         PotionEffectType effect = args.getPotionEffectType(0);
         Integer duration = args.getInteger(1, 60);
         PotionEffect newEffect = new PotionEffect(
                 effect, duration * 20, 1);
 
         List<Player> targets = args.getPlayers(2, player);
 
         for (Player target : targets) {
             target.addPotionEffect(newEffect, true);
             player.sendMessage(ChatColor.GREEN +"Vous êtes sous influence de " +
                     ChatColor.GOLD + effect.getName());
 
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GOLD + target.getName() + 
                         ChatColor.GREEN + " est désormé sous l'effet de " + 
                         ChatColor.GOLD + effect.getName() + 
                         ChatColor.GREEN + " pour " + effect.getDurationModifier());
             }
         }
     }
 }
