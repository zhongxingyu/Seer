 package il.technion.ewolf.server;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 import il.technion.ewolf.EwolfAccountCreator;
 import il.technion.ewolf.EwolfAccountCreatorModule;
 import il.technion.ewolf.EwolfModule;
 import il.technion.ewolf.chunkeeper.ChunKeeper;
 import il.technion.ewolf.chunkeeper.ChunKeeperModule;
 import il.technion.ewolf.dht.SimpleDHTModule;
 import il.technion.ewolf.http.HttpConnector;
 import il.technion.ewolf.http.HttpConnectorModule;
 import il.technion.ewolf.kbr.KeybasedRouting;
 import il.technion.ewolf.kbr.openkad.KadNetModule;
 import il.technion.ewolf.socialfs.SocialFSCreatorModule;
 import il.technion.ewolf.socialfs.SocialFSModule;
 import il.technion.ewolf.stash.StashModule;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class EwolfServer {
 	
 	final static Object lock = new Object();
 	private static final int EWOLF_PORT = 10000;
 	private static final int SERVER_PORT = 10200;
 	private static final String EWOLF_CONFIG = "ewolf.config.properties";
 
 
 	public static void main(String[] args) {
 
 		Injector serverInjector = Guice.createInjector(
 				new HttpConnectorModule()
 					.setProperty("httpconnector.net.port", ""+(SERVER_PORT)),
 				new KadNetModule()
 					.setProperty("openkad.keyfactory.keysize", "1")
 					.setProperty("openkad.bucket.kbuckets.maxsize", "3")
 					.setProperty("openkad.seed", ""+(SERVER_PORT))
 					.setProperty("openkad.net.udp.port", ""+(SERVER_PORT)));
 		
 		HttpConnector connector = serverInjector.getInstance(HttpConnector.class);
 		connector.bind();
 		connector.start();
 
 		
 		connector.register("/json*", new JsonHandler() );
 		//TODO
         //connector.register("*", new HttpFileHandler("/",new ServerResourceFactory()));
 		new HttpFileHandler("/", "*", new ServerResourceFactory(), connector);
 		
 		System.out.println("server started");
 
 /*
 		try {
 			PropertiesConfiguration config = new PropertiesConfiguration(EWOLF_CONFIG);
 			config.setProperty("username", "user");
 			config.setProperty("password", "1234");
 			config.save(EWOLF_CONFIG);
 		} catch (ConfigurationException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 */
 		
 		String username = null;
 		String password = null;
 		try {
 			PropertiesConfiguration config = new PropertiesConfiguration(EWOLF_CONFIG);
 			username = config.getString("username");
 			password = config.getString("password");
 			if (username == null) {
 				//TODO get username/password from user, store to EWOLF_CONFIG and continue
 			}
 		} catch (ConfigurationException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 		
 		Injector injector = Guice.createInjector(
 				new KadNetModule()
 					.setProperty("openkad.keyfactory.keysize", "20")
 					.setProperty("openkad.bucket.kbuckets.maxsize", "20")
 					.setProperty("openkad.net.udp.port", ""+(EWOLF_PORT)),
 					
 				new HttpConnectorModule()
 					.setProperty("httpconnector.net.port", ""+(EWOLF_PORT)),
 				
 				new SimpleDHTModule(),
 					
 				new ChunKeeperModule(),
 				
 				new StashModule(),
 				
 				new SocialFSCreatorModule()
 					.setProperty("socialfs.user.username", username)
 					.setProperty("socialfs.user.password", password),
 				
 				new SocialFSModule(),
 				
 				new EwolfAccountCreatorModule(),
 				
 				new EwolfModule()
 		);
 		
 		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
 		try {
 			kbr.create();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		// bind the http connector
 		HttpConnector ewolfConnector = injector.getInstance(HttpConnector.class);
 		ewolfConnector.bind();
 		ewolfConnector.start();
 		
 		// bind the chunkeeper
 		ChunKeeper chnukeeper = injector.getInstance(ChunKeeper.class);
 		chnukeeper.bind();
 		
 		//TODO for testing only
 		try {
 			startEwolf();
 		} catch (Exception e3) {
 			// TODO Auto-generated catch block
 			e3.printStackTrace();
 		}
 		
 		try {
 			//FIXME port for testing
 			kbr.join(Arrays.asList(new URI("openkad.udp://127.0.0.1:10100/")));
 		} catch (URISyntaxException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 		EwolfAccountCreator accountCreator = injector.getInstance(EwolfAccountCreator.class);
 		
 		try {
 			accountCreator.create();
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		
 		synchronized (lock) {
 			while(true)
 				try {
 					lock.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 		}
 
 	}
 
 
 	private static void startEwolf() throws Exception {
 		final int base_port = 10100;
 		List<Injector> injectors = new LinkedList<Injector>();
 		
 		for (int i=0; i < 5; ++i) {
			Injector injector = Guice.createInjector(
 					new KadNetModule()
 						.setProperty("openkad.keyfactory.keysize", "20")
 						.setProperty("openkad.bucket.kbuckets.maxsize", "20")
 						.setProperty("openkad.net.udp.port", ""+(base_port+i)),
 						
 					new HttpConnectorModule()
 						.setProperty("httpconnector.net.port", ""+(base_port+i)),
 					
 					new SimpleDHTModule(),
 						
 					new ChunKeeperModule(),
 					
 					new StashModule(),
 						
 					new SocialFSCreatorModule()
 						.setProperty("socialfs.user.username", "user_"+i)
 						.setProperty("socialfs.user.password", "1234"),
 					
 					new SocialFSModule(),
 					
 					new EwolfAccountCreatorModule(),
 					
 					new EwolfModule()
 			);
			injectors.add(injector);
 		}
 		
 		for (Injector injector : injectors) {
 			
 			// start the Keybased routing
 			KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
 			kbr.create();
 			
 			// bind the http connector
 			HttpConnector connector = injector.getInstance(HttpConnector.class);
 			connector.bind();
 			connector.start();
 			
 			// bind the chunkeeper
 			ChunKeeper chnukeeper = injector.getInstance(ChunKeeper.class);
 			chnukeeper.bind();
 		}
 		
 		
 		for (int i=1; i < injectors.size(); ++i) {
 			int port = base_port + i - 1;
 			System.out.println(i+" ==> "+(i-1));
 			KeybasedRouting kbr = injectors.get(i).getInstance(KeybasedRouting.class);
 			kbr.join(Arrays.asList(new URI("openkad.udp://127.0.0.1:"+port+"/")));
 		}
 		
 		
 		for (Injector injector : injectors) {
 			System.out.println("creating...");
 			EwolfAccountCreator accountCreator = injector.getInstance(EwolfAccountCreator.class);
 			accountCreator.create();
 			System.out.println("done\n");
 			
 		}
		Thread.sleep(1000);
 		
 	}
 
 }
