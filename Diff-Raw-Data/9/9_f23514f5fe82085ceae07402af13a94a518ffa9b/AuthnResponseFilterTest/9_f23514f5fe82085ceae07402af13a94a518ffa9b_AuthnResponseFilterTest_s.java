 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package test.unit.net.link.safeonline.sdk.auth.filter;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import com.google.common.base.Function;
 import java.util.*;
 import javax.servlet.http.*;
 import net.link.safeonline.sdk.api.attribute.AttributeSDK;
 import net.link.safeonline.sdk.auth.filter.AuthnResponseFilter;
 import net.link.safeonline.sdk.auth.filter.LoginManager;
 import net.link.safeonline.sdk.auth.protocol.*;
 import net.link.safeonline.sdk.configuration.AuthenticationContext;
 import net.link.safeonline.sdk.configuration.Protocol;
 import net.link.util.test.web.*;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.*;
 
 
 public class AuthnResponseFilterTest {
 
     private static final Log LOG = LogFactory.getLog( AuthnResponseFilterTest.class );
 
     private ServletTestManager servletTestManager;
 
     private ProtocolHandler             mockProtocolHandler;
     private AuthnProtocolRequestContext authnRequest;
 
     @Before
     public void setUp()
             throws Exception {
 
         servletTestManager = new ServletTestManager();
         HashMap<String, ProtocolContext> contexts = new HashMap<String, ProtocolContext>();
 
         mockProtocolHandler = createMock( ProtocolHandler.class );
        authnRequest = new AuthnProtocolRequestContext( UUID.randomUUID().toString(), "test-application", mockProtocolHandler, null, false, false );
         contexts.put( authnRequest.getId(), authnRequest );
         servletTestManager.setUp( new ContainerSetup( //
                 new ServletSetup( LoginTestServlet.class ), //
                 new FilterSetup( AuthnResponseFilter.class ) ) //
                 .addSessionAttribute( ProtocolContext.SESSION_CONTEXTS, contexts ) );
         servletTestManager.createSocketConnector();
     }
 
     public static class LoginTestServlet extends HttpServlet {
 
         private static final Log ltLOG = LogFactory.getLog( LoginTestServlet.class );
 
         @Override
         protected void doGet(HttpServletRequest request, HttpServletResponse response) {
 
             ltLOG.debug( "doGet" );
         }
     }
 
     @After
     public void tearDown()
             throws Exception {
 
         servletTestManager.tearDown();
     }
 
     @Test
     public void normalRequestPasses()
             throws Exception {
 
         // Setup Data
         HttpClient httpClient = new HttpClient();
         GetMethod getMethod = new GetMethod( servletTestManager.getServletLocation() );
 
         // Setup Mocks
         expect( mockProtocolHandler.findAndValidateAuthnResponse( (HttpServletRequest) anyObject(),
                 (Function<AuthnProtocolResponseContext, AuthenticationContext>) anyObject() ) ).andReturn( null );
         replay( mockProtocolHandler );
 
         // Test
         int statusCode = httpClient.executeMethod( getMethod );
 
         // Verify
         LOG.debug( "status code: " + statusCode );
         assertEquals( HttpStatus.SC_OK, statusCode );
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void canLogin()
             throws Exception {
 
         // Setup Data
         HttpClient httpClient = new HttpClient();
         GetMethod getMethod = new GetMethod( servletTestManager.getServletLocation() );
 
         // Setup Mocks
         String userId = UUID.randomUUID().toString();
         String authenticatedDevice = "test-device";
         AuthnProtocolResponseContext authnResponse = new AuthnProtocolResponseContext( authnRequest, UUID.randomUUID().toString(), userId, null,
                 Collections.singletonList( authenticatedDevice ), new HashMap<String, List<AttributeSDK<?>>>(), true, null, null );
         expect( mockProtocolHandler.findAndValidateAuthnResponse( (HttpServletRequest) anyObject(),
                 (Function<AuthnProtocolResponseContext, AuthenticationContext>) anyObject() ) ).andReturn( authnResponse );
         expect( mockProtocolHandler.getProtocol() ).andReturn( Protocol.SAML2 );
         replay( mockProtocolHandler );
 
         // Test
         int statusCode = httpClient.executeMethod( getMethod );
 
         // Verify
         assertEquals( HttpStatus.SC_OK, statusCode );
         String resultUserId = (String) servletTestManager.getSessionAttribute( LoginManager.USERID_SESSION_ATTRIBUTE );
         LOG.debug( "result userId: " + resultUserId );
         assertEquals( userId, resultUserId );
         List<String> resultAuthenticatedDevices = (List<String>) servletTestManager.getSessionAttribute( LoginManager.AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE );
         LOG.debug( "result authenticatedDevices: " + resultAuthenticatedDevices );
         assertTrue( resultAuthenticatedDevices.contains( authenticatedDevice ) );
     }
 }
