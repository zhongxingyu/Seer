 package models;
 
 import play.db.*;
 import java.sql.*;
 import models.Interest;
 import models.User;
 import java.util.List;
 import java.util.ArrayList;
 
 public class Topic {
 
 	public String name;
 	public String description;
 	
 	public Topic(String name) {
 		this.name = name;
 	}
 
 	private Topic(String name, String description) {
 		this.name = name;
 		this.description = description;
 	}
 
 	public static List<Topic> getAll() throws SQLException {
 		ResultSet result;
 		Connection conn;
 		PreparedStatement stmt;
 
 		String name, description;
 		List<Topic> list;
 
 		conn = DB.getConnection();
 		stmt = conn.prepareStatement("select name, description from topic");
 		result = stmt.executeQuery();
 		list = new ArrayList<Topic>();
 		while(result.next()) {
 			name = result.getString(1);
 			description = result.getString(2);
 			list.add(new Topic(name, description));
 		}
 		conn.close();
 		return list;
 	}
 
 	public void addUser(User user, String description, boolean asMentor, boolean asStudent) throws SQLException {
 		
 		Connection conn = DB.getConnection();
 		PreparedStatement stmt = conn.prepareStatement("replace into interest (user, topic, description, as_mentor, as_student) values (?, ?, ?, ?, ?)");
 		stmt.setLong(1, user.id);
 		stmt.setString(2, name);
 		stmt.setString(3, description);
 		stmt.setBoolean(4, asMentor);
 		stmt.setBoolean(5, asStudent);
 		stmt.executeUpdate();
 		conn.close();
 	}
 
 	public void removeUser(User user) throws SQLException {
 		Connection conn = DB.getConnection();
		PreparedStatement stmt = conn.prepareStatement("delete from interest where user = ?");
 		stmt.setLong(1, user.id);
 		stmt.executeUpdate();
 		conn.close();
 	}
 
 	public boolean hasUser(User user) throws SQLException {
 		ResultSet result;
 		int rownum = 0;
 		Connection conn = DB.getConnection();
 		PreparedStatement stmt = conn.prepareStatement("select topic from interest where user = ? and topic = ?");
 		stmt.setLong(1, user.id);
 		stmt.setString(2, name);
 		result = stmt.executeQuery();
 		while(result.next())
 			rownum++;
 		conn.close();
 		return rownum != 0;
 	}
 
 	public Interest getInterestOf(User user) throws SQLException {
 		ResultSet result;
 		Connection conn;
 		PreparedStatement stmt;
 
 		String description;
 		boolean asMentor, asStudent;
 
 		conn = DB.getConnection();
 		stmt = conn.prepareStatement("select description, as_mentor, as_student from interest where user = ? and topic = ?");
 		stmt.setLong(1, user.id);
 		stmt.setString(2, name);
 		result = stmt.executeQuery();
 		if(result.next() == false) {
 			conn.close();
 			return null;
 		}
 		description = result.getString(1);
 		asMentor = result.getBoolean(2);
 		asStudent = result.getBoolean(3);
 		conn.close();
 
 		return new Interest(user.id, this.name, description, asMentor, asStudent);
 	}
 
 	public List<Interest> getInterests() throws SQLException {
 		ResultSet result;
 		Connection conn;
 		PreparedStatement stmt;
 
 		String description;
 		boolean asMentor, asStudent;
 		User user;
 		ArrayList<Interest> list;
 
 		conn = DB.getConnection();
 		stmt = conn.prepareStatement("select user, description, as_mentor, as_student from interest where topic = ?");
 		stmt.setString(1, name);
 		result = stmt.executeQuery();
 		list = new ArrayList<Interest>();
 		while(result.next()) {
 			user = User.getByUserId(result.getLong(1));
 			description = result.getString(2);
 			asMentor = result.getBoolean(3);
 			asStudent = result.getBoolean(4);
 			list.add(new Interest(user.id, this.name, description, asMentor, asStudent));
 		}
 		conn.close();
 
 		return list;
 	}
 }
