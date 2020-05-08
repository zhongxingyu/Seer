 package tk.kirlian.util.provider;
 
 import java.util.Comparator;
 
 /**
  * A {@link Comparator} that compares {@link Provider}s based on their priority.
  * @see Provider#getPriority()
  */
 public class PriorityProviderComparator implements Comparator<Provider> {
     public int compare(Provider o1, Provider o2) {
        if(o1.isAvailable() && !o2.isAvailable()) {
             return -1;
        } else if(!o1.isAvailable() && o2.isAvailable()) {
             return 1;
         } else {
             return o1.getPriority() - o2.getPriority();
         }
     }
 }
