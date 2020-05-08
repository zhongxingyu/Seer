 package fr.aumgn.diamondrush.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.bukkitutils.command.Commands;
 import fr.aumgn.bukkitutils.util.Vector;
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.Team;
 import fr.aumgn.diamondrush.stage.JoinStage;
 import fr.aumgn.diamondrush.stage.Stage;
import fr.aumgn.diamondrush.stage.StaticStage;
 
 @NestedCommands(name = "diamondrush")
 public class PlayerCommands implements Commands {
 
     @Command(name = "join", max = 1)
     public void joinTeam(Player player, CommandArgs args) {
         Game game = DiamondRush.getGame();
         Stage stage = game.getStage();
 
         if (game.getSpectators().contains(player)) {
             throw new CommandError(
                     "Vous ne pouvez pas rejoindre la partie tant que vous etre spectateur.");
         }
 
        if (game.getStage() instanceof StaticStage) {
             throw new CommandError(
                     "Impossible de rejoindre la partie durant une phase de pause.");
         }
 
         Team team = null;
         if (args.length() > 0) {
             team = game.getTeam(args.get(0));
         }
 
         if (stage instanceof JoinStage) {
             JoinStage joinStage = (JoinStage) stage;
             if (joinStage.contains(player)) {
                 throw new CommandError("Vous êtes déjà dans la partie.");
             }
 
             ((JoinStage) stage).addPlayer(player, team);
         } else {
             if (game.contains(player)) {
                 throw new CommandError("Vous êtes déjà dans la partie.");
             }
 
             game.addPlayer(player, team);
             team = game.getTeam(player);
             Vector pos;
             if (team.getTotem() != null) {
                 pos = team.getTotem().getTeleportPoint();
             } else {
                 pos = new Vector(team.getForeman().getLocation());
             } 
             player.teleport(pos.toLocation(game.getWorld()));
             game.sendMessage(player.getDisplayName() + ChatColor.YELLOW +
                     " a rejoint l'équipe " + team.getDisplayName());
         }
     }
 
     @Command(name = "quit")
     public void quitGame(Player player, CommandArgs args) {
         Game game = DiamondRush.getGame();
         Stage stage = game.getStage();
 
         if (stage instanceof JoinStage) {
             JoinStage joinStage = (JoinStage) stage;
             if (!joinStage.contains(player)) {
                 throw new CommandError("Vous n'êtes pas dans la partie.");
             }
 
             joinStage.removePlayer(player);
         } else {
             if (!game.contains(player)) {
                 throw new CommandError("Vous n'êtes pas dans la partie.");
             }
 
             game.removePlayer(player);
             player.sendMessage(ChatColor.GREEN + "Vous avez quitté la partie.");
         }
     }
 }
