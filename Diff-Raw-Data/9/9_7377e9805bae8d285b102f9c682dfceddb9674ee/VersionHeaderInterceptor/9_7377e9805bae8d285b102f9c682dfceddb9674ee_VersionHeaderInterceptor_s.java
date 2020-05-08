 package org.jboss.pressgang.ccms.server.rest.interceptor;
 
 import javax.ws.rs.Path;
 import javax.ws.rs.ext.Provider;
 import java.lang.reflect.Method;
 
 import org.jboss.pressgang.ccms.server.utils.Constants;
 import org.jboss.pressgang.ccms.utils.common.VersionUtilities;
 import org.jboss.resteasy.annotations.interception.ServerInterceptor;
 import org.jboss.resteasy.core.ServerResponse;
 import org.jboss.resteasy.spi.interception.AcceptedByMethod;
 import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
 
 @Provider
 @ServerInterceptor
 public class VersionHeaderInterceptor implements PostProcessInterceptor, AcceptedByMethod {
     @Override
     public boolean accept(final Class declaring, final Method method) {
         // Make sure the request is a REST endpoint
         if (declaring.isAnnotationPresent(Path.class)) {
             final Path path = (Path) declaring.getAnnotation(Path.class);
             if (path.value().startsWith(Constants.BASE_REST_PATH)) {
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     public void postProcess(ServerResponse response) {
         response.getMetadata().add(Constants.X_PRESSGANG_VERSION_HEADER, VersionUtilities.getAPIVersion(VersionHeaderInterceptor.class));
     }
 }
