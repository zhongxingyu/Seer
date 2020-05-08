 /*----------------------------------------------------------------
  *  Author:        Matt Farmer
  *  Written:       08/17/2012
  *  Last updated:  08/25/2012
  *
  *  Compilation:   javac Percolation.java
  *  Execution:     java Percolation
  *
  *  Tests the percolation as per the specification available at:
  *    http://coursera.cs.princeton.edu/algs4/assignments/percolation.html
  *
  *----------------------------------------------------------------*/
 public class Percolation {
 
   private int rowLen;
   private int topIndex;
   private int bottomIndex;
   private boolean[] grid;
 
   private WeightedQuickUnionUF uf;
 
   // create N-by-N grid, with all sites blocked
   public Percolation(int N) {
     int gridSize;
 
     rowLen = N;
     gridSize = N*N;
     uf = new WeightedQuickUnionUF(gridSize + 2);
     grid = new boolean[gridSize];
     topIndex = gridSize;
     bottomIndex = gridSize + 1;
   }
 
   // open site (row i, column j) if it is not already
   public void open(int i, int j) {
     int index = getIndex(i, j);
    if (grid[index]) {
      grid[index] = true;
 
       // If it is in the top or bottom row, connect it with the top or bottom node
       if (0 == i) {
         uf.union(index, topIndex);
       } else if (rowLen-1 == i) {
         uf.union(index, bottomIndex);
       }
 
       // If the spot we just opened has any open neighbors, connect them
       int n; // Neighbor's index
       for (int d = 0; d < 4; d++) {
         n = getNeighborIndex(i, j, d);
         if (-1 != n && isOpen(n)) {
           uf.union(index, n);
         }
       }
     }
   }
 
   private int getIndex(int i, int j) {
     return i*rowLen + j;
   }
 
   /*
    * Get the index of a neighbor in the specified direction.
    *
    * @param (int) i the index
    * @param (int) j the index
    * @param (int) d the direction of the neighbor:
    *                  0 = UP, 1 = RIGHT, 2 = DOWN, 3 = LEFT
    *
    * @return (int) the index of the neighbor or -1 if it is out of bounds
    *
    */
   private int getNeighborIndex(int i, int j, int d) {
     switch (d) {
       case 0:  // UP
         if (0 == i) {
           return -1;
         }
         return getIndex(i-1, j);
       case 1:  // RIGHT
         if (j+1 == rowLen) {
           return -1;
         }
         return getIndex(i, j+1);
       case 2:  // DOWN
         if (1+i == rowLen) {
           return -1;
         }
         return getIndex(i+1, j);
       case 3:  // LEFT
         if (0 == j) {
           return -1;
         }
         return getIndex(i, j-1);
       default:
     }
     return -1;
   }
 
   // is site at given index open
   private boolean isOpen(int index) {
     return grid[index];
   }
 
   // is site (row i, column j) open?
   public boolean isOpen(int i, int j) {
     return isOpen(getIndex(i, j));
   }
 
   // is site (row i, column j) full?
   public boolean isFull(int i, int j) {
     return !isOpen(i, j);
   }
 
   // does the system percolate?
   public boolean percolates() {
     return uf.connected(topIndex, bottomIndex);
   }
 
   public static void main(String[] args) {
     Percolation p = new Percolation(4);
     p.open(0, 0);
     p.open(1, 0);
     p.open(1, 1);
     p.open(2, 1);
     p.open(2, 2);
     p.open(3, 2);
   }
 
 
 }
