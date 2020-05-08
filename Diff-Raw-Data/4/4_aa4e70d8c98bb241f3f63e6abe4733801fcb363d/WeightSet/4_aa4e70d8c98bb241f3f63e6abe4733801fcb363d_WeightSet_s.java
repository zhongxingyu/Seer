 /*
  * Copyright 1996-2002 by Andruid Kerne. All rights reserved. CONFIDENTIAL. Use is subject to
  * license terms.
  */
 package ecologylab.collections;
 
 import java.util.*;
 
 import ecologylab.appframework.Memory;
 import ecologylab.generic.Debug;
 import ecologylab.generic.Generic;
 import ecologylab.generic.MathTools;
 import ecologylab.generic.ObservableDebug;
 import ecologylab.generic.ThreadMaster;
 
 /**
  * Provides the facility of efficient weighted random selection from a set elements, each of whicn
  * includes a characterizing floating point weight.
  * <p>
  * 
  * Works in cooperation w <code>SetElement</code>s; requiring that user object to keep an integer
  * index slot, which only we should be accessing. This impure oo style is an adaption to Java's lack
  * of multiple inheritance.
  * <p>
  * 
  * Gotta be careful to be thread safe. Seems no operations can safely occur concurrently. There are
  * a bunch of synchronized methods to affect this.
  **/
 public class WeightSet<E extends AbstractSetElement> extends ObservableDebug implements Iterable<E>
 {
 	private static final int						NO_MAX_SIZE	= -1;
 
 	private final ArrayListX<E>					arrayList;
 	
 	private final HashSet<E>						hashSet;
 
 	private final WeightingStrategy<E>	weightingStrategy;
 
 	private final Comparator<E>					comparator;
 
 	private int													maxSize					= NO_MAX_SIZE;
 	
 	private static final int						DEFAULT_SIZE			= 16;
 
 	/**
 	 * This might pause us before we do an expensive operation.
 	 */
 	ThreadMaster												threadMaster;
 
 	public WeightSet (int maxSize, int setSize, WeightingStrategy<E> weightingStrategy)
 	{
 		assert weightingStrategy != null;
 		
 		this.hashSet			= new HashSet<E>(setSize);
 		this.arrayList		= new ArrayListX<E>(setSize);
 		this.maxSize			= maxSize;
 		
 		this.weightingStrategy	= weightingStrategy;
 		this.comparator 				= new FloatWeightComparator(weightingStrategy);
 //		for (E e : arrayList)
 //			weightingStrategy.insert(e);
 	}
 	
 	public WeightSet ( WeightingStrategy<E> getWeightStrategy )
 	{
 		this(NO_MAX_SIZE, DEFAULT_SIZE, getWeightStrategy);
 	}
 
 	public WeightSet (int maxSize, ThreadMaster threadMaster, WeightingStrategy<E> weightStrategy )
 	{
 		this(maxSize, maxSize, weightStrategy);
 		this.threadMaster	= threadMaster;
 	}
 
 	// ///////////////////////////////////////////////////////
 	// DEFAULT COMPARATOR
 	// ///////////////////////////////////////////////////////
 	public final class FloatWeightComparator implements Comparator<E>
 	{
 		private WeightingStrategy<E>	strat;
 
 		public FloatWeightComparator ( WeightingStrategy<E> getWeightStrategy )
 		{
 			strat = (WeightingStrategy<E>) getWeightStrategy;
 		}
 
 		public int compare ( E o1, E o2 )
 		{
 			return Double.compare(strat.getWeight(o1), strat.getWeight(o2));
 		}
 	};
 
 	private void sortIfWeShould ( )
 	{
 		// TODO remove this check, make getWeightStrategy call sort?
 		if (weightingStrategy.hasChanged())
 		{
 			Collections.sort(arrayList, comparator);
 			setChanged();
 			notifyObservers();
 			weightingStrategy.clearChanged();
 		}
 	}
 
 	public synchronized double mean ( )
 	{
 		if (arrayList.size() == 0)
 			return 0;
 		double mean = 0;
 		for (E e : arrayList)
 			mean += weightingStrategy.getWeight(e);
 		return mean / arrayList.size();
 	}
 
 	private synchronized void clearAndRecycle (int start, int end)
 	{
 		int size = arrayList.size();
 		debug("^^^size before = " + size);
 		for (int i=end - 1; i>=start; i--)
 		{
 			E element = arrayList.get(i);	// was remove(i), but that's inefficent
 			element.deleteHook();
 			element.recycle(true);// will also call deleteHook?!
 		}
 		// all of these elements are probably at the beginning of the list
 		// remove from there is worst case behavior of arrayList, because all of the higher elements
 		// must be moved.
 		// minimize this by doing it once.
 		//
 		// in case of CfContainers, recycle removes them from a set; however a weird case results in
 		// some of them not being removed, so this makes sure that all recycled elements are removed.
 		int expectedSize 	= size - (end - start);
 		int newSize 			= arrayList.size();
 		if (expectedSize < newSize)
 		{
 			int sizeDiff = newSize - expectedSize;
 			arrayList.removeRange(start, sizeDiff);
 		}
 	}
 
 	/**
 	 * Selects the top weighted element from the set.<br>
 	 * This method removes the selected from the set.
 	 * 
 	 * @return
 	 */
 	public synchronized E maxSelect ( )
 	{
 		ArrayList<E> list = this.arrayList;
 		int size = list.size();
 		if (size == 0)
 			return null;
 		try
 		{
 			sortIfWeShould();
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 			boolean foundNulls	= false;
 			for (int i=list.size() - 1; i>=0; i--)
 			{
 				E entry	= list.get(i);
 				if (entry == null)
 				{
 					error("Oh my! Null Entry!!!!!");
 					list.remove(i);
 					foundNulls	= true;
 				}
 			}
 			if (foundNulls)
 				return maxSelect();
 		}
 		return list.remove(--size);
 	}
 
 	public synchronized E maxPeek ( )
 	{
 		ArrayList<E> list = this.arrayList;
 		int size = list.size();
 		if (size == 0)
 			return null;
 		sortIfWeShould();
 		return list.get(--size);
 	}
 	
 	public synchronized E maxPeek(int index)
 	{
 		ArrayList<E> list = this.arrayList;
 		int size = list.size();
 		if (size == 0)
 			return null;
 		sortIfWeShould();
 		int desiredIndex = size - index - 1;
 		return list.get(desiredIndex);
 	}
 
 	/**
 	 * Default implementation of the prune to keep only maxSize elements
 	 */
 	public synchronized void prune()
 	{
 		prune(maxSize);
 	}
 	
 	public synchronized void prune ( int numToKeep )
 	{
 		if (maxSize < 0)
 			return;
 		ArrayList<E> list = this.arrayList;
 		int numToDelete = list.size() - numToKeep;
 		if (numToDelete <= 0)
 			return;
 		debug("prune() -> "+numToKeep);
 		sortIfWeShould();
 //		List<E> deletionList = list.subList(0, numToDelete);
 		clearAndRecycle(0, numToDelete);
 		
 		Memory.reclaim();
 	}
 
 	public synchronized void insert ( E el )
 	{
 		if (!hashSet.contains(el))
 		{
 			weightingStrategy.insert(el);
 			arrayList.add(el);
 			hashSet.add(el);
 			el.addSet(this);
 			el.insertHook();
 		}
 	}
 
 	public synchronized void remove ( E el )
 	{
 		removeFromSet(el);
 		el.removeSet(this);
 	}
 
 	protected synchronized void removeFromSet ( E e )
 	{
 		weightingStrategy.remove(e);
 		arrayList.remove(e);
 		hashSet.remove(e);
 	}
 
 	public E get(int i)
 	{
 		return arrayList.get(i);
 	}
 	/**
 	 * Delete all the elements in the set, as fast as possible.
 	 * 
 	 * @param doRecycleElements
 	 *            TODO
 	 * 
 	 */
 	public synchronized void clear ( boolean doRecycleElements )
 	{
 		synchronized (arrayList)
 		{
			for (int i=0; i<size(); i++)
 			{
 				E e	= arrayList.remove(i);
 				e.deleteHook();
 				if (doRecycleElements)
 					e.recycle(false);
 			}
 		}
 		hashSet.clear();
 	}
 
 	/**
 	 * Prune to the set's specified maxSize, if necessary, then do a maxSelect(). The reason for
 	 * doing these operations together is because both require sorting.
 	 * 
 	 * @return element in the set with the highest weight, or in case of a tie, a random pick of
 	 *         those.
 	 */
 	public synchronized E pruneAndMaxSelect ( )
 	{
 		prune(maxSize);
 		return maxSelect();
 	}
 
 	public int size ( )
 	{
 		return arrayList.size();
 	}
 
 	public String toString ( )
 	{
 		return super.toString() + "[" + size() + "]";
 	}
 
 	/**
 	 * Check to see if the set has any elements.
 	 * 
 	 * @return
 	 */
 	public boolean isEmpty ( )
 	{
 		return size() == 0;
 	}
 
 	/**
 	 * Fetches the i'th element in the sorted list.
 	 * 
 	 * @param i
 	 *            index into the list
 	 * @return the element at index i
 	 */
 	public synchronized E at ( int i )
 	{
 		ArrayList<E> list = this.arrayList;
 		if (list.size() == 0 || i >= list.size())
 			return null;
 		sortIfWeShould();
 		return list.get(i);
 	}
 
 	/**
 	 * Fetches the weight of the i'th element in the sorted list.
 	 * 
 	 * @param i
 	 *            index into the list
 	 * @return the weight of the element at index i
 	 */
 	public synchronized Double weightAt ( int i )
 	{
 		ArrayList<E> list = this.arrayList;
 		if (list.size() == 0)
 			return null;
 		sortIfWeShould();
 		
 		return weightingStrategy.getWeight(list.get(i));
 	}
 	
 	
 	/**
 	 * Fetches the highest weight in this set
 	 * @return
 	 */
 	public synchronized Double maxWeight()
 	{
 		return weightAt(this.arrayList.size() - 1);
 	}
 	
 	/**
 	 * Fetches the weight of the passed in element.
 	 * 
 	 * @param e
 	 *            Element to weigh.  Doesn't have to be a member of the set.
 	 * @return the weight of the element
 	 */
 	public double getWeight ( E e )
 	{
 		return weightingStrategy.getWeight(e);
 	}
 	/**
 	 * Method Overriden by {@link cf.model.VisualPool VisualPool} to return true
 	 * 
 	 * @return
 	 */
 	public boolean isRunnable ( )
 	{
 		return false;
 	}
 
 	public WeightingStrategy<E> getWeightStrategy ( )
 	{
 		return weightingStrategy;
 	}
 
 	public synchronized Iterator<E> iterator ( )
 	{
 		return arrayList.iterator();
 	}
 }
