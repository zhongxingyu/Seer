 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.web.controllers;
 
 import com.google.gson.Gson;
 import com.mne.advertmanager.model.*;
 import com.mne.advertmanager.service.*;
 import com.mne.advertmanager.util.Page;
 import com.mne.advertmanager.util.PageCtrl;
 import java.io.IOException;
 import java.util.Collection;
 import javax.servlet.http.HttpServletResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author Misha
  */
 @Controller
 public class ProgramDetailsController {
 
     //logger
     private static Logger logger = LoggerFactory.getLogger(ProgramDetailsController.class);
 
     
     //constant declaration
     private static final String AFFPROGRAM = "affprograms";
     private static final String DETAILS = "/details";
     private static final String ORDERS = "/orders";
     private static final String ACCESS = "/access";
     private static final String FINANCE = "/finance";
     private static final String AFFPROGRAM_DETAILS_REQ_MAPPING = AFFPROGRAM + DETAILS;
     private static final String AFFPROGRAM_ACCESS_REQ_MAPPING = AFFPROGRAM + "/{programId}/items/{items}/accessPage/{pageNumber}";
     private static final String AFFPROGRAM_CALL_AGGR_DATA_REQ_MAPPING = AFFPROGRAM + "/{programId}/calculateAggregationData";
     private static final String AFFPROGRAM_ORDERS_REQ_MAPPING = AFFPROGRAM + ORDERS;
     private static final String AFFPROGRAM_FINANCE_REQ_MAPPING = AFFPROGRAM + FINANCE;
     private static final String BLNG_IMPORT_REQ_MAPPING = ControllerSupport.BILLING + "/import";
 
     //variables and object declarations
     private Gson gson = new Gson();
     
     private AffProgramService affProgramService;
     private AccessLogService accessLogService;
     private PurchaseOrderService purchaseOrderService;
     private AffiliateService affiliateService;
     private BillingProjectService billingProjectService;
     private BehaviorStatisticsService fbsService;
 
 //functions
 //==============================================================================
 //function that respond to access list request 
 //========================== viewAffProgDefintionForm ==========================
     @RequestMapping(value = AFFPROGRAM_DETAILS_REQ_MAPPING + "/{programId}", method = RequestMethod.GET)
     public ModelAndView viewProgramDetails(@PathVariable int programId) {
 
         //ATTENTION BE CAREFUL WITH "programId" PARAMETER THAT YOU RECEIVE BECAUSE EVE MAY INCLUDE
         //SQL INJECTION HERE SO YOU MUST CHECK IT BEFORE QUERY IT!!
 
         AffProgram program = affProgramService.findAffProgramByID(programId);
 
         Page<AccessLog> accessPage = null;
         Collection<PurchaseOrder> orderList = null;
         FilterableBehaviorStatistics totalStats = null;
         FilterableBehaviorStatistics pmStats = null;
         FilterableBehaviorStatistics cmStats = null;
         FilterableBehaviorStatistics dailyStats = null;
         if (program != null) {
             //only find data for valid programs
             //find all accesses related to this program
             accessPage = accessLogService.findAccessByAffProgamId(new PageCtrl(), programId);
             orderList  = purchaseOrderService.findPurchaseOrdersByAffProgamId(programId);
             totalStats = fbsService.findTotalAffProgramStatistics(programId);
             pmStats = fbsService.findPrevMonthAffProgramStatistics(programId);
             cmStats = fbsService.findCurMonthAffProgramStatistics(programId);
             dailyStats = fbsService.findDailyAffProgramGeneralStats(programId);
             
         }
 
 
         ModelAndView mav = ControllerSupport.forwardToView(logger, AFFPROGRAM_DETAILS_REQ_MAPPING + "/" + programId, AFFPROGRAM_DETAILS_REQ_MAPPING, "program", program);
         mav.addObject("accessPage", accessPage);
         mav.addObject("orderList", orderList);
         mav.addObject("partnerList", program.getPartners());
         mav.addObject("totalStats", totalStats);
         mav.addObject("pmStats", pmStats);
         mav.addObject("cmStats", cmStats);
         mav.addObject("dailyStats", dailyStats);
 
         return mav;
 
     }
 
 //============================= importBillingData ==============================
     /**
      * This function start importing process of data for specified AffProgramm this function invoked from by web link and has context variable affProgramId
      */
     @RequestMapping(value = BLNG_IMPORT_REQ_MAPPING + "/{affProgramId}", method = RequestMethod.GET)
     public ModelAndView importBillingData(@PathVariable final int affProgramId, SecurityContextHolderAwareRequestWrapper securityContextWrapper, HttpServletResponse response) {
 
         String status = null; // hold msg that will apear to user upon failure/success.
 
         //get current affName
         String affName = securityContextWrapper.getUserPrincipal().getName();
         //find curent affiliate by name
         final Affiliate aff = affiliateService.findAffiliateWithAffPrograms(affName);
         //get security context data
         final SecurityContext securityContext = SecurityContextHolder.getContext();
         //find wanted affProgram
         final AffProgram program = affProgramService.findAffProgramByID(affProgramId);
 
         //run data collection in separate Thread:
         try {
             new Thread() {
                 //prepare new thread function 
 
                 @Override
                 public void run() {
                     //set security context of this Thread
                     SecurityContextHolder.setContext(securityContext);
                     //set thread name for debug purposes
                     setName(ControllerSupport.BILLING + "DataImportThread" + " programId " + affProgramId);
                     //go collect data:
                     billingProjectService.importBillingData(program);
                     
                     affProgramService.save(program);
                 }
                 //start thread execution
             }.start();
 
 
             status = "Started importing data for program id=" + affProgramId;
         } catch (Exception e) {
             status = ControllerSupport.handleException(logger, e, "import", "Program Data", "id=" + affProgramId);
         }
 
         //transfer user back to import page(same place he came from)
         ModelAndView mav = viewProgramDetails(affProgramId);
         mav.addObject("status", status);
 
         return mav;
     }
 ////=============================== calculateAggregationData ================================
 
     @RequestMapping(value = AFFPROGRAM_CALL_AGGR_DATA_REQ_MAPPING, method = RequestMethod.GET)
     public ModelAndView calculateAggregationData(@PathVariable final int programId, SecurityContextHolderAwareRequestWrapper securityContextWrapper, HttpServletResponse response) {
 
         // hold msg that will apear to user upon failure/success.
         String status = null; 
 
         //get security context data
         final SecurityContext securityContext = SecurityContextHolder.getContext();
         
         //run aggregation data calculation in separate Thread:
         try {
             new Thread() {
                 //prepare new thread function 
                 @Override
                 public void run() {
                     //set security context of this Thread
                     SecurityContextHolder.setContext(securityContext);
                     //set thread name for debug purposes
                     setName(AFFPROGRAM + "AggrDataCalculationThread" + " programId " + programId);
                     //calculate aggregation data:
                     fbsService.calculateAffProgramStatistics(programId);
                 }
                 //start thread execution
             }.start();
             status = "Started calculating aggregation data for program id=" + programId;
         } catch (Exception e) {
             status = ControllerSupport.handleException(logger, e, "AggregationCalculation", "Program Data", "id=" + programId);
         }
 
         //transfer user back to import page(same place he came from)
         ModelAndView mav = viewProgramDetails(programId);
         mav.addObject("status", status);
 
         return mav;
     }
 
     //=============================== getAccessPage ================================
     @RequestMapping(value = AFFPROGRAM_ACCESS_REQ_MAPPING, method = RequestMethod.GET)
     void getAccessPage(@PathVariable int items, @PathVariable int programId, @PathVariable int pageNumber, HttpServletResponse response) {
         try {
 
             Page<AccessLog> accessPage = accessLogService.findAccessByAffProgamId(new PageCtrl(pageNumber, 0, items), programId);
 
             String curRequest = AFFPROGRAM_ACCESS_REQ_MAPPING + "/" + programId + "/items/" + items + "/accessPage/" + pageNumber;
             logger.info(curRequest);
             String result = gson.toJson(accessPage);
             logger.info(result);
             response.getWriter().write(result);
         } catch (IOException e) {
             String errMsg = ",Exception:" + e.getClass().getSimpleName()
                     + ((e.getMessage() == null) ? "" : " ,Message:"
                     + e.getMessage());
 
             logger.error("failed to retrieve accessee of affprogram (id={},Exception:{})", programId, errMsg);
         }
     }
 
     //=============================== SETTERS ======================================
     @Autowired
     public void setAccessLogService(AccessLogService accessLogService) {
         this.accessLogService = accessLogService;
     }
 
     @Autowired
     public void setAffProgramService(AffProgramService affProgramService) {
         this.affProgramService = affProgramService;
     }
 
     @Autowired
     public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
         this.purchaseOrderService = purchaseOrderService;
     }
 
     //============================= setAffiliateService ============================
     @Autowired
     public void setAffiliateService(AffiliateService affiliateService) {
         this.affiliateService = affiliateService;
     }
 
     //============================= setBillingProjectService =======================    
     @Autowired
     public void setBillingProjectService(BillingProjectService billingProjectService) {
         this.billingProjectService = billingProjectService;
     }
 
     //============================= setPoAggrService =======================    
     @Autowired
     public void setFbsService(BehaviorStatisticsService fbsService) {
         this.fbsService = fbsService;
     }
 }
