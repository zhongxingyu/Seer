 /*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 * 
 * Licensed under the Academic Free License v. 3.0. 
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 /**
  * Compares two cells for equality by row and subsequently, column (colors of 
  * cells are ignored).
  * 
  * @author awaterma@ecosur.mx
  *
  */
 
 package mx.ecosur.multigame.grid.comparator;
 
 import java.io.Serializable;
 import java.util.Comparator;
 
import mx.ecosur.multigame.grid.Color;
 import mx.ecosur.multigame.grid.model.GridCell;
 
 public class CellComparator implements Comparator<GridCell>, Serializable {
 
     private static final long serialVersionUID = 1192095593909731426L;
 
     public int compare(GridCell cell1, GridCell cell2) {
         int ret = 0;
         if (cell1 == null || cell2 == null)
                 return ret;
 
         if (ret == 0) {
            if (cell1.getRow() > cell2.getRow())
                 ret = 1;
             else if (cell1.getRow() < cell2.getRow())
                 ret = -1;
             else if (cell1.getRow() == cell2.getRow())
                 ret = 0;
         }
 
         if (ret == 0) {
             if (cell1.getColumn() > cell2.getColumn())
                     ret = 1;
             else if (cell1.getColumn() < cell2.getColumn())
                     ret = -1;
             else if (cell1.getColumn() == cell2.getColumn())
                     ret = 0;
         }
 
        if (ret == 0) {
            if (!cell1.getColor().equals(Color.UNKNOWN) && !cell2.getColor().equals(Color.UNKNOWN)) {
                if (!cell1.getColor().equals(cell2.getColor())) {
                    Color[] p = Color.playable();
                    int pos1 = -1, pos2 = -1;
                    for (int i = 0; i < p.length; i++) {
                        if (p [ i ].equals(cell1.getColor()))
                            pos1 = i;
                        else if (p [ i ].equals(cell2.getColor()))
                            pos2 = i;
                    }

                    if (pos1 == -1 || pos2 == -1)
                        throw new RuntimeException ("Unable to perform Color Comparison!");

                    if (pos1 > pos2) {
                        ret = 1;
                    } else if (pos1 < pos2)
                        ret = -1;
                    }
            }
        }

         return ret;
     }
 }
