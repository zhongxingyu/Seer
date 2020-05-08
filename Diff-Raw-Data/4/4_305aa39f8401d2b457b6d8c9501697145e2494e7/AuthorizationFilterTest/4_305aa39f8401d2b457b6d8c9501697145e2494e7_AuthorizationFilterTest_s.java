 package no.steria.swhrs;
 
 import com.gargoylesoftware.htmlunit.HttpMethod;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebRequestSettings;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.util.NameValuePair;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.mockito.Mockito.*;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class AuthorizationFilterTest {
     private static WebClient client;
     private static JettyServer jettyServer;
 
     @BeforeClass
     public static void setUp() throws Exception {
         client = new WebClient();
         client.setThrowExceptionOnFailingStatusCode(false);
         client.setThrowExceptionOnScriptError(false);
         jettyServer = new JettyServer();
         jettyServer.setPort(10000);
         jettyServer.startServer();
     }
 
     @Test
     public void testShallDenyProtectedResource() throws Exception {
         HtmlPage page = client.getPage(new WebRequestSettings(new URL("http://localhost:10000/swhrs-app/hours/daylist"), HttpMethod.GET));
         assertThat(page.getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
     }
 
 
     @Test
     public void testAllowUnprotectedResource() throws Exception {
         HtmlPage page = client.getPage(new WebRequestSettings(new URL("http://localhost:10000/swhrs-app/"), HttpMethod.GET));
         assertThat(page.getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
     }
 
     @Test
    @Ignore
     public void testAllowAfterLogin() throws Exception {
         WebRequestSettings settings = new WebRequestSettings(new URL("http://localhost:10000/swhrs-app/hours"), HttpMethod.GET);
 
         HtmlPage page = client.getPage(withAuthenticationHeader(settings));
         assertThat(page.getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
     }
 
 
     private static WebRequestSettings withAuthenticationHeader(WebRequestSettings settings) throws IOException {
        settings.setAdditionalHeader(AuthorizationFilter.AUTHENTICATION_TOKEN_HEADER_NAME, "{username: 'matb', password: + '" + Password.fromPlaintext("salt", "password") + "'}");
         return settings;
     }
 
     @Test
     @Ignore
     public void testGetOperation() throws Exception {
         WebRequestSettings settings = new WebRequestSettings(new URL("http://localhost:10000/swhrs-app/hours/week"), HttpMethod.GET);
         List<NameValuePair> requestParameters = new ArrayList<NameValuePair>();
         requestParameters.add(new NameValuePair(RegistrationConstants.USER, "ROR"));
         requestParameters.add(new NameValuePair(RegistrationConstants.DATE, "2012-09-05"));
         settings.setRequestParameters(requestParameters);
         settings.setAdditionalHeader(AuthorizationFilter.AUTHENTICATION_TOKEN_HEADER_NAME, "{\"username\": \"ror\", \"password\": \"" + Password.fromPlaintext("salt", "password") + "\"}");
         HtmlPage page = client.getPage(settings);
         assertThat(page.getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
     }
 
     @Test
     public void testJson() throws Exception {
         HttpServletRequest request = mock(HttpServletRequest.class);
         HttpServletResponse response = mock(HttpServletResponse.class);
 
         when(request.getParameter(RegistrationConstants.DATE)).thenReturn("2012-09-05");
         when(request.getHeader(AuthorizationFilter.AUTHENTICATION_TOKEN_HEADER_NAME)).thenReturn("{\"username\": \"ror\", \"password\": \"salt_IcZa2+j8IMsLptIK4JFG1ODO8Fk=\"}");
         when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:10000/swhrs-app/hours/week"));
         User user = new User();
         user.setUsername("ROR");
         user.setPassword(Password.fromPlaintext("salt", "password"));
         when(request.getAttribute(RegistrationConstants.USER)).thenReturn(user);
         PrintWriter printWriter = new PrintWriter(new StringWriter());
         when(response.getWriter()).thenReturn(printWriter);
 
         TestServlet servlet = new TestServlet();
         servlet.init();
         servlet.doGet(request, response);
     }
 }
