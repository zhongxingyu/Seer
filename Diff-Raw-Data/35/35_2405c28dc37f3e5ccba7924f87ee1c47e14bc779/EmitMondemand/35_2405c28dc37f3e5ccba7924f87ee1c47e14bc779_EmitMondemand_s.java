 package com.inmobi.stats.emitter;
 
 import java.net.InetAddress;
 import org.mondemand.Client;
 import org.mondemand.Transport;
 import org.mondemand.transport.LWESTransport;
 import com.inmobi.stats.StatsExposer;
 import com.inmobi.stats.StatsEmitter;
 import com.inmobi.stats.StatsEmitterBase;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.io.FileInputStream;
 
 public class EmitMondemand extends StatsEmitterBase implements Runnable {
     private Thread t = null;
     private Client client = null;
     private Transport transport = null;
     private volatile boolean should_run = true;
     private Properties props = new Properties();
     private Integer sleep; 
     private String hostname = null;
 
     public void init(Properties props) {
        try { hostname = InetAddress.getLocalHost().getHostName(); } catch (Exception e) {}
         sleep = Integer.valueOf(props.getProperty("poll_interval", "10000"));
         client = new Client(props.getProperty("app_name", "sample_app"));
 
         try {
             transport = new LWESTransport(InetAddress.getByName(props.getProperty("emit_address", "224.1.1.11")), Integer.valueOf(props.getProperty("emit_port", "9191")), null);
             client.addTransport(transport);
         } catch(Exception e) { e.printStackTrace(); }
         t = new Thread(this);
     }
 
     protected void start() {
         if ((t != null) && !t.isAlive()) {
             should_run = true;
             t.start();
         }
     }
 
     public void run() {
        HashMap<String, Number> stats;
        HashMap<String, String> contexts;
         while (should_run) {
             synchronized(statsExposers) {
                 for (StatsExposer exposer : statsExposers) {
 
                     // can't remove contexts selectively because different
                     // exposers may set different set of contexts
                     client.removeAllContexts();
 
                     // add the hostname context back as removeAllContexts()
                     // really removes all contexts :)
                     if (hostname != null)
                         client.addContext("hostname", hostname);
 
                     stats = exposer.getStats();
                     contexts = exposer.getContexts();
 
                    Iterator it = stats.entrySet().iterator();
                     while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        client.setKey((String)pair.getKey(), ((Number)pair.getValue()).longValue());
                     }
                    it = contexts.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        client.addContext((String)pair.getKey(), (String)(pair.getValue() == null ? "" : pair.getValue()));
                     }
 
                     client.flushStats(true);
                 }
             }
             try {
                 Thread.sleep(this.sleep);
                 // any signal can interrupt our sweet sleep
             } catch (InterruptedException e) {}
         }
     }
 
     protected void stop() {
         if ((t != null) && t.isAlive()) {
             should_run = false;
             t.interrupt();
         }
     }
 
     @Override
     public synchronized void add(StatsExposer s) {
        super(s);
         start();
     }
 
     @Override
     public synchronized void remove(StatsExposer s) {
        super(s);
         if (isEmpty()) {
             stop();
         }
     }
 
     @Override
     public synchronized void removeAll() {
        super(s);
         stop();
     }
 }
