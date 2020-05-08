 package com.ocbcmcd.monitoring.service.impl;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.ocbcmcd.monitoring.domain.RegistrationCommand;
 import com.ocbcmcd.monitoring.domain.UserUpdateCommand;
 import com.ocbcmcd.monitoring.domain.User;
 import com.ocbcmcd.monitoring.exception.UserNotFoundException;
 import com.ocbcmcd.monitoring.service.IRegistrationService;
 
 @Service
 @Scope("prototype")
 public class RegistrationService implements IRegistrationService {
 	
 	@Autowired
 	HibernateTemplate hibernateTemplate;
 	
 	@Autowired
 	UserFactory userFactory;
 	
 	@Transactional
 	@Override
 	public void register(RegistrationCommand command) {
 		User user = userFactory.createUser(command);
 		hibernateTemplate.save(user);
 	}
 
 	@Transactional(readOnly=true)
 	@Override
 	public User getUser(int id) {
 		return hibernateTemplate.get(User.class, id);
 	}
 
 	@Transactional
 	@Override
 	public void disable(int userId) throws UserNotFoundException {
 		User user = getUser(userId);
 		if (user == null) {
 			throw new UserNotFoundException();
 		} else {
 			user.disable();
 			hibernateTemplate.update(user);
 		}
 	}
 
 	@Transactional
 	@Override
 	public void enable(int userId) throws UserNotFoundException {
 		User user = getUser(userId);
 		if (user == null) {
 			throw new UserNotFoundException();
 		}
 		user.enable();
 		hibernateTemplate.update(user);
 	}
 	
 	@Transactional
 	@Override
 	public void update(UserUpdateCommand command) throws UserNotFoundException {
 		User user = getUser(command.getId());
 		if (user == null) {
 			throw new UserNotFoundException();
 		} 
 		
		user.setPassword(command.getHashedPassword());
 		hibernateTemplate.update(user);
 	}
 
 }
