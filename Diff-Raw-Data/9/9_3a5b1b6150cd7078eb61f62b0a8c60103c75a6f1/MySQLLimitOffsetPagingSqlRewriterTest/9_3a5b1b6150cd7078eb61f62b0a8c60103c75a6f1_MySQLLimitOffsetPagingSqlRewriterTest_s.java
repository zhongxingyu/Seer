 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
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
 package org.seasar.dao.pager;
 
 import junit.framework.TestCase;
 
 /**
  * @author azusa
  * 
  */
 public class MySQLLimitOffsetPagingSqlRewriterTest extends TestCase {
 
     LimitOffsetPagingSqlRewriter rewriter = new MySQLLimitOffsetPagingSqlRewriter();
 
     public void testMakeCountSql() {
         assertEquals("SELECT FOUND_ROWS()", rewriter
                 .makeCountSql("SELECT * FROM DEPARTMENT order by id"));
     }
 
     /*
      * public void testMakeBaseSql() throws Exception { try {
      * PagerContext.getContext().pushArgs(createNormalArgs()); assertEquals(
      * "SELECTの前のネイティブSQLを除去", "SELECT * FROM DEPARTMENT",
      * wrapper.makeBaseSql("native sql ... SELECT * FROM DEPARTMENT"));
      * assertEquals( "ネイティブSQLが存在しない場合、元のSQLも変化なし", "SELECT * FROM DEPARTMENT",
      * wrapper.makeBaseSql("SELECT * FROM DEPARTMENT")); } finally {
      * PagerContext.getContext().popArgs(); } }
      */
     public void testLimitOffsetSql() throws Exception {
         assertEquals(
                 "指定されたlimit offsetが付加されたSQLを生成",
                 "SELECT SQL_CALC_FOUND_ROWS * FROM DEPARTMENT LIMIT 10 OFFSET 55",
                 rewriter.makeLimitOffsetSql("SELECT * FROM DEPARTMENT", 10, 55));
     }
 
}
