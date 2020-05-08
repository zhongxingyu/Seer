 /**
  * This file is part of Waarp Project.
  * 
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
  * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
  * 
  * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
  * 
  * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Waarp . If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.waarp.common.crypto.ssl;
 
 import java.security.Security;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLEngine;
 
 import org.jboss.netty.handler.ssl.SslHandler;
 import org.waarp.common.logging.WaarpInternalLogger;
 import org.waarp.common.logging.WaarpInternalLoggerFactory;
 
 /**
  * SSL ContextFactory for Netty.
  * 
  * @author Frederic Bregier
  * 
  */
 public class WaarpSslContextFactory {
 	/**
 	 * Internal Logger
 	 */
 	private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
 			.getLogger(WaarpSslContextFactory.class);
 
 	/**
     *
     */
 	private static final String PROTOCOL = "TLS";
 
 	/**
     *
     */
 	private final SSLContext SERVER_CONTEXT;
 
 	/**
     *
     */
 	private final SSLContext CLIENT_CONTEXT;
 
 	private boolean needClientAuthentication = false;
 
 	/**
 	 * Create both CONTEXT
 	 * 
 	 * @param ggSecureKeyStore
 	 */
 	public WaarpSslContextFactory(WaarpSecureKeyStore ggSecureKeyStore) {
 		// Both construct Client and Server mode
 		SERVER_CONTEXT = initSslContextFactory(ggSecureKeyStore, true);
 		CLIENT_CONTEXT = initSslContextFactory(ggSecureKeyStore, false);
 	}
 
 	/**
 	 * Create only one of the CONTEXT
 	 * 
 	 * @param ggSecureKeyStore
 	 * @param serverMode
 	 */
 	public WaarpSslContextFactory(WaarpSecureKeyStore ggSecureKeyStore, boolean serverMode) {
 		if (serverMode) {
 			SERVER_CONTEXT = initSslContextFactory(ggSecureKeyStore, serverMode);
 			CLIENT_CONTEXT = null;
 		} else {
 			CLIENT_CONTEXT = initSslContextFactory(ggSecureKeyStore, serverMode);
 			SERVER_CONTEXT = null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param ggSecureKeyStore
 	 * @param serverMode
 	 * @return the SSLContext
 	 */
 	private SSLContext initSslContextFactory(WaarpSecureKeyStore ggSecureKeyStore,
 			boolean serverMode) {
 		String algorithm = Security
 				.getProperty("ssl.KeyManagerFactory.algorithm");
 		if (algorithm == null) {
 			algorithm = "SunX509";
 		}
 
 		SSLContext serverContext = null;
 		SSLContext clientContext = null;
 		if (serverMode) {
 			try {
 				// Initialize the SSLContext to work with our key managers.
 				serverContext = SSLContext.getInstance(PROTOCOL);
 				WaarpSecureTrustManagerFactory secureTrustManagerFactory =
 						ggSecureKeyStore.getSecureTrustManagerFactory();
 				needClientAuthentication = secureTrustManagerFactory.needAuthentication();
 				if (secureTrustManagerFactory.hasTrustStore()) {
 					logger.debug("Has TrustManager");
 					serverContext.init(ggSecureKeyStore.getKeyManagerFactory().getKeyManagers(),
 							secureTrustManagerFactory.getTrustManagers(), null);
 				} else {
 					logger.debug("No TrustManager");
 					serverContext.init(ggSecureKeyStore.getKeyManagerFactory().getKeyManagers(),
 							null, null);
 				}
 				return serverContext;
 			} catch (Exception e) {
 				logger.error("Failed to initialize the server-side SSLContext", e);
 				throw new Error("Failed to initialize the server-side SSLContext",
 						e);
 			}
 		} else {
 			try {
 				clientContext = SSLContext.getInstance(PROTOCOL);
 				WaarpSecureTrustManagerFactory secureTrustManagerFactory =
 						ggSecureKeyStore.getSecureTrustManagerFactory();
 				needClientAuthentication = secureTrustManagerFactory.needAuthentication();
 				if (secureTrustManagerFactory.hasTrustStore()) {
 					logger.debug("Has TrustManager");
 					clientContext.init(ggSecureKeyStore.getKeyManagerFactory().getKeyManagers(),
 							secureTrustManagerFactory.getTrustManagers(), null);
 				} else {
 					logger.debug("No TrustManager");
 					clientContext.init(ggSecureKeyStore.getKeyManagerFactory().getKeyManagers(),
 							null, null);
 				}
 				return clientContext;
 			} catch (Exception e) {
 				logger.error("Failed to initialize the client-side SSLContext", e);
 				throw new Error("Failed to initialize the client-side SSLContext",
 						e);
 			}
 		}
 	}
 
 	/**
 	 * @return the Server Context
 	 */
 	public SSLContext getServerContext() {
 		return SERVER_CONTEXT;
 	}
 
 	/**
 	 * @return the Client Context
 	 */
 	public SSLContext getClientContext() {
 		return CLIENT_CONTEXT;
 	}
 
 	/**
 	 * To be called before adding as first entry in the PipelineFactory as<br>
 	 * pipeline.addLast("ssl", sslhandler);<br>
 	 * 
 	 * @param serverMode
 	 *            True if in Server Mode, else False in Client mode
 	 * @param needClientAuth
 	 *            True if the client needs to be authenticated (only if serverMode is True)
 	 * @param renegotiationEnable
 	 *            True if you want to enable renegotiation (security issue CVE-2009-3555)
 	 * @return the sslhandler
 	 */
 	public SslHandler initPipelineFactory(boolean serverMode,
			boolean needClientAuth, boolean renegotiationEnable) {
 		// Add SSL handler first to encrypt and decrypt everything.
 		SSLEngine engine;
 		logger.debug("Has TrustManager? " + needClientAuth + " Is ServerMode? " + serverMode + " IsRenegotiation enable? "+renegotiationEnable);
 		if (serverMode) {
 			engine = getServerContext().createSSLEngine();
 			engine.setUseClientMode(false);
 			engine.setNeedClientAuth(needClientAuth);
 		} else {
 			engine = getClientContext().createSSLEngine();
 			engine.setUseClientMode(true);
 		}
		SslHandler handler = new SslHandler(engine);
 		// Set the RenegotiationEnable or not
 		handler.setEnableRenegotiation(renegotiationEnable);
 		return handler;
 	}
 
 	/**
 	 * To be called before adding as first entry in the PipelineFactory as<br>
 	 * pipeline.addLast("ssl", sslhandler);<br>
 	 * 
 	 * @param serverMode
 	 *            True if in Server Mode, else False in Client mode
 	 * @param needClientAuth
 	 *            True if the client needs to be authenticated (only if serverMode is True)
 	 * @param renegotiationEnable
 	 *            True if you want to enable renegotiation (security issue CVE-2009-3555)
 	 * @param host
 	 * 			Host for which a resume is allowed
 	 * @param port
 	 * 			port associated with the host for which a resume is allowed
 	 * @return the sslhandler
 	 */
 	public SslHandler initPipelineFactory(boolean serverMode,
 			boolean needClientAuth, boolean renegotiationEnable,
			String host, int port) {
 		// Add SSL handler first to encrypt and decrypt everything.
 		SSLEngine engine;
 		logger.debug("Has TrustManager? " + needClientAuth + " Is ServerMode? " + serverMode);
 		if (serverMode) {
 			engine = getServerContext().createSSLEngine(host, port);
 			engine.setUseClientMode(false);
 			engine.setNeedClientAuth(needClientAuth);
 		} else {
 			engine = getClientContext().createSSLEngine(host, port);
 			engine.setUseClientMode(true);
 		}
		SslHandler handler = new SslHandler(engine);
 		// Set the RenegotiationEnable or not
 		handler.setEnableRenegotiation(renegotiationEnable);
 		return handler;
 	}
 
 	/**
 	 * 
 	 * @return True if the associated KeyStore has a TrustStore
 	 */
 	public boolean needClientAuthentication() {
 		return needClientAuthentication;
 	}
 }
