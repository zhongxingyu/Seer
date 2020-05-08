 package org.esupportail.opi.web.beans.paginator;
 
 import org.esupportail.commons.services.i18n.I18nService;
 import org.esupportail.commons.services.paginator.ListPaginator;
 import org.esupportail.commons.utils.ContextUtils;
 import org.esupportail.opi.domain.DomainApoService;
 import org.esupportail.opi.domain.ParameterService;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.web.beans.BeanTrtCmi;
 import org.esupportail.opi.web.beans.pojo.RechVetDTO;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.SessionController;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.util.StringUtils;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 
 /**
  *
  *
  */
 public class VETPaginator extends ListPaginator<VersionEtapeDTO> {
 
     /**
      * currentGest.
      */
     private Gestionnaire currentGest;
 
     /**
      * The serialization id.getString
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * la liste des versions d'étape
      */
     private VersionEtapeDTO etape;
 
     /**
      * DTO servant de critères de recherche au VETPaginator
      */
     private RechVetDTO rvd;
 
     /**
      * The DomainApoService
      */
     private DomainApoService domainApoService;
 
     /**
      * The SessionController.
      */
     private SessionController sessionController;
 
     /**
      * see {@link I18nService}.
      */
     private I18nService i18nService;
 
     /**
      * The parameterService.
      */
     private ParameterService parameterService;
 
 
 	/**
      * Constructors.
      */
     public VETPaginator() {
         super();
         this.currentGest = new Gestionnaire();
         this.rvd = new RechVetDTO();
         rvd.setCampagnes(new ArrayList<Campagne>());
         reset();
 
     }
 
 
     /**
      * Look for the commissions.
      */
     public void lookForVets() {
         reset();
         forceReload();
         //Tests si assez de critères (une commission ou un centre de gestion)
         //et sinon si pas de vet trouvée
         if ((rvd.getCodCge()==null ||rvd.getCodCge().equalsIgnoreCase("")) && rvd.getIdCmi() == null) {
             addMissingCriteriaMessage();
         } else {
             if (getData().isEmpty())
                 addEmptyDataMessage();
         }
         
     }
 
     /**
      * @see org.esupportail.commons.dao.AbstractHibernatePaginator#afterPropertiesSet()
      *      <p/>
      *      méthode qui permet de retourner une liste d'étapes par commission ou par code vet/lib vet/code cge
      *      gérée par le paginator
      */
     @Override
     public List<VersionEtapeDTO> getData() {
         List<VersionEtapeDTO> etapes = new ArrayList<VersionEtapeDTO>();
         Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         int codeRI = gest.getProfile().getCodeRI();
 
         //Dans le cas d'une recherche par commission. recherche indépendate du code vet lib vet ou code Cge
        if (rvd.getIdCmi() != null) {
             Commission commission = getParameterService().getCommission(rvd.getIdCmi(), null);
 
             for (TraitementCmi t : commission.getTraitementCmi()) {
 
                 if (!Utilitaires.isTraitementCmiOff(t, codeRI)) {
                     BeanTrtCmi b = new BeanTrtCmi(t,
                             getParameterService().getTypeTraitements());
                     VersionEtapeDTO vdto = getVetFromTrtCmi(b);
                     etapes.add(vdto);
                 }
             }
 
         } else {//Dans le cas d'une recherche par code vet, lib vet ou code Cge. recherche indépendante de recherche par commission
             if (StringUtils.hasText(rvd.getCodCge()) && StringUtils.hasLength(rvd.getCodAnu())) {
                 Campagne campagne = parameterService.getCampagneEnServ(codeRI);
                 etapes = domainApoService.getVersionEtapes(rvd.getCodeVet(), rvd.getLibWebVet(), rvd.getCodCge(), campagne.getCodAnu());
                 Collections.sort(etapes, new ComparatorString(VersionEtapeDTO.class));
             }
         }
 
 
         return etapes;
     }
 
     /**
      * Renvoie une VersionEtapeDTO en fonction du code etape et du code version etape stocké dans un objet BeanTrtCmi
      *
      * @param bt
      * @return VersionEtapeDTO
      */
     private VersionEtapeDTO getVetFromTrtCmi(final BeanTrtCmi bt) {
         return sessionController.getDomainApoService().getVersionEtape(
                 bt.getTraitementCmi().getVersionEtpOpi().getCodEtp(),
                 bt.getTraitementCmi().getVersionEtpOpi().getCodVrsVet());
     }
 
 
     /**
      * Ajoute un message signalant des critères de recherche manquants
      */
     private void addMissingCriteriaMessage() {
         addMessage("PJ.MISSING_CRITERIA_FOR_SEARCH");
     }
 
     /**
      * Ajoute un message signalant des critères de recherche manquants
      */
     private void addEmptyDataMessage() {
         addMessage("PJ.EMPTY_DATA");
     }
 
     /**
      * @param message Construit un message
      */
     private void addMessage(String message) {
         Locale locale;
 
         if (ContextUtils.isWeb()) {
             locale = (Locale) ContextUtils.getSessionAttribute("locale");
         } else
             locale = LocaleContextHolder.getLocale();
 
         if (locale == null)
             locale = i18nService.getDefaultLocale();
 
         String mess = i18nService.getString(message, locale);
         FacesMessage fm = new FacesMessage(mess);
         FacesContext.getCurrentInstance().addMessage(null, fm);
     }
 
 
     /**
      * @return etape
      */
     public VersionEtapeDTO getEtape() {
         return etape;
     }
 
     /**
      * @param etape
      */
     public void setEtape(VersionEtapeDTO etape) {
         this.etape = etape;
     }
 
     /**
      * @return the  current Gestionnaire
      */
     public Gestionnaire getCurrentGest() {
         return currentGest;
     }
 
     /**
      * @param currentGest
      */
     public void setCurrentGest(Gestionnaire currentGest) {
         this.currentGest = currentGest;
     }
 
 
     /**
      * @return the rechVetDTO
      */
     public RechVetDTO getRvd() {
         return rvd;
     }
 
     /**
      * @param rvd
      */
     public void setRvd(RechVetDTO rvd) {
         this.rvd = rvd;
     }
 
     /**
      * @return the sessionController
      */
     public SessionController getSessionController() {
         return sessionController;
     }
 
     /**
      * @param sessionController
      */
     public void setSessionController(SessionController sessionController) {
         this.sessionController = sessionController;
     }
 
     /**
      * @return the parameter Service
      */
     public ParameterService getParameterService() {
         return parameterService;
     }
 
     /**
      * @param parameterService
      */
     public void setParameterService(ParameterService parameterService) {
         this.parameterService = parameterService;
     }
 
     /**
      * @return the domainApoService
      */
     public DomainApoService getDomainApoService() {
         return domainApoService;
     }
 
     /**
      * @param domainApoService
      */
     public void setDomainApoService(final DomainApoService domainApoService) {
         this.domainApoService = domainApoService;
     }
 
     /**
      * @return i18nService
      */
     public I18nService getI18nService() {
         return i18nService;
     }
 
     /**
      * @param i18nService
      */
     public void setI18nService(I18nService i18nService) {
         this.i18nService = i18nService;
     }
     
 
 }
