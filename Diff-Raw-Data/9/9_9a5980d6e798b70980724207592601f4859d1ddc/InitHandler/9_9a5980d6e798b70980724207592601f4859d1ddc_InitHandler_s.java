 package net.contextfw.web.application.internal.service;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.contextfw.web.application.HttpContext;
 import net.contextfw.web.application.ResourceCleaner;
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.WebApplicationHandle;
 import net.contextfw.web.application.component.Component;
 import net.contextfw.web.application.internal.LifecycleListeners;
 import net.contextfw.web.application.internal.scope.WebApplicationScopedBeans;
 import net.contextfw.web.application.lifecycle.PageFlowFilter;
 import net.contextfw.web.application.properties.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
import com.google.inject.Singleton;
 
@Singleton
 public class InitHandler {
 
     private Logger logger = LoggerFactory.getLogger(InitHandler.class);
     @Inject
     private WebApplicationContextHandler handler;
 
     //private final InitializerProvider initializers;
     @Inject
     private Provider<WebApplication> webApplicationProvider;
     @Inject
     private LifecycleListeners listeners;
     @Inject
     private PageFlowFilter pageFlowFilter;
     
     private final long initialMaxInactivity;
     
     private DirectoryWatcher watcher;
     
     private ResourceCleaner cleaner;
     
     private final boolean developmentMode;
 
     public InitHandler(Properties properties) {
         initialMaxInactivity = properties.get(Properties.INITIAL_MAX_INACTIVITY);
         developmentMode = properties.get(Properties.DEVELOPMENT_MODE);
     }
 
     public final void handleRequest(
             List<Class<? extends Component>> chain, 
             HttpServlet servlet,
             HttpServletRequest request, 
             HttpServletResponse response)
             throws ServletException, IOException {
         
     	if (watcher != null && watcher.hasChanged()) {
     		logger.info("Reloading resources");
     		cleaner.clean();
     	}
     	
         if(!pageFlowFilter.beforePageCreate(
                 handler.getContextCount(),
                     request)) {
             return;
         }
 
         response.addHeader("Expires", "Sun, 19 Nov 1978 05:00:00 GMT");
         response.addHeader("Last-Modified", new Date().toString());
         response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate");
         response.addHeader("Cache-Control", "post-check=0, pre-check=0");
         response.addHeader("Pragma", "no-cache");
 
         if (chain == null) {
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
         } else {
             
             WebApplicationContext context = prepareWebApplicationScope(servlet,
                         request, response);
             WebApplication app = webApplicationProvider.get();
             app.setInitializerChain(chain);
             context.setApplication(app);
             handler.addContext(context);
 
             synchronized (context.getApplication()) {
                 try {
                     
                     pageFlowFilter.onPageCreate(
                                 handler.getContextCount(),
                                 pageFlowFilter.getRemoteAddr(request),
                                 context.getHandle().getKey());
 
                     listeners.beforeInitialize();
                     app.initState();
                     listeners.afterInitialize();
                     listeners.beforeRender();
                     boolean expired = app.sendResponse();
                     listeners.afterRender();
 
                     // Setting expiration here so that long page processing is
                     // not
                     // penalizing client
                     if (expired) {
                         context.setExpires(System.currentTimeMillis());
                     } else {
                         context.setExpires(System.currentTimeMillis() + initialMaxInactivity);
                     }
 
                 } catch (Exception e) {
                     listeners.onException(e);
                     if (e instanceof WebApplicationException) {
                         throw (WebApplicationException) e;
                     } else {
                         throw new WebApplicationException(e);
                     }
                 } finally {
                     context.getHttpContext().setServlet(null);
                     context.getHttpContext().setRequest(null);
                     context.getHttpContext().setResponse(null);
                 }
             }
         }
     }
 
     private WebApplicationContext prepareWebApplicationScope(HttpServlet servlet, HttpServletRequest request,
             HttpServletResponse response) {
         WebApplicationScopedBeans beans = WebApplicationScopedBeans
                 .createNewInstance();
         HttpContext httpContext = new HttpContext(servlet, request, response);
         WebApplicationContext context = new WebApplicationContext(httpContext,
                 pageFlowFilter.getRemoteAddr(request),
                 System.currentTimeMillis() + initialMaxInactivity,
                 handler.createNewHandle(), beans);
         beans.seed(HttpContext.class, httpContext);
         beans.seed(WebApplicationHandle.class, context.getHandle());
         return context;
     }
 
     @Inject
     public void setWatcher(DirectoryWatcher watcher) {
        if (!developmentMode) {
             this.watcher = watcher;
         }
     }
     
     @Inject
     public void setCleaner(ResourceCleaner cleaner) {
        if (!developmentMode) {
             this.cleaner = cleaner;
         }
     }
 }
