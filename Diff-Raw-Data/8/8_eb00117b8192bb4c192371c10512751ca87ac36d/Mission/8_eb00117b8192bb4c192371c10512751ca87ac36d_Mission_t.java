 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 /**
  * A Mission represents the set of Monsters to be defeated
  * in order to complete a particular level.
  */
 public class Mission
 {
 	/** * Has the mission been completed? */
 	private boolean		mCompleted;
 	
 	/** * What kind of monster must be defeated? */
 	private Monster		mTarget;
 	
 	/** * How many of these monsters must be defeated? */
 	private int			mQuota;
 	
	public int getQuota()
	{
		return mQuota;
	}
	
 	public void adjustQuota(Thing theVictim)
 	{
 		if (theVictim.getName().equals(mTarget.getName()))
 		{
 			mQuota--;
 			JCavernApplet.log("You mission is now " + this);			
 		}
 	}
 	
 	public String toString()
 	{
 		if (mQuota > 0)
 		{
			return "to kill " + mQuota + " " + mTarget.getName() + "s";
 		}
 		else
 		{
 			return "completed";
 		}
 	}
 	
 	public Mission(Monster target, int quota)
 	{
 		mTarget = target;
 		mQuota = quota;
 	}
 }
