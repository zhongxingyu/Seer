 package org.remus.server;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.thrift.TException;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.remus.JSON;
 import org.remus.PeerInfo;
 import org.remus.RemusAttach;
 import org.remus.RemusDB;
 import org.remus.RemusDatabaseException;
 import org.remus.RemusWeb;
 import org.remus.core.BaseStackIterator;
 import org.remus.core.BaseStackNode;
 import org.remus.core.RemusInstance;
 import org.remus.core.RemusMiniDB;
 import org.remus.plugin.PeerManager;
 import org.remus.plugin.PluginManager;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.Constants;
 import org.remus.thrift.JobState;
 import org.remus.thrift.JobStatus;
 import org.remus.thrift.NotImplemented;
 import org.remus.thrift.PeerType;
 import org.remus.thrift.RemusNet;
 import org.remus.thrift.WorkDesc;
 import org.remus.thrift.WorkMode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JettyServer extends RemusWeb {
 
 	Map params;
 	Server server;
 
 	public static final int DEFAULT_PORT = 16016;
 
 	private Logger logger;
 	private PeerManager pm;
 
 	@Override
 	public PeerInfo getPeerInfo() {
 		PeerInfo out = new PeerInfo();
 		out.name = "Jetty Remus Web Server";
 		out.peerType = PeerType.WEB_SERVER;
 		return out;
 	}
 
 	Map<AppletRef, BaseStackNode> stackMap;
 	private RemusMiniDB miniDB = null;
 
 	@Override
 	public void init(Map params) {
 		this.params = params;		
 		logger = LoggerFactory.getLogger(JettyServer.class);
 		stackMap = new HashMap<AppletRef, BaseStackNode>();
 	}	
 
 	@Override
 	public void start(PluginManager pluginManager) throws RemusDatabaseException {
 		this.pm = pluginManager.getPeerManager();
 		System.setProperty("org.mortbay.http.HttpRequest.maxFormContentSize", "0");		
 
 		int serverPort = DEFAULT_PORT;
 		if (params != null && params.containsKey("port")) {
 			serverPort = Integer.parseInt(params.get("port").toString());
 		}
 
 		server = new Server(serverPort);
 		Context root = new Context(server, "/", Context.SESSIONS);
 
 		/*
 		for (Map.Entry<Object, Object> propItem : prop.entrySet()) {
 			String key = (String) propItem.getKey();
 			String value = (String) propItem.getValue();
 			sh.setInitParameter(key, value);
 		}
 		 */		
 
 		if (params != null && params.containsKey("fileDir")) {
 			Map dirMap = (Map)params.get("fileDir");
 			for (Object name : dirMap.keySet() ) {
 				FileServlet fs = new FileServlet( (String)name, new File((String)dirMap.get(name)) );
 				root.addServlet(new ServletHolder(fs), "/" + (String)name + "/*");
 			}
 		}
 
 		ServletHolder sh = new ServletHolder(new MasterServlet(this));
 
 		root.addServlet(sh, "/*");
 
 		Thread serverThread = new Thread() {	
 			@Override
 			public void run() {
 				try {
 					server.start();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}		
 			}		
 		};		
 		serverThread.start();		
 	}
 
 	@Override
 	public void stop() {
 		try {
 			server.stop();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public RemusAttach getAttachStore() {		
 		try {
 			return RemusAttach.wrap(pm.getPeer(pm.getAttachStore()));
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public RemusDB getDataStore() {
 		try {
 			return RemusDB.wrap(pm.getPeer(pm.getDataServer()));
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	
 	public RemusNet.Iface getMaster() {
 		try {
 			return pm.getPeer(pm.getManager());
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		return null;
 	}
 	
 
 	Map<String,Map<String,Object>> dbMap = new HashMap<String, Map<String,Object>>();
 	Map<String,BaseStackNode> nodeMap = new HashMap<String, BaseStackNode>();
 	@Override
 	public void jsRequest(String string, WorkMode mode,
 			BaseStackNode appletView, BaseStackNode appletOut) {
 		String peerID = pm.getPeerID(this);
 		logger.info("Javascript Query: " + peerID);
 		RemusNet.Iface jsWorker = null;
 		//RemusManager manager = pm.getManager();
 		//jsWorker = manager.getWorker("javascript");
 
 		if (miniDB == null) {
 			miniDB = new RemusMiniDB(getDataStore());
 		}
 
 		try {
 			for (String worker : pm.getWorkers("javascript")) {
 				if (jsWorker == null) {
 					jsWorker = pm.getPeer(worker);
 				}
 			}
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		String tmpStack = UUID.randomUUID().toString();
 		String tmpStackIn = tmpStack + "_IN";
 		String tmpStackWork = tmpStack + Constants.WORK_APPLET;
 
 		WorkDesc work = new WorkDesc();
 		work.mode = mode;
 		Map infoMap = new HashMap();
		infoMap.put("jsCode", string);
 		Map inMap = new HashMap();
 		inMap.put("_instance", RemusInstance.STATIC_INSTANCE);
 		inMap.put("_applet", tmpStackIn);
		infoMap.put("_input", inMap);
 		work.setInfoJSON(JSON.dumps(infoMap));
 		work.workStart = 0;
 
 
 		BaseStackIterator<String> keyIter = new BaseStackIterator<String>(appletView, "", "", false) {
 			@Override
 			public void processKeyValue(String key, Object val) {
 				addElement(key);
 			}
 		};
 		Map<String,Object> keyStack = new HashMap<String,Object>();
 		long i = 0;
 		for (String key : keyIter) {
 			keyStack.put(Long.toString(i), key);
 			i++;
 		}
 		synchronized (dbMap) {
 			dbMap.put(tmpStackWork, keyStack);			
 		}
 		synchronized (nodeMap) {
 			nodeMap.put(tmpStackIn, appletView);	
 			nodeMap.put(tmpStack, appletOut);	
 		}
 		work.workEnd = keyStack.size();
 		work.setLang("javascript");
 		work.setMode(WorkMode.MAP);
 		work.setWorkStack(new AppletRef("@jsInteractive", RemusInstance.STATIC_INSTANCE_STR, tmpStack));
 		try {
 			String jobID = jsWorker.jobRequest(peerID, null, work);
 			boolean done = false;
 			do {
 				JobStatus stat = jsWorker.jobStatus(jobID);
 				if (stat.status == JobState.QUEUED || stat.status == JobState.WORKING) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else {
 					done = true;
 				}
 			} while (!done);
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 
 		synchronized (dbMap) {
 			dbMap.remove(tmpStackWork);
 		}
 		synchronized (nodeMap) {
 			nodeMap.remove(tmpStackIn);
 			nodeMap.remove(tmpStackWork);
 		}
 
 	}
 
 	@Override
 	public String status() throws TException {
 		return "OK";
 	}
 
 	@Override
 	public List<String> getValueJSON(AppletRef stack, String key)
 	throws NotImplemented, TException {
 		logger.debug("WEB_DB GET: " + stack + " " + key);
 		synchronized (dbMap) {
 			if (dbMap.containsKey(stack.applet)) {
 				return Arrays.asList(JSON.dumps(dbMap.get(stack.applet).get(key)));
 			}
 		}
 		synchronized (nodeMap) {
 			if (nodeMap.containsKey(stack.applet)) {
 				List<String> out = nodeMap.get(stack.applet).getValueJSON(key);
 				return out;
 			}
 		}
 		return Arrays.asList();
 	}
 
 	@Override
 	public void addDataJSON(AppletRef stack, long jobID, long emitID, String key,
 			String data) throws NotImplemented, TException {
 		logger.debug("WEB_DB addData: " + stack + " " + key);
 		synchronized (nodeMap) {
 			if (nodeMap.containsKey(stack.applet)) {
 				nodeMap.get(stack.applet).add(key, data);
 			}
 		}
 	}
 
 }
