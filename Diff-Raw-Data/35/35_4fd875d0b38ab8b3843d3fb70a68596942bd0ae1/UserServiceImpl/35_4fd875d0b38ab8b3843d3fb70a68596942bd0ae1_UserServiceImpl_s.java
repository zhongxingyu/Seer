 package org.gwtapp.startapp.server.service;
 
 import javax.persistence.EntityExistsException;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.RollbackException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.gwtapp.core.rpc.exception.NotImplementedException;
 import org.gwtapp.core.rpc.exception.RpcException;
 import org.gwtapp.extension.user.client.api.UserService;
 import org.gwtapp.extension.user.client.data.User;
 import org.gwtapp.extension.user.client.data.exception.UserValidationException;
 import org.gwtapp.extension.user.client.data.exception.UserValidationException.Email;
 import org.gwtapp.extension.user.client.data.exception.UserValidationException.Login;
 import org.gwtapp.extension.user.server.local.stub.UserAdd;
 import org.gwtapp.validation.rpc.exception.ValidationException;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.wideplay.warp.persist.Transactional;
 
 @Singleton
 public class UserServiceImpl implements UserService, UserAdd {
 
 	private static final Logger log = Logger.getLogger(UserServiceImpl.class);
 
 	@Inject
 	Provider<EntityManager> em;
 
 	@Override
 	public User getUser(String login) throws RpcException {
 		log.warn("Not implemented");
 		throw new NotImplementedException();
 	}
 
 	@Override
 	public long addUser(User user) throws RpcException {
 		try {
 			validateBeforeAddUser(user);
 			persistUser(user);
 		} catch (RollbackException e) {
 			if (e.getCause() instanceof EntityExistsException) {
 				UserValidationException validation = new UserValidationException();
 				if (!isLoginAvailable(user.getLogin())) {
 					validation.addLogin(Login.ALREADY_EXISTS);
 				}
 				if (!isEmailAvailable(user.getEmail())) {
 					validation.addEmail(Email.ALREADY_EXISTS);
 				}
 				validation.validate();
 			} else {
 				throw e;
 			}
 		}
 		log.debug("persisted id=" + user.getId());
 		return user.getId();
 	}
 
 	@Override
 	public void validateBeforeAddUser(User user) throws UserValidationException {
 		UserValidationException validation = new UserValidationException();
 		try {
 			if (StringUtils.isEmpty(user.getLogin())) {
 				validation.addLogin(Login.EMPTY);
 			} else {
 				if (user.getLogin().matches(
 						UserValidationException.ANY_UPPER_CASE_REGEXP)) {
 					validation.addLogin(Login.NOT_LOWER_CASE);
 				}
 				if (!user.getLogin().matches(
 						UserValidationException.ONLY_LETTERS_REGEXP)) {
 					validation.addLogin(Login.NOT_LETTERS_ONLY);
 				}
 				if (user.getLogin().length() < 3) {
 					validation.addLogin(Login.TOO_SHORT);
 				} else if (user.getLogin().length() > 25) {
 					validation.addLogin(Login.TOO_LONG);
 				}
 				if (!isLoginAvailable(user.getLogin())) {
 					validation.addLogin(Login.ALREADY_EXISTS);
 				}
 			}
 			if (StringUtils.isEmpty(user.getEmail())) {
 				validation.addEmail(Email.EMPTY);
 			} else {
 				if (!user.getEmail().matches(
 						UserValidationException.EMAIL_REGEXP)) {
 					validation.addEmail(Email.INVALID);
 				}
 				if (!isEmailAvailable(user.getEmail())) {
 					validation.addEmail(Email.ALREADY_EXISTS);
 				}
 			}
 			validation.validate();
 		} catch (ValidationException e) {
 			throw e;
 		} catch (RuntimeException e) {
 			log.error("", e);
 			throw e;
 		}
 	}
 
 	@Transactional
 	protected void persistUser(User user) {
 		em.get().persist(user);
 	}
 
 	protected boolean isLoginAvailable(String login) {
		String q = "SELECT u FROM UserEntity u WHERE u.login = '" + login + "'";
 		Query query = em.get().createQuery(q);
 		query.setMaxResults(1);
 		return query.getResultList().isEmpty();
 	}
 
 	protected boolean isEmailAvailable(String email) {
		String q = "SELECT u FROM UserEntity u WHERE u.email = '" + email + "'";
 		Query query = em.get().createQuery(q);
 		query.setMaxResults(1);
 		return query.getResultList().isEmpty();
 	}
 }
