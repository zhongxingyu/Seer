 package ar.edu.utn.frba.proyecto.dao.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import ar.edu.utn.frba.proyecto.constants.ConstantsDatatable;
 import ar.edu.utn.frba.proyecto.domain.Analisis;
 import ar.edu.utn.frba.proyecto.domain.Criterio;
 import ar.edu.utn.frba.proyecto.domain.Paso;
 
 import com.mysql.jdbc.Statement;
 
 public class AnalisisDao extends BaseAbmDao<Analisis> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5774061313214568651L;
 
 	@Override
 	public Analisis getFromResult(ResultSet result) {
 		try {
 			Analisis analisis = new Analisis(
 					result.getInt(ConstantsDatatable.ANALISIS_ID),
 					result.getString(ConstantsDatatable.GENERAL_NOMBRE));
 
 			analisis.setFechaUltimaModificacion(getFechaUltimaMod(result));
 
 			return analisis;
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	@Override
 	protected PreparedStatement prepareAddStatement(Analisis element) {
 		String query = "INSERT INTO ANALISIS (nombre,idUsuarioCreacion) VALUES (?,?)";
 		PreparedStatement prepStatement = null;
 		try {
 			prepStatement = conn.prepareStatement(query,
 					Statement.RETURN_GENERATED_KEYS);
 			prepStatement.setString(1, element.getNombre());
 			prepStatement.setInt(2, element.getUsuarioCreacion().getId());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return prepStatement;
 	}
 
 	@Override
 	protected PreparedStatement prepareUpdateStatement(Analisis element) {
 		String query = "UPDATE ANALISIS SET nombre = ?, idUsuarioUltimaMod = ? WHERE idAnalisis = ? ";
 		PreparedStatement prepStatement = null;
 		try {
 			prepStatement = conn.prepareStatement(query);
 			prepStatement.setString(1, element.getNombre());
 			prepStatement.setInt(2, element.getUsuarioUltimaModificacion()
 					.getId());
 			prepStatement.setInt(3, element.getId());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return prepStatement;
 	}
 
 	@Override
 	protected PreparedStatement prepareUniqueStatement(Analisis element) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void addCriteriosToAnalisis(Analisis analisis, Criterio[] criterios) {
 		Connection conn = getConnection();
 		ResultSet result = null;
 		String query = "INSERT INTO Criterio_Por_Analisis (idCriterio,idAnalisis) VALUES (?,?)";
 		try {
 			for (Criterio criterio : criterios) {
 
 				PreparedStatement prepStatement = conn.prepareStatement(query);
 				prepStatement.setInt(1, criterio.getId());
 				prepStatement.setInt(2, analisis.getId());
 
 				prepStatement.executeUpdate();
 			}
 			analisis.setCriterios(Arrays.asList(criterios));
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
 
 	public List<Analisis> getAnalisisByPaso(Paso paso) {
 		Connection conn = getConnection();
 		ResultSet result = null;
 		List<Analisis> analisisList = new ArrayList<Analisis>();
		String query = "SELECT ap.* from Analisis_por_Paso ap WHERE ap.idproducto = ? and ap.idpaso = ? and ap.idversion = (select max(p2.idversion) from receta p2 where ap.idproducto=p2.idproducto) GROUP BY idAnalisis ORDER BY fechaCreacion ASC";
 		try {
 			PreparedStatement prepStatement = conn.prepareStatement(query);
 			prepStatement.setInt(1, paso.getProductoId());
 			prepStatement.setInt(2, paso.getId());
 
 			result = prepStatement.executeQuery();
 
 			while (result.next())
 				analisisList.add(new Analisis(result
 						.getInt(ConstantsDatatable.ANALISIS_ID), ""));
 
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
 
 		return analisisList;
 	}
 
 	public List<Analisis> getAnalisisByPasoT(Paso paso) {
 		Connection conn = getConnection();
 		ResultSet result = null;
 		List<Analisis> analisisList = new ArrayList<Analisis>();
 		String query = "SELECT ana.nombre,ana.idAnalisis,cri.nombre,cri.idCriterio,ap.valoresperado from Analisis_por_Paso ap, Analisis ana, Criterio cri "
 				+ "WHERE ap.idanalisis = ana.idanalisis and ap.idcriterio = cri.idcriterio and ap.idproducto = ? and ap.idpaso = ? "
				+ "and ap.idversion = (select max(p2.idversion) from receta p2 where ap.idproducto=p2.idproducto) "
 				+ "ORDER BY ana.nombre";
 		try {
 			PreparedStatement prepStatement = conn.prepareStatement(query,
 					ResultSet.TYPE_FORWARD_ONLY);
 			prepStatement.setInt(1, paso.getProductoId());
 			prepStatement.setInt(2, paso.getId());
 
 			result = prepStatement.executeQuery();
 
 			result.next();
 
 			while (result.getRow() != 0) {
 				List<Criterio> currentCriterios = new ArrayList<Criterio>();
 				String analisisNombre1 = result.getString(1);
 				int analisisId = result.getInt(2);
 				String analisisNombre2 = analisisNombre1;
 
 				while (result.getRow() != 0 && analisisNombre1.equals(analisisNombre2)) {
 					Criterio criterio = new Criterio(result.getInt(4), result.getString(3),
 							null,
 							result.getString(ConstantsDatatable.VALOR_ESPERADO));
 
 					currentCriterios.add(criterio);
 					result.next();
 					analisisNombre2 = result.getRow() != 0 ? result
 							.getString(1) : "";
 				}
 				Analisis analisis = new Analisis(analisisId, analisisNombre1);
 				analisis.setCriterios(currentCriterios);
 
 				analisisList.add(analisis);
 			}
 
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
 
 		return analisisList;
 	}
 
 }
