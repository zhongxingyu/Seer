 package com.bukkit.Top_Cat.MIA;
 
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.ItemStack;
 
 import com.maxmind.geoip.IPLocation;
 import com.maxmind.geoip.LookupService;
 import com.maxmind.geoip.regionName;
 
 /**
  * Handle events for all Player related events
  * @author Thomas Cheyney
  */
 public class MIAPlayerListener extends PlayerListener {
     private final MIA plugin;
 
     HashMap<String, OnlinePlayer> userinfo = new HashMap<String, OnlinePlayer>();
     
     public MIAPlayerListener(MIA instance) {
         plugin = instance;
     }
     
     public boolean cbal(String target, int ammount) {
     		OnlinePlayer inf = userinfo.get(target);
     		int res = inf.getBalance() + ammount;
     		if (inf.cbal(ammount)) {
     			// = String.valueOf(res);
     			//userinfo.put(target, inf);
     			
     			String q = "UPDATE users SET balance = '" + res + "' WHERE name = '" + target + "'";
     			try {
         			PreparedStatement pr = plugin.conn.prepareStatement(q);
 					pr.executeUpdate();
 					return true;
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
     		}
     		return false;
     }
     
     public void loadallusers() {
     	try {
 	    	String q = "SELECT * FROM users";
 	    	PreparedStatement pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery();
 			OnlinePlayer op = null;
 			while (r.next()) {
 				op = new OnlinePlayer(r.getInt("balance"), r.getInt("cloak"), r.getString("name"), r.getString("prefix"), r.getInt("town"));
 				userinfo.put(r.getString("name"), op);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     @Override
     public void onPlayerJoin(PlayerEvent event) {    	
     	String nam = event.getPlayer().getDisplayName();
     	
     	PreparedStatement pr;
 		try {
 			String q = "SELECT * FROM users WHERE name = '" + event.getPlayer().getDisplayName() + "'";
 			pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery();
 			OnlinePlayer op = null;	
 			if (r.next()) {
 		    	nam = r.getString("prefix") + "f " + r.getString("name");
 		    	op = new OnlinePlayer(r.getInt("balance"), r.getInt("cloak"), event.getPlayer().getDisplayName(), r.getString("prefix"), r.getInt("town"));
 			} else {
 				// User doesn't exist! Make a new record
 				String q2 = "INSERT INTO users (name) VALUES('" + event.getPlayer().getDisplayName() + "')";
 				PreparedStatement pr2 = plugin.conn.prepareStatement(q2);
 				pr2.executeUpdate();
 				
 		    	nam = "0[G]f " + event.getPlayer().getDisplayName();
 		    	op = new OnlinePlayer(0, 1, event.getPlayer().getDisplayName(), "0[G]", 0);
 			}
 			userinfo.put(r.getString("name"), op);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		String msg = nam + " joined!";
 		
 		try {
 		    String sep = System.getProperty("file.separator");
 
 		    String dir = System.getProperty("user.dir"); 
 
 		    String dbfile = dir + sep + "GeoIP.dat"; 
 		    LookupService cl = new LookupService(dbfile,LookupService.GEOIP_MEMORY_CACHE);
 
 		    IPLocation l = cl.getLocation(event.getPlayer().getAddress().getHostName());
 		    
 		    msg = nam + "a has joined the server from b" + regionName.regionNameByCode(l.countryCode,l.region) + ", " + l.countryName + " (" + ((int) l.distance(cl.getLocation("81.109.21.62" ))) + " km)";
 
 		    cl.close();
 		}
 		catch (NullPointerException e) {
 			msg = nam + "a has joined the server from a local connection (0 km)";
 		}	
 		catch (IOException e) {
 		    System.out.println("IO Exception");
 		}
 		
 		plugin.mf.sendmsg(plugin.getServer().getOnlinePlayers(), msg);
     }
     
     HashMap<String, String> tprequests = new HashMap<String, String>();
     
     @Override
     public void onPlayerCommand(PlayerChatEvent event) {
     	boolean canc = true;
     	String com = event.getMessage();
 		String coms[] = com.split(" ");
     	if (com.startsWith("/g ")) {
     		plugin.mf.sendmsg(plugin.getServer().getOnlinePlayers(), userinfo.get(event.getPlayer().getDisplayName()).getPrefix() + " " + event.getPlayer().getDisplayName() + ":f " + com.substring(3));
     	} else if (com.startsWith("/tpc") && coms.length > 1 && plugin.getServer().getPlayer(coms[1]) != null) {
			tprequests.put(plugin.getServer().getPlayer(coms[1]).getDisplayName(), event.getPlayer().getDisplayName());
 			plugin.mf.sendmsg(plugin.getServer().getPlayer(coms[1]), "Player " + event.getPlayer().getDisplayName() + " requested to teleport to you!");
     	} else if (com.equalsIgnoreCase("/deny") && tprequests.containsKey(event.getPlayer().getDisplayName())) {
     		tprequests.remove(event.getPlayer().getDisplayName());
     	} else if (com.equalsIgnoreCase("/accept") && tprequests.containsKey(event.getPlayer().getDisplayName())) {
     		System.out.println("Teleport?");
     		plugin.getServer().getPlayer(tprequests.get(event.getPlayer().getDisplayName())).teleportTo(event.getPlayer());
     		tprequests.remove(event.getPlayer().getDisplayName());
     	} else if (com.equals("/getpos")) {
     		Player[] p = new Player[1];
     		p[0] = event.getPlayer();
     		plugin.mf.sendmsg(p, event.getPlayer().getLocation().getBlockX() + ", " + event.getPlayer().getLocation().getBlockY() + ", " + event.getPlayer().getLocation().getBlockZ());
     	} else if (com.startsWith("/pay")) {
     		if (com.split(" ").length != 3) {
     			plugin.mf.sendmsg(event.getPlayer(), "bCorrect usage is: /pay <person> <ammount>");
     		} else {
     			int amm = Integer.parseInt(com.split(" ")[2]);
     			if (userinfo.containsKey(com.split(" ")[1])) {
         			//Take Money
         			cbal(event.getPlayer().getDisplayName(), -amm);
         			//Give Money
     				cbal(com.split(" ")[1], amm);
     			} else {
     				plugin.mf.sendmsg(event.getPlayer(), "bPlayer " + com.split(" ")[1] + " not found!");
     			}
     		}
     	} else if (com.startsWith("/money")) {
     		if (coms.length > 1) {
 	    		if (coms[1].equalsIgnoreCase("top")) {
 	    			int amm = 5;
 	    			if (coms.length > 2) {
 	    				amm = Integer.parseInt(coms[2]);
 	    			}
 		    		//loadallusers();
 		    		ArrayList<OnlinePlayer> ops = new ArrayList<OnlinePlayer>(userinfo.values());
 		    		Collections.sort(ops);
 		    		plugin.mf.sendmsg(event.getPlayer(), "bTop Players List");
 		    		for (int i = 0; i < amm; i++) {
 		    			String col = "b";
 		    				if (i == 0) {
 		    					col = "6";
 		    				}
 		    			plugin.mf.sendmsg(event.getPlayer(), "" + col + "#" + (i + 1) + ": " + ops.get(i).getName() + " (" + ops.get(i).getBalance() + ")");
 		    		}
 	    		} else if (coms[1].equalsIgnoreCase("rank")) {
 		    		//loadallusers();
 		    		ArrayList<OnlinePlayer> ops = new ArrayList<OnlinePlayer>(userinfo.values());
 		    		Collections.sort(ops);
 		    		plugin.mf.sendmsg(event.getPlayer(), "bCurrent rank: " + ops.indexOf(userinfo.get(event.getPlayer().getDisplayName())));
 		    		//[Money] Current rank: 1
 	    		} else {
 	    			if (userinfo.containsKey(coms[1])) {
 	    				plugin.mf.sendmsg(event.getPlayer(), "b[Money] " + coms[1] + "'s Balance: f" + userinfo.get(coms[1]).getBalance() + " bISK");
 	    			}
 	    		}
     		} else {
     			plugin.mf.sendmsg(event.getPlayer(), "b[Money] Balance: f" + userinfo.get(event.getPlayer().getDisplayName()).getBalance() + " bISK");
     		}
     	} else if (com.startsWith("/shop")) {
     		if (coms.length < 2 || coms.length > 4) {
     			plugin.mf.sendmsg(event.getPlayer(), "bCorrect usage is: /shop <sell/buy> [id] [ammount]");
     		} else {
     			boolean opsell = true;
     			int itemid = 0;
     			int itemamm = 0;
     			if (coms[1].equalsIgnoreCase("sell") || coms[1].equalsIgnoreCase("buy")) {
     				if (coms[1].equalsIgnoreCase("buy")) {
     					opsell = false;
     				}
     				if (coms.length > 2) {
     					itemid = Integer.parseInt(coms[2]);
     				} else {
     					itemid = event.getPlayer().getItemInHand().getTypeId();
     				}
     				if (coms.length > 3) {
     					itemamm = Integer.parseInt(coms[3]);
     				} else {
     					itemamm = event.getPlayer().getItemInHand().getAmount();
     				}
     				// Check item is in shop
     				int inzone = plugin.mf.inzone(event.getPlayer());
     				HashMap<Integer, Integer[]> sitems = plugin.mf.shopitems(inzone);
     				if (sitems.containsKey(itemid) && (opsell || sitems.get(itemid)[2] >= itemamm || inzone == 0)) {
 	    				if (opsell) {
 	    					event.getPlayer().getInventory().removeItem(new ItemStack(itemid, itemamm));
 	    					// Give Money
 	    					cbal(event.getPlayer().getDisplayName(), sitems.get(itemid)[1] * itemamm);
 	    					if (inzone > 0)
 	    						plugin.mf.changestock(inzone, itemid, itemamm);
 	    					
 	    					plugin.mf.sendmsg(event.getPlayer(), "bReceived " + (sitems.get(itemid)[1] * itemamm) + " for " + itemamm + " " + new ItemStack(itemid, itemamm).getType().toString());
 	    				} else {
 	    					// Take Money
 	    					cbal(event.getPlayer().getDisplayName(), -(sitems.get(itemid)[0] * itemamm));
 	    					
 	    					event.getPlayer().getInventory().addItem(new ItemStack(itemid, itemamm));
 	    					if (inzone > 0)
 	    						plugin.mf.changestock(inzone, itemid, -itemamm);
 	    					
 	    					plugin.mf.sendmsg(event.getPlayer(), "bBought " + itemamm + " " + new ItemStack(itemid, itemamm).getType().toString() + " for " + (sitems.get(itemid)[0] * itemamm));
 	    				}
     				} else {
     					plugin.mf.sendmsg(event.getPlayer(), "bThis shop does not stock that item, or does not have sufficient stock");
     				}
     			} else {
     				plugin.mf.sendmsg(event.getPlayer(), "bCorrect usage is: /shop <sell/buy> [id] [ammount]");
     			}
     		}
     	} else {
     		canc = false;
     	}
     	if (canc)
     		event.setCancelled(true);
     }
     
     @Override
     public void onPlayerChat(PlayerChatEvent event) {
     	Player[] p = null;
     	String tw = "";
     	String prefix = userinfo.get(event.getPlayer().getDisplayName()).getPrefix();
     	PreparedStatement pr;
 		try {
 			String q = "SELECT prefix, name FROM users WHERE town > 0 and town = (SELECT town FROM users WHERE name = '" + event.getPlayer().getDisplayName() + "')";
 			pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery();
 			if (r.last()) {
 				p = new Player[r.getRow()];
 				r.beforeFirst();
 				int i = 0;
 				while (r.next()) {
 					Player t = plugin.getServer().getPlayer(r.getString("name"));
 					if (t != null) {
 						p[i++] = t;
 					}
 				}
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (p == null) {
 			p = plugin.getServer().getOnlinePlayers();
 		} else {
 			tw = "9(TOWN) ";
 		}
 		
     	plugin.mf.sendmsg(p, tw + prefix + " " + event.getPlayer().getDisplayName() + ":f " + event.getMessage());
     	event.setCancelled(true);
     }
     
     @Override
     public void onPlayerQuit(PlayerEvent event) {
     	Block[] blox = new Block[plugin.blockListener.opengate.size()];
     	int j = 0;
     	for (Integer[] i : plugin.blockListener.opengate.values()) {
     		if (event.getPlayer().getEntityId() == i[1]) {
     			World w = event.getPlayer().getWorld();
     			Integer[] l = plugin.mf.gateSign(i[0], event.getPlayer().getWorld());
     			Integer[] l2 = plugin.mf.gateSign(i[2], event.getPlayer().getWorld());
     			plugin.mf.closeportal(w.getBlockAt(l[0], l[1], l[2]), w);
     			plugin.mf.closeportal(w.getBlockAt(l2[0], l2[1], l2[2]), w);
     			blox[j++] = w.getBlockAt(l[0], l[1], l[2]);
     			blox[j++] = w.getBlockAt(l2[0], l2[1], l2[2]);
     		}
     	}
     	for (Block b : blox) {
     		plugin.blockListener.opengate.remove(b);
     	}
     }
     
     int lastup = 0;
     String[] sign_rss = {"Top_Cat wins!", "Lol at you...", "Meow", "Balls to you!", "Bigger than us"};
     
     @Override
     public void onPlayerMove(PlayerMoveEvent event) {
     	Date time = new Date();
     	World w = event.getPlayer().getWorld();
     	if ((time.getTime() / 1000) - 5 > lastup) {
     		lastup = (int) (time.getTime() / 1000);
     		Sign sign = ((Sign) w.getBlockAt(409, 4, -353).getState());
     		SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
    		sign.setLine(1, sdf.	format(time) + " GMT"); //sign_rss[(int) Math.round(Math.random() * 4)]
     		time.setTime(time.getTime() - 18000000);
     		sign.setLine(2, sdf.format(time) + " EST");
     		time.setTime(time.getTime() - 10800000);
     		sign.setLine(3, sdf.format(time) + " PST");
     		sign.update();
     	}
     	
     	if (Math.sqrt(Math.pow(event.getTo().getBlockX() - 466, 2) + Math.pow(event.getTo().getBlockZ() + 303, 2)) > 1000) {
     		if (Math.sqrt(Math.pow(event.getFrom().getBlockX() - 466, 2) + Math.pow(event.getFrom().getBlockZ() + 303, 2)) < 1000) {
     			event.setCancelled(true);
     		} else {
     			double vX = event.getTo().getBlockX() - 466;
     			double vY = event.getTo().getBlockZ() + 303;
     			double magV = Math.sqrt(vX*vX + vY*vY);
     			int aX = (int) (466 + vX / magV * 1000);
     			int aY = (int) (-303 + vY / magV * 1000);
     			
     			Location dest = new Location(event.getPlayer().getWorld(), aX, event.getPlayer().getWorld().getHighestBlockYAt(aX, aY), aY);
     			event.getPlayer().teleportTo(dest);
     			event.setTo(dest);
     		}
     	}
     	
     	// Put lapis in! and do bedrock
     	Location pl = event.getPlayer().getLocation();
     	for (int i = -8; i < 8; i++) {
     		for (int k = -8; k < 8; k++) {
 	    		for (int j = 0; j < 5; j++) {
 	    			Block b = w.getBlockAt(pl.getBlockX() + i, j, pl.getBlockZ() + k);
 	    			if (j == 0 && b.getType() != Material.BEDROCK) {
 	    				b.setType(Material.BEDROCK);
 	    			} else if (j > 0 && b.getType() == Material.BEDROCK) {
 	    				b.setType(Material.STONE);
 	    			}
 	    		}
 	    		if ((Math.random() * 50000000) > 49999999) {
 	    			System.out.println("Deposit lapis :)");
 	    		}
     		}
     	}
     	
     	if (plugin.mf.heal(event.getPlayer())) {
     		int newh = event.getPlayer().getHealth() + 1;
     		if (newh > 20) {
     			newh = 20;
     		}
     		event.getPlayer().setHealth(newh);
     	}
     	
     	int town1 = plugin.mf.intown(event.getFrom());
     	int town2 = plugin.mf.intown(event.getTo());
     	if (town1 != town2) {
     		if (town1 == 0) {
     			plugin.mf.sendmsg(event.getPlayer(), "6Welcome to " + plugin.mf.towninfo(town2).get("name"));
     		} else {
     			plugin.mf.sendmsg(event.getPlayer(), "6Now leaving " + plugin.mf.towninfo(town1).get("name"));
     		}
     	}
     	
     	/*if (event.getTo().getBlockY() < 1) {  Depreciated. Need loadChunk!
     		Location dest = event.getTo();
     		dest.setY(2);
 			event.getPlayer().teleportTo(dest);
 			event.setTo(dest);
     	}*/
     	for (Block i : plugin.blockListener.opengate.keySet()) {
     		if (event.getPlayer().getEntityId() == plugin.blockListener.opengate.get(i)[1]) {
 	    		Integer rot = Integer.parseInt(plugin.mf.gateData(i, w).get("rot"));
 	    		
 	    		int xo = 0;
 	    		int zo = 0;
 	    		int ax = 1;
 	    		int ay = 1;
 	    		switch (rot) {
 	    		case 0: //North (-X)
 	            	xo = -1;
 	            	zo = -1;
 	            	ay = 2;
 	            	break;
 	            case 2: //South (+X)
 	            	xo = 1;
 	            	zo = 1;
 	            	ay = 2;
 	            	break;
 	            case 1: //East (-Z)
 	            	xo = 1;
 	            	zo = -1;
 	            	ax = 2;
 	            	break;
 	            case 3: //West (+Z)
 	            	xo = -1;
 	            	zo = 1;
 	            	ax = 2;
 	            	break;
 	    		}	
 	    		
 	    		
 	    		Integer[] b = plugin.blockListener.opengate.get(i);
 				Integer tnow = (int) Math.round(((double) time.getTime() / 1000));
 				Integer[] l = plugin.mf.gateSign(b[0], w);
 				if (b[3] + 90 < tnow) {
 	    			plugin.blockListener.opengate.remove(i);
 	    			plugin.blockListener.opengate.remove(w.getBlockAt(l[0], l[1], l[2]));
 	    			plugin.mf.closeportal(w.getBlockAt(l[0], l[1], l[2]), w);
 	    			plugin.mf.closeportal(i, w);
 	    		}
 	    		 
 	    		if (event.getPlayer().getEntityId() == b[1] && ((event.getTo().getBlockX() == (i.getX() + (xo * ax)) && event.getTo().getBlockZ() == (i.getZ() + (zo * ay))) || (event.getTo().getBlockX() == (i.getX() + xo) && event.getTo().getBlockZ() == (i.getZ() + zo))) && event.getTo().getBlockY() == (i.getY() - 1)) {
 	    			Integer rot2 = Integer.parseInt(plugin.mf.gateData(b[0]).get("rot"));
 	        		
 	    			int xo2 = 2;
 	        		int zo2 = 0;
 	        		int r = 0;
 	        		if (rot2 == 0) { //North (-X)
 	        			r = 270;
 	        			zo2 = -1;
 	        			xo2 = 0;
 	        		} else if (rot2 == 2) { //South (+X)
 	        			r = 90;
 	        			zo2 = 2;
 	        			xo2 = 1;
 	        		} else if (rot2 == 3) { //West (+Z)
 	        			r = 180;
 	        			xo2 = -1;
 	        			zo2 = 1;
 	        		}
 	        		
 	        		
 	    			Location dest = new Location(w, l[0] + xo2, l[1] - 1, l[2] + zo2, r, 0);
 	    			event.getPlayer().teleportTo(dest);
 	    			event.setTo(dest);
 	    			
 	    			plugin.blockListener.opengate.remove(i);
 	    			plugin.blockListener.opengate.remove(w.getBlockAt(l[0], l[1], l[2]));
 	    			plugin.mf.closeportal(w.getBlockAt(l[0], l[1], l[2]), w);
 	    			plugin.mf.closeportal(i, w);
 	    			
 	    			break;
 	    		}
 	    	}
     	}
     }
 }
 
