 package org.ebayopensource.turmeric.test.services.policyenforcementservice;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.common.v1.types.ErrorData;
 import org.ebayopensource.turmeric.runtime.sif.service.Service;
 import org.ebayopensource.turmeric.runtime.sif.service.ServiceInvokerOptions;
 import org.ebayopensource.turmeric.security.v1.services.SubjectType;
 import org.ebayopensource.turmeric.security.v1.services.VerifyAccessRequest;
 import org.ebayopensource.turmeric.security.v1.services.VerifyAccessResponse;
 import org.ebayopensource.turmeric.services.policyenforcementservice.gen.BasePolicyEnforcementServiceConsumer;
 import org.ebayopensource.turmeric.test.services.utils.SecurityTokenUtility;
 import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
 import org.ebayopensource.turmeric.test.services.utils.TestTokenRetrivalObject;
 import org.ebayopensource.turmeric.utils.AuthenticationDataLoader;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 /**
  * 
  * @author sukoneru
  * 
  */
 @RunWith(Parameterized.class)
 public class PolicyEnforcementServiceTests extends CreateValidatePES {
 
 	private BasePolicyEnforcementServiceConsumer m_policyEnforcementConsumer = null;
 	private Map<String, String> subjects = null;
 	private String testcaseNumber;
 	private String testcaseDesc;
 	private String policyId;
 	private String request_id;
 
 	private static final String s_PropFilePath = "PolicyEnforcementServiceTests.properties";
 	private static Class className = PolicyEnforcementServiceTests.class;
 	private static TestDataReader reader = null;
 	private static int max;
 	private static SecurityTokenUtility util = null;
 	private static AuthenticationDataLoader authnLoader = null;
 
 	
 
 	@Before
 	public void setUp() throws Exception {
 			reader = new TestDataReader(className);
 			max = maxPolicies(reader.getProps());
 			// createPolicies(reader, max);
 			util = TestTokenRetrivalObject.getSecurityTokenRetrival();
 			authnLoader = new AuthenticationDataLoader();
 			authnLoader.initData();
 
 		m_policyEnforcementConsumer = new BasePolicyEnforcementServiceConsumer(
 				"PolicyEnforcementService", "PESTestEnv");
 		Service service = m_policyEnforcementConsumer.getService();
 		service.setSessionTransportHeader("X-TURMERIC-SECURITY-USERID", "admin");
 		service.setSessionTransportHeader("X-TURMERIC-SECURITY-PASSWORD",
 				"admin");
 		ServiceInvokerOptions options = service.getInvokerOptions();
 		options.setTransportName("LOCAL");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		cleanUpPolicy(reader, max);
 		reader.unloadProperties();
 		reader = null;
 		util = null;
 		m_policyEnforcementConsumer = null;
 		authnLoader.cleanUpResources();
 	}
 
 	public PolicyEnforcementServiceTests(String policyId, String request_id,
 			String testcaseNumber, String testcaseDesc) {
 		this.policyId = policyId;
 		this.request_id = request_id;
 		this.testcaseNumber = testcaseNumber;
 		this.testcaseDesc = testcaseDesc;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Parameters
 	public static Collection data() throws IOException {
 		return loadInputData();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Collection loadInputData() throws IOException {
 		Properties props = new Properties();
 		List list = new ArrayList();
 		List eachRowData = new ArrayList();
 		InputStream inputStream = className.getResourceAsStream(s_PropFilePath);
 		props.load(inputStream);
 		String debug_tests = props.getProperty("test_to_debug");
 		String total_tests = props.getProperty("total_testcases");
 		if (!debug_tests.equals("") && !debug_tests.equals("null")) {
 			String[] debug_num = debug_tests.split(",");
 			for (String s : debug_num) {
 				int num = Integer.parseInt(s);
 				String policyid = props.getProperty("testcase" + s
 						+ ".policyid");
 				String testcasenum = props.getProperty("testcase" + s
 						+ ".number");
 				String testcaseDesc = props.getProperty("testcase" + s
 						+ ".description");
 
 				if (policyid != null) {
 					eachRowData = new ArrayList();
 					eachRowData.add(policyid);
 					eachRowData.add("testcase" + testcasenum);
 					eachRowData.add(testcasenum);
 					eachRowData.add(testcaseDesc);
 					list.add(eachRowData.toArray());
 				}
 			}
 		} else {
 			// int len = Integer.parseInt(total_tests);
 			int len = totalTestCount(props);
 			for (int i = 0; i <= len; i++) {
 				String policyid = props.getProperty("testcase" + i
 						+ ".policyid");
 				String testcasenum = props.getProperty("testcase" + i
 						+ ".number");
 				String testcaseDesc = props.getProperty("testcase" + i
 						+ ".description");
 
 				if (policyid != null) {
 					eachRowData = new ArrayList();
 					eachRowData.add(policyid);
 					eachRowData.add("testcase" + testcasenum);
 					eachRowData.add(testcasenum);
 					eachRowData.add(testcaseDesc);
 					list.add(eachRowData.toArray());
 				}
 			}
 		}
 		return list;
 	}
 
 	@Test
 	public void testPES() throws Exception {
 		System.out.println("\n -- Testcase Number = " + testcaseNumber
 				+ "\n -- Testcase Description= " + testcaseDesc);
 
 		String request_id = this.request_id;
 		VerifyAccessRequest request = new VerifyAccessRequest();
 		populatePESRequest(util, reader, request, request_id);
 
 		VerifyAccessResponse response = m_policyEnforcementConsumer
 				.verifyAccess(request);
 		validateOutput(request, response, request_id);
 	}
 
 	private void validateOutput(VerifyAccessRequest request,
 			VerifyAccessResponse response, String request_id) {
 		List<String> policies = request.getPolicyType();
 		if (policies.contains("AUTHN")) {
 			assertNotNull(response.getAuthenticatedSubject());
 			System.out.println("Authenticated Subjects are ==> ");
 			for (SubjectType subjectType : response.getAuthenticatedSubject()) {
 				System.out.println("Value : " + subjectType.getValue()
 						+ "Domain : " + subjectType.getDomain());
 			}
 		}
 		if (!policies.contains("RL"))
 			assertNull(response.getRateLimiterStatus());
 
 		String ack = reader.getPreEntryValue(request_id, "response_AckValue");
 		if (ack != null) {
 			if (ack.trim().equals("SUCCESS"))
 				assertEquals("Unexpected Ack Value.", AckValue.SUCCESS, response.getAck());
 			else if (ack.trim().equals("FAILURE")) {
 				assertTrue(response.getAck() == AckValue.FAILURE);
 				String em = reader.getPreEntryValue(request_id,
 						"response_ErrorMessage");
 				if (em != null) {
 					for (ErrorData errorData : response.getErrorMessage()
 							.getError()) {
 						System.out.println("The error id is ==> "
 								+ errorData.getErrorId());
 						System.out.println("The error Message is ==> "
 								+ errorData.getMessage());
 						assertTrue(errorData.getMessage().contains(em.trim()));
 					}
 				}
 			}
 		}
 	}
 }
