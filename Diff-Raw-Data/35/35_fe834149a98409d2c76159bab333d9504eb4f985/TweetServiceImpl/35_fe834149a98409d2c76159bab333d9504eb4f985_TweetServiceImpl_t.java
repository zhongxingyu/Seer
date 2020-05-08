 package name.felixbecker.brokenaop.services;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import name.felixbecker.brokenaop.entities.TweetBO;
 
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
 
 @Component
 class TweetServiceImpl implements TweetService {
 
	@PersistenceContext
	private EntityManager em;

	@Transactional
 	public List<TweetBO> getAllTweets() {
 		Session session = em.unwrap(Session.class);
 		return session.createCriteria(TweetBO.class).addOrder(Order.desc("created")).list();
 	}
 }
