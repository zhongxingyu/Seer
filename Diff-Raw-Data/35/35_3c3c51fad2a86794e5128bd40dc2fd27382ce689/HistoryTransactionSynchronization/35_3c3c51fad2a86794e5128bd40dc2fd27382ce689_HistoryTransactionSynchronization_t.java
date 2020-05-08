 // Copyright 2013 Structure Eng Inc.
 
 package com.structureeng.persistence.history;
 
 import static com.structureeng.persistence.history.HistoryListener.RevisionInfo;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.support.TransactionSynchronization;
 
 import java.util.List;
 
 import javax.inject.Provider;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 
 /**
  * Transaction synchronization call backs that persist the revision entities once a
  * transaction has been committed.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  */
 public class HistoryTransactionSynchronization implements TransactionSynchronization {
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private final Provider<EntityManager> entityManagerProvider;
 
     public HistoryTransactionSynchronization(Provider<EntityManager> entityManagerProvider) {
         this.entityManagerProvider = entityManagerProvider;
     }
 
     @Override
     public void suspend() {
         logger.debug("suspend is not implemented.");
     }
 
     @Override
     public void resume() {
         logger.debug("resume is not implemented.");
     }
 
     @Override
     public void flush() {
         logger.debug("flush is not implemented.");
     }
 
     @Override
     public void beforeCommit(boolean bln) {
         logger.debug("before commit is not implemented.");
     }
 
     @Override
     public void beforeCompletion() {
         logger.debug("before completion is not implemented.");
     }
 
     @Override
     public void afterCommit() {
        logger.debug("after commit completion is not implemented.");
     }
 
     @Override
     public void afterCompletion(int i) {
         switch (i) {
             case TransactionSynchronization.STATUS_COMMITTED:
                 committed();
                 return;
             case TransactionSynchronization.STATUS_ROLLED_BACK:
                 rolledback();
                 return;
             case TransactionSynchronization.STATUS_UNKNOWN:
             default:
                 txCompleted();
         }
     }
 
     private void committed() {
        logger.debug("About to store the revision entities.");
        RevisionInfo revisionInfo = HistoryListener.removeCurrentRevisionInfo();
        if (revisionInfo == null) {
            logger.warn("Revision Info was not found.");
        } else {
            EntityManager entityManager = entityManagerProvider.get();
            EntityTransaction tx = entityManager.getTransaction();
            tx.begin();
            List<HistoryEntity> historyEntities = revisionInfo.getHistoryEntity();
            for (HistoryEntity he : historyEntities) {
                entityManager.persist(he);
             }
            tx.commit();
            logger.debug("The revision entities has been persisted.");
         }
     }
 
     private void rolledback() {
         logger.debug("The revision entities will not be persisted because the transaction was "
                 + "rolledback.");
         HistoryListener.removeCurrentRevisionInfo();
     }
 
     private void txCompleted() {
         logger.warn("The revision entities will not be persisted because the transaction "
                 + "demarcation was not found.");
         HistoryListener.removeCurrentRevisionInfo();
     }
 }
