 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package control;
 
 import java.util.List;
 import model.User;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 /**
  *
  * @author baxter
  */
 public class HiberBDDConnector {
 
     public HiberBDDConnector() {
         sessionFactory = new Configuration().configure().buildSessionFactory();
     }
     private static Session session;
     private Query query;
     private static SessionFactory sessionFactory;
 
     private void connect() {
         session = sessionFactory.openSession();
         session.beginTransaction();
     }
 
     private void stop() {
         if (session.isConnected()) {
             session.close();
         }
     }
 
     public List<User> getAllUsers() {
         connect();
         query = session.createQuery("from User");
         return query.list();
     }
 
     public List<User> getAllAdmins() {
         connect();
         query = session.createQuery("from User where admin='1'");
 
         return query.list();
     }
 
     public boolean insertUser(String nom, String password, int postCode) {
         boolean out = false;
         connect();
         if (userExist(nom) == false) {
             byte admin = 0;
             User uTemp = new User(nom, password, postCode, admin);
             session.save(uTemp);
             session.getTransaction().commit();
             out = true;
         } else {
             System.err.println("User exist déjà");
         }
         stop();
         return out;
     }
 
     public boolean insertUser(User u) {
 
         boolean out = false;
         if (userExist(u.getNom()) == false) {
             connect();
             byte admin = 0;
             u.setAdmin(admin);
            System.err.println("user " + u.getNom() + " pass " + u.getPassword()+ " postcode" + u.getPostcode() 
                    + " admin "+u.getAdmin());
            session.save(u);
             session.getTransaction().commit();
             
             if (session.getTransaction().wasCommitted()) {
                 out = true;
                 System.err.print("User error commit");
             }
             else {
             System.err.print("User commit don");
             }
             stop();
         } else {
             System.err.println("User exist déjà");
         }
 
         return out;
     }
 
     public boolean userExist(String nom) {
         connect();
         boolean out = false;
         System.err.print("User " + nom);
         query = session.createQuery("from User where nom='" + nom + "'");
         List<User> lst = query.list();
         if (!lst.isEmpty()) {
             out = true;
             System.err.print("User Exist true");
         }
         System.err.print("User d'ont exist " + nom);
         stop();
         return out;
     }
 }
