 package com.cyprias.Lifestones.Databases;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.block.Block;
 
 import com.cyprias.Lifestones.Attunements;
 import com.cyprias.Lifestones.Config;
 import com.cyprias.Lifestones.Database;
 import com.cyprias.Lifestones.Attunements.Attunement;
 import com.cyprias.Lifestones.Lifestones.lifestoneLoc;
 
 public class SQLite {
 	private Database database;
 	private String pluginPath;
 
 	private String sqlDB;
 
 	public SQLite(Database database, File file) {
 		this.database = database;
 		this.pluginPath = file.getPath() + file.separator;
 
 		sqlDB = "jdbc:sqlite:" + pluginPath + "database.sqlite";
		
 	}
 
 	public boolean tableExists(String tableName) {
 		boolean exists = false;
 		String query;
 		
 		
 		try {
 			Class.forName("java.sql.Driver");
 	
 			
 			Connection con = DriverManager.getConnection(sqlDB);
 			Statement stat = con.createStatement();
 			ResultSet result = stat.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';");
 
 			while (result.next()) {
 				exists = true;
 				break;
 			}
 
 			result.close();
 			con.close();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return exists;
 	}
 
 	public void createTables() {
 		try {
 			Connection con = DriverManager.getConnection(sqlDB);
 			Statement stat = con.createStatement();
 
 			if (tableExists("Lifestones") == false) {
 				stat.executeUpdate("CREATE TABLE `Lifestones` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `world` VARCHAR(32) NOT NULL, `x` INT NOT NULL, `y` INT NOT NULL, `z` INT NOT NULL)");
 			}
 			if (tableExists("Attunements") == false) {
 				stat.executeUpdate("CREATE TABLE `Attunements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `player` VARCHAR(32) NOT NULL UNIQUE, `world` VARCHAR(32) NOT NULL, `x` DOUBLE NOT NULL, `y` DOUBLE NOT NULL, `z` DOUBLE NOT NULL, `yaw` FLOAT NOT NULL, `pitch` FLOAT NOT NULL)");
 				//player, world, x,y,z,yaw, pitch
 			}
 			
 			stat.close();
 			con.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void sqliteTest1() {
 		try {
 
 			Connection con = DriverManager.getConnection("jdbc:sqlite:" + pluginPath + "test.db");
 
 			Statement stat = con.createStatement();
 
 			stat.executeUpdate("drop table if exists people;");
 			stat.executeUpdate("create table people (name, occupation);");
 			PreparedStatement prep = con.prepareStatement("insert into people values (?, ?);");
 
 			prep.setString(1, "Gandhi");
 			prep.setString(2, "politics");
 			prep.addBatch();
 			prep.setString(1, "Turing");
 			prep.setString(2, "computers");
 			prep.addBatch();
 			prep.setString(1, "Wittgenstein");
 			prep.setString(2, "smartypants");
 			prep.addBatch();
 
 			con.setAutoCommit(false);
 			prep.executeBatch();
 			con.setAutoCommit(true);
 
 			ResultSet rs = stat.executeQuery("select * from people;");
 			while (rs.next()) {
 				System.out.println("name = " + rs.getString("name"));
 				System.out.println("job = " + rs.getString("occupation"));
 			}
 			rs.close();
 			con.close();
 
 			// Statement stat = conn.createStatement();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	public void loadAttunements() {
 		
 		try {
 			Connection con = DriverManager.getConnection(sqlDB);
 			Statement stat = con.createStatement();
 			ResultSet rs = stat.executeQuery("select * from Attunements;");
 			while (rs.next()) {
 				Attunements.players.put(rs.getString("player"), new Attunement(rs.getString("player"), rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")));
 			}
 			rs.close();
 			con.close();
 			
 			
 			
 		} catch (SQLException e) {e.printStackTrace();}
 		
 	}
 	
 	public void loadLifestones() {
 		
 		try {
 			Connection con = DriverManager.getConnection(sqlDB);
 			Statement stat = con.createStatement();
 			ResultSet rs = stat.executeQuery("select * from Lifestones;");
 			while (rs.next()) {
 				database.plugin.regsterLifestone(new lifestoneLoc(rs.getString("world"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
 			}
 			rs.close();
 			con.close();
 
 		} catch (SQLException e) {e.printStackTrace();}
 		
 	}
 	
 	public void saveLifestone(String bWorld, int bX, int bY, int bZ) {//Block block
 		//String bWorld = block.getWorld().getName();
 		//int bX, bY, bZ;
 		//bX = block.getX();
 		//bY = block.getY();
 		//bZ = block.getZ();
 		String table = "Lifestones";
 		
 		try {
 			Connection con = DriverManager.getConnection(sqlDB);
 
 			Statement stat = con.createStatement();
 			ResultSet rs = stat.executeQuery("select * from `"+table+"` where `world` LIKE "+bWorld+" AND `x` LIKE " + bX + " AND `y` LIKE " + bY+ " AND `z` LIKE "+bZ);
 			while (rs.next()) {
 				if (rs.getString("world").equalsIgnoreCase(bWorld)){
 					
 					if (rs.getInt("x") == bX && rs.getInt("y") == bY && rs.getInt("z") == bZ){
 						System.out.println("Lifestone already in DB!");
 						rs.close();
 						stat.close();
 						con.close();
 						return;
 					}
 					
 				}
 			}
 			
 			PreparedStatement prep = con.prepareStatement("insert into `"+table+"` (world, x, y, z) values (?, ?, ?, ?)");
 			prep.setString(1, bWorld);
 			prep.setInt(2, bX);
 			prep.setInt(3, bY);
 			prep.setInt(4, bZ);
 
 			prep.execute();
 
 			
 			rs.close();
 			stat.close();
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void saveAttunment(String player, String bWorld, double x, double y, double z,  float yaw, float pitch) {
 		String table = "Attunements";
 		try {
 			Connection con = DriverManager.getConnection(sqlDB);
 
 			Statement stat = con.createStatement();
 			PreparedStatement prep;
 			/**/
 			prep = con.prepareStatement("UPDATE `"+table+"` SET `world` = ?, `x` = ?, `y` = ?, `z` = ?, `yaw` = ?, `pitch` = ? WHERE `player` = ?");
 			
 			
 			prep.setString(1, bWorld);
 			prep.setDouble(2, x);
 			prep.setDouble(3, y);
 			prep.setDouble(4, z);
 			prep.setFloat(5, yaw);
 			prep.setFloat(6, pitch);
 			prep.setString(7, player);
 			int rs = prep.executeUpdate();
 			if (rs > 0){
 				return;
 			}
 			
 			
 		
 			prep = con.prepareStatement("insert into `"+table+"` (player, world, x,y,z,yaw, pitch) values (?, ?, ?, ?,?,?,?)");
 			prep.setString(1, player);
 			prep.setString(2, bWorld);
 			
 			prep.setDouble(3, x);
 			prep.setDouble(4, y);
 			prep.setDouble(5, z);
 			prep.setFloat(6, yaw);
 			prep.setFloat(7, pitch);
 			
 			System.out.println("Saved attune to DB.");
 
 			prep.execute();
 
 			
 			
 			stat.close();
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public void removeLifestone(String bWorld, int bX, int bY, int bZ) {
 		
 		try {
 			Connection con = DriverManager.getConnection(sqlDB);
 
 			Statement stat = con.createStatement();
 	
 			PreparedStatement prep = con.prepareStatement("DELETE from Lifestones where `world` LIKE ? AND `x` LIKE ? AND `y` LIKE ? AND `z` LIKE ?");
 			prep.setString(1, bWorld);
 			prep.setInt(2, bX);
 			prep.setInt(3, bY);
 			prep.setInt(4, bZ);
 
 			prep.execute();
 
 			stat.close();
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		
 	}
 
 }
