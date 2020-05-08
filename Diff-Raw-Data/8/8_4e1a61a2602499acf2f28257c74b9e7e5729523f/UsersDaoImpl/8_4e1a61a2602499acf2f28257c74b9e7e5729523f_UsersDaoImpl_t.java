 package com.spring.mti.dao;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.spring.mti.model.Category;
 import com.spring.mti.model.security.Users;
 
 @Repository("usersDAO")
 public class UsersDaoImpl extends GenericDaoImpl<Users, Long> implements UsersDao {
 	@Override
 	@Transactional
 	public void delete(Users userDetail) {
		this.entityManager.createQuery("delete from Authorities s where s.fk_user.id = :user_id").setParameter("user_id", userDetail.getId()).executeUpdate();
 		this.entityManager.createQuery("delete from Users s where s = :user").setParameter("user", userDetail).executeUpdate();
 		this.entityManager.flush();
 	}
 
 	@Override
 	@Transactional
 	public int getEnabled(Users userDetail){
 		return userDetail.getEnabled();
 	}
 }
