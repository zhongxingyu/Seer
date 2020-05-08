 /**
  *
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, NCSA.  All rights reserved.
  *
  * Developed by:
  * The Automated Learning Group
  * University of Illinois at Urbana-Champaign
  * http://www.seasr.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal with the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject
  * to the following conditions:
  *
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimers.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimers in
  * the documentation and/or other materials provided with the distribution.
  *
  * Neither the names of The Automated Learning Group, University of
  * Illinois at Urbana-Champaign, nor the names of its contributors may
  * be used to endorse or promote products derived from this Software
  * without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  *
  */
 
 package org.seasr.meandre.components.tools.db;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.UUID;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.meandre.support.generic.io.Serializer;
 import org.seasr.meandre.support.generic.io.Serializer.SerializationFormat;
 import org.seasr.meandre.support.generic.util.Tuples.Tuple2;
 import org.seasr.meandre.support.generic.util.UUIDUtils;
 
 @Component(
         name = "Persist To DB",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.any,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "tools, database, db, persistence",
         description = "This component persists the input into a database",
         dependency = {"protobuf-java-2.2.0.jar", "guava-r06.jar", "slf4j-api-1.6.1.jar", "slf4j-log4j12-1.6.1.jar"}
 )
 public class PersistToDB extends AbstractDBComponent {
 
     //------------------------------ INPUTS -----------------------------------------------------
 
     @ComponentInput(
            name = "data1",
             description = "The data to be persisted in the database" +
                           "<br>TYPE: java.lang.Object"
     )
    protected static final String IN_DATA1 = "data1";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "id",
             description = "Identifier for the persisted data" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_ID = "id";
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             name = "db_table",
             description = "The table name where to store the persisted data (will be created if it doesn't exist)",
             defaultValue = ""
     )
     protected static final String PROP_TABLE = "db_table";
 
    @ComponentProperty(
            name = "port1_name",
            description = "The port name of the component's output port that's connected to this component's 'data1' port",
            defaultValue = ""
    )
    protected static final String PROP_PORT1_NAME = "port1_name";

     //--------------------------------------------------------------------------------------------
 
 
     /** This is the table used as a directory of metainformation for persistence "units" */
     public static final String PERSISTENCE_META_TABLE_NAME = "persistence_meta";
 
     protected String _dbTable;
     protected Queue<Object> _inputQueue = new LinkedList<Object>();
 
     protected boolean _ensuredPersistenceTablesExist = false;
     protected boolean _ensuredPersistenceTableExists = false;
 
     protected String _sqlInsertMeta;
     protected String _sqlInsertData;
 
     protected boolean _isStreaming = false;
 
     protected BigDecimal _uuid;
     protected int _seqNo;
 
     /** The number of ports that can contain data to be persisted */
     protected final int _portCount = 1;
 
    protected String _port1Name;


     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         super.initializeCallBack(ccp);
 
         _dbTable = getPropertyOrDieTrying(PROP_TABLE, ccp);
        _port1Name = getPropertyOrDieTrying(PROP_PORT1_NAME, ccp);
 
         _sqlInsertMeta = String.format(
                 "INSERT INTO %s (uuid, table_name, date, streaming, port_count, flow, flow_exec_id) " +
                 "  VALUES (?, ?, NOW(), ?, ?, ?, ?);", PERSISTENCE_META_TABLE_NAME);
 
         _sqlInsertData = String.format(
                 "INSERT INTO %s (uuid, seq_no, data, type, port_name, serializer) " +
                 "  VALUES (?, ?, ?, ?, ?, ?);", _dbTable);
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         super.executeCallBack(cc);
 
        if (cc.isInputAvailable(IN_DATA1))
             // We want to allow queueing of StreamDelimiters so
             // that we know when to process in streaming mode and when not
            _inputQueue.offer(cc.getDataComponentFromInput(IN_DATA1));
 
         if (connectionPool == null || _inputQueue.isEmpty())
             // we're not ready to process yet, return
             return;
 
         if (!_ensuredPersistenceTablesExist)
             ensurePersistenceTablesExist();
 
         Connection connection = null;
         PreparedStatement psMeta = null;
         PreparedStatement psData = null;
         try {
             connection = connectionPool.getConnection();
             connection.setAutoCommit(false);
 
             psMeta = connection.prepareStatement(_sqlInsertMeta);
             psData = connection.prepareStatement(_sqlInsertData);
 
             Object input;
             while ((input = _inputQueue.poll()) != null) {
                 if (input instanceof StreamInitiator) {
                     console.finer("Received StreamInitiator");
                     if (_isStreaming)
                         console.severe("Already received a StreamInitiator! Stream-within-stream is not yet supported!");
 
                     _isStreaming = true;
                     insertPersistenceMetaInfo(psMeta);
 
                     continue;
                 }
 
                 if (input instanceof StreamTerminator) {
                     console.finer("Received StreamTerminator");
                     _isStreaming = false;
                     if (_seqNo > 1) {  // Only commit if non-empty stream
                         connection.commit();
                         cc.pushDataComponentToOutput(OUT_ID,
                                 BasicDataTypesTools.stringToStrings(UUIDUtils.fromBigInteger(_uuid.toBigInteger()).toString()));
                     } else
                         // Empty stream - roll back the insertPersistenceMetaInfo call
                         rollbackTransaction(connection);
 
                     continue;
                 }
 
                 Tuple2<byte[], SerializationFormat> result = Serializer.serializeObject(input);
                 if (result == null) {
                     console.warning(String.format("Could not serialize object of type '%s' - ignoring it...", input.getClass().getName()));
                     continue;
                 }
 
                 if (!_isStreaming)
                     insertPersistenceMetaInfo(psMeta);
 
                 psData.setBigDecimal(1, _uuid);
                 psData.setInt(2, _seqNo++);
                 psData.setBytes(3, result.getT1());
                 psData.setString(4, input.getClass().getName());
                psData.setString(5, _port1Name);
                 psData.setString(6, result.getT2().name());
                 int rowCount = psData.executeUpdate();
                 console.finer(String.format("psData: rowCount=%d", rowCount));
 
                 if (!_isStreaming) {
                     connection.commit();
                     cc.pushDataComponentToOutput(OUT_ID,
                             BasicDataTypesTools.stringToStrings(UUIDUtils.fromBigInteger(_uuid.toBigInteger()).toString()));
                 }
             }
 
         }
         catch (SQLException e) {
             rollbackTransaction(connection);
             throw e;
         }
         finally {
             releaseConnection(connection, psMeta, psData);
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         super.disposeCallBack(ccp);
     }
 
     //--------------------------------------------------------------------------------------------
 
     private void ensurePersistenceTablesExist() throws SQLException {
         String extra = connectionPool.getConfig().getJdbcUrl().contains(":mysql://") ? " ENGINE=InnoDB" : "";
 
         Connection connection = null;
         Statement stmt = null;
         try {
             connection = connectionPool.getConnection();
             connection.setAutoCommit(false);
 
             stmt = connection.createStatement();
             stmt.execute(String.format(
                     "CREATE TABLE IF NOT EXISTS %s (" +
                     "  uuid DECIMAL(39) NOT NULL," +
                     "  table_name VARCHAR(15) NOT NULL," +
                     "  date DATETIME NOT NULL," +
                     "  streaming BOOLEAN NOT NULL," +
                     "  port_count TINYINT NOT NULL," +
                     "  flow VARCHAR(255) NOT NULL," +
                     "  flow_exec_id VARCHAR(255) NOT NULL," +
                     "  PRIMARY KEY (uuid), INDEX (date), INDEX (table_name)" +
                     ")%s;", PERSISTENCE_META_TABLE_NAME, extra));
 
             stmt.execute(String.format(
                     "CREATE TABLE IF NOT EXISTS %s (" +
                     "  uuid DECIMAL(39) NOT NULL," +
                     "  seq_no MEDIUMINT UNSIGNED NOT NULL," +
                     "  data LONGBLOB NOT NULL," +
                     "  type VARCHAR(255) NOT NULL," +
                    "  port_name VARCHAR(30) NOT NULL," +
                     "  serializer ENUM ('protobuf', 'java') NOT NULL," +
                     "  PRIMARY KEY (uuid, seq_no, port_name)," +
                     "  FOREIGN KEY (uuid) REFERENCES %s (uuid)," +
                     "  INDEX (uuid)" +
                     ")%s;", _dbTable, PERSISTENCE_META_TABLE_NAME, extra));
 
             connection.commit();
 
             _ensuredPersistenceTablesExist = true;
         }
         catch (SQLException e) {
             rollbackTransaction(connection);
             throw e;
         }
         finally {
             releaseConnection(connection, stmt);
         }
     }
 
     private void insertPersistenceMetaInfo(PreparedStatement psMeta) throws SQLException {
         _uuid = new BigDecimal(UUIDUtils.toBigInteger(UUID.randomUUID()));
         _seqNo = 1;
 
         psMeta.setBigDecimal(1, _uuid);
         psMeta.setString(2, _dbTable);
         psMeta.setBoolean(3, _isStreaming);
         psMeta.setInt(4, _portCount);
         psMeta.setString(5, componentContext.getFlowID());
         psMeta.setString(6, componentContext.getFlowExecutionInstanceID());
         int rowCount = psMeta.executeUpdate();
         console.finer(String.format("psMeta: rowCount=%d", rowCount));
     }
 }
