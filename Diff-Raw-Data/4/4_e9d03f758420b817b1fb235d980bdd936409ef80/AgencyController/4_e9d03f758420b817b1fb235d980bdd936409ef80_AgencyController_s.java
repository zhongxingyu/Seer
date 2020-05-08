 package au.gov.nsw.records.search.web;
 
 import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import au.gov.nsw.records.search.model.Activity;
 import au.gov.nsw.records.search.model.Agency;
 import au.gov.nsw.records.search.model.Functionn;
 import au.gov.nsw.records.search.model.Organisation;
 import au.gov.nsw.records.search.model.Serie;
 import au.gov.nsw.records.search.service.ControllerUtils;
 
 @RequestMapping("/agencies")
 @Controller
 @RooWebScaffold(path = "agencies", formBackingObject = Agency.class, update=false, create=false, delete=false)
 public class AgencyController {
 	
 	@RequestMapping(produces = "text/html")
     public String list(@RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
         if (page != null || size != null) {
             int sizeNo = size == null ? 10 : size.intValue();
             final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
             uiModel.addAttribute("agencys", Agency.findAgencyEntries(firstResult, sizeNo));
             float nrOfPages = (float) Agency.countAgencys() / sizeNo;
             uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
             uiModel.addAttribute("page", page);
             uiModel.addAttribute("size", size);
         } else {
             uiModel.addAttribute("agencys", Agency.countAgencys());
         }
         uiModel.addAttribute("view", "agencies/list");
         uiModel.addAttribute("count", Activity.countActivitys());
         addDateTimeFormatPatterns(uiModel);
         return "agencies/list";
     }
 
 	@RequestMapping(value = "/{agencyNumber}", produces = "text/html")
     public String show(@PathVariable("agencyNumber") int agencyNumber, Model uiModel, 
     		@RequestParam(value = "functions_page", required = false, defaultValue="1") Integer functions_page,
     		@RequestParam(value = "organisations_page", required = false, defaultValue="1") Integer organisations_page,
     		@RequestParam(value = "persons_page", required = false, defaultValue="1") Integer persons_page,
     		@RequestParam(value = "series_created_page", required = false, defaultValue="1") Integer series_created_page,
     		@RequestParam(value = "series_controlled_page", required = false, defaultValue="1") Integer series_controlled_page,
     		@RequestParam(value = "preceding_page", required = false, defaultValue="1") Integer preceding_page,
     		@RequestParam(value = "related_page", required = false, defaultValue="1") Integer related_page,
     		@RequestParam(value = "subordinates_page", required = false, defaultValue="1") Integer subordinates_page,
     		@RequestParam(value = "succeeding_page", required = false, defaultValue="1") Integer succeeding_page,
     		@RequestParam(value = "superiors_page", required = false, defaultValue="1") Integer superiors_page) {
         addDateTimeFormatPatterns(uiModel);
         uiModel.addAttribute("agency", Agency.findAgency(agencyNumber));
         uiModel.addAttribute("itemId", agencyNumber);
         
         if (Agency.findAgency(agencyNumber)!=null){
 	        int size = 5;
 	        int arraySize =  Agency.findAgency(agencyNumber).getFunctions().size();
 	        uiModel.addAttribute("rel_functions",  Agency.findAgency(agencyNumber).getFunctions().subList(Math.max((functions_page-1)*size, 0), Math.min(functions_page*size, arraySize)));
 	        uiModel.addAttribute("rel_functions_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_functions_page", functions_page);
 	        
 	        arraySize =  Agency.findAgency(agencyNumber).getOrganisations().size();
 	        uiModel.addAttribute("rel_organisations",  Agency.findAgency(agencyNumber).getOrganisations().subList(Math.max((organisations_page-1)*size, 0), Math.min(organisations_page*size, arraySize)));
 	        uiModel.addAttribute("rel_organisations_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_organisations_page", organisations_page);
 	        
 	        arraySize =  Agency.findAgency(agencyNumber).getPersons().size();
 	        uiModel.addAttribute("rel_persons",  Agency.findAgency(agencyNumber).getPersons().subList(Math.max((persons_page-1)*size, 0), Math.min(persons_page*size, arraySize)));
 	        uiModel.addAttribute("rel_persons_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_persons_page", persons_page);
 	        
 	        arraySize =  Agency.findAgency(agencyNumber).getSeriesCreated().size();
 	        uiModel.addAttribute("rel_series_created",  Agency.findAgency(agencyNumber).getSeriesCreated().subList(Math.max((series_created_page-1)*size, 0), Math.min(series_created_page*size, arraySize)));
 	        uiModel.addAttribute("rel_series_created_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_series_created_page", series_created_page);
 	        
 	        arraySize =  Agency.findAgency(agencyNumber).getSeriesControlled().size();
 	        uiModel.addAttribute("rel_series_controlled",  Agency.findAgency(agencyNumber).getSeriesControlled().subList(Math.max((series_controlled_page-1)*size, 0), Math.min(series_controlled_page*size, arraySize)));
 	        uiModel.addAttribute("rel_series_controlled_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_series_controlled_page", series_controlled_page);
 	        
 	        arraySize =  Agency.findAgency(agencyNumber).getPreceding().size();
 	        uiModel.addAttribute("rel_preceding",  Agency.findAgency(agencyNumber).getPreceding().subList(Math.max((preceding_page-1)*size, 0), Math.min(preceding_page*size, arraySize)));
 	        uiModel.addAttribute("rel_preceding_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_preceding_page", preceding_page);
 	        
 	        arraySize =  Agency.findAgency(agencyNumber).getRelated().size();
 	        uiModel.addAttribute("rel_related",  Agency.findAgency(agencyNumber).getRelated().subList(Math.max((related_page-1)*size, 0), Math.min(related_page*size, arraySize)));
 	        uiModel.addAttribute("rel_related_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_related_page", related_page);
 	        
 	        arraySize =  Agency.findAgency(agencyNumber).getSubordinates().size();
 	        uiModel.addAttribute("rel_subordinates",  Agency.findAgency(agencyNumber).getSubordinates().subList(Math.max((subordinates_page-1)*size, 0), Math.min(subordinates_page*size, arraySize)));
 	        uiModel.addAttribute("rel_subordinates_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_subordinates_page", subordinates_page);
 	        
 	        arraySize =  Agency.findAgency(agencyNumber).getSucceeding().size();
 	        uiModel.addAttribute("rel_succeeding",  Agency.findAgency(agencyNumber).getSucceeding().subList(Math.max((succeeding_page-1)*size, 0), Math.min(succeeding_page*size, arraySize)));
 	        uiModel.addAttribute("rel_succeeding_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_succeeding_page", succeeding_page);
 	        
	        arraySize =  Agency.findAgency(agencyNumber).getSucceeding().size();
	        uiModel.addAttribute("rel_superiors",  Agency.findAgency(agencyNumber).getSucceeding().subList(Math.max((superiors_page-1)*size, 0), Math.min(superiors_page*size, arraySize)));
 	        uiModel.addAttribute("rel_superiors_size", Double.valueOf(Math.ceil(arraySize/(float)size)).intValue());
 	        uiModel.addAttribute("rel_superiors_page", superiors_page);
         }
         uiModel.addAttribute("view", "agencies/show");
         return "agencies/show";
     }
 	
 	@RequestMapping(value="/{agencyNumber}/functions", produces = "text/html")
 	public String listFunctions(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getFunctions(), "functionns", page, size, uiModel, Functionn.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "functions/list");
 		return "functions/list";
 	}
 	
 	@RequestMapping(value="/{agencyNumber}/organisations", produces = "text/html")
 	public String listOrganisations(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getOrganisations(), "organisations", page, size, uiModel, Organisation.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "organisations/list");
 		return "organisations/list";
 	}
 	
 	@RequestMapping(value="/{agencyNumber}/persons", produces = "text/html")
 	public String listPersons(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getPersons(), "persons", page, size, uiModel, Organisation.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "persons/list");
 		return "persons/list";
 	}
 	
 	@RequestMapping(value="/{agencyNumber}/series_created", produces = "text/html")
 	public String listSeriesCreated(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getSeriesCreated(), "series", page, size, uiModel, Serie.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "series/list");
 		return "series/list";
 	}
 	
 	@RequestMapping(value="/{agencyNumber}/series_controlled", produces = "text/html")
 	public String listSeriesControlled(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getSeriesControlled(), "series", page, size, uiModel, Serie.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "series/list");
 		return "series/list";
 	}
 	
 	// preceding
 	@RequestMapping(value="/{agencyNumber}/preceding", produces = "text/html")
 	public String listPreceding(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getPreceding(), "agencys", page, size, uiModel, Agency.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "agencies/list");
 		return "agencies/list";
 	}
   // related
 	@RequestMapping(value="/{agencyNumber}/related", produces = "text/html")
 	public String listRelated(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getRelated(), "agencys", page, size, uiModel, Agency.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "agencies/list");
 		return "agencies/list";
 	}
 	// succeeding
 	@RequestMapping(value="/{agencyNumber}/succeeding", produces = "text/html")
 	public String listSucceeding(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getSucceeding(), "agencys", page, size, uiModel, Agency.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "agencies/list");
 		return "agencies/list";
 	}
 	
 	// superior
 	@RequestMapping(value="/{agencyNumber}/superior", produces = "text/html")
 	public String listSuperior(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getSuperiors(), "agencys", page, size, uiModel, Agency.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "agencies/list");
 		return "agencies/list";
 	}
 	
 	@RequestMapping(value="/{agencyNumber}/subordinate", produces = "text/html")
 	public String listSubordinate(@PathVariable("agencyNumber") int agencyNumber, @RequestParam(value = "page", required = false, defaultValue="1") Integer page, @RequestParam(value = "size", required = false, defaultValue="30") Integer size, Model uiModel) {
 
 		Agency ag = Agency.findAgency(agencyNumber);
 		if (ag!=null){
 			ControllerUtils.populateRelationshipModel(ag.getSubordinates(), "agencys", page, size, uiModel, Agency.class);
 			addDateTimeFormatPatterns(uiModel);
 		}
 		uiModel.addAttribute("view", "agencies/list");
 		return "agencies/list";
 	}
 }
