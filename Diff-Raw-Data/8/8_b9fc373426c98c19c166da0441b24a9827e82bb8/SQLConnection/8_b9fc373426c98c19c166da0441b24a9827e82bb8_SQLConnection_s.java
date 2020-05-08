 package com.appspot.omega;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.appengine.api.rdbms.AppEngineDriver;
 
 import com.google.gson.Gson;
 
 public class SQLConnection {
 	Connection conn = null;
 	Statement stmt = null;
 	ResultSet rs = null;
 
	static private String dbURL = "jdbc:google:rdbms://omega-vl-sql/omega-vl";
 	static private String username = "appfog";
 	static private String password = "562325";
 
 	public SQLConnection() {
 		super();
 		create();
 	}
 
 	public String execute(String query){
 		String result = null;
 		try {
 			if (stmt.execute(query)) {
 				rs = stmt.getResultSet();
 			} else {
 				System.err.println("select failed");
 			}
 			Gson gs = new Gson();
 			ArrayList<KandidaatByRegion> pk = new ArrayList<KandidaatByRegion>();
 			while (rs.next()) {
 				pk.add(new KandidaatByRegion(rs.getInt(1), rs.getString(2) +" "+ rs.getString(3), rs.getInt(4), rs.getString(5)));
 			}
 			result = gs.toJson(pk);
 
 		} 
 		catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage()); 
 			System.out.println("SQLState: " + ex.getSQLState()); 
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		return result;
 	}
 
 	public String execute_piirkond(){
 		String result = null;
 		try {
 			PreparedStatement stmt = conn.prepareStatement("select * from piirkond order by nimi");
 			//if () {
 			rs = stmt.executeQuery();
 			//} else {
 			//    System.err.println("select failed");
 			//}
 			Gson gs = new Gson();
 			ArrayList<Piirkond> pk = new ArrayList<Piirkond>();
 			while (rs.next()) {
 				pk.add(new Piirkond(rs.getInt(1), rs.getString(2)));
 			}
 			result = gs.toJson(pk);
 
 		} 
 		catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage()); 
 			System.out.println("SQLState: " + ex.getSQLState()); 
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		return result;
 	}
 
 	public String executeHaaled(String command){
 		String result = null;
 		if (command == "koik"){
 			List<HaaledKandidaat> haaled = new ArrayList<HaaledKandidaat>();
 
 			PreparedStatement stmt;
 			try {
				stmt = conn.prepareStatement("select eesnimi, perenimi, erakond.nimi, piirkond.nimi as piirkond, count(kandidaat.id) as haaled " +
						"from appfog.kandidaat, appfog.isik, appfog.erakond, appfog.piirkond where " +
						"isik.kandidaat = kandidaat.id and kandidaat.piirkond = piirkond.id and " +
						"kandidaat.erakond = erakond.id group by kandidaat.id order by haaled desc");
 				rs = stmt.executeQuery();
 
 				while (rs.next()) {
 
 					haaled.add(new HaaledKandidaat(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5)));
 				}
 
 				Gson gs = new Gson();
 				result = gs.toJson(haaled);
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 
 
 
 
 		}
 		return result;
 	}
 
 	public Connection create(){
 		try{
 			//Class.forName("com.mysql.jdbc.Driver");
 			DriverManager.registerDriver(new AppEngineDriver());
 			conn = DriverManager.getConnection(dbURL, username, password);
 		}
 		/*catch (ClassNotFoundException ex) {
             System.err.println("Failed to load mysql driver");
             System.err.println(ex);
     	}*/
 		catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage()); 
 			System.out.println("SQLState: " + ex.getSQLState()); 
 			System.out.println("VendorError: " + ex.getErrorCode()); 
 		}
 		return conn;
 	}
 	public void close(){
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (SQLException ex) { /* ignore */ }
 			rs = null;
 		}
 		if (stmt != null) {
 			try {
 				stmt.close();
 			} catch (SQLException ex) { /* ignore */ }
 			stmt = null;
 		}
 		if (conn != null) {
 			try {
 				conn.close();
 			} catch (SQLException ex) { /* ignore */ }
 			conn = null;
 		}
 	}
 	public ResultSet execCommand(String command){
 
 
 		PreparedStatement stmt;
 		try {
 			stmt = conn.prepareStatement(command);
 			rs = stmt.executeQuery();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 
 
 		return rs;
 	}
 }
