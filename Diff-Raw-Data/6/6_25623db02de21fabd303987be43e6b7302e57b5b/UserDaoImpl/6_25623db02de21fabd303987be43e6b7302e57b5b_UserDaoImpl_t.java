 package cbd.dao;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import cbd.model.UserAccountModel;
 
 @Repository @Transactional
 public class UserDaoImpl implements UserDao {
 	
 	@Autowired SessionFactory sessionFactory;
 
 	@SuppressWarnings("unchecked")
 	public List<UserAccountModel> get(String userId) {
 		return sessionFactory.getCurrentSession()
 				.createQuery("from UserAccountModel where userId = :userId")
 				.setString("userId", userId)
 				.list();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<UserAccountModel> allUsers() {
 		return sessionFactory.getCurrentSession()
 				.createQuery("from UserAccountModel")
 				.list();
 	}
 
 	public void saveOrUpdate(UserAccountModel userAccount) {
 		sessionFactory.getCurrentSession().saveOrUpdate(userAccount);
 	}
 
 	public void delete(String userId) {
 		sessionFactory.getCurrentSession()
 		.createQuery("delete from UserAccountModel where userId = :userId")
		.setString("userId", userId)
		.executeUpdate();
 	}
 }
