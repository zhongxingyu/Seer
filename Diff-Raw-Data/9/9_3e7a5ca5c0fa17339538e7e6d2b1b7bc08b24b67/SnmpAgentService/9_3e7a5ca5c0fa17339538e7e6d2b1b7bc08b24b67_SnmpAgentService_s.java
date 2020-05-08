 /*
  * Copyright (c) 2003,  Intracom S.A. - www.intracom.com
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * This package and its source code is available at www.jboss.org
  */
 package org.jboss.jmx.adaptor.snmp.agent;
 
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.management.Notification;
 import javax.management.ObjectName;
 
 import org.jboss.jmx.adaptor.snmp.config.attribute.ManagedBean;
 import org.jboss.jmx.adaptor.snmp.config.notification.Mapping;
 import org.jboss.jmx.adaptor.snmp.config.user.User;
 import org.jboss.system.ListenerServiceMBeanSupport;
 import org.jboss.xb.binding.MappingObjectModelFactory;
 import org.jboss.xb.binding.Unmarshaller;
 import org.jboss.xb.binding.UnmarshallerFactory;
 import org.snmp4j.MessageDispatcherImpl;
 import org.snmp4j.Snmp;
 import org.snmp4j.TransportMapping;
 import org.snmp4j.mp.MPv1;
 import org.snmp4j.mp.MPv2c;
 import org.snmp4j.mp.MPv3;
 import org.snmp4j.security.Priv3DES;
 import org.snmp4j.security.SecurityModels;
 import org.snmp4j.security.SecurityProtocols;
 import org.snmp4j.security.USM;
 import org.snmp4j.security.UsmUser;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.UdpAddress;
 import org.snmp4j.transport.DefaultUdpTransportMapping;
 
 
 /**
  * <tt>SnmpAgentService</tt> is an MBean class implementing an SNMP agent.
  *
  * It allows to send V1 or V2 traps to one or more SNMP managers defined
  * by their IP address, listening port number and expected SNMP version.
  * 
  * It support mapping SNMP get/set requests JMX mbean attribute get/sets.
  *
  * @jmx:mbean
  *    extends="org.jboss.system.ListenerServiceMBean"
  *
  * @author  <a href="mailto:spol@intracom.gr">Spyros Pollatos</a>
  * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
  * @author  <a href="mailto:krishnaraj@ieee.org">Krishnaraj S</a>
  * @version $Revision: 110838 $
  */
 public class SnmpAgentService extends ListenerServiceMBeanSupport
    implements SnmpAgentServiceMBean
 {
    /** Supported versions */
    public static final int SNMPV1 = 1;
    public static final int SNMPV2 = 2;
    public static final int SNMPV3 = 3;
     
    /** Default communities */
    public static final String DEFAULT_READ_COMMUNITY  = "public";
    public static final String DEFAULT_WRITE_COMMUNITY = "private";
    
    // Private data --------------------------------------------------
    
    /** Time keeping*/
    private Clock clock = null;
    
    /** Trap counter */
    private Counter trapCounter = null;
 
    /** Name of the file containing SNMP users */
    private String usersResName = null;
    
    /** Name of the file containing SNMP manager specifications */
    private String managersResName = null;  
 
    /** Name of resource file containing notification to trap mappings */
    private String notificationMapResName = null;   
    
    /** Name of resource file containing get/set mappings */
    private String requestHandlerResName = null;
    
    /** Name of the trap factory class to be utilised */
    private String trapFactoryClassName = null;
 
    /** Name of request handler implementation class */
    private String requestHandlerClassName = null;
 
    /** The SNMP read community to use */
    private String readCommunity = DEFAULT_READ_COMMUNITY;
    
    /** The SNMP write community to use */
    private String writeCommunity = DEFAULT_WRITE_COMMUNITY;
    
    /** Controls the request processing thread pool */
    private int numberOfThreads = 1;
    
    /** Agent listening port */
    private int port = 1161;
    
    /** Agent SNMP protocol version */
    private int snmpVersion = SNMPV1;
    
    /** The interface to bind, useful for multi-homed hosts */
    private InetAddress bindAddress;
    
    /** Name of the utilised timer MBean */
    private ObjectName timerName = null;
      
    /** Heartbeat emission period (in seconds) and switch */
    private int heartBeatPeriod = 60;
    
    /** Dynamic subscriptions flag */
    private boolean dynamicSubscriptions = true;   
 
    /** Reference to heartbeat emission controller */
    private Heartbeat heartbeat = null;
 
    /** The trap emitting subsystem*/
    private TrapEmitter trapEmitter = null;
    
    /** the request handler instance handling get/set requests */
    private RequestHandler requestHandler;
    
    /** the location of the agent's settings**/
    private String configFileResName = null;
    
    /** the location of the boot counter persistent store **/
    private String bootCounterResName = null;
    
    private Snmp session;
    
    private TransportMapping[] transportMappings;
    
    private SnmpRequest responder;
   
    private USM usm;
    
    private MPv3 mpv3;
    
    private MessageDispatcherImpl dispatcher;
    
    private Map<String, User> userMap = new HashMap<String, User>();
    
    // Constructors --------------------------------------------------
    
    /**
     * Default CTOR
     */
    public SnmpAgentService()
    {
       // empty
    }
    
    // Attributes ----------------------------------------------------
    
    /**
     * Gets the heartbeat switch
     *
     * @jmx:managed-attribute
     */ 
    public int getHeartBeatPeriod()
    {
       return this.heartBeatPeriod;
    }
    
    /**
     * Sets the heartbeat period (in seconds) switch
     *
     * @jmx:managed-attribute
     */     
    public void setHeartBeatPeriod(int heartBeatPeriod)
    {
       this.heartBeatPeriod = heartBeatPeriod;
    }
    
    /**
     * Returns the difference, measured in milliseconds, between the 
     * instantiation time and midnight, January 1, 1970 UTC.
     *
     * @jmx:managed-attribute
     */        
    public long getInstantiationTime()
    {
       return this.clock.instantiationTime();
    }
     
    /**
     * Returns the up-time
     *
     * @jmx:managed-attribute
     */ 
    public long getUptime()
    {
       return this.clock.uptime();
    }
 
    /**
     * Returns the current trap counter reading
     *
     * @jmx:managed-attribute
     */    
    public long getTrapCount()
    {
       return this.trapCounter.peek();
    }
      
    /**
     * Sets the name of the file containing SNMP manager specifications
     *
     * @jmx:managed-attribute
     */ 
    public void setManagersResName(String managersResName)
    {
       this.managersResName = managersResName;
    }
 
    /**
     * Gets the name of the file containing SNMP manager specifications
     *
     * @jmx:managed-attribute
     */    
    public String getManagersResName()
    {
       return this.managersResName;
    }
 
    /**
     * Sets the name of the file containing the notification/trap mappings
     *
     * @jmx:managed-attribute
     */ 
    public void setNotificationMapResName(String notificationMapResName)
    {
       this.notificationMapResName = notificationMapResName;
    }    
     
    /**
     * Gets the name of the file containing the notification/trap mappings
     *
     * @jmx:managed-attribute
     */ 
    public String getNotificationMapResName()
    {
       return this.notificationMapResName;
    }   
 
    /**
     * Sets the utilised trap factory name
     *
     * @jmx:managed-attribute
     */
    public void setTrapFactoryClassName(String name)
    {
       this.trapFactoryClassName = name;        
    }
     
    /**
     * Gets the utilised trap factory name
     *
     * @jmx:managed-attribute
     */
    public String getTrapFactoryClassName()
    {
       return this.trapFactoryClassName;
    }
     
    /**
     * Sets the utilised timer MBean name
     *
     * @jmx:managed-attribute
     */          
    public void setTimerName(ObjectName timerName)
    {
       this.timerName = timerName;
    }
     
    /**
     * Gets the utilised timer MBean name
     *
     * @jmx:managed-attribute
     */      
    public ObjectName getTimerName()
    {
       return this.timerName;
    }    
 
    /**
     * Sets the agent bind address
     * 
     * @jmx:managed-attribute
     */
    public void setBindAddress(String bindAddress)
       throws UnknownHostException
    {
       this.bindAddress = toInetAddress(bindAddress);
    }
    
    /**
     * Gets the agent bind address
     * 
     * @jmx:managed-attribute
     */
    public String getBindAddress()
    {
       String address = null;
       
       if (this.bindAddress != null)
          address = this.bindAddress.getHostAddress();
       
       return address;
    }
    
    /**
     * Sets the number of threads in the agent request processing thread pool
     * 
     * @jmx:managed-attribute
     */
    public void setNumberOfThreads(int numberOfThreads)
    {
       if (numberOfThreads > 0 && numberOfThreads <= 12)
       {
          this.numberOfThreads = numberOfThreads;
       }
    }
    
    /**
     * Gets the number of threads in the agent requests processing thread pool
     * 
     * @jmx:managed-attribute
     */
    public int getNumberOfThreads()
    {
       return numberOfThreads;
    }
 
    /**
     * Sets the agent listening port number
     *
     * @jmx:managed-attribute
     */
    public void setPort(int port)
    {
       if (port >= 0)
       {
          this.port = port;
       }
    }
    
    /**
     * Gets the agent listening port number
     * 
     * @jmx:managed-attribute
     */
    public int getPort()
    {
       return port;
    }
 
    /**
     * Sets the snmp protocol version
     * 
     * @jmx:managed-attribute
     */
    public void setSnmpVersion(int snmpVersion)
    {
       switch (snmpVersion)
       {
          case SNMPV2:
             this.snmpVersion = SNMPV2;
             break;
             
          default:
             this.snmpVersion = SNMPV1;
             break;
       }
    }
    
    /**
     * Gets the snmp protocol version
     * 
     * @jmx:managed-attribute
     */
    public int getSnmpVersion()
    {
       return snmpVersion;
    }
    
    /**
     * Sets the read community (no getter)
     * 
     * @jmx:managed-attribute
     */
    public void setReadCommunity(String readCommunity)
    {
       if (readCommunity != null && readCommunity.length() > 0)
       {
          this.readCommunity = readCommunity;
       }
    }
 
    /**
     * Sets the write community (no getter)
     * @jmx:managed-attribute
     */
    public void setWriteCommunity(String writeCommunity)
    {
       if (writeCommunity != null && writeCommunity.length() > 0)
       {
          this.writeCommunity = writeCommunity;
       }
    }
    
    /**
     * Sets the RequestHandler implementation class
     * 
     * @jmx:managed-attribute
     */
    public void setRequestHandlerClassName(String requestHandlerClassName)
    {
       this.requestHandlerClassName = requestHandlerClassName;
    }
    
    /**
     * Gets the RequestHandler implementation class
     * 
     * @jmx:managed-attribute
     */
    public String getRequestHandlerClassName()
    {
       return requestHandlerClassName;
    }
 
    /**
     * Sets the resource file name containing get/set mappings
     * 
     * @jmx:managed-attribute
     */
    public void setRequestHandlerResName(String requestHandlerResName)
    {
       this.requestHandlerResName = requestHandlerResName;
    }
    
    /**
     * Gets the resource file name containing get/set mappings
     * 
     * @jmx:managed-attribute
     */
    public String getRequestHandlerResName()
    {
       return requestHandlerResName;
    }
    
    /**
     * Enables/disables dynamic subscriptions
     *
     * @jmx:managed-attribute
     */
    public void setDynamicSubscriptions(boolean dynamicSubscriptions)
    {
       this.dynamicSubscriptions = dynamicSubscriptions;
    }
 
    /**
     * Gets the dynamic subscriptions status
     *
     * @jmx:managed-attribute
     */
    public boolean getDynamicSubscriptions()
    {
       return this.dynamicSubscriptions;
    }
    
    // Operations ----------------------------------------------------
    
    /**
     * Reconfigures the RequestHandler, reponsible for handling get requests etc.
     * 
     * @jmx:managed-operation
     */
    public void reconfigureRequestHandler() throws Exception
    {
       if (requestHandler instanceof Reconfigurable)
          ((Reconfigurable)requestHandler).reconfigure(getRequestHandlerResName());
       else
          throw new UnsupportedOperationException("Request handler is not Reconfigurable");
    }
    
    // Lifecycle operations ------------------------------------------
    
    /**
     * Perform service start-up
     */
    protected void startService()
       throws Exception
    {
 
 	  // initialise the snmp agent
       log.debug("Starting snmp agent ...");
       
       startAgent();
       
       log.info("SNMP agent going active");
       
       // initialize clock and trapCounter
       this.clock = new Clock();
       this.trapCounter = new Counter(0);
       
       // Notification subscription are handled by
       // ListenerServiceMBeanSupport baseclass
       
       log.debug("Instantiating trap emitter ...");
       this.trapEmitter = new TrapEmitter(this);
     
       // Start trap emitter
       log.debug("Starting trap emitter ...");        
       this.trapEmitter.start();
 
       // Get the heartbeat going 
       this.heartbeat = new Heartbeat(this.getServer(),
                                      this.getTimerName(),
                                      this.getHeartBeatPeriod());
                                      
       log.debug("Starting heartbeat controller ...");
       heartbeat.start();
 
       // subscribe for notifications, with the option for dynamic subscriptions
       super.subscribe(this.dynamicSubscriptions);     
         
       // Send the cold start!
       this.sendNotification(new Notification(EventTypes.COLDSTART, this,
                                              getNextNotificationSequenceNumber()));
    }
     
    /**
     * Perform service shutdown
     */
    protected void stopService()
       throws Exception
    {
       // unsubscribe for notifications
       super.unsubscribe();
       
       log.debug("Stopping heartbeat controller ...");
       this.heartbeat.stop();
       this.heartbeat = null; // gc
 
       log.debug("Stopping trap emitter ...");
       this.trapEmitter.stop();
       this.trapEmitter = null;
       
       log.debug("Stopping snmp agent ...");
 //      this.agentSession.close();
 //      this.agentSession = null;
       session.close();
       log.info("SNMP agent stopped");
    }    
    
    // Notification handling -----------------------------------------
    
    /**
     * All notifications are intercepted here and are routed for emission.
     */
    public void handleNotification2(Notification n, Object handback)
    {
       if (log.isDebugEnabled()) {
          log.debug("Received notification: <" + n + "> Payload " +
                    "TS: <" + n.getTimeStamp() + "> " +
                    "SN: <" + n.getSequenceNumber() + "> " +
                    "T:  <" + n.getType() + ">");
       }
       
       try {
          this.trapEmitter.send(n);           
       }
       catch (Exception e) {
          log.error("Sending trap", e);
       }    
    }
    
    // Private -------------------------------------------------------
    
    private void initResponder(){
 	   OctetString localEngineID=new OctetString(MPv3.createLocalEngineID());
 	   this.responder = new SnmpRequest(this.requestHandler, log, localEngineID);
    }
    // add updateEngineBoots to USM constuctor call
    private void initMessageDispatcher(){
 	   this.dispatcher = new MessageDispatcherImpl();	   
 	   usm = new USM(SecurityProtocols.getInstance(),
 			   		 responder.getLocalEngineID(),
 			   		 0);
 	   mpv3 = new MPv3(usm);
 	   SecurityModels.getInstance().addSecurityModel(usm);
 	   SecurityProtocols.getInstance().addDefaultProtocols();
 	   // all other privacy and authentication protocols are provided by the above method
 	   SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());	   
 	   dispatcher.addMessageProcessingModel(new MPv1());
 	   dispatcher.addMessageProcessingModel(new MPv2c());
 	   dispatcher.addMessageProcessingModel(mpv3);
    }
    
    private void initRequestHandler()throws Exception{
 	   this.requestHandler = (RequestHandler)Class.forName(this.requestHandlerClassName,true,this.getClass().getClassLoader()).newInstance();
 	   this.requestHandler.initialize(this.requestHandlerResName, 
 			   				     this.getServer(), 
 			   				     this.log, 
 			   				     this.clock); 
    }
    
    private void initTransportMapping(){
 	   this.transportMappings = new TransportMapping[1];
 	   try{
 		   this.transportMappings[0] = new DefaultUdpTransportMapping(new UdpAddress(this.bindAddress, this.port));
 	   }catch(Exception e){
 		   log.debug("Exception in initialization of Tranport Map!!");
 	   }
    }
    
    private void initSession(){
 	   this.session = new Snmp(dispatcher);
 	   for (int i = 0; i < transportMappings.length;i++){
 		   try{
 			   session.addTransportMapping(transportMappings[i]);
 		   }
 		   catch(Exception e){
 			   log.warn("Tranport mapping invalid. Failed to initialize mapping '"+
 					    transportMappings[i]+"' with: "+e.getMessage());
 			   
 		   }
 	   }
 	   this.session.addCommandResponder(responder);
    }
    
    /*
     * Read the users from the users.xml and add them to the usm
     */
    private void addUsmUsers() throws Exception {
 	   MappingObjectModelFactory momf = new MappingObjectModelFactory();
 	   momf.mapElementToClass("user-list", ArrayList.class);
 	   momf.mapElementToClass("user", User.class);
 	      	   
 	   List<User> userList = null;
 	   InputStream is = null;
 	   try {
 		   // locate managers.xml
 		   final String resName = this.usersResName;
 		   is = SecurityActions.getThreadContextClassLoaderResource(resName);
          
 		   // create unmarshaller
 		   Unmarshaller unmarshaller = UnmarshallerFactory.newInstance()
                .newUnmarshaller();
          
 		   // let JBossXB do it's magic using the MappingObjectModelFactory
 		   userList = (ArrayList<User>) unmarshaller.unmarshal(is, momf, null);         
       } catch (Exception e) {
     	  log.error("Accessing resource '" + usersResName + "'");
     	  throw e;
       } finally {
     	  if (is != null) {
             // close the XML stream
             is.close();            
     	  }
       }
 	  
       for (Iterator<User> userIt = userList.iterator(); userIt.hasNext(); ) {
     	  User user = userIt.next();
         	 
     	  UsmUser usmUser = new UsmUser(new OctetString(user.getSecurityName()),
                user.getAuthenticationProtocolID(),
                new OctetString(user.getAuthenticationPassphrase()),
                user.getPrivacyProtocolID(),
                new OctetString(user.getPrivacyPassphrase()));
     	  this.usm.addUser(usmUser.getSecurityName(), usm.getLocalEngineID(),usmUser);
     	  this.userMap.put(user.getSecurityName(), user);
       }
    }
 
    // the new Snmp(TransportMapping) constructor already initializes a MessageDispatcherImpl with all MP's associated. 
    /**
     * Start the embedded agent 
     */
    private void startAgent()
       throws Exception
    {
 	  initRequestHandler();
 	  initResponder();
 	  initMessageDispatcher();	   
 	  initTransportMapping();
       initSession();
       addUsmUsers();
      
       session.listen();
    }
    
    /**
     * Safely convert a host string to InetAddress or null
     */
    private InetAddress toInetAddress(String host)
       throws UnknownHostException
    {
       if (host == null || host.length() == 0)
          return null;
       else
          return InetAddress.getByName(host);
    }
 
 	/**
 	 * @param usersResName the usersResName to set
 	 */
 	public void setUsersResName(String usersResName) {
 		this.usersResName = usersResName;
 	}
 	
 	/**
 	 * @return the usersResName
 	 */
 	public String getUsersResName() {
 		return usersResName;
 	}
 
 	/**
 	 * @return the clock
 	 */
 	public Clock getClock() {
 		return clock;
 	}
 	
 	/**
 	 * @return the trapCounter
 	 */
 	public Counter getTrapCounter() {
 		return trapCounter;
 	}
 
 	/**
 	 * @return the userList
 	 */
 	public Map<String, User> getUserMap() {
 		return userMap;
 	}
 	
 	/**
 	 * @return the session
 	 */
 	public Snmp getSession() {
 		return session;
 	}
 
 	/**
 	 * @return the responder
 	 */
 	public SnmpRequest getResponder() {
 		return responder;
 	}
 
 	/**
 	 * @return the requestHandler
 	 */
 	public RequestHandler getRequestHandler() {
 		return requestHandler;
 	}
 
 	/**
 	 * @return the transportMappings
 	 */
 	public TransportMapping[] getTransportMappings() {
 		return transportMappings;
 	}
 
 	@Override
 	public void sendJMXNotification(Notification notification) {
 		if(notification.getSequenceNumber() <= 0) {
 			notification.setSequenceNumber(getNextNotificationSequenceNumber());
 		}
 		try {
 			this.trapEmitter.send(notification);           
 	    } catch (Exception e) {
 	         log.error("Problem occured while Sending trap", e);
 	    }   		
 	}
 	
 	@Override
 	public long getNextJMXNotificationSequenceNumber() {		
 		return getNextNotificationSequenceNumber();
 	}
 
 	@Override
 	public void addAttributeMappings(List<ManagedBean> mappings) {
		new Exception("TRALALA").printStackTrace();
 		requestHandler.addAttributeMappings(mappings);
 	}
 
 	@Override
 	public void removeAttributeMappings(List<ManagedBean> mappings) {
 		requestHandler.removeAttributeMappings(mappings);
 	}
 
 	@Override
 	public void addNotifications(List<Mapping> notifications) {
		new Exception("TRALALA").printStackTrace();
 		trapEmitter.addNotifications(notifications);
 	}
 
 	@Override
 	public void removeNotifications(List<Mapping> notifications) {
 		trapEmitter.removeNotifications(notifications);		
 	}
 
 }
