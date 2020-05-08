 package com.cj.scmconduit.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.httpobjects.HttpObject;
 import org.httpobjects.Request;
 import org.httpobjects.Response;
 import org.httpobjects.jetty.HttpObjectsJettyHandler;
 import org.httpobjects.util.ClasspathResourceObject;
 import org.httpobjects.util.HttpObjectUtil;
 import org.httpobjects.util.Method;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.ResourceHandler;
 
 import scala.Tuple2;
 
 import com.cj.scmconduit.core.BzrP4Conduit;
 import com.cj.scmconduit.core.GitP4Conduit;
 import com.cj.scmconduit.core.p4.ClientSpec;
 import com.cj.scmconduit.core.p4.P4Credentials;
 import com.cj.scmconduit.core.p4.P4DepotAddress;
 import com.cj.scmconduit.core.util.CommandRunner;
 import com.cj.scmconduit.core.util.CommandRunnerImpl;
 import com.cj.scmconduit.server.conduit.ConduitController;
 import com.cj.scmconduit.server.config.Config;
 import com.cj.scmconduit.server.fs.TempDirAllocator;
 import com.cj.scmconduit.server.jetty.ConduitHandler;
 import com.cj.scmconduit.server.jetty.VFSResource;
 
 public class ConduitServerMain {
 	public static void main(String[] args) throws Exception {
 		try {
 			setupLogging();
 			doBzrSafetyCheck();
 			
 			File path = new File(args[0]);
 			String p4Address = args[1];
 			String p4User = args[2];
 			String publicHostname = args.length>3?args[3]:InetAddress.getLocalHost().getCanonicalHostName();
 			
 			Config config = new Config(publicHostname, path, p4Address, p4User);
 			
 			new ConduitServerMain(config);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 
 	private static void setupLogging() {
 		BasicConfigurator.resetConfiguration();
 		BasicConfigurator.configure();
 		Logger.getRootLogger().setLevel(Level.INFO);
 	}
 
 	private static void doBzrSafetyCheck() {
 		File f = new File(System.getProperty("user.dir"));
 		while(f!=null){
 			File dotBzrDir = new File(f, ".bzr"); 
 			if(dotBzrDir.exists()){
 				throw new RuntimeException("Looks like we're being run from under bzr (" + f.getAbsolutePath() + ").  This can be dangerous.  Run me from somewhere else instead.");
 			}
 			f = f.getParentFile();
 		}
 	}
 	
 	private final File tempDirPath;
 	private final File path;
 	private final Server jetty;
 	private final Map<String, String> credentials = new HashMap<String, String>();
 	{
 		credentials.put("someuser", "test");
 	}
 	
 	public ConduitServerMain(final Config config) throws Exception {
 		this.path = config.path;
 		this.tempDirPath = new File(this.path, "tmp");
 		if(!this.tempDirPath.exists() || !this.tempDirPath.isDirectory()){
 			throw new IOException("Directory does not exist: " + this.tempDirPath);
 		}
 		
 		final String basePublicUrl = "http://" + config.publicHostname + ":8034";
 		System.out.println("My public url is " + basePublicUrl);
 		
 		final P4DepotAddress p4Address = new P4DepotAddress(config.p4Address);
 		
 		jetty = new Server(8034);
 		ResourceHandler defaultHandler = new ResourceHandler();
 		final VFSResource root = new VFSResource("/");
 		defaultHandler.setBaseResource(root);
 		defaultHandler.setWelcomeFiles(new String[]{"hi.txt"});
 		final List<Handler> handlers = new ArrayList<Handler>();
 		handlers.add(defaultHandler);
 		
 		final TempDirAllocator allocator = new TempDirAllocator() {
 			private Set<File> allocatedPaths = new HashSet<File>();
 			public File newTempDir() {
 				try {
 					File path = File.createTempFile("conduit-server", ".dir", tempDirPath);
 					if(!path.delete() & !path.mkdirs()){
 						throw new RuntimeException("Could not create directory at path " + tempDirPath.getAbsolutePath());
 					}
 					return path;
 				} catch (IOException e) {
 					throw new RuntimeException("Error creating temp dir at " + tempDirPath, e);
 				}
 			}
 			public void dispose(File tempDir) {
 				if(!allocatedPaths.contains(tempDir))
 					throw new RuntimeException("This is not something I allocated");
 				
 				try {
 					FileUtils.deleteDirectory(tempDir);
 				} catch (IOException e) {
 					throw new RuntimeException("Could not delete directory:" + tempDir.getAbsolutePath());
 				}
 			}
 		};
 		
 		final List<ConduitStuff> conduits = new ArrayList<ConduitServerMain.ConduitStuff>();
 		for(ConduitConfig conduit: findConduits()){
 			ConduitStuff stuff = prepareConduit(basePublicUrl, root, allocator, conduit);
 			conduits.add(stuff);
 		}
 		
 		// Add shared bzr repository
 		root.addVResource("/.bzr", new File(config.path, ".bzr"));
 		
 		HttpObject addConduitPage = new AddConduitResource(new AddConduitResource.Listener() {
 			
 			@Override
 			public void addConduit(ConduitType type, String name, String p4Path, Integer p4FirstCL) {
 				final CommandRunner shell = new CommandRunnerImpl();
 				File localPath = new File(config.basePathForNewConduits, name);
 				System.out.println("I am going to create something at " + localPath);
 				if(localPath.exists()) throw new RuntimeException("There is already a conduit called \"" + name + "\" " +
 						"(given that there is already a directory at " + localPath.getAbsolutePath() + ")");
 				
 				String clientId = config.clientIdPrefix + name;
 				
 				@SuppressWarnings("deprecation")
 				scala.collection.immutable.List<Tuple2<String, String>> view = scala.collection.immutable.List.fromArray(new Tuple2[]{
 						new Tuple2<String, String>(p4Path + "/...", "/...")
 				});
 				
 				ClientSpec spec = new ClientSpec(
 									localPath, 
 									config.p4User, 
 									clientId, 
 									config.publicHostname, 
 									view);
 				
 				P4Credentials credentials = new P4Credentials(config.p4User, "");
 				
 				if(type == ConduitType.GIT){
 					GitP4Conduit.create(p4Address, spec, p4FirstCL, shell, credentials);
 				}else if(type == ConduitType.BZR){
 					BzrP4Conduit.create(p4Address, spec, p4FirstCL, shell, credentials);
 				}else{
 					throw new RuntimeException("not sure how to create a \"" + type + "\" conduit");
 				}
 				
 				ConduitConfig conduit = new ConduitConfig("/" + name, localPath);
 				ConduitStuff stuff = prepareConduit(basePublicUrl, root, allocator, conduit);
 				conduits.add(stuff);
 			}
 		});
 		
 		HttpObject depotHandler = new HttpObject("/{conduitName}/{remainder*}") {
 			
 			public ConduitStuff findConduitForPath(String conduitName) {
 				ConduitStuff foundConduit = null;
 				for (ConduitStuff conduit : conduits) {
 					final String next = conduit.handler.name;
 					if (conduitName.equals(next)) {
 						foundConduit = conduit;
 					}
 				}
 				return foundConduit;
 			}
 			
 			public Response relay(Request req, final Method m) {
 				String conduitName = "/" + req.pathVars().valueFor("conduitName");
 				
 				ConduitStuff match = findConduitForPath(conduitName);
 				if(match!=null) return HttpObjectUtil.invokeMethod(match.handler, m, req);
 				else {
 					System.out.println("There is no conduit at " + conduitName);
 					return null;
 				}
 				
 			}
 			
 			@Override
 			public Response get(Request req) {
 				return relay(req, Method.GET);
 			}
 			
 			@Override
 			public Response post(Request req) {
 				return relay(req, Method.POST);
 			}
 		};
 		
 		final HttpObject conduitsApiResource = new HttpObject("/api/conduits"){
 			@Override
 			public Response get(Request req) {
 				StringBuilder text = new StringBuilder("{\"conduits\":[");
 				
 				for(int x=0;x<conduits.size();x++){
 					final ConduitStuff conduit = conduits.get(x);
 					final String url = basePublicUrl + conduit.config.hostingPath + (new File(conduit.config.localPath, ".git").exists()?"/.git":"");

 					if(x>0) text.append(",");
 					text.append(
 							"{" +
 							"    \"name\":\""  + conduit.config.localPath.getName() + "\"," +
 							"    \"p4path\":\""  + conduit.p4path + "\"," +
							"    \"url\":\"" + url + "\"" + 
 							"}");
 					
 				}
 				text.append("]}");
 				
 				return OK(Json(text.toString()));
 			}
 		};
 		
 		handlers.add(new HttpObjectsJettyHandler(
 							new ClasspathResourceObject("/", "index.html", getClass()), 
 							new ClasspathResourceObject("/submit.py", "submit.py", getClass()), 
 							addConduitPage, 
 							depotHandler, 
 							conduitsApiResource
 							));
 		
 		jetty.setHandlers(handlers.toArray(new Handler[]{}));
 		jetty.start();
 		
 	}
 	
 	interface MethodInvoker {
 		Response invoke(Request req, HttpObject o);
 	}
 	
 	class ConduitConfig {
 		
 		public final String hostingPath;
 		
 		public final File localPath;
 		
 		private ConduitConfig(String hostingPath, File localPath) {
 			super();
 			this.hostingPath = hostingPath;
 			this.localPath = localPath;
 		}
 		
 	}
 
 	private List<ConduitConfig> findConduits(){
 		List<ConduitConfig> conduits = new ArrayList<ConduitConfig>();
 		
 		File conduitsDir = new File(path, "conduits");
 		
 		if(!conduitsDir.isDirectory() && !conduitsDir.mkdirs()) throw new RuntimeException("Could not create directory at " + conduitsDir);
 		
 		for(File localPath : conduitsDir.listFiles()){
 			if(localPath.isDirectory() && !localPath.getName().toLowerCase().endsWith(".bak")){
 				String httpPath = "/" + localPath.getName();//.replaceAll(Pattern.quote("-"), "/");
 				
 				conduits.add(new ConduitConfig(httpPath, localPath));
 			}
 		}
 		
 		return conduits;
 	}
 	
 	class ConduitStuff {
 		final String p4path;
 		final ConduitConfig config;
 		final ConduitHandler handler;
 		final ConduitController controller;
 		
 		private ConduitStuff(ConduitConfig config, ConduitHandler handler,
 				ConduitController controller) {
 			super();
 			this.config = config;
 			this.handler = handler;
 			this.controller = controller;
 			this.p4path = controller.p4Path();
 		}
 		
 	}
 	
 	private ConduitStuff prepareConduit(final String basePublicUrl, VFSResource root, TempDirAllocator allocator, ConduitConfig conduit) {
 		URI publicUri = URI(basePublicUrl + conduit.hostingPath);
 		ConduitController controller = new ConduitController(publicUri, conduit.localPath, allocator);
 		controller.start();
 		
 		ConduitHandler handler = new ConduitHandler(conduit.hostingPath, controller);
 		
 		// For basic read-only "GET" access
 		System.out.println("Serving " + conduit.localPath + " at " + conduit.hostingPath);
 		root.addVResource(conduit.hostingPath, conduit.localPath);
 		return new ConduitStuff(conduit, handler, controller);
 	}
 	
 	URI URI(String uri){
 		try {
 			return new URI(uri);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 }
