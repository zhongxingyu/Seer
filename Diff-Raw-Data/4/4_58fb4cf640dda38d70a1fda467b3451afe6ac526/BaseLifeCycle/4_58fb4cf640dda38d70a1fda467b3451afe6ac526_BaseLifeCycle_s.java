 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.common;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 
 import javax.jbi.JBIException;
 import javax.jbi.component.ComponentContext;
 import javax.jbi.component.ComponentLifeCycle;
 import javax.jbi.messaging.DeliveryChannel;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.resource.spi.work.Work;
 import javax.resource.spi.work.WorkManager;
 import javax.transaction.Status;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 
 import org.apache.commons.logging.Log;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Base class for life cycle management of components.
  * This class may be used as is.
  * 
  * @author Guillaume Nodet
  * @version $Revision$
  * @since 3.0
  */
 public class BaseLifeCycle implements ComponentLifeCycle {
 
     protected final transient Log logger;
     
     protected BaseComponent component;
     protected ComponentContext context;
     protected ObjectName mbeanName;
     protected WorkManager workManager;
     protected AtomicBoolean running;
     protected DeliveryChannel channel;
     protected Thread poller;
     protected AtomicBoolean polling;
     protected TransactionManager transactionManager;
     protected boolean workManagerCreated;
     protected Map processors = new ConcurrentHashMap();
     
     
     public BaseLifeCycle(BaseComponent component) {
         this.component = component;
         this.logger = component.logger;
         this.running = new AtomicBoolean(false);
         this.polling = new AtomicBoolean(false);
         this.processors = new ConcurrentHashMap();
     }
     
     /* (non-Javadoc)
      * @see javax.jbi.component.ComponentLifeCycle#getExtensionMBeanName()
      */
     public ObjectName getExtensionMBeanName() {
         return mbeanName;
     }
 
     protected Object getExtensionMBean() throws Exception {
         return null;
     }
     
     protected ObjectName createExtensionMBeanName() throws Exception {
         return this.context.getMBeanNames().createCustomComponentMBeanName("Configuration");
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.ComponentLifeCycle#init(javax.jbi.component.ComponentContext)
      */
     public void init(ComponentContext context) throws JBIException {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Initializing component");
             }
             this.context = context;
             this.channel = context.getDeliveryChannel();
             this.transactionManager = (TransactionManager) context.getTransactionManager();
             doInit();
             if (logger.isDebugEnabled()) {
                 logger.debug("Component initialized");
             }
         } catch (JBIException e) {
             throw e;
         } catch (Exception e) {
             throw new JBIException("Error calling init", e);
         }
     }
 
     protected void doInit() throws Exception {
         // Register extension mbean
         Object mbean = getExtensionMBean();
         if (mbean != null) {
             MBeanServer server = this.context.getMBeanServer();
             if (server == null) {
                 // TODO: log a warning ?
                 //throw new JBIException("null mBeanServer");
             } else {
                 this.mbeanName = createExtensionMBeanName();
                 if (server.isRegistered(this.mbeanName)) {
                     server.unregisterMBean(this.mbeanName);
                 }
                 server.registerMBean(mbean, this.mbeanName);
             }
         }
         // Obtain or create the work manager
         // When using the WorkManager from ServiceMix,
         // some class loader problems can appear when
         // trying to uninstall the components.
         // Some threads owned by the work manager have a 
         // security context referencing the component class loader
         // so that every loaded classes are locked
         //this.workManager = findWorkManager();
         if (this.workManager == null) {
             this.workManagerCreated = true;
             this.workManager = createWorkManager();
         }
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.ComponentLifeCycle#shutDown()
      */
     public void shutDown() throws JBIException {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Shutting down component");
             }
             doShutDown();
             this.context = null;
             if (logger.isDebugEnabled()) {
                 logger.debug("Component shut down");
             }
         } catch (JBIException e) {
             throw e;
         } catch (Exception e) {
             throw new JBIException("Error calling shutdown", e);
         }
     }
 
     protected void doShutDown() throws Exception {
         // Unregister mbean
         if (this.mbeanName != null) {
             MBeanServer server = this.context.getMBeanServer();
             if (server == null) {
                 throw new JBIException("null mBeanServer");
             }
             if (server.isRegistered(this.mbeanName)) {
                 server.unregisterMBean(this.mbeanName);
             }
         }
         // Destroy work manager, if created
         if (this.workManagerCreated) {
             if (this.workManager instanceof BasicWorkManager) {
                 ((BasicWorkManager) this.workManager).shutDown();
             }
         }
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.ComponentLifeCycle#start()
      */
     public void start() throws JBIException {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Starting component");
             }
             if (this.running.compareAndSet(false, true)) {
                 doStart();
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("Component started");
             }
         } catch (JBIException e) {
             throw e;
         } catch (Exception e) {
            throw new JBIException("Error calling init", e);
         }
     }
 
     protected void doStart() throws Exception {
         synchronized (this.polling) {
             workManager.startWork(new Work() {
                 public void release() { }
                 public void run() {
                     poller = Thread.currentThread();
                     pollDeliveryChannel();
                 }
             });
             polling.wait();
         }
     }
     
     protected void pollDeliveryChannel() {
         synchronized (polling) {
             polling.set(true);
             polling.notify();
         }
         while (running.get()) {
             try {
                 final MessageExchange exchange = channel.accept(1000L);
                 if (exchange != null) {
                     final Transaction tx = (Transaction) exchange.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME);
                     if (tx != null) {
                         if (transactionManager == null) {
                             throw new IllegalStateException("Exchange is enlisted in a transaction, but no transaction manager is available");
                         }
                         transactionManager.suspend();
                     }
                     workManager.scheduleWork(new Work() {
                         public void release() {
                         }
                         public void run() {
                             try {
                                 if (tx != null) {
                                     transactionManager.resume(tx);
                                 }
                                 processExchange(exchange);
                             } catch (Throwable t) {
                                 logger.error("Error processing exchange " + exchange, t);
                                 // Set an error on message
                                 try {
                                     if (t instanceof Exception) {
                                         exchange.setError((Exception) t);
                                     } else {
                                         exchange.setError(new Exception("Throwable", t));
                                     }
                                     channel.send(exchange);
                                 } catch (Exception inner) {
                                     logger.error("Error setting exchange status to ERROR", inner);
                                 }
                             } finally {
                                 // Check transaction status
                                 if (tx != null) {
                                     int status = Status.STATUS_NO_TRANSACTION;
                                     try {
                                         status = transactionManager.getStatus();
                                     } catch (Throwable t) {
                                         logger.error("Error checking transaction status.", t);
                                     }
                                     if (status != Status.STATUS_NO_TRANSACTION) {
                                         logger.error("Transaction is still active after exchange processing. Trying to rollback transaction.");
                                         try {
                                             transactionManager.rollback();
                                         } catch (Throwable t) {
                                             logger.error("Error trying to rollback transaction.", t);
                                         }
                                     }
                                 }
                             }
                         }
                     });
                 }
             } catch (Throwable t) {
                 if (running.get() == false) {
                     // Should have been interrupted, discard the throwable
                     if (logger.isDebugEnabled()) {
                         logger.debug("Polling thread will stop");
                     }
                 } else {
                     logger.error("Error polling delivery channel", t);
                 }
             }
         }
         synchronized (polling) {
             polling.set(false);
             polling.notify();
         }
     }
 
     /* (non-Javadoc)
      * @see javax.jbi.component.ComponentLifeCycle#stop()
      */
     public void stop() throws JBIException {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Stopping component");
             }
             if (this.running.compareAndSet(true, false)) {
                 doStop();
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("Component stopped");
             }
         } catch (JBIException e) {
             throw e;
         } catch (Exception e) {
             throw new JBIException("Error calling stop", e);
         }
     }
 
     protected void doStop() throws Exception {
         // Interrupt the polling thread and await termination
         try {
             synchronized (polling) {
                 if (polling.get()) {
                     poller.interrupt();
                     polling.wait();
                 }
             }
         } finally {
             poller = null;
         }
     }
 
     /**
      * @return Returns the context.
      */
     public ComponentContext getContext() {
         return context;
     }
 
     public WorkManager getWorkManager() {
         return workManager;
     }
 
     protected WorkManager createWorkManager() {
         // Create a very simple one
         return new BasicWorkManager();
     }
 
     protected WorkManager findWorkManager() {
         // If inside ServiceMix, retrieve its work manager
         try {
             Method getContainerMth = context.getClass().getMethod("getContainer", new Class[0]);
             Object container = getContainerMth.invoke(context, new Object[0]);
             Method getWorkManagerMth = container.getClass().getMethod("getWorkManager", new Class[0]);
             return (WorkManager) getWorkManagerMth.invoke(container, new Object[0]);
         } catch (Throwable t) {
             if (logger.isDebugEnabled()) {
                 logger.debug("JBI container is not ServiceMix. Will create our own WorkManager", t);
             }
         }
         // TODO: should look in jndi for an existing work manager
         return null;
     }
     
     public void processExchange(MessageExchange exchange) throws Exception {
         if (logger.isDebugEnabled()) {
             logger.debug("Received exchange: status: " + exchange.getStatus() + ", role: " + exchange.getRole());
         }
         if (exchange.getRole() == Role.PROVIDER) {
             String key = EndpointSupport.getKey(exchange.getEndpoint());
             Endpoint ep = (Endpoint) this.component.getRegistry().getEndpoint(key);
             if (ep == null) {
                 throw new IllegalStateException("Endpoint not found: " + key);
             }
             ExchangeProcessor processor = ep.getProcessor();
             if (processor == null) {
                 throw new IllegalStateException("No processor found for endpoint: " + key);
             }
             processor.process(exchange);
         } else {
             ExchangeProcessor processor = (ExchangeProcessor) processors.remove(exchange.getExchangeId());
             if (processor == null) {
                 throw new IllegalStateException("No processor found for: " + exchange.getExchangeId());
             }
             processor.process(exchange);
         }
     }
 
     public void sendConsumerExchange(MessageExchange exchange, ExchangeProcessor processor) throws MessagingException {
         // If this is a DONE status, no answer is expected
         if (exchange.getStatus() != ExchangeStatus.DONE) {
             processors.put(exchange.getExchangeId(), processor);
         }
         channel.send(exchange);
     }
 
 }
