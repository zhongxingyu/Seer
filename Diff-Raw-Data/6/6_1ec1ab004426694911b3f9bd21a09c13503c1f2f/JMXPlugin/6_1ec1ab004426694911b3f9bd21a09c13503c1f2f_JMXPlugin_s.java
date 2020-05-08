 package com.minecarts.verrier.jmxplugin;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import javax.management.MBeanServer;
 import javax.management.remote.*;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.rmi.registry.LocateRegistry;
 import java.util.Map;
 
 
 //References
 //  http://mx4j.sourceforge.net/docs/ch03s04.html
 //  http://download.oracle.com/javase/6/docs/technotes/guides/management/faq.html
 //  http://pub.admc.com/howtos/jmx/jmx.html
 
 public class JMXPlugin extends JavaPlugin {
     private JMXConnectorServer cs;
     public void onEnable(){
         //Create JMX server
         try{
            org.bukkit.util.config.Configuration config = getConfiguration();
 
             Integer port = config.getInt("port",8888);
             String hostname = config.getString("hostname",InetAddress.getLocalHost().getHostName());
 
             try{
                 LocateRegistry.createRegistry(port); //http://lists.xcf.berkeley.edu/lists/advanced-java/2000-July/030432.html
             } catch (Exception e){
                 //You can only create the registry once per VM
             }
 
             MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
             JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://"+hostname+":"+port+"/jndi/rmi://"+hostname+":"+port+"/jmxrmi");
             Map env = null;
             cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
             cs.start();
             System.out.println("JMXPlugin> JMX server started at: " + url.getURLPath());
         } catch (MalformedURLException e){
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace(); 
         }
     }
 
     public void onDisable(){
         //Close JMX server
         try{
             cs.stop();
             System.out.println("JMXPlugin> JMX server stopped");
         } catch(IOException e){
             e.printStackTrace();
         }
     }
 }
