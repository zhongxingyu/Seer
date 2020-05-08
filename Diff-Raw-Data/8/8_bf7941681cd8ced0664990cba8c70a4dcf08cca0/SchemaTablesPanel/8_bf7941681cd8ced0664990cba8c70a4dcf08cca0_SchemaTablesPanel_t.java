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
 * $Id: SchemaTablesPanel.java,v 1.8 2004-03-21 02:29:15 shahid.shah Exp $
  */
 
 package com.netspective.sparx.console.panel.data.schema;
 
 import java.util.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.commons.report.tabular.column.GeneralColumn;
 import com.netspective.commons.report.tabular.column.NumericColumn;
 import com.netspective.commons.report.tabular.TabularReportDataSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.commons.value.source.RedirectValueSource;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.panel.AbstractHtmlTabularReportPanel;
 import com.netspective.sparx.report.tabular.HtmlTabularReport;
 import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
 import com.netspective.sparx.report.tabular.AbstractHtmlTabularReportDataSource;
 import com.netspective.axiom.schema.Schemas;
 import com.netspective.axiom.schema.Schema;
 import com.netspective.axiom.schema.Tables;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.BasicSchema;
 import com.netspective.axiom.schema.Rows;
 import com.netspective.axiom.schema.table.type.EnumerationTable;
 import com.netspective.axiom.schema.table.BasicTable;
 
 public class SchemaTablesPanel extends AbstractHtmlTabularReportPanel
 {
     private static final Log log = LogFactory.getLog(SchemaTablesPanel.class);
     public static final String REQPARAMNAME_SHOW_DETAIL_TABLE = "schema-table";
     private static final HtmlTabularReport structureReport = new BasicHtmlTabularReport();
     private static final GeneralColumn schemaTableColumn = new GeneralColumn();
     protected static final ValueSource noTableSelected = new StaticValueSource("No table selected.");
 
     static
     {
         schemaTableColumn.setHeading(new StaticValueSource("SQL Table Name"));
        //schemaTableColumn.setRedirect(new RedirectValueSource("table?"+ REQPARAMNAME_SHOW_DETAIL_TABLE +"=%{1}"));
         structureReport.addColumn(schemaTableColumn);
 
         GeneralColumn column = new GeneralColumn();
         column.setHeading(new StaticValueSource("XML Node Name"));
         structureReport.addColumn(column);
 
         column = new NumericColumn();
         column.setHeading(new StaticValueSource("Columns"));
         structureReport.addColumn(column);
 
         column = new NumericColumn();
         column.setHeading(new StaticValueSource("Indexes"));
         structureReport.addColumn(column);
 
         column = new NumericColumn();
         column.setHeading(new StaticValueSource("Static Rows"));
         structureReport.addColumn(column);
 
         column = new GeneralColumn();
         column.setHeading(new StaticValueSource("Class"));
         structureReport.addColumn(column);
     }
 
     protected static class StructureRow
     {
         protected int level;
         protected StructureRow parentRow;
         protected List ancestors;
         protected String heading;
         protected Schema.TableTreeNode tableTreeNode;
         protected EnumerationTable enumTable;
 
         protected StructureRow(int level, StructureRow parentRow, String heading)
         {
             this.level = level;
             this.parentRow = parentRow;
             this.heading = heading;
         }
 
         protected StructureRow(int level, Schema.TableTreeNode tableTreeNode, List ancestors)
         {
             this.level = level;
             this.parentRow = (StructureRow) ancestors.get(0);
             this.ancestors = ancestors;
             this.tableTreeNode = tableTreeNode;
         }
 
         protected StructureRow(int level, EnumerationTable enumTable, List ancestors)
         {
             this.level = level;
             this.parentRow = (StructureRow) ancestors.get(0);
             this.ancestors = ancestors;
             this.enumTable = enumTable;
         }
 
         public StructureRow getParentRow()
         {
             return parentRow;
         }
 
         public Table getTable()
         {
             if(tableTreeNode != null)
                 return tableTreeNode.getTable();
 
             if(enumTable != null)
                 return enumTable;
 
             return null;
         }
 
         public boolean isTable(String schemaAndTableName)
         {
             String[] schemaAndTableNameArray = TextUtils.split(schemaAndTableName, ".", false);
             String schemaName = schemaAndTableNameArray[0];
             String tableName = schemaAndTableNameArray[1];
 
             if(tableTreeNode != null && tableTreeNode.getTable().getName().equalsIgnoreCase(tableName) &&
                     tableTreeNode.getTable().getSchema().getName().equalsIgnoreCase(schemaName))
                 return true;
 
             if(enumTable != null && enumTable.getName().equalsIgnoreCase(tableName) &&
                     enumTable.getSchema().getName().equalsIgnoreCase(schemaName))
                 return true;
 
             return false;
         }
     }
 
     private static Map rowsCache = new HashMap();
     private SchemaTablesPanelViewEnumeratedAttribute view = new SchemaTablesPanelViewEnumeratedAttribute(SchemaTablesPanelViewEnumeratedAttribute.ALL);
 
     public SchemaTablesPanel()
     {
         getFrame().setHeading(new StaticValueSource("Overview"));
     }
 
     public static void addStructurRow(List rows, int level, Schema.TableTreeNode treeNode, List ancestors)
     {
         StructureRow activeStructureRow = new StructureRow(level, treeNode, ancestors);
         rows.add(activeStructureRow);
         List children = treeNode.getChildren();
         if(children != null)
         {
             for(int c = 0; c < children.size(); c++)
             {
                 List childAncestors = new ArrayList();
                 childAncestors.add(activeStructureRow);
                 childAncestors.addAll(ancestors);
                 addStructurRow(rows, level+1, (Schema.TableTreeNode) children.get(c), childAncestors);
             }
         }
     }
 
     public static List createStructureRows(Schemas schemas)
     {
         List rows = (List) rowsCache.get(schemas);
         if(rows != null)
             return rows;
 
         rows = new ArrayList();
         for(int i = 0; i < schemas.size(); i++)
         {
             Schema schema = schemas.get(i);
             Schema.TableTree tree = schema.getStructure();
 
             StructureRow schemaRow = new StructureRow(0, null, "Schema: '" + schema.getName() + "'");
             rows.add(schemaRow);
 
             StructureRow appTablesRow = new StructureRow(1, schemaRow, "Application Tables");
             rows.add(appTablesRow);
 
             List appTableAncestors = new ArrayList();
             appTableAncestors.add(schemaRow);
             appTableAncestors.add(appTablesRow);
 
             List children = tree.getChildren();
             for(int c = 0; c < children.size(); c++)
                 addStructurRow(rows, 2, (Schema.TableTreeNode) children.get(c), appTableAncestors);
 
             StructureRow enumTablesRow = new StructureRow(1, schemaRow, "Enumeration Tables");
             rows.add(enumTablesRow);
 
             List enumTableAncestors = new ArrayList();
             enumTableAncestors.add(schemaRow);
             enumTableAncestors.add(enumTablesRow);
 
             Set sortedEnumTables = new TreeSet(BasicSchema.TABLE_COMPARATOR);
             Tables tables = schema.getTables();
             for(int c = 0; c < tables.size(); c++)
             {
                 Table table = tables.get(c);
                 if(table instanceof EnumerationTable)
                     sortedEnumTables.add(table);
             }
             for(Iterator iter = sortedEnumTables.iterator(); iter.hasNext(); )
                 rows.add(new StructureRow(2, (EnumerationTable) iter.next(), enumTableAncestors));
         }
 
         rowsCache.put(schemas, rows);
         return rows;
     }
 
     public SchemaTablesPanelViewEnumeratedAttribute getView()
     {
         return view;
     }
 
     public void setView(SchemaTablesPanelViewEnumeratedAttribute view)
     {
         this.view = view;
     }
 
     public boolean affectsNavigationContext(NavigationContext nc)
     {
         return true;
     }
 
     public TabularReportDataSource createDataSource(NavigationContext nc)
     {
         List rows = createStructureRows(nc.getSqlManager().getSchemas());
         StructureRow selectedRow = getSelectedStructureRow(nc, rows);
 
         if(view.getValueIndex() == SchemaTablesPanelViewEnumeratedAttribute.ALL)
             return new StructureDataSource(createStructureRows(nc.getSqlManager().getSchemas()), selectedRow);
         else
         {
             if(selectedRow == null)
                 return new SimpleMessageDataSource(noTableSelected);
             else
                 return new StructureDataSource(createStructureRows(nc.getSqlManager().getSchemas()), selectedRow);
         }
     }
 
     public HtmlTabularReport getReport(NavigationContext nc)
     {
         return structureReport;
     }
 
     public static StructureRow getSelectedStructureRow(NavigationContext nc, List structureRows)
     {
         String selectedTable = nc.getRequest().getParameter(REQPARAMNAME_SHOW_DETAIL_TABLE);
         if(selectedTable == null)
             return null;
 
         for(int i = 0; i < structureRows.size(); i++)
         {
             StructureRow structureRow = (StructureRow) structureRows.get(i);
             if(structureRow.isTable(selectedTable))
             {
                 String abbrev = structureRow.getTable().getAbbrev();
                 nc.setPageHeading(abbrev.equals(structureRow.getTable().getName()) ? selectedTable : (selectedTable + " (" + abbrev + ")"));
                 return structureRow;
             }
         }
 
         return null;
     }
 
     protected class StructureDataSource extends AbstractHtmlTabularReportDataSource
     {
         protected int row = -1;
         protected int lastRow;
         protected StructureRow activeRow;
         protected StructureRow selectedRow;
         protected List rows;
         protected TabularReportDataSource.Hierarchy hierarchy = new ActiveHierarchy();
 
         protected class ActiveHierarchy implements TabularReportDataSource.Hierarchy
         {
             public int getColumn()
             {
                 return 0;
             }
 
             public int getLevel()
             {
                 return activeRow.level;
             }
 
             public int getParentRow()
             {
                 return activeRow.getParentRow() != null ? rows.indexOf(activeRow.getParentRow()) : -1;
             }
         }
 
         public boolean isHierarchical()
         {
             return true;
         }
 
         public TabularReportDataSource.Hierarchy getActiveHierarchy()
         {
             return hierarchy;
         }
 
         public StructureDataSource(List structureRows, StructureRow selectedRow)
         {
             super();
             this.rows = structureRows;
             this.selectedRow = selectedRow;
             lastRow = structureRows.size() - 1;
 
             if(view.getValueIndex() == SchemaTablesPanelViewEnumeratedAttribute.ACTIVE_TABLE)
             {
                 this.rows = new ArrayList();
 
                 if(selectedRow != null)
                 {
                     for(int i = 0; i < structureRows.size(); i++)
                     {
                         StructureRow checkRow = (StructureRow) structureRows.get(i);
                         if(checkRow == selectedRow || selectedRow.ancestors.contains(checkRow) || (checkRow.ancestors != null && checkRow.ancestors.contains(selectedRow)))
                             this.rows.add(structureRows.get(i));
                     }
                 }
 
                 lastRow = this.rows.size() - 1;
             }
         }
 
         public String createTableHref(Table table)
         {
             return "<a href=\"table?schema-table="+ table.getSchema().getName() + "." + table.getName() +"\">" + table.getName() + "</a>";
         }
 
         public Object getActiveRowColumnData(int columnIndex, int flags)
         {
             Table activeTable = activeRow.getTable();
 
             switch(columnIndex)
             {
                 case 0:
                     if(activeTable != null)
                         return createTableHref(activeTable);
                     else
                        return "<b>" + activeRow.heading + "</b>";
 
                 case 1:
                     if(activeTable != null)
                         return activeTable.getXmlNodeName();
 
                 case 2:
                     if(activeTable != null)
                         return new Integer(activeTable.getColumns().size());
 
                 case 3:
                     if(activeTable != null)
                         return new Integer(activeTable.getIndexes().size());
 
                 case 4:
                     Rows rows = null;
                     if(activeTable != null)
                         rows = activeTable.getData();
                     return rows != null ? new Integer(rows.size()) : null;
 
                 case 5:
                     if(activeRow.tableTreeNode != null && (selectedRow == activeRow || activeRow.tableTreeNode.getTable().getClass() != BasicTable.class))
                         return reportValueContext.getSkin().constructClassRef(activeRow.tableTreeNode.getTable().getClass());
                     else if(activeRow.enumTable != null && (selectedRow == activeRow || activeRow.enumTable.getClass() != EnumerationTable.class))
                         return reportValueContext.getSkin().constructClassRef(activeRow.enumTable.getClass());
 
                 default:
                     return null;
             }
         }
 
         public int getTotalRows()
         {
             return rows.size();
         }
 
         public boolean hasMoreRows()
         {
             return row < lastRow;
         }
 
         public boolean isScrollable()
         {
             return true;
         }
 
         public void setActiveRow(int rowNum)
         {
             row = rowNum;
             activeRow = (StructureRow) rows.get(row);
         }
 
         public boolean next()
         {
             if(! hasMoreRows())
                 return false;
 
             setActiveRow(row + 1);
             return true;
         }
 
         public int getActiveRowNumber()
         {
             return row + 1;
         }
 
         public boolean isActiveRowSelected()
         {
             return activeRow == selectedRow;
         }
 
         public ValueSource getNoDataFoundMessage()
         {
             return noTableSelected;
         }
     }
 }
