 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.axiom.schema.table.type;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.schema.Column;
 import com.netspective.axiom.schema.ColumnValues;
 import com.netspective.axiom.schema.Columns;
 import com.netspective.axiom.schema.Index;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.Schema;
 import com.netspective.axiom.schema.TableRowTrigger;
 import com.netspective.axiom.schema.table.BasicTable;
 import com.netspective.axiom.sql.dynamic.QueryDefnSelect;
 import com.netspective.commons.value.Value;
 
 public class EntityHierarchyTable extends BasicTable
 {
     // if any of these values are changed, be sure to update the Relationship_Type table-type in axiom/conf/schema.xml
     public static final int RELTYPEID_HIERARCHY_PARENT = 0;
     public static final int RELTYPEID_HIERARCHY_ANCESTOR = 1;
 
     public static final Long RELTYPEID_OBJ_HIERARCHY_PARENT = new Long(RELTYPEID_HIERARCHY_PARENT);
     public static final Long RELTYPEID_OBJ_HIERARCHY_ANCESTOR = new Long(RELTYPEID_HIERARCHY_ANCESTOR);
 
     private String hierTypeIdColName;
     private String primaryIdColName;
     private String relatedIdColName;
     private String hierDistanceColName;
     private String uniqueHierIndexName;
     private Column hierTypeIdColumn;
     private Column primaryIdColumn;
     private Column relatedIdColumn;
     private Column relationshipDistanceColumn;
     private Index uniqueHierIndex;
     private QueryDefnSelect uniqueHierAccessor;
     private String selectParentsSql;
     private String removeAncestorSql;
 
     public EntityHierarchyTable(Column parentColumn)
     {
         super(parentColumn);
     }
 
     public EntityHierarchyTable(Schema schema)
     {
         super(schema);
     }
 
     public String getHierTypeIdColName()
     {
         return hierTypeIdColName;
     }
 
     public void setHierTypeIdColName(String hierTypeIdColName)
     {
         this.hierTypeIdColName = hierTypeIdColName;
     }
 
     public String getPrimaryIdColName()
     {
         return primaryIdColName;
     }
 
     public void setPrimaryIdColName(String primaryIdColName)
     {
         this.primaryIdColName = primaryIdColName;
     }
 
     public String getRelatedIdColName()
     {
         return relatedIdColName;
     }
 
     public void setRelatedIdColName(String relatedIdColName)
     {
         this.relatedIdColName = relatedIdColName;
     }
 
     public String getHierDistanceColName()
     {
         return hierDistanceColName;
     }
 
     public void setHierDistanceColName(String hierDistanceColName)
     {
         this.hierDistanceColName = hierDistanceColName;
     }
 
     public String getUniqueHierIndexName()
     {
         return uniqueHierIndexName;
     }
 
     public void setUniqueHierIndexName(String uniqueHierIndexName)
     {
         this.uniqueHierIndexName = uniqueHierIndexName;
     }
 
     public void finishConstruction()
     {
         super.finishConstruction();
         final Columns columns = getColumns();
         hierTypeIdColumn = columns.getByName(hierTypeIdColName);
         if(hierTypeIdColumn == null)
             throw new RuntimeException("Unable to find column '" + hierTypeIdColName + "' in table '" + this.getName() + "'");
 
         primaryIdColumn = columns.getByName(primaryIdColName);
         if(primaryIdColumn == null)
             throw new RuntimeException("Unable to find column '" + primaryIdColName + "' in table '" + this.getName() + "'");
 
         relatedIdColumn = columns.getByName(relatedIdColName);
         if(relatedIdColumn == null)
             throw new RuntimeException("Unable to find column '" + relatedIdColName + "' in table '" + this.getName() + "'");
 
         relationshipDistanceColumn = columns.getByName(hierDistanceColName);
         if(relationshipDistanceColumn == null)
             throw new RuntimeException("Unable to find column '" + hierDistanceColName + "' in table '" + this.getName() + "'");
 
         uniqueHierIndex = getIndexes().get(uniqueHierIndexName);
         if(uniqueHierIndex == null)
             throw new RuntimeException("Unable to find index '" + uniqueHierIndexName + "' in table '" + this.getName() + "'");
 
         uniqueHierAccessor = getAccessorByIndexEquality(uniqueHierIndex);
         if(uniqueHierAccessor == null)
             throw new RuntimeException("Unable to find accessor for '" + uniqueHierIndexName + "' index in table '" + this.getName() + "'");
 
         // SQL to find all parents of a given child ID
         selectParentsSql = "select " + primaryIdColName + " from " + getName() + " where " + hierTypeIdColName + " = " + RELTYPEID_HIERARCHY_PARENT + " and " +
                            relatedIdColName + " = ?";
 
         // SQL to remove all parents of a given child ID
         removeAncestorSql = "delete from " + getName() + " where " + hierTypeIdColName + " = " + RELTYPEID_HIERARCHY_ANCESTOR + " and " +
                             primaryIdColName + " = ? and " + relatedIdColName + " = ?";
 
         addTrigger(new HierarchyParentTrigger());
     }
 
     public boolean isHierarchyPresent(ConnectionContext cc, Long hierType, Value primaryId, Value relatedId) throws SQLException
     {
         return getHierarchyRow(cc, hierType, primaryId.getValue(), relatedId.getValue()) != null;
     }
 
     public Row getHierarchyRow(ConnectionContext cc, Long hierType, Value primaryId, Value relatedId) throws SQLException
     {
         return getHierarchyRow(cc, hierType, primaryId.getValue(), relatedId.getValue());
     }
 
     public Row getHierarchyRow(ConnectionContext cc, Long hierType, Object primaryId, Object relatedId) throws SQLException
     {
         try
         {
             return getRowByAccessor(cc, uniqueHierAccessor, new Object[]{
                 hierType,
                 primaryId,
                 relatedId
             }, null);
         }
         catch(NamingException e)
         {
             throw new SQLException(e.getMessage());
         }
     }
 
     /**
      * Base trigger class for managing hierarchy relationships.
      */
     protected abstract class HierarchyManager implements TableRowTrigger
     {
         private int hierTypeId;
 
         protected HierarchyManager(int hierTypeId)
         {
             this.hierTypeId = hierTypeId;
         }
 
         public boolean isRowOfThisHierarchyType(Row row)
         {
             final ColumnValues columnValues = row.getColumnValues();
             Value relTypeValue = columnValues.getByColumn(hierTypeIdColumn);
             return relTypeValue.getIntValue() == hierTypeId;
         }
 
         public void beforeTableRowInsert(ConnectionContext cc, Row row) throws SQLException
         {
             // not doing anything before
         }
 
         public void beforeTableRowUpdate(ConnectionContext cc, Row row) throws SQLException
         {
             throw new SQLException(getName() + " table rows should not be updated -- only inserted and deleted.");
         }
 
         public void afterTableRowUpdate(ConnectionContext cc, Row row) throws SQLException
         {
             throw new SQLException(getName() + " table rows should not be updated -- only inserted and deleted.");
         }
 
         public void beforeTableRowDelete(ConnectionContext cc, Row row) throws SQLException
         {
             // not doing anything before
         }
     }
 
    public void addAncestor(ConnectionContext cc, PreparedStatement stmt, Object activeParentId, Object childId, int distance) throws NamingException, SQLException
     {
         stmt.setObject(1, activeParentId);
         ResultSet rs = stmt.executeQuery();
         try
         {
             while(rs.next())
             {
                 Object ancestorId = rs.getObject(1);
 
                 Row ancestorRow = createRow();
                 final ColumnValues parentRowColumnValues = ancestorRow.getColumnValues();
                 parentRowColumnValues.getByColumn(hierTypeIdColumn).setValue(RELTYPEID_OBJ_HIERARCHY_ANCESTOR);
                 parentRowColumnValues.getByColumn(primaryIdColumn).setValue(ancestorId);
                 parentRowColumnValues.getByColumn(relatedIdColumn).setValue(childId);
                 parentRowColumnValues.getByColumn(relationshipDistanceColumn).setValue(new Long(distance));
                insertOrUpdateIfDuplicateRowFound(cc, ancestorRow);
 
                 addAncestor(cc, stmt, ancestorId, childId, distance + 1);
             }
         }
         finally
         {
             rs.close();
         }
     }
 
     public void removeAncestor(ConnectionContext cc, PreparedStatement parentStmt, PreparedStatement removeStmt, Object activeParentId, Object childId) throws SQLException
     {
         parentStmt.setObject(1, activeParentId);
         ResultSet rs = parentStmt.executeQuery();
         try
         {
             while(rs.next())
             {
                 Object ancestorId = rs.getObject(1);
 
                 removeStmt.setObject(1, ancestorId);
                 removeStmt.setObject(2, childId);
                 removeStmt.executeUpdate();
 
                 removeAncestor(cc, parentStmt, removeStmt, ancestorId, childId);
             }
         }
         finally
         {
             rs.close();
         }
     }
 
     /**
      * This trigger manages child records; whenever a relationship row of type RELTYPEID_HIERARCHY_CHILD is
      * inserted/deleted this class will maintain the ancestors.
      */
     protected class HierarchyParentTrigger extends HierarchyManager
     {
         public HierarchyParentTrigger()
         {
             super(RELTYPEID_HIERARCHY_PARENT);
         }
 
         public void afterTableRowInsert(ConnectionContext cc, Row row) throws SQLException
         {
             if(!isRowOfThisHierarchyType(row))
                 return;
 
             final ColumnValues childRowColumnValues = row.getColumnValues();
             Value parentIdValue = childRowColumnValues.getByColumn(primaryIdColumn);
             Value childIdValue = childRowColumnValues.getByColumn(relatedIdColumn);
 
             Object parentId = parentIdValue.getValue();
             Object childId = childIdValue.getValue();
 
             /*
              * Our table just added a "child" record so we need to create the descendent relationships. We basically
              * walk up the tree of all the parents of the child record and add a descendant record for our ancestors.
              */
             try
             {
                 Row ancestorRow = createRow();
                 final ColumnValues parentRowColumnValues = ancestorRow.getColumnValues();
                 parentRowColumnValues.getByColumn(hierTypeIdColumn).setValue(RELTYPEID_OBJ_HIERARCHY_ANCESTOR);
                 parentRowColumnValues.getByColumn(primaryIdColumn).setValue(parentId);
                 parentRowColumnValues.getByColumn(relatedIdColumn).setValue(childId);
                 parentRowColumnValues.getByColumn(relationshipDistanceColumn).setValue(new Long(0));
                insertOrUpdateIfDuplicateRowFound(cc, ancestorRow);
 
                 PreparedStatement stmt = cc.getConnection().prepareStatement(selectParentsSql);
                 addAncestor(cc, stmt, parentId, childId, 1);
                 stmt.close();
             }
             catch(NamingException e)
             {
                 throw new SQLException(e.getMessage());
             }
         }
 
         public void afterTableRowDelete(ConnectionContext cc, Row row) throws SQLException
         {
             if(!isRowOfThisHierarchyType(row))
                 return;
 
             final ColumnValues childRowColumnValues = row.getColumnValues();
             Value parentIdValue = childRowColumnValues.getByColumn(primaryIdColumn);
             Value childIdValue = childRowColumnValues.getByColumn(relatedIdColumn);
 
             Object parentId = parentIdValue.getValue();
             Object childId = childIdValue.getValue();
 
             try
             {
                 PreparedStatement parentsStmt = cc.getConnection().prepareStatement(selectParentsSql);
                 PreparedStatement removeStmt = cc.getConnection().prepareStatement(removeAncestorSql);
 
                 removeStmt.setObject(1, parentId);
                 removeStmt.setObject(2, childId);
                 removeStmt.executeUpdate();
 
                 removeAncestor(cc, parentsStmt, removeStmt, parentId, childId);
                 parentsStmt.close();
                 removeStmt.close();
             }
             catch(NamingException e)
             {
                 throw new SQLException(e.getMessage());
             }
         }
     }
 }
