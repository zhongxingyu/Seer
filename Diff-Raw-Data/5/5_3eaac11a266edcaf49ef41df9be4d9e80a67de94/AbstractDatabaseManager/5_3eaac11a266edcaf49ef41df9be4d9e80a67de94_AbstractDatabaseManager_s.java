 package ch.epfl.data.distribdb.lowlevel;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Abstract implementation of DatabaseManager, essentially to handle connection
  * and disconnection and manage nodes.
  * 
  * @author tranbaoduy
  * 
  */
 public abstract class AbstractDatabaseManager implements DatabaseManager {
 	
 	public static boolean DEBUG = false;
 
     /**
      * Batch size for data shipment (number of tuples for each INSERT query
      * typically used to ship results from source node(s) to destination
      * node(s)). Zero means no splitting into batches is required.
      */
     private int batchSize;
 
     /**
      * Repository of active node connections.
      */
     private final Map<String, Connection> nodes;
 
     /**
      * Default constructor.
      */
     public AbstractDatabaseManager() {
         
         this.batchSize = 0;
         this.nodes = new HashMap<String, Connection>();
     }
 
     @Override
     public void shutDown() {
         // Nothing to do
     }
 
     @Override
     public void setResultShipmentBatchSize(int batchSize) {
 
         if (batchSize < 0) {
             throw new IllegalArgumentException("Invalid batchSize: "
                     + batchSize);
         }
 
         this.batchSize = batchSize;
     }
 
     @Override
     public int getResultShipmentBatchSize() {
 
         return this.batchSize;
     }
 
     @Override
     public void connect(String nodeId, String jdbcUrl, String username,
             String password) throws SQLException {
 
         Properties connectionProps = new Properties();
         connectionProps.put("user", username);
         connectionProps.put("password", password);
 
         Connection conn = DriverManager.getConnection(jdbcUrl, connectionProps);
 
         //System.out.println("Connected to node [" + nodeId + "] @ " + jdbcUrl);
         this.nodes.put(nodeId, conn);
     }
 
     @Override
     public void disconnect(String nodeId) throws SQLException {
 
         if (this.nodes.containsKey(nodeId)) {
             this.nodes.get(nodeId).close();
         }
     }
     
     @Override
     public List<String> getNodeNames() {
     	
     	List<String> nodeNames = new ArrayList<String>(this.nodes.keySet());
     	Collections.sort(nodeNames);
     	return nodeNames;
     }
     
     @Override
     public int getNumNodes() {
         return this.nodes.size();
     }
 
     @Override
     public void execute(String query, String nodeId) throws SQLException {
 
         if (query.isEmpty()) {
             return;
         }
 
         this.checkNodeId(nodeId);
         if(DEBUG) System.out.println("AbstractDatabaseManager::execute {" + query + "} on " + nodeId);
         this.nodes.get(nodeId).createStatement().execute(query);
     }
 
     @Override
     public void execute(String query, List<String> nodeIds,
             String resultTableSchema) throws SQLException, InterruptedException {
 
         this.execute(
                 this.generateInsertQueryFromQuery(query, resultTableSchema),
                 nodeIds);
     }
 
     @Override
     public void execute(String query, String nodeId, String resultTableSchema)
             throws SQLException, InterruptedException {
 
         this.execute(
                 this.generateInsertQueryFromQuery(query, resultTableSchema),
                 nodeId);
     }
 
     @Override
     public void execute(String query, String sourceNodeId,
             String resultTableSchema, String destinationNodeId)
             throws SQLException {
 
         List<String> subQueries = this.executeAndGenerateShipmentQuery(query,
                 sourceNodeId, resultTableSchema);
 
         for (final String subQuery : subQueries) {
             this.execute(subQuery, destinationNodeId);
         }
     }
 
     @Override
     public ResultSet fetch(String query, String nodeId) throws SQLException {
 
         this.checkNodeId(nodeId);
         if(DEBUG) System.out.println("AbstractDatabaseManager::fetch {" + query + "} on " + nodeId);
         return this.nodes.get(nodeId).createStatement().executeQuery(query);
     }
 
     @Override
     public void copyTable(String sourceRelationName, String sourceNodeId,
             String targetRelationSchema, String destinationNodeId)
             throws SQLException {
 
         this.execute("SELECT * FROM " + sourceRelationName, sourceNodeId,
                 targetRelationSchema, destinationNodeId);
     }
 
     /**
      * Executes the given query on the given node and uses the results to
      * construct one "CREATE TABLE" and multiple INSERT queries
      * (one for each batch, according to the configured batch size).
      * 
      * @param query
      *            Single query string
      * @param nodeId
      *            Single node ID
      * @param resultTableSchema
      *            Result table schema
      * 
      * @return Query strings
      * 
      * @throws SQLException
      */
     protected List<String> executeAndGenerateShipmentQuery(String query,
             String nodeId, String resultTableSchema) throws SQLException {
 
         final ResultSet rs = this.fetch(query, nodeId);
         final ResultSetMetaData rsMetaData = rs.getMetaData();
         final List<String> insertQueries = new ArrayList<String>();
 
         StringBuilder insertValues = new StringBuilder("");
         int count = 0;
 
         while (rs.next()) {
 
             insertValues.append("(");
 
             for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
 
                 if (i != 1) {
                     insertValues.append(",");
                 }
 
                 if (rsMetaData.getColumnType(i) == Types.VARCHAR
                         || rsMetaData.getColumnType(i) == Types.CHAR
                         || rsMetaData.getColumnType(i) == Types.DATE
                         || rsMetaData.getColumnType(i) == Types.ARRAY) {
 
                     insertValues.append("'" + rs.getString(i) + "'");
 
                 } else {
                     insertValues.append(rs.getString(i));
                 }
 
             }
 
             insertValues.append("),");
             count++;
 
             if (this.batchSize != 0 && count % this.batchSize == 0) {
                 
                 insertQueries.add(this.generateInsertQuery(resultTableSchema,
                         insertValues));
 
                 insertValues = new StringBuilder("");
             }
         }
 
         insertQueries.add(this.generateInsertQuery(resultTableSchema,
                 insertValues));
 
         final List<String> queries = new ArrayList<String>();
         for (final String insertQuery : insertQueries) {
             if (!insertQuery.isEmpty()) {
                 queries.add(insertQuery);
             }
         }
 
         //if (!queries.isEmpty()) {
 
             queries.add(0, this.generateCreateTableQueryIfNotExists(
                     rsMetaData,
                     resultTableSchema.contains("(") ? resultTableSchema
                             .substring(0, resultTableSchema.indexOf("("))
                             : resultTableSchema));
         //}
 
         return queries;
     }
 
     private String generateInsertQuery(String resultTableSchema,
             final StringBuilder insertQueryValues) throws SQLException {
 
         if (insertQueryValues.length() < 1) {
             return "";
         }
 
         // delete last comma
         insertQueryValues.deleteCharAt(insertQueryValues.length() - 1);
 
         String insertQuery = String.format("INSERT INTO " + resultTableSchema
                 + " VALUES %s", insertQueryValues.toString());
 
         return insertQuery;
     }
 
     /**
      * Constructs a query that creates a table if it doesn't exist yet.
      * 
      * @param rsMetaData
      *            Meta-data from result set
      * @param tableName
      *            Table name
      * 
      * @return CREATE TABLE query string
      * 
      * @throws SQLException
      */
     private String generateCreateTableQueryIfNotExists(
             ResultSetMetaData rsMetaData, String tableName) throws SQLException {
     	
     	return String.format("select createtableifnotexists('%s', '%s');", 
     			tableName, tableSchemaFromMetaData(rsMetaData));
     	
     }
     
     /**
      * Builds the table schema from the data set meta data.
      * 
      * @param rsMetaData
      *            Meta-data from result set
      * 
      * @return Table Schema
      * 
      * @throws SQLException
      */
     public static String tableSchemaFromMetaData(
     		ResultSetMetaData rsMetaData) throws SQLException {
     	
         final StringBuilder createQuery = new StringBuilder();
 
         for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
 
             if (i != 1) {
                 createQuery.append(",");
             }
 
             if ((rsMetaData.getColumnType(i) == Types.VARCHAR
                     || rsMetaData.getColumnType(i) == Types.CHAR)
                    && !rsMetaData.getColumnTypeName(i).equalsIgnoreCase("TEXT")
                    /* PostgreSQL TEXT identifies as Types.VARCHAR, but shouldn't have size */) {
                 createQuery.append(String.format("%s %s(%s)",
                         rsMetaData.getColumnLabel(i),
                         rsMetaData.getColumnTypeName(i),
                         rsMetaData.getColumnDisplaySize(i)));
             } else {
                 createQuery.append(String.format("%s %s",
                         rsMetaData.getColumnLabel(i),
                         rsMetaData.getColumnTypeName(i)));
             }
         }
 
         return createQuery.toString();
     }
 
     /**
      * Checks and throws an exception if the node ID is unknown.
      * 
      * @param nodeId
      *            Node ID
      * 
      * @throws SQLException
      */
     private void checkNodeId(String nodeId) throws SQLException {
         if (!this.nodes.containsKey(nodeId)) {
             throw new SQLException("Uknown node ID: [" + nodeId + "]");
         }
     }
 
     /**
      * Generates the INSERT query string that include a sub (SELECT) query as
      * VALUES, using the given result table schema.
      * 
      * @param query
      *            SELECT query string
      * @param resultTableSchema
      *            Result (target) table schema
      * @return INSERT query string
      */
     private String generateInsertQueryFromQuery(String query,
             String resultTableSchema) {
 
     	query = query.replace("'", "''");
         return String.format("select executeinto('%s', '%s');", query, 
         		resultTableSchema);
     }
 }
