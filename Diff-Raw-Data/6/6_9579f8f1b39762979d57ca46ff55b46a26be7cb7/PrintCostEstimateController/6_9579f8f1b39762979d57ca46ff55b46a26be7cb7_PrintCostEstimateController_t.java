 package com.ffe.web.overlay;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.ffe.common.exception.GTSException;
 import com.ffe.common.framework.util.CommonControllerUtil;
 import com.ffe.estimate.model.EstimateCommonTrailerInfo;
 import com.ffe.estimate.model.EstimateCostHeader;
 import com.ffe.estimate.model.EstimateCosting;
 import com.ffe.estimate.model.FilmEstimate;
 import com.ffe.estimate.model.PrintCostEstimate;
 import com.ffe.estimate.model.ServiceTrailerWrapper;
 import com.ffe.estimate.service.EstimateService;
 import com.ffe.estimate.service.FilmEstimateService;
 import com.ffe.service.model.DigitalCostsVendor;
 import com.ffe.service.model.DigitalService;
 import com.ffe.service.service.DigitalCostService;
 import com.ffe.title.model.Title;
 
 
 @Controller
 public class PrintCostEstimateController {
 	
 	@Autowired
 	private DigitalCostService digitalCostService ;
 	
 	@Autowired
 	private EstimateService estimateService ;		
 	
 	@Autowired
 	private FilmEstimateService filmEstimateService ;	
 		
 	
 	public void setFilmEstimateService(FilmEstimateService filmEstimateService) {
 		this.filmEstimateService = filmEstimateService;
 	}
 
 	public void setDigitalCostService(DigitalCostService digitalCostService) {
 		this.digitalCostService = digitalCostService;
 	}
 
 	public void setEstimateService(EstimateService estimateService) {
 		this.estimateService = estimateService;
 	}
 
 	@Autowired
 	private CommonControllerUtil commonControllerUtil;
 
 	public CommonControllerUtil getCommonControllerUtil() {
 		return commonControllerUtil;
 	}
 	
 	
 	private static final Logger log = LoggerFactory.getLogger(PrintCostEstimateController.class);
 	
  /*	@RequestMapping(value = "/loadServicePricingEstimateForm")
 	public String getServicePricingEstimateForm(Model model, HttpServletRequest request) throws GTSException{
 		log.info("Inside EstimateOverLayDisplayController.getServicePricingEstimateForm"+request.getRequestURI());
 		try {			 
 			model.addAttribute("estimate", new Estimate());	
 			model.addAttribute("title", new Title());	
 			populateModelWithReferenceTypes(model);				
 		} catch(Exception e){
 			log.error("Exception Occured in EstimateOverLayDisplayController.getServicePricingEstimateForm : ",e);
 			throw new GTSException(e.getMessage(),e.getCause());
 		}
 		return "film_service_pricing_overlay"; 
 	}*/
 
 	@RequestMapping(value = "/saveServicePricingEstimate")
 	public String saveServicePricingEstimate(@ModelAttribute(value="estimate")PrintCostEstimate estimate, BindingResult result,
 			Model model, HttpServletRequest request) throws GTSException{
 		log.info("Inside PrintCostEstimateController.saveServicePricingEstimate"+ estimate);
 		try {			 
 			FilmEstimate filmEstimate = null;
 			System.out.println("estimate" +estimate);
 			PrintCostEstimate saved_estimate = this.estimateService.saveEstimate(estimate);
 			if (null!=saved_estimate){
 			filmEstimate =this.filmEstimateService.getFilmEstimate(saved_estimate.getFilmEstimateId());
 			}
 			model.addAttribute("estimate", filmEstimate);
 			model.addAttribute("filmEstimate", filmEstimate);
 			model = commonControllerUtil.getModelObject(model);
 			model.addAttribute("returnView", "form");			 
 //			model.addAttribute("searchcriteria", new SearchCriteria());			 		
 		} catch(Exception e){
 			e.printStackTrace();
 			log.error("Exception Occured in EstimateOverLayDisplayController.saveServicePricingEstimate : ",e);
 			throw new GTSException(e.getMessage(),e.getCause());
 		}
 		return "estimate_form"; 
 	}
 	
 	private void populateModelWithReferenceTypes(Model model) throws GTSException {			
 		model.addAttribute("digitalFeatureServiceList", digitalCostService.lstServiceByCostType(1L));
 		model.addAttribute("digitalTrailerServiceList", digitalCostService.lstServiceByCostType(2L));
 		model.addAttribute("dubbingServiceList", digitalCostService.lstServiceByCostType(3L));
 		model.addAttribute("allServiceList", digitalCostService.lstAllService());	
 		
 		model.addAttribute("gbp_usd_exchange_rate", 1.53);
 		model.addAttribute("euro_usd_exchange_rate", 1.35);
 		model.addAttribute("gbp_euro_exchange_rate", 1.13);
 		
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		Date date = new Date();		
 		model.addAttribute("today_date", dateFormat.format(date));
 		
 	}
 	
 	
 	@RequestMapping(value = "/loadServicePricingEstimateForm")
 	public String getServicePricingEstimateForm(@RequestParam(value = "featureServiceTemplateId") Long featureServiceTemplateId,
 			@RequestParam(value = "dubbingServiceTemplateId") Long dubbingServiceTemplateId,
 			@RequestParam(value = "trailerServiceTemplateId") Long trailerServiceTemplateId,
 			@RequestParam(value = "filmEstimateId") Long filmEstimateId,
 			Model model, HttpServletRequest request) throws GTSException{	
 		log.info("Inside EstimateOverLayDisplayController.getServicePricingEstimateForm");
 		try {			
 			
 			List<DigitalService> services_list = null;
 			PrintCostEstimate printCostEstimate= null; 
 			printCostEstimate=estimateService.getEstimateByFilmEstimateID(filmEstimateId);
 		
 			log.debug("-----------------printCostEstimate-------before----->"+printCostEstimate);
 			
 			if (null==printCostEstimate){
 				
				printCostEstimate = new PrintCostEstimate();	
			}
 				printCostEstimate.setFilmEstimateId(filmEstimateId);
 				
 				printCostEstimate.setLstEstimateCostingDigitalFeature(new ArrayList<EstimateCosting>());
 				printCostEstimate.setLstEstimateCostingDubbing(new ArrayList<EstimateCosting>());
 				printCostEstimate.setLstEstimateCostingTrailering(new ArrayList<ServiceTrailerWrapper>());
 				printCostEstimate.setLstEstimateCostingOthers(new ArrayList<EstimateCosting>());
 				printCostEstimate.setEstimateCostHeader(new EstimateCostHeader());
 				printCostEstimate.setCommonTrailerSection(new ArrayList<EstimateCommonTrailerInfo>());		
 				
 				
 				if(featureServiceTemplateId != null && featureServiceTemplateId != 0){
 					services_list =  digitalCostService.lstServicebyTemplate(featureServiceTemplateId);					
 					for(DigitalService dst :services_list){
 						EstimateCosting estCost =  new EstimateCosting();
 						estCost.setServiceId(dst.getDigiServiceId());
 						estCost.setEstimateCostTypeId(1L);
 						printCostEstimate.getLstEstimateCostingDigitalFeature().add(estCost);
 					}							
 				}
 				
 				if(dubbingServiceTemplateId != null && dubbingServiceTemplateId != 0){
 					services_list =  digitalCostService.lstServicebyTemplate(dubbingServiceTemplateId);						
 					for(DigitalService dst :services_list){
 						EstimateCosting estCost =  new EstimateCosting();
 						estCost.setServiceId(dst.getDigiServiceId());
 						estCost.setEstimateCostTypeId(2L);
 						printCostEstimate.getLstEstimateCostingDubbing().add(estCost);
 					}							
 				}
 				
 				if(trailerServiceTemplateId != null && trailerServiceTemplateId != 0){
 					services_list =  digitalCostService.lstServicebyTemplate(trailerServiceTemplateId);					
 					for(DigitalService dst :services_list){
 						ServiceTrailerWrapper stw =  new ServiceTrailerWrapper();
 						stw.setServiceId(dst.getDigiServiceId());
 						stw.setEstimateCostTypeId(3L);
 						printCostEstimate.getLstEstimateCostingTrailering().add(stw);
 					}							
 				}
 			
 			
			
 			
 			log.debug("-----------------printCostEstimate----after-------->"+printCostEstimate);
 			
 		/*	est.setEstimateId(filmEstimateId); // check here
 			
 			serviceTemplateId = 1L;
 			if(serviceTemplateId != null && serviceTemplateId != 0){
 				List<DigitalService> services_list =  digitalCostService.lstServicebyTemplate(serviceTemplateId);			
 				est.setLstEstimateCostingDigitalFeature(new ArrayList<EstimateCosting>());
 				for(DigitalService dst :services_list){
 					EstimateCosting estCost =  new EstimateCosting();
 					estCost.setServiceId(dst.getDigiServiceId());
 					estCost.setEstimateCostTypeId(1L);
 					est.getLstEstimateCostingDigitalFeature().add(estCost);
 				}
 			}	
 				
 			est.setLstEstimateCostingOthers(new ArrayList<EstimateCosting>());				
 			EstimateCosting estCost1 =  new EstimateCosting();
 			//estCost1.setAdhocServiceName("adhocServiceName1");
 			estCost1.setEstimateCostTypeId(4L);
 			estCost1.setDesc("v this is a description 111");
 			est.getLstEstimateCostingOthers().add(estCost1);
 			
 			EstimateCosting estCost2 =  new EstimateCosting();
 			//estCost2.setAdhocServiceName("adhocServiceName2");
 			estCost2.setDesc("v this is a description 222");
 			estCost2.setEstimateCostTypeId(4L);
 			est.getLstEstimateCostingOthers().add(estCost2);									
 							
 									
 			est.setCommonTrailerSection(new ArrayList<EstimateCostHeaderExtended>());
 			EstimateCostHeaderExtended eche1 = new EstimateCostHeaderExtended();
 			eche1.setTrailerId(1L);	
 			eche1.setFilmLength(new BigDecimal(88.0));
 			eche1.setNumberOfDigitalVersions(9L);
 			eche1.setNumberOfOriginalVersions(27L);
 			eche1.setNumberOfLocalVersions(18L);
 			eche1.setTagged("vtag1");			
 			est.getCommonTrailerSection().add(eche1);
 			
 			EstimateCostHeaderExtended eche2 = new EstimateCostHeaderExtended();
 			eche1.setTrailerId(2L);	
 			eche2.setFilmLength(new BigDecimal(99.0));
 			eche2.setNumberOfDigitalVersions(33L);
 			eche2.setNumberOfOriginalVersions(22L);
 			eche2.setNumberOfLocalVersions(11L);
 			eche2.setTagged("vtag2");						
 			est.getCommonTrailerSection().add(eche2); 
 			
 			
 			serviceTemplateId = 3L;
 			if(serviceTemplateId != null && serviceTemplateId != 0){
 				List<DigitalService> services_list =  digitalCostService.lstServicebyTemplate(serviceTemplateId);			
 				est.setLstEstimateCostingTrailering(new ArrayList<ServiceTrailerWrapper>());
 				for(DigitalService dst :services_list){
 					ServiceTrailerWrapper estCost =  new ServiceTrailerWrapper();
 					estCost.setServiceId(dst.getDigiServiceId());
 					estCost.setEstimateCostTypeId(1L);
 					est.getLstEstimateCostingTrailering().add(estCost);
 				}							
 			}		
 			
 			est.setLstEstimateCostingTrailering(new ArrayList<ServiceTrailerWrapper>());			
 			ServiceTrailerWrapper estCost3 =  new ServiceTrailerWrapper();
 			estCost3.setTrailerCosts(new ArrayList<TrailerSTWDisplay>());
 			
 			TrailerSTWDisplay tsd1 =  new TrailerSTWDisplay();
 			tsd1.setTrailerServiceCost(new BigDecimal(88.0));
 			tsd1.setTrailerUXId("tst-1");			
 			estCost3.getTrailerCosts().add(tsd1);
 			
 			TrailerSTWDisplay tsd2 =  new TrailerSTWDisplay();
 			tsd2.setTrailerServiceCost(new BigDecimal(66.0));
 			tsd2.setTrailerUXId("tst-2");			
 			estCost3.getTrailerCosts().add(tsd2);
 			
 			TrailerSTWDisplay tsd3 =  new TrailerSTWDisplay();
 			tsd3.setTrailerServiceCost(new BigDecimal(44.0));
 			tsd3.setTrailerUXId("tst-3");			
 			estCost3.getTrailerCosts().add(tsd3);
 			
 			TrailerSTWDisplay tsd4 =  new TrailerSTWDisplay();
 			tsd4.setTrailerServiceCost(new BigDecimal(22.0));
 			tsd4.setTrailerUXId("tst-4");			
 			estCost3.getTrailerCosts().add(tsd4);
 			
 			estCost3.setEstimateCostTypeId(3L);
 			est.getLstEstimateCostingTrailering().add(estCost3);
 			
 			ServiceTrailerWrapper estCost4 =  new ServiceTrailerWrapper();
 			estCost4.setTrailerCosts(new ArrayList<TrailerSTWDisplay>());
 			
 			TrailerSTWDisplay tsd5 =  new TrailerSTWDisplay();
 			tsd5.setTrailerServiceCost(new BigDecimal(22.0));
 			tsd5.setTrailerUXId("tst-1");			
 			estCost4.getTrailerCosts().add(tsd5);
 			
 			TrailerSTWDisplay tsd6 =  new TrailerSTWDisplay();
 			tsd6.setTrailerServiceCost(new BigDecimal(44.0));
 			tsd6.setTrailerUXId("tst-2");			
 			estCost4.getTrailerCosts().add(tsd6);
 			
 			TrailerSTWDisplay tsd7 =  new TrailerSTWDisplay();
 			tsd7.setTrailerServiceCost(new BigDecimal(66.0));
 			tsd7.setTrailerUXId("tst-3");			
 			estCost4.getTrailerCosts().add(tsd7);
 			
 			TrailerSTWDisplay tsd8 =  new TrailerSTWDisplay();
 			tsd8.setTrailerServiceCost(new BigDecimal(88.0));
 			tsd8.setTrailerUXId("tst-4");			
 			estCost4.getTrailerCosts().add(tsd8);
 			
 			estCost4.setEstimateCostTypeId(3L);
 			est.getLstEstimateCostingTrailering().add(estCost4);*/
 				
 			model.addAttribute("estimate", printCostEstimate);	
 			model.addAttribute("title", new Title());	
 			populateModelWithReferenceTypes(model);	
 			
 			log.debug("-----------------model------------>"+model);
 			
 		} catch(GTSException ex){
 			log.error("Exception Occured in EstimateOverLayDisplayController.getServicePricingEstimateForm : ",ex);
 			throw ex;
 		} catch(Exception e){
 			log.error("Exception Occured in EstimateOverLayDisplayController.getServicePricingEstimateForm : ",e);
 			throw new GTSException(e.getMessage(),e.getCause());
 		}
 		return "film_service_pricing_overlay"; 
 	}
 	
 
 	@RequestMapping(value = "/getVendors")
 	public String getVendors(@RequestParam (value = "serviceId")  long serviceId,
 			@RequestParam (value = "costTypeId")  long costTypeId,
 			@RequestParam (value = "current_row_id")  long current_row_id, Model model, HttpServletRequest request) throws GTSException{
 		log.info("Inside EstimateOverLayDisplayController.getServicePricingEstimateForm");
 		List<DigitalCostsVendor> lstofVendors = null;
 		try {
 			lstofVendors = digitalCostService.lstVendorBasedonService("Talent_Fee","3");	
 			 model.addAttribute("lstofVendors", lstofVendors);	
 			 model.addAttribute("current_row_id", current_row_id);
 			 model.addAttribute("costTypeId", costTypeId);
 		} catch(Exception e){
 			log.error("Exception Occured in EstimateOverLayDisplayController.getServicePricingEstimateForm : ",e);
 				throw new GTSException(e.getMessage(),e.getCause());
 		}
 		return "vendor_results";
 	}
 	
 	
 }
