 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.security.simpleservices.authentication.loginmodule;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 import javax.security.auth.login.LoginException;
 import javax.security.auth.spi.LoginModule;
 
 import org.eclipse.riena.internal.security.simpleservices.Activator;
 import org.eclipse.riena.security.common.authentication.SimplePrincipal;

import org.eclipse.equinox.log.Logger;
 import org.osgi.service.log.LogService;
 
 /**
  * Test module that implements the JAAS LoginModule interface
  * 
  */
 public class SampleLoginModule implements LoginModule {
 
 	private Subject subject;
 	private CallbackHandler callbackHandler;
	private Map<String, ?> sharedState;
	private Map<String, ?> options;

	// configurable option
	private boolean debug = false;
 
 	private String username;
 	private String password;
 
 	private Properties accounts;
 
 	private static final Logger LOGGER = Activator.getDefault().getLogger(SampleLoginModule.class);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.security.auth.spi.LoginModule#abort()
 	 */
 	public boolean abort() throws LoginException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.security.auth.spi.LoginModule#commit()
 	 */
 	public boolean commit() throws LoginException {
 		subject.getPrincipals().add(new SimplePrincipal(username));
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject
 	 * , javax.security.auth.callback.CallbackHandler, java.util.Map,
 	 * java.util.Map)
 	 */
 	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
 			Map<String, ?> options) {
 		this.subject = subject;
 		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;
 
 		try {
 			accounts = loadProperties((String) options.get("accounts.file")); //$NON-NLS-1$
 		} catch (IOException e) {
 			e.printStackTrace();
 		}

		// initialize any configured options
		debug = Boolean.valueOf((String) options.get("debug")); //$NON-NLS-1$
 	}
 
 	private Properties loadProperties(String path) throws IOException {
 		URL url = Activator.getDefault().getContext().getBundle().getEntry(path);
 		Properties properties = new Properties();
 		properties.load(url.openStream());
 		return properties;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.security.auth.spi.LoginModule#login()
 	 */
 	public boolean login() throws LoginException {
 		Callback[] callbacks = new Callback[2];
 		callbacks[0] = new NameCallback("username: "); //$NON-NLS-1$
 		callbacks[1] = new PasswordCallback("password: ", false); //$NON-NLS-1$
 		if (callbackHandler == null) {
 			LOGGER.log(LogService.LOG_ERROR, "callbackhandler cant be null"); //$NON-NLS-1$
 			return false;
 		}
 		try {
 			callbackHandler.handle(callbacks);
 			username = ((NameCallback) callbacks[0]).getName();
 			password = new String(((PasswordCallback) callbacks[1]).getPassword());
 			String psw = (String) accounts.get(username);
			if (psw != null && psw.equals(password)) {
				return true;
			} else {
				return false;
			}
 			//			if (username != null && password != null) {
 			//				if (username.equals("testuser") && password.equals("testpass")) { //$NON-NLS-1$ //$NON-NLS-2$
 			//					return true;
 			//				} else {
 			//					if (username.equals("testuser2") && password.equals("testpass2")) { //$NON-NLS-1$ //$NON-NLS-2$
 			//						return true;
 			//					} else {
 			//						if (username.equals("cca") && password.equals("christian")) { //$NON-NLS-1$ //$NON-NLS-2$
 			//							return true;
 			//						}
 			//					}
 			//				}
 			//			}
 			//
 			//			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} catch (UnsupportedCallbackException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.security.auth.spi.LoginModule#logout()
 	 */
 	public boolean logout() throws LoginException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
