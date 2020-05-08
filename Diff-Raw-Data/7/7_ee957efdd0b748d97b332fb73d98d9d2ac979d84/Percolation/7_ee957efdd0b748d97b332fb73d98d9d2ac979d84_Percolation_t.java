 public class Percolation {
     int N;
     private UF grid;
 
     // create N-by-N grid, with all sites blocked
     public Percolation(int N) {
         this.N = N;
         this.grid = new UF(gridSize());
     }
 
     // open site (row i, column j) if it is not already
     public void open(int r, int c) {
         validateRange(r, c);
         grid.union(openElement(), site(r, c));
        if(r == 1){
            grid.union(topElement(), site(r, c));
         }
        if(r == N){
            grid.union(bottomElement(), site(r, c));
         }
     }
 
     // is site (row i, column j) open?
     public boolean isOpen(int i, int j) {
         validateRange(i, j);
         return grid.connected(openElement(), site(i, j));
     }
 
     // is site (row i, column j) full?
     public boolean isFull(int i, int j) {
         validateRange(i, j);
         return isOpen(i, j) && grid.connected(topElement(), site(i, j));
     }
 
     private int site(int i, int j) {
         return (i - 1) * N + j;
     }
 
     // does the system percolate?
     public boolean percolates() {
         return grid.connected(topElement(), bottomElement());
     }
 
     private int topElement() {
         return 0;
     }
 
     private int bottomElement() {
         return gridSize() - 2;
     }
 
     private int openElement() {
         return gridSize() - 1;
     }
 
     private int gridSize() {
         int top = 1;
         int bottom = 1;
         int open = 1;
         return N * N + top + bottom + open;
     }
 
     private void validateRange(int row, int col) {
         validateRowRange(row);
         validateColRange(col);
     }
 
     private void validateColRange(int col) {
         if (col < 1 || col > N) {
             throw new IndexOutOfBoundsException("Column is invalid! Should <1,N>");
         }
     }
 
     private void validateRowRange(int row) {
         if (row < 1 || row > N) {
             throw new IndexOutOfBoundsException("Row is invalid! Should be <1,N>");
         }
     }
 }
