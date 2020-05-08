 package com.robonobo.wang.server;
 
 import javax.management.MBeanServer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.remoting.*;
 import org.jboss.remoting.callback.InvokerCallbackHandler;
 import org.jboss.remoting.transport.Connector;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.TransactionCallback;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import com.robonobo.common.remote.RemoteCall;
 import com.robonobo.wang.server.dao.UserAccountDao;
 
 /**
  * The server end of a remote wang service (client end is RemoteWangFacade in
  * midas-model project)
  * 
  * @author macavity
  * 
  */
 public class RemoteWangService implements ServerInvocationHandler, InitializingBean, DisposableBean {
 	private Connector connector;
 	private String secret;
 	@Autowired
 	private UserAccountDao uaDao;
 	@Autowired
 	private PlatformTransactionManager transactionManager;
 	private TransactionTemplate transTemplate;
 	private Log log = LogFactory.getLog(getClass());
 
 	public RemoteWangService(String url, String secret) throws Exception {
 		this.secret = secret;
 		log.info("Setting up remote wang service on " + url);
 		InvokerLocator locator = new InvokerLocator(url);
 		connector = new Connector();
 		connector.setInvokerLocator(locator.getLocatorURI());
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		transTemplate = new TransactionTemplate(transactionManager);
 		log.info("Starting remote wang service");
 		connector.start();
 		connector.addInvocationHandler("wang", this);
 	}
 	
 	@Override
 	public void destroy() throws Exception {
 		connector.stop();
 	}
 
 	public Object invoke(InvocationRequest req) throws Throwable {
 		Object obj = req.getParameter();
 		if (!(obj instanceof RemoteCall)) {
 			log.error("Remote invocation with parameter " + obj.getClass().getName());
 			throw new IllegalArgumentException("Invalid param");
 		}
 		final RemoteCall params = (RemoteCall) obj;
 		if (!secret.equals(params.getSecret())) {
 			log.error("Remote invocation with invalid secret '" + params.getSecret() + "'");
 			throw new IllegalArgumentException("Invalid secret");
 		}
 		final String method = params.getMethodName();
 		// Make sure everything happens inside a transaction
 		return transTemplate.execute(new TransactionCallback<Object>() {
 			@Override
 			public Object doInTransaction(TransactionStatus arg0) {
 				try {
 					if (method.equals("getBalance")) {
 						String email = (String) params.getArg();
 						String passwd = (String) params.getExtraArgs().get(0);
 						return getBalance(email, passwd);
 					} else if (method.equals("changePassword")) {
 						String email = (String) params.getArg();
 						String oldPasswd = (String) params.getExtraArgs().get(0);
 						String newPasswd = (String) params.getExtraArgs().get(1);
 						changePassword(email, oldPasswd, newPasswd);
 						return null;
 					} else if (method.equals("countUsers")) {
 						return countUsers();
 					} else if(method.equals("createUser")) {
 						String email = (String) params.getArg();
 						String friendlyName = (String) params.getExtraArgs().get(0);
 						String password = (String) params.getExtraArgs().get(1);
 						createUser(email, friendlyName, password);
 						return null;
 					} else if(method.equals("topUpBalance")) {
 						String email = (String) params.getArg();
 						double amount = Double.parseDouble((String) params.getExtraArgs().get(0));
 						topUpBalance(email, amount);
 						return null;
 					} else
 						throw new IllegalArgumentException("Invalid method");
 				} catch (Exception e) {
 					log.error("Caught exception servicing remote request '"+method+"'", e);
 					// By default, the transactiontemplate only rolls back for RuntimeExceptions, and I can't figure out how
 					// to change this...
 					throw new RuntimeException(e);
 				}
 			}
 		});
 	}
 
 	private void topUpBalance(String email, double amount) throws Exception {
 		UserAccount ua = uaDao.getAndLockUserAccount(email);
 		try {
 			ua.setBalance(ua.getBalance()+amount);
 		} finally {
 			uaDao.putUserAccount(ua);
 		}
 	}
 
 	private void createUser(String email, String friendlyName, String password) throws Exception {
 		uaDao.createUserAccount(friendlyName, email, password);
 	}
 	
 	private Double getBalance(String email, String passwd) throws Exception {
 		UserAccount ua = uaDao.getUserAccount(email);
 		if (!ua.getPassword().equals(passwd))
 			throw new IllegalAccessException("Invalid password");
 		return ua.getBalance();
 	}
 
 	private void changePassword(String email, String oldPasswd, String newPasswd) throws Exception {
 		UserAccount ua = uaDao.getAndLockUserAccount(email);
 		try {
 			if (!ua.getPassword().equals(oldPasswd))
 				throw new IllegalAccessException("Invalid password");
 			ua.setPassword(newPasswd);
 		} finally {
 			uaDao.putUserAccount(ua);
 		}
 	}
 
 	private Long countUsers() throws Exception {
 		return uaDao.countUsers();
 	}
 
 	public void addListener(InvokerCallbackHandler arg0) {
 		// Do nothing
 	}
 
 	public void removeListener(InvokerCallbackHandler arg0) {
 		// Do nothing
 	}
 
 	public void setInvoker(ServerInvoker arg0) {
 		// Do nothing
 	}
 
 	public void setMBeanServer(MBeanServer arg0) {
 		// Do nothing
 	}
 }
