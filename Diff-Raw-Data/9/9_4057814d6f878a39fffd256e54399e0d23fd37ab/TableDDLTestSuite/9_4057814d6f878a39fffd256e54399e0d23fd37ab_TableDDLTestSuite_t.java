 /*******************************************************************************
  * Copyright (c) 2011 Oracle. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
  * which accompanies this distribution.
  * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
  * and the Eclipse Distribution License is available at
  * http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors:
  *     Mike Norman - June 10 2011, created DDL parser package
  *     David McCann - July 2011, visit tests
  ******************************************************************************/
 package org.eclipse.persistence.tools.oracleddl.test.databasetypebuilder;
 
 //javase imports
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 //JUnit4 imports
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 //DDL imports
 import org.eclipse.persistence.tools.oracleddl.metadata.DatabaseType;
 import org.eclipse.persistence.tools.oracleddl.metadata.FieldType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ScalarDatabaseTypeEnum;
 import org.eclipse.persistence.tools.oracleddl.metadata.SizedType;
 import org.eclipse.persistence.tools.oracleddl.metadata.TableType;
 import org.eclipse.persistence.tools.oracleddl.metadata.VarChar2Type;
 import org.eclipse.persistence.tools.oracleddl.util.DatabaseTypeBuilder;
 
 //testing imports
 import org.eclipse.persistence.tools.oracleddl.test.AllTests;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.buildConnection;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.createTable;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.dropTable;
 
 public class TableDDLTestSuite {
 
     static final String SIMPLETABLE = "DTB_SIMPLETABLE";
     static final String SIMPLETABLE_FIELD1 = 
         "ID";
     static final String SIMPLETABLE_FIELD2 = 
         "NAME";
     static final String SIMPLETABLE_FIELD3 = 
         "SINCE";
     static final String CREATE_SIMPLETABLE = 
         "CREATE TABLE " + SIMPLETABLE + " (\n" +
             SIMPLETABLE_FIELD1 + " INTEGER NOT NULL,\n" +
             SIMPLETABLE_FIELD2 + " VARCHAR2(25),\n" +
             SIMPLETABLE_FIELD3 + " DATE,\n" +
             "PRIMARY KEY (" + SIMPLETABLE_FIELD1 + "," + SIMPLETABLE_FIELD2 + ")\n" +
         ")";
     static final String DROP_SIMPLETABLE =
         "DROP TABLE " + SIMPLETABLE;
     
     //fixtures
     static DatabaseTypeBuilder dtBuilder = DatabaseTypeBuilderTestSuite.dtBuilder;
     static Connection conn = AllTests.conn;
     static TableType tableType = null;
     static List<String> expectedFieldNames = new ArrayList<String>();
     static List<String> expectedPKFieldNames = new ArrayList<String>();
     @BeforeClass
     public static void setUp() throws SQLException, ClassNotFoundException {
         conn = buildConnection();
         dtBuilder = new DatabaseTypeBuilder();
         //send DDL to database
         createTable(conn, CREATE_SIMPLETABLE);
         boolean worked = true;
         String msg = null;
         try {
            tableType = dtBuilder.buildTables(conn, "SCOTT", SIMPLETABLE).get(0);
         }
         catch (Exception e) {
             worked = false;
             msg = e.getMessage();
         }
         if (!worked) {
             fail(msg);
         }
         expectedPKFieldNames.add(SIMPLETABLE_FIELD1);
         expectedPKFieldNames.add(SIMPLETABLE_FIELD2);
         expectedFieldNames.add(SIMPLETABLE_FIELD1);
         expectedFieldNames.add(SIMPLETABLE_FIELD2);
         expectedFieldNames.add(SIMPLETABLE_FIELD3);
     }
     
     @Test
     public void testTableName() {
         assertEquals("incorrect table name", SIMPLETABLE , tableType.getTableName());
     }
     
     @Test
     public void testNumberOfColumns() {
         List<FieldType> columns = tableType.getColumns();
         assertTrue("incorrect number of columns", columns.size() ==  3);
     }
     
     @Test
     public void testPrimaryKeys() {
         List<FieldType> columns = tableType.getColumns();
         List<String> pkFieldNames = new ArrayList<String>();
         for (FieldType field : columns) {
             if ((field.pk())) {
                 pkFieldNames.add(field.getFieldName());
             }
         }
         assertEquals("incorrect PK column names", expectedPKFieldNames, pkFieldNames);
     }
     
     @Test
     public void testColumnNames() {
         List<FieldType> columns = tableType.getColumns();
         List<String> fieldNames = new ArrayList<String>();
         for (FieldType field : columns) {
             fieldNames.add(field.getFieldName());
         }
         assertEquals("incorrect column names", expectedFieldNames, fieldNames);
     }
     
     @Test
     public void testColumnTypes() {
         List<FieldType> columns = tableType.getColumns();
         FieldType field1 = columns.get(0);
         DatabaseType col1Type = field1.getDataType();
         assertEquals("incorrect type for column [" + SIMPLETABLE_FIELD1 + "]",
             ScalarDatabaseTypeEnum.INTEGER_TYPE.getTypeName(), col1Type.getTypeName());
         assertTrue("incorrect NULL constraint for column [" + SIMPLETABLE_FIELD1 + "]",
             field1.notNull());
 
         FieldType field2 = columns.get(1);
         DatabaseType col2Type = field2.getDataType();
         assertEquals("incorrect type for column [" + SIMPLETABLE_FIELD2 + "]",
             new VarChar2Type().getTypeName(), col2Type.getTypeName());
         assertFalse("incorrect NULL constraint for column [" + SIMPLETABLE_FIELD2 + "]",
             field2.notNull());
         assertTrue("incorrect size for column [" + SIMPLETABLE_FIELD2 + "]",
             ((SizedType)col2Type).getSize() == 25);
 
         FieldType field3 = columns.get(2);
         DatabaseType col3Type = field3.getDataType();
         assertEquals("incorrect type for column [" + SIMPLETABLE_FIELD3 + "]",
             ScalarDatabaseTypeEnum.DATE_TYPE.getTypeName(), col3Type.getTypeName());
         assertFalse("incorrect NULL constraint for column [" + SIMPLETABLE_FIELD3 + "]",
             field3.notNull());
     }
 
     @AfterClass
     public static void tearDown() {
         dropTable(conn, DROP_SIMPLETABLE);
     }
 
 }
