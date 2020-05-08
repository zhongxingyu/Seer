 package lv.k2611a.domain.unitgoals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import lv.k2611a.domain.Building;
 import lv.k2611a.domain.Bullet;
 import lv.k2611a.domain.BulletType;
 import lv.k2611a.domain.Entity;
 import lv.k2611a.domain.Map;
 import lv.k2611a.domain.Unit;
 import lv.k2611a.domain.ViewDirection;
 import lv.k2611a.service.game.GameServiceImpl;
 import lv.k2611a.util.Point;
 
 public class Attack implements UnitGoal {
 
     private Entity entity;
     private int entityId;
 
     public Attack(Entity entity, int entityId) {
         this.entity = entity;
         this.entityId = entityId;
     }
 
     public Entity getEntity() {
         return entity;
     }
 
     public void setEntity(Entity entity) {
         this.entity = entity;
     }
 
     public int getEntityId() {
         return entityId;
     }
 
     public void setEntityId(int entityId) {
         this.entityId = entityId;
     }
 
     @Override
     public void process(Unit unit, Map map, GameServiceImpl gameService) {
         if (entity == Entity.BUILDING) {
             if (map.getBuilding(entityId) == null) {
                 return;
             }
             attackBuilding(unit,map,gameService);
         }
     }
 
     private void attackBuilding(Unit unit, Map map, GameServiceImpl gameService) {
         Building building = map.getBuilding(entityId);
         if (inRange(building,unit,map)) {
             fire(building,unit,map,gameService);
         } else {
             move(building,unit,map);
         }
     }
 
     private void move(Building building, Unit unit, Map map) {
         Point bestPoint = getClosestPoint(building, unit);
         unit.insertGoalBeforeCurrent(new Move(bestPoint.getX(), bestPoint.getY(),unit.getUnitType().getAttackRange()-1));
     }
 
     private Point getClosestPoint(Building building, Unit unit) {
         List<Point> points = new ArrayList<Point>();
         points.add(building.getPoint());
         points.add(building.getPoint2());
         points.add(building.getPoint3());
         points.add(building.getPoint4());
 
         return Map.getClosestNode(unit.getPoint(), points);
     }
 
     private void fire(Building building, Unit unit, Map map, GameServiceImpl gameService) {
         Point closestPoint = getClosestPoint(building, unit);
         unit.setViewDirection(ViewDirection.getDirection(unit.getPoint(), closestPoint));
         if (unit.getTicksReloading() == 0) {
             Bullet bullet = new Bullet();
             bullet.setDamageToDeal(unit.getUnitType().getAttackDamage());
             bullet.setStartX(unit.getX());
             bullet.setStartY(unit.getY());
             bullet.setGoalX(closestPoint.getX());
             bullet.setGoalY(closestPoint.getY());
            bullet.setTicksToMove(unit.getUnitType().getBulletSpeed());
            bullet.setTicksToMoveTotal(unit.getUnitType().getBulletSpeed());
             bullet.setBulletType(BulletType.TANK_SHOT);
             map.addBullet(bullet);
             unit.setTicksReloading(unit.getUnitType().getTicksToAttack());
         }
     }
 
     private boolean inRange(Building building, Unit unit, Map map) {
         Point bestPoint = getClosestPoint(building, unit);
         return Map.getDistanceBetween(bestPoint, unit.getPoint()) <= unit.getUnitType().getAttackRange();
     }
 }
