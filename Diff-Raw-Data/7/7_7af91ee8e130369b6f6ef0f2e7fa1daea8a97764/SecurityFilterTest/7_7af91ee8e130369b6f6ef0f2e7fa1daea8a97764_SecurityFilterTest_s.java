 package org.deegree.securityproxy.filter;
 
 import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
 import static javax.servlet.http.HttpServletResponse.SC_OK;
 import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.reset;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.deegree.securityproxy.authorization.wcs.RequestAuthorizationManager;
 import org.deegree.securityproxy.authorization.wcs.TestWcsRequestAuthorizationManager;
 import org.deegree.securityproxy.logger.SecurityRequestResposeLogger;
 import org.deegree.securityproxy.report.SecurityReport;
 import org.deegree.securityproxy.request.OwsRequest;
 import org.deegree.securityproxy.request.WcsRequest;
 import org.deegree.securityproxy.request.WcsRequestParser;
 import org.deegree.securityproxy.responsefilter.ResponseFilterManager;
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.core.Authentication;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Tests for {@link SecurityFilterTest}
  * 
  * @author <a href="erben@lat-lon.de">Alexander Erben</a>
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
  * @author last edited by: $Author: erben $
  * 
  * @version $Revision: $, $Date: $
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath*:org/deegree/securityproxy/filter/SecurityFilterTestContext.xml" })
 @DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
 public class SecurityFilterTest {
 
     private static final int SERIAL_UUID_LENGTH = 36;
 
     private static final String CLIENT_IP_ADDRESS = "127.0.0.1";
 
     private static final String TARGET_URL = "devcloud.blackbridge.com";
 
     private static final String QUERY_STRING = "request=GetCapabilities";
 
     @Autowired
     private SecurityFilter filter;
 
     /**
      * Autowire a mocked instance of {@link SecurityRequestResposeLogger}
      */
     @Autowired
     private SecurityRequestResposeLogger logger;
 
     @Autowired
     private WcsRequestParser requestParserMock;
 
     @Autowired
     private ResponseFilterManager responseFilterManagerMock;
 
     /**
      * Reset the mocked instance of logger to prevent side effects between tests
      */
     @Before
     public void resetMock()
                             throws Exception {
         reset( logger, requestParserMock, responseFilterManagerMock );
         WcsRequest wcsRequestMockToReturn = mock( WcsRequest.class );
         doReturn( wcsRequestMockToReturn ).when( requestParserMock ).parse( (HttpServletRequest) anyObject() );
         doReturn( true ).when( responseFilterManagerMock ).supports( wcsRequestMockToReturn.getClass() );
     }
 
     @Test
     public void testLoggingFilterShouldGenerateReportWithSerialUuid()
                             throws IOException, ServletException {
         filter.doFilter( generateMockRequest(), generateMockResponse(), new FilterChainTestImpl( SC_OK ) );
         verify( logger ).logProxyReportInfo( argThat( hasCorrectSerialUuid() ) );
     }
 
     @Test
     public void testLoggingFilterShouldGenerateReportWithCorrectIpAddress()
                             throws IOException, ServletException {
         filter.doFilter( generateMockRequest(), generateMockResponse(), new FilterChainTestImpl( SC_OK ) );
         verify( logger ).logProxyReportInfo( argThat( hasCorrectIpAddress() ) );
     }
 
     @Test
     public void testLoggingShouldGenerateReportWithCorrectTargetUrl()
                             throws IOException, ServletException {
         filter.doFilter( generateMockRequest(), generateMockResponse(), new FilterChainTestImpl( SC_OK ) );
         verify( logger ).logProxyReportInfo( argThat( hasCorrectTargetUrl() ) );
     }
 
     @Test
     public void testLoggingShouldGenerateCorrectReportForSuccessfulReponse()
                             throws IOException, ServletException {
         filter.doFilter( generateMockRequest(), generateMockResponse(), new FilterChainTestImpl( SC_OK ) );
         verify( logger ).logProxyReportInfo( argThat( hasResponse( SC_OK ) ) );
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testLoggingShouldGenerateCorrectReportForUnauthenticatedReponse()
                             throws IOException, ServletException {
         RequestAuthorizationManager requestAuthorizationManager = new TestWcsRequestAuthorizationManager( false );
         filter.setRequestAuthorizationManager( requestAuthorizationManager );
         filter.doFilter( generateMockRequest(), generateMockResponse(), new FilterChainTestImpl( SC_UNAUTHORIZED ) );
         verify( logger ).logProxyReportInfo( argThat( hasResponse( SC_UNAUTHORIZED ) ) );
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testLoggingShouldGenerateCorrectReportNullQueryString()
                             throws IOException, ServletException {
         RequestAuthorizationManager requestAuthorizationManager = new TestWcsRequestAuthorizationManager( false );
         filter.setRequestAuthorizationManager( requestAuthorizationManager );
         filter.doFilter( generateMockRequestNullQueryString(), generateMockResponse(),
                          new FilterChainTestImpl( SC_BAD_REQUEST ) );
         verify( logger ).logProxyReportInfo( argThat( hasCorrectTargetUrlWithNullQueryString() ) );
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testFilterShouldThrowExceptionOnUnauthorized()
                             throws IOException, ServletException {
         RequestAuthorizationManager requestAuthorizationManager = new TestWcsRequestAuthorizationManager( false );
         filter.setRequestAuthorizationManager( requestAuthorizationManager );
         filter.doFilter( generateMockRequest(), generateMockResponse(), new FilterChainTestImpl( SC_UNAUTHORIZED ) );
     }
 
     @Test
     public void testResponseShouldContainSerialUuidHeader()
                             throws IOException, ServletException {
         HttpServletResponse response = generateMockResponse();
         filter.doFilter( generateMockRequest(), response, new FilterChainTestImpl( SC_OK ) );
         filter.doFilter( generateMockRequest(), response, new FilterChainTestImpl( SC_OK ) );
 
         ArgumentCaptor<String> uuidArgumentFirstInvocation = ArgumentCaptor.forClass( String.class );
         verify( response, times( 2 ) ).setHeader( argThat( is( "serial_uuid" ) ), uuidArgumentFirstInvocation.capture() );
         verify( response, times( 2 ) ).setHeader( argThat( is( "serial_uuid" ) ), uuidArgumentFirstInvocation.capture() );
 
         String firstUuidArgument = uuidArgumentFirstInvocation.getAllValues().get( 0 );
         String secondUuidArgument = uuidArgumentFirstInvocation.getAllValues().get( 1 );
 
         assertThat( firstUuidArgument, is( not( secondUuidArgument ) ) );
     }
 
     @Test
     public void testResponseShouldInvokeResponseFilterManager()
                             throws IOException, ServletException {
         RequestAuthorizationManager requestAuthorizationManager = new TestWcsRequestAuthorizationManager( true );
         filter.setRequestAuthorizationManager( requestAuthorizationManager );
         filter.doFilter( generateMockRequest(), generateMockResponse(), new FilterChainTestImpl( SC_OK ) );
         verify( responseFilterManagerMock ).filterResponse( (StatusCodeResponseBodyWrapper) anyObject(),
                                                             (OwsRequest) anyObject(), (Authentication) anyObject() );
     }
 
     private ServletRequest generateMockRequestNullQueryString() {
         HttpServletRequest mockRequest = mock( HttpServletRequest.class );
         when( mockRequest.getRemoteAddr() ).thenReturn( CLIENT_IP_ADDRESS );
         when( mockRequest.getRequestURL() ).thenReturn( new StringBuffer( TARGET_URL ) );
         when( mockRequest.getQueryString() ).thenReturn( null );
         return mockRequest;
     }
 
     private HttpServletRequest generateMockRequest() {
         HttpServletRequest mockRequest = mock( HttpServletRequest.class );
         when( mockRequest.getRemoteAddr() ).thenReturn( CLIENT_IP_ADDRESS );
         when( mockRequest.getRequestURL() ).thenReturn( new StringBuffer( TARGET_URL ) );
         when( mockRequest.getQueryString() ).thenReturn( QUERY_STRING );
         return mockRequest;
     }
 
     private HttpServletResponse generateMockResponse() {
         return mock( HttpServletResponse.class );
     }
 
     private Matcher<SecurityReport> hasCorrectSerialUuid() {
         return new BaseMatcher<SecurityReport>() {
 
             public boolean matches( Object item ) {
                 SecurityReport report = (SecurityReport) item;
                 return SERIAL_UUID_LENGTH == report.getSerialUuid().length();
             }
 
             public void describeTo( Description description ) {
                 description.appendText( "Length of serial uuid should be " + SERIAL_UUID_LENGTH );
             }
         };
     }
 
     private Matcher<SecurityReport> hasCorrectIpAddress() {
         return new BaseMatcher<SecurityReport>() {
 
             public boolean matches( Object item ) {
                 SecurityReport report = (SecurityReport) item;
                 return CLIENT_IP_ADDRESS.equals( report.getIpAddressOfRequestingUser() );
             }
 
             public void describeTo( Description description ) {
                 description.appendText( "Client IP address should be " + CLIENT_IP_ADDRESS );
             }
         };
     }
 
     private Matcher<SecurityReport> hasCorrectTargetUrl() {
         return new BaseMatcher<SecurityReport>() {
 
             private final String expected = TARGET_URL + "?" + QUERY_STRING;
 
             public boolean matches( Object item ) {
                 SecurityReport report = (SecurityReport) item;
                 return expected.equals( report.getTargetUri() );
             }
 
             public void describeTo( Description description ) {
                 description.appendText( "Target url should be " + TARGET_URL + "?" + QUERY_STRING );
             }
         };
     }
 
     private Matcher<SecurityReport> hasCorrectTargetUrlWithNullQueryString() {
         return new BaseMatcher<SecurityReport>() {
 
             public boolean matches( Object item ) {
                 SecurityReport report = (SecurityReport) item;
                 return TARGET_URL.equals( report.getTargetUri() );
             }
 
             public void describeTo( Description description ) {
                 description.appendText( "Target url should be " + TARGET_URL );
             }
         };
     }
 
     private Matcher<SecurityReport> hasResponse( final int expected ) {
         return new BaseMatcher<SecurityReport>() {
 
             public boolean matches( Object item ) {
                 SecurityReport report = (SecurityReport) item;
                 if ( SC_OK == expected )
                     return report.isResponseSuccessfullySent();
                 else
                     return !report.isResponseSuccessfullySent();
             }
 
             public void describeTo( Description description ) {
                 String not = SC_OK == expected ? "" : " not";
                 String message = "Response report should indicate that the response has" + not
                                  + " been successfully sent!";
                 description.appendText( message );
             }
 
         };
     }
 }
