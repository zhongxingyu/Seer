 package com.kpro.algorithm;
 
 import java.util.ArrayList;
 
 import com.kpro.dataobjects.Action;
 import com.kpro.dataobjects.PolicyObject;
 
 /**
  * a very simple conclusion class. result is based on the closest object only.
  * 
  * @author ngerstle
  * @version 29.09.11.1
  */
 public class Conclusion_Simple extends ConclusionAlgorithm {
 
 	private DistanceMetric distanceMetric; // distance metric to use for choosing
 	
 	
 	/**
 	 * a simple conclusion that just bases the result on the inverse distance of the policies,
 	 * and returns as a confidence the sum of inverse distances
 	 * 
 	 * @param knearestns
 	 * @return the action to take
 	 */
 	public Conclusion_Simple(DistanceMetric distanceMetric)
 	{
 		this.distanceMetric = distanceMetric;
 	}
 	
 	/**
 	 * makes a decision on the reduced set
 	 * 
 	 * @author ngerstle
 	 * 
 	 * @param np the object under consideration
 	 * @param releventSet the reduced set of neighbors
 	 * @return an arraylist of {Action a, double Confidence) 
 	 *
 	 */
 	@Override
 	public Action conclude(PolicyObject np, Iterable<PolicyObject> releventSet)
 	{
 		ArrayList<String> approveList = new ArrayList<String>();
 		ArrayList<String> rejectList = new ArrayList<String>();
 		double appdistance = 0; //the sum of the inverse distance to all approved PolicyObjects
 		double rejdistance = 0; //the sum of the inverse distance to all rejected PolicyObjects
 		
 		for( PolicyObject i : releventSet)
 		{
 			if((i.getAction() != null)&&(i.getAction().getAccepted()))
 			{
 				approveList.add(i.getContextDomain());
 				appdistance+=(1/distanceMetric.getTotalDistance(np, i));
 			}
 			else
 			{
 				rejectList.add(i.getContextDomain());
 				rejdistance+=(1/distanceMetric.getTotalDistance(np, i));				
 			}
 		}
 		if(appdistance > rejdistance)
 		{
			return new Action(true, approveList, appdistance, false);
 		}
 		else
 		{
			return new Action(true, rejectList, rejdistance, false);
 		}
 	}
 
 }
