 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.chartsearch;
 
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.chartsearch.solr.ChartSearchIndexer;
 import org.openmrs.module.chartsearch.solr.SolrManagement;
 
 /**
  * This class contains the logic that is run every time this module is either started or stopped. * 
  */
 
 public class ChartSearchActivator extends BaseModuleActivator{
 	
 	protected Log log = LogFactory.getLog(getClass());	
 	/**
 	 * @see BaseModuleActivator#willRefreshContext()
 	 */
 	public void willRefreshContext() {
 		log.info("Refreshing Chart Search Module");
 	}
 	
 	/**
 	 * @see BaseModuleActivator#contextRefreshed()
 	 */
 	public void contextRefreshed() {
 		log.info("Chart Search Module refreshed");
 	}
 	
 	/**
 	 * @see BaseModuleActivator#willStart()
 	 */
 	public void willStart() {
 		log.info("Starting Chart Search Module");
 	}
 	
 	/**
 	 * @see BaseModuleActivator#started()
 	 */
 	public void started() {	
		log.info("Chart Search Module started");	
		ChartSearchIndexer chartSearchIndexer = getComponent(ChartSearchIndexer.class);
		chartSearchIndexer.getStatistics();
 	}
 	
 	/**
 	 * @see BaseModuleActivator#willStop()
 	 */
 	public void willStop() {
 		log.info("Stopping Chart Search Module");
 	}
 	
 	/**
 	 * @see BaseModuleActivator#stopped()
 	 */
 	public void stopped() {
 		log.info("Chart Search Module stopped");
 		SolrManagement solrManagement = getComponent(SolrManagement.class);
 		solrManagement.shutdown();
 	}
 	
 	private <T> T getComponent(Class<T> clazz) {
 		List<T> list = Context.getRegisteredComponents(clazz);
 		if (list == null || list.size() == 0)
 			throw new RuntimeException("Cannot find component of " + clazz);
 		return list.get(0);
 	}
 	
 		
 }
