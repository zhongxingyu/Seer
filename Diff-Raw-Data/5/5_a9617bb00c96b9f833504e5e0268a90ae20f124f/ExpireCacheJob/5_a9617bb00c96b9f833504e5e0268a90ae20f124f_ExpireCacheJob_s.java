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
 package org.infoglue.deliver.jobs;
 
 import java.io.File;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.RequestAnalyser;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 /**
  * @author mattias
  *
  * This jobs searches for expiring contents or sitenodes and clears caches if found.
  */
 
 public class ExpireCacheJob implements Job
 {
     private final static Logger logger = Logger.getLogger(ExpireCacheJob.class.getName());
     private static Integer intervalCount = 0;
     
     public synchronized void execute(JobExecutionContext context) throws JobExecutionException
     {
         try
         {
             CacheController.evictWaitingCache();
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
 
         logger.info("---" + context.getJobDetail().getFullName() + " executing.[" + new Date() + "]");
 
         try
         {
             Date firstExpireDateTime = CacheController.expireDateTime;
             logger.info("firstExpireDateTime:" + firstExpireDateTime);
             Date now = new Date();
             
             if(firstExpireDateTime != null && now.after(firstExpireDateTime))
             {
                 logger.info("setting block");
                 synchronized(RequestAnalyser.getRequestAnalyser()) 
         	    {
         	       	if(RequestAnalyser.getRequestAnalyser().getBlockRequests())
         		    {
         			    logger.warn("evictWaitingCache allready in progress - returning to avoid conflict");
         		        return;
         		    }
 
         	       	RequestAnalyser.getRequestAnalyser().setBlockRequests(true);
         		}
 
 				try
                 {
 	        	    String operatingMode = CmsPropertyHandler.getOperatingMode();
 	        	    if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
 	        	    {
 	        	        logger.info("Updating all caches as this was a publishing-update");
 		    			CacheController.clearCastorCaches();
 		
 		    			logger.info("clearing all except page cache as we are in publish mode..");
 		    		    CacheController.clearCaches(null, null, new String[] {"pageCache", "pageCacheExtra", "NavigationCache", "pagePathCache", "userCache", "pageCacheParentSiteNodeCache", "pageCacheLatestSiteNodeVersions", "pageCacheSiteNodeTypeDefinition"});
 		    			
 		    			logger.info("Recaching all caches as this was a publishing-update");
 		    			CacheController.cacheCentralCastorCaches();
 		    			
 		    			logger.info("Finally clearing page cache as this was a publishing-update");
 		    		    CacheController.clearCache("pageCache");
 		    		    CacheController.clearCache("pageCacheExtra");
 	        	    }
 	        	    else
 	        	    {
 		    		    logger.info("Updating all caches as this was a publishing-update");
 		    			CacheController.clearCastorCaches();
 		
 		    			logger.info("clearing all except page cache as we are in publish mode..");
 		    		    CacheController.clearCaches(null, null, null);
 	        	    }
                 }
                 catch(Exception e)
                 {
                     logger.error("An error occurred when we tried to update cache:" + e.getMessage(), e);
                 }
     		    
     		    logger.info("releasing block");
     		    RequestAnalyser.getRequestAnalyser().setBlockRequests(false);
             }
 
             Date firstPublishDateTime = CacheController.publishDateTime;
             logger.info("firstPublishDateTime:" + firstPublishDateTime);
             
             if(firstPublishDateTime != null && now.after(firstPublishDateTime))
             {
                 logger.info("setting block");
                 synchronized(RequestAnalyser.getRequestAnalyser()) 
         	    {
         	       	if(RequestAnalyser.getRequestAnalyser().getBlockRequests())
         		    {
         			    logger.warn("evictWaitingCache allready in progress - returning to avoid conflict");
         		        return;
         		    }
 
         	       	RequestAnalyser.getRequestAnalyser().setBlockRequests(true);
         		}
                 
                 try
                 {
 	        	    String operatingMode = CmsPropertyHandler.getOperatingMode();
 	        	    if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
 	        	    {
 	        	        logger.info("Updating all caches as this was a publishing-update");
 		    			CacheController.clearCastorCaches();
 		
 		    			logger.info("clearing all except page cache as we are in publish mode..");
 		    		    CacheController.clearCaches(null, null, new String[] {"pageCache", "pageCacheExtra", "NavigationCache", "pagePathCache", "userCache", "pageCacheParentSiteNodeCache", "pageCacheLatestSiteNodeVersions", "pageCacheSiteNodeTypeDefinition"});
 		    			
 		    			logger.info("Recaching all caches as this was a publishing-update");
 		    			CacheController.cacheCentralCastorCaches();
 		    			
 		    			logger.info("Finally clearing page cache as this was a publishing-update");
 		    		    CacheController.clearCache("pageCache");
 		    		    CacheController.clearCache("pageCacheExtra");
 	        	    }
 	        	    else
 	        	    {
 		    		    logger.info("Updating all caches as this was a publishing-update");
 		    			CacheController.clearCastorCaches();
 		
 		    			logger.info("clearing all except page cache as we are in publish mode..");
 		    		    CacheController.clearCaches(null, null, null);
 	        	    }
                 }
                 catch(Exception e)
                 {
                     logger.error("An error occurred when we tried to update cache:" + e.getMessage(), e);
                 }
 
                 logger.info("releasing block");
                 RequestAnalyser.getRequestAnalyser().setBlockRequests(false);
             }
             
             synchronized (intervalCount)
 			{
                 intervalCount++;
                 logger.info("intervalCount:" + intervalCount);
	            if(intervalCount > 2)
 	            {
 	                String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches";
 	                File dirFile = new File(dir);
 	                if(dirFile.exists())
 	                {
 		                File[] subCaches = dirFile.listFiles();
 		                for(int i=0; i<subCaches.length; i++)
 		                {
 		                	File subCacheDir = subCaches[i];
 		                	logger.info("subCacheDir:" + subCacheDir.getName());
 		                	if(subCacheDir.isDirectory())
 		                	{
 			                	File[] cacheFiles = subCacheDir.listFiles();
 			                	for(int j=0; j<cacheFiles.length; j++)
 			                	{
 			                		File cacheFile = cacheFiles[j];
 				                	logger.info("cacheFile:" + cacheFile.getName());
 			                		long lastModified = cacheFile.lastModified();
 			                		long differensInHours = (System.currentTimeMillis() - lastModified) / (60 * 60 * 1000);
			                		if(differensInHours > (24 * 7))
 			                		{
 			                			logger.info("Deleting cached file as it was to old:" + differensInHours);
 			                			cacheFile.delete();
 			                		}
 			                		else
 			                		{
 			                			logger.info("Keeping cached file as it was new:" + differensInHours);
 			                		}
 			                	}
 		                	}			                
 		                }
 	                }
 	                intervalCount = 0;
 	            }
 			}
         }
         catch (Exception e)
         {
             logger.error("An error occurred when we tried to update cache:" + e.getMessage(), e);
         }
     }
     
 
 }
