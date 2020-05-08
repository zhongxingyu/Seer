 /*
  * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
  *
  * Licensed under the Academic Free License v. 3.0.
  * http://www.opensource.org/licenses/afl-3.0.php
  */
 
 /**
  * The GameGrid class holds the current state of a specific game.  This class
  * is intended to be a transitive object, in the sense that a specific 
  * SharedBoardwill return a populated GameGrid to a specific caller. This 
  * hides gamegrid specific information for runtime callers; allowing quick
  * access to specific cells within a running game.
  * 
  * @author awaterma@ecosur.mx
  *
  */
 
 package mx.ecosur.multigame.impl.model;
 
 import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import mx.ecosur.multigame.impl.CellComparator;
 
 @Entity
 public class GameGrid implements Serializable, Cloneable {
 
     private static final long serialVersionUID = -2579204312184918693L;
 
    TreeSet<GridCell> cells;
 
     private int id;
 
     public GameGrid () {
        cells = new TreeSet<GridCell>(new CellComparator());
     }
 
     /**
      * Initializes a GameGrid object containing the List of Cells.
      */
     public GameGrid(TreeSet<GridCell> cells) {
         this.cells = cells;
     }
 
     @Id
     @GeneratedValue
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public GridCell getLocation (GridCell location) {
        GridCell ret = null;
        if (location != null) {
            CellComparator comparator = (CellComparator) cells.comparator();
            SortedSet<GridCell> sublist = cells.tailSet(location);

            for (GridCell c : sublist) {
                int value = comparator.compare(location, c);
                if (value == 0) {
                    ret = c;
                    break;
                }
            }
         }
 
        return ret;
     }
 
     public void updateCell (GridCell cell) {
         if (cells.contains(cell))
                 cells.remove(cell);
         cells.add(cell);
     }
 
     public void removeCell (GridCell cell) {
         cells.remove(cell);
     }
 
     @OneToMany (cascade={CascadeType.ALL})
     public Set<GridCell> getCells () {
         return cells;
     }
 
     public void setCells(Set<GridCell> cells){
        this.cells = new TreeSet<GridCell> (new CellComparator());
        this.cells.addAll(cells);
     }
 
     public String toString () {
         StringBuffer buf = new StringBuffer();
         Iterator<GridCell> iter = cells.iterator();
         buf.append ("GameGrid: [");
         while (iter.hasNext()) {
             GridCell cell = iter.next();
             buf.append("column: " + cell.getColumn() + ", row: " +
                             cell.getRow() + ", color: " + cell.getColor());
             if (iter.hasNext()) {
                     buf.append ("; ");
             }
             else {
                     buf.append ("]");
             }
         }
 
         return buf.toString();
     }
 
     @Override
     public Object clone() throws CloneNotSupportedException {
         super.clone();
         GameGrid ret = new GameGrid();
         for (GridCell cell : cells) {
                 GridCell cloneCell = cell.clone();
                 ret.updateCell(cloneCell);
         }
 
         return ret;
     }
 }
