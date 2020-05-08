 /*
  * Copyright (c) 2012 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.cloud.demo.model.sql;
 
 import com.google.cloud.demo.ConfigManager;
 import com.google.cloud.demo.model.DemoUser;
 import com.google.cloud.demo.model.DemoUserManager;
 import com.google.cloud.demo.model.Utils;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * Demo user entity manager for Cloud SQL.
  *
  */
 public class DemoUserManagerSql extends DemoEntityManagerSql<DemoUser> implements DemoUserManager {
   private static final String SQL_SELECT_ALL_USERS =
       "SELECT UserId, Email, Nickname FROM DemoUsers";
   private static final String QUERY_SELECT_USER = SQL_SELECT_ALL_USERS + " WHERE UserId=?";
   private static final String QUERY_DELETE_USER = "DELETE FROM DemoUsers WHERE UserId=?";
   private static final String SQL_UPDATE_USER =
       "UPDATE DemoUsers SET Email=?, Nickname=? WHERE UserId=?";
   private static final String SQL_INSERT_USER =
       "INSERT INTO DemoUsers(Email, Nickname, UserId) VALUES(?, ?, ?)";
 
   public DemoUserManagerSql(ConfigManager configManager) {
     super(configManager);
   }
 
   @Override
   public List<DemoUser> getEntities() {
     return runInTransaction(new TransactionalOperation<List<DemoUser>>() {
       @Override
       public List<DemoUser> execute(Connection conn) throws SQLException {
         return queryEntities(conn, SQL_SELECT_ALL_USERS, new DemoUserSelectQueryCallback() {
           @Override
           public void prepareStatement(PreparedStatement stmt) {
             // NOP
           }
         });
       }});
   }
 
   @Override
   public DemoUser deleteEntity(final DemoUser entity) {
     return runInTransaction(new TransactionalOperation<DemoUser>() {
       @Override
       public DemoUser execute(Connection conn) throws SQLException {
         return deleteEntity(conn, entity, QUERY_DELETE_USER, new DefaultUpdateQueryCallback() {
           @Override
           public void prepareStatement(PreparedStatement stmt) throws SQLException {
             stmt.setString(1, entity.getUserId());
           }
         });
       }});
   }
 
   @Override
   public DemoUser upsertEntity(final DemoUser demoEntity) {
     Utils.assertTrue(demoEntity instanceof DemoUserSql, "Could only handle sql entity");
     DemoUserSql sqlEntity = (DemoUserSql) demoEntity;
     final boolean isUpdate = sqlEntity.getUserId() != null && sqlEntity.isPersistent();
     final String query = isUpdate ? SQL_UPDATE_USER : SQL_INSERT_USER;
     return runInTransaction(new TransactionalOperation<DemoUser>() {
       @Override
       public DemoUser execute(Connection conn) throws SQLException {
         return upsertEntity(conn, demoEntity, query, new DefaultUpdateQueryCallback() {
           @Override
           public void prepareStatement(PreparedStatement stmt) throws SQLException {
             int count = 1;
             stmt.setString(count++, demoEntity.getEmail());
             stmt.setString(count++, demoEntity.getNickname());
             stmt.setString(count, demoEntity.getUserId());
           }
         });
       }});
   }
 
   @Override
   public DemoUser getUser(final String userId) {
     final DemoUserSelectQueryCallback callback = new DemoUserSelectQueryCallback() {
       @Override
       public void prepareStatement(PreparedStatement stmt) throws SQLException {
         stmt.setString(1, userId);
       }
 
       @Override
       public DemoUser fromResultSet(ResultSet rs) throws SQLException {
         DemoUser user = newUser(userId);
        user.setEmail(rs.getString(2));
        user.setNickname(rs.getString(3));
         return user;
       }
     };
     return runInTransaction(new TransactionalOperation<DemoUser>() {
       @Override
       public DemoUser execute(Connection conn) throws SQLException {
         return queryEntity(conn, QUERY_SELECT_USER, callback);
       }});
   }
 
   @Override
   public DemoUser newUser(String userId) {
     return new DemoUserSql(userId);
   }
 
   private abstract class DemoUserSelectQueryCallback
       implements QueryCallback.SelectQueryCallback<DemoUser> {
     @Override
     public DemoUser fromResultSet(ResultSet rs) throws SQLException {
       int count = 1;
       String userId = rs.getString(count++);
       String email = rs.getString(count++);
       String nickname = rs.getString(count++);
       DemoUser user = newUser(userId);
       user.setEmail(email);
       user.setNickname(nickname);
       return user;
     }
   }
 }
