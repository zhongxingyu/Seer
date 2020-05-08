 package deus.gatekeeper.cerberus.impl;
 
 import java.util.List;
 import java.util.Vector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import deus.core.access.storage.api.user.api.AccountDao;
 import deus.core.access.storage.api.user.api.LocalUserDao;
 import deus.gatekeeper.cerberus.Cerberus;
 import deus.gatekeeper.cerberus.LoginCredentialChecker;
 import deus.gatekeeper.cerberus.UserLoginStateObserver;
 import deus.gatekeeper.puddle.LoginCredentials;
 import deus.model.user.Account;
 import deus.model.user.id.UserId;
 
 @Component("cerberus")
 public class CerberusImpl implements Cerberus {
 
 	private final Logger logger = LoggerFactory.getLogger(CerberusImpl.class);
 
 	private final List<UserLoginStateObserver> observers;
 
 	@Autowired
 	private LoginCredentialChecker loginCredentialChecker;
 
 	@Autowired
 	private LocalUserDao localUserDao;
 
 	@Autowired
 	private AccountDao accountDao;
 
 
 	public CerberusImpl() {
 		super();
 		this.observers = new Vector<UserLoginStateObserver>();
 	}
 
 
 	@Override
	public void login(LoginCredentials credentials) {
 		if (!loginCredentialChecker.isValid(credentials))
 			// FIXME: think about what to do here
 			;
 
 		// TODO: do more login stuff, that is necessary
 		UserId userId = localUserDao.getByNaturalId(credentials.getLocalUsername());
 
 		logger.debug("user with id {} logged in", userId);
 
 
 		storeLoggedInState(userId, true);
 
 
 		// FIXME: implement thread safe notifying (see RegistratorImpl)
 		for (UserLoginStateObserver observer : observers)
 			observer.loggedIn(userId);
 	}
 
 
 	private void storeLoggedInState(UserId userId, boolean loggedIn) {
 		Account account = accountDao.getByNaturalId(userId);
 		account.setLoggedIn(loggedIn);
 		accountDao.updateEntity(account);
 	}
 
 
 	@Override
 	public void logout(String localUsername) {
 		UserId userId = localUserDao.getByNaturalId(localUsername);
 
 		storeLoggedInState(userId, false);
 
 		logger.debug("user with id {} logged out", userId);
 
 		// FIXME: implement thread safe notifying (see RegistratorImpl)
 		for (UserLoginStateObserver observer : observers)
 			observer.loggedOut(userId);
 	}
 
 
 	@Override
 	public void addUserLoginStateObserver(UserLoginStateObserver observer) {
 		observers.add(observer);
 	}
 
 
 	@Override
 	public void removeUserLoginStateObserver(UserLoginStateObserver observer) {
 		if (observers.remove(observer) == false)
 			throw new IllegalArgumentException("observer was not added!");
 	}
 
 }
