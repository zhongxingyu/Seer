 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.koji;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.fedoraproject.eclipse.packager.FedoraSSL;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.koji.preferences.PreferencesConstants;
 
 /**
  * Koji hub client which uses certificate based
  * authentication (SSL).
  */
 public class KojiHubClient extends AbstractKojiHubClient {
 	
 	
 	/**
 	 * Default 0-arg constructor.
 	 */
 	public KojiHubClient() {
 		super();
 	}
 	
 	/**
 	 * Constructor taking a Koji hub and Web URL.
 	 * 
 	 * @param hubUrl 
 	 * @param webUrl 
 	 * 
 	 * @throws KojiHubClientInitException if either one of the 
 	 *         provided URLs is invalid.
 	 */
 	public KojiHubClient(String hubUrl, String webUrl) throws KojiHubClientInitException {
 		setHubUrl(hubUrl);
 		setWebUrl(webUrl);
 	}
 	
 	/**
 	 * SSL implementation of XMLRPC based login()
 	 * 
 	 * Login to remote URL specified by constructor or setter
 	 * using the SSL client certificate. It is the user's
 	 * responsibility to initialize the SSL connection as
 	 * appropriate.
 	 * 
 	 * @see IKojiHubClient#login()
 	 * 
 	 * @return session key and session id as a Map.
 	 * @throws IllegalStateException if hub URL has not been
 	 *         configured.
 	 * @throws KojiHubClientLoginException if login fails for some
 	 *         other reason.
 	 */
 	@Override
 	public HashMap<?, ?> login() throws KojiHubClientLoginException {
 		if (getHubUrl() == null) {
 			throw new IllegalStateException("Hub URL must be set before trying to login");
 		}
 		// Initialize SSL connection
 		try {
 			initSSLConnection(
 				new File(FedoraSSL.DEFAULT_CERT_FILE), 
 				new File(FedoraSSL.DEFAULT_UPLOAD_CA_CERT),
 				new File(FedoraSSL.DEFAULT_SERVER_CA_CERT));
 		} catch (KojiHubClientInitException e) {
 			throw new KojiHubClientLoginException(e);
 		}
 		return doSslLogin();
 	}
 	
 	/**
 	 * Set Koji Web- and hub URL according to preferences 
 	 */
 	@Override
 	public synchronized void setUrlsFromPreferences() throws KojiHubClientInitException {
 		// Sets Koji host according to preferences and statically sets kojiHubUrl and kojiWebUrl
 		IPreferenceStore kojiPrefStore = PackagerPlugin.getDefault().getPreferenceStore();
 		String preference = kojiPrefStore.getString(PreferencesConstants.PREF_KOJI_HUB_URL);
 		// Eclipse does not seem to store default preference values in metadata.
 		if (preference.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
 			setHubUrl(PreferencesConstants.DEFAULT_KOJI_HUB_URL);
 		} else {
			setHubUrl(kojiPrefStore.getString(PreferencesConstants.PREF_KOJI_HUB_URL));
 		}
 		preference = kojiPrefStore.getString(PreferencesConstants.PREF_KOJI_WEB_URL);
 		// Eclipse does not seem to store default preference values in metadata.
 		if (preference.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			setHubUrl(PreferencesConstants.DEFAULT_KOJI_WEB_URL);
 		} else {
			setHubUrl(kojiPrefStore.getString(PreferencesConstants.PREF_KOJI_WEB_URL));
 		}	
 	}
 
 	/**
 	 * Log on to URL using SSL.
 	 * @throws KojiHubClientLoginException if login returns something unexpected.
 	 */
 	private HashMap<?, ?> doSslLogin() throws KojiHubClientLoginException {
 		// prepare XMLRPC
 		setupXmlRpcConfig();
 		setupXmlRpcClient();
 		// do the login
 		ArrayList<String> params = new ArrayList<String>();
 		Object result = null;
 		HashMap<?, ?> hashMap = null;
 		try {
 			result = xmlRpcClient.execute("sslLogin", params); //$NON-NLS-1$
 			hashMap = (HashMap<?, ?>)result;
 		} catch (ClassCastException e) {
 			// TODO: Externalize
 			throw new KojiHubClientLoginException("Login returned unexpected result");
 		} catch (XmlRpcException e) {
 			throw new KojiHubClientLoginException(e);
 		}
 		return hashMap;
 	}
 
 	/**
 	 * Initialize SSL connection
 	 */
 	private void initSSLConnection(File fedoraCert, File fedoraUploadCert,
 			File fedoraServerCert) throws KojiHubClientInitException {
 		FedoraSSL connection = new FedoraSSL(fedoraCert, fedoraUploadCert, fedoraServerCert);
 		try {
 			connection.initSSLConnection();
 		} catch (GeneralSecurityException e) {
 			throw new KojiHubClientInitException(e);
 		} catch (IOException e) {
 			throw new KojiHubClientInitException(e);
 		}
 	}
 	
 }
