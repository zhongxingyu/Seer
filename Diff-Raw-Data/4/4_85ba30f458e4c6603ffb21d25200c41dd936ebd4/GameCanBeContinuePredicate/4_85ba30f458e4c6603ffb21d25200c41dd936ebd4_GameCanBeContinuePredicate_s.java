 package com.github.colorlines.domainimpl;
 
 import com.github.colorlines.domain.Area;
 import com.github.colorlines.domain.Position;
 import com.google.common.base.Predicate;
 
 import static com.github.colorlines.domain.Position.X_VALUES;
 import static com.github.colorlines.domain.Position.Y_VALUES;
 
 
 /**
  * @author Stanislav Kurilin
  */
 public class GameCanBeContinuePredicate implements Predicate<Area> {
     @Override
     public boolean apply(Area input) {
         int empty = 0;
         for (int x : X_VALUES)
             for (int y : Y_VALUES)
                if (input.contains(Position.create(x, y))) empty++;
        return empty < 7;
     }
 }
