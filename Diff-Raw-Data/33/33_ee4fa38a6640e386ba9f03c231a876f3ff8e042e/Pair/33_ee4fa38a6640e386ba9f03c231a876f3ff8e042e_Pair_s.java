 package histaroach.util;
 
 import java.io.Serializable;
 
 
 public class Pair<T, S> implements Serializable {
     /**
 	 * serial version id
 	 */
 	private static final long serialVersionUID = -3696904408956964528L;
 	
 	private final T first;
     private final S second;
     
     public Pair(T first, S second) {
         this.first = first;
         this.second = second;
     }
     
     public T getFirst() {
         return first;
     }
     
     public S getSecond() {
         return second;
     }
 }
