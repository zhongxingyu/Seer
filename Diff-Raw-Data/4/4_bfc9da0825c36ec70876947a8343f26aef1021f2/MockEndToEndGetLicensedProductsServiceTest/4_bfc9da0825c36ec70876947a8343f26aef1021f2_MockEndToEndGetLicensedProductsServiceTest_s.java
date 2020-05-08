 package com.pearson.ed.lp.ws;
 
 import static com.pearson.ed.lp.LicensedProductTestHelper.configureMockLicensePoolService;
 import static com.pearson.ed.lp.LicensedProductTestHelper.generateDummyGetLicensedProductRequest;
 import static com.pearson.ed.lp.LicensedProductTestHelper.generateDummyGetOrgRequest;
 import static com.pearson.ed.lp.LicensedProductTestHelper.generateDummyGetOrgResponseData;
 import static com.pearson.ed.lp.LicensedProductTestHelper.generateDummyGetProductRequest;
 import static com.pearson.ed.lp.LicensedProductTestHelper.generateDummyGetProductResponse;
 import static com.pearson.ed.lp.LicensedProductTestHelper.generateDummyOrderRequest;
 import static com.pearson.ed.lp.LicensedProductTestHelper.generateDummyOrderResponseMultipleItems;
 import static org.easymock.EasyMock.verify;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.log4j.BasicConfigurator;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.ws.WebServiceMessageFactory;
 import org.springframework.ws.test.client.MockWebServiceServer;
 import org.springframework.ws.test.client.RequestMatchers;
 import org.springframework.ws.test.client.ResponseCreators;
 import org.springframework.ws.test.server.MockWebServiceClient;
 import org.springframework.ws.test.server.RequestCreators;
 import org.springframework.ws.test.server.ResponseMatchers;
 import org.springframework.ws.transport.WebServiceMessageReceiver;
 
 import com.pearson.ed.lp.LicensedProductTestHelper.OrgRequestType;
 import com.pearson.ed.lp.exception.LicensedProductExceptionFactory;
 import com.pearson.ed.lp.exception.ProductNotFoundException;
 import com.pearson.ed.lp.message.ProductData;
 import com.pearson.ed.lp.stub.impl.LicensePoolServiceWrapper;
 import com.pearson.ed.lp.stub.impl.OrderLifeCycleClientImpl;
 import com.pearson.ed.lp.stub.impl.OrganizationLifeCycleClientImpl;
 import com.pearson.ed.lp.stub.impl.ProductLifeCycleClientImpl;
 import com.pearson.ed.lplc.model.LicensePoolMapping;
 import com.pearson.ed.lplc.model.OrderLineItemLPMapping;
 import com.pearson.ed.lplc.model.OrganizationLPMapping;
 import com.pearson.ed.lplc.services.api.LicensePoolService;
 import com.pearson.rws.licensedproduct.doc.v2.GetLicensedProductResponseElement;
 import com.pearson.rws.licensedproduct.doc.v2.LicensedProduct;
 import com.pearson.rws.licensedproduct.doc.v2.QualifyingLicensePool;
 
 /**
  * Tests of Spring Integration configuration, 
  * verifying that the end-to-end messaging is functioning as expected
  * complete with message routing. Uses mock services and clients.
  * 
  * NOTE: must use DirectChannels for synchronous behavior for these tests
  * to work reliably!  If the main applicationContext-lp-integration.xml changes
  * please move the channel configurations to a separate XML file for test configuration
  * pluggability!
  * 
  * @author ULLOYNI
  * 
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { 
 		"classpath:applicationContext-test-lp-client-mocks.xml",
 		"classpath:applicationContext-lp-clients.xml",
 		"classpath:applicationContext-lp-integration.xml",
 		"classpath:applicationContext-lp-exception.xml",
 		"classpath:applicationContext-test-lplc-ws.xml",
 		"classpath:applicationContext-lplc.xml"
 })
 public class MockEndToEndGetLicensedProductsServiceTest {
 	
 	private MockWebServiceClient mockLicensedProductClient;
 	
 	@Autowired(required = true)
 	private LicensePoolService mockLicensePoolService;
 	
 	@Autowired(required = true)
 	private LicensePoolServiceWrapper testLicensePoolClient;
 	
 	private MockWebServiceServer mockOrgService;
 
 	@Autowired(required = true)
 	private OrganizationLifeCycleClientImpl testOrgClient;
 	
 	private MockWebServiceServer mockOrderService;
 
 	@Autowired(required = true)
 	private OrderLifeCycleClientImpl testOrderClient;
 	
 	private MockWebServiceServer mockProductService;
 
 	@Autowired(required = true)
 	private ProductLifeCycleClientImpl testProductClient;
 	
 	@Autowired(required = true)
 	private WebServiceMessageReceiver messageReceiver;
 	
 	@Autowired(required = true)
 	private WebServiceMessageFactory messageFactory;
 	
 	@Autowired(required = true)
 	private LicensedProductExceptionFactory exceptionFactory;
 	
 	private Resource licensedProductXmlSchema;
 	
 	private String dummyOrgId = "dummy-org-id";
 	private String dummyOrderLineItemId = "dummy-order-line-item-id";
 	private String dummyProductId = "product-id";
 	private String dummyIsbn = "0123456789012";
 	
 	/**
 	 * Setup test logging.
 	 */
 	@BeforeClass
 	public static void setUpClass() {
 		BasicConfigurator.configure();
 	}
 	
 	/**
 	 * Test setup, initialize the MockWebServiceClient and MockWebServiceServer instances.
 	 * @throws Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		
		// LicensedProduct.xsd schema file referenced through contract repo jar file
		licensedProductXmlSchema = new ClassPathResource("licensedproduct/LicensedProduct.xsd");
 		
 		// set the default locale to ENGLISH for SOAPFault checks later on
 		Locale.setDefault(Locale.ENGLISH);
 
 		mockLicensedProductClient = MockWebServiceClient.createClient(messageReceiver, messageFactory);
 		mockOrgService = MockWebServiceServer.createServer(testOrgClient.getServiceClient());
 		mockOrderService = MockWebServiceServer.createServer(testOrderClient.getServiceClient());
 		mockProductService = MockWebServiceServer.createServer(testProductClient.getServiceClient());
 	}
 	
 	@After
 	public void tearDown() {
 
 		mockLicensedProductClient = null;
 		mockOrgService = null;
 		mockOrderService = null;
 		mockProductService = null;
 	}
 
 	/**
 	 * Setup the mock client service object behaviors based on the value for the qualifying license pool.
 	 * 
 	 * @param qualifyingLicensePool
 	 *            {@link QualifyingLicensePool} value
 	 * @param failingMock optional mock client instance to set to mimic a fail scenario
 	 * @param toThrow exception to be thrown by the failingMock instance
 	 */
 	private void setMockClientsBehaviors(QualifyingLicensePool qualifyingLicensePool, 
 			Object failingMock, Throwable toThrow) {
 
 		// configure mock licensepool service client
 		Long dummyProductEntityId = Long.valueOf(123);
 		OrganizationLPMapping dummyLicensePool = new OrganizationLPMapping();
 		dummyLicensePool.setOrganization_id(dummyOrgId);
 		dummyLicensePool.setUsed_quantity(10);
 		LicensePoolMapping dummyLicensePoolMapping = new LicensePoolMapping();
 		dummyLicensePool.setLicensepoolMapping(dummyLicensePoolMapping);
 		
 		dummyLicensePoolMapping.setLicensepoolId("dummy-license-pool-id");
 		dummyLicensePoolMapping.setOrg_id(dummyOrgId);
 		dummyLicensePoolMapping.setProduct_id(dummyProductEntityId.toString());
 		dummyLicensePoolMapping.setType("dummy-license-pool-type");
 		dummyLicensePoolMapping.setStatus("dummy-license-pool-status");
 		dummyLicensePoolMapping.setDenyManualSubscription(0);
 		dummyLicensePoolMapping.setQuantity(20);
 		dummyLicensePoolMapping.setStart_date(new Date());
 		dummyLicensePoolMapping.setEnd_date(new Date());
 		
 		OrderLineItemLPMapping dummyOrderLineItem = new OrderLineItemLPMapping();
 		dummyLicensePoolMapping.getOrderLineItems().add(dummyOrderLineItem);
 		dummyOrderLineItem.setOrderLineItemId(dummyOrderLineItemId);
 		
 		if(mockLicensePoolService.equals(failingMock)) {
 			if(toThrow instanceof ProductNotFoundException) {
 				configureMockLicensePoolService(mockLicensePoolService, 
 						Arrays.asList(new OrganizationLPMapping[]{}));
 			} else {
 				configureMockLicensePoolService(mockLicensePoolService, 
 						toThrow);
 				// stop here
 				return;
 			}
 		} else {
 			configureMockLicensePoolService(mockLicensePoolService, 
 					Arrays.asList(new OrganizationLPMapping[]{dummyLicensePool}));
 		}
 
 		// configure mock organization service client
 		String dummyOrgDisplayName = "dummy-org-display-name";
 
 		Map<String, String> dummyOrgData = new Hashtable<String, String>();
 		dummyOrgData.put(dummyOrgId, dummyOrgDisplayName);
 		
 		switch (qualifyingLicensePool) {
 		case ALL_IN_HIERARCHY:
 			if(mockOrgService.equals(failingMock)) {
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.PARENT_TREE)))
 						.andRespond(ResponseCreators.withClientOrSenderFault(
 								toThrow.getMessage(), 
 								Locale.getDefault()));
 				
 				if(toThrow.getMessage().contains("No parent organizations found")) {
 					// consumed exception
 					mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 							OrgRequestType.CHILD_TREE)))
 							.andRespond(ResponseCreators.withPayload(
 									generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.CHILD_TREE)));
 					mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 							OrgRequestType.ROOT_ONLY)))
 							.andRespond(ResponseCreators.withPayload(
 									generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.ROOT_ONLY)));
 				} else {
 					// stop here
 					return;
 				}
 			} else {
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.PARENT_TREE)))
 						.andRespond(ResponseCreators.withPayload(
 								generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.PARENT_TREE)));
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.CHILD_TREE)))
 						.andRespond(ResponseCreators.withPayload(
 								generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.CHILD_TREE)));
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.ROOT_ONLY)))
 						.andRespond(ResponseCreators.withPayload(
 								generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.ROOT_ONLY)));
 			}
 			break;
 		case ROOT_AND_PARENTS:
 			if(mockOrgService.equals(failingMock)) {
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.PARENT_TREE)))
 						.andRespond(ResponseCreators.withClientOrSenderFault(
 								toThrow.getMessage(), 
 								Locale.getDefault()));
 				
 				if(toThrow.getMessage().contains("No parent organizations found")) {
 					// consumed exception
 					mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 							OrgRequestType.ROOT_ONLY)))
 							.andRespond(ResponseCreators.withPayload(
 									generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.ROOT_ONLY)));
 				} else {
 					// stop here
 					return;
 				}
 			} else {
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.PARENT_TREE)))
 						.andRespond(ResponseCreators.withPayload(
 								generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.PARENT_TREE)));
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.ROOT_ONLY)))
 						.andRespond(ResponseCreators.withPayload(
 								generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.ROOT_ONLY)));
 			}
 			break;
 		case ROOT_ONLY:
 			if(mockOrgService.equals(failingMock)) {
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.ROOT_ONLY)))
 						.andRespond(ResponseCreators.withClientOrSenderFault(
 								toThrow.getMessage(), 
 								Locale.getDefault()));
 				// stop here
 				return;
 			} else {
 				mockOrgService.expect(RequestMatchers.payload(generateDummyGetOrgRequest(dummyOrgId, 
 						OrgRequestType.ROOT_ONLY)))
 						.andRespond(ResponseCreators.withPayload(
 								generateDummyGetOrgResponseData(dummyOrgData, OrgRequestType.ROOT_ONLY)));
 			}
 			break;
 		}
 		
 		if(mockLicensePoolService.equals(failingMock) && (toThrow instanceof ProductNotFoundException)) {
 			// stop here
 			return;
 		}
 		
 		// configure mock product service client
 		Long[] dummyProductEntityIds = new Long[] { dummyProductEntityId };
 		Map<Long, ProductData> dummyProductData = new Hashtable<Long, ProductData>(5);
 		dummyProductData.put(dummyProductEntityIds[0], new ProductData(
 				dummyProductId, "product-display-name", "product-short-desc",
 				"product-long-desc", "cgProgram-1 cgProgram-2", new String[] { "K", "6", "12" }));
 		
 		if(mockProductService.equals(failingMock)) {
 			if(toThrow.getMessage().contains("No display information")) {
 				dummyProductData.get(dummyProductEntityIds[0]).setDisplayName(null);
 				dummyProductData.get(dummyProductEntityIds[0]).setCgProgram(null);
 				dummyProductData.get(dummyProductEntityIds[0]).setShortDescription(null);
 				dummyProductData.get(dummyProductEntityIds[0]).setLongDescription(null);
 				dummyProductData.get(dummyProductEntityIds[0]).setGradeLevels(null);
 				mockProductService.expect(RequestMatchers.payload(
 						generateDummyGetProductRequest(dummyProductEntityIds)))
 						.andRespond(ResponseCreators.withPayload(
 								generateDummyGetProductResponse(dummyProductData)));
 			} else {
 				mockProductService.expect(RequestMatchers.payload(
 						generateDummyGetProductRequest(dummyProductEntityIds)))
 						.andRespond(ResponseCreators.withClientOrSenderFault(
 								toThrow.getMessage(), Locale.getDefault()));
 			}
 			// stop here
 			return;
 		} else {
 			mockProductService.expect(RequestMatchers.payload(
 					generateDummyGetProductRequest(dummyProductEntityIds)))
 					.andRespond(ResponseCreators.withPayload(
 							generateDummyGetProductResponse(dummyProductData)));
 		}
 		
 		// configure mock order service client
 		if(mockOrderService.equals(failingMock)) {
 			if(toThrow.getMessage().contains("No ISBN number")) {
 				// simulate response without ordered isbn number, which generates an exception
 				mockOrderService.expect(RequestMatchers.payload(
 						generateDummyOrderRequest(dummyOrderLineItemId)))
 						.andRespond(ResponseCreators.withPayload(
 								generateDummyOrderResponseMultipleItems(
 										dummyOrderLineItemId, null, true)));
 			} else {
 				mockOrderService.expect(RequestMatchers.payload(
 						generateDummyOrderRequest(dummyOrderLineItemId)))
 						.andRespond(ResponseCreators.withClientOrSenderFault(
 								toThrow.getMessage(), Locale.getDefault()));
 			}
 		} else {
 			mockOrderService.expect(RequestMatchers.payload(
 					generateDummyOrderRequest(dummyOrderLineItemId)))
 					.andRespond(ResponseCreators.withPayload(
 							generateDummyOrderResponseMultipleItems(
 									dummyOrderLineItemId, dummyIsbn, true)));
 		}
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ROOT_ONLY.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolRootOnly() throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ROOT_ONLY;
 		setMockClientsBehaviors(qualifyingLicensePool, null, null);
 
 		UnmarshallingResponseCollector responseCollector = new UnmarshallingResponseCollector();
 		
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.noFault())
 				.andExpect(ResponseMatchers.validPayload(licensedProductXmlSchema))
 				.andExpect(responseCollector);
 		
 		Object response = responseCollector.getResponse();
 		
 		assertNotNull(response);
 		assertThat(response, is(GetLicensedProductResponseElement.class));
 		GetLicensedProductResponseElement licensedProductResponse = (GetLicensedProductResponseElement)response;
 		assertEquals(1, licensedProductResponse.getLicensedProduct().size());
 		
 		LicensedProduct licensedProduct = licensedProductResponse.getLicensedProduct().iterator().next();
 		assertEquals(dummyOrgId, licensedProduct.getOrganizationId());
 		assertEquals(dummyProductId, licensedProduct.getProductId());
 		assertEquals(dummyIsbn, licensedProduct.getOrderedISBN());
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ROOT_AND_PARENTS.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolRootAndParents() throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ROOT_AND_PARENTS;
 		setMockClientsBehaviors(qualifyingLicensePool, null, null);
 
 		UnmarshallingResponseCollector responseCollector = new UnmarshallingResponseCollector();
 		
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.noFault())
 				.andExpect(ResponseMatchers.validPayload(licensedProductXmlSchema))
 				.andExpect(responseCollector);
 		
 		Object response = responseCollector.getResponse();
 		
 		assertNotNull(response);
 		assertThat(response, is(GetLicensedProductResponseElement.class));
 		GetLicensedProductResponseElement licensedProductResponse = (GetLicensedProductResponseElement)response;
 		assertEquals(1, licensedProductResponse.getLicensedProduct().size());
 		
 		LicensedProduct licensedProduct = licensedProductResponse.getLicensedProduct().iterator().next();
 		assertEquals(dummyOrgId, licensedProduct.getOrganizationId());
 		assertEquals(dummyProductId, licensedProduct.getProductId());
 		assertEquals(dummyIsbn, licensedProduct.getOrderedISBN());
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ALL_IN_HIERARCHY.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolAllInHierarchy() throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ALL_IN_HIERARCHY;
 		setMockClientsBehaviors(qualifyingLicensePool, null, null);
 
 		UnmarshallingResponseCollector responseCollector = new UnmarshallingResponseCollector();
 		
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.noFault())
 				.andExpect(ResponseMatchers.validPayload(licensedProductXmlSchema))
 				.andExpect(responseCollector);
 		
 		Object response = responseCollector.getResponse();
 		
 		assertNotNull(response);
 		assertThat(response, is(GetLicensedProductResponseElement.class));
 		GetLicensedProductResponseElement licensedProductResponse = (GetLicensedProductResponseElement)response;
 		assertEquals(1, licensedProductResponse.getLicensedProduct().size());
 		
 		LicensedProduct licensedProduct = licensedProductResponse.getLicensedProduct().iterator().next();
 		assertEquals(dummyOrgId, licensedProduct.getOrganizationId());
 		assertEquals(dummyProductId, licensedProduct.getProductId());
 		assertEquals(dummyIsbn, licensedProduct.getOrderedISBN());
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ALL_IN_HIERARCHY but with exceptions thrown
 	 * due to no license pools found.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolAllInHierarchyNoLicensePools() throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ALL_IN_HIERARCHY;
 		Throwable toThrow = new ProductNotFoundException();
 		setMockClientsBehaviors(qualifyingLicensePool, mockLicensePoolService, 
 				toThrow);
 
 		UnmarshallingResponseCollector responseCollector = new UnmarshallingResponseCollector();
 		
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.noFault())
 				.andExpect(ResponseMatchers.validPayload(licensedProductXmlSchema))
 				.andExpect(responseCollector);
 		
 		Object response = responseCollector.getResponse();
 		
 		assertNotNull(response);
 		assertThat(response, is(GetLicensedProductResponseElement.class));
 		GetLicensedProductResponseElement licensedProductResponse = (GetLicensedProductResponseElement)response;
 		assertEquals(0, licensedProductResponse.getLicensedProduct().size());
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ALL_IN_HIERARCHY but with exceptions thrown
 	 * due to no parent organizations found (which should not result in a fault).
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolAllInHierarchyNoParentOrgs() throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ALL_IN_HIERARCHY;
 		Throwable toThrow = new Exception("No parent organizations found");
 		setMockClientsBehaviors(qualifyingLicensePool, mockOrgService, 
 				toThrow);
 
 		UnmarshallingResponseCollector responseCollector = new UnmarshallingResponseCollector();
 		
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.noFault())
 				.andExpect(ResponseMatchers.validPayload(licensedProductXmlSchema))
 				.andExpect(responseCollector);
 		
 		Object response = responseCollector.getResponse();
 		
 		assertNotNull(response);
 		assertThat(response, is(GetLicensedProductResponseElement.class));
 		GetLicensedProductResponseElement licensedProductResponse = (GetLicensedProductResponseElement)response;
 		assertEquals(1, licensedProductResponse.getLicensedProduct().size());
 		
 		LicensedProduct licensedProduct = licensedProductResponse.getLicensedProduct().iterator().next();
 		assertEquals(dummyOrgId, licensedProduct.getOrganizationId());
 		assertEquals(dummyProductId, licensedProduct.getProductId());
 		assertEquals(dummyIsbn, licensedProduct.getOrderedISBN());
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ROOT_ONLY but with exceptions thrown
 	 * due to a generic exception from the OrganizationLifeCycle service.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolRootOnlyGenericException() throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ROOT_ONLY;
 		Throwable toThrow = new Exception("Generic OrganizationLifeCycle service exception");
 		setMockClientsBehaviors(qualifyingLicensePool, mockOrgService, 
 				toThrow);
 
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.clientOrSenderFault(
 						exceptionFactory.getLicensedProductException(toThrow).getMessage()));
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ALL_IN_HEIRARCHY but with exceptions thrown
 	 * due to no order found.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolAllInHeirarchyOrderNotFound() throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ALL_IN_HIERARCHY;
 		Throwable toThrow = new Exception("Required object not found");
 		setMockClientsBehaviors(qualifyingLicensePool, mockOrderService, 
 				toThrow);
 
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.clientOrSenderFault(
 						exceptionFactory.getLicensedProductException(toThrow).getMessage()));
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ALL_IN_HEIRARCHY but with exceptions thrown
 	 * due to product found.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolAllInHeirarchyProductNotFound() 
 		throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ALL_IN_HIERARCHY;
 		Throwable toThrow = new Exception("No display information for product with entity id");
 		setMockClientsBehaviors(qualifyingLicensePool, mockProductService, 
 				toThrow);
 
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.clientOrSenderFault(
 						exceptionFactory.getLicensedProductException(toThrow).getMessage()));
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 
 	/**
 	 * Test behavior with {@link QualifyingLicensePool} set to ALL_IN_HEIRARCHY but with exceptions thrown
 	 * due to no order found.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testEndToEndMessagingWithQualifyingLicensePoolAllInHeirarchyNoProductDisplayInfo() 
 		throws IOException {
 		QualifyingLicensePool qualifyingLicensePool = QualifyingLicensePool.ALL_IN_HIERARCHY;
 		Throwable toThrow = new Exception("No display information");
 		setMockClientsBehaviors(qualifyingLicensePool, mockProductService, 
 				toThrow);
 
 		mockLicensedProductClient.sendRequest(RequestCreators.withPayload(
 				generateDummyGetLicensedProductRequest(dummyOrgId, qualifyingLicensePool)))
 				.andExpect(ResponseMatchers.clientOrSenderFault(
 						exceptionFactory.getLicensedProductException(toThrow).getMessage()));
 		
 		verify(mockLicensePoolService);
 		
 		mockOrgService.verify();
 		mockProductService.verify();
 		mockOrderService.verify();
 	}
 	
 }
