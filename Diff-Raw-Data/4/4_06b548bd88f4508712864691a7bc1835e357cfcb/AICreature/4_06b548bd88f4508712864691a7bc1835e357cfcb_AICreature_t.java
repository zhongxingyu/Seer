 package edu.brown.cs32.goingrogue.gameobjects.creatures;
 
 import edu.brown.cs32.goingrogue.gameobjects.actions.Action;
 import edu.brown.cs32.goingrogue.gameobjects.actions.MoveAction;
 import edu.brown.cs32.goingrogue.util.CreatureSize;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import static edu.brown.cs32.bweedon.geometry.Point2DUtil.getAngleFromTo;
 import edu.brown.cs32.goingrogue.gameobjects.actions.ArcAttackAction;
 import edu.brown.cs32.jcadler.GameLogic.RogueMap.Corridor;
 import edu.brown.cs32.jcadler.GameLogic.RogueMap.Room;
 import java.awt.geom.Rectangle2D;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class AICreature extends Creature {
 
     private List<Creature> _creatures;
     private List<Room> _rooms;
     private final double DIST_TO_ATTACK = 0.5;
 
     public AICreature(Point2D.Double pos, double direction, String name, List<Attribute> attributes,
             CreatureStats stats, String spritePath, CreatureSize size, List<Creature> creatures,
             List<Room> rooms) {
         super(pos, direction, name, attributes, stats, spritePath, size);
         _creatures = creatures;
         _rooms = rooms;
         //DIST_TO_ATTACK = getWeaponRange();
 
         _shouldRotate = false;
         _shouldFlip = true;
     }
 
     @Override
     public boolean isItem() {
         return false;
     }
 
     @Override
     public List<Action> getActionsWithUpdate(int delta) {
         Creature closestCreature = getClosestCreature();
 
         List<Action> returnActions = new ArrayList<>();
         Point2D targetPoint;
         //if (getCreatureRoom(closestCreature) != null) {
         if (closestCreature != null) {
             targetPoint = closestCreature.getCenterPosition();
         } else {
             return new ArrayList<>();
         }
 //        } else {
 //            return new ArrayList<>();
 //        }
 //        else {
 //            Room creatureRoom = getCreatureRoom(this);
 //            Corridor playerCorridor = getCreatureCorridor(closestCreature);
 //            Point2D corridorEntrance = getEntrance(playerCorridor, creatureRoom);
 //            targetPoint = corridorEntrance;
 //        }
 
         setDirection(getAngleFromTo(getCenterPosition(), targetPoint));
         if (getCenterPosition().distance(targetPoint) < DIST_TO_ATTACK) {
             returnActions.add(
                     new ArcAttackAction(getDirection(), getWeaponRange(), getWeaponArcLength(),
                     getWeaponAttackTimer(), this));
         } else {
             returnActions.add(new MoveAction(getDirection(), this, delta));
         }
 
         setActions(returnActions);
         return returnActions;
     }
 
     private Creature getClosestCreature() {
         Creature closestCreature = null;
         for (int i = 0; i < _creatures.size(); ++i) {
             Creature currCreature = _creatures.get(i);
             Point2D currCreaturePos = currCreature.getCenterPosition();
             if ((closestCreature == null) && (!currCreature.equals(this))
                     && (currCreature.getAttributes().contains(Attribute.PLAYER))) {
                 closestCreature = currCreature;
             } else if ((closestCreature != null)
                     && (getCenterPosition().distance(currCreaturePos)
                     < getCenterPosition().distance(closestCreature.getCenterPosition()))
                     && (!currCreature.equals(this))
                     && (currCreature.getAttributes().contains(Attribute.PLAYER))
                     && inSameRoom(closestCreature, this)) {
                 closestCreature = currCreature;
             }
         }
         if (inSameRoom(closestCreature, this)) {
             return closestCreature;
         } else {
             return null;
         }
     }
 
     private Room getCreatureRoom(Creature creature) {
         for (Room room : _rooms) {
            Rectangle2D roomRec = new Rectangle2D.Double(room.getX() - 0.5, room.getY() - 0.5,
                    room.getWidth() + 0.5, room.getHeight() + 0.5);
             if (roomRec.contains(creature.getCenterPosition())) {
                 return room;
             }
         }
         return null;
     }
 
     private Corridor getCreatureCorridor(Creature creature) {
         for (Room room : _rooms) {
             for (Corridor corridor : room.getCorridors()) {
                 if (corridor.getRectangle().contains(creature.getCenterPosition())) {
                     return corridor;
                 }
             }
         }
         return null;
     }
 
     private Point2D getEntrance(Corridor corridor, Room room) {
 
         int posInRoom;
 
         Room r = corridor.getStart();
         String id = r.getID();
         String rid = room.getID();
         if (id.equals(rid)) {
             posInRoom = corridor.getPos1();
         } else if (corridor.getEnd().getID().equals(room.getID())) {
             posInRoom = corridor.getPos2();
         } else {
             posInRoom = -1000; // extreme value will make it easy to tell in debugging
             // without having to throw an error
         }
 
         double xVal;
         double yVal;
         final double OFFSET_INTO_ROOM = 1.0;
         switch (corridor.getDirection()) {
             case 0:
                 xVal = room.getX() + posInRoom + (corridor.getWidth() / 2.0);
                 yVal = room.getY() + OFFSET_INTO_ROOM;
                 break;
             case 1:
                 xVal = room.getX() + room.getWidth() - OFFSET_INTO_ROOM;
                 yVal = room.getY() + posInRoom + (corridor.getWidth() / 2.0);
                 break;
             case 2:
                 xVal = room.getX() + posInRoom + (corridor.getWidth() / 2.0);
                 yVal = room.getY() + room.getHeight() - OFFSET_INTO_ROOM;
                 break;
             case 3:
                 xVal = room.getX() + OFFSET_INTO_ROOM;
                 yVal = room.getY() + posInRoom + (corridor.getWidth() / 2.0);
                 break;
             default:
                 xVal = -1000;
                 yVal = -1000; // for debugging, as seen above
                 break;
         }
         return new Point2D.Double(xVal, yVal);
     }
 
     private boolean inSameRoom(Creature c1, Creature c2) {
         Room c1Room = getCreatureRoom(c1);
         Room c2Room = getCreatureRoom(c2);
         if ((c1Room == null) || (c2Room == null)) {
             return false;
         } else {
             return c1Room.getID().equals(c2Room.getID());
         }
     }
 }
