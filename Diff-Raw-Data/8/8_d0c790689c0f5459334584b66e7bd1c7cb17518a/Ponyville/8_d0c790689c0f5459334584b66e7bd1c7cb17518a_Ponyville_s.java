 package me.corriekay.pppopp3.ponyville;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 
 import me.corriekay.pppopp3.Mane;
 import me.corriekay.pppopp3.events.JoinEvent;
 import me.corriekay.pppopp3.events.QuitEvent;
 import me.corriekay.pppopp3.modules.Equestria;
 import me.corriekay.pppopp3.modules.InvSee;
 import me.corriekay.pppopp3.utils.PSCmdExe;
 import me.corriekay.pppopp3.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.PlayerInventory;
 
 public final class Ponyville extends PSCmdExe{
 
 	private HashMap<String,Pony> ponies = new HashMap<String,Pony>();
 	private static Ponyville pv;
 
 	public Ponyville(){
 		super("Ponyville");
 		pv = this;
 		File dir = new File(Mane.getInstance().getDataFolder() + File.separator + "Players");
 		if(!dir.isDirectory()) {
 			dir.mkdirs();
 		}
 		init();
 	}
 
 	private void init(){
 		for(Player p : Bukkit.getOnlinePlayers()) {
 			Ponyville.addPony(p);
 		}
 	}
 
 	public static Pony getPony(Player p){
 		return getPony(p.getName());
 	}
 
 	public static Pony getPony(String s){
 		return pv.ponies.get(s);
 	}
 
 	public static void removePony(Player p){
 		removePony(p.getName());
 	}
 
 	public static void removePony(Pony p){
 		removePony(p.getName());
 	}
 
 	public static void removePony(String s){
 		Pony p = getPony(s);
 		p.save();
 		pv.ponies.remove(s);
 	}
 
 	public static Pony addPony(Player p){
 		Pony po = Pony.moveToPonyville(p);
 		pv.ponies.put(p.getName(), po);
 		return po;
 	}
 
 	public static Pony getOfflinePony(Player p){
 		return getOfflinePony(p.getName());
 	}
 
 	public static Pony getOfflinePony(String p){
 		Pony pony = getPony(p);
 		if(pony == null) {
 			try {
 				return new Pony(p);
 			} catch(FileNotFoundException e) {
 				return null;
 			}
 		} else {
 			return pony;
 		}
 	}
 
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event){
 		Player pl = event.getPlayer();
 		Pony p = addPony(pl);
 		JoinEvent je = new JoinEvent(pl, p, true);
 		{
 			//load info on player join
 			PlayerInventory i = pl.getInventory();
 			PlayerInventory i2 = p.getInventory(Equestria.get().getParentWorld(pl.getWorld()).getName());
 			i.setContents(i2.getContents());
 			i.setArmorContents(i2.getArmorContents());
 			p.setLastLogon(Utils.getSystemTime(System.currentTimeMillis()));
 
 		}
 		p.save();
 		pl.setDisplayName(p.getNickname());
 		Pony.getWorldStats(pl, pl.getWorld().getName());
 		Bukkit.getPluginManager().callEvent(je);
 		event.setJoinMessage(je.getMsg());
 		pl.sendMessage(ChatColor.DARK_RED + "ATTENTION: This server is unstable! We are in the process of testing new code for the server. Please keep calm, and report any bugs you find at this link: http://mlpf.im/PPPoPP3 Please bear with us as there will be random server reboots, restarts, and even rollbacks. I appreciate your cooperation!");
 	}
 
 	@EventHandler
 	public void onQuit(PlayerQuitEvent event){
 		QuitEvent qe = new QuitEvent(event.getPlayer(), getPony(event.getPlayer()), true);
 		Bukkit.getPluginManager().callEvent(qe);
 		event.setQuitMessage(qe.getMsg());
 		Player pl = event.getPlayer();
 		Pony p = getPony(pl);
 		{
 			//Saved info on quit.
 			p.setLastLogout(Utils.getSystemTime(System.currentTimeMillis()));
 			for(World w : Bukkit.getWorlds()) {
 				if(p.getRemoteChest(w) != null) {
 					p.saveRemoteChest(w);
 				}
 			}
 			Pony.setWorldStats(pl, pl.getWorld().getName());
 			if(!InvSee.get().isInvsee(pl.getName())) p.setInventory(pl.getInventory(), Equestria.get().getParentWorld(pl.getWorld()).getName());
 		}
 		p.save();
 		removePony(event.getPlayer());
 	}
 }
