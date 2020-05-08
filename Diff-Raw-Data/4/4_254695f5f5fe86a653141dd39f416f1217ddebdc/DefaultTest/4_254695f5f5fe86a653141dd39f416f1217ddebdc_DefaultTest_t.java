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
 package org.seasar.dao.impl;
 
 import java.io.Serializable;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.seasar.extension.jdbc.impl.MapListResultSetHandler;
 import org.seasar.extension.jdbc.util.DatabaseMetaDataUtil;
 import org.seasar.extension.unit.S2TestCase;
 import org.seasar.framework.exception.SRuntimeException;
 
 /**
  * @author manhole
  */
 public class DefaultTest extends S2TestCase {
 
     private DefaultTableDao defaultTableDao;
 
     private PkOnlyTableDao pkOnlyTableDao;
 
     protected void setUp() throws Exception {
         super.setUp();
         include("DefaultTest.dicon");
     }
 
     public void testLearningMetaDataForColumnsTx() throws Exception {
         final DatabaseMetaData metaData = getConnection().getMetaData();
         String userName = metaData.getUserName();
         userName = DatabaseMetaDataUtil.convertIdentifier(metaData, userName);
         final ResultSet rset = metaData.getColumns(null, userName,
                 "DEFAULT_TABLE", null);
         final ResultSetMetaData rMeta = rset.getMetaData();
         final int columnCount = rMeta.getColumnCount();
         for (int i = 1; i <= columnCount; i++) {
             final String columnName = rMeta.getColumnName(i);
             System.out.println("[" + i + "] " + columnName);
         }
         MapListResultSetHandler handler = new MapListResultSetHandler();
         List l = (List) handler.handle(rset);
         for (Iterator it = l.iterator(); it.hasNext();) {
             Map m = (Map) it.next();
             System.out.println(m);
         }
     }
 
     public void testLearningGetDefaultValueTx() throws Exception {
         final DatabaseMetaData metaData = getConnection().getMetaData();
         String userName = DatabaseMetaDataUtil.convertIdentifier(metaData,
                 metaData.getUserName());
         final ResultSet rset = metaData.getColumns(null, userName,
                 "DEFAULT_TABLE", null);
 
         final int[] columns = { 0, 0, 0, 0 };
         while (rset.next()) {
             final String columnName = rset.getString("COLUMN_NAME");
             final String columnDef = rset.getString("COLUMN_DEF");
             System.out.println(columnName + "[" + columnDef + "]");
             if ("ID".equals(columnName)) {
                 columns[0]++;
                 // assertEquals((String) null, columnDef);
             } else if ("AAA".equals(columnName)) {
                 columns[1]++;
                 // assertEquals("'ABC'", columnDef);
                 assertEquals(columnDef, true, columnDef.indexOf("ABC") > -1);
             } else if ("BBB".equals(columnName)) {
                 columns[2]++;
                 assertEquals(columnDef, (String) null, columnDef);
             } else if ("VERSION_NO".equals(columnName)) {
                 columns[3]++;
                 assertEquals(columnDef, (String) null, columnDef);
             } else {
                 fail(columnName);
             }
         }
         assertEquals(1, columns[0]);
         assertEquals(1, columns[1]);
         assertEquals(1, columns[2]);
         assertEquals(1, columns[3]);
     }
 
     public void testInsertByAutoSqlTx() throws Exception {
         Integer id;
         {
             DefaultTable bean = new DefaultTable();
             bean.setAaa("1234567");
             bean.setBbb("890");
             defaultTableDao.insert(bean);
             id = bean.getId();
         }
         {
             final DefaultTable bean = defaultTableDao.getDefaultTable(id);
             assertEquals("inserted setted value", "1234567", bean.getAaa());
             assertEquals("890", bean.getBbb());
             assertEquals(new Integer(0), bean.getVersionNo());
         }
     }
 
     public void testInsertBatchByAutoSqlTx() throws Exception {
         DefaultTable bean1 = new DefaultTable();
         bean1.setAaa("11");
         bean1.setBbb("12");
         DefaultTable bean2 = new DefaultTable();
         bean2.setAaa("21");
         bean2.setBbb("22");
         int ret = defaultTableDao
                 .insertBatch(new DefaultTable[] { bean1, bean2 });
         assertEquals(2, ret);
 
         final List defaultTables = defaultTableDao.getDefaultTables();
         assertEquals(2, defaultTables.size());
         {
             final DefaultTable object = (DefaultTable) defaultTables.get(0);
             assertEquals("11", object.getAaa());
             assertEquals("12", object.getBbb());
         }
         {
             final DefaultTable object = (DefaultTable) defaultTables.get(1);
             assertEquals("21", object.getAaa());
             assertEquals("22", object.getBbb());
         }
     }
 
     public void testInsertBatchDefaultByAutoSqlTx() throws Exception {
         DefaultTable bean1 = new DefaultTable();
         bean1.setAaa("11");
         bean1.setBbb("12");
         DefaultTable bean2 = new DefaultTable();
         bean2.setAaa(null);
         bean2.setBbb("22");
         DefaultTable bean3 = new DefaultTable();
         bean3.setAaa("31");
         bean3.setBbb(null);
         int ret = defaultTableDao.insertBatch(new DefaultTable[] { bean1,
                 bean2, bean3 });
         assertEquals(3, ret);
 
         final List defaultTables = defaultTableDao.getDefaultTables();
         assertEquals(3, defaultTables.size());
         {
             final DefaultTable object = (DefaultTable) defaultTables.get(0);
             assertEquals("11", object.getAaa());
             assertEquals("12", object.getBbb());
         }
         {
             final DefaultTable object = (DefaultTable) defaultTables.get(1);
             assertEquals((String) null, object.getAaa());
             assertEquals("22", object.getBbb());
         }
         {
             final DefaultTable object = (DefaultTable) defaultTables.get(2);
             assertEquals("31", object.getAaa());
             assertEquals((String) null, object.getBbb());
         }
     }
 
     // [DAO-9]
     public void testInsertBatchDefaultByAutoSql2Tx() throws Exception {
         DefaultTable bean1 = new DefaultTable();
         bean1.setAaa("11");
         bean1.setBbb(null);
         DefaultTable bean2 = new DefaultTable();
         bean2.setAaa("21");
         bean2.setBbb("22");
         int ret = defaultTableDao
                 .insertBatch(new DefaultTable[] { bean1, bean2 });
         assertEquals(2, ret);
 
         final List defaultTables = defaultTableDao.getDefaultTables();
         assertEquals(2, defaultTables.size());
         {
             final DefaultTable object = (DefaultTable) defaultTables.get(0);
             assertEquals("11", object.getAaa());
             assertEquals((String) null, object.getBbb());
         }
         {
             final DefaultTable object = (DefaultTable) defaultTables.get(1);
             assertEquals("21", object.getAaa());
             assertEquals("22", object.getBbb());
         }
     }
 
     public void testInsertDefaultByAutoSqlTx() throws Exception {
         Integer id;
         {
             DefaultTable bean = new DefaultTable();
             bean.setBbb("bbbb");
             defaultTableDao.insert(bean);
             id = bean.getId();
         }
         {
             final DefaultTable bean = defaultTableDao.getDefaultTable(id);
             assertEquals("inserted DEFAULT value", "ABC", bean.getAaa());
             assertEquals("bbbb", bean.getBbb());
             assertEquals(new Integer(0), bean.getVersionNo());
         }
     }
 
     /*
      * [DAO-29]にて、例外を投げないようにしました。
      */
     public void testNotThrownExceptionWhenNullDataOnlyTx() throws Exception {
         Integer id;
         {
             DefaultTable bean = new DefaultTable();
             defaultTableDao.insert(bean);
             id = bean.getId();
         }
         {
             final DefaultTable bean = defaultTableDao.getDefaultTable(id);
             assertEquals("inserted DEFAULT value", "ABC", bean.getAaa());
            assertEquals((String) null, bean.getBbb());
             assertEquals(new Integer(0), bean.getVersionNo());
         }
     }
 
     /*
      * 問題が見つかったため[DAO-9]、バッチ更新ではINSERT文からnull値のカラムを
      * 除外する機能(s2dao-1.0.33で追加)をサポートしないことにしました。
      */
     public void no_testThrownExceptionWhenNullDataOnlyByBatchTx()
             throws Exception {
         DefaultTable bean = new DefaultTable();
         try {
             defaultTableDao.insertBatch(new DefaultTable[] { bean });
             fail("should be thrown SRuntimeException, when only null properties");
         } catch (SRuntimeException e) {
             e.printStackTrace();
             assertEquals("EDAO0014", e.getMessageCode());
         }
     }
 
     public void testInsertByManualSqlTx() throws Exception {
         Integer id;
         {
             DefaultTable bean = new DefaultTable();
             bean.setAaa("foooo");
             defaultTableDao.insertBySql(bean);
             id = bean.getId();
         }
         {
             final DefaultTable bean = defaultTableDao.getDefaultTable(id);
             assertEquals("foooo", bean.getAaa());
             assertEquals((String) null, bean.getBbb());
             assertEquals(new Integer(0), bean.getVersionNo());
         }
     }
 
     public void testInsertDefaultByManualSqlTx() throws Exception {
         Integer id;
         {
             DefaultTable bean = new DefaultTable();
             bean.setBbb("ttt");
             defaultTableDao.insertBySql(bean);
             id = bean.getId();
         }
         {
             final DefaultTable bean = defaultTableDao.getDefaultTable(id);
             assertEquals("ABC", bean.getAaa());
             assertEquals("ttt", bean.getBbb());
             assertEquals(new Integer(0), bean.getVersionNo());
         }
     }
 
    // https://www.seasar.org/issues/browse/DAO-16
     public void testInsertPkOnlyTableTx() throws Exception {
         PkOnlyTable bean = new PkOnlyTable();
         bean.setAaa(new Integer(123));
         bean.setBbb(new Integer(456));
         pkOnlyTableDao.insert(bean);
         final List list = pkOnlyTableDao.findAll();
         assertEquals(1, list.size());
     }
 
     public static interface DefaultTableDao {
 
         public Class BEAN = DefaultTable.class;
 
         public String getDefaultTable_ARGS = "id";
 
         public DefaultTable getDefaultTable(Integer id);
 
         public String getDefaultTables_QUERY = "ORDER BY ID";
 
         public List getDefaultTables();
 
         public void insert(DefaultTable largeBinary);
 
         public void insertBySql(DefaultTable largeBinary);
 
         public void update(DefaultTable largeBinary);
 
         public int insertBatch(DefaultTable[] largeBinaries);
 
     }
 
     public static class DefaultTable implements Serializable {
 
         private static final long serialVersionUID = 1L;
 
         public static final String TABLE = "DEFAULT_TABLE";
 
         public static final String id_ID = "identity";
 
         private Integer id;
 
         private String aaa;
 
         private String bbb;
 
         private Integer versionNo;
 
         public Integer getId() {
             return id;
         }
 
         public void setId(Integer id) {
             this.id = id;
         }
 
         public String getAaa() {
             return aaa;
         }
 
         public void setAaa(String defaultColumn) {
             this.aaa = defaultColumn;
         }
 
         public String getBbb() {
             return bbb;
         }
 
         public void setBbb(String bbb) {
             this.bbb = bbb;
         }
 
         public Integer getVersionNo() {
             return versionNo;
         }
 
         public void setVersionNo(Integer versionNo) {
             this.versionNo = versionNo;
         }
     }
 
     public static interface PkOnlyTableDao {
 
         Class BEAN = PkOnlyTable.class;
 
         void insert(PkOnlyTable table);
 
         List findAll();
 
     }
 
     public static class PkOnlyTable implements Serializable {
 
         private static final long serialVersionUID = 1L;
 
         public static final String TABLE = "PK_ONLY_TABLE";
 
         private Integer aaa;
 
         private Integer bbb;
 
         public Integer getAaa() {
             return aaa;
         }
 
         public void setAaa(Integer aaa) {
             this.aaa = aaa;
         }
 
         public Integer getBbb() {
             return bbb;
         }
 
         public void setBbb(Integer bbb) {
             this.bbb = bbb;
         }
 
     }
 
 }
