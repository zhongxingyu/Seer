 package to.richard.tsp;
 
 /**
  * Author: Richard To
  * Date: 2/6/13
  */
 
 /**
  * Custom errors for TSP problem.
  *
  * Using unchecked exceptions here because we want the
  * program to stop on these errors for now.
  *
  * Some of these may be changed to checked exceptions, especially
  * if use input will be involved.
  */
 public class Errors {
 
     public static class ValueLessThanZero extends Error {
         public ValueLessThanZero() {}
         public ValueLessThanZero(String msg) {
             super(msg);
         }
     }
 
     public static class MinValueGreaterThanMaxValue extends Error {
         public MinValueGreaterThanMaxValue() {}
         public MinValueGreaterThanMaxValue(String msg) {
             super(msg);
         }
     }
 
     public static class MatrixColumnsNotEqualToRows extends Error {
         public MatrixColumnsNotEqualToRows() {}
         public MatrixColumnsNotEqualToRows(String msg) {
             super(msg);
         }
     }
 
     public static class MatrixRowsNotEqualToNamedAlleles extends Error {
         public MatrixRowsNotEqualToNamedAlleles() {}
         public MatrixRowsNotEqualToNamedAlleles(String msg) {
             super(msg);
         }
     }
 
     public static class AllelesDoNotMatchGenes extends Error {
         public AllelesDoNotMatchGenes() {}
         public AllelesDoNotMatchGenes(String msg) {
             super(msg);
         }
     }
 
     public static class AlleleNotFound extends Error {
         public AlleleNotFound() {}
         public AlleleNotFound(String msg) {
             super(msg);
         }
     }
 
     public static class DuplicateObjectFound extends Error {
         public DuplicateObjectFound() {}
         public DuplicateObjectFound(String msg) {
             super(msg);
         }
     }
 
     /**
      * This error is a bit too generic. But basically handles the
      * case where a crossover algorithm failed. This is mostly for
      * debugging purposes.
      *
      * But it's possible a crossover error could occur with invalid
      * genes.
      */
     public static class CrossoverFailed extends Error {
         public CrossoverFailed() {}
         public CrossoverFailed(String msg) {
             super(msg);
         }
     }

    public static class TournamentSizeOutOfBounds extends Error {
        public TournamentSizeOutOfBounds() {}
        public TournamentSizeOutOfBounds(String msg) {
            super(msg);
        }
    }
 }
