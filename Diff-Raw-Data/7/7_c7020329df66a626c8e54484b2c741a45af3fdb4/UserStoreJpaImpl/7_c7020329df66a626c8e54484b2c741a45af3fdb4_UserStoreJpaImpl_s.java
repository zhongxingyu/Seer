 package com.financial.tools.recorderserver.store.impl;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.springframework.transaction.annotation.Transactional;
 
 import com.financial.tools.recorderserver.entity.User;
 import com.financial.tools.recorderserver.store.UserStore;
 
 @Transactional
 public class UserStoreJpaImpl implements UserStore {
 
 	private EntityManager entityManager;
 
 	@Override
 	public User getUser(long userId) {
 		return entityManager.find(User.class, userId);
 	}
 
 	@Override
 	public long saveUser(User user) {
 		entityManager.persist(user);
 		return user.getId();
 	}
 
 	@Override
 	public void updateBalance(long userId, long balance) {
		Query query = entityManager.createNativeQuery("UPDATE FR_USER SET BALANCE=" + balance + " WHERE ID=?",
				User.class);
		query.setParameter(1, userId);
 		query.executeUpdate();
 	}
 
 	@PersistenceContext
 	public void setEntityManager(EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 
 }
