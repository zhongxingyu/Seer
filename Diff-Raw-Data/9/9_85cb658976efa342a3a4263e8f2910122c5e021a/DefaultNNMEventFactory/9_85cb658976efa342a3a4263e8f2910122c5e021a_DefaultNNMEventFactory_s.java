 package org.opennms.opennmsd;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.opennms.nnm.swig.OVsnmpPdu;
 import org.opennms.nnm.swig.OVsnmpVarBind;
 import org.opennms.ovapi.OVsnmpPduUtils;
 
 public class DefaultNNMEventFactory implements NNMEventFactory {
     
     private static Logger log = Logger.getLogger(DefaultNNMEventFactory.class);
 
     
     private EventConfiguration m_eventConfiguration;
 
 
     protected static long s_startTime = System.currentTimeMillis();
     public void setEventConfiguation(EventConfiguration eventConfiguration) {
         m_eventConfiguration = eventConfiguration;
     }
 
     public NNMEvent createEvent(OVsnmpPdu trap) {
         
         String enterpriseId = trap.getEnterpriseObjectId();
         int generic = trap.getGenericType();
         int specific = trap.getSpecificType();
         
         String key = NNMEvent.getEventConfigurationKey(enterpriseId, generic, specific);
         if (m_eventConfiguration.getName(key) == null) {
             log.info("Could not find trap with id "+key+" in trapd.conf");
             return null;
         }
         
         
         NNMEvent e = new NNMEvent();
         e.setSourceAddress(trap.getAgentAddress());
         e.setTimeStamp(new Date(DefaultNNMEventFactory.s_startTime+trap.getTime()*10));
         e.setCommunity(trap.getCommunity());
         e.setEnterpriseId(enterpriseId);
         e.setGeneric(generic);
         e.setSpecific(specific);
         e.setSnmpHost(trap.getIpAddress());
         e.setVersion(1);
         
         e.setName(m_eventConfiguration.getName(key));
         e.setCategory(m_eventConfiguration.getCategory(key));
         e.setSeverity(m_eventConfiguration.getSeverity(key));
 
         OVsnmpVarBind varBind = trap.getVarBinds();
         
         String nodeLabel = null;
         while(varBind != null) {
             String objectId = varBind.getObjectId();
             String varbindValue = OVsnmpPduUtils.getVarbindValue(varBind);
             if (".1.3.6.1.4.1.11.2.17.2.2.0".equals(objectId)) {
                 /* then this is possibly the hostname of ip address sent in an internal
                  * node manager event.  if it can resolve to ip address and host name the
                  * use them in 'nodelabel' parm in interface rather than agent address
                  */
                 try {
                     InetAddress addr = InetAddress.getByName(varbindValue);
                     String iface = addr.getHostAddress();
                     nodeLabel = addr.getHostName();
                     e.setSourceAddress(iface);
                     
                 } catch (UnknownHostException ex) {
                     // this is the normal case so do nothing
                     log.info("Unable to resolve "+varbindValue+" as hostname/ipAddress for event "+e.getName()+" using trap supplied values");
                 }
             }
             e.addVarBind(objectId, 
                     OVsnmpPduUtils.getTypeString(varBind.getType()),
                     varbindValue);
      
             varBind = varBind.getNextVarBind();
         }
         
         if (nodeLabel != null) {
             e.addVarBind("nodelabel", "OctetString", nodeLabel);
         }
         
         return e;
     }
 
 }
