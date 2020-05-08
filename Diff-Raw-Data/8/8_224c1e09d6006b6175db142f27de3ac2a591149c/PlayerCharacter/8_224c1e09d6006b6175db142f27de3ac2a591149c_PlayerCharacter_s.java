 package org.glowacki.core;
 
 import java.util.List;
 
 import org.glowacki.core.astar.MapPathFinder;
 
 /**
  * Character-related exception
  */
 class CharacterException
     extends CoreException
 {
     CharacterException(String msg)
     {
         super(msg);
     }
 }
 
 /**
  * Player character
  */
 public class PlayerCharacter
     extends BaseCharacter
 {
     private String name;
 
     private Level level;
 
     private List<MapPoint> path;
 
     /**
      * Create a player character.
      *
      * @param name player name
      * @param str strength
      * @param dex dexterity
      * @param spd speed
      */
     public PlayerCharacter(String name, int str,
                            int dex, int spd)
     {
         super(str, dex, spd);
 
         this.name = name;
     }
 
     /**
      * Build a path from the current position to the goal.
      *
      * @param goal target point
      *
      * @throws CoreException if there is a problem
      */
     public void buildPath(MapPoint goal)
         throws CoreException
     {
         MapPathFinder pathFinder = new MapPathFinder(getLevel().getMap());
         path = pathFinder.findBestPath(this, goal);
     }
 
     private Direction findDirection(MapPoint goal)
         throws PlayerException
     {
         final int x = goal.getX() - getX();
         final int y = goal.getY() - getY();
 
         if (x == -1) {
             if (y == -1) {
                 return Direction.LEFT_UP;
             } else if (y == 0) {
                 return Direction.LEFT;
             } else if (y == 1) {
                 return Direction.LEFT_DOWN;
             }
         } else if (x == 0) {
             if (y == -1) {
                 return Direction.UP;
             } else if (y == 1) {
                 return Direction.DOWN;
             } else if (y == 0) {
                 final String msg =
                     String.format("Goal [%d,%d] is current position of %s",
                                   goal.getX(), goal.getY(), name);
                 throw new PlayerException(msg);
             }
         } else if (x == 1) {
             if (y == -1) {
                 return Direction.RIGHT_UP;
             } else if (y == 0) {
                 return Direction.RIGHT;
             } else if (y == 1) {
                 return Direction.RIGHT_DOWN;
             }
         }
 
         final String msg =
             String.format("Goal [%d,%d] for %s is more than one square" +
                           " from [%d,%d]", goal.getX(), goal.getY(), name,
                           getX(), getY());
         throw new PlayerException(msg);
     }
 
     /**
      * Get character's current level
      *
      * @return level
      */
     public Level getLevel()
     {
         return level;
     }
 
     /**
      * Return character's name.
      *
      * @return name
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Does this character have an existing path?
      *
      * @return <tt>true</tt> if this character has an ongoing path
      */
     public boolean hasPath()
     {
         return path != null && path.size() > 0;
     }
 
     /**
      * Is this character a player?
      *
      * @return <tt>true</tt> if this character is a player
      */
     public boolean isPlayer()
     {
         return true;
     }
 
     /**
      * Move the computer character.
      *
      * @param dir direction
      *
      * @return number of turns
      *
      * @throws CoreException if there is a problem
      */
     public int move(Direction dir)
         throws CoreException
     {
         if (level == null) {
             throw new CharacterException("Level cannot be null");
         }
 
         if (dir == Direction.CLIMB) {
             Terrain t = level.getTerrain(getX(), getY());
             if (t != Terrain.UPSTAIRS) {
                 throw new CharacterException("You cannot climb here");
             }
 
             Level prevLevel = level.getPreviousLevel();
             if (prevLevel == null) {
                 throw new CharacterException("You cannot exit here");
             }
 
             level.exit(this);
             prevLevel.enterUp(this);
 
             level = prevLevel;
 
             return moveInternal(t, false);
         } else if (dir == Direction.DESCEND) {
             Terrain t = level.getTerrain(getX(), getY());
             if (t != Terrain.DOWNSTAIRS) {
                 throw new CharacterException("You cannot descend here");
             }
 
             Level nextLevel = level.getNextLevel();
             if (nextLevel == null) {
                 throw new CharacterException("You are at the bottom");
             }
 
             level.exit(this);
             nextLevel.enterDown(this);
 
             level = nextLevel;
 
             return moveInternal(t, false);
         } else {
             return move(level.getMap(), dir);
         }
     }
 
     /**
      * Move to the next point in the path.
      *
      * @return number of turns
      *
      * @throws CoreException always
      */
     public int movePath()
         throws CoreException
     {
         if (path == null || path.size() == 0) {
             throw new CoreException("No current path");
         }
 
         int rtnval;
         try {
             rtnval = move(level.getMap(), findDirection(path.remove(0)));
         } catch (CoreException ce) {
             path = null;
             throw ce;
         }
 
         return rtnval;
     }
 
     /**
      * Set character's current level
      *
      * @param l level
      */
     public void setLevel(Level l)
     {
         this.level = l;
     }
 
     /**
      * Perform this turn's action(s).
      */
     public void takeTurn()
     {
         throw new UnimplementedError();
     }
 
     /**
      * Return a debugging string.
      *
      * @return debugging string
      */
     public String toString()
     {
         return name + super.toString();
     }
 }
