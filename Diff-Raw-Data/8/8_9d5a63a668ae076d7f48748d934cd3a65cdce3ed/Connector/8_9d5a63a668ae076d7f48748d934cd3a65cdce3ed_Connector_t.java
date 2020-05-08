 package ca.wasabistudio.chat.connector;
 
 import java.util.Calendar;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 
 /**
  * Connector to the phpBB database.
  *
  * @author Brad Chen
  */
 public class Connector {
 
    private static final int ANONYMOUS_USER_ID = 1;
     private static final String IP_DELIMITER_REGEX = "\\.";
 
     private EntityManager em;
 
     public void setEntityManagerFactory(EntityManagerFactory emf) {
         em = emf.createEntityManager();
     }
 
     public void destroy() {
         em.close();
     }
 
     public boolean validateSession(String sessionId, String ip) {
         EntityTransaction transaction = em.getTransaction();
         transaction.begin();
         Session session = findSession(sessionId);
         transaction.commit();
         if (session == null) {
             return false;
         }
         String[] ipTokens = ip.split(IP_DELIMITER_REGEX);
         String[] sessionIpTokens = ip.split(IP_DELIMITER_REGEX);
         for (int i = 0; i < 2; i++) {
             if (!sessionIpTokens[i].equals(ipTokens[i])) {
                 return false;
             }
         }

        // check if user is anonymous
        if (session.getUserId() == ANONYMOUS_USER_ID) {
            return false;
        }

         return true;
     }
 
     public String getUsername(String sessionId) {
         EntityTransaction transaction = em.getTransaction();
         transaction.begin();
         Session session = findSession(sessionId);
         if (session == null) {
             transaction.commit();
             return null;
         }
 
         User user = findUser(session.getUserId());
         if (user == null) {
             transaction.commit();
             return null;
         }
 
         transaction.commit();
         return user.getUsername();
     }
 
     public boolean refreshSession(String sessionId) {
         EntityTransaction transaction = em.getTransaction();
         transaction.begin();
         Session session = findSession(sessionId);
         if (session == null) {
             transaction.commit();
             return false;
         }
 
         session.setLastUpdate(Calendar.getInstance());
         transaction.commit();
         return true;
     }
 
     private User findUser(int userId) {
         return (User)em.find(User.class, userId);
     }
 
     private Session findSession(String sessionId) {
         return (Session)em.find(Session.class, sessionId);
     }
 
 }
