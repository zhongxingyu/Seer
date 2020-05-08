 package net.berinle.dao.impl;
 
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import net.berinle.dao.EmployeeDao;
 import net.berinle.model.Employee;
 
 @Repository
 public class EmployeeDaoImpl implements EmployeeDao {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 	
 	public void edit(Employee emp) {
		getSession().update(emp);
 	}
 	
 	public Session getSession(){
 		return sessionFactory.getCurrentSession();
 	}
 
 	public List<Employee> getAll() {
 		return getSession().createCriteria(Employee.class).list();
 	}
 
 	public Employee get(Long id) {
 		return (Employee) getSession().get(Employee.class, id);
 	}
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 }
