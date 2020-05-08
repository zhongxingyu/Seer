 package com.cerberus.server.persistence.DAO;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.springframework.stereotype.Component;
 import com.cerberus.server.persistence.HibernateUtil;
 
 @Component
 public class GenericDAO<T, ID> {
	
	private Class<T> clazz;
	
 	@Resource(name = "sessionFactory")
 	protected SessionFactory sessionFactory;
 	
 	public GenericDAO(){
 		this.sessionFactory = HibernateUtil.getSessionFactory();
 	}
 
 	@SuppressWarnings("unchecked")
 	public T save(final T o){
 		Session session = sessionFactory.getCurrentSession();
 		Transaction trans=session.beginTransaction();
 		T savedO = (T) session.save(o);
 		trans.commit();
 		return savedO;
 	}
 
 
 	public void delete(final Object object){
 		sessionFactory.getCurrentSession().delete(object);
 	}
 
 	/***/
 	@SuppressWarnings("unchecked")
 	protected T getById(final Class<T> T, final ID id){
 		return (T) sessionFactory.getCurrentSession().get(T, (Serializable) id);
 	}
 
 	/***/
 	@SuppressWarnings("unchecked")
 	public T merge(final T o)   {
 		return (T) sessionFactory.getCurrentSession().merge(o);
 	}
 
 	/***/
 	public void saveOrUpdate(final T o){
 		sessionFactory.getCurrentSession().saveOrUpdate(o);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<T> getAll(final Class<T> type) {
 		//TODO Add try catch finally
 		final Session session = sessionFactory.getCurrentSession();
 		Transaction trans=session.beginTransaction();
 		final Criteria crit = session.createCriteria(type);
 
 		List<T> list = crit.list();
 		trans.commit();
 		return list;
 	}
 
	public Class<T> getClazz() {
		return clazz;
	}

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

 }
