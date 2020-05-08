 /**
  *
  */
 package org.esupportail.opi.web.controllers.formation;
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.services.smtp.SmtpService;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.BusinessUtil;
 import org.esupportail.opi.domain.beans.etat.EtatNonArrive;
 import org.esupportail.opi.domain.beans.formation.Cles2AnnuForm;
 import org.esupportail.opi.domain.beans.formation.Domaine2AnnuForm;
 import org.esupportail.opi.domain.beans.formation.GrpTypDip;
 import org.esupportail.opi.domain.beans.parameters.*;
 import org.esupportail.opi.domain.beans.references.NombreVoeuCge;
 import org.esupportail.opi.domain.beans.references.calendar.CalendarCmi;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.ContactCommission;
 import org.esupportail.opi.domain.beans.references.commission.LinkTrtCmiCamp;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.domain.beans.user.candidature.VersionEtpOpi;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.utils.Conversions;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.parameters.FormationContinue;
 import org.esupportail.opi.web.beans.parameters.FormationInitiale;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.*;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorSelectItem;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractAccessController;
 import org.esupportail.wssi.services.remote.VersionDiplomeDTO;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.springframework.util.StringUtils;
 
 import javax.faces.model.SelectItem;
 import javax.faces.model.SelectItemGroup;
 import java.util.*;
 import java.util.Map.Entry;
 
 
 /**
  * @author cleprous
  *         FormationController : managed the formation for candidat.
  */
 public class FormationController extends AbstractAccessController {    /*
 	 ******************* PROPERTIES ******************* */
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = -8192605406046759423L;
 
     /**
      * Code groupe Type diplome pour les licences.
      */
     private static final String COD_LICENCE = "LICENCE";
 
     /**
      * Code groupe Type diplome pour les Masters.
      */
     private static final String COD_MASTER = "MASTER";
 
     /**
      * Code groupe Type diplome pour les Doctorats.
      */
     private static final String COD_DOCTORAT = "DOCTORAT";
 
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
 
     /**
      * The SearchFormationPojo.
      */
     private SearchFormationPojo searchFormationPojo;
 
     /**
      * the ActionEnum.
      */
     private ActionEnum action;
 
     /**
      * indVoeuPojo selected to do anywhere action.
      */
     private IndVoeuPojo indVoeuPojo;
 
     /**
      * see {@link SummaryWishesPojo}.
      */
     private List<SummaryWishesPojo> summaryWishes;
 
     /**
      * see {@link SmtpService}.
      */
     private SmtpService smtpService;
 	
 
 	/*
 	 ******************* INIT ************************* */
 
     /**
      * Constructors.
      */
     public FormationController() {
         super();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         searchFormationPojo = new SearchFormationPojo();
         action = new ActionEnum();
         indVoeuPojo = null;
         summaryWishes = null;
 
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractContextAwareController#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         super.afterPropertiesSetInternal();
         Assert.notNull(this.smtpService,
                 "property smtpService of class " + this.getClass().getName() + " can not be null");
         reset();
     }
 	
 
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * Callback vers le recapitulatif des voeux du candidat.
      *
      * @return String
      */
     public String goRecapChoice() {
         return NavigationRulesConst.RECAP_FORMATION;
     }
 
     /**
      * Go to search Formation.
      *
      * @return String
      */
     public String goSearchFormation() {
         reset();
         return NavigationRulesConst.SEARCH_FORMATION;
     }
 
     /**
      * Go to search Formation again.
      *
      * @return String
      */
     public String goSearchAgain() {
         searchFormationPojo.resetSearch();
         return NavigationRulesConst.SEARCH_FORMATION;
     }
 
     /**
      * Go to diplay individu's vows.
      *
      * @return String
      */
     public String goSeeVoeux() {
         return NavigationRulesConst.SEE_VOEUX;
     }
 
     /**
      * @return String
      */
     public String goSummaryWishes() {
         Set<IndVoeuPojo> i = new HashSet<IndVoeuPojo>();
         i.add(indVoeuPojo);
         reset();
         initSummaryWishes(i);
         return NavigationRulesConst.SUMMARY_WISHES;
     }
 
 	/*
 	 ******************* METHODS ********************** */
 
     /**
      * Add a vows in etapeChoice to the database.
      *
      * @return String
      */
     public String add() {
         Set<IndVoeuPojo> indVoeuAdd = new HashSet<IndVoeuPojo>();
 
         // on récupère le régime
         RegimeInscription regime = getRegimeIns().get(
                 Utilitaires.getCodeRIIndividu(getCurrentInd().getIndividu(), getDomainService()));
 
         // récupération de la campagne
         Campagne campagne = getParameterService().getCampagneEnServ(regime.getCode());
 
         for (VersionEtapePojo vrsVet : searchFormationPojo.getVrsEtpSelected()) {
             IndVoeu vb = null;
             //Search the type traitement by default to put it in the student's choice
             // 1 cherche un traitementCMI de type vrsVet
             TraitementCmi traitementCmi = getParameterService().getTraitementCmi(
                     new VersionEtpOpi(vrsVet.getVersionEtape()), true);
             TypeTraitement typTrt = null;
             // 2 recupere type traitement par defaut
             Boolean authorizedAddWish = null;
             if (traitementCmi != null) {
                 // on récupère le linkTrtCmiCamp
                 LinkTrtCmiCamp linkTrtCmiCamp = getParameterService().
                         getLinkTrtCmiCamp(traitementCmi, campagne);
                 // création du voeu
                 vb = new IndVoeu(linkTrtCmiCamp, getCurrentInd().getIndividu());
                 vb = (IndVoeu) getDomainService().add(vb,
                         Utilitaires.codUserThatIsAction(getCurrentGest(), getCurrentInd()));
                 //All vows save init to state non arrive.
                 vb.setState(EtatNonArrive.I18N_STATE);
                 typTrt = BusinessUtil.getTypeTraitement(
                         getParameterService().getTypeTraitements(),
                         traitementCmi.getCodTypeTrait());
                 vb.setCodTypeTrait(typTrt.getCode());
                 String codCge = traitementCmi.getVersionEtpOpi().getCodCge();
                 //vb.getVersionEtpOpi().setCodCge(codCge);
                 if (typTrt instanceof AccesSelectif) {
                     vb.setHaveBeTraited(true);
                 }
                 // dans le cas de la formation continue
                 // on met automatiquement en VA et traité
                 if (regime instanceof FormationContinue) {
                     vb.setCodTypeTrait(BusinessUtil.getCodeVATypeTraitement(
                             getParameterService().getTypeTraitements()));
                     vb.setHaveBeTraited(true);
                 }
                 //control VET par CGE.
                 authorizedAddWish =
                         controlNbVowByCge(getCurrentInd().getIndividu().getVoeux(), codCge);
 
             }
             if (authorizedAddWish != null && authorizedAddWish) {
                 CalendarRDV cal = Utilitaires.getRecupCalendarRdv(vb, getParameterService().getCalendarRdv());
                 IndVoeuPojo indVoeuP = new IndVoeuPojo(
                         vb, vrsVet.getVersionEtape(),
                         getI18nService(), false,
                         typTrt, cal);
                 indVoeuAdd.add(indVoeuP);
                 getDomainService().addIndVoeu(vb);
                 getCurrentInd().getIndividu().getVoeux().add(vb);
                 getCurrentInd().getIndVoeuxPojo().add(indVoeuP);
 
 
             } else {
                 addErrorMessage(null, "ERROR.NB_WISH_MAX", vrsVet.getVersionEtape().getLibWebVet());
             }
         }
 
 
         //sendMail
         sendMailIfAddWishes(indVoeuAdd);
         reset();
 
         //init the information to individual.
         initSummaryWishes(indVoeuAdd);
 
         if (!indVoeuAdd.isEmpty()) {
             log.info("save to database the wishes for the individu"
                     + " with the num dossier = "
                     + getCurrentInd().getIndividu().getNumDossierOpi());
         }
 
         return NavigationRulesConst.SUMMARY_WISHES;
     }
 
     /**
      * Delete an IndVoeu in indVoeuToDelete attribute.
      */
     public void delete() {
         getDomainService().deleteIndVoeu(indVoeuPojo.getIndVoeu());
         reset();
     }
 
     /**
      * Recupere les versions etapes en fonction de la version Diplome selectionnee.
      */
    public String selectEtape() {
         Campagne camp = getParameterService()
                 .getCampagneEnServ(getCodeRI());
         List<VersionEtapeDTO> list = getDomainApoService().getVersionEtapes(
                 searchFormationPojo.getVrsDipSelected(), camp.getCodAnu());
 
         //on ne doit garder que les version Etape rattache a au moins une commission.
 
         //1. on regarde si c'est rattachee des commissions et on cree une map contenant cmi et vrsEtp.
         Set<Commission> cmi = getParameterService().getCommissions(true);
         Map<Commission, Set<VersionEtapeDTO>> mapCmi = Utilitaires.getCmiForVetDTO(cmi, new HashSet<VersionEtapeDTO>(list), camp);
 
         if (log.isDebugEnabled()) {
             log.debug("map commission - vet = " + mapCmi);
         }
         //on regarde si le calendrier d'inscription sont ouvert
         searchFormationPojo.setVersionEtapes(
                 getManagedCalendar().getVrsEtpPojo(mapCmi,
                         getCurrentInd()));
        return null;
     }
 
     /**
      * Recupere les versions etapes en fonction de la version Diplome selectionnee.
      */
     public void selectEtapeNotCurrentUser() {
         Campagne camp = getParameterService()
                 .getCampagneEnServ(getCodeRI());
         List<VersionEtapeDTO> list = getDomainApoService().getVersionEtapes(
                 searchFormationPojo.getVrsDipSelected(), camp.getCodAnu());
 
         //on ne doit garder que les version Etape rattache a au moins une commission.
 
         //1. on regarde si c'est rattachee des commissions et on cree une map contenant cmi et vrsEtp.
         Set<Commission> cmi = getParameterService().getCommissions(true);
         Map<Commission, Set<VersionEtapeDTO>> mapCmi = Utilitaires.getCmiForVetDTO(cmi, new HashSet<VersionEtapeDTO>(list), camp);
 
         if (log.isDebugEnabled()) {
             log.debug("map commission - vet = " + mapCmi);
         }
         //on regarde si le calendrier d'inscription sont ouvert
         searchFormationPojo.setVersionEtapes(
                 getManagedCalendar().getVrsEtpPojo(mapCmi));
     }
 
     /**
      * The item for the key words.
      *
      * @return List< SelectItem>
      */
     public List<SelectItem> getKeyWordItems() {
         List<SelectItem> l = new ArrayList<SelectItem>();
         if (getSearchFormationPojo().getGroupTypSelected() != null) {
             String locale = getSessionController().getLocale().getLanguage();
 
             Map<Domaine2AnnuForm, List<Cles2AnnuForm>> domains =
                     getDomainApoService().getDomaine2AnnuForm(
                             getSearchFormationPojo().getGroupTypSelected(), locale.toUpperCase());
 
             List<SelectItem> listGroup = new ArrayList<SelectItem>();
             SelectItemGroup group = null;
             l.add(new SelectItem("", ""));
 
             for (Entry<Domaine2AnnuForm, List<Cles2AnnuForm>> entry : domains.entrySet()) {
                 Set<SelectItem> si = new TreeSet<SelectItem>(new Comparator<SelectItem>() {
 
                     @Override
                     public int compare(SelectItem s1, SelectItem s2) {
                         String cmp1 = s1.getLabel() + s1.getValue();
                         String cmp2 = s2.getLabel() + s2.getValue();
                         return cmp1.compareToIgnoreCase(cmp2);
                     }
                 });
                 for (Cles2AnnuForm cles : entry.getValue()) {
                     SelectItem s = new SelectItem(cles.getClesAnnuForm().getCodCles(), cles.getLibCles());
                     si.add(s);
                 }
                 if (si.size() != 0) {
                     group = new SelectItemGroup(entry.getKey().getLibDom(), null, false, si.toArray(new SelectItem[si.size()]));
                     listGroup.add(group);
                 }
 
             }
             Collections.sort(listGroup, new ComparatorSelectItem());
             l.addAll(listGroup);
 
         }
         return l;
     }
 
     /**
      * Make the Version diplome list for the codKeyWordSelected.
      */
     public void selectKeyWord() {
         Campagne camp = getParameterService()
                 .getCampagneEnServ(getCodeRI());
        getSearchFormationPojo().setVersionEtapes(null);
         if (StringUtils.hasText(getSearchFormationPojo().getCodKeyWordSelected())) {
             getSearchFormationPojo().setVersionDiplomes(getDomainApoService()
                     .getVersionDiplomes(getSearchFormationPojo().getCodKeyWordSelected(),
                             getSearchFormationPojo().getGroupTypSelected(),
                             camp.getCodAnu()));
             Set<Commission> cmi = getParameterService().getCommissions(true);
             List<VersionDiplomeDTO> vdi = new ArrayList<VersionDiplomeDTO>();
             //on retire le diplome qui n'ont pas de VET rattachees e des commissions.
             for (VersionDiplomeDTO v : getSearchFormationPojo().getVersionDiplomes()) {
                 List<VersionEtapeDTO> list = getDomainApoService().getVersionEtapes(
                         v, camp.getCodAnu());
 
                 Map<Commission, Set<VersionEtapeDTO>> mapCmi =
                         Utilitaires.getCmiForVetDTO(cmi, new HashSet<VersionEtapeDTO>(list), camp);
 
                 if (!mapCmi.isEmpty()) {
                     vdi.add(v);
                 }
             }
             getSearchFormationPojo().setVersionDiplomes(vdi);
 
         }
 
 
     }
 
 
     /**
      * Init the initSummaryWishes attributes.
      *
      * @param indVoeuPojos
      */
     private void initSummaryWishes(final Set<IndVoeuPojo> indVoeuPojos) {
         Individu ind = getCurrentInd().getIndividu();
         RegimeInscription regimeIns = getRegimeIns().get(Utilitaires.getCodeRIIndividu(ind,
                 getDomainService()));
         Campagne camp = getParameterService()
                 .getCampagneEnServ(regimeIns.getCode());
         //list of all commissions in use
         Set<Commission> cmi = getParameterService().getCommissions(true);
 
         //map with the commission and its etapes sur lesquelles le candidat a deposer des voeux
         Map<Commission, Set<VersionEtapeDTO>> mapCmi = Utilitaires.getCmiForIndVoeux(cmi, indVoeuPojos, camp);
 
         summaryWishes = new ArrayList<SummaryWishesPojo>();
         for (Map.Entry<Commission, Set<VersionEtapeDTO>> cEntry : mapCmi.entrySet()) {
             Commission c = getParameterService().getCommission(null, cEntry.getKey().getCode());
             ContactCommission contact = new ContactCommission();
             if (c.getContactsCommission().get(regimeIns.getCode()) != null) {
                 contact = c.getContactsCommission().get(regimeIns.getCode());
             }
             SummaryWishesPojo s = new SummaryWishesPojo();
             CommissionPojo cmiPojo = new CommissionPojo(
                     c, new AdressePojo(contact.getAdresse(), getDomainApoService()), contact);
             CalendarCmi calCmi = cmiPojo.getCommission().getCalendarCmi();
             //initialize the proxy hibernate
     		getDomainService().initOneProxyHib(calCmi, calCmi.getDatEndBackDossier(), Calendar.class);
             s.setCommission(cmiPojo);
             List<PieceJustificative> listPJ = getParameterService().getPiecesJ(
                     Conversions.convertVetInVetOpi(cEntry.getValue()),
                     String.valueOf(regimeIns.getCode()));
             Collections.sort(listPJ, new ComparatorString(Nomenclature.class));
             s.setPieces(listPJ);
             Set<VersionEtpOpi> vOpi = Conversions.convertVetInVetOpi(cEntry.getValue());
             Boolean canDownload = true;
             for (VersionEtpOpi v : vOpi) {
                 for (IndVoeuPojo indVPojo : indVoeuPojos) {
                     if (indVPojo.getIndVoeu().getLinkTrtCmiCamp()
                             .getTraitementCmi().getVersionEtpOpi().equals(v)) {
                         s.getVows().add(indVPojo);
                         if (canDownload) {
                             canDownload = indVPojo.
                                     getTypeTraitement().getDownloadDocument();
                         }
 //						if (!(indVPojo.getTypeTraitement() instanceof AccesSelectif)) {
 //							canDownload = true;
 //							
 //						}
                         //comment parce qu'on doit parcourir tous
                         //les voeux pour ajouter tous ceux de la cmi
                         //break;
                     }
                     //if (notAS) { break; }
                 }
                 //if (notAS) { break; }
             }
             s.setCanDonwload(canDownload);
 
             summaryWishes.add(s);
 
         }
     }
 
     /**
      * Parcours la liste des voeux ajoutes et envoie un plusieurs mail en fonction du cas.
      * <p/>
      * Si typTrt = AS : un mail par commission
      * sinon = un mail pour toutes les VET.
      *
      * @param indVoeuAdd
      */
     private void sendMailIfAddWishes(final Set<IndVoeuPojo> indVoeuAdd) {
         Individu ind = getCurrentInd().getIndividu();
         RegimeInscription regimeIns = getRegimeIns().get(Utilitaires.getCodeRIIndividu(ind,
                 getDomainService()));
         Campagne camp = getParameterService()
                 .getCampagneEnServ(regimeIns.getCode());
         Boolean sendMail = false;
         Map<Commission, Set<VersionEtapeDTO>> wishesByCmi = Utilitaires.getCmiForIndVoeux(
                 getParameterService().getCommissions(true),
                 indVoeuAdd, camp);
 
 //		Map<Commission, Set<VersionEtapeDTO>> wishesVaOrTr = 
 //				new HashMap<Commission, Set<VersionEtapeDTO>>();
         for (Map.Entry<Commission, Set<VersionEtapeDTO>> cmiEntry : wishesByCmi.entrySet()) {
             Commission cmi = cmiEntry.getKey();
             CommissionPojo cmiPojo = new CommissionPojo(
                     cmi,
                     new AdressePojo(cmi.getContactsCommission().get(regimeIns.getCode())
                             .getAdresse(), getDomainApoService()),
                     cmi.getContactsCommission().get(regimeIns.getCode()));
             TypeTraitement typTrt = null;
             Set<VersionEtapeDTO> vetDTO = cmiEntry.getValue();
             for (VersionEtapeDTO vDTO : vetDTO) {
                 for (IndVoeuPojo iPojo : indVoeuAdd) {
                     TraitementCmi trtCmi = iPojo.getIndVoeu().getLinkTrtCmiCamp()
                             .getTraitementCmi();
                     if (vDTO.getCodEtp().equals(
                             trtCmi.getVersionEtpOpi().getCodEtp())
                             && vDTO.getCodVrsVet().equals(
                             trtCmi.getVersionEtpOpi().getCodVrsVet())) {
                         //on regarde son type de traitement
                         typTrt = iPojo.getTypeTraitement();
                         break;
                     }
                 }
                 if (typTrt != null) {
                     break;
                 }
             }
             List<Object> list = new ArrayList<Object>();
             list.add(vetDTO);
             list.add(ind);
             list.add(cmiPojo);
             if (typTrt instanceof AccesSelectif) {
                 //on envoie un mail par commission
                 if (regimeIns.getMailAddWishesAS() != null) {
                     regimeIns.getMailAddWishesAS().send(ind.getAdressMail(),
                             ind.getEmailAnnuaire(), list);
                     sendMail = true;
                 }
             } else {
 //				wishesVaOrTr.put(cmi, wishesByCmi.get(cmi));
                 if (regimeIns.getMailAddWishesTRVA() != null) {
                     regimeIns.getMailAddWishesTRVA().send(ind.getAdressMail(),
                             ind.getEmailAnnuaire(), list);
                     sendMail = true;
                 }
             }
         }
 
 //		if (!wishesVaOrTr.isEmpty()) {
 //			//send on mail for type trt VA and TR
 //			Set<VersionEtapeDTO> vetMail = new HashSet<VersionEtapeDTO>();
 //			for (Commission cmi : wishesVaOrTr.keySet()) {
 //				Set<VersionEtapeDTO> vetDTO = wishesVaOrTr.get(cmi);
 //				for (VersionEtapeDTO vDTO : vetDTO) {
 //					vetMail.add(vDTO);
 //				}
 //			}
 //			// TODO ajouter les commissions pojo??? FC
 //			if (regimeIns.getMailAddWishesTRVA() !=  null) {
 //				List<Object> list = new ArrayList<Object>();
 //				list.add(ind);
 //				list.add(vetMail);
 //				regimeIns.getMailAddWishesTRVA().send(ind.getAdressMail(), 
 //						ind.getEmailAnnuaire(), list);
 //				sendMail = true;
 //			}
 //		}
 
         if (sendMail) {
             addInfoMessage(null, "INFO.CANDIDAT.SEND_MAIL.SUMMARY_WISHES");
         }
 
     }
 
 
     /**
      * control if the candidate does not exceed
      * the number authorized by the wish management center.
      *
      * @param listIndVoeu
      * @return Boolean false if exceed the number authorized.
      */
     private Boolean controlNbVowByCge(final Set<IndVoeu> listIndVoeu, final String codCge) {
         Integer cpt = 0;
         for (IndVoeu i : listIndVoeu) {
             TraitementCmi trtCmi = getParameterService().getTraitementCmi(i.getLinkTrtCmiCamp().getTraitementCmi().getId());
             if (trtCmi.getVersionEtpOpi().getCodCge().equals(codCge)) {
                 cpt++;
             }
             if (cpt >= nbVoeu(codCge)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * @param codCge
      * @return NB_VOEU_BY_CGE
      */
     private int nbVoeu(final String codCge) {
         List<NombreVoeuCge> listNbVoeuByCGE =
                 getParameterService().getAllNombreDeVoeuByCge();
         for (NombreVoeuCge nbVoeuByCGE : listNbVoeuByCGE) {
             if (codCge.equals(nbVoeuByCGE.getCodeCge())) {
                 return nbVoeuByCGE.getNbVoeu();
             }
         }
         return Constantes.DEFAULT_NB_VOEU_BY_CGE;
     }
 
 
     /**
      * @return le codeRI soit du gestionnaire, soit du candidat soit celui par defaut.
      */
     public Integer getCodeRI() {
         Integer codeRI = null;
         if (getSessionController().getCurrentUser() instanceof Gestionnaire) {
             Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
             codeRI = gest.getProfile().getCodeRI();
         } else if (getCurrentInd() != null) {
             codeRI = Utilitaires.getCodeRIIndividu(getCurrentInd().getIndividu(),
                     getDomainService());
         } else {
             // cas par défaut, le codeRI est formation Initiale
             codeRI = FormationInitiale.CODE;
         }
         return codeRI;
     }
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
     /**
      * The all Ren1GrpTypDip in use wihout LICENCE, MASTER, DOCTORAT.
      *
      * @return List< Ren1GrpTypDip>
      */
     public List<GrpTypDip> getGroupTypeDip() {
         Campagne camp = getParameterService()
                 .getCampagneEnServ(getCodeRI());
         List<GrpTypDip> l = new ArrayList<GrpTypDip>();
         List<GrpTypDip> group = getDomainApoService().getGrpTypDip(camp);
 
         for (GrpTypDip r : group) {
             if (!r.getCodGrpTpd().equals(COD_LICENCE)
                     && !r.getCodGrpTpd().equals(COD_MASTER)
                     && !r.getCodGrpTpd().equals(COD_DOCTORAT)) {
                 l.add(r);
             }
         }
         return l;
     }
 
     /**
      * @return the group licence
      */
     public GrpTypDip getLicence() {
         Campagne camp = getParameterService()
                 .getCampagneEnServ(getCodeRI());
 
         List<GrpTypDip> group = getDomainApoService().getGrpTypDip(camp);
         for (GrpTypDip r : group) {
             if (r.getCodGrpTpd().equals(COD_LICENCE)) {
                 return r;
             }
         }
         return null;
 
 
     }
 
     /**
      * @return the group Master
      */
     public GrpTypDip getMaster() {
         Campagne camp = getParameterService()
                 .getCampagneEnServ(getCodeRI());
         List<GrpTypDip> group = getDomainApoService().getGrpTypDip(camp);
         for (GrpTypDip r : group) {
             if (r.getCodGrpTpd().equals(COD_MASTER)) {
                 return r;
             }
         }
         return null;
     }
 
     /**
      * @return the group Doctorat
      */
     public GrpTypDip getDoctorat() {
         Campagne camp = getParameterService()
                 .getCampagneEnServ(getCodeRI());
         List<GrpTypDip> group = getDomainApoService().getGrpTypDip(camp);
         for (GrpTypDip r : group) {
             if (r.getCodGrpTpd().equals(COD_DOCTORAT)) {
                 return r;
             }
         }
         return null;
     }
 
     /**
      * @return the searchFormationPojo
      */
     public SearchFormationPojo getSearchFormationPojo() {
         return searchFormationPojo;
     }
 
     /**
      * @param searchFormationPojo the searchFormationPojo to set
      */
     public void setSearchFormationPojo(final SearchFormationPojo searchFormationPojo) {
         this.searchFormationPojo = searchFormationPojo;
     }
 
     /**
      * @return the action
      */
     public ActionEnum getAction() {
         return action;
     }
 
     /**
      * @param action the action to set
      */
     public void setAction(final ActionEnum action) {
         this.action = action;
     }
 
     /**
      * @return the indVoeuToDelete
      */
     public IndVoeuPojo getIndVoeuPojo() {
         return indVoeuPojo;
     }
 
     /**
      * @param indVoeuToDelete the indVoeuToDelete to set
      */
     public void setIndVoeuPojo(final IndVoeuPojo indVoeuToDelete) {
         this.indVoeuPojo = indVoeuToDelete;
     }
 
     /**
      * @return the summaryWishes
      */
     public List<SummaryWishesPojo> getSummaryWishes() {
         return summaryWishes;
     }
 
     /**
      * @param summaryWishes the summaryWishes to set
      */
     public void setSummaryWishes(final List<SummaryWishesPojo> summaryWishes) {
         this.summaryWishes = summaryWishes;
     }
 
     /**
      * @param smtpService the smtpService to set
      */
     public void setSmtpService(final SmtpService smtpService) {
         this.smtpService = smtpService;
     }
 
 }
