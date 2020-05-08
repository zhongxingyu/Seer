 /**
  * JBoss, Home of Professional Open Source. Copyright 2012, Red Hat, Inc., and
  * individual contributors as indicated by the @author tags. See the
  * copyright.txt file in the distribution for a full listing of individual
  * contributors.
  * 
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this software; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
  * site: http://www.fsf.org.
  */
 package org.jboss.cluster.proxy;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.cluster.proxy.container.MCMNodeService;
 import org.jboss.logging.Logger;
 
 /**
  * {@code ProxyMain}
  * 
  * Created on Jun 18, 2012 at 4:18:59 PM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class ProxyMain {
 
 	private static final String DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol";
 	private static final String DEFAULT_MCM_PROTOCOL = "org.jboss.cluster.proxy.http11.Http11NioProtocol";
 	private static final String DEFAULT_SCHEME = "http";
 	private static final List<Thread> threads = new ArrayList<>();
 	private static final List<WebConnectorService> services = new ArrayList<>();
 	protected static final NodeService NODE_SERVICE = new NodeService();
 	protected static final ConnectionManager CONNECTION_MANAGER = new ConnectionManager();
 	public static final int DEFAULT_MCM_PORT = 6666;
 	public static final int DEFAULT_HTTP_PORT = 8080;
 
 	private static final Logger logger = Logger.getLogger(ProxyMain.class);
 	private static final String CONFIG_PATH = "conf" + File.separatorChar + "config.properties";
 
 	/**
 	 * Create a new instance of {@code ProxyMain}
 	 */
 	public ProxyMain() {
 		super();
 	}
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		long time = System.currentTimeMillis();
 
 		// Loading configuration first
 
 		try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
 			logger.info("Loading configuration");
 			System.getProperties().load(fis);
 		} catch (Throwable t) {
 			logger.error("Unable to load configurations", t);
 			System.exit(-1);
 		}
 
 		try {
 			String protocol = System.getProperty("http-protocol", DEFAULT_PROTOCOL);
 			String scheme = System.getProperty("org.apache.coyote.http11.SCHEME", DEFAULT_SCHEME);
 			// Creating the web connector service
 			// use the static NodeService if configured.
			WebConnectorService service = new WebConnectorService(protocol, scheme, new MCMNodeService());
 			// configure the web connector service
 
 			// Setting the address (host:port)
 			int port = DEFAULT_HTTP_PORT;
 			String portStr = System.getProperty("org.apache.tomcat.util.net.PORT");
 
 			if (portStr != null) {
 				try {
 					port = Integer.valueOf(portStr);
 				} catch (Throwable t) {
 					logger.error(t.getMessage(), t);
 					System.setProperty("org.apache.tomcat.util.net.PORT", "" + DEFAULT_HTTP_PORT);
 				}
 			} else {
 				System.setProperty("org.apache.tomcat.util.net.PORT", "" + DEFAULT_HTTP_PORT);
 			}
 
 			String hostname = System.getProperty("org.apache.tomcat.util.net.ADDRESS");
 			if (hostname == null) {
 				hostname = "0.0.0.0";
 				System.setProperty("org.apache.tomcat.util.net.ADDRESS", hostname);
 			}
 			// Setting the address
 			service.setAddress(new InetSocketAddress(hostname, port));
 
 			// TODO finish configuration setup
 
 			// Starting the web connector service
 			service.start();
 			services.add(service);
 
 			// Add the static node somewhere and otherwise add the MCM one.
 			// Adding node web connector service
 
 			protocol = System.getProperty("http-protocol", DEFAULT_MCM_PROTOCOL);
 			scheme = System.getProperty("org.jboss.cluster.proxy.http11.SCHEME", DEFAULT_SCHEME);
 			// Creating the web connector service
			WebConnectorService nodeService = new WebConnectorService(protocol, scheme, null);
 			// configure the web connector service
 
 			// Setting the address (host:port)
 			int mcmPort = DEFAULT_MCM_PORT;
 			String mcmPortStr = System.getProperty("org.jboss.cluster.proxy.net.PORT");
 			if (mcmPortStr != null) {
 				try {
 					mcmPort = Integer.valueOf(mcmPortStr);
 				} catch (Throwable t) {
 					logger.error(t.getMessage(), t);
 					System.setProperty("org.jboss.cluster.proxy.net.PORT", "" + DEFAULT_MCM_PORT);
 				}
 			} else {
 				System.setProperty("org.jboss.cluster.proxy.net.PORT", "" + DEFAULT_MCM_PORT);
 			}
 
 			// Retrieve the MCMP hostname
 			String mcmHostname = System.getProperty("org.jboss.cluster.proxy.net.ADDRESS");
 			if (mcmHostname == null) {
 				mcmHostname = "0.0.0.0";
 				System.setProperty("org.jboss.cluster.proxy.net.ADDRESS", mcmHostname);
 			}
 
 			nodeService.setAddress(new InetSocketAddress(mcmHostname, mcmPort));
 
 			// TODO finish configuration setup
 
 			// Starting the web connector service
 			nodeService.start();
 			services.add(nodeService);
 
 		} catch (Throwable e) {
 			logger.error("creating protocol handler error", e);
 			e.printStackTrace();
 			System.exit(-1);
 		}
 
 		threads.add(new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
 					String line = null;
 					while ((line = br.readLine()) != null) {
 						line = line.trim();
 						if (line.isEmpty()) {
 							continue;
 						}
 						if (line.equalsIgnoreCase("stop") || line.equalsIgnoreCase("quit")) {
 							logger.info("Processing command '" + line + "'");
 							break;
 						} else {
 							logger.error("Unknow command : " + line);
 						}
 					}
 				} catch (IOException e) {
 					logger.error(e.getMessage(), e);
 				}
 				System.exit(0);
 			}
 		}));
 
 		time = System.currentTimeMillis() - time;
 		logger.info("JBoss Mod Cluster Proxy started in " + time + "ms");
 		// Add shutdown hook
 		addShutdownHook();
 		// Start all threads
 		startThreads();
 	}
 
 	/**
 	 * Add shutdown hook
 	 */
 	private static void addShutdownHook() {
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				try {
 					long time = System.currentTimeMillis();
 					logger.info("Stopping JBoss Mod Cluster Proxy....");
 					for (WebConnectorService service : services) {
 						service.stop();
 					}
 
 					interruptThreads();
 					logger.info("JBoss Mod Cluster Proxy stopped in "
 							+ (System.currentTimeMillis() - time) + "ms");
 				} catch (Throwable e) {
 					logger.fatal(e.getMessage(), e);
 				}
 			}
 		});
 	}
 
 	/**
 	 * @throws Exception
 	 */
 	private static void startThreads() throws Exception {
 		for (Thread t : threads) {
 			t.start();
 		}
 		for (Thread t : threads) {
 			t.join();
 		}
 	}
 
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	private static void interruptThreads() throws Exception {
 		for (Thread t : threads) {
 			t.interrupt();
 		}
 	}
 
 }
