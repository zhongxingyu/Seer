 
 package com.github.shimal.query_utils.sql;
 
 import com.github.shimal.query_utils.Querable;
 import org.junit.Test;
 
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.and;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.asc;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.desc;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.eq;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.gt;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.gte;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.like;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.likeLower;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.lt;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.lte;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.ne;
 import static com.github.shimal.query_utils.sql.SqlQueryUtils.or;
 
 
 
 public class SqlQueryTest {
 
 
 
     //~ --- [CONSTRUCTORS] ---------------------------------------------------------------------------------------------
 
     public SqlQueryTest() {
 
     }
 
 
 
     //~ --- [METHODS] --------------------------------------------------------------------------------------------------
 
     @Test
     public void testCountQuery() {
 
         SqlQuery sqlQuery = new SqlQuery("USERS");
        String   expected = "SELECT COUNT(S.*) FROM USERS S";
 
         if (!expected.equals(sqlQuery.count())) {
             assert false;
         }
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Test
     public void testJoinQuery() {
 
         try {
             SqlQuery sqlQuery = new SqlQuery("USERS").join("USER_PERMISSIONS", "UP");
             String   expected = "SELECT S.* FROM USERS S, USER_PERMISSIONS UP";
 
             if (!expected.equals(sqlQuery.select())) {
                 assert false;
             }
 
             sqlQuery = new SqlQuery("USERS").join("USER_PERMISSIONS", "UP", "UP.USER_ID", "U.ID");
             expected = "SELECT S.* FROM USERS S, USER_PERMISSIONS UP WHERE (UP.USER_ID = U.ID)";
 
             if (!expected.equals(sqlQuery.select())) {
                 assert false;
             }
 
             sqlQuery = new SqlQuery("USERS", "U");
             sqlQuery = sqlQuery.join("USER_PERMISSIONS", "UP", "UP.USER_ID", "U.ID");
             sqlQuery = sqlQuery.join("PERMISSIONS", "P", "P.ID", "UP.PERMISSION_ID");
             sqlQuery = sqlQuery.join("ROLE_PERMISSIONS", "RP", "RP.PERMISSION_ID", "P.ID");
             sqlQuery = sqlQuery.join("ROLES", "R", "R.ID", "RP.ROLE_ID");
             sqlQuery = sqlQuery.join("USER_ROLES", "UR", "UR.USER_ID", "U.ID");
 
             expected =  "SELECT U.*";
             expected += " FROM";
             expected += " USERS U, USER_ROLES UR, PERMISSIONS P, ROLES R, USER_PERMISSIONS UP, ROLE_PERMISSIONS RP";
             expected += " WHERE";
             expected += " (UP.USER_ID = U.ID";
             expected += " AND";
             expected += " P.ID = UP.PERMISSION_ID";
             expected += " AND";
             expected += " RP.PERMISSION_ID = P.ID";
             expected += " AND";
             expected += " R.ID = RP.ROLE_ID";
             expected += " AND";
             expected += " UR.USER_ID = U.ID)";
 
             if (!expected.equals(sqlQuery.select())) {
                 assert false;
             }
         } catch (Exception e) {
             assert false;
         }
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Test
     public void testJoinWithMultiWhereQuery() {
 
         try {
             SqlQuery sqlQuery = new SqlQuery("USERS", "U");
             sqlQuery = sqlQuery.join("USER_PERMISSIONS", "UP", "UP.USER_ID", "U.ID");
             sqlQuery = sqlQuery.join("PERMISSIONS", "P", "P.ID", "UP.PERMISSION_ID");
             sqlQuery = sqlQuery.join("ROLE_PERMISSIONS", "RP", "RP.PERMISSION_ID", "P.ID");
             sqlQuery = sqlQuery.join("ROLES", "R", "R.ID", "RP.ROLE_ID");
             sqlQuery = sqlQuery.join("USER_ROLES", "UR", "UR.USER_ID", "U.ID");
             sqlQuery = (SqlQuery) sqlQuery.where(or(eq("U.NAME", "U.USERNAME"), and(gte("U.ID", 5), eq("U.ID", 2))));
 
             String expected = "SELECT U.*";
             expected += " FROM";
             expected += " USERS U, USER_ROLES UR, PERMISSIONS P, ROLES R, USER_PERMISSIONS UP, ROLE_PERMISSIONS RP";
             expected += " WHERE";
             expected += " (UP.USER_ID = U.ID";
             expected += " AND";
             expected += " P.ID = UP.PERMISSION_ID";
             expected += " AND";
             expected += " RP.PERMISSION_ID = P.ID";
             expected += " AND";
             expected += " R.ID = RP.ROLE_ID";
             expected += " AND";
             expected += " UR.USER_ID = U.ID";
             expected += " AND";
             expected += " (U.NAME = U.USERNAME OR (U.ID >= 5 AND U.ID = 2)))";
 
             if (!expected.equals(sqlQuery.select())) {
                 assert false;
             }
         } catch (Exception e) {
             assert false;
         }
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Test
     public void testMultiWhereQuery() {
 
         Querable sqlQuery = new SqlQuery("USERS", "U").where("U.NAME", "U.USERNAME").where("U.ID", 3);
         String   expected = "SELECT U.* FROM USERS U WHERE (U.NAME = U.USERNAME AND U.ID = 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         SqlAnd andTree = new SqlAnd();
         andTree.add(new SqlConstraint("U.NAME", "U.USERNAME"));
         andTree.add(new SqlConstraint("U.ID", 3));
         sqlQuery = new SqlQuery("USERS", "U").where(andTree);
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         andTree  = new SqlAnd(new SqlConstraint("U.NAME", "U.USERNAME"), new SqlConstraint("U.ID", 3));
         sqlQuery = new SqlQuery("USERS", "U").where(andTree);
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         andTree  = new SqlAnd(eq("U.NAME", "U.USERNAME"), eq("U.ID", 3));
         sqlQuery = new SqlQuery("USERS", "U").where(andTree);
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where(and());
         sqlQuery = sqlQuery.where(eq("U.NAME", "U.USERNAME"));
         sqlQuery = sqlQuery.where(eq("U.ID", 3));
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where(new SqlOr(eq("U.NAME", "U.USERNAME"), eq("U.ID", 3)));
         expected = "SELECT U.* FROM USERS U WHERE (U.NAME = U.USERNAME OR U.ID = 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where(or(eq("U.NAME", "U.USERNAME"), eq("U.ID", 3)));
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where(or(eq("U.NAME", "U.USERNAME"), and(gte("U.ID", 5), eq("U.ID", 2))));
         expected = "SELECT U.* FROM USERS U WHERE (U.NAME = U.USERNAME OR (U.ID >= 5 AND U.ID = 2))";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Test
     public void testOrder() {
 
         Querable sqlQuery = new SqlQuery("USERS").asc("S.ID");
         String   expected = "SELECT S.* FROM USERS S ORDER BY S.ID ASC";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS").asc("S.ID").desc("S.USERNAME");
         expected = "SELECT S.* FROM USERS S ORDER BY S.ID ASC, S.USERNAME DESC";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS").order(asc("S.ID"), desc("S.USERNAME"));
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Test
     public void testSelectQuery() {
 
         SqlQuery sqlQuery = new SqlQuery("USERS");
         String   expected = "SELECT S.* FROM USERS S";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         // Use another alias for table
         sqlQuery = new SqlQuery("USERS", "A");
         expected = "SELECT A.* FROM USERS A";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Test
     public void testWhereQuery() {
 
         Querable sqlQuery = new SqlQuery("USERS", "U").where("U.NAME", "U.USERNAME");
         String   expected = "SELECT U.* FROM USERS U WHERE (U.NAME = U.USERNAME)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where("U.USER_ID", 3);
         expected = "SELECT U.* FROM USERS U WHERE (U.USER_ID = 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where("U.USERNAME", "'admin'");
         expected = "SELECT U.* FROM USERS U WHERE (U.USERNAME = 'admin')";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where(eq("U.USER_ID", 3));
         expected = "SELECT U.* FROM USERS U WHERE (U.USER_ID = 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where(ne("U.USER_ID", 3));
         expected = "SELECT U.* FROM USERS U WHERE (U.USER_ID != 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where(like("U.NAME", "'ahmet'"));
         expected = "SELECT U.* FROM USERS U WHERE (U.NAME LIKE 'ahmet')";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         sqlQuery = new SqlQuery("USERS", "U").where(likeLower("U.NAME", "'ahmet'"));
         expected = "SELECT U.* FROM USERS U WHERE (LOWER(U.NAME) LIKE LOWER('ahmet'))";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         // less than
         sqlQuery = new SqlQuery("USERS", "U").where(lt("U.USER_ID", 3));
         expected = "SELECT U.* FROM USERS U WHERE (U.USER_ID < 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         // less than or equal to
         sqlQuery = new SqlQuery("USERS", "U").where(lte("U.USER_ID", 3));
         expected = "SELECT U.* FROM USERS U WHERE (U.USER_ID <= 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         // greater than
         sqlQuery = new SqlQuery("USERS", "U").where(gt("U.USER_ID", 3));
         expected = "SELECT U.* FROM USERS U WHERE (U.USER_ID > 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
 
         // greater than or equal to
         sqlQuery = new SqlQuery("USERS", "U").where(gte("U.USER_ID", 3));
         expected = "SELECT U.* FROM USERS U WHERE (U.USER_ID >= 3)";
 
         if (!expected.equals(sqlQuery.select())) {
             assert false;
         }
     }
 }
