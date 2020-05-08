 package me.smecsia.smartfox.tools.service;
 
 import com.smartfoxserver.v2.entities.User;
 import me.smecsia.smartfox.tools.annotations.Security;
 import me.smecsia.smartfox.tools.common.BasicHandler;
 import me.smecsia.smartfox.tools.common.BasicService;
 import me.smecsia.smartfox.tools.error.MetadataException;
 import me.smecsia.smartfox.tools.error.UnauthorizedException;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  *
  * @author Ilya Sadykov
  *         Date: 13.10.12
  *         Time: 17:15
  */
 public class AuthService extends BasicService {
 
     private static class AuthStrategy {
         public AuthService service;
         public Boolean authRequired;
     }
 
     private static final Map<Class<? extends BasicHandler>, AuthStrategy> authCache =
             new ConcurrentHashMap<Class<? extends BasicHandler>, AuthStrategy>();
 
     public static void checkAuthIfRequired(BasicHandler handler, User user) {
         if (!authCache.containsKey(handler.getClass())) {
             Security security = handler.getClass().getAnnotation(Security.class);
             AuthStrategy authStrategy = new AuthStrategy();
             try {
                authStrategy.service = security.authService().newInstance();
                authStrategy.authRequired = security.authRequired();
                 authCache.put(handler.getClass(), authStrategy);
             } catch (Exception e) {
                 throw new MetadataException(e);
             }
         }
         if (authCache.containsKey(handler.getClass()) && authCache.get(handler.getClass()).authRequired) {
             authCache.get(handler.getClass()).service.check(user);
         }
     }
 
     public void check(User user) throws UnauthorizedException {
         // Override me
     }
 }
