 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pv243.mymaps.serviceImpl;
 
 import cz.muni.fi.pv243.mymaps.dao.MapPermissionDao;
 import cz.muni.fi.pv243.mymaps.dao.MyMapDao;
 import cz.muni.fi.pv243.mymaps.dao.PointOfInterestDao;
 import cz.muni.fi.pv243.mymaps.dto.MapPermission;
 import cz.muni.fi.pv243.mymaps.dto.MyMap;
 import cz.muni.fi.pv243.mymaps.dto.PointOfInterest;
 import cz.muni.fi.pv243.mymaps.dto.User;
 import cz.muni.fi.pv243.mymaps.entities.MapPermissionEntity;
 import cz.muni.fi.pv243.mymaps.entities.MyMapEntity;
 import cz.muni.fi.pv243.mymaps.entities.PointOfInterestEntity;
 import cz.muni.fi.pv243.mymaps.entities.UserEntity;
 import cz.muni.fi.pv243.mymaps.service.MapService;
 import cz.muni.fi.pv243.mymaps.util.EntityDTOconvertor;
 import java.util.ArrayList;
 import java.util.List;
import javax.inject.Inject;
 
 /**
  *
  * @author Kuba
  */
 public class MapServiceImpl implements MapService {
 
    @Inject
     private MyMapDao myMapDao;
    
    @Inject
     private MapPermissionDao mapPermissionDao;
    
    @Inject
     private PointOfInterestDao pointOfInterestDao;
 
     @Override
     public void createMap(MyMap myMap) {
         if (myMap == null) {
             throw new IllegalArgumentException("Map can not be null.");
         }
 
         MyMapEntity newMyMap = EntityDTOconvertor.convertMyMap(myMap);
 
         myMapDao.create(newMyMap);
     }
 
     @Override
     public void updateMap(MyMap myMap) {
         if (myMap == null) {
             throw new IllegalArgumentException("Map can not be null.");
         }
 
         MyMapEntity newMyMap = EntityDTOconvertor.convertMyMap(myMap);
 
         myMapDao.update(newMyMap);
     }
 
     @Override
     public void deleteMap(MyMap myMap) {
         if (myMap == null) {
             throw new IllegalArgumentException("Map can not be null.");
         }
 
         MyMapEntity myMapEntity = EntityDTOconvertor.convertMyMap(myMap);
         List<MapPermissionEntity> mapPermissions = mapPermissionDao.getMapsPermissions(myMapEntity);
 
         for (MapPermissionEntity p : mapPermissions) {
 
             mapPermissionDao.delete(p);
         }
 
         myMapDao.delete(myMapEntity);
     }
 
     @Override
     public MyMap getMapById(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("ID can not be null.");
         }
 
         return EntityDTOconvertor.convertMyMap(myMapDao.getById(id));
     }
 
     @Override
     public List<MyMap> getMapsByUser(User user) {
         if (user == null) {
             throw new IllegalArgumentException("User can not be null.");
         }
 
         UserEntity userEntity = EntityDTOconvertor.convertUser(user);
         List<MyMap> mapList = new ArrayList<>();
         List<MyMapEntity> mapEntityList = myMapDao.findMapsByCreator(userEntity);
 
         for (MyMapEntity m : mapEntityList) {
             mapList.add(EntityDTOconvertor.convertMyMap(m));
         }
 
         return mapList;
     }
 
     @Override
     public List<MapPermission> getMapPermissionsForUser(User user) {
         if (user == null) {
             throw new IllegalArgumentException("User can not be null.");
         }
 
         UserEntity userEntity = EntityDTOconvertor.convertUser(user);
         List<MapPermission> permissionList = new ArrayList<>();
         List<MapPermissionEntity> mapPermissions = mapPermissionDao.getUsersPermissions(userEntity);
 
         for (MapPermissionEntity p : mapPermissions) {
             permissionList.add(EntityDTOconvertor.convertMapPermission(p));
         }
 
         return permissionList;
     }
 
     @Override
     public void createMapPermission(MapPermission mapPermission) {
         if (mapPermission == null) {
             throw new IllegalArgumentException("Map permission can not be null.");
         }
 
         MapPermissionEntity mapPermissionEntity = EntityDTOconvertor.convertMapPermission(mapPermission);
 
         mapPermissionDao.create(mapPermissionEntity);
     }
 
     @Override
     public void updateMapPermission(MapPermission mapPermission) {
         if (mapPermission == null) {
             throw new IllegalArgumentException("Map permission can not be null.");
         }
 
         MapPermissionEntity mapPermissionEntity = EntityDTOconvertor.convertMapPermission(mapPermission);
 
         mapPermissionDao.update(mapPermissionEntity);
     }
 
     @Override
     public void deleteMapPermission(MapPermission mapPermission) {
         if (mapPermission == null) {
             throw new IllegalArgumentException("Map permission can not be null.");
         }
 
         MapPermissionEntity mapPermissionEntity = EntityDTOconvertor.convertMapPermission(mapPermission);
 
         mapPermissionDao.delete(mapPermissionEntity);
     }
 
     @Override
     public MapPermission getMapPermissionById(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("ID can not be null.");
         }
 
         return EntityDTOconvertor.convertMapPermission(mapPermissionDao.getById(id));
     }
 
     @Override
     public List<MapPermission> getMapPermissionsForMap(MyMap myMap) {
         if (myMap == null) {
             throw new IllegalArgumentException("Map can not be null.");
         }
 
         MyMapEntity myMapEntity = EntityDTOconvertor.convertMyMap(myMap);
         List<MapPermission> permissionList = new ArrayList<>();
         List<MapPermissionEntity> mapPermissions = mapPermissionDao.getMapsPermissions(myMapEntity);
 
         for (MapPermissionEntity p : mapPermissions) {
             permissionList.add(EntityDTOconvertor.convertMapPermission(p));
         }
 
         return permissionList;
     }
 
     @Override
     public void createPointOfInterest(PointOfInterest pointOfInterest) {
         if (pointOfInterest == null) {
             throw new IllegalArgumentException("Point Of Interest can not be null.");
         }
 
         PointOfInterestEntity pointOfInterestEntity = EntityDTOconvertor.convertPointOfInterest(pointOfInterest);
 
         pointOfInterestDao.create(pointOfInterestEntity);
     }
 
     @Override
     public void updatePointOfInterest(PointOfInterest pointOfInterest) {
         if (pointOfInterest == null) {
             throw new IllegalArgumentException("Point Of Interest can not be null.");
         }
 
         PointOfInterestEntity pointOfInterestEntity = EntityDTOconvertor.convertPointOfInterest(pointOfInterest);
 
         pointOfInterestDao.update(pointOfInterestEntity);
     }
 
     @Override
     public void deletePointOfInterest(PointOfInterest pointOfInterest) {
         if (pointOfInterest == null) {
             throw new IllegalArgumentException("Point Of Interest can not be null.");
         }
 
         PointOfInterestEntity pointOfInterestEntity = EntityDTOconvertor.convertPointOfInterest(pointOfInterest);
 
         pointOfInterestDao.delete(pointOfInterestEntity);
     }
 
     @Override
     public PointOfInterest getPointOfInterestById(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("ID can not be null.");
         }
 
         return EntityDTOconvertor.convertPointOfInterest(pointOfInterestDao.getById(id));
     }
 }
