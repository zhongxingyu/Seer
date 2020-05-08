 /**
  * ESUP-Portail Blank Application - Copyright (c) 2006 ESUP-Portail consortium
  * http://sourcesup.cru.fr/projects/esup-opiR1
  */
 package org.esupportail.opi.dao;
 
 import static com.mysema.query.types.ConstantImpl.create;
 import static fj.data.List.list;
 import static fj.data.Option.some;
 import static fj.data.Option.somes;
 
 import java.lang.Class;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import fj.*;
 import org.esupportail.commons.dao.AbstractJdbcJndiHibernateDaoService;
 import org.esupportail.commons.dao.HqlUtils;
 import org.esupportail.commons.services.application.UninitializedDatabaseException;
 import org.esupportail.commons.services.application.VersionningUtils;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.opi.dao.utils.PaginatorFactory;
 import org.esupportail.opi.domain.beans.VersionManager;
 import org.esupportail.opi.domain.beans.parameters.AutoListPrincipale;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.parameters.PieceJustificative;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.parameters.VetAutoLp;
 import org.esupportail.opi.domain.beans.pilotage.ArchiveTask;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.LinkTrtCmiCamp;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV;
 import org.esupportail.opi.domain.beans.references.rendezvous.Horaire;
 import org.esupportail.opi.domain.beans.references.rendezvous.IndividuDate;
 import org.esupportail.opi.domain.beans.references.rendezvous.JourHoraire;
 import org.esupportail.opi.domain.beans.references.rendezvous.TrancheFermee;
 import org.esupportail.opi.domain.beans.references.rendezvous.VetCalendar;
 import org.esupportail.opi.domain.beans.user.Adresse;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.User;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.domain.beans.user.candidature.MissingPiece;
 import org.esupportail.opi.domain.beans.user.candidature.VersionEtpOpi;
 import org.esupportail.opi.domain.beans.user.indcursus.IndBac;
 import org.esupportail.opi.domain.beans.user.indcursus.IndCursus;
 import org.esupportail.opi.domain.beans.user.indcursus.IndCursusScol;
 import org.esupportail.opi.domain.beans.user.situation.IndSituation;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.utils.primefaces.PFFilters;
 import org.hibernate.FetchMode;
 import org.hibernate.Hibernate;
 import org.hibernate.LockMode;
 import org.hibernate.criterion.CriteriaSpecification;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.jdbc.BadSqlGrammarException;
 import org.springframework.orm.hibernate3.HibernateSystemException;
 import org.springframework.util.StringUtils;
 
 import com.mysema.query.jpa.hibernate.HibernateQuery;
 import com.mysema.query.types.path.PathBuilder;
 
 import fj.data.Option;
 import fj.data.Stream;
 
 /**
  * The Hibernate implementation of the DAO service.
  */
 public class HibernateDaoServiceImpl extends AbstractJdbcJndiHibernateDaoService implements DaoService {
 
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = 3152554337896617315L;
 
 	/**
 	 * The name of the 'id' attribute.
 	 */
 	private static final String ID_ATTRIBUTE = "id";
 
 	/**
 	 * The name of the 'temoinEnService' attribute.
 	 */
 	private static final String IN_USE_ATTRIBUTE = "temoinEnService";
 
 	private PaginatorFactory pf;
 	
 	/**
 	 * A logger.
 	 */
 	private final Logger log = new LoggerImpl(getClass());
 
 	/**
 	 * Bean constructor.
 	 */
 	public HibernateDaoServiceImpl() {
 		super();
 	}
 	
 	public void setPf(PaginatorFactory pf) {
 	    this.pf = pf;
 	}
 	
 	
 
 	//////////////////////////////////////////////////////////////
 	// OBJECT
 	//////////////////////////////////////////////////////////////
 	/*Cette partie est utilise pour initialiser certain attribut des objets.
 	 * Ceci car nos objets sont utilise en mode detache*/
 
 	/**
 	 * Permet de rattacher un objet e la session hibernate.
 	 * @param o1
 	 */
 	private void lockObject(final Object o1) {
 
 		if (log.isDebugEnabled()) {
 			log.debug("entering lockObject( " + o1 + " )");
 		}
 //		Session s = getHibernateTemplate().getSessionFactory().getCurrentSession();
 
 
 		//on ne lock que si l'objet n'est pas en session
 		//si 2 objet identique n'ont pas le meme emplacement memoire : contains ne les reconnais pas
 		//par contre une NonUniqueObjectException sera leve e cause des id database identique
 		if (log.isDebugEnabled()) {
 			log.debug("getHibernateTemplate.contains(o1) = " + getHibernateTemplate().contains(o1));
 		}
 		if (!getHibernateTemplate().contains(o1)) {
 			getHibernateTemplate().lock(o1, LockMode.NONE);
 		} 
 	}
 
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#initOneProxyHib(
 	 * java.lang.Object, java.lang.Object, java.lang.Class)
 	 */
 	public void initOneProxyHib(final Object args, final Object proxy, final Class< ? > proxyClass) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering initOneProxyHib ( " + args + ", le proxy )");
 		}
 //		Session s = getHibernateTemplate().getSessionFactory().getCurrentSession();
 		if (proxy != null) {
 //			NormeSI obj = (NormeSI) args;
 			try {
 //				if (!proxy.getClass().isAssignableFrom(proxyClass)) {
 					
 					lockObject(args);
 					if (log.isDebugEnabled()) {
 						log.debug("execute Hibernate.initialize(proxy)");
 					}
 					if (!getHibernateTemplate().contains(proxy)) {
 						Hibernate.initialize(proxy);
 					}
 
 
 //				}
 			} catch (HibernateSystemException e) {
 				log.info(" exception when init Proxy hibernate : " + e);
 				// TODO Correction du pb de mise à jour des attributs de
 				// l'individu, vérifier si effets de bord JPI 22/12/2010
 				getHibernateTemplate().merge(args);
 				//TODO reflechir a une meilleure solution
 				getHibernateTemplate().flush();
 				getHibernateTemplate().clear();
 				lockObject(args);
 				if (log.isDebugEnabled()) {
 					log.debug("execute Hibernate.initialize(proxy)");
 				}
 				if (!getHibernateTemplate().contains(proxy)) {
 					Hibernate.initialize(proxy);
 				}
 				// TODO solution à améliorer, récupéré depuis Esup-ancien
 //				getHibernateTemplate().get(obj.getClass(), obj.getId());
 //				obj = (NormeSI) s.get(obj.getClass(), obj.getId());
 //				log.info(" object from hibernate template : " + obj);
 			} 
 
 		}
 
 	}
 
 	//////////////////////////////////////////////////////////////
 	// User
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#getUser(String, Class)
 	 */
 	@SuppressWarnings("unchecked")
 	public User getUser(final String id, final Class< ? > clazz) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getUser( " + id + ", " + clazz + " )");
 		}
 		User u = null;
 		if (clazz.equals(Gestionnaire.class)) {
 			u = getManager(id);
 			//TODO ajout critere test date validite
 		} else if (clazz.equals(Individu.class)) {
 			DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 			criteria.add(Restrictions.eq("codeEtu", id));
 			criteria.add(Restrictions.eq(IN_USE_ATTRIBUTE, true));
 			// ajout des fetchMode
 			criteria.setFetchMode("campagnes", FetchMode.JOIN);
 			criteria.setFetchMode("cursus", FetchMode.JOIN);
 			criteria.setFetchMode("cursusScol", FetchMode.JOIN);
 			criteria.setFetchMode("adresses", FetchMode.JOIN);
 			List<Individu> individus = getHibernateTemplate().findByCriteria(criteria);
 			if (!individus.isEmpty()) {
 				u = individus.get(0);
 			}
 		}
 		return u;
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getUser(java.lang.Integer)
 	 */
 	public User getUser(final Integer id) {
 		return getHibernateTemplate().get(User.class, id);
 	}
 
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#getIndividu(java.lang.String, java.util.Date)
 	 */
 	@SuppressWarnings("unchecked")
 	public Individu getIndividu(final String numDosOpi, final Date dateNaissance) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividu( " + numDosOpi + ", " + dateNaissance + " )");
 		}
 		if (StringUtils.hasText(numDosOpi)) {
 			DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 			criteria.add(Restrictions.eq("numDossierOpi", numDosOpi));
 			if (dateNaissance != null) {
 				criteria.add(Restrictions.eq("dateNaissance", dateNaissance));
 			}
 			
 			criteria.add(Restrictions.eq(IN_USE_ATTRIBUTE, true));
 			
 			criteria.setResultTransformer(
 					CriteriaSpecification.DISTINCT_ROOT_ENTITY);
 			// ajout des fetchMode
 			criteria.setFetchMode("campagnes", FetchMode.JOIN);
 			criteria.setFetchMode("cursus", FetchMode.JOIN);
 			criteria.setFetchMode("cursusScol", FetchMode.JOIN);
 			criteria.setFetchMode("adresses", FetchMode.JOIN);
 			List<Individu> individus = getHibernateTemplate().findByCriteria(criteria);
 			if (!individus.isEmpty()) {
 				return individus.get(0);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#getIndividuCodEtu(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Individu getIndividuCodEtu(final String codEtu) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividuSearch( " + codEtu + ")");
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 		criteria.add(Restrictions.eq("codeEtu", codEtu));
 
 		// cas de récupération d'un étudiant archivé
 //		criteria.add(Restrictions.eq(IN_USE_ATTRIBUTE, true));
 		
 		List<Individu> individus = getHibernateTemplate().findByCriteria(criteria);
 		
 		if (individus != null && !individus.isEmpty()) {
 			return individus.get(0);
 		}
 		return null;	
 	}
 
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#getIndividuINE(java.lang.String, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Individu getIndividuINE(final String codeNNE, final String cleNNE) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividuINE( " + codeNNE + ", " + cleNNE + ")");
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 		criteria.add(Restrictions.ilike("codeNNE", codeNNE, MatchMode.EXACT));
 		criteria.add(Restrictions.ilike("codeClefNNE", cleNNE, MatchMode.EXACT));
 
 		// cas de récupération d'un étudiant archivé
 //		criteria.add(Restrictions.eq(IN_USE_ATTRIBUTE, true));
 		
 		List<Individu> individus = getHibernateTemplate().findByCriteria(criteria);
 		
 		if (individus != null && !individus.isEmpty()) {
 			return individus.get(0);
 		}
 		return null;	
 	}
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#getIndividu(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Individu getIndividu(final String mail) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividu( " + mail + " )");
 		}
 		DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 		criteria.add(
 				Restrictions.or(
 						Restrictions.eq("emailAnnuaire", mail), 
 						Restrictions.eq("adressMail", mail)));
 
 		// COMMENT 24/02/10 : on ne doit pas avoir 2 individus (archivé ou non)
 		// avec le même mail
 //		criteria.add(Restrictions.eq(IN_USE_ATTRIBUTE, true));
 		
 		List<Individu> individus = getHibernateTemplate().findByCriteria(criteria);
 		if (individus != null && !individus.isEmpty()) {
 			return individus.get(0);
 		}
 
 		return null;
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Individu> getIndividus(final Commission commission, final Boolean validate,
 	    final List<String> listeRICodes) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividu( " + commission + ", " + validate + " )");
 		}
 
 		// TODO : voir pour améliorer la requête
 //		List<Integer> trtCmi = new ArrayList<Integer>();
 //		
 //		for (TraitementCmi trt : commission.getTraitementCmi()) {
 //			trtCmi.add(trt.getId());
 //		}
 
 		String hqlQuery = HqlUtils.selectFromWhere("distinct i", 
 				"Individu as i left join i.voeux as v", "i.temoinEnService = " + true);
 
 		hqlQuery += " AND (v.linkTrtCmiCamp.traitementCmi in (SELECT trtCmi "
 			+ "FROM Commission c inner join c.traitementCmi as trtCmi where c = ? ))";
 //		hqlQuery += " AND (v.linkTrtCmiCamp.traitementCmi.id in ( " + trtCmi + " ))";
 		
 		if (listeRICodes != null) {
 			hqlQuery += " AND (v.linkTrtCmiCamp.campagne.codeRI in (";
 			boolean firstRI = true;	
 			for (String rICode : listeRICodes) {
 				if (firstRI)	{
 					hqlQuery += rICode;
 					firstRI = false;
 				} else {
 					hqlQuery += ",";
 					hqlQuery += rICode;
 				}
 			}
 			hqlQuery += ")) ";
 
 		}
 		if (validate != null) {
 			hqlQuery += " AND (v in (SELECT a.indVoeu from Avis a WHERE a.validation = " + validate
 			+ " AND a.temoinEnService = true))";
 		}  else {
 			hqlQuery += ")";
 		}
 
 		return getHibernateTemplate().find(hqlQuery, commission);
 //		return getHibernateTemplate().find(hqlQuery);
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getIndividus(
 	 * java.lang.Boolean, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Individu> getIndividus(final Boolean withWishes, final String codeTypeTrt) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividus withWishes = " 
 					+ withWishes + ", codeTypeTrt = " + codeTypeTrt);
 		}
 
 		StringBuilder hqlQuery = new StringBuilder(HqlUtils.selectFromWhere("distinct i", 
 				"Individu as i left join i.voeux as v", "i.temoinEnService = " + true));
 		if (withWishes != null && !withWishes) {
 			hqlQuery.append(" AND v is null");
 		} else if (StringUtils.hasText(codeTypeTrt)) {
 			hqlQuery.append(" AND v.codTypeTrait = '" + codeTypeTrt + "'");
 		}
 		
 		return getHibernateTemplate().find(hqlQuery.toString());
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getAllIndividusEtu()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Individu> getAllIndividusEtu() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getAllIndividusEtu()" );
 		}
 
 		
 		DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 		criteria.add(Restrictions.isNotNull("codeEtu"))
 				.setResultTransformer(
 						CriteriaSpecification.DISTINCT_ROOT_ENTITY);
 
 		criteria.add(Restrictions.eq("temoinEnService", true));
 		
 //		StringBuilder hqlQuery = new StringBuilder(HqlUtils.selectFromWhere("i", 
 //				"Individu as i ", "i.temoinEnService = " + true));
 //		hqlQuery.append(" AND i.codEtu is not Null");
 		List<Individu> i = getHibernateTemplate().findByCriteria(criteria);
 		
 		if (log.isDebugEnabled()) {
 			log.debug("finishing getAllIndividusEtu()" );
 		}
 		return i;		
 	}
 
 	
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getIndividus(
 	 * org.esupportail.opi.domain.beans.references.commission.TraitementCmi, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Individu> getIndividus(final TraitementCmi trt, final String state) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividus trt = " 
 					+ trt + ", state = " + state);
 		}
 		DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 		criteria.createCriteria("voeux")
 				.add(Restrictions.eq("state", state))
 				.createCriteria("linkTrtCmiCamp")
 				.add(Restrictions.eq("traitementCmi", trt))
 				.setResultTransformer(
 						CriteriaSpecification.DISTINCT_ROOT_ENTITY);
 		
 		criteria.add(Restrictions.eq("temoinEnService", true));
 		
 		List<Individu> i = getHibernateTemplate().findByCriteria(criteria);
 		
 		if (log.isDebugEnabled()) {
 			log.debug("finishing getIndividus" );
 		}
 		return i;	
 	}
 	
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getIndividus(
 	 * org.esupportail.opi.domain.beans.references.commission.TraitementCmi, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Individu> getIndividus(final TraitementCmi trt) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividus trt = " + trt);
 		}
 		DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 		criteria.createCriteria("voeux")
 				.createCriteria("linkTrtCmiCamp")
 				.add(Restrictions.eq("traitementCmi", trt))
 				.setResultTransformer(
 						CriteriaSpecification.DISTINCT_ROOT_ENTITY);
 
 		criteria.add(Restrictions.eq("temoinEnService", true));
 
 		List<Individu> i = getHibernateTemplate().findByCriteria(criteria);
 
 		if (log.isDebugEnabled()) {
 			log.debug("finishing getIndividus");
 		}
 		return i;
 	}
 	
 	
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getIndividusByCampagne(
 	 * org.esupportail.opi.domain.beans.parameters.Campagne, Boolean)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Individu> getIndividusByCampagne(final Campagne campagne, final Boolean temSve) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividusByCampagne( " + campagne + " )");
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 		criteria.createCriteria("campagnes")
 				.add(Restrictions.eq("id", campagne.getId()));
 
 		criteria.add(Restrictions.eq("temoinEnService", temSve));
 		
 		List<Individu> i = getHibernateTemplate().findByCriteria(criteria);
 		
 		if (log.isDebugEnabled()) {
 			log.debug("finishing getIndividusByCampagne" );
 		}
 		return i;	
 	}
 	
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * getIndividuSearch(java.lang.String, java.lang.String, 
 	 * java.util.Date, java.lang.String, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Individu> getIndividuSearch(final String nomPatronymique, final String prenom,
 			final Date dateNaissance, final String codPayNaissance, final String codDepPaysNaissance) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividuSearch( " + nomPatronymique 
 					+ ", " + prenom + ", " + dateNaissance + ", " + codPayNaissance 
 					+ ", " + codDepPaysNaissance + ")");
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(Individu.class);
 		criteria.add(Restrictions.eq("nomPatronymique", nomPatronymique));
 		criteria.add(Restrictions.eq("prenom", prenom));
 		criteria.add(Restrictions.eq("dateNaissance", dateNaissance));
 		criteria.add(Restrictions.eq("codPayNaissance", codPayNaissance));
 		if (codPayNaissance.equals(Constantes.CODEFRANCE)) {
 			criteria.add(Restrictions.eq("codDepPaysNaissance", codDepPaysNaissance));
 		}
 
 		criteria.setResultTransformer(
 				CriteriaSpecification.DISTINCT_ROOT_ENTITY);
 		List<Individu> individus = getHibernateTemplate().findByCriteria(criteria);
 		
 		if (log.isDebugEnabled()) {
 			log.debug("finishing getIndividuSearch" );
 		}
 		return individus;	
 	}
 
     // TODO : move all the below filters and the sliceOfInd method in an IndividuDAO
     final F<Set<TraitementCmi>, F<HibernateQuery, HibernateQuery>> trtCmiFilter =
             new F<Set<TraitementCmi>, F<HibernateQuery,HibernateQuery>>() {
                 public F<HibernateQuery, HibernateQuery> f(final Set<TraitementCmi> trtCmis) {
                     return new F<HibernateQuery, HibernateQuery>() {
                         public HibernateQuery f(final HibernateQuery query) {
                             final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();
                             final PathBuilder<TraitementCmi> subPath =
                                     path.getSet("voeux", IndVoeu.class).any()
                                             .get("linkTrtCmiCamp", LinkTrtCmiCamp.class)
                                             .get("traitementCmi", TraitementCmi.class);
                             return (trtCmis.size() > 0) ? query.where(subPath.in(trtCmis)) : query;
                         }
                     };
                 }
             };
 	
     final F<Set<TypeDecision>, F<HibernateQuery, HibernateQuery>> typeDecSeqFilter =
             new F<Set<TypeDecision>, F<HibernateQuery, HibernateQuery>>() {
                 public F<HibernateQuery, HibernateQuery> f(final Set<TypeDecision> typeDecisions) {
                     return new F<HibernateQuery, HibernateQuery>() {
                         public HibernateQuery f(HibernateQuery query) {
                             final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();
                             PathBuilder<Avis> avisPath =
                                     path.getSet("voeux", IndVoeu.class).any()
                                             .getSet("avis", Avis.class).any();
                             PathBuilder<TypeDecision> typeDecPath = avisPath.get("result", TypeDecision.class);
                             HibernateQuery base = query.where(avisPath.get("temoinEnService").eq(true));
                             return (typeDecisions.size() > 0) ? base.where(typeDecPath.in(typeDecisions)) : base;
                         }
                     };
                 }
             };
 
     final F<HibernateQuery, HibernateQuery> temoinFilter =
             new F<HibernateQuery, HibernateQuery>() {
                 public HibernateQuery f(final HibernateQuery query) {
                     final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();
                     return query.where(path.get("temoinEnService").eq(true));
                 }
             };
 	
     final F<HibernateQuery, HibernateQuery> campagneFilter =
             new F<HibernateQuery, HibernateQuery>() {
                 public HibernateQuery f(HibernateQuery query) {
                     final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();
                     return query.where(path.getSet("campagnes", Campagne.class).any()
                             .get("temoinEnService").eq(true));
                 }
             };
             
 
     final F<Set<Integer>, F<HibernateQuery, HibernateQuery>> regInscrFilter =
             new F<Set<Integer>, F<HibernateQuery,HibernateQuery>>() {
                 public F<HibernateQuery, HibernateQuery> f(final Set<Integer> listCodesRI) {
                     return new F<HibernateQuery, HibernateQuery>() {
                         public HibernateQuery f(HibernateQuery query) {
                             final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();	            
                             return (listCodesRI.size() > 0) ? query.where(
                                     path.getSet("campagnes", Campagne.class).any()
                                             .get("codeRI").in(listCodesRI)) : query;
                         }
                     };
                 }
             };
             
     final F<Boolean, F<HibernateQuery, HibernateQuery>> treatedWishFilter =
             new F<Boolean, F<HibernateQuery,HibernateQuery>>() {
                 public F<HibernateQuery, HibernateQuery> f(final Boolean treated) {
                     return new F<HibernateQuery, HibernateQuery>() {
                         public HibernateQuery f(HibernateQuery query) {
                             final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();
                             return query.where(
                                     path.getSet("voeux", IndVoeu.class).any()
                                             .get("haveBeTraited").eq(treated));
                         }
                     };
                 }
             };
 	
     final F<String, F<HibernateQuery, HibernateQuery>> notCodeTypeTrtmtFilter =
             new F<String, F<HibernateQuery,HibernateQuery>>() {
                 public F<HibernateQuery, HibernateQuery> f(final String code) {
                     return new F<HibernateQuery, HibernateQuery>() {
                         public HibernateQuery f(HibernateQuery query) {
                             final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();
                             return query.where(
                                     path.getSet("voeux", IndVoeu.class).any()
                                             .get("codTypeTrait").ne(code));
                         }
                     };
                 }
             };
 
     final F<Boolean, F<HibernateQuery, HibernateQuery>> validWishFilter =
             new F<Boolean, F<HibernateQuery, HibernateQuery>>() {
                 public F<HibernateQuery, HibernateQuery> f(final Boolean valid) {
                     return new F<HibernateQuery, HibernateQuery>() {
                         public HibernateQuery f(HibernateQuery query) {
                             final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();
                             return query.where(
                                     path.getSet("voeux", IndVoeu.class).any()
                                             .getSet("avis", Avis.class).any()
                                             .get("validation", Boolean.class).eq(valid));
                         }
                     };
                 }
             };
 
     final F<Date, F<HibernateQuery, HibernateQuery>> wishCreationFilter =
             new F<Date, F<HibernateQuery, HibernateQuery>>() {
                 public F<HibernateQuery, HibernateQuery> f(final Date date) {
                     return new F<HibernateQuery, HibernateQuery>() {
                         public HibernateQuery f(HibernateQuery query) {
                             final PathBuilder<Individu> path = pf.indPaginator().getTPathBuilder();
                             return query.where(
                                     path.getSet("voeux", IndVoeu.class).any()
                                             .getDate("dateCreaEnr", Date.class).eq(date));
                         }
                     };
                 }
             };
 	
     @SuppressWarnings("unchecked")
     @Override
     public P2<Long, Stream<Individu>> sliceOfInd(PFFilters pfFilters,
                                                  Set<TypeDecision> typesDec,
                                                  Option<Boolean> treatedWish,
                                                  Option<Boolean> validWish,
                                                  Option<Date> wishCreation,
                                                  Option<String> codeTypeTrtmt,
                                                  Set<TraitementCmi> trtCmis,
                                                  Set<Integer> listCodesRI) {
         final F<HibernateQuery, HibernateQuery> customFilter =
                 somes(list(
                         some(temoinFilter),
                         some(campagneFilter),
                         some(regInscrFilter.f(listCodesRI)),
                         some(typeDecSeqFilter.f(typesDec)),
                         treatedWish.map(treatedWishFilter),
                         validWish.map(validWishFilter),
                         wishCreation.map(wishCreationFilter),
                         codeTypeTrtmt.map(notCodeTypeTrtmtFilter),
                         some(trtCmiFilter.f(trtCmis)))).foldLeft1(Function.<HibernateQuery, HibernateQuery, HibernateQuery>andThen());
 	    
         return pf.indPaginator().lazySliceOf(
                 pfFilters.first,
                 pfFilters.pageSize,
                 pfFilters.sortField,
                 pfFilters.sortOrder,
                 pfFilters.filters,
                 some(customFilter));
     }
 	
 	
     /**
 	 * @see org.esupportail.opi.dao.DaoService#getVoeuxAcceptes()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<IndVoeu> getVoeuxAcceptes() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getVoeuxAcceptes()");
 		}
 
 		String hqlQuery = HqlUtils.selectFromWhere("distinct i", 
 				"IndVoeu as v", "v.temoinEnService = " + true);
 		hqlQuery += " AND (v in (SELECT a.indVoeu from Avis a WHERE a.validation = true"
 			+ " AND a.temoinEnService = true))";
 
 		return getHibernateTemplate().find(hqlQuery);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getManager(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	public Gestionnaire getManager(final String uid) {
 		DetachedCriteria criteria = DetachedCriteria.forClass(Gestionnaire.class);
 		criteria.add(Restrictions.like("login", uid + "%"));
 		criteria.add(Restrictions.eq(IN_USE_ATTRIBUTE, true));
		//TODO Pertinence du critere test date validite
//		criteria.add(Restrictions.le("dateDbtValidite", new Date()));
//		criteria.add(Restrictions.or(
//				Restrictions.ge("dateFinValidite", new Date()),
//				Restrictions.isNull("dateFinValidite")));
 		List<Gestionnaire> list = getHibernateTemplate().findByCriteria(criteria);
 		if (list != null && !list.isEmpty()) {
 			if (list.size() > 1) {
 				//TODO throw exception
 			}
 			return list.get(0);
 		}
 
 		return null;
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getManagers(java.util.Date)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Gestionnaire> getManagers(final Date date) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getManagers( " + date + " )");
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(Gestionnaire.class);
 		if (date != null) {
 			criteria.add(Restrictions.le("dateDbtValidite", new Date()));
 			criteria.add(Restrictions.or(
 					Restrictions.ge("dateFinValidite", new Date()),
 					Restrictions.isNull("dateFinValidite")));
 		}
 
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 	
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getManagers()
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Gestionnaire> getManagers() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getManagers()");
 		}
 		DetachedCriteria criteria = DetachedCriteria.forClass(Gestionnaire.class);
 		criteria.add(Restrictions.eq("temoinEnService", true));
 
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#getManagersByCmi(
 	 * org.esupportail.opi.domain.beans.references.commission.Commission)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Gestionnaire> getManagersByCmi(final Commission commission) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getManagersByCmi( " + commission +  " )");
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(Gestionnaire.class);
 		criteria.createCriteria("rightOnCmi")
 					.add(Restrictions.eq("id", commission.getId()));
 		
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#addUser(org.esupportail.opi.domain.beans.user.User)
 	 */
 	public void addUser(final User user) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addUser( " + user + " )");
 		}
 		addObject(user);
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#deleteUser(org.esupportail.opi.domain.beans.user.User)
 	 */
 	public void deleteUser(final User user) {
 		deleteObject(user);
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#deleteUserList(java.util.List)
 	 */
 	public void deleteUserList(final List<User> users) {
 		deleteObjects(users);
 	}
 	
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#updateUser(org.esupportail.opi.domain.beans.user.User)
 	 */
 	public void updateUser(final User user) {
 		updateObject(user);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#addIndCursus(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndCursus)
 	 */
 	public void addIndCursus(final IndCursus indCursus) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addIndCursus( " + indCursus + " )");
 		}
 		addObject(indCursus);		
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#updateIndCursus(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndCursus)
 	 */
 	public void updateIndCursus(final IndCursus indCursus) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateIndCursus( " + indCursus + " )");
 		}
 		updateObject(indCursus);
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#deleteIndCursus(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndCursus)
 	 */
 	public void deleteIndCursus(final IndCursus indCursus) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering deleteIndCursus( " + indCursus + " )");
 		}
 		deleteObject(indCursus);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#addIndCursusScol(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndCursusScol)
 	 */
 	public void addIndCursusScol(final IndCursusScol indCursusScol) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addIndCursusScol( " + indCursusScol + " )");
 		}
 		addObject(indCursusScol);		
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#updateIndCursusScol(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndCursusScol)
 	 */
 	public void updateIndCursusScol(final IndCursusScol indCursusScol) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateIndCursusScol( " + indCursusScol + " )");
 		}
 		updateObject(indCursusScol);
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#deleteIndCursusScol(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndCursusScol)
 	 */
 	public void deleteIndCursusScol(final IndCursusScol indCursusScol) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering deleteIndCursusScol( " + indCursusScol + " )");
 		}
 		deleteObject(indCursusScol);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#addIndBac(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndBac)
 	 */
 	public void addIndBac(final IndBac indBac) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addIndBac( " + indBac + " )");
 		}
 		addObject(indBac);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#updateIndBac(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndBac)
 	 */
 	public void updateIndBac(final IndBac indBac) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateIndBac( " + indBac + " )");
 		}
 		updateObject(indBac);
 	}
 
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#deleteIndBac(
 	 * 			org.esupportail.opi.domain.beans.user.indcursus.IndBac)
 	 */
 	public void deleteIndBac(final IndBac indBac) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering deleteIndBac( " + indBac + " )");
 		}
 		deleteObject(indBac);
 	}
 
 	//////////////////////////////////////////////////////////////
 	// Voeu
 	//////////////////////////////////////////////////////////////
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#addIndVoeu(
 	 * org.esupportail.opi.domain.beans.user.candidature.IndVoeu)
 	 */
 	public void addIndVoeu(final IndVoeu voeu) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addVoeu( " + voeu + " )");
 		}
 		addObject(voeu);
 
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#deleteIndVoeu(
 	 * org.esupportail.opi.domain.beans.user.candidature.IndVoeu)
 	 */
 	@Override
 	public void deleteIndVoeu(final IndVoeu voeu) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering deleteIndVoeu( " + voeu + " )");
 		}
 		deleteObject(voeu);
 
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#updateIndVoeu(
 	 * org.esupportail.opi.domain.beans.user.candidature.IndVoeu)
 	 */
 	public void updateIndVoeu(final IndVoeu voeu) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateIndVoeu( " + voeu + " )");
 		}		
 		updateObject(voeu);
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#updateIndVoeuTemEnServ(
 	 * org.esupportail.opi.domain.beans.references.commission.LinkTrtCmiCamp, boolean)
 	 */
 	public void updateIndVoeuTemEnServ(final LinkTrtCmiCamp link, final boolean temEnServ) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateIndVoeuTemEnServ( " + link + ", " + temEnServ + ")");
 		}
 		String condition = null;
 //		if (ind != null && link == null) {
 //			condition = "v.individu.id=" + ind.getId();
 //		} else if (link != null) {
 			condition = "v.linkTrtCmiCamp.id=" + link.getId();
 //		}
 		String hqlQuery = HqlUtils.updateWhere("IndVoeu as v", "v.temoinEnService=" + temEnServ, condition);
 		if (log.isDebugEnabled()) {
 			log.debug("updateIndVoeuTemEnServ with the hqlQuery " + hqlQuery);
 		}
 		executeUpdate(hqlQuery);
 	}
 
 	//////////////////////////////////////////////////////////////
 	// Adresse
 	//////////////////////////////////////////////////////////////
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#addAdresse(
 	 * 			org.esupportail.opi.domain.beans.user.Adresse)
 	 */
 	public void addAdresse(final Adresse adresse) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addAdresse( " + adresse + " )");
 		}
 		addObject(adresse);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#updateAdresse(
 	 * 			org.esupportail.opi.domain.beans.user.Adresse)
 	 */
 	public void updateAdresse(final Adresse adresse) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateAdresse( " + adresse + " )");
 		}
 		updateObject(adresse);
 	}
 
 	//////////////////////////////////////////////////////////////
 	// VersionManager
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#getVersionManager()
 	 */
 	@SuppressWarnings("unchecked")
 	public VersionManager getVersionManager() {
 		DetachedCriteria criteria = DetachedCriteria.forClass(VersionManager.class);
 		criteria.addOrder(Order.asc(ID_ATTRIBUTE));
 		List<VersionManager> versionManagers;
 		try {
 			versionManagers = getHibernateTemplate().findByCriteria(criteria);
 		} catch (BadSqlGrammarException e) {
 			throw new UninitializedDatabaseException(
 					"your database is not initialized, please run 'ant init-data'", e);
 		}
 		if (versionManagers.isEmpty()) {
 			VersionManager versionManager = new VersionManager();
 			versionManager.setVersion(VersionningUtils.VERSION_0);
 			addObject(versionManager);
 			return versionManager;
 		}
 		return versionManagers.get(0);
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#updateVersionManager(
 	 * org.esupportail.opi.domain.beans.VersionManager)
 	 */
 	public void updateVersionManager(final VersionManager versionManager) {
 		updateObject(versionManager);
 	}
 
 
 	//////////////////////////////////////////////////////////////
 	// MissingPiece
 	//////////////////////////////////////////////////////////////
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getMissingPiece(
 	 * org.esupportail.opi.domain.beans.user.Individu,
 	 *  org.esupportail.opi.domain.beans.references.commission.Commission)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<MissingPiece> getMissingPiece(final Individu individu, final Commission cmi) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getMissingPiece( " + individu + ", " + cmi + " )");
 		}
 		DetachedCriteria criteria = DetachedCriteria.forClass(MissingPiece.class);
 		if (individu != null) {
 			criteria.add(Restrictions.eq("individu", individu));
 		}
 		if (cmi != null) {
 			criteria.add(Restrictions.eq("commission", cmi));
 		}
 
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#addMissingPiece(
 	 * org.esupportail.opi.domain.beans.user.candidature.MissingPiece)
 	 */
 	@Override
 	public void addMissingPiece(final MissingPiece missingPiece) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addMissingPiece( " + missingPiece + " )");
 		}
 		addObject(missingPiece);
 
 	}
 
 
 	
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#deleteMissingPiece(
 	 * java.util.List, org.esupportail.opi.domain.beans.parameters.PieceJustificative)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void deleteMissingPiece(final List<MissingPiece> missingPiece, final PieceJustificative piece) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering deleteMissingPiece( " + missingPiece + ", " + piece + " )");
 		}
 		if (piece != null) {
 			DetachedCriteria crit = DetachedCriteria.forClass(MissingPiece.class);
 			crit.add(Restrictions.eq("piece", piece));
 			List<MissingPiece> l = getHibernateTemplate().findByCriteria(crit);
 			deleteObjects(l);
 		}
 		
 		if (missingPiece != null) {
 			deleteObjects(missingPiece);
 		}
 
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#updateMissingPiece(
 	 * org.esupportail.opi.domain.beans.user.candidature.MissingPiece)
 	 */
 	@Override
 	public void updateMissingPiece(final MissingPiece missingPiece) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateMissingPiece( " + missingPiece + " )");
 		}
 		updateObject(missingPiece);
 
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#purgeMissingPieceCamp(
 	 * org.esupportail.opi.domain.beans.parameters.Campagne)
 	 */
 	public void purgeMissingPieceCamp(final Campagne camp) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering purgeMissingPieceCamp( " + camp + " )");
 		}
 		String selectQuery = HqlUtils.selectFromWhere("distinct i.id",
 				"Individu as i left join i.campagnes as c", "c.id in (" + camp.getId() + ")");
 		String condition = "m.individu.id in (" + selectQuery + ")";
 		String hqlQuery = HqlUtils.deleteWhere("MissingPiece m", condition);
 //		List<MissingPiece> miss = getHibernateTemplate().find(hqlQuery);
 		executeUpdate(hqlQuery);
 		
 		
 	}
 
 
 	//////////////////////////////////////////////////////////////
 	// Opinion
 	//////////////////////////////////////////////////////////////
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getAvis(
 	 * org.esupportail.opi.domain.beans.user.candidature.IndVoeu)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Avis> getAvis(final IndVoeu indVoeu) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getAvis( " + indVoeu + " )");
 		}
 		DetachedCriteria criteria = DetachedCriteria.forClass(Avis.class);
 		if (indVoeu != null) {
 			criteria.add(Restrictions.eq("indVoeu", indVoeu));
 		}
 
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 	
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getAvis(
 	 * org.esupportail.opi.domain.beans.user.candidature.IndVoeu)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Avis> getAvisByEtp(final String codEtp, final Integer codVrsVet) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getAvisByEtp( " + codEtp + " , " + codVrsVet + " )");
 		}
 		
 		DetachedCriteria criteria = DetachedCriteria.forClass(Avis.class, "a");
 		criteria.createAlias("a.indVoeu", "v")
 				.createAlias("v.linkTrtCmiCamp", "l")
 				.createAlias("l.traitementCmi", "t")
 				.add(Restrictions.eq("t.versionEtpOpi.codEtp", codEtp))
 				.add(Restrictions.eq("t.versionEtpOpi.codVrsVet", codVrsVet));
 
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#addAvis(
 	 * org.esupportail.opi.domain.beans.user.candidature.Avis)
 	 */
 	@Override
 	public void addAvis(final Avis avis) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addAvis( " + avis + " )");
 		}
 		addObject(avis);
 
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#updateAvis(
 	 * org.esupportail.opi.domain.beans.user.candidature.Avis)
 	 */
 	@Override
 	public void updateAvis(final Avis avis) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateAvis( " + avis + " )");
 		}
 		updateObject(avis);
 
 	}
 
 	//////////////////////////////////////////////////////////////
 	// RENDEZ VOUS
 	//////////////////////////////////////////////////////////////
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#addIndividuDate(
 	 * org.esupportail.opi.domain.beans.references.rendezvous.IndividuDate)
 	 */
 	public void addIndividuDate(final IndividuDate indDate) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		addObject(indDate);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#deleteIndividuDate(
 	 * org.esupportail.opi.domain.beans.references.rendezvous.IndividuDate)
 	 */
 	public void deleteIndividuDate(final IndividuDate indDate) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		deleteObject(indDate);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#updateIndividuDate(
 	 * org.esupportail.opi.domain.beans.references.rendezvous.IndividuDate)
 	 */
 	public void updateIndividuDate(final IndividuDate indDate) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateIndividuDate indDate  "  + indDate);
 		}
 		updateObject(indDate);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getIndividusDate(
 	 * org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<IndividuDate> getIndividusDate(final CalendarRDV calRdv) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividusDate calRdv  "  + calRdv);
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(IndividuDate.class);
 		criteria.add(Restrictions.eq("calendrierRdv", calRdv));
 
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getIndividuDate(
 	 * org.esupportail.opi.domain.beans.user.candidature.IndVoeu)
 	 */
 	@SuppressWarnings("unchecked")
 	public IndividuDate getIndividuDate(final IndVoeu indVoeu) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndividuDate indVoeu : " + indVoeu);
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(IndividuDate.class);
 		criteria.add(Restrictions.eq("voeu", indVoeu));
 
 		List<IndividuDate> individus = getHibernateTemplate().findByCriteria(criteria);
 		if (individus != null && !individus.isEmpty()) {
 			return individus.get(0);
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * deleteAllIndividuDate(org.esupportail.opi.domain.beans.user.Individu)
 	 */
 	public void deleteAllIndividuDate(final Individu ind) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering deleteAllIndividuDate ind : " + ind);
 		}
 //		String listIdIndVoeu = "";
 //		int nbVoeu = 0;
 //		for (IndVoeu voeu : indVoeux) {
 //			nbVoeu++;
 //			listIdIndVoeu += voeu.getId();
 //			if (nbVoeu < indVoeux.size()) {
 //				listIdIndVoeu += ", ";
 //			}
 //		}
 		String hqlQuery = HqlUtils.deleteWhere("IndividuDate as d", "d.candidat.id = " + ind.getId());
 		if (log.isDebugEnabled()) {
 			log.debug("deleteAllIndividuDate with the hqlQuery " + hqlQuery);
 		}
 		executeUpdate(hqlQuery);
 		
 	}
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#purgeIndividuDateCamp(
 	 * org.esupportail.opi.domain.beans.parameters.Campagne)
 	 */
 	public void purgeIndividuDateCamp(final Campagne camp) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering purgeIndividuDateCamp( " + camp + " )");
 		}
 		String selectQuery = HqlUtils.selectFromWhere("distinct i.id",
 				"Individu as i left join i.campagnes as c", "c.id in (" + camp.getId() + ")");
 		String condition = "i.candidat.id in (" + selectQuery + ")";
 		String hqlQuery = HqlUtils.deleteWhere("IndividuDate i", condition);
 		executeUpdate(hqlQuery);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#addHoraire
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.Horaire)
 	 */
 	public void addHoraire(final Horaire ho) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addHoraire ho  "  + ho);
 		}
 		addObject(ho);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#updateHoraire
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.Horaire)
 	 */
 	public void updateHoraire(final Horaire ho) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		updateObject(ho);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#deleteHoraire
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.Horaire)
 	 */
 	public void deleteHoraire(final Horaire ho) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		deleteObject(ho);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#addTrancheFermee
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.TrancheFermee)
 	 */
 	public void addTrancheFermee(final TrancheFermee trFermee) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		addObject(trFermee);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#updateTrancheFermee
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.TrancheFermee)
 	 */
 	public void updateTrancheFermee(final TrancheFermee trFermee) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		updateObject(trFermee);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#deleteTrancheFermee
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.TrancheFermee)
 	 */
 	public void deleteTrancheFermee(final TrancheFermee trFermee) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		deleteObject(trFermee);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#addVetCalendar
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.VetCalendar)
 	 */
 	public void addVetCalendar(final VetCalendar vetCalendar) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		addObject(vetCalendar);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#updateVetCalendar
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.VetCalendar)
 	 */
 	public void updateVetCalendar(final VetCalendar vetCalendar) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		updateObject(vetCalendar);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#deleteVetCalendar
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.VetCalendar)
 	 */
 	public void deleteVetCalendar(final VetCalendar vetCalendar) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		deleteObject(vetCalendar);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#addJourHoraire
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.JourHoraire)
 	 */
 	public void addJourHoraire(final JourHoraire jourHoraire) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		addObject(jourHoraire);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#updateJourHoraire
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.JourHoraire)
 	 */
 	public void updateJourHoraire(final JourHoraire jourHoraire) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		updateObject(jourHoraire);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#deleteJourHoraire
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.JourHoraire)
 	 */
 	public void deleteJourHoraire(final JourHoraire jourHoraire) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		deleteObject(jourHoraire);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#refreshJourHoraire
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.JourHoraire)
 	 */
 	public void refreshJourHoraire(final JourHoraire jourHoraire) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 //		Session s = getHibernateTemplate().getSessionFactory().getCurrentSession();
 		getHibernateTemplate().refresh(jourHoraire);
 	}
 
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#refreshCalendarRdv
 	 * (org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV)
 	 */
 	public void refreshCalendarRdv(final CalendarRDV calendarRdv) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 //		Session s = getHibernateTemplate().getSessionFactory().getCurrentSession();
 		getHibernateTemplate().refresh(calendarRdv);
 	}
 
 
 	//////////////////////////////////////////////////////////////
 	// ARCHIVE TASK
 	//////////////////////////////////////////////////////////////
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * addArchiveTask(org.esupportail.opi.domain.beans.pilotage.ArchiveTask)
 	 */
 	@Override
 	public void addArchiveTask(final ArchiveTask archiveTask) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering -- addArchiveTask(" + archiveTask + ")");
 		}
 		addObject(archiveTask);
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getArchiveTasks()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<ArchiveTask> getArchiveTasks() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering -- getArchiveTasks()");
 		}
 		DetachedCriteria criteria = DetachedCriteria.forClass(ArchiveTask.class);
 
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 
 	
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#getArchiveTasksToExecute()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<ArchiveTask> getArchiveTasksToExecute() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering -- getArchiveTasksToExecute()");
 		}
 		DetachedCriteria criteria = DetachedCriteria.forClass(ArchiveTask.class);
 		criteria.add(Restrictions.eq("isExecuted", false));
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * updateArchiveTask(org.esupportail.opi.domain.beans.pilotage.ArchiveTask)
 	 */
 	@Override
 	public void updateArchiveTask(final ArchiveTask archiveTask) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering -- updateArchiveTask(" + archiveTask + ")");
 		}
 		updateObject(archiveTask);
 	}
 
 
 	/** 
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * deleteArchiveTask(org.esupportail.opi.domain.beans.pilotage.ArchiveTask)
 	 */
 	@Override
 	public void deleteArchiveTask(final ArchiveTask archiveTask) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering -- deleteArchiveTask(" + archiveTask + ")");
 		}
 		deleteObject(archiveTask);
 	}
 	
 	
 	//////////////////////////////////////////////////////////////
 	// AUTOMATION SUPPLEMENTARY LISTS
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * @param autoLp
 	 */
 	public void addAutoListPrincipale(final AutoListPrincipale autoLp) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		addObject(autoLp);
 	}
 
 	/**
 	 * @param autoLp
 	 */
 	public void updateAutoListPrincipale(final AutoListPrincipale autoLp) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		updateObject(autoLp);
 	}
 
 	/**
 	 * @param autoLp
 	 */
 	public void deleteAutoListPrincipale(final AutoListPrincipale autoLp) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		deleteObject(autoLp);
 	}
 	
 	/**
 	 * @param vet
 	 */
 	public void addVetAutoLp(final VetAutoLp vet) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		addObject(vet);
 	}
 
 	/**
 	 * @param vet
 	 */
 	public void updateVetAutoLp(final VetAutoLp vet) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		updateObject(vet);
 	}
 
 	/**
 	 * @param vet
 	 */
 	public void deleteVetAutoLp(final VetAutoLp vet) {
 		if (log.isDebugEnabled()) {
 			log.debug("");
 		}
 		deleteObject(vet);
 	}
 	
 	/**
 	 * 
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * getRecupListIndVoeuLc(org.esupportail.opi.domain.beans.parameters.TypeDecision,
 	 * 		org.esupportail.opi.domain.beans.user.candidature.VersionEtpOpi)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<IndVoeu> getRecupListIndVoeuLc(final TypeDecision typeDec,
 			final VersionEtpOpi versionEtpOpi) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering -- getRecupIndVoeuLc(" + typeDec + ", " + versionEtpOpi + ")");
 		}
 		
 		DetachedCriteria criteria = DetachedCriteria.forClass(IndVoeu.class, "i");
 		criteria.createAlias("i.linkTrtCmiCamp", "l");
 		criteria.createAlias("l.traitementCmi", "t");
 		criteria.createAlias("avis", "a");
 		criteria.add(Restrictions.eq("i.temoinEnService", true))
 			.add(Restrictions.eq("t.versionEtpOpi.codEtp", versionEtpOpi.getCodEtp()))
 			.add(Restrictions.eq("t.versionEtpOpi.codVrsVet", versionEtpOpi.getCodVrsVet()))
 			.add(Restrictions.eq("a.result.id", typeDec.getId()));
 		
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 
 
 	//////////////////////////////////////////////////////////////
 	// IndSituation
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * addIndSituation(org.esupportail.opi.domain.beans.user.situation.IndSituation)
 	 */
 	@Override
 	public void addIndSituation(final IndSituation indSituation) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering addIndSituation( " + indSituation + " )");
 		}
 		addObject(indSituation);		
 		
 	}
 
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * deleteIndSituation(org.esupportail.opi.domain.beans.user.situation.IndSituation)
 	 */
 	@Override
 	public void deleteIndSituation(final IndSituation indSituation) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering deleteIndSituation( " + indSituation + " )");
 		}
 		deleteObject(indSituation);
 	}
 
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * updateIndSituation(org.esupportail.opi.domain.beans.user.situation.IndSituation)
 	 */
 	@Override
 	public void updateIndSituation(final IndSituation indSituation) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering updateIndSituation( " + indSituation + " )");
 		}
 		updateObject(indSituation);
 		
 	}
 
 
 	/**
 	 * @see org.esupportail.opi.dao.DaoService#
 	 * getIndSituation(org.esupportail.opi.domain.beans.user.Individu)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public IndSituation getIndSituation(final Individu ind) {
 		if (log.isDebugEnabled()) {
 			log.debug("entering getIndSituation ind : " + ind);
 		}
 
 		DetachedCriteria criteria = DetachedCriteria.forClass(IndSituation.class);
 		criteria.add(Restrictions.eq("individu", ind));
 
 		List<IndSituation> indSitu = getHibernateTemplate().findByCriteria(criteria);
 		if (indSitu != null && !indSitu.isEmpty()) {
 			return indSitu.get(0);
 		}
 
 		return null;
 	
 	}
 }
 
