 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dao.handler;
 
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.seasar.extension.jdbc.StatementFactory;
 import org.seasar.extension.jdbc.ValueType;
 import org.seasar.extension.jdbc.impl.BasicStatementFactory;
 import org.seasar.extension.jdbc.types.ValueTypes;
 import org.seasar.extension.jdbc.util.ConnectionUtil;
 import org.seasar.extension.jdbc.util.DataSourceUtil;
 import org.seasar.extension.jdbc.util.DatabaseMetaDataUtil;
 import org.seasar.framework.exception.EmptyRuntimeException;
 import org.seasar.framework.exception.SQLRuntimeException;
 import org.seasar.framework.exception.SRuntimeException;
 import org.seasar.framework.util.ResultSetUtil;
 
 /**
  * @author higa
  * 
  */
 public abstract class AbstractBasicProcedureHandler implements ProcedureHandler {

     protected boolean initialised = false;
 
     protected DataSource dataSource_;
 
     protected String procedureName_;
 
     protected String sql_;
 
     protected Integer[] columnInOutTypes_;
 
     protected Integer[] columnTypes_;
 
     protected String[] columnNames_;
 
     protected StatementFactory statementFactory_ = BasicStatementFactory.INSTANCE;
 
     public DataSource getDataSource() {
         return dataSource_;
     }
 
     public void setDataSource(DataSource dataSource) {
         dataSource_ = dataSource;
     }
 
     public String getProcedureName() {
         return procedureName_;
     }
 
     public void setProcedureName(String procedureName) {
         this.procedureName_ = procedureName;
     }
 
     public StatementFactory getStatementFactory() {
         return statementFactory_;
     }
 
     public void setStatementFactory(StatementFactory statementFactory) {
         statementFactory_ = statementFactory;
     }
 
     protected Connection getConnection() {
         if (dataSource_ == null) {
             throw new EmptyRuntimeException("dataSource");
         }
         return DataSourceUtil.getConnection(dataSource_);
     }
 
     protected CallableStatement prepareCallableStatement(Connection connection) {
         if (sql_ == null) {
             throw new EmptyRuntimeException("sql");
         }
         return statementFactory_.createCallableStatement(connection, sql_);
     }
 
     public Object execute(Object[] args) throws SQLRuntimeException {
         Connection connection = getConnection();
         try {
             return execute(connection, args);
         } finally {
             ConnectionUtil.close(connection);
         }
     }
 
     protected String[] confirmProcedureName(DatabaseMetaData dmd)
             throws SQLException {
         String[] names = DatabaseMetaDataUtil.convertIdentifier(dmd,
                 procedureName_).split("\\.");
        final int namesLength = names.length;
         ResultSet rs = null;
         try {
             if (namesLength == 1) {
                 rs = dmd.getProcedures(null, null, names[0]);
             } else if (namesLength == 2) {
                rs = dmd.getProcedures(null, names[0], names[1]);
             } else if (namesLength == 3) {
                 rs = dmd.getProcedures(names[0], names[1], names[2]);
             }
             int len = 0;
             names = new String[3];
             while (rs.next()) {
                 names[0] = rs.getString(1);
                 names[1] = rs.getString(2);
                 names[2] = rs.getString(3);
                 len++;
             }
             if (len < 1) {
                 throw new SRuntimeException("EDAO0012",
                         new Object[] { procedureName_ });
             }
             if (len > 1) {
                 throw new SRuntimeException("EDAO0013",
                         new Object[] { procedureName_ });
             }
             return names;
         } finally {
             ResultSetUtil.close(rs);
         }
     }
 
     protected int initTypes() {
         StringBuffer buff = new StringBuffer();
         buff.append("{ call ");
         buff.append(procedureName_);
         buff.append("(");
         List columnNames = new ArrayList();
         List dataType = new ArrayList();
         List inOutTypes = new ArrayList();
         ResultSet rs = null;
         int outparameterNum = 0;
         Connection connection = null;
         try {
             connection = getConnection();
             DatabaseMetaData dmd = ConnectionUtil.getMetaData(connection);
             String[] names = confirmProcedureName(dmd);
             rs = dmd.getProcedureColumns(names[0], names[1], names[2], null);
             boolean commaRequired = false;
             while (rs.next()) {
                 columnNames.add(rs.getObject(4));
                 int columnType = rs.getInt(5);
                 inOutTypes.add(new Integer(columnType));
                 dataType.add(new Integer(rs.getInt(6)));
                 if (columnType == DatabaseMetaData.procedureColumnIn) {
                     if (commaRequired) {
                         buff.append(",");
                     }
                     buff.append("?");
                     commaRequired = true;
                 } else if (columnType == DatabaseMetaData.procedureColumnReturn) {
                     buff.setLength(0);
                     buff.append("{? = call ");
                     buff.append(procedureName_);
                     buff.append("(");
                 } else if (columnType == DatabaseMetaData.procedureColumnOut
                         || columnType == DatabaseMetaData.procedureColumnInOut) {
                     if (commaRequired) {
                         buff.append(",");
                     }
                     buff.append("?");
                     commaRequired = true;
                     outparameterNum++;
                 } else {
                     throw new SRuntimeException("EDAO0010",
                             new Object[] { procedureName_ });
                 }
             }
         } catch (SQLException e) {
             throw new SQLRuntimeException(e);
         } finally {
             ResultSetUtil.close(rs);
             ConnectionUtil.close(connection);
         }
         buff.append(")}");
         sql_ = buff.toString();
         columnNames_ = (String[]) columnNames.toArray(new String[columnNames
                 .size()]);
         columnTypes_ = (Integer[]) dataType
                 .toArray(new Integer[dataType.size()]);
         columnInOutTypes_ = (Integer[]) inOutTypes
                 .toArray(new Integer[inOutTypes.size()]);
         return outparameterNum;
     }
 
     protected abstract Object execute(Connection connection, Object[] args);
 
     protected void bindArgs(CallableStatement ps, Object[] args)
             throws SQLException {
         if (args == null) {
             return;
         }
         int argPos = 0;
         for (int i = 0; i < columnTypes_.length; i++) {
             if (isOutputColum(columnInOutTypes_[i].intValue())) {
                 ps.registerOutParameter(i + 1, columnTypes_[i].intValue());
             }
             if (isInputColum(columnInOutTypes_[i].intValue())) {
                 ps.setObject(i + 1, args[argPos++], columnTypes_[i].intValue());
             }
         }
     }
 
     protected boolean isInputColum(int columnInOutType) {
         return columnInOutType == DatabaseMetaData.procedureColumnIn
                 || columnInOutType == DatabaseMetaData.procedureColumnInOut;
     }
 
     protected boolean isOutputColum(int columnInOutType) {
         return columnInOutType == DatabaseMetaData.procedureColumnReturn
                 || columnInOutType == DatabaseMetaData.procedureColumnOut
                 || columnInOutType == DatabaseMetaData.procedureColumnInOut;
     }
 
     protected String getCompleteSql(Object[] args) {
         if (args == null || args.length == 0) {
             return sql_;
         }
         StringBuffer buf = new StringBuffer(200);
         int pos = 0;
         int pos2 = 0;
         int index = 0;
         while (true) {
             pos = sql_.indexOf('?', pos2);
             if (pos > 0) {
                 buf.append(sql_.substring(pos2, pos));
                 buf.append(getBindVariableText(args[index++]));
                 pos2 = pos + 1;
             } else {
                 buf.append(sql_.substring(pos2));
                 break;
             }
         }
         return buf.toString();
     }
 
     protected String getBindVariableText(Object bindVariable) {
         if (bindVariable instanceof String) {
             return "'" + bindVariable + "'";
         } else if (bindVariable instanceof Number) {
             return bindVariable.toString();
         } else if (bindVariable instanceof Timestamp) {
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
             return "'" + sdf.format((java.util.Date) bindVariable) + "'";
         } else if (bindVariable instanceof java.util.Date) {
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             return "'" + sdf.format((java.util.Date) bindVariable) + "'";
         } else if (bindVariable instanceof Boolean) {
             return bindVariable.toString();
         } else if (bindVariable == null) {
             return "null";
         } else {
             return "'" + bindVariable.toString() + "'";
         }
     }
 
     protected ValueType getValueType(Class clazz) {
         return ValueTypes.getValueType(clazz);
     }
 }
