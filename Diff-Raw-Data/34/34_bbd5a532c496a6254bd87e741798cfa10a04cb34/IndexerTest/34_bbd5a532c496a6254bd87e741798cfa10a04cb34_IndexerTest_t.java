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
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
import org.apache.commons.lang.StringUtils;
 import org.apache.solr.handler.dataimport.custom.IndexClearStrategies;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.chartsearch.server.ConfigCommands;
 import org.openmrs.module.chartsearch.server.PatientInfo;
 import org.openmrs.module.chartsearch.solr.ChartSearchIndexer;
 import org.openmrs.module.chartsearch.solr.ChartSearchSearcher;
 import org.openmrs.module.chartsearch.solr.SolrManagement;
 import org.openmrs.module.chartsearch.solr.SolrSearch;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 
 /**
  *
  */
 public class IndexerTest extends BaseModuleContextSensitiveTest{
 
 	
 	SolrSearch solr;
 	
 	/**
 	 * Test method for {@link org.openmrs.module.chartsearch.solr.ChartSearchIndexer#indexPatientData(java.lang.Integer)}.
 	 */
 	@Test
 	public void testIndexPatiendData() {

 		int patientID = 14;
 		solr.initiateServer();
 		solr.indexPatientData(patientID);
		solr.search(patientID, "111", 0, 10);
		
 		PatientInfo pi = solr.getPatientInfo(patientID);
 		assertNotNull(pi);
 		assertEquals(pi.getPatientId().intValue(), patientID);
 		try {
 			List<ChartListItem> obs = solr.search(patientID, "blood", 0, 10);
 			for (ChartListItem item : obs) {
 				System.out.println(item.getConceptName());
 			}
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 				
 	}
 	
 	@Before
     public void setUp() {
 		solr = new SolrSearch();
     }
  
     @After
     public void tearDown() {
         SolrManagement  management = new SolrManagement();
         management.shutdown();
     }
 	
 
 
 }
