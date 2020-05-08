 package net.roguedraco.infobutton;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.downwithdestruction.core.lang.Lang;
 import net.roguedraco.infobutton.player.RDPlayer;
 import net.roguedraco.infobutton.player.RDPlayers;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 public class InfoButton {
 
 	private Location location;
 
 	private boolean enabled = false;
 	private double price = 0.00;
 	private String permission = "infobutton.use";
 
 	private Set<ButtonAction> actions = new HashSet<ButtonAction>();
 
 	public InfoButton(Block block) {
 		this.location = block.getLocation();
 		this.enabled = InfoButtonPlugin.getPlugin().getConfig()
 				.getBoolean("buttonDefaults.enabled");
 		this.price = InfoButtonPlugin.getPlugin().getConfig()
 				.getDouble("buttonDefaults.price");
 		this.permission = InfoButtonPlugin.getPlugin().getConfig()
 				.getString("buttonDefaults.permission");
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public String getPermission() {
 		return permission;
 	}
 
 	public Set<ButtonAction> getActions() {
 		return actions;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
 	}
 
 	public void setPermission(String permission) {
 		this.permission = permission;
 	}
 
 	public void execute(Player player) {
 		if (enabled == false) {
 			player.sendMessage(InfoButtonPlugin.getLang().get("exceptions.notEnabled"));
 			return;
 		}
 
 		if (permission != null && permission != "") {
 			if (!InfoButtonPlugin.permission.playerHas(player, permission)) {
 				player.sendMessage(InfoButtonPlugin.getLang().get("exceptions.noPermission"));
 				return;
 			}
 		}
 
 		if (price > 0) {
 			if (InfoButtonPlugin.economy.getBalance(player.getName()) >= price) {
 				RDPlayer rdp = RDPlayers.getPlayer(player.getName());
 
 				int x, y, z = 0;
 				x = this.location.getBlockX();
 				y = this.location.getBlockY();
 				z = this.location.getBlockZ();
 				long time = (new Date().getTime()) * 1000;
 				long confirmed = rdp.getLong("confirmed." + x + "-" + y + "-"
 						+ z);
 
 				if (confirmed <= time) {
 					rdp.set("confirmed." + x + "-" + y + "-" + z, null);
 					InfoButtonPlugin.economy.withdrawPlayer(player.getName(),
 							price);
 					player.sendMessage(InfoButtonPlugin.getLang().get("other.paidForButton"));
 				} else {
 					rdp.set("confirmed." + x + "-" + y + "-" + z,
 							time
 									+ InfoButtonPlugin.getPlugin().getConfig()
 											.getInt("buttonTimeout"));
 					player.sendMessage(InfoButtonPlugin.getLang().get("other.confirmTransaction"));
 				}
 			}
 		}
 
 		for (ButtonAction action : actions) {
 			ActionType type = action.getType();
 			if (type == ActionType.CONSOLE_COMMAND) {
 				Bukkit.getServer().dispatchCommand(
 						Bukkit.getServer().getConsoleSender(),
 						action.getValue().replaceAll("%player%",
 								player.getName()));
 			}
 			if (type == ActionType.PLAYER_COMMAND) {
 				player.chat("/"
 						+ action.getValue().replaceAll("%player%",
 								player.getName()));
 			}
 			if (type == ActionType.FILE_READ) {
 				try {
 					FileInputStream fstream = new FileInputStream(
 							InfoButtonPlugin.getPlugin().getDataFolder()
 									+ "/files/" + action.getValue() + ".txt");
 					DataInputStream in = new DataInputStream(fstream);
 					BufferedReader br = new BufferedReader(
 							new InputStreamReader(in));
 					String strLine;
 					while ((strLine = br.readLine()) != null) {
 						InfoButtonPlugin.getLang();
 						player.sendMessage(Lang.parseColours(strLine)
 								.replaceAll("%player%", player.getName()));
 					}
 					in.close();
 				} catch (Exception e) {
 					player.sendMessage(InfoButtonPlugin.getLang().get("exceptions.invalidFile"));
 				}
 			}
 		}
 	}
 
 	public void addAction(ActionType type, String value) {
 		ButtonAction action = new ButtonAction(type, value);
 		this.addAction(action);
 	}
 
 	public void addAction(ButtonAction action) {
 		actions.add(action);
 	}
 
 }
