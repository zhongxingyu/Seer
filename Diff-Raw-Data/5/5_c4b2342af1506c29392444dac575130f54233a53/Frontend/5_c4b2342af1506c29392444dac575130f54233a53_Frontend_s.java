 package ceid.netcins.exo;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServlet;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ContextHandler;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.server.handler.DefaultHandler;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 import rice.environment.Environment;
 import rice.environment.logging.Logger;
 import rice.environment.params.Parameters;
 import rice.environment.random.RandomSource;
 import rice.p2p.commonapi.Id;
 import rice.p2p.commonapi.IdFactory;
 import rice.p2p.commonapi.rawserialization.RawMessage;
 import rice.pastry.PastryNode;
 import rice.pastry.PastryNodeFactory;
 import rice.pastry.commonapi.PastryIdFactory;
 import rice.pastry.direct.DirectNodeHandle;
 import rice.pastry.direct.DirectPastryNodeFactory;
 import rice.pastry.direct.EuclideanNetwork;
 import rice.pastry.direct.GenericNetwork;
 import rice.pastry.direct.NetworkSimulator;
 import rice.pastry.direct.SphereNetwork;
 import rice.pastry.dist.DistPastryNodeFactory;
 import rice.pastry.socket.SocketPastryNodeFactory;
 import rice.pastry.socket.nat.rendezvous.RendezvousSocketPastryNodeFactory;
 import rice.pastry.standard.RandomNodeIdFactory;
 import rice.persistence.LRUCache;
 import rice.persistence.MemoryStorage;
 import rice.persistence.PersistentStorage;
 import rice.persistence.StorageManagerImpl;
 import ceid.netcins.exo.frontend.handlers.AcceptFriendRequestHandler;
 import ceid.netcins.exo.frontend.handlers.GetContentHandler;
 import ceid.netcins.exo.frontend.handlers.GetContentIDsHandler;
 import ceid.netcins.exo.frontend.handlers.GetContentTagsHandler;
 import ceid.netcins.exo.frontend.handlers.GetFriendRequestsHandler;
 import ceid.netcins.exo.frontend.handlers.GetFriendUIDsHandler;
 import ceid.netcins.exo.frontend.handlers.GetUserProfileHandler;
 import ceid.netcins.exo.frontend.handlers.GetUserTagsHandler;
 import ceid.netcins.exo.frontend.handlers.RejectFriendRequestHandler;
 import ceid.netcins.exo.frontend.handlers.SearchContentDHTHandler;
 import ceid.netcins.exo.frontend.handlers.SearchContentPNHandler;
 import ceid.netcins.exo.frontend.handlers.SearchUserDHTHandler;
 import ceid.netcins.exo.frontend.handlers.SearchUserPNHandler;
 import ceid.netcins.exo.frontend.handlers.SendFriendRequestHandler;
 import ceid.netcins.exo.frontend.handlers.SetContentTagsHandler;
 import ceid.netcins.exo.frontend.handlers.SetUserProfileHandler;
 import ceid.netcins.exo.frontend.handlers.ShareFileHandler;
 import ceid.netcins.exo.frontend.json.Json;
 import ceid.netcins.exo.user.User;
 import ceid.netcins.exo.user.UserNodeIdFactory;
 
 /**
  * 
  * @author <a href="mailto:loupasak@ceid.upatras.gr">Andreas Loupasakis</a>
  * @author <a href="mailto:ntarmos@cs.uoi.gr">Nikos Ntarmos</a>
  * @author <a href="mailto:peter@ceid.upatras.gr">Peter Triantafillou</a>
  * 
  * "eXO: Decentralized Autonomous Scalable Social Networking"
  * Proc. 5th Biennial Conf. on Innovative Data Systems Research (CIDR),
  * January 9-12, 2011, Asilomar, California, USA.
  * 
  */
 public class Frontend {
 	public static final int REPLICATION_FACTOR = 1;
 	public static final int LEASE_PERIOD = 10000; // 10 seconds
 	public static final int TIME_TO_FIND_FAULTY = 15000; // 15 seconds
	public static final String INSTANCE = "CatalogFrontend";
 	public static final String PROTOCOL_DIRECT = "direct";
 	public static final String PROTOCOL_SOCKET = "socket";
 	public static final String PROTOCOL_RENDEZVOUS = "rendezvous";
 	public static final String SIMULATOR_SPHERE = "sphere";
 	public static final String SIMULATOR_EUCLIDEAN = "euclidean";
 	public static final String SIMULATOR_GT_ITM = "gt-itm";
 
 	private InetSocketAddress bootstrapNodeAddress;
 	private int webServerPort = 8080;
 	private int pastryNodePort;
 	private Logger logger;
 
 	private User[] users = null;
 	private PastryNode[] nodes = null;
 	private CatalogService[] apps = null;
 	private boolean isSimulated = false;
 
 	private Hashtable<String, Vector<String>> queue = null;
 
 	private String userName = null;
 	private String resourceName = null;
 	private boolean isBootstrap = false;
 	private Server server = null;
 	private Environment environment = null;
 	private IdFactory pastryIdFactory = null;
 	private NetworkSimulator<DirectNodeHandle, RawMessage> simulator = null;
 	private volatile static RandomSource reqIdGenerator = null;
 
 	public Frontend(Environment env, String userName, String resourceName, int jettyPort, int pastryPort, String bootstrap) throws Exception {
 		this.logger = env.getLogManager().getLogger(getClass(),null);
 		this.environment = env;
 		if (reqIdGenerator == null)
 			reqIdGenerator = env.getRandomSource();
 		pastryIdFactory = new PastryIdFactory(env);
 
 		Parameters params = env.getParameters();
 
 		this.userName = userName;
 		this.resourceName = resourceName;
 		if (jettyPort > 0 && jettyPort < 65536)
 			this.webServerPort = jettyPort;
 		this.pastryNodePort = pastryPort;
 		if (pastryPort <= 0 || pastryPort > 65535)
 			pastryNodePort = params.getInt("exo_pastry_port");
 		this.queue = new Hashtable<String, Vector<String>>();
 		String pastryNodeProtocol = params.getString("exo_pastry_protocol");
 		String simulatorType = params.getString("direct_simulator_topology");
 		int numSimulatedNodes = params.getInt("exo_sim_num_nodes");
 		if (numSimulatedNodes == 0 || !pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_DIRECT))
 			numSimulatedNodes = 1;
 		nodes = new PastryNode[numSimulatedNodes];
 		apps = new CatalogService[numSimulatedNodes];
 		users = new User[numSimulatedNodes];
 
 		try {
 			if (bootstrap == null) {
 				bootstrapNodeAddress = params.getInetSocketAddress("exo_pastry_bootstrap");
 			} else {
 				InetAddress bootstrapHostAddr = InetAddress.getByName(bootstrap.substring(0, bootstrap.indexOf(":")));
 				int bootstrapPort = Integer.parseInt(bootstrap.substring(bootstrap.indexOf(":") + 1));
 				bootstrapNodeAddress = (bootstrap == null) ? params.getInetSocketAddress("exo_pastry_bootstrap") : new InetSocketAddress(bootstrapHostAddr, bootstrapPort);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw e;
 		}
 
 		InetSocketAddress localhost = new InetSocketAddress(InetAddress.getLocalHost(), pastryNodePort);
 		if (localhost.getAddress().getHostAddress().equals(bootstrapNodeAddress.getAddress().getHostAddress()) && pastryNodePort == bootstrapNodeAddress.getPort()) {
 			isBootstrap = true;
 		}
 
 		UserNodeIdFactory nodeIdFactory = new UserNodeIdFactory(userName, resourceName);
 		PastryNodeFactory nodeFactory = null;
 		if (pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_DIRECT)) {
 			isSimulated = true;
 			if (simulatorType.equalsIgnoreCase(SIMULATOR_SPHERE)) {
 				simulator = new SphereNetwork<DirectNodeHandle, RawMessage>(env);
 			} else if (simulatorType.equalsIgnoreCase(SIMULATOR_GT_ITM)){
 				simulator = new GenericNetwork<DirectNodeHandle, RawMessage>(env);        
 			} else {
 				simulator = new EuclideanNetwork<DirectNodeHandle, RawMessage>(env);
 			}
 			nodeFactory = new DirectPastryNodeFactory(nodeIdFactory, simulator, env);
 		} else if (pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_SOCKET)) {
 			nodeFactory = new SocketPastryNodeFactory(nodeIdFactory, bootstrapNodeAddress.getAddress(), pastryNodePort, env);
 		} else if (pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_RENDEZVOUS)) {
 			nodeFactory = new RendezvousSocketPastryNodeFactory(nodeIdFactory, bootstrapNodeAddress.getAddress(), pastryNodePort, env, false);
 		}
 
 		if (nodeFactory == null)
 			nodeFactory = DistPastryNodeFactory.getFactory(new RandomNodeIdFactory(environment),
 					DistPastryNodeFactory.PROTOCOL_SOCKET, pastryNodePort, env);
 
 		Id id = UserNodeIdFactory.generateNodeId(this.userName, this.resourceName);
 		users[0] = new User(id, this.userName, this.resourceName);
 		try {
 			nodes[0] = nodeFactory.newNode((rice.pastry.Id)id);
 		} catch (IOException e) {
 			logger.logException("Unable to create pastry node", e);
 			throw e;
 		}
 		System.err.println("User/Node ID: " + id.toStringFull());
 
 		for (int i = 1; i < numSimulatedNodes; i++) {
 			String uname = Long.toHexString(environment.getRandomSource().nextLong());
 			String rname = Long.toHexString(environment.getRandomSource().nextLong());
 			Id simId = UserNodeIdFactory.generateNodeId(uname, rname);
 			users[i] = new User(simId, uname, rname);
 			try {
 				nodes[i] = nodeFactory.newNode((rice.pastry.Id)simId);
 			} catch (IOException e) {
 				logger.logException("Unable to create pastry node", e);
 				throw e;
 			}
 		}
 	}
 
 	public static int nextReqID() {
 		return reqIdGenerator.nextInt();
 	}
 
 	private int waitForNode(PastryNode node) {
 		synchronized (node) {
 			while (!node.isReady()) {
 				try {
 					node.wait(1000);
 				} catch (InterruptedException ie) {
 					logger.logException("Error booting pastry node", ie);
 					return -1;
 				}
 				if (!node.isReady()) {
 					System.out.print("waiting... ");
 				}
 			}
 		}
 		return 0;
 	}
 
 	private int startPastryNodes() {
 		if (nodes == null)
 			return -1;
 
 		if (!isSimulated) {
 			System.out.print("Starting node... ");
 			if (isBootstrap) {
 				nodes[0].boot((Object)null);
 			} else
 				nodes[0].boot(bootstrapNodeAddress);
 			waitForNode(nodes[0]);
 			System.out.println("done");
 		} else {
 			System.out.print("Starting bootstrap node... ");
 			nodes[nodes.length - 1].boot((Object)null);
 			waitForNode(nodes[nodes.length - 1]);
 			System.out.println("done");
 
 			for (int i = 0; i < nodes.length - 1; i++) {
 				System.out.print("Booting node #" + (i + 2) + "/" + nodes.length + "... ");
 				nodes[i].boot(nodes[nodes.length - 1].getLocalHandle());
 				System.out.println("done");
 			}
 
 			for (int i = 0; i < nodes.length - 1; i++) {
 				System.out.print("Waiting for node #" + (i + 2) + "/" + nodes.length + " to become ready... ");
 				waitForNode(nodes[i]);
 				System.out.println("done");
 			}
 		}
 		return 0;
 	}
 
 	private CatalogService startCatalogService(PastryNode node, User user) {
 		StorageManagerImpl storage = null;
 		try {
 			storage = new StorageManagerImpl(pastryIdFactory,
 					new PersistentStorage(pastryIdFactory, user.getUsername() + "@" + user.getResourceName(), "eXO_Storage_Root", -1, environment), new LRUCache(new MemoryStorage(pastryIdFactory), 100000, environment));
 		} catch (IOException e) {
 			logger.logException("Error initializing storage manager", e);
 			return null;
 		}
 
 		CatalogService catalogService = new CatalogService(node, storage, REPLICATION_FACTOR, INSTANCE, user);
 		catalogService.start();
 
 		return catalogService;
 	}
 
 	private int startCatalogServices() {
 		if (apps == null || apps.length < 1)
 			return -1;
 		System.out.print("Starting CatalogService... ");
 		if ((apps[0] = startCatalogService(nodes[0], users[0])) == null)
 			return -1;
 		System.out.println("done");
 		for (int i = 1; i < apps.length; i++) {
 			System.out.print("Starting CatalogService #" + (i + 2) + "/" + apps.length  + "... ");
 			if ((apps[i] = startCatalogService(nodes[i], users[i])) == null)
 				return -1;
 			System.out.println("done");
 		}
 
 		System.out.print("Queueing profile indexing... ");
 
 		System.out.println("done");
 
 		for (int i = 1; i < apps.length; i++) {
 			System.out.print("Queueing profile indexing for user #" + (i + 1) + "/" + apps.length  + "... ");
 
 			System.out.println("done");
 		}
 		return 0;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void addServletToContext(Class handlerClass, ServletContextHandler context) {
 		Constructor constructor = null;
 		Class[] params = new Class[] { CatalogService.class, Hashtable.class };
 		try {
 			constructor = handlerClass.getConstructor(params);
 		} catch (Exception e) {
 			logger.logException("Unable to find constructor", e);
 			return;
 		}
 		try {
 			context.addServlet(new ServletHolder((HttpServlet)constructor.newInstance(apps[0], queue)),  "/" + handlerClass.getSimpleName().replace("Handler", "/"));
 		} catch (Exception e) {
 			logger.logException("Unable to instantiate new handler", e);
 			return;
 		}
 	}
 
 	private ContextHandler mountFileRoute(String url, String templateName) {
 		ContextHandler plainFileContext = new ContextHandler();
 		plainFileContext.setContextPath(url);
 		ResourceHandler plainFileHandler = new ResourceHandler();
 		plainFileHandler.setDirectoriesListed(false);
 		plainFileHandler.setWelcomeFiles(new String[] { templateName});
 		plainFileHandler.setResourceBase(System.getProperty("jetty.home", "/"));
 		plainFileContext.setHandler(plainFileHandler);
 		return plainFileContext;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private int startWebServer() {
 		String rootDir = environment.getParameters().getString("exo_jetty_root");
 		if (rootDir != null)
 			System.setProperty("jetty.home", rootDir);
 			
 		server = new Server();
 
 		SelectChannelConnector connector = new SelectChannelConnector();
 		connector.setPort(webServerPort);
 		server.setConnectors(new Connector[] { connector });
 
 		ContextHandlerCollection handlersList = new ContextHandlerCollection();
 
 		/*
 		 * Placeholder for Wicket integration
 		 */
 		/*
 		// Needs slf4j jars
 		ServletContextHandler root = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
 		FilterHolder filterHolder = new FilterHolder(WicketFilter.class);
 		filterHolder.setInitParameter(ContextParamWebApplicationFactory.APP_CLASS_PARAM,
 				WICKET_WEBAPP_CLASS_NAME);
 		root.addFilter(filterHolder, "/*", 1);
 		root.addServlet(DefaultServlet.class, "/*");
 		handlersList.addHandler(root);
 		*/
 
 		// XXX: Watch out! Handlers are scanned in-order until baseRequest.handled = true, and matched on a String.startsWith() basis
 		Class[] handlerClasses = new Class[] {
 				ShareFileHandler.class,
 				SetUserProfileHandler.class,
 				SetContentTagsHandler.class,
 				GetUserProfileHandler.class,
 				GetUserTagsHandler.class,
 				GetFriendRequestsHandler.class,
 				GetFriendUIDsHandler.class,
 				GetContentTagsHandler.class,
 				GetContentIDsHandler.class,
 				GetContentHandler.class,
 				SendFriendRequestHandler.class,
 				AcceptFriendRequestHandler.class,
 				RejectFriendRequestHandler.class,
 				SearchUserDHTHandler.class,
 				SearchUserPNHandler.class,
 				SearchContentDHTHandler.class,
 				SearchContentPNHandler.class
 		};
 		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		servletContextHandler.setContextPath("/servlet");
 		servletContextHandler.setResourceBase(System.getProperty("jetty.home", "/"));
 		servletContextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
 		servletContextHandler.setAllowNullPathInfo(true);
 		for (Class<ContextHandler> handlerClass : handlerClasses) {
 			addServletToContext(handlerClass, servletContextHandler);
 		}
 		handlersList.addHandler(servletContextHandler);
 
 		/*
 		 * File URL routes deployment  
 		 */		
 		handlersList.addHandler(mountFileRoute("/", "index.html"));
 		handlersList.addHandler(mountFileRoute("/search", "search.html"));
 		handlersList.addHandler(mountFileRoute("/content", "content.html"));
 		handlersList.addHandler(mountFileRoute("/friends", "friends.html"));
 
 		handlersList.addHandler(new DefaultHandler());
 
 		server.setHandler(handlersList);
 		Json.init(); // Make sure Json singleton is instantiated
 
 		try {
 			server.start();
 			server.join();
 		} catch (Exception e) {
 			logger.logException("Error starting web server", e);
 			return -1;
 		}
 		return 0;
 	}
 
 	public void run() {
 		Thread.currentThread().setName("eXO main thread");
 
 		if (startPastryNodes() == -1 || startCatalogServices() == -1 || startWebServer() == -1)
 			return;
 	}
 
 	private static void usage() {
 		System.err.println(
				"Usage: java ... ceid.netcins.CatalogFrontend\n" +
 				"\t-u|--user <username>\n" +
 				"\t-r|--resource <resourcename>\n" +
 				"\t-w|--webport <web ui port>\n" +
 				"\t-d|--dhtport <pastry node port>\n" +
 				"\t-b|--bootstrap <bootstrap node address:port>\n" +
 				"\t-h|--help (this message)"
 		);
 	}
 
 	public static void main(String[] args) {
 		String userName = null, resourceName = null, bootstrap = null;
 		int webPort = 0, pastryPort = 0, c;
 
 		LongOpt[] longopts = new LongOpt[6];
 		longopts[0] = new LongOpt("user", LongOpt.REQUIRED_ARGUMENT, null, 'u');
 		longopts[1] = new LongOpt("resource", LongOpt.REQUIRED_ARGUMENT, null, 'r');
 		longopts[2] = new LongOpt("webport", LongOpt.REQUIRED_ARGUMENT, null, 'w');
 		longopts[3] = new LongOpt("dhtport", LongOpt.REQUIRED_ARGUMENT, null, 'd');
 		longopts[4] = new LongOpt("bootstrap", LongOpt.REQUIRED_ARGUMENT, null, 'b');
 		longopts[5] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
 		Getopt opts = new Getopt("Frontend", args, "u:r:w:d:b:h", longopts);
 
 		while ((c = opts.getopt()) != -1) {
 			switch (c) {
 				case 'u':
 					userName = opts.getOptarg();
 					break;
 				case 'r':
 					resourceName = opts.getOptarg();
 					break;
 				case 'w':
 					webPort = Integer.parseInt(opts.getOptarg());
 					break;
 				case 'd':
 					pastryPort = Integer.parseInt(opts.getOptarg());
 					break;
 				case 'b':
 					bootstrap = opts.getOptarg();
 					break;
 				case 'h':
 				default:
 					usage();
 					return;
 			}
 		}
 		if (userName == null || resourceName == null || webPort < 0 || webPort > 65535 || pastryPort < 0 || pastryPort > 65535) {
 			usage();
 			return;
 		}
 
 		Environment env = new Environment(new String[] { "freepastry", "eXO" }, null);
 		Frontend cf = null;
 		try {
 			cf = new Frontend(env, userName, resourceName, webPort, pastryPort, bootstrap);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 		cf.run();
 	}
 }
