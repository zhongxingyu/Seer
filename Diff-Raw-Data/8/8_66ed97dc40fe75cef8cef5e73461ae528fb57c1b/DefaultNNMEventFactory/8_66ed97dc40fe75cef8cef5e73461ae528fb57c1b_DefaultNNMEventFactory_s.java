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
         
         // add trap information to event
         NNMEvent e = new NNMEvent(trap.getEnterpriseObjectId(), trap.getGenericType(), trap.getSpecificType());
         
         e.setAgentAddress(trap.getAgentAddress());
         e.setSnmpHost(trap.getIpAddress());
        e.setTimeStamp(new Date(DefaultNNMEventFactory.s_startTime+trap.getTime()*10));
         e.setCommunity(trap.getCommunity());
         e.setVersion(1);
         
         for(OVsnmpVarBind varBind = trap.getVarBinds(); varBind != null; varBind = varBind.getNextVarBind()) 
         {
             e.addVarBind(varBind.getObjectId(), 
                     OVsnmpPduUtils.getTypeString(varBind.getType()),
                     OVsnmpPduUtils.getVarbindValue(varBind));
      
             
         }
 
         // fix up event fields for nnm-internal events
         String newAddr = e.getVarBindValue(".1.3.6.1.4.1.11.2.17.2.2.0");
         if (newAddr != null) {
             try {
                 InetAddress addr = InetAddress.getByName(newAddr);
                 String iface = addr.getHostAddress();
                 String nodeLabel = addr.getHostName();
                 e.setAgentAddress(iface);
                 e.setNodeLabel(nodeLabel);
                 
             } catch (UnknownHostException ex) {
                 // this is the normal case so do nothing
                 log.info("Unable to resolve "+newAddr+" as hostname/ipAddress for event "+e+" using trap supplied values");
             }
         }
 
         // add formatting information to event
         EventFormat format = m_eventConfiguration.getFormat(e);
         format.apply(e);
 
         return e;
     }
 
 
 }
