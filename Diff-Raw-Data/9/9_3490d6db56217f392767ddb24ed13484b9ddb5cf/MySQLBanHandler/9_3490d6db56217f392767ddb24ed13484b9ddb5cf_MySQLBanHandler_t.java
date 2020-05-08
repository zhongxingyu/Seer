 package com.cole2sworld.ColeBans.handlers;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.Vector;
 import com.cole2sworld.ColeBans.GlobalConf;
 import com.cole2sworld.ColeBans.Main;
 import com.cole2sworld.ColeBans.framework.PlayerAlreadyBannedException;
 import com.cole2sworld.ColeBans.framework.PlayerNotBannedException;
 import com.unibia.simplemysql.SimpleMySQL;
 
 public class MySQLBanHandler extends BanHandler {
 	private SimpleMySQL sqlHandler;
 	/**
 	 * Creates a new MySQLBanHandler using a database with the given settings
 	 * @param username - The username to log into the server
 	 * @param password - The password to log into the server
 	 * @param host - The hostname to log into the server
 	 * @param port - The port to log into the server
 	 * @param prefix - The table prefix
 	 * @param db - The database to use
 	 */
	public MySQLBanHandler(String username, String password, String host, String prefix, String db) {
 		System.out.println(GlobalConf.logPrefix+"[MySQLBanHandler] Opening connection");
 		long oldtime = System.currentTimeMillis();
 		sqlHandler = SimpleMySQL.getInstance();
 		Main.debug("Got instance");
 		sqlHandler.enableReconnect();
 		Main.debug("Enabled reconnect");
 		sqlHandler.setReconnectNumRetry(25);
 		Main.debug("Reconnect retries set to 25");
 		sqlHandler.connect(host, username, password);
		Main.debug("Connected. Setting database");
		sqlHandler.use(db);
 		long newtime = System.currentTimeMillis();
 		System.out.println(GlobalConf.logPrefix+"[MySQLBanHandler] Done. Took "+(newtime-oldtime)+" ms.");
 	}
 	/**
 	 * Sanitizes the input to be safe for usage in a SQL query.
 	 * @param workset The string to process
 	 * @return The sanitized string
 	 */
 	public static String addSlashes(String workset) {
 		Main.debug("Sanitizing "+workset);
 		StringBuilder sanitizer = new StringBuilder();
 		for (int i = 0; i<workset.length(); i++) {
 			if (!isSQLSpecialCharacter(workset.charAt(i))) {
 				sanitizer.append("\\");
 				sanitizer.append(workset.charAt(i));
 			}
 			else {
 				sanitizer.append(workset.charAt(i));
 			}
 		}
 		Main.debug("Done - workset is now "+workset);
 		return sanitizer.toString();
 	}
 	/**
 	 * @param charAt Character to check
 	 * @return If escaping this character in MySQL will cause strange things to happen
 	 */
 	public static boolean isSQLSpecialCharacter(Character charAt) {
 		String workset = charAt.toString();
 		if ("0".equalsIgnoreCase(workset)) return true;
 		else if ("b".equalsIgnoreCase(workset)) return true;
 		else if ("n".equalsIgnoreCase(workset)) return true;
 		else if ("r".equalsIgnoreCase(workset)) return true;
 		else if ("t".equalsIgnoreCase(workset)) return true;
 		else if ("z".equalsIgnoreCase(workset)) return true;
 		else return false;
 	}
 
 	@Override
 	public void banPlayer(String player, String reason, String admin) throws PlayerAlreadyBannedException {
 		if (isPlayerBanned(player, admin)) throw new PlayerAlreadyBannedException(player+" is already banned!");
 		String tbl = GlobalConf.Sql.prefix+"perm";
 		if (sqlHandler.checkTable(tbl)) {
 			sqlHandler.query("INSERT INTO "+tbl+" (" +
 					"username, " +
 					"reason" +
 					") VALUES (" +
 					"'"+addSlashes(player)+"', " +
 					"'"+addSlashes(reason)+"'"+
 					");");
 		}
 		else {
 			sqlHandler.query("CREATE  TABLE `"+GlobalConf.Sql.db+"`.`"+tbl+"` (" +
 					"`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ," +
 					"`username` VARCHAR(255) NULL ," +
 					"`reason` VARCHAR(255) NULL ," +
 					"PRIMARY KEY (`id`) );");
 			sqlHandler.query("ALTER TABLE `"+GlobalConf.Sql.db+"`.`"+tbl+"`"+
 					"ADD INDEX `NAMEINDEX` (`username` ASC);");
 			banPlayer(player, reason, admin);
 		}
 	}
 
 	@Override
 	public void tempBanPlayer(String player, long primTime, String admin) throws PlayerAlreadyBannedException, UnsupportedOperationException {
 		if (!GlobalConf.allowTempBans) throw new UnsupportedOperationException("Temp bans are disabled!");
 		if (isPlayerBanned(player, admin)) throw new PlayerAlreadyBannedException(player+" is already banned!");
 		Long time = System.currentTimeMillis()+((primTime*60)*1000);
 		String tbl = GlobalConf.Sql.prefix+"temp";
 		if (sqlHandler.checkTable(tbl)) {
 			sqlHandler.query("INSERT INTO `"+GlobalConf.Sql.db+"`.`"+tbl+"` (" +
 					"username, " +
 					"time" +
 					") VALUES (" +
 					"'"+addSlashes(player)+"', " +
 					"'"+time+"'"+
 					");");
 		}
 		else {
 			sqlHandler.query("CREATE  TABLE `"+GlobalConf.Sql.db+"`.`"+tbl+"` (" +
 					"`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ," +
 					"`username` VARCHAR(255) NULL ," +
 					"`time` VARCHAR(255) NULL ," +
 					"PRIMARY KEY (`id`) );");
 			sqlHandler.query("ALTER TABLE `"+GlobalConf.Sql.db+"`.`"+tbl+"`"+
 					"ADD INDEX `NAMEINDEX` (`username` ASC);");
 			tempBanPlayer(player, primTime, admin);
 		}
 	}
 
 	@Override
 	public void unbanPlayer(String player, String admin) throws PlayerNotBannedException {
 		BanData bd = getBanData(player, admin);
 		if (bd.getType() == Type.PERMANENT)  {
 			String tbl = GlobalConf.Sql.prefix+"perm";
 			if (sqlHandler.checkTable(tbl)) {
 				sqlHandler.query("DELETE FROM `"+GlobalConf.Sql.db+"`.`"+tbl+"` WHERE username='"+addSlashes(player)+"';");
 				return;
 			}
 		}
 		else if (bd.getType() == Type.TEMPORARY) {
 			String tbl = GlobalConf.Sql.prefix+"temp";
 			if (sqlHandler.checkTable(tbl)) {
 				sqlHandler.query("DELETE FROM `"+GlobalConf.Sql.db+"`.`"+tbl+"` WHERE username='"+addSlashes(player)+"';");
 				return;
 			}
 		}
 		throw new PlayerNotBannedException(player+" is not banned!");
 	}
 
 	@Override
 	public boolean isPlayerBanned(String player, String admin) {
 		return (getBanData(player, admin).getType()) != Type.NOT_BANNED;
 	}
 
 
 	@Override
 	public BanData getBanData(String player, String admin) {
 		String tbl = GlobalConf.Sql.prefix+"perm";
 		if (sqlHandler.checkTable(tbl)) {
 			ResultSet reasonResult = sqlHandler.query("SELECT reason FROM `"+GlobalConf.Sql.db+"`.`"+tbl+"` WHERE username='"+addSlashes(player)+"';");
 			boolean results = false;
 			try {
 				results = reasonResult.first();
 			} catch (SQLException e) {}
 			if (results) {
 				String reason = "";
 				try {
 					reason = reasonResult.getString("reason");
 				} catch (SQLException e) {}
 				if (!reason.isEmpty()) {
 					return new BanData(player, reason);
 				}
 			}
 		}
 		String tblB = GlobalConf.Sql.prefix+"temp";
 		if (sqlHandler.checkTable(tblB)) {
 			ResultSet reasonResultB = sqlHandler.query("SELECT time FROM `"+GlobalConf.Sql.db+"`.`"+tblB+"` WHERE username='"+addSlashes(player)+"';");
 			boolean resultsB = false;
 			try {
 				resultsB = reasonResultB.first();
 			} catch (SQLException e) {}
 			if (resultsB) {
 				long time = -1L;
 				try {
 					time = reasonResultB.getLong("time");
 					if (time <= System.currentTimeMillis()) {
 						if (sqlHandler.checkTable(tblB)) {
 							sqlHandler.query("DELETE FROM `"+GlobalConf.Sql.db+"`.`"+tbl+"` WHERE username='"+addSlashes(player)+"';");
 						}
 						return new BanData(player);
 					}
 				}
 				catch (SQLException e) {}
 				if (time > -1) {
 					return new BanData(player, time);
 				}
 			}	
 		}
 		return new BanData(player);
 	}
 
 	@Override
 	public void onDisable() {
 		System.out.println(GlobalConf.logPrefix+"[MySQLBanHandler] Closing connection");
 		long oldtime = System.currentTimeMillis();
 		sqlHandler.close();
 		long newtime = System.currentTimeMillis();
 		System.out.println(GlobalConf.logPrefix+"[MySQLBanHandler] Done. Took "+(newtime-oldtime)+" ms.");
 	}
 	public static BanHandler onEnable() {
 		Map<String, String> data = Main.getBanHandlerInitArgs();
		return new MySQLBanHandler(data.get("username"), data.get("password"), data.get("host"), data.get("prefix"), data.get("db"));
 	}
 	@Override
 	public void convert(BanHandler handler) {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public Vector<BanData> dump(String admin) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public Vector<String> listBannedPlayers(String admin) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 }
