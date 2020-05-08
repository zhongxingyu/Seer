 package daoLayer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.Stateful;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.enterprise.context.ConversationScoped;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.ParameterExpression;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import util.Config;
 import businessLayer.Company;
 
 @Stateful
 @ConversationScoped
 @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
 public class CompanySearchService extends Pager<Company> {
 
 	@PersistenceContext(unitName = "primary", type = PersistenceContextType.EXTENDED)
 	private EntityManager em;
 
 	private Pager<Company> pager;
 
 	
 	public void init(String nameLike) {
 		
 
 		TypedQuery<Company> query;
 		TypedQuery<Long> countQuery;
 
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Company> c = cb.createQuery(Company.class);
 		Root<Company> comp = c.from(Company.class);
 		c.select(comp);
 		comp.alias("comp");
 
 		CriteriaQuery<Long> countC = cb.createQuery(Long.class);
 		Root<Company> compC = countC.from(Company.class);
 		countC.select(cb.countDistinct(compC));
 		compC.alias("comp");
 
 		query = em.createQuery(c);
 		countQuery = em.createQuery(countC);
 
 		List<Predicate> criteria = new ArrayList<Predicate>();
 
 		if (nameLike != null) {
 			
 			nameLike = nameLike.trim().toLowerCase();
 			ParameterExpression<String> p = cb.parameter(String.class, "like");
 			criteria.add(cb.like(cb.lower(cb.trim((comp.<String> get("name")))), p));
 		}
 		
 		
 		
 		if(criteria.size() != 0){
 			c.where(cb.and(criteria.toArray(new Predicate[0])));
 			countC.where(cb.and(criteria.toArray(new Predicate[0])));
 		}
 		
 		query = em.createQuery(c);
 		countQuery = em.createQuery(countC);
 		
 		
 		if (nameLike != null) {
 
 			query.setParameter("like", nameLike+"%");
 			countQuery.setParameter("like", nameLike+"%");
 		}
 
 		pager = new ResultPager<>(0, Config.defaultPageSize, query, countQuery);
 	}
 
 	public void next() {
 		pager.next();
 	}
 
 	public void previous() {
 		pager.previous();
 	}
 
 	public int getCurrentPage() {
 		return pager.getCurrentPage();
 	}
 
 	public void setCurrentPage(int currentPage) {
 		pager.setCurrentPage(currentPage);
 	}
 
 	public List<Company> getCurrentResults() {
 		return pager.getCurrentResults();
 	}
 
 	public int getPageSize() {
 		return pager.getPageSize();
 	}
 
 	public void setPageSize(int pageSize) {
 		pager.setPageSize(pageSize);
 	}
 
 	public Long getResultNumber() {
 		return pager.getResultNumber();
 	}
 
 	public void finished() {
 		pager.finished();
 	}
 
 }
