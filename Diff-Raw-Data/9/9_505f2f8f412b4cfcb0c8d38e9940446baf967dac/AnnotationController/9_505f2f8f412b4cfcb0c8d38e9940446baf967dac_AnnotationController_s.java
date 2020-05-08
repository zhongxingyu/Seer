 package edu.dfci.cccb.mev.annotation.server.controllers;
 
 import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 import static org.springframework.web.bind.annotation.RequestMethod.PUT;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 import static edu.dfci.cccb.mev.dataset.rest.context.RestPathVariableDatasetRequestContextInjector.*;
 
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 
 import lombok.extern.log4j.Log4j;
 
 import org.springframework.core.env.Environment;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.support.WebApplicationObjectSupport;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.refine.RefineServlet;
 import com.google.refine.io.FileProjectManager;
 
 import edu.dfci.cccb.mev.dataset.domain.contract.Dataset;
 import edu.dfci.cccb.mev.dataset.domain.contract.DatasetNotFoundException;
 import edu.dfci.cccb.mev.dataset.domain.contract.InvalidDatasetNameException;
 import edu.dfci.cccb.mev.dataset.domain.contract.Workspace;
 import edu.dfci.cccb.mev.dataset.domain.mock.DatasetMock;
 import freemarker.ext.beans.BeansWrapper;
 import freemarker.template.TemplateHashModel;
 import freemarker.template.TemplateModelException;
 import static java.util.Arrays.asList;
 import edu.dfci.cccb.mev.dataset.domain.contract.Dimension.Type;
 import java.math.RoundingMode;
 @Controller
 @RequestMapping ("/annotations")
 @Log4j
 public class AnnotationController extends WebApplicationObjectSupport {
 
   private RefineServlet refineServlet;
   private @Inject Environment environment;
   private @Inject Workspace workspace;
   private @Inject FileProjectManager projectManager;
   
   @PostConstruct
   private void createRefineServlet () throws ServletException {
     refineServlet = new RefineServlet ();
     refineServlet.init (new DelegatingServletConfig ());
   }
 
   @RequestMapping ("/")
   public ModelAndView annotationsHome () throws InvalidDatasetNameException, TemplateModelException {
 
     try {
       workspace.get ("boom");
     } catch (DatasetNotFoundException e) {
       Dataset mockHeatmap = new DatasetMock ("boom", "aaa,bbb,ccc", "a,b,c");       
       workspace.put (mockHeatmap);
     }
     try {
       workspace.get ("shmock");
     } catch (DatasetNotFoundException e) {
       Dataset mockHeatmap = new DatasetMock ("shmock", "aaa,bbb,ccc", "e,f,g");       
       workspace.put (mockHeatmap);
     }
 
     ModelAndView mav = new ModelAndView ();
    /*
    BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
    TemplateHashModel enumModels = wrapper.getEnumModels();
    TemplateHashModel dimensionColumnTypeEnum =
        (TemplateHashModel) enumModels.get(Type.class.getCanonicalName ());
    mav.addObject("ColumnType", dimensionColumnTypeEnum);
    */    
        
     mav.addObject ("workspace", workspace);
     mav.setViewName ("annotations");
     return mav;
 
   }
   
 
   @RequestMapping (method = { GET, POST, PUT, DELETE }, value = { "/"+DATASET_URL_ELEMENT+"/annotation/"+DIMENSION_URL_ELEMENT+"/**" })
   @ResponseBody
   public void handleAnnotation (@PathVariable (DATASET) final String heatmapId,
                                 @PathVariable (DIMENSION) final String dimension,
                                 HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, DatasetNotFoundException {
     log.debug (String.format ("Handling annotation request: %s", request.getServletPath ()));    
     
     
     HttpServletRequest wrappedRequest = new HttpServletRequestWrapper (request) {
       @Override
       public String getPathInfo () {
         return super.getServletPath ().replace ("/annotations/" + heatmapId + "/annotation/" + dimension, "");
       }      
     };
 
     Dataset heatmap = workspace.get (heatmapId);
     long projectId=projectManager.getProjectID (heatmap.name ());
     if(projectId!=-1){
       if(wrappedRequest.getPathInfo().trim().equals("/")){
         if(wrappedRequest.getParameter ("reset")!=null){
           projectManager.deleteProject (projectId);
         }else{
           response.sendRedirect ("project?project="+projectId);
           return;
         }
       }
     }
     
     wrappedRequest.setAttribute ("dataset", heatmap);
     wrappedRequest.setAttribute ("dimension", dimension);    
     this.refineServlet.service (wrappedRequest, response);    
   }
 
   /**
    * Internal implementation of the ServletConfig interface, to be passed to the
    * wrapped servlet. Delegates to ServletWrappingController fields and methods
    * to provide init parameters and other environment info.
    */
   private class DelegatingServletConfig implements ServletConfig {
 
     private Properties properties;
 
     {
       properties = new Properties ();
       properties.setProperty ("refine.version",
                               environment.getProperty ("refine.version", "$VERSION"));
       properties.setProperty ("refine.revision", environment.getProperty ("refine.revision", "$REVISION"));
       properties.setProperty ("refine.data",
                               environment.getProperty ("refine.data", System.getProperty ("java.io.tmpdir")));
     }
 
     public String getServletName () {
       return "refine";
     }
 
     public ServletContext getServletContext () {
       return AnnotationController.this.getServletContext ();
     }
 
     public String getInitParameter (String paramName) {
       return properties.getProperty (paramName);
     }
 
     @SuppressWarnings ({ "unchecked", "rawtypes" })
     public Enumeration getInitParameterNames () {
       return properties.keys ();
     }
   }
 }
