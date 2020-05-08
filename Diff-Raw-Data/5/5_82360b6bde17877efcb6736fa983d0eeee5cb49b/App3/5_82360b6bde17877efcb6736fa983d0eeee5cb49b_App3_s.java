 package edu.buet.cse.hibernate.lesson07;
 
 import java.util.Date;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import edu.buet.cse.hibernate.lesson07.model.Role;
 import edu.buet.cse.hibernate.lesson07.model.User;
 import edu.buet.cse.hibernate.lesson07.util.HibernateUtil;
 
// create a new user, then attach a pre-existing role with him
 public class App3 {
   public static void main(String... args) {
     Session session = null;
     
     try {
       session = HibernateUtil.getSession();
       // create the entities
       User user = new User();
       user.setUsername("tux");
       user.setCreatedDate(new Date());
       
       Transaction tx = session.beginTransaction();
       Role role = (Role) session.get(Role.class, 2L);
       user.addRole(role);
       role.addUser(user);
 
       session.save(user);
       tx.commit();
       System.out.printf("New user created with role %s%n", role.getRoleName());
     } catch (HibernateException ex) {
       ex.printStackTrace(System.err);
     } finally {
       if (session != null) {
         session.close();
       }
       
       HibernateUtil.cleanUp();
     }
   }
 }
