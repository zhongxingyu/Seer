 package org.cognitor.server.platform.web.security;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.security.web.RedirectStrategy;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import static org.mockito.Mockito.atLeastOnce;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 /**
  * @author Patrick Kranz
  */
 @RunWith(MockitoJUnitRunner.class)
 public class RequestQueryAwareAuthenticationFailureHandlerTest {
     @Mock
     private HttpServletRequest requestMock;
 
     @Mock
     private HttpServletResponse responseMock;
 
     @Mock
     private RedirectStrategy strategyMock;
 
     private RequestQueryAwareAuthenticationFailureHandler handler;
 
     @Before
     public void setUp() {
         handler = new RequestQueryAwareAuthenticationFailureHandler();
         handler.setRedirectStrategy(strategyMock);
     }
 
     @Test
     public void shouldSendRedirectWithQueryStringWhenQueryGiven() throws Exception {
         when(requestMock.getQueryString()).thenReturn("key=value");
         when(requestMock.getContextPath()).thenReturn("http://localhost:8080");
         handler.onAuthenticationFailure(requestMock, responseMock, null);
         verify(strategyMock, atLeastOnce()).sendRedirect(requestMock,
                responseMock, "/?key=value&loginError=true");
     }
 
     @Test
     public void shouldSendRedirectWithoutQueryStringWhenNoQueryGiven() throws Exception {
         when(requestMock.getQueryString()).thenReturn(null);
         when(requestMock.getContextPath()).thenReturn("http://localhost:8080");
         handler.onAuthenticationFailure(requestMock, responseMock, null);
         verify(strategyMock, atLeastOnce()).sendRedirect(requestMock,
                responseMock, "/?loginError=true");
     }
 }
