 /*
  * Copyright (C) 2000 - 2011 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection withWriter Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have recieved a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://www.silverpeas.org/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.silverpeas.migration.contentmanagement;
 
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.database.DatabaseConnection;
 import org.springframework.jdbc.datasource.DataSourceUtils;
 import java.sql.Connection;
 import org.dbunit.dataset.ITable;
 import javax.inject.Inject;
 import javax.sql.DataSource;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.DataSourceDatabaseTester;
 import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 import static org.dbunit.Assertion.*;
 
 /**
  * Tests on the migration of the sb_contentmanager_content table.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations="/spring-content-datasource.xml")
 public class DuplicatedContentRemovingTest {
   
   public static final String CONTENT_TABLE = "sb_contentmanager_content";
   public static final String CONTENT_CLASSIFICATION_TABLE = "sb_classifyengine_classify";
   public static final int CONTENT_COUNT = 12;
   public static final int DUPLICATE_CONTENT_COUNT = 5;
   public static final int DUPLICATE_CONTENT_CLASSIFICATION_COUNT = 1;
   public static final int CONTENT_CLASSIFICATION_COUNT = 4;
   
   private IDatabaseTester databaseTester;
   @Inject
   private DataSource dataSource;
 
   public DuplicatedContentRemovingTest() {
   }
 
   @Before
   public void prepareDatabase() throws Exception {
     assertThat(dataSource, notNullValue());
     databaseTester = new DataSourceDatabaseTester(dataSource);
     databaseTester.setDataSet(getDataSet());
     databaseTester.onSetup();
   }
   
   @After
   public void cleanDatabase() throws Exception {
     databaseTester.onTearDown();
   }
 
   /**
    * Test the migration of the sb_contentmanager_content table.
    */
   @Test
   public void testMigration() throws Exception {
     DuplicateContentRemoving duplicateContentRemoving = new DuplicateContentRemoving();
     duplicateContentRemoving.setConnection(databaseTester.getConnection().getConnection());
     
     duplicateContentRemoving.migrate();
     
     ITable actualContentTable = getActualTable(CONTENT_TABLE);
     ITable expectedContentTable = getExpectedTable(CONTENT_TABLE);
     assertThat(actualContentTable.getRowCount(), is(CONTENT_COUNT - DUPLICATE_CONTENT_COUNT));
     assertEquals(expectedContentTable, actualContentTable);
     
     ITable actualClassificationTable = getActualTable(CONTENT_CLASSIFICATION_TABLE);
     ITable expectedClassificationTable = getExpectedTable(CONTENT_CLASSIFICATION_TABLE);
     assertThat(actualClassificationTable.getRowCount(), is(CONTENT_CLASSIFICATION_COUNT -
             DUPLICATE_CONTENT_CLASSIFICATION_COUNT));
     assertEquals(expectedClassificationTable, actualClassificationTable);
   }
 
   protected IDataSet getDataSet() throws Exception {
    return new FlatXmlDataSet(getClass().getResourceAsStream("pdc-dataset.xml"));
   }
   
   protected IDataSet getExpectedDataSet() throws Exception {
    return new FlatXmlDataSet(getClass().getResourceAsStream("expected-pdc-dataset.xml"));
   }
   
   protected ITable getActualTable(String tableName) throws Exception {
     Connection connection = DataSourceUtils.getConnection(dataSource);
     IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
     IDataSet dataSet = databaseConnection.createDataSet();
     ITable table = dataSet.getTable(tableName);
     DataSourceUtils.releaseConnection(connection, dataSource);
     return table;
   }
   
   protected ITable getExpectedTable(String tableName) throws Exception {
     IDataSet dataSet = getExpectedDataSet();
     return dataSet.getTable(tableName);
   }
 }
