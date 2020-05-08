 package fr.cg95.cvq.service.request.aspect;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.authority.Category;
 import fr.cg95.cvq.business.authority.CategoryRoles;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.service.authority.ICategoryService;
import fr.cg95.cvq.service.request.annotation.RequestFilter;
import fr.cg95.cvq.util.Critere;

 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.springframework.core.Ordered;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
 * @author bor@zenexity.fr
  */
 @Aspect
 public class RequestFilterAspect implements Ordered {
 
     private ICategoryService categoryService;
 
     @Before("fr.cg95.cvq.SystemArchitecture.businessService() && @annotation(requestFilter) && within(fr.cg95.cvq.service.request..*)")
     public void contextAnnotatedMethod(JoinPoint joinPoint, RequestFilter requestFilter) {
 
         Object[] arguments = joinPoint.getArgs();
         Set<Critere> criteriaSet = (Set<Critere>) arguments[0];
 
         Critere crit = new Critere();
         if (SecurityContext.isBackOfficeContext()) {
             List<Category> agentCategories = null;
             if (requestFilter.privilege().equals(ContextPrivilege.MANAGE)) {
                 agentCategories = categoryService.getManaged();
             } else if (requestFilter.privilege().equals(ContextPrivilege.READ)) {
                 Agent agent = SecurityContext.getCurrentAgent();
                 Set<CategoryRoles> agentCategoryRoles = agent.getCategoriesRoles();
                 if (agentCategoryRoles == null) {
                     return;
                 }
                 agentCategories = new ArrayList<Category>();
                 for (CategoryRoles categoryRole : agentCategoryRoles) {
                     agentCategories.add(categoryRole.getCategory());
                 }
             }
             StringBuffer sb = new StringBuffer();
            if (agentCategories == null || agentCategories.isEmpty()) {
                 sb.append("'-1'");
             } else {
                 for (Category category : agentCategories) {
                     if (sb.length() > 0) {
                         sb.append(",");
                     }
                     sb.append("'").append(category.getId()).append("'");
                 }
             }
             crit.setAttribut("belongsToCategory");
             crit.setComparatif(Critere.EQUALS);
             crit.setValue(sb.toString());
             criteriaSet.add(crit);
         } else if (SecurityContext.isFrontOfficeContext()) {
             Adult adult = SecurityContext.getCurrentEcitizen();
             crit.setAttribut(Request.SEARCH_BY_HOME_FOLDER_ID);
             crit.setComparatif(Critere.EQUALS);
             crit.setValue(adult.getHomeFolder().getId());
             criteriaSet.add(crit);
         }
     }
 
     public int getOrder() {
         return 2;
     }
 
     public void setCategoryService(ICategoryService categoryService) {
         this.categoryService = categoryService;
     }
 }
