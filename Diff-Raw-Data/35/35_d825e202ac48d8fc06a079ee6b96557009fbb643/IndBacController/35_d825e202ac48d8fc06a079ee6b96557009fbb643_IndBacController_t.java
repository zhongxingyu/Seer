 /**
  *
  */
 package org.esupportail.opi.web.controllers.user;
 
 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.ContextUtils;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.indcursus.IndBac;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.pojo.IndBacPojo;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractAccessController;
 import org.esupportail.wssi.services.remote.BacOuxEqu;
 import org.esupportail.wssi.services.remote.CommuneDTO;
 import org.esupportail.wssi.services.remote.Etablissement;
 import org.esupportail.wssi.services.remote.MentionNivBac;
 import org.springframework.util.StringUtils;
 
 
 /**
  * @author cleprous
  */
 public class IndBacController extends AbstractAccessController {
 
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = -7466022760402264721L;
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
 
 
 
 
 	/*
      ******************* PROPERTIES ******************* */
 
     /**
      * The IndBac.
      */
     private IndBac indBac;
 
     /**
      * The IndBac List.
      */
     private List<IndBacPojo> indBacPojos;
 
     /**
      * The actionEnum.
      */
     private ActionEnum actionEnum;
 	
 
 	/*
 	 ******************* INIT ************************* */
 
 
     /**
      * Constructors.
      */
     public IndBacController() {
         super();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         indBac = new IndBac();
         indBacPojos = new ArrayList<IndBacPojo>();
         indBac.setCodPay(Constantes.CODEFRANCE);
        indBac.setDateObtention(null);
         actionEnum = new ActionEnum();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractContextAwareController#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         super.afterPropertiesSetInternal();
     }
 
 
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * Callback to add ind cursus scol.
      *
      * @return String
      */
     public String goAddCursusScol() {
         if (!indBacPojos.isEmpty() || ctrlEnter(this.indBac)) {
             addTheCurrentRoad(NavigationRulesConst.ADD_CURSUS_SCOL,
                     getString("INDIVIDU.CURSUS_SCOL"), getString("INDIVIDU.CURSUS_SCOL"));
             return NavigationRulesConst.ADD_CURSUS_SCOL;
         }
         return null;
 
     }
 
     /**
      * Callback to see cursus details for the current connected user.
      *
      * @return String
      */
     public String goSeeIndBac() {
         reset();
        IndividuPojo i = getCurrentIndInit();
         if (i != null) {
             if (i.getIndividu().getIndBac() == null
                     || i.getIndividu().getIndBac().isEmpty()) {
                 actionEnum.setWhatAction(ActionEnum.ADD_ACTION);
             } else {
            	removeIndBac();
                 initIndBac(
                         new ArrayList<IndBac>(
                                 i.getIndividu().getIndBac()), false);
             }
         }
 
         return NavigationRulesConst.SEE_IND_BAC;
     }
 
     /**
      * CallBack to Update Cursus.
      */
     public String goUpdateIndBac() {
         return NavigationRulesConst.UPDATE_IND_BAC;
     }
 
 
 	/*
 	 ******************* METHODS ********************** */
 
     /**
      * The selected departement.
      *
      * @param event
      */
     public void selectDep(final ValueChangeEvent event) {
         String codeDep = (String) event.getNewValue();
         indBac.setCodDep(codeDep);
         FacesContext.getCurrentInstance().renderResponse();
     }
 
     /**
      * The selected commune.
      *
      * @param event
      */
     public void selectCommune(final ValueChangeEvent event) {
         String codeCom = (String) event.getNewValue();
         indBac.setCodCom(codeCom);
         FacesContext.getCurrentInstance().renderResponse();
     }
 
     /**
      * The selected pays.
      *
      * @param event
      */
     public void selectPay(final ValueChangeEvent event) {
         String codePay = (String) event.getNewValue();
         indBac.setCodPay(codePay);
         //SI Pays != france  on remet à null le code etablissement.
         if (!codePay.equals(Constantes.CODEFRANCE)) {
             indBac.setCodEtb(null);
             indBac.setCodDep(null);
             indBac.setCodCom(null);
         }
         FacesContext.getCurrentInstance().renderResponse();
     }
 
     /**
      * Remove Cursus in cursusList.
      */
     public void removeIndBac() {
         if (log.isDebugEnabled()) {
             log.debug("entering removeIndBac with indBac = " + indBac);
         }
         indBacPojos.remove(indBac);
     }
 
     /**
      * @param indBacs
      * @param initDossier value is true if initialisation du dossier.
      */
     public void initIndBac(final List<IndBac> indBacs, final Boolean initDossier) {
         indBacPojos = new ArrayList<IndBacPojo>();
         for (IndBac myIndBac : indBacs) {
             if (initDossier) {
                 myIndBac.setTemoinFromApogee(true);
             }
            
             IndBacPojo indBacPojo = new IndBacPojo(myIndBac);
             if (StringUtils.hasText(myIndBac.getCodEtb())) {
                 indBacPojo.setEtablissement(
                         getDomainApoService().getEtablissement(myIndBac.getCodEtb()));
                 //indBacPojo.getIndBac().setCodTpe(indBacPojo.getEtablissement().getCodTpe());
             }
             if (StringUtils.hasText(myIndBac.getCodPay())) {
                 indBacPojo.setPays(getDomainApoService().getPays(myIndBac.getCodPay()));
             }
             if (StringUtils.hasText(myIndBac.getCodCom())) {
                 indBacPojo.setCommune(getDomainApoService().getCommune(myIndBac.getCodCom()));
             }
             if (StringUtils.hasText(myIndBac.getCodDep())) {
 //				indBacPojo.setDepartement(
 //						getBusinessCacheService().getDepartement(myIndBac.getCodDep()));
                 indBacPojo.setDepartement(getDomainApoService().getDepartement(myIndBac.getCodDep()));
             }
             if (StringUtils.hasText(myIndBac.getCodMnb())) {
                 indBacPojo.setMentionNivBac(
                         getDomainApoService().getMentionNivBac(myIndBac.getCodMnb()));
             }
             if (StringUtils.hasText(myIndBac.getCodBac())) {
                 indBacPojo.setBacOuxEqu(
                         getDomainApoService().getBacOuxEqu(
                                 myIndBac.getDateObtention(), myIndBac.getCodBac()));
             }
            
             indBacPojos.add(indBacPojo);
         }
     }
 
     /**
      * Save the Bac info for this individu.
      *
      * @param individu
      */
     public void add(final Individu individu) {
         //Dans le cas oe le candidat ne vient pas de Rennes1
         if (indBacPojos.isEmpty()) {
             //TODO ajout controle
             indBacPojos.add(new IndBacPojo(indBac));
         }
         for (IndBacPojo indBacPojo : indBacPojos) {
             indBacPojo.getIndBac().setIndividu(individu);
         }
         add();
 
     }
 
 
     /**
      * Add to directly to the dataBase.
      *
      * @return String
      */
     public String add() {
         if (indBacPojos.isEmpty()) {
             //TODO ajout controle
             indBacPojos.add(new IndBacPojo(indBac));
         }
 
 //		boolean updateUser = false;
         // Add all the indBac
         for (IndBacPojo indBacPojo : indBacPojos) {
             if (indBacPojo.getIndBac().getId() == 0) {
                 if (indBacPojo.getIndBac().getIndividu() == null) {
                     //on met le currentInd
                     indBacPojo.getIndBac().setIndividu(
                             getCurrentInd().getIndividu());
 //					updateUser = true;
 
                 }
 
 
                 Etablissement e = getDomainApoService().getEtablissement(
                         indBacPojo.getIndBac().getCodEtb());
                 if (e != null) {
                     indBacPojo.getIndBac().setCodTpe(e.getCodTpe());
                 }
 
                 IndividuPojo i = new IndividuPojo();
                 i.setIndividu(indBacPojo.getIndBac().getIndividu());
 
                 getDomainService().addIndBac(getDomainService().add(
                                 indBacPojo.getIndBac(),
                                 Utilitaires.codUserThatIsAction(
                                         getCurrentGest(), i)));
 
             }
 //			if (updateUser) {
 //				getDomainService().updateStateIndividu(
 //							getCurrentInd().getIndividu(), getCurrentGest());
 //				
 //			}
         }
         if (actionEnum.getWhatAction().equals(ActionEnum.ADD_ACTION)) {
             addInfoMessage(null, "INFO.CANDIDAT.BAC.ADD_OK");
         }
         return NavigationRulesConst.ACCUEIL_CANDIDAT;
     }
 
     /**
      * Update directly in the database.
      *
      * @return String
      */
     public String update() {
 		indBac = getDomainService().update(
 				indBac,
 				Utilitaires.codUserThatIsAction(getCurrentGest(),
 						getCurrentInd()));
         Etablissement e = getDomainApoService().getEtablissement(indBac.getCodEtb());
         if (e != null) {
             indBac.setCodTpe(e.getCodTpe());
         }
 
         getDomainService().updateIndBac(indBac);
         List<IndBac> list = new ArrayList<IndBac>();
         list.add(indBac);
         reset();
 
         initIndBac(list, false);
 
         return NavigationRulesConst.SEE_IND_BAC;
     }
 
     /**
      * Control the pojoIndividu attributes.
      *
      * @param indB IndBac
      * @return Boolean
      */
     private Boolean ctrlEnter(final IndBac indB) {
         Boolean ctrlOk = true;
         //dans le cas de bac recuperer d'apogee indBac est e new
         // Check indBac's fields
         if (!StringUtils.hasText(indB.getCodPay())) {
             addErrorMessage(null, Constantes.I18N_EMPTY, getString("ADRESS.PAY"));
             ctrlOk = false;
         }
         if (!StringUtils.hasText(indB.getCodBac())) {
             addErrorMessage(null, Constantes.I18N_EMPTY, getString("INDIVIDU.CURSUS.SERIE_BAC"));
             ctrlOk = false;
         }
         if (!StringUtils.hasText(indB.getDateObtention())) {
             addErrorMessage(null, Constantes.I18N_EMPTY, getString("INDIVIDU.BAC.DAA_OBT"));
             ctrlOk = false;
         } else if (!Utilitaires.isFormatDateValid(
                 indB.getDateObtention(), Constantes.YEAR_FORMAT)) {
             addErrorMessage(null, "ERROR.FIELD.INVALID.DAT", getString("INDIVIDU.BAC.DAA_OBT"));
             ctrlOk = false;
         }
 
         return ctrlOk;
     }
 
 
 
 
 	/*
 	 ******************* ACCESSORS ******************** */
 
 
     /**
      * Return the bac list by Date d'obtention.
      *
      * @return List< BacOuxEqu>
      */
     public List<BacOuxEqu> getBacs() {
         List<BacOuxEqu> c = new ArrayList<BacOuxEqu>();
         final String daaObt = indBac.getDateObtention();
         if(StringUtils.hasText(daaObt)) {
             try {
                 SimpleDateFormat s = new SimpleDateFormat(Constantes.YEAR_FORMAT);
                 s.parse(daaObt);
                 c.addAll(getDomainApoService().getBacOuxEqus(daaObt));
             } catch (ParseException e) {
                 addErrorMessage(null, "ERROR.FIELD.INVALID.DAT", getString("INDIVIDU.BAC.DAA_OBT"));
             }
 	        Collections.sort(c, new ComparatorString(BacOuxEqu.class));
         }
         return c;
     }
 
     /**
      * Returns all mentions in use.
      *
      * @return List< MentionNivBac>
      */
     public List<MentionNivBac> getMentionNivBacs() {
         return getDomainApoService().getMentionNivBacs();
     }
 
     /**
      * Returns list empty if codeDep == null else returns town in dep.
      *
      * @return Set< CommuneDTO>
      */
     public List<CommuneDTO> getCommunes() {
         List<CommuneDTO> c = new ArrayList<CommuneDTO>();
 
         if (StringUtils.hasText(indBac.getCodDep())) {
             c.addAll(getDomainApoService().getCommunes(indBac.getCodDep(), true, null));
         }
 
         return c;
     }
 
     /**
      * Returns list of Etablissement according to the codCom if codPay == Fr.
      * if codPay != Fr   : return the list of the not in France Etablissement.
      * if codCom == null : return a empty list.
      *
      * @return List< Etablissement>
      */
     public List<Etablissement> getLycees() {
         List<Etablissement> e = new ArrayList<Etablissement>();
 
         if (indBac.getCodPay() != null
                 && indBac.getCodPay().equals(Constantes.CODEFRANCE)) {
             if (StringUtils.hasText(indBac.getCodCom())) {
                 e.addAll(getDomainApoService().getLycees(indBac.getCodCom(), indBac.getCodDep()));
             }
 
         } else {
             //the dep = etranger
             e.addAll(getDomainApoService().getLycees(null, Constantes.COD_DEP_ETR));
         }
 
         return e;
     }
 
     /**
      * @return the indBac
      */
     public IndBac getIndBac() {
         return indBac;
     }
 
     /**
      * @param indBac the indBac to set
      */
     public void setIndBac(final IndBac indBac) {
         this.indBac = indBac;
     }
 
     /**
      * @return the indBacPojos
      */
     public List<IndBacPojo> getIndBacPojos() {
         return indBacPojos;
     }
 
     /**
      * @param indBacPojos the indBacPojos to set
      */
     public void setIndBacPojos(final List<IndBacPojo> indBacPojos) {
         this.indBacPojos = indBacPojos;
     }
 
     /**
      * @return the actionEnum
      */
     public ActionEnum getActionEnum() {
         return actionEnum;
     }
 
     /**
      * @param actionEnum the actionEnum to set
      */
     public void setActionEnum(final ActionEnum actionEnum) {
         this.actionEnum = actionEnum;
     }
 
 
}
