 package es.udc.cartolab.gvsig.fonsagua.utils;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Vector;
 
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 import es.icarto.gvsig.navtableforms.gui.tables.model.NotEditableTableModel;
 import es.udc.cartolab.gvsig.fonsagua.forms.alternativas.AlternativasForm;
 import es.udc.cartolab.gvsig.users.utils.DBSession;
 import es.udc.cartolab.gvsig.users.utils.DBSessionSpatiaLite;
 
 public class DatabaseDirectAccessQueries {
 
     public static void insertDefaultPreferences(String codAlt)
 	    throws SQLException {
 	DBSession session = DBSession.getCurrentSession();
 	session.insertRow(FonsaguaConstants.dataSchema,
		FonsaguaConstants.preferencesTable,
 		new String[] { AlternativasForm.PKFIELD },
 		new String[] { codAlt });
     }
 
     public static DefaultTableModel getFuentesIntersectingAlternative(
 	    String codAlt) throws SQLException {
 
 	String query = "SELECT fuentes.fuente AS \"Fuente\", fuentes.tipo_fuente AS \"Tipo fuente\", fuentes.aforo AS \"Aforo\", fuentes.q_ecologico AS \"Q eco (l/s)\", (SELECT q_usar FROM fonsagua.fuentes_implicadas WHERE fuente = fuentes.fuente) AS \"Q usar (l/s)\" FROM ( SELECT fuente, tipo_fuente, COALESCE(aforo.aforo,0) AS aforo,  CASE WHEN tipo_fuente IN ('Manantial', 'Punto rio') THEN COALESCE(aforo.aforo,0) * (SELECT coef_q_ecologico FROM fonsagua.preferencias_disenho WHERE cod_alternativa = '####') ELSE NULL END AS q_ecologico FROM fonsagua.fuentes AS fuente JOIN fonsagua.alternativas AS alt ON st_intersects(alt.geom, fuente.geom) FULL OUTER JOIN (select cod_fuente, min(aforo) as aforo from fonsagua.aforos group by cod_fuente) AS aforo ON fuente.cod_fuente = aforo.cod_fuente WHERE alt.cod_alternativa = '####' UNION SELECT fuente, tipo_fuente_alternativa AS tipo_fuente, aforo, q_ecologico FROM fonsagua.alt_fuentes WHERE cod_alternativa = '####' AND existencia_elemento <> 'Existente' UNION SELECT embalse, 'Embalse', aforo, NULL FROM fonsagua.alt_embalses WHERE cod_alternativa = '####' AND existencia_elemento <> 'Existente' ) AS fuentes;";
 	ResultSet rs = convertAndExecuteQuery(codAlt, query);
 
 	DefaultTableModel modelo = new OnlyOneColumnEditable(4);
 	ConversorResultSetADefaultTableModel.rellena(rs, modelo);
 	return modelo;
 
     }
 
     public static DefaultTableModel getComunitiesIntersectingAlternative(
 	    String codAlt) throws SQLException {
 
 	String query = "SELECT c.comunidad AS\" Comunidad\", c.n_habitantes AS \"Habitantes totales\", ci.n_hab_alternativa AS \"Habitantes alternativa\" FROM fonsagua.comunidades AS c JOIN fonsagua.alternativas AS a ON st_intersects(a.geom, c.geom) FULL OUTER JOIN fonsagua.comunidades_implicadas AS ci ON ci.comunidad = c.comunidad AND a.cod_alternativa = ci.cod_alternativa WHERE a.cod_alternativa = '####';";
 	ResultSet rs = convertAndExecuteQuery(codAlt, query);
 
 	DefaultTableModel modelo = new OnlyOneColumnEditable(2);
 	ConversorResultSetADefaultTableModel.rellena(rs, modelo);
 	return modelo;
     }
 
     public static DefaultTableModel getFuentesImplicadasTable(String codAlt)
 	    throws SQLException {
 	String query = "SELECT fuente AS \"Fuente\", tipo_fuente AS \"Tipo fuente\", aforo AS \"Aforo\", q_ecol AS \"Q eco (l/s)\", q_usar AS \"Q usar (l/s)\" FROM fonsagua.fuentes_implicadas WHERE cod_alternativa = '####'";
 	ResultSet rs = convertAndExecuteQuery(codAlt, query);
 
 	DefaultTableModel model = new NotEditableTableModel();
 	ConversorResultSetADefaultTableModel.rellena(rs, model);
 	if (model.getRowCount() == 0) {
 	    model.addRow(new Vector<Object>());
 	}
 
 	return model;
     }
 
     public static DefaultTableModel getComunidadesImplicadasTable(String codAlt)
 	    throws SQLException {
 
 	String query = "SELECT comunidad AS \"Comunidad\", n_habitantes AS \"Habitantes totales\", n_hab_alternativa AS \"Habitantes alternativa\" FROM fonsagua.comunidades_implicadas WHERE cod_alternativa = '####'";
 	ResultSet rs = convertAndExecuteQuery(codAlt, query);
 
 	DefaultTableModel model = new NotEditableTableModel();
 	ConversorResultSetADefaultTableModel.rellena(rs, model);
 	if (model.getRowCount() == 0) {
 	    model.addRow(new Vector<Object>());
 	}
 
 	return model;
     }
 
     public static boolean isValidAlternative(String codAlt) throws SQLException {
 
 	String query = "SELECT demanda, caudal_fuentes FROM fonsagua.alternativas WHERE cod_alternativa = '####'";
 	ResultSet rs = convertAndExecuteQuery(codAlt, query);
 
 	while (rs.next()) {
 	    final double demanda = rs.getDouble("demanda");
 	    final double caudalFuentes = rs.getDouble("caudal_fuentes");
 
 	    if (demanda > 0 && caudalFuentes > 0 && demanda < caudalFuentes) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public static void removeAndInsertModelFuentes(TableModel model, String code)
 	    throws SQLException {
 	DBSession session = DBSession.getCurrentSession();
 
 	String whereClause = getQueryWithCodeInsteadOfPlaceHolders(code,
 		"WHERE cod_alternativa = '####'");
 	session.deleteRows(FonsaguaConstants.dataSchema,
 		FonsaguaConstants.FUENTES_IMPLICADAS, whereClause);
 
 	final String[] columnNames = { "cod_alternativa", "fuente",
 		"tipo_fuente", "aforo", "q_ecol", "q_usar" };
 
 	Object[] values;
 	for (int row = 0; row < model.getRowCount(); row++) {
 	    values = new Object[model.getColumnCount() + 1];
 	    values[0] = code;
 	    values[1] = model.getValueAt(row, 0);
 	    values[2] = model.getValueAt(row, 1);
 	    values[3] = model.getValueAt(row, 2);
 	    values[4] = model.getValueAt(row, 3);
 	    values[5] = model.getValueAt(row, 4);
 	    session.insertRow(FonsaguaConstants.dataSchema,
 		    FonsaguaConstants.FUENTES_IMPLICADAS, columnNames, values);
 	}
 
     }
 
     public static void removeAndInsertModelComunidades(TableModel model,
 	    String code) throws SQLException {
 	DBSession session = DBSession.getCurrentSession();
 
 	String whereClause = getQueryWithCodeInsteadOfPlaceHolders(code,
 		"WHERE cod_alternativa = '####'");
 	session.deleteRows(FonsaguaConstants.dataSchema,
 		FonsaguaConstants.COMUNIDADES_IMPLICADAS, whereClause);
 
 	final String[] columnNames = { "cod_alternativa", "comunidad",
 		"n_habitantes", "n_hab_alternativa" };
 
 	Object[] values;
 	for (int row = 0; row < model.getRowCount(); row++) {
 	    values = new Object[model.getColumnCount() + 1];
 	    values[0] = code;
 	    values[1] = model.getValueAt(row, 0);
 	    values[2] = model.getValueAt(row, 1);
 	    values[3] = model.getValueAt(row, 2);
 	    session.insertRow(FonsaguaConstants.dataSchema,
 		    FonsaguaConstants.COMUNIDADES_IMPLICADAS, columnNames,
 		    values);
 	}
 
     }
 
     public static ResultSet convertAndExecuteQuery(String code, String query)
 	    throws SQLException {
 	DBSession session = DBSession.getCurrentSession();
 	query = getQueryWithCodeInsteadOfPlaceHolders(code, query);
 	query = getQueryWithOutDataSchemaIfSQLiteSession(query);
 
 	Statement statement = session.getJavaConnection().createStatement();
 	ResultSet rs = statement.executeQuery(query);
 	return rs;
     }
 
     public static String getQueryWithCodeInsteadOfPlaceHolders(String code,
 	    String query) {
 	query = query.replace("####", code);
 	query = query.replace("##dataSchema##", FonsaguaConstants.dataSchema);
 	query = query.replace("##preferencesTable##",
		FonsaguaConstants.preferencesTable);
 	return query;
     }
 
     public static String getQueryWithOutDataSchemaIfSQLiteSession(String sql) {
 	DBSession session = DBSession.getCurrentSession();
 	if (session instanceof DBSessionSpatiaLite) {
 	    return sql.replace(FonsaguaConstants.dataSchema, "");
 	}
 	return sql;
     }
 
 }
