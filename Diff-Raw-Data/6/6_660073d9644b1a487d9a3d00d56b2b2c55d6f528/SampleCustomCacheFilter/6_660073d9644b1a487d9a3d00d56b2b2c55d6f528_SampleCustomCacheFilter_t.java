 package org.jahia.modules.sample;
 
 import org.jahia.services.render.filter.AbstractFilter;
 import org.jahia.services.render.RenderContext;
 import org.jahia.services.render.Resource;
 import org.jahia.services.render.filter.RenderChain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jcr.RepositoryException;
 import javax.servlet.http.HttpSession;
 
 public class SampleCustomCacheFilter extends AbstractFilter {
 
     private static final String SESSION_ATTR_NAME = "sessionAttr";
     private static final String BY_SESSION_ATTR = "bySessionAttr";
     private static final String BY_USER_PROPERTY = "byUserAttr";
     private static final String USER_PROPERTY_NAME = "j:firstName";
 
     public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
         List l = (List) renderContext.getRequest().getAttribute("module.cache.additional.key");
         if (l == null) {
             l = new ArrayList();
             renderContext.getRequest().setAttribute("module.cache.additional.key", l);
         }
         l.add(appendCustomKey(resource, renderContext));
         return super.prepare(renderContext, resource, chain);
     }
 
     public String appendCustomKey(Resource resource, RenderContext renderContext) throws RepositoryException {
         HttpSession session = renderContext.getRequest().getSession();
         String customKey = "";
         if (resource.getNode().hasProperty(BY_SESSION_ATTR)) {
             if (resource.getNode().getPropertyAsString(BY_SESSION_ATTR).equals("true")) {
                 if (session.getAttribute(SESSION_ATTR_NAME) != null) {
                     customKey += (String) session.getAttribute(SESSION_ATTR_NAME);
                 }
             }
         }
         if (resource.getNode().hasProperty(BY_USER_PROPERTY)) {
             if (resource.getNode().getPropertyAsString(BY_USER_PROPERTY).equals("true")) {
                 customKey += resource.getNode().getSession().getUser().getProperty(USER_PROPERTY_NAME);
             }
         }
         return customKey;
     }
 
     @Override
     public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
         List l = (List) renderContext.getRequest().getAttribute("module.cache.additional.key");
         if (l == null) {
             return super.execute(previousOut, renderContext, resource, chain);
         }
         l.remove(appendCustomKey(resource, renderContext));

         return super.execute(previousOut, renderContext, resource, chain);
     }
 }
