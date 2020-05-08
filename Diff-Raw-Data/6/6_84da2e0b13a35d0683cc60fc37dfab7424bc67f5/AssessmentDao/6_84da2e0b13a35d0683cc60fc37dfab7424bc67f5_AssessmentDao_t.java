 package net.frontlinesms.plugins.learn.data.repository;
 
 import java.util.List;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
 
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;
 import net.frontlinesms.plugins.learn.data.domain.Assessment;
 import net.frontlinesms.plugins.learn.data.domain.Topic;
 
 public class AssessmentDao extends BaseHibernateDao<Assessment> {
 	protected AssessmentDao() {
 		super(Assessment.class);
 	}
 	
 	public void save(Assessment a) {
		if(a.getId() > 0) {
			super.updateWithoutDuplicateHandling(a);
		} else {
			super.saveWithoutDuplicateHandling(a);
		}
 	}
 	
 	public void delete(Assessment a) {
 		super.delete(a);
 	}
 	
 	public int count() {
 		return super.countAll();
 	}
 	
 	public List<Assessment> list() {
 		return super.getAll();
 	}
 
 	public List<Assessment> findAllByTopic(Topic t) {
 		DetachedCriteria criteria = getCriterion();
 		criteria.add(Restrictions.eq("topic", t));
 		return super.getList(criteria);
 	}
 	
 	public List<Assessment> findAllByGroup(Group g) {
 		DetachedCriteria criteria = getCriterion();
 		criteria.add(Restrictions.eq("group", g));
 		return super.getList(criteria);
 	}
 
 	public List<Assessment> findAllByGroupAndTopic(Group g, Topic t) {
 		DetachedCriteria criteria = getCriterion();
 		criteria.add(Restrictions.eq("group", g));
 		criteria.add(Restrictions.eq("topic", t));
 		return super.getList(criteria);
 	}
 }
