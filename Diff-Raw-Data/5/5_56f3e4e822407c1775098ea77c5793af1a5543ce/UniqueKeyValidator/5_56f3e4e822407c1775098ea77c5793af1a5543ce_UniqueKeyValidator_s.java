 package topshelf.utils.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
 
import org.apache.commons.beanutils.BeanUtils;
 
 public class UniqueKeyValidator implements ConstraintValidator<UniqueKey, Object> {
 
 		@Inject Provider<EntityManager> entityManager;
 	    private String[] columnNames;
 	    private String tmpl;
 	    
 	    @Override
 	    public void initialize(UniqueKey constraintAnnotation) {
 	        this.columnNames = constraintAnnotation.columnNames();
 	        this.tmpl = constraintAnnotation.message();
 	    }
 
 	    @Override
 	    public boolean isValid(Object target, ConstraintValidatorContext context) {
 
 	        CriteriaBuilder criteriaBuilder = entityManager.get().getCriteriaBuilder();
 	        CriteriaQuery<?> criteriaQuery = criteriaBuilder.createQuery(target.getClass());
 	        Root<?> root = criteriaQuery.from(target.getClass());
 	        List<Predicate> predicates = new ArrayList<Predicate>(columnNames.length);
 
 	        try {
 	            for(int i=0; i<columnNames.length; i++) {
 	                String propertyName = columnNames[i];
	                Object propertyValue = BeanUtils.getProperty(target, propertyName);
 	                Predicate predicate = criteriaBuilder.equal(root.get(propertyName), propertyValue);
 	                predicates.add(predicate);
 	            }
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 
 	        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
 	        TypedQuery<?> typedQuery = entityManager.get().createQuery(criteriaQuery);
 	        List<?> resultSet = typedQuery.getResultList(); 
 
 	        boolean valid = resultSet.size() == 0 || resultSet.contains(target);
 
 	        if (!valid) {
 			      context.disableDefaultConstraintViolation();
 			      ConstraintViolationBuilder cvb = context.buildConstraintViolationWithTemplate(tmpl);
 			      for (int i=0; i<columnNames.length; i++) {
 			    	  cvb.addNode(columnNames[i]).addConstraintViolation();
 			      }
 		    }
 
 		    return valid;
 	    }
 }
