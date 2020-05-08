 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 package org.infoglue.deliver.util;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * @author mattias
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class Counter
 {
     private static Integer count = new Integer(0);
     private static Integer activeCount = new Integer(0);
     private static Integer totalCount = new Integer(0);
     private static Long totalElapsedTime = new Long(0);
     private static Long maxElapsedTime = new Long(0);
     private static Map allComponentsStatistics = new HashMap();
     private static LinkedBlockingQueue latestPublications = new LinkedBlockingQueue(5);
     
     private Counter(){}
 
     static int getNumberOfCurrentRequests()
     {
         return count.intValue();
     }
 
     static int getNumberOfActiveRequests()
     {
         return activeCount.intValue();
     }
 
     static int getTotalNumberOfRequests()
     {
         return totalCount.intValue();
     }
 
     static long getAverageElapsedTime()
     {
     	if(totalElapsedTime != null && totalCount.intValue() != 0)
     		return totalElapsedTime.longValue() / totalCount.intValue();
     	else
     		return 0;
     }
 
     static long getMaxElapsedTime()
     {
         return maxElapsedTime.longValue();
     }
 
     static List getLatestPublications()
     {
     	List latestPublicationsList = new ArrayList();
     	synchronized (latestPublications)
 		{
     		Iterator latestPublicationsIterator = latestPublications.iterator();
     		while(latestPublicationsIterator.hasNext())
     		{
     			latestPublicationsList.add(latestPublicationsIterator.next());
     		}
 		}
         return latestPublicationsList;
     }
 
     synchronized static void addPublication(Date publicationDate)
     {
     	synchronized (latestPublications)
 		{
     		if(latestPublications.remainingCapacity() == 0)
     			latestPublications.poll();
     		
     		latestPublications.add(publicationDate);
 		}
     }
 
     synchronized static void incNumberOfCurrentRequests(boolean active)
     {
         count = new Integer(count.intValue() + 1);
         if(active)
         	activeCount = new Integer(activeCount.intValue() + 1);
     }
 
     synchronized static void decNumberOfCurrentRequests(long elapsedTime)
     {
     	if(count > 0)
     		count = new Integer(count.intValue() - 1);
         
    	if(elapsedTime != -1 && activeCount > 0)
         	activeCount = new Integer(activeCount.intValue() - 1);
     		
     	totalCount = new Integer(totalCount.intValue() + 1);
 
         if(elapsedTime != -1)
         {
 	    	totalElapsedTime = new Long(totalElapsedTime.longValue() + elapsedTime);
 	    	if(elapsedTime > maxElapsedTime.longValue())
 	    		maxElapsedTime = new Long(elapsedTime);
         }
     }
 
     synchronized static Set getAllComponentNames()
     {
     	synchronized (allComponentsStatistics) 
     	{
     		return allComponentsStatistics.keySet();
 		}
     }
 
     synchronized private static Map getComponentStatistics(String componentName)
     {
     	Map componentStatistics = (Map)allComponentsStatistics.get(componentName);
     	if(componentStatistics == null)
     	{
     		componentStatistics = new HashMap();
     		componentStatistics.put("totalElapsedTime", new Long(0));
     		componentStatistics.put("totalNumberOfInvokations", new Integer(0));
     		allComponentsStatistics.put(componentName, componentStatistics);
         }
     	
     	return componentStatistics;
     }
 
     synchronized static void registerComponentStatistics(String componentName, long elapsedTime)
     {
     	Map componentStatistics = getComponentStatistics(componentName);   
     	synchronized (componentStatistics) 
     	{
         	Long oldTotalElapsedTime = (Long)componentStatistics.get("totalElapsedTime");
         	Long totalElapsedTime = new Long(oldTotalElapsedTime.longValue() + elapsedTime);			
         	componentStatistics.put("totalElapsedTime", totalElapsedTime);
 
         	Integer oldTotalNumberOfInvokations = (Integer)componentStatistics.get("totalNumberOfInvokations");
         	Integer totalNumberOfInvokations = new Integer(oldTotalNumberOfInvokations.intValue() + 1);			
         	componentStatistics.put("totalNumberOfInvokations", totalNumberOfInvokations);
     	}    	
     }
 
     static long getAverageElapsedTime(String componentName)
     {
     	Map componentStatistics = getComponentStatistics(componentName);
     	synchronized (componentStatistics) 
     	{
         	Long totalElapsedTime = (Long)componentStatistics.get("totalElapsedTime");
         	Integer oldTotalNumberOfInvokations = (Integer)componentStatistics.get("totalNumberOfInvokations");
  
         	return totalElapsedTime.longValue() / oldTotalNumberOfInvokations.intValue();			
 		}
     }
 
     static int getNumberOfHits(String componentName)
     {
     	Map componentStatistics = getComponentStatistics(componentName);
     	synchronized (componentStatistics) 
     	{
         	return ((Integer)componentStatistics.get("totalNumberOfInvokations")).intValue();
  		}
     }
 
     static void resetComponentStatistics()
     {
     	synchronized (allComponentsStatistics) 
     	{
     		allComponentsStatistics.clear();
     	}
    	}
 
 }
