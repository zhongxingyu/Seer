 /*
  * Copyright (c) Members of the EGEE Collaboration. 2004. 
  * See http://www.eu-egee.org/partners/ for details on the copyright
  * holders.  
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *     http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package org.glite.ce.monitor.holder;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import javax.naming.NamingException;
 
 import org.apache.axis2.databinding.types.URI;
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.log4j.Logger;
 import org.glite.ce.monitor.configuration.CEMonServiceConfig;
 import org.glite.ce.monitor.registry.SubscriptionRegistry;
 import org.glite.ce.monitorapij.queryprocessor.QueryProcessor;
 import org.glite.ce.monitorapij.queryprocessor.QueryResult;
 import org.glite.ce.monitorapij.resource.types.Action;
 import org.glite.ce.monitorapij.resource.types.Dialect;
 import org.glite.ce.monitorapij.resource.types.Parameter;
 import org.glite.ce.monitorapij.resource.types.Policy;
 import org.glite.ce.monitorapij.resource.types.Query;
 import org.glite.ce.monitorapij.resource.types.SubscriptionPersistent;
 import org.glite.ce.monitorapij.resource.types.Topic;
 import org.glite.ce.monitorapij.sensor.SensorEvent;
 import org.glite.ce.monitorapij.sensor.SensorOutputDataFormat;
 import org.glite.ce.monitorapij.types.Event;
 import org.glite.ce.monitorapij.ws.CEMonitorConsumerStub;
 
 import eu.emi.security.canl.axis2.CANLAXIS2SocketFactory;
 
 /**
  * This classed is used to take care of all aspects relating notifications. It
  * uses a set of classes to process a query request for sensor events, apply
  * predefined dialects and actions to these events, and hold a list of listeners
  * subscribed for notifications. Finally it submits the opportunely formatted
  * notifications.
  */
 public final class NotificationHolder
     implements Runnable {
 
     private final static Logger logger = Logger.getLogger(NotificationHolder.class.getName());
 
     private final static int EVN_PAGE_SIZE = 100;
 
     private final static String CREDENTIALS_PROXY_FILE = "proxy";
 
     private final static String CREDENTIALS_CERT_FILE = "cert";
 
     private final static String CREDENTIALS_KEY_FILE = "key";
 
     private final static String CREDENTIALS_KEY_PASSWD = "password";
 
     private final static String CA_LOCATION = "truststore";
 
     private final static String CRL_MODE = "crlcheckingmode";
 
     private final static String CACRL_REFRESH = "updateinterval";
 
     private String name;
 
     private TopicHolder topicHolder;
 
     private int rate = 0;
 
     private QueryProcessorHolder queryProcessorHolder;
 
     private SubscriptionRegistry subscriptionRegistry;
 
     private boolean exit = false;
 
     private Thread notificationThread;
 
     private HashMap<String, SubscriptionPersistent> subscrMap;
 
     private NotificationPool notificationPool;
 
     /**
      * Creates a new NotificationHolder object specifying the rate and the
      * <code>TopicHolder</code>.
      * 
      * @param name
      *            The name of this holder
      * @param rate
      *            The subscription rate.
      * @param topicHolder
      *            The <code>TopicHolder</code>.
      */
     public NotificationHolder(String name, int rate, TopicHolder topicHolder,
             QueryProcessorHolder queryProcessorHolder, SubscriptionRegistry subscriptionRegistry, NotificationPool pool)
         throws IllegalArgumentException {
 
         super();
 
         if (name == null) {
             throw new IllegalArgumentException("name not specified!");
         }
 
         this.name = name;
 
         if (rate <= 0) {
             throw new IllegalArgumentException("rate must be > 0!");
         }
 
         if (topicHolder == null) {
             throw new IllegalArgumentException("topicHolder not specified!");
         }
 
         if (queryProcessorHolder == null) {
             throw new IllegalArgumentException("queryProcessorHolder not specified!");
         }
 
         if (subscriptionRegistry == null) {
             throw new IllegalArgumentException("subscriptionRegistry not specified!");
         }
 
         this.rate = rate;
         this.topicHolder = topicHolder;
         this.queryProcessorHolder = queryProcessorHolder;
         this.subscriptionRegistry = subscriptionRegistry;
         this.notificationPool = pool;
 
         subscrMap = new HashMap<String, SubscriptionPersistent>();
 
         Protocol.registerProtocol("https", new Protocol("https", new CANLAXIS2SocketFactory(), 8443));
 
         notificationThread = new Thread(this);
         notificationThread.setName(name);
         notificationThread.start();
     }
 
     public String getName() {
         return name;
     }
 
     /**
      * Get the number of registered subscriptions.
      * 
      * @return The size of the subscription map.
      */
     public synchronized int getSubscriptionListSize() {
         return subscrMap.size();
     }
 
     /**
      * Add a <code>SubscriptionPersistent</code> to the list of the registered
      * subscriptions.
      * 
      * @param subscription
      *            The <code>SubscriptionPersistent</code> to add.
      */
     public synchronized void addSubscription(SubscriptionPersistent subscription) {
         if (subscription != null) {
             /*
              * TODO missing clone
              */
             subscrMap.put(subscription.getId(), subscription);
         }
     }
 
     /**
      * Remove a <code>SubscriptionPersistent</code> from the list of the
      * registered subscriptions.
      * 
      * @param subscription
      *            The <code>SubscriptionPersistent</code> to remove.
      */
     public synchronized void removeSubscription(SubscriptionPersistent subscription) {
         if (subscription != null) {
             subscrMap.remove(subscription.getId());
         }
     }
 
     public synchronized List<SubscriptionPersistent> getSubscriptions() {
         return new ArrayList<SubscriptionPersistent>(subscrMap.values());
     }
 
     private void processPolicy(SensorEventArrayList eventList, Policy policy,
             CEMonitorConsumerStub.Notification notification, SubscriptionPersistent subscription)
         throws Exception {
 
         Query query = policy.getQuery();
         QueryProcessor qp = null;
 
         Action[] actions = policy.getAction();
         if (actions == null) {
             throw (new Exception("policy.actions not found!"));
         }
 
         if (query != null) {
 
             qp = queryProcessorHolder.getQueryProcessor(query.getQueryLanguage());
 
             if (qp == null) {
                 logger.error("QueryProcessor \"" + query.getQueryLanguage() + "\" not found!");
                 throw (new Exception("QueryProcessor \"" + query.getQueryLanguage() + "\" not found!"));
             }
         }
 
         Dialect[] dialectArray = subscription.getTopic().getDialect();
         String dataFormat = "default";
 
         if ((dialectArray != null) && dialectArray.length > 0 && dialectArray[0].getName() != null) {
             dataFormat = dialectArray[0].getName();
         }
 
         ArrayList<Event> notificationEventList = new ArrayList<Event>(0);
         ArrayList<QueryResult> queryResultList = new ArrayList<QueryResult>(0);
 
         for (int i = 0; i < eventList.size(); i++) {
             SensorEvent event = eventList.get(i);
 
             if (event == null) {
                 continue;
             }
 
             if (event.isExpired()) {
 
                 try {
                     logger.debug("removing expired event [id=" + event.getID() + "] [name=" + event.getName()
                             + "] [expirationTime=" + event.getExpirationTime() + "]");
                     topicHolder.removeEvent(event);
                 } catch (Throwable t) {
                     logger.error("cannot remove the event [id=" + event.getID() + "] [name=" + event.getName() + "]!",
                             t);
                 }
 
                 eventList.remove(i);
 
                 continue;
 
             }
 
             SensorOutputDataFormat format = event.getSensorOutputDataFormatApplied();
 
             if (format == null || !format.getName().equals(dataFormat)) {
                 event.applyFormat(dataFormat);
             }
 
             if (qp != null) {
                 queryResultList.add(qp.evaluate(query, event));
             } else {
                 QueryResult queryRes = new QueryResult(event.getMessage().length);
                 for (int x = 0; x < queryRes.size(); x++) {
                     queryRes.setResult(x, true);
                 }
 
                 queryResultList.add(queryRes);
             }
 
             if (event.getMessage() != null && event.getMessage().length > 0) {
                 Event tmpEvent = new Event();
                 tmpEvent.setID(event.getID());
                 tmpEvent.setTimestamp(event.getTimestamp());
                 tmpEvent.setMessage(event.getMessage());
                 tmpEvent.setProducer(event.getProducer());
                 notificationEventList.add(tmpEvent);
             }
         }
 
         notificationEventList.trimToSize();
         CEMonitorConsumerStub.Event[] eventArray = new CEMonitorConsumerStub.Event[notificationEventList.size()];
         for (int k = 0; k < eventArray.length; k++) {
             Event srcEvn = notificationEventList.get(k);
             CEMonitorConsumerStub.Event dstEvn = new CEMonitorConsumerStub.Event();
             dstEvn.setID(srcEvn.getID());
             dstEvn.setMessage(srcEvn.getMessage());
             dstEvn.setProducer(srcEvn.getProducer());
             dstEvn.setTimestamp(srcEvn.getTimestamp());
             eventArray[k] = dstEvn;
         }
         notificationEventList.clear();
 
         notification.setEvent(eventArray);
 
         QueryResult[] queryResult = new QueryResult[notificationEventList.size()];
         queryResult = (QueryResult[]) queryResultList.toArray(queryResult);
 
         for (int i = 0; i < actions.length; i++) {
             try {
 
                 Action action = new Action();
                 action.setCreationTime(actions[i].getCreationTime());
                 action.setDoActionWhenQueryIs(actions[i].isDoActionWhenQueryIs());
                 action.setId(actions[i].getId());
                 action.setJarPath(actions[i].getJarPath());
                 action.setName(actions[i].getName());
                 action.setType(actions[i].getType());
                 action.setParameter(actions[i].getParameter());
                 action.setProperty(actions[i].getProperty());
                 action.addParameter(new Parameter("notification", notification));
                 action.addParameter(new Parameter("queryResult", queryResult));
                 action.addParameter(new Parameter("subscriptionId", subscription.getId()));
                 action.execute();
 
             } catch (Exception ex) {
                 logger.error(ex.getMessage(), ex);
             }
         }
     }
 
     /**
      * This method is used by a scheduled execution of this class.The operations
      * performed are:<br>
      * 1) For each registered subscription a control is made to verify if this
      * is expired. If this is the case the subscription is unregistered.<br>
      * 2) If the subscription is not expired or paused then the query is
      * processed on the event, then actions on notification are performed based
      * on the query processing results, and finally the consumer or purchaser
      * registered for that notification is notified. <br>
      */
     public void run() {
 
         while (!exit) {
 
             List<SubscriptionPersistent> subscriptionList = this.getSubscriptions();
 
             long tmpTS = System.currentTimeMillis();
 
             for (SubscriptionPersistent subscription : subscriptionList) {
 
                 URI consumerURI = null;
                 try {
                     java.net.URI tmpURI = subscription.getMonitorConsumerURL();
                     consumerURI = new URI(tmpURI.toString());
                 } catch (URI.MalformedURIException mEx) {
                     logger.error(mEx.getMessage(), mEx);
                 }
 
                 if (consumerURI == null) {
                     eraseSubscription(subscription, "consumer URL not found!");
                     continue;
                 }
 
                 if (subscription.isExpired()) {
                     eraseSubscription(subscription, "the subscription is expired!");
                     continue;
                 }
 
                 if (subscription.isPaused()) {
                     continue;
                 }
 
                 try {
                     logger.debug("[name=" + this.name + "] - processing subscription id " + subscription.getId());
 
                     Topic topic = subscription.getTopic();
 
                     Policy policy = subscription.getPolicy();
                     if (policy == null) {
                         continue;
                     }
                     SensorEventArrayList event = topicHolder.getEvents(topic.getName(), subscription.getSubscriberId(),
                             subscription.getSubscriberGroup());
 
                     logger.debug("[name=" + this.name + "] - found " + event.size() + " events");
 
                     CEMonitorConsumerStub.Notification notification = new CEMonitorConsumerStub.Notification();
                     notification.setExpirationTime(subscription.getExpirationTime());
                     CEMonitorConsumerStub.Topic newTopic = new CEMonitorConsumerStub.Topic();
                     newTopic.setName(topic.getName());
                     if (topic.getDialect() != null && topic.getDialect().length > 0) {
                         Dialect[] dialect = topic.getDialect();
                         CEMonitorConsumerStub.Dialect[] newDialect = new CEMonitorConsumerStub.Dialect[dialect.length];
 
                         for (int x = 0; x < dialect.length; x++) {
                             newDialect[x] = new CEMonitorConsumerStub.Dialect();
                             newDialect[x].setName(dialect[x].getName());
                             newDialect[x].setQueryLanguage(dialect[x].getQueryLanguage());
                         }
                         newTopic.setDialect(newDialect);
                     }
                     notification.setTopic(newTopic);
                     notification.setEvent(null);
                     notification.setConsumerURL(consumerURI);
 
                     while (event.size() > 0) {
 
                         SensorEventArrayList subEventList = event.drain(EVN_PAGE_SIZE);
 
                         processPolicy(subEventList, policy, notification, subscription);
 
                         if (notification.getEvent().length > 0) {
                             logger.info("[name=" + this.name + "] - sending notification (containing "
                                     + notification.getEvent().length + " events) to " + consumerURI.toString());
 
                             if (logger.isDebugEnabled()) {
                                 printNotification(subscription, subEventList);
                             }
 
                             Properties sslConfig = this.getSSLParameters(subscription.getCredentialFile(),
                                     subscription.getPassphrase());
 
                             CANLAXIS2SocketFactory.setCurrentProperties(sslConfig);
                             CEMonitorConsumerStub consumer = new CEMonitorConsumerStub(consumerURI.toString());
                             CEMonitorConsumerStub.Notify msg = new CEMonitorConsumerStub.Notify();
                             msg.setNotification(notification);
                             consumer.notify(msg);
 
                             logger.info("[name=" + this.name + "] - [done]");
                         } else {
                             logger.info("[name=" + this.name
                                     + "] - the notification doesn't contains messages to be notified! [aborted]");
                         }
 
                         if (subscription.getMaxRetryCount() != -1) {
                             subscription.resetRetryCount();
 
                             try {
 
                                 this.removeSubscription(subscription);
                                 this.subscriptionRegistry.update(subscription);
                                 this.addSubscription(subscription);
 
                             } catch (Throwable th) {
                                 logger.error(th.getMessage(), th);
                             }
                         }
 
                     }
 
                 } catch (NamingException e) {
                     logger.error("[name=" + this.name + "] - NamingException occurred: subscription id = "
                             + subscription.getId() + " consumer URL = " + subscription.getMonitorConsumerURL()
                             + " message error = " + e.getMessage());
                 } catch (IOException e) {
                     logger.error("[name=" + this.name + "] - IOException occurred: subscription id = "
                             + subscription.getId() + " consumer URL = " + subscription.getMonitorConsumerURL()
                             + " message error = " + e.getMessage());
 
                     if (subscription.getMaxRetryCount() != -1) {
                         int retries = subscription.decrRetryCount();
 
                         logger.info("[name=" + this.name + "] - decrementing the retry count to " + retries
                                 + " for the subscription id = " + subscription.getId() + "; consumer URL = "
                                 + subscription.getMonitorConsumerURL() + "; message error = " + e.getMessage());
 
                         if (retries == 0) {
 
                             eraseSubscription(subscription, "retry count = 0");
 
                         } else {
 
                             try {
                                 this.removeSubscription(subscription);
                                 this.subscriptionRegistry.update(subscription);
                                 this.addSubscription(subscription);
 
                             } catch (Throwable th) {
                                 logger.error(th.getMessage(), th);
                             }
                         }
                     }
                 } catch (Exception e) {
                     logger.error(
                             "[name=" + this.name + "] - Exception catched: subscriptionId = " + subscription.getId()
                                     + " consumer URL = " + subscription.getMonitorConsumerURL() + " message error = "
                                     + e.getMessage() + ". Follow the Stack trace:", e);
                 }
 
             }
 
             try {
                 long waitTime = rate - System.currentTimeMillis() + tmpTS;
 
                 if (waitTime > 0) {
                     logger.debug("[name=" + this.name + "] - sleeping " + waitTime);
                     Thread.sleep(waitTime);
                 } else {
                     logger.warn("Notifications overlapping");
                 }
             } catch (InterruptedException intEx) {
                 logger.info("[name=" + this.name + "] - destroyed");
             }
         }
 
     }
 
     private void eraseSubscription(SubscriptionPersistent subscription, String msg) {
         logger.info("[name=" + this.name + "] - unregistering subscription " + subscription.getId() + " [reason: "
                 + msg + "]");
         this.removeSubscription(subscription);
         try {
             this.subscriptionRegistry.unregister(subscription);
         } catch (Exception ex) {
             logger.error(ex.getMessage(), ex);
         }
         notificationPool.check();
     }
 
     private void printNotification(SubscriptionPersistent subscription, SensorEventArrayList eventList) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("\n******* NOTIFICATION *******");
         sb.append("\nsubscriptionId: " + subscription.getId());
         sb.append("\nuser: " + subscription.getSubscriberId());
         sb.append("\nconsumer url: " + subscription.getMonitorConsumerURL());
         if (subscription.getTopic() != null && subscription.getTopic().getName() != null) {
             sb.append("\topic name: " + subscription.getTopic().getName());
         }
 
         if (eventList == null) {
             sb.append("\nNONE EVENT FOUND!");
         } else {
             SensorEvent event = null;
 
             for (int i = 0; i < eventList.size(); i++) {
                 event = (SensorEvent) eventList.get(i);
 
                 sb.append("\n").append(i).append(") event_name: ").append(event.getName());
 
                 if (event.getTimestamp() != null) {
                     sb.append(" - timestamp: ").append(event.getTimestamp().getTime());
                 }
                 if (event.getExpirationTime() != null) {
                     sb.append(" - expirationTime: ").append(event.getExpirationTime().getTime());
                 }
 
                 sb.append(" - messages: ");
 
                 String[] msg = event.getMessage();
                 if (msg == null) {
                     sb.append("0");
                 } else {
                     sb.append(msg.length);
                 }
             }
         }
         sb.append("\n******* END NOTIFICATION *******\n");
         logger.debug(sb.toString());
     }
 
     private Properties getSSLParameters(String credFile, String passphrase) {
 
         Properties sslConfig = new Properties();
 
         CEMonServiceConfig sConfiguration = CEMonServiceConfig.getConfiguration();
         if (sConfiguration == null) {
             throw new RuntimeException("Service is not configured");
         }
 
         if (credFile != null) {
 
             sslConfig.put(CREDENTIALS_PROXY_FILE, credFile);
 
         } else {
 
             String tmps = sConfiguration.getGlobalAttributeAsString("gridproxyfile");
             if (tmps != "") {
 
                 sslConfig.put(CREDENTIALS_PROXY_FILE, tmps);
 
             } else {
 
                 String certFilename = sConfiguration.getGlobalAttributeAsString("sslcertfile");
                 String keyFilename = sConfiguration.getGlobalAttributeAsString("sslkeyfile");
                 String passwd = sConfiguration.getGlobalAttributeAsString("sslkeypasswd");
                 if (passwd == null)
                     passwd = "";
 
                 if (certFilename == "" || keyFilename == "") {
                     throw new RuntimeException("Missing user credentials");
                 } else {
                     sslConfig.put(CREDENTIALS_CERT_FILE, certFilename);
                     sslConfig.put(CREDENTIALS_KEY_FILE, keyFilename);
                 }
 
                 sslConfig.put(CREDENTIALS_KEY_PASSWD, passwd);
 
             }
         }
 
         if (passphrase != "") {
             sslConfig.put(CREDENTIALS_KEY_PASSWD, passphrase);
         }
 
         /*
          * TODO changes in configurations: removed protocol from subscriptions
          * (no SSLv3 available); replaced sslCAfiles with sslCALocation; removed
          * sslCRLfiles; add disableCRL and sslRefreshTime
          */
 
         String CALocation = sConfiguration.getGlobalAttributeAsString("sslCALocation");
         if (CALocation != "") {
             sslConfig.put(CA_LOCATION, CALocation);
         }
 
         String disableCRL = sConfiguration.getGlobalAttributeAsString("disableCRL");
         if (disableCRL.equalsIgnoreCase("true")) {
             sslConfig.put(CRL_MODE, "ignore");
         } else {
             sslConfig.put(CRL_MODE, "ifvalid");
         }
 
        String updateinterval = sConfiguration.getGlobalAttributeAsString("sslRefreshTime", "3600000");
        sslConfig.put(CACRL_REFRESH, updateinterval);
 
         return sslConfig;
     }
 
     public void destroy() {
         exit = true;
         notificationThread.interrupt();
     }
 }
