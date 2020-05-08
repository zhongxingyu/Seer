 package examples;
 
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.ArrayList;
 
 class User<E> {
     void remove(Collection<E> c, E x) {
 	Iterator<E> i = c.iterator();
 	while (i.hasNext()) {
 	    E e = i.next();
 	    if (e.equals(x)) i.remove();
 	}
     }
 }
 
 public class IterExample {
     public static void main(String[] args) {
 	User<Integer> u = new User<Integer>();
 	Collection<Integer> c = new ArrayList<Integer>();
 	Integer i = new Integer(3);
 	c.add(i);
 	Iterator<Integer> it = c.iterator();
 	u.remove(c, i); // changes c, so it becoms invalid
	i = it.next; // should result in error from automaton
     }
}
