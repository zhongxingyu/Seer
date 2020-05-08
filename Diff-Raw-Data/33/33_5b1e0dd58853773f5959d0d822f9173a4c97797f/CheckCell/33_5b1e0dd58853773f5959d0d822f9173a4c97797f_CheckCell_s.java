 package backend.cell;
 
 import backend.FoodTypeState;
 import backend.Grid;
 import backend.element.Food;
 import backend.element.FoodType;
 import backend.move.Direction;
 
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: daniel
  * Date: 01/06/13
  * Time: 14:10
  * To change this template use File | Settings | File Templates.
  */
 public class CheckCell extends Cell {
     private Map<FoodType, FoodTypeState> foodMap;
 
     public CheckCell(Grid grid, Map<FoodType, FoodTypeState> foodMap){
         super(grid);
         this.foodMap = foodMap;
     }
 
     public boolean fallUpperContent(){
         Cell upperCell = this.getAround()[Direction.UP.ordinal()];
        if (upperCell.getContent().isCheckable() && !(this.getContent().isSolid())){   // TODO condicion isSolid agregada para solucionar problema
            foodMap.get(((Food)(upperCell.getContent())).getFoodType()).reachedCheckCell();
            upperCell.getAndClearContent();
         }
         return super.fallUpperContent();
     }
 }
