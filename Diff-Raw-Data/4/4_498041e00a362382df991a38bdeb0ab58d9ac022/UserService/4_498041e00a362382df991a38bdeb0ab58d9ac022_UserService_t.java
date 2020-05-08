 package org.mat.nounou.services;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.mat.nounou.model.Account;
 import org.mat.nounou.model.Child;
 import org.mat.nounou.model.User;
 import org.mat.nounou.servlets.EntityManagerLoaderListener;
 import org.mat.nounou.util.Constants;
 import org.mat.nounou.vo.UserVO;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Value Object for the User entity
  * UserVO: mlecoutre
  * Date: 27/10/12
  * Time: 12:01
  */
 @Path("/users")
 @Produces(MediaType.APPLICATION_JSON)
 public class UserService {
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public List<UserVO> get() {
         System.out.println("Get Users service");
         return executeUserRequestList("FROM User", null);
     }
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     public Response registerUser(UserVO user) {
        System.out.println("Register " + user);
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
             User u = new User();
             Account account = null;
             BeanUtils.populate(u, BeanUtils.describe(user));
             u.setUserId(null); //forced
             em.getTransaction().begin();
             if (user.getAccountId() == null) {
                 System.out.println("Create a new account.");
                 user.setNewUser(true);
                 account = new Account();
                 em.persist(account);
             } else {
                 TypedQuery<Account> tQ = em.createQuery("FROM Account where accountId=:accountId", Account.class);
                 tQ.setParameter("accountId", user.getAccountId());
                 account = tQ.getSingleResult();
             }
             u.setAccount(account);
             em.persist(u);
             em.getTransaction().commit();
             user.setAccountId(u.getAccount().getAccountId());
            user.setUserId(u.getUserId());
         } catch (NoResultException nre) {
             System.out.println("ERROR no account found with accountId: " + user.getAccountId());
         } catch (Exception e) {
             e.printStackTrace();
         }    finally {
             em.close();
         }
         return Response.created(URI.create("/services/users/" + user.getUserId())).entity(user).build();
     }
 
     @GET
     @Path("/account/{accountId}")
     public List<UserVO> findByAccountId(@PathParam("accountId") Integer accountId) {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("accountId", accountId);
         return executeUserRequestList("FROM User WHERE account.accountId=:accountId", parameters);
     }
 
     @GET
     @Path("/{userId}")
     public UserVO findByUserId(@PathParam("userId") Integer userId) {
         UserVO u = null;
         User user = null;
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
             TypedQuery<User> query = em.createQuery("FROM User WHERE userId=:userId", User.class);
             query.setParameter("userId", userId);
             user = query.getSingleResult();
             BeanUtils.populate(u, BeanUtils.describe(user));
             if (user.getAccount() != null) {
                 u.setAccountId(user.getAccount().getAccountId());
             } else {
                 System.out.println("ERROR a user MUST be bound to an existing account");
             }
         } catch (NoResultException nre) {
             System.out.println("No result found for userId=" + userId);
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
         return u;
     }
 
     /**
      * Execute search request on User entity and return the ValueObject list
      *
      * @param queryString jpql query
      * @param parameters  jpql parameters
      * @return list of UserVO
      */
     private List<UserVO> executeUserRequestList(String queryString, HashMap<String, Object> parameters) {
         List<User> users = null;
         List<UserVO> vos = new ArrayList<UserVO>();
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
 
             TypedQuery<User> query = em.createQuery(queryString, User.class);
             if (parameters != null) {
                 Iterator<String> it = parameters.keySet().iterator();
                 while (it.hasNext()) {
                     String pName = it.next();
                     query.setParameter(pName, parameters.get(pName));
                 }
             }
             query.setMaxResults(Constants.MAX_RESULT);
             users = query.getResultList();
 
             /* Populate the value object */
             for (User user : users) {
                 UserVO u = new UserVO();
                 BeanUtils.populate(u, BeanUtils.describe(user));
                 u.setAccountId(user.getAccount().getAccountId());
                 vos.add(u);
             }
         } catch (NoResultException nre) {
             System.out.println("No result found while search users " + nre.getMessage());
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
         return vos;
     }
 
 
     @GET
     @Path("/delete/{userId}")
     public Response deleteById(@PathParam("userId") Integer userId) {
         List<Child> c = null;
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
 
             em.getTransaction().begin();
             Query query = em.createQuery("DELETE FROM User WHERE userId=:userId");
 
             query.setParameter("userId", userId);
             query.executeUpdate();
             em.getTransaction().commit();
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
         return Response.ok().build();
     }
 }
