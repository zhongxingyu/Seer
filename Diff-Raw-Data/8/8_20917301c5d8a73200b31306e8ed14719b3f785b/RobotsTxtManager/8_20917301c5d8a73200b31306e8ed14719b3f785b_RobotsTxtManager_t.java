 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.filter.robots.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.ConnectException;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLDecoder;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.Status;
 
 import org.apache.commons.httpclient.CircularRedirectException;
 import org.apache.commons.httpclient.ConnectTimeoutException;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.InvalidRedirectLocationException;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.ProxyHost;
 import org.apache.commons.httpclient.RedirectException;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.Constants;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.monitor.Monitorable;
 import org.osgi.service.monitor.StatusVariable;
 import org.paxle.filter.robots.IRobotsTxtManager;
 import org.paxle.filter.robots.impl.rules.AllowRule;
 import org.paxle.filter.robots.impl.rules.DisallowRule;
 import org.paxle.filter.robots.impl.rules.RobotsTxt;
 import org.paxle.filter.robots.impl.rules.RuleBlock;
 import org.paxle.filter.robots.impl.store.IRuleStore;
 
 public class RobotsTxtManager implements IRobotsTxtManager, ManagedService, Monitorable {
 	private static final String CACHE_NAME = "robotsTxtCache";
 	
 	/**
 	 * Systemwidth unique ID
 	 * @see Constants#SERVICE_PID
 	 */
 	private static final String PID = IRobotsTxtManager.class.getName();
 	
 	/* =========================================================
 	 * Config Properties
 	 * ========================================================= */	
 	private static final String PROP_CONNECTION_TIMEOUT 	= PID + '.' + "connectionTimeout";
 	private static final String PROP_SOCKET_TIMEOUT 		= PID + '.' + "socketTimeout";
 	private static final String PROP_MAXCONNECTIONS_TOTAL 	= PID + '.' + "maxConnectionsTotal";
 	private static final String PROP_MAX_CACHE_SIZE 		= PID + '.' + "maxCacheSize";
 	
 	private static final String PROP_PROXY_USE 				= PID + '.' + "useProxy";
 	private static final String PROP_PROXY_HOST 			= PID + '.' + "proxyHost";
 	private static final String PROP_PROXY_PORT 			= PID + '.' + "proxyPort";
 	private static final String PROP_PROXY_USER 			= PID + '.' + "proxyUser";
 	private static final String PROP_PROXY_PASSWORD 		= PID + '.' + "proxyPassword";
 	
 	private static final String PROP_WORKER_MAX_ALIVE       = PID + '.' + "threads.maxAlive";
 	private static final String PROP_WORKER_MAX_IDLE        = PID + '.' + "threads.maxIdle";
 	
 	private static final String ROBOTS_AGENT_PAXLE = "paxle";
 	
 	/* =========================================================
 	 * OSGi Monitorable CONSTANTS
 	 * ========================================================= */		
 	public static final String MONITOR_PID = "org.paxle.robots-txt";
 	private static final String MONITOR_STORE_SIZE = "store.size";
 	
 	private static final String MONITOR_JOBS_PREFIX = "jobs";
 	private static final String MONITOR_JOBS_ACTIVE = MONITOR_JOBS_PREFIX + ".active";
 	private static final String MONITOR_JOBS_IDLE = MONITOR_JOBS_PREFIX + ".idle";
 	private static final String MONITOR_JOBS_MAX = MONITOR_JOBS_PREFIX + ".max";
 	private static final String MONITOR_JOBS_PENDING = MONITOR_JOBS_PREFIX + ".pending";
 	private static final String MONITOR_JOBS_TOTAL = MONITOR_JOBS_PREFIX + ".total";
 	
 	/**
 	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
 	 */
 	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(Arrays.asList(new String[] {
 			MONITOR_STORE_SIZE,
 			MONITOR_JOBS_ACTIVE,
 			MONITOR_JOBS_IDLE,
 			MONITOR_JOBS_MAX,
 			MONITOR_JOBS_PENDING,
 			MONITOR_JOBS_TOTAL
 	}));	
 	
 	/**
 	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
 	 */
 	private static final HashMap<String, String> VAR_DESCRIPTIONS = new HashMap<String, String>();
 	static {
 		VAR_DESCRIPTIONS.put(MONITOR_STORE_SIZE, "Known robots.txt definitions");
 		VAR_DESCRIPTIONS.put(MONITOR_JOBS_ACTIVE, "Number of jobs currently running");
 		VAR_DESCRIPTIONS.put(MONITOR_JOBS_IDLE, "Number of idle jobs");
 		VAR_DESCRIPTIONS.put(MONITOR_JOBS_MAX, "Maximum allowed number of active jobs");
 		VAR_DESCRIPTIONS.put(MONITOR_JOBS_PENDING, "Number of jobs waiting for execution");
 		VAR_DESCRIPTIONS.put(MONITOR_JOBS_TOTAL, "Total number of executed jobs");
 	}	
 
 	/**
 	 * Lock object required to synchronize functions affected by {@link #updated(Dictionary)} 
 	 * 
 	 * @see #updated(Dictionary)
 	 * @see #getFromCache(String)
 	 * @see #putIntoCache(String, RobotsTxt)
 	 * @see #getFromWeb(URI)
 	 * 
 	 * @see #httpClient
 	 * @see #cache
 	 */
 	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 	
 	/**
 	 * @see #rwl
 	 */
 	private final Lock r = rwl.readLock();
 	
 	/**
 	 * @see #rwl
 	 */
 	private final Lock w = rwl.writeLock();
 	
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 
 	/**
 	 * The cachemanager to use
 	 */
 	private CacheManager manager = null;
 
 	/**
 	 * A cach to hold {@link RobotsTxt} objects in memory
 	 */
 	private Cache cache = null;
 
 	/**
 	 * Component to read and write {@link RobotsTxt} objects
 	 */
 	private IRuleStore loader;
 	
 	/**
 	 * Connection manager used for http connection pooling
 	 */
 	private MultiThreadedHttpConnectionManager connectionManager = null;
 
 	/**
 	 * http client class
 	 */
 	private HttpClient httpClient = null;	
 
 	/**
 	 * Thread pool
 	 */
 	private final ThreadPoolExecutor execService;
 	
 	/**
 	 * @param path the path where the {@link RobotsTxt} objects should be stored
 	 */
 	public RobotsTxtManager(IRuleStore loader) {
 		// data storage
 		this.loader = loader;
 
 		// configure caching manager
 		this.manager = CacheManager.getInstance();
 		
 		// init threadpool
 		final int nThreads = 20;
 		this.execService = new ThreadPoolExecutor(nThreads, nThreads,
                 0L, TimeUnit.MILLISECONDS,
                 new LinkedBlockingQueue<Runnable>());
 		
 		// configure with default configuration
 		try {
 			this.updated(this.getDefaults());
 		} catch (ConfigurationException e) {
 			throw new RuntimeException("error configuring with defaults: " + e.getReason(), e);
 		}
 	}
 	
 	public void terminate() {
 		if (this.manager.getStatus().equals(Status.STATUS_ALIVE)) {
 			// clear cache
 			this.manager.removeCache(CACHE_NAME);
 		}
 		this.manager = null;
 		
 
 		// cleanup http-client
 		if (this.connectionManager != null) {
 			this.connectionManager.shutdown();
 			this.connectionManager = null;
 			this.httpClient = null;
 		}		
 		
 		// shutdown exec-service
 		// XXX maybe we should use shutdownNow here?
 		this.execService.shutdown();
 	}
 
 	/**
 	 * This method is only used for testing.
 	 * @return the {@link Cache} to use
 	 */
 	Cache getCache() {
 		return this.cache;
 	}
 	
 	/**
 	 * @return the default configuration of this service
 	 */
 	public Hashtable<String,Object> getDefaults() {
 		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
 		
 		// default connection manager properties
 		defaults.put(PROP_MAXCONNECTIONS_TOTAL, Integer.valueOf(MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS));
 		defaults.put(PROP_CONNECTION_TIMEOUT, Integer.valueOf(15000));
 		defaults.put(PROP_SOCKET_TIMEOUT, Integer.valueOf(15000));
 		
 		// default cache props
 		defaults.put(PROP_MAX_CACHE_SIZE, Integer.valueOf(1000));
 		
 		// default proxy settings
 		defaults.put(PROP_PROXY_USE, Boolean.FALSE);
 		defaults.put(PROP_PROXY_HOST, "");
 		defaults.put(PROP_PROXY_PORT, Integer.valueOf(3128));		// default squid port
 		defaults.put(PROP_PROXY_USER, "");
 		defaults.put(PROP_PROXY_PASSWORD, "");
 		
 		// default thread pool executor settings
 		defaults.put(PROP_WORKER_MAX_ALIVE, Integer.valueOf(20));
 		defaults.put(PROP_WORKER_MAX_IDLE, Integer.valueOf(20));
 		
 		// a systemwidth unique ID needed by the CM
 		defaults.put(Constants.SERVICE_PID, IRobotsTxtManager.class.getName());
 		
 		return defaults;
 	}	
 	
 	/**
 	 * @see ManagedService#updated(Dictionary)
 	 */
 	@SuppressWarnings("unchecked")		// we're only implementing an interface
 	public void updated(Dictionary configuration) throws ConfigurationException {
 		// our caller catches all runtime-exceptions and silently ignores them, leaving us confused behind,
 		// so this try/catch-block exists for debugging purposes
 		try {
 			if (configuration == null) {
 				logger.warn("updated configuration is null");
 				/*
 				 * Generate default configuration
 				 */
 				configuration = this.getDefaults();
 			}
 			
 			// enter critical section
 			this.w.lock();
 			
 			// remove old cache
 			final Integer maxCacheSize = (Integer) configuration.get(PROP_MAX_CACHE_SIZE);
 			if (maxCacheSize != null || this.cache == null) {
 				this.manager.removeCache(CACHE_NAME);
 				this.cache = null;
 				
 				// init a new cache 
 				this.cache = new Cache(CACHE_NAME, (maxCacheSize == null) ? 1000 : maxCacheSize.intValue(), false, false, 60*60, 30*60);
 				this.manager.addCache(this.cache);
 			}
 			
 			// configure thread pool min. idle and max. alive sizes
 			final Integer maxIdle = (Integer)configuration.get(PROP_WORKER_MAX_IDLE);
 			final Integer maxAlive = (Integer)configuration.get(PROP_WORKER_MAX_ALIVE);
 			if (maxIdle != null) {
 				final int maxIdleVal = maxIdle.intValue();
 				final int maxAliveNow = (maxAlive == null) ? execService.getMaximumPoolSize() : maxAlive.intValue();
 				if (maxAliveNow < maxIdleVal)
 					throw new ConfigurationException(PROP_WORKER_MAX_IDLE, "max. idle threads must be <= min alive threads");
 				if (maxIdleVal <= 0)
 					throw new ConfigurationException(PROP_WORKER_MAX_IDLE, "max. idle threads must be >= 0");
 				execService.setCorePoolSize(maxIdleVal);
 			}
 			if (maxAlive != null) {
 				final int maxAliveVal = maxAlive.intValue();
 				if (maxAliveVal < execService.getCorePoolSize())
 					throw new ConfigurationException(PROP_WORKER_MAX_ALIVE, "max. alive threads must be >= min idle threads");
 				if (maxAliveVal <= 0)
 					throw new ConfigurationException(PROP_WORKER_MAX_ALIVE, "max. alive threads must be >= 0");
 				execService.setMaximumPoolSize(maxAliveVal);
 			}
 			
 			// shutdown old connection-manager
 			if (this.connectionManager != null) this.connectionManager.shutdown();
 			this.connectionManager = null;
 			
 			// init http-client
 			this.connectionManager = new MultiThreadedHttpConnectionManager();
 			final Integer connectionTimeout = (Integer) configuration.get(PROP_CONNECTION_TIMEOUT);
 			if (connectionTimeout != null)
 				this.connectionManager.getParams().setConnectionTimeout(connectionTimeout.intValue());
 			final Integer socketTimeout = (Integer) configuration.get(PROP_SOCKET_TIMEOUT);
 			if (socketTimeout != null)
 				this.connectionManager.getParams().setSoTimeout(socketTimeout.intValue());
 			final Integer maxConnections = (Integer) configuration.get(PROP_MAXCONNECTIONS_TOTAL);
 			if (maxConnections != null)
 				this.connectionManager.getParams().setMaxTotalConnections(maxConnections.intValue());
 			this.httpClient = new HttpClient(this.connectionManager);
 			
 			// proxy configuration
 			final Boolean useProxyVal = (Boolean)configuration.get(PROP_PROXY_USE);
 			final boolean useProxy = (useProxyVal == null) ? false : useProxyVal.booleanValue();
 			final String host = (String)configuration.get(PROP_PROXY_HOST);
 			final Integer portVal = (Integer)configuration.get(PROP_PROXY_PORT);
 			
 			if (useProxy && host != null && host.length() > 0 && portVal != null) {
 				final int port = portVal.intValue();
 				this.logger.info(String.format("Proxy is enabled: %s:%d",host,Integer.valueOf(port)));
 				final ProxyHost proxyHost = new ProxyHost(host, port);
 				this.httpClient.getHostConfiguration().setProxyHost(proxyHost);
 				
 				final String user = (String)configuration.get(PROP_PROXY_HOST);
 				final String pwd = (String)configuration.get(PROP_PROXY_PASSWORD);
 				
 				if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0)
 					this.httpClient.getState().setProxyCredentials(
 							new AuthScope(host, port),
 							new UsernamePasswordCredentials(user, pwd)
 					);
 			} else {
 				this.logger.info("Proxy is disabled");
 				this.httpClient.getHostConfiguration().setProxyHost(null);
 				this.httpClient.getState().clearCredentials();
 			}			
 			
 		} catch (Throwable e) {
 			this.logger.error("Internal exception during configuring", e);
 			if (e instanceof ConfigurationException)
 				throw (ConfigurationException)e;
 		} finally {
 			this.w.unlock();
 		}
 	}
 	
 	/* =========================================================================
 	 * Monitorable support
 	 * ========================================================================= */
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.osgi.service.monitor.Monitorable#getDescription(String)
 	 */
 	public String getDescription(String id) throws IllegalArgumentException {
 		if (!VAR_NAMES.contains(id)) {
 			throw new IllegalArgumentException("Invalid Status Variable name " + id);
 		}		
 		
 		return VAR_DESCRIPTIONS.get(id);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.osgi.service.monitor.Monitorable#getStatusVariable(String)
 	 */
 	public StatusVariable getStatusVariable(String id) throws IllegalArgumentException {
 		if (!VAR_NAMES.contains(id)) {
 			throw new IllegalArgumentException("Invalid Status Variable name " + id);
 		}
 		
 		int val = 0;
 		int type = StatusVariable.CM_GAUGE;
 		
 		if (id.equals(MONITOR_STORE_SIZE)) {
 			val = this.loader.size();
 		} else if (id.startsWith(MONITOR_JOBS_PREFIX)) {
 			ThreadPoolExecutor execService = this.execService;
 			if (id.equals(MONITOR_JOBS_ACTIVE)) {
 				val = execService.getActiveCount();
 			} else if (id.equals(MONITOR_JOBS_IDLE)) {
				long max = execService.getMaximumPoolSize();
				long active = execService.getActiveCount();
				val = (int) (max - active);
 			} else if (id.equals(MONITOR_JOBS_MAX)) {
 				val = execService.getMaximumPoolSize();
 			} else if (id.equals(MONITOR_JOBS_PENDING)) {
 				long enqued = execService.getTaskCount();
 				long total = execService.getCompletedTaskCount();
				long active = execService.getActiveCount();
				val = (int) (enqued - total - active);
 			} else if (id.equals(MONITOR_JOBS_TOTAL)) {
 				val = (int) execService.getCompletedTaskCount();
 				type = StatusVariable.CM_CC;
 			}
 		}
 		
 		return new StatusVariable(id, type, val);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.osgi.service.monitor.Monitorable#getStatusVariableNames()
 	 */
 	public String[] getStatusVariableNames() {
 		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.osgi.service.monitor.Monitorable#notifiesOnChange(String)
 	 */
 	public boolean notifiesOnChange(String id) throws IllegalArgumentException {
 		return false;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.osgi.service.monitor.Monitorable#resetStatusVariable(String)
 	 */
 	public boolean resetStatusVariable(String id) throws IllegalArgumentException {
 		return false;
 	}
 
 	/**
 	 * @param location the URL
 	 * @return the hostname:port part of the location
 	 */
 	private String getHostPort(URI location) {
 		String prot = location.getScheme();
 		String host = location.getHost();
 		int port = location.getPort();
 		
 		if (port == -1 && prot.equals("http")) port = 80;
 		if (port == -1 && prot.equals("https")) port = 443;
 		
 		return String.format("%s:%d",host,Integer.valueOf(port));
 	}
 	
 	/**
 	 * Downloads a <i>robots.txt</i> file from the given url and parses it
 	 * @param robotsUrlStr the URL to the robots.txt. This must be a http(s) resource
 	 * @return the parsed robots.txt file as a {@link RobotsTxt}-object
 	 * @throws IOException
 	 * @throws URISyntaxException 
 	 */
 	RobotsTxt getFromWeb(URI robotsURL) throws IOException, URISyntaxException {
 		String hostPort = this.getHostPort(robotsURL);
 
 		String statusLine = null;
 		if (!robotsURL.getScheme().startsWith("http")) {
 			throw new IOException(String.format("Unsupported protocol: %s", robotsURL.getScheme()));
 		}
 
 		InputStream inputStream = null;
 		HttpMethod getMethod = null;
 		try {
 			this.r.lock();
 			
 			getMethod = new GetMethod(robotsURL.toASCIIString());
 			int code = this.httpClient.executeMethod(getMethod);
 			statusLine = getMethod.getStatusLine().toString();
 
 			if (code == HttpStatus.SC_UNAUTHORIZED || code == HttpStatus.SC_FORBIDDEN) {
 				// access to the whole website is restricted
 				return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine, true);
 			} else if (code == HttpStatus.SC_NOT_FOUND) {
 				// no robots.txt provided
 				return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
 			} else if (code != HttpStatus.SC_OK) {
 				// the robots.txt seems not to be deliverable
 				return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
 			}
 
 			Header contentTypeHeader = getMethod.getResponseHeader("Content-Type");
 			if (contentTypeHeader != null && !contentTypeHeader.getValue().startsWith("text/plain")) {
 				// the robots.txt seems not to be available
 				return new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_ERROR, "Wrong mimeType " + contentTypeHeader.getValue());
 			}
 			
 			inputStream = getMethod.getResponseBodyAsStream();
 			RobotsTxt robotsTxt = new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
 			return this.parseRobotsTxt(robotsTxt, inputStream);
 		} catch (IOException e) {
 			long reloadInterval = RobotsTxt.RELOAD_INTERVAL_TEMP_ERROR;
 			String status = e.getMessage();
 			if (e instanceof UnknownHostException) {
 				reloadInterval = RobotsTxt.RELOAD_INTERVAL_ERROR;
 				status = "Unknown host";
 				logger.info(String.format("Unknown host '%s'.",robotsURL.getHost()));	
 			} else if (e instanceof CircularRedirectException || e instanceof RedirectException || e instanceof InvalidRedirectLocationException) {
 				reloadInterval = RobotsTxt.RELOAD_INTERVAL_ERROR;
 				logger.info(String.format("Invalid redirection on host '%s'.",hostPort));				
 			} else if (e instanceof SocketTimeoutException || e instanceof ConnectTimeoutException) {
 				logger.debug(String.format("TimeOut while loading robots.txt from host '%s'.",hostPort));
 			} else if (!(
 					e instanceof ConnectException ||
 					e instanceof SocketException
 			)) {
 				logger.error("Exception while loading robots.txt from " + hostPort, e);
 			}
 
 			return new RobotsTxt(hostPort, reloadInterval, status);
 		} catch (IllegalArgumentException e) {
 			// occurs if redirected to an invalid URI, see https://bugs.pxl.li/view.php?id=172
 			// we treat it like a 404, see above
 			logger.info(String.format("Invalid redirection URI on host '%s'.", hostPort));
 			return new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_DEFAULT, "Redirected to illegal URI");
 		} catch (IllegalStateException e) {
 			// occurs if redirected to an URI with an invalid protocol, see https://bugs.pxl.li/view.php?id=169
 			// we treat it like a 404, see above
 			logger.info(String.format("Invalid redirection URI on host '%s'.", hostPort));
 			return new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_DEFAULT, "Redirected to illegal URI");
 			
 		} finally {
 			if (inputStream != null) try { inputStream.close(); } catch (Exception e) {/* ignore this */}
 			if (getMethod != null) getMethod.releaseConnection();
 			this.r.unlock();
 		}
 	}
 
 	/**
 	 * Reads a <i>robots.txt</i> from stream and stores all parsed properties into a {@link RobotsTxt} object.
 	 * @param robotsTxt the {@link RobotsTxt} object that should be filled with the parsed properties
 	 * @param inputStream the {@link InputStream} to read the data 
 	 * @return the {@link RobotsTxt} object. 
 	 * @throws IOException
 	 */
 	RobotsTxt parseRobotsTxt(RobotsTxt robotsTxt, InputStream inputStream) throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));		
 		RuleBlock currentBlock = null;
 
 		boolean isInRuleBlock = false;
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			// trim and cut of comments
 			line = line.trim();
 			line = this.cutOfComments(line);
 
 			// ignore empty lines
 			if (line.length() == 0) continue;
 
 			if (line.toLowerCase().startsWith("User-agent:".toLowerCase())) {
 
 				// getting the user-agent
 				line = line.substring("User-agent:".length()).trim();
 				if (line.length() > 0) {
 					if (isInRuleBlock || currentBlock == null) {
 						// the start of a new block was detected
 						isInRuleBlock = false;
 						currentBlock = new RuleBlock();
 						robotsTxt.addRuleBlock(currentBlock);
 					}
 					currentBlock.addAgent(line);
 				}
 			} else if (line.toLowerCase().startsWith("Disallow:".toLowerCase())) {
 				if (currentBlock == null) continue;
 				isInRuleBlock = true;
 
 				// getting the user-agent
 				line = line.substring("Disallow:".length()).trim();
 
 				if (line.length() > 0) {
 					try {
 						line = URLDecoder.decode(line,"UTF-8");
 					} catch (Exception e) {/* ignore this */}
 
 					currentBlock.addRule(new DisallowRule(line));
 				}
 			} else if (line.toLowerCase().startsWith("Allow:".toLowerCase())) {
 				if (currentBlock == null) continue;
 				isInRuleBlock = true;
 
 				// getting the user-agent
 				line = line.substring("Allow:".length()).trim();
 
 				if (line.length() > 0) {
 					try {
 						line = URLDecoder.decode(line,"UTF-8");
 					} catch (Exception e) {/* ignore this */}
 
 					currentBlock.addRule(new AllowRule(line));
 				}
 			} else if (line.toLowerCase().startsWith("Crawl-Delay:".toLowerCase())) {
 				line = line.substring("Crawl-Delay:".length()).trim();
 				
 				/* 
 				 * According to [1] this should be the "time in seconds between page requests" [1]
 				 * [1] http://drupal.org/node/14177
 				 */
 				try {
 					// just test if it's a valid number
                 	float crawlDelayFloat = Float.parseFloat(line);
                 	int crawlDelay = Math.round(crawlDelayFloat);
                 	if (currentBlock != null) {
                 		currentBlock.addProperty("Crawl-Delay", Integer.toString(crawlDelay));
                 	} else {
                 		this.logger.warn(robotsTxt.getHostPort() + ": Crawl-Delay not within a rule block.");
                 	}
 				} catch (Exception e) {
 					this.logger.error(String.format("Invalid crawl delay directive '%s' in robots.txt from host '%s': %s",
 							line,
 							robotsTxt.getHostPort(),
 							e.getMessage()
 					));
 				}
 			} else if (line.toLowerCase().startsWith("Sitemap:".toLowerCase())) {
 				line = line.substring("Sitemap:".length()).trim();
 				
 				// fix for uses that do not understand the syntax of the sitemap directive
 				if (line.startsWith("<") && line.endsWith(">") && line.length() > 2) {
 					line = line.substring(1, line.length()-1);
 				}
 
 				/* 
 				 * According to [1] this "should be the complete URL to the Sitemap" [1]
 				 * [1] http://www.sitemaps.org/protocol.php#submit_robots
 				 */
 				try {
 					robotsTxt.addSitemap(URI.create(line));
 				} catch (Exception e) {
 					this.logger.error(String.format("Invalid sitemaps directive '%s' in robots.txt from host '%s': %s",
 							line,
 							robotsTxt.getHostPort(),
 							e.getMessage()
 					));
 				}
 			}
 
 		}
 
 		return robotsTxt;
 	}
 	
 	private String cutOfComments(String line) {
 		int pos = line.indexOf("#");
 		if (pos != -1) line = line.substring(0,pos).trim();
 		return line;
 	}
 	
 	public Collection<URI> getSitemaps(String location) {
 		return this.getSitemaps(URI.create(location));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Collection<URI> getSitemaps(URI location) {
 		try {		
 			RobotsTxt robotsTxt = this.getRobotsTxt(location);
 			return robotsTxt.getSitemaps();
 		} catch (Exception e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while getting sitemaps from robots.txt hosted on '%s'.",
 					e.getClass().getName(),
 					this.getHostPort(location)
 			),e);
 			return Collections.EMPTY_LIST;
 		} 
 	}
 	
 	public Map<String, String> getRobotsProperties(String location) {
 		return this.getRobotsProperties(URI.create(location));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Map<String, String> getRobotsProperties(URI location) {
 		try {		
 			RobotsTxt robotsTxt = this.getRobotsTxt(location);
 			RuleBlock ruleBlock = robotsTxt.getRuleBlock(ROBOTS_AGENT_PAXLE);
 			return (ruleBlock == null) ? Collections.EMPTY_MAP : ruleBlock.getProperties();			
 		} catch (Exception e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while getting properties from robots.txt hosted on '%s'.",
 					e.getClass().getName(),
 					this.getHostPort(location)
 			),e);
 			return Collections.EMPTY_MAP;
 		} 
 	}
 	
 	/**
 	 * Check weather the specified {@link URI} is blocked by the robots.txt of the server hosting the {@link URI}
 	 * @return <code>true</code> if crawling of the {@link URI} is disallowed
 	 */
 	public boolean isDisallowed(String location) {
 		return isDisallowed(URI.create(location));		// XXX please re-check whether non-escaped hosts (i.e. "umlaut-domains") work
 	}
 	
 	/**
 	 * Check whether the specified {@link URI} is blocked by the robots.txt of the server which hosts the {@link URI}
 	 * @return <code>true</code> if crawling of the {@link URI} is disallowed
 	 */
 	public boolean isDisallowed(URI location) {
 		String protocol = location.getScheme();
 		if (!protocol.startsWith("http")) {
 			this.logger.debug(String.format("Protocol of location '%s' not supported", location));
 			return false;
 		}
 
 		// check if the URI is blocked
 		return this.isDisallowed(Arrays.asList(location)).size() == 1;
 	}
 	
 	/**
 	 * Check a list of {@link URI URI} against the robots.txt file of the servers hosting the {@link URI}.
 	 * @param hostPort the web-server hosting the {@link URI URIs}
 	 * @param urlList a list of {@link URI}
 	 * 
 	 * @return all {@link URI} that are blocked by the servers
 	 */
 	public List<URI> isDisallowed(Collection<URI> urlList) {
 		if (urlList == null) throw new NullPointerException("The URI-list is null.");
 		
 		// group the URL list based on hostname:port
 		HashMap<URI, List<URI>> uriBlocks = this.groupURI(urlList);
 		ArrayList<URI> disallowedURI = new ArrayList<URI>();
 
 		/*
 		 * Asynchronous execution and parallel check of all blocks 
 		 */
 		final CompletionService<Collection<URI>> execCompletionService = new ExecutorCompletionService<Collection<URI>>(this.execService);
 		
 		// loop through the blocks and start a worker for each block
 		for (Entry<URI, List<URI>> uriBlock : uriBlocks.entrySet()) {
 			URI baseUri = uriBlock.getKey();
 			List<URI> uriList = uriBlock.getValue();			
 			execCompletionService.submit(new RobotsTxtManagerCallable(baseUri,uriList));
 		}
 		
 		// wait for the worker-threads to finish execution
 		for (int i = 0; i < uriBlocks.size(); ++i) {
 			try {
 				Collection<URI> disallowedInGroup = execCompletionService.take().get();
 				if (disallowedInGroup != null) {
 					disallowedURI.addAll(disallowedInGroup);
 				}
 			} catch (InterruptedException e) {
 				this.logger.info(String.format("Interruption detected while waiting for robots.txt-check result."));
 				// XXX should we break here?
 			} catch (ExecutionException e) {
 				this.logger.error(String.format("Unexpected '%s' while performing robots.txt check.",
 						e.getClass().getName()
 				),e);
 			}
 		}
 		
 		return disallowedURI;
 	}
 	
 	/**
 	 * Check a list of {@link URI URI} against the robots.txt file of the specified server.
 	 * @param baseUri the base-URI of the server hosting the {@link URI URIs}
 	 * @param uriList a list of {@link URI}. Each {@link URI} must belong to the specified <code>hostname:port</code>
 	 * 
 	 * @return all {@link URI} that are blocked by the specified server
 	 */
 	private Collection<URI> isDisallowed(URI baseUri , Collection<URI> uriList) {
 		if (baseUri == null) throw new NullPointerException("The base-URI is null.");
 		if (uriList == null) throw new NullPointerException("The URI-list is null.");
 		if (!baseUri.getScheme().startsWith("http")) throw new IllegalArgumentException("Only http(s) resources are allowed.");
 		if (baseUri.getPath().length() > 1) throw new IllegalArgumentException("Invalid base-URI.");
 		
 		ArrayList<URI> disallowedList = new ArrayList<URI>();
 		
 		try {
 			String hostPort = this.getHostPort(baseUri);
 			
 			// getting the RobotsTxt-info for this host[:port]
 			RobotsTxt robotsTxt = this.getRobotsTxt(baseUri);
 			
 			// loop through each URI and check against the robots.txt
 			for (URI location : uriList) {
 				assert(this.getHostPort(location).equals(hostPort));
 				String path = location.getPath();
 				
 				if (robotsTxt.isDisallowed(ROBOTS_AGENT_PAXLE, path)) {
 					disallowedList.add(location);
 				}
 			}
 		} catch (Exception e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while checking URIs against the robots.txt hosted on '%s'.",
 					e.getClass().getName(),
 					baseUri.toASCIIString()
 			),e);
 		}
 		
 		return disallowedList;
 	}
 
 	private RobotsTxt getRobotsTxt(URI baseUri) throws IOException, URISyntaxException {
 		String hostPort = this.getHostPort(baseUri);
 		
 		synchronized (hostPort.intern()) {
 			RobotsTxt robotsTxt = null;
 			
 			// trying to get the robots.txt from cache
 			robotsTxt = this.getFromCache(hostPort);
 
 			// trying to get the robots.txt from file
 			if (robotsTxt == null) {
 				robotsTxt = this.loader.read(hostPort);
 				if (robotsTxt != null) {
 					this.putIntoCache(hostPort, robotsTxt);
 				}
 			}
 
 			// trying to download the robots.txt
 			boolean newDomain = robotsTxt == null;
 			if (newDomain || (System.currentTimeMillis() - robotsTxt.getLoadedDate().getTime() > robotsTxt.getReloadInterval())) {				
 				robotsTxt = this.getFromWeb(URI.create(baseUri.toASCIIString() + "/robots.txt"));
 				this.putIntoCache(hostPort, robotsTxt);
 				this.loader.write(robotsTxt);
 			}
 			
 			return robotsTxt;
 		}
 	}
 	
 	/**
 	 * Function to put a robots.txt file into the {@link #cache}
 	 * @param hostPort the <code>hostname:port</code> the robots.txt file belongs to
 	 * @param robotsTxt the robots.txt file
 	 */
 	private void putIntoCache(String hostPort, RobotsTxt robotsTxt) {
 		try {
 			/* Synchronize access to the cache with the update function
 			 * 
 			 * A r-lock is ok here, because writing to cache is synchronized internally.
 			 * A "write" in our situation is an update to the component via CM
 			 */
 			this.r.lock();
 			
 			Element element = new Element(hostPort, robotsTxt);
 			this.cache.put(element);
 		} finally {
 			this.r.unlock();
 		}
 	}
 	
 	/**
 	 * Function to get a robots.txt file from {@link #cache}
 	 * @param hostPort the <code>hostname:port</code> for which the robots.txt file should be fetched
 	 * @return the robots.txt file
 	 */
 	private RobotsTxt getFromCache(String hostPort) {
 		try {
 			this.r.lock();
 			
 			Element element = this.cache.get(hostPort);
 			return (element == null) ? null : (RobotsTxt) element.getValue();
 		} finally {
 			this.r.unlock();
 		}
 	}	
 	
 	/**
 	 * Groups a list of {@link URI} into blocks hosted by a single server. 
 	 * {@link URI} which are not accessible via <code>http(s)</code> are ignored.
 	 * 
 	 * @param urlList the {@link URI} list
 	 * @return a map containing the base-URI <code>http(s)://hostname:port</code> as key and the list of {@link URI}
 	 *         hosted by the host as values.
 	 */
 	private HashMap<URI, List<URI>> groupURI(Collection<URI> urlList) {
 		HashMap<URI, List<URI>> group = new HashMap<URI, List<URI>>();
 		
 		for (URI uri : urlList) {
 			if (!uri.getScheme().startsWith("http")) continue;
 			
 			// getting hostname:port
 			String hostPort = this.getHostPort(uri);
 			URI baseURI = URI.create(uri.getScheme() + "://" + hostPort);
 			
 			List<URI> pathList = null;
 			if (!group.containsKey(baseURI)) {
 				pathList = new ArrayList<URI>();
 				group.put(baseURI, pathList);
 			} else {
 				pathList = group.get(baseURI);
 			}
 			pathList.add(uri);			
 		}
 		
 		return group;
 	}
 	
 	/**
 	 * Callable class for async. execution of {@link RobotsTxtManager#isDisallowed(URI, Collection)} check.
 	 */
 	class RobotsTxtManagerCallable implements Callable<Collection<URI>> {		
 		private URI baseUri = null;
 		private List<URI> uriList = null;
 		
 		public RobotsTxtManagerCallable(URI baseUri, List<URI> uriList) {
 			if (baseUri == null) throw new NullPointerException("The base-URI is null.");
 			if (uriList == null) throw new NullPointerException("The URI-list is null.");
 			this.baseUri = baseUri;
 			this.uriList = uriList;
 		}
 
 		@SuppressWarnings("unchecked")
 		public Collection<URI> call() throws Exception {
 			String oldThreadName = Thread.currentThread().getName();
 			try {
 				Thread.currentThread().setName(String.format("Robots.txt: %s", this.baseUri));
 				
 				long start = System.currentTimeMillis(); 
 				Collection<URI> disallowedList = isDisallowed(this.baseUri, this.uriList);
 				long end = System.currentTimeMillis();
 
 				logger.debug(String.format(
 						"Robots.txt check of %d URI hosted on '%s' took %d ms. Access to %d URI disallowed.",
 						Integer.valueOf(this.uriList.size()),
 						this.baseUri.toASCIIString(),
 						Long.valueOf(end-start),
 						Integer.valueOf((disallowedList==null)?0:disallowedList.size())
 				));
 
 				return disallowedList;
 			} catch (Exception e) {
 				logger.error(String.format("Unexpected '%s' while performing robots.txt check agains '%s'.",
 						e.getClass().getName(),
 						this.baseUri.toASCIIString()
 				),e);
 				return Collections.EMPTY_LIST;
 			} finally {
 				Thread.currentThread().setName(oldThreadName);
 			}
 		}
 
 	}	
 }
