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
 
package org.openmrs.module.sdmxhdintegration.db.hibernate;
 
 import java.util.List;
 
 import org.hibernate.ObjectDeletedException;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.sdmxhdintegration.SDMXHDMessage;
 import org.openmrs.module.sdmxhdintegration.SDMXHDService;
 import org.openmrs.module.sdmxhdintegration.db.hibernate.HibernateSDMXHDMessageDAO;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.openmrs.test.Verifies;
 
/**
 * Tests for module DAO implementation
 */
 public class HibernateSDMXHDMessageDAOTest extends BaseModuleContextSensitiveTest {
 	
 	public Integer testMsg1Id;
 	public Integer testMsg2Id;
 	
 	@Before
 	public void runBeforeAllTests() throws Exception {
		executeDataSet("IndicatorTest.xml");
 		SDMXHDService sdmxhdService = (SDMXHDService) Context.getService(SDMXHDService.class);
 		
 		SDMXHDMessage sdmxhdMessage = new SDMXHDMessage();
 		
 		sdmxhdMessage.setName("test1");
 		sdmxhdMessage.setDescription("test1");
		sdmxhdMessage.setSdmxhdZipFileName("SDMX-HD.v1.0 sample1.zip");
 		
 		SDMXHDMessage saveSDMXHDMessage = sdmxhdService.saveSDMXHDMessage(sdmxhdMessage);
 		testMsg1Id = saveSDMXHDMessage.getId();
 		
 		sdmxhdMessage = new SDMXHDMessage();
 		
 		sdmxhdMessage.setName("test2");
 		sdmxhdMessage.setDescription("test2");
		sdmxhdMessage.setSdmxhdZipFileName("SDMX-HD.v1.0 sample2.zip");
 		
 		saveSDMXHDMessage = sdmxhdService.saveSDMXHDMessage(sdmxhdMessage);
 		testMsg2Id = saveSDMXHDMessage.getId();
 	}
 	
 	/**
 	 * @see {@link HibernateSDMXHDMessageDAO#deleteSDMXHDMessage(SDMXHDMessage)}
 	 * 
 	 */
 	@Test
 	@Verifies(value = "should void the sdmx message with the given id", method = "deleteSDMXHDMessage(SDMXHDMessage)")
 	public void deleteSDMXHDMessage_shouldDeleveTheSdmxMessageWithTheGivenId() throws Exception {
 		SDMXHDMessage sdmxhdMessage = new SDMXHDMessage();
 		
 		sdmxhdMessage.setName("deltest");
 		sdmxhdMessage.setDescription("deltest");
		sdmxhdMessage.setSdmxhdZipFileName("SDMX-HD.v1.0 sample1.zip");
 		
 		SDMXHDService sdmxhdService = (SDMXHDService) Context.getService(SDMXHDService.class);
 		SDMXHDMessage savedSDMXHDMessage = sdmxhdService.saveSDMXHDMessage(sdmxhdMessage);
 		Integer id = savedSDMXHDMessage.getId();
 		SDMXHDMessage msg = sdmxhdService.getSDMXHDMessage(id);
 		sdmxhdService.purgeSDMXHDMessage(msg);
 		try {
 			msg = sdmxhdService.getSDMXHDMessage(id);
 		} catch (ObjectDeletedException e) {
 			//Test passed!
 			return;
 		}
 		
 		//Otherwise fail
 		Assert.fail();
 	}
 	
 	/**
 	 * @see {@link HibernateSDMXHDMessageDAO#getAllSDMXHDMessages()}
 	 * 
 	 */
 	@Test
 	@Verifies(value = "should return all sdmx messages", method = "getAllSDMXHDMessages()")
 	public void getAllSDMXHDMessages_shouldReturnAllSdmxMessages() throws Exception {
 		SDMXHDService sdmxhdService = (SDMXHDService) Context.getService(SDMXHDService.class);
 		List<SDMXHDMessage> messages = sdmxhdService.getAllSDMXHDMessages(false);
 		
 		Assert.assertEquals(2, messages.size());
 	}
 	
 	/**
 	 * @see {@link HibernateSDMXHDMessageDAO#getSDMXHDMessage(Integer)}
 	 * 
 	 */
 	@Test
 	@Verifies(value = "should get the correct sdmxhd message for the given id", method = "getSDMXHDMessage(Integer)")
 	public void getSDMXHDMessage_shouldGetTheCorrectSdmxhdMessageForTheGivenId() throws Exception {
 		SDMXHDService sdmxhdService = (SDMXHDService) Context.getService(SDMXHDService.class);
 		SDMXHDMessage msg = sdmxhdService.getSDMXHDMessage(testMsg1Id);
 		
 		Assert.assertNotNull(msg);
 		Assert.assertEquals("test1", msg.getName());
 	}
 	
 	/**
 	 * @see {@link HibernateSDMXHDMessageDAO#saveSDMXHDMessage(SDMXHDMessage)}
 	 * 
 	 */
 	@Test
 	@Verifies(value = "should save the given sdmxhd message", method = "saveSDMXHDMessage(SDMXHDMessage)")
 	public void saveSDMXHDMessage_shouldSaveTheGivenSdmxhdMessage() throws Exception {
 		SDMXHDMessage sdmxhdMessage = new SDMXHDMessage();
 		
 		sdmxhdMessage.setName("test3");
 		sdmxhdMessage.setDescription("test3");
		sdmxhdMessage.setSdmxhdZipFileName("SDMX-HD.v1.0 sample1.zip");
 		
 		SDMXHDService sdmxhdService = (SDMXHDService) Context.getService(SDMXHDService.class);
 		SDMXHDMessage savedSDMXHDMessage = sdmxhdService.saveSDMXHDMessage(sdmxhdMessage);
 		
 		Assert.assertNotNull(savedSDMXHDMessage);
 	}
 }
