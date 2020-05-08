 package il.technion.ewolf.server;
 
 import il.technion.ewolf.chunkeeper.ChunKeeper;
 import il.technion.ewolf.chunkeeper.ChunKeeperModule;
 import il.technion.ewolf.dht.SimpleDHTModule;
 import il.technion.ewolf.ewolf.EwolfAccountCreator;
 import il.technion.ewolf.ewolf.EwolfAccountCreatorModule;
 import il.technion.ewolf.ewolf.EwolfModule;
 import il.technion.ewolf.http.HttpConnector;
 import il.technion.ewolf.http.HttpConnectorModule;
 import il.technion.ewolf.kbr.KeybasedRouting;
 import il.technion.ewolf.kbr.openkad.KadNetModule;
 import il.technion.ewolf.server.ServerResources.EwolfConfigurations;
 import il.technion.ewolf.server.cache.CacheModule;
 import il.technion.ewolf.server.ewolfHandlers.DownloadFileFromSFS;
 import il.technion.ewolf.server.ewolfHandlers.UploadFileToSFS;
 import il.technion.ewolf.server.handlers.JarResourceHandler;
 import il.technion.ewolf.server.handlers.JsonHandler;
 import il.technion.ewolf.server.handlers.SFSHandler;
 import il.technion.ewolf.server.handlers.SFSUploadHandler;
 import il.technion.ewolf.server.jsonDataHandlers.AddWolfpackMemberHandler;
 import il.technion.ewolf.server.jsonDataHandlers.CreateAccountHandler;
 import il.technion.ewolf.server.jsonDataHandlers.CreateWolfpackHandler;
 import il.technion.ewolf.server.jsonDataHandlers.InboxFetcher;
 import il.technion.ewolf.server.jsonDataHandlers.LoginHandler;
 import il.technion.ewolf.server.jsonDataHandlers.NewsFeedFetcher;
 import il.technion.ewolf.server.jsonDataHandlers.PostToNewsFeedHandler;
 import il.technion.ewolf.server.jsonDataHandlers.ProfileFetcher;
 import il.technion.ewolf.server.jsonDataHandlers.SendMessageHandler;
 import il.technion.ewolf.server.jsonDataHandlers.WolfpackMembersFetcher;
 import il.technion.ewolf.server.jsonDataHandlers.WolfpacksFetcher;
 import il.technion.ewolf.socialfs.SocialFSCreatorModule;
 import il.technion.ewolf.socialfs.SocialFSModule;
 import il.technion.ewolf.stash.StashModule;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.configuration.ConfigurationException;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class EwolfServer {
 
 	private static final String EWOLF_CONFIG = "/ewolf.config.properties";
 
 	private String config = EWOLF_CONFIG;
 	private EwolfConfigurations configurations;
 	Injector serverInjector;
 	HttpConnector serverConnector;
 	Injector ewolfInjector;
 
 	private JsonHandler jsonHandler;
 	private SFSUploadHandler sfsUploadHandler = new SFSUploadHandler();
 	private SFSHandler sfsHandler;
 
 	public volatile boolean isReady = false;
 	private Date startTime;
 	public Date beforeStartTime;
 
 	public EwolfServer(String config) {
 		if (config == null) {
 			throw new IllegalArgumentException("Have to specify configuration file.");
 		}
 		this.config = config;
 	}
 
 	public EwolfServer() {
 		this(EWOLF_CONFIG);
 	}
 
 	public static void main(String[] args) throws Exception {
 		EwolfServer server = new EwolfServer();
 		server.initEwolf();
 	}
 
 	private Injector createDefaultInjector() {
 		String port = String.valueOf(configurations.serverPort);
 
 		return Guice.createInjector(
 				new HttpConnectorModule()
 					.setProperty("httpconnector.httpservice", "webgui")
 					.setProperty("httpconnector.net.port", port),
 				new KadNetModule()
 					.setProperty("openkad.keyfactory.keysize", "20")
 					.setProperty("openkad.bucket.kbuckets.maxsize", "20")
 					.setProperty("openkad.seed", port)
 					.setProperty("openkad.net.udp.port", port));
 	}
 
 	public Date startTime() {
 		return startTime;
 	}
 	public void initEwolf() throws IOException, ConfigurationException, Exception {
 		this.configurations = ServerResources.getConfigurations(config);
 
 		this.serverInjector = createDefaultInjector();
 
 		serverConnector = serverInjector.getInstance(HttpConnector.class);
 		serverConnector.bind();
 		jsonHandler = serverInjector.getInstance(JsonHandler.class);
 		registerConnectorHandlers();
 		jsonHandler.addHandler("createAccount", new CreateAccountHandler(this, config));

 		serverConnector.start();
 
 		startTime = new Date();
 		beforeStartTime = new Date(System.currentTimeMillis()-1000);
 
 		while (configurations.username == null || configurations.password == null
 				|| configurations.name == null) {
 			System.out.println("Username and/or password and/or name weren't provided.");
 			this.configurations = ServerResources.getConfigurations(config);
 		}
		jsonHandler.addHandler("login", new LoginHandler(config));
 		this.ewolfInjector = createInjector();
 
 		KeybasedRouting kbr = ewolfInjector.getInstance(KeybasedRouting.class);
 		kbr.create();
 
 		// bind the chunkeeper
 		ChunKeeper chnukeeper = ewolfInjector.getInstance(ChunKeeper.class);
 		chnukeeper.bind();
 
 		HttpConnector ewolfConnector = ewolfInjector.getInstance(HttpConnector.class);
 		ewolfConnector.bind();
 		ewolfConnector.start();
 
 		//FIXME port for testing
 		kbr.join(configurations.kbrURIs);
 
 		EwolfAccountCreator accountCreator =
 				ewolfInjector.getInstance(EwolfAccountCreator.class);
 		accountCreator.create();
 
 		new Thread(ewolfInjector.getInstance(PokeMessagesAcceptor.class),
 				"PokeMessagesAcceptorThread").start();
 		addEwolfHandlers();
 
 		isReady = true;
 		System.out.println("Server started.");
 	}
 
 	private void registerConnectorHandlers() {
 		serverConnector.register("/json", jsonHandler);
 		serverConnector.register("/sfsupload", sfsUploadHandler);
 		sfsHandler = new SFSHandler(this);
 		serverConnector.register("/sfs", sfsHandler);
 
 		serverConnector.register("*", new JarResourceHandler(this));
 	}
 
 	private void addEwolfHandlers() {
 		jsonHandler
 			.addHandler("inbox", ewolfInjector.getInstance(InboxFetcher.class))
 			.addHandler("wolfpacks", ewolfInjector.getInstance(WolfpacksFetcher.class))
 			.addHandler("wolfpacksAll", ewolfInjector.getInstance(WolfpacksFetcher.class))
 			.addHandler("profile", ewolfInjector.getInstance(ProfileFetcher.class))
 			.addHandler("wolfpackMembers", ewolfInjector.getInstance(WolfpackMembersFetcher.class))
 			.addHandler("wolfpackMembersAll", ewolfInjector.getInstance(WolfpackMembersFetcher.class))
 			.addHandler("wolfpackMembersNotAllowed", ewolfInjector.getInstance(WolfpackMembersFetcher.class))
 			.addHandler("newsFeed", ewolfInjector.getInstance(NewsFeedFetcher.class))
 			.addHandler("createWolfpack", ewolfInjector.getInstance(CreateWolfpackHandler.class))
 			.addHandler("addWolfpackMember", ewolfInjector.getInstance(AddWolfpackMemberHandler.class))
 			.addHandler("post", ewolfInjector.getInstance(PostToNewsFeedHandler.class))
 			.addHandler("sendMessage", ewolfInjector.getInstance(SendMessageHandler.class));
 		sfsUploadHandler
 			.addHandler(ewolfInjector.getInstance(UploadFileToSFS.class));
 		sfsHandler
 			.addHandler(ewolfInjector.getInstance(DownloadFileFromSFS.class));
 	}
 
 	private Injector createInjector() {
 		String port = String.valueOf(configurations.ewolfPort);
 
 		return Guice.createInjector(
 
 				new KadNetModule()
 					.setProperty("openkad.keyfactory.keysize", "20")
 					.setProperty("openkad.bucket.kbuckets.maxsize", "20")
 					.setProperty("openkad.seed", port)
 					.setProperty("openkad.net.udp.port", port),
 
 				new HttpConnectorModule()
 					.setProperty("httpconnector.net.port", port),
 
 				new SimpleDHTModule()
 					//TODO temporary property - replicating bug workaround
 					.setProperty("dht.storage.checkInterval", ""+TimeUnit.HOURS.toMillis(3)),
 
 				new ChunKeeperModule(),
 
 				new StashModule(),
 
 				new SocialFSCreatorModule()
 					.setProperty("socialfs.user.username", 
 						configurations.username)
 					.setProperty("socialfs.user.password", 
 						configurations.password)
 					.setProperty("socialfs.user.name", 
 						configurations.name),
 
 				new SocialFSModule(),
 
 				new EwolfAccountCreatorModule(),
 
 				new EwolfModule(),
 				new CacheModule()
 		);
 	}
 }
