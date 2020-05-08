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
 
 package org.seasr.meandre.components.tools.tuples;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
import java.util.Calendar;
 import java.util.List;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.tools.db.AbstractDBComponent;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Tuple To SQL",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.any,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "tuple, tools, database",
         description = "This component writes tuples to a db table",
         dependency = { "protobuf-java-2.2.0.jar", "sqlite-jdbc-3.7.2.jar",
                        "guava-r09.jar", "slf4j-api-1.6.1.jar", "slf4j-log4j12-1.6.1.jar" }
 )
 public class TupleToSQL extends AbstractDBComponent {
 
     //------------------------------ INPUTS -----------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TUPLES,
             description = "tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
     )
     protected static final String IN_TUPLES = Names.PORT_TUPLES;
 
     @ComponentInput(
             name = Names.PORT_META_TUPLE,
             description = "meta data for the tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "table_name",
             description = "The table name where the tuples have been written to" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TABLE_NAME = "table_name";
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             description = "The column definitions (example: name VARCHAR(30) NOT NULL, birthday DATETIME, isRetired BOOLEAN). " +
             		"Note: The names of the columns must match the meta tuple field names, but order is arbitrary. " +
             		"You can also specify just a subset of the tuple's fields, so that only those fields are saved in the database.",
             name = "column_definitions",
             defaultValue = ""
     )
     protected static final String PROP_COLUMNDEFS = "column_definitions";
 
     @ComponentProperty(
             description = "Any table options usually specified after the column definitions " +
             		"(example: ENGINE=InnoDB DEFAULT CHARACTER SET utf8)",
             name = "table_options",
             defaultValue = ""
     )
     protected static final String PROP_TABLE_OPTIONS = "table_options";
 
     @ComponentProperty(
             description = "Set to true if you want the table to be removed when the flow concludes",
             name = "drop_table",
             defaultValue = "true"
     )
     protected static final String PROP_DROP_TABLE = "drop_table";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected static final int MAX_INSERTS_PER_BATCH = 100;
    protected static final Calendar CALENDAR = Calendar.getInstance();
 
     protected String _columnDefs;
     protected String _tableOptions;
 
     protected boolean _dropTable;
     protected boolean _isStreaming = false;
     protected String _currentTableName;
     protected List<String> _currentTableColumns;
 
     protected List<String> _tableNames = new ArrayList<String>();
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         super.initializeCallBack(ccp);
 
         _columnDefs = getPropertyOrDieTrying(PROP_COLUMNDEFS, ccp);
         _tableOptions = getPropertyOrDieTrying(PROP_TABLE_OPTIONS, true, false, ccp);
         _dropTable = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_DROP_TABLE, ccp));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         super.executeCallBack(cc);
 
         componentInputCache.storeIfAvailable(cc, IN_META_TUPLE);
         componentInputCache.storeIfAvailable(cc, IN_TUPLES);
 
         if (connectionPool == null || !componentInputCache.hasData(IN_META_TUPLE) || !componentInputCache.hasData(IN_TUPLES))
             // we're not ready to process yet, return
             return;
 
         Connection connection = null;
         try {
             connection = connectionPool.getConnection();
             PreparedStatement ps = null;
 
             do {
                 Object inMeta = componentInputCache.retrieveNext(IN_META_TUPLE);
                 Object inTuple = componentInputCache.retrieveNext(IN_TUPLES);
 
                 if (inMeta instanceof StreamInitiator || inTuple instanceof StreamInitiator) {
                     if (inMeta instanceof StreamInitiator && inTuple instanceof StreamInitiator) {
                         StreamInitiator siMeta = (StreamInitiator) inMeta;
                         StreamInitiator siTuple = (StreamInitiator) inTuple;
                         if (siMeta.getStreamId() != siTuple.getStreamId())
                             throw new ComponentExecutionException("Streaming error - received different stream ids on different ports!");
 
                         if (siMeta.getStreamId() != streamId) {
                             // Forward the stream delimiter along
                             cc.pushDataComponentToOutput(OUT_TABLE_NAME, siMeta);
                             continue;
                         }
 
                         console.finer("Received StreamInitiator");
                         if (_isStreaming)
                             console.severe("Stream error - start stream marker already received!");
 
                         _currentTableName = createNewTable(connection);
                         _isStreaming = true;
 
                         continue;
                     } else
                         throw new ComponentExecutionException("Unbalanced stream delimiter received!");
                 }
 
                 if (inMeta instanceof StreamTerminator || inTuple instanceof StreamTerminator) {
                     if (inMeta instanceof StreamTerminator && inTuple instanceof StreamTerminator) {
                         StreamTerminator stMeta = (StreamTerminator) inMeta;
                         StreamTerminator stTuple = (StreamTerminator) inTuple;
                         if (stMeta.getStreamId() != stTuple.getStreamId())
                             throw new ComponentExecutionException("Streaming error - received different stream ids on different ports!");
 
                         if (stMeta.getStreamId() != streamId) {
                             // Forward the stream delimiter along
                             cc.pushDataComponentToOutput(OUT_TABLE_NAME, stMeta);
                             continue;
                         }
 
                         console.finer("Received StreamTerminator");
                         if (!_isStreaming)
                             console.severe("Stream error - end stream marker received without a start stream marker!");
 
                         cc.pushDataComponentToOutput(OUT_TABLE_NAME, new StreamInitiator(streamId));
                         cc.pushDataComponentToOutput(OUT_TABLE_NAME, _currentTableName);
                         cc.pushDataComponentToOutput(OUT_TABLE_NAME, new StreamTerminator(streamId));
 
                         _isStreaming = false;
 
                         continue;
                     } else
                         throw new ComponentExecutionException("Unbalanced stream delimiter received!");
                 }
 
                 if (!_isStreaming)
                     _currentTableName = createNewTable(connection);
 
                 SimpleTuplePeer metaPeer  = new SimpleTuplePeer((Strings) inMeta);
                 Strings[] tuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) inTuple);
                 SimpleTuple tuple = metaPeer.createTuple();
 
                 try {
                     ps = connection.prepareStatement(
                             String.format("INSERT INTO %s VALUES (%s);",
                                     _currentTableName, getSQLInsertParams(_currentTableColumns)));
 
                     int count = 0;
 
                     for (Strings t : tuples) {
                         tuple.setValues(t);
 
                         for (int i = 0, iMax = _currentTableColumns.size(); i < iMax; i++) {
                             String tupleValue = tuple.getValue(_currentTableColumns.get(i));
                             ps.setObject(i + 1, tupleValue.length() > 0 ? tupleValue : null);
                         }
 
                         ps.addBatch();
 
                         // only batch MAX_INSERTS_PER_BATCH inserts at a time for better efficiency
                         if (++count > MAX_INSERTS_PER_BATCH - 1) {
                             ps.executeBatch();
                             count = 0;
                         }
                     }
 
                     if (count > 0)
                         ps.executeBatch();
                 }
                 finally {
                     closeStatement(ps);
                     ps = null;
                 }
 
                 if (!_isStreaming)
                     cc.pushDataComponentToOutput(OUT_TABLE_NAME, _currentTableName);
 
             } while (componentInputCache.hasData(IN_META_TUPLE) && componentInputCache.hasData(IN_TUPLES));
         }
         finally {
             releaseConnection(connection);
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         // Do cleanup here (delete created tables)
         if (_dropTable && connectionPool != null && _tableNames.size() > 0) {
             StringBuilder sb = new StringBuilder();
             for (String tableName : _tableNames)
                 sb.append(",").append(tableName);
             String tables = sb.substring(1);
 
             Connection conn = null;
             Statement stmt = null;
             try {
                 conn = connectionPool.getConnection();
                 stmt = conn.createStatement();
                 stmt.execute(String.format("DROP TABLE IF EXISTS %s;", tables));
             }
             finally {
                 releaseConnection(conn, stmt);
             }
         }
 
         super.disposeCallBack(ccp);
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public boolean isAccumulator() {
         return true;
     }
 
     @Override
     public void handleStreamInitiators() throws Exception {
         executeCallBack(componentContext);
     }
 
     @Override
     public void handleStreamTerminators() throws Exception {
         executeCallBack(componentContext);
     }
 
     //--------------------------------------------------------------------------------------------
 
     protected synchronized String generateTableName() {
        return "temp" + Long.toString(CALENDAR.getTimeInMillis());
     }
 
     protected String createNewTable(Connection connection) throws SQLException {
         String tableName = generateTableName();
         Statement stmt = null;
         try {
             stmt = connection.createStatement();
             stmt.execute(String.format("CREATE TABLE %s (%s) %s;", tableName, _columnDefs, _tableOptions));
             _tableNames.add(tableName);
 
             ResultSet rs = stmt.executeQuery(
                     String.format("SELECT column_name FROM information_schema.columns WHERE table_name='%s';", tableName));
             _currentTableColumns = new ArrayList<String>();
             while (rs.next())
                 _currentTableColumns.add(rs.getString(1));
 
             return tableName;
         }
         finally {
             closeStatement(stmt);
         }
     }
 
     protected String getSQLInsertParams(List<String> tableColumns) {
         String params = "";
 
         for (int i = 0, iMax = tableColumns.size(); i < iMax; i++)
             params += ",?";
 
         return params.substring(1);
     }
 }
