 package uk.ac.cam.sup.queries;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.ScrollMode;
 import org.hibernate.ScrollableResults;
 
 import uk.ac.cam.sup.exceptions.QueryAlreadyOrderedException;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.util.Mappable;
 
 public abstract class Query<T extends Mappable> {
 	protected Criteria criteria;
 	protected boolean modified = false;
 	
 	public static Query<?> all(Class<?> qclass) {
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<T> list() {
 		return criteria.list();
 	}
 	
 	public List<Map<String,?>> maplist(boolean shadowed) {
 		List<T> list = this.list();
 		List<Map<String, ?>> result = new ArrayList<Map<String,?>>();
 		
 		for (T t: list) {
 			result.add(t.toMap(shadowed));
 		}
 		
 		return result;
 	}
 	
 	public List<Map<String,?>> maplist() {
 		return this.maplist(true);
 	}
 	
 	public abstract Query<T> withTags(List<Tag> tags);
 	public abstract Query<T> withOwners(List<User> owners);
 	public abstract Query<T> withStar();
 	public abstract Query<T> withoutStar();
 	public abstract Query<T> byStudent();
 	public abstract Query<T> bySupervisor();
 	public abstract Query<T> after(Date d);
 	public abstract Query<T> before(Date d);
 	public abstract Query<T> minDuration(int duration);
 	public abstract Query<T> maxDuration(int duration);
 	
 	public Criteria getCriteria() {
 		return criteria;
 	}
 	
 	public Query<T> maxResults(int amount){
 		modified = true;
 		criteria.setMaxResults(amount);
 		return this;
 	}
 	
 	public Query<T> offset(int offset) {
 		modified = true;
 		criteria.setFirstResult(offset);
 		return this;
 	}
 	
 	public int size() throws QueryAlreadyOrderedException {
 		try{
 			/*int result = ((Long)criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
 			criteria.setProjection(null);
 			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
 			return result;*/
			ScrollableResults sr = criteria.scroll(ScrollMode.SCROLL_INSENSITIVE);
 			sr.last();
 			int result = sr.getRowNumber() + 1;
 			return result;
 		} catch(HibernateException e) {
 			throw new QueryAlreadyOrderedException("Order was already applied to this QuestionQuery!",e);
 		}
 	}
 	
 	public boolean isModified(){return modified;}
 }
