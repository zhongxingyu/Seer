 /**
  * 
  */
 package org.esupportail.opi.web.controllers.parameters;
 
 import static fj.data.Stream.iterableStream;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.myfaces.component.html.ext.HtmlInputText;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.parameters.InscriptionAdm;
 import org.esupportail.opi.domain.beans.parameters.MotivationAvis;
 import org.esupportail.opi.domain.beans.parameters.Nomenclature;
 import org.esupportail.opi.domain.beans.parameters.PieceJustiVet;
 import org.esupportail.opi.domain.beans.parameters.PieceJustificative;
 import org.esupportail.opi.domain.beans.parameters.TypeConvocation;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.parameters.TypeTraitement;
 import org.esupportail.opi.domain.beans.user.candidature.VersionEtpOpi;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.beanEnum.WayfEnum;
 import org.esupportail.opi.web.beans.parameters.FormationContinue;
 import org.esupportail.opi.web.beans.parameters.FormationInitiale;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.NomenclaturePojo;
 import org.esupportail.opi.web.beans.pojo.PieceJustiVetPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorSelectItem;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.esupportail.opi.web.controllers.references.EtapeController;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.model.UploadedFile;
 import org.springframework.util.StringUtils;
 
 import fj.F;
 
 /**
  * @author cleprous
  *
  */
 public class NomenclatureController extends AbstractContextAwareController {
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = 6123349004186376190L;
 	/**
 	 * A logger.
 	 */
 	private final Logger log = new LoggerImpl(getClass());
 	
 	
 	
 	/*
 	 ******************* PROPERTIES ******************* */
 	/**
 	 * The Nomenclature.
 	 */
 	private Nomenclature nomenclature;
 	
 	/**
 	 *  The VET (DTO)
 	 */
 	private VersionEtapeDTO vetDTO;
 
 
 	/**
 	 * The actionEnum.
 	 */
 	private ActionEnum actionEnum;
 	
 	/**
 	 * 	The first element of the list is the Nomenclature to add.
 	 * The others elements are all Nomenclature in dataBase.
 	 */
 	private List<Nomenclature> addNomenclatures;
 	
 	/**
 	 * From where you are from.
 	 */
 	private WayfEnum wayfEnum;
 
 	/**
 	 * List of Etapes attached to the PJ. 
 	 */
 	private Set<PieceJustiVetPojo>  allEtapes;
 	
 	/**
 	 * List of PJs attached to the Etape. 
 	 */
 	private Set<NomenclaturePojo>  allPJs;
 	
 	/**
 	 * List of PJs attached to the Etape. 
 	 */
 	private Set<NomenclaturePojo>  addPJs;
 
 	/**
 	 * List of Etapes attached to the PJ to by deleted. 
 	 */
 	private Set<PieceJustiVetPojo>  deleteEtapes;
 	
 	/**
 	 * List of PJs attached to the etapes to by deleted. 
 	 */
 	private Set<NomenclaturePojo>  deletePJs;
 
 	/**
 	 * The Etape to add.
 	 */
 	private PieceJustiVetPojo etapeTraitee; 
 	
 	
 	/**
 	 * The current PJ.
 	 */
 	private NomenclaturePojo PJTraitee;
 
 	/**
 	 * The manager or the versionEtape to add to the pj.
 	 */
 	private Object[] objectToAdd;
 	
 
 	/**
 	 * see {@link EtapeController}.
 	 */
 	private EtapeController etapeController;
 	
 	/**
 	 * see {@link InscriptionAdm}.
 	 */
 	private InscriptionAdm inscriptionAdm;
 	
 	/**
 	 * see {@link EtapeController}
 	 * the inscription regime code given by etapeController
 	 */
 	private int codeRI;
 	
 	/**
 	 * 
 	 */
 	private HtmlInputText code;
 	
 	/**
 	 * 
 	 */
 	private UploadedFile uploadedFile;
 
 	/**
 	 * 
 	 */
 	private String fileName;
 	
 	/**
 	 * return le chemin ou vont être téléchargés les documents liés aux pièces jointes.
 	 */
 	private String uploadPath;
 	
 	/**
 	 * Témoin gérant l'usage ou non de la fonctionnalité de téléchargement de document.
 	 */
 	private boolean useUpload;
 
 	private List<TypeDecision> typesDec;
 	private List<TypeDecision> sortedTypesDec;
 	private List<TypeDecision> typesDecInUse;
 	private List<SelectItem> typesDecItems;
 	private List<SelectItem> typesDecInUseItems;
 	
 	/**
 	 * default value : false.
 	 */
 	private Boolean isFinal = false;
 
 	private static final F<Collection<TypeDecision>, Collection<SelectItem>> typeDec2SelectItems = 
 			new F<Collection<TypeDecision>, Collection<SelectItem>>() {
 		public Collection<SelectItem> f(Collection<TypeDecision> c) {
 			return iterableStream(c).map(new F<TypeDecision, SelectItem>() {
 				public SelectItem f(final TypeDecision t) {
 					return new SelectItem(t, t.getCode() + "-"
 							+ t.getShortLabel());
 				}
 			}).cons(new SelectItem(new TypeDecision(), "")).toCollection();
 		}
 	};
 	
 	/*
 	 ******************* INIT ************************* */
 	/**
 	 * Constructors.
 	 */
 	public NomenclatureController() {
 		super();
 		reset();
 	}
 	
 	public void initTypesDec() {
 		typesDec = new ArrayList<TypeDecision>(
 				getParameterService().getTypeDecisions(null));
 		sortedTypesDec = new ArrayList<TypeDecision>(typesDec);
 		Collections.sort(sortedTypesDec,
 				new ComparatorString(TypeDecision.class));
 		typesDecInUse = new ArrayList<TypeDecision>(
 				getParameterService().getTypeDecisions(true));
 		typesDecItems = new ArrayList<SelectItem>(typeDec2SelectItems.f(typesDec));
 		typesDecInUseItems = new ArrayList<SelectItem>(typeDec2SelectItems.f(typesDecInUse));
 	}
 	
 	/** 
 	 * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
 	 */
 	@Override
 	public void reset() {
 		super.reset();
 		nomenclature = null;
 		actionEnum = new ActionEnum();
 		addNomenclatures = new ArrayList<Nomenclature>();
 		objectToAdd = new Object[0];
 		allEtapes = new HashSet<PieceJustiVetPojo>();
 		allPJs = new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 		addPJs = new HashSet<NomenclaturePojo>();
 		deleteEtapes = new HashSet<PieceJustiVetPojo>();
 		deletePJs = new HashSet<NomenclaturePojo>();	
 		etapeTraitee = new PieceJustiVetPojo();
 		wayfEnum = new WayfEnum();
 	}
 	
 	/**
 	 * RAZ sans toucher à deletePJs
 	 */
 	public void resetSpecial() {
 		super.reset();
 		nomenclature = null;
 		actionEnum = new ActionEnum();
 		addNomenclatures = new ArrayList<Nomenclature>();
 		objectToAdd = new Object[0];
 		allEtapes = new HashSet<PieceJustiVetPojo>();
 		allPJs = new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 		addPJs = new HashSet<NomenclaturePojo>();
 		deleteEtapes = new HashSet<PieceJustiVetPojo>();	
 		etapeTraitee = new PieceJustiVetPojo();
 		wayfEnum = new WayfEnum();
 	}
 	
 	/** 
 	 */
 	@Override
 	public void afterPropertiesSetInternal() {
 		super.afterPropertiesSetInternal();
 		Assert.notNull(this.etapeController, "property etapeController of class " 
 				+ this.getClass().getName() + " can not be null");
 		Assert.notNull(this.inscriptionAdm, "property inscriptionAdm of class " 
 				+ this.getClass().getName() + " can not be null");
 		
 	}
 	
 	
 	
 	/*
 	 ******************* CALLBACK ********************** */
 	/**
 	 * Callback to treatment type list.
 	 * @return String 
 	 */
 	public String goSeeAllTypTrt() {
 		reset();
 		return NavigationRulesConst.MANAGED_TYP_TRT;
 	}
 	
 	/**
 	 * Callback to decision type list.
 	 * @return String 
 	 */
 	public String goSeeAllTypDecision() {
 		reset();
 		return NavigationRulesConst.MANAGED_TYP_DECISION;
 	}
 	
 	/**
 	 * Callback to convocation type list.
 	 * @return String 
 	 */
 	public String goSeeAllTypConv() {
 		reset();
 		return NavigationRulesConst.MANAGED_TYP_CONV;
 	}
 	
 	/**
 	 * Callback to convocation type list.
 	 * @return String 
 	 */
 	public String goSeeAllMotivAvis() {
 		reset();
 		return NavigationRulesConst.MANAGED_MOTIV_AVIS;
 	}
 	
 	/**
 	 * Callback to convocation type list.
 	 * @return String 
 	 */
 	public String goSeeAllCampagnes() {
 		reset();
 		return NavigationRulesConst.MANAGED_CAMPAGNES;
 	}
 	
 	/**
 	 * Callback to PJ list.
 	 * @return String 
 	 */
 	public String goSeeAllPJ() {
 		reset();
 		return NavigationRulesConst.MANAGED_PJ;
 	}
 	
 	/**
 	 * Callback to PJ list to affect VET.
 	 * @return String 
 	 */
 	public String goSeeAllAffectPJ() {
 		reset();
 		return NavigationRulesConst.SEE_ALL_AFFECT_PJ;
 	}
 	
 	/**
 	 * Callback to searchPJForVet
 	 * @return  String
 	 */
 	public String goSeePJforVet(){
 		//reset();
 		return NavigationRulesConst.SEARCH_PJ_FOR_VET;
 	}
 	
 	/**
 	 * Callback to document list.
 	 * @return String 
 	 */
 	public String goSeeAllDocument() {
 		reset();
 		return NavigationRulesConst.SEE_ALL_DOCUMENT;
 	}
 	
 	
 	/**
 	 * Callback to treatment type list.
 	 * @return String 
 	 */
 	public String goAddTypDecision() {
 		addNomenclatures = new ArrayList<Nomenclature>();
 		addNomenclatures.add(new TypeDecision());
 		addNomenclatures.addAll(getTypeDecisions());
 		return NavigationRulesConst.ADD_TYP_DECISION;
 	}
 	
 	/**
 	 * Callback to motivation add.
 	 */
 	public void goAddMotivation() {
 		nomenclature = new MotivationAvis();
 		actionEnum.setWhatAction(ActionEnum.ADD_ACTION);
 	}
 	
 	/**
 	 * Callback to campagne add.
 	 */
 	public void goAddCampagne() {
 		nomenclature = new Campagne();
 		actionEnum.setWhatAction(ActionEnum.ADD_ACTION);
 	}
 	
 	
 	/**
 	 * Callback to treatment type list.
 	 * @return String 
 	 */
 	public String goAddPJ() {
 		nomenclature = new PieceJustificative();
 		// type of action : add
 		this.actionEnum.setWhatAction(this.actionEnum.getAddAction());
 		this.wayfEnum.setWhereAreYouFrom(WayfEnum.PJ_VALUE);
 		// for the return
 		this.wayfEnum.setWhereAreYouFrom(this.wayfEnum.getPJValue());
 		return NavigationRulesConst.ENTER_PJ;
 	}
 	
 	/**
 	 * Callback to treatment type list.
 	 * @return String 
 	 */
 	public String goAddOrChoicesPJ() {
 		return NavigationRulesConst.CHOICES_PJ;
 	}
 	
 	/**
 	 * Callback to treatment type list.
 	 * @return String 
 	 */
 	public String goUpdatePJ() {
 		// type of action : Update
 		this.actionEnum.setWhatAction(this.actionEnum.getUpdateAction());
 		this.wayfEnum.setWhereAreYouFrom(WayfEnum.PJ_VALUE);
 		// Charge VersionEtape
 		this.allEtapes.clear();
 		Set<VersionEtpOpi> listEtpByRight = Utilitaires.getListEtpByRight(getCurrentGest());
 		PieceJustificative laPJ = (PieceJustificative) nomenclature;
 		for (PieceJustiVet p : laPJ.getVersionEtapes()) {
 			PieceJustiVetPojo pjv = new PieceJustiVetPojo();
 			pjv.setVersionEtape(getDomainApoService().getVersionEtape(
 					p.getVersionEtpOpi().getCodEtp(), p.getVersionEtpOpi().getCodVrsVet()));
 			pjv.setPieceJustiVet(p);
 			if (getSessionController().isAllViewPJ()) {
 				pjv.setAllRight(true);
 			} else {
 				pjv.setAllRight(Utilitaires.isVetByRight(listEtpByRight,
 						p.getVersionEtpOpi(), getCurrentGest(), getDomainApoService()));
 			}
 			allEtapes.add(pjv);
 		}
 		// for the return
 		this.wayfEnum.setWhereAreYouFrom(this.wayfEnum.getPJValue());
 		return NavigationRulesConst.ENTER_PJ;
 	}
 	
 	/**
 	 * Callback to read PJ.
 	 * @return String 
 	 */
 	public String goSeeOnePJ() {
 		// type of action : Read
 		this.actionEnum.setWhatAction(this.actionEnum.getReadAction());
 		// Charge les Etapes
 		this.allEtapes.clear();
 		Set<VersionEtpOpi> listEtpByRight = Utilitaires.getListEtpByRight(getCurrentGest());
 		PieceJustificative laPJ = (PieceJustificative) nomenclature;
 		for (PieceJustiVet p : laPJ.getVersionEtapes()) {
 			PieceJustiVetPojo pjv = new PieceJustiVetPojo();
 			pjv.setVersionEtape(getDomainApoService().getVersionEtape(
 					p.getVersionEtpOpi().getCodEtp(), p.getVersionEtpOpi().getCodVrsVet()));
 			pjv.setPieceJustiVet(p);
 			if (getSessionController().isAllViewPJ()) {
 				pjv.setAllRight(true);
 			} else {
 				pjv.setAllRight(Utilitaires.isVetByRight(listEtpByRight,
 						p.getVersionEtpOpi(), getCurrentGest(), getDomainApoService()));
 			}
 			allEtapes.add(pjv);
 		}
 		// for the return
 		this.wayfEnum.setWhereAreYouFrom(this.wayfEnum.getPJValue());
 		return NavigationRulesConst.ENTER_PJ;
 	}
 	
 	/**
 	 * Callback to affect PJ to VET.
 	 * @return String 
 	 */
 	public String goSeeAffectPJ() {
 		// Charge les Etapes
 		this.allEtapes.clear();
 		PieceJustificative laPJ = (PieceJustificative) nomenclature;
 		Set<VersionEtpOpi> listEtpByRight = Utilitaires.getListEtpByRight(getCurrentGest());
 		for (PieceJustiVet p : laPJ.getVersionEtapes()) {
 			PieceJustiVetPojo pjv = new PieceJustiVetPojo();
 			pjv.setVersionEtape(getDomainApoService().getVersionEtape(
 					p.getVersionEtpOpi().getCodEtp(), p.getVersionEtpOpi().getCodVrsVet()));
 			pjv.setPieceJustiVet(p);
 			if (getSessionController().isAllViewPJ()) {
 				pjv.setAllRight(true);
 			} else {
 				pjv.setAllRight(Utilitaires.isVetByRight(listEtpByRight,
 						p.getVersionEtpOpi(), getCurrentGest(), getDomainApoService()));
 			}
 			allEtapes.add(pjv);
 		}
 		// for the return
 		this.wayfEnum.setWhereAreYouFrom(WayfEnum.AFFECT_PJ_VALUE);
 		return NavigationRulesConst.AFFECT_PJ;
 	}
 	
 	/**
 	 * Callback to search version etape.
 	 * @return String 
 	 */
 	public String goSearchEtpForPJ() {
 		etapeController.reset();
 		etapeController.setCodCge(getCurrentGest().getCodeCge());
 		// define from where we go to search Vet
 		etapeController.setWayfEnum(this.wayfEnum);
 		// on initialise la liste de campagne
 //		Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
 //		int codeRI = gest.getProfile().getCodeRI();
 		etapeController.getCampagnes().addAll(getParameterService().getCampagnes(null,
 		    String.valueOf(codeRI)));
 		etapeController.setCodAnu(getParameterService().getCampagneEnServ(codeRI).getCodAnu());
 		
 		if (!getSessionController().isAllViewPJ()
 				&& !StringUtils.hasText(getCurrentGest().getCodeCge())
 				&& getCurrentGest().getRightOnCmi() != null
 				&& !getCurrentGest().getRightOnCmi().isEmpty()) {
 			etapeController.setEtapes(new ArrayList<VersionEtapeDTO>(
 				Utilitaires.getListEtpDtoByRight(getCurrentGest(), getDomainApoService())));
 		}
 		return NavigationRulesConst.SEARCH_VET;
 	}	
 
 	
 	/**
 	 * Callback to search version etape.
 	 * @return String 
 	 */
 	public String goBackFromSearchPJ() {
 		reset();
 		return etapeController.goSearchVetForGestPJ();
 	}
 	
 	/*
 	 ******************* METHODS ********************** */
 	/**
 	 * Add a Nomenclature to the dataBase.
 	 * @return String
 	 */
 	public String add() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering add with nomenclature = " + nomenclature);
 		}
 		String target = null;
 
 		if (this.wayfEnum.getWhereAreYouFrom() != this.wayfEnum.getPJValue()
 				&& nomenclature == null)	{
 			//Get the first element of addNomenclatures
 			nomenclature = getAddNomenclatures().get(0);
 		}
 
 		if (ctrlEnter(nomenclature)) {
 			addInfoMessage(null, "INFO.ENTER.SUCCESS");
 			//comment the 21/04/2009
 //			if (nomenclature instanceof TypeConvocation) { 
 //				target = NavigationRulesConst.MANAGED_TYP_CONV;
 //			} else
 			if (nomenclature instanceof TypeDecision) {	
 				target = NavigationRulesConst.MANAGED_TYP_DECISION;
 			} else if (nomenclature instanceof PieceJustificative) {
 				target = NavigationRulesConst.MANAGED_PJ;
 				Set<PieceJustiVet> listP = new HashSet<PieceJustiVet>();
 				for (PieceJustiVetPojo p : allEtapes) {					
 					listP.add(p.getPieceJustiVet());
 				}
 				PieceJustificative piece = (PieceJustificative) nomenclature;
 				piece.setVersionEtapes(listP);
 				// it's not necessary here to use deleteEtapes (cause this is a add)
 			}
 			nomenclature = getDomainService().add(nomenclature, getCurrentGest().getLogin());
 			getParameterService().addNomenclature(nomenclature);
 			reset();
 		}
 		if (log.isDebugEnabled()) {
 			log.debug("leaving add");
 		}
 		return target;
 	}
 	
 	/**
 	 * Update a Domain to the dataBase.
 	 * @return String
 	 */
 	public String update() {
 		if (log.isDebugEnabled()) {
 			log.debug("enterind update with nomenclature = " + nomenclature);
 		}
 		String jsfRetour = null;
 		for (NomenclaturePojo camp : getCampagnes()) {
 			if (nomenclature.getId().equals(camp.getNomenclature().getId())) {
 				((Campagne) nomenclature).setCodAnu(
 						((Campagne) camp.getNomenclature()).getCodAnu());
 			}
 		}
 		if (ctrlEnter(nomenclature)) {
 			if (nomenclature instanceof PieceJustificative) {
 				if (wayfEnum.getWhereAreYouFrom().equals(WayfEnum.PJ_VALUE)) {
 					jsfRetour = NavigationRulesConst.MANAGED_PJ;
 				} else if (wayfEnum.getWhereAreYouFrom().equals(WayfEnum.AFFECT_PJ_VALUE)) {
 					jsfRetour = NavigationRulesConst.SEE_ALL_AFFECT_PJ;
 				}
 				
 				Set<PieceJustiVet> listP = new HashSet<PieceJustiVet>();
 				for (PieceJustiVetPojo p : allEtapes) {
 					listP.add(p.getPieceJustiVet());
 				}
 				PieceJustificative piece = (PieceJustificative) nomenclature;
 				piece.setVersionEtapes(listP);
 			}
 			nomenclature = getDomainService().update(
 					nomenclature, getCurrentGest().getLogin());
 			getParameterService().updateNomenclature(nomenclature);
 			// delete the etapes deleted by the user
 			for (PieceJustiVetPojo p : deleteEtapes) {
 				if (p.getPieceJustiVet().getId() != 0) {
 					getParameterService().deletePieceJustiVet(p.getPieceJustiVet());
 				}
 			}
 			reset();
 			addInfoMessage(null, "INFO.ENTER.SUCCESS");
 		}
 
 		if (log.isDebugEnabled()) {
 			log.debug("leaving update");
 		}
 		return jsfRetour;
 	}
 	
 	
 	/**
 	 * Delete a Nomenclature to the dataBase.
 	 */
 	public void delete() {
 		if (log.isDebugEnabled()) {
 			log.debug("enterind delete with nomenclature = " + nomenclature);
 		}
 		if (ctrlDelete(nomenclature)) {
 			if (nomenclature instanceof PieceJustificative) {
 				// Delete all etapes attached
 				for (PieceJustiVetPojo p : allEtapes) {
 					getParameterService().deletePieceJustiVet(p.getPieceJustiVet());
 				}
 				getDomainService().deleteMissingPiece(null, (PieceJustificative) nomenclature);
 			}
 			
 			// delete nomenclature
 			getParameterService().deleteNomenclature(nomenclature);	
 			addInfoMessage(null, "INFO.DELETE.SUCCESS");
 		} else {
 			addErrorMessage(null, "ERROR.NOM.CAN_NOT.DELETE");
 		}
 		reset();
 		if (log.isDebugEnabled()) {
 			log.debug("leaving delete");
 		}
 	}
 	
 	/**
 	 * Add the members in objectToAdd to allEtapes (to display).
 	 * @return String
 	 */
 	public String addEtapes() {
 		Set<VersionEtpOpi> listEtpByRight = Utilitaires.getListEtpByRight(getCurrentGest());
 		if (objectToAdd.length > 0) {
 			for (Object o : objectToAdd) {
 				VersionEtapeDTO v = (VersionEtapeDTO) o;
 				PieceJustiVetPojo a = new PieceJustiVetPojo(v);
 				if (getSessionController().isAllViewPJ()) {
 					a.setAllRight(true);
 				} else {
 					a.setAllRight(Utilitaires.isVetByRight(listEtpByRight, 
 							new VersionEtpOpi(v), getCurrentGest(), getDomainApoService()));
 				}
 				this.allEtapes.add(a);
 				// remove to the list of etapes to by deleted (if exist)
 				this.deleteEtapes.remove(a);
 				
 			}
 		} else if (etapeController.getAllChecked()) {
 			//on ajout toutes les etapes
 			for (VersionEtapeDTO v : etapeController.getEtapes()) {
 				PieceJustiVetPojo a = new PieceJustiVetPojo(v);
 				if (getSessionController().isAllViewPJ()) {
 					a.setAllRight(true);
 				} else {
 					a.setAllRight(Utilitaires.isVetByRight(listEtpByRight,
 							new VersionEtpOpi(v), getCurrentGest(), getDomainApoService()));
 				}
 				this.allEtapes.add(a);
 				// remove to the list of etapes to by deleted (if exist)
 				this.deleteEtapes.remove(a);
 				
 			}
 		}
 		objectToAdd = new Object[0];
 		this.etapeController.reset();
 		String callback = null;
 		if (wayfEnum.getWhereAreYouFrom().equals(WayfEnum.AFFECT_PJ_VALUE)) {
 			callback = NavigationRulesConst.AFFECT_PJ;
 		} else if (wayfEnum.getWhereAreYouFrom().equals(WayfEnum.PJ_VALUE)) {
 			callback = NavigationRulesConst.ENTER_PJ;
 		}
 		return callback;
 	}
 	
 	/**
 	 * Add the members in objectToAdd to allPJs (to display).
 	 * @return String
 	 */
 	public String addPJs() {
 		allPJs= new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 //		Set<VersionEtpOpi> listEtpByRight = Utilitaires.getListEtpByRight(getCurrentGest());
 		if (getObjectToAdd().length > 0) {
 			for (Object o : objectToAdd) {
 				NomenclaturePojo v = (NomenclaturePojo) o;
 				
 //				if (!isInSet(v,allPJs)){
 					if(!isInSet(v,deletePJs)){
 						this.addPJs.add(v);
 						this.allPJs.add(v);
 					}else{
 					// remove from PJs to delete
 					deletePJs = removePJfromSet(v, deletePJs);
 					}
 //				}
 				
 			}
 		}
 		this.etapeController.reset();
 		getPiecesJToNomenclaturePojo();
 		objectToAdd = new Object[0];
 		
 		return NavigationRulesConst.ENTER_VET;
 		
 	}
 
 	/**
 	 * @param np
 	 * @param set
 	 * @return true si la PieceJustificative est déjà dans le set en paramètre
 	 */
 	public boolean isInSet(NomenclaturePojo np, Set<NomenclaturePojo> set){
 		PieceJustificative pj = (PieceJustificative)np.getNomenclature();
 		
 		Iterator<NomenclaturePojo> it = set.iterator();
 		
 		while(it.hasNext()){
 			NomenclaturePojo piecePojo = it.next();
 			PieceJustificative pjust = (PieceJustificative)piecePojo.getNomenclature();
 			if (pjust.getCode().equalsIgnoreCase(pj.getCode()))
 				return true;
 		}
 		
 		return false;
 		
 	}
 	
 	
 	/**
 	 * @param np
 	 * @param set
 	 * @return the set without the nomenclature Pojo in Parameter
 	 */
 	public Set<NomenclaturePojo> removePJfromSet(NomenclaturePojo np, Set<NomenclaturePojo> set){
 		
 		Set<NomenclaturePojo> setTemp = new HashSet<NomenclaturePojo>();
 		setTemp.addAll(set);
 				
 		Iterator<NomenclaturePojo> it = setTemp.iterator();
 		
 		while(it.hasNext()){
 			NomenclaturePojo piecePojo = it.next();
 			
 			if (np.getNomenclature().getCode().equalsIgnoreCase(piecePojo.getNomenclature().getCode()))
 				set.remove(piecePojo);
 		}
 		
 		return set;
 		
 	}
 
 
 	/**
 	 * Remove Etape in allEtapes.
 	 */
 	public void removeTrtEtape() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering removeTrtEtape etapeTraitee" + etapeTraitee);
 		}
 		// ajoute dans la liste des etapes e supprimer
 		this.deleteEtapes.add(this.etapeTraitee);
 		// enleve de l'objet pj
 		this.allEtapes.remove(this.etapeTraitee);
 	}
 	
 	
 	/**
 	 * Remove PJ in allPJs.
 	 */
 	public void removeTrtPJ() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering removeTrtPJ PJTraitee" + PJTraitee);
 		}
 		// ajoute dans la liste des etapes e supprimer
 		this.deletePJs.add(this.PJTraitee);
 		// enleve de l'objet pj
 		this.allPJs.remove(this.PJTraitee);
 		this.addPJs.remove(this.PJTraitee);
 	}
 	
 	/**
 	 * Sauvegarde les modifications en ajout ou en suppression des PJs (pièces justificatives)
 	 * 
 	 */
 	public void updatePJs(){
 
 		
 	Set<PieceJustiVet> listP = new HashSet<PieceJustiVet>();
 	PieceJustiVetPojo pjvp = new PieceJustiVetPojo(vetDTO);	
 	listP.add(pjvp.getPieceJustiVet());
 	
 	Iterator<NomenclaturePojo> itAdd = addPJs.iterator();
 	
 	//on sauvegarde les PJs ajoutées
 	while(itAdd.hasNext()){
 		NomenclaturePojo piecePojo = itAdd.next();
 		PieceJustificative pj = (PieceJustificative)piecePojo.getNomenclature();
 		pj.setVersionEtapes(listP);
 		getParameterService().updateNomenclature(pj);
 	}
 	
 	
 	//on supprime les PJs à supprimer
 	Iterator<NomenclaturePojo> itDel = deletePJs.iterator();
 	
 		while(itDel.hasNext()){
 			NomenclaturePojo piecePojo = itDel.next();
 
 			PieceJustificative pj = (PieceJustificative)piecePojo.getNomenclature();
 			Set<PieceJustiVet> setPiece = pj.getVersionEtapes();
 
 			Iterator<PieceJustiVet> itVet = setPiece.iterator();
 		
 		
 			//on cherche le bon code etp pour supprimer la PieceJutsiVet correpondant à la pièce justificative et 
 			// à la vet
 			while(itVet.hasNext()){
 				PieceJustiVet pjv = itVet.next();	
 				VersionEtpOpi ved = pjv.getVersionEtpOpi();
 			
 				if (vetDTO.getCodEtp().equalsIgnoreCase(ved.getCodEtp()))
 					getParameterService().deletePieceJustiVetWithFlush(pjv);
 			}
 
 			//allPJs = getPiecesJToNomenclaturePojo();
 			
 	
 		}
 		allPJs = new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 	
 		Set<NomenclaturePojo> affichAllPJs = getPiecesJToNomenclaturePojo();
 		affichAllPJs.removeAll(deletePJs);
 		deletePJs = new HashSet<NomenclaturePojo>();
 //		List<NomenclaturePojo> list = new ArrayList<NomenclaturePojo>(affichAllPJs);
 //		Collections.sort(list, new ComparatorString(NomenclaturePojo.class));
 //		allPJs.addAll(list);
 		allPJs.addAll(affichAllPJs);
 		addInfoMessage(null, "INFO.ENTER.SUCCESS");
 	}
 	
 	
 	
 	/* ### ALL CONTROL ####*/
 	
 	/**
 	 * Control Nomenclature attributes for the adding and updating.
 	 * @param nom
 	 * @return Boolean
 	 */
 	private Boolean ctrlEnter(final Nomenclature nom) {
 		Boolean ctrlOk = true;
 		if (!StringUtils.hasText(nom.getCode())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.CODE"));
 			ctrlOk = false;
 		} else {
 			if (!getParameterService().nomenclatureCodeIsUnique(nom)) {
 				ctrlOk = false;
 				addErrorMessage(null, "ERROR.FIELD.NOT_UNIQUE", getString("FIELD_LABEL.CODE"));
 			}
 		}
 		if (!(nom instanceof Campagne) && !StringUtils.hasText(nom.getLibelle())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.LIBELLE"));
 			ctrlOk = false;
 		}
 		if (!(nom instanceof PieceJustificative) && !(nom instanceof Campagne) 
 				&& !StringUtils.hasText(nom.getShortLabel())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.SHORT_LIB"));
 			ctrlOk = false;
 		}
 		if (nom instanceof TypeDecision) {
 			TypeDecision t = (TypeDecision) nom;
 			if (!StringUtils.hasText(t.getCodeTypeConvocation())) {
 				addErrorMessage(null, Constantes.I18N_EMPTY, 
 						getString("TYP_DECISION.CONVOCATION_TYPE"));
 				ctrlOk = false;
 			} else {
 				//on controle qu'il n'y a qu'une inscript Adm
 				if (t.getCodeTypeConvocation().equals(inscriptionAdm.getCode())) {
 					Set<TypeDecision> list = getParameterService().getTypeDecisions(null);
 					for (TypeDecision tlist : list) {
 						if (StringUtils.hasText(tlist.getCodeTypeConvocation())) {
 							if (!tlist.equals(t)
 									&& tlist.getCodeTypeConvocation()
 									.equals(t.getCodeTypeConvocation())) {
 								addErrorMessage(null, 
 										"ERROR.TYP_DEC.JUST_ONE_INS_ADM",
 										inscriptionAdm.getLabel());
 								ctrlOk = false;
 								break;
 							}
 						}
 					}
 				}
 				
 				
 			}
 		}
 		
 		//Ajout try par rapport au bug 26 pour exception non-catchée
 		try{
 			if (nom instanceof Campagne) {
 				Campagne c = (Campagne) nom;
 				// on controle si les dates sont saisis correctement
 				// les dates ne doivent pas être nulles
 				if (c.getDateDebCamp() == null) {
 					addErrorMessage(null, "ERROR.CAMP.DATE_DEB_EMPTY");
 					ctrlOk = false;
 				}
 				if (c.getDateFinCamp() == null) {
 					addErrorMessage(null, "ERROR.CAMP.DATE_FIN_EMPTY");
 					ctrlOk = false;
 				}
 				// la date de fin de campagne doit être supérieure à celle de début
 				if (c.getDateDebCamp() != null && c.getDateFinCamp() != null
 						&& !c.getDateFinCamp().after(c.getDateDebCamp())) {
 					addErrorMessage(null, "ERROR.CAMP.DATE_FIN_SUP_DEB");
 					ctrlOk = false;
 				}
 				
 				
 				// on controle que le codeAnu saisi est correct
 				if (getDomainApoService().getAnneeUni(c.getCodAnu()) == null) {
 					addErrorMessage(null, "ERROR.CAMP.COD_ANU_APOGEE");
 					ctrlOk = false;
 				}
 				
 				
 				Set<Campagne> camps = getParameterService().getCampagnes(null, null);
 				RegimeInscription rI = getRegimeIns().get(c.getCodeRI());
 				for (Campagne camp : camps) {
 					// on ne peut pas avoir 2 campagnes avec le même code
 					// sauf si l'une est en FC et l'autre non
 					if (!camp.equals(c) && camp.getCode().equals(c.getCode())
 							&& camp.getCodeRI() == c.getCodeRI()) {
 						if (camp.getCodeRI() != FormationContinue.CODE) {
 							addErrorMessage(null, "ERROR.CAMP.JUST_ONE_YEAR",
 									camp.getCodAnu());
 						} else if (camp.getCodeRI() == FormationContinue.CODE) {
 							addErrorMessage(null, "ERROR.CAMP.JUST_ONE_YEAR_SFC",
 									camp.getCodAnu());
 						}
 						ctrlOk = false;
 						break;
 					}
 				}
 				for (Campagne camp : camps) {
 					// on ne peut pas avoir 2 campagnes avec le même codAnu
 					// sauf si l'une est en FC et l'autre non
 					if (!camp.equals(c) && camp.getCodAnu().equals(c.getCodAnu())
 							&& camp.getCodeRI() == c.getCodeRI()) {
 						if (camp.getCodeRI() != FormationContinue.CODE) {
 							addErrorMessage(null, "ERROR.CAMP.JUST_ONE_ANU",
 									camp.getCode());
 						} else if (camp.getCodeRI() == FormationContinue.CODE) {
 							addErrorMessage(null, "ERROR.CAMP.JUST_ONE_ANU_SFC",
 									camp.getCode());
 						}
 						ctrlOk = false;
 						break;
 					}
 				}
 				for (Campagne camp : camps) {
 					// on ne peut pas avoir 2 campagnes FI en service
 					if (!camp.equals(c) && !(rI instanceof FormationInitiale)
 							&& camp.getTemoinEnService().equals(c.getTemoinEnService())
 							&& camp.getCodeRI() == c.getCodeRI()) {
 						addErrorMessage(null, "ERROR.CAMP.JUST_ONE_EN_SERV");
 						ctrlOk = false;
 						break;
 					}
 				}
 			}
 		
 		}catch(Exception e){
 			//Si le contrôle du code anu renvoi une erreur du type SOAPFaultException c'est que la campagne n'existe pas dans Apogee
 			if (e instanceof javax.xml.ws.soap.SOAPFaultException) {
 				addErrorMessage(null, "ERROR.CAMP.COD_ANU_APOGEE");
 			}
 			
 			ctrlOk=false;
 		}
 
 		if (log.isDebugEnabled()) {
 			log.debug("leaving ctrlAdd return = " + ctrlOk);
 		}
 		return ctrlOk;
 	}
 
 	/**
 	 * Contrôle si la suppression est possible.
 	 * @param nom
 	 * @return boolean true if can delete 
 	 */
 	private boolean ctrlDelete(final Nomenclature nom) {
 		return getParameterService().canDeleteNomclature(nom);
 	}
 	
 	
 	/**
 	 * List of specific type for convocation type.
 	 * @return List of SelectItem
 	 */
 	public List<SelectItem> getTypeConvocationsItems() {
 		List<SelectItem> s = new ArrayList<SelectItem>();
 		s.add(new SelectItem("", ""));
 		for (TypeConvocation t : getTypeConvocations()) {
 			s.add(new SelectItem(t.getCode(), t.getLabel()));
 		}
 		
 		Collections.sort(s, new ComparatorSelectItem());
 		return s;
 		
 	}
 	
 	/**
 	 * List of specific type for decision type.
 	 * @return List of SelectItem
 	 */
 	public List<SelectItem> getTypeDecisionItems() {
 		return typesDecItems;
 	}
 	
 	/**
 	 * List of specific type for decision type.
 	 * @return List of SelectItem
 	 */
 	public List<SelectItem> getTypeDecisionInUseItems() {
 		return typesDecInUseItems;
 	}
 	
 	/**
 	 * List of specific type for regimeInscriptions type.
 	 * @return List of SelectItem
 	 */
 	public List<SelectItem> getRegimeInscriptionsItems() {
 		List<SelectItem> s = new ArrayList<SelectItem>();
 		Map<Integer, RegimeInscription> mapRI = getRegimeIns();
 		for (Map.Entry<Integer, RegimeInscription> entryRI : mapRI.entrySet()) {
 			s.add(new SelectItem(entryRI.getKey(), entryRI.getValue().getLabel()));
 		}
 		
 		Collections.sort(s, new ComparatorSelectItem());
 		return s;
 		
 	}
 
 	
 	/**
 	 * List of specific type for regimeInscriptions type.
 	 * @return List of SelectItem
 	 */
 	public List<SelectItem> getRegimeInscriptionsItemsConv() {
 		List<SelectItem> s = new ArrayList<SelectItem>();
 		Map<Integer, RegimeInscription> mapRI = getRegimeIns();
 		for (Map.Entry<Integer, RegimeInscription> entryRI : mapRI.entrySet()) {
 			s.add(new SelectItem(entryRI.getValue(), entryRI.getValue().getLabel()));
 		}
 		
 		Collections.sort(s, new ComparatorSelectItem());
 		return s;
 		
 	}
 	
 	/**
 	 * @return la liste des RegimeInscription
 	 */
 	public List<RegimeInscription> getAllRegimeInscription() {
 		List<RegimeInscription> listeRI = new ArrayList<RegimeInscription>();
 		for (Map.Entry<Integer, RegimeInscription> ri : getRegimeIns().entrySet()) {
 			listeRI.add(ri.getValue());
 		}
 		return listeRI;
 	}
 	
 	/**
 	 * @return la première valeur de la liste des régimes d'inscriptions comme valeur par défaut proposée
 	 * sinon 0
 	 */
 	public int getRIByDefault(){
 		List<RegimeInscription> allRI = getAllRegimeInscription();
 		if(!allRI.isEmpty()){
 			RegimeInscription ri = allRI.get(0);
 		
 			if (ri!=null)
 				return ri.getCode();
 		}
 		return 0;
 	}
 	
 	/**
 	 * @return boolean
 	 */
 	public boolean isRightPjForAllVet() {
 		if (getSessionController().isAllViewPJ()) {
 			return true;
 		} else if (StringUtils.hasText(getCurrentGest().getCodeCge())
 					|| (getCurrentGest().getRightOnCmi() != null
 							&& !getCurrentGest().getRightOnCmi().isEmpty())) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	
 	/**
 	 * @return the regime inscription label from the codeRI given by etapeController
 	 */
 	public String getRegimeInscription(){
 		Map<Integer, RegimeInscription> mapRI = getRegimeIns();
 		for (Map.Entry<Integer, RegimeInscription> entryRI : mapRI.entrySet()) {
 			if (entryRI.getValue().getCode()==codeRI)
 				return entryRI.getValue().getLabel();
 		}
 		
 		return null;
 	}
 	
 	
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 	
 	public Boolean getIsFinal() {
 		return isFinal;
 	}
 
 	public void setIsFinal(final Boolean isFinal) {
 		this.isFinal = isFinal;
 	}
 
 	/**
 	 * Return all PieceJustificative.
 	 * @return Set< NomenclaturePojo>
 	 */
 	public Set<NomenclaturePojo> getAllPieceJustificatives() {
 		Set<NomenclaturePojo> nom = new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 		
 		for (PieceJustificative m : getParameterService().getPJs(null)) {
 			nom.add(new NomenclaturePojo(m, getRegimeIns().get(m.getCodeRI())));
 		}
 		
 		return nom;
 	}
 	
 	/**
 	 * Return all PieceJustificative.
 	 * @return Set< NomenclaturePojo>
 	 */
 	public List<NomenclaturePojo> getPieceJustificatives() {
 		Set<NomenclaturePojo> nom = new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 		Set<VersionEtpOpi> listEtpByRight = Utilitaires.getListEtpByRight(getCurrentGest());
 		
 		for (PieceJustificative m : getParameterService().getPJs(null)) {
 			if (getSessionController().isAllViewPJ()) {
 				nom.add(new NomenclaturePojo(m, getRegimeIns().get(m.getCodeRI()), true));
 			} else if (StringUtils.hasText(getCurrentGest().getCodeCge())) {
 				boolean allRight = true;
 				boolean isCreatePj = false;
 				for (PieceJustiVet pjVet : m.getVersionEtapes()) {
 					if (Utilitaires.isEtpInCge(pjVet.getVersionEtpOpi().getCodEtp(),
 							getCurrentGest().getCodeCge(),
 							getDomainApoService())) {
 						isCreatePj = true;
 					} else {
 						allRight = false;
 					}
 				}
 				if (isCreatePj) {
 					nom.add(new NomenclaturePojo(m, getRegimeIns().get(m.getCodeRI()), allRight));
 				}
 			} else if (getCurrentGest().getRightOnCmi() != null
 					&& !getCurrentGest().getRightOnCmi().isEmpty()) {
 				boolean allRight = true;
 				boolean isCreatePj = false;
 				for (PieceJustiVet pjVet : m.getVersionEtapes()) {
 					if (listEtpByRight.contains(pjVet.getVersionEtpOpi())) {
 						isCreatePj = true;
 					} else {
 						allRight = false;
 					}
 				}
 				if (isCreatePj) {
 					nom.add(new NomenclaturePojo(m, getRegimeIns().get(m.getCodeRI()), allRight));
 				}
 			} else {
 				nom.add(new NomenclaturePojo(m, getRegimeIns().get(m.getCodeRI()), true));
 			}
 		}
 		
 		return new ArrayList<NomenclaturePojo>(nom);
 	}
 	
 	
 	/**
 	 * @return the piece justificatives selected
 	 */
 	public List<NomenclaturePojo> getPieceJustificativesSelected() {
 		List<NomenclaturePojo> nom = getPieceJustificatives();
 		
 		for (NomenclaturePojo np : nom) {
 			if (!np.getNomenclature().getCode().equals(getCode().getValue()))
 				nom.remove(np);
 		}
 		
 		return nom;
 	}
 	
 	/**
 	 * Return all NomenclaturePojo in use.
 	 * @return List< NomenclaturePojo>
 	 */
 	public List<NomenclaturePojo> getPieceJustificativesItems() {
 		List<NomenclaturePojo> pj = new ArrayList<NomenclaturePojo>(getPieceJustificatives());	
 		return pj;
 	}
 	
 	/**
 	 * @return pieces justificative d'une vet sous forme de NomenclaturePojo
 	 */
 	
 	public Set<NomenclaturePojo> getPiecesJToNomenclaturePojo() {
 		Set<NomenclaturePojo> nom = new TreeSet<NomenclaturePojo>(
 				new ComparatorString(NomenclaturePojo.class));
 
 		List<PieceJustificative> pjs = getParameterService().getPiecesJ(
 				new VersionEtpOpi(vetDTO), null);
 		List<PieceJustificative> pjsTemp = new ArrayList<PieceJustificative>();
 		pjsTemp.addAll(pjs);
 
 		Iterator<PieceJustificative> it1 = pjsTemp.iterator();
 
 		while (it1.hasNext()) {
 			PieceJustificative pj = it1.next();
 
 			Iterator<NomenclaturePojo> it2 = deletePJs.iterator();
 
 			while (it2.hasNext()) {
 				NomenclaturePojo np = it2.next();
 				String codeTemp = np.getNomenclature().getCode();
 				if (codeTemp.equalsIgnoreCase(pj.getCode())) {
 					pjs.remove(pj);
 				}
 			}
 		}
 
 		for (PieceJustificative m : pjs) {
 			NomenclaturePojo np = new NomenclaturePojo(m, getRegimeIns().get(
 					m.getCodeRI()));
 
 			if (!isInSet(np, allPJs))
 				nom.add(np);
 		}
 
 		allPJs.addAll(nom);
 
 		return allPJs;
 	}
 	
 	/**
 	 * Return all TypeTraitement.
 	 * @return Set< TypeTraitement>
 	 */
 	public List<TypeTraitement> getTypeTrts() {
 		return getParameterService().getTypeTraitements();
 	}
 	
 	/**
 	 * Return all MotivationAvis.
 	 * @return Set< NomenclaturePojo>
 	 */
 	public Set<NomenclaturePojo> getMotivationsAvis() {
 		Set<MotivationAvis> mo = getParameterService().getMotivationsAvis(true);
 		Set<NomenclaturePojo> nom = new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 		for (MotivationAvis m : mo) {
 			nom.add(new NomenclaturePojo(m));
 		}
 		return nom;
 	}
 	
 	/**
 	 * Return all MotivationAvis.
 	 * @return Set< NomenclaturePojo>
 	 */
 	public Set<NomenclaturePojo> getAllMotivationsAvis() {
 		Set<MotivationAvis> mo = getParameterService().getMotivationsAvis(null);
 		Set<NomenclaturePojo> nom = new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 		for (MotivationAvis m : mo) {
 			nom.add(new NomenclaturePojo(m));
 		}
 		return nom;
 	}
 	
 	/**
 	 * Return all MotivationAvis in use.
 	 * @return List< NomenclaturePojo>
 	 */
 	public List<NomenclaturePojo> getAllMotivationsAvisItems() {
 		List<NomenclaturePojo> np = new ArrayList<NomenclaturePojo>(getAllMotivationsAvis());	
 		return np;
 	}
 	
 	/**
 	 * Return all Campagne.
 	 * @return Set< Campagne>
 	 */
 	public Set<NomenclaturePojo> getCampagnes() {
 		Set<Campagne> ca = getParameterService().getCampagnes(null, null);
 		Set<NomenclaturePojo> nom = new TreeSet<NomenclaturePojo>(new ComparatorString(NomenclaturePojo.class));
 		for (Campagne c : ca) {
 			nom.add(new NomenclaturePojo(c, getRegimeIns().get(c.getCodeRI())));
 		}
 		return nom;
 	}
 	
 	/**
 	 * Return all Campagne in use.
 	 * @return List< NomenclaturePojo>
 	 */
 	public List<NomenclaturePojo> getCampagnesInUse() {
 		List<NomenclaturePojo> pj = new ArrayList<NomenclaturePojo>(getCampagnes());	
 		return pj;
 	}
 	
 	/**
 	 * Return all Campagne in use.
 	 * @return Set< Campagne>
 	 */
 	public List<Campagne> getCampagnesItems() {
 		List<Campagne> l = new ArrayList<Campagne>(getParameterService().getCampagnes(true, null));
 		Collections.sort(l, new ComparatorString(Campagne.class));
 		
 		return l;
 	}
 	
 	 /**
 	 * Add a file to the justificative piece
 	 */
 	public void ajouterFichierPJ() {        
 	        // Prepare file and outputstream.
 	    File file = null;
 	    OutputStream output = null;
 	        
 	    try {
 	        	
 	       if (FilenameUtils.getExtension(uploadedFile.getFileName()).equalsIgnoreCase("txt") || FilenameUtils.getExtension(uploadedFile.getFileName()).equalsIgnoreCase("pdf")
 	        	|| FilenameUtils.getExtension(uploadedFile.getFileName()).equalsIgnoreCase("doc") || FilenameUtils.getExtension(uploadedFile.getFileName()).equalsIgnoreCase("odt")
 	        	|| FilenameUtils.getExtension(uploadedFile.getFileName()).equalsIgnoreCase("xls")){
 	    	   // Create file with unique name in upload folder and write to it.
 	    	   file = new File(uploadPath,uploadedFile.getFileName());
 	    	   output = new FileOutputStream(file);
 	    	   IOUtils.copy(uploadedFile.getInputstream(), output);
 	    	   fileName = file.getName();
 
 	    	   // Show succes message.
 	    	   addInfoMessage(null,"PJ.FILE_IS_ADDED");
 	            
 	    	   PieceJustificative pj = (PieceJustificative) nomenclature;
 	    	   pj.setNomDocument(fileName);
 	    	   getParameterService().updateNomenclature(pj);
 	        }else 
 	        		throw new IOException();
 	 
 	        } catch (IOException e) {
 	            // Cleanup.
 	           if (file != null) file.delete();
 
 	            // Show error message.
 	           addErrorMessage(null, "PJ.FILE_ADDED_FAILED");
 
 	            // Always log stacktraces (with a real logger).
 	           e.printStackTrace();
 	        } finally {
 	           IOUtils.closeQuietly(output);
 	        }
 	}
 	
 	
 	/**
 	 * Remove the file from the justification piece 
 	 */
 	public void removeFile(){
 		PieceJustificative pj = (PieceJustificative)nomenclature;
 		pj.setNomDocument(null);
 		getParameterService().updateNomenclature(pj);
 		addInfoMessage(null, "PJ.REMOVED_FILE");
 	}
 	
 
 	
 	/**
 	 * Return all Typeconvocation.
 	 * @return List< TypeConvocation>
 	 */
 	public List<TypeConvocation> getTypeConvocations() {
 		return getParameterService().getTypeConvocations();
 	}
 	
 	/**
 	 * Return all Typedecision not sorted.
 	 * @return Set< TypeDecision>
 	 */
 	public List<TypeDecision> getTypeDecisions() {
 		return typesDec;
 	}
 	
 	/**
 	 * Return all Typedecision sorted.
 	 * @return List< TypeDecision>
 	 */
 	public List<TypeDecision> getTypeDecisionsSorted() {
 		typesDec = new ArrayList<TypeDecision>(
 				getParameterService().getTypeDecisions(null));
 		sortedTypesDec = new ArrayList<TypeDecision>(typesDec);
 		Collections.sort(sortedTypesDec,
 				new ComparatorString(TypeDecision.class));
 		return sortedTypesDec;
 	}
 	
 	/**
 	 * Return all Typedecision in use.
 	 * @return Set< TypeDecision>
 	 */
 	public List<TypeDecision> getTypeDecisionsInUse() {
 		return typesDecInUse;
 	}
 
 	public List<TypeDecision> getFinalTypesDecisions() {
 		return new ArrayList<TypeDecision>(
 				iterableStream(sortedTypesDec).filter(
 						new F<TypeDecision, Boolean>() {
 							public Boolean f(TypeDecision t) {
 								return getIsFinal() == t.getIsFinal();
 							}
 						}).toCollection());
 	}
 
 	/**
 	 * @return the nomenclature
 	 */
 	public Nomenclature getNomenclature() {
 		return nomenclature;
 	}
 
 	/**
 	 * @param nomenclature the nomenclature to set
 	 */
 	public void setNomenclature(final Nomenclature nomenclature) {
 		//Clone est utilise afin que l'utilisateur puisse modifier l'objet sans toucher au CACHE (par r?f?rence)
 		//Probleme rencontre lors du modification annulee(par exemple), le cache etait tout de meme modifier
 		if (nomenclature instanceof TypeDecision) {
 			
 			TypeDecision t = (TypeDecision) nomenclature;
 			this.nomenclature = t.clone();
 		} else if (nomenclature instanceof PieceJustificative) {
 			PieceJustificative p = (PieceJustificative) nomenclature;
 			this.nomenclature = p.clone();
 		} else if (nomenclature instanceof MotivationAvis) {
 			MotivationAvis m = (MotivationAvis) nomenclature;
 			this.nomenclature = m.clone();
 		} else if (nomenclature instanceof Campagne) {
 			Campagne m = (Campagne) nomenclature;
 			this.nomenclature = m.clone();
 		}
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
 	 * @param addNomenclatures the addNomenclatures to set
 	 */
 	public void setAddNomenclatures(final List<Nomenclature> addNomenclatures) {
 		this.addNomenclatures = addNomenclatures;
 	}
 
 	/**
 	 * @return the addNomenclatures
 	 */
 	public List<Nomenclature> getAddNomenclatures() {
 		return addNomenclatures;
 	}
 
 	/**
 	 * @return the wayfEnum
 	 */
 	public WayfEnum getWayfEnum() {
 		return wayfEnum;
 	}
 
 	/**
 	 * @param wayfEnum the wayfEnum to set
 	 */
 	public void setWayfEnum(final WayfEnum wayfEnum) {
 		this.wayfEnum = wayfEnum;
 	}
 
 	/**
 	 * @return the allEtapes
 	 */
 	public Set<PieceJustiVetPojo> getAllEtapes() {
 		return allEtapes;
 	}
 
 	/**
 	 * Return all PieceJustiVetPojo in use.
 	 * @return List< PieceJustiVetPojo>
 	 */
 	public List<PieceJustiVetPojo> getAllEtapesItems() {
 		List<PieceJustiVetPojo> pj = new ArrayList<PieceJustiVetPojo>(getAllEtapes());	
 		return pj;
 	}
 	
 	/**
 	 * @param allEtapes the allEtapes to set
 	 */
 	public void setAllEtapes(final Set<PieceJustiVetPojo> allEtapes) {
 		this.allEtapes = allEtapes;
 	}
 
 	/**
 	 * @return the etapeTraitee
 	 */
 	public PieceJustiVetPojo getEtapeTraitee() {
 		return etapeTraitee;
 	}
 
 	/**
 	 * @param etapeTraitee the etapeTraitee to set
 	 */
 	public void setEtapeTraitee(final PieceJustiVetPojo etapeTraitee) {
 		this.etapeTraitee = etapeTraitee;
 	}
 
 	/**
 	 * @return the objectToAdd
 	 */
 	public Object[] getObjectToAdd() {
 		return objectToAdd;
 	}
 
 	/**
 	 * @param objectToAdd the objectToAdd to set
 	 */
 	public void setObjectToAdd(final Object[] objectToAdd) {
 		this.objectToAdd = objectToAdd;
 	}
 
 	/**
 	 * @param etapeController the etapeController to set
 	 */
 	public void setEtapeController(final EtapeController etapeController) {
 		this.etapeController = etapeController;
 	}
 
 	/**
 	 * @return the deleteEtapes
 	 */
 	public Set<PieceJustiVetPojo> getDeleteEtapes() {
 		return deleteEtapes;
 	}
 
 	/**
 	 * @param deleteEtapes the deleteEtapes to set
 	 */
 	public void setDeleteEtapes(final Set<PieceJustiVetPojo> deleteEtapes) {
 		this.deleteEtapes = deleteEtapes;
 	}
 
 	/**
 	 * @param inscriptionAdm the inscriptionAdm to set
 	 */
 	public void setInscriptionAdm(final InscriptionAdm inscriptionAdm) {
 		this.inscriptionAdm = inscriptionAdm;
 	}
 	
 	/**
 	 * @return vetDTO
 	 */
 	public VersionEtapeDTO getVetDTO() {
 		return vetDTO;
 	}
 
 	/**
 	 * @param vetDTO
 	 */
 	public void setVetDTO(VersionEtapeDTO vetDTO) {
 		this.vetDTO = vetDTO;
 	}
 
 	/**
 	 * @return codeRI
 	 */
 	public int getCodeRI() {
 		return codeRI;
 	}
 
 	/**
 	 * @param codeRI
 	 */
 	public void setCodeRI(int codeRI) {
 		this.codeRI = codeRI;
 	}
 
 	/**
 	 * @return allPJs
 	 */
 	public List<NomenclaturePojo> getAllPJs() {
 		return new ArrayList<NomenclaturePojo>(allPJs);
 	}
 
 	/**
 	 * @param allPJs
 	 */
 	public void setAllPJs(Collection<NomenclaturePojo> allPJs) {
 		Set<NomenclaturePojo> set = new TreeSet<NomenclaturePojo>(
 				new ComparatorString(NomenclaturePojo.class));
 		set.addAll(allPJs);
 		this.allPJs = set;
 	}
 	
 	/**
 	 * @return code
 	 */
 	public HtmlInputText getCode() {
 		return code;
 	}
 
 	/**
 	 * @param code
 	 */
 	public void setCode(HtmlInputText code) {
 		this.code = code;
 	}
 
 	/**
 	 * @param pJTraitee
 	 */
 	public void setPJTraitee(NomenclaturePojo pJTraitee) {
 		PJTraitee = pJTraitee;
 	}
 
 	/**
 	 * @return pjTraitee
 	 */
 	public NomenclaturePojo getPJTraitee() {
 		return PJTraitee;
 	}
 	
 	/**
 	 * @return uploadPath
 	 */
 	public String getUploadPath() {
 		return uploadPath;
 	}
 
 	/**
 	 * @param uploadPath
 	 */
 	public void setUploadPath(String uploadPath) {
 		this.uploadPath = uploadPath;
 	}
 
 	/**
 	 * @return uploadedFile
 	 */
 	public UploadedFile getUploadedFile() {
 		return uploadedFile;
 	}
 
 	/**
 	 * @param uploadedFile
 	 */
 	public void setUploadedFile(UploadedFile uploadedFile) {
 		this.uploadedFile = uploadedFile;
 	}
 	
 	/**
 	 * @return String
 	 */
 	public String getFileName() {
 		return fileName;
 	}
 
 	/**
 	 * @param fileName
 	 */
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public boolean getUseUpload() {
 		return useUpload;
 	}
 
 	public void setUseUpload(final boolean useUpload) {
 		this.useUpload = useUpload;
 	}
 
 }
