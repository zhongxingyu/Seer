 package ch.zhaw.labyrinth.solver.heading;
 
 import ch.zhaw.labyrinth.utils.Labyrinth;
 
 /**
  * This is the North implementation of Heading
  *
  * @author b.buetikofer
  */
 public class North extends AbstractHeading implements Heading {
 
 
     public North(int x, int y, Labyrinth maze, Labyrinth solvedMaze) {
         this.x = x;
         this.y = y;
         this.maze = maze;
         this.solvedMaze = solvedMaze;
     }
 
     // Check methods
     @Override
     public boolean isRight() {
         return maze.getCellValueAt(x+1, y);
     }
     @Override
     public boolean isLeft() {
         return maze.getCellValueAt(x-1, y);
     }
     @Override
     public boolean isStraight() {
         return maze.getCellValueAt(x, y-1);
     }
     @Override
     public boolean isBack() {
         return maze.getCellValueAt(x, y + 1);
     }
 
     @Override
     public int getVisits(int x, int y) {
         return maze.isCellVisited(x, y);
     }
 
     // Move methods
     @Override
     public East goRight() {
         // mark current cell as visited
         maze.setCellVisited(x, y);
         // Store it in the solvedMaze Map
         setX(x+1);
         storeIt();
         return new East(x, y, maze, solvedMaze);
     }
 
     @Override
     public West goLeft() {
         // mark current cell as visited
         maze.setCellVisited(x, y);
         // Store it in the solvedMaze Map
         setX(x-1);
         storeIt();
         return new West(x, y, maze, solvedMaze);
     }
 
     @Override
     public North goStraight() {
         // mark current cell as visited
         maze.setCellVisited(x, y);
         // Store it in the solvedMaze Map
         setY(y - 1);
         storeIt();
         return new North(x, y, maze, solvedMaze);
     }
 
     @Override
     public South goBack() {
         // mark current cell as visited
         maze.setCellVisited(x, y);
         // Store it in the solvedMaze Map
         setY(y + 1);
         storeIt();
 
        return new South(x, y, maze, solvedMaze);
     }
 
 }
