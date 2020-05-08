 package com.iver.cit.gvsig.gui.cad.createLayer;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.swing.JOptionPane;
 
 import jwizardcomponent.DefaultJWizardComponents;
 import jwizardcomponent.JWizardPanel;
 
 import org.cresques.cts.IProjection;
 
 import com.hardcode.driverManager.Driver;
 import com.hardcode.driverManager.DriverLoadException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.ui.wizard.WizardAndami;
 import com.iver.cit.gvsig.fmap.core.ICanReproject;
 import com.iver.cit.gvsig.fmap.crs.CRSFactory;
 import com.iver.cit.gvsig.fmap.drivers.ConnectionFactory;
 import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
 import com.iver.cit.gvsig.fmap.drivers.DBException;
 import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.drivers.IConnection;
 import com.iver.cit.gvsig.fmap.drivers.IVectorialJDBCDriver;
 import com.iver.cit.gvsig.fmap.drivers.db.utils.ConnectionWithParams;
 import com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGISWriter;
 import com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGisDriver;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.LayerFactory;
 import com.iver.cit.gvsig.gui.cad.panels.ChooseGeometryType;
 import com.iver.cit.gvsig.gui.cad.panels.JPanelFieldDefinition;
 import com.prodevelop.cit.gvsig.vectorialdb.wizard.NewVectorDBConnectionPanel;
 
 public class NewPostgisLayerWizard implements NewLayerWizard {
     private ChooseGeometryType geometryType;
     private JPanelFieldDefinition fieldDefinition;
     private NewVectorDBConnectionPanel connection;
 
     @Override
     public JWizardPanel[] getPanels(WizardAndami wizard)
 	    throws DriverLoadException {
 	DefaultJWizardComponents components = wizard.getWizardComponents();
 	geometryType = new ChooseGeometryType(components);
 	geometryType.setDriver(getDriver());
 
 	fieldDefinition = new JPanelFieldDefinition(components);
 	fieldDefinition.setWriter(getWriter());
	connection = new NewVectorDBConnectionPanel(
		wizard.getWizardComponents(), PostGisDriver.NAME, 20);
 
	return new JWizardPanel[] { geometryType, fieldDefinition, connection };
     }
 
     @Override
     public FLyrVect createLayer(IProjection projection) throws Exception {
 	String _layerName = geometryType.getLayerName();
 	String _tableName = connection.getTableName();
 	String selectedDriver = geometryType.getSelectedDriver();
 	int geomType = geometryType.getSelectedGeometryType();
 	FieldDescription[] fieldsDesc = fieldDefinition.getFieldsDescription();
 
 	IVectorialJDBCDriver dbDriver = (IVectorialJDBCDriver) LayerFactory
 		.getDM().getDriver(selectedDriver);
 	ConnectionWithParams cwp = connection.getConnectionWithParams();
 	if (cwp == null) {
 	    return null;
 	}
 
 	IConnection conn;
 	try {
 	    conn = ConnectionFactory.createConnection(cwp.getConnectionStr(),
 		    cwp.getUser(), cwp.getPw());
 	} catch (DBException e) {
 	    throw new IOException(e);
 	}
 
 	PostGISWriter writer = getWriter();
 	if (!existTable(conn, cwp.getSchema(), _tableName)) {
 
 	    DBLayerDefinition dbLayerDef = new DBLayerDefinition();
 	    dbLayerDef.setCatalogName(cwp.getDb());
 	    dbLayerDef.setSchema(cwp.getSchema());
 	    dbLayerDef.setTableName(_tableName);
 	    dbLayerDef.setShapeType(geomType);
 	    dbLayerDef.setFieldsDesc(fieldsDesc);
 	    dbLayerDef.setFieldGeometry("the_geom");
 
 	    // create gid & add it to FieldDescription array
 	    dbLayerDef.setNewFieldID();
 
 	    dbLayerDef.setWhereClause("");
 	    String strSRID = projection.getAbrev().substring(5);
 	    dbLayerDef.setSRID_EPSG(strSRID);
 	    dbLayerDef.setConnection(conn);
 
 	    writer.setWriteAll(true);
 	    writer.setCreateTable(true);
 	    writer.initialize(dbLayerDef);
 	    writer.preProcess();
 	    writer.postProcess();
 
 	    if (dbDriver instanceof ICanReproject) {
 		((ICanReproject) dbDriver).setDestProjection(strSRID);
 	    }
 
 	    try {
 		dbDriver.setData(conn, dbLayerDef);
 	    } catch (DBException e) {
 		throw new IOException(e);
 	    }
 
 	    IProjection proj = null;
 	    if (dbDriver instanceof ICanReproject) {
 		ICanReproject reprojectDriver = (ICanReproject) dbDriver;
 		String sourceProjection = reprojectDriver.getSourceProjection(
 			null, null);
 		proj = CRSFactory.getCRS("EPSG:" + sourceProjection);
 	    }
 
 	    return (FLyrVect) LayerFactory.createDBLayer(dbDriver, _layerName,
 		    proj);
 	} else {
 	    JOptionPane.showMessageDialog(null, PluginServices.getText(this,
 		    "table_already_exists_in_database"), PluginServices
 		    .getText(this, "warning_title"),
 		    JOptionPane.WARNING_MESSAGE);
 	    return null;
 	}
     }
 
     private Driver getDriver() throws DriverLoadException {
 	return LayerFactory.getDM().getDriver(PostGisDriver.NAME);
     }
 
     private PostGISWriter getWriter() throws DriverLoadException {
 	return new PostGISWriter();
     }
 
     private boolean existTable(IConnection conex, String schema,
 	    String tableName) throws IOException {
 
 	Statement st = null;
 	boolean exists = false;
 
 	if (schema == null || schema.equals("")) {
 	    schema = " current_schema()::Varchar ";
 	} else {
 	    schema = "'" + schema + "'";
 	}
 
 	String sql = "select relname,nspname "
 		+ "from pg_class inner join pg_namespace "
 		+ "on relnamespace = pg_namespace.oid where "
 		+ " relkind = 'r' and relname = '" + tableName
 		+ "' and nspname = " + schema;
 
 	try {
 	    st = ((ConnectionJDBC) conex).getConnection().createStatement();
 	    ResultSet rs = st.executeQuery(sql);
 	    if (rs.next()) {
 		exists = true;
 	    }
 	    rs.close();
 	    st.close();
 	} catch (SQLException e) {
 	    throw new IOException(e);
 	}
 
 	return exists;
     }
 }
