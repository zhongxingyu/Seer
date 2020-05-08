 package com.smartaleq.bukkit.dwarfcraft;
 
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 import org.bukkit.World;
 
 
 public class DataManager {
 
 	static List <Dwarf> dwarves = new ArrayList <Dwarf>();
 	
 	public static void dbInitialize() {
 	    try{
 	    	Class.forName("org.sqlite.JDBC");
 		    Connection conn =
 		      DriverManager.getConnection("jdbc:sqlite:DwarfCraft.db");
 		    Statement statement = conn.createStatement();
 		    ResultSet rs = statement.executeQuery("select * from dwarfs;");
 	    	if(rs != null) {
 	    		rs.close();
 	    		return;
 	    	}
 	    	//TODO if different skill columns, transfer to new table with 0's for new skills
 	    }
 	    catch (SQLException e) {
 	    	if(e.getMessage().equalsIgnoreCase("no such table: dwarfs")){
 		    	try{
 				    createDB();
 			    	System.out.println("DB created successfully");
 		    	}
 		    	catch (Exception f){
 		    		f.printStackTrace();
 		    		//total failure
 		    	}
 	    	}
 	    }
 	    catch (Exception e){
 	    	e.printStackTrace();
 	    	//total failure
 	    }
 	}
 		 
 	public static void createDB() {
     	try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conn =
 			  DriverManager.getConnection("jdbc:sqlite:DwarfCraft.db");
 			Statement statement = conn.createStatement();
 			String skillTableCreater = "";
 			for (Skill skill:ConfigManager.getAllSkills()){
 				if (skill != null) skillTableCreater = skillTableCreater.concat("," + skill.toString() );
 			}
 			String sqlLine = "create table dwarfs (playername,iself" + skillTableCreater + ");";
 			statement.executeUpdate(sqlLine);
 			System.out.println(sqlLine);
 			sqlLine = "create table schoolzones (school,x1,y1,z1,x2,y2,z2,world,name);";
 			statement.executeUpdate(sqlLine);
 			System.out.println(sqlLine);
 			conn.close();
 		} 
 		catch (SQLException e) {
 	    	System.out.println("DB not created successfully");
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void updateDwarfsTable(){
 		//TODO when table is not right size, resize it
 		//add columns or:
 		
 		//remove columns
 	}
 	
 	public static void createDwarfData(Dwarf dwarf) {
 	    try{
 	    	Class.forName("org.sqlite.JDBC");
 	    	Connection conn =
 	    		DriverManager.getConnection("jdbc:sqlite:DwarfCraft.db");
 	    	Statement statement = conn.createStatement();
 	    	String sql = "insert into dwarfs (playername, iself";
 	    	List<Skill> allSkills = ConfigManager.getAllSkills();
 	    	for (Skill skill: allSkills){
 				if (skill != null) sql = sql.concat("," + skill.toString());
 			}
 	    	sql = sql.concat(") values (");
 	    	sql = sql.concat("'"+dwarf.player.getName()+"'," + dwarf.isElf());
 	    	for (Skill skill: allSkills){
 				if (skill != null) sql = sql.concat("," + skill.level);
 			}
 	    	sql = sql.concat(");");
 	    	statement.executeUpdate(sql);
 	    	conn.close();
 	    }
 	    catch (Exception e){
 	    	e.printStackTrace();
 	    }	    
 	}
 	
 	public static boolean saveDwarfData(Dwarf dwarf){
 		try{
 			Class.forName("org.sqlite.JDBC");
 		    Connection conn =
 		      DriverManager.getConnection("jdbc:sqlite:DwarfCraft.db");
 		    Statement statement = conn.createStatement();
 	    	String sqlsend = "UPDATE dwarfs "+ "SET iself=" + dwarf.isElf + ", "; 
 	    	for (Skill skill: dwarf.skills) 
 	    		if (skill!=null) sqlsend = sqlsend.concat(skill.toString() + "=" + skill.level + ", ");
 	    	sqlsend = sqlsend.substring(0,sqlsend.length()-2)
 	    		+ " WHERE playername = '" + dwarf.player.getName()+ "'";
 	    	statement.execute(sqlsend);
 	    	conn.close();
 			return true;
 		}
 		catch (Exception e) {
 	    	e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public static boolean getDwarfData(Dwarf dwarf){
 	    try{
 	    	Class.forName("org.sqlite.JDBC");
 		    Connection conn =
 		      DriverManager.getConnection("jdbc:sqlite:DwarfCraft.db");
 		    Statement statement = conn.createStatement();
 		    String query = "select * from dwarfs WHERE playername = '" + dwarf.player.getName() + "';";
 //		    String query = "select * from dwarfs;";
 			ResultSet rs = statement.executeQuery(query);
 			rs.next();
 			if (rs.isClosed()) return false;
 			System.out.println("got dwarf data for " + dwarf.player.getName());
 			dwarf.isElf = rs.getBoolean("iself");
 			for (Skill skill: dwarf.skills){
 				if (skill!=null) skill.level = rs.getInt(skill.toString());
 			}
 			rs.close();
 	    	conn.close();
 			return true;
 		}
 		catch (Exception e){
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public static void removeDwarf(Dwarf dwarf) {
 		// TODO removedwarf
 		
 	}
 
 	@SuppressWarnings("unused")
 	public static TrainingZone[] getSchoolZones(World world) {
 	    try{
 	    	Class.forName("org.sqlite.JDBC");
		    Connection conn = DriverManager.getConnection("jdbc:sqlite:DwarfCraft.db");
 		    Statement statement = conn.createStatement();
		    String query = "select * from schoolzones Where world='"+world.getName()+"';";
 			ResultSet rs = statement.executeQuery(query);
 			if (rs == null) return null;
//			rs.last();
// i am not sure if this statement has value, but it throws an exception
 			TrainingZone[] zoneList = new TrainingZone[rs.getRow()];
 			rs = statement.executeQuery(query);
 			rs.next();
 			for(TrainingZone newzone:zoneList){
 				newzone = new TrainingZone(new Vector(rs.getDouble("x1"),rs.getDouble("y1"),rs.getDouble("z1")), new Vector(rs.getDouble("x2"),rs.getDouble("y2"),rs.getDouble("z2")), School.getSchool(rs.getString("school")), world, rs.getString("name"));
 				rs.next();
 			}
 			rs.close();
 	    	conn.close();
 			return zoneList;
 		}
 		catch (Exception e){
 	    	e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static boolean addSchoolZone(Vector vector1, Vector vector2, World world, School school, String name){
 	    try{
 	    	Class.forName("org.sqlite.JDBC");
 	    	Connection conn =
 	    		DriverManager.getConnection("jdbc:sqlite:DwarfCraft.db");
 	    	PreparedStatement prep = conn.prepareStatement("insert into schoolzones values (?,?,?,?,?,?,?,?,?);");
 	    	prep.setString(1, school.name());
 	    	prep.setDouble(2, vector1.getX());
 	    	prep.setDouble(3, vector1.getY());
 	    	prep.setDouble(4, vector1.getZ());
 	    	prep.setDouble(5, vector2.getX());
 	    	prep.setDouble(6, vector2.getY());
 	    	prep.setDouble(7, vector2.getZ());
 	    	prep.setString(8, world.getName());
 	    	prep.setString(9, name);
 	    	
 	    	prep.addBatch();
 	    	conn.setAutoCommit(false);
 	    	prep.executeBatch();
 	    	conn.setAutoCommit(true);
 	    	conn.close();
 	    	return true;
 	    }
 	    catch (Exception e){
 	    	e.printStackTrace();
 	    	return false;
 	    }	    
 	}
 
 	public static List<Dwarf> getDwarves() {
 		return dwarves;
 	}
 	
 	public static Dwarf createDwarf(Player player){
 		Dwarf newDwarf = new Dwarf(player);
 		dwarves.add(newDwarf);
 //		for (Dwarf d:dwarves) if (d==null){
 //			d = new Dwarf(player);
 //			if (DwarfCraft.debugging) System.out.println("Debug Message: added dwarf to dwarves array");
 //			for (int i = 0; i<4; i++)
 //				if (dwarves[i]==null) if (DwarfCraft.debugging) System.out.println("Debug Message: dwarves["+i+"] is null");
 //			return d;
 //		}
 //		
 		return newDwarf;
 	}
 }
 
 
 
 //statement.executeUpdate("create table people (name, occupation);");
 //PreparedStatement prep = conn.prepareStatement(
 //  "insert into dwarfs values (?, ?);");
 //
 //prep.setString(1, "Gandhi");
 //prep.setString(2, "politics");
 //prep.addBatch();
 //
 //conn.setAutoCommit(false);
 //prep.executeBatch();
 //conn.setAutoCommit(true);
 //
 //ResultSet rs = statement.executeQuery("select * from people;");
 //while (rs.next()) {
 //  System.out.println("name = " + rs.getString("name"));
 //  System.out.println("job = " + rs.getString("occupation"));
 //}
 //rs.close();
 //conn.close();
 //}
