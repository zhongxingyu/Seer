 package ogo.spec.game.model;
 
 import java.util.Set;
 import java.util.HashSet;
 
 public class LandCreature extends Creature
 {
 
     public LandCreature(Tile currentTile, GameMap map)
     {
         super(currentTile, map);
     }
 
     @Override
     protected int getMoveSpeed(TileType tileType) {
         assert (tileType != TileType.DEEP_WATER);
         if(tileType == TileType.LAND)
         {
             return Creature.TICKS_PER_TILE_FAST;
         }
         if(tileType == TileType.SHALLOW_WATER)
         {
             return Creature.TICKS_PER_TILE_SLOW;
         }
         return -1;
     }
 
     @Override
     protected int getEatValue(Creature creature) {
         if(creature instanceof AirCreature)
         {
             return 4;
         }
         if(creature instanceof SeaCreature)
         {
             return 6;
         }
         return 0;
     }
 
     @Override
     protected Set<TileType> getAllowedTypes()
     {
         HashSet<TileType> types = new HashSet<TileType>();
         types.add(TileType.SHALLOW_WATER);
         types.add(TileType.LAND);
         return types;
     }
 
 }
