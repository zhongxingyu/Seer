 package com.sgrailways.resteasy.repositories;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.jdbc.JdbcConnectionSource;
 import com.j256.ormlite.support.ConnectionSource;
 import com.sgrailways.resteasy.model.Locomotive;
 
 import java.sql.SQLException;
import java.util.Collections;
 import java.util.List;
 
 public class LocomotivesRepository {
     private Dao<Locomotive, Integer> locomotiveDao;
     String databaseUrl = "jdbc:h2:resteasy";
 
     public LocomotivesRepository() {
         ConnectionSource connectionSource = null;
         try {
             connectionSource = new JdbcConnectionSource(databaseUrl, "sa", "");
              locomotiveDao =
                     DaoManager.createDao(connectionSource, Locomotive.class);
 
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public List<Locomotive> list() {
         try {
             return locomotiveDao.queryForAll();
         } catch (SQLException e) {
             e.printStackTrace();
            return Collections.emptyList();
         }
     }
 }
