 package org.fusesource.camel.component.salesforce;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.junit4.CamelTestSupport;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.fusesource.camel.component.salesforce.api.dto.*;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Properties;
 
 public class SalesforceComponentTest extends CamelTestSupport {
 
     private static final Logger LOG = LoggerFactory.getLogger(SalesforceComponentTest.class);
     private static final String TEST_LOGIN_PROPERTIES = "/test-login.properties";
     private static final String TEST_LINE_ITEM_ID = "1";
     private static final String NEW_LINE_ITEM_ID = "100";
     private static final String API_VERSION = "25.0";
     private static final String DEFAULT_FORMAT = "json";
 
     private ObjectMapper objectMapper;
     private static String testId;
 
     @Override
     public boolean isCreateCamelContextPerClass() {
         // only create the context once for this class
         return true;
     }
 
     @Test
     public void testGetVersions() throws Exception {
         doTestGetVersion("");
         doTestGetVersion("Xml");
     }
 
     private void doTestGetVersion(String suffix) throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetVersions" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         // test versions doesn't need a body
         sendBody("direct:testGetVersions" + suffix, null);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         List<Version> versions = null;
         Versions versions1 = ex.getIn().getBody(Versions.class);
         if (versions1 == null) {
             versions = ex.getIn().getBody(List.class);
         } else {
             versions = versions1.getVersions();
         }
         assertNotNull(versions);
         LOG.trace("Versions: {}", versions);
     }
 
     @Test
     public void testGetResources() throws Exception {
         doTestGetResources("");
         doTestGetResources("Xml");
     }
 
     private void doTestGetResources(String suffix) throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetResources" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testGetResources" + suffix, null);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         RestResources resources = ex.getIn().getBody(RestResources.class);
         assertNotNull(resources);
         LOG.trace("Resources: {}", resources);
     }
 
     @Test
     public void testGetGlobalObjects() throws Exception {
         doTestGetGlobalObjects("");
         doTestGetGlobalObjects("Xml");
     }
 
     private void doTestGetGlobalObjects(String suffix) throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetGlobalObjects" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testGetGlobalObjects" + suffix, null);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         GlobalObjects globalObjects = ex.getIn().getBody(GlobalObjects.class);
         assertNotNull(globalObjects);
         LOG.trace("GlobalObjects: {}", globalObjects);
     }
 
     @Test
     public void testGetSObjectBasicInfo() throws Exception {
         doTestGetSObjectBasicInfo("");
         doTestGetSObjectBasicInfo("Xml");
     }
 
     private void doTestGetSObjectBasicInfo(String suffix) throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetSObjectBasicInfo" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testGetSObjectBasicInfo" + suffix, null);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         SObjectBasicInfo objectBasicInfo = ex.getIn().getBody(SObjectBasicInfo.class);
         assertNotNull(objectBasicInfo);
         LOG.trace("SObjectBasicInfo: {}", objectBasicInfo);
     }
 
     @Test
     public void testGetSObjectDescription() throws Exception {
         doTestGetSObjectDescription("");
         doTestGetSObjectDescription("Xml");
     }
 
     private void doTestGetSObjectDescription(String suffix) throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetSObjectDescription" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testGetSObjectDescription" + suffix, null);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         SObjectDescription sObjectDescription = ex.getIn().getBody(SObjectDescription.class);
         assertNotNull(sObjectDescription);
         LOG.trace("SObjectDescription: {}", sObjectDescription);
     }
 
     @Test
     public void testGetSObjectById() throws Exception {
         doTestGetSObjectById("");
         doTestGetSObjectById("Xml");
     }
 
     private void doTestGetSObjectById(String suffix) throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetSObjectById" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testGetSObjectById" + suffix, testId);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         Merchandise__c merchandise = ex.getIn().getBody(Merchandise__c.class);
         assertNotNull(merchandise);
         if (suffix.isEmpty()) {
             assertNull(merchandise.getTotal_Inventory__c());
             assertNotNull(merchandise.getPrice__c());
         } else {
             assertNotNull(merchandise.getTotal_Inventory__c());
             assertNull(merchandise.getPrice__c());
         }
         LOG.trace("SObjectById: {}", merchandise);
     }
 
     @Test
     public void testCreateUpdateDeleteById() throws Exception {
         doTestCreateUpdateDeleteById("");
         doTestCreateUpdateDeleteById("Xml");
     }
 
     private void doTestCreateUpdateDeleteById(String suffix) throws InterruptedException {
         MockEndpoint mock = getMockEndpoint("mock:testCreateSObject" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         Merchandise__c merchandise__c = new Merchandise__c();
         merchandise__c.setName("Wee Wee Wee Plane");
         merchandise__c.setDescription__c("Microlite plane");
         merchandise__c.setPrice__c(2000.0);
         merchandise__c.setTotal_Inventory__c(50.0);
         sendBody("direct:testCreateSObject" + suffix, merchandise__c);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         assertNull(ex.getException());
         CreateSObjectResult result = ex.getIn().getBody(CreateSObjectResult.class);
         assertNotNull(result);
         assertTrue("CreateSObject success", result.getSuccess());
         LOG.trace("CreateSObject: " + result);
 
         // test JSON update
         mock = getMockEndpoint("mock:testUpdateSObjectById" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         merchandise__c = new Merchandise__c();
         // make the plane cheaper
         merchandise__c.setPrice__c(1500.0);
         // change inventory to half
         merchandise__c.setTotal_Inventory__c(25.0);
         template().sendBodyAndHeader("direct:testUpdateSObjectById" + suffix,
             merchandise__c, SalesforceEndpointConfig.SOBJECT_ID, result.getId());
         mock.assertIsSatisfied();
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         // empty body and no exception
         assertNull(ex.getException());
         assertNull(ex.getOut().getBody());
         LOG.trace("UpdateSObjectById successful");
 
         // delete the newly created SObject
         mock = getMockEndpoint("mock:testDeleteSObjectById" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testDeleteSObjectById" + suffix, result.getId());
         mock.assertIsSatisfied();
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         // empty body and no exception
         assertNull(ex.getException());
         assertNull(ex.getOut().getBody());
         LOG.trace("DeleteSObjectById successful");
     }
 
     @Test
     public void testCreateUpdateDeleteByExternalId() throws Exception {
         doTestCreateUpdateDeleteByExternalId("");
         doTestCreateUpdateDeleteByExternalId("Xml");
     }
 
     private void doTestCreateUpdateDeleteByExternalId(String suffix) throws InterruptedException {
         MockEndpoint mock = getMockEndpoint("mock:testGetSObjectByExternalId" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         // get line item with Name 1
         sendBody("direct:testGetSObjectByExternalId" + suffix, TEST_LINE_ITEM_ID);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         assertNull(ex.getException());
         Line_Item__c line_item__c = ex.getIn().getBody(Line_Item__c.class);
         assertNotNull(line_item__c);
         LOG.trace("GetSObjectByExternalId: {}", line_item__c);
 
         // test JSON update
         mock = getMockEndpoint("mock:testCreateOrUpdateSObjectByExternalId" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         // change line_item__c to create a new Line Item
         // otherwise we will get an error from Salesforce
         line_item__c.clearBaseFields();
         // set the unit price and sold
         line_item__c.setUnit_Price__c(1000.0);
         line_item__c.setUnits_Sold__c(50.0);
         // update line item with Name NEW_LINE_ITEM_ID
         template().sendBodyAndHeader("direct:testCreateOrUpdateSObjectByExternalId" + suffix,
             line_item__c, SalesforceEndpointConfig.SOBJECT_EXT_ID_VALUE, NEW_LINE_ITEM_ID);
         mock.assertIsSatisfied();
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         CreateSObjectResult result = ex.getIn().getBody(CreateSObjectResult.class);
         assertNotNull(result);
         assertTrue(result.getSuccess());
         LOG.trace("CreateSObjectByExternalId: {}", result);
 
         // change line_item__c to update existing Line Item
         // otherwise we will get an error from Salesforce
         line_item__c.clearBaseFields();
         // clear read only parent type fields
         line_item__c.setInvoice_Statement__c(null);
         line_item__c.setMerchandise__c(null);
         // change the units sold
         line_item__c.setUnits_Sold__c(25.0);
 
         // update line item with Name NEW_LINE_ITEM_ID
         template().sendBodyAndHeader("direct:testCreateOrUpdateSObjectByExternalId" + suffix,
             line_item__c, SalesforceEndpointConfig.SOBJECT_EXT_ID_VALUE, NEW_LINE_ITEM_ID);
         mock.assertIsSatisfied();
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         result = ex.getIn().getBody(CreateSObjectResult.class);
         assertNotNull(result);
         assertTrue(result.getSuccess());
         LOG.trace("UpdateSObjectByExternalId: {}", result);
 
         // delete the SObject with Name=2
         mock = getMockEndpoint("mock:testDeleteSObjectByExternalId" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testDeleteSObjectByExternalId" + suffix, NEW_LINE_ITEM_ID);
         mock.assertIsSatisfied();
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         // empty body and no exception
         assertNull(ex.getException());
         assertNull(ex.getOut().getBody());
         LOG.trace("DeleteSObjectByExternalId successful");
     }
 
     @Test
     public void testExecuteQuery() throws Exception {
         doTestExecuteQuery("");
         doTestExecuteQuery("Xml");
     }
 
     private void doTestExecuteQuery(String suffix) throws InterruptedException {
         MockEndpoint mock = getMockEndpoint("mock:testExecuteQuery" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testExecuteQuery" + suffix, null);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         assertNull(ex.getException());
         QueryRecordsLine_Item__c queryRecords = ex.getIn().getBody(QueryRecordsLine_Item__c.class);
         assertNotNull(queryRecords);
         LOG.trace("ExecuteQuery: {}", queryRecords);
     }
 
 
     @Test
     public void testExecuteSearch() throws Exception {
         doTestExecuteSearch("");
         doTestExecuteSearch("Xml");
     }
 
     private void doTestExecuteSearch(String suffix) throws InterruptedException {
         MockEndpoint mock = getMockEndpoint("mock:testExecuteSearch" + suffix);
         mock.expectedMinimumMessageCount(1);
 
         sendBody("direct:testExecuteSearch" + suffix, null);
         mock.assertIsSatisfied();
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         assertNull(ex.getException());
         SearchResults queryRecords = ex.getIn().getBody(SearchResults.class);
         List<SearchResult> searchResults = null;
         if (queryRecords != null) {
             searchResults = queryRecords.getResults();
         } else {
             searchResults = ex.getIn().getBody(List.class);
         }
         assertNotNull(searchResults);
         LOG.trace("ExecuteSearch: {}", searchResults);
     }
 
     @Override
     protected RouteBuilder createRouteBuilder() throws Exception {
 
         // create a json mapper
         objectMapper = new ObjectMapper();
 
         // create the component
         SalesforceComponent component = new SalesforceComponent();
         setLoginProperties(component);
 
         // default component level payload format
         component.setFormat(DEFAULT_FORMAT);
         // default api version
         component.setApiVersion(API_VERSION);
 
         // add it to context
         context().addComponent("force", component);
 
         // create test route
         return new RouteBuilder() {
             public void configure() {
 
                 // testGetVersion
                 from("direct:testGetVersions")
                     .to("force://getVersions")
                     .to("mock:testGetVersions");
 
                 // allow overriding format per endpoint
                 from("direct:testGetVersionsXml")
                     .to("force://getVersions?format=xml")
                     .to("mock:testGetVersionsXml");
 
                 // testGetResources
                 from("direct:testGetResources")
                     .to("force://getResources")
                     .to("mock:testGetResources");
 
                 from("direct:testGetResourcesXml")
                     .to("force://getResources?format=xml")
                     .to("mock:testGetResourcesXml");
 
                 // testGetGlobalObjects
                 from("direct:testGetGlobalObjects")
                     .to("force://getGlobalObjects")
                     .to("mock:testGetGlobalObjects");
 
                 from("direct:testGetGlobalObjectsXml")
                     .to("force://getGlobalObjects?format=xml")
                     .to("mock:testGetGlobalObjectsXml");
 
                 // testGetSObjectBasicInfo
                 from("direct:testGetSObjectBasicInfo")
                     .to("force://getSObjectBasicInfo?sObjectName=Merchandise__c")
                     .to("mock:testGetSObjectBasicInfo");
 
                 from("direct:testGetSObjectBasicInfoXml")
                     .to("force://getSObjectBasicInfo?format=xml&sObjectName=Merchandise__c")
                     .to("mock:testGetSObjectBasicInfoXml");
 
                 // testGetSObjectDescription
                 from("direct:testGetSObjectDescription")
                     .to("force://getSObjectDescription?sObjectName=Merchandise__c")
                     .to("mock:testGetSObjectDescription");
 
                 from("direct:testGetSObjectDescriptionXml")
                     .to("force://getSObjectDescription?format=xml&sObjectName=Merchandise__c")
                     .to("mock:testGetSObjectDescriptionXml");
 
                 // testGetSObjectById
                 from("direct:testGetSObjectById")
                    .to("force://getSObjectById?sObjectName=Merchandise__c&sObjectClass=org.fusesource.camel.component.salesforce.Merchandise__c&sObjectFields=Description__c,Price__c")
                     .to("mock:testGetSObjectById");
 
                 from("direct:testGetSObjectByIdXml")
                    .to("force://getSObjectById?format=xml&sObjectName=Merchandise__c&sObjectClass=org.fusesource.camel.component.salesforce.Merchandise__c&sObjectFields=Description__c,Total_Inventory__c")
                     .to("mock:testGetSObjectByIdXml");
 
                 // testCreateSObject
                 from("direct:testCreateSObject")
                    .to("force://createSObject?sObjectName=Merchandise__c&sObjectClass=org.fusesource.camel.component.salesforce.Merchandise__c")
                     .to("mock:testCreateSObject");
 
                 from("direct:testCreateSObjectXml")
                    .to("force://createSObject?format=xml&sObjectName=Merchandise__c&sObjectClass=org.fusesource.camel.component.salesforce.Merchandise__c")
                     .to("mock:testCreateSObjectXml");
 
                 // testUpdateSObjectById
                 from("direct:testUpdateSObjectById")
                    .to("force://updateSObjectById?sObjectName=Merchandise__c&sObjectClass=org.fusesource.camel.component.salesforce.Merchandise__c")
                     .to("mock:testUpdateSObjectById");
 
                 from("direct:testUpdateSObjectByIdXml")
                    .to("force://updateSObjectById?format=xml&sObjectName=Merchandise__c&sObjectClass=org.fusesource.camel.component.salesforce.Merchandise__c")
                     .to("mock:testUpdateSObjectByIdXml");
 
                 // testDeleteSObjectById
                 from("direct:testDeleteSObjectById")
                     .to("force://deleteSObjectById?sObjectName=Merchandise__c")
                     .to("mock:testDeleteSObjectById");
 
                 from("direct:testDeleteSObjectByIdXml")
                     .to("force://deleteSObjectById?format=xml&sObjectName=Merchandise__c")
                     .to("mock:testDeleteSObjectByIdXml");
 
                 // testGetSObjectByExternalId
                 from("direct:testGetSObjectByExternalId")
                    .to("force://getSObjectByExternalId?sObjectName=Line_Item__c&sObjectIdName=Name&sObjectClass=org.fusesource.camel.component.salesforce.Line_Item__c")
                     .to("mock:testGetSObjectByExternalId");
 
                 from("direct:testGetSObjectByExternalIdXml")
                    .to("force://getSObjectByExternalId?format=xml&sObjectName=Line_Item__c&sObjectIdName=Name&sObjectClass=org.fusesource.camel.component.salesforce.Line_Item__c")
                     .to("mock:testGetSObjectByExternalIdXml");
 
                 // testCreateOrUpdateSObjectByExternalId
                 from("direct:testCreateOrUpdateSObjectByExternalId")
                    .to("force://createOrUpdateSObjectByExternalId?sObjectName=Line_Item__c&sObjectIdName=Name&sObjectClass=org.fusesource.camel.component.salesforce.Line_Item__c")
                     .to("mock:testCreateOrUpdateSObjectByExternalId");
 
                 from("direct:testCreateOrUpdateSObjectByExternalIdXml")
                    .to("force://createOrUpdateSObjectByExternalId?format=xml&sObjectName=Line_Item__c&sObjectIdName=Name&sObjectClass=org.fusesource.camel.component.salesforce.Line_Item__c")
                     .to("mock:testCreateOrUpdateSObjectByExternalIdXml");
 
                 // testDeleteSObjectByExternalId
                 from("direct:testDeleteSObjectByExternalId")
                     .to("force://deleteSObjectByExternalId?sObjectName=Line_Item__c&sObjectIdName=Name")
                     .to("mock:testDeleteSObjectByExternalId");
 
                 from("direct:testDeleteSObjectByExternalIdXml")
                     .to("force://deleteSObjectByExternalId?format=xml&sObjectName=Line_Item__c&sObjectIdName=Name")
                     .to("mock:testDeleteSObjectByExternalIdXml");
 
                 // testExecuteQuery
                 from("direct:testExecuteQuery")
                     .to("force://executeQuery?sObjectQuery=SELECT name from Line_Item__c&sObjectClass=org.fusesource.camel.component.salesforce.QueryRecordsLine_Item__c")
                     .to("mock:testExecuteQuery");
 
                 from("direct:testExecuteQueryXml")
                     .to("force://executeQuery?format=xml&sObjectQuery=SELECT name from Line_Item__c&sObjectClass=org.fusesource.camel.component.salesforce.QueryRecordsLine_Item__c")
                     .to("mock:testExecuteQueryXml");
 
                 // testExecuteSearch
                 from("direct:testExecuteSearch")
                     .to("force://executeSearch?sObjectSearch=FIND {Wee}")
                     .to("mock:testExecuteSearch");
 
                 from("direct:testExecuteSearchXml")
                     .to("force://executeSearch?format=xml&sObjectSearch=FIND {Wee}")
                     .to("mock:testExecuteSearchXml");
             }
         };
     }
 
     private void setLoginProperties(SalesforceComponent component) throws IllegalAccessException, IOException {
         // load test-login properties
         Properties properties = new Properties();
         InputStream stream = getClass().getResourceAsStream(TEST_LOGIN_PROPERTIES);
         if (null == stream) {
             throw new IllegalAccessException("Create a properties file named " +
                 TEST_LOGIN_PROPERTIES + " with clientId, clientSecret, userName, password and a testId" +
                 " for a Salesforce account with the Merchandise object from Salesforce Guides.");
         }
         properties.load(stream);
         component.setClientId(properties.getProperty("clientId"));
         component.setClientSecret(properties.getProperty("clientSecret"));
         component.setUserName(properties.getProperty("userName"));
         component.setPassword(properties.getProperty("password"));
 
         testId = properties.getProperty("testId");
 
         assertNotNull("Null clientId", component.getClientId());
         assertNotNull("Null clientSecret", component.getClientSecret());
         assertNotNull("Null userName", component.getUserName());
         assertNotNull("Null password", component.getPassword());
 
         assertNotNull("Null testId", testId);
     }
 
 }
