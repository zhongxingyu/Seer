 package ar.edu.utn.frba.proyecto.dao.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import ar.edu.utn.frba.proyecto.constants.ConstantsDatatable;
 import ar.edu.utn.frba.proyecto.dao.AbmDao;
 import ar.edu.utn.frba.proyecto.domain.AuditObject;
 import ar.edu.utn.frba.proyecto.domain.Usuario;
 
 import com.mysql.jdbc.Statement;
 
 public abstract class BaseAbmDao<T extends AuditObject> extends BaseDao<T>
 		implements AbmDao<T> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2202133430458748400L;
 
 	@Override
 	public T getByUnique(T element) {
 
 		Connection conn = getConnection();
 		ResultSet result = null;
 		try {
 			PreparedStatement prepStatement = prepareUniqueStatement(element);
 			result = prepStatement.executeQuery();
 			return result.first() ? getFromResult(result) : null;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			releaseConnection(conn);
 		}
 		return null;
 	}
 
 	@Override
 	public void delete(List<T> elements) {
 		conn = getConnection();
 		try {
 			for (T element : elements) {
 				PreparedStatement prepStatement = prepareDeleteStatement(element);
 				prepStatement.execute();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			releaseConnection(conn);
 		}
 
 	}
 
 	@Override
 	public void deleteAll() {
		String query = "DELETE * FROM " + DATATABLE_NAME;
 		conn = getConnection();
 		try {
 			PreparedStatement prepStatement = conn.prepareStatement(query);
 			prepStatement.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			releaseConnection(conn);
 		}
 
 	}
 
 	@Override
 	public void add(T element) {
 		conn = getConnection();
 		ResultSet result = null;
 
 		try {
 			PreparedStatement prepStatement = prepareAddStatement(element);
 
 			prepStatement.executeUpdate();
 			result = prepStatement.getGeneratedKeys();
 
 			if (result.next())
 				element.setId(result.getInt(1));
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (result != null)
 				try {
 					result.close();
 				} catch (SQLException logOrIgnore) {
 				}
 			releaseConnection(conn);
 		}
 	}
 
 	protected abstract PreparedStatement prepareAddStatement(T element);
 
 	protected abstract PreparedStatement prepareUpdateStatement(T element);
 
 	protected PreparedStatement prepareDeleteStatement(T element) {
 		String query = "DELETE FROM " + DATATABLE_NAME + " WHERE "
 				+ DATATABLE_ID + " = ?";
 		PreparedStatement prepStatement = null;
 		try {
 			prepStatement = conn.prepareStatement(query,
 					Statement.RETURN_GENERATED_KEYS);
 			prepStatement.setInt(1, element.getId());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return prepStatement;
 	}
 
 	protected abstract PreparedStatement prepareUniqueStatement(T element);
 
 	@Override
 	public void update(T element) {
 		conn = getConnection();
 		try {
 			PreparedStatement prepStatement = prepareUpdateStatement(element);
 			prepStatement.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			releaseConnection(conn);
 		}
 	}
 
 	protected String getFechaUltimaMod(ResultSet result) throws SQLException {
 		String fechaUltimaMod = result
 				.getString(ConstantsDatatable.AUDIT_FECHA_ULTIMA_MOD) != null ? result
 				.getString(ConstantsDatatable.AUDIT_FECHA_ULTIMA_MOD)
 				.split(" ")[0] : result.getString(
 				ConstantsDatatable.AUDIT_FECHA_CREACION).split(" ")[0];
 
 		return fechaUltimaMod;
 	}
 
 	protected String getFechaCreacion(ResultSet result) throws SQLException {
 		String fechaCreacion = result.getString(
 				ConstantsDatatable.AUDIT_FECHA_CREACION).split(" ")[0];
 
 		return fechaCreacion;
 	}
 
 	protected Integer getIdUsuarioUltimaMod(ResultSet result) throws SQLException {
 		Integer idUsuarioUltimaMod = result.getInt(ConstantsDatatable.AUDIT_USUARIO_ULTIMA_MOD);
 		
 		if ( idUsuarioUltimaMod != null )
 			idUsuarioUltimaMod = result.getInt(ConstantsDatatable.AUDIT_USUARIO_CREACION);
 		
 		return idUsuarioUltimaMod;
 	}
 
 	protected Integer getIdUsuarioCreacion(ResultSet result) throws SQLException {
 		Integer idUsuarioCreacion = result.getInt(ConstantsDatatable.AUDIT_USUARIO_CREACION) ;
 
 		return idUsuarioCreacion;
 	}
 	
 	protected void fillAuditInfo(T elem, ResultSet result) throws SQLException{
 		
 		elem.setFechaCreacion(getFechaCreacion(result));
 		elem.setFechaUltimaModificacion(getFechaUltimaMod(result));
 		elem.setUsuarioCreacion(new Usuario(getIdUsuarioCreacion(result)));
 		elem.setUsuarioUltimaModificacion(new Usuario(getIdUsuarioUltimaMod(result)));
 		
 	}
 }
