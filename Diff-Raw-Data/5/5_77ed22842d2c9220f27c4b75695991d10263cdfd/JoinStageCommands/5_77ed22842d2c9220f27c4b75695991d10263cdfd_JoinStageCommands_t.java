 package fr.aumgn.diamondrush.command;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.aumgn.bukkitutils.command.Commands;
 import fr.aumgn.bukkitutils.util.Vector;
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.Team;
 import fr.aumgn.diamondrush.game.TeamColor;
 import fr.aumgn.diamondrush.stage.JoinStage;
 import fr.aumgn.diamondrush.stage.RandomJoinStage;
 import fr.aumgn.diamondrush.stage.SimpleJoinStage;
 import fr.aumgn.diamondrush.stage.Stage;
 import fr.aumgn.diamondrush.stage.TotemStage;
 
 @NestedCommands(name = "diamondrush")
 public class JoinStageCommands implements Commands {
 
     @Command(name = "init", min = 1, max = -1, flags = "cn")
     public void initGame(Player player, CommandArgs args) {
         if (DiamondRush.isRunning()) {
             throw new CommandError("Une partie est déjà en cours.");
         }
 
         Map<String, TeamColor> teams = new HashMap<String, TeamColor>();
         if (args.hasFlag('n')) {
             List<String> teamsNames = args.asList();
             Iterator<TeamColor> colors = 
                     TeamColor.getRandomColors(teamsNames.size()).iterator();
             for (String name : teamsNames) {
                 teams.put(name, colors.next());
             }
         } else {
             if (args.length() > 1) {
                 throw new CommandUsageError("Cette commande ne prends qu'un seul argument.");
             }
             int amount = Integer.parseInt(args.get(0));
             Iterator<TeamColor> colors = 
                     TeamColor.getRandomColors(amount).iterator();
             for (;amount > 0; amount--) {
                 TeamColor color = colors.next();
                 teams.put(color.getColorName(), color);
             }
         }
 
         Game game = new Game(teams, player.getWorld(),
                 new Vector(player.getLocation()));
 
         JoinStage stage;
         if (args.hasFlag('c')) {
             stage = new SimpleJoinStage(game);
         } else {
             stage = new RandomJoinStage(game);
         }
         DiamondRush.initGame(game, stage);
     }
 
     @Command(name = "join", max = 1)
     public void joinTeam(Player player, CommandArgs args) {
         Game game = DiamondRush.getGame();
         Stage stage = game.getStage();
 
         if (game.getSpectators().contains(player)) {
             throw new CommandError(
                     "Vous ne pouvez pas rejoindre la partie tant que vous etre spectateur.");
         }
 
        if (game.isPaused()) {
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
 
     @Command(name = "start")
     public void startGame(CommandSender sender, CommandArgs args) {
         Game game = DiamondRush.getGame();
         Stage stage = game.getStage();
 
         if (!(stage instanceof JoinStage)) {
             throw new CommandError("Cette commande ne peut être utilisée que durant la phase de join.");
         }
 
         JoinStage joinStage = (JoinStage) stage;
         joinStage.ensureIsReady();
 
         if (stage.hasNextStageScheduled()) {
             throw new CommandError(
                     "La partie est déjà sur le point de démarrer.");
         }
 
         joinStage.prepare();
         game.sendMessage(ChatColor.GREEN + "La partie va commencer.");
         stage.scheduleNextStage(15, new TotemStage(game));
     }
 }
