 package histaroach.util;
 
import histaroach.model.DiffFile;

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
    
    @Override public boolean equals(Object other) {
    	if (other == null || !other.getClass().equals(this.getClass())) {
			return false;
		}
		
		Pair<T, S> pair = (Pair<T, S>) other;
		
		return first.equals(pair.first) && second.equals(pair.second);
	}
	
	@Override
	public int hashCode() {
		return 11 * first.hashCode() + 13 * second.hashCode();
	}
 }
