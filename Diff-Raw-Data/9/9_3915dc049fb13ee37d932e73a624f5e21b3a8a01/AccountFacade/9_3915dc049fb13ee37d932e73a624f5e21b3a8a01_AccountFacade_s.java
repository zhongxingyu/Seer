 package se.liu.tdp024.facade;
 
 import java.util.*;
 import javax.persistence.*;
 import se.liu.tdp024.entity.Account;
 import se.liu.tdp024.entity.SavedTransaction;
 import se.liu.tdp024.util.EMF;
 import se.liu.tdp024.util.Monlog;
 /**
  *
  */
 public abstract class AccountFacade {
    private final static Monlog LOGGER = Monlog.getLogger();
 
     public static Account create(int accountType,
                               String personKey,
                               String bankKey) {
         EntityManager em = EMF.getEntityManager();
         try {
 
             em.getTransaction().begin();
 
             Account acc = new Account();
             acc.setAccountType(accountType);
             acc.setPersonKey(personKey);
             acc.setBankKey(bankKey);
 
             em.persist(acc);
 
             em.getTransaction().commit();
 
             return acc;
 
         } catch (EntityExistsException eee) {
             /*
              * Comes from em.persist();
              * Should log something here
              */
             return null;
         } finally {
 
             if(em.getTransaction().isActive()) {
                 em.getTransaction().rollback();
             }
 
             em.close();
         }
     }
 
     public static Account find(long accountNumber) {
         EntityManager em = EMF.getEntityManager();
         try {
             return em.find(Account.class, accountNumber);
         } catch (IllegalArgumentException e) {
             /*
             em.find():
             Throws:
                 IllegalArgumentException - if the first argument does not denote
                       an entity type or the second argument is is not a valid
                       type for that entity's primary key or is null
              *
              * Log something here
              */
             return null;
         } finally {
             em.close();
         }
     }
 
     public static List<Account> findByPersonKey(String personKey) {
         EntityManager em = EMF.getEntityManager();
         Query query = em.createQuery("SELECT a FROM Account a WHERE a.personKey = :personkey");
         query.setParameter("personkey", personKey);
         return query.getResultList();
     }
 
     public static List<Account> findByBankKey(String bankKey) {
         EntityManager em = EMF.getEntityManager();
         Query query = em.createQuery("SELECT a FROM Account a WHERE a.bankKey = :bankkey");
         query.setParameter("bankkey", bankKey);
         return query.getResultList();
     }
 
     public static long balance(long accountNumber) {
         Account acc = find(accountNumber);
         if (acc == null) {
             return -1;
         }
 
         return acc.getBalance();
     }
 
     private static boolean changeBalance(long sender, long reciever, long amount) {
         EntityManager em = EMF.getEntityManager();
 
         SavedTransaction st = new SavedTransaction();
         st.setSender(sender);
         st.setReciever(reciever);
         st.setAmount(amount);
         st.setSuccess(true); // Crossing fingers, "Everything will be ok!"
 
         try {
             em.getTransaction().begin();
             if (sender == reciever) {
                 /* LOG */
                 return false;
             }
 
             Account senderAcc = null;
             Account recieverAcc = null;
 
             // Check validity of sender account
             if (sender != 0) {
                 senderAcc = em.find(Account.class, sender, LockModeType.PESSIMISTIC_WRITE);
                 if (senderAcc == null) {
                     return false;
                 } else {
                     if ((senderAcc.getBalance() - amount) < 0 ||
                             (senderAcc.getBalance() - amount) > Long.MAX_VALUE) {
                         // Value out of range
                         return false;
                     }
                 }
             }
 
             if (reciever != 0) {
                 recieverAcc = em.find(Account.class, reciever, LockModeType.PESSIMISTIC_WRITE);
                 if (recieverAcc == null) {
                     return false;
                 } else {
                     if ((recieverAcc.getBalance() + amount) < 0 ||
                             (recieverAcc.getBalance() + amount) > Long.MAX_VALUE) {
                         // Value out of range
                         return false;
                     }
                 }
             }
 
             if (senderAcc != null) {
                 senderAcc.changeBalance(-amount);
                 em.merge(senderAcc); // Commit account changes
             }
             if (recieverAcc != null) {
                 recieverAcc.changeBalance(amount);
                 em.merge(recieverAcc); // Commit account changes
             }
 
             em.merge(st);  // Save transaction
             em.getTransaction().commit();
             return true;
 
         } catch (IllegalArgumentException e) {
             /*  if instance is not an entity or is a removed entity */
             /*
              * Log something here
              */
             return false;
 
         } finally {
             /* If the transaction is still active, the commit never happened. */
             /* Do a barrel rollback. */
             if(em.getTransaction().isActive()) {
                 em.getTransaction().rollback();
 
                 /* We had to rollback the SavedTransaction, so recommit it */
                 /* Also, if the SavedTransaction raised exception and couldn't */
                 /* be saved, success needs to be set to false before recommit */
                 st.setSuccess(false);
                 try {
                     em.getTransaction().begin();
                     em.merge(st);
                     em.getTransaction().commit();
                 } catch (Exception e) {
                     if(em.getTransaction().isActive()) {
                         em.getTransaction().rollback();
                         /*
                          * Couldn't save SavedTransaction. Log to monlog
                          */
                     }
                 }
             }
             em.close();
         }
     }
 
     public static boolean transfer(long sender, long reciever, long amount) {
         return changeBalance(sender, reciever, amount);
     }
 
     public static boolean withdrawCash(long account, long amount) {
         return changeBalance(account, 0, amount);
     }
 
     public static boolean depositCash(long account, long amount) {
         return changeBalance(0, account, amount);
     }
 }
