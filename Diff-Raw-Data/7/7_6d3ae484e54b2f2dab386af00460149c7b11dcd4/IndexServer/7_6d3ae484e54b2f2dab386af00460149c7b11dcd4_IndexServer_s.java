 package ar.uba.fi.tppro.core.service;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.Collection;
 
 import org.apache.thrift.server.TServer;
 import org.apache.thrift.server.TThreadPoolServer;
 import org.apache.thrift.transport.TServerSocket;
 import org.apache.thrift.transport.TServerTransport;
 import org.apache.thrift.transport.TTransportException;
 import org.apache.zookeeper.CreateMode;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.DefaultHandler;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.retry.RetryOneTime;
 
 import ar.uba.fi.tppro.core.index.IndexCoreHandler;
 import ar.uba.fi.tppro.core.index.RemoteIndexNodeDescriptor;
 import ar.uba.fi.tppro.core.index.ClusterManager.ClusterManager;
 import ar.uba.fi.tppro.core.index.lock.LockManager;
 import ar.uba.fi.tppro.core.index.lock.NullLockManager;
 import ar.uba.fi.tppro.core.index.versionTracker.GroupVersionTracker;
 import ar.uba.fi.tppro.core.index.versionTracker.ZkShardVersionTracker;
 import ar.uba.fi.tppro.core.service.thrift.IndexNode;
 import ar.uba.fi.tppro.partition.PartitionResolver;
 import ar.uba.fi.tppro.partition.ZookeeperPartitionResolver;
 import ar.uba.fi.tppro.util.NetworkUtils;
 
 public class IndexServer implements Runnable {
 
 	final static Logger logger = LoggerFactory
 			.getLogger(IndexCoreHandler.class);
 
 	private String bindIp;
 	private int port;
 	private File dataDir;
 	private IndexCoreHandler handler;
 	private TServer server;
 	private CuratorFramework curator;
 	
 	private PartitionResolver partitionResolver;
 	private GroupVersionTracker versionTracker;
 	private LockManager lockManager;
 	private ClusterManager clusterManager;
 
 	private String zookeeperUrl;
 
 	public static void main(String[] args) throws NumberFormatException,
 			Exception {
 
 		String port = System.getProperty("port", "9090");
 		String dataDir = System.getProperty("dataDir", "data");
 		String zookeeperHost = System
 				.getProperty("zookeeper", "localhost:2181");
 
 		if (zookeeperHost == null) {
 			System.out
 					.println("Zookeeper server not specified. Specify one with -Dzookeeper");
 			return;
 		}
 
 		IndexServerConfig config = new IndexServerConfig();
 		config.listenPort = Integer.parseInt(port);
 		config.dataDir = new File(dataDir);
 		config.zookeeperUrl = zookeeperHost;
 
 		new Thread(new IndexServer(config)).start();
 
 	}
 
 	private static CuratorFramework createZookeeperClient(String zookeeperHost) {
 		return CuratorFrameworkFactory.newClient(zookeeperHost,
 				new RetryOneTime(1));
 	}
 
 	public IndexServer(IndexServerConfig config) {
 		this.port = config.listenPort;
 		this.dataDir = config.dataDir;
 		this.bindIp = config.bindIp;
 		this.zookeeperUrl = config.zookeeperUrl;
 	}
 
 	public void stop() {
 		logger.info("Stoping server");
 		if (server != null) {
 			server.stop();
 		}
 	}
 
 	@Override
 	public void run() {
 
 		logger.info("Starting execution of IndexCore");
 		
 		if(curator == null){
 			curator = createZookeeperClient(this.zookeeperUrl);
 			curator.start();
 			logger.debug("Connected to zookeeper server " + this.zookeeperUrl);
 		}
 		
 		if(this.bindIp == null){
 			Collection<InetAddress> ips;
 			try {
 				ips = NetworkUtils.getAllLocalIPs();
 				if (ips.size() == 0) {
 					logger.error("Could not retrieve the local ip");
 				}
 				this.bindIp = ips.iterator().next().getHostAddress();
 
 			} catch (SocketException e) {
 				logger.error("Could not retrieve the local ip", e);
 				return;
 			}
 		}
 
 		RemoteIndexNodeDescriptor localNodeDescriptor = new RemoteIndexNodeDescriptor(
 				this.bindIp, port);
 		try {
 			this.clusterManager.registerNode(localNodeDescriptor, ClusterManager.NodeType.INDEX);
 		} catch (Exception e) {
 			logger.error("Could not register indexCore in zookeeper", e);
 			return;
 		}
 
 		if(this.lockManager == null){
 			logger.debug("Using mock lock manager. It should use no more than 1 broker");
 			this.lockManager = new NullLockManager();
 		}
 		
 		if(this.versionTracker == null){
 			this.versionTracker = new ZkShardVersionTracker(curator);
 		}
 		
 		if(this.partitionResolver == null){
 			this.partitionResolver = new ZookeeperPartitionResolver(
 					curator);
 		}
 
 		this.handler = new IndexCoreHandler(localNodeDescriptor,
 				this.getPartitionResolver(), this.getVersionTracker(), this.getLockManager());
 
 		if (!this.dataDir.exists()) {
 			logger.debug("Data dir does not exist. Creating it");
 			if (!this.dataDir.mkdir()) {
 				logger.error("Could not create directory " + this.dataDir);
 				return;
 			}
 		}
 		logger.debug("Using data directory " + this.dataDir);
 		
 		this.handler.open(this.dataDir, true);
 
 		Server webServer = new Server(this.port + 1);
 		ResourceHandler resource_handler = new ResourceHandler();
 		resource_handler.setDirectoriesListed(true);
 		resource_handler.setWelcomeFiles(new String[] { "index.html" });
 		resource_handler.setResourceBase(this.dataDir.getAbsolutePath());
 
 		HandlerList handlers = new HandlerList();
 		handlers.setHandlers(new Handler[] { resource_handler,
 				new DefaultHandler() });
 		webServer.setHandler(handlers);
 
 		logger.info("Starting http server on port port " + (this.port + 1)
 				+ "...");
 		try {
 			webServer.start();
 		} catch (Exception e) {
 			logger.error("Could not start http server", e);
 			return;
 		}
 
 		TServerTransport serverTransport;
 		try {
 			serverTransport = new TServerSocket(this.port);
 		} catch (TTransportException e) {
 			logger.error("could not bind service to address", e);
 			return;
 		}
 		server = new TThreadPoolServer(
 				new TThreadPoolServer.Args(serverTransport)
 						.processor(new IndexNode.Processor<IndexNode.Iface>(
 								handler)));
 
 		logger.info("Index core listening on " + this.bindIp + ":" + this.port);
 		server.serve();
 	}
 
 	public CuratorFramework getCurator() {
 		return curator;
 	}
 
 	public void setCurator(CuratorFramework curator) {
 		this.curator = curator;
 	}
 
 	private LockManager getLockManager() {
 		return this.lockManager;
 	}
 
 	private GroupVersionTracker getVersionTracker() {
 		return this.versionTracker;
 	}
 
 	private PartitionResolver getPartitionResolver() {
 		return this.partitionResolver;
 	}
 
 	public IndexCoreHandler getHandler() {
 		return handler;
 	}
 	
 	public void setPartitionResolver(PartitionResolver partitionResolver) {
 		this.partitionResolver = partitionResolver;
 	}
 
 	public void setVersionTracker(GroupVersionTracker versionTracker) {
 		this.versionTracker = versionTracker;
 	}
 
 	public void setLockManager(LockManager lockManager) {
 		this.lockManager = lockManager;
 	}
 	
 	public ClusterManager getClusterManager() {
 		return clusterManager;
 	}
 
 	public void setClusterManager(ClusterManager clusterManager) {
 		this.clusterManager = clusterManager;
 	}
 }
