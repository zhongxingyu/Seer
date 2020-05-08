 /**
  * 
  */
 package org.esco.portlet.changeetab.mvc.controller;
 
 import java.util.Collection;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import org.esco.portlet.changeetab.model.Etablissement;
 import org.esco.portlet.changeetab.service.IEtablissementService;
 import org.esco.portlet.changeetab.service.IUserInfoService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.portlet.ModelAndView;
 import org.springframework.web.portlet.mvc.AbstractController;
 
 /**
  * @author GIP RECIA 2013 - Maxime BOSSARD.
  *
  */
 @Controller
 @RequestMapping("VIEW")
 public class ChangeEtablissementController extends AbstractController {
 
 	/** Logger. */
 	private static final Logger LOG = LoggerFactory.getLogger(ChangeEtablissementController.class);
 
 	private static final String ETAB_LIST_KEY = "etabs";
 
 	@Autowired
 	private IUserInfoService userInfoService;
 
 	@Autowired
 	private IEtablissementService etablissementService;
 
 	@Override
 	public void handleActionRequestInternal(final ActionRequest request, final ActionResponse response) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public ModelAndView handleRenderRequestInternal(final RenderRequest request, final RenderResponse response) throws Exception {
 		ChangeEtablissementController.LOG.debug("Rendering Change Etablissement portlet View...");
 
		final ModelAndView mv = new ModelAndView("indexnop");
 
 		final String uaiCourant = this.userInfoService.getEscoUaiCourant(request);
 		final Collection<String> changeableUais = this.userInfoService.getEscoUai(request);
 
 		final Collection<Etablissement> changeableEtabs = this.etablissementService.retrieveEtablissementsByUais(changeableUais);
 
		ChangeEtablissementController.LOG.debug("Found [{}] etablissements whose use user can change to.", changeableEtabs.size());
 
 		mv.addObject(ChangeEtablissementController.ETAB_LIST_KEY, changeableEtabs);
 
 		return mv;
 	}
 
 	/**
 	 * Getter of userInfoService.
 	 *
 	 * @return the userInfoService
 	 */
 	public IUserInfoService getUserInfoService() {
 		return this.userInfoService;
 	}
 
 	/**
 	 * Setter of userInfoService.
 	 *
 	 * @param userInfoService the userInfoService to set
 	 */
 	public void setUserInfoService(final IUserInfoService userInfoService) {
 		this.userInfoService = userInfoService;
 	}
 
 	/**
 	 * Getter of etablissementService.
 	 *
 	 * @return the etablissementService
 	 */
 	public IEtablissementService getEtablissementService() {
 		return this.etablissementService;
 	}
 
 	/**
 	 * Setter of etablissementService.
 	 *
 	 * @param etablissementService the etablissementService to set
 	 */
 	public void setEtablissementService(final IEtablissementService etablissementService) {
 		this.etablissementService = etablissementService;
 	}
 
 
 }
