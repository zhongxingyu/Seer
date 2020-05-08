 package net.swagserv.andrew2060.swagservbounties;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.massivecraft.factions.FPlayer;
 import com.massivecraft.factions.FPlayers;
 import com.massivecraft.factions.Faction;
 import com.massivecraft.factions.struct.Rel;
 
 //Command Handling for /bounty: faction bounties in FactionCommandHandler
 public class CommandHandler implements CommandExecutor {
 	private Bounties plugin;
 	private String posterPlayerName;
 	private String wantedPlayerName;
 	private double temp = 0.00;
 	private double accntBalance = 0.00;
 	private double bountyamount = 0.00;
 	private MySQLConnection sqlHandler;
 	
   	
 	public CommandHandler(Bounties plugin) {
 		this.plugin = plugin;
 		try {
 			this.sqlHandler = new MySQLConnection(plugin.dbHost, plugin.dbPort, plugin.dbDatabase, plugin.dbUser,
 					plugin.dbPass);
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		sqlHandler.connect();
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if(cmd.getName().equalsIgnoreCase("bounty")){ 
 			if (!(sender instanceof Player)) {
 					sender.sendMessage("This command cannot be run from the console!");
 					return true;		        
 			} 
 			switch (args.length){
 				case 0: 
 					sender.sendMessage(ChatColor.AQUA + "======Swagserv-Bounties Plugin======");
 					sender.sendMessage(ChatColor.GRAY + "Developed By Andrew2060 of Minecraft Server www.swagserv.us");
 					sender.sendMessage(ChatColor.GRAY + "For Help, use " + ChatColor.RED + "/bounty help");
 					break;
 				case 1: 
 					if(args[0].equalsIgnoreCase("help")){
 						sender.sendMessage(ChatColor.AQUA + "=======Swagserv-Bounties Help=======");
 						sender.sendMessage(ChatColor.RED + "/bounty" + ChatColor.GRAY + " - Bounty Plugin Info");
 						sender.sendMessage(ChatColor.RED + "/bounty help list" + ChatColor.GRAY + " - Help Viewing Currently Active Bounties");
 						sender.sendMessage(ChatColor.RED + "/bounty help create " + ChatColor.GRAY + "- Help With Bounty Creation");
 						sender.sendMessage(ChatColor.RED + "/bounty help delete " + ChatColor.GRAY + "- Help With Bounty Deletion");
 						sender.sendMessage(ChatColor.RED + "/bounty reload " + ChatColor.GRAY + "- Reload Bounty Config");
 						break;
 					}
 					if(args[0].equalsIgnoreCase("reload")){
 						sender.sendMessage("Not Implemented Yet");
 						break;
 					}
 					displayError(sender);
 					return true;
 				case 2: 
 					if(args[0].equalsIgnoreCase("help")){
 						if(args[1].equalsIgnoreCase("create")){
 							sender.sendMessage(ChatColor.AQUA + "=======Swagserv-Bounties Help=======");
 							sender.sendMessage(ChatColor.RED + "/bounty create [playername] [amount]");
 							sender.sendMessage(ChatColor.GRAY + "Replace [playername] with desired target player.");
 							sender.sendMessage(ChatColor.GRAY + "Replace [amount] with amount you are paying for the bounty");
 							break;
 						}
 						if(args[1].equalsIgnoreCase("list")) {
 							sender.sendMessage(ChatColor.RED + "/bounty list [threshold]" + ChatColor.GRAY + " - Lists all currently active bounties with a payout higher than the threshold");
 							break;
 						}
 						if(args[1].equalsIgnoreCase("delete")){
 							sender.sendMessage(ChatColor.RED + "/bounty delete [BountyID]" + ChatColor.GRAY + " - Delete Bounty of ID [BountyID]");
 							break;
 						}
 						displayError(sender);
 						return true;
 					}
 					if(args[0].equalsIgnoreCase("list")) {
 						double lowerbound = 0;
 						try {
 							lowerbound = Double.parseDouble(args[1]);
 						} catch (NumberFormatException e){
 							sender.sendMessage(ChatColor.RED + "This is not a valid lower threshold number!");
 							return true;
 						}
 						sender.sendMessage(ChatColor.AQUA + "=======Bounties List with Payouts Greater Than $" + lowerbound + "=======");
 						try {
 							ResultSet list = sqlHandler.executeQuery("SELECT * FROM bountiesplayer WHERE amount >='" + lowerbound + "'", false);
 							while(list.next()) {
 								String target = list.getString("target");
 								Double amount = list.getDouble("amount");
 								if(amount <= 0) {
 									continue;
 								}
 								sender.sendMessage(target + " - " + ChatColor.GOLD + "$" + amount);
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						break;
 					}
 					break;
 				case 3: 
 					if(args[0].equalsIgnoreCase("delete")){
 						sender.sendMessage("Not Implemented Yet");
 						break;
 					}
 					if(args[0].equalsIgnoreCase("create")){
 						bountyamount = Double.parseDouble(args[2]);
 						if(bountyamount <= 0) {
 							sender.sendMessage(ChatColor.RED + "Bounties cannot be set at a 0 or negative value!");
 							return true;
 						}
 						wantedPlayerName = args[1];
 						posterPlayerName = sender.getName();				
 						//For Players
 						if(plugin.permission.has(sender, "bounties.create")) { //Permissions Check
 							//Begin checking validity of command
 							//Check to ensure player exists
 							if(Bukkit.getServer().getPlayer(wantedPlayerName) == null) {
 								sender.sendMessage("Target Player Does Not Exist or is not online!");
 							return true;
 							}
 							else if(Bukkit.getServer().getPlayer(wantedPlayerName) != null) {			
 								FPlayer fpWanted = FPlayers.i.get(Bukkit.getServer().getPlayer(wantedPlayerName));
 								FPlayer fpPoster = FPlayers.i.get(Bukkit.getServer().getPlayer(sender.getName()));
 								Faction fWanted = fpWanted.getFaction();
 								Faction fPoster = fpPoster.getFaction();
 								if(fWanted.getRelationTo(fPoster).equals(Rel.ALLY) || fWanted.getRelationTo(fPoster).equals(Rel.TRUCE)) {
 									sender.sendMessage(ChatColor.GRAY + "You cannot create a bounty on an ally/truced faction member!");
 									return true;
 								}
 								if (fWanted == fPoster) {
 									sender.sendMessage(ChatColor.GRAY + "You cannot create a bounty on your own factionmates! How absolutely rude!");
 									return true;
 								} else {
 									accntBalance = plugin.economy.getBalance(posterPlayerName);
 									if(accntBalance<bountyamount*1.1) {
 										temp = (bountyamount*1.1)-accntBalance;
 										sender.sendMessage(ChatColor.YELLOW + "You do not have enough money to place a bounty on the player " + 
 												ChatColor.RED + wantedPlayerName + ChatColor.YELLOW + " for" + ChatColor.GREEN + " $" + bountyamount + 
 												ChatColor.YELLOW + " in addition to the"+ ChatColor.GOLD + " $" + bountyamount*0.1 + ChatColor.YELLOW + " bounty posting fee");								
 										sender.sendMessage(ChatColor.YELLOW + "You need $" + temp + " more.");
 									}
 									plugin.getServer().broadcastMessage(ChatColor.BLUE + "[Bounty]: " + 
 											ChatColor.AQUA + posterPlayerName + 
 											ChatColor.WHITE + " placed a hit on " + ChatColor.RED + wantedPlayerName + 
 											ChatColor.WHITE + " to be killed for" + ChatColor.DARK_GREEN + " $" + 
 											bountyamount + ChatColor.GRAY + ".");
 									temp = bountyamount*1.1;
									plugin.economy.withdrawPlayer(sender.getName(), temp);
 									sender.sendMessage(ChatColor.YELLOW + "The bounty fee of " + ChatColor.RED + "$" + bountyamount*0.1 +
 											ChatColor.YELLOW + " and your bounty of " + ChatColor.RED + "$" + bountyamount + 
 											ChatColor.YELLOW + " has been withdrawn from your account.");
 									if (plugin.MySQL = true) {
 										if(checkExists(wantedPlayerName) == 1) {
 											String checkExists = "SELECT * FROM bountiesplayer WHERE target = '"+wantedPlayerName + "'";
 											try {
 												ResultSet rs = sqlHandler.executeQuery(checkExists, false);
 												rs.last();
 												int id = rs.getInt("id");
 												double currentbounty = rs.getDouble("amount");
 												double newbounty = currentbounty + bountyamount;
 												String update = "UPDATE bountiesplayer SET amount='" + newbounty + "' WHERE id='"+id+"'";
 												sqlHandler.executeQuery(update, true);
 											} catch (SQLException e){
 												e.printStackTrace();
 											}
 										} else {
 						 					String query = "INSERT INTO bountiesplayer (target,amount) VALUES ('" 
 						 							+ wantedPlayerName + "', '" + bountyamount + "')";
 						 					try {
 						 						sqlHandler.executeQuery(query, true);
 						 					} catch (SQLException e) {
 						 						e.printStackTrace();
 						 					}
 										}
 									}
 
 									//End Economy Section: Note that nothing actually happens as of yet 
 								} 
 							}
 							return true;
 						} else {
 							sender.sendMessage(ChatColor.RED + "No Permissions!");
 							return true;
 						}
 					}
 					displayError(sender);
 					break;
 				default:
 					displayError(sender);
 					break;
 			}
 		}
         return true;
     }
 	public void displayError(CommandSender sender) {
 		sender.sendMessage(ChatColor.AQUA + "======Swagserv-Bounties Plugin======");
 		sender.sendMessage(ChatColor.RED + "Invalid Command");
 		sender.sendMessage(ChatColor.GRAY + "For Help, use " + ChatColor.RED + "/bounty help");
 		return;
 	}
 	public int checkExists(String wantedPlayerName) {
 		String checkExists = "SELECT * FROM bountiesplayer WHERE target = '"+wantedPlayerName + "'";
 		try {
 			ResultSet checkResult = sqlHandler .executeQuery(checkExists, false);
 			if(checkResult.last()) {
 				return 1;
 			} else {
 				return 0;
 			}
 		} catch (SQLException e) {
 				e.printStackTrace();
 		}
 		return 0;
 		
 	}
 }
 
