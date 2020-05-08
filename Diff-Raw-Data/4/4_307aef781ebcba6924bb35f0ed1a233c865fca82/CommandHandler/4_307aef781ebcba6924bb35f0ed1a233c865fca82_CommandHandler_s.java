 package net.swagserv.andrew2060.swagservbounties;
 
 import net.swagserv.jones12.swagservbounties.Bounties;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.massivecraft.factions.Factions;
 
 //Command Handling for /bounty: faction bounties in FactionCommandHandler
 public class CommandHandler implements CommandExecutor {
 	private Bounties plugin;
 	@SuppressWarnings("unused")
 	private Player player = null;
 	
 	private String posterPlayerName;
 	private String wantedPlayerName;
 	private String factionName;
 	
 	private double temp = 0.00;
 	private double accntBalance = 0.00;
 	private double bountyamount = 0.00;
 	
 	private int killcount = 0;
 	
 	public CommandHandler(Bounties plugin) {
 		this.plugin = plugin;
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		if(cmd.getName().equalsIgnoreCase("bounty")){ 
 			if ((sender instanceof Player)) {
 					player = (Player) sender;
 		        } 
 				else {
 					sender.sendMessage("This command cannot be run from the console!");
 					return true;
 		        }
 			if (args.length > 5) {
 					return false;
 		        } 
 			//No args splash screen
 			if (args.length == 0){
 					sender.sendMessage(ChatColor.AQUA + "======Swagserv-Bounties Plugin======");
 					sender.sendMessage(ChatColor.GRAY + "Developed By Jones12 and Andrew2060 of Minecraft Server www.swagserv.us");
 					sender.sendMessage(ChatColor.GRAY + "For Help, use " + ChatColor.RED + "/bounty help");
 					return true;
 			}
 			if (args.length == 1){
 				//Help Screen
 				if(args[0].equalsIgnoreCase("help")){
 					sender.sendMessage(ChatColor.AQUA + "=======Swagserv-Bounties Help=======");
 					sender.sendMessage(ChatColor.RED + "/bounty help create " + ChatColor.GRAY + "- Help With Bounty Creation");
 					sender.sendMessage(ChatColor.RED + "/bounty help delete " + ChatColor.GRAY + "- Help With Bounty Deletion");
 					sender.sendMessage(ChatColor.RED + "/bounty reload " + ChatColor.GRAY + "- Reload Bounty Config");
 					return true;
 				}
 				//included for efficiency (config reload command)
 				if(args[0].equalsIgnoreCase("reload")){
 					sender.sendMessage("Not Implemented Yet");
 					return true;
 				}
 				else {
 					return false;
 				}
 			}
 			if (args.length == 2){
 				if(args[0].equalsIgnoreCase("help")){
 					if(args[1].equalsIgnoreCase("create")){
 						sender.sendMessage(ChatColor.AQUA + "=======Swagserv-Bounties Help=======");
 						sender.sendMessage(ChatColor.RED + "/bounty create [player|faction] [playerorfactionname] [amount] [killcount]");
 						sender.sendMessage(ChatColor.GRAY + "Replace [player|faction] with player or faction as desired");
 						sender.sendMessage(ChatColor.GRAY + "Replace [playerorfactionname] with desired target player or target faction, " +
 							"depending on your previous argument");
 						sender.sendMessage(ChatColor.GRAY + "Replace [amount] with amount you are paying for the bounty");
 						sender.sendMessage(ChatColor.GRAY + "Replace [killcount] with number of times you want the target killed before paying reward," +
 							" in the case of a faction, this is the number of faction members killed");
 						return true;
 					}
 					if(args[1].equalsIgnoreCase("delete")){
 						sender.sendMessage(ChatColor.RED + "/bounty delete [BountyID]" + ChatColor.GRAY + " - Delete Bounty of ID [BountyID]");
 						return true;
 					}
 				}
 			}
 			//Begin /bounty delete implementation
 			if(args.length == 3){
 				if(args[0].equalsIgnoreCase("delete")){
 					sender.sendMessage("Not Implemented Yet");
 					return true;
 				}
 				else {
 					return false;
 				}
 			}
 			if(args.length == 5){
 				//Begin /bounty create implementation
 				if (args[0].equalsIgnoreCase("create")){
 					bountyamount = Double.parseDouble(args[3]);
 					killcount = Integer.parseInt(args[4]);
 				
 					//For Players
 					if(args[1].equalsIgnoreCase ("player")) {
 						wantedPlayerName = args[2];
 						if(plugin.permission.has(sender, "bounties.bountycreate.player")) { //Permissions Check
 						//Begin checking validity of command
 						//Check to ensure player exists
 							if(Bukkit.getServer().getPlayer(wantedPlayerName) == null) {
 								sender.sendMessage("Target Player Does Not Exist or is not online!");
 								return true;
 							}
 					
 							else if(Bukkit.getServer().getPlayer(wantedPlayerName) != null) {
 							//if(InsertFactionAllianceCheckHere)
 							//Begin Economy Section (Derived from com.gmail.brandonjones1212.swagservbounties.SignListener.java (deprecated))
 								accntBalance = plugin.economy.getBalance(posterPlayerName);
 								if(accntBalance<500+bountyamount) {
 									temp = (500+bountyamount)-accntBalance;
									sender.sendMessage(ChatColor.YELLOW + "You do not have enough money to place a bounty on the faction " + 
 											ChatColor.RED + wantedPlayerName + ChatColor.YELLOW + " for" + ChatColor.GREEN + " $" + bountyamount + 
 											ChatColor.YELLOW + " in addition to the"+ ChatColor.GOLD + " $500" + ChatColor.YELLOW + " bounty posting fee");								
 									sender.sendMessage(ChatColor.YELLOW + "You need $" + temp + " more.");
 								}	
 								else {
 									plugin.getServer().broadcastMessage(ChatColor.BLUE + "[Bounty]: " + 
 											ChatColor.AQUA + posterPlayerName + 
 											ChatColor.WHITE + " placed a hit on " + ChatColor.RED + wantedPlayerName + 
 											ChatColor.WHITE + " to be killed " + ChatColor.GOLD + killcount + 
 											ChatColor.WHITE + " times for" + ChatColor.DARK_GREEN + " $" + 
 											bountyamount + ChatColor.WHITE + ".");
 									temp = 500+bountyamount;
 									plugin.economy.withdrawPlayer(wantedPlayerName, temp);
 									sender.sendMessage(ChatColor.YELLOW + "The bounty fee of " + ChatColor.RED + "$" + 500 +
 											ChatColor.YELLOW + " and your bounty of " + ChatColor.RED + "$" + bountyamount + 
 											ChatColor.YELLOW + " has been withdrawn from your account.");
 								}
 							//End Economy Section: Note that nothing actually happens as of yet 
 							//} End Factions Check If
 							return true;
 							}
 						}
 				//Insert Else Statement for no perms here (or let bukkit handle it)
 					}
 					else if(args[1].equalsIgnoreCase ("faction")) {
 						if(plugin.permission.has(sender, "bounties.bountycreate.faction")) {
 							//Check for valid faction
 							factionName = args[2];
 							//Factions.i contains all the faction commands we'll need
 							//because it is a static variable it can be used
 							//from any class without being initialized, because it's already
 							//initialized in the Factions class.
 							if(Factions.i.isTagTaken(factionName)) {
 								accntBalance = plugin.economy.getBalance(posterPlayerName);
 								if(accntBalance<500+bountyamount) {
 									temp = (500+bountyamount)-accntBalance;
 									sender.sendMessage(ChatColor.YELLOW + "You do not have enough money to place a bounty on the faction " + 
 												   ChatColor.RED + factionName + ChatColor.YELLOW + " for" + ChatColor.GREEN + " $" + bountyamount + 
 												   ChatColor.YELLOW + " or the"+ ChatColor.GOLD + " $500" + ChatColor.YELLOW + " bounty posting fee");
 									sender.sendMessage(ChatColor.YELLOW + "You need $" + temp + " more.");
 								}
 							else {
 									plugin.getServer().broadcastMessage(ChatColor.BLUE + "[Bounty]: " + 
 										ChatColor.AQUA + posterPlayerName + 
 										ChatColor.WHITE + " placed a hit on the faction " + ChatColor.RED + factionName + 
 										ChatColor.WHITE + " to have members killed " + ChatColor.GOLD + killcount + 
 										ChatColor.WHITE + " times for" + ChatColor.DARK_GREEN + " $" + 
 										bountyamount + ChatColor.WHITE + ".");
 									temp = 500+bountyamount;
 									plugin.economy.withdrawPlayer(wantedPlayerName, temp);
 									sender.sendMessage(ChatColor.YELLOW + "The bounty fee of " + ChatColor.RED + "$" + 500 + 
 													ChatColor.YELLOW + " and your bounty of " + ChatColor.RED + "$" + bountyamount + 
 													ChatColor.YELLOW + " has been withdrawn from your account.");
 							}
 							return true;
 						}
 						else if(!Factions.i.isTagTaken(factionName)) {
 							sender.sendMessage("Faction doesn't exist!");
 							return true;
 						}
 						}
 					}
 				}
 			}
 			}
 		return false; 
 		}
 	}
 
