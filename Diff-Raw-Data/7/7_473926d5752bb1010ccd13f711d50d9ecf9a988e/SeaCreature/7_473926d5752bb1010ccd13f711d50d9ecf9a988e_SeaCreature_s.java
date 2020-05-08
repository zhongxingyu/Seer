 package ogo.spec.game.model;
 
 import java.util.Set;
 import java.util.HashSet;
 
 public class SeaCreature extends Creature
 {
 
     public SeaCreature(Tile currentTile, GameMap map)
     {
         super(currentTile, map);
     }
 
     @Override
     protected int getMoveSpeed(TileType tileType) {
         assert (tileType != TileType.LAND);
         return Creature.TICKS_PER_TILE_AVG;
     }
 
     @Override
     protected int getEatValue(Creature creature) {
        assert !(creature instanceof SeaCreature);
         if(creature instanceof AirCreature)
         {
             return 4;
         }
         if(creature instanceof LandCreature)
         {
             return 6;
         }
         return 0;
     }
 
     @Override
     protected Set<TileType> getAllowedTypes()
     {
         HashSet<TileType> types = new HashSet<TileType>();
         types.add(TileType.DEEP_WATER);
         types.add(TileType.SHALLOW_WATER);
         return types;
     }
 }
