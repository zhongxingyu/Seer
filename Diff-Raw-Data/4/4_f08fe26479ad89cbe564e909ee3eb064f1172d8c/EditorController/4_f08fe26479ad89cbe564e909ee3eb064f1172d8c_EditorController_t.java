 package com.bouncingdata.plfdemo.controller;
 
 import java.security.Principal;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.bouncingdata.plfdemo.datastore.pojo.dto.Attachment;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.DashboardDetail;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.DashboardPosition;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.VisualizationDetail;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.VisualizationType;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Analysis;
 import com.bouncingdata.plfdemo.datastore.pojo.model.AnalysisDataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Dataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.User;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Visualization;
 import com.bouncingdata.plfdemo.service.ApplicationExecutor;
 import com.bouncingdata.plfdemo.service.ApplicationStoreService;
 import com.bouncingdata.plfdemo.service.DatastoreService;
 import com.bouncingdata.plfdemo.util.Utils;
 
 
 @Controller
 @RequestMapping(value="/editor")
 public class EditorController {
   
   private Logger logger = LoggerFactory.getLogger(this.getClass());
   
   @Autowired
   private DatastoreService datastoreService;
   
   @Autowired
   private ApplicationStoreService appStoreService;
   
   @Autowired
   private ApplicationExecutor appExecutor;
   
   @RequestMapping(method = RequestMethod.GET)
   public String errorPage() {
     return "error";
   }
   
   @RequestMapping(value="/anls/{guid}/{mode}", method = RequestMethod.GET)
   public String openEditor(@PathVariable String guid, @PathVariable String mode, ModelMap model, Principal principal) {
     
     if (!"edit".equals(mode) && !"size".equals(mode) && !"describe".equals(mode)) {
       model.addAttribute("errorMsg", "Unknown page.");
       return "error";
     }
     
     // business logic here
     try {
       Analysis anls = datastoreService.getAnalysisByGuid(guid);
       if (anls == null) {
         model.addAttribute("errorMsg", "Analysis not found!");
         return "error";
       }
       
       User user = (User) ((Authentication)principal).getPrincipal();
       if (user == null || (!user.getUsername().equals(anls.getUser().getUsername()) && !anls.isPublished())) {
         model.addAttribute("errorMsg", "This analysis is not public!");
         return "error";
       }
       
       if (anls.getUser().getUsername().equals(user.getUsername())) {
         model.addAttribute("isOwner", true);
       } else model.addAttribute("isOwner", false);
       
       model.addAttribute("anls", anls);
       
       String code = appStoreService.getScriptCode(guid, null);
       model.addAttribute("anlsCode", StringEscapeUtils.escapeJavaScript(code));
       
       if ("edit".equals(mode)) {
         return "editor";
       }
       
       if ("size".equals(mode)) {
         List<Visualization> visuals = datastoreService.getAnalysisVisualizations(anls.getId());
         Map<String, VisualizationDetail> visualsMap = null;
         if (visuals != null) {
           visualsMap = new HashMap<String, VisualizationDetail>();
           for (Visualization v : visuals) {
             if ("html".equals(v.getType())) {
               visualsMap.put(v.getName(), new VisualizationDetail(v.getGuid(), "visualize/app/" + guid + "/" + v.getGuid() + "/html", VisualizationType.getVisualType(v.getType())));
             } else if ("png".equals(v.getType())) {
               try {
                 String source = appStoreService.getVisualization(guid, v.getGuid(), v.getType());
                 visualsMap.put(v.getName(), new VisualizationDetail(v.getGuid(), source, VisualizationType.getVisualType(v.getType())));
               } catch (Exception e) {
                 if (logger.isDebugEnabled()) {
                   logger.debug("Error occurs when retrieving visualizations {} from analysis {}", v.getGuid(), guid);
                   logger.debug("Exception detail", e);
                 }
                 continue;
               }
             }
           }
         }
         Map<String, DashboardPosition> dashboard = Utils.parseDashboard(anls);
   
         DashboardDetail dbDetail = new DashboardDetail(visualsMap, dashboard);
         ObjectMapper mapper = new ObjectMapper();
         model.addAttribute("dashboardDetail", mapper.writeValueAsString(dbDetail));
         
         // retrives related datasets
         try {
           List<AnalysisDataset> relations = datastoreService.getAnalysisDatasets(anls.getId());
           if (relations != null) {
             // key: dataset guid, value: dataset name
             Map<String, String> datasetList = new HashMap<String, String>();
             for (AnalysisDataset relation : relations) {
               Dataset ds = relation.getDataset();
               datasetList.put(ds.getGuid(), ds.getName());
             }
             model.addAttribute("datasetList", datasetList);
           }
         } catch (Exception e) {
           logger.debug("Error when trying to get relation datasets", e);
         }
         
         List<Attachment> attachments = appStoreService.getAttachmentData(guid);
         model.addAttribute("attachments", attachments);
         return "size";
       }
       
      if ("describe".equals(mode)) {
        return "describe";
      }
      
     } catch (Exception e) {
       logger.debug("Failed to load analysis {}", guid);
       model.addAttribute("errorMsg", e.getMessage());
       return "error";
     }
     
     return "editor";
   }
   
   
   
 }
