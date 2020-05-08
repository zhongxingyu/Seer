 import java.util.Set;
 /**
  * Acts as the central hub of the game.
  * 
  * @author Josh Gillham, Jesse Nelson
  * @version 11/9/2012 Windows 8(x64) Java 1.7 U9
  */
 
 /* TODO : implement makeMove and setGameEventsListener
    */
 public class Logic {
     private Maze maze;
     private Character character;
     
     /** Used to signal an illegal move direction. */
     class BadDirectionException extends Exception { }
     
     /**
      * Initializes the class.
     *      
      * Post Conditions:
     * -character is at the starting position.          
      * 
      * @param character is the player.
      * @param maze is the maze.
      * 
      * @throws NullPointerException if either character or maze is null.
      * @throws IllegalArgumentException if the characters position is 
      *  off the map.
      */
     public Logic( Character character, Maze maze ) {
         if( character == null || maze == null ) {
             throw new NullPointerException
             ("Instance of Logic was instantiated with a null value!");
         }
         
         if( !maze.contains( character.getCoordinate() ) ) {
             throw new IllegalArgumentException
             ("The character is not on the map!");
         } 
         
         this.character = character;
         this.maze = maze;
     }
     
     /**
      * Causes the player to move in the direction specified.
      * 
      * @param direction is the direction to move.
      * 
     * @throws BadDirectionException when the direction is not 
      *  allowed by the maze.
      */
     public void makeMove( Direction direction ) throws BadDirectionException {
         if( direction == null ) {
             throw new NullPointerException( "makeMove was called with a null value" );
         }
         
         Set< Direction > directions = this.maze.getWall( this.character.getCoordinate() ).getDirections();
         
         if( !directions.contains( direction ) ) {
             throw new BadDirectionException();
         }
         
         this.character.getCoordinate().translate( direction );
         
         if(!this.maze.contains(this.character.getCoordinate())) {
             //player wins          
         }//else do nothing
     }
     
     /**
      * Gets the character.
      * 
      * @return the character.
      */
     public Character getCharacter() {
         return this.character;
     }
     
     /**
      * Accesses the maze.
      * 
      * @return the maze.
      */
     public Maze getMaze() {
         return this.maze;
     }
     
     /**
      * Sets the GameEvents listener
      * 
      * @param listener the new listener (could be null).
      */
     public void setGameEventsListener( GameEvent listener ) {
         throw new UnsupportedOperationException();
     }
 }
 
