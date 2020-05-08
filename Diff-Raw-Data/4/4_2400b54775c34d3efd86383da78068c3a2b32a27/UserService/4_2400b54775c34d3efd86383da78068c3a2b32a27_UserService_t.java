 package com.tradespeople.service;
 
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.tradespeople.common.exception.TradesPeopleDaoException;
 import com.tradespeople.common.exception.TradesPeopleServiceException;
 import com.tradespeople.dao.IUserHibernateDao;
 import com.tradespeople.model.User;
 import com.tradespeople.searchcriteria.PaginationSearchCriteria;
 import com.tradespeople.utils.ApiUtils;
 import com.tradespeople.utils.CipherUtils;
 import com.tradespeople.validators.LengthValidator;
 import com.tradespeople.validators.RequiredStringValidator;
 
 @Service
 public class UserService implements IUserService {
 	
 	@Autowired
 	private IUserHibernateDao userDao;
 	
 	@Transactional
 	public void create(User user)throws TradesPeopleServiceException {
 		try {
 			if (user.hasPersisted()) {
 				ApiUtils.throwNotPersistedException();
 			}
 			if (user.hasNotExistAnyRole()) {
 				ApiUtils.throwUserRolesObligationException();
 			}
 			if (isExistUserName(user.getUsername(),user.getId())) {
 				ApiUtils.throwSameUserNameObligationException();
 			}
 			
 			validatePassword(user);
 			
 			user.setCreateddate(new Date());
 			user.setToken(generateUniqueUserToken(user));
 			user.setPassword(CipherUtils.encrypt(user.getPassword()));
 			userDao.create(user);
 		} catch (TradesPeopleDaoException e) {
 			throw new TradesPeopleServiceException(e);
 		}
 	}
 	
 	private void validatePassword(User user) throws TradesPeopleServiceException {
 		if (!new RequiredStringValidator(user.getPassword()).validate()) {
 			ApiUtils.throwRequiredFieldException();
 		}
 		if (!new LengthValidator(user.getPassword(),8).validate()) {
 			ApiUtils.throwPasswordLengthRestrictionException();
 		}
 	}
 
 	@Transactional(readOnly=true)
 	private boolean isExistUserName(String username,Long id) throws TradesPeopleDaoException {
 		User user =userDao.getUserBy(username);
 		if (user==null) {
 			return false;
 		}
 		if (user.getUsername().equals(username) && user.getId().equals(id)) {
 			return false;
 		}
 		return true;
 	}
 
 	private String generateUniqueUserToken(User user) {
 		StringBuilder sb=new StringBuilder();
 		sb.append(user.getName());
 		sb.append(user.getUsername());
 		sb.append(user.getSurname());
 		return sb.toString();
 	}
 
 	@Transactional
 	public void update(User user)throws TradesPeopleServiceException {
 		try {
 			if (!user.hasPersisted()) {
 				ApiUtils.throwPersistedException();
 			}
 			if (user.hasNotExistAnyRole()) {
 				ApiUtils.throwUserRolesObligationException();
 			}
 			if (isUserTokenChanged(user)) {
 				ApiUtils.throwUserTokenNotChangedObligationException();
 			}
 			if (isExistUserName(user.getUsername(),user.getId())) {
 				ApiUtils.throwSameUserNameObligationException();
 			}
 			user.setUpdateddate(new Date());
 			userDao.update(user);
 		} catch (TradesPeopleDaoException e) {
 			throw new TradesPeopleServiceException(e);
 		}
 	}
 	
 	@Transactional(readOnly=true)
 	private boolean isUserTokenChanged(User user) throws TradesPeopleDaoException {
 		User updatedUser=userDao.getUserBy(user.getId());
 		if (user.getToken().equals(updatedUser.getToken())) {
 			return false;
 		}
 		return true;
 	}
 
 	public void setUserDao(IUserHibernateDao userDao) {
 		this.userDao = userDao;
 	}
 
 	@Override
 	@Transactional(readOnly=true)
 	public List<User> all(PaginationSearchCriteria searchCriteria) throws TradesPeopleServiceException {
 		try {
 			return userDao.listUsers(searchCriteria);
 		} catch (TradesPeopleDaoException e) {
 			throw new TradesPeopleServiceException(e);
 		}
 	}
 
 	@Override
 	@Transactional
 	public User getUserBy(Long id) throws TradesPeopleServiceException {
 		try {
 			return userDao.getUserBy(id);
 		} catch (TradesPeopleDaoException e) {
 			throw new TradesPeopleServiceException(e);
 		}
 	}
 
 	@Override
 	@Transactional
 	public User getUserBy(String username) throws TradesPeopleServiceException {
 		try {
 			return userDao.getUserBy(username);
 		} catch (TradesPeopleDaoException e) {
 			throw new TradesPeopleServiceException(e);
 		}
 	}
 
 	@Override
 	@Transactional
 	public User login(String username, byte[] pass)throws TradesPeopleServiceException {
          try {
 			User user=userDao.getUserBy(username);
 			if (user==null) {
 				ApiUtils.throwWrongUserNameException();
 			}
 			String password=CipherUtils.decrypt(user.getPassword());
			if (!password.equals(String.valueOf(pass))) {
 				ApiUtils.throwWrongPasswordException();
 			}
 			return user;
 		} catch (TradesPeopleDaoException e) {
 			throw new TradesPeopleServiceException(e);
 		}
 	}
 
 }
