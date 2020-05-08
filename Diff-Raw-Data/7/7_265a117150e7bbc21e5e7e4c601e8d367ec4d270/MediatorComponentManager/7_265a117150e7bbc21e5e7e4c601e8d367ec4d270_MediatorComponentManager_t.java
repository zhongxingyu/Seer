 /*
  * Copyright Adele Team LIG
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.liglab.adele.cilia.internals.factories;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import org.apache.felix.ipojo.ComponentInstance;
 import org.apache.felix.ipojo.ConfigurationException;
 import org.apache.felix.ipojo.Handler;
 import org.apache.felix.ipojo.HandlerManager;
 import org.apache.felix.ipojo.InstanceManager;
 import org.apache.felix.ipojo.metadata.Element;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.liglab.adele.cilia.exceptions.CiliaRuntimeException;
 import fr.liglab.adele.cilia.model.Component;
 import fr.liglab.adele.cilia.model.MediatorComponent;
 import fr.liglab.adele.cilia.model.Port;
 import fr.liglab.adele.cilia.model.impl.Dispatcher;
 import fr.liglab.adele.cilia.model.impl.Scheduler;
 import fr.liglab.adele.cilia.runtime.MediatorDescriptionEntry;
 import fr.liglab.adele.cilia.runtime.impl.DispatcherHandler;
 import fr.liglab.adele.cilia.runtime.impl.DispatcherInstanceManager;
 import fr.liglab.adele.cilia.runtime.impl.MonitorHandler;
 import fr.liglab.adele.cilia.runtime.impl.SchedulerHandler;
 import fr.liglab.adele.cilia.runtime.impl.SchedulerInstanceManager;
 import fr.liglab.adele.cilia.util.Const;
 import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
 import fr.liglab.adele.cilia.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
 
 /**
  * 
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
  *         Team</a>
  * 
  */
 public abstract class MediatorComponentManager extends InstanceManager {
 
 	protected final MediatorComponentFactory mfactory;
 
 	protected SchedulerInstanceManager schedulerManager;
 
 	protected DispatcherInstanceManager dispatcherManager;
 
 	private static Logger logger = LoggerFactory.getLogger(Const.LOGGER_CORE);
 
 	private MonitorHandler monitor;
 
 	protected volatile int ciliaState = MediatorComponent.INVALID;
 
 	protected Dictionary configuration;
 
 	protected volatile int processing = 0;
 
 	protected final ReadWriteLock mutex;
 
 	private ServiceRegistration entryRegistry;
 
 
 
 	/**
 	 * @param factory
 	 * @param context
 	 * @param handlers
 	 */
 	public MediatorComponentManager(MediatorComponentFactory factory,
 			BundleContext context, HandlerManager[] handlers) {
 		super(factory, context, handlers);
 		mfactory = factory;
 		mutex = new ReentrantWriterPreferenceReadWriteLock();
 	}
 
 	public Port getInPort(String name) {
 		return this.mfactory.getInPort(name);
 	}
 
 	public Port getOutPort(String name) {
 		return this.mfactory.getOutPort(name);
 	}
 
 	public void startManagers() {
 		updateSchedulerManager();
 		updateDispatcherManager();
 		configureHandlersMonitoring();
 		addDescriptionEntry();
 	}
 
 	public void stopManagers() {
 		stopSchedulerManager();
 		stopDispatcherManager();
 		removeDescriptionEntry();
 	}
 
 	private void configureHandlerMonitoring(InstanceManager im,
 			String handlerName) {
 		Handler handler = (Handler) (im).getHandler(Const
 				.ciliaQualifiedName(handlerName));
 		if (handler != null) {
 			Properties props = new Properties();
 			props.put("cilia.monitor.handler", getMonitor());
 			handler.reconfigure(props);
 		}
 	}
 
 	private MonitorHandler getMonitor() {
 		if (monitor == null) {
 			monitor = (MonitorHandler) getInstanceManager().getHandler(
 					Const.ciliaQualifiedName("monitor-handler"));
 		}
 		return monitor;
 	}
 
 	private void configureHandlersMonitoring() {
 		InstanceManager im = getInstanceManager();
 		configureHandlerMonitoring(im, "dependency");
 		configureHandlerMonitoring(im, "audit");
 	}
 
 	private void updateSchedulerManager() {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {	}
 		try{
 			if (schedulerManager != null) {
 				return;
 			}
 		}finally{
 			mutex.readLock().release();
 		}
 		String schedulerName = mfactory.getSchedulerDescription().getId();
 		String schedulerNS = mfactory.getSchedulerDescription().getNamespace();
 
 		Scheduler schedulerDescription = new Scheduler("scheduler",
 				schedulerName, schedulerNS, configuration);
 
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {	}
 		schedulerManager = new SchedulerInstanceManager(getContext(),
 				getScheduler(), schedulerDescription, this);
 		mutex.writeLock().release();
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			schedulerManager.start();
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	private void stopSchedulerManager() {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			if (schedulerManager != null) {
 				schedulerManager.stop();
 			}
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	private synchronized void updateDispatcherManager() {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			if (dispatcherManager != null) {
 				return;
 			}
 		}finally{
 			mutex.readLock().release();
 		}
 
 		String dispatcherName = mfactory.getDispatcherDescription().getId();
 		String dispatcherNS = mfactory.getDispatcherDescription()
 				.getNamespace();
 
 		Dispatcher dispatcherDescription = new Dispatcher("dispatcher",
 				dispatcherName, dispatcherNS, configuration);
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {}
 
 		dispatcherManager = new DispatcherInstanceManager(getContext(),
 				getDispatcher(), dispatcherDescription, this);
 		mutex.writeLock().release();
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			dispatcherManager.start();
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	private synchronized void stopDispatcherManager() {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			if (dispatcherManager != null) {
 				dispatcherManager.stop();
 			}
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	public synchronized void removeCollector(String port, Component collector) {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			if (schedulerManager == null) {
 				logger.warn("Unable to remove Collector {} : Manager is invalid",
 						collector.getId());
 				return;
 			}
 			schedulerManager.removeCollector(port, collector);
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	public synchronized void addCollector(String port, Component collector) {
 		updateSchedulerManager();
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			schedulerManager.addCollector(port, collector,true);
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	public synchronized void removeSender(String port, Component sender) {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			if (dispatcherManager == null) {
 				logger.warn("Unable to remove Sender {} : Manager is invalid",
 						sender.getId());
 				return;
 			}
 			dispatcherManager.removeSender(port, sender);
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	public synchronized void addSender(String port, Component sender) {
 		updateDispatcherManager();
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			dispatcherManager.addSender(port, sender, true);
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	private SchedulerHandler getScheduler() {
 		SchedulerHandler r = null;
 		InstanceManager im = getInstanceManager();
 		r = (SchedulerHandler) im.getHandler(Const
 				.ciliaQualifiedName("scheduler"));
 		return r;
 	}
 
 	private DispatcherHandler getDispatcher() {
 		DispatcherHandler r = null;
 		InstanceManager im = getInstanceManager();
 		r = (DispatcherHandler) im.getHandler(Const
 				.ciliaQualifiedName("dispatcher"));
 		return r;
 	}
 
 	private InstanceManager getInstanceManager() {
 		InstanceManager im = null;
 		ComponentInstance componentInstance = this;
 		if (componentInstance instanceof InstanceManager) {
 			im = (InstanceManager) componentInstance;
 		}
 		if (componentInstance instanceof MediatorManager) {
 			MediatorManager mm = (MediatorManager) componentInstance;
 			if (mm != null) {
 				im = (InstanceManager) mm.getProcessorInstance();
 			}
 		}
 		return im;
 	}
 
 	public void reconfigure(Dictionary configuration) {
 		super.reconfigure(configuration);
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {}
 		try{
 			schedulerManager.reconfigureConstituant(configuration);
 			dispatcherManager.reconfigureConstituant(configuration);
 		}finally{
 			mutex.readLock().release();
 		}
 	}
 
 	public void startProcessing(){
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {}
 		processing++;
 		mutex.writeLock().release();
 	}
 
 	public void stopProcessing(){
		try {
			mutex.writeLock().acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
 		processing--;
 		if (processing<0){
 			processing = 0;
 		}
		mutex.writeLock().release();
 	}
 
 	public synchronized void waitToProcessing(long maxtime) throws CiliaRuntimeException {
 		long currentTime = System.currentTimeMillis();
 		long finalTime = currentTime + maxtime;
 		while(isProcessing() != 0 && currentTime < finalTime ){
 			try {
 				Thread.sleep(100);
 				currentTime = System.currentTimeMillis();
 			} catch (InterruptedException e) {
 				throw new CiliaRuntimeException(e.getMessage());
 			}
 		}
 	}
 
 	private synchronized int isProcessing(){
 		int returningValue = 1;
 		returningValue = processing;
 		return returningValue;
 	}
 
 	public void configure(Element metadata, Dictionary config) throws ConfigurationException {
 		this.configuration = config;
 		super.configure(metadata, config);
 	}
 	
 	private void addDescriptionEntry(){
 		Hashtable props = new Hashtable();
 		props.put(Const.PROPERTY_INSTANCE_TYPE, configuration.get(Const.PROPERTY_INSTANCE_TYPE));
 		props.put(Const.PROPERTY_CHAIN_ID, configuration.get(Const.PROPERTY_CHAIN_ID));
 		props.put(Const.PROPERTY_COMPONENT_ID, configuration.get(Const.PROPERTY_COMPONENT_ID));
 		MediatorDescriptionEntry mde = new MediatorDescriptionEntry() {	};
 		entryRegistry = getContext().registerService(MediatorDescriptionEntry.class.getName(), mde, props);
 	}
 	
 	private void removeDescriptionEntry(){
 		entryRegistry.unregister();
 	}
 	
 }
