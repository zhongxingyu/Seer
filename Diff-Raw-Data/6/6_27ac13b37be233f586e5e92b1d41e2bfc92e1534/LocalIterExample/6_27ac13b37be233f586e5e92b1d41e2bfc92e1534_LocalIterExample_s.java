 package examples;
 
 // Local version of IterExample.java
 // in that it does not require instrumentation of library code
 
 interface Iter<E> {
     public boolean hasNext();
     public E next();
     public void remove();
 }
 
 // dummy collection
 class Collector<E> {
     
     E elem = null;
 
     // dummy iterator
     class It implements Iter<E> {
 	public E next() {
 	    return elem;
 	}
 	public boolean hasNext() {
 	    return elem != null;
 	}
 	public void remove() {
 	    elem = null;
 	}
     }
 
     public Iter<E> iterator() {
 	return new It();
     }
 
    public void add(E e, int i) {
 	elem = e;
     }
 }
 
 class LocalUser<E> {
     void remove(Collector<E> c, E x) {
 	Iter<E> i = c.iterator();
 	while (i.hasNext()) {
 	    E e = i.next();
 	    if (e.equals(x)) i.remove();
 	}
     }
 }
 
 public class LocalIterExample {
     public static void main(String[] args) {
 	LocalUser<Integer> u = new LocalUser<Integer>();
 	Collector<Integer> c = new Collector<Integer>();
 	Integer i = new Integer(3);
	c.add(i, 5);
 	Iter<Integer> it = c.iterator();
 	u.remove(c, i); // changes c, so it becomes invalid
 	i = it.next(); // should result in error from automaton
     }
 }
