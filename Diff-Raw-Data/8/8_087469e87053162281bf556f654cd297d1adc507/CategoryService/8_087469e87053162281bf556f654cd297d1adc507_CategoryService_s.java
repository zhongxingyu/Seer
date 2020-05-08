 package fr.cg95.cvq.service.request.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.authority.LocalAuthority;
 import fr.cg95.cvq.business.request.Category;
 import fr.cg95.cvq.business.request.CategoryProfile;
 import fr.cg95.cvq.business.request.CategoryRoles;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.dao.request.ICategoryDAO;
 import fr.cg95.cvq.dao.request.IRequestTypeDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.IAgentService;
 import fr.cg95.cvq.service.authority.ILocalAuthorityLifecycleAware;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.impl.LocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.ICategoryService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 
 /**
  * Implementation of the {@link ICategoryService category service}.
  *
  * @author bor@zenexity.fr
  */
 public class CategoryService implements ICategoryService, ILocalAuthorityLifecycleAware {
 
     static Logger logger = Logger.getLogger(CategoryService.class);
 
     private ICategoryDAO categoryDAO;
     private IRequestTypeDAO requestTypeDAO;
     private IRequestServiceRegistry requestServiceRegistry;
     private ILocalAuthorityRegistry localAuthorityRegistry;
 
     private IAgentService agentService;
     
     @Override
     @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public Category getById(final Long id) {
         return categoryDAO.findById(id);
     }
 
     private Category getByName(final String name) {
         return categoryDAO.findByName(name);
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public List<Category> getAll() {
         
         if (SecurityContext.isAdminContext())
             return categoryDAO.listAll();
         
         // if agent is an admin, return the whole list
         if (SecurityContext.getCurrentCredentialBean().hasSiteAdminRole())
             return categoryDAO.listAll();
         
         // else only return those categories it has a role on
         return categoryDAO.listByAgent(SecurityContext.getCurrentAgent().getId(), null);
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public List<Category> getManaged() {
         return categoryDAO.listByAgent(SecurityContext.getCurrentUserId(), CategoryProfile.MANAGER);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.NONE)
     public List<Category> getAssociated() {
         return categoryDAO.listByAgent(SecurityContext.getCurrentUserId(), null);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public List<Category> getAgentCategories(final Long agentId) {
         return categoryDAO.listByAgent(agentId, null);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public Long create(final Category category)
         throws CvqException, CvqModelException {
 
         if (category == null)
             throw new CvqException("No Category object provided !");
 
         // check there is not yet a category with the same name
         if (getByName(category.getName()) != null) {
             logger.error("create() there is already a category with name : " + category.getName());
             throw new CvqModelException("category.error.nameAlreadyExists");
         }
 
         Long categoryId = categoryDAO.create(category).getId();
 
         logger.debug("create() created category object with id : " + categoryId);
 
         return categoryId;
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public void modify(final Category category) {
         // FIXME : check the new name does not conflit with an existing one
         if (category != null)
             categoryDAO.update(category);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public void delete(final Long id) {
 
         logger.debug("delete() gonna delete category object with id : " + id);
 
         Category category = categoryDAO.findById(id);
         if (category.getRequestTypes() != null) {
             for (RequestType requestType : category.getRequestTypes()) {
                 requestType.setCategory(null);
             }
             category.setRequestTypes(null);
         }
 
         categoryDAO.delete(category);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public List<Agent> getAuthorizedForCategory(Long categoryId) {
 
         List<Agent> agentsList = new ArrayList<Agent>();
         Category category = getById(categoryId);
         for (CategoryRoles categoryRoles : category.getCategoriesRoles()) {
             agentsList.add(agentService.getById(categoryRoles.getAgentId()));
         }
 
         return agentsList;
     }
 
     @Override
    @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public boolean hasProfileOnCategory(Agent agent, Long categoryId) {
 
        if (categoryId == null)
             return false;
        
         Category category = getById(categoryId);
         for (CategoryRoles categoryRoles : category.getCategoriesRoles()) {
             if (categoryRoles.getAgentId().equals(agent.getId()))
                 return true;
         }
 
         return false;
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public boolean hasWriteProfileOnCategory(Agent agent, Long categoryId) {
         
         if (categoryId == null)
             return false;
 
         Category category = getById(categoryId);
         for (CategoryRoles categoryRole : category.getCategoriesRoles()) {
             if (categoryRole.getAgentId().equals(agent.getId())
                 && (categoryRole.getProfile().equals(CategoryProfile.MANAGER)
                 || categoryRole.getProfile().equals(CategoryProfile.READ_WRITE )))
                 return true;
         }
 
         return false;        
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public boolean hasManagerProfile(Agent agent) {
         for(Category category : getAll()) {
             for (CategoryRoles role : category.getCategoriesRoles()) {
                 if (role.getAgentId().equals(agent.getId())
                         && CategoryProfile.MANAGER.equals(role.getProfile()))
                     return true;
             }
         }
         return false;
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public CategoryProfile getProfileForCategory(final Long categoryId) {
         return getProfileForCategory(SecurityContext.getCurrentAgent().getId(), categoryId);
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public CategoryProfile getProfileForCategory(final Long agentId, final Long categoryId) {
         Category category = getById(categoryId);
         for (CategoryRoles categoryRoles : category.getCategoriesRoles()) {
             if (categoryRoles.getAgentId().equals(agentId))
                 return categoryRoles.getProfile();
         }
 
         return null;
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public Category addRequestType(Long categoryId, Long requestTypeId) {
 
         Category category = categoryDAO.findById(categoryId);
         RequestType requestType = requestTypeDAO.findById(requestTypeId);
         requestType.setCategory(category);
         if (category.getRequestTypes() == null)
             category.setRequestTypes(new HashSet<RequestType>());
         category.getRequestTypes().add(requestType);
 
         categoryDAO.update(category);
         return category;
    }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public Category removeRequestType(Long categoryId, Long requestTypeId) {
 
         Category category = getById(categoryId);
         RequestType requestType = requestTypeDAO.findById(requestTypeId);
         requestType.setCategory(null);
         category.getRequestTypes().remove(requestType);
 
         categoryDAO.update(category);
         return category;
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public void addCategoryRole(final Long agentId, final  Long categoryId,
             final CategoryProfile categoryProfile ) throws CvqException {
 
         if (agentId == null)
             throw new CvqException("No agent id provided");
         Category category = getById(categoryId);
 
         CategoryRoles categoryRoles = new CategoryRoles();
         categoryRoles.setAgentId(agentId);
         categoryRoles.setCategory(category);
         categoryRoles.setProfile(categoryProfile);
         category.addCategoryRole(categoryRoles);
 
         modify(category);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public void modifyCategoryRole(final Long agentId, final  Long categoryId,
             final CategoryProfile categoryProfile ) throws CvqException {
 
         if (agentId == null)
             throw new CvqException("No agent id provided");
         Category category = getById(categoryId);
 
         boolean foundCategoryRole = false;
         for (CategoryRoles categoryRoles : category.getCategoriesRoles()) {
             if (categoryRoles.getAgentId().equals(agentId)) {
                 categoryRoles.setProfile(categoryProfile);
                 foundCategoryRole = true;
                 break;
             }
         }
         if (!foundCategoryRole) {
             CategoryRoles categoryRoles = new CategoryRoles();
             categoryRoles.setAgentId(agentId);
             categoryRoles.setCategory(getById(categoryId));
             categoryRoles.setProfile(categoryProfile);
             category.getCategoriesRoles().add(categoryRoles);
         }
 
         modify(category);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.NONE)
     public void removeCategoryRole(final Long agentId, final  Long categoryId) throws CvqException {
 
         if (agentId == null)
             throw new CvqException("No agent id provided");
         Category category = getById(categoryId);
 
         boolean foundCategoryRole = false;
         for (CategoryRoles categoryRoles : category.getCategoriesRoles()) {
             if (categoryRoles.getAgentId().equals(agentId)) {
                 category.getCategoriesRoles().remove(categoryRoles);
                 foundCategoryRole = true;
                 break;
             }
         }
         if (foundCategoryRole)
             modify(category);
     }
 
     public void setCategoryDAO(ICategoryDAO categoryDAO) {
         this.categoryDAO = categoryDAO;
     }
 
     public void setRequestTypeDAO(IRequestTypeDAO requestTypeDAO) {
         this.requestTypeDAO = requestTypeDAO;
     }
 
     public void setAgentService(IAgentService agentService) {
         this.agentService = agentService;
     }
 
     @Override
     @Context(types = {ContextType.SUPER_ADMIN})
     public void addLocalAuthority(String localAuthorityName) {
         LocalAuthority localAuthority =
             localAuthorityRegistry.getLocalAuthorityByName(localAuthorityName);
         String adminEmail = localAuthority.getAdminEmail();
         logger.debug("bootstraping categories");
         if (getAll().size() > 0) {
             logger.debug("some categories already exist, returning");
             return;
         }
         // reuse existing defaultDisplayGroup label
         Map<String, Category> categories = new HashMap<String, Category>();
         categories.put("civil", new Category("État civil", adminEmail));
         categories.put("urbanism", new Category("Urbanisme", adminEmail));
         categories.put("accounts", new Category("Gestion des comptes", adminEmail));
         categories.put("school", new Category("Scolaire", adminEmail));
         categories.put("environment", new Category("Environnement", adminEmail));
         categories.put("social", new Category("Social", adminEmail));
         categories.put("leisure", new Category("Loisirs", adminEmail));
         categories.put("technical", new Category("Service technique", adminEmail));
         categories.put("security", new Category("Sécurité", adminEmail));
         try {
             Long agentId = null;
             Long managerId = null;
             if (LocalAuthorityRegistry.DEVELOPMENT_LOCAL_AUTHORITY.equals(localAuthorityName)) {
                 agentId =
                     agentService.getByLogin(LocalAuthorityRegistry.DEVELOPMENT_AGENT_NAME).getId();
                 managerId = agentService.getByLogin(LocalAuthorityRegistry.DEVELOPMENT_MANAGER_NAME)
                     .getId();
             }
             for (Category c : categories.values()) {
                 create(c);
                 if (LocalAuthorityRegistry.DEVELOPMENT_LOCAL_AUTHORITY.equals(localAuthorityName)) {
                     addCategoryRole(agentId, c.getId(), CategoryProfile.READ_WRITE);
                     addCategoryRole(managerId, c.getId(), CategoryProfile.MANAGER);
                 }
             }
             // those display groups are merged in the same category
             categories.put("election", categories.get("civil"));
             categories.put("culture", categories.get("leisure"));
             for (RequestType rt : requestTypeDAO.listAll()) {
                 IRequestService service = requestServiceRegistry.getRequestService(rt.getLabel());
                 Category c =  categories.get(service.getDefaultDisplayGroup());
                 if (c != null)
                     addRequestType(c.getId(), rt.getId());
                 else
                     addRequestType(categories.get("accounts").getId(), rt.getId());
             }
         } catch (CvqException cvqe) {
             logger.equals("Display Group init failed !");
             cvqe.printStackTrace();
         }
     }
 
     @Override
     @Context(types = {ContextType.SUPER_ADMIN})
     public void removeLocalAuthority(String localAuthorityName) {
         // do not remove categories on local authority unloading
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 }
