 /**
  * Represents a tile of the game map.
  */
 public class Tile implements Comparable<Tile> {
 
     private final int _row;
     private final int _col;
     private final int _hash;
     
     /**
      * Creates new {@link Tile} object.
      * 
     * @param _row _row index
     * @param _col _column index
      */
     public Tile(int row, int col) {
         _row = row;
         _col = col;
         _hash = _row * Ants.MAX_MAP_SIZE + _col;
     }
     
     /**
      * Returns _row index.
      * 
      * @return _row index
      */
     public int getRow() {
         return _row;
     }
     
     /**
      * Returns _column index.
      * 
      * @return _column index
      */
     public int getCol() {
         return _col;
     }
     
     /** 
      * {@inheritDoc}
      */
     @Override
     public int compareTo(Tile o) {
         return hashCode() - o.hashCode();
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         return _hash;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object o) {
         boolean result = false;
         if (o instanceof Tile) {
             Tile tile = (Tile)o;
             result = _row == tile._row && _col == tile._col;
         }
         return result;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return _row + " " + _col;
     }
 }
