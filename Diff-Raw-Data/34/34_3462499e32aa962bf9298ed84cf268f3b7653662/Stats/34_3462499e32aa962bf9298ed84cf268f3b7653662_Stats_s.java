 package tumblstats;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import tumblib.Post;
 
 public abstract class Stats {
 
 	String tableName; 
 	String keyName;
 	Post[] postArray;
 	Statement stat = null;
 	Connection conn = null;
 	PreparedStatement prep = null;
 
 	public Stats(String tableName, String keyName, Post[] postArray){
 		setTableName(tableName);
 		setKeyName(keyName);
 		setPostArray(postArray);
 	}
 
 	/**@param accumulate Whether or not this instance of stats should accumulate
 	 * its data with that of the previous instance.*/
 	void createTable(boolean accumulate){
 		/* Open a connection to the database, drop the old table if we're not 
 		 * accumulating, create a table if it doesn't exist, prepare a statement 
 		 * that will be executed several times.*/
 		try{
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection("jdbc:sqlite:tumblr.db");
 			stat = conn.createStatement();
 			if(!accumulate)
 				stat.executeUpdate("drop table if exists " + tableName + ";");
 			stat.executeUpdate("create table if not exists " + tableName + 
 					" (" + keyName + " primary key, posts);");
 			prep = conn.prepareStatement("insert or replace into " + tableName +
 			" values (?, ?);");
 		}catch(ClassNotFoundException e){
 			e.printStackTrace();
 		}catch(SQLException e){
 			e.printStackTrace();
 			System.err.println("Verify SQL syntax.  Also verify " +
 			"that the database exists.");
 		}
 		
 		//setData has different implementations depending on the data type.
 		setData();
 		printData();
 	}
 
 	protected abstract void setData();
 
 	void printData(){
 		try {
 
 			ResultSet rs = stat.executeQuery("select * from " + tableName + " " +
 					"order by " + keyName + ";");
			System.out.printf("\t%.11s     %s\n", keyName, "\t\tposts");
 			while(rs.next())
				System.out.printf("\t%-4.10s     %s\n", rs.getString(keyName), 
						"\t\t" + rs.getString("posts"));
 
 			rs.close();
 			conn.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 
 	/**
 	 * @return the tableName
 	 */
 	public String getTableName() {
 		return tableName;
 	}
 
 	/**
 	 * @param tableName the tableName to set
 	 */
 	public void setTableName(String tableName) {
 		this.tableName = tableName;
 	}
 
 	/**
 	 * @return the keyName
 	 */
 	public String getKeyName() {
 		return keyName;
 	}
 
 	/**
 	 * @param keyName the keyName to set
 	 */
 	public void setKeyName(String keyName) {
 		this.keyName = keyName;
 	}
 
 	/**
 	 * @return the postArray
 	 */
 	public Post[] getPostArray() {
 		return postArray;
 	}
 
 	/**
 	 * @param postArray the postArray to set
 	 */
 	public void setPostArray(Post[] postArray) {
 		this.postArray = postArray;
 	}
 
 }
