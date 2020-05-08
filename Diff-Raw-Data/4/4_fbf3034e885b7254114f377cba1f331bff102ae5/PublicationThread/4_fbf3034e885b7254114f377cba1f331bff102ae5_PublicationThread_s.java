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
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 /**
  * @author mattias
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class PublicationThread extends Thread
 {
 
     public final static Logger logger = Logger.getLogger(PublicationThread.class.getName());
 
 	public synchronized void run() 
 	{
         logger.info("setting block");
 	    RequestAnalyser.setBlockRequests(true);
 
 		try
 		{
 		    int publicationDelay = 5000;
 		    String publicationThreadDelay = CmsPropertyHandler.getProperty("publicationThreadDelay");
		    if(publicationThreadDelay != null && publicationThreadDelay.equalsIgnoreCase("") && publicationThreadDelay.indexOf("publicationThreadDelay") == -1)
 		        publicationDelay = Integer.parseInt(publicationThreadDelay);
 		    
 			sleep(publicationDelay);
 		
 		    logger.info("\n\n\nUpdating all caches as this was a publishing-update\n\n\n");
 			CacheController.clearCastorCaches();
 
 			logger.info("\n\n\nclearing all except page cache as we are in publish mode..\n\n\n");											
 		    CacheController.clearCaches(null, null, new String[] {"pageCache", "NavigationCache", "pagePathCache", "userCache", "pageCacheParentSiteNodeCache", "pageCacheLatestSiteNodeVersions", "pageCacheSiteNodeTypeDefinition"});
 		    
 			logger.info("\n\n\nRecaching all caches as this was a publishing-update\n\n\n");
 			CacheController.cacheCentralCastorCaches();
 
 			logger.info("\n\n\nFinally clearing page cache as this was a publishing-update\n\n\n");
 		    CacheController.clearCache("pageCache");
 		} 
 		catch (Exception e)
 		{
 		    logger.error("An error occurred in the PublicationThread:" + e.getMessage(), e);
 		}
 
 		logger.info("released block");
 		RequestAnalyser.setBlockRequests(false);
 
 	}
 }
