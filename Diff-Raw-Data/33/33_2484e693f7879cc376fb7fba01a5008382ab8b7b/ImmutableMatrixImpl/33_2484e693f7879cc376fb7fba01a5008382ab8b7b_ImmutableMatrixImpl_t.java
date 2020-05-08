 package adp2.implementations;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import adp2.interfaces.Matrix;
 
 public final class ImmutableMatrixImpl<E> extends AbstractMatrix<E> {
 
 
     
     public static <E> ImmutableMatrixImpl<E> valueOf(int width, int height, List<E> values) {
         // pre-condition check in factory!
         return new ImmutableMatrixImpl<E>(width, height, values);
     }
 
     private ImmutableMatrixImpl(int width, int height, List<E> values) {
         this.width = width;
         this.height = height;
         this.values = new ArrayList<E>(values);
     }
 
     
 
 
 }
