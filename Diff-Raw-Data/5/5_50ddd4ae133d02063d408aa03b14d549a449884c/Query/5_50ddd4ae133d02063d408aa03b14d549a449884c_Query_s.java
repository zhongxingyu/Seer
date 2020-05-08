 package swsec;
 
 import java.sql.*;
 
 public class Query {	
 	
 	public static ResultSet SelectCountry()  throws Exception
 	{
 		ResultSet rs = null;
 		Connection con = ConnectionDB.getConnection();
 		PreparedStatement stat = con.prepareStatement("SELECT full_name FROM country");
 		rs = stat.executeQuery();		
 		return rs;		
 	}
 	
 	public static ResultSet selectSchools(String countryFullName) throws Exception
 	{
 		Connection con = ConnectionDB.getConnection();
 		PreparedStatement stat=con.prepareStatement("SELECT * FROM country, school WHERE school.country = country.short_name AND country.full_name = ?");
 		stat.setString(1, countryFullName);
 		return stat.executeQuery();
 	}
 	
 	public static ResultSet selectReviews(String school_name) throws Exception
 	{
 		Connection con = ConnectionDB.getConnection();
 		PreparedStatement  stat = con.prepareStatement("SELECT * FROM user_reviews, school WHERE user_reviews.school_id = school.school_id AND school.full_name = ?");
 		stat.setString(1,school_name);
 		return stat.executeQuery();
 	}
 	
 	public static void insertReview(String school_id, String name, String review) throws Exception
 	{
 		Connection con = ConnectionDB.getConnection();
 		PreparedStatement stat = con.prepareStatement("INSERT INTO user_reviews VALUES (?,?,?)");
		stat.setString(1, school_id);
 		stat.setString(2, name);
 		stat.setString(3, review);
		stat.executeQuery();
 	}
 }
