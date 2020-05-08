 package pian.model.dao;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /*
  * Create database 'pian.sqlite' in root folder
  */
 
 public class InitDatabase {
 	public static void main(String[] args) throws SQLException, ClassNotFoundException {
 		Class.forName("org.sqlite.JDBC");
	    Connection connection = DriverManager.getConnection("jdbc:sqlite:pian.sqlite");
 	    Statement stat = connection.createStatement();
 	    String tableSong = "CREATE TABLE Song(ID INTEGER PRIMARY KEY AUTOINCREMENT, Title TEXT, Link TEXT, AlbumID int, ArtistID int, Resource BLOP);";
 	    String tableAlbum = "CREATE TABLE Album(ID INTEGER PRIMARY KEY AUTOINCREMENT, ArtistID int, Name TEXT);";
 	    String tableArtist = "CREATE TABLE Artist(ID INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT);";
 	    stat.addBatch(tableSong);
 	    stat.addBatch(tableArtist);
 	    stat.addBatch(tableAlbum);
 	    connection.setAutoCommit(false);
 	    connection.commit();
 	    stat.executeBatch();
 	    connection.commit();
 	    stat.close();
 	    connection.close();
 	    System.out.println("-------------Done---------------");
 	}
 
 }
