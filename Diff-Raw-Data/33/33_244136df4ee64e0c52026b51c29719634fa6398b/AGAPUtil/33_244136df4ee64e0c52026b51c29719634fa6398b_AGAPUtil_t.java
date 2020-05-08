 /****************************************************************************
 
 	ASCMII is a web application developped for the Ecole Centrale de Nantes
 	aiming to organize quizzes during courses or lectures.
     Copyright (C) 2013  Malik Olivier Boussejra
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
 
  ******************************************************************************/
 
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
 
 import models.Eleve;
 import functions.agap.Groupe;
 //import functions.agap.AGAPStringUtil;
 import functions.agap.Matiere;
 
 /**
  * Contient les fonctions pour pouvoir se connecter à AGAP
  * @author malik
  *
  */
 public class AGAPUtil {	
 	private static String dbURL;
 	private static String dbUser;
 	private static String dbPass;
 
 	public static List<Matiere> listMatieres = new ArrayList<Matiere>();
 	public static List<Groupe> listGroupes = new ArrayList<Groupe>();

 	/**
 	 * init data
 	 */
 	public static void init() {
 		String dbDriver = play.Play.application().configuration().getString("agap.driver");
 		String dbProtocol = play.Play.application().configuration().getString("agap.protocol");
 		String dbHost = play.Play.application().configuration().getString("agap.host");
 		String dbPort = play.Play.application().configuration().getString("agap.port");
 		String dbName = play.Play.application().configuration().getString("agap.dbName");
 
 		dbURL = dbProtocol + "://" + dbHost + ":" + dbPort + "/" + dbName;
 		dbUser = play.Play.application().configuration().getString("agap.user");
 		dbPass = play.Play.application().configuration().getString("agap.passw");
 		try {
 			// try with the main server
 			System.out.println("Loading PostgreSQL driver...");
 			Class.forName(dbDriver);
 			System.out.println("Loading PostgreSQL driver... LOADED");
 		} catch (ClassNotFoundException e) {
 			Logger.getLogger(AGAPUtil.class.getName()).log(Level.INFO, null, e);
 			System.out.println("Loading PostgreSQL driver... Failed");
 		}
 
 		getMatiereList();
 		getGroupeList();
 	}
 
 	/**
 	 * Get connector to the database
 	 */
 	public static Connection getConnection() {
 		Connection connection = null;
 		try {
 			System.out.println("Connecting to : " +dbURL+ " " + dbUser+" "+dbPass);
 			connection = DriverManager.getConnection(dbURL, dbUser, dbPass);
 		} catch (Exception e) {
 			Logger.getLogger(AGAPUtil.class.getName()).log(Level.INFO, null, e);
 			e.printStackTrace();
 
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
 			System.out.println("Discharging PostgreSQL driver...");
 			Driver theDriver = DriverManager.getDriver(dbURL);
 			DriverManager.deregisterDriver(theDriver);
 			dbURL = null;
 			System.out.println("Discharging PostgreSQL driver... DISCHARGED");
 		} catch (SQLException ex) {
 			Logger.getLogger(AGAPUtil.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	/**
 	 * Remplie la variable listMatieres avec la liste des matières dans AGAP
 	 */
 	private static void getMatiereList(){
 		String theQuery = listeCours();
 		System.out.println("Connecting to AGAP...");
 		Connection connection = getConnection();
 		if(connection!=null){
 			try {
 				System.out.println("Executing : " + theQuery);
 				Statement theStmt = connection.createStatement();
 				ResultSet theRS1 = theStmt.executeQuery(theQuery);
 				while (theRS1.next()) {
 					Integer id = theRS1.getInt("ActionFormation_ID");
 					//String semestre = AGAPStringUtil.getSemestre(theRS1.getInt("ActionFormation_ID"),connection,theRS1.getString("actionformation_libellecourt"));
 					//On laisse le semestre tel quel (on garde le libelle_court qui est dans AGAP).
 					//Ce choix n'a pas été fait au départ car les libelle_court ne contenaient pas les
 					//semestres dans leur nom
 					String semestre = "";
 					listMatieres.add(new Matiere(theRS1.getString("actionformation_libellecourt"),
 							theRS1.getString("actionformation_libellecourt"),semestre,
 							id
 							));
 				}
 			} catch (SQLException ex) {
 				Logger.getLogger(AGAPUtil.class.getName()).log(Level.SEVERE, "query error " + theQuery, ex);
 			}
 			releaseConnection(connection);
 		} else{
 			//On ajoute des matières pour faire des tests, même sans être connecté à AGAP
 			Matiere.createList();
 			System.out.println("Impossible de se connecter à AGAP...");
 		}
 	}
 
 	/**
 	 * Requête pour avoir la liste des cours
 	 * @return
 	 */
 	private static String listeCours(){
 		return "SELECT * FROM actionformation "
 				+ "NATURAL JOIN cycle "
 				+ "WHERE cycle_defaut=1";
 	}
 
 	/**
 	 * Requête pour avoir la liste des groupes
 	 * @return
 	 */
 	private static String listeGroupes(){
 		return "SELECT * FROM groupe "
 				+ "NATURAL JOIN cycle "
 				+ "WHERE cycle_defaut=1";
 	}

 	/**
 	 * Remplie la variable listGroupes avec la liste des groupes dans AGAP
 	 */
 	private static void getGroupeList(){
 		String theQuery = listeGroupes();
 		System.out.println("Connecting to AGAP...");
 		Connection connection = getConnection();
 		if(connection!=null){
 			try {
 				System.out.println("Executing : " + theQuery);
 				Statement theStmt = connection.createStatement();
 				ResultSet theRS1 = theStmt.executeQuery(theQuery);
 				while (theRS1.next()) {
 					listGroupes.add(new Groupe(theRS1.getInt("groupe_id"),theRS1.getString("groupe_nom")));
 				}
 			} catch (SQLException ex) {
 				Logger.getLogger(AGAPUtil.class.getName()).log(Level.SEVERE, "query error " + theQuery, ex);
 			}
 			releaseConnection(connection);
 		} else{
 			//On ajoute des matières pour faire des tests, même sans être connecté à AGAP
 			Groupe.createList();
 			System.out.println("Impossible de se connecter à AGAP...");
 		}
 	}


 	/**
 	 * Trouve les élèves suivant le cours ayant l'id donnée
 	 * @param libellecourt
 	 * @return
 	 */
 	public static List<Eleve> getInscrits(Integer mat_id){
 		List<Eleve> eleves = new ArrayList<Eleve>();
 		String theQuery = listeInscrits(mat_id);
 		System.out.println("Connecting to AGAP...");
 		Connection connection = getConnection();
 		if(connection!=null){
 			try {
 				System.out.println("Executing : " + theQuery);
 				Statement theStmt = connection.createStatement();
 				ResultSet theRS1 = theStmt.executeQuery(theQuery);
 				while (theRS1.next()) {
 					String uid = theRS1.getString("personne_uid");
 					if(uid!=null){
 						Eleve eleve = Eleve.find.byId(uid);
 						if(eleve==null){
 							System.out.println("uid not found : " + uid);
 						}else{
 							eleves.add(eleve);
 						}
 					}else	System.out.println("Found a null uid");
 				}
 			} catch (SQLException ex) {
 				Logger.getLogger(AGAPUtil.class.getName()).log(Level.SEVERE, "query error " + theQuery, ex);
 			}
 			releaseConnection(connection);
 		} else{
 			System.out.println("Impossible de se connecter à AGAP...");
 		}
 		return eleves;
 	}
 
 	/**
 	 * Requête pour avoir la liste des inscrits
 	 * @param ActionFormation_ID
 	 * @return
 	 */
 	private static String listeInscrits(Integer ActionFormation_ID){
 		return "SELECT personne_uid FROM InscriptionAction "
 				+ "NATURAL JOIN Inscription "
 				+ "NATURAL JOIN CursusPrepare "
 				+ "NATURAL JOIN DossierScolaire "
 				+ "NATURAL JOIN Eleve "
 				+ "NATURAL JOIN Personne "
 				+ "WHERE ActionFormation_ID="+ActionFormation_ID;
 	}
 
 	/**
 	 * Trouve les élèves dans le groupe donné suivant le cours ayant l'id donnée.
 	 * @param mat_id
 	 * @param groupe_nom
 	 * @return
 	 */
 	public static List<Eleve> getInscritsParGroupe(Integer mat_id, String groupe_nom){
 		List<Eleve> eleves = new ArrayList<Eleve>();
 		String theQuery = listeInscritsGroupe(mat_id, groupe_nom);
 		System.out.println("Connecting to AGAP...");
 		Connection connection = getConnection();
 		if(connection!=null){
 			try {
 				System.out.println("Executing : " + theQuery);
 				Statement theStmt = connection.createStatement();
 				ResultSet theRS1 = theStmt.executeQuery(theQuery);
 				while (theRS1.next()) {
 					String uid = theRS1.getString("personne_uid");
 					Eleve eleve = Eleve.find.byId(uid);
 					if(eleve==null){
 						System.out.println("uid not found : " + uid);
 					}else{
 						eleves.add(eleve);
 					}
 				}
 			} catch (SQLException ex) {
 				Logger.getLogger(AGAPUtil.class.getName()).log(Level.SEVERE, "query error " + theQuery, ex);
 			}
 			releaseConnection(connection);
 		} else{
 			System.out.println("Impossible de se connecter à AGAP...");
 		}
 		return eleves;
 	}
 
 
 	/**
	 * Requête pour avoir la liste des inscrits dans un groupe et suivant un enseignement donné.
 	 * @param ActionFormation_ID
	 * @param groupe_nom
 	 * @return
 	 */
 	private static String listeInscritsGroupe(Integer ActionFormation_ID, String groupe_nom){
		/*return "SELECT DISTINCT personne_uid FROM InscriptionAction "
 				+ "NATURAL JOIN Inscription "
 				+ "NATURAL JOIN CursusPrepare "
 				+ "NATURAL JOIN DossierScolaire "
 				+ "NATURAL JOIN Eleve "
 				+ "NATURAL JOIN Personne "
 				+ "NATURAL JOIN EleveDansGroupe "
 				+ "NATURAL JOIN Groupe "
 				+ "NATURAL JOIN GroupeStructure "
 				+ "NATURAL JOIN Structures "
 				+ "WHERE ActionFormation_ID="+ActionFormation_ID+" "
				+ "AND groupe_nom='"+groupe_nom+"'";*/
		return "SELECT personne_uid FROM Groupe "
		+ "NATURAL JOIN EleveDansGroupe "
		+ "NATURAL JOIN InscriptionAction "
		+ "NATURAL JOIN Inscription "
		+ "NATURAL JOIN CursusPrepare "
		+ "NATURAL JOIN DossierScolaire "
		+ "NATURAL JOIN Eleve "
		+ "NATURAL JOIN Personne "
		+ "NATURAL JOIN cycle "
		+ "WHERE cycle_defaut=1 "
		+ "AND ActionFormation_ID="+ActionFormation_ID+" "
		+ "AND groupe_nom='"+groupe_nom+"'";
 	}
 }
