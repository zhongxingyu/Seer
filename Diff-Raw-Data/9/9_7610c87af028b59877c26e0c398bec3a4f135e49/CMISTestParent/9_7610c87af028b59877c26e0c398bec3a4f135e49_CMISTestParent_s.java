 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.cmis.automation.testcases;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.Document;
 import org.apache.chemistry.opencmis.client.api.Folder;
 import org.apache.chemistry.opencmis.client.api.ItemIterable;
 import org.apache.chemistry.opencmis.client.api.ObjectId;
 import org.apache.chemistry.opencmis.client.api.Policy;
 import org.apache.chemistry.opencmis.client.api.Relationship;
 import org.apache.chemistry.opencmis.commons.data.Ace;
 import org.apache.chemistry.opencmis.commons.data.Acl;
 import org.apache.chemistry.opencmis.commons.data.ContentStream;
 import org.apache.chemistry.opencmis.commons.data.Principal;
 import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
 import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.Timeout;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.module.cmis.VersioningState;
 import org.mule.tck.junit4.FunctionalTestCase;
 import org.mule.transport.NullPayload;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class CMISTestParent extends FunctionalTestCase {
 
 	// Set global timeout of tests to 10minutes
     @Rule
     public Timeout globalTimeout = new Timeout(600000);
 
 	protected static final String[] SPRING_CONFIG_FILES = new String[] { "AutomationSpringBeans.xml" };
 	protected static ApplicationContext context;
 	protected Map<String, Object> testObjects;
 	
 	@Override
 	protected String getConfigResources() {
 		return "automation-test-flows.xml";
 	}
 
 	protected MessageProcessor lookupFlowConstruct(String name) {
 		return (MessageProcessor) muleContext.getRegistry().lookupFlowConstruct(name);
 	}
 	
 	@BeforeClass
 	public static void beforeClass() {
 		context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILES);
 	}
 	
 	/*
 	 * Helper methods below
 	 */
 	
 	protected ObjectId createFolder(String folderName, String parentObjectId) throws Exception {
 		testObjects.put("folderName", folderName);
 		testObjects.put("parentObjectId", parentObjectId);
 		MessageProcessor flow = lookupFlowConstruct("create-folder");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 
 		return (ObjectId) response.getMessage().getPayload();
 	}
 	
 	protected CmisObject getObjectById(String objectId) throws Exception {
 		testObjects.put("objectId", objectId);
 		MessageProcessor flow = lookupFlowConstruct("get-object-by-id");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (CmisObject) response.getMessage().getPayload();
 		
 	}
 	
	protected Object delete(Object payload, String objectId, boolean allVersions) throws Exception {
		return delete(lookupFlowConstruct("delete"), payload, objectId, allVersions);
 	}
 	
	protected Object delete(MessageProcessor flow, Object payload, String objectId, boolean allVersions) throws Exception {
 		MuleEvent event = getTestEvent(payload);
 	
 		testObjects.put("objectId", objectId);
 		testObjects.put("allVersions", allVersions);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
		return response.getMessage().getPayload();
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected List<Relationship> getObjectRelationships(String objectId) throws Exception {
 		testObjects.put("objectId", objectId);
 		
 		MessageProcessor flow = lookupFlowConstruct("get-object-relationships");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 
 		Object resultPayload = response.getMessage().getPayload();
 		if(resultPayload == null || resultPayload instanceof NullPayload) {
 			return null;
 		} else {
 			return (List<Relationship>) resultPayload;
 		}
 	}
 	
 	protected RepositoryInfo getRepositoryInfo() throws Exception {
 		MessageProcessor flow = lookupFlowConstruct("repository-info");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 
 		RepositoryInfo result = (RepositoryInfo) response.getMessage().getPayload();
 		return result;
 	}
 	
 	protected String rootFolderId() throws Exception {
 		return getRepositoryInfo().getRootFolderId();
 	}
 	
 	protected Acl getAcl(MessageProcessor flow, Object payload, String objectId) throws Exception {
 		testObjects.put("objectId", objectId);
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
 		return (Acl) response.getMessage().getPayload();
 	}
 	
 	protected Acl getAcl(Object payload, String objectId) throws Exception {
 		return getAcl(lookupFlowConstruct("get-acl"), payload, objectId);
 	}
 	
 	protected List<Folder> getParentFolders(Object payload, String objectId) throws Exception {
 		return getParentFolders(lookupFlowConstruct("get-parent-folders-sessionvars-no-cmis-object-ref"), payload, objectId);
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected List<Folder> getParentFolders(MessageProcessor flow, Object payload, String objectId) throws Exception {
 		testObjects.put("objectId", objectId);
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
 		return (List<Folder>) response.getMessage().getPayload();
 	}
 	
 	protected ObjectId createDocumentById(String folderId, String filename, Object payload, String mimeType, 
 			VersioningState versioningState, String objectType, Map<String, Object> propertiesRef) throws Exception {
 		return createDocumentById(lookupFlowConstruct("create-document-by-id"), folderId, filename, payload, mimeType, versioningState, objectType, propertiesRef);
 	}
 	
 	protected ObjectId createDocumentById(MessageProcessor flow, String folderId, String filename, Object payload, String mimeType, 
 			VersioningState versioningState, String objectType, Map<String, Object> propertiesRef) throws Exception {
 		testObjects.put("folderId", folderId);
 		testObjects.put("filename", filename);
 		testObjects.put("mimeType", mimeType);
 		testObjects.put("versioningState", versioningState);
 		testObjects.put("objectType", objectType);
 		testObjects.put("propertiesRef", propertiesRef);
 		
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
 		return (ObjectId) response.getMessage().getPayload();
 	}
 	
 	protected ObjectId createDocumentByIdFromContent(String folderId, String filename, Object payload, String mimeType, 
 			VersioningState versioningState, String objectType, Map<String, Object> propertiesRef) throws Exception {
 		return createDocumentByIdFromContent(lookupFlowConstruct("create-document-by-id-from-content"), folderId, filename, payload, mimeType, 
 				versioningState, objectType, propertiesRef);
 	}
 	
 	protected ObjectId createDocumentByIdFromContent(MessageProcessor flow, String folderId, String filename, Object payload, String mimeType, 
 			VersioningState versioningState, String objectType, Map<String, Object> propertiesRef) throws Exception {
 		testObjects.put("folderId", folderId);
 		testObjects.put("filename", filename);
 		testObjects.put("mimeType", mimeType);
 		testObjects.put("versioningState", versioningState);
 		testObjects.put("objectType", objectType);
 		testObjects.put("propertiesRef", propertiesRef);
 		
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
 		return (ObjectId) response.getMessage().getPayload();
 	}
 	
 	protected ObjectId createDocumentByPath(String folderPath, String filename, Object payload, String mimeType,
 			VersioningState versioningState, String objectType, Map<String, Object> propertiesRef, Boolean force) throws Exception {
 		return createDocumentByPath(lookupFlowConstruct("create-document-by-path"), folderPath, filename, payload, mimeType,
 				versioningState, objectType, propertiesRef, force);
 	}
 	
 	protected ObjectId createDocumentByPath(MessageProcessor flow, String folderPath, String filename, Object payload, String mimeType,
 			VersioningState versioningState, String objectType, Map<String, Object> propertiesRef, Boolean force) throws Exception {
 		testObjects.put("folderPath", folderPath);
 		testObjects.put("filename", filename);
 		testObjects.put("mimeType", mimeType);
 		testObjects.put("versioningState", versioningState);
 		testObjects.put("objectType", objectType);
 		testObjects.put("propertiesRef", propertiesRef);
 		testObjects.put("force", force);
 		
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
 		return (ObjectId) response.getMessage().getPayload();
 	}
 	
 	protected ObjectId createDocumentByPathFromContent(String folderPath, String filename, Object payload, String mimeType,
 			VersioningState versioningState, String objectType, Map<String, Object> propertiesRef, Boolean force) throws Exception {
 		return createDocumentByPathFromContent(lookupFlowConstruct("create-document-by-path-from-content"), folderPath,
 				filename, payload, mimeType, versioningState, objectType, propertiesRef, force);
 	}
 	
 	protected ObjectId createDocumentByPathFromContent(MessageProcessor flow, String folderPath, String filename, Object payload, String mimeType,
 			VersioningState versioningState, String objectType, Map<String, Object> propertiesRef, Boolean force) throws Exception {
 		testObjects.put("folderPath", folderPath);
 		testObjects.put("filename", filename);
 		testObjects.put("mimeType", mimeType);
 		testObjects.put("versioningState", versioningState);
 		testObjects.put("objectType", objectType);
 		testObjects.put("propertiesRef", propertiesRef);
 		testObjects.put("force", force);
 		
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
 		return (ObjectId) response.getMessage().getPayload();
 	}
 	
 	protected Object createRelationship(String parentObjectId, String childObjectId, String relationshipType) throws Exception {
 		testObjects.put("parentObjectId", parentObjectId);
 		testObjects.put("childObjectId", childObjectId);
 		testObjects.put("relationshipType", relationshipType);
 		
 		MessageProcessor flow = lookupFlowConstruct("create-relationship");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return response.getMessage().getPayload();
 	}
 	
 	protected List<String> deleteTree(Object payload, String folderId, Boolean allversions, Boolean continueOnFailure) throws Exception {
 		return deleteTree(lookupFlowConstruct("delete-tree-session-vars"), payload, folderId, allversions, continueOnFailure);
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected List<String> deleteTree(MessageProcessor flow, Object payload, String folderId, Boolean allversions, Boolean continueOnFailure) throws Exception {
 		testObjects.put("folderId", folderId);
 		testObjects.put("allversions", allversions);
 		testObjects.put("continueOnFailure", continueOnFailure);
 		
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
 		return (List<String>) response.getMessage().getPayload();
 	}
 	
 	protected ContentStream getContentStream(MessageProcessor flow, Object payload, String objectId) throws Exception {
 		testObjects.put("objectId", objectId);
 		
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		
 		MuleEvent response = flow.process(event);
 		return (ContentStream) response.getMessage().getPayload();
 	}
 	
 	protected ContentStream getContentStream(Object payload, String objectId) throws Exception {
 		return getContentStream(lookupFlowConstruct("get-content-stream-sessionvars-no-cmis-object-ref"), payload, objectId);
 	}
 	
 	protected Acl applyAcl(CmisObject payload, String objectId, AclPropagation aclPropagation, List<Ace> removeAces, List<Ace> addAces) throws Exception {
 		testObjects.put("objectId", objectId);
 		testObjects.put("aclPropagation", aclPropagation);
 		testObjects.put("removeAces", removeAces);
 		testObjects.put("addAces", addAces);
 		
 		MuleEvent event = getTestEvent(payload);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		MessageProcessor flow = lookupFlowConstruct("apply-acl");
 		MuleEvent response = flow.process(event);
 		return (Acl) response.getMessage().getPayload();
 	}
 	
 	protected Principal getPrincipal(Acl acl) {
 		Principal principal = null;
 		if(acl != null) {
 			List<Ace> aces = acl.getAces();
 			if(aces != null && aces.size() > 0) {
 				principal = aces.get(0).getPrincipal();
 			}
 		}
 		return principal;
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected List<Policy> getAppliedPolicies(String objectId, CmisObject cmisObject) throws Exception {
 		testObjects.put("objectId", objectId);
 		
 		MuleEvent event = getTestEvent(cmisObject);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		MessageProcessor flow = lookupFlowConstruct("get-applied-policies");
 		MuleEvent response = flow.process(event);
 		return (List<Policy>) response.getMessage().getPayload();
 	}
 	
 	protected void applyAspect(String objectId, String aspectName, Map<String, String> properties) throws Exception {
 		testObjects.put("aspectName", aspectName);
 		testObjects.put("objectId", objectId);
 		testObjects.put("properties", properties);
 		
 		MuleEvent event = getTestEvent(testObjects);
 		
 		for(String key : testObjects.keySet()) {
 			event.setSessionVariable(key, testObjects.get(key));
 		}
 		MessageProcessor flow = lookupFlowConstruct("apply-aspect");
 		flow.process(event);
 	}
 		
 	protected ObjectId checkIn(String checkinComment, String documentId, String filename, String content, String mimeType, boolean major, 
 			Map<String, Object> properties) throws Exception {
 
 		testObjects.put("checkinComment", checkinComment);
 		testObjects.put("documentId", documentId);
 		testObjects.put("filename", filename);
 		testObjects.put("mimeType", mimeType);
 		testObjects.put("contentRef", content);
 		testObjects.put("major", major);
 		testObjects.put("propertiesRef", properties);
 		
 		MessageProcessor flow = lookupFlowConstruct("check-in");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (ObjectId) response.getMessage().getPayload();
 	}
 	
 	protected ObjectId checkOut(String documentId) throws Exception {
 		testObjects.put("documentId", documentId);
 		
 		MessageProcessor flow = lookupFlowConstruct("check-out");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (ObjectId) response.getMessage().getPayload();
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected ItemIterable<Document> getCheckedOutDocuments() throws Exception {
 		MessageProcessor flow = lookupFlowConstruct("get-checkout-docs");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (ItemIterable<Document>) response.getMessage().getPayload();
 	}
 	
 	protected void cancelCheckOut(String documentId) throws Exception {
 		testObjects.put("documentId", documentId);
 		
 		MessageProcessor flow = lookupFlowConstruct("cancel-check-out");
 		flow.process(getTestEvent(testObjects));
 	}
 	
 	protected void applyPolicy(String objectId, List<ObjectId> policies) throws Exception  {
 		testObjects.put("objectId", objectId);
 		testObjects.put("policyIdsRef", policies);
 		
 		MessageProcessor flow = lookupFlowConstruct("apply-policy");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 	}
 }
