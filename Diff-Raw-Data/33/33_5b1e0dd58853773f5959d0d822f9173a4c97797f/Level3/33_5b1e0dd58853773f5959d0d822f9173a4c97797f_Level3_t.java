 package backend.level;
 
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.Map;
 
 import backend.*;
 import backend.MixedCondition;
 import backend.cell.CandyGeneratorCell;
 import backend.cell.Cell;
 import backend.cell.CheckCell;
 import backend.cell.FoodGenerator;
 import backend.element.FoodType;
 import backend.element.Wall;
 
 public class Level3 extends Grid {
     private static int REQUIRED_SCORE = 0;
     private static int MAX_MOVES = 20;
     private static int REQUIRED_CHERRIES = 1;
    private static int REQUIRED_HAZELNUT = 0;
     private Map<FoodType, FoodTypeState> foodMap = new HashMap<FoodType, FoodTypeState>();
 
     private Cell wallCell;
     private Cell candyGenCell;
     private FoodGenerator FoodGenCell;
     
     @Override
     protected GameState newState() {
         MixedCondition conditions = new MixedCondition();
         GameState gameState = new GameState(conditions);
         conditions.addCondition(new BasicCondition(REQUIRED_SCORE, MAX_MOVES, gameState));
         conditions.addCondition(new FoodCondition(foodMap));
         return gameState;
     }
 
     public void initialize(){
         foodMap.put(FoodType.CHERRY, new FoodTypeState(REQUIRED_CHERRIES));
         foodMap.put(FoodType.HAZELNUT, new FoodTypeState(REQUIRED_HAZELNUT));
         super.initialize();
     }
 
     @Override
     protected void fillGrid(){
         for (int row = 0; row < SIZE-1; row++) {
             for (int col = 0; col < SIZE; col++) {
                 g()[row][col] = new Cell(this);
                 getMap().put(g()[row][col], new Point(row,col));
             }
         }
 
         for(int col = 0; col < SIZE; col++){
             g()[SIZE-1][col] = new CheckCell(this, foodMap);
             getMap().put(g()[SIZE-1][col], new Point(SIZE-1,col));
         }
     }
 
     @Override
     protected void fillCells() {
 
         wallCell = new Cell(this);
         wallCell.setContent(new Wall());
         candyGenCell = new CandyGeneratorCell(this);
         FoodGenCell= new FoodGenerator(this, foodMap); // se agrega foodgenerator.
 
         //corners
         g()[0][0].setAround(FoodGenCell, g()[1][0], wallCell, g()[0][1]);
         g()[0][SIZE-1].setAround(candyGenCell, g()[1][SIZE-1], g()[0][SIZE-2], wallCell);
         g()[SIZE-1][0].setAround(g()[SIZE-2][0], wallCell, wallCell, g()[SIZE-1][1]);
         g()[SIZE-1][SIZE-1].setAround(g()[SIZE-2][SIZE-1], wallCell, g()[SIZE-1][SIZE-2], wallCell);
 
         //upper line cells
         for (int j = 1; j < SIZE-1; j++) {
             g()[0][j].setAround(FoodGenCell,g()[1][j],g()[0][j-1],g()[0][j+1]);
         }
         //bottom line cells
         for (int j = 1; j < SIZE-1; j++) {
             g()[SIZE-1][j].setAround(g()[SIZE-2][j], wallCell, g()[SIZE-1][j-1],g()[SIZE-1][j+1]);
         }
         //left line cells
         for (int i = 1; i < SIZE-1; i++) {
             g()[i][0].setAround(g()[i-1][0],g()[i+1][0], wallCell ,g()[i][1]);
         }
         //right line cells
         for (int i = 1; i < SIZE-1; i++) {
             g()[i][SIZE-1].setAround(g()[i-1][SIZE-1],g()[i+1][SIZE-1], g()[i][SIZE-2], wallCell);
         }
         //central cells
         for (int i = 1; i < SIZE-1; i++) {
             for (int j = 1; j < SIZE-1; j++) {
                 g()[i][j].setAround(g()[i-1][j],g()[i+1][j],g()[i][j-1],g()[i][j+1]);
             }
         }
     }
 
     @Override
     public boolean tryMove(int i1, int j1, int i2, int j2) {
         boolean ret;
         if (ret = super.tryMove(i1, j1, i2, j2)) {
             state().addMove();
         }
         return ret;
     }
 }
 
