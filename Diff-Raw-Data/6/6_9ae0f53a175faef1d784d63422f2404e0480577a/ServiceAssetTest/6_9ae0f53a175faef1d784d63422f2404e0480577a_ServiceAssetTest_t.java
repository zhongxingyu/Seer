 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 
 package org.ebayopensource.turmeric.repository.wso2;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assume.assumeTrue;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.wso2.carbon.registry.app.RemoteRegistry;
 import org.wso2.carbon.registry.core.Resource;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.repository.v2.services.Artifact;
 import org.ebayopensource.turmeric.repository.v2.services.ArtifactInfo;
 import org.ebayopensource.turmeric.repository.v2.services.ArtifactValueType;
 import org.ebayopensource.turmeric.repository.v2.services.AssetInfo;
 import org.ebayopensource.turmeric.repository.v2.services.AssetKey;
 import org.ebayopensource.turmeric.repository.v2.services.AssetLifeCycleInfo;
 import org.ebayopensource.turmeric.repository.v2.services.AttributeNameValue;
 import org.ebayopensource.turmeric.repository.v2.services.BasicAssetInfo;
 import org.ebayopensource.turmeric.repository.v2.services.CreateCompleteAssetRequest;
 import org.ebayopensource.turmeric.repository.v2.services.CreateCompleteAssetResponse;
 import org.ebayopensource.turmeric.repository.v2.services.ExtendedAssetInfo;
 import org.ebayopensource.turmeric.repository.v2.services.GetAssetInfoRequest;
 import org.ebayopensource.turmeric.repository.v2.services.GetAssetInfoResponse;
 import org.ebayopensource.turmeric.repository.v2.services.Library;
 import org.ebayopensource.turmeric.repository.v2.services.LockAssetRequest;
 import org.ebayopensource.turmeric.repository.v2.services.LockAssetResponse;
 import org.ebayopensource.turmeric.repository.v2.services.UpdateAssetRequest;
 import org.ebayopensource.turmeric.repository.v2.services.UpdateAssetResponse;
 
 /**
  */
 public class ServiceAssetTest extends Wso2Base {
     // First resource path must be the primary resource created by the test
     // in order for the assumption checks to work correctly.
     private static final String[] resources = {
            "/_system/governance/trunk/services/com/domain/www/services/ServiceAssetCompleteTest",
            "/_system/governance/trunk/services/com/domain/www/services/UpdatedServiceAssetCompleteTest",
            "/_system/governance/trunk/endpoints/com/domain/www/ep-ServiceAssetCompleteTest", };
 
     private static final String assetType = "Service";
     private static final String assetName = "ServiceAssetCompleteTest";
     private static final String assetDesc = "ServiceAssetCompleteTest description";
     private static final String libraryName = "http://www.domain.com/services";
     private static final String baseUrl = "http://www.domain.com/services/";
     private static final String stringProperty = "string property";
     private static final Long longProperty = new Long(1234567l);
     private static final Boolean booleanProperty = Boolean.FALSE;
     private static final String version = "1.0.0";
 
     @Before
     public void checkRepository() {
         try {
             RemoteRegistry wso2 = RSProviderUtil.getRegistry();
 
             for (String resource : resources) {
                 if (wso2.resourceExists(resource)) {
                     wso2.delete(resource);
                 }
             }
         }
         catch (Exception ex) {
         }
     }
 
     private CreateCompleteAssetResponse createCompleteAsset() throws Exception {
         AssetKey key = new AssetKey();
         Library lib = new Library();
         lib.setLibraryName(libraryName);
         key.setLibrary(lib);
         key.setAssetName(assetName);
 
         BasicAssetInfo basicInfo = new BasicAssetInfo();
         basicInfo.setAssetKey(key);
         basicInfo.setAssetName(assetName);
         basicInfo.setAssetDescription(assetDesc);
         basicInfo.setAssetType(assetType);
         basicInfo.setVersion(version);
         
         ExtendedAssetInfo extendedInfo = new ExtendedAssetInfo();
         List<AttributeNameValue> attrs = extendedInfo.getAttribute();
         attrs.add(RSProviderUtil.newAttribute("namespace", libraryName));
         attrs.add(RSProviderUtil.newAttribute("stringProperty", stringProperty));
         attrs.add(RSProviderUtil.newAttribute("longProperty", longProperty.longValue()));
         attrs.add(RSProviderUtil.newAttribute("booleanProperty", booleanProperty.booleanValue()));
 
         AssetLifeCycleInfo lifeCycleInfo = new AssetLifeCycleInfo();
         lifeCycleInfo.setDomainOwner("John Doe");
         lifeCycleInfo.setDomainType("Technical Owner");
 
         AssetInfo assetInfo = new AssetInfo();
         assetInfo.setBasicAssetInfo(basicInfo);
         assetInfo.setExtendedAssetInfo(extendedInfo);
         assetInfo.setAssetLifeCycleInfo(lifeCycleInfo);
 
         Artifact artifact = new Artifact();
         artifact.setArtifactName("ep-" + assetName);
         artifact.setArtifactCategory("Endpoint");
         artifact.setArtifactValueType(ArtifactValueType.URL);
 
         String endpointUrl = baseUrl + assetName;
         ArtifactInfo endpointInfo = new ArtifactInfo();
         endpointInfo.setArtifact(artifact);
         endpointInfo.setArtifactDetail(endpointUrl.getBytes("UTF-8"));
 
         List<ArtifactInfo> artifactList = assetInfo.getArtifactInfo();
         artifactList.add(endpointInfo);
 
         CreateCompleteAssetRequest request = new CreateCompleteAssetRequest();
         request.setAssetInfo(assetInfo);
 
         RepositoryServiceProviderImpl provider = new RepositoryServiceProviderImpl();
         CreateCompleteAssetResponse response = provider.createCompleteAsset(request);
         
         assertEquals(AckValue.SUCCESS, response.getAck());
         assertEquals(null, response.getErrorMessage());
         
         return response;
     }
 
     public UpdateAssetResponse updateAsset() throws Exception {
         AssetKey key = new AssetKey();
         key.setAssetId(resources[0]);
 
         LockAssetRequest lockReq = new LockAssetRequest();
         lockReq.setAssetKey(key);
 
         RepositoryServiceProviderImpl provider = new RepositoryServiceProviderImpl();
         LockAssetResponse lockRes = provider.lockAsset(lockReq);
         assertEquals(AckValue.SUCCESS, lockRes.getAck());
 
         BasicAssetInfo basicInfo = new BasicAssetInfo();
         basicInfo.setAssetKey(key);
         basicInfo.setAssetName("Updated" + assetName);
         basicInfo.setAssetDescription("Updated " + assetDesc);
         basicInfo.setAssetType(assetType);
 
         UpdateAssetRequest request = new UpdateAssetRequest();
         request.setBasicAssetInfo(basicInfo);
 
         UpdateAssetResponse response = provider.updateAsset(request);
 
         assertEquals(AckValue.SUCCESS, response.getAck());
         assertEquals(null, response.getErrorMessage());
         
         return response;
     }
 
     @Test
     public void testCreate() throws Exception {
         boolean clean = false;
         try {
             RemoteRegistry wso2 = RSProviderUtil.getRegistry();
             clean = !wso2.resourceExists(resources[0]);
         }
         catch (Exception ce) {
         }
         assumeTrue(clean);
 
         CreateCompleteAssetResponse response = createCompleteAsset();
 
         // Retrieve the resource to check property values
         RemoteRegistry wso2 = RSProviderUtil.getRegistry();
         Resource resource = wso2.get(resources[0]);
         assertEquals(stringProperty, resource.getProperty("stringProperty"));
         assertEquals(longProperty.toString(), resource.getProperty("longProperty"));
         assertEquals(booleanProperty.toString(), resource.getProperty("booleanProperty"));
         validateAssetKey(response.getAssetKey());
     }
 
     @Test
     public void testGet() throws Exception {
         boolean clean = false;
         try {
             RemoteRegistry wso2 = RSProviderUtil.getRegistry();
             clean = !wso2.resourceExists(resources[0]);
         }
         catch (Exception ce) {
         }
         assumeTrue(clean);
 
         CreateCompleteAssetResponse created = createCompleteAsset();
         assertEquals(AckValue.SUCCESS, created.getAck());
 
         AssetKey key = new AssetKey();
         key.setAssetId(created.getAssetKey().getAssetId());
 
         GetAssetInfoRequest request = new GetAssetInfoRequest();
         request.setAssetKey(key);
         RepositoryServiceProviderImpl provider = new RepositoryServiceProviderImpl();
         GetAssetInfoResponse got = provider.getAssetInfo(request);
         assertEquals(AckValue.SUCCESS, got.getAck());
 
         AssetInfo assetInfo = got.getAssetInfo();
         BasicAssetInfo basicInfo = assetInfo.getBasicAssetInfo();
 
         assertEquals(key.getAssetId(), basicInfo.getAssetKey().getAssetId());
         assertEquals(assetType, basicInfo.getAssetType());
         assertEquals(assetDesc, basicInfo.getAssetDescription());
 
         assertEquals(1, assetInfo.getArtifactInfo().size());
         assertEquals("ep-" + assetName, assetInfo.getArtifactInfo().get(0).getArtifact()
                         .getArtifactName());
         validateAssetInfo(got.getAssetInfo());
     }
 
     @Test
     public void testBasicUpdate() throws Exception {
         boolean clean = false;
         try {
             RemoteRegistry wso2 = RSProviderUtil.getRegistry();
             clean = !wso2.resourceExists(resources[0]);
         }
         catch (Exception e) {
         }
         assumeTrue(clean);
 
         createCompleteAsset();
 
         UpdateAssetResponse updated = updateAsset();
         validateAssetInfo(updated.getAssetInfo());
         
         AssetKey key = new AssetKey();
         key.setAssetId(updated.getAssetInfo().getBasicAssetInfo().getAssetKey().getAssetId());
 
         GetAssetInfoRequest request = new GetAssetInfoRequest();
         request.setAssetKey(key);
         
         RepositoryServiceProviderImpl provider = new RepositoryServiceProviderImpl();
         GetAssetInfoResponse response = provider.getAssetInfo(request);
         assertEquals(AckValue.SUCCESS, response.getAck());
 
         AssetInfo assetInfo = response.getAssetInfo();
         BasicAssetInfo basicInfo = assetInfo.getBasicAssetInfo();
 
         assertEquals(key.getAssetId(), basicInfo.getAssetKey().getAssetId());
         assertEquals(assetType, basicInfo.getAssetType());
         assertEquals("Updated " + assetDesc, basicInfo.getAssetDescription());
         
         validateAssetInfo(response.getAssetInfo());
     }
 
 }
