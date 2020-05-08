 /**
  * XWeb project
  * Created by Hamed Abdollahpour
  * https://github.com/abdollahpour/xweb
  */
 
 package ir.xweb.module;
 
 import ir.xweb.module.*;
 import ir.xweb.server.XWebUser;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import static org.mockito.Mockito.*;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 
 @RunWith(value = Parameterized.class)
 public class TestAuthenticationModule {
 
     private final String username;
 
     private final String password;
 
     private final static int CAPTCHA = 87654;
 
     final ServletContext servletContext = mock(ServletContext.class);
 
     final Manager manager = mock(Manager.class);
 
     final ModuleInfo moduleInfo = mock(ModuleInfo.class);
 
     final ModuleParam moduleParam = mock(ModuleParam.class);
 
     final HttpSession session = mock(HttpSession.class);
 
     public TestAuthenticationModule(
             final String username,
             final String password) {
 
         this.username = username;
         this.password = password;
     }
 
     @Parameterized.Parameters
     public static Collection<Object[]> data() {
         Object[][] data = new Object[][] {
                 {"hamed", "123456"}
         };
         return Arrays.asList(data);
     }
 
     @Before
     public void setup() {
         // setup require values
         when(moduleParam.getString(AuthenticationModule.PARAM_DEFAULT, null)).thenReturn(username);
 
         when(session.getAttribute(CaptchaModule.SESSION_CAPTCHA_CODE)).thenReturn(CAPTCHA);
         when(session.getAttribute(CaptchaModule.SESSION_CAPTCHA_EXPIRE)).thenReturn(new Date(
                 System.currentTimeMillis() + 100000
         ).getTime());
     }
 
     @Test(expected=ModuleException.class)
     public void loginFail() throws IOException {
         // set up
         final HttpServletRequest request = mock(HttpServletRequest.class);
         final HttpServletResponse response = mock(HttpServletResponse.class);
 
 
         when(request.getSession()).thenReturn(session);
 
         final HashMap<String, String> paramsMap = new HashMap<String, String>();
         paramsMap.put("action", "login");
         paramsMap.put("id", username);
         paramsMap.put("password", password);
         paramsMap.put("captcha", Integer.toString(CAPTCHA));
 
         final ModuleParam requestParams = new ModuleParam(paramsMap);
 
 
         final AuthenticationModule module = new AuthenticationModule(manager, moduleInfo, moduleParam) {
             @Override
            public XWebUser getUserWithId(String userId, String pass) {
                 return null;
             }
         };
 
         module.init(servletContext);
 
         module.process(servletContext, request, response, requestParams, null);
     }
 
     public void loginSuccess() throws IOException {
         // set up
         final HttpServletRequest request = mock(HttpServletRequest.class);
         final HttpServletResponse response = mock(HttpServletResponse.class);
 
 
         when(request.getSession()).thenReturn(session);
 
         final HashMap<String, String> paramsMap = new HashMap<String, String>();
         paramsMap.put("action", "login");
         paramsMap.put("id", username);
         paramsMap.put("password", password);
         paramsMap.put("captcha", Integer.toString(CAPTCHA));
 
         final ModuleParam requestParams = new ModuleParam(paramsMap);
 
         final XWebUser user = new XWebUser() {
             @Override
             public String getId() {
                 return username;
             }
 
             @Override
             public String getRole() {
                 return null;
             }
 
             @Override
             public Object getExtra() {
                 return null;
             }
         };
 
 
         final AuthenticationModule module = new AuthenticationModule(manager, moduleInfo, moduleParam) {
             @Override
            public XWebUser getUserWithId(String userId, String pass) {
                 return user;
             }
         };
 
         module.init(servletContext);
 
         module.process(servletContext, request, response, requestParams, null);
     }
 
 }
