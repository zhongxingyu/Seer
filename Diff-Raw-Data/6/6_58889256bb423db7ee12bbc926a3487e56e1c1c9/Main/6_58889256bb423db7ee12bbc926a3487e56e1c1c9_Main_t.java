 package ycp.edu.seniordesign.main;
 
 import java.sql.SQLException;
 
import ycp.edu.seniordesign.model.User;
import ycp.edu.seniordesign.model.persist.Database;

 public class Main {
 	public static void main(String[] args) throws SQLException {
		Database.getInstance().createAccount("Nick","Password", "nbrady1@ycp.edu", User.PROFESSOR_PROFILE);
 	}
 		
 }
