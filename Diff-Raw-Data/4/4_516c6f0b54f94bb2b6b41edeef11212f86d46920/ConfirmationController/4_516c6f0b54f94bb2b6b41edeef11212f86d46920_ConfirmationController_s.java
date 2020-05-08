 package org.esupportail.opi.web.controllers.formation;
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.services.smtp.SmtpService;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.BusinessUtil;
 import org.esupportail.opi.domain.OpiWebService;
 import org.esupportail.opi.domain.beans.etat.EtatConfirme;
 import org.esupportail.opi.domain.beans.etat.EtatDesiste;
 import org.esupportail.opi.domain.beans.parameters.AutoListPrincipale;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.parameters.InscriptionAdm;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.references.calendar.CalendarCmi;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.services.mails.MailContentService;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.utils.converters.xml.DateUtil;
 import org.esupportail.opi.web.beans.parameters.FormationContinue;
 import org.esupportail.opi.web.beans.parameters.FormationInitiale;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.AdressePojo;
 import org.esupportail.opi.web.beans.pojo.CommissionPojo;
 import org.esupportail.opi.web.beans.pojo.IndVoeuPojo;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractAccessController;
 import org.esupportail.opi.web.controllers.opinions.ValidOpinionController;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.springframework.util.StringUtils;
 
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * @author ylecuyer
  */
 public class ConfirmationController extends AbstractAccessController {
 
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = 1053082620153284395L;
 
 	/*
      ******************* PROPERTIES ******************* */
 
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
     /**
      * The list of indVoeuPojo with voeu favorable.
      */
     private Set<IndVoeuPojo> indVoeuxPojoFav;
 
     /**
      * The address of the IA Web Primo.
      */
     private String addressIAPrimo;
 
     /**
      * The address of the IA Web réinscription.
      */
     private String addressIAReins;
 
     /**
      * see {@link InscriptionAdm}.
      */
     private InscriptionAdm inscriptionAdm;
 
     /**
      * see {@link OpiWebService}.
      */
     private OpiWebService opiWebService;
 
     /**
      * see {@link SaisieRdvEtuController}.
      */
     private SaisieRdvEtuController saisieRdvEtuController;
 
     /**
      * {@link SmtpService}.
      */
     private SmtpService smtpService;
 
     /**
      * see {@link ValidOpinionController}.
      */
     private ValidOpinionController validOpinionController;
 
 	/*
      ******************* INIT ************************* */
 
     /**
      * Constructors.
      */
     public ConfirmationController() {
         super();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         indVoeuxPojoFav = new TreeSet<IndVoeuPojo>(new ComparatorString(IndVoeuPojo.class));
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractAccessController#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         super.afterPropertiesSetInternal();
         Assert.notNull(this.saisieRdvEtuController,
                 "property saisieRdvEtuController of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.validOpinionController,
                 "property validOpinionController of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.inscriptionAdm,
                 "property inscriptionAdm of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.opiWebService,
                 "property opiWebService of class " + this.getClass().getName()
                         + " can not be null");
         reset();
     }
 
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * @return String
      */
     public String goConfirmCandidatures() {
         reset();
         initIndVoeuxPojoFav();
         return NavigationRulesConst.CONFIRM_CANDIDATURES;
     }
 
 	/*
 	 ******************* METHODS ********************** */
 
     /**
      * Confirme et désiste les candidatures de voeux favorables
      * selon le choix de l'étudiant.
      *
      * @return String
      */
     public String finishCandidature() {
         if (log.isDebugEnabled()) {
             log.debug("entering finishCandidature");
         }
         //list de voeu confirme
         List<IndVoeu> list = new ArrayList<IndVoeu>();
         List<IndVoeu> listDesiste = new ArrayList<IndVoeu>();
         Set<IndVoeuPojo> listVoeuxMail = new HashSet<IndVoeuPojo>();
         for (IndVoeuPojo indVoeuPojo : indVoeuxPojoFav) {
             if (indVoeuPojo.getStateConf() != null) {
                 IndVoeu indVoeu = indVoeuPojo.getIndVoeu();
                 if (indVoeuPojo.getStateConf().equals(EtatConfirme.I18N_STATE)) {
                     // cas d'une confirmation
                     indVoeu.setState(EtatConfirme.I18N_STATE);
                     getDomainService().updateIndVoeu(indVoeu);
                     list.add(indVoeu);
                     listVoeuxMail.add(indVoeuPojo);
                     addInfoMessage(null, "STATE.CONFIRM.VALID");
                 } else if (indVoeuPojo.getStateConf().equals(EtatDesiste.I18N_STATE)) {
                     // cas d'un désistement
                     indVoeu.setState(EtatDesiste.I18N_STATE);
                     getDomainService().updateIndVoeu(indVoeu);
                     listDesiste.add(indVoeu);
                     //si c'est le candidat qui se désiste
                     gestionAutoLp(indVoeuPojo);
                     if (!indVoeu.getState().equals(EtatDesiste.I18N_STATE)) {
                         addErrorMessage(null, "STATE.DESIST.WARNING");
                     }
                 }
                 indVoeuPojo.initEtat(indVoeu.getState(), getI18nService());
             }
         }
 
         if (!list.isEmpty()) {
             //on deverse tous les temps dans Apogee
             Boolean inApogeeOk = opiWebService.launchWebService(getCurrentInd().getIndividu(), list);
 
             if (inApogeeOk) {
                 //on creer les laisser passer
                 getDomainApoService().addTelemLaisserPasser(list, StringUtils.hasText(
                         getCurrentInd().getIndividu().getCodeEtu()));
 
                 // envoi du mail
                 sendMailConf(getCurrentInd().getIndividu(), listVoeuxMail);
             }
 
         }
         getDomainApoService().deleteTelemLaisserPasser(listDesiste, StringUtils.hasText(
                 getCurrentInd().getIndividu().getCodeEtu()));
 
 //		if (getCurrentGest() == null) {
 //			return NavigationRulesConst.ACCUEIL_CANDIDAT;
 //		}
 //		
 //		return null;
         return NavigationRulesConst.ACCUEIL_CANDIDAT;
     }
 
     /**
      * Gestion automatique des listes complémentaires.
      *
      * @param indVoeuPojo
      */
     private void gestionAutoLp(final IndVoeuPojo indVoeuPojo) {
         if (log.isDebugEnabled()) {
             log.debug("entering gestionAutoLp");
         }
         //Récupération de la gestion automatique des listes complémentaire
         AutoListPrincipale autoLp = getParameterService().getAutoListPrincipale(indVoeuPojo.getIndVoeu());
         if (autoLp == null) {
             return;
         }
 
         TypeDecision typeDec = null;
         for (Avis avis : indVoeuPojo.getIndVoeu().getAvis()) {
             if (avis.getValidation() && avis.getTemoinEnService()) {
                 typeDec = avis.getResult();
             }
         }
         //Teste si le type de decision du voeu et le type de decision de la liste automatique sont identiques
         if (typeDec == null || !typeDec.getId().equals(autoLp.getTypeDecisionDeLP().getId())) {
             return;
         }
 
         //Récupération du premier voeu se trouvant dans la liste complementaire
         IndVoeu indVoeuLc = getDomainService().getRecupIndVoeuLc(autoLp,
                 indVoeuPojo.getIndVoeu().getLinkTrtCmiCamp().getTraitementCmi().getVersionEtpOpi());
         if (indVoeuLc == null) {
             return;
         }
 
         //Mise à jour du voeu (passage de la liste complémentaire à la liste principale)
         for (Avis avis : indVoeuLc.getAvis()) {
             if (avis.getTemoinEnService()) {
                 avis.setTemoinEnService(false);
                 getDomainService().updateAvis(avis);
             }
         }
         //Création de l'avis
         Avis avis = new Avis();
         getDomainService().add(avis, getCurrentInd().getIndividu().getNumDossierOpi());
         avis.setIndVoeu(indVoeuLc);
         avis.setValidation(true);
         avis.setTemoinEnService(true);
         avis.setResult(autoLp.getTypeDecisionDeLP());
         getDomainService().addAvis(avis);
 
         indVoeuLc.getAvis().add(avis);
         getDomainService().updateIndVoeu(indVoeuLc);
 
         //param Set <Avis>
         Set<Avis> listAvis = new HashSet<Avis>();
         listAvis.add(avis);
 
         RegimeInscription regimeIns = getRegimeIns().get(indVoeuLc.getLinkTrtCmiCamp()
                 .getCampagne().getCodeRI());
 
         // Commission
         Commission c = indVoeuLc.getLinkTrtCmiCamp().getTraitementCmi().getCommission();
         CommissionPojo commissionPojo = new CommissionPojo(c,
                 new AdressePojo(c.getContactsCommission().get(regimeIns.getCode()).getAdresse(),
                         getDomainApoService()), c.getContactsCommission().get(regimeIns.getCode()));
 
         // IndividuPojo
         // param Set <Commission>
         Set<Commission> lesCommissions = new HashSet<Commission>();
         lesCommissions.add(c);
         // param Set <TypeDecisions>
         Set<TypeDecision> lesTypeDecisions = new HashSet<TypeDecision>();
         lesTypeDecisions.add(avis.getResult());
         // param Set <VersionEtapeDTO>
         Set<VersionEtapeDTO> versionsEtape = new HashSet<VersionEtapeDTO>();
         versionsEtape.add(getDomainApoService().getVersionEtape(
                 indVoeuLc.getLinkTrtCmiCamp().getTraitementCmi().getVersionEtpOpi().getCodEtp(),
                 indVoeuLc.getLinkTrtCmiCamp().getTraitementCmi().getVersionEtpOpi().getCodVrsVet()));
         // new IndividuPojo()
         IndividuPojo individuPojo = new IndividuPojo(
                 indVoeuLc.getIndividu(),
                 getDomainApoService(),
                 getI18nService(),
                 getParameterService(),
                 lesCommissions,
                 lesTypeDecisions,
                 getParameterService().getTypeTraitements(),
                 getParameterService().getCalendarRdv(),
                 versionsEtape);
 
         boolean isAppel = false;
         for (Avis a : listAvis) {
             if (a.getAppel()) {
                 isAppel = true;
                 break;
             }
         }
 
         if (regimeIns.getMailTypeConvoc() != null) {
             MailContentService mail = regimeIns.getMailContentServiceTypeConvoc(
                     inscriptionAdm, isAppel);
             if (mail != null) {
                 //Envoie du mail à l'individu
                 validOpinionController.sendMail(
                         individuPojo,
                         listAvis,
                         mail,
                         commissionPojo,
                         true,
                         null);
 
                 if (autoLp.getMail() == null || autoLp.getMail().isEmpty()) {
                     //Envoie du mail à l'adresse de la commission
                     validOpinionController.sendMail(
                             individuPojo,
                             listAvis,
                             mail,
                             commissionPojo,
                             false,
                             null);
                 } else {
                     //Envoie du mail à l'adresse => AutoListPrincipale.getMail()
                     validOpinionController.sendMail(
                             individuPojo,
                             listAvis,
                             mail,
                             commissionPojo,
                             false,
                             autoLp.getMail());
                 }
             }
         }
     }
 
     /**
      * Envoi le mail après la confirmation de l'individu.
      *
      * @param individu
      * @param voeux
      */
     private void sendMailConf(final Individu individu, final Set<IndVoeuPojo> voeux) {
         int codeRI = Utilitaires.getCodeRIIndividu(individu,
                 getDomainService());
         Campagne camp = Utilitaires.getCampagneEnServ(individu, getDomainService());
         // list of commissions
         Set<Commission> cmi = getParameterService().getCommissions(true);
         // map des commissions sur lesquels le candidat a confirmé des voeux
         Map<Commission, Set<VersionEtapeDTO>> mapCmi = Utilitaires.getCmiForIndVoeux(cmi, voeux, camp);
 
         for (Map.Entry<Commission, Set<VersionEtapeDTO>> entryCmi : mapCmi.entrySet()) {
 
             if (getRegimeIns().get(codeRI).getConfirmInscription() != null) {
                 // list contenant la commission et les vet
                 List<Object> list = new ArrayList<Object>();
                 list.add(new CommissionPojo(entryCmi.getKey(),
                         new AdressePojo(entryCmi.getKey().getContactsCommission()
                                 .get(codeRI).getAdresse(),
                                 getDomainApoService()),
                         entryCmi.getKey().getContactsCommission()
                                 .get(codeRI)));
                 list.add(entryCmi.getValue());
                 list.add(individu);
                 getRegimeIns().get(codeRI).getConfirmInscription()
                         .send(individu.getAdressMail(), individu.getEmailAnnuaire(), list);
             }
 
             //to delete if ok 23/11/2009
 //			String htmlDebut = "";
 //			String htmlBody = "";
 //			String htmlSubject = "";
 //			String endBody = "";
 //			
 //			// récupération de la commission
 //			Commission oneCmi = entryCmi.getKey();
 //			CommissionPojo currentCmiPojo = new CommissionPojo(oneCmi, 
 //					new AdressePojo(oneCmi.getAdress(), getBusinessCacheService()));
 //			// récupération de la liste des vets
 //			Set<VersionEtapeDTO> vets = entryCmi.getValue();
 //			
 //			htmlSubject = getString("MAIL.CANDIDAT_AVIS.CONF.SUBJECT");
 //			htmlDebut += getString("MAIL.CANDIDAT_AVIS.CONF.HTMLTEXT_DEBUT", 
 //					Utilitaires.getCivilite(getI18nService(),
 //							individu.getSexe()));
 //			// list of libelle voeux
 //			String htmlList = "";
 //			for (VersionEtapeDTO vet : vets) {
 //				htmlList +=  getString("MAIL.LIST_VET", vet.getLibWebVet());
 //			}
 //			htmlBody += htmlList;
 //			
 //			htmlBody += getString("MAIL.CANDIDAT_AVIS.CONF.HTMLTEXT_BODY1");
 //	
 //			// adresse CMI
 //			htmlBody += Utilitaires.getAdrCmiForSendMail(getI18nService(), currentCmiPojo, null);
 //			// formule politesse signature
 //			htmlBody += getString("MAIL.CANDIDAT_FORMULE_POLITESSE.HTMLTEXT");
 //	
 //			// send mail
 //			Utilitaires.sendEmailIndividu(
 //					htmlSubject,
 //					htmlDebut + htmlBody,
 //					endBody,
 //					individu,
 //					smtpService, getI18nService());
         }
     }
 
     /**
      * @return true si au moins un des voeux confirmés dispose d'un accès à distance
      *         pour l'inscription administrative
      */
     public Boolean getHasIAForVoeux() {
         RegimeInscription regimeIns = getCurrentInd().getRegimeInscription();
         for (IndVoeuPojo indVoeuPojo : indVoeuxPojoFav) {
             if (indVoeuPojo.getIsEtatConfirme()
                     && (indVoeuPojo.getHasIAForVoeu()
                     || regimeIns.getDisplayInfoFC())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @return le contenu html du bloc IA Web correctement formé
      */
     public String getHtmlBlockIAWeb() throws ParseException {
         String htmlBlockIAWeb = "";
         Individu individu = getCurrentInd().getIndividu();
         Integer codeRI = Utilitaires.getCodeRIIndividu(individu,
                 getDomainService());
         if (codeRI.equals(FormationInitiale.CODE)) {
             htmlBlockIAWeb += getFormationInitialOutput(individu);
         } else if (codeRI.equals(FormationContinue.CODE)) {
             htmlBlockIAWeb += getFormationContinueOutput();
         }
         return htmlBlockIAWeb;
     }
 
     private String getFormationContinueOutput() {
         return getString("CONFIRMATION.INFO_MESSAGE_FC.SAVE");
     }
 
     private String getFormationInitialOutput(Individu individu) throws ParseException {
         String result = "";
         if (StringUtils.hasText(individu.getCodeNNE())) {
             result += getWithCodeNNEOutput();
 
         } else {
             result += getString("CONFIRMATION.INFO_MESSAGE.NO_NNE");
         }
         return result;
     }
 
     private String getWithCodeNNEOutput() throws ParseException {
         String result = "";
 
         if (getHasIAForVoeux()) {
             result += getOutputforHasIAForVoeux();
         }
         return result;
     }
 
     private String getOutputforHasIAForVoeux() throws ParseException {
         String result = "";
         // true si aucune vet n'a son IA Web ouverte
         boolean noIAOpen = true;
 
         // individu courant
         Individu ind = getSessionController().getCurrentInd().getIndividu();
         // true si individu primo entrant, false si réinscription
         boolean indPrimo = !StringUtils.hasLength(ind.getCodeEtu());
 
         SimpleDateFormat formatter = new SimpleDateFormat(Constantes.APOGEE_DATE_FORMAT);
         // date d'ouverture la plus proche en cas d'IA fermé
         Date dateOuverture = determineDateOuverture(indPrimo, formatter);
 
         // liste des vet dont l'IA n'est pas ouverte
         Set<VersionEtapeDTO> listVetIANotOpen = new TreeSet<VersionEtapeDTO>(
                 new ComparatorString(VersionEtapeDTO.class));
 
         String adresseIA = determineAdressIA(ind);
 
         // on boucle sur la liste des voeux favorables confirmés
         // pour déterminer l'IA Web est ouverte
         for (IndVoeuPojo indVoeuPojo : indVoeuxPojoFav) {
             //TODO check coherence as we already are in ConfirmationController.getHasIAForVoeux() == true  here
             //ie:  indVoeuPojo.getIsEtatConfirme() && (indVoeuPojo.getHasIAForVoeu()||regimeIns.getDisplayInfoFC()) == true
             if (indVoeuPojo.getIsEtatConfirme() && indVoeuPojo.getHasIAForVoeu()) {
                 // true si aucune vet n'a son IA Web ouverte
                 noIAOpen = determineNoIAOpen(indPrimo, formatter, listVetIANotOpen, indVoeuPojo);
             }
         }
 
         // Edition du contenu du bloc
         if (noIAOpen) {
             // Aucune IA Web n'est ouverte
             result += getString("CONFIRMATION.IA_WEB.NOT_OPEN",
                     Utilitaires.convertDateToString(
                             dateOuverture, Constantes.DATE_FORMAT));
         } else {
             if (indPrimo) {
                 // cas d'un primo
                 result += getString(
                         "CONFIRMATION.IA_WEB.OPEN_PRIMO", adresseIA);
             } else {
                 Boolean isInterrupt = true;
                 Campagne camp = Utilitaires.getCampagneEnServ(ind, getDomainService());
                 int anuPrecInt = Integer.parseInt(camp.getCodAnu()) - 1;
                 String[] anneesIA = getDomainApoService().getAnneesIa(ind);
                 if (anneesIA != null) {
                     for (String anneeIa : anneesIA) {
                         if (anneeIa.equals(Integer.toString(anuPrecInt))) {
                             isInterrupt = false;
                             break;
                         }
                     }
                 }
                 if (!isInterrupt) {
                     // si l'étudiant est inscrit cet année,
                     // on le redirige vers son ENT
                     result += getString(
                             "CONFIRMATION.IA_WEB.OPEN_REINS");
                 } else {
                     // sinon, on le redirige vers l'ia reins
                     result += getString(
                             "CONFIRMATION.IA_WEB.OPEN_PRIMO",
                             adresseIA);
                 }
 
             }
         }
         // certaines des IA Web ne sont pas ouvertes
         if (!listVetIANotOpen.isEmpty()) {
             result += getString("CONFIRMATION.IA_WEB.SOME_VET_NOT_OPEN");
             StringBuffer html = new StringBuffer();
             for (VersionEtapeDTO vetNotOpen : listVetIANotOpen) {
                 Date date;
                 if (indPrimo && vetNotOpen.getDatDebMinpVet() != null) {
                     date =
                             vetNotOpen.getDatDebMinpVet() == null ? null : DateUtil.transformIntoDate(vetNotOpen.getDatDebMinpVet());
                 } else if (vetNotOpen.getDatDebMinVet() != null) {
                     date =
                             vetNotOpen.getDatDebMinVet() == null ? null : DateUtil.transformIntoDate(vetNotOpen.getDatDebMinVet());
                 } else {
                     date = dateOuverture;
                 }
                 html.append(getString("CONFIRMATION.IA_WEB.VET_NOT_OPEN",
                         vetNotOpen.getLibWebVet(),
                         Utilitaires.convertDateToString(
                                 date, Constantes.DATE_FORMAT)));
             }
             result += html.toString();
         }
         return result;
     }
 
     /**
      * Retourne true si aucune vet n'a son IA Web ouverte
      *
      * @param indPrimo
      * @param formatter
      * @param listVetIANotOpen
      * @param indVoeuPojo
      * @return
      * @throws ParseException
      */
     private boolean determineNoIAOpen(boolean indPrimo, SimpleDateFormat formatter, Set<VersionEtapeDTO> listVetIANotOpen, IndVoeuPojo indVoeuPojo) throws ParseException {
         boolean result = true;
         VersionEtapeDTO vet = indVoeuPojo.getVrsEtape();
         Date dateDeb;
         Date dateFin;
         if (indPrimo) {
             //TODO better handle (once there is functional test) the date == null because it's already checked on next ifelse below
             dateDeb = vet.getDatDebMinpVet() == null ? null : DateUtil.transformIntoDate(vet.getDatDebMinpVet());
             dateFin =
                     vet.getDatFinMinpVet() == null ? null : DateUtil.transformIntoDate(vet.getDatFinMinpVet());
         } else {
             dateDeb =
                     vet.getDatDebMinVet() == null ? null : DateUtil.transformIntoDate(vet.getDatDebMinVet());
             dateFin =
                     vet.getDatFinMinVet() == null ? null : DateUtil.transformIntoDate(vet.getDatFinMinVet());
         }
 
         Date today;
         today = formatter.parse(Utilitaires.convertDateToString(
                 new Date(), Constantes.APOGEE_DATE_FORMAT));
         // on compare avec les dates saisies dans la vet
         if (dateDeb != null && dateFin != null) {
             if ((today.after(dateDeb) || today.equals(dateDeb))
                     && (today.before(dateFin)
                     || today.equals(dateFin))) {
                 result = false;
             } else {
                 listVetIANotOpen.add(vet);
             }
         } else {
             // sinon, on compare avec la date d'ouverture présente
             // dans le référentiel
             Date dateDebWebPrimoNb = formatter.parse(
                     getDomainApoService().getDateDebWebPrimoNb());
             Date dateDebWebPrimoNnb = formatter.parse(
                     getDomainApoService().getDateDebWebPrimoNnb());
             Date dateDebWeb = formatter.parse(getDomainApoService().getDateDebWeb());
             Date dateFinWebPrimoNb = formatter.parse(
                     getDomainApoService().getVariableAppli("DAT_FIN_WB_PRIMO_NB"));
             Date dateFinWebPrimoNnb = formatter.parse(
                     getDomainApoService().getVariableAppli("DAT_FIN_WB_PRIMO_NNB"));
             Date dateFinWeb = formatter.parse(getDomainApoService().getVariableAppli("DAT_FIN_WEB"));
             if (indPrimo && (((today.after(dateDebWebPrimoNb)
                     || today.equals(dateDebWebPrimoNb))
                     && (today.before(dateFinWebPrimoNb)
                     || today.equals(dateFinWebPrimoNb)))
                     || ((today.after(dateDebWebPrimoNnb)
                     || today.equals(dateDebWebPrimoNnb))
                     && (today.before(dateFinWebPrimoNnb)
                     || today.equals(dateFinWebPrimoNnb))))) {
                 result = false;
             } else if (!indPrimo && ((today.after(dateDebWeb)
                     || today.equals(dateDebWeb))
                     && (today.before(dateFinWeb)
                     || today.equals(dateFinWeb)))) {
                 result = false;
             } else {
                 listVetIANotOpen.add(vet);
             }
 
         }
         return result;
     }
 
     private Date determineDateOuverture(boolean indPrimo, SimpleDateFormat formatter) throws ParseException {
         Date dateOuverture;
         Date dateDebWebPrimoNb = formatter.parse(
                 getDomainApoService().getDateDebWebPrimoNb());
         Date dateDebWebPrimoNnb = formatter.parse(
                 getDomainApoService().getDateDebWebPrimoNnb());
         Date dateDebWeb = formatter.parse(getDomainApoService().getDateDebWeb());
         if (indPrimo) {
             dateOuverture = dateDebWebPrimoNb == null ? dateDebWebPrimoNnb : dateDebWebPrimoNb;
         } else {
             dateOuverture = dateDebWeb;
         }
         return dateOuverture;
     }
 
     private String determineAdressIA(Individu ind) {
         String datNais = new SimpleDateFormat(
                 Constantes.DATE_SHORT_FORMAT).format(ind.getDateNaissance());
         String adresseIA = "";
         if (StringUtils.hasText(ind.getCodeEtu())) {
             adresseIA =
                     addressIAReins + "?etape=0&user="
                             + ind.getCodeEtu() + "&motDePasseS=" + datNais;
 
         } else {
             adresseIA =
                     addressIAPrimo + "?codIndOpi="
                             + ind.getNumDossierOpi() + "&datNai=" + datNais;
         }
         return adresseIA;
     }
 
     /**
      * Initialise la liste des voeux favorables.
      */
     public void initIndVoeuxPojoFav() {
         // récupère la liste des commissions
         Set<Commission> listComm = getParameterService().getCommissions(true);
         // Initialise la liste des voeux favorables
         Set<IndVoeuPojo> listIndVoeu = getCurrentInd().getIndVoeuxPojo();
         for (IndVoeuPojo indVoeuPojo : listIndVoeu) {
             Avis a = indVoeuPojo.getAvisEnService();
             // Sort the type of avis
            if (a != null && a.getResult().getIsFinal()
                     && a.getResult().getCodeTypeConvocation()
                     .equals(inscriptionAdm.getCode())) {
                 if (indVoeuPojo.getIsEtatConfirme()) {
                     indVoeuPojo.setStateConf("STATE.CONFIRM");
                 }
                 if (indVoeuPojo.getIsEtatDesiste()) {
                     indVoeuPojo.setStateConf("STATE.DESIST");
                 }
                 // teste si le calendrier d'inscription n'est pas fermé
                 CalendarCmi calCmi = BusinessUtil.getCmiForVetDTO(listComm,
                         indVoeuPojo.getVrsEtape()).getCalendarCmi();
 
                 SimpleDateFormat formatter = new SimpleDateFormat(Constantes.ENGLISH_DATE_FORMAT);
                 ParsePosition pos = new ParsePosition(0);
                 Date today = formatter.parse(Utilitaires.convertDateToString(
                         new Date(), Constantes.ENGLISH_DATE_FORMAT), pos);
 
                 Boolean canConfirm = false;
                 if (calCmi == null) {
                     canConfirm = true;
                 } else if (calCmi.getEndDatConfRes() == null
                         || today.before(calCmi.getEndDatConfRes())
                         || today.equals(calCmi.getEndDatConfRes())) {
                     canConfirm = true;
                 }
                 // si l'utilisateur n'est pas un gestionnaire sans droit ET
                 // si le calendrier est ferme ou si le voeu est desiste
                 // l'utilisateur ne peut pas confirmer le voeu
                 if (getCurrentInd().getAsRightsToUpdate() && getCurrentInd().getIsManager()) {
                     indVoeuPojo.setDisableConfirm(false);
                 } else if (!getCurrentInd().getAsRightsToUpdate() && getCurrentInd().getIsManager()) {
                     indVoeuPojo.setDisableConfirm(true);
                 } else {
                     indVoeuPojo.setDisableConfirm(!canConfirm || indVoeuPojo.getIsEtatDesiste());
                 }
 
                 // recuperation du rdv
                 indVoeuPojo.setIndividuDate(getDomainService().getIndividuDate(
                         indVoeuPojo.getIndVoeu()));
 
                 indVoeuxPojoFav.add(indVoeuPojo);
             }
         }
     }
 
     /**
      * @return true si au moins un voeu est favorable
      */
     public Boolean getCanConfirmVoeux() {
         //can be null when it's the first connect for an individu (in ent)
         if (getCurrentInd() != null) {
             Set<IndVoeuPojo> listIndVoeu = getCurrentInd().getIndVoeuxPojo();
             for (IndVoeuPojo indVoeuPojo : listIndVoeu) {
                 Avis a = indVoeuPojo.getAvisEnService();
                 // Sort the type of avis
                 if (a != null && a.getValidation() && a.getResult().getIsFinal()
                         && a.getResult().getCodeTypeConvocation()
                         .equals(inscriptionAdm.getCode())
                         ) {
                     return true;
                 }
             }
         }
         return false;
     }
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
 
     /**
      * @return the indVoeuxPojoFav
      */
     public Set<IndVoeuPojo> getIndVoeuxPojoFav() {
         return indVoeuxPojoFav;
     }
 
     /**
      * @param indVoeuxPojoFav the indVoeuxPojoFav to set
      */
     public void setIndVoeuxPojoFav(final Set<IndVoeuPojo> indVoeuxPojoFav) {
         this.indVoeuxPojoFav = indVoeuxPojoFav;
     }
 
     /**
      * List of IndVoeuPojo in use.
      *
      * @return
      */
     public List<IndVoeuPojo> getIndVoeuxPojoFavItems() {
         List<IndVoeuPojo> indVoeuxPojo = new ArrayList<IndVoeuPojo>();
         indVoeuxPojo.addAll(indVoeuxPojoFav);
         return indVoeuxPojo;
     }
 
     /**
      * @return the addressIAPrimo
      */
     public String getAddressIAPrimo() {
         return addressIAPrimo;
     }
 
     /**
      * @param addressIAPrimo the addressIAPrimo to set
      */
     public void setAddressIAPrimo(final String addressIAPrimo) {
         this.addressIAPrimo = addressIAPrimo;
     }
 
     /**
      * @return the addressIAReins
      */
     public String getAddressIAReins() {
         return addressIAReins;
     }
 
     /**
      * @param addressIAReins the addressIAReins to set
      */
     public void setAddressIAReins(final String addressIAReins) {
         this.addressIAReins = addressIAReins;
     }
 
     /**
      * @return the inscriptionAdm
      */
     public InscriptionAdm getInscriptionAdm() {
         return inscriptionAdm;
     }
 
     /**
      * @param inscriptionAdm the inscriptionAdm to set
      */
     public void setInscriptionAdm(final InscriptionAdm inscriptionAdm) {
         this.inscriptionAdm = inscriptionAdm;
     }
 
     /**
      * @return the saisieRdvEtuController
      */
     public SaisieRdvEtuController getSaisieRdvEtuController() {
         return saisieRdvEtuController;
     }
 
     /**
      * @param saisieRdvEtuController the saisieRdvEtuController to set
      */
     public void setSaisieRdvEtuController(
             final SaisieRdvEtuController saisieRdvEtuController) {
         this.saisieRdvEtuController = saisieRdvEtuController;
     }
 
     /**
      * @param opiWebService the opiWebService to set
      */
     public void setOpiWebService(final OpiWebService opiWebService) {
         this.opiWebService = opiWebService;
     }
 
     /**
      * @return the smtpService
      */
     public SmtpService getSmtpService() {
         return smtpService;
     }
 
     /**
      * @param smtpService the smtpService to set
      */
     public void setSmtpService(final SmtpService smtpService) {
         this.smtpService = smtpService;
     }
 
     /**
      * @return the validOpinionController
      */
     public ValidOpinionController getValidOpinionController() {
         return validOpinionController;
     }
 
     /**
      * @param validOpinionController the validOpinionController to set
      */
     public void setValidOpinionController(
             ValidOpinionController validOpinionController) {
         this.validOpinionController = validOpinionController;
     }
 }
