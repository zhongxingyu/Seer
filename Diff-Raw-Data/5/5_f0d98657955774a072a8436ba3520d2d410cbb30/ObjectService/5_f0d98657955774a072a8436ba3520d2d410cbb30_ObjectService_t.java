 package fr.kissy.hellion.server.service;
 
 import fr.kissy.hellion.proto.Common;
 import fr.kissy.hellion.server.domain.Player;
 
 /**
  * @author Guillaume LE BILLER
  */
 public class ObjectService {
 
     private static final String MOVABLE = "Movable";
     private static final String PLAYER = "Player";
     private static final String MESH = "Mesh";
     private static final String UPDATABLE = "Updatable";
     private static final String NETWORK = "Network";
 
     /**
      * Get the CREATE player Object.
      *
      * @param player The player to convert.
      * @param isControllable Is the player controllable ?
      * @return The player Object created.
      */
     public Common.Object getCreateObject(Player player, boolean isControllable) {
         Common.Object.Builder builder = Common.Object.newBuilder();
         builder.setId(player.getId().toString());
         builder.setName(player.getName());
 
         Common.SystemObject.Builder graphicSystemObject = builder.addSystemObjectsBuilder();
         graphicSystemObject.setSystemType(Common.SystemType.Graphic);
         graphicSystemObject.setType(MESH);
         graphicSystemObject.addProperties(player.getMeshProperty().build());
 
         if (isControllable) {
             Common.SystemObject.Builder inputSystemObject = builder.addSystemObjectsBuilder();
             inputSystemObject.setSystemType(Common.SystemType.Input);
             inputSystemObject.setType(PLAYER);
         }
 
         Common.SystemObject.Builder networkSystemObject = builder.addSystemObjectsBuilder();
         networkSystemObject.setSystemType(Common.SystemType.Network);
         networkSystemObject.setType(isControllable ? PLAYER : UPDATABLE);
 
         Common.SystemObject.Builder physicSystemObject = builder.addSystemObjectsBuilder();
         physicSystemObject.setSystemType(Common.SystemType.Physic);
         physicSystemObject.setType(MOVABLE);
         physicSystemObject.addProperties(player.getPositionProperty().build());
         physicSystemObject.addProperties(player.getOrientationProperty().build());
         physicSystemObject.addProperties(player.getVelocityProperty().build());
 
         return builder.build();
     }
 
     /**
      * Get the UPDATE player Object.
      *
      * @param player The player to convert.
      * @return The player Object created.
      */
     public Common.Object getUpdateObject(Player player) {
         Common.Object.Builder builder = Common.Object.newBuilder();
         builder.setId(player.getId().toString());
        builder.setName(player.getName());
         // Only network system update objects
         Common.SystemObject.Builder networkSystemObject = builder.addSystemObjectsBuilder();
         networkSystemObject.setSystemType(Common.SystemType.Network);
         networkSystemObject.setType(NETWORK);
         networkSystemObject.addProperties(player.getPositionProperty().build());
         networkSystemObject.addProperties(player.getOrientationProperty().build());
         networkSystemObject.addProperties(player.getVelocityProperty().build());
         return builder.build();
     }
 
     /**
      * Get the DELETE player Object.
      *
      * @param player The player to convert.
      * @return The player Object created.
      */
     public Common.Object getDeleteObject(Player player) {
         Common.Object.Builder builder = Common.Object.newBuilder();
         builder.setId(player.getId().toString());
        builder.setName(player.getName());
         return builder.build();
     }
 }
