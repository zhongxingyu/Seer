 package sps.entities;
 
 import sps.bridge.*;
import sps.core.Logger;
 import sps.core.Point2;
 import sps.core.RNG;
 import sps.core.Settings;
 import sps.graphics.Renderer;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 public class EntityManager {
 
     public static EntityManager __instance;
 
     public static EntityManager get() {
         if (__instance == null) {
             __instance = new EntityManager();
         }
         return __instance;
     }
 
     public static void reset() {
         __instance = new EntityManager();
     }
 
     private EntityManager() {
     }
 
     private List<Entity> _contents = new ArrayList<Entity>();
     private HashMap<Point2, List<Entity>> _gridContents = new HashMap<Point2, List<Entity>>();
     private List<Entity> players = new ArrayList<Entity>();
     private HashMap<EntityType, List<Entity>> entityBuckets = new HashMap<EntityType, List<Entity>>();
     private HashMap<ActorType, List<IActor>> actorBuckets = new HashMap<ActorType, List<IActor>>();
 
     public Entity addEntity(Entity entity) {
         entity.loadContent();
         _contents.add(entity);
         Collections.sort(_contents);
         addToGrid(entity);
         addToBuckets(entity);
         return entity;
     }
 
     public void addEntities(List<? extends Entity> cache) {
         for (Entity e : cache) {
             addEntity(e);
         }
     }
 
     private void addToBuckets(Entity entity) {
         if (entity.getEntityType() == EntityTypes.get(Sps.Entities.Actor)) {
             IActor actor = (IActor) entity;
             if (actor.getActorType() == ActorTypes.get(Sps.Actors.Player)) {
                 players.add(entity);
                Logger.info("PLayers size in EM: "+players.size());
             }
             if (!actorBuckets.containsKey(actor)) {
                 actorBuckets.put(actor.getActorType(), new ArrayList<IActor>());
             }
             actorBuckets.get(actor.getActorType()).add(actor);
         }
         if (!entityBuckets.containsKey(entity)) {
             entityBuckets.put(entity.getEntityType(), new ArrayList<Entity>());
         }
         entityBuckets.get(entity.getEntityType()).add(entity);
     }
 
     private void addToGrid(Entity entity) {
         if (!_gridContents.containsKey(entity.getLocation())) {
             _gridContents.put(entity.getLocation(), new ArrayList<Entity>());
         }
         _gridContents.get(entity.getLocation()).add(entity);
     }
 
     public Entity getEntity(EntityType type) {
         if (_contents != null) {
             return entityBuckets.get(type).get(0);
         }
         return null;
     }
 
     private final List<Entity> _gopResults = new ArrayList<Entity>();
     private final List<Entity> _goResults = new ArrayList<Entity>();
 
     public List<Entity> getAllEntities() {
         return _contents;
     }
 
     public List<Entity> getEntities(EntityType type, Point2 target) {
         if (_contents != null) {
             _gopResults.clear();
             _goResults.clear();
             _goResults.addAll(_gridContents.get(target));
             for (Entity goResult : _goResults) {
                 if (goResult.getEntityType() == type) {
                     _gopResults.add(goResult);
                 }
             }
             return _gopResults;
         }
         return null;
     }
 
     private List<Entity> empty = new ArrayList<Entity>();
 
     public List<Entity> getEntities(EntityType type) {
         if (entityBuckets.get(type) == null) {
             return empty;
         }
         return entityBuckets.get(type);
     }
 
     private final List<IActor> _creatures = new ArrayList<IActor>();
 
     public List<IActor> getActors(ActorType type) {
         _creatures.clear();
         if (type != ActorTypes.get(Sps.ActorGroups.Non_Player)) {
             if (actorBuckets.get(type) != null) {
                 _creatures.addAll(actorBuckets.get(type));
             }
         }
         else {
             for (Entity elem : _contents) {
                 if (elem.getEntityType() == EntityTypes.get(Sps.Entities.Actor)) {
                     if (((IActor) elem).getActorType() != ActorTypes.get(Sps.Actors.Player)) {
                         _creatures.add(((IActor) elem));
                     }
                 }
             }
         }
         return _creatures;
     }
 
     private IActor _nextResult;
 
     public List<IActor> getActorsAt(Point2 target, ActorType actorType) {
         _creatures.clear();
         for (Entity elem : _gridContents.get(target)) {
             if (elem.getEntityType() == EntityTypes.get(Sps.Entities.Actor)) {
                 _nextResult = (IActor) elem;
                 if (actorType == null || _nextResult.getActorType() == actorType || (actorType == ActorTypes.get(Sps.ActorGroups.Non_Player) && _nextResult.getActorType() != ActorTypes.get(Sps.Actors.Player))) {
                     _creatures.add(_nextResult);
                 }
             }
         }
         return _creatures;
     }
 
     public List<IActor> getActorsAt(Point2 target) {
         return getActorsAt(target, null);
     }
 
     private final List<IActor> _creatures2 = new ArrayList<IActor>();
     private Point2 buffer = new Point2(0, 0);
 
     public List<IActor> getActorsSurrounding(Point2 target, int distance) {
         _creatures2.clear();
         for (int ii = -distance; ii < distance + 1; ii++) {
             for (int jj = -distance; jj < distance + 1; jj++) {
                 if (ii != 0 || jj != 0) {
                     buffer.copy(target.add(new Point2(ii, jj)));
                     if (CoordVerifier.isValid(buffer)) {
                         for (IActor creature : getActorsAt(buffer, null)) {
                             _creatures2.add(creature);
                         }
                     }
                 }
             }
         }
         return _creatures2;
     }
 
     public boolean isLocationBlocked(Point2 location) {
         for (Entity elem : _gridContents.get(location)) {
             if (elem.isBlocking()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean anyAt(Point2 target, EntityType type) {
         if (CoordVerifier.isValid(target)) {
             for (Entity entity : _gridContents.get(target)) {
                 if (entity.getEntityType() == type) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public void removeEntity(Entity target) {
         _contents.remove(target);
         _gridContents.get(target.getLocation()).remove(target);
         if (target.getEntityType() == EntityTypes.get(Sps.Entities.Actor)) {
             IActor actor = (IActor) target;
             if (actor.getActorType() == ActorTypes.get(Sps.Actors.Player)) {
                 players.remove(target);
             }
             actorBuckets.remove(actor);
         }
         entityBuckets.get(target.getEntityType()).remove(target);
     }
 
     public void clear() {
         _contents.clear();
         _gridContents.clear();
         actorBuckets.clear();
         entityBuckets.clear();
         players.clear();
     }
 
     public void update() {
         for (int ii = 0; ii < _contents.size(); ii++) {
             if (ii >= _contents.size()) {
                 return;
             }
             if (!_contents.get(ii).isActive()) {
                 removeEntity(_contents.get(ii));
                 ii--;
                 continue;
             }
             _contents.get(ii).update();
         }
     }
 
     public void draw() {
         if (Renderer.get() != null) {
             for (Entity component : _contents) {
                 component.draw();
             }
         }
     }
 
     public void loadContent() {
         if (Renderer.get() != null) {
             for (Entity component : _contents) {
                 component.loadContent();
             }
         }
     }
 
     public void updateGridLocation(Entity Entity, Point2 oldLocation) {
         if (_gridContents != null && oldLocation != null) {
             _gridContents.get(oldLocation).remove(Entity);
             addToGrid(Entity);
         }
     }
 
     public List<Entity> getPlayers() {
         return players;
     }
 
     public IActor getNearestPlayer(Entity target) {
         List<Entity> actors = getPlayers();
         if (actors.size() > 0) {
             Entity closest = actors.get(0);
             for (Entity actor : actors) {
                 if (HitTest.getDistanceSquare(target, actor) < HitTest.getDistanceSquare(target, closest)) {
                     closest = actor;
                 }
             }
             return (IActor) closest;
         }
         return null;
     }
 
     public Point2 getEmptyLocation() {
         List<Point2> emptyLocations = new ArrayList<Point2>();
         for (Point2 location : _gridContents.keySet()) {
             if (location.GridX > 0 && location.GridY > 0 && location.GridX < Settings.get().tileMapWidth - 1 && location.GridY < Settings.get().tileMapHeight - 1) {
                 boolean exclude = false;
                 for (int ii = 0; ii < _gridContents.get(location).size(); ii++) {
                     if (_gridContents.get(location).get(ii).getEntityType() == EntityTypes.get(Sps.Entities.Actor)) {
                         exclude = true;
                     }
                 }
                 if (!exclude) {
                     emptyLocations.add(location);
                 }
             }
         }
         return emptyLocations.get(RNG.next(0, emptyLocations.size()));
     }
 
     public IActor getTouchingCreature(Entity entity) {
         for (Entity _content : _contents) {
             if (_content.getEntityType() == EntityTypes.get(Sps.Entities.Actor)) {
                 if (_content.contains(entity.getLocation())) {
                     return (IActor) _content;
                 }
             }
         }
         return null;
     }
 }
