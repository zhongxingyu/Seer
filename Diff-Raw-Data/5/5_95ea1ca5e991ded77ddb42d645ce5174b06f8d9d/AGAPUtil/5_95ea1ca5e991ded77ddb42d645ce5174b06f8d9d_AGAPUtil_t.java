 package functions;
 
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class AGAPUtil {	
 	private static String dbURL;
 	private static String dbUser;
 	private static String dbPass;
 
 	public static List<String> listMatieres;
 
 	/**
 	 * init data
 	 */
 	public static void init() {
 		String dbDriver = "org.postgresql.Driver";
 		String dbProtocol = "jdbc:postgresql";
 		String dbHost = "agapbd.ec-nantes.fr";
 		String dbPort = "5432";
 		String dbName = "AGAP";
 
 		dbURL = dbProtocol + "://" + dbHost + ":" + dbPort + "/" + dbName;
 		dbUser = "ascmii";
 		dbPass = "secret";
 		try {
 			// try with the main server
 			Class.forName(dbDriver);
 		} catch (Exception e) {
 			Logger.getLogger(AGAPUtil.class.getName()).log(Level.INFO, null, e);
 		}
 
 		getMatiereList();
 	}
 
 	/**
 	 * Get connector to the database
 	 */
 	public static Connection getConnection() {
 		Connection connection = null;
 		try {
 			connection = DriverManager.getConnection(dbURL, dbUser, dbPass);
 		} catch (Exception e) {
 			Logger.getLogger(AGAPUtil.class.getName()).log(Level.INFO, null, e);
 		}
 		return connection;
 	}
 
 	/**
 	 * Release connector to the database
 	 */
 	public static void releaseConnection(Connection connection) {
 		if (connection != null) {
 			try {
 				connection.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Décharge le driver postgres
 	 */
 	public static void release() {
 		try {
 			Driver theDriver = DriverManager.getDriver(dbURL);
 			DriverManager.deregisterDriver(theDriver);
 			dbURL = null;
 		} catch (SQLException ex) {
 			Logger.getLogger(AGAPUtil.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	/**
 	 * Remplie la variable listMatieres avec la liste des matières dans AGAP
 	 */
 	private static void getMatiereList(){
 		listMatieres = new ArrayList<String>();
		listMatieres.add("ALGPR");
 		listMatieres.add("GEMAT");
 		listMatieres.add("SCUBE");
 		listMatieres.add("dSIBAD");
 		listMatieres.add("PEINS");
 		listMatieres.add("CHEPA");
 		listMatieres.add("GAGAG");
 		listMatieres.add("AUTOM");
 		listMatieres.add("CONEN");
 		listMatieres.add("VIVRE");
		listMatieres.add("MATIE");
 		String theQuery = listeCours();
 		Connection connection = getConnection();
 		if(connection!=null){
 			try {
 				Statement theStmt = connection.createStatement();
 				ResultSet theRS1 = theStmt.executeQuery(theQuery);
 				while (theRS1.next()) {
 					String libelle = theRS1.getString("ActionFormation_Libelle");
 					//int id = theRS1.getInt("ActionFormation_ID");
 					listMatieres.add(libelle);
 				}
 			} catch (SQLException ex) {
 				Logger.getLogger(AGAPUtil.class.getName()).log(Level.SEVERE, "query error " + theQuery, ex);
 			}
 			releaseConnection(connection);
 		}
 		else{
 			System.out.println("Impossible de se connecter à AGAP...");
 		}
 	}
 
 	/**
 	 * Requête pour avoir la liste des cours
 	 * @return
 	 */
 	public static String listeCours(){
 		return "SELECT * FROM ActionFormation "
 				+ "NATURAL JOIN Cycle "
 				+ "WHERE Cycle_Courant=1";
 	}
 
 	/**
 	 * Requête pour avoir la liste des inscrits
 	 * @param ActionFormation_ID
 	 * @return
 	 */
 	public static String listeInscrits(String ActionFormation_ID){
 		return "SELECT * FROM InscriptionAction "
 				+ "NATURAL JOIN Inscription "
 				+ "NATURAL JOIN CursusPrepare "
 				+ "NATURAL JOIN DossierScolaire "
 				+ "NATURAL JOIN Eleve "
 				+ "NATURAL JOIN Personne "
 				+ "WHERE ActionFormation_ID="+ActionFormation_ID;
 	}
 
 	/**
 	 * Requête pour avoir la liste des inscrits
 	 * @param ActionFormation_ID
 	 * @return
 	 */
 	public static String listeInscritsGroupe(String ActionFormation_ID){
 		return "SELECT * FROM InscriptionAction "
 				+ "NATURAL JOIN Inscription "
 				+ "NATURAL JOIN CursusPrepare "
 				+ "NATURAL JOIN DossierScolaire "
 				+ "NATURAL JOIN Eleve "
 				+ "NATURAL JOIN Personne "
 				+ "NATURAL JOIN EleveDansGroupe "
 				+ "NATURAL JOIN Groupe "
 				+ "NATURAL JOIN GroupeStructure "
 				+ "NATURAL JOIN Structures "
 				+ "WHERE ActionFormation_ID="+ActionFormation_ID;
 	}
 }
