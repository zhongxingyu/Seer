 import java.util.Set;
 import java.util.EnumSet;
 /**
  * Stores information on which directions are blocked.
  * 
  * @author Josh Gillham
  * @version 10-29-12
  */
 public enum Wall {
     // #0 An empty square.
     EMPTY( EnumSet.noneOf( Direction.class ) ),
     // #1 A North Wall.
     SIDEN( EnumSet.of( Direction.North ) ),
     // #2 An East wall.
     SIDEE( EnumSet.of( Direction.East ) ),
     // #3 A corner on the North-East side.
     CORNERNE( EnumSet.of( Direction.North, Direction.East ) ),
     // #4 A South wall.
     SIDES( EnumSet.of( Direction.South ) ),
     // #5 A hallway going from East to West or vice-versa.
     HALLEW( EnumSet.of( Direction.North, Direction.South ) ),
     // #6 A corner on the South-East side.
     CORNERSE( EnumSet.of( Direction.South, Direction.East ) ),
     // #7 An East dead-end.
     DEADENDE( EnumSet.of( Direction.North, Direction.South, Direction.East ) ),
     // #8 A West wall.
     SIDEW( EnumSet.of( Direction.West ) ),
     // #9 A corner on the Norht-West side.
    CORNERNW( EnumSet.of( Direction.East, Direction.West ) ),
     // #10 A hallway going from North to South or vice-versa.
     HALLNS( EnumSet.of( Direction.East, Direction.West ) ),
     // #11 A North dead-end.
     DEADENDN( EnumSet.of( Direction.North, Direction.East, Direction.West ) ),
     // #12 A corner on the South-West side.
     CORNERSW( EnumSet.of( Direction.South, Direction.West ) ),
     // #13 A West dead-end.
     DEADENDW( EnumSet.of( Direction.North, Direction.South, Direction.West ) ),
     // #14 A South dead-end.
    DEADENDs( EnumSet.of( Direction.South, Direction.East, Direction.West ) ),
     // #15 A closed block.
     BLOCK( EnumSet.allOf( Direction.class ) )
     ;
     private Set< Direction > directions;
     /**
      * Initializes the class.
      * 
      * @param directions is a list of compass directions not 
      *  blocked with the wall.
      * 
      * @throw NullPointerException when directions is null.
      */
     Wall( Set< Direction > directions ) {
         this.directions = directions;
     }
     
     /**
      * Accesses the list of directions not blocked by the wall.
      * 
      * @return the list of unblocked directions.
      */
     public Set< Direction > getDirections() {
         return this.directions;
     }
 }
