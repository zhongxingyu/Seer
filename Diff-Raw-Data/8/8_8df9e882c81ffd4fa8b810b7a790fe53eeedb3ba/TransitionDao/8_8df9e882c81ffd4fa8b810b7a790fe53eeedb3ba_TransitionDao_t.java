 package com.dao;
 
 import org.hibernate.FlushMode;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 
 import com.pojo.Position;
 import com.pojo.Sysuser;
 import com.pojo.Transaction;
 
 public class TransitionDao extends BaseDao {
 	public static final int OPERATION_NEW = 0;
 	public static final int OPERATION_UPDATE = 1;
 	public static final int OPERATION_DELETE = 2;
 	private static TransitionDao instance = new TransitionDao();
 
 	public static TransitionDao getInstance() {
 		return instance;
 	}
 
 	private TransitionDao() {
 		super(Transaction.class);
 	}
 
 	public boolean buyAndSell(Position p, Sysuser user, Transaction tran, int operation) {
 		Session session = null;
 		boolean flag = true;
 		try {
 			session = HibernateUtil.getSession();
 			session.setFlushMode(FlushMode.AUTO);
 			session.getTransaction().begin();
 			switch (operation) {
 			case OPERATION_NEW:
 				session.save(p);
 				break;
 			case OPERATION_UPDATE:
 				session.update(p);
 				break;
 			case OPERATION_DELETE:
 				session.delete(p);
				break;
 			}
 			session.update(user);
 			session.update(tran);
 			session.flush();
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			flag = false;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 		return flag;
 	}
 	
 	public boolean withDrawAndDepsit(Sysuser user, Transaction tran) {
 		Session session = null;
 		boolean flag = true;
 		try {
 			session = HibernateUtil.getSession();
 			session.setFlushMode(FlushMode.AUTO);
 			session.getTransaction().begin();
 			session.update(user);
 			session.update(tran);
 			session.flush();
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			flag = false;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 		return flag;
 	}
 }
