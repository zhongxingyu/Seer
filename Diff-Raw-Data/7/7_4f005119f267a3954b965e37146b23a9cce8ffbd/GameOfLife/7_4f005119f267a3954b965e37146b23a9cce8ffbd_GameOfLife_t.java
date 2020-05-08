 package net.diegolemos.gameoflife;
 
 import static java.lang.Thread.sleep;
 
 /**
  * @author: Diego Lemos
  * @since: 12/9/12
  */
 public class GameOfLife {
 
     private static final Cell[] BLINDER = new Cell[] {new Cell(1,0), new Cell(1,1), new Cell(1,2)};
     private static final Cell[] GLIDER = new Cell[] {new Cell(3,4), new Cell(4,4), new Cell(5,4), new Cell(5,5), new Cell(4,6)};
 
     public static void main(String... args) throws InterruptedException {
         World world = new World(GLIDER);
 
         do {
             printSpace(world);
         } while(world.nextGeneration());
     }
 
     private static void printSpace(World world) throws InterruptedException {
         String space = takeAPicture(world);
         System.out.println(space);
         sleep(500L);
     }
 
     private static String takeAPicture(World world) {
         int minX = 0;
         int minY = 0;
        int maxX = 30;
         int maxY = 10;
 
         StringBuilder space = new StringBuilder();
 
        for(int y = maxY; y >= minY; y--) {
            for(int x = minX; x <= maxX; x++) {
                 if(world.isAlive(new Cell(x, y))) {
                     space.append("X");
                 } else {
                     space.append(" ");
                 }
             }
 
             space.append("\n");
         }
 
         return space.toString();
     }
 }
