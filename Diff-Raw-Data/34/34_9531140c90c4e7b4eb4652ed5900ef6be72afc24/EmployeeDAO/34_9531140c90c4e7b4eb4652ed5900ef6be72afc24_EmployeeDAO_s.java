 package ch.bli.mez.model.dao;
 
 import java.util.List;
 
 import org.hibernate.*;
 
 import ch.bli.mez.model.Employee;
 import ch.bli.mez.model.SessionManager;
 
 @SuppressWarnings("unchecked")
 public class EmployeeDAO {
 
   private SessionFactory sessionFactory;
   private Session session;
 
   public EmployeeDAO() {
     sessionFactory = SessionManager.getSessionFactory();
     session = sessionFactory.openSession();
   }
 
   public List<Employee> findAll() {
	Transaction tx = session.beginTransaction();
    List<Employee> employees = session.createQuery("from Employee e order by e.firstName").list();
     session.flush();
     tx.commit();
     return employees;
   }
 
   public void addEmployee(Employee employee) {
     Transaction tx = session.beginTransaction();
     try {
       session.save(employee);
       session.flush();
       tx.commit();
     } catch (IllegalArgumentException ex) {
       tx.rollback();
       throw ex;
     }
   }
 
   public Employee getEmployee(Integer id) {
	Transaction tx = session.beginTransaction();
     Employee employee = (Employee) session.load(Employee.class, id);
     session.flush();
     tx.commit();
     return employee;
   }
 
   public void updateEmployee(Employee employee) {
     Transaction tx = session.beginTransaction();
     try {
       session.update(employee);
       session.flush();
       tx.commit();
     } catch (Exception ex) {
       tx.rollback();
     }
   }
 
   public void deleteEmployee(Integer id) {
	Transaction tx = session.beginTransaction();
     Employee employee = (Employee) session.load(Employee.class, id);
     if (null != employee) {
       session.delete(employee);
     }
     tx.commit();
   }
 }
