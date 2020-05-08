 package fr.noogotte.useful_commands.command;
 
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.noogotte.useful_commands.component.HomeComponent;
 import fr.noogotte.useful_commands.component.HomeComponent.Home;
 
 @NestedCommands(name = "useful")
 public class HomeCommands extends UsefulCommands {
 
 	private HomeComponent hm;
 
 	public HomeCommands(HomeComponent homeComponent) {
 		this.hm = homeComponent;
 	}
 
 	@Command(name = "sethome")
 	public void setHome(Player sender, CommandArgs args) {
 		if (hm.haveHome(sender)) {
 			throw new CommandError("Vous avez déja définis un home");
 		}
 
 		sender.sendMessage(ChatColor.GREEN + "Home définis.");
 		hm.addHome(sender);
 	}
 
 	@Command(name = "home")
 	public void home(Player sender, CommandArgs args) {
 		if(!hm.haveHome(sender)) {
 			throw new CommandError("Vous n'avez pas de home !");
 		}
 
 		Home home = hm.getHome(sender);
 		sender.sendMessage(ChatColor.GREEN + "Poof !");
 		sender.teleport(home.toLocation());
		hm.save();
 	}
 
 	@Command(name = "deletehome")
 	public void delHome(Player sender, CommandArgs args) {
 		if (!hm.haveHome(sender)) {
 			throw new CommandError("Vous n'avez pas de home !");
 		}
 
 		sender.sendMessage(ChatColor.GREEN + "Home supprimer.");
 		hm.deleteHome(sender);
 		hm.save();
 	}
 
 	@Command(name = "homes")
 	public void homes(Player sender, CommandArgs args) {
		if( hm.isEmpty()) {
 			throw new CommandError("Aucun home de sauvegarder !");
 		}
 
 		sender.sendMessage(ChatColor.YELLOW +"Warp (" + hm.getNbHome() + "):");
 		for (Entry<String, Home> homes : hm.homes()) {
 			sender.sendMessage(ChatColor.YELLOW + " - " + homes.getKey());
 		}
 	}
 }
