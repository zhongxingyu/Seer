 package com.minecraftdimensions.bungeesuite;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.WordUtils;
 
 import com.minecraftdimensions.bungeesuite.config.Config;
 import com.minecraftdimensions.bungeesuite.database.SQL;
 import com.minecraftdimensions.bungeesuite.listener.PlayerBanCheckDelay;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.connection.Server;
 
 public class Utilities {
 
 	BungeeSuite plugin;
 	SQL sql;
 	Connection connection;
 
 	public Utilities(BungeeSuite bungeeSuite) {
 		plugin = bungeeSuite;
 		sql = plugin.sql;
 	}
 
 	public void createTable(String name, String query) throws SQLException {
 		boolean tableExists = false;
 		tableExists = sql.doesTableExist(name);
 		if (!tableExists) {
 			sql.standardQuery(query);
 		}
 	}
 
 	public void addColumns(String table, String query) {
 		boolean tableExists = false;
 		tableExists = sql.doesTableExist(table);
 		if (tableExists) {
 			try {
 				sql.standardQuery(query);
 			} catch (Exception e) {
 
 			}
 		}
 
 	}
 
 	public void insertData(String table, String columns, String data)
 			throws SQLException {
 		sql.standardQuery("INSERT INTO " + table + " (" + columns + ") VALUE ("
 				+ data + ");");
 	}
 
 	public void addServer(String server) throws SQLException {
 		if (!serverExists(server)) {
 			
 			sql.standardQuery("INSERT INTO BungeeServers (servername) VALUE ('"
 					+ server + "');");
 			
 		}
 	}
 
 	public boolean serverExists(String name) {
 		
 		boolean check = sql
 				.existanceQuery("SELECT servername FROM BungeeServers WHERE servername = '"
 						+ name + "'");
 		
 		return check;
 	}
 
 	public void TeleportToPlayer(String player, String target) {
 		ProxiedPlayer targetP = getClosestPlayer(target);
 		ProxiedPlayer playerP = getClosestPlayer(player);
 
 		if (targetP == null) {
 			this.sendMessage(player, "PLAYER_NOT_ONLINE");
 			return;
 		}
 		if (playerP == null) {
 			this.sendMessage(target, "PLAYER_NOT_ONLINE");
 			return;
 		}
 		ByteArrayOutputStream bgetloc = new ByteArrayOutputStream();
 		DataOutputStream outgetloc = new DataOutputStream(bgetloc);
 		try {
 			outgetloc.writeUTF("StorePlayersBackLocation");
 			outgetloc.writeUTF(playerP.getName());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(
 						plugin,
 						new SendPluginMessage("BungeeSuiteTp", playerP
 								.getServer().getInfo(), bgetloc));
 
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TeleportToPlayer");
 			out.writeUTF(player);
 			out.writeUTF(targetP.getName());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		if (targetP.getServer().getInfo().getName()
 				.equals(playerP.getServer().getInfo().getName())) {
 			plugin.getProxy()
 					.getScheduler()
 					.schedule(
 							plugin,
 							new SendPluginMessage("BungeeSuiteTp", targetP
 									.getServer().getInfo(), b),
 							plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 			return;
 		} else {
 			playerP.connect(targetP.getServer().getInfo());
 			plugin.getProxy()
 					.getScheduler()
 					.schedule(
 							plugin,
 							new SendPluginMessage("BungeeSuiteTp", targetP
 									.getServer().getInfo(), b),
 							plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 			return;
 		}
 	}
 
 	public ProxiedPlayer getClosestPlayer(String player) {
 		if (plugin.proxy.getPlayer(player) != null) {
 			return plugin.proxy.getPlayer(player);
 		}
 		for (ProxiedPlayer data : plugin.proxy.getPlayers()) {
 			if (data.getName().toLowerCase().contains(player.toLowerCase())) {
 				return data;
 			}
 		}
 		return null;
 	}
 
 	public boolean playerExists(String name) {
 		
 		boolean check = false;
 		check = sql
 				.existanceQuery("SELECT * FROM BungeePlayers WHERE playername = '"
 						+ name + "'");
 		
 		return check;
 	}
 
 	public void sendQuery(String query) throws SQLException {
 		
 		sql.standardQuery(query);
 		
 	}
 
 	public void createPlayer(String name, String ip) throws SQLException {
 		
 		sql.standardQuery("INSERT INTO BungeePlayers (playername, lastonline, ipaddress) VALUES ('"
 				+ name + "',CURRENT_DATE(), '" + ip + "')");
 		
 		if (plugin.newPlayerBroadcast) {
 			String msg = plugin.newPlayerBroadcastMessage;
 			msg = msg.replace("{player}", name);
 			msg = this.colorize(msg);
 			for (ProxiedPlayer data : plugin.getProxy().getPlayers()) {
 				data.sendMessage(msg);
 			}
 		}
 	}
 
 	public void updatePlayer(String name, String ip) throws SQLException {
 		
 		sql.standardQuery("UPDATE BungeePlayers SET lastonline=CURRENT_DATE(), ipaddress='"
 				+ ip + "' WHERE playername ='" + name + "'");
 		
 	}
 
 	public Date getPlayerDate(String name) {
 		
 		Date date = null;
 		ResultSet results = sql
 				.sqlQuery("SELECT lastonline FROM BungeePlayers Where playername='"
 						+ name + "'");
 		try {
 			while (results.next()) {
 				date = results.getDate("lastonline");
 			}
 			results.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return date;
 	}
 
 	public String getPlayerIP(String name) {
 		
 		String ip = null;
 		ResultSet results = sql
 				.sqlQuery("SELECT ipaddress FROM BungeePlayers WHERE playername='"
 						+ name + "'");
 		try {
 			while (results.next()) {
 				ip = results.getString("ipaddress");
 			}
 			results.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return ip;
 	}
 
 	public boolean playerOnline(String name) {
 		return plugin.getProxy().getPlayer(name) != null;
 	}
 
 	public ProxiedPlayer getPlayer(String name) {
 		return ProxyServer.getInstance().getPlayer(name);
 	}
 
 	public Server getPlayersServer(String name) {
 		Server s = ProxyServer.getInstance().getPlayer(name).getServer();
 		if (s == null) {
 			plugin.proxy
 					.getConsole()
 					.sendMessage(
 							ChatColor.DARK_RED
 									+ "Player connected to server without connecting to bungeecord expect errors");
 		}
 		return ProxyServer.getInstance().getPlayer(name).getServer();
 	}
 
 	public ServerInfo getServer(String name) {
 		return ProxyServer.getInstance().getServerInfo(name);
 	}
 
 	
 
 	public void TpAll(String player, String targetPlayer) {
 		ProxiedPlayer target = this.getClosestPlayer(targetPlayer);
 		if (target == null) {
 			sendMessage(player, "PLAYER_NOT_ONLINE");
 			return;
 		}
 		for (ProxiedPlayer data : plugin.getProxy().getPlayers()) {
 			this.TeleportToPlayer(data.getName(), target.getName());
 		}
 
 	}
 
 	public boolean checkTeleportPending(String player) {
 		return plugin.pendingTeleportsTPA.containsKey(player)
 				|| plugin.pendingTeleportsTPA.containsValue(player)
 				|| plugin.pendingTeleportsTPAHere.containsKey(player)
 				|| plugin.pendingTeleportsTPAHere.containsValue(player);
 	}
 
 	public void tpaRequest(String player, String targetPlayer) {
 		ProxiedPlayer target = this.getClosestPlayer(targetPlayer);
 		if (target == null) {
 			this.sendMessage(player, "PLAYER_NOT_ONLINE");
 			return;
 		}
 		if(plugin.denyTeleport.contains(target.getName())){
 			sendMessage(player, "TELEPORT_UNABLE");
 			return;
 		}
 		if (checkTeleportPending(player)) {
 			sendMessage(player, "PLAYER_TELEPORT_PENDING");
 			return;
 		}
 		if (checkTeleportPending(target.getName())) {
 			sendMessage(player, "PLAYER_TELEPORT_PENDING_OTHER");
 			return;
 		}
 		plugin.pendingTeleportsTPA.put(target.getName(), player);
 		sendMessage(player, "TELEPORT_REQUEST_SENT");
 		String msg = plugin.getMessage("PLAYER_REQUESTS_TO_TELEPORT_TO_YOU");
 		msg = msg.replace("{player}", player);
 		target.sendMessage(msg);
 		plugin.proxy.getScheduler().schedule(plugin,
 				new PendingTeleportTpa(plugin, target.getName()), 10,
 				TimeUnit.SECONDS);
 	}
 
 	public void tpaHereRequest(String player, String targetPlayer) {
 		ProxiedPlayer target = this.getClosestPlayer(targetPlayer);
 		if (target == null) {
 			this.sendMessage(player, "PLAYER_NOT_ONLINE");
 			return;
 		}
 		if(plugin.denyTeleport.contains(target.getName())){
 			sendMessage(player, "TELEPORT_UNABLE");
 			return;
 		}
 		if (checkTeleportPending(player)) {
 			sendMessage(player, "PLAYER_TELEPORT_PENDING");
 			return;
 		}
 		if (checkTeleportPending(target.getName())) {
 			sendMessage(player, "PLAYER_TELEPORT_PENDING_OTHER");
 			return;
 		}
 		plugin.pendingTeleportsTPAHere.put(target.getName(), player);
 		sendMessage(player, "TELEPORT_REQUEST_SENT");
 		String msg = plugin.getMessage("PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM");
 		msg = msg.replace("{player}", player);
 		target.sendMessage(msg);
 		plugin.proxy.getScheduler().schedule(plugin,
 				new PendingTeleportTpaHere(plugin, target.getName()), 10,
 				TimeUnit.SECONDS);
 	}
 
 	public void sendMessage(String player, String string) {
 		if (playerOnline(player)) {
 			String message = plugin.getMessage(string);
 			if (message == null) {
 				message = string;
 			}
 			plugin.getProxy().getPlayer(player).sendMessage(message);
 		}
 	}
 
 	public void tpAccept(String player) {
 		if (plugin.pendingTeleportsTPA.containsKey(player)) {
 			TeleportToPlayer(plugin.pendingTeleportsTPA.get(player), player);
 			plugin.pendingTeleportsTPA.remove(player);
 
 		} else if (plugin.pendingTeleportsTPAHere.containsKey(player)) {
 			TeleportToPlayer(player, plugin.pendingTeleportsTPAHere.get(player));
 			plugin.pendingTeleportsTPAHere.remove(player);
 		} else {
 			sendMessage(player, "NO_TELEPORTS");
 		}
 	}
 
 	public void tpDeny(String player) {
 		if (plugin.pendingTeleportsTPA.containsKey(player)) {
 			sendMessage(plugin.pendingTeleportsTPA.get(player),
 					"TELEPORT_REQUEST_DENIED");
 			plugin.pendingTeleportsTPA.remove(player);
 			sendMessage(player, "TELEPORT_DENIED");
 			return;
 		} else if (plugin.pendingTeleportsTPAHere.containsKey(player)) {
 			if (plugin.pendingTeleportsTPAHere.containsKey(player)) {
 				sendMessage(plugin.pendingTeleportsTPAHere.get(player),
 						"TELEPORT_REQUEST_DENIED");
 				plugin.pendingTeleportsTPAHere.remove(player);
 				sendMessage(player, "TELEPORT_DENIED");
 				return;
 			}
 
 		} else {
 			// sendMessage(player, "NO_TELEPORTS");
 		}
 	}
 
 	public void getWarpsList(String name, boolean permission)
 			throws SQLException {
 		ProxiedPlayer pp = getPlayer(name);
 		if (pp != null) {
 			
 			String msg = ChatColor.BLUE + "Warps: ";
 			ResultSet res = sql
 					.sqlQuery("SELECT warpname FROM BungeeWarps WHERE private=FALSE");
 			while (res.next()) {
 				msg += res.getString("warpname") + ", ";
 			}
 			res.close();
 			pp.sendMessage(msg.substring(0, msg.length() - 2));
 			if (permission) {
 				msg = ChatColor.GRAY + "Private Warps: ";
 				ResultSet res2 = sql
 						.sqlQuery("SELECT warpname FROM BungeeWarps WHERE private=TRUE");
 				while (res2.next()) {
 					msg += res2.getString("warpname") + ", ";
 				}
 				res2.close();
 				pp.sendMessage(msg.substring(0, msg.length() - 2));
 			}
 			
 		}
 
 	}
 
 	public void deleteWarp(String sender, String name) throws SQLException {
 		if (!warpExists(name)) {
 			getPlayer(sender).sendMessage(
 					plugin.getMessage("WARP_DOES_NOT_EXIST"));
 			return;
 		}
 		
 		sql.standardQuery("DELETE FROM BungeeWarps WHERE warpname ='" + name
 				+ "'");
 		
 		getPlayer(sender).sendMessage(plugin.getMessage("WARP_DELETED"));
 	}
 
 	public boolean warpExists(String name) {
 		
 		boolean check = sql
 				.existanceQuery("SELECT * FROM BungeeWarps WHERE warpname='"
 						+ name + "'");
 		
 		return check;
 	}
 
 	public boolean warpIsPrivate(String name) {
 		
 		boolean check = sql
 				.existanceQuery("SELECT * FROM BungeeWarps WHERE warpname='"
 						+ name + "' AND private=TRUE");
 		
 		return check;
 	}
 
 	public void createWarp(String sender, String name, String server,
 			String world, double x, double y, double z, float yaw, float pitch,
 			boolean hidden) throws SQLException {
 		if (warpExists(name)) {
 			getPlayer(sender).sendMessage(
 					plugin.getMessage("WARP_ALREADY_EXISTS"));
 			return;
 		}
 		
 		sql.standardQuery("INSERT INTO BungeeWarps VALUES('" + name + "','"
 				+ server + "','" + world + "'," + x + "," + y + "," + z + ","
 				+ yaw + "," + pitch + "," + hidden + ")");
 		
 		sendMessage(sender, "WARP_CREATED");
 	}
 
 	public void warpPlayer(String sender, String player, String warpName,
 			boolean paccess) throws SQLException {
 		ProxiedPlayer playerP = getClosestPlayer(player);
 		if (!warpExists(warpName)) {
 			getPlayer(sender).sendMessage(
 					plugin.getMessage("WARP_DOES_NOT_EXIST"));
 			return;
 		}
 		if (warpIsPrivate(warpName) && !paccess) {
 			getPlayer(sender).sendMessage(
 					plugin.getMessage("WARP_NO_PERMISSION"));
 			return;
 		}
 		if (playerP == null) {
 			getPlayer(sender).sendMessage(
 					plugin.getMessage("PLAYER_NOT_ONLINE"));
 			return;
 		}
 
 		String loc = getWarp(warpName);
 		String server = loc.split("~")[0];
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("WarpPlayer");
 			out.writeUTF(playerP.getName());
 			out.writeUTF(loc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		if (server.equals(playerP.getServer().getInfo().getName())) {
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(
 							plugin,
 							new SendPluginMessage("BungeeSuiteWarps",
 									getServer(server), b));
 			playerP.sendMessage(plugin.getMessage("PLAYER_WARPED"));
 			return;
 		} else {
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(
 							plugin,
 							new SendPluginMessage("BungeeSuiteWarps",
 									getServer(server), b));
 			playerP.connect(getServer(server));
 			playerP.sendMessage(plugin.getMessage("PLAYER_WARPED"));
 			return;
 		}
 
 	}
 
 	private String getWarp(String warpName) {
 		String loc = "";
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM BungeeWarps WHERE warpname = '"
 						+ warpName + "'");
 		try {
 			while (res.next()) {
 				try {
 					loc += res.getString("server") + "~";
 					loc += res.getString("warpname") + "~";
 					loc += res.getString("world") + "~";
 					loc += res.getDouble("x") + "~";
 					loc += res.getDouble("y") + "~";
 					loc += res.getDouble("z") + "~";
 					loc += res.getFloat("yaw") + "~";
 					loc += res.getFloat("pitch");
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return loc;
 	}
 
 	public void getPortals(String server) throws SQLException {
 		
 		boolean checkexist = sql
 				.existanceQuery("SELECT * FROM BungeePortals WHERE server ='"
 						+ server + "'");
 		if (!checkexist) {
 			return;
 		}
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM BungeePortals WHERE server ='"
 						+ server + "'");
 		while (res.next()) {
 			ByteArrayOutputStream b = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(b);
 			try {
 				out.writeUTF("Portal");
 				out.writeUTF(res.getString("portalname"));
 				String check = res.getString("toServer");
 				String type = null;
 				String dest = null;
 				if (check == null) {
 					type = "warp";
 					dest = res.getString("towarp");
 				} else {
 					type = "server";
 					dest = res.getString("toserver");
 				}
 				out.writeUTF(type);
 				out.writeUTF(dest);
 				out.writeUTF(res.getString("world"));
 				out.writeUTF(res.getString("filltype"));
 				out.writeInt(res.getInt("xmax"));
 				out.writeInt(res.getInt("xmin"));
 				out.writeInt(res.getInt("ymax"));
 				out.writeInt(res.getInt("ymin"));
 				out.writeInt(res.getInt("zmax"));
 				out.writeInt(res.getInt("zmin"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(
 							plugin,
 							new SendPluginMessage("BungeeSuitePorts",
 									getServer(server), b));
 		}
 
 		
 
 	}
 
 	public boolean portalExists(String name) {
 		
 		boolean check = false;
 		check = sql
 				.existanceQuery("SELECT * FROM BungeePortals WHERE portalname ='"
 						+ name + "'");
 		
 		return check;
 	}
 
 	public void createPortal(String sender, String name, String type,
 			String dest, String world, String filltype, int xmax, int xmin,
 			int ymax, int ymin, int zmax, int zmin) throws SQLException {
 
 		if (portalExists(name)) {
 			sendMessage(sender, "PORTAL_ALREADY_EXISTS");
 			return;
 		}
 		if (!(filltype.equalsIgnoreCase("water")
 				|| filltype.equalsIgnoreCase("air")
 				|| filltype.equalsIgnoreCase("lava")
 				|| filltype.equalsIgnoreCase("web")
 				|| filltype.equalsIgnoreCase("portal") || filltype
 					.equalsIgnoreCase("end_portal"))) {
 			sendMessage(sender, "PORTAL_FILLTYPE");
 			return;
 		}
 		if (!(type.equalsIgnoreCase("server") || type.equalsIgnoreCase("warp"))) {
 			sendMessage(sender, "PORTAL_WRONG_TYPE");
 			return;
 		}
 		filltype = filltype.toUpperCase();
 		if (type.equalsIgnoreCase("server")) {
 			if (!serverExists(dest)) {
 				sendMessage(sender, "SERVER_DOEST_NOT_EXIST");
 				return;
 			}
 			
 			sql.standardQuery("INSERT INTO BungeePortals (portalname, server, toserver,world,filltype,xmax,xmin,ymax,ymin,zmax,zmin) VALUES('"
 					+ name
 					+ "','"
 					+ getPlayer(sender).getServer().getInfo().getName()
 					+ "','"
 					+ dest
 					+ "','"
 					+ world
 					+ "','"
 					+ filltype
 					+ "',"
 					+ xmax
 					+ ","
 					+ xmin
 					+ ","
 					+ ymax
 					+ ","
 					+ ymin
 					+ ","
 					+ zmax
 					+ ","
 					+ zmin + ")");
 		}
 		if (type.equalsIgnoreCase("warp")) {
 			if (!warpExists(dest)) {
 				sendMessage(sender, "WARP_DOES_NOT_EXIST");
 				return;
 			}
 			
 			sql.standardQuery("INSERT INTO BungeePortals VALUES('" + name
 					+ "','" + getPlayer(sender).getServer().getInfo().getName()
 					+ "',NULL,'" + dest + "','" + world + "','" + filltype
 					+ "'," + xmax + "," + xmin + "," + ymax + "," + ymin + ","
 					+ zmax + "," + zmin + ")");
 		}
 		
 
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("Portal");
 			out.writeUTF(name);
 			out.writeUTF(type);
 			out.writeUTF(dest);
 			out.writeUTF(world);
 			out.writeUTF(filltype);
 			out.writeInt(xmax);
 			out.writeInt(xmin);
 			out.writeInt(ymax);
 			out.writeInt(ymin);
 			out.writeInt(zmax);
 			out.writeInt(zmin);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(
 						plugin,
 						new SendPluginMessage("BungeeSuitePorts", getPlayer(
 								sender).getServer().getInfo(), b));
 
 		sendMessage(sender, "PORTAL_CREATED");
 
 	}
 
 	public void warpPlayerSilent(String sender, String player, String warpName) {
 		ProxiedPlayer playerP = getClosestPlayer(player);
 		if (!warpExists(warpName)) {
 			getPlayer(sender).sendMessage(
 					plugin.getMessage("WARP_DOES_NOT_EXIST"));
 			return;
 		}
 		if (playerP == null) {
 			getPlayer(sender).sendMessage(
 					plugin.getMessage("PLAYER_NOT_ONLINE"));
 			return;
 		}
 
 		String loc = getWarp(warpName);
 		String server = loc.split("~")[0];
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("WarpPlayer");
 			out.writeUTF(playerP.getName());
 			out.writeUTF(loc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		if (server.equals(playerP.getServer().getInfo().getName())) {
 			plugin.getProxy()
 					.getScheduler()
 					.schedule(
 							plugin,
 							new SendPluginMessage("BungeeSuiteWarps",
 									getServer(server), b), plugin.DELAY_TIME,
 							TimeUnit.MILLISECONDS);
 			return;
 		} else {
 			playerP.connect(getServer(server));
 			plugin.getProxy()
 					.getScheduler()
 					.schedule(
 							plugin,
 							new SendPluginMessage("BungeeSuiteWarps",
 									getServer(server), b), plugin.DELAY_TIME,
 							TimeUnit.MILLISECONDS);
 			return;
 		}
 
 	}
 
 	public void deletePortal(String sender, String portal) throws SQLException {
 		if (!portalExists(portal)) {
 			sendMessage(sender, "PORTAL_DOES_NOT_EXIST");
 			return;
 		}
 		String server = null;
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT server FROM BungeePortals WHERE portalname ='"
 						+ portal + "'");
 		try {
 			while (res.next()) {
 				server = res.getString("server");
 			}
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 		sql.standardQuery("DELETE FROM BungeePortals WHERE portalname = '"
 				+ portal + "'");
 		
 
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("PortalDelete");
 			out.writeUTF(portal);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(
 						plugin,
 						new SendPluginMessage("BungeeSuitePorts",
 								getServer(server), b));
 		sendMessage(sender, "PORTAL_DELETED");
 	}
 
 	public void listPortals(String sender) {
 		ProxiedPlayer pp = getPlayer(sender);
 		if (pp != null) {
 			
 			String msg = ChatColor.BLUE + "Portals: ";
 			ResultSet res = sql
 					.sqlQuery("SELECT portalname FROM BungeePortals");
 			try {
 				while (res.next()) {
 					msg += res.getString("portalname") + ", ";
 				}
 				res.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			pp.sendMessage(msg.substring(0, msg.length() - 2));
 			
 
 		}
 
 	}
 
 	public void setSpawn(String sender, String type, String server,
 			String world, double x, double y, double z, float yaw, float pitch)
 			throws SQLException {
 		if (type.equals("world")) {
 			type = server + "-" + world;
 		}
 		if (spawnExists(type)) {
 			
 			sql.standardQuery("UPDATE BungeeSpawns SET server='" + server
 					+ "', world='" + world + "', x=" + x + ", y=" + y + ", z="
 					+ z + ", yaw=" + yaw + ", pitch= " + pitch
 					+ " WHERE spawnname='" + type + "'");
 			
 			sendMessage(sender, "SPAWN_SET");
 			return;
 		} else {
 			
 			sql.standardQuery("INSERT INTO BungeeSpawns VALUES ('" + type
 					+ "','" + server + "', '" + world + "', " + x + ", " + y
 					+ ", " + z + ", " + yaw + "," + pitch + ")");
 			
 			sendMessage(sender, "SPAWN_SET");
 			return;
 		}
 
 	}
 
 	private boolean spawnExists(String type) {
 		
 		boolean check = false;
 		check = sql
 				.existanceQuery("SELECT * FROM BungeeSpawns WHERE spawnname = '"
 						+ type + "'");
 		
 		return check;
 	}
 
 	public String getSpawn(String spawnname) throws SQLException {
 		String loc = "";
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM BungeeSpawns WHERE spawnname = '"
 						+ spawnname + "'");
 
 		while (res.next()) {
 			loc += res.getString("server") + "~";
 			loc += res.getString("world") + "~";
 			loc += res.getDouble("x") + "~";
 			loc += res.getDouble("y") + "~";
 			loc += res.getDouble("z") + "~";
 			loc += res.getFloat("yaw") + "~";
 			loc += res.getFloat("pitch");
 		}
 		res.close();
 		
 		return loc;
 	}
 
 	public void sendPlayerToSpawn(String sender, String spawnname)
 			throws SQLException {
 
 		if (spawnname.equalsIgnoreCase("newplayerspawn") && plugin.newspawn) {
 			if (!spawnExists("newplayerspawn")) {
 				System.out.println("NO NEWSPAWN");
 				sendPlayerToSpawn(sender, "spawn");
 				return;
 			} else {
 				String loc = getSpawn("newplayerspawn");
 				String server = loc.split("~")[0];
 				ByteArrayOutputStream b = new ByteArrayOutputStream();
 				DataOutputStream out = new DataOutputStream(b);
 				try {
 					out.writeUTF("SpawnPlayer");
 					out.writeUTF(sender);
 					out.writeUTF(loc);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				if (server.equals(getPlayersServer(sender).getInfo().getName())) {
 					plugin.getProxy()
 							.getScheduler()
 							.schedule(
 									plugin,
 									new SendPluginMessage("BungeeSuiteSpawn",
 											getServer(server), b),
 									plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 					return;
 				} else {
 					getPlayer(sender).connect(getServer(server));
 					plugin.getProxy()
 							.getScheduler()
 							.schedule(
 									plugin,
 									new SendPluginMessage("BungeeSuiteSpawn",
 											getServer(server), b),
 									plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 					return;
 				}
 
 			}
 		} else if (spawnExists("spawn")) {
 			String loc = getSpawn("spawn");
 			String server = loc.split("~")[0];
 			ByteArrayOutputStream b = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(b);
 			try {
 				out.writeUTF("SpawnPlayer");
 				out.writeUTF(sender);
 				out.writeUTF(loc);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			if (server.equals(getPlayersServer(sender).getInfo().getName())) {
 				plugin.getProxy()
 						.getScheduler()
 						.schedule(
 								plugin,
 								new SendPluginMessage("BungeeSuiteSpawn",
 										getServer(server), b),
 								plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 				return;
 			} else {
 				getPlayer(sender).connect(getServer(server));
 				plugin.getProxy()
 						.getScheduler()
 						.schedule(
 								plugin,
 								new SendPluginMessage("BungeeSuiteSpawn",
 										getServer(server), b),
 								plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 				return;
 			}
 
 		} else {
 			sendMessage(sender, "SPAWN_DOES_NOT_EXIST");
 			return;
 		}
 
 	}
 
 	public boolean playerIsBanned(String player) {
 		boolean check = false;
 		
 		check = sql.existanceQuery("SELECT * FROM BungeeBans WHERE player = '"
 				+ player + "'");
 		
 		return check;
 	}
 
 	public void banPlayer(String sender, String player, String msg)
 			throws SQLException {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		String pname = null;
 		if (pp == null) {
 			if (!playerExists(player)) {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 			if (playerIsBanned(player)) {
 				sendMessage(sender, "PLAYER_ALREADY_BANNED");
 				return;
 			}
 			
 			sql.standardQuery("INSERT INTO BungeeBans (player,banned_by,banned_on,reason,type) VALUES ('"
 					+ player
 					+ "', '"
 					+ sender
 					+ "', CURRENT_DATE(), '"
 					+ msg
 					+ "', 'ban')");
 			
 
 		} else {
 			if (playerIsBanned(pp.getName())) {
 				sendMessage(sender, "PLAYER_ALREADY_BANNED");
 				return;
 			}
 			pname = pp.getDisplayName();
 			
 			sql.standardQuery("INSERT INTO BungeeBans (player,banned_by,banned_on,reason,type) VALUES ('"
 					+ pp.getName()
 					+ "', '"
 					+ sender
 					+ "', CURRENT_DATE(),'"
 					+ msg + "', 'ban')");
 			
 			String message = null;
 			if (msg.equalsIgnoreCase("")) {
 				message = plugin.getMessage("BAN_PLAYER");
 			} else {
 				message = plugin.getMessage("BAN_PLAYER_MESSAGE_PREFIX");
 				message = message.replace("{message}", msg);
 			}
 			pp.disconnect(message);
 		}
 		String message = null;
 		if (msg.equalsIgnoreCase("")) {
 			message = plugin.getMessage("BAN_PLAYER_BROADCAST");
 			if (pp == null) {
 				message = message.replace("{player}", player);
 			} else {
 				message = message.replace("{player}", pname);
 			}
 		} else {
 			message = plugin.getMessage("BAN_PLAYER_BROADCAST_MESSAGE_PREFIX");
 			message = message.replace("{message}", msg);
 			if (pp == null) {
 				message = message.replace("{player}", player);
 			} else {
 				message = message.replace("{player}", pname);
 			}
 		}
 		sendBroadcast(message);
 
 	}
 
 	public void kickAll(String msg) {
 		if (msg.equalsIgnoreCase("")) {
 			for (ProxiedPlayer data : plugin.getProxy().getPlayers()) {
 				data.disconnect(plugin.getMessage("KICK_PLAYER"));
 			}
 			return;
 		} else {
 			String message = plugin.getMessage("KICK_PLAYER_MESSAGE");
 			message = message.replace("{message}", msg);
 			for (ProxiedPlayer data : plugin.getProxy().getPlayers()) {
 				data.disconnect(message);
 			}
 			return;
 		}
 
 	}
 
 	public void kickPlayer(String sender, String player, String msg) {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		if (pp == null) {
 			sendMessage(sender, "PLAYER_NOT_ONLINE");
 			return;
 		}
 		if (msg.equals("")) {
 			pp.disconnect(plugin.getMessage("KICK_PLAYER"));
 			if (plugin.broadcastBans) {
 				String message = plugin.getMessage("KICK_PLAYER_BROADCAST");
 				message = message.replace("{player}", pp.getDisplayName());
 				sendBroadcast(message);
 			}
 			return;
 		} else {
 			String message = plugin.getMessage("KICK_PLAYER_MESSAGE");
 			message = message.replace("{message}", msg);
 			pp.disconnect(message);
 			if (plugin.broadcastBans) {
 				String message2 = plugin
 						.getMessage("KICK_PLAYER_BROADCAST_MESSAGE_PREFIX");
 				message2 = message2.replace("{player}", pp.getDisplayName());
 				message2 = message2.replace("{message}", msg);
 				sendBroadcast(message2);
 			}
 			return;
 		}
 
 	}
 
 	public void sendBroadcast(String msg) {
 		for (ProxiedPlayer data : plugin.getProxy().getPlayers()) {
 			data.sendMessage(msg);
 		}
 	}
 
 	public void tempBanPlayer(String sender, String player, int minute,
 			int hour, int day, String msg) throws SQLException {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		String pname = null;
 		if (pp == null) {
 			if (!playerExists(player)) {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 			if (playerIsBanned(player)) {
 				sendMessage(sender, "PLAYER_ALREADY_BANNED");
 				return;
 			}
 			
 
 			sql.standardQuery("INSERT INTO BungeeBans(player, banned_by,banned_on,reason,type, banned_until) VALUES('"
 					+ player
 					+ "','"
 					+ sender
 					+ "', NOW(),'"
 					+ msg
 					+ "', 'temp ban', DATE_ADD(DATE_ADD(DATE_ADD(NOW(), INTERVAL "
 					+ minute
 					+ " MINUTE), INTERVAL "
 					+ hour
 					+ " HOUR),INTERVAL " + day + " DAY))");
 
 			
 
 		} else {
 			if (playerIsBanned(pp.getName())) {
 				sendMessage(sender, "PLAYER_ALREADY_BANNED");
 				return;
 			}
 			pname = pp.getDisplayName();
 			
 			sql.standardQuery("INSERT INTO BungeeBans(player, banned_by,banned_on,reason,type, banned_until) VALUES('"
 					+ pp.getName()
 					+ "','"
 					+ sender
 					+ "',NOW(), '"
 					+ msg
 					+ "', 'temp ban', DATE_ADD(DATE_ADD(DATE_ADD(NOW(), INTERVAL "
 					+ minute
 					+ " MINUTE), INTERVAL "
 					+ hour
 					+ " HOUR),INTERVAL " + day + " DAY))");
 			
 			String message = null;
 			if (msg.equalsIgnoreCase("")) {
 				message = plugin.getMessage("TEMP_BAN");
 				message = message.replace("{time}", "Days: " + day + " Hours: "
 						+ hour + " Minutes: " + minute);
 			} else {
 				message = plugin.getMessage("TEMP_BAN_MESSAGE");
 				message = message.replace("{time}", "Days: " + day + " Hours: "
 						+ hour + " Minutes: " + minute);
 				message = message.replace("{message}", msg);
 			}
 			pp.disconnect(message);
 		}
 		String message = null;
 		if (msg.equalsIgnoreCase("")) {
 			message = plugin.getMessage("TEMP_BAN_BROADCAST");
 			if (pp == null) {
 				message = message.replace("{player}", player);
 			} else {
 				message = message.replace("{player}", pname);
 			}
 			message = message.replace("{time}", "Days: " + day + " Hours: "
 					+ hour + " Minutes: " + minute);
 		} else {
 			message = plugin.getMessage("TEMP_BAN_BROADCAST_MESSAGE");
 			message = message.replace("{message}", msg);
 			if (pp == null) {
 				message = message.replace("{player}", player);
 			} else {
 				message = message.replace("{player}", pname);
 			}
 			message = message.replace("{time}", "Days: " + day + " Hours: "
 					+ hour + " Minutes: " + minute);
 		}
 		sendBroadcast(message);
 
 	}
 
 	public void unbanPlayer(String sender, String player) throws SQLException {
 		if (!playerExists(player)) {
 			sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 			return;
 		}
 		if (!playerIsBanned(player)) {
 			sendMessage(sender, "PLAYER_NOT_BANNED");
 			return;
 		}
 		
 		sql.standardQuery("DELETE FROM BungeeBans WHERE player = '" + player
 				+ "'");
 		
 		String message = plugin.getMessage("PLAYER_UNBANNED");
 		message = message.replace("{player}", player);
 		sendBroadcast(message);
 	}
 
 	public String getBanReason(String name) {
 		
 		String reason = null;
 		ResultSet res = sql
 				.sqlQuery("SELECT reason FROM BungeeBans WHERE player = '"
 						+ name + "'");
 		try {
 			while (res.next()) {
 				reason = res.getString("reason");
 			}
 			res.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 
 		return reason;
 	}
 
 	public boolean playerBanIsTemp(String name) {
 		boolean check = false;
 		
 		check = sql
 				.existanceQuery("SELECT * FROM BungeeBans WHERE type = 'temp ban' AND player = '"
 						+ name + "'");
 		
 		return check;
 	}
 
 	public Calendar playerBanTempDate(String name) {
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT banned_until FROM BungeeBans WHERE player = '"
 						+ name + "'");
 		Calendar cal = null;
 		try {
 			while (res.next()) {
 				Timestamp date = res.getTimestamp("banned_until");
 				cal = Calendar.getInstance();
 				cal.setTime(date);
 			}
 			res.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return cal;
 	}
 
 	public void removeTempBan(String name) throws SQLException {
 		
 		sql.standardQuery("DELETE FROM BungeeBans WHERE player = '" + name
 				+ "'");
 		
 
 	}
 
 	public boolean tableExists(String string) {
 		boolean check;
 		
 		check = sql.doesTableExist(string);
 		
 		return check;
 	}
 
 	public void createChatConfig() {
 		if (!plugin.channelsCreated) {
 			String configpath = "/plugins/BungeeSuite/chat.yml";
 			plugin.chatConfig = new Config(configpath);
 			Config c = plugin.chatConfig;
 			plugin.logChat = c.getBoolean("Log chat to console", true);
 			plugin.chatSpySeesAll = c
 					.getBoolean("ChatSpy sees all chat", false);
 			plugin.CleanTab = c.getBoolean("CleanTab", false);
 			plugin.nickNameLimit = c.getInt("NicknameLengthLimit", 16);
 			plugin.prefaceNicks = c.getBoolean("PrefaceDisplayNames", false);
 			plugin.cleanChatRegex = c.getString("CleanChatRegex",
 					"([&][klmno])");
 			plugin.globalChatRegex = c.getString("GlobalChatRegex",
 					"\\{(factions_.*?)\\}");
 			plugin.localChatRange = c.getInt("Channels.LocalChatRadius", 100);
 			plugin.defaultChannel = c.getString("DefaultChannel", "Global");
 			plugin.adminColor = c.getString("AdminChatColor", "&f");
 			plugin.channelFormats.put(
 					"Global",
 					c.getString("Channels.Global",
 							plugin.getMessage("CHANNEL_DEFAULT_GLOBAL")));
 			String faction = c.getString("Channels.Faction",
 					plugin.getMessage("CHANNEL_DEFAULT_FACTION"));
 			plugin.channelFormats.put("Faction", faction);
 			String factionally = c.getString("Channels.FactionAlly",
 					plugin.getMessage("CHANNEL_DEFAULT_FACTION_ALLY"));
 			plugin.channelFormats.put("FactionAlly", factionally);
 			// plugin.adminChannel = c.getBoolean("AdminChannelEnabled", true);
 			// if(plugin.adminChannel){
 			// plugin.channelFormats.put("Admin", c.getString("Channels.Admin",
 			// plugin.getMessage("CHANNEL_DEFAULT_ADMIN")));
 			// }
 			for (String data : plugin.proxy.getServers().keySet()) {
 				String server = c.getString("Channels.Servers." + data
 						+ ".Server",
 						plugin.getMessage("CHANNEL_DEFAULT_SERVER"));
 				plugin.channelFormats.put(data, server);
 				String local = c.getString("Channels.Servers." + data
 						+ ".Local", plugin.getMessage("CHANNEL_DEFAULT_LOCAL"));
 				plugin.shortForm.put(data, c.getString("Channels.Servers."
 						+ data + ".Shortname", data.charAt(0) + ""));
 
 				plugin.forcedChannels.put(
 						data,
 						c.getString("Channels.Servers." + data
 								+ ".ForcedChannel", null));
 				plugin.channelFormats.put(data + "local", local);
 			}
 			plugin.channelsCreated = true;
 			plugin.usingPrefix = c.getBoolean("Prefix.Enabled", false);
 			c.getString("Prefix.Groups.Default", "&5[Member]");
 			if (plugin.usingPrefix) {
 				List<String> grouplist = c.getSubNodes("Prefix.Groups");
 				for (String data : grouplist) {
 					plugin.prefixes.put(data,
 							c.getString("Prefix.Groups." + data, null));
 				}
 			}
 			plugin.usingSuffix = c.getBoolean("Suffix.Enabled", false);
 			c.getString("Suffix.Groups.Default", "&4");
 			if (plugin.usingSuffix) {
 				List<String> grouplist = c.getSubNodes("Suffix.Groups");
 				for (String data : grouplist) {
 					plugin.suffixes.put(data,
 							c.getString("Suffix.Groups." + data, null));
 				}
 			}
 		}
 	}
 
 	public void reloadChat(String sender) {
 		plugin.channelsCreated = false;
 		plugin.forcedChannels.clear();
 		createChatConfig();
 		for (String data : plugin.proxy.getServers().keySet()) {
 			if (plugin.proxy.getServerInfo(data) != null) {
 				getChannelFormats(data);
 				sendLocalChannelRadius(data);
 				this.getForcedServerChannel(data);
 			}
 		}
 		sendMessage(sender, "ChatReloaded");
 	}
 
 	public void getChannelFormats(String server) {
 		String formats = "";
 		formats += "Global" + "~" + plugin.channelFormats.get("Global") + "~";
 		formats += "Faction" + "~" + plugin.channelFormats.get("Faction") + "~";
 		formats += "FactionAlly" + "~"
 				+ plugin.channelFormats.get("FactionAlly") + "~";
 		formats += "Server" + "~" + plugin.channelFormats.get(server) + "~";
 		formats += "Local" + "~" + plugin.channelFormats.get(server + "local")
 				+ "~";
 		if (plugin.adminChannel) {
 			formats += "Admin" + "~" + plugin.channelFormats.get("Admin");
 		}
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("SendFormat");
 			out.writeUTF(server);
 			out.writeUTF(formats);
 			out.writeUTF(plugin.shortForm.get(server));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		ServerInfo s = plugin.getProxy().getServerInfo(server);
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(plugin,
 						new SendPluginMessage("BungeeSuiteChat", s, b));
 		if (plugin.usingPrefix) {
 			sendPrefixes(server);
 		}
 		if (plugin.usingSuffix) {
 			sendSuffixes(server);
 		}
 	}
 
 	private void sendPrefixes(String server) {
 		if (plugin.prefixes.isEmpty()) {
 			return;
 		}
 		String prefix = "";
 		for (String data : plugin.prefixes.keySet()) {
 			prefix += data + "~" + plugin.prefixes.get(data) + "~";
 		}
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("SendPrefix");
 			out.writeUTF(prefix);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		ServerInfo s = plugin.getProxy().getServerInfo(server);
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(plugin,
 						new SendPluginMessage("BungeeSuiteChat", s, b));
 
 	}
 
 	private void sendSuffixes(String server) {
 		if (plugin.suffixes.isEmpty()) {
 			return;
 		}
 		String prefix = "";
 		for (String data : plugin.suffixes.keySet()) {
 			prefix += data + "~" + plugin.suffixes.get(data) + "~";
 		}
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("SendSuffix");
 			out.writeUTF(prefix);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		ServerInfo s = plugin.getProxy().getServerInfo(server);
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(plugin,
 						new SendPluginMessage("BungeeSuiteChat", s, b));
 
 	}
 
 	public void sendLocalChannelRadius(String server) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("LocalRadius");
 			out.writeInt(plugin.localChatRange);
 			out.writeUTF(plugin.adminColor);
 			out.writeUTF(plugin.cleanChatRegex);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		ServerInfo s = plugin.getProxy().getServerInfo(server);
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(plugin,
 						new SendPluginMessage("BungeeSuiteChat", s, b));
 
 	}
 
 	public void sendPrivateMessage(String sender, String player, String message) {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		ProxiedPlayer send = plugin.proxy.getPlayer(sender);
 		if (pp == null) {
 			sendMessage(sender, "PLAYER_NOT_ONLINE");
 			return;
 		}
 		if (plugin.playersIgnores.get(pp.getName()).contains(sender)) {
 			String msg = plugin.getMessage("PLAYER_IGNORING");
 			msg = msg.replace("{player}", pp.getName());
 			sendMessage(sender, msg);
 			return;
 		}
 		String msgsent = plugin.getMessage("PRIVATE_MESSAGE_OTHER_PLAYER");
 		msgsent = msgsent.replace("{player}", pp.getDisplayName());
 		msgsent = msgsent.replace("{message}", message);
 		this.sendMessage(sender, msgsent);
 		String msgrec = plugin.getMessage("PRIVATE_MESSAGE_RECEIVE");
 		msgrec = msgrec.replace("{player}", send.getDisplayName());
 		msgrec = msgrec.replace("{message}", message);
 		sendMessage(pp.getName(), msgrec);
 		plugin.replyMessages.put(pp.getName(), sender);
 		String spy = plugin.getMessage("PRIVATE_MESSAGE_SPY");
 		spy = spy.replace("{sender}", send.getDisplayName());
 		spy = spy.replace("{player}", pp.getDisplayName());
 		spy = spy.replace("{message}", message);
 		for (String data : plugin.chatspies) {
 			if (data.equals(sender) || data.equals(pp.getName())) {
 
 			} else {
 				sendMessage(data, spy);
 			}
 		}
 		if (plugin.logChat) {
 			plugin.proxy.getConsole().sendMessage(spy);
 		}
 
 	}
 
 	public void muteAll(String sender) {
 		if (!plugin.muteAll) {
 			sendMessage(sender, plugin.getMessage("MUTE_ALL_ENABLED"));
 			muteEnable();
 		} else {
 			sendMessage(sender, plugin.getMessage("MUTE_ALL_DISABLED"));
 			muteDisable();
 		}
 
 	}
 
 	private void muteDisable() {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("MuteAll");
 			out.writeUTF("disable");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		for (ServerInfo data : plugin.proxy.getServers().values()) {
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(plugin,
 							new SendPluginMessage("BungeeSuiteChat", data, b));
 		}
 		plugin.muteAll = false;
 
 	}
 
 	private void muteEnable() {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("MuteAll");
 			out.writeUTF("enable");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		for (ServerInfo data : plugin.proxy.getServers().values()) {
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(plugin,
 							new SendPluginMessage("BungeeSuiteChat", data, b));
 		}
 		plugin.muteAll = true;
 
 	}
 
 	public void mutePlayer(String sender, String player) throws SQLException {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		String mute;
 		if (pp == null) {
 			if (!playerExists(player)) {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 			mute = player;
 		} else {
 			mute = pp.getName();
 		}
 		if (isMuted(mute)) {
 			
 			sql.standardQuery("UPDATE BungeePlayers SET muted=0 WHERE playername = '"
 					+ mute + "'");
 			
 			if (pp != null) {
 				ByteArrayOutputStream b = new ByteArrayOutputStream();
 				DataOutputStream out = new DataOutputStream(b);
 				try {
 					out.writeUTF("UnMute");
 					out.writeUTF(pp.getName());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				plugin.getProxy()
 						.getScheduler()
 						.runAsync(
 								plugin,
 								new SendPluginMessage("BungeeSuiteChat", pp
 										.getServer().getInfo(), b));
 				pp.sendMessage(plugin.getMessage("UNMUTED"));
 				if (plugin.playerdata.containsKey(mute)) {
 					plugin.playerdata.get(mute).mute = false;
 				}
 			}
 			sendMessage(sender, plugin.getMessage("PLAYER_UNMUTED"));
 		} else {
 			
 			sql.standardQuery("UPDATE BungeePlayers SET muted=1 WHERE playername = '"
 					+ mute + "'");
 			if (pp != null) {
 				ByteArrayOutputStream b = new ByteArrayOutputStream();
 				DataOutputStream out = new DataOutputStream(b);
 				try {
 					out.writeUTF("Mute");
 					out.writeUTF(pp.getName());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				plugin.getProxy()
 						.getScheduler()
 						.runAsync(
 								plugin,
 								new SendPluginMessage("BungeeSuiteChat", pp
 										.getServer().getInfo(), b));
 				pp.sendMessage(plugin.getMessage("MUTED"));
 			}
 			sendMessage(sender, plugin.getMessage("PLAYER_MUTED"));
 			
 			if (plugin.playerdata.containsKey(mute)) {
 				plugin.playerdata.get(mute).mute = true;
 			}
 		}
 	}
 
 	private boolean isMuted(String mute) {
 		boolean check;
 		if (plugin.playerdata.containsKey(mute)) {
 			check = plugin.playerdata.get(mute).mute;
 		} else {
 			
 			check = sql
 					.existanceQuery("SELECT * FROM BungeePlayers WHERE playername = '"
 							+ mute + "' AND muted = 1");
 			
 		}
 		return check;
 	}
 
 	public void nicknamePlayer(String sender, String player, String nickname)
 			throws SQLException {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		// String cleannick = nickname.replaceAll("[&][0-9a-fk-or]", "");
 		if (nickname.length() > plugin.nickNameLimit) {
 			// sendMessage(sender, cleannick);
 			sendMessage(sender, "NICKNAME_TOO_LONG");
 			return;
 		}
 		if (nicknameExists(sender, nickname)) {
 			sendMessage(sender, "NICKNAME_TAKEN");
 			return;
 		}
 		if (pp == null) {
 			if (!playerExists(player)) {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 		} else {
 			player = pp.getName();
 		}
 		
 		if (player.equals(nickname)) {
 			sql.standardQuery("UPDATE BungeePlayers SET nickname = NULL WHERE playername = '"
 					+ player + "'");
 		} else {
 			sql.standardQuery("UPDATE BungeePlayers SET nickname = '"
 					+ nickname + "' WHERE playername = '" + player + "'");
 		}
 		
 		String send = plugin.getMessage("NICKNAMED_PLAYER");
 		send = send.replace("{player}", player);
 		send = send.replace("{name}", nickname);
 		send = colorize(send);
 		sendMessage(sender, send);
 		if (pp != null) {
 			String play = plugin.getMessage("NICKNAME_CHANGED");
 			play = play.replace("{name}", nickname);
 			play = colorize(play);
 			pp.sendMessage(play);
 			ByteArrayOutputStream b = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(b);
 			try {
 				out.writeUTF("NicknamedPlayer");
 				out.writeUTF(player);
 				out.writeUTF(nickname);
 				out.writeBoolean(plugin.prefaceNicks);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(
 							plugin,
 							new SendPluginMessage("BungeeSuiteChat",
 									getPlayersServer(player).getInfo(), b));
 			if (plugin.playerdata.containsKey(player)) {
 				plugin.playerdata.get(player).nickname = nickname;
 			}
 		}
 
 	}
 
 	private boolean nicknameExists(String sender, String nickname) {
 		boolean check;
 		
 		check = sql
 				.existanceQuery("SELECT nickname FROM BungeePlayers WHERE nickname = '"
 						+ nickname + "'");
 		
 		return check;
 	}
 
 	public void replyToPlayer(String sender, String message) {
 		ProxiedPlayer send = plugin.proxy.getPlayer(sender);
 		if (!plugin.replyMessages.containsKey(sender)) {
 			sendMessage(sender, "NO_ONE_TO_REPLY");
 			return;
 		}
 		ProxiedPlayer pp = plugin.proxy.getPlayer(plugin.replyMessages
 				.get(sender));
 		if (pp == null) {
 			sendMessage(sender, "PLAYER_NOT_ONLINE");
 			return;
 		}
 		if (plugin.playersIgnores.get(pp.getName()).contains(sender)) {
 			String msg = plugin.getMessage("PLAYER_IGNORING");
 			msg = msg.replace("{player}", pp.getName());
 			sendMessage(sender, msg);
 			return;
 		}
 		String msgsent = plugin.getMessage("PRIVATE_MESSAGE_OTHER_PLAYER");
 		msgsent = msgsent.replace("{player}", pp.getDisplayName());
 		msgsent = msgsent.replace("{message}", message);
 		sendMessage(sender, msgsent);
 		String msgrec = plugin.getMessage("PRIVATE_MESSAGE_RECEIVE");
 		msgrec = msgrec.replace("{player}", send.getDisplayName());
 		msgrec = msgrec.replace("{message}", message);
 		sendMessage(pp.getName(), msgrec);
 		plugin.replyMessages.put(pp.getName(), sender);
 		for (String data : plugin.chatspies) {
 			if (data.equals(sender) || data.equals(pp.getName())) {
 			} else {
 				String spy = plugin.getMessage("PRIVATE_MESSAGE_SPY");
 				spy = spy.replace("{sender}", send.getDisplayName());
 				spy = spy.replace("{player}", pp.getDisplayName());
 				spy = spy.replace("{message}", message);
 				sendMessage(data, spy);
 			}
 		}
 		if (plugin.logChat) {
 			String spy = plugin.getMessage("PRIVATE_MESSAGE_SPY");
 			spy = spy.replace("{sender}", send.getDisplayName());
 			spy = spy.replace("{player}", pp.getDisplayName());
 			spy = spy.replace("{message}", message);
 			plugin.proxy.getConsole().sendMessage(spy);
 		}
 
 	}
 
 	public void toggleToChannel(String sender, String channel)
 			throws SQLException {
 		if (channel.equalsIgnoreCase("global")
 				|| channel.equalsIgnoreCase("server")
 				|| channel.equalsIgnoreCase("local")) {
 			if (plugin.forcedChannels.get(getPlayersServer(sender).getInfo()
 					.getName()) != null) {
 				setPlayersChannel(sender,
 						plugin.forcedChannels.get(getPlayersServer(sender)
 								.getInfo().getName()));
 			} else {
 				setPlayersChannel(sender, channel);
 			}
 		} else if (channelExists(channel)) {
 			setPlayersChannel(sender, channel);
 		} else {
 			sendMessage(sender, "CHANNEL_DOES_NOT_EXIST");
 		}
 
 	}
 
 	private boolean channelExists(String channel) {
 		return plugin.channelFormats.containsKey(channel);
 	}
 
 	public void getPlayersNextChannel(String sender) throws SQLException {
 		// TODO Auto-generated method stub
 		String channel = null;
 		if (plugin.forcedChannels.containsKey(getPlayersServer(sender)
 				.getInfo().getName())) {
 
 			channel = plugin.forcedChannels.get(getPlayersServer(sender)
 					.getInfo().getName());
 			if (channel == null) {
 				channel = "Server";
 			}
 		}
 		toggleToChannel(sender, channel);
 
 	}
 
 	public void setPlayersChannel(String sender, String channel)
 			throws SQLException {
 		
 		sql.standardQuery("UPDATE BungeePlayers SET channel = '" + channel
 				+ "' WHERE playername='" + sender + "'");
 		
 		String message = plugin.getMessage("CHANNEL_TOGGLE");
 		sendMessage(sender, message.replace("{channel}", channel));
 		if (plugin.playerdata.containsKey(sender)) {
 			plugin.playerdata.get(sender).channel = channel;
 		}
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("PlayersNextChannel");
 			out.writeUTF(sender);
 			out.writeUTF(channel);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(
 						plugin,
 						new SendPluginMessage("BungeeSuiteChat",
 								getPlayersServer(sender).getInfo(), b));
 
 	}
 
 	public void unMuteAll(String sender) {
 		sendMessage(sender, plugin.getMessage("MUTE_ALL_DISABLED"));
 		muteDisable();
 	}
 
 	public void unMutePlayer(String sender, String player) throws SQLException {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		String mute;
 		if (pp == null) {
 			if (!playerExists(player)) {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 			mute = player;
 		} else {
 			mute = pp.getName();
 		}
 		if (isMuted(mute)) {
 			
 			sql.standardQuery("UPDATE BungeePlayers SET muted= 0 WHERE playername = '"
 					+ mute + "'");
 			
 			if (pp != null) {
 				ByteArrayOutputStream b = new ByteArrayOutputStream();
 				DataOutputStream out = new DataOutputStream(b);
 				try {
 					out.writeUTF("UnMute");
 					out.writeUTF(pp.getName());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				plugin.getProxy()
 						.getScheduler()
 						.runAsync(
 								plugin,
 								new SendPluginMessage("BungeeSuiteChat", pp
 										.getServer().getInfo(), b));
 				pp.sendMessage(plugin.getMessage("UNMUTED"));
 				if (plugin.playerdata.containsKey(sender)) {
 					plugin.playerdata.get(sender).mute = false;
 				}
 			}
 			sendMessage(sender, plugin.getMessage("PLAYER_UNMUTED"));
 
 		} else {
 			sendMessage(sender, "PLAYER_NOT_MUTE");
 		}
 
 	}
 
 	public void sendSpyMessageAndLog(ServerInfo server, String format) {
 		if (plugin.logChat) {
 			plugin.proxy.getConsole().sendMessage(format);
 		}
 		if (plugin.chatSpySeesAll) {
 			for (String player : plugin.chatspies) {
 				ProxiedPlayer p = plugin.getProxy().getPlayer(player);
 				if (!p.getServer().getInfo().equals(server)) {
 					p.sendMessage(format);
 				}
 			}
 		}
 	}
 
 	public void sendGlobalMessage(ServerInfo server, String sender,
 			String format) {
 		if (plugin.logChat) {
 			plugin.proxy.getConsole().sendMessage(format);
			System.out.println(format);
 		}
 		// filter regex
		if(plugin.globalChatRegex==null){
			this.createChatConfig();
		}
 		format = format.replaceAll(plugin.globalChatRegex, "");
 		for (ServerInfo serverinfo : plugin.proxy.getServers().values()) {
 			if (!serverinfo.equals(server)) {
 				for (ProxiedPlayer data : serverinfo.getPlayers()) {
 					if (plugin.playersIgnores.containsKey(data.getName())) {
 						if (!plugin.playersIgnores.get(data.getName())
 								.contains(sender)) {
 							if (!plugin.playerdata.get(data.getName()).cleanchatting) {
 								data.sendMessage(format);
 							}
 						}
 					} else {
 						if (!plugin.playerdata.get(data.getName()).cleanchatting) {
 							data.sendMessage(format);
 						}
 					}
 
 				}
 			}
 		}
 	}
 
 	public void sendCleanGlobal(ServerInfo server, String sender, String format) {
 		for (ProxiedPlayer data : plugin.cleanChatters) {
 			if (!data.getServer().getInfo().equals(server)) {
 				if (plugin.playersIgnores.containsKey(data.getName())) {
 					if (!plugin.playersIgnores.get(data.getName()).contains(
 							sender)) {
 						data.sendMessage(format);
 					}
 				} else {
 					data.sendMessage(format);
 				}
 			}
 		}
 	}
 
 	public String deCapitalize(String input) {
 		String words[] = input.split(" ");
 		if (words.length > 0) {
 			int count = 0;
 			for (String word : words) {
 				if (count == 0) {
 					words[count] = WordUtils.capitalizeFully(word);
 					count++;
 				} else {
 					if (word.length() > 2) {
 						words[count] = word.toLowerCase();
 						count++;
 					} else if (word.equals("i")) {
 						words[count] = WordUtils.capitalize(word);
 						count++;
 					} else if (word.equalsIgnoreCase("im")
 							|| word.equals("i'm")) {
 						words[count] = WordUtils.capitalize(word);
 						count++;
 					} else if (!word.startsWith(":") && !word.equals("XD")) {
 						words[count] = word.toLowerCase();
 						count++;
 					}
 				}
 			}
 		}
 		String output = StringUtils.join(words, " ");
 		return output;
 	}
 
 	public void getPlayersInfo(String player) throws SQLException {
 		if(player==null){
 			return;
 		}
 		String server = getPlayersServer(player).getInfo().getName();
 		if(server == null){
 			
 			return;
 		}
 		String channel = null;
 		boolean mute = false;
 		boolean spying = false;
 		boolean cleanchatting = false;
 		String nickname = null;
 		if (plugin.playerdata.containsKey(player)) {
 			PlayerInfo pi = plugin.playerdata.get(player);
 			channel = pi.channel;
 			mute = pi.mute;
 			nickname = pi.nickname;
 			cleanchatting = pi.cleanchatting;
 		} else {
 			
 			ResultSet res = sql
 					.sqlQuery("SELECT * FROM BungeePlayers WHERE playername ='"
 							+ player + "'");
 			try {
 				while (res.next()) {
 					channel = res.getString("channel");
 					mute = res.getBoolean("muted");
 					spying = res.getBoolean("chat_spying");
 					nickname = res.getString("nickname");
 					cleanchatting = res.getBoolean("clean_chat");
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			if (cleanchatting) {
 				plugin.cleanChatters.add(plugin.proxy.getPlayer(player));
 			}
 			if (channel == null) {
 				if (plugin.defaultChannel == null) {
 					this.createChatConfig();
 				}
 				sql.standardQuery("UPDATE BungeePlayers SET channel = '"
 						+ plugin.defaultChannel + "' WHERE playername = '"
 						+ player + "'");
 				channel = plugin.defaultChannel;
 			}
 			if (spying) {
 				plugin.chatspies.add(player);
 			}
 			
 			plugin.playerdata.put(player, new PlayerInfo(player, channel, mute,
 					nickname, cleanchatting));
 		}
 		if (plugin.forcedChannels.containsKey(server)
 				&& plugin.forcedChannels.get(server) != null) {
 			channel = plugin.forcedChannels.get(server);
 			this.setPlayersChannel(player, channel);
 		}
 		if (!(channel.equalsIgnoreCase("global")
 				|| channel.equalsIgnoreCase("local") || channel
 					.equalsIgnoreCase("server"))) {
 			channel = plugin.defaultChannel;
 		}
 		if (nickname != null) {
 			ByteArrayOutputStream b = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(b);
 			try {
 				out.writeUTF("NicknamedPlayer");
 				out.writeUTF(player);
 				out.writeUTF(nickname);
 				out.writeBoolean(plugin.prefaceNicks);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(
 							plugin,
 							new SendPluginMessage("BungeeSuiteChat",
 									getPlayersServer(player).getInfo(), b));
 		}
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("SendPlayersInfo");
 			out.writeUTF(player);
 			out.writeUTF(channel);
 			out.writeBoolean(mute);
 			out.writeBoolean(cleanchatting);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(
 						plugin,
 						new SendPluginMessage("BungeeSuiteChat",
 								getPlayersServer(player).getInfo(), b));
 		getPlayersIgnores(player);
 
 	}
 
 	private void getPlayersIgnores(String player) throws SQLException {
 		String ignoreList = "";
 		if (plugin.playersIgnores.containsKey(player)) {
 			for (String data : plugin.playersIgnores.get(player)) {
 				ignoreList += data + "~";
 			}
 		} else {
 			ArrayList<String> list = new ArrayList<String>();
 			
 			ResultSet res = sql
 					.sqlQuery("SELECT ignoring FROM BungeeChatIgnores WHERE player = '"
 							+ player + "'");
 			while (res.next()) {
 				ignoreList += res.getString("ignoring") + "~";
 				list.add(res.getString("ignoring"));
 			}
 			res.close();
 			
 			plugin.playersIgnores.put(player, list);
 		}
 
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("SendPlayersIgnores");
 			out.writeUTF(player);
 			out.writeUTF(ignoreList);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(
 						plugin,
 						new SendPluginMessage("BungeeSuiteChat",
 								getPlayersServer(player).getInfo(), b));
 
 	}
 
 	public void nicknamePermission(String player, String nickname) {
 		if (plugin.CleanTab) {
 			nickname = cleanChat(nickname);
 		}
 		if (nickname.length() > 15) {
 			plugin.proxy.getPlayer(player).setDisplayName(
 					colorize(nickname).substring(0, 15));
 		} else {
 			plugin.proxy.getPlayer(player).setDisplayName(colorize(nickname));
 		}
 	}
 
 	public String colorize(String input) {
 		return ChatColor.translateAlternateColorCodes('&', input);
 	}
 
 	public String cleanChat(String input) {
 		input = input.replaceAll(plugin.cleanChatRegex, "");
 		return input;
 	}
 
 	public void unIgnorePlayer(String sender, String player)
 			throws SQLException {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		if (pp == null) {
 			if (!playerExists(player)) {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 
 		} else {
 			player = pp.getName();
 		}
 		if (!plugin.playersIgnores.get(sender).contains(player)) {
 			String message = plugin.getMessage("PLAYER_NOT_IGNORED");
 			message = message.replace("{player}", player);
 			sendMessage(sender, message);
 			return;
 		}
 		
 		sql.standardQuery("DELETE FROM BungeeChatIgnores WHERE player='"
 				+ sender + "' AND ignoring='" + player + "'");
 		
 		plugin.playersIgnores.get(sender).remove(player);
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("RemoveIgnorePlayer");
 			out.writeUTF(sender);
 			out.writeUTF(player);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(
 						plugin,
 						new SendPluginMessage("BungeeSuiteChat",
 								getPlayersServer(sender).getInfo(), b));
 		String message = plugin.getMessage("PLAYER_UNIGNORED");
 		message = message.replace("{player}", player);
 		sendMessage(sender, message);
 	}
 
 	public void ignorePlayer(String sender, String player) throws SQLException {
 		ProxiedPlayer pp = getClosestPlayer(player);
 		if (pp == null) {
 			if (!playerExists(player)) {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 
 		} else {
 			player = pp.getName();
 		}
 		if (plugin.playersIgnores.get(sender).contains(player)) {
 			String message = plugin.getMessage("PLAYER_ALREADY_IGNORED");
 			message = message.replace("{player}", player);
 			sendMessage(sender, message);
 			return;
 		}
 		
 		sql.standardQuery("INSERT INTO BungeeChatIgnores VALUES('" + sender
 				+ "' , '" + player + "')");
 		
 		plugin.playersIgnores.get(sender).add(player);
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("AddIgnorePlayer");
 			out.writeUTF(sender);
 			out.writeUTF(player);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(
 						plugin,
 						new SendPluginMessage("BungeeSuiteChat",
 								getPlayersServer(sender).getInfo(), b));
 		String message = plugin.getMessage("PLAYER_IGNORED");
 		message = message.replace("{player}", player);
 		sendMessage(sender, message);
 	}
 
 	public void chatSpy(String sender) throws SQLException {
 		if (plugin.chatspies.contains(sender)) {
 			plugin.chatspies.remove(sender);
 			
 			sql.standardQuery("UPDATE BungeePlayers SET chat_spying = 0 WHERE playername ='"
 					+ sender + "'");
 			
 			sendMessage(sender, "CHATSPY_DISABLED");
 		} else {
 			plugin.chatspies.add(sender);
 			
 			sql.standardQuery("UPDATE BungeePlayers SET chat_spying = 1 WHERE playername ='"
 					+ sender + "'");
 			
 			sendMessage(sender, "CHATSPY_ENABLED");
 		}
 
 	}
 
 	public void cleanPlayersChat(String sender) throws SQLException {
 		boolean c = plugin.playerdata.get(sender).cleanchatting;
 		if (c) {
 			
 			sql.standardQuery("UPDATE BungeePlayers SET clean_chat = 0 WHERE playername ='"
 					+ sender + "'");
 			
 			sendMessage(sender, "CLEANCHAT_DISABLED");
 			plugin.playerdata.get(sender).cleanchatting = false;
 			plugin.cleanChatters.remove(getPlayer(sender));
 		} else {
 			
 			sql.standardQuery("UPDATE BungeePlayers SET clean_chat = 1 WHERE playername ='"
 					+ sender + "'");
 			
 			sendMessage(sender, "CLEANCHAT_ENABLED");
 			plugin.playerdata.get(sender).cleanchatting = true;
 			plugin.cleanChatters.add(getPlayer(sender));
 		}
 
 	}
 
 	public void getForcedServerChannel(String server) {
 		if (plugin.forcedChannels.containsKey(server)) {
 			String channel = plugin.forcedChannels.get(server);
 			if (channel == null) {
 				return;
 			}
 			if (channel.equalsIgnoreCase("global")
 					|| channel.equalsIgnoreCase("server")
 					|| channel.equalsIgnoreCase("local")) {
 				ByteArrayOutputStream b = new ByteArrayOutputStream();
 				DataOutputStream out = new DataOutputStream(b);
 				try {
 					out.writeUTF("SendForcedChannel");
 					out.writeUTF(channel);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				plugin.getProxy()
 						.getScheduler()
 						.runAsync(
 								plugin,
 								new SendPluginMessage("BungeeSuiteChat",
 										getServer(server), b));
 			}
 
 		}
 
 	}
 
 	public void sendPlayerBack(String sender) {
 		if (!plugin.backLocations.containsKey(sender)) {
 			sendMessage(sender, "NO_BACK_TP");
 			return;
 		}
 		String loc = plugin.backLocations.get(sender);
 		ServerInfo server = getServer(loc.split("~")[0]);
 		ServerInfo pserver = getPlayersServer(sender).getInfo();
 		ProxiedPlayer p = getPlayer(sender);
 		if (!server.getName().equals(pserver.getName())) {
 			p.connect(server);
 		}
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TeleportPlayerToLocation");
 			out.writeUTF(sender);
 			out.writeUTF(loc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.schedule(plugin,
 						new SendPluginMessage("BungeeSuiteTp", server, b),
 						plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 		sendMessage(sender, "SENT_BACK");
 	}
 
 	public void whoIsPlayer(String sender, String player) throws SQLException {
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM `BungeePlayers` WHERE nickname LIKE '%"
 						+ player + "%'");
 		sendMessage(sender, ChatColor.DARK_AQUA
 				+ "Players with nicknames like: " + ChatColor.GREEN
 				+ colorize(player));
 		while (res.next()) {
 			String start = "-" + res.getString("playername") + " ->"
 					+ colorize(res.getString("nickname"));
 			sendMessage(sender, start);
 		}
 		res.close();
 		
 	}
 
 	public void createHomeConfig() {
 		if (!plugin.homesLoaded) {
 			String configpath = "/plugins/BungeeSuite/homes.yml";
 			plugin.homeConfig = new Config(configpath);
 			Config c = plugin.homeConfig;
 			// c.setString("Homes.Global", "");
 			// c.setString("Homes.Servers", "");
 			ArrayList<String> globalNodes = (ArrayList<String>) c
 					.getSubNodes("Homes.Defaults");
 			HashMap<String, Integer> globalLimits = new HashMap<String, Integer>();
 			for (String data : globalNodes) {
 				plugin.homeGroupsList.add(data);
 				globalLimits.put(data, c.getInt("Homes.Defaults." + data, 0));
 			}
 			plugin.homeLimits.put("Defaults", globalLimits);
 
 			ArrayList<String> serversList = (ArrayList<String>) c
 					.getSubNodes("Homes.Servers");
 			for (String server : serversList) {
 				HashMap<String, Integer> serverLimits = new HashMap<String, Integer>();
 				ArrayList<String> serverGroupNodes = (ArrayList<String>) c
 						.getSubNodes("Homes.Servers." + server);
 				for (String group : serverGroupNodes) {
 					serverLimits.put(group, c.getInt("Homes.Servers." + server
 							+ "." + group, 0));
 				}
 				plugin.homeLimits.put(server, serverLimits);
 			}
 			plugin.homesLoaded = true;
 		}
 	}
 
 	public void sendHomeGroups(String server) {
 		String groups = "";
 		for (String data : plugin.homeGroupsList) {
 			groups += data + "~";
 		}
 		if (!groups.equals("")) {
 			ByteArrayOutputStream b = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(b);
 			try {
 				out.writeUTF("ReceiveGroups");
 				out.writeUTF(groups);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(
 							plugin,
 							new SendPluginMessage("BungeeSuiteHomes",
 									getServer(server), b));
 		}
 	}
 
 	public void setPlayersHome(String player, String permissions,
 			String location, String home, String server) throws SQLException {
 		ArrayList<String> perms = new ArrayList<String>();
 		for (String perm : permissions.split("~")) {
 			perms.add(perm);
 		}
 		int allowedHomes = 0;
 		if (perms.contains("*")) {
 			allowedHomes = 2000;
 		} else {
 			for (String permission : perms) {
 				int sallowed = 0;
 				if (plugin.homeLimits.get(server).get(permission) == null) {
 					sallowed = plugin.homeLimits.get("Defaults")
 							.get(permission);
 				} else {
 					sallowed = plugin.homeLimits.get(server).get(permission);
 				}
 				if (sallowed > allowedHomes) {
 					allowedHomes = sallowed;
 				}
 			}
 		}
 		if (allowedHomes == 0) {
 			sendMessage(player, "NO_HOMES_ALLOWED");
 			return;
 		}
 		int playersCurrentHomes = playersHomeCount(player, server);
 		if ((playersCurrentHomes + 1) > allowedHomes) {
 			sendMessage(player, "NO_MORE_HOMES");
 			return;
 		}
 		String[] locs = location.split("~");
 		String world = locs[0];
 		double x = Double.parseDouble(locs[1]);
 		double y = Double.parseDouble(locs[2]);
 		double z = Double.parseDouble(locs[3]);
 		float yaw = Float.parseFloat(locs[4]);
 		float pitch = Float.parseFloat(locs[5]);
 		
 		if (homeExists(player, server, home)) {
 			sql.standardQuery("UPDATE BungeeHomes SET world ='" + world
 					+ "', x=" + x + ", y=" + y + ", z=" + z + ", yaw =" + yaw
 					+ ", pitch=" + pitch + " WHERE player = '" + player
 					+ "' AND server = '" + server + "' AND home_name = '"
 					+ home + "'");// update
 			sendMessage(player, "HOME_UPDATED");
 		} else {
 			sql.standardQuery("INSERT INTO BungeeHomes (player, home_name, server, world, x, y, z, yaw, pitch) VALUES('"
 					+ player
 					+ "','"
 					+ home
 					+ "','"
 					+ server
 					+ "','"
 					+ world
 					+ "', "
 					+ x
 					+ ", "
 					+ y
 					+ ", "
 					+ z
 					+ ", "
 					+ yaw
 					+ ", "
 					+ pitch + ")");// insert
 			sendMessage(player, "HOME_SET");
 		}
 		
 		if (home.equalsIgnoreCase("home")) {
 			sendPlayersHome(player, location, server);
 		}
 	}
 
 	private void sendPlayersHome(String player, String loc, String server) {
 		ServerInfo s = getServer(server);
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("ReceiveHome");
 			out.writeUTF(player);
 			out.writeUTF(loc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.schedule(plugin,
 						new SendPluginMessage("BungeeSuiteHomes", s, b),
 						plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 	}
 
 	public void sendPlayersHome(String player, String server)
 			throws SQLException {
 		ServerInfo s = getServer(server);
 		
 		if (!homeExists(player, server, "home")) {
 			
 			return;
 		}
 		String loc = "";
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM BungeeHomes WHERE server = '" + server
 						+ "' AND player ='" + player
 						+ "' AND home_name = 'home'");
 		while (res.next()) {
 			loc += res.getString("world") + "~";
 			loc += res.getDouble("x") + "~";
 			loc += res.getDouble("y") + "~";
 			loc += res.getDouble("z") + "~";
 			loc += res.getFloat("yaw") + "~";
 			loc += res.getFloat("pitch");
 		}
 		res.close();
 		
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("ReceiveHome");
 			out.writeUTF(player);
 			out.writeUTF(loc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.runAsync(plugin,
 						new SendPluginMessage("BungeeSuiteHomes", s, b));
 	}
 
 	private boolean homeExists(String player, String server, String home) {
 		boolean check;
 		check = sql.existanceQuery("SELECT * FROM BungeeHomes WHERE player = '"
 				+ player + "' AND server ='" + server + "' AND home_name='"
 				+ home + "'");
 		return check;
 	}
 
 	public int playersHomeCount(String player, String server)
 			throws SQLException {
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM BungeeHomes WHERE player  = '"
 						+ player + "' AND server='" + server + "'");
 		int count = 0;
 		while (res.next()) {
 			count++;
 		}
 		res.close();
 		
 		return count;
 	}
 
 	public void deletePlayersHome(String player, String home, String server)
 			throws SQLException {
 		
 		if (!homeExists(player, server, home)) {
 			if (getServer(server) == null) {
 				sendMessage(player, "HOME_DOES_NOT_EXIST");
 				
 				return;
 			} else {
 				server = home;
 				home = "home";
 				if (!homeExists(player, server, home)) {
 					sendMessage(player, "HOME_DOES_NOT_EXIST");
 					
 					return;
 				}
 			}
 		}
 		sql.standardQuery("DELETE FROM BungeeHomes WHERE player ='" + player
 				+ "' AND home_name ='" + home + "' AND server ='" + server
 				+ "'");
 		
 		sendMessage(player, "HOME_DELETED");
 	}
 
 	public void listPlayersHomes(String player) throws SQLException {
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM BungeeHomes WHERE player = '" + player
 						+ "'");
 		HashMap<String, ArrayList<String>> homesList = new HashMap<String, ArrayList<String>>();
 		while (res.next()) {
 			String homename = res.getString("home_name");
 			String servername = res.getString("server");
 			if (!homesList.containsKey(servername)) {
 				homesList.put(servername, new ArrayList<String>());
 			}
 			homesList.get(servername).add(homename);
 		}
 		res.close();
 		
 		sendMessage(player, ChatColor.AQUA + "Your homes:");
 		for (String data : homesList.keySet()) {
 			ArrayList<String> homes = homesList.get(data);
 			Collections.sort(homes);
 			String server = ChatColor.GOLD + data + ": " + ChatColor.WHITE;
 			for (String home : homes) {
 				server += " " + home + ",";
 			}
 			sendMessage(player, server.substring(0, server.length() - 1));
 		}
 	}
 
 	public void sendPlayerToHome(String player, String homename, String server)
 			throws SQLException {
 		
 		if (!homeExists(player, server, homename)) {
 			if (getServer(server) == null) {
 				sendMessage(player, "HOME_DOES_NOT_EXIST");
 				
 				return;
 			} else {
 				server = homename;
 				homename = "home";
 				if (!homeExists(player, server, homename)) {
 					sendMessage(player, "HOME_DOES_NOT_EXIST");
 					
 					return;
 				}
 			}
 		}
 		ServerInfo s = getServer(server);
 		ProxiedPlayer p = getPlayer(player);
 		String loc = "";
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM BungeeHomes WHERE server = '" + server
 						+ "' AND player ='" + player + "' AND home_name = '"
 						+ homename + "'");
 		while (res.next()) {
 			loc += res.getString("world") + "~";
 			loc += res.getDouble("x") + "~";
 			loc += res.getDouble("y") + "~";
 			loc += res.getDouble("z") + "~";
 			loc += res.getFloat("yaw") + "~";
 			loc += res.getFloat("pitch");
 		}
 		res.close();
 		
 		if (!p.getServer().getInfo().getName().equalsIgnoreCase(s.getName())) {
 			p.connect(s);
 		}
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TeleportPlayerToHome");
 			out.writeUTF(player);
 			out.writeUTF(loc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		plugin.getProxy()
 				.getScheduler()
 				.schedule(plugin,
 						new SendPluginMessage("BungeeSuiteHomes", s, b),
 						plugin.DELAY_TIME, TimeUnit.MILLISECONDS);
 
 	}
 
 	public void setPlayersHome(String player, String location, String home,
 			String server) throws SQLException {
 		if (!playerExists(player)) {
 			return;
 		}
 		String[] locs = location.split("~");
 		String world = locs[0];
 		double x = Double.parseDouble(locs[1]);
 		double y = Double.parseDouble(locs[2]);
 		double z = Double.parseDouble(locs[3]);
 		float yaw = Float.parseFloat(locs[4]);
 		float pitch = Float.parseFloat(locs[5]);
 		
 		if (homeExists(player, server, home)) {
 			
 			return;
 		}
 		if (homeExists(player, server, home)) {
 			sql.standardQuery("UPDATE BungeeHomes SET world ='" + world
 					+ "', x=" + x + ", y=" + y + ", z=" + z + ", yaw =" + yaw
 					+ ", pitch=" + pitch + " WHERE player = '" + player
 					+ "' AND server = '" + server + "' AND home_name = '"
 					+ home + "'");// update
 		} else {
 			sql.standardQuery("INSERT INTO BungeeHomes (player, home_name, server, world, x, y, z, yaw, pitch) VALUES('"
 					+ player
 					+ "','"
 					+ home
 					+ "','"
 					+ server
 					+ "','"
 					+ world
 					+ "', "
 					+ x
 					+ ", "
 					+ y
 					+ ", "
 					+ z
 					+ ", "
 					+ yaw
 					+ ", "
 					+ pitch + ")");// insert
 		}
 		
 	}
 
 	public void reloadHomes(String player) {
 		plugin.homeConfig = null;
 		plugin.homeGroupsList.clear();
 		plugin.homeLimits.clear();
 		plugin.homesLoaded = false;
 		this.createHomeConfig();
 		for (String data : plugin.proxy.getServers().keySet()) {
 			this.sendHomeGroups(data);
 		}
 		sendMessage(player, "Homes reloaded");
 	}
 
 	public void createBanConfig() {
 		if (!plugin.bansLoaded) {
 			plugin.bans = true;
 			String configpath = "/plugins/BungeeSuite/bans.yml";
 			plugin.bansConfig = new Config(configpath);
 			Config b = plugin.bansConfig;
 			plugin.bans = b.getBoolean("Bans.Enabled", true);
 			plugin.broadcastBans = b.getBoolean("Bans.BroadcastBans", true);
 			plugin.detectAltAccs = b.getBoolean("Bans.DetectAltAccounts", true);
 			plugin.showAltAccountsIfBanned = b.getBoolean(
 					"Bans.ShowAltAccountsOnlyIfBanned", false);
 			plugin.bansLoaded = true;
 		}
 		if (plugin.firstLogin && plugin.bans) {
 			plugin.getProxy()
 					.getScheduler()
 					.schedule(
 							plugin,
 							new PlayerBanCheckDelay(plugin.getProxy()
 									.getPlayers().iterator().next(), plugin),
 							400, TimeUnit.MILLISECONDS);
 			plugin.firstLogin = false;
 			return;
 		}
 	}
 
 	public void getSimilarIps(String player, String ip) throws SQLException {
 		String players = "";
 		boolean banonly = false;
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT playername from BungeePlayers WHERE ipaddress = '"
 						+ ip + "'");
 		while (res.next()) {
 			String p = res.getString("playername");
 			if (!p.equals(player)) {
 				if (playerIsBanned(p)) {
 					banonly = true;
 					players += ChatColor.DARK_RED + "[Banned] "
 							+ ChatColor.GREEN + p + ",";
 				} else {
 					players += ChatColor.GREEN + p + ",";
 				}
 			}
 		}
 		res.close();
 		
 		if (!players.equals("")) {
 			if (!plugin.showAltAccountsIfBanned || banonly) {
 				players = players.substring(0, players.length() - 1);
 				String message = plugin.getMessage("SAME_IP");
 				message = message.replace("{player}", player);
 				message = message.replace("{list}", players);
 				message = message.replace("{ip}", ip);
 				System.out.println(message);
 				ByteArrayOutputStream b = new ByteArrayOutputStream();
 				DataOutputStream out = new DataOutputStream(b);
 				try {
 					out.writeUTF("AltAccount");
 					out.writeUTF(message);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				for (ServerInfo data : plugin.proxy.getServers().values()) {
 					if (!data.getPlayers().isEmpty()) {
 						plugin.getProxy()
 								.getScheduler()
 								.runAsync(
 										plugin,
 										new SendPluginMessage(
 												"BungeeSuiteBans", data, b));
 					}
 				}
 			}
 		}
 
 	}
 
 	public void reloadBans(String sender) {
 		plugin.bansConfig = null;
 		plugin.bansLoaded = false;
 		this.createBanConfig();
 		sendMessage(sender, "Bans reloaded");
 	}
 
 	public void tempMutePlayer(String sender, String player, int time)
 			throws SQLException {
 		this.mutePlayer(sender, player);
 		plugin.proxy.getScheduler().schedule(plugin,
 				new TempBan(plugin, player, sender), time, TimeUnit.MINUTES);
 		plugin.tempMutes.add(player);
 	}
 
 	public boolean IPExists(String ip) {
 		
 		boolean check = false;
 		check = sql
 				.existanceQuery("SELECT * FROM BungeePlayers WHERE ipaddress = '"
 						+ ip + "'");
 		
 		return check;
 	}
 
 	public boolean ipIsBanned(String ip) {
 		
 		boolean check = false;
 		check = sql
 				.existanceQuery("SELECT * FROM `BungeeBans` b INNER JOIN BungeePlayers p WHERE b.player = p.playername AND ipaddress ='"
 						+ ip + "' AND type ='ipban'");
 		
 		return check;
 	}
 
 	public ArrayList<String> getIpsPlayers(String ip) throws SQLException {
 		ArrayList<String> players = new ArrayList<String>();
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT playername from BungeePlayers WHERE ipaddress ='"
 						+ ip + "'");
 		while (res.next()) {
 			players.add(res.getString("playername"));
 		}
 		res.close();
 		
 		return players;
 	}
 
 	public void ipBanPlayer(String sender, String player, String msg)
 			throws SQLException {
 		ProxiedPlayer p = this.getClosestPlayer(player);
 		String ip = null;
 		if (p != null) {
 			ip = p.getName();
 		} else {
 			ip = player;
 		}
 		if (!IPExists(ip)) {
 			if (playerExists(ip)) {
 				ip = this.getPlayerIP(ip);
 			} else {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 		}
 		if (ipIsBanned(ip)) {
 			sendMessage(sender, "PLAYER_ALREADY_BANNED");
 			return;
 		}
 		ArrayList<String> players = getIpsPlayers(ip);
 		for (String p2 : players) {
 			if (this.playerIsBanned(p2)) {
 				
 				sql.standardQuery("UPDATE BungeeBans SET type='ipban' WHERE player = '"
 						+ p2 + "'");
 			} else {
 				
 				ProxiedPlayer pp = plugin.proxy.getPlayer(p2);
 				if (pp != null) {
 					if (msg.equalsIgnoreCase("")) {
 						pp.disconnect(plugin.getMessage("IPBAN_PLAYER"));
 					} else {
 						pp.disconnect(plugin.getMessage(
 								"BAN_PLAYER_MESSAGE_PREFIX").replace(
 								"{message}", msg));
 					}
 				}
 				if (msg.equalsIgnoreCase("")) {
 					this.sendBroadcast(plugin
 							.getMessage("BAN_PLAYER_BROADCAST").replace(
 									"{player}", ip));
 				} else {
 					this.sendBroadcast(plugin
 							.getMessage("IPBAN_PLAYER_BROADCAST_MESSAGE_PREFIX")
 							.replace("{player}", ip).replace("{message}", msg));
 				}
 				sql.standardQuery("INSERT INTO BungeeBans VALUES('" + p2
 						+ "','" + sender + "','" + msg
 						+ "','ipban',CURRENT_DATE(),NULL)");
 			}
 		}
 		
 	}
 
 	public void unIpBanPlayer(String sender, String player, String msg)
 			throws SQLException {
 		String ip = player;
 		if (!IPExists(ip)) {
 			if (playerExists(ip)) {
 				ip = this.getPlayerIP(ip);
 			} else {
 				sendMessage(sender, "PLAYER_DOES_NOT_EXIST");
 				return;
 			}
 		}
 		if (!ipIsBanned(ip)) {
 			sendMessage(sender, "PLAYER_NOT_BANNED");
 			return;
 		} else {
 			
 			sql.standardQuery("DELETE FROM BungeeBans B INNER JOIN BungeePlayers P ON B.player = P.playername WHERE ipadress ='"
 					+ ip + "'");
 			
 			this.sendBroadcast(plugin.getMessage("PLAYER_UNBANNED").replace(
 					"{player}", ip));
 		}
 	}
 
 	public void sendWorldSpawns(ServerInfo serverInfo) throws SQLException {
 		String loc = "";
 		
 		ResultSet res = sql
 				.sqlQuery("SELECT * FROM BungeeSpawns WHERE spawnname LIKE '"
 						+ serverInfo.getName() + "%' AND server = '"
 						+ serverInfo.getName() + "'");
 
 		while (res.next()) {
 			loc += res.getString("world") + "~";
 			loc += res.getDouble("x") + "~";
 			loc += res.getDouble("y") + "~";
 			loc += res.getDouble("z") + "~";
 			loc += res.getFloat("yaw") + "~";
 			loc += res.getFloat("pitch");
 			ByteArrayOutputStream b = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(b);
 			try {
 				out.writeUTF("WorldSpawn");
 				out.writeUTF(loc);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			plugin.getProxy()
 					.getScheduler()
 					.runAsync(
 							plugin,
 							new SendPluginMessage("BungeeSuiteSpawn",
 									serverInfo, b));
 			loc = "";
 		}
 		res.close();
 		
 
 	}
 
 	public void createSpawnConfig() {
 		if (plugin.spawnConfig == null) {
 			String configpath = "/plugins/BungeeSuite/spawns.yml";
 			plugin.spawnConfig = new Config(configpath);
 			Config c = plugin.spawnConfig;
 			plugin.newspawn = c.getBoolean("Spawn new players at newspawn",
 					false);
 		}
 	}
 
 	public void checkPlayerBans(String sender, String name) throws SQLException {
 		if (!playerExists(name)) {
 			sendMessage(sender, "PLAYER_NOT_EXIST");
 			return;
 		}
 		if (plugin.utils.playerIsBanned(name)) {
 			String reason = plugin.utils.getBanReason(name);
 			double hours = 0;
 			if (plugin.utils.playerBanIsTemp(name)) {
 				Calendar bannedTill = plugin.utils.playerBanTempDate(name);
 				Calendar now = Calendar.getInstance();
 				if (now.after(bannedTill)) {
 					plugin.utils.removeTempBan(name);
 					sendMessage(sender, "PLAYER_NOT_BANNED");
 					return;
 				}
 				hours = ((((bannedTill.getTimeInMillis() - now
 						.getTimeInMillis()) / 1000) / 60) / 60);
 			}
 			String message = plugin.getMessage("CHECK_PLAYER_BAN");
 			if (reason.equalsIgnoreCase("") || reason == null) {
 				reason = "<none>";
 			}
 			message = message.replace("{message}", reason);
 			if (hours == 0) {
 				message = message.replace("{time}", "forever");
 			} else {
 				message = message.replace("{time}", hours + " hours");
 			}
 			message = message.replace("{name}", name);
 			sendMessage(sender, message);
 		} else {
 			sendMessage(sender, "PLAYER_NOT_BANNED");
 		}
 	}
 
 	public void toggleTeleports(String player) {
 		if(plugin.denyTeleport.contains(player)){
 			plugin.denyTeleport.remove(player);
 			sendMessage(player,"TELEPORT_TOGGLE_OFF");
 		}else{
 			plugin.denyTeleport.add(player);
 			sendMessage(player,"TELEPORT_TOGGLE_ON");
 		}
 		
 	}
 
 }
