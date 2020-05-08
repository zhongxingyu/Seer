 package intersect.org.au.Pro_JPA_2;
 
 import javax.persistence.*;
 
 public class EmployeeApp {
 	public static void main(String[] args) {
 		EntityManagerFactory emf = Persistence
 				.createEntityManagerFactory("pro_jpa_2");
 		EntityManager em = emf.createEntityManager();
 		Employee emp = new Employee(158);
 		em.persist(emp);
		emp = em.find(Employee.class, 158);
		System.out.println(emp.getName());
 	}
 }
