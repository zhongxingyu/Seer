 package edu.teco.dnd.eclipse;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import edu.teco.dnd.discover.ModuleQuery;
 import edu.teco.dnd.module.Module;
 import edu.teco.dnd.network.ConnectionListener;
 import edu.teco.dnd.network.ConnectionManager;
 import edu.teco.dnd.network.UDPMulticastBeacon;
 import edu.teco.dnd.util.FutureListener;
 import edu.teco.dnd.util.FutureNotifier;
 
 /**
  * This class coordinates a List of currently running modules. It provides the
  * views and editors with information to display to the user.
  * 
  * @author jung
  * 
  */
 public class ModuleManager implements ConnectionListener,
 DNDServerStateListener,FutureListener<FutureNotifier<Module>> {
 	private Map<UUID,Module> map;
 	
 	private final Set<ModuleManagerListener> moduleManagerListener = new HashSet<ModuleManagerListener>();
 	private final ReadWriteLock moduleLock = new ReentrantReadWriteLock();
 	
 	private Activator activator;
 	private ConnectionManager connectionManager;
 	private ModuleQuery query;
 	
 	public ModuleManager(){
 		map = new HashMap<UUID,Module>();
 		
 		activator = Activator.getDefault();
 		activator.addServerStateListener(this);
 	}
 	
 	
 	public void addModuleManagerListener(final ModuleManagerListener listener){
 		moduleLock.writeLock().lock();
 		try {
 			moduleManagerListener.add(listener);
 			if (connectionManager == null) {
 				listener.serverOffline();
 			} else {
 				listener.serverOnline(map);
 			}
 		} finally {
 			moduleLock.writeLock().unlock();
 		}
 	}
 	
 	public void removeModuleManagerListener(final ModuleManagerListener listener){
 		moduleLock.writeLock().lock();
 		moduleManagerListener.remove(listener);
 		moduleLock.writeLock().unlock();
 	}
 	
 	@Override
 	public void serverStarted(ConnectionManager externConnectionManager,
 			UDPMulticastBeacon beacon) {
		query = new ModuleQuery(connectionManager);
		
 		connectionManager = externConnectionManager;
 		for (UUID id : new ArrayList<UUID>(map.keySet())) {
 			map.remove(id);
 		}
 		connectionManager.addConnectionListener(this);
 		Collection<UUID> modules = connectionManager.getConnectedModules();
 
 		for (UUID id : modules) {
 			map.put(id, null);
 			query.getModuleInfo(id).addListener(this);
 		}
 		//TODO: Synchronisieren
 		for (final ModuleManagerListener listener : moduleManagerListener) {
 			listener.serverOnline(map);
 		}
 	}
 
 	@Override
 	public void serverStopped() {
 		map.clear();
 		if (connectionManager != null) {
 			connectionManager.removeConnectionListener(this);
 		}
 		for (final ModuleManagerListener listener : moduleManagerListener) {
 			listener.serverOffline();
 		}
 	}
 
 	@Override
 	public void connectionEstablished(UUID uuid) {
 		map.put(uuid, null);
 		query.getModuleInfo(uuid).addListener(this);
 		
 		for (final ModuleManagerListener listener : moduleManagerListener) {
 			listener.moduleOnline(uuid);
 		}
 	}
 
 	@Override
 	public void connectionClosed(UUID uuid) {
 		map.remove(uuid);
 		for (final ModuleManagerListener listener : moduleManagerListener) {
 			listener.moduleOffline(uuid);
 		}
 	}
 	
 	@Override
 	public void operationComplete(FutureNotifier<Module> future)
 			throws Exception {
 		if (future.isSuccess()) {
 			Module module = future.getNow();
 			UUID id = module.getUUID();
 			map.put(id, module);
 
 			for (final ModuleManagerListener listener : moduleManagerListener) {
 				listener.moduleResolved(id, module);
 			}
 		}
 		
 	}
 	
 	public Map<UUID, Module> getMap(){
 		return map;
 	}
 	
 	
 }
