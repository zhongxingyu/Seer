 package uk.ac.ox.oucs.oauth.dao;
 
 import uk.ac.ox.oucs.oauth.domain.Accessor;
 import uk.ac.ox.oucs.oauth.domain.Consumer;
 
 import java.util.Collection;
 
 /**
  * Delegate method to access the OAuthDAO.
  * <p>
  * As the DAO might work with Hibernate, and everything Hibernate related has to be in Shared, the DAO interface is in the API.
  * Even so, the DAO shouldn't be used (except in here), so the interface is package-visible, and the only way to access to it
  * is by creating a delegate class to do every call.
  * </p>
  * <p>
  * This shouldn't be used anywhere else for obvious security reasons!
  * </p>
  *
  * @author Colin Hebert
  */
 public class OAuthProvider {
     private AccessorDao accessorDao;
     private ConsumerDao consumerDao;
 
     public void setAccessorDao(AccessorDao accessorDao) {
         this.accessorDao = accessorDao;
     }
 
     public void setConsumerDao(ConsumerDao consumerDao) {
         this.consumerDao = consumerDao;
     }
 
     public void createAccessor(Accessor accessor) {
         accessorDao.create(accessor);
     }
 
     public Accessor getAccessor(String accessorId) {
         return accessorDao.get(accessorId);
     }
 
     public Collection<Accessor> getAccessorsByUser(String userId) {
         return accessorDao.getByUser(userId);
     }
 
     public Accessor updateAccessor(Accessor accessor) {
         return accessorDao.update(accessor);
     }
 
     public void removeAccessor(Accessor accessor) {
         accessorDao.remove(accessor);
     }
 
     public void markExpiredAccessors() {
         accessorDao.markExpiredAccessors();
     }
 
     public void createConsumer(Consumer consumer) {
         consumerDao.create(consumer);
     }
 
     public Consumer getConsumer(String consumerId) {
         return consumerDao.get(consumerId);
     }
 
     public Consumer updateConsumer(Consumer consumer) {
         return consumerDao.update(consumer);
     }
 
    public void removeConsumer(String consumerId) {
        consumerDao.remove(consumerId);
     }
 }
