 package edu.uoc.tdp.pac4.dao;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 
 
 import edu.uoc.tdp.pac4.beans.Usuari;
 import edu.uoc.tdp.pac4.exception.DAOException;
 
 
 /**
  * Smart Repair 
  * ETIG - TDP PAC 4 Primavera 2013
  * Grup: FiveCoreDumped
  */
 public class GestorConexionDAOImpl extends ConnectionPostgressDB implements GestorConexionDAO {
 
 	/**
 	 * Implementar las operaciones de base de datos
 	 * para el subsistema de Conexion y Mantenimiento
 	 */
 	
 	private static final String QUERY_ALL_USUARIS = "SELECT * FROM USUARI";
 	private static final String QUERY_USUARI_BY_USUARI = "SELECT * FROM USUARI WHERE UPPER(USUARI) = ?";
 	private static final String QUERY_USUARIS_BY_FILTER = "SELECT * FROM USUARI WHERE 1=1 ";
 	private static final String UPDATE_DISABLE_USER = "UPDATE USUARI SET ACTIU = FALSE WHERE ID = ?";
 	
 	public GestorConexionDAOImpl (){
 		super();
 	}
 	
 	public Usuari getUsuariByUsuari(String usuari) throws DAOException {
 		getConnectionDB();	
 		Usuari result = null;
 		PreparedStatement ps = createPrepareStatment(QUERY_USUARI_BY_USUARI, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try{
 			ps.setString(1, usuari);	
 			rs = ps.executeQuery();
 			if (rs.next()){
 				result = new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"),
 						rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 						rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 						rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades"));				
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}	
 	}
 
 	public List<Usuari> getAllUsuaris() throws DAOException {
 		List<Usuari> result = new LinkedList<Usuari>();
 		getConnectionDB();	
 		
 		Statement stm = createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			rs = stm.executeQuery(QUERY_ALL_USUARIS);
 			while (rs.next()){
 				result.add(
 						new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"), 
 								rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 								rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 								rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades")));	
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(),  e);
 		} finally {
 			if (stm!=null) {
 				try {
 					stm.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs!=null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	public void disableUser (int id) throws DAOException {
 		getConnectionDB();
 		PreparedStatement ps = createPrepareStatment(UPDATE_DISABLE_USER, ResultSet.CONCUR_UPDATABLE);
 		try {
 			ps.setInt(1, id);
 			ps.execute();
 		} catch (SQLException e){
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (ps != null){
 				try {
 					ps.close();
 				} catch (SQLException e){
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage() ,e);
 				}
 			}
 		}
 	}
 	
 	public List<Usuari> getUsuarisByFilter (String id, String nif, String nombre, String apellidos, String perfil)
 			throws DAOException {
 		getConnectionDB();
 		List<Usuari> result = new LinkedList<Usuari>();
 		String sql = QUERY_USUARIS_BY_FILTER;
 		if (id.length() > 0)
 			sql += " AND id = " + id;
 		if (nif.length() > 0)
 			sql += " AND nif = '" + nif +"'";
 		if ((nombre.length() > 0))
			sql += " AND nom = '" + nombre +"'";
 		if (apellidos.length() > 0)
			sql += " AND cognoms = '" + apellidos +"'";
 		if (perfil.length() > 0)
 			sql += " AND perfil = '" + perfil +"'";
 		getConnectionDB();	
 		
 		Statement stm = createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = null;
 		try {
 			rs = stm.executeQuery(sql);
 			while (rs.next()){
 				result.add(
 						new Usuari(rs.getInt("id"), rs.getInt("taller"), rs.getString("usuari"), rs.getString("perfil"), 
 								rs.getString("nif"), rs.getString("nom"), rs.getString("cognoms"),
 								rs.getString("contrasenya"), rs.getBoolean("actiu"), rs.getDate("dataAlta"), rs.getDate("dataModificacio"), 
 								rs.getDate("dataBaixa"), rs.getInt("reparacionsAssignades")));	
 			}
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException(DAOException.ERR_SQL, e.getMessage(), e);
 		} finally {
 			if (stm!=null) {
 				try {
 					stm.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 			if (rs!=null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DAOException(DAOException.ERR_RESOURCE_CLOSED, e.getMessage(), e);
 				}
 			}
 		}
 		
 	}
 }
