 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.web.controllers;
 
 import com.google.gson.Gson;
 import com.mne.advertmanager.model.AffProgram;
 import com.mne.advertmanager.model.AffProgramGroup;
 import com.mne.advertmanager.model.Affiliate;
 import com.mne.advertmanager.parsergen.model.DataSpec;
 import com.mne.advertmanager.parsergen.model.Project;
 import com.mne.advertmanager.parsergen.model.SelectableItem;
 import com.mne.advertmanager.service.*;
 import com.mne.advertmanager.web.model.BillingSpec;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author Nina Eidelshtein and Misha Lebedev
  */
 @Controller
 @RequestMapping("/")
 public class AdvertManagerController {
 
     private static final String AFFILIATES = "affiliates";
     private static final String AFFPROGRAM = "affprograms";
     private static final String AFFPROG_GROUPS = "afprgroups";
 
     private static final String DATAGEN = "dataGen";
     private static final String APPS = "apps";
     
     private static final String APPS_PARSERGEN_REQ_MAPPING = APPS + "/parsergen";
     private static final String AFF_LIST_REQ_MAPPING = AFFILIATES + ControllerSupport.LIST;
     private static final String AFF_NEW_REQ_MAPPING = AFFILIATES + ControllerSupport.NEW;
     private static final String AFF_ADD_REQ_MAPPING = AFFILIATES + ControllerSupport.ADD;
     private static final String DG_GEN_REQ_MAPPING = DATAGEN + "/generate";
     private static final String AFFPROGRAM_NEW_REQ_MAPPING = AFFPROGRAM + ControllerSupport.NEW;
     private static final String AFFPROGRAM_ADD_REQ_MAPPING = AFFPROGRAM + ControllerSupport.ADD;
     private static final String AFFPROGRAM_LIST_REQ_MAPPING = AFFPROGRAM + ControllerSupport.LIST;
 
 
     private static final String BLNG_LIST_REQ_MAPPING = ControllerSupport.BILLING + ControllerSupport.LIST;
     private static final String BLNG_NEW_REQ_MAPPING = ControllerSupport.BILLING + ControllerSupport.NEW;
     private static final String BLNG_ADD_REQ_MAPPING = ControllerSupport.BILLING + ControllerSupport.ADD;
     private static final String BLNG_DETAILS_REQ_MAPPING = ControllerSupport.BILLING + "/details";
     private static final String BLNG_DELETE_REQ_MAPPING = ControllerSupport.BILLING + "/delete";
 
     //============= variables and objects ======================================
     
 
     private DataGenService dataGenerator;
     private AffiliateService affiliateService;
     private AffProgramService affProgramService;
     private AffProgramGroupService apgService;
     private BillingProjectService billingProjectService;
 
     private Gson gson = new Gson();
     private Unmarshaller jaxbUnmarshaller;
     
     private static final Logger logger = LoggerFactory.getLogger(AdvertManagerController.class);
 
     //C-tor
     public AdvertManagerController() {
         try {
             //preapare XML marshaler
             JAXBContext jaxbCtx = JAXBContext.newInstance(com.mne.advertmanager.parsergen.model.Project.class);
             jaxbUnmarshaller = jaxbCtx.createUnmarshaller();
         } catch (JAXBException ex) {
             logger.error(ex.toString());
         }
     }
 
 //=========================== redirect =========================================
     /**
      * this ctrl function redirect users from root URL to home page URL
      */
 //    @RequestMapping("/")
 //    public String redirect() {
 //        logger.info("redirecting to home page");
 //        return "redirect:/index";//mvc/home/";
 //    }
     
     
     @RequestMapping("/")
     public void loginPage() {
         logger.info("go to view / "); 
       
     }
     
 //=========================== goToMain =========================================
     /**
      * this ctrl function redirect users from root URL to home page URL
      */
    @RequestMapping(value="main",method = RequestMethod.GET)
     public void goToMain() {
         logger.info("go to main page");
     }
     
 //======================== generateHome ========================================
     /**
      * view resolution works through tiles configuration file WEB-INF/tiles-def/templates.xml tile which defines presentation automatically equals the url for example for url
      * "home" corresponding tile is <definition name="home" extends=".mainTemplate"> <put-attribute name="content" value="/WEB-INF/view/home.jsp" /> </definition>
      *
      * @param securityContext
      * @return
      */
     @RequestMapping(value = "home", method = RequestMethod.GET)
     public @ModelAttribute("data")
     Affiliate generateHome(SecurityContextHolderAwareRequestWrapper securityContext) {
         String affName = securityContext.getUserPrincipal().getName();
         Affiliate aff = affiliateService.findAffiliateWithAffPrograms(affName);
         return aff;
     }
 //========================== generateData ======================================
 
     @RequestMapping(value = DG_GEN_REQ_MAPPING, method = RequestMethod.GET)
     public ModelAndView generateData() {
         new Thread() {
 
             @Override
             public void run() {
                 setName(DATAGEN + "Thread");
                 dataGenerator.generateDummyData();
             }
         }.start();
 
         return ControllerSupport.forwardToView(logger,DG_GEN_REQ_MAPPING, "home", "status","Dummy data generation started.");
     }
 
 //========================== viewAffProgDefintionForm ==========================
     @RequestMapping(value = AFFPROGRAM_NEW_REQ_MAPPING, method = RequestMethod.GET)
     public ModelAndView viewAffProgDefintionForm(SecurityContextHolderAwareRequestWrapper securityContext) {
 
         ModelAndView mav = new ModelAndView(AFFPROGRAM_NEW_REQ_MAPPING);
         String affName = securityContext.getUserPrincipal().getName();
 
         Collection<AffProgramGroup> apGroups = apgService.findAffiliateProgramGroups(affName);
 
         mav.addObject("affprogram", new AffProgram());
         mav.addObject("apGroups", apGroups);
 
         return mav;
 
     }
 //============================ addAffProgram ===================================
 
     @RequestMapping(value = AFFPROGRAM_ADD_REQ_MAPPING, method = RequestMethod.POST)
     public ModelAndView addAffProgram(@ModelAttribute("affprogram") AffProgram affprogram, SecurityContextHolderAwareRequestWrapper securityContext) {
 
         String status = "";
         try {
             affProgramService.createAffProgram(affprogram);
             status = "Affprogram:" + affprogram.getName() + " created successfully";
         } catch (Exception e) {
             status = ControllerSupport.handleException(logger,e, "create", "Affprogram", affprogram.getName());
         }
 
         ModelAndView mav = ControllerSupport.forwardToView(logger,AFFPROGRAM_ADD_REQ_MAPPING, "home", "data", generateHome(securityContext));
 
         mav.addObject("status", status);
 
         return mav;
     }
 
 //=============================== viewAffPrograms ==============================
     @RequestMapping(value = AFFPROGRAM_LIST_REQ_MAPPING, method = RequestMethod.GET)
     public @ModelAttribute("data") Collection<AffProgram> viewAffPrograms() {
 
         logger.info("getting affprogram data");
 
         Collection<AffProgram> affprograms = affProgramService.findAllAffPrograms();
 
         int size = (affprograms != null) ? affprograms.size() : 0;
 
         logger.info("return affprogram data to view. found {} affprograms", size);
 
 
         return affprograms;
     }
 
 //=========================== viewAffiliates ===================================  
     @RequestMapping(value = AFF_LIST_REQ_MAPPING, method = RequestMethod.GET)
     public @ModelAttribute("data") Collection<Affiliate> viewAffiliates() {
 
         Collection<Affiliate> affiliates = affiliateService.findAllAffiliates();
 
         return affiliates;
     }
 //============================= addUser ========================================
 
     @RequestMapping(value = AFF_ADD_REQ_MAPPING, method = RequestMethod.POST)
     public ModelAndView addUser(@ModelAttribute("affiliate") Affiliate affiliate,SecurityContextHolderAwareRequestWrapper securityContext) {
 
         String status = null;
         try {
             affiliateService.createAffiliate(affiliate);
             status = "User:" + affiliate.getAffiliateName() + " created successfully";
         } catch (Exception e) {
             status = ControllerSupport.handleException(logger,e, "create", "affiliate", affiliate.getAffiliateName());
         }
 
         ModelAndView mav = null;
         if (securityContext.isUserInRole("ROLE_ADMIN")) {
             mav = ControllerSupport.forwardToView(logger,AFF_ADD_REQ_MAPPING, AFF_LIST_REQ_MAPPING, "data", viewAffiliates());
         } else {
             mav = ControllerSupport.forwardToView(logger,AFF_ADD_REQ_MAPPING, "login", null, null);
         }
 
         mav.addObject("status", status);
 
         return mav;
     }
 //============================ launchParserGenerator ===========================
 
     @RequestMapping(value = APPS_PARSERGEN_REQ_MAPPING, method = RequestMethod.GET)
     public @ModelAttribute("codebase") String launchParserGenerator(HttpServletRequest request) {
 
         String codebase = "http://" + request.getServerName() + ":"
                 + request.getServerPort()
                 + request.getServletContext().getContextPath()
                 + "/apps";
 
         logger.info("Returning codebase=" + codebase);
         return codebase;
     }
 
     @RequestMapping(value = AFF_NEW_REQ_MAPPING, method = RequestMethod.GET)
     public @ModelAttribute("affiliate") Affiliate viewRegistrationForm() {
 
         return new Affiliate();
     }
 
 //============================= getAffProgramGroup =============================
     @RequestMapping(value = AFFPROG_GROUPS + "/{pgId}", method = RequestMethod.GET)
     public void getAffProgramGroup(@PathVariable int pgId, HttpServletResponse response) {
         try {
             AffProgramGroup pg = apgService.findById(pgId);
             pg.setProgramCollection(null);
             pg.setAffiliateId(null);
             String result = gson.toJson(pg);
             logger.info(result);
             response.getWriter().write(result);
         } catch (IOException e) {
             String errMsg = ",Exception:" + e.getClass().getSimpleName()
                     + ((e.getMessage() == null) ? "" : " ,Message:"
                     + e.getMessage());
 
             logger.error("failed to retrieve affprogram  group (id={},Exception:{})", pgId, errMsg);
         }
     }
 //============================= viewBillingProjects ============================
 
     @RequestMapping(value = BLNG_LIST_REQ_MAPPING, method = RequestMethod.GET)
     public @ModelAttribute("data") Collection<Project> viewBillingProjects() {
 
         Collection<Project> result = billingProjectService.findAllBillingProjects();
 
         int nProjects = 0;
         if (result != null)
             nProjects = result.size();
         
         logger.info("{} ::: Found {} Billing project specifications", BLNG_LIST_REQ_MAPPING, nProjects);
         
         return result;
     }
 //============================= uploadBillingSpecification =====================
 
     /**
      * This function open Billing specification file, then create billing Project and store it in db.
      */
     @RequestMapping(value = BLNG_ADD_REQ_MAPPING, method = RequestMethod.POST)
     public ModelAndView uploadBillingSpecification(@ModelAttribute("billingSpec") BillingSpec blngSpec,
             SecurityContextHolderAwareRequestWrapper securityContext) {
 
         String status = null;
         MultipartFile specFile = null;  //spec file handler
         try {
             //get spec file that uploaded
             specFile = blngSpec.getSpecFile();
 
             //create new Billing Project form uploaded file data
             Project proj = (Project) jaxbUnmarshaller.unmarshal(specFile.getInputStream());
 
 
             //save Billing Project to DB
             billingProjectService.createProject(proj);
 
             //compose status massege
             status = "File " + specFile.getOriginalFilename() + " uploaded successfuly";
 
         } catch (Exception e) {
             String specName = "";
             if (specFile != null) {
                 specName += specFile.getName();
             }
             status = ControllerSupport.handleException(logger,e, "upload", "billingSpec", specName);
         }
 
         ModelAndView mav = null;
         mav = ControllerSupport.forwardToView(logger,BLNG_ADD_REQ_MAPPING, BLNG_LIST_REQ_MAPPING, "data", viewBillingProjects());
         mav.addObject("status", status);
 
         return mav;
     }
 //============================= viewUploadSpecForm =============================
 
     @RequestMapping(value = BLNG_NEW_REQ_MAPPING, method = RequestMethod.GET)
     public @ModelAttribute("billingSpec") BillingSpec viewUploadSpecForm() {
 
         return new BillingSpec();
     }
 //============================ viewBillingProjectDetails ================================
     @RequestMapping(value = BLNG_DETAILS_REQ_MAPPING+"/{projectId}", method = RequestMethod.GET)
     public ModelAndView viewBillingProjectDetails(@PathVariable int projectId) {
         
         Project proj = billingProjectService.findProjectById(projectId);
         
         ModelAndView result = ControllerSupport.forwardToView(logger,BLNG_DETAILS_REQ_MAPPING+"/"+projectId, BLNG_DETAILS_REQ_MAPPING, "project", proj);
         result.addObject("selectedDataSpec", 0);
         
         return result;
         
     }
     
     //========================= getBillingDataSpec ==============================
     @RequestMapping(value = BLNG_DETAILS_REQ_MAPPING + "/{bpId}"+"/ds/{dsId}", method = RequestMethod.GET)
     public void getBillingDataSpec(@PathVariable int bpId,@PathVariable int dsId, HttpServletResponse response) {
         try {
             DataSpec ds = billingProjectService.findProjectDataSpec(bpId,dsId);
             String result = "{}";
             if (ds == null) {
                 logger.error("Invalid billing project id - {} or dataspec id - {}",bpId,dsId);
             }else {
                 ds.setProject(null);
                 List<SelectableItem> siList = ds.getAllSubItems();
                 if (siList == null) {
                     logger.error("Project.Dataspec - {}.{} contains no items",bpId,dsId);
                 }else {
                     for (SelectableItem si:ds.getAllSubItems()) {
                         si.setDataSpec(null);
                     }
                     result = gson.toJson(ds);
                 }
             }
             logger.info(result);
             response.getWriter().write(result);
 
         }catch (IOException e) {
             String errMsg = ",Exception:" + e.getClass().getSimpleName() +
                 ((e.getMessage() == null) ? "" :
                 " ,Message:"  + e.getMessage());
             logger.error("failed to retrieve billing project dataspec  (bpId={},dsId={},Exception:{})", new Object[]{bpId,dsId, errMsg});
         }
     }
     
     
     //========================== deleteBillingProjectDetails ================================
     @RequestMapping(value = BLNG_DELETE_REQ_MAPPING+"/{projectId}", method = RequestMethod.GET)
     public ModelAndView deleteBillingProjectDetails(@PathVariable int projectId) {
         
         billingProjectService.delete(projectId);
         
         ModelAndView result = ControllerSupport.forwardToView(logger,BLNG_DELETE_REQ_MAPPING+"/"+projectId, BLNG_LIST_REQ_MAPPING, "status", "Successfully deleted project:"+projectId);
 
         result.addObject("data", viewBillingProjects());
         
         return result;
         
     }    
 
 
 
 //================================= SETTERS =====================================
 //============================= setDataGenerator ===============================
     @Autowired
     public void setDataGenerator(DataGenService dataGenerator) {
         this.dataGenerator = dataGenerator;
     }
 //============================= setAffiliateService ============================
 
     @Autowired
     public void setAffiliateService(AffiliateService affiliateService) {
         this.affiliateService = affiliateService;
     }
 //============================= setAffProgramService ===========================
 
     @Autowired
     public void setAffProgramService(AffProgramService affprogramService) {
 
         logger.info("AdvertManagerController:setAffProgramService...");
         this.affProgramService = affprogramService;
     }
 
 //============================= setAffProgramGroupService ======================
     @Autowired
     public void setAffProgramGroupService(AffProgramGroupService pgService) {
         this.apgService = pgService;
     }
 //============================= setBillingProjectService =======================    
 
     @Autowired
     public void setBillingProjectService(BillingProjectService billingProjectService) {
         this.billingProjectService = billingProjectService;
     }
 
 }
