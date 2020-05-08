 package org.otherobjects.cms.controllers;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.otherobjects.cms.controllers.renderers.ResourceRenderer;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.SiteFolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 /**
  * Site controller handles all site page requests. For custom application functionality
  * you will need to create and map your own controllers.
  * 
  * @author rich
  */
 @Controller
 public class SiteController extends AbstractController
 {
     private final Logger logger = LoggerFactory.getLogger(SiteController.class);
 
     private DaoService daoService;
 
     private Map<String, ResourceRenderer> handlers = new HashMap<String, ResourceRenderer>();
 
     @Override
     @RequestMapping("/**")
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         //        if (true)
         //        {
         //            // Return page and context
         //            ModelAndView view = new ModelAndView("layouts/three-column.ftl");
         //            view.addObject("counter", 1);
         //            return view;
         //        }
         //        else
         //        {
         // Make sure folders end with slash
         String path = request.getPathInfo();
         this.logger.info("Requested resource: {}", path);
 
         if (path == null)
             path = "/";
         else if (path.length() > 1 && !path.contains(".") && !path.endsWith("/"))
             path = path + "/";
 
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         BaseNode resourceObject = null;
         if (path.contains("."))
         {
             // Page requested
             resourceObject = universalJcrDao.getByPath("/site" + path);
         }
         else
         {
             // Folder requested. If this folder has it's own template then
             // render that.
             resourceObject = universalJcrDao.getByPath("/site" + path);
 
             if (resourceObject instanceof SiteFolder)
             {
                 resourceObject = null;
                 // Folder requested. Get first item that isn't a folder.
                 List<BaseNode> contents = universalJcrDao.getAllByPath("/site" + path);
 
                 for (BaseNode n : contents)
                 {
                     if (!n.isFolder())
                     {
                         resourceObject = n;
                         break;
                     }
                 }
                 Assert.notNull(resourceObject, "No resources in this folder.");
             }
         }
 
         Assert.notNull(resourceObject, "No matching node for path: " + path);
 
         // Pass control to handler
         //        ResourceHandler handler = handlers.get(resourceObject.getClass().getName());
         //        if (handler == null)
         ResourceRenderer handler = handlers.get("*");
         return handler.handleRequest(resourceObject, request, response);
     }
 
     public Map<String, ResourceRenderer> getHandlers()
     {
         return handlers;
     }
 
     public void setHandlers(Map<String, ResourceRenderer> handlers)
     {
         this.handlers = handlers;
     }

    public void setDaoService(DaoService daoService)
    {
        this.daoService = daoService;
    }
 }
