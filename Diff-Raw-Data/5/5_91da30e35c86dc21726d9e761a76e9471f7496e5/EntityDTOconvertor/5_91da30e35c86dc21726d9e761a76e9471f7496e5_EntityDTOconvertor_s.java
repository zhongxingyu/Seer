 package cz.muni.fi.pv243.mymaps.util;
 
 import cz.muni.fi.pv243.mymaps.dto.MapPermission;
 import cz.muni.fi.pv243.mymaps.dto.MyMap;
 import cz.muni.fi.pv243.mymaps.dto.Point;
 import cz.muni.fi.pv243.mymaps.dto.PointOfInterest;
 import cz.muni.fi.pv243.mymaps.dto.User;
 import cz.muni.fi.pv243.mymaps.dto.View;
 import cz.muni.fi.pv243.mymaps.entities.MapPermissionEntity;
 import cz.muni.fi.pv243.mymaps.entities.MyMapEntity;
 import cz.muni.fi.pv243.mymaps.entities.PointEntity;
 import cz.muni.fi.pv243.mymaps.entities.PointOfInterestEntity;
 import cz.muni.fi.pv243.mymaps.entities.UserEntity;
 import cz.muni.fi.pv243.mymaps.entities.ViewEntity;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Kuba
  */
 public class EntityDTOconvertor {
 
     public static MapPermission convertMapPermission(MapPermissionEntity mapPermission) {
         if (mapPermission == null) {
             throw new IllegalArgumentException("Map Permission can not be null.");
         }
 
         MapPermission newMapPermission = new MapPermission();
         newMapPermission.setMap(EntityDTOconvertor.convertMyMap(mapPermission.getMap()));
         newMapPermission.setPermission(mapPermission.getPermission());
         newMapPermission.setUser(EntityDTOconvertor.convertUser(mapPermission.getUser()));

         return newMapPermission;
 
     }
 
     public static MapPermissionEntity convertMapPermission(MapPermission mapPermission) {
         if (mapPermission == null) {
             throw new IllegalArgumentException("Map Permission can not be null.");
         }
 
         MapPermissionEntity newMapPermission = new MapPermissionEntity();
         newMapPermission.setMap(EntityDTOconvertor.convertMyMap(mapPermission.getMap()));
         newMapPermission.setPermission(mapPermission.getPermission());
         newMapPermission.setUser(EntityDTOconvertor.convertUser(mapPermission.getUser()));

         return newMapPermission;
     }
 
     public static MyMap convertMyMap(MyMapEntity myMap) {
         if (myMap == null) {
             throw new IllegalArgumentException("My Map can not be null.");
         }
 
         MyMap newMyMap = new MyMap();
         newMyMap.setId(myMap.getId());
         newMyMap.setCreationDate(myMap.getCreationDate());
         newMyMap.setCreator(EntityDTOconvertor.convertUser(myMap.getCreator()));
         newMyMap.setName(myMap.getName());
         newMyMap.setView(EntityDTOconvertor.convertView(myMap.getView()));
 
         List<PointOfInterest> newList = new ArrayList<>();
         if (!myMap.getPointsOfInterest().isEmpty()) {
             for (PointOfInterestEntity p : myMap.getPointsOfInterest()) {
                 newList.add(EntityDTOconvertor.convertPointOfInterest(p));
             }
         }
         newMyMap.setPointsOfInterest(newList);
 
         return newMyMap;
 
     }
 
     public static MyMapEntity convertMyMap(MyMap myMap) {
         if (myMap == null) {
             throw new IllegalArgumentException("My Map can not be null.");
         }
 
         MyMapEntity newMyMap = new MyMapEntity();
         newMyMap.setId(myMap.getId());
         newMyMap.setCreationDate(myMap.getCreationDate());
         newMyMap.setCreator(EntityDTOconvertor.convertUser(myMap.getCreator()));
         newMyMap.setName(myMap.getName());
         newMyMap.setView(EntityDTOconvertor.convertView(myMap.getView()));
 
         List<PointOfInterestEntity> newList = new ArrayList<>();
         if (!myMap.getPointsOfInterest().isEmpty()) {
             for (PointOfInterest p : myMap.getPointsOfInterest()) {
                 newList.add(EntityDTOconvertor.convertPointOfInterest(p));
             }
         }
         newMyMap.setPointsOfInterest(newList);
 
         return newMyMap;
 
     }
 
     public static Point convertPoint(PointEntity point) {
         if (point == null) {
             throw new IllegalArgumentException("Point can not be null.");
         }
 
         Point newPoint = new Point();
         newPoint.setLatitude(point.getLatitude());
         newPoint.setLongitude(point.getLongitude());
 
         return newPoint;
 
     }
 
     public static PointEntity convertPoint(Point point) {
         if (point == null) {
             throw new IllegalArgumentException("Point can not be null.");
         }
 
         PointEntity newPoint = new PointEntity();
         newPoint.setLatitude(point.getLatitude());
         newPoint.setLongitude(point.getLongitude());
 
         return newPoint;
 
     }
 
     public static PointOfInterest convertPointOfInterest(PointOfInterestEntity pointOfInterest) {
         if (pointOfInterest == null) {
             throw new IllegalArgumentException("Point Of Interest can not be null.");
         }
 
         PointOfInterest newPointOfInterest = new PointOfInterest();
         newPointOfInterest.setId(pointOfInterest.getId());
         newPointOfInterest.setPoint(EntityDTOconvertor.convertPoint(pointOfInterest.getLocation()));
         newPointOfInterest.setDescription(pointOfInterest.getDescription());
         newPointOfInterest.setIconPath(pointOfInterest.getIconPath());
 
         return newPointOfInterest;
 
     }
 
     public static PointOfInterestEntity convertPointOfInterest(PointOfInterest pointOfInterest) {
         if (pointOfInterest == null) {
             throw new IllegalArgumentException("Point Of Interest can not be null.");
         }
         PointOfInterestEntity newPointOfInterest = new PointOfInterestEntity();
         newPointOfInterest.setId(pointOfInterest.getId());
         newPointOfInterest.setLocation(EntityDTOconvertor.convertPoint(pointOfInterest.getPoint()));
         newPointOfInterest.setDescription(pointOfInterest.getDescription());
         newPointOfInterest.setIconPath(pointOfInterest.getIconPath());
 
         return newPointOfInterest;
 
     }
 
     public static User convertUser(UserEntity user) {
         if (user == null) {
             throw new IllegalArgumentException("User can not be null.");
         }
 
         User newUser = new User();
         newUser.setId(user.getId());
         newUser.setName(user.getName());
         newUser.setNick(user.getLogin());
         newUser.setPassword(user.getPasswordHash());
         newUser.setRole(user.getRole());
 
         List<View> newList = new ArrayList<>();
         if (!user.getViews().isEmpty()) {
             for (ViewEntity v : user.getViews()) {
                 newList.add(EntityDTOconvertor.convertView(v));
             }
         }
         newUser.setViews(newList);
         return newUser;
 
     }
 
     public static UserEntity convertUser(User user) {
         if (user == null) {
             throw new IllegalArgumentException("User can not be null.");
         }
         UserEntity newUser = new UserEntity();
         newUser.setId(user.getId());
         newUser.setName(user.getName());
         newUser.setLogin(user.getNick());
         newUser.setPasswordHash(user.getPassword());
         newUser.setRole(user.getRole());
 
         List<ViewEntity> newList = new ArrayList<>();
         if (!user.getViews().isEmpty()) {
             for (View v : user.getViews()) {
                 newList.add(EntityDTOconvertor.convertView(v));
             }
         }
         newUser.setViews(newList);
         return newUser;
 
 
     }
 
     public static View convertView(ViewEntity view) {
         if (view == null) {
             throw new IllegalArgumentException("View can not be null.");
         }
 
         View newView = new View();
         newView.setId(view.getId());
         newView.setNorthEast(EntityDTOconvertor.convertPoint(view.getNorthEast()));
         newView.setSouthWest(EntityDTOconvertor.convertPoint(view.getSouthWest()));
         newView.setName(view.getName());
 
         return newView;
 
     }
 
     public static ViewEntity convertView(View view) {
         if (view == null) {
             throw new IllegalArgumentException("View can not be null.");
         }
 
         ViewEntity newView = new ViewEntity();
         newView.setId(view.getId());
         newView.setNorthEast(EntityDTOconvertor.convertPoint(view.getNorthEast()));
         newView.setSouthWest(EntityDTOconvertor.convertPoint(view.getSouthWest()));
         newView.setName(view.getName());
 
         return newView;
 
     }
 }
