 /*******************************************************************************
  * Copyright (c) 20 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 
 package org.ebayopensource.turmeric.repository.wso2.assets;
 
 import static org.junit.Assert.*;
 
 
 import org.junit.Before;
 import org.junit.Test;
 import org.wso2.carbon.governance.api.services.ServiceManager;
 import org.wso2.carbon.governance.api.services.dataobjects.Service;
 import org.wso2.carbon.registry.core.Registry;
 
 import org.ebayopensource.turmeric.repository.v2.services.*;
 import org.ebayopensource.turmeric.repository.wso2.RSProviderUtil;
 import org.ebayopensource.turmeric.repository.wso2.Wso2Base;
 
 public class ServiceAssetTest extends Wso2Base {
 	
 	private ServiceAsset service = null;
 	private Registry registry = null;
 	
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		registry = RSProviderUtil.getRegistry();
 		
 		BasicAssetInfo basicInfo = new BasicAssetInfo();
 		basicInfo.setAssetName("TestService");
 		basicInfo.setAssetType("Service");
 		basicInfo.setNamespace("http://www.example.org");
 		basicInfo.setGroupName("TestGroup");
 		basicInfo.setAssetDescription("A short description.");
 		basicInfo.setVersion("1.0.0");
 		basicInfo.setAssetLongDescription("A longer description than the short description");
 		AssetKey assetKey = new AssetKey();
 		assetKey.setAssetName(basicInfo.getAssetName());
 		assetKey.setType(basicInfo.getAssetType());
 		assetKey.setVersion(basicInfo.getVersion());
 		service = new ServiceAsset(basicInfo, registry);
 		
 	}
 
     @Test
     public void testCreateService() throws Exception {
     	assertTrue(service.createAsset());
    	service.createAsset();
     	assertTrue(service.addAsset());
     	assertNotNull(service.getId());
     	assertNotNull(service.getGovernanceArtifact());
     }
     
     @Test
     public void testLock() throws Exception {
     	service.createAsset();
     	String assetId = service.getId();
     	
     	service.lockAsset();
     	service.save();
     	
     	ServiceManager serviceManager = new ServiceManager(registry);
     	Service updatedService = serviceManager.getService(assetId);
     	assertEquals("true",updatedService.getAttribute(AssetConstants.TURMERIC_LOCK));
     }
     
     @Test
     public void testunLock() throws Exception {
     	service.createAsset();
     	String assetId = service.getId();
     	
     	service.unlock();
     	service.save();
     	
     	ServiceManager serviceManager = new ServiceManager(registry);
     	Service updatedService = serviceManager.getService(assetId);
     	assertEquals("false",updatedService.getAttribute(AssetConstants.TURMERIC_LOCK));
     }
     
         	    	
 }
