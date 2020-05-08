 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: Query.java,v 1.3 2003-04-09 04:24:11 shahbaz.javeed Exp $
  */
 
 package com.netspective.axiom.sql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.exception.NestableRuntimeException;
 
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.text.ExpressionText;
 import com.netspective.commons.text.ValueSourceOrJavaExpressionText;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.axiom.value.DatabaseConnValueContext;
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.DatabasePolicies;
 
 import javax.naming.NamingException;
 
 public class Query
 {
     protected static final Log log = LogFactory.getLog(Query.class);
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options();
     public static final String LISTPARAM_PREFIX = "param-list:";
     public static long queryNumber = 0;
 
     static
     {
         XML_DATA_MODEL_SCHEMA_OPTIONS.addIgnoreAttributes(new String[] { "sql-dynamic", "sql-text-has-expressions" });
         XML_DATA_MODEL_SCHEMA_OPTIONS.addIgnoreNestedElements(new String[] { "presentation" } );
         XML_DATA_MODEL_SCHEMA_OPTIONS.setPcDataHandlerMethodName("appendSqlText");
     }
 
     private class QuerySqlExpressionText extends ValueSourceOrJavaExpressionText
     {
         public QuerySqlExpressionText(String staticExpr, Map vars)
         {
             super(staticExpr, vars);
         }
 
         public QuerySqlExpressionText(String staticExpr)
         {
             super(staticExpr);
         }
 
         protected String getReplacement(ValueContext vc, String entireText, String replaceToken)
         {
             if(replaceToken.startsWith(LISTPARAM_PREFIX)) // format is param:#
             {
                 StringBuffer sb = new StringBuffer();
                 try
                 {
                     int paramNum = Integer.parseInt(replaceToken.substring(LISTPARAM_PREFIX.length()));
                     if(paramNum >= 0 && paramNum < parameters.size())
                     {
                         QueryParameter param = (QueryParameter) parameters.get(paramNum);
                         if(!param.isListType())
                             throw new RuntimeException("Query '"+ getNameForMapKey() +"': only list parameters may be specified here (param '" + paramNum + "')");
 
                         ValueSource source = param.getValue();
                         String[] values = source.getTextValues(vc);
 
                         for(int q = 0; q < values.length; q++)
                         {
                             if(q > 0)
                                 sb.append(", ");
                             sb.append("?");
                         }
                     }
                     else
                         throw new RuntimeException("Query '"+ getQualifiedName() +"': parameter '" + paramNum + "' does not exist");
                 }
                 catch(Exception e)
                 {
                     throw new NestableRuntimeException(e);
                 }
 
                 return sb.toString();
             }
             else
                 return super.getReplacement(vc, entireText, replaceToken);
         }
     }
 
     private class QueryDbmsSqlTexts extends DbmsSqlTexts
     {
         public QueryDbmsSqlTexts()
         {
             super(Query.this, "query");
         }
 
         public ExpressionText createExpr(String sql)
         {
             return new QuerySqlExpressionText(sql, createVarsMap());
         }
     }
 
     private QueriesNameSpace nameSpace;
     private String queryName;
     private boolean sqlTextHasExpressions;
     private ValueSource dataSourceId;
     private QueryDbmsSqlTexts sqlTexts = new QueryDbmsSqlTexts();
     private QueryParameters parameters;
     private QueryExecutionLog execLog = new QueryExecutionLog();
 
     public Query()
     {
         queryNumber++;
         setName(this.getClass().getName() + "-" + queryNumber);
     }
 
     public Query(QueriesNameSpace nameSpace)
     {
         queryNumber++;
         setName(this.getClass().getName() + "-" + queryNumber);
         setNameSpace(nameSpace);
     }
 
     public static String translateNameForMapKey(String name)
     {
         return name.toUpperCase();
     }
 
     public String getNameForMapKey()
     {
         return translateNameForMapKey(getQualifiedName());
     }
 
     public QueriesNameSpace getNameSpace()
     {
         return nameSpace;
     }
 
     public void setNameSpace(QueriesNameSpace pkg)
     {
         this.nameSpace = pkg;
     }
 
     public String getQualifiedName()
     {
         return nameSpace != null ? nameSpace.getNameSpaceId() + "." + queryName : queryName;
     }
 
     public String getName()
     {
         return queryName;
     }
 
     public void setName(String name)
     {
         this.queryName = name;
     }
 
     public QueryParameters getParams()
     {
         return parameters;
     }
 
     public QueryParameters createParams()
     {
         return new QueryParameters(this);
     }
 
     public void addParams(QueryParameters params)
     {
         this.parameters = params;
     }
 
     public ValueSource getDataSrc()
     {
         return dataSourceId;
     }
 
     public void setDataSrc(ValueSource dataSourceId)
     {
         this.dataSourceId = dataSourceId;
     }
 
     public boolean isSqlTextHasExpressions()
     {
         return sqlTextHasExpressions;
     }
 
     public void setSqlTextHasExpressions(boolean sqlTextHasExpressions)
     {
         this.sqlTextHasExpressions = sqlTextHasExpressions;
     }
 
     public QueryExecutionLog getExecLog()
     {
         return execLog;
     }
 
     public DbmsSqlText createSql()
     {
         return sqlTexts.create();
     }
 
     public void addSql(DbmsSqlText text)
     {
         sqlTexts.add(text);
     }
 
     public void appendSqlText(String sql)
     {
         DbmsSqlText sqlText = sqlTexts.getByDbms(DatabasePolicies.DBPOLICY_ANSI);
         if(sqlText == null)
             setSqlText(sql);
         else
             sqlText.addText(sql);
     }
 
     protected void setSqlText(String sql)
     {
         DbmsSqlText text = sqlTexts.create();
         text.setSql(sql);
         sqlTexts.add(text);
     }
 
     public QueryDbmsSqlTexts getSqlTexts()
     {
         return sqlTexts;
     }
 
     public String getSqlText(ConnectionContext cc) throws NamingException, SQLException
     {
         DbmsSqlText sqlText = sqlTexts.getByDbmsOrAnsi(cc.getDatabasePolicy());
         return sqlText != null ? sqlText.getSql(cc) : null;
     }
 
     public void trace(ConnectionContext cc, Object[] overrideParams) throws NamingException, SQLException
     {
         StringBuffer traceMsg = new StringBuffer();
         traceMsg.append(QueryExecutionLogEntry.class.getName() + " '"+ getQualifiedName() +"' at "+ cc.getContextLocation() +"\n");
         traceMsg.append(getSqlText(cc));
         if(overrideParams != null)
         {
             for(int i = 0; i < overrideParams.length; i++)
             {
                 traceMsg.append("["+ i +"] ");
                 traceMsg.append(overrideParams[i]);
                 if(overrideParams[i] != null)
                     traceMsg.append(" (" + overrideParams[i].getClass().getName() + ")");
                 traceMsg.append("\n");
             }
         }
         else
         {
             QueryParameters params = getParams();
             if(params != null)
             {
                 for(int i = 0; i < params.size(); i++)
                     ((QueryParameter) params.get(i)).appendBindText(traceMsg, cc, "\n");
             }
         }
         log.trace(traceMsg);
     }
 
     public String createExceptionMessage(ConnectionContext cc, Object[] overrideParams) throws NamingException, SQLException
     {
         StringBuffer text = new StringBuffer();
 
         text.append("Query id = ");
         text.append(getQualifiedName());
         text.append("\n");
         text.append(getSqlText(cc));
         text.append("\n");
         if(parameters != null)
         {
             if(overrideParams != null)
             {
                 text.append("\nBind Parameters (overridden in method):\n");
                 for(int i = 0; i < overrideParams.length; i++)
                 {
                     text.append("["+ (i + 1) +"] ");
                     text.append(overrideParams[i] + " ("+ overrideParams[i].getClass().getName() +")");
                 }
             }
             else if(parameters != null)
             {
                 text.append("\nBind Parameters (in query):\n");
                 for(int i = 0; i < parameters.size(); i++)
                     ((QueryParameter) parameters.get(i)).appendBindText(text, cc, "\n");
             }
             text.append("\n");
         }
         return text.toString();
     }
 
     protected QueryResultSet executeAndRecordStatistics(ConnectionContext cc, Object[] overrideParams, boolean scrollable) throws NamingException, SQLException
     {
         if(log.isTraceEnabled()) trace(cc, overrideParams);
         QueryExecutionLogEntry logEntry = execLog.createNewEntry(cc, this.getQualifiedName());
         try
         {
             logEntry.registerGetConnectionBegin();
             Connection conn = cc.getConnection();
             logEntry.registerGetConnectionEnd(conn);
             PreparedStatement stmt = null;
             String sql = getSqlText(cc);
             if (scrollable)
                 stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             else
                 stmt = conn.prepareStatement(sql);
 
             logEntry.registerBindParamsBegin();
             if(overrideParams != null)
             {
                 for(int i = 0; i < overrideParams.length; i++)
                     stmt.setObject(i + 1, overrideParams[i]);
             }
             else if(parameters != null)
                 parameters.apply(cc, stmt);
             logEntry.registerBindParamsEnd();
 
             logEntry.registerExecSqlBegin();
             boolean executStmtResult = stmt.execute();
             logEntry.registerExecSqlEndSuccess();
             return new QueryResultSet(this, cc, executStmtResult, stmt.getResultSet(), logEntry);
         }
         catch(SQLException e)
         {
             logEntry.registerExecSqlEndFailed();
             log.error(createExceptionMessage(cc, overrideParams), e);
             throw e;
         }
         finally
         {
             logEntry.finalize(cc, log);
         }
     }
 
     protected QueryResultSet executeAndIgnoreStatistics(ConnectionContext cc, Object[] overrideParams, boolean scrollable) throws NamingException, SQLException
     {
         if(log.isTraceEnabled()) trace(cc, overrideParams);
         try
         {
             Connection conn = cc.getConnection();
             PreparedStatement stmt = null;
             String sql = getSqlText(cc);
             if (scrollable)
                 stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             else
                 stmt = conn.prepareStatement(sql);
 
             if(overrideParams != null)
             {
                 for(int i = 0; i < overrideParams.length; i++)
                     stmt.setObject(i + 1, overrideParams[i]);
             }
             else if(parameters != null)
                 parameters.apply(cc, stmt);
 
             boolean executStmtResult = stmt.execute();
             return new QueryResultSet(this, cc, executStmtResult, stmt.getResultSet());
         }
         catch(SQLException e)
         {
             log.error(createExceptionMessage(cc, overrideParams), e);
             throw e;
         }
     }
 
     protected QueryResultSet executeAndRecordStatistics(DatabaseConnValueContext dbvc, Object[] overrideParams, boolean scrollable) throws SQLException, NamingException
     {
         String dataSrcIdText = dataSourceId != null ? dataSourceId.getTextValue(dbvc) : null;
         return executeAndRecordStatistics(
                     dataSrcIdText != null ? dbvc.getConnection(dataSrcIdText, false) : dbvc.getConnection(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE, false),
                     overrideParams, scrollable);
     }
 
     protected QueryResultSet executeAndIgnoreStatistics(DatabaseConnValueContext dbvc, Object[] overrideParams, boolean scrollable) throws SQLException, NamingException
     {
         String dataSrcIdText = dataSourceId == null ? null : dataSourceId.getTextValue(dbvc);
         return executeAndIgnoreStatistics(
                     dataSrcIdText != null ? dbvc.getConnection(dataSrcIdText, false) : dbvc.getConnection(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE, false),
                     overrideParams, scrollable);
     }
 
     public QueryResultSet execute(DatabaseConnValueContext dbvc, Object[] overrideParams, boolean scrollable) throws NamingException, SQLException
     {
         return log.isInfoEnabled() ? executeAndRecordStatistics(dbvc, overrideParams, scrollable) : executeAndIgnoreStatistics(dbvc, overrideParams, scrollable);
     }
 
     public QueryResultSet execute(DatabaseConnValueContext dbvc, String dataSourceId, Object[] overrideParams) throws NamingException, SQLException
     {
		this.dataSourceId = null == dataSourceId ? null : ValueSources.getInstance().getValueSource(dataSourceId, ValueSources.VSNOTFOUNDHANDLER_NULL);
         return log.isInfoEnabled() ? executeAndRecordStatistics(dbvc, overrideParams, false) : executeAndIgnoreStatistics(dbvc, overrideParams, false);
     }
 
     public QueryResultSet execute(ConnectionContext cc, Object[] overrideParams, boolean scrollable) throws NamingException, SQLException
     {
        return log.isInfoEnabled() ? executeAndRecordStatistics(cc, overrideParams, scrollable) : executeAndIgnoreStatistics(cc, overrideParams, scrollable);
     }
 }
