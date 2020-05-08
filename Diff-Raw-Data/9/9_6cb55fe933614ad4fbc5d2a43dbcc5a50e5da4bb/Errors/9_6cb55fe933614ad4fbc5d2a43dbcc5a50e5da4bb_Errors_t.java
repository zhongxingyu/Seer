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
 
     public static class PriceLessThanZero extends Error {
         public PriceLessThanZero() {}
         public PriceLessThanZero(String msg) {
             super(msg);
         }
     }
 
     public static class MinPriceGreaterThanMaxPrice extends Error {
         public MinPriceGreaterThanMaxPrice() {}
         public MinPriceGreaterThanMaxPrice(String msg) {
             super(msg);
         }
     }
 
     public static class MatrixColumnsNotEqualToRows extends Error {
         public MatrixColumnsNotEqualToRows() {}
         public MatrixColumnsNotEqualToRows(String msg) {
             super(msg);
         }
     }
 
     public static class MatrixRowsNotEqualToCityNames extends Error {
         public MatrixRowsNotEqualToCityNames() {}
         public MatrixRowsNotEqualToCityNames(String msg) {
             super(msg);
         }
     }
 
     public static class AllelesDoNotMatchGenes extends Error {
         public AllelesDoNotMatchGenes() {}
         public AllelesDoNotMatchGenes(String msg) {
             super(msg);
         }
     }

    public static class DuplicateObjectFound extends Error {
        public DuplicateObjectFound() {}
        public DuplicateObjectFound(String msg) {
            super(msg);
        }
    }
 }
