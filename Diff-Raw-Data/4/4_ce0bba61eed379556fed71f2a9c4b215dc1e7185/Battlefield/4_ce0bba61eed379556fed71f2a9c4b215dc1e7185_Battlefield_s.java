 package com.ghostofpq.seltyrtactical.entities.battlefield;
 
 import com.ghostofpq.seltyrtactical.commons.Position;
 import lombok.Getter;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Getter
 public class Battlefield implements Serializable {
 
     private static final long serialVersionUID = -6878010880627782277L;
     private int length;  //x
     private int height;  //y
     private int depth;  //z
    public Map<Position, BattlefieldElement> battlefieldElementMap;
     private int numberOfPlayers;
     private Map<Integer, List<Position>> deploymentZones;
 
     public Battlefield(int length, int height, int depth, int numberOfPlayers) {
         this.length = length;
         this.height = height;
         this.depth = depth;
         this.numberOfPlayers = numberOfPlayers;
         battlefieldElementMap = new HashMap<Position, BattlefieldElement>();
         deploymentZones = new HashMap<Integer, List<Position>>();
     }
 
     public void addDeployementZone(Integer playerNumber, Position position) {
         if (null != playerNumber) {
             List<Position> deploymentZone = deploymentZones.get(playerNumber);
             if (null == deploymentZone) {
                 deploymentZone = new ArrayList<Position>();
             }
             deploymentZone.add(position);
             deploymentZones.put(playerNumber, deploymentZone);
         }
     }
 
     public void addBattlefieldElement(int x, int y, int z, BattlefieldElement.BattlefieldElementType type) {
         Position position = new Position(x, y, z);
         BattlefieldElement battlefieldElement = null;
         switch (type) {
             case BLOC:
                 battlefieldElement = new Block();
                 break;
         }
         if (battlefieldElement != null) {
             battlefieldElementMap.put(position, battlefieldElement);
         }
     }

 }
