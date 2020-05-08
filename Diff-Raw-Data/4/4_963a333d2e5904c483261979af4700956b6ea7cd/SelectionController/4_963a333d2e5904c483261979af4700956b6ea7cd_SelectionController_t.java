 /**
  *
  */
 package org.esupportail.opi.web.controllers.references;
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.Selection;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.web.beans.BeanTrtCmi;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractAccessController;
 import org.springframework.util.StringUtils;
 
 import java.util.*;
 
 
 /**
  * @author cleprous
  */
 public class SelectionController extends AbstractAccessController {
 
 
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = -385061645426193790L;
 
 	
 	/*
      ******************* PROPERTIES ******************* */
 
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
 
     /**
      * The list with one selection for treatments.
      */
     private Map<BeanTrtCmi, Selection> selections;
 
     /**
      * see {@link TrtCmiController}.
      */
     private TrtCmiController trtCmiController;
 	
 	/*
 	 ******************* INIT ************************* */
 
 
     /**
      * Constructors.
      */
     public SelectionController() {
         super();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         trtCmiController.reset();
         selections = new TreeMap<BeanTrtCmi, Selection>(new ComparatorString(BeanTrtCmi.class));
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         Assert.notNull(this.trtCmiController,
                 "property trtCmiController of class " + this.getClass().getName()
                         + " can not be null");
         reset();
     }
 	
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * Callback to enter selection form.
      *
      * @return String
      */
     public String goEnterSelection() {
         reset();
         return NavigationRulesConst.ENTER_SELECTION;
     }
 
 	/*
 	 ******************* METHODS ********************** */
 
 
     public void saveUpdate() {
         for (BeanTrtCmi b : selections.keySet()) {
             Selection s = selections.get(b);
             if (ctrl(s)) {
                 if (s.getId().equals(0)) {
                     //add
                     s = (Selection) getDomainService().add(s, getCurrentGest().getLogin());
                 } else {
                     //update
                     s = (Selection) getDomainService().update(s, getCurrentGest().getLogin());
                 }
 
                 b.getTraitementCmi().setSelection(s);
                 TraitementCmi t = (TraitementCmi) getDomainService().update(
                         b.getTraitementCmi(), getCurrentGest().getLogin());
                 getParameterService().updateTraitementCmi(t);
             } else {
                 if (!s.getId().equals(0)) {
                     b.getTraitementCmi().setSelection(null);
                     TraitementCmi t = (TraitementCmi) getDomainService().update(
                             b.getTraitementCmi(), getCurrentGest().getLogin());
                     getParameterService().updateTraitementCmi(t);
                 }
             }
         }
         if (!selections.isEmpty()) {
             addInfoMessage(null, "INFO.ENTER.SUCCESS");
         }
     }
 
     /**
      * Look For treatment commission.
      */
     public void lookForBeanTrtCmi() {
         selections = new HashMap<BeanTrtCmi, Selection>();
         List<BeanTrtCmi> b = null;
         Integer idCmi = trtCmiController.getIdCmi();
         if (idCmi != null && idCmi != 0) {
             Commission c = getParameterService().getCommission(
                     idCmi, null);
             trtCmiController.initAllTraitementCmi(c, true);
             b = trtCmiController.getAllTraitementCmi();
 
 
         } else {
             b = trtCmiController.getEtapeController().searchEtapeInCmi();
         }
         for (BeanTrtCmi be : b) {
             boolean testEtape = false;
             boolean addBe = false;
             if (StringUtils.hasText(trtCmiController.getEtapeController().getCodEtp())) {
                 testEtape = true;
                 if (be.getEtape().getCodEtp().toUpperCase()
                         .contains(
                                 trtCmiController.getEtapeController()
                                         .getCodEtp().toUpperCase())) {
                     addBe = true;
                 }
             }
             if (StringUtils.hasText(trtCmiController.getEtapeController().getLibWebVet())) {
                 testEtape = true;
                 if (be.getEtape().getLibWebVet().toUpperCase()
                         .contains(
                                 trtCmiController.getEtapeController()
                                         .getLibWebVet().toUpperCase())) {
                     addBe = true;
                 }
             }
            
             Selection s = be.getTraitementCmi().getSelection();
             if (s == null) {
                 s = new Selection();
 
             }
             if (!testEtape || (testEtape && addBe)) {
                 selections.put(be, s);
             }
         }
 
         if (log.isDebugEnabled()) {
             log.debug("leaving lookForBeanTrtCmi with selections = " + selections);
         }
     }
 
     /**
      * @param s
      * @return False if all attributes are empty
      */
     public Boolean ctrl(final Selection s) {
         if (StringUtils.hasText(s.getPlace())) {
             return true;
         }
         if (StringUtils.hasText(s.getPeriodeAdmissibilite())) {
             return true;
         }
         if (StringUtils.hasText(s.getResultSelection())) {
             return true;
         }
         if (StringUtils.hasText(s.getComment())) {
             return true;
         }
         return false;
     }
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
     /**
      * @return Set < BeanTrtCmi>
      */
     public List<BeanTrtCmi> getBeansTrt() {
         return new ArrayList<BeanTrtCmi>(selections.keySet());
     }
 
 
     /**
      * @return the selections
      */
     public Map<BeanTrtCmi, Selection> getSelections() {
         return selections;
     }
 
     /**
      * @param selections the selections to set
      */
     public void setSelections(final Map<BeanTrtCmi, Selection> selections) {
         this.selections = selections;
     }
 
 
     /**
      * @param trtCmiController the trtCmiController to set
      */
     public void setTrtCmiController(final TrtCmiController trtCmiController) {
         this.trtCmiController = trtCmiController;
     }
 
 
 }
