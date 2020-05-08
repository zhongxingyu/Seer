 package org.young.sh.dao;
 
 
 import org.young.hibernate.dao.IOrderDAO;
 import org.young.hibernate.entity.Order;
 
 /**
  * 
  * @author by Young.ZHU
  * on 下午5:55:38 2013-7-27
  *
  * Package: org.young.sh.dao.OrderDAOHibernateImpl
  */
 public class OrderDAOImpl extends DefaultDAO implements
 		IOrderDAO {
 
	@Override
 	public void add(Order order) {
 		
 		getHibernateTemplate().save(order);
 	}
 
 
 }
