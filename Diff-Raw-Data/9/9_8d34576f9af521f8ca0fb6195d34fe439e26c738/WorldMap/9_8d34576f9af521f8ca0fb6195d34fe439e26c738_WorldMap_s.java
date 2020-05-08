 import ants.*;
 import java.io.DataOutputStream;
 import java.io.DataInputStream;
 import java.util.ArrayList;
 import java.io.IOException;
 
 public class WorldMap{
 
   /**
    * This will contain the timestep when this tile was last updated.
    */
   private int[][] lastSeenTimeStep; 
 
   /**
    * These are the walls that have been seen. We will never degrade the value of
    * these, as walls are not apt to move. 
    */
   private boolean[][] walls;
 
   /**
    * This contains how much food was in the tiles when they were last seen. This
    * will degrade slowly as the timesteps increase.
    */
   private int[][] foodAmounts;
 
   /**
    * This contains how many ants were in the tiles when they were last seen.
    * This will degrade rapidly, since ants are moving quite frequently.
    */
   private int[][] antAmounts;
 
   /**
    * The center in the x direction. Our coordinate system will take an input of
    * (0, 0) to be the center of the grid.
    */
   private int centerx;
  
   /**
    * The center in the y direction. Our coordinate system will take an input of
    * (0, 0) to be the center of the grid.
    */
   private int centery;
 
   /**
    * Test Procedure. 
    * @param argv Command line arguments
    */
   public static void main(String[] argv){
     WorldMap testWorld = new WorldMap(20);
     testWorld.updateMap(0, 0, true, 0, 0, 0);
     testWorld.updateMap(0, 1, false, 1, 0, 0);
     testWorld.updateMap(1, 1, false, 0, 0, 0);
     testWorld.updateMap(1, 0, false, 0, 0, 0);
     testWorld.updateMap(1, -1, false, 0, 0, 0);
     testWorld.updateMap(0, -1, false, 0, 0, 0);
     testWorld.updateMap(-1, -1, false, 0, 0, 0);
     testWorld.updateMap(-1, 0, false, 0, 0, 0);
     testWorld.updateMap(-1, 1, false, 0, 0, 0);
     System.out.println(testWorld);
   }
 
   WorldMap(){
     this(20);
   }
   /**
    * Primary constructor. Initializes all necessary variables to their
    * appropriate sizes. 
    * @param size The size of the new world. (Will dynamically resize)
    */
   WorldMap(int size){
     this.lastSeenTimeStep = new int[size][size];
     for(int x = 0; x < size; x++){
       for(int y = 0; y < size; y++){
         this.lastSeenTimeStep[x][y] = -1;
       }
     }
     this.walls = new boolean[size][size];
     this.foodAmounts = new int[size][size];
     this.antAmounts = new int[size][size];
     this.centerx = size / 2;
     this.centery = size / 2;
   }
 
 
   public void adjustTimes(int previousStep, int newStep){
     int difference = newStep - previousStep;
    for(int y = 0; y < this.lastSeenTimeStep.length; y++){
      for(int x = 0; x < this.lastSeenTimeStep[y].length; x++){
        this.lastSeenTimeStep[y][x] += difference;
      }
    }
   }
 
 
   public void serializeMap(DataOutputStream dataWriter) throws IOException{
     dataWriter.writeInt(this.walls.length);
     dataWriter.writeInt(this.walls[0].length);
     dataWriter.writeInt(this.centerx);
     dataWriter.writeInt(this.centery);
     for(int y = 0; y < this.lastSeenTimeStep.length; y++){
       for(int x = 0; x < this.lastSeenTimeStep[y].length; x++){
         dataWriter.writeInt(this.lastSeenTimeStep[y][x]);
         dataWriter.writeInt(this.foodAmounts[y][x]);
         dataWriter.writeInt(this.antAmounts[y][x]);
         dataWriter.writeBoolean(this.walls[y][x]);
       }
     }
   }
 
   public void deserializeMap(DataInputStream dataReader) throws IOException{
     int height = dataReader.readInt();
     int width = dataReader.readInt();
     this.centerx = dataReader.readInt();
     this.centery = dataReader.readInt();
     this.lastSeenTimeStep = new int[height][width];
     this.walls = new boolean[height][width];
     this.foodAmounts = new int[height][width];
     this.antAmounts = new int[height][width];
     for(int y = 0; y < this.lastSeenTimeStep.length; y++){
       for(int x = 0; x < this.lastSeenTimeStep[y].length; x++){
         this.lastSeenTimeStep[y][x] = dataReader.readInt();
         this.foodAmounts[y][x] = dataReader.readInt();
         this.antAmounts[y][x] = dataReader.readInt();
         this.walls[y][x] = dataReader.readBoolean();
       }
     }
   }
 
   /**
    * Updates a position on the map with the specified tile. Calls the generic
    * version of updateMap after reading necessary information from the tile.
    * @param x The x distance from the anthill
    * @param y The y distance from the anthill
    * @param currentTile The tile that we are adding to the world
    * @param timestep The current timestep of the simulation. Used when we
    * degrade the value of the information gathered. 
    */
   public void updateMap(int x, int y, Tile currentTile, int timestep){
     this.updateMap(x, y, !currentTile.isTravelable(),
                           currentTile.getAmountOfFood(),
                           currentTile.getNumAnts(), 
                           timestep);
   }
 
 
   /**
    * Actually updates the values of the many arrays that hold the information
    * about the world. 
    * @param x The x distance from the anthill
    * @param y The y distance from the anthill
    * @param wall True if this tile is a wall
    * @param food The amount of food in this tile
    * @param ants The number of ants in this tile
    * @param timestep The current timestep of the simulation.
    */
   public void updateMap(int x, int y, boolean wall, int food, int ants, 
                         int timestep){
     // Convert the coordinates to our internal representation. 
     int xcoord = x + centerx;
     int ycoord = y + centery;
 
     // If a coordinate is outside the range addressible, we must resize
     if(xcoord < 0 || ycoord < 0 || 
        ycoord >= this.walls.length || xcoord >= this.walls[0].length){
 
       // Double the size of the board. 
       int size = this.walls.length * 2;
       
       // Create our new arrays to hold the bigger data
       boolean[][] newWalls = new boolean[size][size];
       int[][] newFood = new int[size][size];
       int[][] newAnts = new int[size][size];
       int[][] newSeen = new int[size][size];
 
       // Initialize seen positions to -1
       for(int yy = 0; yy < newSeen.length; yy++){
         for(int xx = 0; xx < newSeen[yy].length; xx++){
           newSeen[yy][xx] = -1;
         }
       }
 
       // Calculate the estimated new center.
       int newx = size/2;
       int newy = size/2;
 
       // Calculate the difference between the center, for easier copying.
       int difx = newx - this.centerx;
       int dify = newy - this.centery;
 
       // Copy data from old arrays
       for(int yp = 0; yp < this.walls.length; yp++){
         boolean[] wallRow = this.walls[yp];
         int[] foodRow = this.foodAmounts[yp];
         int[] antRow = this.antAmounts[yp];
         int[] lstRow = this.lastSeenTimeStep[yp];
         for(int xp = 0; xp < wallRow.length; xp++){
           newWalls[yp + dify][xp + difx] = wallRow[xp];
           newFood[yp + dify][xp + difx] = foodRow[xp];
           newAnts[yp + dify][xp + difx] = antRow[xp];
           newSeen[yp + dify][xp + difx] = lstRow[xp];
         }
       }
 
       // Reassign variables to the new values
       this.centerx = newx;
       this.centery = newy;
       this.walls = newWalls;
       this.foodAmounts = newFood;
       this.antAmounts = newAnts;
       this.lastSeenTimeStep = newSeen;
       
       // Recalculate internal coordinates.
       xcoord = x + centerx;
       ycoord = y + centery;
     }
 
     // Set the values of the world.
     this.walls[ycoord][xcoord] = wall;
     this.foodAmounts[ycoord][xcoord] = food;
     this.antAmounts[ycoord][xcoord] = ants;
     this.lastSeenTimeStep[ycoord][xcoord] = timestep;
   }
 
   /**
    * Analyze the current world and decides possible moves for our ant to take.
    * @param x The x distance from the anthill
    * @param y the y distance from the anthill
    * @param hasFood True if the ant is currently carrying food
    * @return An array of possible moves for the ant
    */
   public Move[] getPossibleMoves(int x, int y, boolean hasFood){
     int xc = x + centerx;
     int yc = y + centery;
 
     ArrayList<Move> retval = new ArrayList<Move>();
 
     if(validMove(Action.move(Direction.NORTH), x, y, hasFood)){
       retval.add(new Move(Action.move(Direction.NORTH), 
                           new Position(x, y-1)));
     }
     if(validMove(Action.move(Direction.EAST), x, y, hasFood)){
       retval.add(new Move(Action.move(Direction.EAST), 
                           new Position(x+1, y)));
     }
     if(validMove(Action.move(Direction.SOUTH), x, y, hasFood)){
       retval.add(new Move(Action.move(Direction.SOUTH), 
                           new Position(x, y+1)));
     }
     if(validMove(Action.move(Direction.WEST), x, y, hasFood)){
       retval.add(new Move(Action.move(Direction.WEST), 
                           new Position(x-1, y)));
     }
 
     return retval.toArray(new Move[0]);
   }
 
   /**
    * Returns the amount of food at the given coordinates.
    * @param x The x distance from the anthill
    * @param y the y distance from the anthill
    * @return the amount of food at a given square
    */
   public int getFood(int x, int y){
     if(validPosition(x,y)){
       return this.foodAmounts[y + centery][x + centerx];
     }else{
       return 0;
     }
   }
 
   /**
    * Returns wether or not the coordinates are within the current squares. This
    * keeps us from addressing outside the arrays.
    * @param x the x distance from the anthill
    * @param y the y distance from the anthill
    * @return If the coordinates lie within the internal arrays.
    */
   private boolean validPosition(int x, int y){
     int cx = x + centerx;
     int cy = y + centery;
 
     if(cx < 0) return false;
     if(cy < 0) return false;
     if(cx >= walls[0].length) return false;
     if(cy >= walls.length) return false;
     return true;
   }
 
   /**
    * Determines if the move can be taken. For example, if there is no food in a
    * square, then gather is not a valid action. 
    * @param a The action being tested
    * @param x the x distance from the anthill
    * @param y the y distance from the anthill
    * @param hasFood Wether or not the ant is carrying food.
    * @return Wether or not the ant will be able to make this action
    */
   public boolean validMove(Action a, int x, int y, boolean hasFood){
     int cx = x + centerx;
     int cy = y + centery;
 
     if(!validPosition(x, y)) return false;
 
     if(a == Action.HALT){
       return true;
     }else if(a == Action.GATHER){
       return this.foodAmounts[cy][cx] > 0 && !hasFood;
     }else if(a == Action.DROP_OFF){
       return hasFood;
     }else{
       switch(a.getDirection()){
         case NORTH:
           if(validPosition(x, y-1)){
             return (!this.walls[cy - 1][cx] 
                     && this.lastSeenTimeStep[cy-1][cx] >= 0);
           }
           return false;
         case EAST:
           if(validPosition(x+1, y)){
             return (!this.walls[cy][cx + 1]
                     && this.lastSeenTimeStep[cy][cx+1] >= 0);
           }
           return false;
         case SOUTH:
           if(validPosition(x, y+1)){
             return (!this.walls[cy + 1][cx]
                     && this.lastSeenTimeStep[cy+1][cx] >= 0);
           }
           return false;
         case WEST:
           if(validPosition(x-1, y)){
             return (!this.walls[cy][cx - 1]
                     && this.lastSeenTimeStep[cy][cx-1] >= 0);
           }
           return false;
       }
     }
     return false;
   }
 
   /**
    * Converts the current information about the world into a string to be
    * displayed. Useful for debugging, or just seeing what's going on inside of
    * an ant's brain
    */
   public String toString(){
     String retval = "";
     int minx = 999, miny = 999;
     int maxx = -999, maxy = -999;
 
     // Find the minimum/maximum x and y values.
     for(int y = 0; y < this.walls.length; y++){
       for(int x = 0; x < this.walls[y].length; x++){
         if(this.lastSeenTimeStep[y][x] >= 0){
           minx = Math.min(minx, x);
           miny = Math.min(miny, y);
           maxx = Math.max(maxx, x);
           maxy = Math.max(maxy, y);
         }
       }
     }
 
     // Draw the map within the min/max x/y values
     for(int y = miny; y <= maxy; y++){
       int[] seenRow = this.lastSeenTimeStep[y];
       boolean[] wallRow = this.walls[y];
       int[] foodRow = this.foodAmounts[y];
       int[] antRow = this.antAmounts[y];
       for(int x = minx; x <= maxx; x++){
         int seen = seenRow[x];
         boolean wall = wallRow[x];
         int food = foodRow[x];
         int ant = antRow[x];
         if(x == centerx && y == centery){
           retval += "AH";
         }else if(wall){
           retval += "##";
         }else if(food > 0){
           retval += String.format("%02d", food);
         }else if(seen < 0){
           retval += "??";
         }else{
           retval += "  ";
         }
         retval += "|";
       }
       retval += '\n';
     }
     return retval;
   }
 }
