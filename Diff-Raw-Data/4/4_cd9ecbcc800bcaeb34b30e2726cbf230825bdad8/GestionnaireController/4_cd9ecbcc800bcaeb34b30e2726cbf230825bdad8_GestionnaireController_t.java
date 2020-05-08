 /**
  * 
  */
 package org.esupportail.opi.web.controllers.user;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.faces.event.ValueChangeEvent;
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.parameters.accessRight.Profile;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.beanEnum.WayfEnum;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.controllers.AbstractAccessController;
 import org.esupportail.opi.web.controllers.references.CommissionController;
 import org.esupportail.wssi.services.remote.CentreGestion;
 import org.springframework.util.StringUtils;
 
 /**
  * @author cleprous
  *
  */
 public class GestionnaireController extends AbstractAccessController {
 
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = 1739075202274589002L;
 
 
 	
 	/*
 	 ******************* PROPERTIES ******************* */
 
 	/**
 	 * The manager.
 	 */
 	private Gestionnaire manager;
 	
 	/**
 	 * The list of managers.
 	 */
 	private List<Gestionnaire> listeGestionnaires;
 	
 	/**
 	 * The actionEnum.
 	 */
 	private ActionEnum actionEnum;
 
 	/**
 	 * The id profil selected.
 	 */
 	private Integer idProfilSelected;
 	
 	/**
 	 * The CommissionController.
 	 */
 	private CommissionController commissionController;
 	
 	/**
 	 * A logger.
 	 */
 	private final Logger log = new LoggerImpl(getClass());
 	
 	
 	
 	/*
 	 ******************* INIT ************************* */
 
 	
 
 	/**
 	 * Constructors.
 	 */
 	public GestionnaireController() {
 		super();
 	}
 	
 	/** 
 	 * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
 	 */
 	@Override
 	public void reset() {
 		super.reset();
 		manager = new Gestionnaire();
 		actionEnum = new ActionEnum();
 		
 
 	}
 
 	/** 
 	 * @see org.esupportail.opi.web.controllers.AbstractContextAwareController#afterPropertiesSetInternal()
 	 */
 	@Override
 	public void afterPropertiesSetInternal() {
 		super.afterPropertiesSetInternal();
 		Assert.notNull(this.commissionController, "property commissionController of class " 
 				+ this.getClass().getName() + " can not be null");
 	}
 	
 	/*
 	 ******************* CALLBACK ********************** */
 	
 	/**
 	 * Callback to Gestionnaires list.
 	 * @return String 
 	 */
 	public String goSeeAllManagers() {
 		listeGestionnaires =  getDomainService().getManagers();
 		return NavigationRulesConst.MANAGED_MANAGER;
 	}
 	
 	
 	
 	
 	/**
 	 * Callback to Gestionnaires add or update.
 	 * @return String 
 	 */
 	public String goEnterManager() {
 		commissionController.reset();
 		if (actionEnum.getWhatAction().equals(ActionEnum.ADD_ACTION)) {
 			reset();
 			actionEnum.setWhatAction(ActionEnum.ADD_ACTION);
 		} else if (actionEnum.getWhatAction().equals(ActionEnum.UPDATE_ACTION)) {
 			manager = getDomainService().getManager(manager.getLogin());
 			commissionController.setSelectedCommissions(new ArrayList<Commission>(manager.getRightOnCmi()));
 			setIdProfilSelected(manager.getProfile().getId());
 		}
 		commissionController.getWayfEnum().setWhereAreYouFrom(WayfEnum.MANAGER_VALUE);
 		return NavigationRulesConst.ENTER_MANAGER;
 	}
 	
 	/**
 	 * Callback to Gestionnaire consult.
 	 * @return String 
 	 */
 	public String goSeeOneManager() {
 		commissionController.reset();
 		commissionController.setSelectedCommissions(new ArrayList<Commission>(manager.getRightOnCmi()));
 		return NavigationRulesConst.SEE_MANAGER;
 	}
 	
 	
 	/**
 	 * Callback to Search the manager in LDAP.
 	 * @return String 
 	 */
 	public String goSearchManager() {
 		return NavigationRulesConst.SEARCH_MANAGER;
 	}
 	
 	/**
 	 * Callback to Affect right the manager in LDAP.
 	 * @return String 
 	 */
 	public String goAffectRightManager() {
 		Gestionnaire g = getCurrentGest();
 		if (g != null) {
			List<Commission> cmiSelected = commissionController.getSelectedCommissions();
 			commissionController.reset();
			commissionController.setSelectedCommissions(cmiSelected);
 			setIdProfilSelected(g.getProfile().getId());
 			return NavigationRulesConst.AFFECT_RIGHT_MANAGER;
 		}
 		return "";
 	}
 	
 	
 	/*
 	 ******************* METHODS ********************** */
 
 	/**
 	 * Add a Manager to the dataBase.
 	 */
 	public String add() {
 		if (log.isDebugEnabled()) {
 			log.debug("enterind add with gestionnaire = " + manager);
 		}
 		String result = null;
 		if (ctrlEnter(manager)) {
 			if (!StringUtils.hasText(manager.getCodeCge())) {
 				manager.setCodeCge(null);
 			}
 			manager = (Gestionnaire) getDomainService().add(manager, getCurrentGest().getLogin());
 			manager.setProfile(new Profile());
 			manager.getProfile().setId(getIdProfilSelected());
 			
 			//s'il n''est pas rattache e une centre de gestion, il a des droits sur des commissions
 			if (manager.getCodeCge() == null) {
 				manager.setRightOnCmi(new HashSet<Commission>(commissionController.getSelectedCommissions()));
 			}
 			
 			manager = toUpperCase(manager);
 			
 			getDomainService().addUser(manager);
 			
 			reset();
 			commissionController.reset();
 			addInfoMessage(null, "INFO.ENTER.SUCCESS");
 			result = NavigationRulesConst.MANAGED_MANAGER;
 		}
 		if (log.isDebugEnabled()) {
 			log.debug("leaving add");
 		}
 		actionEnum.setWhatAction(ActionEnum.ADD_ACTION);
 		return result;
 	}
 	
 	
 	/**
 	 * Update a Manager to the dataBase.
 	 * @return String 
 	 */
 	public String update() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering update with Gestionnaire = " + manager);
 		}
 		//TODO add currentUser
 		if (ctrlEnter(manager)) {
 			if (!StringUtils.hasText(manager.getCodeCge())) {
 				manager.setCodeCge(null);
 			}
 			
 			//s'il n''est pas rattache e une centre de gestion, il a des droits sur des commissions
 			if (manager.getCodeCge() == null) {
 				manager.setRightOnCmi(new HashSet<Commission>(
 						commissionController.getSelectedCommissions()));
 			} else {
 				manager.setRightOnCmi(null);
 			}
 			
 			manager = (Gestionnaire) getDomainService().update(manager, getCurrentGest().getLogin());
 			if (!manager.getProfile().getId().equals(getIdProfilSelected())) {
 				manager.setProfile(
 						getParameterService().getProfile(getIdProfilSelected(), null));
 			}
 			
 			manager = toUpperCase(manager);
 			
 			getDomainService().updateUser(manager);
 			
 			reset();
 			commissionController.reset();
 			addInfoMessage(null, "INFO.ENTER.SUCCESS");
 			return goSeeAllManagers();
 		}
 		
 		if (log.isDebugEnabled()) {
 			log.debug("leaving update");
 		}
 		return null;
 		
 	}
 	
 	
 	/**
 	 * Delete a Manager to the dataBase.
 	 */
 	public String delete() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering delete with manager = " + manager);
 		}
 
 		getDomainService().deleteUser(manager);
 		reset();
 		
 		addInfoMessage(null, "INFO.DELETE.SUCCESS");
 		
 		if (log.isDebugEnabled()) {
 			log.debug("leaving delete");
 		}
 		return goSeeAllManagers();
 	}
 
 	
 	/**
 	 * The selected cge.
 	 * @param event
 	 */
 	public void selectCge(final ValueChangeEvent event) {
 		String codeCge = (String) event.getNewValue();
 		manager.setCodeCge(codeCge);
 
 	}
 	
 	
 	
 	/* ### ALL CONTROL ####*/
 	
 	/**
 	 * Control manager attributes for the adding and updating.
 	 * @param g
 	 * @return Boolean
 	 */
 	private Boolean ctrlEnter(final Gestionnaire g) {
 		Boolean ctrlOk = true;
 		if (!StringUtils.hasText(g.getNomUsuel())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.NAME"));
 			ctrlOk = false;
 		} 
 		if (!StringUtils.hasText(g.getPrenom())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("INDIVIDU.PRENOM"));
 			ctrlOk = false;
 		}
 		if (!StringUtils.hasText(g.getLogin())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("GESTIONNAIRE.LOGIN"));
 			ctrlOk = false;
 		} else {
 			if (!getDomainService().gestionnaireLoginIsUnique(g)) {
 				ctrlOk = false;
 				addErrorMessage(null, "ERROR.FIELD.NOT_UNIQUE", getString("GESTIONNAIRE.LOGIN"));
 			}
 		}
 		if (!StringUtils.hasText(g.getAdressMail())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.MAIL"));
 			ctrlOk = false;
 		}
 		if (getIdProfilSelected() == null || getIdProfilSelected() == 0) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("PROFIL"));
 			ctrlOk = false;
 		}
 		if (g.getDateDbtValidite() == null) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("GESTIONNAIRE.DBT_VALID"));
 			ctrlOk = false;
 		} else if (g.getDateFinValidite() != null) {
 			//si dateDbt et dateFin != null alors la date de Fin doit etre superieure e la date de debut
 			if (g.getDateFinValidite().before(g.getDateDbtValidite())) {
 				addErrorMessage(null, "ERROR.FIELD.DAT_FIN.VAL");
 				ctrlOk = false;
 			}
 		}
 		
 			
 		if (log.isDebugEnabled()) {
 			log.debug("leaving ctrlEnter return = " + ctrlOk);
 		}
 		return ctrlOk;
 	}
 	
 	
 	/**
 	 * to upper case attributes nomUsuel ans Prenom.
 	 * @param gest
 	 * @return Gestionnaire
 	 */
 	private Gestionnaire toUpperCase(final Gestionnaire gest) {
 		Gestionnaire g = gest;
 		g.setNomUsuel(gest.getNomUsuel().toUpperCase());
 		g.setPrenom(gest.getPrenom().toUpperCase());
 		
 		return g;
 	}
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
 	/**
 	 * @return List< CentreGestion>
 	 */
 	public List<CentreGestion> getCentreGestion() {
 		return getDomainApoService().getCentreGestion();
 	}
 	
 	
 	/**
 	 * @param manager the manager to set
 	 */
 	public void setManager(final Gestionnaire manager) {
 		//Clone est utilise afin que l'utilisateur puisse modifier l'objet sans toucher au CACHE (par reference)
 		//Probleme rencontre lors du modification annulee(par exemple), le cache etait tout de meme modifier
 		this.manager = manager.clone();
 	}
 	
 	/**
 	 * @return the manager
 	 */
 	public Gestionnaire getManager() {
 		return manager;
 	}
 
 	
 
 	/**
 	 * @return the listeGestionnaires
 	 */
 	public List<Gestionnaire> getListeGestionnaires() {
 		return listeGestionnaires;
 	}
 
 	/**
 	 * @param listeGestionnaires the listeGestionnaires to set
 	 */
 	public void setListeGestionnaires(final List<Gestionnaire> listeGestionnaires) {
 		this.listeGestionnaires = listeGestionnaires;
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
 	 * @return the idProfilSelected
 	 */
 	public Integer getIdProfilSelected() {
 		return idProfilSelected;
 	}
 
 	/**
 	 * @param idProfilSelected the idProfilSelected to set
 	 */
 	public void setIdProfilSelected(final Integer idProfilSelected) {
 		this.idProfilSelected = idProfilSelected;
 	}
 
 
 	/**
 	 * @param commissionController the commissionController to set
 	 */
 	public void setCommissionController(final CommissionController commissionController) {
 		this.commissionController = commissionController;
 	}
 
 	
 }
