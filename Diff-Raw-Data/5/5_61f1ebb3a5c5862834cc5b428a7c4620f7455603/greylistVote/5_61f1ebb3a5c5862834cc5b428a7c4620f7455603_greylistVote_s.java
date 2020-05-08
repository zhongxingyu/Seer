 package me.ellbristow.greylistVote;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class greylistVote extends JavaPlugin {
 	
     public static greylistVote plugin;
     public final greyBlockListener blockListener = new greyBlockListener(this);
     public final greyPlayerListener loginListener = new greyPlayerListener(this);
     protected FileConfiguration config;
     public FileConfiguration usersConfig = null;
     private File usersFile = null;
     protected int approvalVotes;
     protected boolean noPVP;
     private boolean allowApprovedVote;
 	
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public void onEnable() {
             PluginManager pm = getServer().getPluginManager();
             pm.registerEvents(blockListener, this);
             pm.registerEvents(loginListener, this);
             this.config = this.getConfig();
             approvalVotes = this.config.getInt("required_votes", 2);
             this.config.set("required_votes", approvalVotes);
             noPVP = this.config.getBoolean("no_pvp", true);
             this.config.set("no_pvp", noPVP);
             allowApprovedVote = this.config.getBoolean("allow_all_approved_to_vote", false);
             this.config.set("allow_all_approved_to_vote", allowApprovedVote);
             this.saveConfig();
             this.usersConfig = this.getUsersConfig();
 	}
 	
         @Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("glv")) {
 			if (args.length == 0) {
 				PluginDescriptionFile pdfFile = this.getDescription();
 				sender.sendMessage(ChatColor.GOLD + pdfFile.getName() + " version " + pdfFile.getVersion() + " by " + pdfFile.getAuthors());
 				sender.sendMessage(ChatColor.GOLD + "Commands: {optional} [required]");
 				sender.sendMessage(ChatColor.GOLD + "  /glv " + ChatColor.GRAY + ": View all GreylistVote commands");
 				sender.sendMessage(ChatColor.GOLD + "  /greylist [player] || /gl [player] || /trust Player " + ChatColor.GRAY + ":");
 				sender.sendMessage(ChatColor.GRAY + "    Increase player's reputation");
 				sender.sendMessage(ChatColor.GOLD + "  /griefer [player] " + ChatColor.GRAY + ":");
 				sender.sendMessage(ChatColor.GRAY + "    Reduce player's reputation");
 				sender.sendMessage(ChatColor.GOLD + "  /votelist {player} || /glvlist {player} " + ChatColor.GRAY + ":");
 				sender.sendMessage(ChatColor.GRAY + "    View your (or player's) reputation");
 				if (sender.hasPermission("greylistvote.admin")) {
 					sender.sendMessage(ChatColor.RED + "Admin Commands:");
 					sender.sendMessage(ChatColor.GOLD + "  /glv setrep [req. rep] " + ChatColor.GRAY + ": Set required reputation");
                                         sender.sendMessage(ChatColor.GOLD + "  /glv toggle [pvp|approvedvote] " + ChatColor.GRAY + ": Toggle true/false config options");
 					sender.sendMessage(ChatColor.GOLD + "  /glv clearserver [player] " + ChatColor.GRAY + ": Remove player's Server votes");
 					sender.sendMessage(ChatColor.GOLD + "  /glv clearall [player] " + ChatColor.GRAY + ": Remove all player's votes");
 				}
 				return true;
 			}
 			else if (args.length == 2) {
 				if (!sender.hasPermission("greylistvote.admin")) {
 					sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
 					return false;
 				}
 				if (args[0].equalsIgnoreCase("setrep")) {
 					try {
 						approvalVotes = Integer.parseInt(args[1]);
 					}
 					catch(NumberFormatException nfe) {
 						// Failed. Number not an integer
 						sender.sendMessage(ChatColor.RED + "[req. votes] must be a number!" );
 						return false;
 					}
 					this.config.set("required_votes", approvalVotes);
 					this.saveConfig();
 					sender.sendMessage(ChatColor.GOLD + "Reputation requirement now set to " + ChatColor.WHITE + args[1]);
 					sender.sendMessage(ChatColor.GOLD + "Player approval will be updated on next login.");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("clearserver")) {
 					OfflinePlayer target = getServer().getOfflinePlayer(args[1]);
 					if (!target.hasPlayedBefore()) {
 						sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + args[0] + ChatColor.RED + "not found!");
 						return true;
 					}
 					String griefList = this.usersConfig.getString(target.getName().toLowerCase() + ".griefer", null);
 					String voteList = this.usersConfig.getString(target.getName().toLowerCase() + ".votes", null);
 					String[] voteArray;
 					String[] griefArray;
 					if (griefList == null && voteList == null) {
 						sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + target.getName() + ChatColor.RED + " does not have any votes!");
 						return true;
 					}
 					String newVoteList = null;
 					String[] newVoteArray = null;
 					if (voteList != null) {
 						voteArray = voteList.split(",");
 						for (String vote : voteArray) {
 							if (!vote.equals("Server")) {
 								if (newVoteList != null) {
 									newVoteList += "," + vote;
 								} else {
 									newVoteList = vote;
 								}
 							}
 						}
 						if (newVoteList != null) {
 							newVoteArray = newVoteList.split(",");
 						}
 						usersConfig.set(target.getName().toLowerCase() + ".votes", newVoteList);
 					}
 					String newGriefList = null;
 					String[] newGriefArray = null;
 					if (griefList != null) {
 						griefArray = griefList.split(",");
 						for (String vote : griefArray) {
 							if (!vote.equals("Server")) {
 								if (newGriefList != null) {
 									newGriefList += "," + vote;
 								} else {
 									newGriefList = vote;
 								}
 							}
 						}
 						if (newGriefList != null) {
 							newGriefArray = newGriefList.split(",");
 						}
 						usersConfig.set(target.getName().toLowerCase() + ".griefer", newGriefList);
 					}
 					saveUsersConfig();
 					int rep = 0;
 					if (newVoteList != null) {
 						rep += newVoteArray.length;
 					}
 					if (newGriefList != null) {
 						rep -= newGriefArray.length;
 					}
 					sender.sendMessage(ChatColor.GOLD + "'Server' votes removed from " + ChatColor.WHITE + target.getName());
 					if (target.isOnline()) {
 						target.getPlayer().sendMessage(ChatColor.GOLD + "Your Server Approval/Black-Ball votes were removed!");
 						if (rep >= approvalVotes && !target.getPlayer().hasPermission("greylistvote.build") && !target.getPlayer().hasPermission("greylistvote.approved")) {
 							setApproved(target.getPlayer());
 						}
 						else if (rep < approvalVotes && target.getPlayer().hasPermission("greylistvote.build") && !target.getPlayer().hasPermission("greylistvote.approved")) {
 							setGriefer(target.getPlayer());
 						}
 					} else {
 						Player[] onlinePlayers = getServer().getOnlinePlayers();
 						for (Player chatPlayer : onlinePlayers) {
 							if (!chatPlayer.getName().equals(sender.getName())) {
 								chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation has been reset to 0!");
 							}
 						}
 					}
 					this.saveUsersConfig();
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("clearall")) {
 					OfflinePlayer target = getServer().getOfflinePlayer(args[1]);
 					if (!target.hasPlayedBefore()) {
 						sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + args[0] + ChatColor.RED + "not found!");
 						return true;
 					}
 					String griefList = this.usersConfig.getString(target.getName().toLowerCase() + ".griefer", null);
 					String voteList = this.usersConfig.getString(target.getName().toLowerCase() + ".votes", null);
 					if (griefList == null && voteList == null) {
 						sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + target.getName() + ChatColor.RED + " does not have any votes!");
 						return true;
 					}
 					usersConfig.set(target.getName().toLowerCase() + ".votes", null);
 					usersConfig.set(target.getName().toLowerCase() + ".griefer", null);
 					sender.sendMessage(ChatColor.GOLD + "ALL votes removed from " + ChatColor.WHITE + target.getName());
 					if (target.isOnline()) {
 						target.getPlayer().sendMessage(ChatColor.RED + "Your reputation was reset to 0!");
 						if (0 >= approvalVotes && !target.getPlayer().hasPermission("greylistvote.build") && !target.getPlayer().hasPermission("greylistvote.approved")) {
 							setApproved(target.getPlayer());
 						}
 						else if (0 < approvalVotes && target.getPlayer().hasPermission("greylistvote.build") && !target.getPlayer().hasPermission("greylistvote.approved")) {
 							setGriefer(target.getPlayer());
 						}
 					} else {
 						Player[] onlinePlayers = getServer().getOnlinePlayers();
 						for (Player chatPlayer : onlinePlayers) {
 							if (!chatPlayer.getName().equals(sender.getName())) {
 								chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation has been reset to 0!");
 							}
 						}
 					}
 					this.saveUsersConfig();
 					return true;
 				} else if (args[0].equalsIgnoreCase("toggle")) {
                                     if (args[1].equalsIgnoreCase("pvp")) {
                                         if (noPVP) {
                                             noPVP = false;
                                             sender.sendMessage(ChatColor.GOLD + "Unapproved players are no longer protected against PVP.");
                                         } else {
                                             noPVP = true;
                                             sender.sendMessage(ChatColor.GOLD + "Unapproved players are now protected against PVP.");
                                         }
                                         config.set("no_pvp", noPVP);
                                         saveConfig();
                                     } else if (args[1].equalsIgnoreCase("approvedvote")) {
                                         if (allowApprovedVote) {
                                             allowApprovedVote = false;
                                             sender.sendMessage(ChatColor.GOLD + "Only player with permission may now vote!");
                                         } else {
                                             allowApprovedVote = true;
                                             sender.sendMessage(ChatColor.GOLD + "All approved players may now vote!");
                                         }
                                         config.set("allow_all_approved_to_vote", allowApprovedVote);
                                         saveConfig();
                                     } else {
                                         sender.sendMessage(ChatColor.RED + "Toggle option not recognised!");
                                         sender.sendMessage(ChatColor.RED + "Allowed toggles: pvp, approvedvote");
 					return false;
                                     }
                                 }
 				else {
 					sender.sendMessage(ChatColor.RED + "Command not recognised!");
 					return false;
 				}
 				
 			}
                         sender.sendMessage(ChatColor.RED + "Command not recognised!");
 			return false;
 		}
 		else if (commandLabel.equalsIgnoreCase("greylist") || commandLabel.equalsIgnoreCase("gl") || commandLabel.equalsIgnoreCase("trust")) {
 			if (args.length != 1) {
 				// No player specified or too many arguments
 				return false;
 			}
 			else {
				if (!sender.hasPermission("greylistvote.vote") && !(allowApprovedVote && sender.hasPermission("greylistvote.approved"))) {
 					sender.sendMessage(ChatColor.RED + "You do not have permission to vote!");
 					return true;
 				}
 				OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
 				if (!target.hasPlayedBefore() && !target.isOnline()) {
 					// Player not online
 					sender.sendMessage(args[0] + ChatColor.RED + " not found!");
 					return false;
 				}
 				
 				if (!(sender instanceof Player)) {
 					// Voter is the console
 					this.usersConfig.set(target.getName().toLowerCase() + ".votes", "Server");
 					this.usersConfig.set(target.getName().toLowerCase() + ".griefer", null);
 					sender.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation was set to " + approvalVotes + "!");
 					Player[] onlinePlayers = getServer().getOnlinePlayers();
 					for (Player chatPlayer : onlinePlayers) {
 						if (!chatPlayer.getName().equals(target.getName())) {
 							chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation was set to " + approvalVotes + " by the Server!");
 						}
 						else {
 							target.getPlayer().sendMessage(ChatColor.GOLD + "Your reputation was set to " + approvalVotes + " by the Server!");
 						}
 					}
 					if (target.isOnline() && !target.getPlayer().hasPermission("greylistvote.build") && !target.getPlayer().hasPermission("greylistvote.approved")) {
 						this.setApproved(target.getPlayer());
 					}
 					this.saveUsersConfig();
 					return true;
 				}
 				if (sender.getName().equalsIgnoreCase(target.getName())) {
 					// Player voting for self
 					sender.sendMessage(ChatColor.RED + "You cannot vote for yourself!");
 					return true;
 				}
 				String griefList = this.usersConfig.getString(target.getName().toLowerCase() + ".griefer", null);
 				String voteList = this.usersConfig.getString(target.getName().toLowerCase() + ".votes", null);
 				String[] voteArray = null;
 				String[] griefArray = null;
 				if (voteList != null) {
 					voteArray = voteList.split(",");
 				}
 				else {
 					voteList = "";
 				}
 				if (griefList != null) {
 					griefArray = griefList.split(",");
 				}
 				boolean found = false;
 				if (voteArray != null) {
 					for (String vote : voteArray) {
 						if (vote.equalsIgnoreCase(sender.getName())) {
 							found = true;
 						}
 					}
 				}
 				if (found) {
 					// Voter has already voted for this target player
 					sender.sendMessage(ChatColor.RED + "You have already voted for " + ChatColor.WHITE + target.getName());
 					return true;
 				}
 				if (griefArray != null) {
 					String newGriefList = null;
 					for (String vote : griefArray) {
 						if (!vote.equalsIgnoreCase(sender.getName())) {
 							if (newGriefList != null) {
 								newGriefList += "," + vote;
 							} else {
 								newGriefList = vote;
 							}
 						}
 					}
 					if (newGriefList != null) {
 						newGriefList = newGriefList.replaceFirst(",", "");
 						usersConfig.set(target.getName().toLowerCase() + ".griefer", newGriefList);
 						griefArray = newGriefList.split(",");
 					}
 					else {
 						griefArray = null;
 						usersConfig.set(target.getName().toLowerCase() + ".griefer", null);
 					}
 				}
 				sender.sendMessage(ChatColor.GOLD + "You have increased " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + "'s reputation!");
 				Player[] onlinePlayers = getServer().getOnlinePlayers();
 				// Tell everyone about the reputation change
 				for (Player chatPlayer : onlinePlayers) {
 					if (!chatPlayer.getName().equals(target.getName()) && !chatPlayer.getName().equals(sender.getName())) {
 						chatPlayer.sendMessage(sender.getName() + ChatColor.GOLD + " increased " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + "'s reputation!");
 					}
 					else if (!chatPlayer.getName().equals(sender.getName())) {
 						chatPlayer.sendMessage(sender.getName() + ChatColor.GREEN + " increased your reputation!");
 						chatPlayer.sendMessage(ChatColor.GOLD + "Type " + ChatColor.WHITE + "/votelist" + ChatColor.GOLD + " to check your reputation.");
 					}
 				}
 				if (voteList.equals("")) {
 					voteList = sender.getName();
 				}
 				else {
 					voteList = voteList + "," + sender.getName(); 
 				}
 				this.usersConfig.set(target.getName().toLowerCase() + ".votes", voteList);
 				voteArray = voteList.split(",");
 				int rep = 0;
 				if (voteArray.length != 0) {
 					rep += voteArray.length;
 				}
 				if (griefArray != null) {
 					if (griefArray.length != 0) {
 						rep -= griefArray.length;
 					}
 				}
 				if (target.isOnline() && rep >= approvalVotes && !target.getPlayer().hasPermission("greylistvote.build") && !target.getPlayer().hasPermission("greylistvote.approved")) {
 					// Enough votes received
 					this.setApproved(target.getPlayer());
 				}
 				else if (!target.isOnline() && rep >= approvalVotes) {
 					for (Player chatPlayer : onlinePlayers) {
 						if (!chatPlayer.getName().equals(target.getName())) {
 							chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation has reached " + approvalVotes + "!");
 							chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + " can now build!");
 						}
 					}
 				}
 				this.saveUsersConfig();
 				return true;
 			}
 		}
 		else if (commandLabel.equalsIgnoreCase("griefer") || commandLabel.equalsIgnoreCase("distrust")) {
			if (!sender.hasPermission("greylistvote.griefer") && !(allowApprovedVote && sender.hasPermission("greylistvote.approved"))) {
 				sender.sendMessage(ChatColor.RED + "You do not have permission to vote!");
 				return true;
 			}
 			if (args.length != 1) {
 				// No player specified or too many arguments
 				return false;
 			}
 			else {
 				OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
 				if (!target.hasPlayedBefore()) {
 					// Player not online
 					sender.sendMessage(args[0] + ChatColor.RED + " not found!");
 					return false;
 				}
 				String griefList = this.usersConfig.getString(target.getName().toLowerCase() + ".griefer", null);
 				String voteList = this.usersConfig.getString(target.getName().toLowerCase() + ".votes", null);
 				String[] voteArray = null;
 				String[] griefArray = null;
 				if (voteList != null) {
 					voteArray = voteList.split(",");
 				}
 				if (griefList != null) {
 					griefArray = griefList.split(",");
 				}
 				else {
 					griefList = "";
 				}
 				if (!(sender instanceof Player)) {
 					// Voter is the console
 					this.usersConfig.set(target.getName().toLowerCase() + ".griefer", "Server");
 					this.usersConfig.set(target.getName().toLowerCase() + ".votes", null);
 					sender.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation was set to -1!");
 					Player[] onlinePlayers = getServer().getOnlinePlayers();
 					for (Player chatPlayer : onlinePlayers) {
 						if (!chatPlayer.getName().equals(target.getName())) {
 							chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation was set to -1 by the Server!");
 						}
 						else {
 							target.getPlayer().sendMessage(ChatColor.GOLD + "Your reputation was set to -1 by the Server!");
 						}
 					}
 					if (target.getPlayer().hasPermission("greylistvote.build") && !target.getPlayer().hasPermission("greylistvote.approved")) {
 						this.setGriefer(target.getPlayer());
 					}
 					this.saveUsersConfig();
 					return true;
 				}
 				if (!sender.getName().equals(target.getName())) {
 					// Player voting for self
 					sender.sendMessage(ChatColor.RED + "You cannot vote for yourself!");
 					return true;
 				}
 				boolean found = false;
 				if (griefArray != null) {
 					for (String vote : griefArray) {
 						if (vote.equalsIgnoreCase(sender.getName())) {
 							found = true;
 						}
 					}
 				}
 				if (found) {
 					// Voter has already voted for this target player
 					sender.sendMessage(ChatColor.RED + "You have already voted for " + ChatColor.WHITE + target.getName());
 					return true;
 				}
 				if (voteArray != null) {
 					String newVoteList = null;
 					for (String vote : voteArray) {
 						if (!vote.equalsIgnoreCase(sender.getName())) {
 							if (newVoteList != null) {
 								newVoteList += "," + vote;
 							} else {
 								newVoteList = vote;
 							}
 						}
 					}
 					if (newVoteList != null) {
 						newVoteList = newVoteList.replaceFirst(",", "");
 						usersConfig.set(target.getName().toLowerCase() + ".votes", newVoteList);
 						voteArray = newVoteList.split(",");
 					}
 					else {
 						voteArray = null;
 						usersConfig.set(target.getName().toLowerCase() + ".votes", null);
 					}
 				}
 				sender.sendMessage(ChatColor.GOLD + "You have reduced " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + "'s reputation!");
 				Player[] onlinePlayers = getServer().getOnlinePlayers();
 				for (Player chatPlayer : onlinePlayers) {
 					if (!chatPlayer.getName().equals(target.getName()) && !chatPlayer.getName().equals(sender.getName())) {
 						chatPlayer.sendMessage(sender.getName() + ChatColor.GOLD + " reduced " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + "'s reputation!");
 					}
 					else if (!chatPlayer.getName().equals(sender.getName())) {
 						chatPlayer.sendMessage(sender.getName() + ChatColor.RED + " reduced your reputation!");
 						chatPlayer.sendMessage(ChatColor.GOLD + "Type " + ChatColor.WHITE + "/votelist" + ChatColor.GOLD + " to check your reputation.");
 					}
 				}
 				if (griefList.equals("")) {
 					griefList = sender.getName();
 				}
 				else {
 					griefList = griefList + "," + sender.getName(); 
 				}
 				this.usersConfig.set(target.getName().toLowerCase() + ".griefer", griefList);
 				griefArray = griefList.split(",");
 				int rep = 0;
 				if (voteArray != null) {
 					rep += voteArray.length;
 				}
 				if (griefArray != null) {
 					rep -= griefArray.length;
 				}
 				if (target.isOnline() && rep < approvalVotes && target.getPlayer().hasPermission("greylistvote.build") && !target.getPlayer().hasPermission("greylistvote.approved")) {
 					// Enough votes received
 					this.setGriefer(target.getPlayer());
 				}
 				else if (!target.isOnline() && rep < approvalVotes) {
 					for (Player chatPlayer : onlinePlayers) {
 						if (!chatPlayer.getName().equals(target.getName())) {
 							chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation has dropped below " + approvalVotes + "!");
 						}
 					}
 				}
 				this.saveUsersConfig();
 				return true;
 			}
 		}
 		else if (commandLabel.equalsIgnoreCase("votelist") || commandLabel.equalsIgnoreCase("glvlist") || commandLabel.equalsIgnoreCase("rep")) {
 			if (args == null || args.length == 0) {
 				String voteList = this.usersConfig.getString(sender.getName().toLowerCase() + ".votes", null);
 				String griefList = this.usersConfig.getString(sender.getName().toLowerCase() + ".griefer", null);
 				if (voteList == null && griefList == null) {
 					sender.sendMessage(ChatColor.GOLD + "You have not received any votes.");
 					sender.sendMessage(ChatColor.GOLD + "Current Reputation: " + ChatColor.WHITE + "0");
 					sender.sendMessage(ChatColor.GOLD + "Required Reputation: " + ChatColor.WHITE + approvalVotes);
 				}
 				else {
 					sender.sendMessage(ChatColor.GOLD + "You have received votes from:");
 					int reputation = 0;
 					boolean serverVote = false;
 					String[] voteArray;
 					String[] griefArray;
 					if (voteList != null) {
 						voteArray = voteList.split(",");
 						if (voteArray.length != 0) {
 							String votes = ChatColor.GREEN + "  Approvals: " + ChatColor.GOLD;
 							for (String vote : voteArray) {
 								votes = votes + vote + " ";
 								if (vote.equals("Server")) {
 									serverVote = true;
 								}
 								reputation ++;
 							}
 							if (serverVote) {
 								reputation = approvalVotes;
 							}
 							sender.sendMessage(votes);
 						}
 					}
 					if (griefList != null) {
 						griefArray = griefList.split(",");
 						if (griefArray.length != 0) {
 							String votes = ChatColor.DARK_GRAY + "  Black-Balls: " + ChatColor.GOLD;
 							serverVote = false;
 							for (String vote : griefArray) {
 								votes = votes + vote + " ";
 								if (vote.equals("Server")) {
 									serverVote = true;
 								}
 								reputation--;
 							}
 							if (serverVote) {
 								reputation = -1;
 							}
 							sender.sendMessage(votes);
 						}
 					}
 					String repText;
 					if (reputation >= approvalVotes) {
 						repText = " " + ChatColor.GREEN + reputation;
 					}
 					else {
 						repText = " " + ChatColor.RED + reputation;
 					}
 					sender.sendMessage(ChatColor.GOLD + "Current Reputation:" + repText);
 					sender.sendMessage(ChatColor.GOLD + "Required Reputation: " + ChatColor.WHITE + approvalVotes);
 				}
 				return true;
 			}
 			else {
 				OfflinePlayer checktarget = getServer().getOfflinePlayer(args[0]);
 				String DN = null;
 				String target = null;
 				if (checktarget.isOnline()) {
 					target = checktarget.getPlayer().getName();
 					DN = checktarget.getPlayer().getDisplayName();
 				}
 				else {
 					if (checktarget != null) {
 						target = checktarget.getName();
 						DN = checktarget.getName();
 					}
 				}
 				if (target == null) {
 					// Player not found
 					sender.sendMessage(args[0] + ChatColor.RED + " not found!");
 					return false;
 				}
 				String voteList = this.usersConfig.getString(target.toLowerCase() + ".votes", null);
 				String griefList = this.usersConfig.getString(target.toLowerCase() + ".griefer", null);
 				if (voteList == null && griefList == null) {
 					sender.sendMessage(DN + ChatColor.GOLD + " has not received any votes.");
 					sender.sendMessage(ChatColor.GOLD + "Current Reputation: " + ChatColor.WHITE + "0");
 					sender.sendMessage(ChatColor.GOLD + "Required Reputation: " + ChatColor.WHITE + approvalVotes);
 				}
 				else {
 					sender.sendMessage(DN + ChatColor.GOLD + " has received votes from:");
 					int reputation = 0;
 					boolean serverVote = false;
 					String[] voteArray;
 					String[] griefArray;
 					if (voteList != null) {
 						voteArray = voteList.split(",");
 						if (voteArray.length != 0) {
 							String votes = ChatColor.GREEN + "  Approvals: " + ChatColor.GOLD;
 							for (String vote : voteArray) {
 								votes = votes + vote + " ";
 								if (vote.equals("Server")) {
 									serverVote = true;
 								}
 								reputation ++;
 							}
 							if (serverVote) {
 								reputation = approvalVotes;
 							}
 							sender.sendMessage(votes);
 						}
 					}
 					if (griefList != null) {
 						griefArray = griefList.split(",");
 						if (griefArray.length != 0) {
 							String votes = ChatColor.DARK_GRAY + "  Black-Balls: " + ChatColor.GOLD;
 							serverVote = false;
 							for (String vote : griefArray) {
 								votes = votes + vote + " ";
 								if (vote.equals("Server")) {
 									serverVote = true;
 								}
 								reputation--;
 							}
 							if (serverVote) {
 								reputation = -1;
 							}
 							sender.sendMessage(votes);
 						}
 					}
 					String repText;
 					if (reputation >= approvalVotes) {
 						repText = " " + ChatColor.GREEN + reputation;
 					}
 					else {
 						repText = " " + ChatColor.RED + reputation;
 					}
 					sender.sendMessage(ChatColor.GOLD + "Current Reputation:" + repText);
 					sender.sendMessage(ChatColor.GOLD + "Required Reputation: " + ChatColor.WHITE + approvalVotes);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void setApproved(Player target) {
 		PermissionAttachment attachment = target.addAttachment(this);
 		attachment.setPermission("greylistvote.build", true);
 		Player[] onlinePlayers = getServer().getOnlinePlayers();
 		for (Player chatPlayer : onlinePlayers) {
 			if (!chatPlayer.getName().equals(target.getName())) {
 				chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation has reached " + approvalVotes + "!");
 				chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + " can now build!");
 			}
 			else {
 				chatPlayer.sendMessage(ChatColor.GREEN + "Your reputation has reached " + approvalVotes + "!");
 				chatPlayer.sendMessage(ChatColor.GREEN + "You can now build!");
 			}
 		}
 	}
 	
 	public void setGriefer(Player target) {
 		PermissionAttachment attachment = target.addAttachment(this);
 		attachment.setPermission("greylistvote.build", false);
 		Player[] onlinePlayers = getServer().getOnlinePlayers();
 		for (Player chatPlayer : onlinePlayers) {
 			if (!chatPlayer.getName().equals(target.getName())) {
 				chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + "'s reputation has dropped below " + approvalVotes + "!");
 				chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + " can no longer build!");
 			}
 			else {
 				chatPlayer.sendMessage(ChatColor.RED + "Your reputation has dropped below " + approvalVotes + "!");
 				chatPlayer.sendMessage(ChatColor.RED + "Your build rights have been revoked!");
 			}
 		}
 	}
 	
 	public void loadUsersConfig() {
 		if (this.usersFile == null) {
 			this.usersFile = new File(getDataFolder(),"users.yml");
 		}
 		this.usersConfig = YamlConfiguration.loadConfiguration(this.usersFile);
 	}
 	
 	public FileConfiguration getUsersConfig() {
 		if (this.usersConfig == null) {
 			this.loadUsersConfig();
 		}
 		return this.usersConfig;
 	}
 	
 	public void saveUsersConfig() {
 		if (this.usersConfig == null || this.usersFile == null) {
 			return;
 		}
 		try {
 			this.usersConfig.save(this.usersFile);
 		} catch (IOException ex) {
 			getLogger().log(Level.SEVERE, "Could not save " + this.usersFile, ex );
 		}
 	}
 }
