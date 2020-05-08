 package dk.nsi.minlog;
 
import static net.javacrumbs.smock.common.SmockCommon.*;
import static net.javacrumbs.smock.common.server.CommonSmockServer.*;
 import static org.springframework.ws.test.server.ResponseMatchers.*;
 
 import javax.annotation.Resource;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.ws.test.server.MockWebServiceClient;
 
 import dk.nsi.minlog.test.IntegrationUnitTestSupport;
 import dk.nsi.minlog.test.utils.SoapHeaders;
 
 public class MinLogEndpointFunctionalTest extends IntegrationUnitTestSupport{
 	@Resource
     private ApplicationContext applicationContext;
 
     private MockWebServiceClient mockClient;
     
     @Before
     public void createClient() {
         mockClient = MockWebServiceClient.createClient(applicationContext);
     }
 	
 	@Test
 	public void allByCpr() throws Exception{
 		String payload = 
 				"<ml:ListLogStatementsRequest xmlns:ml=\"http://nsi.dk/minlog/2012/05/24/\">"+
 				"<cprNR>1111111999</cprNR>" +
 				"</ml:ListLogStatementsRequest>"; 		
 		mockClient
 		.sendRequest(withMessage(SoapHeaders.getSoapEnvelope(payload)))
 		.andExpect(noFault())
 		.andExpect(payload(resource("ws/unbound/response.xml")));
 	}
 	
 	@Test
 	public void fromDateByCpr() throws Exception{
 		String payload =
 				
 				"<ml:ListLogStatementsRequest xmlns:ml=\"http://nsi.dk/minlog/2012/05/24/\">"+
 				"<cprNR>1111111999</cprNR>" +
 				"<fraDato>2012-01-01T12:51:00Z</fraDato>" +
 				"</ml:ListLogStatementsRequest>"; 		
 		mockClient
 		.sendRequest(withMessage(SoapHeaders.getSoapEnvelope(payload)))
 		.andExpect(noFault())
 		.andExpect(payload(resource("ws/from/response.xml")));
 	}
 	
 	@Test
 	public void toDateByCpr() throws Exception{
 		String payload =
 				"<ml:ListLogStatementsRequest xmlns:ml=\"http://nsi.dk/minlog/2012/05/24/\">"+
 				"<cprNR>1111111999</cprNR>" +
 				"<fraDato>2012-01-02T07:00:00Z</fraDato>" +
 				"<tilDato>2012-01-02T08:00:00Z</tilDato>" +
 				"</ml:ListLogStatementsRequest>"; 		
 		mockClient
 		.sendRequest(withMessage(SoapHeaders.getSoapEnvelope(payload)))
 		.andExpect(noFault())
 		.andExpect(payload(resource("ws/to/response.xml")));
 	}
 }
