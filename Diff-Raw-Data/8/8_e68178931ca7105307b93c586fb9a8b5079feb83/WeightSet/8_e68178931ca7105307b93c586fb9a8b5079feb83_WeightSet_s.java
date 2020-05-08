 /*
  * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
  * CONFIDENTIAL. Use is subject to license terms.
  */
 package ecologylab.collections;
 
 import java.util.*;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.Generic;
 import ecologylab.generic.MathTools;
 import ecologylab.generic.ObservableDebug;
 import ecologylab.generic.ThreadMaster;
 
 /**
  * Provides the facility of efficient weighted random selection from a set
  * elements, each of whicn includes a characterizing floating point weight.
  * <p>
  * 
  * Works in cooperation w <code>SetElement</code>s; requiring that
  * user object to keep an integer index slot, which only we should be
  * accessing. This impure oo style is an adaption to Java's lack of
  * multiple inheritance. 
  * <p>
  *
  * Gotta be careful to be thread safe.
  * Seems no operations can safely occur concurrently.
  * There are a bunch of synchronized methods to affect this.
  **/
 public class WeightSet<E extends SetElement>
 extends ObservableDebug
 {
   /////////////////////////////////////////////////////////
   //  DEFAULT WEIGHTING STRATEGY
   /////////////////////////////////////////////////////////
   public final class DefaultWeightStrategy extends WeightingStrategy<E>
   {
     public double getWeight(E e) {
       return e.getWeight();
     }
     public boolean hasChanged() {
       return true;
     }
   };
   /////////////////////////////////////////////////////////
   // DEFAULT COMPARATOR
   /////////////////////////////////////////////////////////
   public final class FloatWeightComparator implements Comparator<E> {
     private WeightingStrategy<E> strat;
     public FloatWeightComparator(WeightingStrategy<SetElement> getWeightStrategy) {
       strat = (WeightingStrategy<E>) getWeightStrategy;
     }
     public int compare(E o1, E o2) {
       return -Double.compare(strat.getWeight(o1), strat.getWeight(o2));
     }
   };
 
   
   private LinkedList<E>  list = new LinkedList<E>();
   private WeightingStrategy<E> getWeightStrategy = new DefaultWeightStrategy();
   private Comparator<E> comparator = new FloatWeightComparator((WeightingStrategy<SetElement>) getWeightStrategy);
   private int maxSize = -1;
   
   /**
    * This might pause us before we do an expensive operation.
    */
   ThreadMaster    threadMaster;
 
 
   public WeightSet() {}
   
   public WeightSet(WeightingStrategy<SetElement> getWeightStrategy) {
     setWeightingStrategy(getWeightStrategy);
   }
   public WeightSet(int maxSize, ThreadMaster threadMaster) {
     this.threadMaster = threadMaster;
     this.maxSize = maxSize;
   }
   public WeightSet(int maxSize, ThreadMaster threadMaster, WeightingStrategy<SetElement> getWeightStrategy) {
     this(maxSize, threadMaster);
     setWeightingStrategy(getWeightStrategy);
   }
 
   // SETS WEIGHTING TO NEW STRATEGY AND RECONSTRUCTS COMPARATOR
   // TODO: rework this as a constructor parameter instead.
   public synchronized void setWeightingStrategy(WeightingStrategy<SetElement> getWeightStrategy)
   {
     this.getWeightStrategy = (WeightingStrategy<E>) getWeightStrategy;
     this.comparator = new FloatWeightComparator(getWeightStrategy);
     for (E e : list)
       getWeightStrategy.insert(e);
   }
 
   private void sortIfWeShould() {
     if (getWeightStrategy.hasChanged()) {
       Collections.sort(list,comparator);
       setChanged();
       notifyObservers();
       getWeightStrategy.clearChanged();
     }
   }
   
   public synchronized double mean() {
     double mean = 0;
     for (E e : list)
       mean += getWeightStrategy.getWeight(e);
     return mean / list.size();
   }
 
   private void clearAndRecycle(E e) {
     e.deleteHook();
     e.recycle();
   }
 
   public synchronized E maxSelect() {
     LinkedList<E> list = this.list;
     if (list.size() == 0)
       return null;
     sortIfWeShould();
    return list.removeFirst();
   }
   
   public synchronized E maxPeek() {
     LinkedList<E> list = this.list;
     if (list.size() == 0)
       return null;
     sortIfWeShould();
     return list.getFirst();
   }
 
   public synchronized void prune(int numToKeep)
   {
 	if (maxSize < 0)
 		return;
     LinkedList<E> list = this.list;
     int numToDelete = list.size() - numToKeep;
     if (numToDelete <= 0)
       return;
     sortIfWeShould();
     for (int i=0; i<numToDelete; i++)
       clearAndRecycle(list.removeLast());
   }
 
   public synchronized void insert(E el)
   {
     getWeightStrategy.insert(el);
     list.add(el);
     el.addSet(this);
     el.insertHook();
   }
   
   public synchronized void remove(E el) {
     removeFromSet(el);
     el.removeSet(this);
   }
 
   protected synchronized void removeFromSet(E e) {
     getWeightStrategy.remove(e);
     list.remove(e);
   }
 
   
   /**
    * Delete all the elements in the set, as fast as possible.
    * @param doRecycleElements TODO
    *
    */
   public synchronized void clear(boolean doRecycleElements)
   {
     for (E e : list) {
       e.deleteHook();
       if (doRecycleElements)
         e.recycle();
     }
     list.clear();
   }
 
   /**
    * Prune to the set's specified maxSize, if necessary, then do a maxSelect().
    * The reason for doing these operations together is because both require sorting.
    * 
    * @return	element in the set with the highest weight, or in case of a tie,
    * 			a random pick of those.
    */
   public synchronized E pruneAndMaxSelect()
   {
     prune(maxSize);
     return maxSelect();
   }
 
   public int size()
   {
     return list.size();
   }
   public String toString()
   {
     return super.toString() + "[" + size() + "]";
   }
   /**
    * Check to see if the set has any elements.
    * @return
    */
   public boolean isEmpty()
   {
     return size() == 0;
   }
 
   /**
    * Fetches the i'th element in the sorted list.
    * @param i index into the list
    * @return the element at index i
    */
   public synchronized E at(int i) {
 	  LinkedList<E> list = this.list;
 	  if (list.size() == 0)
 		  return null;
 	  sortIfWeShould();
 	  return list.get(i);
   }
   
   /**
    * Fetches the weight of the i'th element in the sorted list.
    * @param i index into the list
    * @return the weight of the element at index i
    */
   public synchronized Double weightAt(int i) {
 	  LinkedList<E> list = this.list;
 	  if (list.size() == 0)
 		  return null;
 	  sortIfWeShould();
 	  return getWeightStrategy.getWeight(list.get(i));
   }
   
   /**
    * Method Overriden by {@link cf.model.VisualPool VisualPool} to return true 
    * @return
    */
   public boolean isRunnable()
   {
     return false;
   }
   
   public WeightingStrategy getWeightStrategy()
   {
 	  return getWeightStrategy;
   }
 }
