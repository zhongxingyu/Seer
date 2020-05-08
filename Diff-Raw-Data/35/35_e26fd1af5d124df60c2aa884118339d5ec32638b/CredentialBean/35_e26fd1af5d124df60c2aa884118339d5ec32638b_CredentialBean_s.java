 package fr.cg95.cvq.security;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.authority.Category;
 import fr.cg95.cvq.business.authority.CategoryProfile;
 import fr.cg95.cvq.business.authority.CategoryRoles;
 import fr.cg95.cvq.business.authority.LocalAuthority;
 import fr.cg95.cvq.business.authority.SiteProfile;
 import fr.cg95.cvq.business.authority.SiteRoles;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 
 /**
  * A data structure / cache that stores all the credentials necessary when
  * computing access privileges against a given User. This CredentialBean
  * is passed around across all access checks done for a given user.
  * Most of the methods here memorize (cache) their results for speed, and
  * also in order to break some possible loops.
  *
  * In order to speed up the security computations even more, as
  * well as allowing sophisticated use-cases (bypassing privileges,
  * allowing saving then reloading an object to the database without
  * having to enforce extra checks, etc.), the CredentialBean maintains
  * an <strong>ObjectBean cache</strong> that has the same lifetime as
  * the CredentialBean itself (typically, one transaction).
  *
  * @author dom@idealx.com
  */
 public class CredentialBean {
 
     private static Logger logger = Logger.getLogger(CredentialBean.class);
 
     private Agent agent;
     private Adult adult;
     private String externalService;
 
     private LocalAuthority localAuthority;
 
     private Locale locale;
     
     private boolean boContext;
     private boolean foContext;
     private boolean adminContext;
 
     /**
      * Used to keep trace of current agent's site roles.
      */
     private SiteRoles[] siteRoles = null;
 
     /**
      * Used to keep trace of current agent's category roles.
      */
     private CategoryRoles[] categoryRoles = null;
 
     /**
      * Used to keep trace of current citizen's "managed" individuals.
      */
     private Set<IndividualRole> individualRoles = null;
     
     /**
      * Used to keep track of individuals belonging to ecitizen's home folder.
      */
     private Set<Long> individualsIds = null;
     
     public CredentialBean(LocalAuthority localAuthority, String context) {
         this.localAuthority = localAuthority;
         if (context.equals(SecurityContext.BACK_OFFICE_CONTEXT)) {
             boContext = true;
             foContext = false;
             adminContext = false;
         } else if (context.equals(SecurityContext.FRONT_OFFICE_CONTEXT)) {
             boContext = false;
             foContext = true;
             adminContext = false;
         } else if (context.equals(SecurityContext.ADMIN_CONTEXT)) {
             boContext = false;
             foContext = false;
             adminContext = true;
         }
     }
     
     public boolean isFoContext() {
         return foContext;
     }
 
     public boolean isBoContext() {
         return boContext;
     }
 
     public boolean isAdminContext() {
         return adminContext;
     }
     
     public void setContext(final String context) {
 		if (context.equals(SecurityContext.BACK_OFFICE_CONTEXT)) {
 			boContext = true;
 			foContext = false;
 			adminContext = false;
 		} else if (context.equals(SecurityContext.FRONT_OFFICE_CONTEXT)) {
 			boContext = false;
 			foContext = true;
 			adminContext = false;
 		} else {
 			boContext = false;
 			foContext = false;
 			adminContext = true;
 		}
     }
     
     private void resetCaches() {
 		objectBeanCache.clear();
 		siteRoles = null;
 		categoryRoles = null;
 		individualRoles = null;
 		individualsIds = null;
     }
     
     public Agent getAgent() {
         return agent;
     }
 
     public void setAgent(Agent agent) {
 		this.agent = agent;
 
 		// in case we are changing of user inside a transaction, reset the cache
 		// (only happens during unit tests)
 		resetCaches();
 		
 		getSiteRoles();
 		getCategoryRoles();
     }
 
 	public Adult getEcitizen() {
         return adult;
     }
 
 	public void setEcitizen(Adult adult) {
 		this.adult = adult;
 		
 		// in case we are changing of user inside a transaction, reset the cache
 		resetCaches();
 
 		this.individualRoles = adult.getIndividualRoles();
 	        
 		this.individualsIds = new HashSet<Long>();
 		List<Individual> individuals = adult.getHomeFolder().getIndividuals();
 		for (Individual individual : individuals) {
 		    this.individualsIds.add(individual.getId());
 		}
 	}
 	
     public String getExternalService() {
         return externalService;
     }
 
     public void setExternalService(String externalService) {
         this.externalService = externalService;
         
         // in case we are changing of user inside a transaction, reset the cache
         resetCaches();        
     }
 
     public LocalAuthority getSite() {
         return localAuthority;
     }
 
     /**
      * Return the locale for the current thread. Defaults to Locale.FRANCE if none set.
      */
     public final Locale getLocale() {
         if (locale != null)
             return locale;
         else
             return Locale.FRANCE;
     }
 
     public final void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     /**
      * Returns the array of site-scoped roles the user in the bean belongs to.
      */
     public SiteRoles[] getSiteRoles() {
         if (agent == null) {
             logger.warn("getSiteRoles() no agent");
             return new SiteRoles[0];
         }
 
         if (siteRoles == null) {
             Set<SiteRoles> siteRolesSet = agent.getSitesRoles();
             siteRoles = new SiteRoles[siteRolesSet.size()];
             int i = 0;
             for (SiteRoles sr : siteRolesSet) {
                 siteRoles[i] = sr;
                 i++;
             }
         }
 
         return siteRoles;
     }
 
     public boolean hasSiteAdminRole() {
         if (agent == null) {
             logger.warn("hasSiteAdminRole() no agent");
             return false;
         }
         
         if (siteRoles == null)
             getSiteRoles();
         
         for (SiteRoles siteRole : siteRoles) {
             if (siteRole.getProfile().equals(SiteProfile.ADMIN))
                 return true;
         }
         
         return false;
     }
     
     public boolean hasSiteAgentRole() {
         if (agent == null) {
             logger.warn("hasSiteAdminRole() no agent");
             return false;
         }
         
         if (siteRoles == null)
             getSiteRoles();
         
         for (SiteRoles siteRole : siteRoles) {
             if (siteRole.getProfile().equals(SiteProfile.AGENT))
                 return true;
         }
         
         return false;        
     }
     
     /**
      * Returns the array of category-scoped roles the user in the bean
      * belongs to.
      */
     public CategoryRoles[] getCategoryRoles() {
         if (agent == null) {
             logger.info("getCategoryRoles() no agent");
             return new CategoryRoles[0];
         }
 
         if (categoryRoles == null) {
             Set<CategoryRoles> categoryRolesSet = agent.getCategoriesRoles();
             if (categoryRolesSet != null) {
                 categoryRoles = new CategoryRoles[categoryRolesSet.size()];
                 int i = 0;
                 for (CategoryRoles cr : categoryRolesSet) {
                     categoryRoles[i] = cr;
                     i++;
                 }
             }
         }
 
         return categoryRoles;
     }
 
     public CategoryProfile getProfileForCategory(Category category) {
         CategoryRoles[] categoryRoles = getCategoryRoles();
         for (CategoryRoles cr : categoryRoles) {
             if (cr.getCategory().getId().equals(category.getId()))
                 return cr.getProfile();
         }
         return null;
     }
 
     public Set<IndividualRole> getIndividualsRoles() {
         return individualRoles;
     }
 
     /**
      * Return current user's roles on his home folder (and not on individuals).
      */
     public Set<IndividualRole> getHomeFolderRoles() {
         Set<IndividualRole> homeFolderRoles = new HashSet<IndividualRole>();
         for (IndividualRole individualRole : individualRoles) {
             if (individualRole.getHomeFolderId() != null)
                 homeFolderRoles.add(individualRole);
         }
         
         return homeFolderRoles;
     }
     
     /**
      * Return current user's roles on his home folder's individuals.
      */
     public Set<IndividualRole> getIndividualRoles(final Long individualId) {
         Set<IndividualRole> roles = new HashSet<IndividualRole>();
         for (IndividualRole individualRole : individualRoles) {
             if (individualRole.getIndividualId() != null 
                     && individualRole.getIndividualId().equals(individualId))
                 roles.add(individualRole);
         }
         
         return roles;
     }
 
     /**
      * Return whether given individual belongs to same home folder
      * than currently logged-in ecitizen.
      */
     public boolean belongsToSameHomeFolder(final Long individualId) {
         if (this.individualsIds == null || this.individualsIds.isEmpty())
             return false;
         
         return this.individualsIds.contains(individualId);
     }
     
     /* ==================== Grants cache API =========================== */
 
     private ArrayList<ObjectBean> objectBeanCache = new ArrayList<ObjectBean>();
 
     /**
      * Returns a fresh {@link ObjectBean}, passing parameters
      * <code>object</code> and <code>baseClass</code> to the
      * constructor of this class, or a not-so-fresh ObjectBean from the cache
      * (if <code>getObjectBean()</code> was previously called with the
      * same object and base class).
      */
     public ObjectBean getObjectBean(Object object, Class<?> baseClass) {
 
         for (ObjectBean bean : objectBeanCache) {
 
             if (object.equals(bean.getObject()) &&
                 baseClass.equals(bean.getBaseClass())) {
             		logger.debug("getObjectBean() returning cached object bean");
             		return bean;
             }
         }
 
         ObjectBean bean = new ObjectBean(object, baseClass);
         objectBeanCache.add(bean);
         return bean;
     }
 }
