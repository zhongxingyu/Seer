 package org.delegator.engine;
 
 import javax.persistence.NoResultException;
 import org.delegator.engine.HibernateUtils;
 import org.delegator.entities.Employee;
 import org.hibernate.Session;
 
 public class LoggedUserBean implements LoggedUser {
 
 	private int _userEid;
 	private Employee emp;
 
 	public LoggedUserBean() {
 	}
 	
 	public Employee getEmp() {
 		return emp;
 	}
 
 	public void setEmp(Employee emp) {
 		this.emp = emp;
 	}
 
 	public int get_UserEid() {
 		return _userEid;
 	}
 
 	public void set_UserEid(int userEid) {
 		_userEid = userEid;
 	}
 
 	public int isRegistered(String username, String password) {
 		try {
 			Session session = HibernateUtils.getSessionFactory().getCurrentSession();
			session.beginTransaction();
 			emp = (Employee)session
 					.createQuery(
							"from Employee where userName = :u and password = :p")
					.setParameter("u", username)
					.setParameter("p", Integer.valueOf(password))
 					.uniqueResult();
 			if (emp != null) {
 				_userEid = emp.getEid();
 				return _userEid;
 			}
 		} catch (NoResultException ex) {
 			return -1;
 		}
 		return -1;
 	}
 }
