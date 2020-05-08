 package daniel.web.http;
 
 import daniel.data.option.Option;
 import daniel.data.unit.Duration;
 import daniel.data.unit.Instant;
 import daniel.web.http.cookies.Cookie;
 import daniel.web.http.cookies.CookieManager;
 import daniel.web.util.UuidUtils;
 
 public final class SessionGenerator {
   private static final Duration sessionCookieDuration = Duration.fromDays(7);
 
   private SessionGenerator() {}
 
   public static String getSessionId(HttpRequest request) {
     Option<String> optSessionId = request.getCookies().getValues("session_id").tryGetOnlyElement();
    if (optSessionId.isDefined())
       return optSessionId.getOrThrow();
 
     for (Cookie cookie : CookieManager.getCookies())
       if (cookie.getName().equals("session_id"))
         return cookie.getValue();
 
     String sessionId = UuidUtils.randomAlphanumericUuid();
     Cookie cookie = new Cookie.Builder()
         .setName("session_id")
         .setValue(sessionId)
         .setExpires(Instant.now().plus(sessionCookieDuration))
         .build();
     CookieManager.setCooke(cookie);
     return sessionId;
   }
 }
