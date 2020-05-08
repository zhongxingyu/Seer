 package adp2.implementations;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import adp2.interfaces.Matrix;
 
 public final class NaM<T> implements Matrix<T> {
     
     private static Matrix<Object> instance;
     
     @SuppressWarnings("unchecked")
     public static <T> Matrix<T> valueOf() {
         if (instance == null) {
             instance = (NaM<Object>)new NaM<T>();
         }
         return (NaM<T>)instance;
     }
     
     private NaM() {}
 
     @Override
     public Iterator<T> iterator() {
         return new ArrayList<T>().iterator();
     }
 
     @Override
     public int width() {
         return 0;
     }
 
     @Override
     public int height() {
         return 0;
     }
 
     @Override
     public T get(int x, int y) {
         throw new IndexOutOfBoundsException();
     }
     
     @Override
     public String toString() {
         return "NaM";
     }
 
	@Override
	public Matrix<T> deepClone() {
		return valueOf();
	}

     
     
 
 }
