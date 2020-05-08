 package com.ecnmelog.app;
 
 import java.util.*;
 import java.sql.*;
 
 /**
  * Classe du système de stockage de container
  * @author Clément Delafargue
  * @author Benjamin Vialle
  */
 public class Stockage implements Entrepot
 {
 	/**
 	 * Initialise un espace de stockage 
 	 * @param contenance Contenance de la zone de stockage
 	 */
 	public Stockage(int contenance){
 		
 		// Initialisation des nombres d'emplacements libres
 		
 		// Un emplacement sur 20 est frigorifique
 		int nbEmplacementsFrigoDispo = (int) contenance / 20;
 		
 		// 65% des emplacements sont sur-tarifés
 		int nbEmplacementsSurtarifesDispo = (int) (contenance * 0.65);
 		
 		// Le reste est normal
 		int nbEmplacementsNormauxDispo = contenance - nbEmplacementsFrigoDispo - nbEmplacementsSurtarifesDispo;
 		
 		
 		Connection conn = DbConn.getInstance();
 		
 		try{
 			Statement stat = conn.createStatement();
 			// On vide le schéma (si on reconstruit l'espace de stockage alors qu'il existe déjà, ça évite des bugs)
 			stat.executeUpdate("DROP TABLE IF EXISTS type;");
 			stat.executeUpdate("DROP TABLE IF EXISTS container;");
 			stat.executeUpdate("DROP TABLE IF EXISTS emplacement;");
 			
 			// On initialise le schéma
 			stat.executeUpdate("CREATE TABLE type (type_id INTEGER, type_nom, type_couleur, PRIMARY KEY(type_id ASC));");
 			stat.executeUpdate("CREATE TABLE container (container_id INTEGER, type_id INTEGER REFERENCES type (type_id) ON DELETE CASCADE, emplacement_id INTEGER REFERENCES emplacement (emplacement_id) ON DELETE SET NULL DEFAULT NULL, PRIMARY KEY(container_id ASC));");
 			stat.executeUpdate("CREATE TABLE emplacement (emplacement_id INTEGER, type_id INTEGER REFERENCES type (type_id) ON DELETE CASCADE, PRIMARY KEY(emplacement_id ASC));");
 
 			// On crée les 3 types de containers / emplacements
 			PreparedStatement types = conn.prepareStatement("INSERT INTO type (type_id, type_nom, type_couleur) VALUES (?, ?, ?);");
 
 				types.setInt(1, 0);
 				types.setString(2, "Normal");
 				types.setString(3, "0xFFFFFF");
 				types.addBatch();
 				types.setInt(1, 1);
 				types.setString(2, "Frigo");
 				types.setString(3, "0x0000FF");
 				types.addBatch();
 				types.setInt(1, 2);
 				types.setString(2, "Prioritaire");
 				types.setString(3, "0xFF0000");
 				types.addBatch();
 
 				conn.setAutoCommit(false);
 				types.executeBatch();
 				conn.setAutoCommit(true);
 				
 			// On crée les différents emplacements
 			PreparedStatement emplacements = conn.prepareStatement("INSERT INTO emplacement (type_id) VALUES (?);");
 				
 				for(int i=0; i<nbEmplacementsNormauxDispo; i++){
 					emplacements.setInt(1, 0);
 					emplacements.addBatch();
 				}
 				for(int i=0; i<nbEmplacementsFrigoDispo; i++){
 					emplacements.setInt(1, 1);
 					emplacements.addBatch();
 				}
 				for(int i=0; i<nbEmplacementsSurtarifesDispo; i++){
 					emplacements.setInt(1, 2);
 					emplacements.addBatch();
 				}
 
 				conn.setAutoCommit(false);
 				emplacements.executeBatch();
 				conn.setAutoCommit(true);
 		}
 		catch(SQLException e){
 			System.out.println("Impossible de créer l'espace de stockage !");
 		}
 	}
 	
 	/**
 	 * Renvoie le nombre d'emplacements libres (tous types confondus) 
 	 * @return Nombre d'emplacements libres
 	 */
 	public int countEmplacementsDispo(){
 		Connection conn = DbConn.getInstance();
 		int dispo = 0;
 		
 		try{
 			Statement stat = conn.createStatement();
 			ResultSet rs = stat.executeQuery("SELECT COUNT(*) AS dispo FROM emplacement a LEFT JOIN container b ON a.emplacement_id = b.emplacement_id WHERE b.emplacement_id ISNULL;");
 			if(rs.next()) {
 				dispo = rs.getInt("dispo");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			// TODO THROW EXCEPTION
 			System.out.println(e.getMessage());
 		}
 		
 		return dispo;
 	}
 	
 	/**
 	 * Renvoie le nombre d'emplacements normaux libres
 	 * @param type Le type d'emplacements à compter
 	 * @return Nombre d'emplacements libres
 	 */
 	public int countEmplacementsDispo(int type){
 		Connection conn = DbConn.getInstance();
 		int dispo = 0;
 		
 		try{
 			PreparedStatement stat = conn.prepareStatement("SELECT COUNT(*) AS dispo FROM emplacement a LEFT JOIN container b ON a.emplacement_id = b.emplacement_id WHERE a.type_id = ? AND b.emplacement_id ISNULL;");
 			stat.setInt(1, type);
 			ResultSet rs = stat.executeQuery();
 			if(rs.next()) {
 				dispo = rs.getInt("dispo");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			// TODO THROW EXCEPTION
 			System.out.println(e.getMessage());
 		}
 		
 		return dispo;
 	}
 	
 	
 	/**
 	 * Renvoie le nombre d'emplacements frigorifiques libres 
 	 * @return Nombre d'emplacements libres
 	 */
 	public int getNbEmplacementsSurtarifesDispo(){
 		Connection conn = DbConn.getInstance();
 		int dispo = 0;
 		
 		try{
 			Statement stat = conn.createStatement();
 			ResultSet rs = stat.executeQuery("SELECT COUNT(*) AS dispo FROM emplacement a LEFT JOIN container b ON a.emplacement_id = b.emplacement_id WHERE a.type_id = 2 AND b.emplacement_id ISNULL;");
 			if(rs.next()) {
 				dispo = rs.getInt("dispo");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			// TODO THROW EXCEPTION
 			System.out.println(e.getMessage());
 		}
 		
 		return dispo;
 	}
 
 	/** 
 	* Permet de stocker un container. ie modifie son emplacement (de null) à 0,1 ou 2
 	* @param container_id id du container
 	* @param emplacement_id id de l'emplacement
 	*/
 	public void storeContainer(int container_id, int emplacement_id) throws ContainerException
 	{
 		Connection conn = DbConn.getInstance();
 		int type_id = 0;
 		//On vérifie que le container existe
 		try{
 			PreparedStatement stat = conn.prepareStatement("SELECT container_id, type_id FROM container WHERE container_id=? LIMIT 1;");
 			stat.setInt(1, container_id);
 			ResultSet rs = stat.executeQuery();
 			if(!rs.next()) {
 				throw new ContainerException("Le container n'existe pas");
 			}
 			else
 			{
 				type_id = rs.getInt("type_id");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			System.out.println(e.getMessage());
 		}
 		
 		int type_emplacement = 0;
 		//On vérifie que l'emplacement est disponible
 		try{
 			PreparedStatement stat = conn.prepareStatement("SELECT a.type_id, a.emplacement_id FROM emplacement a LEFT JOIN container b ON a.emplacement_id=b.emplacement_id WHERE a.emplacement_id=? AND b.container_id ISNULL");
 			stat.setInt(1, emplacement_id);
 			ResultSet rs = stat.executeQuery();
 			if(!rs.next()) {
				throw new ContainerException("L'emplacement est indisponible"+emplacement_id);
 			}
 			else
 			{
 				type_emplacement = rs.getInt("type_id");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			System.out.println(e.getMessage());
 		}
 
 		//On vérifie que si l'emplacement est frigo ou privilégié et que le container est de type normal
 		//il ne faut pas le mettre sur l'emplacement
 		if ((type_emplacement == 1 || type_emplacement == 2) && (type_id == 0))
 		{
			throw new ContainerException("La mise en place d'un container normal sur un emplacement frigorifique ou privilégié est impossible");
 		}
 
 		//On stocke alors le container dans l'emplacement
 		try{
 			PreparedStatement stat = conn.prepareStatement("UPDATE container SET emplacement_id=? WHERE container_id=?");
 			stat.setInt(1,emplacement_id);
 			stat.setInt(2,container_id);
 			stat.executeUpdate();
 		}
 		catch(SQLException e){
 			System.out.println(e.getMessage());
 		}
 	}
   
 	/**
 	 * Traite les containers en attente. Stocke les containers qui peuvent l'être, laisse les autres dans la zone de stockage
 	 */
 	public void traiterAttente(){
 		// C'est là qu'est le gros du travail ;-)
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * -----------------------------------------------------------------
 	 * 
 	 * Implémentation de l'interface Entrepôt
 	 * 
 	 * -----------------------------------------------------------------
 	 */
 	
 		
 	/**
 	 * Renvoie le nombre de containers stockés
 	 * @return Le nombre de containers (types confondus) stockés
 	 */
 	public int countContainers(){
 		Connection conn = DbConn.getInstance();
 		int enStock = 0;
 		
 		try{
 			Statement stat = conn.createStatement();
 			ResultSet rs = stat.executeQuery("SELECT COUNT(*) AS stock FROM container WHERE emplacement_id NOT NULL;");
 			if(rs.next()) {
 				enStock = rs.getInt("stock");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			// TODO THROW EXCEPTION
 			System.out.println(e.getMessage());
 		}
 		
 		return enStock;
 	}
 	
 	
 	/**
 	 * Renvoie le nombre de containers d'un type donné stockés
 	 * @return Le nombre de containers du type donné stockés
 	 */
 	public int countContainers(int type){
 		Connection conn = DbConn.getInstance();
 		int enStock = 0;
 		
 		try{
 			PreparedStatement stat = conn.prepareStatement("SELECT COUNT(*) AS stock FROM container WHERE emplacement_id NOT NULL AND type_id= ?;");
 			stat.setInt(1, type);
 			ResultSet rs = stat.executeQuery();
 			if(rs.next()) {
 				enStock = rs.getInt("stock");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			// TODO THROW EXCEPTION
 			System.out.println(e.getMessage());
 		}
 		
 		return enStock;
 	}
 	
 	
 	/**
 	 * Enlève le container demandé de la zone de stockage
 	 * @param id L'id du container à enlever
 	 * */
 	public void removeContainerById(int id){
 		Connection conn = DbConn.getInstance();
 		
 		try{
 			PreparedStatement stat = conn.prepareStatement("DELETE FROM container WHERE container_id=? AND emplacement_id NOT NULL;");
 			stat.setInt(1, id);
 			stat.execute();
 		}
 		catch(SQLException e){
 			System.out.println("Erreur SQL");
 		}
 	}
 	
 	/**
 	 * Enlève un container du type demandé de la zone de stockage
 	 * @param type Le type de container à enlever
 	 * */
 	public void removeContainerByType(int type){
 		Connection conn = DbConn.getInstance();
 		Integer id = null;
 		
 		try{
 			// On repère le container à enlever (du type qu'on veut)
 			PreparedStatement toDelete = conn.prepareStatement("SELECT container_id FROM container a NATURAL JOIN type b WHERE a.type_id=? AND a.emplacement_id NOT NULL ORDER BY b.type_id ASC LIMIT 1;");
 			toDelete.setInt(1, type);
 			ResultSet rs = toDelete.executeQuery();
 			if(rs.next()) {
 				id = rs.getInt("container_id");
 			}
 			rs.close();
 			
 			if(id != null){
 				PreparedStatement delete = conn.prepareStatement("DELETE FROM container WHERE container_id=?;");
 				delete.setInt(1, id);
 				delete.executeUpdate();
 			}
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Enlève tous les containers du type demandé de la zone de stockage
 	 * @param type Le type de containers à enlever
 	 * */
 	public void removeContainersByType(int type){
 		Connection conn = DbConn.getInstance();
 		
 		try{
 			PreparedStatement stat = conn.prepareStatement("DELETE FROM container WHERE type_id=? AND emplacement_id NOT NULL;");
 			stat.setInt(1, type);
 			stat.execute();
 		}
 		catch(SQLException e){
 			System.out.println("Erreur SQL");
 		}
 	}
 	
 	/** Vide la zone de stockage */
 	public void empty(){
 		Connection conn = DbConn.getInstance();
 		try{
 			Statement stat = conn.createStatement();
 			stat.executeUpdate("DELETE FROM container WHERE emplacement_id NOT NULL;");
 		}
 		catch(SQLException e){
 			System.out.println("Erreur SQL");
 		}
 	}
 }
