 package me.corriekay.pppopp3.modules;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.corriekay.pppopp3.Mane;
 import me.corriekay.pppopp3.events.JoinEvent;
 import me.corriekay.pppopp3.events.QuitEvent;
 import me.corriekay.pppopp3.ponyville.Pony;
 import me.corriekay.pppopp3.ponyville.Ponyville;
 import me.corriekay.pppopp3.rpa.RemotePonyAdmin;
 import me.corriekay.pppopp3.utils.PSCmdExe;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 
 import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
 import pgDev.bukkit.DisguiseCraft.api.PlayerDisguiseEvent;
 import pgDev.bukkit.DisguiseCraft.api.PlayerUndisguiseEvent;
 
 public class InvisibilityHandler extends PSCmdExe{
 
 	private final ArrayList<String> invisiblePlayers = new ArrayList<String>();
 	private final ArrayList<String> noPickup = new ArrayList<String>();
 	public static InvisibilityHandler ih;
 
	public InvisibilityHandler(){
 		super("InventoryHandler", "hide", "nopickup");
 		ih = this;
 		for(Player player : Bukkit.getOnlinePlayers()) {
 			calculateInvisibility(player, false, DisguiseCraft.getAPI().isDisguised(player), false);
 		}
 	}
 
 	@Override
 	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
 		Player player;
 		if(!(sender instanceof Player)) {
 			sendMessage(sender, notPlayer);
 			return true;
 		}
 		player = (Player)sender;
 		if(cmd.getName().equals("hide")) {
 			if(label.equals("fakehide")) {
 				toggleInvisibility(player.getName(), false, true, DisguiseCraft.getAPI().isDisguised(player));
 				return true;
 			} else {
 				toggleInvisibility(player.getName(), true, true, DisguiseCraft.getAPI().isDisguised(player));
 				return true;
 			}
 		} else if(cmd.getName().equals("nopickup")) {
 			togglePickup(player);
 			return true;
 		}
 		return true;
 	}
 
 	/**
 	 * Pickup methods
 	 */
 	public boolean isPickingUp(String player){
 		if(noPickup.contains(player)) {
 			return false;
 		} else return true;
 	}
 
 	public void togglePickup(Player player){
 		if(isPickingUp(player.getName())) {
 			pickupOff(player);
 		} else pickupOn(player);
 	}
 
 	private void pickupOff(Player player){
 		noPickup.add(player.getName());
 		sendMessage(player, "Disabling pickup!");
 	}
 
 	private void pickupOn(Player player){
 		noPickup.remove(player.getName());
 		sendMessage(player, "Enabling pickup!");
 	}
 
 	public boolean isHidden(String player){
 		if(invisiblePlayers.contains(player)) {
 			return true;
 		} else return false;
 	}
 
 	public void toggleInvisibility(String name, boolean silent, boolean notify, boolean disguised){
 		if(isHidden(name)) {
 			turnOff(name, silent, notify, disguised, false);
 		} else turnOn(name, silent, notify, disguised);
 	}
 
 	//turns on invisibility for the player
 	private void turnOn(String pName, boolean silent, boolean notify, boolean disguised){
 		Player player = Bukkit.getPlayer(pName);
 		invisiblePlayers.add(pName);
 		if(notify) {
 			sendMessage(player, "Shhh!~ You're hidden now!");
 		}
 		if(!isPickingUp(pName)) {
 			pickupOff(Bukkit.getServer().getPlayer(pName));
 		}
 		for(Player player2 : Bukkit.getServer().getOnlinePlayers()) {
 			if((!player2.equals(player) && !player2.hasPermission("pppopp3.seehidden") || disguised)) {
 				player2.hidePlayer(player);
 			}
 		}
 		QuitEvent qe = new QuitEvent(player, Ponyville.getPony(player), false);
 		qe.setCancelled(silent);
 		Bukkit.getPluginManager().callEvent(qe);
 		if(!qe.isCancelled()) {
 			for(Player player2 : Bukkit.getOnlinePlayers()) {
 				player2.sendMessage(qe.getMsg());
 			}
 		}
 		List<Entity> nearby = player.getNearbyEntities(100, 100, 100);
 		for(Entity entity : nearby) {
 			if(entity instanceof Creature) {
 				Creature creature = (Creature)entity;
 				Entity entity2 = creature.getTarget();
 				if(entity2 instanceof Player) {
 					Player player2 = (Player)entity2;
 					if(player.equals(player2)) {
 						creature.setTarget(null);
 					}
 				}
 			}
 		}
 		Pony pony = Ponyville.getPony(player);
 		pony.setInvisible(true);
 		pony.save();
 	}
 
 	private void turnOff(String pName, boolean silent, boolean notify, boolean disguised, boolean isDisguising){
 		Player player = Bukkit.getPlayer(pName);
 		if(disguised && !isDisguising) {
 			sendMessage(player, "Please dont unhide while disguised!");
 			return;
 		}
 		if(isDisguising) {
 			for(Player player2 : Bukkit.getOnlinePlayers()) {
 				player2.hidePlayer(player);
 			}
 			return;
 		}
 		invisiblePlayers.remove(pName);
 		noPickup.remove(pName);
 		if(player.hasPermission("pppopp3.hide")) {
 			if(notify) {
 				sendMessage(player, "You're now visible!");
 			}
 		}
 		for(Player player2 : Bukkit.getOnlinePlayers()) {
 			player2.showPlayer(player);
 		}
 		JoinEvent je = new JoinEvent(player, Ponyville.getPony(player), false);
 		je.setCancelled(silent);
 		Bukkit.getPluginManager().callEvent(je);
 		if(!je.isCancelled()) {
 			for(Player player2 : Bukkit.getOnlinePlayers()) {
 				player2.sendMessage(je.getMsg());
 			}
 		}
 		Pony pony = Ponyville.getPony(player);
 		pony.setInvisible(false);
 		pony.save();
 	}
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onJoin(JoinEvent event){
 		if(event.isJoining()) {
 			Player player = event.getPlayer();
 			Pony pony = Ponyville.getPony(player);
 			if(pony.isInvisible()) {
 				turnOn(player.getName(), true, true, DisguiseCraft.getAPI().isDisguised(event.getPlayer()));
 				event.setCancelled(true);
 				Bukkit.broadcast(ChatColor.DARK_GRAY + player.getDisplayName() + ChatColor.DARK_GRAY + " has logged in silently!", "pppopp3.invisible.seeinvis");
 				RemotePonyAdmin.rpa.messageCorrie(ChatColor.stripColor(player.getDisplayName() + ChatColor.DARK_GRAY + " has logged in silently!"));
 			} else {
 				turnOff(player.getName(), true, true, DisguiseCraft.getAPI().isDisguised(event.getPlayer()), false);
 			}
 			for(Player player2 : Bukkit.getOnlinePlayers()) {
 				if(!invisiblePlayers.contains(player2.getName())) {
 					player.showPlayer(player2);
 				} else if(!player.hasPermission("pppopp3.seehidden")) {
 					player.hidePlayer(player2);
 				}
 			}
 		}
 	}
 
 	@EventHandler
	public void onTarget(EntityTargetLivingEntityEvent event){
 		if(event.getTarget() instanceof Player) {
 			if(isHidden(((Player)event.getTarget()).getName())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onQuit(QuitEvent event){
 		Player player = event.getPlayer();
 		if(invisiblePlayers.contains(player.getName()) && event.isQuitting()) {
 			event.setCancelled(true);
 			invisiblePlayers.remove(player.getName());
 			noPickup.remove(player.getName());
 		}
 	}
 
 	@EventHandler
 	public void onPickup(PlayerPickupItemEvent event){
 		if(noPickup.contains(event.getPlayer().getName())) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void disguise(final PlayerDisguiseEvent event){
 		Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
 			@Override
 			public void run(){
 				calculateInvisibility(event.getPlayer(), false, true, true);
 			}
 		}, 1);
 	}
 
 	@EventHandler
 	public void disguise(final PlayerUndisguiseEvent event){
 		Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
 			@Override
 			public void run(){
 				calculateInvisibility(event.getPlayer(), false, false, false);
 			}
 		}, 1);
 	}
 
 	private void calculateInvisibility(Player player, boolean notify, boolean disguised, boolean isDisguising){
 		Pony pony = Ponyville.getPony(player);
 		if(pony.isInvisible()) {
 			turnOn(player.getName(), true, notify, disguised);
 		} else {
 			turnOff(player.getName(), true, notify, disguised, isDisguising);
 		}
 	}
 }
