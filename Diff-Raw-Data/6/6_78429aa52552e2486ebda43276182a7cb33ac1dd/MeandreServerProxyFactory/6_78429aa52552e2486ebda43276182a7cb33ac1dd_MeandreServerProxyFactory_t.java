 package org.imirsel.nema.flowservice;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.imirsel.nema.contentrepository.client.ArtifactService;
 import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
 import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
 import org.imirsel.nema.repository.RepositoryClientConnectionPool;
 
 /**
  * Handles the instantiation of new {@link MeandreServerProxy} instances.
  * 
  * @author shirk
  * @since 0.6.0
  * 
  */
 @ThreadSafe
 public class MeandreServerProxyFactory {
 
    private static Logger logger = Logger.getLogger(
          MeandreServerProxyFactory.class.getName());
    
 	private JobStatusMonitor jobStatusMonitor;
 
 	private ArtifactService artifactService;
 	private RepositoryClientConnectionPool repositoryClientConnectionPool;
 	
 
 	private Map<String, MeandreServerProxy> proxyInstances = 
 	   new HashMap<String, MeandreServerProxy>(8);
 
 	/**
 	 * Given a {@link MeandreServerProxyConfig}, create a new
 	 * {@link MeandreServerProxy} instance.
 	 * 
 	 * @param config
 	 *            The configuration upon which the new
 	 *            {@link MeandreServerProxy} instance should be based.
 	 * @param isHead
 	 *            Whether or not the instance will be a head server.
 	 * @return A {@link MeandreServerProxy} instance.
 	 * @throws MeandreServerException if there is a problem instantiating
 	 * the server, such as a communication error, or an unknown server.
 	 */
 	public synchronized MeandreServerProxy getServerProxyInstance(
 			MeandreServerProxyConfig config, boolean isHead) throws MeandreServerException {
 		String key = keyFor(config);
 		MeandreServerProxy instance = null;
 		if (proxyInstances.containsKey(key)) {
 			instance = proxyInstances.get(key);
          logger.info("Returning cached instance of server " + instance.toString());
 		} else {
 			instance = new RemoteMeandreServerProxy();
 			instance.setConfig(config);
 			instance.setJobStatusMonitor(jobStatusMonitor);
 			instance.setRepositoryClientConnectionPool(
 			      repositoryClientConnectionPool);
 	      instance.setHead(isHead);
	      instance.setArtifactService(artifactService);
	      instance.init();
      	proxyInstances.put(key, instance);
          logger.info("Instantiated server " + instance.toString());
 		}
 
 		return instance;
 	}
 
 	/**
 	 * Release any held references to the specified proxy.
 	 * 
 	 * @param proxy {@link MeandreServerProxy} to release.
 	 */
 	public synchronized void release(MeandreServerProxy proxy) {
 	   logger.fine("Releasing server " + proxy.toString() + ".");
 	   proxyInstances.remove(keyFor(proxy.getConfig()));
 	}
 	
 	/**
 	 * Create a key to be used in the instance map for the given
 	 * {@link MeandreServerProxyConfig}.
 	 * 
 	 * @param config
 	 *            The {@link MeandreServerProxyConfig} for which the key will be
 	 *            generated.
 	 * @return The key string.
 	 */
 	private String keyFor(MeandreServerProxyConfig config) {
 		return config.getHost() + ":" + config.getPort();
 	}
 
 	/**
 	 * Set the {@link JobStatusMonitor} that new {@link MeandreServerProxy}
 	 * instances will be given.
 	 * 
 	 * @param jobStatusMonitor
 	 *            Reference to the {@link JobStatusMonitor} the
 	 *            {@link MeandreServerProxy} instances will use to monitor the
 	 *            jobs they are running.
 	 */
 	public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
 		this.jobStatusMonitor = jobStatusMonitor;
 	}
 
 	/**
 	 * Return the {@link JobStatusMonitor} that new {@link MeandreServerProxy}
 	 * instances are being created with references to.
 	 * 
 	 * @return Current {@link JobStatusMonitor}
 	 */
 	public JobStatusMonitor getJobStatusMonitor() {
 		return jobStatusMonitor;
 	}
 
 	/**
 	 * Get the {@link RepositoryClientConnectionPool} that new
 	 * {@link MeandreServerProxy} instances are being given.
 	 * 
 	 * @return {@link RepositoryClientConnectionPool} that are being set upon
 	 *         new {@link MeandreServerProxy} instances.
 	 */
 	public RepositoryClientConnectionPool getRepositoryClientConnectionPool() {
 		return repositoryClientConnectionPool;
 	}
 
 	/**
 	 * Set the {@link RepositoryClientConnectionPool} that new
 	 * {@link MeandreServerProxy} instances should be given.
 	 * 
 	 * {@link RepositoryClientConnectionPool} that should be set upon
 	 *         new {@link MeandreServerProxy} instances.
 	 */
 	public void setRepositoryClientConnectionPool(
 			RepositoryClientConnectionPool repositoryClientConnectionPool) {
 		this.repositoryClientConnectionPool = repositoryClientConnectionPool;
 	}
 
 	public ArtifactService getArtifactService() {
 		return artifactService;
 	}
 
 	public void setArtifactService(ArtifactService artifactService) {
 		this.artifactService = artifactService;
 	}
 
 }
