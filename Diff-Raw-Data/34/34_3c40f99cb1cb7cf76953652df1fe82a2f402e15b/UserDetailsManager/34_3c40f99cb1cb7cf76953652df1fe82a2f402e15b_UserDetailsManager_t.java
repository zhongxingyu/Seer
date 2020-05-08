 package au.edu.uq.cmm.eccles;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.aclslib.message.Certification;
 
 
 public class UserDetailsManager {
    private static final Logger LOG = LoggerFactory.getLogger(UserDetailsManager.class);
    
     private Random random = new Random();
    private EntityManagerFactory emf;
     private Certification defaultCertification = Certification.VALID;
     
 
    public UserDetailsManager(EntityManagerFactory emf) {
        this.emf = emf;
     }
     
     public UserDetails lookupUser(String userName) throws UnknownUserException {
        EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<UserDetails> query = em.createQuery(
                     "from UserDetails u where u.userName = :userName", 
                     UserDetails.class);
             query.setParameter("userName", userName);
             return query.getSingleResult();
         } catch (NoResultException ex) {
             throw new UnknownUserException(userName);
         } finally {
             em.close();
         }
     }
     
     public List<String> getUserNames() {
        EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<String> query = em.createQuery(
                     "select u.userName from UserDetails u", String.class);
             return query.getResultList();
         } finally {
             em.close();
         }
     }
 
     public List<UserDetails> getUsers() {
        EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<UserDetails> query = em.createQuery(
                     "from UserDetails u", UserDetails.class);
             return query.getResultList();
         } finally {
             em.close();
         }
     }
 
     public void refreshUserDetails(String userName, String email, 
             AclsLoginDetails loginDetails) {
        EntityManager em = emf.createEntityManager();
         try {
             em.getTransaction().begin();
             TypedQuery<UserDetails> query = em.createQuery(
                     "from UserDetails u where u.userName = :userName", 
                     UserDetails.class);
             query.setParameter("userName", userName);
             UserDetails userDetails = query.getSingleResult();
            LOG.debug("Refreshing cached user details for " + userName);
             byte[] digest = createDigest(loginDetails.getPassword(), userDetails.getSeed());
             userDetails.setAccounts(new HashSet<String>(loginDetails.getAccounts()));
             userDetails.setDigest(digest);
             userDetails.setOrgName(loginDetails.getOrgName());
             userDetails.setOnsiteAssist(loginDetails.isOnsiteAssist());
             userDetails.getCertifications().put(
                     loginDetails.getFacilityName(), loginDetails.getCertification().toString());
             em.getTransaction().commit();
         } catch (NoResultException ex) {
            LOG.debug("Caching user details for " + userName);
             long seed = random.nextLong();
             byte[] digest = createDigest(loginDetails.getPassword(), seed);
             UserDetails newDetails = new UserDetails(userName, email, loginDetails, seed, digest);
             em.persist(newDetails);
            em.getTransaction().commit();
         } finally {
             em.close();
         }
     }
     
     public AclsLoginDetails authenticateAgainstCachedCredentials(
             String userName, String password, FacilityConfig facility) {
        LOG.debug("Attempting to authenticate using cached user details for " + userName);
         try {
             UserDetails userDetails = lookupUser(userName);
             byte[] myDigest = createDigest(password, userDetails.getSeed());
             byte[] savedDigest = userDetails.getDigest();
             if (sameDigest(myDigest, savedDigest)) {
                 Certification cert = Certification.valueOf(
                         userDetails.getCertifications().get(facility.getFacilityName()));
                 if (cert == null) {
                     cert = defaultCertification;
                 }
                 return new AclsLoginDetails(userName,
                         userDetails.getOrgName(), password, 
                         facility.getFacilityName(), 
                         new ArrayList<String>(userDetails.getAccounts()),
                         cert, userDetails.isOnsiteAssist(), true);
             }
             return null;
         } catch (UnknownUserException ex) {
             return null;
         }
     }
 
     private byte[] createDigest(String password, long seed) {
         try {
         MessageDigest digester = MessageDigest.getInstance("MD5");
         for (int i = 0; i < 8; i++) {
             byte b = (byte) ((seed >> (i * 8)) & 0xff);
             digester.update(b);
         }
         digester.update(password.getBytes("UTF-8"));
         return digester.digest();
         } catch (NoSuchAlgorithmException ex) {
             throw new AssertionError("Don't understand MD5?", ex);
         } catch (UnsupportedEncodingException ex) {
             throw new AssertionError("Don't understand UTF-8?", ex);
         }
     }
     
     private boolean sameDigest(byte[] d1, byte[] d2) {
         if (d1.length != d2.length) {
             return false;
         } else {
             for (int i = 0; i < d1.length; i++) {
                 if (d1[i] != d2[i]) {
                     return false;
                 }
             }
             return true;
         }
     }
 }
