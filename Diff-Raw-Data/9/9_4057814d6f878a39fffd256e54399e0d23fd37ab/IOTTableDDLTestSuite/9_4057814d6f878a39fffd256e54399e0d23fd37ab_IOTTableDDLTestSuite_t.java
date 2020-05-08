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
 import org.eclipse.persistence.tools.oracleddl.metadata.NumericType;
 import org.eclipse.persistence.tools.oracleddl.metadata.SizedType;
 import org.eclipse.persistence.tools.oracleddl.metadata.TableType;
 import org.eclipse.persistence.tools.oracleddl.metadata.URowIdType;
 import org.eclipse.persistence.tools.oracleddl.metadata.VarChar2Type;
 import org.eclipse.persistence.tools.oracleddl.util.DatabaseTypeBuilder;
 
 //testing imports
 import org.eclipse.persistence.tools.oracleddl.test.AllTests;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.buildConnection;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.createTable;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.dropTable;
 
 
 public class IOTTableDDLTestSuite {
 
     static final String IOTTABLE = "DTB_IOTTABLE";
     static final String IOTTABLE_FIELD1 = 
         "ID";
     static final String IOTTABLE_FIELD2 = 
         "NAME";
     static final String IOTTABLE_FIELD3 = 
         "RID";
     static final String CREATE_IOTTABLE = 
         "CREATE TABLE " + IOTTABLE + " (\n" +
             IOTTABLE_FIELD1 + " NUMBER(4,0) NOT NULL ENABLE,\n" +
             IOTTABLE_FIELD2 + " VARCHAR2(25),\n" +
             IOTTABLE_FIELD3 + " UROWID(4000),\n" +
             "PRIMARY KEY (" + IOTTABLE_FIELD1 + "," + IOTTABLE_FIELD2 + ")\n" +
         ") ORGANIZATION INDEX NOCOMPRESS OVERFLOW";
     static final String DROP_IOTTABLE =
         "DROP TABLE " + IOTTABLE;
     
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
         createTable(conn, CREATE_IOTTABLE);
         boolean worked = true;
         String msg = null;
         try {
            tableType = dtBuilder.buildTables(conn, "", IOTTABLE).get(0);
         }
         catch (Exception e) {
             worked = false;
             msg = e.getMessage();
         }
         if (!worked) {
             fail(msg);
         }
         expectedPKFieldNames.add(IOTTABLE_FIELD1);
         expectedPKFieldNames.add(IOTTABLE_FIELD2);
         expectedFieldNames.add(IOTTABLE_FIELD1);
         expectedFieldNames.add(IOTTABLE_FIELD2);
         expectedFieldNames.add(IOTTABLE_FIELD3);
     }
     
     @Test
     public void testTableName() {
         assertEquals("incorrect table name", IOTTABLE , tableType.getTableName());
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
         assertEquals("incorrect type for column [" + IOTTABLE_FIELD1 + "]",
             new NumericType().getTypeName(), col1Type.getTypeName());
         assertTrue("incorrect NULL constraint for column [" + IOTTABLE_FIELD1 + "]",
             field1.notNull());
 
         FieldType field2 = columns.get(1);
         DatabaseType col2Type = field2.getDataType();
         assertEquals("incorrect type for column [" + IOTTABLE_FIELD2 + "]",
             new VarChar2Type().getTypeName(), col2Type.getTypeName());
         assertFalse("incorrect NULL constraint for column [" + IOTTABLE_FIELD2 + "]",
             field2.notNull());
         assertTrue("incorrect size for column [" + IOTTABLE_FIELD2 + "]",
             ((SizedType)col2Type).getSize() == 25);
 
         FieldType field3 = columns.get(2);
         DatabaseType col3Type = field3.getDataType();
         assertEquals("incorrect type for column [" + IOTTABLE_FIELD3 + "]",
             new URowIdType().getTypeName(), col3Type.getTypeName());
         assertFalse("incorrect NULL constraint for column [" + IOTTABLE_FIELD3 + "]",
             field3.notNull());
     }
 
     @AfterClass
     public static void tearDown() {
         dropTable(conn, DROP_IOTTABLE);
     }
 }
