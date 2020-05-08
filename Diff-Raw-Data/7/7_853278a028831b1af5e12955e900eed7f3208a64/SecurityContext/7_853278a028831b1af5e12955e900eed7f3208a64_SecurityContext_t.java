 package fr.cg95.cvq.security;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.authority.LocalAuthority;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.service.authority.IAgentService;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.users.IUserSearchService;
 
 /**
  * A manager for the notions of "current everything" that
  * pertain to security. Here we store the current
  * {@link fr.cg95.cvq.business.authority.Agent}, the
  * current {@link fr.cg95.cvq.business.authority.LocalAuthority}, etc.
  *
  * @author dom@idealx.com
  * @author bor@zenexity.fr
  */
 public class SecurityContext {
 
     private static Logger logger = Logger.getLogger(SecurityContext.class);
 
     public static final String BACK_OFFICE_CONTEXT = "backOffice";
     public static final String FRONT_OFFICE_CONTEXT = "frontOffice";
     public static final String ADMIN_CONTEXT = "adminContext";
     public static final String EXTERNAL_SERVICE_CONTEXT = "externalServiceContext";
 
     private static ILocalAuthorityRegistry localAuthorityRegistry;
     private static IAgentService agentService;
     private static IUserSearchService userSearchService;
 
     private static List<String> administratorGroups;
     private static List<String> agentGroups;
 
     private static ThreadLocal<CredentialBean> currentContextThreadLocal =
         new InheritableThreadLocal<CredentialBean>();
     private static ThreadLocal<Stack<CredentialBean>> stashedContextThreadLocal =
           new InheritableThreadLocal<Stack<CredentialBean>>();
 
     public void init() throws CvqConfigurationException {
         if (agentGroups == null || agentGroups.isEmpty() 
             || administratorGroups == null || administratorGroups.isEmpty())
             throw new CvqConfigurationException("security.error.accessGroupsNotConfigured");
     }
 
     /**
      * Return whether one of the group in the given list permits access to the Back Office.
      */
     public static boolean isAuthorizedGroup(List<String> groupList) {
         for (String group : groupList) {
             if (administratorGroups.contains(group) || agentGroups.contains(group))
                 return true;
         }
 
         return false;
     }
 
     /**
      * Return whether at least one of the provided group is within the list of administrator
      * groups. 
      */
     public static boolean isOfAnAdminGroup(List<String> groupList) {
         for (String group : groupList) {
             if (administratorGroups.contains(group)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Return whether at least one of the provided group is within the list of agent
      * groups.
      */
     public static boolean isOfAnAgentGroup(List<String> groupList) {
         for (String group : groupList) {
             if (agentGroups.contains(group)) {
                return true;
             }
         }
 
         return false;
     }
 
     public static List<String> getAdministratorGroups() {
         return administratorGroups;
     }
 
     public static List<String> getAgentGroups() {
         return agentGroups;
     }
 
     /**
      * Return the user we are talking to (from the WWW session), or
      * null if we are not in a WWW or other user-driven context.
      *
      * @throws CvqException if security context is not initialized or if no agent is set in it
      *  or if it is not in the {@link #BACK_OFFICE_CONTEXT back office context}.
      */
     public static Agent getCurrentAgent() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return null;
 
         if (!credentialBean.isBoContext())
             return null;
 
         return credentialBean.getAgent();
     }
 
     /**
      * Set the current agent. Can only be called after the current
      * local authority is set with {@link #setCurrentSite}.
      */
     public static void setCurrentAgent(Agent agent)
         throws CvqException {
 
         logger.debug("setCurrentAgent() agent = " + agent);
 
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             throw new CvqException("security.error.siteMustBeSetBeforeAgent");
 
         if (!credentialBean.isBoContext())
             throw new CvqException("security.error.agentExistInBackOfficeContextonly");
 
         credentialBean.setAgent(agent);
     }
 
     /**
      * @see #setCurrentAgent(Agent)
      */
     public static void setCurrentAgent(String agentLogin)
         throws CvqException, CvqObjectNotFoundException {
 
         Agent agent = null;
         try {
             agent = agentService.getByLogin(agentLogin);
         } catch (CvqObjectNotFoundException confe) {
             throw confe;
         } catch (Exception e) {
             logger.error("setCurrentAgent() error while retrieving agent " + agentLogin);
             e.printStackTrace();
             throw new CvqObjectNotFoundException("Error while retrieving agent " + agentLogin);
         }
 
         setCurrentAgent(agent);
     }
 
     /**
      * Return the agent who is doing a request on behalf of an ecitizen in
      * {@link #FRONT_OFFICE_CONTEXT front office context}
      */
     public static Agent getProxyAgent() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return null;
 
         return credentialBean.getProxyAgent();
     }
 
     /**
      * Set the current agent who is doing a request on behalf of an ecitizen.
      */
     public static void setProxyAgent(Agent proxyAgent)
         throws CvqException {
 
         logger.debug("setProxyAgent() proxy agent = " + proxyAgent);
 
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             throw new CvqException("security.error.siteMustBeSetBeforeAgent");
 
         if (!credentialBean.isFoContext())
             throw new CvqException("security.error.proxyAgentExistInFrontOfficeContextOnly");
 
         credentialBean.setProxyAgent(proxyAgent);
     }
 
     /**
      * @see #setProxyAgent(Agent)
      */
     public static void setProxyAgent(String proxyAgentLogin)
         throws CvqException, CvqObjectNotFoundException {
 
         Agent proxyAgent = null;
         try {
             proxyAgent = agentService.getByLogin(proxyAgentLogin);
         } catch (CvqObjectNotFoundException confe) {
             throw confe;
         } catch (Exception e) {
             logger.error("setProxyAgent() error while retrieving proxy agent " + proxyAgentLogin);
             e.printStackTrace();
             throw new CvqException("Error while retrieving proxy agent " + proxyAgentLogin);
         }
 
         setProxyAgent(proxyAgent);
     }
 
     /**
      * Return the user we are talking to (from the WWW session), or
      * null if we are not in a WWW or other user-driven context.
      *
      * @throws CvqException if security context is not initialized or if no ecitizen is set in it
      *  or if it is not in the {@link #FRONT_OFFICE_CONTEXT front office context}.
      */
     public static Adult getCurrentEcitizen() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null) {
             logger.warn("getCurrentEcitizen() no user yet in security context, returning null");
             return null;
         }
 
         if (!credentialBean.isFoContext()) {
             logger.warn("getCurrentEcitizen() ecitizen only exists in Front Office context, returning null");
             return null;
         }
 
         return credentialBean.getEcitizen();
     }
 
     /**
      * Set the current ecitizen. Can only be called after the current
      * local authority is set with {@link #setCurrentSite}.
      */
     public static void setCurrentEcitizen(Adult adult)
         throws CvqException {
 
         logger.debug("setCurrentEcitizen() adult = " + adult);
 
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             throw new CvqException("setCurrentSite() has to be called before setCurrentEcitizen()");
 
         if (!credentialBean.isFoContext())
             throw new CvqException("Adult can only be set in Front Office context");
 
         credentialBean.setEcitizen(adult);
     }
 
     /**
      * @see #setCurrentEcitizen(Adult)
      */
     public static void setCurrentEcitizen(Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         logger.debug("setCurrentEcitizen() id = " + id);
         String context = getCurrentContext();
         setCurrentContext(SecurityContext.ADMIN_CONTEXT);
         Adult adult = userSearchService.getAdultById(id);
         setCurrentContext(context);
         if (adult == null)
             throw new CvqObjectNotFoundException("Adult not found !");
         setCurrentEcitizen(adult);
     }
 
     public static String getCurrentExternalService() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null) {
             logger.debug("getCurrentExternalService() No user yet in security context");
             return null;
         }
 
         if (!credentialBean.isExternalServiceContext()) {
             logger.debug("getCurrentExternalService() external service only exists in External Service context");
             return null;
         }
 
         return credentialBean.getExternalService();
     }
 
     public static void setCurrentExternalService(String externalService) throws CvqException {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             throw new CvqException("setCurrentSite() has to be called before setCurrentEcitizen()");
 
         if (!credentialBean.isExternalServiceContext())
             throw new CvqException("External service can only be set in External Service context");
 
         credentialBean.setExternalService(externalService);
     }
 
     /**
      * Return the login of the current user ("administrator" if admin context,
      * whether it is an agent or an e-citizen.
      *
      * @throws CvqException if security context is not initialized
      */
     public static String getCurrentUserLogin() {
 
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return null;
 
         if (credentialBean.isFoContext()) {
             return credentialBean.getEcitizen() == null ? "" : credentialBean.getEcitizen().getLogin();
         } else if (credentialBean.isBoContext()) {
             return credentialBean.getAgent() == null ? "" : credentialBean.getAgent().getLogin();
         } else if (credentialBean.isExternalServiceContext()) {
             return credentialBean.getExternalService();
         } else {
             return "administrator";
         }
     }
 
     /**
      * Return the id of the current user (-1 if admin context), whether it is an agent
      * or an e-citizen.
      *
      * @throws CvqException if security context is not initialized
      */
     public static Long getCurrentUserId() {
 
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return null;
 
         if (credentialBean.isFoContext()) {
             if (credentialBean.getProxyAgent() != null)
                 return credentialBean.getProxyAgent().getId();
             else
                 return credentialBean.getEcitizen() == null ? null : credentialBean.getEcitizen().getId();
         } else if (credentialBean.isBoContext()) {
             return credentialBean.getAgent() == null ? null : credentialBean.getAgent().getId();
         } else {
             return Long.valueOf("-1");
         }
     }
 
     /**
      * Return the current context for this thread or null if security context is not set.
      *
      * @see #BACK_OFFICE_CONTEXT
      * @see #FRONT_OFFICE_CONTEXT
      * @see #ADMIN_CONTEXT
      * @see #EXTERNAL_SERVICE_CONTEXT
      */
     public static String getCurrentContext() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return null;
         else if (credentialBean.isBoContext())
             return BACK_OFFICE_CONTEXT;
         else if (credentialBean.isFoContext())
             return FRONT_OFFICE_CONTEXT;
         else if (credentialBean.isAdminContext())
             return ADMIN_CONTEXT;
         else if (credentialBean.isExternalServiceContext())
             return EXTERNAL_SERVICE_CONTEXT;
 
         return null;
     }
 
     public static void setCurrentContext(final String context)
         throws CvqException {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             throw new CvqException("Context cannot be changed if not set");
         else
             credentialBean.setContext(context);
     }
 
     public static void pushContext() {
         CredentialBean credentialBean = (CredentialBean) currentContextThreadLocal.get().clone();
        if (stashedContextThreadLocal.get() == null)
            stashedContextThreadLocal.set(new Stack<CredentialBean>());
         stashedContextThreadLocal.get().push(credentialBean);
     }
 
     public static void popContext() {
         CredentialBean credentialBean = stashedContextThreadLocal.get().pop();
         currentContextThreadLocal.set(credentialBean);
     }
 
     /**
      * Return the current local authority for this thread or null if security context is not set.
      */
     public static LocalAuthority getCurrentSite() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return null;
         else
             return credentialBean.getSite();
     }
 
     public static void setCurrentSite(LocalAuthority localAuthority, String context) {
         logger.debug("setCurrentSite() site = " + localAuthority.getName() + ", context = " + context);
 
         CredentialBean credentialBean = new CredentialBean(localAuthority, context);
         currentContextThreadLocal.set(credentialBean);
     }
 
     /**
      * Set the current local authority. To be called by a sort of multi-site listener.
      *
      * @param context the context to set, as defined by
      *  {@link #BACK_OFFICE_CONTEXT}, {@link #FRONT_OFFICE_CONTEXT}, {@link #ADMIN_CONTEXT},
      *  and {@link #EXTERNAL_SERVICE_CONTEXT}
      */
     public static void setCurrentSite(String localAuthorityName, String context)
         throws CvqObjectNotFoundException {
 
         LocalAuthority localAuthority = 
             localAuthorityRegistry.getLocalAuthorityByName(localAuthorityName);
         if (localAuthority == null) {
             logger.error("setCurrentSite() local authority " + localAuthorityName + " not found in DB");
             throw new CvqObjectNotFoundException("local authority " + localAuthorityName + " not found in DB");
         }
 
         setCurrentSite(localAuthority, context);
     }
 
     /**
      * Reset the current security context. To be called at the end of the transaction.
      */
     public static void resetCurrentSite() {
         logger.debug("resetCurrentSite()");
         currentContextThreadLocal.set(null);
     }
 
     public static void setCurrentLocale(Locale locale) throws CvqException {
         logger.debug("setCurrentLocale() locale = " + locale);
 
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             throw new CvqException("setCurrentSite() has to be called before setCurrentLocale()");
 
         credentialBean.setLocale(locale);
     }
 
     /**
      * Return the locale for the current thread.
      */
     public static Locale getCurrentLocale() throws CvqException {
 
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             throw new CvqException("No locale yet in security context");
 
         return credentialBean.getLocale();
     }
 
     /**
      * Return the configuration bean for the current thread.
      */
     public static LocalAuthorityConfigurationBean getCurrentConfigurationBean() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return null;
         String localAuthorityName = credentialBean.getSite().getName();
         return localAuthorityRegistry.getLocalAuthorityBeanByName(localAuthorityName);
     }
 
     /**
      * Returns the current CredentialBean. Only available when both
      * {@link #setCurrentSite} and {@link #setCurrentEcitizen} have been
      * called first.
      *
      * @return null if any of the above two conditions is not met. In this
      * mode, the only accesses the security package will allow is read
      * access on the Users and Sites themselves (in order to bootstrap
      * the CredentialBean).
      */
     public static CredentialBean getCurrentCredentialBean() {
         return currentContextThreadLocal.get();
     }
 
     public static boolean isBackOfficeContext() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return false;
         else
             return credentialBean.isBoContext();
     }
 
     public static boolean isAdminContext() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return false;
         else
             return credentialBean.isAdminContext();
     }
 
     public static boolean isFrontOfficeContext() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return false;
         else
             return credentialBean.isFoContext();
     }
 
     public static boolean isExternalServiceContext() {
         CredentialBean credentialBean = currentContextThreadLocal.get();
         if (credentialBean == null)
             return false;
         else
             return credentialBean.isExternalServiceContext();
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry iLocalAuthorityRegistry) {
         localAuthorityRegistry = iLocalAuthorityRegistry;
     }
 
     public void setAgentService(IAgentService iAgentService) {
         agentService = iAgentService;
     }
 
     public void setUserSearchService(IUserSearchService userSearchService) {
         SecurityContext.userSearchService = userSearchService;
     }
 
     public void setAdministratorGroups(List<String> administratorGroupsToSet) {
         administratorGroups = administratorGroupsToSet;
     }
 
     public void setAgentGroups(List<String> agentGroupsToSet) {
         agentGroups = agentGroupsToSet;
     }
 }
