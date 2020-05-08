 package to.joe.j2mc.tournament.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.tournament.J2MC_Tournament;
 
 public class JoinCommand extends MasterCommand {
 
 	J2MC_Tournament plugin;
 
 	public JoinCommand(J2MC_Tournament tournament) {
 		super(tournament);
 		this.plugin = tournament;
 	}
 
 	@Override
 	public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "Only players may use this command");
 		}
 		if (this.plugin.participants.contains(player)) {
 			sender.sendMessage(ChatColor.AQUA + "You are already signed up for this tournament");
 		} else if (this.plugin.registrationOpen) {
 			this.plugin.participants.add(player);
 			sender.sendMessage(ChatColor.AQUA + "You have been entered into the tournament");
 			J2MC_Manager.getCore().adminAndLog(ChatColor.RED + sender.getName() + ChatColor.AQUA + " has entered the tournament!");
 			J2MC_Manager.getCore().messageNonAdmin(ChatColor.RED + sender.getName() + ChatColor.AQUA + " has entered the tournament!");
 		} else {
 			sender.sendMessage(ChatColor.AQUA + "Tournament registration is closed");
 		}
 	}
 
 }
