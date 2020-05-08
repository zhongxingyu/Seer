 package edu.rivfader.profiling;
 
 import edu.rivfader.relalg.Selection;
 import edu.rivfader.relalg.Projection;
 import edu.rivfader.relalg.Product;
 
 public class Block1Costs implements ICostAccumulator {
     private int costs;
 
     public Block1Costs() {
         costs = 0;
     }
 
     @Override
     public void handleSelectionStatistics(Selection actingNode,
                                    int inputRows,
                                    int outputRows,
                                    int columns) {
        costs += outputRows * columns;
     }
 
     @Override
     public void handleProjectionStatistics(Projection actingNode,
                                     int inputRows,
                                     int columns) {
        costs += actingNode.getSelectedFields().size() * inputRows;
     }
 
     @Override
     public void handleProductStatistics(Product actingNode,
                                  int rows,
                                  int columns) {
        costs += rows * columns;
     }
 
     @Override
     public int getCost() {
         return costs;
     }
 }
