 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: QuerySelect.java,v 1.9 2002-12-11 14:05:53 shahid.shah Exp $
  */
 
 package com.netspective.sparx.xaf.querydefn;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.naming.NamingException;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.netspective.sparx.xif.db.DatabaseContext;
 import com.netspective.sparx.xaf.report.ReportBanner;
 import com.netspective.sparx.xaf.report.ReportFrame;
 import com.netspective.sparx.xaf.report.Report;
 import com.netspective.sparx.xaf.report.StandardReport;
 import com.netspective.sparx.xaf.sql.ResultInfo;
 import com.netspective.sparx.util.value.ListValueSource;
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.ValueContext;
 import com.netspective.sparx.util.ClassPath;
 
 public class QuerySelect
 {
     private QueryDefinition queryDefn;
     private String name;
     private String caption;
    private ReportFrame frame = new ReportFrame();
     private ReportBanner banner;
     private boolean distinctRows;
     private boolean isDirty;
     private boolean alwaysDirty;
     private List reportFields = new ArrayList();
     private List orderBy = new ArrayList();
     private QueryConditions conditions = new QueryConditions(null);
     private List groupByFields = new ArrayList();
     private List whereExprs;
     private List errors;
 
     private String selectSql;
     private List bindParams;
     private String reportClassName;
 
     public QuerySelect()
     {
         this.isDirty = true;
         this.distinctRows = true;
     }
 
     public QuerySelect(QueryDefinition queryDefn)
     {
         this.queryDefn = queryDefn;
         this.isDirty = true;
         this.distinctRows = true;
     }
 
     public void setQueryDefn(QueryDefinition queryDefn)
     {
         this.queryDefn = queryDefn;
     }
 
     public void setAlwaysDirty(boolean flag)
     {
         alwaysDirty = flag;
     }
 
     public String getName()
     {
         return name;
     }
 
     public String getCaption()
     {
         return caption;
     }
 
     public boolean distinctRowsOnly()
     {
         return distinctRows;
     }
 
     public QueryDefinition getQueryDefn()
     {
         return queryDefn;
     }
 
     public ReportFrame getFrame()
     {
         return frame;
     }
 
     public ReportBanner getBanner()
     {
         return banner;
     }
 
     public List getReportFields()
     {
         return reportFields;
     }
 
     public QueryConditions getConditions()
     {
         return conditions;
     }
 
     public List getWhereExpressions()
     {
         return whereExprs;
     }
 
     public List getOrderBy()
     {
         return orderBy;
     }
 
     public List getGroupBy()
     {
         return groupByFields;
     }
 
     public List getErrors()
     {
         return errors;
     }
 
     public List getBindParams()
     {
         return bindParams;
     }
 
     public String getErrorSql()
     {
         return selectSql;
     }
 
     public String getSql(ValueContext vc)
     {
         if(isDirty || alwaysDirty)
         {
             SelectStmtGenerator selectStmt = new SelectStmtGenerator(this);
             selectSql = selectStmt.toString(vc);
             if(!selectStmt.isValid())
                 return null;
 
             bindParams = selectStmt.getBindParams();
             isDirty = false;
         }
         return selectSql;
     }
 
     public String getBindParamsDebugHtml(ValueContext vc)
     {
         if(bindParams == null)
             return "NONE";
 
         StringBuffer result = new StringBuffer();
         result.append("<p><br>BIND PARAMETERS:<ol>");
 
         int bindCount = bindParams.size();
         if(bindCount > 0)
         {
             for(int i = 0; i < bindCount; i++)
             {
                 Object bindObj = bindParams.get(i);
                 if(bindObj instanceof ListValueSource)
                 {
                     ListValueSource vs = (ListValueSource) bindObj;
                     String[] values = vs.getValues(vc);
                     for(int j = 0; j < values.length; j++)
                     {
                         result.append("<li><code><b>");
                         result.append(vs.getId());
                         result.append("</b> = ");
                         result.append(values[j]);
                         result.append("</code></li>");
                     }
                 }
                 else
                 {
                     SingleValueSource vs = (SingleValueSource) bindObj;
                     result.append("<li><code><b>");
                     result.append(vs.getId());
                     result.append("</b> = ");
                     result.append(vs.getValue(vc));
                     result.append("</code></li>");
                 }
 
             }
         }
 
         result.append("</ol>");
         return result.toString();
     }
 
     public void addError(String group, String message)
     {
         if(errors == null) errors = new ArrayList();
         errors.add(group + ": " + message);
         isDirty = true;
     }
 
     public void addReportField(QueryField field)
     {
         reportFields.add(field);
         isDirty = true;
     }
 
     public void addReportField(String fieldName)
     {
         if(fieldName.equals("*"))
         {
             List fields = queryDefn.getFieldsList();
             for(Iterator i = fields.iterator(); i.hasNext();)
                 addReportField((QueryField) i.next());
         }
         else
         {
             QueryField field = queryDefn.getField(fieldName);
             if(field == null)
                 addError("query-select-addField", "field '" + fieldName + "' not found");
             else
                 addReportField(field);
         }
     }
 
     public void addReportFields(String[] fieldNames)
     {
         for(int i = 0; i < fieldNames.length; i++)
             addReportField(fieldNames[i]);
     }
 
     /**
      * Adds a group by field to the "group by" list
      *
      * @param fieldName field Name  string
      * @since [Version 1.2.8 Build 23]
      */
     public void addGroupBy(String fieldName)
     {
         QueryField field = queryDefn.getField(fieldName);
         if(field == null)
             addError("query-select-addGroupBy", "field '" + fieldName + "' not found");
         else
             addGroupBy(field);
     }
 
     /**
      * Adds a group by field to the "group by" list
      *
      * @param field query field object
      * @since [Version 1.2.8 Build 23]
      */
     public void addGroupBy(QueryField field)
     {
         groupByFields.add(field);
         isDirty = true;
     }
 
     public void addOrderBy(QuerySortFieldRef field)
     {
         orderBy.add(field);
         if(field.isStatic())
             isDirty = true;
         else
             alwaysDirty = true;
     }
 
     public void addOrderBy(String fieldName)
     {
         QuerySortFieldRef sortRef = new QuerySortFieldRef(queryDefn, fieldName);
 
         if(sortRef.isStatic())
         {
             QueryDefinition.QueryFieldSortInfo[] fields = sortRef.getFields(null);
             for(int i = 0; i < fields.length; i++)
             {
                 if(fields[i] == null || fields[i].getField() == null)
                 {
                     addError("query-select-addOrderBy", "field '" + fieldName + "' not found");
                     break;
                 }
             }
         }
 
         addOrderBy(sortRef);
     }
 
     public void addOrderBy(String[] fieldNames)
     {
         for(int i = 0; i < fieldNames.length; i++)
             addOrderBy(fieldNames[i]);
     }
 
     public void addCondition(QueryCondition condition)
     {
         if(condition.removeIfValueIsNull())
             alwaysDirty = true;
 
         conditions.add(condition);
         isDirty = true;
     }
 
     public void addCondition(String fieldName, String comparison, String value, String connector)
     {
         boolean isValid = true;
         QueryField field = queryDefn.getField(fieldName);
         if(field == null)
         {
             if(errors == null) errors = new ArrayList();
             errors.add("select-condition-field: field '" + fieldName + "' not found");
             isValid = false;
         }
 
         SqlComparison comp = SqlComparisonFactory.getComparison(comparison);
         if(comp == null)
         {
             if(errors == null) errors = new ArrayList();
             errors.add("select-condition-comparison: comparison '" + comparison + "' not found");
             isValid = false;
         }
 
         if(isValid)
             addCondition(new QueryCondition(field, comp, value, connector));
     }
 
     public void addWhereExpr(SqlWhereExpression expr)
     {
         if(whereExprs == null)
             whereExprs = new ArrayList();
         whereExprs.add(expr);
         isDirty = true;
     }
 
     public void addWhereExpr(String expr, String connector)
     {
         addWhereExpr(new SqlWhereExpression(expr, connector));
     }
 
     public ResultInfo execute(DatabaseContext dc, ValueContext vc, Object[] overrideParams) throws NamingException, SQLException
     {
         if(getSql(vc) == null)
             return null;
 
         String dataSourceId = queryDefn.getDataSource() != null ?queryDefn.getDataSource().getValue(vc) : null;
         Connection conn = dc.getConnection(vc, dataSourceId);
         int rsType = dc.getScrollableResultSetType(conn);
 
         PreparedStatement stmt = null;
         stmt =
                 rsType == DatabaseContext.RESULTSET_NOT_SCROLLABLE ?
                 conn.prepareStatement(selectSql) :
                 conn.prepareStatement(selectSql, rsType, ResultSet.CONCUR_READ_ONLY);
 
         if(overrideParams != null)
         {
             for(int i = 0; i < overrideParams.length; i++)
                 stmt.setObject(i + 1, overrideParams[i]);
         }
         else
         {
             int paramsCount = bindParams.size();
             int index = 1;
             // the 'paramsCount' does not represent the actual number of bind
             // parameters. Each entry of paramCount might be a ListValueSource
             // which will contain additional bind params.
             for(int i = 0; i < paramsCount; i++)
             {
                 Object bindObj = bindParams.get(i);
                 if(bindObj instanceof ListValueSource)
                 {
                     // if its a ListValueSource, loop and get the values
                     String[] values = ((ListValueSource) bindObj).getValues(vc);
                     int q;
                     for(q = 0; q < values.length; q++)
                     {
                         stmt.setString(index + q, values[q]);
                     }
                     index = index + q;
                 }
                 else
                 {
                     stmt.setString(index, ((SingleValueSource) bindObj).getValue(vc));
                     index++;
                 }
 
 
             }
         }
 
         if(stmt.execute())
             return new ResultInfo(vc, conn, stmt);
         else
             return null;
     }
 
     public ResultInfo execute(DatabaseContext dc, ValueContext vc) throws NamingException, SQLException
     {
         return execute(dc, vc, null);
     }
 
     public List inherit(List dest, List source)
     {
         if(source == null)
             return dest;
 
         if(dest == null)
             dest = new ArrayList();
 
         int len = source.size();
         for(int i = 0; i < len; i++)
             dest.add(source.get(i));
 
         return dest;
     }
 
     public String getReportClassName()
     {
         return reportClassName;
     }
 
     public void setReportClassName(String reportClassName)
     {
         this.reportClassName = reportClassName;
     }
 
     public Report createReport()
     {
         if(reportClassName != null)
         {
             ClassPath.InstanceGenerator instGen = new ClassPath.InstanceGenerator(reportClassName, StandardReport.class, true);
             return (Report) instGen.getInstance();
         }
         else
             return new StandardReport();
     }
 
     public void importFromSelect(QuerySelect select)
     {
         distinctRows = select.distinctRowsOnly();
 
         inherit(reportFields, select.getReportFields());
         inherit(conditions, queryDefn.getDefaultConditions());
         inherit(conditions, select.getConditions());
         inherit(orderBy, select.getOrderBy());
         inherit(groupByFields, select.getGroupBy());
 
         conditions.registerDynamicConditions();
         frame = select.getFrame();
         banner = select.getBanner();
 
         // whereExprs and errors can be null, so treat them special
         whereExprs = inherit(whereExprs, select.getWhereExpressions());
         whereExprs = inherit(whereExprs, queryDefn.getWhereExprs());
         errors = inherit(errors, select.getErrors());
     }
 
     public void importFromXml(Element elem)
     {
         name = elem.getAttribute("id");
         caption = elem.getAttribute("heading");
         String value = elem.getAttribute("distinct");
         if(value != null && value.equals("no"))
             distinctRows = false;
 
         String customReportClassName = elem.getAttribute("report-class");
         if(customReportClassName.length() > 0)
             reportClassName = customReportClassName;
 
         String heading = elem.getAttribute("heading");
         String footing = elem.getAttribute("footing");
         if(heading.length() > 0 || footing.length() > 0)
         {
             if(frame == null) frame = new ReportFrame();
             frame.importFromXml(elem);
         }
 
         NodeList children = elem.getChildNodes();
         for(int n = 0; n < children.getLength(); n++)
         {
             Node node = children.item(n);
             if(node.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             String childName = node.getNodeName();
             if(childName.equals("display"))
             {
                 addReportField(((Element) node).getAttribute("field"));
             }
             else if(childName.equals("order-by"))
             {
                 Element obElem = (Element) node;
                 String fieldName = obElem.getAttribute("field");
                 addOrderBy(fieldName);
             }
             else if(childName.equals("group-by"))
             {
                 Element obElem = (Element) node;
                 addGroupBy(obElem.getAttribute("field"));
             }
             else if(childName.equals("condition"))
             {
                 ClassPath.InstanceGenerator instanceGen = new ClassPath.InstanceGenerator(((Element) node).getAttribute("class"), QueryCondition.class, true);
                 QueryCondition cond = (QueryCondition) instanceGen.getInstance();
                 cond.importFromXml(queryDefn, (Element) node);
                 addCondition(cond);
             }
             else if(childName.equals("where-expr"))
             {
                 ClassPath.InstanceGenerator instanceGen = new ClassPath.InstanceGenerator(((Element) node).getAttribute("class"), SqlWhereExpression.class, true);
                 SqlWhereExpression expr = (SqlWhereExpression) instanceGen.getInstance();
                 expr.importFromXml((Element) node);
                 addWhereExpr(expr);
             }
             else if(childName.equals("banner"))
             {
                 banner = new ReportBanner();
                 banner.importFromXml((Element) node);
             }
         }
     }
 }
