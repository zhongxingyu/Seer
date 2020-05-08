 package org.dpi.creditsEntry;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.dpi.category.CategoryService;
 import org.dpi.centroSector.CentroSectorService;
 import org.dpi.creditsEntry.CreditsEntry.GrantedStatus;
 import org.dpi.creditsManagement.CreditsManagerService;
 import org.dpi.creditsPeriod.CreditsPeriodService;
 import org.dpi.employment.EmploymentQueryFilter;
 import org.dpi.employment.EmploymentService;
 import org.dpi.reparticion.Reparticion;
 import org.dpi.reparticion.ReparticionController;
 import org.janux.bus.security.Account;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class CreditsEntryController {
 	
 	private CreditsEntryService creditsEntryService;
 	
 	private EmploymentService employmentService;
 
 	@Resource(name = "creditsManagerService")
 	private CreditsManagerService creditsManagerService;
 
 	@Resource(name = "categoryService")
 	private CategoryService categoryService;
 	
 	@Resource(name = "centroSectorService")
 	private CentroSectorService centroSectorService;
 	
 	
 	@Resource(name = "creditsPeriodService")
 	private CreditsPeriodService creditsPeriodService;
 	
 	@InitBinder
 	public void initBinder(WebDataBinder binder){
 	    binder.setAutoGrowNestedPaths(false);
 	}
 
 
 	@Inject
 	public CreditsEntryController(CreditsEntryService creditsEntryService) {
 		this.creditsEntryService = creditsEntryService;
 	}
 
 
 	public CreditsManagerService getCreditsManagerService() {
 		return creditsManagerService;
 	}
 
 	public void setCreditsManagerService(
 			CreditsManagerService creditsManagerService) {
 		this.creditsManagerService = creditsManagerService;
 	}
 
 	public EmploymentService getEmploymentService() {
 		return employmentService;
 	}
 
 
 	public void setEmploymentService(EmploymentService employmentService) {
 		this.employmentService = employmentService;
 	}
 
 	public CategoryService getCategoryService() {
 		return categoryService;
 	}
 
 
 	public void setCategoryService(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 
 
 	public CentroSectorService getCentroSectorService() {
 		return centroSectorService;
 	}
 
 
 	public void setCentroSectorService(CentroSectorService centroSectorService) {
 		this.centroSectorService = centroSectorService;
 	}
 	
 	
 	@RequestMapping(value = "/reparticiones/creditsentries/{id}/setupFormCambiarEstadoCreditsEntry", method = RequestMethod.GET)
 	public String setupFormCambiarEstadoCreditsEntry(@PathVariable Long id, Model model){  
 			      
           
 		CreditsEntryQueryFilter creditsEntryQueryFilter = new CreditsEntryQueryFilter();
 		
 		creditsEntryQueryFilter.setId(id);
 		
 		
 		List<CreditsEntry> movimientos = creditsEntryService.find(creditsEntryQueryFilter);
 
 		CreditsEntry creditsEntry = movimientos.get(0);
           
 		CreditsEntryForm creditsEntryForm = new CreditsEntryForm(creditsEntry);
 		
 		
 		model.addAttribute("creditsEntryForm", creditsEntryForm);
 		model.addAttribute("grantedStatuses", GrantedStatus.values());
 
         
        return "creditsentries/cambioEstadoMovimientoForm";
     }
 	
 	
 	@RequestMapping(value = "/creditsentries/cambiarEstadoMovimiento", method = RequestMethod.POST)
 	public String processFormCambiarEstadoMovimiento(HttpServletRequest request,
 													@ModelAttribute("creditsEntryForm") CreditsEntryForm creditsEntryForm,
 													Model model,
 													BindingResult result) throws Exception {
 		
 
 		//retrieve the creditsEntry from database
 		CreditsEntryQueryFilter creditsEntryQueryFilter = new CreditsEntryQueryFilter();
 		creditsEntryQueryFilter.setId(creditsEntryForm.getCreditsEntryId());
 				
 		List<CreditsEntry> movimientos = creditsEntryService.find(creditsEntryQueryFilter);
 		CreditsEntry creditsEntry = movimientos.get(0);
 
           
 		creditsEntryService.changeGrantedStatus(creditsEntry, creditsEntryForm.getGrantedStatus());  
           
         //String message = "Team was successfully edited.";  
         //modelAndView.addObject("message", message);  
 
         String creditsPeriodName =  creditsEntry.getCreditsPeriod().getName();
 		return "redirect:/reparticiones/reparticion/showCreditEntries/"+creditsPeriodName;
     }  
 	
 	
 	/**
 	 * Usado para AngularJS
 	 * @param request
 	 * @param response
 	 * @return
 	 */
 	@RequestMapping(value = "/reparticiones/reparticion/creditsentries", method = RequestMethod.GET)
     @ResponseBody
 	public List<CreditsEntryDTO> getMovimientos(
 			HttpServletRequest request, 
 			HttpServletResponse response/*,
 			@PathVariable Long reparticionId, Model model*/) {
 
 		// get the current reparticion in the session
 		final Reparticion reparticion = ReparticionController.getCurrentReparticion(request);
 
 		List<CreditsEntryDTO> creditsEntriesDTO = new ArrayList<CreditsEntryDTO>();
 		if (reparticion != null){
 
 
 			EmploymentQueryFilter employmentQueryFilter = new EmploymentQueryFilter();
 			employmentQueryFilter.setReparticionId(reparticion.getId().toString());
 
 			CreditsEntryQueryFilter creditsEntryQueryFilter = new CreditsEntryQueryFilter();
 			creditsEntryQueryFilter.setEmploymentQueryFilter(employmentQueryFilter);
 			creditsEntryQueryFilter.addCreditsEntryType(CreditsEntryType.CargaInicialAgenteExistente);
 			creditsEntryQueryFilter.addCreditsEntryType(CreditsEntryType.AscensoAgente);
 			creditsEntryQueryFilter.addCreditsEntryType(CreditsEntryType.BajaAgente);
 			creditsEntryQueryFilter.addCreditsEntryType(CreditsEntryType.IngresoAgente);
 
 			List<CreditsEntry> creditsEntryReparticion = creditsEntryService.find(creditsEntryQueryFilter);
 			
 			Object accountObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			Account currenUser = (Account)accountObj;
 					
 			List<CreditsEntryVO> creditsEntryVOReparticion = creditsEntryService.buildCreditsEntryVO(creditsEntryReparticion,currenUser);
 			
 			for(CreditsEntryVO creditsEntryAscensoVO :creditsEntryVOReparticion){
 				CreditsEntryDTO creditsEntryDTO = new CreditsEntryDTO();
 				creditsEntryDTO.setName(creditsEntryAscensoVO.getCreditsEntry().getEmployment().getPerson().getApellidoNombre());
 				creditsEntriesDTO.add(creditsEntryDTO);
 			}
 					
 			//model.addAttribute("movimientos", creditsEntryVOReparticion);
 			
 			//creditos disponibles
 			//long creditosDisponibles = administradorCreditosService.getCreditosDisponiblesSegunSolicitado(reparticion.getId());
 			
 			//model.addAttribute("creditosDisponibles", creditosDisponibles);
 			
 		}
 		return creditsEntriesDTO;
 	}
 	
 	
 	
     @RequestMapping(value = "/reparticiones/creditsentries/processCambiarMultipleEstadoMovimiento", method = RequestMethod.POST)
     public String processCambiarMultipleEstadoMovimiento(@ModelAttribute("cambiosMultiplesEstadoMovimientosForm") CambiosMultiplesEstadoMovimientosForm cambiosMultiplesEstadoMovimientosForm) {
 
         List<CreditsEntry> movimientos = cambiosMultiplesEstadoMovimientosForm.getMovimientos();
          
         for(CreditsEntry submittedMovimiento :movimientos){
         	if(submittedMovimiento!=null){
         		CreditsEntryQueryFilter creditsEntryQueryFilter = new CreditsEntryQueryFilter();
         		creditsEntryQueryFilter.setId(submittedMovimiento.getId());
         		List<CreditsEntry> listCreditsEntry = creditsEntryService.find(creditsEntryQueryFilter);
         		CreditsEntry creditsEntry = listCreditsEntry.get(0);
         		if(creditsEntry.getGrantedStatus()!=submittedMovimiento.getGrantedStatus()){
         			//creditsEntry.setGrantedStatus(submittedMovimiento.getGrantedStatus());
         			//creditsEntryService.saveOrUpdate(creditsEntry);
         			creditsEntryService.changeGrantedStatus(creditsEntry,submittedMovimiento.getGrantedStatus());
         		}
         		
         	}
 
         }
          
         return "redirect:/reparticiones/reparticion/showCreditEntries/"+creditsPeriodService.getCurrentCreditsPeriodYear();
 
     }
 	
 
 
 }
