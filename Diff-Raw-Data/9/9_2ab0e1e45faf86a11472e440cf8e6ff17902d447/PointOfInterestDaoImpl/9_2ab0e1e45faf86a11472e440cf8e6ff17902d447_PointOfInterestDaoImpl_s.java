 package cz.muni.fi.pv243.mymaps.dao.impl;
 
 import cz.muni.fi.pv243.mymaps.dao.PointOfInterestDao;
 import cz.muni.fi.pv243.mymaps.entities.PointOfInterestEntity;
 
 public class PointOfInterestDaoImpl extends GenericDaoImpl<PointOfInterestEntity> implements PointOfInterestDao {
 
    private static final String CACHE_NAME = "User";
 
     @Override
     protected String cacheName() {
         return CACHE_NAME;
     }
 }
