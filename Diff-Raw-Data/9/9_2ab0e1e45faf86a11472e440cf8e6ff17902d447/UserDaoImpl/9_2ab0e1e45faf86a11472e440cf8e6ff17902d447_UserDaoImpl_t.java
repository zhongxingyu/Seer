 package cz.muni.fi.pv243.mymaps.dao.impl;
 
 import cz.muni.fi.pv243.mymaps.dao.UserDao;
 import cz.muni.fi.pv243.mymaps.entities.UserEntity;
 
 public class UserDaoImpl extends GenericDaoImpl<UserEntity> implements UserDao {
 
    private static final String CACHE_NAME = "User";
 
     @Override
     protected String cacheName() {
         return CACHE_NAME;
     }
 }
