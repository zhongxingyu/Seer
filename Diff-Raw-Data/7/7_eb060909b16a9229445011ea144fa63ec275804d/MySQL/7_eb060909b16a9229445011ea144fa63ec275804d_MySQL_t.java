 package com.cyprias.Lifestones.Databases;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.command.CommandSender;
 
 import com.cyprias.Lifestones.Attunements;
 import com.cyprias.Lifestones.Config;
 import com.cyprias.Lifestones.Database;
 import com.cyprias.Lifestones.Attunements.Attunement;
 import com.cyprias.Lifestones.Lifestones.lifestoneLoc;
 
 public class MySQL {
 
 	private Database database;
 	private String pluginPath;
 
 	public MySQL(Database database, File dataFolder) {
 		this.database = database;
 		
 	}
 
 	public static Connection getSQLConnection() {
 		try {
 			Connection con;
 			if (Config.sqlURL.contains("mysql")) {
 				con = DriverManager.getConnection(Config.sqlURL, Config.sqlUsername, Config.sqlPassword);
 			} else {
 				con = DriverManager.getConnection(Config.sqlURL);
 			}
 
 			return con;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public boolean tableExists(String tableName) {
 		boolean exists = false;
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			Connection con = getSQLConnection();
 
 			String query;
 
 			PreparedStatement statement = con.prepareStatement("show tables like '" + tableName + "'");
 			ResultSet result = statement.executeQuery();
 
 			result.last();
 			if (result.getRow() != 0) {
 
 				exists = true;
 			}
 
 			// //////
 			result.close();
 			statement.close();
 			con.close();
 
 		} catch (ClassNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return exists;
 	}
 	
 	public void createTables() {
 		String query;
 
 		Connection con = getSQLConnection();
 		PreparedStatement statement = null;
 
 		try {
 
 			if (tableExists(Config.sqlPrefix + "Lifestones") == false) {
 				query = "CREATE TABLE `" + Config.sqlPrefix + "Lifestones` (`id` INT PRIMARY KEY AUTO_INCREMENT, `world` VARCHAR(32) NOT NULL, `x` INT NOT NULL, `y` INT NOT NULL, `z` INT NOT NULL) ENGINE = InnoDB";
 				statement = con.prepareStatement(query);
 				statement.executeUpdate();
 			}
 
 			
 			if (tableExists(Config.sqlPrefix + "Attunements") == false) {
 				query = "CREATE TABLE `"+Config.sqlPrefix + "Attunements` (`id` INT PRIMARY KEY AUTO_INCREMENT, `player` VARCHAR(32) NOT NULL UNIQUE, `world` VARCHAR(32) NOT NULL, `x` DOUBLE NOT NULL, `y` DOUBLE NOT NULL, `z` DOUBLE NOT NULL, `yaw` FLOAT NOT NULL, `pitch` FLOAT NOT NULL) ENGINE = InnoDB";
 				statement = con.prepareStatement(query);
 				statement.executeUpdate();
 			}
 
 
 			// statement.close();
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void saveLifestone(String bWorld, int bX, int bY, int bZ) {//Block block
 		String table = Config.sqlPrefix + "Lifestones";
 
 		Connection con = getSQLConnection();
 		PreparedStatement statement = null;
 		int success = 0;
 
 		String query = "select * from "+table+" where `world` LIKE ? AND `x` LIKE ?  AND `y` LIKE ?  AND `z` LIKE ? ";
 
 		try {
 			statement = con.prepareStatement(query);
 			statement.setString(1, bWorld);
 			statement.setInt(2, bX);
 			statement.setInt(3, bY);
 			statement.setInt(4, bZ);
 	
 			ResultSet result = statement.executeQuery();
 			while (result.next()) {
 				success = 1;
 				break;
 			}
 			
 			statement.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		if (success > 0){
 			System.out.println("mysql Lifestone already in DB!");
 			return;
 		}
 			
 		
 		if (success == 0) {
 			query = "INSERT INTO `" + table + "` (`world`, `x`, `y`, `z`) VALUES (?, ?, ?, ?);";
 			try {
 				statement = con.prepareStatement(query);
 				statement.setString(1, bWorld);
 				statement.setInt(2, bX);
 				statement.setInt(3, bY);
 				statement.setInt(4, bZ);
 				
 				success = statement.executeUpdate();
 				statement.close();
 				con.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public void loadLifestones() {
 		String table = Config.sqlPrefix + "Lifestones";
 
 		Connection con = getSQLConnection();
 		PreparedStatement statement = null;
 		int success = 0;
 
 		String query = "select * from "+table;
 
 		try {
 			statement = con.prepareStatement(query);
 			
 			ResultSet result = statement.executeQuery();
 			while (result.next()) {
 				database.plugin.regsterLifestone(new lifestoneLoc(result.getString("world"), result.getInt("x"), result.getInt("y"), result.getInt("z")));
 				
 			}
 			
 			statement.close();
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	public void removeLifestone(String bWorld, int bX, int bY, int bZ) {
 		String table = Config.sqlPrefix + "Lifestones";
 		int success = 0;
 		Connection con = getSQLConnection();
 		PreparedStatement statement = null;
 		String query = "DELETE from `"+table+"` where `world` LIKE ? AND `x` LIKE ? AND `y` LIKE ? AND `z` LIKE ?";
 		try {
 			statement = con.prepareStatement(query);
 			statement.setString(1, bWorld);
 			statement.setInt(2, bX);
 			statement.setInt(3, bY);
 			statement.setInt(4, bZ);
 			
 			success = statement.executeUpdate();
 			
 			statement.close();
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		
 		System.out.println("removeLifestone: " +success );
 		
 	}
 	
 	public void saveAttunment(String player, String bWorld, double x, double y, double z,  float yaw, float pitch) {
 		String table = Config.sqlPrefix + "Attunements";
 
 		Connection con = getSQLConnection();
 		PreparedStatement statement = null;
 		int success = 0;
 
 		String query = "UPDATE `"+table+"` SET `world` = ?, `x` = ?, `y` = ?, `z` = ?, `yaw` = ?, `pitch` = ? WHERE `player` = ?";
 
 		try {
 			statement = con.prepareStatement(query);
 			statement.setString(1, bWorld);
 			statement.setDouble(2, x);
 			statement.setDouble(3, y);
 			statement.setDouble(4, z);
 			statement.setFloat(5, yaw);
 			statement.setFloat(6, pitch);
 			statement.setString(7, player);
 	
 			success = statement.executeUpdate();
 			
 			statement.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		if (success > 0){
 			return;
 		}
 			
 		
 		if (success == 0) {
 			query = "insert into `"+table+"` (player, world, x,y,z,yaw, pitch) values (?, ?, ?, ?,?,?,?)";
 			try {
 				statement = con.prepareStatement(query);
 				statement.setString(1, player);
 				statement.setString(2, bWorld);
 				
 				statement.setDouble(3, x);
 				statement.setDouble(4, y);
 				statement.setDouble(5, z);
 				statement.setFloat(6, yaw);
 				statement.setFloat(7, pitch);
 				
 				success = statement.executeUpdate();
 				statement.close();
 				con.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 
 		try {
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void loadAttunements() {
 		String table = Config.sqlPrefix + "Attunements";
 
 		Connection con = getSQLConnection();
 		PreparedStatement statement = null;
 		int success = 0;
 
 		String query = "select * from " + table;
 
 		try {
 			statement = con.prepareStatement(query);
 			
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				Attunements.players.put(rs.getString("player"), new Attunement(rs.getString("player"), rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")));
 				
 				
 			}
 			
 			statement.close();
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
