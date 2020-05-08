 // ========================================================================
 // Copyright 2010 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 package org.cipango.snmp;
 
 
 import java.lang.management.ManagementFactory;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.management.MBeanServerConnection;
 import javax.management.Notification;
 import javax.management.NotificationListener;
 import javax.management.ObjectName;
 
 import org.cipango.log.event.Events;
 import org.eclipse.jetty.util.log.Log;
 import org.snmp4j.agent.DuplicateRegistrationException;
 import org.snmp4j.agent.MOAccess;
 import org.snmp4j.agent.MOGroup;
 import org.snmp4j.agent.MOServer;
 import org.snmp4j.agent.NotificationOriginator;
 import org.snmp4j.agent.mo.MOAccessImpl;
 import org.snmp4j.agent.mo.MOFactory;
 import org.snmp4j.agent.mo.MOScalar;
 import org.snmp4j.agent.mo.jmx.JMXDefaultMOFactory;
 import org.snmp4j.agent.mo.jmx.MBeanAttributeMOScalarSupport;
 import org.snmp4j.mp.SnmpConstants;
 import org.snmp4j.smi.Counter32;
 import org.snmp4j.smi.OID;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.VariableBinding;
 
 public class CipangoMib implements Mib, NotificationListener
 {
 
 	public static final OID 
 		OID_MESSAGES_RECEIVED = new OID("1.3.6.1.4.26588.1.10.1.0"),
 		OID_MESSAGES_SENT = new OID("1.3.6.1.4.26588.1.10.2.0"),
 		OID_SIP_VERSION = new OID("1.3.6.1.4.26588.1.10.3.0"),
 		OID_NB_SESSIONS = new OID("1.3.6.1.4.26588.1.100.1.0"),
 		OID_THRESHOLD_SESSIONS = new OID("1.3.6.1.4.26588.1.100.2.0");
 		  
 
 	
 	public static final ObjectName 
		CONNECTOR_MANAGER = ObjectNameFactory.create("org.cipango.sip:type=connectormanager,id=0"),
		SERVER = ObjectNameFactory.create("org.cipango:type=server,id=0"),
 		JMX_EVENT_LOGGER = ObjectNameFactory.create("org.cipango.log:type=jmxeventlogger,id=0");
 	
 	private static final Object[][] CONNECTOR_MANAGER_ATTR =
 	{
 		{ OID_MESSAGES_RECEIVED, "messagesReceived", Long.class },
 		{ OID_MESSAGES_SENT, "messagesSent", Long.class }
 	};
 	
 	private static final Object[][] SERVER_ATTR =
 	{
 		{ OID_SIP_VERSION, "sipVersion", String.class },
 	};
 
 	private List<MOScalar> _scalars = new ArrayList<MOScalar>();
 	private SnmpAgent _agent;
 
 	public CipangoMib()
 	{
 		super();
 		addJvmManagementMibInstrumentaton();
 	}
 
 	protected void createMO(MOFactory moFactory)
 	{
 		MOAccess readOnly = moFactory.createAccess(MOAccessImpl.ACCESSIBLE_FOR_READ_ONLY);
 		_scalars.add(moFactory.createScalar(OID_MESSAGES_RECEIVED, readOnly, new Counter32()));
 		_scalars.add(moFactory.createScalar(OID_MESSAGES_SENT, readOnly, new Counter32()));
 		_scalars.add(moFactory.createScalar(OID_SIP_VERSION, readOnly, new OctetString()));
 	}
 	
 	
 	
 	private void addJvmManagementMibInstrumentaton()
 	{
 		try
 		{
 			MBeanServerConnection server = ManagementFactory.getPlatformMBeanServer();
 			MBeanAttributeMOScalarSupport scalarSupport = new MBeanAttributeMOScalarSupport(server);
 			JMXDefaultMOFactory jmxFactory = new JMXDefaultMOFactory(server, scalarSupport);
 
 			createMO(jmxFactory);
 			
 			scalarSupport.addAll(CONNECTOR_MANAGER, CONNECTOR_MANAGER_ATTR);
 			scalarSupport.addAll(SERVER, SERVER_ATTR);
 					
 			server.addNotificationListener(JMX_EVENT_LOGGER, this, null, JMX_EVENT_LOGGER);
 					
 		}
 		catch (Exception e)
 		{
 			Log.warn(e);
 		}
 	}
 
 
 	public void registerMOs(MOServer server, OctetString context) throws DuplicateRegistrationException
 	{
 		Iterator<MOScalar> it = _scalars.iterator();
 		while (it.hasNext())
 			server.register(it.next(), context);
 	}
 
 	public void unregisterMOs(MOServer server, OctetString context)
 	{
 		Iterator<MOScalar> it = _scalars.iterator();
 		while (it.hasNext())
 			server.unregister(it.next(), context);
 	}
 
 	public void handleNotification(Notification notification, Object handback)
 	{
 		if (JMX_EVENT_LOGGER.equals(handback))
 		{
 			int event = Integer.parseInt(notification.getType());
 			switch (event)
 			{
 			case Events.START:
 				 _agent.getNotificationOriginator().notify(new OctetString(), SnmpConstants.coldStart,
 	                     new VariableBinding[0]);
 				break;
 			case Events.DEPLOY_FAIL:
 	
 				break;
 			case Events.CALLS_THRESHOLD_READCHED:
 				 _agent.getNotificationOriginator().notify(new OctetString(), OID_THRESHOLD_SESSIONS,
 	                      new VariableBinding[0]);
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	public void setSnmpAgent(SnmpAgent agent)
 	{
 		_agent = agent;
 	}
 
 }
