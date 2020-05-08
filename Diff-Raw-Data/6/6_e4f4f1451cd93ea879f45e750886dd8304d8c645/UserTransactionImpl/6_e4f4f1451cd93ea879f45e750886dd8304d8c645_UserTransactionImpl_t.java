 package ee.homies.gaffer;
 
 import java.io.Serializable;
 
 import javax.transaction.*;
 
 public class UserTransactionImpl implements UserTransaction, Serializable {
   private static final long serialVersionUID = 1L;
 
  private transient TransactionManagerImpl transactionManager;
 
   public UserTransactionImpl(TransactionManagerImpl transactionManager) {
     this.transactionManager = transactionManager;
   }
 
   @Override
   public void begin() throws NotSupportedException, SystemException {
     getTransactionManager().begin();
   }
 
   @Override
   public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
     getTransactionManager().commit();
   }
 
   @Override
   public void rollback() throws IllegalStateException, SecurityException, SystemException {
     getTransactionManager().rollback();
   }
 
   @Override
   public void setRollbackOnly() throws IllegalStateException, SystemException {
     getTransactionManager().setRollbackOnly();
   }
 
   @Override
   public int getStatus() throws SystemException {
     return getTransactionManager().getStatus();
   }
 
   @Override
   public void setTransactionTimeout(int seconds) throws SystemException {
     getTransactionManager().setTransactionTimeout(seconds);
   }
 
   private TransactionManagerImpl getTransactionManager() {
    if (transactionManager == null) {
      transactionManager = ServiceRegistryHolder.getServiceRegistry().getTransactionManager();
    }
     return transactionManager;
   }
 
 }
