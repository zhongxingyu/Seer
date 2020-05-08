 package edu.colorado.csci3308.inventory;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ServerDB {
 	
 	private static String path = "";
 	
 	public static void setPath(String path) {
 		ServerDB.path = path;
 	}
 	
 	public static ServerList getAllServers() {
 		ServerList servers = new ServerList();
 		ResultSet result = query("SELECT * FROM servers s " +
 				"JOIN motherboards m ON s.mb_id = m.mb_id " +
 				"JOIN chassis_models cm ON s.model_id = cm.model_id " +
 				"JOIN chassis_manufacturers cman ON cm.mfg_id = cman.mfg_id ");
 		
 		try {
 			while (result.next()) {
 				Integer serverId = result.getInt("server_id");
 				String hostname = result.getString("hostname");
 				String ipAddress = result.getString("ip_addr");
 				String macAddress = result.getString("mac_addr");
 				Integer totalMemory = result.getInt("total_memory");
 				Integer topHeight = result.getInt("top_height");
 				Integer locationId = result.getInt("location_id");
 				Integer rackId = result.getInt("rack_id");
 				String codeRevision = result.getString("code_revision");
 				Boolean isPingable = result.getInt("pingable") == 0 ? false:true;
 				Boolean lastScanOK = result.getInt("last_scan_ok") == 0 ? false:true;   
 
 				
 				// Motherboard data
 				Integer mbId = result.getInt(10);
 				String mbManufacturer = result.getString("manufacturer");
 				String mbModel = result.getString("model");
 				String mbUrl = result.getString("mb_url");
 				
 				// Model data
 				Integer modelId = result.getInt(11);
 				String modelName = result.getString("model_name");
 				Integer height = result.getInt("height");
 				Integer maxDataDrives = result.getInt("max_data_drives");
 				String mfgName = result.getString("mfg_name");
 				
 				// Processor data
 				String processors = result.getString("processors");
 				
 				// Card data
 				String cards = result.getString("cards");
 				
 				Motherboard motherboard = new Motherboard(mbId, mbManufacturer, mbModel, mbUrl);
 				ChassisModel model = new ChassisModel(modelId, modelName, height, 
 						maxDataDrives, mfgName);
 				
 				servers.add(new Server(serverId, 
 									   hostname, 
 									   ipAddress, 
 									   macAddress, 
 									   totalMemory,
 									   topHeight,
 									   locationId,
 									   rackId,
 									   model,
 									   motherboard,
 									   processors,
 									   codeRevision,
 									   isPingable,
 									   lastScanOK,
 									   cards));
 			}
 			result.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return servers;
 	}
 	
 	public static int getServerCount(){
 		 ResultSet result = query("SELECT COUNT(*) AS count FROM servers");
 		 int count;
 		try {
 			count = result.getInt("count");
 			return count;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return 0;
 		}
 		
 	}
 	
 	public static List<User> getAllUsers() {
 		List<User> users = new ArrayList<User>();
 		String query = "SELECT * FROM users";
 		ResultSet result = query(query);
 		
 		try {
 			while (result.next()) {
 				Integer userId = result.getInt(1);
 				String userName = result.getString(2);
 				
 				users.add(new User(userId, userName));
 			}
 			result.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getAllUsers");
 			e.printStackTrace();
 		}
 		return users;
 	}
 	
 	public static User getUserById(Integer id){
 		User usr = null;
 		
 		ResultSet result = query("SELECT * FROM users WHERE user_id = " + id);
 		
 		try {
 			String username = result.getString("user_name");
 			
 			usr = new User(id, username);
 			result.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return usr;
 	}
 	
 	public static User getUserByName(String name){
 		User usr = null;
 		String query = "SELECT * FROM users WHERE user_name = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setString(1, name);
 			ResultSet result = pstmt.executeQuery();
 			if (result.next()) {
 				Integer userId = result.getInt(1);
 				String userName = result.getString(2);
 				usr = new User(userId, userName);
 			}
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getUserByName");
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			return null;
 		}
 		return usr;
 	}
 	
 	public static void addUser(User user){
 		Integer userId = user.getUserId();
 		String userName = user.getUserName();
 		
 		String query = "INSERT INTO users (user_id, user_name) VALUES (?, ?)";
 		
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try {
 			pstmt.setInt(1, userId);
 			pstmt.setString(2, userName);
 			
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 		
 	public static Server getServerById(Integer serverId) {
 		Server server = null;
 		
 		/*
 		ResultSet result = query("SELECT server_id, hostname, ip_addr, mac_addr, total_memory, " +
 				"top_height, location_id, rack_id, processor_1, processor_2, s.mb_id, " +
 				"manufacturer, model, mb_url,  s.model_id, height, model_name, max_data_drives, " +
 				"mfg_name, code_revision, pingable, last_scan_ok, " +
 				"p1.processor_name AS p1_processor_name, p1.processor_speed AS p1_processor_speed, " +
 				"p1.num_cores AS p1_num_cores, " +
 				"p2.processor_name AS p2_processor_name, p2.processor_speed AS p2_processor_speed, " +
 				"p2.num_cores AS p2_num_cores " +
 				"FROM servers s " +
 				"JOIN motherboards mb ON s.mb_id = mb.mb_id  " +
 				"JOIN chassis_models cm ON cm.model_id = s.model_id  " +
 				"JOIN chassis_manufacturers cman ON cman.mfg_id = cm.mfg_id " +
 				"WHERE server_id = " + serverId);
 				*/
 		String query = "SELECT * FROM servers s " +
 				"JOIN motherboards m ON s.mb_id = m.mb_id " +
 				"JOIN chassis_models cm ON s.model_id = cm.model_id " +
 				"JOIN chassis_manufacturers cman ON cm.mfg_id = cman.mfg_id " +
 				"WHERE server_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try {
 			pstmt.setInt(1, serverId);
 			
 			ResultSet result = pstmt.executeQuery();
 			
 			String hostname = result.getString("hostname");
 			String ipAddress = result.getString("ip_addr");
 			String macAddress = result.getString("mac_addr");
 			Integer totalMemory = result.getInt("total_memory");
 			Integer topHeight = result.getInt("top_height");
 			Integer locationId = result.getInt("location_id");
 			Integer rackId = result.getInt("rack_id");
 			String codeRevision = result.getString("code_revision");
 			Boolean isPingable = result.getBoolean("pingable");
 			Boolean lastScanOK = result.getBoolean("last_scan_ok");  
 
 			
 			// Motherboard data
 			Integer mbId = result.getInt(10);
 			String mbManufacturer = result.getString("manufacturer");
 			String mbModel = result.getString("model");
 			String mbUrl = result.getString("mb_url");
 			
 			// Model data
 			Integer modelId = result.getInt(11);
 			String modelName = result.getString("model_name");
 			Integer height = result.getInt("height");
 			Integer maxDataDrives = result.getInt("max_data_drives");
 			String mfgName = result.getString("mfg_name");
 			
 			// Processor data
 			String processors = result.getString("processors");
 			
 			// Card data
 			String cards = result.getString("cards");
 			
 			Motherboard motherboard = new Motherboard(mbId, mbManufacturer, mbModel, mbUrl);
 			ChassisModel model = new ChassisModel(modelId, modelName, height, 
 					maxDataDrives, mfgName);
 			
 			server = new Server(serverId, 
 								hostname, 
 								ipAddress, 
 							    macAddress, 
 						 	    totalMemory,
 								topHeight,
 								locationId,
 								rackId,
 								model,
 								motherboard,
 								processors,
 								codeRevision,
 								isPingable,
 								lastScanOK,
 								cards);
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return server;
 	}
 	
 	public static Motherboard getMotherboardById(Integer motherboardId) {
 		Motherboard motherboard = null;
 		ResultSet result = query("SELECT * FROM motherboards WHERE mb_id = " + motherboardId);
 
 		try {
 			motherboard = new Motherboard(result.getInt("mb_id"), 
 										  result.getString("manufacturer"), 
 										  result.getString("model"), 
 										  result.getString("mb_url"));
 			result.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return motherboard;
 	}
 	
 	public static ChassisModel getChassisModelById(Integer modelId) {
 		ChassisModel model = null;
 		ResultSet result = query("SELECT * FROM chassis_models cmod " +
 								 "JOIN chassis_manufacturers cman " +
 								 "ON cmod.mfg_id = cman.mfg_id " +
 								 "WHERE cmod.model_id = " + modelId);
 		
 		try {
 			model = new ChassisModel(result.getInt(1), 
 									 result.getString(2), 
 									 result.getInt(3), 
 									 result.getInt(4), 
 									 result.getString(7));
 			result.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return model;
 	}
 	
 	/***
 	 * Adds a server to the database given a Server object
 	 * 
 	 * The server object only needs the following fields:
 	 *    hostname
 	 *    ip address
 	 *    mac address
 	 *    total memory
 	 *    
 	 * The rest, if null, are given a default value or inserted as null
 	 */
 	public static void addServer(Server server) {
 		String hostname = server.getHostname();
 		String ipAddress = server.getIpAddress();
 		String macAddress = server.getMacAddress();
 		Integer totalMemory = server.getTotalMemory();
 		Integer topHeight = server.getTopHeight();
 		Integer locationId = server.getLocationId();
 		Integer rackId = server.getRackId();
 		Integer motherboardId = server.getMotherboard() == null ? 1 : server.getMotherboard().getId();
 		Integer modelId = server.getChassisModel() == null ? 1 : server.getChassisModel().getId();
 		String processors = server.getProcessors();
 		String codeRevision = server.getCodeRevision();
 		Boolean isPingable = server.isPingable();
 		Boolean lastScanOK = server.lastScanOK();  
 		String cards = server.getCards();
 
 		
 		String query = "INSERT INTO servers " +
 				"(hostname, ip_addr, mac_addr, total_memory, top_height, location_id, " +
 				"rack_id, mb_id, model_id, processors, " +
 				"cards, code_revision, pingable, last_scan_ok)" +
 				"VALUES " +
 				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try {
 			pstmt.setString(1, hostname);
 			pstmt.setString(2, ipAddress);
 			pstmt.setString(3, macAddress);
 			pstmt.setInt(4, totalMemory);
 			
 			if (topHeight == null) { pstmt.setNull(5, Types.INTEGER); }
 			else { pstmt.setInt(5, topHeight); }
 			
 			if (locationId == null) { pstmt.setInt(6, 1); }
 			else { pstmt.setInt(6, locationId); }
 			
 			if (rackId == null) { pstmt.setNull(7, Types.INTEGER); }
 			else { pstmt.setInt(7, rackId); }
 			
 			pstmt.setInt(8, motherboardId);
 			pstmt.setInt(9, modelId);
 			pstmt.setString(10, processors);
 			pstmt.setString(11, cards);
 			pstmt.setString(12,  codeRevision);
 			pstmt.setInt(13, isPingable == false ? 0:1);
 			pstmt.setInt(14, lastScanOK == false ? 0:1);
 			
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/***
 	 * Updates a server to the database given a Server object
 	 * 
 	 * The server object only needs the following fields:
 	 *    hostname
 	 *    ip address
 	 *    mac address
 	 *    total memory
 	 *    
 	 * The rest, if null, are given a default value or inserted as null
 	 */
 	public static void updateServer(Server server) {
 		Integer id = server.getServerId();
 		String hostname = server.getHostname();
 		String ipAddress = server.getIpAddress();
 		String macAddress = server.getMacAddress();
 		Integer totalMemory = server.getTotalMemory();
 		Integer topHeight = server.getTopHeight();
 		Integer locationId = server.getLocationId();
 		Integer rackId = server.getRackId();
 		Integer motherboardId = server.getMotherboard() == null ? 1 : server.getMotherboard().getId(); //
 		Integer modelId = server.getChassisModel() == null ? 1 : server.getChassisModel().getId(); //
 		String processors = server.getProcessors();
 		String codeRevision = server.getCodeRevision();
 		Boolean isPingable = server.isPingable();
 		Boolean lastScanOK = server.lastScanOK();
 		String cards = server.getCards();
 		
 		String query = "UPDATE servers SET hostname = ?, ip_addr = ?, mac_addr = ?, total_memory = ?, top_height = ?, location_id = ?, " +
 				"rack_id = ?, mb_id = ?, model_id = ?, processors = ?, cards = ?, code_revision = ?, pingable = ?, last_scan_ok = ? " +
 				"WHERE server_id = ?";
 		
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try {
 			pstmt.setString(1, hostname);
 			pstmt.setString(2, ipAddress);
 			if (macAddress == null) {pstmt.setNull(3, Types.INTEGER);}
 			else { pstmt.setString(3, macAddress);}
 			if (totalMemory == null) {pstmt.setNull(4, Types.INTEGER);}
 			else { pstmt.setInt(4, totalMemory);}
 			
 			if (topHeight == null) { pstmt.setNull(5, Types.INTEGER); }
 			else { pstmt.setInt(5, topHeight); }
 			
 			if (locationId == null) { pstmt.setInt(6, 1); }
 			else { pstmt.setInt(6, locationId); }
 			
 			if (rackId == null) { pstmt.setNull(7, Types.INTEGER); }
 			else { pstmt.setInt(7, rackId); }
 			
 			pstmt.setInt(8, motherboardId);
 			pstmt.setInt(9, modelId);
 			pstmt.setString(10, processors);
 			pstmt.setString(11, cards);
 			pstmt.setString(12,  codeRevision);
 			pstmt.setInt(13, isPingable == false ? 0:1);
 			pstmt.setInt(14, lastScanOK == false ? 0:1);
 			pstmt.setInt(15, id);
 			
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteServer(Integer serverId) {
 		String query = "DELETE FROM servers WHERE server_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, serverId);
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteAll() throws ClassNotFoundException, SQLException{
 		query("drop table if exists servers");
 	}
 	
 	public static Reservation getReservationByReservationId(Integer reservationId) {
 		Reservation res = null;
 		String query = "SELECT * FROM reservations WHERE reservation_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, reservationId);
 			ResultSet result = pstmt.executeQuery();
 			
 			if (result.next()) {
 				Integer userId = result.getInt(2);
 				Integer serverId = result.getInt(3);
 				String startDate = result.getString(4);
 				String endDate = result.getString(5);
 				res = new Reservation(reservationId, userId, serverId, startDate, endDate);
 			}
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getReservationByReservationId");
 			e.printStackTrace();
 		}
 		return res;
 	}
 	
 	public static Reservation getReservationByServerId(Integer serverId) {
 		Reservation res = null;
 		String query = "SELECT * FROM reservations WHERE server_id = ? and start_date <= datetime('now')"
 				+ " and end_date >= datetime('now')";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, serverId);
 			ResultSet result = pstmt.executeQuery();
 			
 			if (result.next()) {
 				Integer reservationId = result.getInt(1);
 				Integer userId = result.getInt(2);
 				String startDate = result.getString(4);
 				String endDate = result.getString(5);
 				res = new Reservation(reservationId, userId, serverId, startDate, endDate);
 			}
 
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getReservationByReservationId");
 			e.printStackTrace();
 		}
 		return res;
 	}
 	
 	public static void addReservation(Reservation reservation) {
 		String query = "INSERT INTO reservations " +
 				"(user_id, server_id, start_date, end_date)" +
 				"VALUES (?, ?, datetime(?), datetime(?))";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, reservation.getUserId());
 			pstmt.setInt(2, reservation.getServerId());
 			pstmt.setString(3, reservation.getStartDate());
 			pstmt.setString(4, reservation.getEndDate());
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			System.out.println("Error in addReservation");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteReservation(Integer reservationId) {
 		String query = "DELETE FROM reservations WHERE reservation_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, reservationId);
 			pstmt.executeUpdate();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in deleteReservation");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void updateReservation(Reservation res){
 		String query = "UPDATE reservations SET user_id = ?, start_date = ?, end_date = ?" +
 				"WHERE server_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try{
 			pstmt.setInt(1, res.getUserId());
 			pstmt.setString(2, res.getStartDate());
 			pstmt.setString(3, res.getEndDate());
 			pstmt.setInt(4, res.getServerId());
 			
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static List<Location> getAllLocations(){
 		List<Location> locs = new ArrayList<Location>();
		String query = "SELECT * FROM locations";
 		
 		try {
 			ResultSet result = query(query);
 			
 			while(result.next()){
 				Integer id = result.getInt(1);
 				String name = result.getString(2);
 				String desc = result.getString(3);
 				Integer width = result.getInt(4);
 				Integer depth = result.getInt(5);
 				
 				locs.add(new Location(id, name, desc, width, depth));
 			}
 			
 			result.close();
 		} catch (SQLException e) {
			System.out.println("Error in getAllRacks");
 			e.printStackTrace();
 		}
 		
 		return locs;
 	}
 	
 	public static Location getLocationById(Integer locationId) {
 		Location loc = null;
 		String query = "SELECT * FROM location WHERE location_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, locationId);
 			ResultSet result = pstmt.executeQuery();
 			
 			if (result.next()) {
 				String name = result.getString(2);
 				String desc  = result.getString(3);
 				Integer width = result.getInt(4);
 				Integer depth = result.getInt(5);
 				loc = new Location(locationId, name, desc, width, depth);
 			}
 
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getLocationById");
 			e.printStackTrace();
 		}
 		return loc;
 	}
 	
 	public static void addLocation(Location loc) {
 		String query = "INSERT INTO location " +
 				"(location_id, location_name, location_description, width, depth)" +
 				"VALUES (?, ?, ?, ?, ?)";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, loc.getId());
 			pstmt.setString(2, loc.getName());
 			pstmt.setString(3, loc.getDescription());
 			pstmt.setInt(4, loc.getWidth());
 			pstmt.setInt(5, loc.getDepth());
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			System.out.println("Error in addLocation");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteLocation(Integer locId) {
 		String query = "DELETE FROM location WHERE location_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, locId);
 			pstmt.executeUpdate();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in deleteLocation");
 			e.printStackTrace();
 		}
 	}
 	
 	public static List<Rack> getAllRacks() {
 		List<Rack> racks = new ArrayList<Rack>();
 		String query = "SELECT * FROM racks";
 		
 		try {
 			ResultSet result = query(query);
 			
 			while (result.next()) {
 				Integer id = result.getInt(1);
 				String description = result.getString(2);
 				Integer maxHeight = result.getInt(3);
 				Integer widthSize = result.getInt(4);
 				Integer depthSize = result.getInt(5);
 				Integer widthStart = result.getInt(6);
 				Integer depthStart = result.getInt(7);
 				
 				racks.add(new Rack(id, description, maxHeight, widthSize, depthSize, widthStart, depthStart));
 			}
 			result.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getAllRacks");
 			e.printStackTrace();
 		}
 		return racks;
 	}
 
 	public static Rack getRackById(Integer rackId) {
 		Rack rack = null;
 		String query = "SELECT * FROM racks WHERE rack_id = " + rackId;
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try {
 			//pstmt.setInt(1, rackId);
 			ResultSet result = pstmt.executeQuery();
 			
 			if (result.next()) {
 				Integer id = result.getInt(1);
 				String rackDesc = result.getString(2);
 				Integer maxHeight  = result.getInt(3);
 				Integer width = result.getInt(4);
 				Integer depth = result.getInt(5);
 				Integer widthStart = result.getInt(6);
 				Integer depthStart = result.getInt(7);
 				rack = new Rack(id, rackDesc, maxHeight, width, depth, widthStart, depthStart);
 			}
 
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getRackById");
 			e.printStackTrace();
 		}
 		
 		return rack;
 	}
 	
 	public static void addRack(Rack rack) {
 		String query = "INSERT INTO racks " +
 				"(rack_id, rack_description, max_height, width_size, depth_size, width_start, depth_start)" +
 				"VALUES (?, ?, ?, ?, ?, ?, ?)";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, rack.getId());
 			pstmt.setString(2, rack.getDescription());
 			pstmt.setInt(3, rack.getMaxHeight());
 			pstmt.setInt(4, rack.getWidth());
 			pstmt.setInt(5, rack.getDepth());
 			pstmt.setInt(6, rack.getWidthStart());
 			pstmt.setInt(7, rack.getDepthStart());
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			System.out.println("Error in addRack");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteRack(Integer rackId) {
 		String query = "DELETE FROM location WHERE location_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, rackId);
 			pstmt.executeUpdate();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in deleteRack");
 			e.printStackTrace();
 		}
 	}
 
 	
 	private static ResultSet query(String query) {
 		ResultSet result = null;
 		
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			Connection con = DriverManager.getConnection("jdbc:sqlite:" + path);
 
 			Statement stmt = con.createStatement();
 			result = stmt.executeQuery(query);
 			
 		} catch (ClassNotFoundException e) {
 			System.out.println(e);
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 		
 		return result;
 	}
 	
 	private static PreparedStatement getPreparedStatement(String string) {
 		PreparedStatement pstmt = null;
 		
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection con = DriverManager.getConnection("jdbc:sqlite:" + path);
 			
 			pstmt = con.prepareStatement(string);
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		return pstmt;
 	}
 	
 	public static void main(String[] args) {
 		setPath("/home/ben/git/3308/Inventory/WebContent/WEB-INF/db/servers.db");
 	}   
 
 }
