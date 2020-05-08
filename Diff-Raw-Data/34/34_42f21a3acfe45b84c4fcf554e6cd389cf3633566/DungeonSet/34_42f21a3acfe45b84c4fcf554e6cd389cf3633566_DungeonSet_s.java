 package aigilas.dungeons;
 
import sps.core.Logger;
 import sps.particles.ParticleEngine;
 
 import java.util.HashMap;
 
 public class DungeonSet {
     private int _currentFloor = 0;
     private final HashMap<Integer, DungeonFloor> _floors = new HashMap<Integer, DungeonFloor>();
 
     /*
       * This whole "area" thing is very messy and doesn't work:an intuitive way.
       * In order to pre-load, we need to make sure we can easily get back to
       * start.
       */
     public DungeonSet() {
         _floors.put(_currentFloor, new DungeonFloor());
     }
 
     public void gotoNext() {
         _floors.get(_currentFloor).preserveFloor();
         _currentFloor++;
         loadOrCreateDungeon(false);
     }
 
     public boolean gotoPrevious() {
         if (_currentFloor > 0) {
             _floors.get(_currentFloor).preserveFloor();
             _currentFloor--;
             loadOrCreateDungeon(true);
             return true;
         }
         return false;
     }
 
    private int count = 0;

     private void loadOrCreateDungeon(boolean goingUp) {
        Logger.info("Creating:" + count++ + ", goingUp: " + goingUp);
         if (!_floors.containsKey(_currentFloor)) {
             _floors.put(_currentFloor, new DungeonFloor(_floors.get(_currentFloor - 1).getDownstairsLocation()));
             Dungeon.increaseFloorCount();
         }
         ParticleEngine.reset();
         _floors.get(_currentFloor).loadTiles(goingUp);
     }
 
     public int getFloorCount() {
         return _currentFloor;
     }
 }
