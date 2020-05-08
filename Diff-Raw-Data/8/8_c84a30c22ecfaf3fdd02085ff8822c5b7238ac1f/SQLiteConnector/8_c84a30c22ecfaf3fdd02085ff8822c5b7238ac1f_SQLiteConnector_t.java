 package ch.epfl.bbcf.psd;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 
 
 
 public class SQLiteConnector {
 
 
 	
 	public static Connection getConnection(String path) throws ClassNotFoundException, SQLException{
 		Class.forName("org.sqlite.JDBC");
 		Connection conn = DriverManager.getConnection("jdbc:sqlite:/" + path);
 		return conn;
 	}
 
 	public static Map<String, Integer> getChromosomesAndLength(Connection conn) throws ClassNotFoundException, SQLException {
 		Map<String,Integer> result = new HashMap<String,Integer>();
 		Statement stat = conn.createStatement();
 		String query = "SELECT t1.name, t1.length FROM chrNames as t1;";
 		ResultSet rs = stat.executeQuery(query);
 		while (rs.next()) {
 			result.put(rs.getString("name"),
 					rs.getInt("length"));
 		}
 		rs.close();
 		return result;
 	}
 
 
 
 
 	/**
 	 * Create the output sqlite databases.
 	 * @param outputDir : the output directory
 	 * @param chromosome : the chromosome
 	 * @param zooms : the zooms
 	 * @return a connection store that will store all connections for the outputed databases
 	 * @throws SQLException 
 	 * @throws ClassNotFoundException 
 	 */
 	public static ConnectionStore createOutputDatabases(String outputDir, String chromosome, int[] zooms) throws ClassNotFoundException, SQLException {
 		ConnectionStore connectionStore = new ConnectionStore();
 		Connection conn;
 		for(int zoom : zooms){
 			String database = chromosome + "_" + zoom + ".db";
 			conn = getConnection(outputDir + File.separator + database);
 			PreparedStatement stat = conn.prepareStatement(
 					"create table sc (number INT,pos INT,score REAL,PRIMARY KEY(number,pos));");
 			stat.execute();
 			PreparedStatement prep = conn.prepareStatement("insert into sc values (?,?,?);");
 			conn.setAutoCommit(false);
 			connectionStore.addDatabase(database,conn,prep);
 		}
 		return connectionStore;
 	}
 
 	public static ResultSet getScores(Connection conn, String chromosome) throws SQLException {
 		Statement stat = conn.createStatement();
 		String query ="select t1.start, t1.end, t1.score from " + protect(chromosome) + " as t1;";
 		return stat.executeQuery(query);
 	}
 
 
 	protected static String protect(String str){
 		return "\""+str+"\"";
 	}
 	
 	public static void filldb(String chromosome, float[] tab,
 			int imageNumber, int zoom, ConnectionStore connectionStore, boolean finish) throws SQLException {
 		
 		
 		String database = chromosome + "_" + zoom + ".db";
 		Connection conn = connectionStore.getConnection(database);
 		
 		if (null == conn || conn.isClosed()){
 			System.err.println("cannot find connection for " + database);
 			return;
 		}
 		PreparedStatement prep = connectionStore.getPreparedStatement(database);
 		if(null == prep){
 			System.err.println("cannot find prepared statement for " + database);
 			return;
 		}
 		float val = tab[0];
 		int pos = 0;
 		for(int i=1;i<tab.length;i++){
 			if(!Main.floatEquals(val, tab[i])){
				try {
					prep.setInt(1, imageNumber);
				} catch(SQLException e){
					prep = conn.prepareStatement("insert into sc values(?, ?, ?);");
					connectionStore.setPreparedStatement(database, prep);
					prep.setInt(1, imageNumber);
				}
 				prep.setInt(2,pos);
 				prep.setFloat(3, val);
 				prep.executeUpdate();
 				pos=i;
 				val=tab[i];
 			}
 		}
 		try {
 			prep.setInt(1, imageNumber);
 			prep.setInt(2,pos);
 			prep.setFloat(3, val);
 			prep.executeUpdate();
 		} catch(SQLException e){}
 
 		int nbQueries = connectionStore.getNbQueries(database);
 
 		if(nbQueries > Main.LIMIT_QUERY_SIZE || finish){
 			conn.commit();
 			nbQueries = - Main.TAB_WIDTH;
 		}
 		connectionStore.setNbQueries(database, nbQueries + Main.TAB_WIDTH);
 	}
 }
 
 
 
 
 
 
 
