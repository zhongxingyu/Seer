 /**********************************************************************
 Copyright (c) 2009 Erik Bengtson and others. All rights reserved.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 Contributors :
     ...
 ***********************************************************************/
 package org.datanucleus.store.hbase;
 
 import java.security.AccessController;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.TableNotFoundException;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.datanucleus.exceptions.NucleusDataStoreException;
 import org.datanucleus.metadata.AbstractClassMetaData;
 import org.datanucleus.metadata.AbstractMemberMetaData;
 import org.datanucleus.metadata.ColumnMetaData;
 import org.datanucleus.metadata.VersionMetaData;
 
 public class HBaseUtils
 {
     /**
      * Accessor for the HBase table name for this class.
      * @param acmd Metadata for the class
      * @return The table name
      */
     public static String getTableName(AbstractClassMetaData acmd)
     {
         if (acmd.getTable() != null)
         {
             return acmd.getTable();
         }
         return acmd.getName();
     }
 
     /**
      * Accessor for the HBase family name for the version of this class. 
      * Extracts the family name using the following priorities
      * <ul>
      * <li>If column is specified as "a:b" then takes "a" as the family name.</li>
      * <li>Otherwise takes table name as the family name</li>
      * </ul>
      * @param vermd Metadata for the version
      * @return The family name
      */
     public static String getFamilyName(VersionMetaData vermd)
     {
         String columnName = null;
 
         // Try from the column name if specified as "a:b"
         ColumnMetaData[] colmds = vermd.getColumnMetaData();
         if (colmds != null && colmds.length > 0)
         {
             columnName = colmds[0].getName();
            if (columnName!= null && columnName.indexOf(":")>-1)
             {
                 return columnName.substring(0,columnName.indexOf(":"));
             }
         }
 
         // Fallback to table name.
         return getTableName((AbstractClassMetaData)vermd.getParent());
     }
 
     /**
      * Accessor for the HBase qualifier name for this version. 
      * Extracts the qualifier name using the following priorities
      * <ul>
      * <li>If column is specified as "a:b" then takes "b" as the qualifier name.</li>
      * <li>Otherwise takes the column name as the qualifier name when it is specified</li>
      * <li>Otherwise takes "VERSION" as the qualifier name</li>
      * </ul>
      * @param acmd Metadata for the class
      * @param absoluteFieldNumber Field number
      * @return The qualifier name
      */
     public static String getQualifierName(VersionMetaData vermd)
     {
         String columnName = null;
 
         // Try the first column if specified
         ColumnMetaData[] colmds = vermd.getColumnMetaData();
         if (colmds != null && colmds.length > 0)
         {
             columnName = colmds[0].getName();
         }
         if (columnName == null)
         {
             // Fallback to "VERSION"
             columnName = "VERSION";
         }
         if (columnName.indexOf(":")>-1)
         {
             columnName = columnName.substring(columnName.indexOf(":")+1);
         }
         return columnName;
     }
     
     /**
      * Accessor for the HBase family name for this field. Extracts the family name using the following priorities
      * <ul>
      * <li>If column is specified as "a:b" then takes "a" as the family name.</li>
      * <li>Otherwise takes the table name as the family name</li>
      * </ul>
      * @param acmd Metadata for the class
      * @param absoluteFieldNumber Field number
      * @return The family name
      */
     public static String getFamilyName(AbstractClassMetaData acmd, int absoluteFieldNumber)
     {
         AbstractMemberMetaData ammd = acmd.getMetaDataForManagedMemberAtAbsolutePosition(absoluteFieldNumber);
         String columnName = null;
 
         // Try the first column if specified
         ColumnMetaData[] colmds = ammd.getColumnMetaData();
         if (colmds != null && colmds.length > 0)
         {
             columnName = colmds[0].getName();
            if (columnName != null && columnName.indexOf(":")>-1)
             {
                 return columnName.substring(0,columnName.indexOf(":"));
             }
         }
 
         // Fallback to the table name
         return HBaseUtils.getTableName(acmd);
     }
 
     /**
      * Accessor for the HBase qualifier name for this field. Extracts the qualifier name using the following priorities
      * <ul>
      * <li>If column is specified as "a:b" then takes "b" as the qualifier name.</li>
      * <li>Otherwise takes the column name as the qualifier name when it is specified</li>
      * <li>Otherwise takes the field name as the qualifier name</li>
      * </ul>
      * @param acmd Metadata for the class
      * @param absoluteFieldNumber Field number
      * @return The qualifier name
      */
     public static String getQualifierName(AbstractClassMetaData acmd, int absoluteFieldNumber)
     {
         AbstractMemberMetaData ammd = acmd.getMetaDataForManagedMemberAtAbsolutePosition(absoluteFieldNumber);
         String columnName = null;
 
         // Try the first column if specified
         ColumnMetaData[] colmds = ammd.getColumnMetaData();
         if (colmds != null && colmds.length > 0)
         {
             columnName = colmds[0].getName();
         }
         if (columnName == null)
         {
             // Fallback to the field/property name
             columnName = ammd.getName();
         }
         if (columnName.indexOf(":")>-1)
         {
             columnName = columnName.substring(columnName.indexOf(":")+1);
         }
         return columnName;
     }
     
     /**
      * Create a schema in HBase. Do not make this method public, since it uses privileged actions
      * @param config
      * @param acmd
      * @param autoCreateColumns
      */
     static void createSchema(final HBaseConfiguration config, final AbstractClassMetaData acmd, final boolean autoCreateColumns)
     {
         try
         {
             final HBaseAdmin hBaseAdmin = (HBaseAdmin) AccessController.doPrivileged(new PrivilegedExceptionAction()
             {
                 public Object run() throws Exception
                 {
                     return new HBaseAdmin(config);
                 }
             });
             
             final HTableDescriptor hTable = (HTableDescriptor) AccessController.doPrivileged(new PrivilegedExceptionAction()
             {
                 public Object run() throws Exception
                 {
                     String tableName = HBaseUtils.getTableName(acmd);
                     HTableDescriptor hTable;
                     try
                     {
                         hTable = hBaseAdmin.getTableDescriptor(tableName.getBytes());
                     }
                     catch(TableNotFoundException ex)
                     {
                         hTable = new HTableDescriptor(tableName);
                         hBaseAdmin.createTable(hTable);
                     }
                     return hTable;
                 }
             });
 
             if (autoCreateColumns)
             {
                 boolean modified = false;
                 if (!hTable.hasFamily(HBaseUtils.getTableName(acmd).getBytes()))
                 {
                     HColumnDescriptor hColumn = new HColumnDescriptor(HBaseUtils.getTableName(acmd));
                     hTable.addFamily(hColumn);
                     modified = true;
                 }
                 int[] fieldNumbers =  acmd.getAllMemberPositions();
                 for(int i=0; i<fieldNumbers.length; i++)
                 {            
                     String familyName = getFamilyName(acmd, fieldNumbers[i]);
                     if (!hTable.hasFamily(familyName.getBytes()))
                     {
                         HColumnDescriptor hColumn = new HColumnDescriptor(familyName);
                         hTable.addFamily(hColumn);
                         modified = true;
                     }
                 }
                 if (modified)
                 {
                     AccessController.doPrivileged(new PrivilegedExceptionAction()
                     {
                         public Object run() throws Exception
                         {
                             hBaseAdmin.disableTable(hTable.getName());
                             hBaseAdmin.modifyTable(hTable.getName(), hTable);
                             hBaseAdmin.enableTable(hTable.getName());
                             return null;
                         }
                     });
                 }
             }
         }
         catch (PrivilegedActionException e)
         {
             throw new NucleusDataStoreException(e.getMessage(), e.getCause());
         }
     }
 }
