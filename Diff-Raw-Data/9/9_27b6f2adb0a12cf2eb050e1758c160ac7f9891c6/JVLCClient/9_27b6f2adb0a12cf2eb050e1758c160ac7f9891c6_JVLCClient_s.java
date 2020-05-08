 package net.lecousin.media.jvlc.client;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.lecousin.framework.Pair;
 import net.lecousin.framework.application.Application;
 import net.lecousin.framework.application.Application.ProcessChecker;
 import net.lecousin.framework.event.Event;
 import net.lecousin.framework.event.Event.Listener;
 import net.lecousin.framework.log.Log;
 import net.lecousin.media.jvlc.server.IJVLCMedia;
 import net.lecousin.media.jvlc.server.IJVLCMediaManager;
 
 public class JVLCClient {
 
 	private static Set<Integer> registries = new HashSet<Integer>();
 	private static MediaListener listener = null;
 	private static Process serverProcess = null;
 	
 	public static Event<IJVLCMedia> started = new Event<IJVLCMedia>();
 	public static Event<IJVLCMedia> paused = new Event<IJVLCMedia>();
 	public static Event<IJVLCMedia> stopped = new Event<IJVLCMedia>();
 	public static Event<IJVLCMedia> ended = new Event<IJVLCMedia>();
 	public static Event<IJVLCMedia> positionChanged = new Event<IJVLCMedia>();
 	public static Event<Pair<IJVLCMedia,Long>> timeChanged = new Event<Pair<IJVLCMedia,Long>>();
 	public static Event<Double> volumeChanged = new Event<Double>();
 	public static Event<Boolean> muteChanged = new Event<Boolean>();
 	
 	public static IJVLCMediaManager get(int port, String path, Listener<Integer> crashListener) throws RemoteException, IOException, NotBoundException {
 		if (launchRegistry(port, path)) { 
 			registerListener(port);
 		}
 		launchServer(port, path, crashListener);
 		return (IJVLCMediaManager)Naming.lookup("//localhost:" + port + "/JVLCMediaManager"); 
 	}
 	
 	private static void registerListener(int port) throws RemoteException {
 		if (listener == null) listener = new MediaListener();
 		if (Log.info(JVLCClient.class))
 			Log.info(JVLCClient.class, "Register listener");
 		try { Naming.rebind("//localhost:" + port + "/JVLCMediaListener", listener); }
 		catch (MalformedURLException e) {
 			// should never happen
 		}
 	}
 
 	private static boolean launchRegistry(int port, String path) throws IOException {
 		if (registries.contains(port) && Application.isProcessRunning("JVLCRegistry"+port)) return false;
 		Map<String,String> env = System.getenv();
 		String[] envp = new String[env.size()];
 		int i = 0;
 		boolean cpSet = false;
 		for (String s : env.keySet())
 			if (s.equalsIgnoreCase("classpath")) {
 				envp[i++] = s + "=" + env.get(s) + ";" + path+"/net.lecousin.media.jvlc.common.jar";
 				cpSet = true;
 			} else
 				envp[i++] = s + "=" + env.get(s);
 		if (!cpSet)
 			envp[i++] = "CLASSPATH=" + path+"/net.lecousin.media.jvlc.common.jar";
 //		if (System.getSecurityManager() == null)
 //			System.setSecurityManager(new RMISecurityManager());
 //		LocateRegistry.createRegistry(port);
 		String options = "-J-cp \"-J" + path+"/net.lecousin.media.jvlc.common.jar" + "\" ";
 		Application.ensureRunningProcess("JVLCRegistry"+port, System.getProperty("java.home") + "/bin/rmiregistry.exe "+options+port, envp, null, null, null);
 		/*
 		Application.ensureRunningJAR(
 				"JVLCRMIRegistry"+port, 
 				path+"/net.lecousin.framework.rmiregistry.jar",
 				path+"/net.lecousin.framework.media.jvlc.server.jar;"+path+"/net.lecousin.framework.media.jvlc.client.jar", //"",//".;"+path+";"+path+"/net.lecousin.framework.media.jvlc.server.jar;"+path+"/net.lecousin.framework.media.jvlc.client.jar;"+path+"/net.lecousin.framework.jar",
 				//"-Djava.security.policy=file://" + path.replace('\\', '/') + "/rmi.security " +
 				//" -Djava.rmi.server.codebase=file://" + path.replace('\\', '/') + //"/net.lecousin.framework.media.jvlc.client.jar",
 				"",
 				" -port " + port, 
 				envp,
 			new Listener<Process>() {
 				public void fire(Process event) {
 					if (Log.info(JVLCClient.class))
 						Log.info(JVLCClient.class, "Starting JVLC RMI Registry");
 					LineNumberReader out = new LineNumberReader(new InputStreamReader(event.getInputStream()));
 					do {
 						long start = System.currentTimeMillis();
 						try {
 							while (!out.ready() && System.currentTimeMillis()-start < 10000)
 								try { Thread.sleep(10); } catch (InterruptedException e) { break; }
 							if (!out.ready()) {
 								if (Log.error(this))
 									Log.error(this, "Unable to start RMIRegistry");
 								return;
 							}
 							String line = out.readLine(); 
 							if (line.contains("Registry launched."))
 								break;
 							if (Log.warning(this))
 								Log.warning(this, "Unexpected output from RMIRegistry: " + line);
 						} catch (IOException e) { break; }
 					} while (true);
 					if (Log.info(JVLCClient.class))
 						Log.info(JVLCClient.class, "JVLC RMI Registry started.");
 				}				
 			},
 			new Listener<Process>() {
 				public void fire(Process event) {
 					try {
 						if (Log.info(JVLCClient.class))
 							Log.info(JVLCClient.class, "Closing JVLC RMI Registry");
 						event.getOutputStream().write("exit".getBytes());
 						event.getOutputStream().flush();
 						for (int i = 0; i < 50; ++i) {
 							try { 
 								event.exitValue();
 								break; 
 							} catch (IllegalThreadStateException e) { 
 								try { Thread.sleep(100); } catch (InterruptedException e2) { break; }
 							}
 						}
 					}
 					catch (IOException e) {
 						if (Log.warning(JVLCClient.class))
 							Log.warning(JVLCClient.class, "Error while sending exit event to JVLC RMI Registry", e);
 					}
 				}
 			});*/
 		registries.add(port);
 		return true;
 	}
 	
 	private static void launchServer(int port, String path, Listener<Integer> crashListener) throws IOException {
 		Map<String,String> env = System.getenv();
 		String[] envp = new String[env.size()];
 		int i = 0;
 		for (String s : env.keySet())
 			if (s.equalsIgnoreCase("path"))
 				envp[i++] = s + "=" + env.get(s)+";"+path+"\\vlc";
 			else
 				envp[i++] = s + "=" + env.get(s);
 		serverProcess = Application.ensureRunningJAR(
 				"JVLCServer"+port, 
 				path+"/net.lecousin.media.jvlc.server.jar", 
				path,
 				"",
 				"-vlc \"" + path + "\\vlc\"" + " -port " + port, 
 				envp,
 			new ProcessChecker() {
 					private boolean ready = false;
 					public ProcessChecker processOutput(String line) {
 						if (line.contains("JVLCServer ready.")) {
 							ready = true;
 							return null;
 						}
 						return this;
 					}
 					public void waitReady() {
 						if (Log.info(JVLCClient.class))
 							Log.info(JVLCClient.class, "Starting JVLCServer");
 						long start = System.currentTimeMillis();
						while (!ready && System.currentTimeMillis()-start < 10000)
 							try { Thread.sleep(10); } catch (InterruptedException e) { break; }
 						if (!ready) {
 							if (Log.error(this))
 								Log.error(this, "Unable to start JVLCServer");
 							return;
 						}
 						if (Log.info(JVLCClient.class))
 							Log.info(JVLCClient.class, "JVLCServer started.");
 					}
 			},
 			new Listener<Process>() {
 				public void fire(Process event) {
 					try {
 						if (Log.info(JVLCClient.class))
 							Log.info(JVLCClient.class, "Closing JVLCServer");
 						event.getOutputStream().write("exit".getBytes());
 						event.getOutputStream().flush();
 						for (int i = 0; i < 50; ++i) {
 							try { 
 								event.exitValue();
 								break; 
 							} catch (IllegalThreadStateException e) { 
 								try { Thread.sleep(100); } catch (InterruptedException e2) { break; }
 							}
 						}
 					}
 					catch (IOException e) {
 						if (Log.warning(JVLCClient.class))
 							Log.warning(JVLCClient.class, "Error while sending exit event to JVLCServer", e);
 					}
 				}
 			},
 			crashListener);
 	}
 	
 	public static void free(IJVLCMediaManager server) {
 		// nothing to do for now
 	}
 	public static void kill() {
 		if (serverProcess != null) {
 			serverProcess.destroy();
 			serverProcess = null;
 		}
 	}
 	
 	private static int transaction = 0;
 	public static void startTransaction() {
 		synchronized (events) {
 			transaction++;
 		}
 	}
 	@SuppressWarnings("unchecked")
 	public static void endTransaction() {
 		synchronized (events) {
 			if (transaction > 0) transaction--;
 			if (transaction == 0) {
 				ArrayList<Pair<Event,Object>> list = new ArrayList<Pair<Event,Object>>(events);
 				events.clear();
 				for (Pair<Event,Object> p : list)
 					p.getValue1().fire(p.getValue2());
 			}
 		}
 	}
 	@SuppressWarnings("unchecked")
 	public static void endTransactionStop() {
 		synchronized (events) {
 			for (Iterator<Pair<Event,Object>> it = events.iterator(); it.hasNext(); ) {
 				Pair<Event,Object> p = it.next();
 				if (p.getValue1() == ended)
 					it.remove();
 			}
 		}
 		endTransaction();
 	}
 	@SuppressWarnings("unchecked")
 	private static List<Pair<Event,Object>> events = new LinkedList<Pair<Event,Object>>();
 	
 	@SuppressWarnings("unchecked")
 	static void started(IJVLCMedia m) { synchronized(events) { if (transaction == 0) started.fire(m); else events.add(new Pair<Event,Object>(started, m)); } } 
 	@SuppressWarnings("unchecked")
 	static void paused(IJVLCMedia m) { synchronized(events) { if (transaction == 0) paused.fire(m); else events.add(new Pair<Event,Object>(paused, m)); } } 
 	@SuppressWarnings("unchecked")
 	static void stopped(IJVLCMedia m) { synchronized(events) { if (transaction == 0) stopped.fire(m); else events.add(new Pair<Event,Object>(stopped, m)); } } 
 	@SuppressWarnings("unchecked")
 	static void ended(IJVLCMedia m) { synchronized(events) { if (transaction == 0) ended.fire(m); else events.add(new Pair<Event,Object>(ended, m)); } } 
 	@SuppressWarnings("unchecked")
 	static void positionChanged(IJVLCMedia m) { synchronized(events) { if (transaction == 0) positionChanged.fire(m); else events.add(new Pair<Event,Object>(positionChanged, m)); } } 
 	@SuppressWarnings("unchecked")
 	static void timeChanged(Pair<IJVLCMedia,Long> m) { synchronized(events) { if (transaction == 0) timeChanged.fire(m); else events.add(new Pair<Event,Object>(timeChanged, m)); } } 
 }
