 package net.pdp7.tvguide.spring.web;
 
 import net.pdp7.tvguide.dao.UserDao;
 
 import org.springframework.social.connect.Connection;
 import org.springframework.social.connect.ConnectionSignUp;
 
 public class SimpleConnectionSignUp implements ConnectionSignUp {
 
 	protected final UserDao userDao;
 
 	public SimpleConnectionSignUp(UserDao userDao) {
 		this.userDao = userDao;
 	}
 	
	@Override
 	public String execute(Connection<?> connection) {
 		return Long.toString(userDao.createUser());
 	}
 
 }
