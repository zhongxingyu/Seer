 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cn.orz.pascal.gfmonitor.model.monitor;
 
 import cn.orz.pascal.gfmonitor.models.entity.MonitorAverageLog;
 import cn.orz.pascal.gfmonitor.models.entity.MonitorLog;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Date;
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 /**
  *
  * @author koduki
  */
 abstract public class GlassFishMonitor {
 
     protected MBeanServerConnection mbeanserver;
 
     public void init() throws MalformedURLException, IOException {
        String url = "service:jmx:rmi://192.168.11.5:8686/jndi/rmi://192.168.11.5:8686/jmxrmi";
         JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(url));
 
         // MBeanServerConnection取得
         this.mbeanserver = connector.getMBeanServerConnection();
     }
 
     protected <T extends MonitorLog> T build(T monitorLog, CompositeDataSupport data) {
         monitorLog.setName((String) data.get("name"));
         monitorLog.setUnit((String) data.get("unit"));
         monitorLog.setDescription((String) data.get("description"));
         monitorLog.setCount((Long) data.get("count"));
         monitorLog.setStartTime(new Date((Long) data.get("startTime")));
         monitorLog.setLastSampleTime(new Date((Long) data.get("lastSampleTime")));
         return monitorLog;
     }
 
     protected <T extends MonitorAverageLog> T build(T monitorLog, CompositeDataSupport data) {
         monitorLog.setName((String) data.get("name"));
         monitorLog.setUnit((String) data.get("unit"));
         monitorLog.setDescription((String) data.get("description"));
         monitorLog.setCurrentValue((Long) data.get("current"));
         monitorLog.setHighWaterMark((Long) data.get("highWaterMark"));
         monitorLog.setLowWaterMark((Long) data.get("lowWaterMark"));
         monitorLog.setStartTime(new Date((Long) data.get("startTime")));
         monitorLog.setLastSampleTime(new Date((Long) data.get("lastSampleTime")));
         return monitorLog;
     }
 
     protected CompositeDataSupport getProperty(MBeanServerConnection mbeanserver, String name, String key) throws IOException, InstanceNotFoundException, MBeanException, ReflectionException, NullPointerException, MalformedObjectNameException, AttributeNotFoundException {
         ObjectName objName = new ObjectName(name);
         CompositeDataSupport activeservletsloadedcount = (CompositeDataSupport) mbeanserver.getAttribute(objName, key);
         return activeservletsloadedcount;
     }
 }
