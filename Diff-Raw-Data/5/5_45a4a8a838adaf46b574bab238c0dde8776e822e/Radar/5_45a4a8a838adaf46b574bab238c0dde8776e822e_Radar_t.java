 import java.util.Arrays;
 
 /**
  * The radar to show what has been discovered of the enemy
  * The default size is 10x10 which represents the 1:10 and A:J
  * and leaves column zero and row zero blank
  *
  * X represents a hit on a ship
  * O represents a miss
  * * represents an unused space
  */
 public class Radar {
     private char[][] radar;
     private int radarWidth;
     private int radarHeight;
 
     public Radar() {
         this(10, 10);
     }
 
     /* If we want a 10x10 board we create an 11x11 array so that we can omit
      * row and column zero and just use 1:n and 1:m indicies */
     public Radar(int radarHeight, int radarWidth) {
         this.radarWidth = radarHeight+1;
         this.radarHeight = radarWidth+1;
         this.radar = new char[this.radarHeight][this.radarWidth];
         radarInit();
     }
 
     /* This assumes a blank (no ships) NxM board (i.e. rows are equal lengths) */
     public Radar(char[][] radar) {
         this.radarWidth = radar.length;
         this.radarHeight = radar[0].length;
         this.radar = radar;
         radarInit();
     }
 
     private void radarInit() {
         /* clear the zero'th row and column */
         for(int i = 0; i < this.radarHeight; i++)
             this.radar[i][0] = ' ';
         for(int i = 0; i < this.radarWidth; i++)
             this.radar[0][i] = ' ';
 
         for(int i = 1; i < this.radarHeight; i++) {
             for(int j = 1; j < this.radarWidth; j++) {
                 this.radar[i][j] = '*';
             }
         }
     }
 
     /**
      * Marks a given shot on the radar
      * @param row Row to mark
      * @param col Column to mark.
      * @param hitOrMiss 'h' or 'H' indicates a hit. 'm' or 'M' indicates a miss.
      * @return Returns true if the spot is successfully marked. False otherwise
      * with a printed error message
      */
     public boolean markRadar(int row, int col, char hitOrMiss) {
         if(!checkShotParams(row, col, hitOrMiss))
             return false;
 
         if(hitOrMiss == 'h' || hitOrMiss == 'H') {
             this.radar[row][col] = 'X';
             return true;
         }
         else { // no need to check for 'm' or 'M' since we passed the check params
             this.radar[row][col] = 'O';
             return true;
         }
     }
 
     /* returns true if it is a valid location, false otherwise */
     private boolean checkLocation(int row, int col) {
         /* check if the coordinates are in the playable range */
        if(row < 1 || row > this.radarHeight-1) {
             System.err.println(String.format("Error: The row must be between 1 and %d", this.radarHeight-1));
             return false;
         }
        else if(col < 1 || col > this.radarWidth-1) {
             System.err.println(String.format("Error: The column must be between 1 and %d", this.radarWidth-1));
             return false;
         }
 
         // Looks ok
         return true;
     }
 
     /* returns true if the parameters are valid, false otherwise */
     private boolean checkShotParams(int row, int col, char hitOrMiss) {
         if(!checkLocation(row, col))
             return false;
 
         /* is the hitOrMiss one we know? */
         else if(hitOrMiss != 'h' && hitOrMiss != 'H' && hitOrMiss != 'm' && hitOrMiss != 'M') {
             System.err.println(String.format("Error: Unrecognized his or miss value '%c'", hitOrMiss));
             return false;
         }
 
         /* Everything looks good! */
         return true;
     }
 
     /**
      * Returns the value stored in the radar at a given location
      * @param row The row to check.
      * @param col The column to check.
      * @return Will return the character stored at that location if it is a
      * valid spot, or the null character for any invalid locations.
      */
     public char getLocation(int row, int col) {
         if(!checkLocation(row, col))
             return '\0';
 
         return this.radar[row][col];
     }
 
     public String toString() {
         StringBuilder s = new StringBuilder();
         for(int i = 0; i < this.radarHeight; i++) {
             s.append(Arrays.toString(this.radar[i]));
             s.append("\n");
         }
 
         return s.toString();
     }
 }
