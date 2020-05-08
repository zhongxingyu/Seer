 package cz.fjfi.guideme.core;
 
 /**
  * This enumeration contains all allowed directions in GuideMe app and methods
  * to work with them.
  * Feel free to modify :-D
  * @author
  * @version
  */
 public enum Direction
 {
     North,
     Northeast,
     East,
     Southeast,
     South,
     Southwest,
     West,
     Northwest;
     
     public enum Relative
     {
         Straight,
         SlightRight,
         Right,
         SharpRight,
         Back,
         SharpLeft,
         Left,
         SlightLeft;
         
         /**
          * Returns direction made by turning by 'turn'
          * @param change in direction
          * @return direction after turning
          */
         public Relative turn(Relative turn)
         {
             Relative[] directions = Relative.values();
             int ordinal = this.ordinal();
            ordinal = (ordinal - turn.ordinal()) % directions.length;
             return directions[ordinal];
         }
     }
 
     /**
      * Returns direction made by turning by 'turn'
      * @param change in direction
      * @return direction after turning
      */
     public Direction turn(Relative turn)
     {
         Direction[] directions = Direction.values();
         int ordinal = this.ordinal();
        ordinal = (ordinal - turn.ordinal()) % directions.length;
         return directions[ordinal];
     }
     
     /**
      * Returns the turn needed to change direction from this to 'other'
      * @param desired target direction
      * @return relative direction
      */
     public Relative subtract(Direction other)
     {
         Relative[] directions = Relative.values();
         int ordinal = this.ordinal();
        ordinal = (other.ordinal() - ordinal) % directions.length;
         return directions[ordinal];
     }
 
 }
