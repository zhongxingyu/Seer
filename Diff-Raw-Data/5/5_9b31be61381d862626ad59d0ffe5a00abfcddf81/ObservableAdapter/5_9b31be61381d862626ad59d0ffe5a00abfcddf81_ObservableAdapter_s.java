 package com.saleem_siddiqui.java.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A simple implementation class for the Observable interface.
  * <p/>
  * The purpose of this class -- like the several "Adapter" class in Swing -- is to simplify the code within
  * Observable classes. Instead of implementing the Observer interfaces, class can extend this ObservableAdapter
  * (when possible) to greatly reduce the amount of code that's needed.
  * <p/>
  * This class provides canonical implementation for all the methods in the Observer interface -- it is not
  * abstract, which means that implementing classes need not implement any methods of the Observer interface.
  * <p/>
  * This class exposes two protected methods: <code>setChanged()</code> and <code>clearChanged</code>. Subclasses
  * should call the <code>setChanged()</code> method to indicate when there are changes in the state of this object of
  * which the observers have not yet been notified. Any subsequent call to <code>notifyObservers()</code>
  * or <code>notifyObservers(Event e)</code> will then call <code>clearChanged()</code> before it exits.
  */
 public class ObservableAdapter<E> implements Observable<E> {
     private boolean changed = false;
     private Map<Observer, Filter> obs;
 
     public ObservableAdapter() {
         obs = new HashMap<Observer, Filter>();
     }
 
     public synchronized void addObserver(Observer observer) {
         obs.put(observer, ALLOW_ALL_FILTER);
     }
 
     public synchronized void addObserver(Observer observer, Filter<E> filter) {
         if (observer == null) throw new NullPointerException();
         if (filter == null) throw new NullPointerException();
         if (!obs.containsKey(observer)) {
             obs.put(observer, filter);
         }
     }
 
     public synchronized void deleteObserver(Observer observer) {
         obs.remove(observer);
     }
 
     public void notifyObservers() {
         notifyObservers(null);
     }
 
     public void notifyObservers(E event) {
        Map<Observer, Filter> localCopy = null;
         synchronized (this) {
             if (!changed) return;
             localCopy = new HashMap<Observer, Filter>(obs);
             clearChanged();
         }
         notifyObservers(localCopy, event);
     }
 
 
     /**
      * This is the only method that uses operations that are theoretically type unsafe. It is annotated to suppress
      * unchecked warnings.
      * The operations are only <i>theoretically</i> type unsafe, because in reality, the compiler will verify that
      * both Observer and Observable are coupled to the same "Event" type. This type check is performed for the
      * addObserver() method of this class -- the only way to associate Observers with this ObservableAdapter.
      * Therefore, there cannot be any Observers in the observerMap that aren't "related" to this ObservableAdapter
      * through the same "Event" type.
      *
      * @param observerMap
      * @param event
      */
     @SuppressWarnings("unchecked")
     private void notifyObservers(Map<Observer, Filter> observerMap, E event) {
        for (Map.Entry<Observer, Filter> entry : obs.entrySet()) {
             if (entry.getValue().accept(event)) {
                 entry.getKey().update(this, event);
             }
         }
     }
 
     public synchronized void deleteObservers() {
         obs.clear();
     }
 
     public synchronized boolean hasChanged() {
         return changed;
     }
 
     public synchronized int countObservers() {
         return obs.size();
     }
 
     protected synchronized void setChanged() {
         changed = true;
     }
 
     protected synchronized void clearChanged() {
         changed = false;
     }
 
     private final Filter ALLOW_ALL_FILTER = new Filter() {
         public boolean accept(Object event) {
             return true;
         }
     };
 }
