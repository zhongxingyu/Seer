 package Project.Game.Entities;
 
 import Project.Game.Behaviours.Collision.SimpleCollision;
 import Project.Game.Behaviours.Constructive.BlueprintConstruction;
 import Project.Game.Behaviours.Constructive.SimpleConstruction;
 import Project.Game.Behaviours.Drawing.SimpleQuad;
 import Project.Game.Behaviours.Influence.SimpleInfluence;
 import Project.Game.Behaviours.Movement.Flocking;
 import Project.Game.Behaviours.Movement.Static;
 import Project.Game.Behaviours.Movement.Wandering;
 import Project.Game.Behaviours.Offensive.SimpleWeapon;
 import Project.Game.Buildings.MetaBuilding;
 import Project.Game.Entities.Meta.Group;
 import Project.Game.*;
 
 /**
  *
  */
 public class EntityFactory {
 
 
     @SuppressWarnings("UnusedDeclaration")
     public static Entity getNaturalEntity() {
         Entity entity = new Entity();
         entity.r = 1;
         entity.g = 1;
         entity.b = 1;
 
         entity.faction = Factions.Nature.getFaction();
 
         entity.movementBehaviour = new Wandering(entity, Utilities.randomLocation(50));
         entity.drawingBehaviour = new SimpleQuad(entity, Main.SQUARE_WIDTH, entity.r, entity.g, entity.b);
         entity.influenceBehaviour = new SimpleInfluence(entity, 3, 1);
         entity.collisionBehaviour = new SimpleCollision(entity, Main.SQUARE_WIDTH);
 
         return entity;
     }
 
     public static Entity getGroupedEntity(Faction faction, Group group, Vector2D location, double strength) {
         Entity entity = new Entity();
 
         setColour(entity, faction);
         entity.faction = faction;
         entity.health = 30;
         entity.movementBehaviour = new Flocking(entity, location, group);
         entity.drawingBehaviour = new SimpleQuad(entity, Main.SQUARE_WIDTH, entity.r, entity.g, entity.b);
         entity.influenceBehaviour = new SimpleInfluence(entity, 7, strength);
         entity.collisionBehaviour = new SimpleCollision(entity, Main.SQUARE_WIDTH);
 
         return entity;
     }
 
     public static Entity getBase(Faction faction, Vector2D location) {
         Entity entity = new Entity();
 
         setColour(entity, faction);
         entity.faction = faction;
         entity.movementBehaviour = new Static(entity, location);
         entity.drawingBehaviour = new SimpleQuad(entity, 40, entity.r, entity.g, entity.b);
         entity.influenceBehaviour = new SimpleInfluence(entity, 9, 1);
         entity.collisionBehaviour = new SimpleCollision(entity, 40);
         //entity.constructionBehaviour = new BaseConstruction(entity, location, 100);
         entity.constructionBehaviour = new BlueprintConstruction(faction, entity, faction.getResourcePool(), location, Main.BLUEPRINT_REGISTRY.get("Home Level 1"));
 
         return entity;
     }
 
     public static Entity getShipyard(Faction faction, Vector2D location) {
         Entity entity = new Entity();
 
         setColour(entity, faction);
         entity.faction = faction;
 
         entity.movementBehaviour = new Static(entity, location);
         entity.drawingBehaviour = new SimpleQuad(entity, 26, entity.r, entity.g, entity.b);
         entity.influenceBehaviour = new SimpleInfluence(entity, 5, 1);
         entity.collisionBehaviour = new SimpleCollision(entity, 26);
         entity.constructionBehaviour = new SimpleConstruction(entity, faction);
 
         return entity;
     }
 
     private static Entity getTower(Faction faction, Vector2D location) {
         Entity entity = new Entity();
 
         setColour(entity, faction);
         entity.faction = faction;
 
         entity.movementBehaviour = new Static(entity, location);
         entity.drawingBehaviour = new SimpleQuad(entity, 20, entity.r, entity.g, entity.b);
         entity.influenceBehaviour = new SimpleInfluence(entity, 11, 10);
         entity.collisionBehaviour = new SimpleCollision(entity, 20);
         entity.offensiveBehaviour = new SimpleWeapon(entity, 50, 10, 60);
 
         return entity;
     }
 
     public static Entity getBuilding(Faction faction, Vector2D location, MetaBuilding type) {
 
         Entity entity = new Entity();
         setColour(entity, faction);
         entity.faction = faction;
         System.out.println(location);
        entity.health = type.getHealth();
         entity.movementBehaviour = new Static(entity, location);
         entity.drawingBehaviour = new SimpleQuad(entity, (int) type.getSize().x, entity.r, entity.g, entity.b);
         entity.influenceBehaviour = new SimpleInfluence(entity, type.getInfluence().getSize(), type.getInfluence().getStrength());
         entity.collisionBehaviour = new SimpleCollision(entity, (int) type.getSize().x);
 
         switch (type.getName()) {
             case "Tower":
                 entity.offensiveBehaviour = new SimpleWeapon(entity, 50, 10, 60);
         }
 
         return entity;
     }
 
     private static void setColour(Entity entity, Faction faction) {
         entity.r = faction.getR();
         entity.g = faction.getG();
         entity.b = faction.getB();
     }
 }
