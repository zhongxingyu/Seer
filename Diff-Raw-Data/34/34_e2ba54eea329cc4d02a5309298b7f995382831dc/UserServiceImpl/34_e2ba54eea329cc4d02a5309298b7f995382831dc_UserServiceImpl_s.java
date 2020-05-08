 package com.sensi.service.impl;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 
 import com.sensi.domain.User;
 import com.sensi.domain.UserExistException;
 import com.sensi.service.UserService;
 
 @Service("userService")
 @Transactional
 public class UserServiceImpl implements UserService, UserDetailsService {
 	
 	@Autowired
 	private SessionFactory sessionFactory;
 	@Autowired
 	private PasswordEncoder passwordEncoder;
 
 	@Override
 	public Long countUsers() {
 		return (Long) sessionFactory.getCurrentSession().createQuery("select count(o) from User o").uniqueResult();
 	}
 
 	@Override
 	public Long countUsersByUsername(String username) {
 		if (!StringUtils.hasText(username))
 			return 0L;
 		return (Long) sessionFactory.getCurrentSession().createQuery("select count(o) from User o where o.username = :username").setString("username", username).uniqueResult();
 	}
 
 	@Override
 	public void delete(User user) {
 		if (user != null && user.getId() != null){
 			sessionFactory.getCurrentSession().delete(user);
 		}
 	}
 
 	@Override
 	public User findUser(Long id) {
 		return (User) sessionFactory.getCurrentSession().createQuery("select o from User o where o.id = :id").setLong("id", id).uniqueResult();
 	}
 
 	@Override
 	public List<User> findUsers() {
 		return sessionFactory.getCurrentSession().createQuery("select o from User o order by o.username").list();
 	}
 
 	@Override
 	public User findUsersByUsername(String username) {
 		return (User) sessionFactory.getCurrentSession().createQuery("select o from User o where o.username = :username").setString("username", username).uniqueResult();
 	}
 
 	@Override
 	public void save(User user) {
 		if(passwordEncoder != null){
 			user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));
 		}
 		sessionFactory.getCurrentSession().save(user);
 	}
 
 	@Override
 	public void update(User user) {
 		sessionFactory.getCurrentSession().update(user);
 	}
 
 	@Override
 	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		List<User> users = sessionFactory.getCurrentSession().createQuery("select o from User o order by o.username").list();
 		if(users == null || users.isEmpty()){
			throw new UsernameNotFoundException("user "+username+" not found");
 		}else {
 			return (UserDetails) users.get(0);
 		}
 	}
 	
 	@Override
 	public void createAccount(User user) {
 		// check user, if exist throw exeption
 		User userEx = findUsersByUsername(user.getUsername());
 		if(userEx!=null){
			throw new UserExistException("User "+userEx.getUsername()+" already exist");
 		}
 		// else save user
 		save(user);
 	}
 
 }
