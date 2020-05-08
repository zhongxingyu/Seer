 package jornado;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Implementation of Request using HttpServletRequest
  *
  * @param <U> user type
  */
 public class ServletBackedRequest<U extends WebUser> implements Request<U> {
   private static final Random rnd = new Random();
  private static final int TIMEOUT_31_DAYS = 3600 * 31; // TODO: make configurable
 
   private final HttpServletRequest servletRequest;
   private final UserService<U> userService;
   private final SecureCookieService secureCookieService;
   private RouteHandlerData routeHandlerData;
   private CachedObjectHolder<U> cachedUser;
   private final String requestId = generateId();
 
   private AtomicBoolean loginCookieInvalid = new AtomicBoolean(false);
 
   public ServletBackedRequest(HttpServletRequest servletRequest, UserService<U> userService, SecureCookieService secureCookieService) {
     this.servletRequest = servletRequest;
     this.userService = userService;
     this.secureCookieService = secureCookieService;
   }
 
   private String generateId() {
     byte[] bytes = new byte[8];
     rnd.nextBytes(bytes);
     return HexUtil.toHexString(bytes);
   }
 
   public String getRequestId() {
     return requestId;
   }
 
   public void setRouteHandlerData(RouteHandlerData routeHandlerData) {
     this.routeHandlerData = routeHandlerData;
   }
 
   public Method getMethod() {
     return Method.fromString(servletRequest.getMethod());
   }
 
   public String getPath() {
     return servletRequest.getServletPath();
   }
 
   public String getReconstructedUrl() {
     // TODO: I think we're dropping query params here?
     return servletRequest.getRequestURI();
   }
 
   public String getCookieValue(String name) {
     final Cookie c = getCookie(name);
     return c != null ? c.getValue() : null;
   }
 
   public Cookie getCookie(String name) {
     Cookie[] cookies = servletRequest.getCookies();
     if (cookies != null) {
       for (Cookie cookie : servletRequest.getCookies()) {
         if (cookie.getName().equals(name)) {
           return cookie;
         }
       }
     }
     return null;
   }
 
   public String getHeader(String name) {
     return servletRequest.getHeader(name);
   }
 
   public String getParameter(String name) {
     return servletRequest.getParameter(name);
   }
 
   public String getParameter(String name, String defaultValue) {
     String rv = servletRequest.getParameter(name);
     if (rv != null) {
       return rv;
     } else {
       return defaultValue;
     }
   }
 
   public boolean isParameterSet(String name) {
     final String val = servletRequest.getParameter(name);
     return val != null && val.trim().equals("1");
   }
 
   public Integer getIntParameter(String name) {
     String value = getParameter(name);
     if (value != null) {
       return Integer.parseInt(value);
     } else {
       return null;
     }
   }
 
   public int getIntParameter(String name, int defaultValue) {
     String rv = servletRequest.getParameter(name);
     if (rv != null) {
       return Integer.parseInt(rv);
     } else {
       return defaultValue;
     }
   }
 
   public Long getLongParameter(String name) {
     return Long.parseLong(servletRequest.getParameter(name));
   }
 
   public long getLongParameter(String name, long defaultValue) {
     final Long val = getLongParameter(name);
     if (val != null) {
       return val;
     } else {
       return defaultValue;
     }
   }
 
   public String getPathParameter(String name) {
     return routeHandlerData.getRouteData().getPathParameter(name);
   }
 
   public Long getLongPathParameter(String name) {
     return Long.parseLong(routeHandlerData.getRouteData().getPathParameter(name));
   }
 
   public HttpSession getSession() {
     return servletRequest.getSession();
   }
 
   public boolean isLoginCookieInvalid() {
     return loginCookieInvalid.get();
   }
 
   public U getUser() {
     if (cachedUser == null) {
       String loginCookie = getCookieValue(Constants.LOGIN_COOKIE);
       U user = null;
       if (loginCookie != null) {
         try {
           final String userId = secureCookieService.extract(loginCookie, TIMEOUT_31_DAYS);
           user = userService.load(userId);
           if (user == null) {
             loginCookieInvalid.set(true);
           }
         } catch (InvalidSecureCookieFormatException e) {
           loginCookieInvalid.set(true);
         } catch (FailedSignatureValidation e) {
           loginCookieInvalid.set(true);
         }
       } else {
         user = null;
       }
       cachedUser = new CachedObjectHolder<U>(user);
     }
     return cachedUser.get();
   }
 
   public String getReferer() {
     return getHeader("Referer");
   }
 
   public boolean isLoggedIn() {
     return getUser() != null;
   }
 
   public String getXsrfCookie() {
     return getCookieValue(Constants.XSRF_FIELD_NAME);
   }
 }
