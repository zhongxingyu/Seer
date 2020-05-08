 package acs.jpbs.server;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import acs.jpbs.core.PbsJob;
 import acs.jpbs.core.PbsQueue;
 import acs.jpbs.core.PbsServer;
 import acs.jpbs.net.jPBSClientInterface;
 import acs.jpbs.net.jPBSServerInterface;
 import acs.jpbs.server.core.IPbsObject;
 import acs.jpbs.server.core.PbsJobHandler;
 import acs.jpbs.server.core.PbsQueueHandler;
 import acs.jpbs.server.core.PbsServerHandler;
 import acs.jpbs.serverUtils.PbsEnvironment;
 import acs.jpbs.serverUtils.PbsUpdateManager;
 import acs.jpbs.utils.Logger;
 
 public class jPBSServer extends UnicastRemoteObject implements jPBSServerInterface {
 
 	private static final long serialVersionUID = 7037513788004765212L;
 	public static PbsServerHandler pbsServer = null;
 	public final static String serviceName = "jPBS-Server";
 	public final static int servicePort = 31122;
 	public List<jPBSClientInterface> clients = new LinkedList<jPBSClientInterface>();
 	private transient final ReentrantReadWriteLock clientsLock = new ReentrantReadWriteLock(true);
 	protected transient final Lock clientsReadLock = clientsLock.readLock();
 	protected transient final Lock clientsWriteLock = clientsLock.writeLock();
 	
 	public jPBSServer() throws RemoteException {
 		super();
 		if(PbsEnvironment.initEnv()) {
 			Logger.logInfo("Environment loaded, 'qstat' utility found at '"+PbsEnvironment.qstat+"'");
 			Logger.logInfo("'qmgr' utility found at '"+PbsEnvironment.qmgr+"'");
 		} else Logger.logError("Failed to load environment info");
 		pbsServer = PbsServerHandler.getInstance();
 		// Update server in the background
 		PbsUpdateManager.beginUpdate(pbsServer);
 	}
 		
 	public PbsServer getServerObject() throws RemoteException { return (PbsServer)pbsServer; }
 	
 	public PbsQueue[] getQueueArray() throws RemoteException {
 		if(pbsServer.queues == null || pbsServer.queues.isEmpty()) return null;
 		PbsQueue[] returnArr = null;
 		
 		pbsServer.queueMapReadLock.lock();
 		try {
 			returnArr = new PbsQueue[pbsServer.queues.size()];
 			int i = 0;
 			for(Entry<String, PbsQueue> qEntry : pbsServer.queues.entrySet()) {
 				returnArr[i++] = qEntry.getValue();
 			}
 		} finally {
 			pbsServer.queueMapReadLock.unlock();
 		}
 		return returnArr;
 	}
 	
 	public PbsJob[] getJobArray() throws RemoteException { 
 		if(pbsServer.queues == null || pbsServer.queues.isEmpty()) return null;
 		List<PbsJob> returnList = new ArrayList<PbsJob>();
 		
 		pbsServer.queueMapReadLock.lock();
 		try {
 			for(Entry<String, PbsQueue> qEntry : pbsServer.queues.entrySet()) {
 				PbsQueue qValue = qEntry.getValue();
 				qValue.jobMapReadLock.lock();
 				try {
 					for(Entry<Integer, PbsJob> jEntry : qValue.jobs.entrySet()) {
 						returnList.add(jEntry.getValue());
 					}
 				} finally {
 					qValue.jobMapReadLock.unlock();
 				}
 			}
 		} finally {
 			pbsServer.queueMapReadLock.unlock();
 		}
 		
 		if(returnList.isEmpty()) return null;
 		else return (PbsJob[])returnList.toArray();
 	}
 	
 	public void register(jPBSClientInterface newClient) throws RemoteException {
 		clientsWriteLock.lock();
 		try {
 			this.clients.add(newClient);
 		} finally {
 			clientsWriteLock.unlock();
 		}
 	}
 	
 	public void updateClients(IPbsObject newObj) {
 		clientsReadLock.lock();
 		try {
 			Iterator<jPBSClientInterface> itr = clients.iterator();
 			while(itr.hasNext()) {
 				jPBSClientInterface client = itr.next();
 				try {
 					if(newObj instanceof PbsJobHandler) client.updateJob((PbsJob)newObj);
 					else if(newObj instanceof PbsQueueHandler) client.updateQueue((PbsQueue)newObj);
 					else if(newObj instanceof PbsServerHandler) client.updateServer((PbsServer)newObj);
 				} catch (Exception e) {
 					Logger.logError("Unable to update client '"+client.toString()+"'");
 				}
 			}
 		} finally {
 			clientsReadLock.unlock();
 		}
 	}
 	
	public static void main(String args[]) {
		System.setProperty("java.rmi.server.hostname", "LT1109A");
		
 		Logger.logInfo("Initializing jPBS Server service...");
 		jPBSServerInterface service = null;
 		try {
 			service = new jPBSServer();
 			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
 			registry.bind(serviceName, service);
 			Logger.logInfo("jPBS service running.");
 		} catch (Exception e) {
 			Logger.logException("Error initializing service", e);
 		}	
 	}
 }
