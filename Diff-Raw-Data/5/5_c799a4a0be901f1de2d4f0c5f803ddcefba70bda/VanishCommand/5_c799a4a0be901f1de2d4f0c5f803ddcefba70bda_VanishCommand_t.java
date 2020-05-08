 package fr.noogotte.useful_commands.command;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.noogotte.useful_commands.UsefulCommandsPlugin;
 import fr.noogotte.useful_commands.component.VanishComponent;
 
 @NestedCommands("useful")
 public class VanishCommand extends UsefulCommands {
 
     private VanishComponent vanishComponent;
 
     public VanishCommand(VanishComponent vanishComponent, UsefulCommandsPlugin plugin) {
         super(plugin);
         this.vanishComponent = vanishComponent;
     }
 
     @Command(name = "vanish", min = 0, max = 1)
     public void vanish(CommandSender sender, CommandArgs args) {
         List<Player> targets = args.getPlayers(0)
                 .matchWithPermOr("useful.vanish.command.other", sender);
 
         for (Player target : targets) {
             if (!vanishComponent.isVanish(target)) {
                 vanishComponent.addPlayer(target);
                 for (Player allPlayer : Bukkit.getOnlinePlayers()) {
                     allPlayer.hidePlayer(target);
                 }
                 target.sendMessage(msg("vanish.target.isNotVisible"));
                 if (!sender.equals(target)) {
                     sender.sendMessage(msg("vanish.sender.isNotVisible", target.getDisplayName()));
                 }
             } else {
                 vanishComponent.removePlayer(target);
                 for (Player allPlayer : Bukkit.getOnlinePlayers()) {
                     allPlayer.showPlayer(target);
                 }
                target.sendMessage(msg("vanish.target.isVisible", target.getName()));
 
                 if (!sender.equals(target)) {
                    sender.sendMessage(msg("vanish.sender.isNotVisible" , target.getName()));
                 }
             }
         }
     }
 
     @Command(name = "vanish-list")
     public void vanishlist(CommandSender sender, CommandArgs args) {
         sender.sendMessage(ChatColor.GREEN + "Joueur caché(s) : ");
         for (Player player : vanishComponent.getVanishPLayer()) {
             sender.sendMessage(ChatColor.YELLOW + " - "
                     + player.getDisplayName());
         }
     }
 }
