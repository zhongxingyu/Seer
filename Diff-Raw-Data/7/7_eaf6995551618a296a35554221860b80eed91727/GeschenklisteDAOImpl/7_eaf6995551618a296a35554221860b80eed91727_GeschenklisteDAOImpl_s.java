 package de.giftbox.daoImpl;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 
 import de.giftbox.dao.GeschenklisteDAO;
 import de.giftbox.domain.Geschenk;
 import de.giftbox.domain.Geschenkliste;
 
 public class GeschenklisteDAOImpl implements GeschenklisteDAO {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(GeschenklisteDAOImpl.class);
 
 	@Autowired
 	SessionFactory sessionFactory;
 	
	public void setSessionFactory(SessionFactory sessionFactory) {
		//this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}
 
 	@Override
 	public void saveGeschenkliste(Geschenkliste geschenkliste) {
 		//hibernateTemplate.saveOrUpdate(geschenkliste);
 	}
 
 	@Override
 	public void deleteGeschenkliste(Geschenkliste geschenkliste) {
 		//hibernateTemplate.delete(geschenkliste);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Geschenkliste> listGeschenkliste() {
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(Geschenkliste.class);
 		return criteria.list();
 	}
 }
