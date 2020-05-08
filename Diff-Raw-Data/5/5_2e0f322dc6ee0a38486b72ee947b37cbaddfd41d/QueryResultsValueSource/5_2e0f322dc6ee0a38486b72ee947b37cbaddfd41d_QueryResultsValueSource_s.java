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
 * $Id: QueryResultsValueSource.java,v 1.5 2003-05-30 23:06:54 shahid.shah Exp $
  */
 
 package com.netspective.axiom.value.source;
 
 import java.util.StringTokenizer;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.exception.NestableRuntimeException;
 
 import com.netspective.commons.value.source.AbstractValueSource;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.value.ValueSourceSpecification;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.value.CachedValue;
 import com.netspective.commons.value.GenericValue;
 import com.netspective.commons.value.ValueSourceDocumentation;
 import com.netspective.commons.value.PresentationValue;
 import com.netspective.commons.value.exception.ValueSourceInitializeException;
 import com.netspective.commons.xdm.XdmComponentFactory;
 import com.netspective.commons.xdm.exception.DataModelException;
 import com.netspective.commons.io.Resource;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.sql.Query;
 import com.netspective.axiom.SqlManagerComponent;
 import com.netspective.axiom.SqlManager;
 import com.netspective.axiom.value.DatabaseConnValueContext;
 
 public class QueryResultsValueSource extends AbstractValueSource
 {
     private static final Log log = LogFactory.getLog(QueryResultsValueSource.class);
     static private String[] RESULT_STYLE_NAMES = new String[] { "single-column", "row-map", "multi-row-map", "single-row-array", "multi-row-matrix", "result-set", "presentation" };
 
     public static final String[] IDENTIFIERS = new String[] { "query" };
     public static final ValueSourceDocumentation DOCUMENTATION = new ValueSourceDocumentation(
             "Executes a static query and returns the results.",
             new ValueSourceDocumentation.Parameter[]
             {
                 new ValueSourceDocumentation.Parameter("query-source", true, "The format is 'query-source/query-id@data-source-id'. Where the only required "+
                                                                                          "item is the query-id. Query-source may be either a static value or a value source and may resolve to either a " +
                                                                                          "resource id or a file name. If a resource id is required, use 'r resourceId' (prefix 'r ' in front of the value "+
                                                                                          "to indicate it's a resource). The Query id is always a static text item and data-source-id may be a value source, "+
                                                                                          "null, or a static text string."),
                 new ValueSourceDocumentation.Parameter("style", false, RESULT_STYLE_NAMES, "multi-row-matrix", "The style of result requested."),
                 new ValueSourceDocumentation.Parameter("cache-timeout", false, "0", "Number of milliseconds to cache the query results."),
                 new ValueSourceDocumentation.Parameter("params", false, "Bind parameters.")
             }
     );
 
     static public final int RESULTSTYLE_SINGLECOLUMN_OBJECT = 0;
     static public final int RESULTSTYLE_FIRST_ROW_MAP_OBJECT = 1;
     static public final int RESULTSTYLE_ALL_ROWS_MAP_LIST = 2;
     static public final int RESULTSTYLE_FIRST_ROW_LIST = 3;
     static public final int RESULTSTYLE_ALL_ROWS_LIST = 4;
     static public final int RESULTSTYLE_RESULTSET = 5;
     static public final int RESULTSTYLE_PRESENTATION = 6;
 
     static private Map cachedValues = new HashMap();
     static private Map cachedPresentationValues = new HashMap();
 
     private int resultStyle = RESULTSTYLE_ALL_ROWS_LIST;
     private ValueSource sourceId;
     private String queryId;
     private long cacheTimeoutMillis;
     private ValueSource dataSourceId;
     private ValueSource[] params;
 
     public static String[] getIdentifiers()
     {
         return IDENTIFIERS;
     }
 
     public static ValueSourceDocumentation getDocumentation()
     {
         return DOCUMENTATION;
     }
 
     public QueryResultsValueSource()
     {
     }
 
     public void setDataSourceId(ValueSource value)
     {
         dataSourceId = value;
     }
 
     /**
      * Assigns the source of the query. The format is 'query-source/query-id@data-source-id'. Where the only required
      * item is the query-id. Query-source may be either a static value or a value source and may resolve to either a
      * resource id or a file name. If a resource id is required, use 'r resourceId' (prefix 'r ' in front of the value
      * to indicate it's a resource). The Query id is always a static text item and data-source-id may be a value source,
      * null, or a static text string.
      * @param params
      */
     public void setSource(String params)
     {
         int dataSrcIdDelim = params.indexOf('@');
         if(dataSrcIdDelim != -1)
         {
             String srcParams = params.substring(0, dataSrcIdDelim);
             int querySrcIdDelim = srcParams.lastIndexOf('/');
             if(querySrcIdDelim != -1)
             {
                 sourceId = ValueSources.getInstance().getValueSourceOrStatic(srcParams.substring(0, querySrcIdDelim));
                 queryId = srcParams.substring(querySrcIdDelim+1);
             }
             else
                 queryId = srcParams;
 
             setDataSourceId(ValueSources.getInstance().getValueSourceOrStatic(params.substring(dataSrcIdDelim+1)));
         }
         else
             dataSourceId = null;
     }
 
     public void initialize(ValueSourceSpecification spec) throws ValueSourceInitializeException
     {
         super.initialize(spec);
 
         StringTokenizer st = new StringTokenizer(spec.getParams(), ",");
         if(st.hasMoreTokens())
             setSource(st.nextToken().trim());
 
         if(st.hasMoreTokens())
         {
             String styleText = st.nextToken().trim();
             if(styleText.equals("-"))
                 resultStyle = RESULTSTYLE_ALL_ROWS_LIST;
             else
             {
                 for(int i = 0; i < RESULT_STYLE_NAMES.length; i++)
                 {
                     resultStyle = -1;
                     if(styleText.equalsIgnoreCase(RESULT_STYLE_NAMES[i]))
                     {
                         resultStyle = i;
                         break;
                     }
                     if(resultStyle == -1)
                         throw new ValueSourceInitializeException("Invalid style '"+ styleText +"' specified", this, spec);
                 }
             }
         }
 
         if(st.hasMoreTokens())
         {
             String cacheAmount = st.nextToken().trim();
             if(cacheAmount.equals("-"))
                 cacheTimeoutMillis = 0;
             else
                 cacheTimeoutMillis = Long.parseLong(cacheAmount);
         }
 
         if(st.hasMoreTokens())
         {
             List queryParams = new ArrayList();
             while(st.hasMoreTokens())
                 queryParams.add(ValueSources.getInstance().getValueSourceOrStatic(st.nextToken().trim()));
             params = (ValueSource[]) queryParams.toArray(new ValueSource[queryParams.size()]);
         }
     }
 
     public SqlManager getSqlManager(DatabaseConnValueContext dbcvc) throws DataModelException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, NoSuchMethodException
     {
         SqlManager result = null;
         if(sourceId != null)
         {
             SqlManagerComponent component = null;
 
             String sourceIdText = sourceId.getTextValue(dbcvc);
             if(sourceIdText == null)
                 throw new RuntimeException("sourceId returned null text value in " + this);
 
             if(sourceIdText.startsWith("r "))
             {
                 String resourceId = sourceIdText.substring(2);
                 component = (SqlManagerComponent) XdmComponentFactory.get(SqlManagerComponent.class, new Resource(QueryResultsValueSource.class, resourceId), XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);
             }
             else
                 component = (SqlManagerComponent) XdmComponentFactory.get(SqlManagerComponent.class, new File(sourceIdText), XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);
             result = component.getManager();
         }
         else
             result = dbcvc.getSqlManager();
 
         return result;
     }
 
     public String getCachedValueKey(ValueContext vc)
     {
         StringBuffer sb = new StringBuffer();
         if(sourceId != null)
             sb.append(sourceId.getTextValue(vc));
         sb.append(" ");
         sb.append(queryId);
 
         if(params != null)
         {
             sb.append(" ");
 
             Object[] parameters = new Object[params.length];
             for(int p = 0; p < params.length; p++)
                 sb.append(parameters[p] = params[p].getValue(vc).getValueForSqlBindParam());
         }
 
         return sb.toString();
     }
 
     public Value getQueryResults(ValueContext vc, int style)
     {
         ValueSources.getInstance().assertValueContextInstance(DatabaseConnValueContext.class, vc, this);
         DatabaseConnValueContext dcvc = (DatabaseConnValueContext) vc;
 
         SqlManager sqlManager = null;
         try
         {
             sqlManager = getSqlManager(dcvc);
             if(sqlManager == null)
                 throw new RuntimeException("Unable to locate SQL Manager for " + this);
         }
         catch (Exception e)
         {
             log.error("Error retrieving SQL Manager", e);
             throw new NestableRuntimeException(e);
         }
 
         Query query = sqlManager.getQuery(queryId);
         if(query == null)
             throw new RuntimeException("Unable to locate Query '"+ queryId +"' in SQL Manager '"+ sqlManager +"' in " + this);
 
         String dataSourceIdText = dataSourceId != null ? dataSourceId.getTextValue(vc) : null;
         QueryResultSet qrs = null;
 
         try
         {
             if(params == null)
                 qrs = query.execute(dcvc, dataSourceIdText, null);
             else
             {
                 Object[] parameters = new Object[params.length];
                 for(int p = 0; p < params.length; p++)
                     parameters[p] = params[p].getValue(vc).getValueForSqlBindParam();
                 qrs = query.execute(dcvc, dataSourceIdText, parameters);
             }
         }
         catch (Exception e)
         {
             log.error("Error executing query", e);
             throw new NestableRuntimeException(e);
         }
 
         Value value = null;
         try
         {
             ResultSet rs = qrs.getResultSet();
             switch(resultStyle)
             {
                 case RESULTSTYLE_SINGLECOLUMN_OBJECT:
                     if(rs.next())
                         value = new GenericValue(rs.getObject(1));
                     else
                         value = null;
                     break;
 
                 case RESULTSTYLE_FIRST_ROW_MAP_OBJECT:
                     if(rs.next())
                     {
                         Map rowMap = new HashMap();
                         ResultSetMetaData rsmd = rs.getMetaData();
                         for(int i = 1; i <= rsmd.getColumnCount(); i++)
                             rowMap.put(rsmd.getColumnName(i), rs.getObject(i));
                         value = new GenericValue(rowMap);
                     }
                     else
                         value = null;
                     break;
 
                 case RESULTSTYLE_ALL_ROWS_MAP_LIST:
                     List rows = new ArrayList();
                     ResultSetMetaData rsmd = rs.getMetaData();
                     while(rs.next())
                     {
                         Map rowMap = new HashMap();
                         for(int i = 1; i <= rsmd.getColumnCount(); i++)
                             rowMap.put(rsmd.getColumnName(i), rs.getObject(i));
                         rows.add(rowMap);
                     }
                     value = new GenericValue(rows);
                     break;
 
                 case RESULTSTYLE_FIRST_ROW_LIST:
                     rsmd = rs.getMetaData();
                     if(rs.next())
                     {
                         List row = new ArrayList();
                         for(int i = 1; i <= rsmd.getColumnCount(); i++)
                             row.add(rs.getObject(i));
                         value = new GenericValue(row);
                     }
                     else
                         value = null;
                     break;
 
                 case RESULTSTYLE_ALL_ROWS_LIST:
                     rsmd = rs.getMetaData();
                     rows = new ArrayList();
                     while(rs.next())
                     {
                         List row = new ArrayList();
                         for(int i = 1; i <= rsmd.getColumnCount(); i++)
                             row.add(rs.getObject(i));
                         rows.add(row);
                     }
                     value = new GenericValue(rows);
                     break;
 
                 case RESULTSTYLE_RESULTSET:
                     value = new GenericValue(qrs);
                     break;
 
                 case RESULTSTYLE_PRESENTATION:
                     PresentationValue pValue = new PresentationValue();
                     PresentationValue.Items items = pValue.createItems();
                     rsmd = rs.getMetaData();
                     rows = new ArrayList();
                     switch(rsmd.getColumnCount())
                     {
                         case 1:
                             while(rs.next())
                                 items.addItem(rs.getString(1));
                             break;
 
                         default:
                             while(rs.next())
                                 items.addItem(rs.getString(1), rs.getString(2));
                             break;
                     }
                     value = pValue;
                     break;
 
                 default:
                     throw new RuntimeException("Invalid style " + resultStyle + " in " + this);
             }
         }
         catch (Exception e)
         {
             log.error("Error retrieving results", e);
             throw new NestableRuntimeException(e);
         }
         finally
         {
             if(resultStyle != RESULTSTYLE_RESULTSET)
             {
                 try
                 {
                     if(qrs != null) qrs.close(true);
                 }
                 catch (SQLException e)
                 {
                     log.error("Error closing result set", e);
                     throw new NestableRuntimeException(e);
                 }
             }
         }
 
         return value;
     }
 
     public Value getValue(ValueContext vc)
     {
         if(cacheTimeoutMillis > 0)
         {
             String cacheKey = getCachedValueKey(vc);
             CachedValue cv = (CachedValue) cachedValues.get(cacheKey);
             if(cv != null)
             {
                 if(cv.isValid())
                     return cv.getValue();
                 else
                     cachedValues.remove(cacheKey);
             }
 
             Value value = getQueryResults(vc, resultStyle);
             cachedValues.put(cacheKey, new CachedValue(value, cacheTimeoutMillis));
             return value;
         }
         else
             return getQueryResults(vc, resultStyle);
     }
 
     public PresentationValue getPresentationValue(ValueContext vc)
     {
         if(cacheTimeoutMillis > 0)
         {
             String cacheKey = getCachedValueKey(vc);
             CachedValue cv = (CachedValue) cachedPresentationValues.get(cacheKey);
             if(cv != null)
             {
                 if(cv.isValid())
                     return (PresentationValue) cv.getValue();
                 else
                     cachedPresentationValues.remove(cacheKey);
             }
 
             PresentationValue value = (PresentationValue) getQueryResults(vc, RESULTSTYLE_PRESENTATION);
             cachedPresentationValues.put(cacheKey, new CachedValue(value, cacheTimeoutMillis));
             return value;
         }
         else
             return (PresentationValue) getQueryResults(vc, RESULTSTYLE_PRESENTATION);
     }
 
     public boolean hasValue(ValueContext vc)
     {
         return false;
     }
 
     public String toString()
     {
         StringBuffer result = new StringBuffer(super.toString());
         result.append(", source-id: " + sourceId);
         result.append(", query-id: " + queryId);
         result.append(", result-style: " + resultStyle + " ("+ RESULT_STYLE_NAMES[resultStyle] +")");
         result.append(", cache-timeout: " + cacheTimeoutMillis);
        result.append(", params: " + params.length);
         return result.toString();
     }
 }
