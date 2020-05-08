 package mx.ecosur.multigame.grid.comparator;
 
 import mx.ecosur.multigame.grid.Color;
 import mx.ecosur.multigame.grid.model.GridPlayer;
 import mx.ecosur.multigame.model.interfaces.GamePlayer;
 
 import java.io.Serializable;
 import java.util.Comparator;
 
 /**
  * Created by IntelliJ IDEA.
  * User: awaterma
  * Date: 3/15/11
  * Time: 1:13 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PlayerComparator implements Comparator<GridPlayer>, Serializable {
 
 
     @Override
     public int compare(GridPlayer a, GridPlayer b) {
         int ret = 0;
         if (!a.getColor().equals(b.getColor())) {
             Color [] p = Color.playable();
             int pos1 = -1, pos2 = -1;
             for (int i = 0; i < p.length; i++) {
                 if (p [ i ].equals(a.getColor()))
                     pos1 = i;
                 else if (p [ i ].equals(b.getColor()))
                     pos2 = i;
             }
 
             if (pos1 == -1 || pos2 == -1)
                 throw new RuntimeException ("Unable to perform Color Comparison!");
 
             if (pos1 > pos2) {
                 ret = 1;
             } else if (pos1 < pos2)
                 ret = -1;
         }
 
        if (ret == 0) {
            if (a.getId() > b.getId())
                ret = 1;
            else if (a.getId() < b.getId())
                ret = -1;
        }

         return ret;
     }
 }
