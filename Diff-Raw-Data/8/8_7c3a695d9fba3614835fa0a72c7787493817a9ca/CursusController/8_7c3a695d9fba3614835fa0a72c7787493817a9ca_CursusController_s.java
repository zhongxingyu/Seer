 /**
  *
  */
 package org.esupportail.opi.web.controllers.user;
 
 import fj.F;
 import fj.P1;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.indcursus.*;
 import org.esupportail.opi.domain.dto.CommissionDTO;
 import org.esupportail.opi.services.i18n.I18NUtilsService;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.*;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractAccessController;
 import org.esupportail.opi.web.utils.DTOs;
 import org.esupportail.wssi.services.remote.CommuneDTO;
 import org.esupportail.wssi.services.remote.DipAutCur;
 import org.esupportail.wssi.services.remote.Etablissement;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.springframework.util.StringUtils;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import java.io.IOException;
 import java.util.*;
 
 import static fj.data.Array.iterableArray;
 import static java.lang.String.format;
 
 
 /**
  * @author cleprous
  */
 public class CursusController extends AbstractAccessController {
 
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = -7466022760402264721L;
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
     private I18NUtilsService i18NUtils;
 
     /**
      * The pojoCursusScol.
      */
     private IndCursusScolPojo pojoCursusScol;
 
     /**
      * The indCursusPojo.
      */
     private PojoIndCursus indCursusPojo;
 
     /**
      * The pojoQualif.
      */
     private PojoIndCursus pojoQualif;
 
     /**
      * The IndCursusScolPojo list.
      */
     private List<IndCursusScolPojo> cursusList;
 
     /**
      * The IndCursusScolPojo list to delete.
      */
     private List<IndCursusScolPojo> cursusListToDelete;
 
     /**
      * The actionEnum.
      */
     private ActionEnum actionEnum;
 
     /**
      * Confirmation de la suppression.
      */
     private Boolean confirmeDelete;
 
     private final P1<String> buttonNo = new P1<String>() {
         public String _1() {
             return getI18nService().getString("_.BUTTON.NO");
         }
     }.memo();
 
     private final P1<String> buttonYes = new P1<String>() {
         public String _1() {
             return getI18nService().getString("_.BUTTON.YES");
         }
     }.memo();
 
     public CursusController() {
         super();
         confirmeDelete = false;
     }
 
     @Override
     public void reset() {
         super.reset();
         pojoCursusScol = new IndCursusScolPojo(new CursusExt());
         pojoCursusScol.setCodPay(Constantes.CODEFRANCE);
         indCursusPojo = new PojoIndCursus(new CursusPro());
         pojoQualif = new PojoIndCursus(new QualifNonDiplomante());
         cursusList = new ArrayList<>();
         actionEnum = new ActionEnum();
         confirmeDelete = false;
     }
 
     @Override
     public void afterPropertiesSetInternal() {
         super.afterPropertiesSetInternal();
     }
 
     /**
      * Callback to see cursus details for the current connected user.
      */
     public String goSeeCursusPro() {
         reset();
         initCursus(getCurrentInd().getIndividu().getCursus());
         return NavigationRulesConst.SEE_CURSUS_PRO;
     }
 
 
     /**
      * Callback to see cursus details for the current connected user.
      */
     public String goSeeQualif() {
         reset();
         initCursus(getCurrentInd().getIndividu().getCursus());
         return NavigationRulesConst.SEE_QUALIF;
     }
 
 
     /**
      * Callback to see cursus details for the current connected user.
      */
     public String goSeeCursusScol() {
         reset();
         List<IndCursusScol> indCursScol = null;
         indCursScol = new ArrayList<>(getCurrentInd().getIndividu().getCursusScol());
         initCursusList(indCursScol);
         actionEnum.setWhatAction(ActionEnum.UPDATE_ACTION);
         return NavigationRulesConst.SEE_CURSUS_SCOL;
     }
 
     /**
      * The selected pays.
      */
     public void selectPay(final ValueChangeEvent event) {
         String codePay = (String) event.getNewValue();
         pojoCursusScol.setCodPay(codePay);
         FacesContext.getCurrentInstance().renderResponse();
     }
 
     /**
      * The selected departement.
      */
     public void selectDep(final ValueChangeEvent event) {
         String codeDep = (String) event.getNewValue();
         pojoCursusScol.setCodDep(codeDep);
         FacesContext.getCurrentInstance().renderResponse();
     }
 
     /**
      * The selected commune.
      */
     public void selectCommune(final ValueChangeEvent event) {
         String codeCom = (String) event.getNewValue();
         pojoCursusScol.setCodCom(codeCom);
         FacesContext.getCurrentInstance().renderResponse();
     }
 
 
     /**
      * Initialise la list de pojo des cursus scolaire (attributs cursusList).
      */
     public void initCursusList(final List<IndCursusScol> cursusScol) {
         cursusList = new ArrayList<>();
         for (IndCursusScol indCursusScol : cursusScol) addCursus(indCursusScol);
     }
 
     public void initCursus(final Set<IndCursus> indCursusLst) {
         indCursusPojo.setCursusList(new ArrayList<IndCursus>());
         pojoQualif.setCursusList(new ArrayList<IndCursus>());
         for (IndCursus indCursus : indCursusLst)
             if (indCursus instanceof CursusPro) {
                 indCursusPojo.getCursusList().add(indCursus);
             } else if (indCursus instanceof QualifNonDiplomante) {
                 pojoQualif.getCursusList().add(indCursus);
             }
         actionEnum.setWhatAction(ActionEnum.UPDATE_ACTION);
     }
 
     /**
      * Add a CursusScol.
      */
     public void addCursusScol() {
         if (isValidCursusScol(true)) {
             CursusExt c = (CursusExt) pojoCursusScol.getCursus();
             c.setLibDac(getDomainApoService().getDipAutCur(c.getCodDac()).getLibDac());
             addCursus(c);
 
             if (actionEnum.getWhatAction().equals(ActionEnum.UPDATE_ACTION)) {
                 //ajout en base
                 addOneCursusScol(getCurrentInd().getIndividu(), pojoCursusScol.getCursus());
                 //Ajout dans l'individu courant
                 getCurrentInd().getIndividu().getCursusScol().add(pojoCursusScol.getCursus());
                 sendMailAddCursus();
             }
             initCursusScol();
         }
 
     }
 
     /**
      * Update the cursus scol if temFromApogee = true.
      */
     public void updateCursusApogee() {
         //university student --> update cursus
         List<IndCursusScol> indCursScol = new ArrayList<>();
         if (StringUtils.hasText(getCurrentInd().getIndividu().getCodeEtu()))
             indCursScol = getDomainApoService().getIndCursusScolFromApogee(getCurrentInd().getIndividu());
 
         Set<IndCursusScol> inCur =
                 new HashSet<>(getCurrentInd().getIndividu().getCursusScol());
         List<IndCursusScol> curExt = new ArrayList<>();
         //delete all cursus in Apogee
         for (IndCursusScol indCur : inCur)
             if (indCur.getTemoinFromApogee()) {
                 getDomainService().deleteIndCursusScol(indCur);
             } else {
                 curExt.add(indCur);
             }
         if (indCursScol != null)
             for (IndCursusScol indCur : indCursScol)
                 addOneCursusScol(getCurrentInd().getIndividu(), indCur);
         //reinit
         reset();
         if (indCursScol != null) curExt.addAll(indCursScol);
         initCursusList(curExt);
         actionEnum.setWhatAction(ActionEnum.UPDATE_ACTION);
     }
 
     /**
      * Prepare the cursus scol from apogee.
      * if temFromApogee = true in individu, prepare the list to delete
      */
     public void initCursusListFromApogee(final Individu individu) {
         //university student --> update cursus
         List<IndCursusScol> indCursScolApo = new ArrayList<>();
         if (StringUtils.hasText(individu.getCodeEtu()))
             indCursScolApo = getDomainApoService().getIndCursusScolFromApogee(individu);
 
         Set<IndCursusScol> indCursusScol = new HashSet<>(individu.getCursusScol());
         List<IndCursusScol> cursusExt = new ArrayList<>();
         cursusListToDelete = new ArrayList<>();
         //delete all cursus in Apogee
         for (IndCursusScol indCur : indCursusScol)
             if (indCur.getTemoinFromApogee()) {
                 final IndCursusScolPojo cursusScolPojo = new IndCursusScolPojo(indCur);
                 final Etablissement etablissement =
                         getDomainApoService().getEtablissement(indCur.getCodEtablissement());
                 indCur.setCodTypeEtab(etablissement.getCodTpe());
                 cursusScolPojo.setEtablissement(etablissement);
                 cursusListToDelete.add(cursusScolPojo);
             } else
                 cursusExt.add(indCur);
         //reinit
         reset();
         if (indCursScolApo != null) {
             cursusExt.addAll(indCursScolApo);
         }
         initCursusList(cursusExt);
     }
 
     /**
      * Cancel a CursusScol.
      */
     public void cancelCursusScol() {
         pojoCursusScol = null;
     }
 
     /**
      * Remove a CursusScol.
      */
     public void removeCursusScol() {
         if (pojoCursusScol.getCursus() != null) {
             // cas de la suppression lors de la création d'un dossier
             if (actionEnum.getWhatAction().equals(ActionEnum.UPDATE_ACTION)) {
                 //Suppression dans l'individu courant
                 getCurrentInd().getIndividu().getCursusScol().remove(pojoCursusScol.getCursus());
                 //Suppression en base
                 getDomainService().deleteIndCursusScol(pojoCursusScol.getCursus());
             }
             //Suppression dans le pojo
             cursusList.remove(pojoCursusScol);
             initCursusScol();
         }
     }
 
     /**
      * Add a CursusPro.
      */
     public void addCursusPro() throws IOException {
         if (isValidIndCursus(CursusPro.class, true)) {
             // on transforme le commentaire pour corriger les caractères spéciaux
             indCursusPojo.getCursus().setComment(indCursusPojo.getCursus().getComment());
             //TODO: Fix this !!
 //				org.esupportail.commons.utils.strings.StringUtils
 //					.htmlToText(indCursusPojo.getCursus().getComment()));
             if (actionEnum.getWhatAction().equals(ActionEnum.UPDATE_ACTION)) {
                 //ajout en base
                 addOneCursus(getCurrentInd().getIndividu(), indCursusPojo.getCursus());
                 //Ajout dans l'individu courant
                 getCurrentInd().getIndividu().getCursus().add(indCursusPojo.getCursus());
             }
             indCursusPojo.addCursus();
             Collections.sort(indCursusPojo.getCursusList(), new ComparatorString(IndCursus.class));
         }
     }
 
     /**
      * Remove a CursusExt.
      */
     public void removeCursusPro() {
         if (indCursusPojo.getCursus() != null) {
             if (actionEnum.getWhatAction().equals(ActionEnum.UPDATE_ACTION)) {
                 //Suppression dans l'individu courant
                 getCurrentInd().getIndividu().getCursus().remove(indCursusPojo.getCursus());
                 //Suppression en base
                 getDomainService().deleteIndCursus(indCursusPojo.getCursus());
             }
             //Suppression dans le pojo
             indCursusPojo.removeCursus();
         }
         this.confirmeDelete = false;
     }
 
     /**
      * Remove from database the cursus R1.
      */
     public void deleteCursusR1() {
         for (IndCursusScolPojo indCur : cursusListToDelete) {
             getDomainService().deleteIndCursusScol(indCur.getCursus());
         }
     }
 
     /**
      * Add a Qualification non diplemantes.
      */
     public void addQualifNoDip() throws IOException {
         if (isValidIndCursus(QualifNonDiplomante.class, true)) {
             // on transforme le commentaire pour corriger les caractères spéciaux
             pojoQualif.getCursus().setComment(pojoQualif.getCursus().getComment());
             //TODO: Fix this !!
 //				org.esupportail.commons.utils.strings.StringUtils
 //					.htmlToText(pojoQualif.getCursus().getComment()));
             if (actionEnum.getWhatAction().equals(ActionEnum.UPDATE_ACTION)) {
                 //ajout en base
                 addOneCursus(getCurrentInd().getIndividu(), pojoQualif.getCursus());
                 //Ajout dans l'individu courant
                 getCurrentInd().getIndividu().getCursus().add(pojoQualif.getCursus());
             }
             pojoQualif.addCursus();
             Collections.sort(pojoQualif.getCursusList(), new ComparatorString(IndCursus.class));
         }
     }
 
     /**
      * Remove a CursusQualif.
      */
     public void removeQualif() {
         if (pojoQualif.getCursus() != null) {
             if (actionEnum.getWhatAction().equals(ActionEnum.UPDATE_ACTION)) {
                 //Suppression dans l'individu courant
                 getCurrentInd().getIndividu().getCursus().remove(pojoQualif.getCursus());
                 //Suppression en base
                 getDomainService().deleteIndCursus(pojoQualif.getCursus());
             }
             //Suppression dans le pojo
             pojoQualif.removeCursus();
         }
         this.confirmeDelete = false;
     }
 
     /**
      * Return the result items.
      */
     public List<SelectItem> getResultItems() {
         List<SelectItem> s = new ArrayList<>();
         s.add(new SelectItem(Constantes.FLAG_YES, getString("_.BUTTON.YES")));
         s.add(new SelectItem(Constantes.FLAG_NO, getString("_.BUTTON.NO")));
         s.add(new SelectItem(getString("FIELD_LABEL.IN_PROGRESS"), getString("FIELD_LABEL.IN_PROGRESS")));
         return s;
     }
 
     /**
      * Save the cursus for this individu.
      */
     public void add(final Individu individu) {
         // Add all the CursusExt
         for (IndCursus indCursus : indCursusPojo.getCursusList())
             addOneCursus(individu, indCursus);
         // Add all the Qualif
         for (IndCursus indCursus : pojoQualif.getCursusList())
             addOneCursus(individu, indCursus);
         // Add IndCursusScol
         for (IndCursusScolPojo indCursusScolPojo : cursusList)
             addOneCursusScol(individu, indCursusScolPojo.getCursus());
     }
 
 
     private void addOneCursus(final Individu currentInd, final IndCursus indCursus) {
         if (log.isDebugEnabled()) {
             log.debug("entering addOneCursus with currentInd =  "
                     + currentInd + " and indCursus = " + indCursus);
         }
         indCursus.setIndividu(currentInd);
         IndividuPojo i = new IndividuPojo();
         i.setIndividu(currentInd);
         getDomainService().addIndCursus(getDomainService().add(
                 indCursus,
                 Utilitaires.codUserThatIsAction(getCurrentGest(), i)));
     }
 
     private void addOneCursusScol(final Individu currentInd, final IndCursusScol indCursusScol) {
         if (log.isDebugEnabled())
             log.debug(format("entering addOneCursusScol with currentInd =  %s and indCursusScol = %s", currentInd, indCursusScol));
         if (indCursusScol.getId() == 0) {
             indCursusScol.setIndividu(currentInd);
             IndividuPojo i = new IndividuPojo();
             i.setIndividu(currentInd);
             IndCursusScol indCur =
                     getDomainService().add(indCursusScol, Utilitaires.codUserThatIsAction(getCurrentGest(), i));
             getDomainService().addIndCursusScol(indCur);
         }
     }
 
     /**
      * send mail to individual when add one cursusScol.
      */
     private void sendMailAddCursus() {
         Individu i = getCurrentInd().getIndividu();
         Campagne camp = Utilitaires.getCampagneEnServ(i, getDomainService());
         RegimeInscription regimeIns = getRegimeIns().get(Utilitaires.getCodeRIIndividu(i,
                 getDomainService()));
         //send mail
         if (regimeIns.getMailAddCursusScol() != null) {
             Map<Commission, Set<VersionEtapeDTO>> wishesByCmi =
                     Utilitaires.getCmiForIndVoeux(
                             getParameterService().getCommissions(true),
                             getCurrentInd().getIndVoeuxPojo(), camp);
             List<CommissionDTO> cmiDTOs = new ArrayList<>();
             for (Map.Entry<Commission, Set<VersionEtapeDTO>> cmiEntry : wishesByCmi.entrySet()) {
 //                Commission cmi = cmiEntry.getKey();
                 // TODO : if this works, remove the commented lines
                 cmiDTOs.add(DTOs.buildCommissionDTO(getDomainApoService(), regimeIns.getCode(), cmiEntry.getKey()));
 //                CommissionPojo cmiPojo = new CommissionPojo(
 //                        cmi,
 //                        new AdressePojo(cmi.getContactsCommission().get(regimeIns.getCode())
 //                                .getAdresse(), getDomainApoService()),
 //                        cmi.getContactsCommission().get(regimeIns.getCode()));
 //                cmiPojo.initTreatmentsPojo(cmiEntry.getValue());
 //                cmiDTOs.add(cmiPojo);
             }
             List<Object> list = new ArrayList<>();
             list.add(i);
             list.add(cmiDTOs);
             regimeIns.getMailAddCursusScol().send(i.getAdressMail(), i.getEmailAnnuaire(), list);
         }
     }
 
     /**
      * Add the IndCursusScol c in cursusList.
      */
     private IndCursusScol addCursus(final IndCursusScol c) {
         final Etablissement etablissement =
                 getDomainApoService().getEtablissement(c.getCodEtablissement());
         final IndCursusScolPojo indCursusScolPojo = new IndCursusScolPojo(c);
         c.setCodTypeEtab(etablissement.getCodTpe());
         indCursusScolPojo.setEtablissement(etablissement);
         cursusList.add(indCursusScolPojo);
         Collections.sort(cursusList, new ComparatorString(IndCursusScolPojo.class));
         return indCursusScolPojo.getCursus();
     }
 
     /**
      * Create a new IndCursusScolPojo in order to prepare a CursusExt add in cursusList.
      */
     public void initCursusScol() {
         pojoCursusScol = new IndCursusScolPojo(new CursusExt());
         pojoCursusScol.setCodPay(Constantes.CODEFRANCE);
         confirmeDelete = false;
     }
 
     /**
      * Control the CursusScol attributes.
      */
     public Boolean isValidCursusScol(final boolean message) {
         Boolean ctrlOk = true;
         final CursusExt c = (CursusExt) pojoCursusScol.getCursus();
         // Check CursusScol fields
         if (!StringUtils.hasText(c.getAnnee())) {
             if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("INDIVIDU.CURSUS.ANNEE_UNI"));
             ctrlOk = false;
         } else if (!Utilitaires.isFormatDateValid(c.getAnnee(), Constantes.YEAR_FORMAT)) {
             if (message) addErrorMessage(null, "ERROR.FIELD.INVALID.DAT", getString("INDIVIDU.CURSUS.ANNEE_UNI"));
             ctrlOk = false;
         }
         if (!StringUtils.hasText(c.getCodDac())) {
             if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.DIPLOME"));
             ctrlOk = false;
         }
         if (!pojoCursusScol.getCodPay().equals(Constantes.CODEFRANCE) && !StringUtils.hasText(c.getLibEtbEtr())) {
             if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.ETABLISSEMENT"));
             ctrlOk = false;
         }
         if (!StringUtils.hasText(c.getResultat())) {
             if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.DIPLOME.OBT"));
             ctrlOk = false;
         }
         return ctrlOk;
     }
 
 
     /**
      * Control the IndCursus attributes.
      */
     public Boolean isValidIndCursus(final Class<?> typeCursus, final boolean message) {
         final IndCursus c =
                 typeCursus.equals(CursusPro.class) ? indCursusPojo.getCursus() : pojoQualif.getCursus();
         Boolean ctrlOk = true;
         // Check CursusScol fields
         if (!StringUtils.hasText(c.getAnnee())) {
             if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.YEAR"));
             ctrlOk = false;
         }
         if (!StringUtils.hasText(c.getDuree())) {
             if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.PERIOD"));
             ctrlOk = false;
         }
 
         if (!StringUtils.hasText(c.getOrganisme())) {
             if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.ORGANIZATION"));
             ctrlOk = false;
         }
         if (StringUtils.hasText(c.getComment()) && c.getComment().length() > 2000) {
             if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("ERROR.FIELD.MAX_LENGTH.CARACTER"), 2000);
             ctrlOk = false;
         }
         if (c instanceof QualifNonDiplomante)
             if (!StringUtils.hasText(c.getLibelle())) {
                 if (message) addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.INTITULE"));
                 ctrlOk = false;
             }
         return ctrlOk;
     }
 
 
     /**
      * Return the result label according to the code.
      */
     public String getNResultatExt() {
         return buttonNo._1();
     }
 
     public String getYResultatExt() {
         return buttonYes._1();
     }
     
     public Boolean getCanUpdateIndCursusScol() {
         final IndividuPojo ip = getCurrentInd();
         return ip.getAsRightsToUpdate() || !getEtatIndCursusScol().equals(i18NUtils.labelEtatNonRenseigne());
     }
 
     public Boolean getCanUpdateIndCursusPro() {
         final IndividuPojo ip = getCurrentInd();
         return ip.getAsRightsToUpdate() || !getEtatIndCursusPro().equals(i18NUtils.labelEtatNonRenseigne());
     }
 
     public Boolean getCanUpdateQualifNonDiplomante() {
         final IndividuPojo ip = getCurrentInd();
         return ip.getAsRightsToUpdate() || !getEtatQualifNonDiplomante().equals(i18NUtils.labelEtatNonRenseigne());
     }
 
     public String getEtatIndCursusScol() {
         final Individu individu = getCurrentInd().getIndividu();
         return ((individu.getCursusScol() != null)
                 && !individu.getCursusScol().isEmpty()) ?
                 i18NUtils.labelEtatComplet() :
                 i18NUtils.labelEtatNonRenseigne();
     }
 
     public String getEtatIndCursusPro() {
         return getEtatCursus(CursusPro.class);
     }
 
     public String getEtatQualifNonDiplomante() {
         return getEtatCursus(QualifNonDiplomante.class);
     }
 
     private String getEtatCursus(final Class<? extends IndCursus> clazz) {
         final Individu individu = getCurrentInd().getIndividu();
         if (individu.getCursus() == null || individu.getCursus().isEmpty())
             return i18NUtils.labelEtatNonRenseigne();
         final boolean cursusExists =
                 iterableArray(individu.getCursus()).exists(new F<IndCursus, Boolean>() {
                     public Boolean f(IndCursus indCursus) {
                         return clazz.isInstance(indCursus);
                     }
                 });
         return cursusExists ? i18NUtils.labelEtatComplet() : i18NUtils.labelEtatNonRenseigne();
     }
 
     /**
      * Returns all DipAutCur.
      */
     public List<DipAutCur> getDipAutCurs() {
         List<DipAutCur> dipAutCur = new ArrayList<>();
         if (pojoCursusScol.getCodPay() != null
                 && pojoCursusScol.getCodPay().equals(Constantes.CODEFRANCE)) {
             List<DipAutCur> d = getDomainApoService().getDipAutCurs();
             DipAutCur dip = new DipAutCur();
             dip.setCodDac("");
             dip.setLibDac("");
             dipAutCur.add(dip);
             dipAutCur.addAll(d);
         } else {
             //cas pays etranger
             DipAutCur d = getDomainApoService().getDipAutCur(getDomainApoService().getCodDacEtr());
             dipAutCur.add(d);
         }
         log.info("retour de getDipAutCurs " + dipAutCur);
         return dipAutCur;
     }
 
     /**
      * Returns all the Etablissements.
      */
     public List<Etablissement> getEtablissements() {
         List<Etablissement> e = new ArrayList<>();
         if (pojoCursusScol.getCodPay() != null &&
                 pojoCursusScol.getCodPay().equals(Constantes.CODEFRANCE) &&
                 StringUtils.hasText(pojoCursusScol.getCodCom()))
             e = getDomainApoService().getEtablissements(pojoCursusScol.getCodCom(), pojoCursusScol.getCodDep());
         return e;
     }
 
     /**
      * Returns list empty if codeDep == null else returns town in departement.
      */
     public Set<CommuneDTO> getCommunes() {
         List<CommuneDTO> c = new ArrayList<>();
         if (StringUtils.hasText(pojoCursusScol.getCodDep()))
             c = getDomainApoService().getCommunes(pojoCursusScol.getCodDep(), null, true);
         Set<CommuneDTO> s = new TreeSet<>(new ComparatorString(CommuneDTO.class));
         s.addAll(c);
         return s;
     }
 
 
     /**
      * @return the pojoCursusScol
      */
     public IndCursusScolPojo getPojoCursusScol() {
         return pojoCursusScol;
     }
 
     /**
      * @param pojoCursusScol the pojoCursusScol to set
      */
     public void setPojoCursusScol(final IndCursusScolPojo pojoCursusScol) {
         this.pojoCursusScol = pojoCursusScol;
     }
 
     /**
      * @return the indCursusPojo
      */
     public PojoIndCursus getIndCursusPojo() {
         return indCursusPojo;
     }
 
     /**
      * @param indCursusPojo the indCursusPojo to set
      */
     public void setIndCursusPojo(final PojoIndCursus indCursusPojo) {
         this.indCursusPojo = indCursusPojo;
     }
 
     /**
      * @return the pojoQualif
      */
     public PojoIndCursus getPojoQualif() {
         return pojoQualif;
     }
 
     /**
      * @param pojoQualif the pojoQualif to set
      */
     public void setPojoQualif(final PojoIndCursus pojoQualif) {
         this.pojoQualif = pojoQualif;
     }
 
     /**
      * @return the cursusList
      */
     public List<IndCursusScolPojo> getCursusList() {
         return cursusList;
     }
 
     /**
      * @param cursusList the cursusList to set
      */
     public void setCursusList(final List<IndCursusScolPojo> cursusList) {
         this.cursusList = cursusList;
     }
 
     /**
      * @return the cursusListToDelete
      */
     public List<IndCursusScolPojo> getCursusListToDelete() {
         return cursusListToDelete;
     }
 
     /**
      * @param cursusListToDelete the cursusListToDelete to set
      */
     public void setCursusListToDelete(final List<IndCursusScolPojo> cursusListToDelete) {
         this.cursusListToDelete = cursusListToDelete;
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
 
     /**
      * @return the confirmeDelete
      */
     public boolean isConfirmeDelete() {
         return confirmeDelete;
     }
 
     public void setConfirmeDelete() {
         this.confirmeDelete = true;
     }
 
     public I18NUtilsService getI18NUtils() {
         return i18NUtils;
     }
 
     public void setI18NUtils(I18NUtilsService i18NUtils) {
         this.i18NUtils = i18NUtils;
     }
 }
