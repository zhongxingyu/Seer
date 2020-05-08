 /*
  * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
  * CONFIDENTIAL. Use is subject to license terms.
  */
 package ecologylab.collections;
 
 import ecologylab.generic.Debug;
 
 /**
  * A FloatWeightSet element.
  *
  * Basic implementation of SetElement for cases when you can inherit from here
  **/
 public class FloatSetElement
 extends Debug
 // implements SetElement
 {
    public static final int	NOT_A_MEMBER	= -1;
 
    private int		index		= NOT_A_MEMBER;
 
 /**
  * Cached version of weight. Made accessible for efficiency's sake.
  * Use carefully, at your own risk, and only w inside, deep understanding.
  */
    public float			weight;
 
    BasicFloatSet	set;
 
    public FloatSetElement()
    {
    }
    public FloatSetElement(float initial)
    {
       weight	= initial;
    }
    public float getWeight()
    {
       return weight;
    }
    /**
     * This can be overridden to exclude elements from selection while keeping them in the set.
     * 
     * @return	The default implementation always returns false, including all elements for selection.
     */
    public boolean filteredOut()
    {
 	   return false;
    }
    public int getIndex()
    {
       return index;
    }
    public void setIndex(int newIndex)
    {
       index	= newIndex;
    }
    public void setSet(BasicFloatSet setArg)
    {
       set		= setArg;
    }
 /**
  * Delete in the most expedient manner possible.
  * This is final because you should override deleteHook() to provide
  * custom behavior.
  */
    public final void delete()
    {
       delete(BasicFloatSet.NO_RECOMPUTE);
    }
    
    public boolean isInSet()
    {
 	   return (set != null) && (index != NOT_A_MEMBER);
    }
 /**
  * Delete the element from the set.
  * This means changing the set's structure, and also changing slots in 
  * this to reflect its lack of membership.
  * 
  * @param recompute	-1 for absolutely no recomputation of the set's internal structures.
  * 			 0 for recompute upwards from el
  * 			 1 for recompute all
  * 
  * @return true if the element was a member of a set, and thus, if the delete does something;
  * 			false if the element was not a member of a set, and thus, if the delete does *nothing*.
  */
    public final synchronized boolean delete(int recompute)
    {
       boolean inSet = isInSet();
       if (inSet)//prevent double dip deletes
       {
     	  set.delete(this, recompute);
        	  clear();
        }
       return inSet;
    }
    
    /**
     * Callback that happens at the end of a delete, and when an element gets pruned.
     * This implementation is empty. Override to provide custom behaviors.
     */
    public void deleteHook()
    {
 	   
    }
    
    /**
	 * A callback method for when an element is inserted into a floatWeightSet.
	 * 
	 * This implementation is empty. Override to provide custom behaviors.
	 */
    public void insertHook()
    {
 	   
    }
    
    /**
     * Only for use by FloatWeightSet.clear(), and delete.
     * This is used to reset state of this, in cases when the state
     * of the FloatWeightSet we were part of is being reset some other way,
     * or if it is being discarded.
     *
     */
    public void clear()
    {
 	   deleteHook();
 	   index	= NOT_A_MEMBER;
 	   set	= null;
    }
    
    /**
     * A synchronized version of clear(), for use in FloatWeightSet.prune().
     *
     */
    synchronized void clearSynch()
    {
       clear();
    }
 /**
  * Change the weight of the element, without propogating the new
  * weight into the data structure. The result of the change will be
  * propogated later by some other operation (such as delete) which
  * performs a recompute
  */
    public void delaySetWeight(float newWeight)
    {
       weight	= newWeight;
    }
 /**
  * Set the weight slot.
  * 
  * Doesn't effect the set if no longer a member.
  */
    public void setWeight(float newWeight)
    {
       weight	= newWeight;
    }
   public String toString()
   {
      return super.toString() + " " + getIndex() + ": " + weight;
   }
 /**
  * Free resources associated w this element.
  */   
    public boolean recycle()
    {
       return true;
    }
    /**
     * The set object that this element is part of, or null if its not a member of any set.
     * 
     * @return
     */
 	protected BasicFloatSet set()
 	{
 		return set;
 	}
 }
