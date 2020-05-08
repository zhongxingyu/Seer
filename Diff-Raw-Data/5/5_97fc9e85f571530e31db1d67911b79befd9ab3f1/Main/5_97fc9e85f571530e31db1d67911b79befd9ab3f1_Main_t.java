 package de.jacobs1.jmxcollector;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import java.rmi.ConnectException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 import org.apache.log4j.Logger;
 
 import org.rrd4j.ConsolFun;
 import org.rrd4j.DsType;
 
 import org.rrd4j.core.RrdDb;
 import org.rrd4j.core.RrdDbPool;
 import org.rrd4j.core.RrdDef;
 import org.rrd4j.core.Sample;
 
 import org.rrd4j.graph.RrdGraph;
 import org.rrd4j.graph.RrdGraphDef;
 
 public class Main {
 
     private static final Logger LOG = Logger.getLogger(Main.class);
 
     private static final Map<Connection, MBeanServerConnection> mBeanServerConnections =
         new ConcurrentHashMap<Connection, MBeanServerConnection>();
 
     private static final AtomicLong updateCount = new AtomicLong();
 
     public static void graph(final String rrdPath, final String dsName, final String outPath) throws Exception {
         RrdGraphDef graphDef = new RrdGraphDef();
         graphDef.setTimeSpan(-3600, -1);
         graphDef.setVerticalLabel("req/s");
         graphDef.datasource("req", rrdPath, dsName, ConsolFun.AVERAGE);
         graphDef.line("req", new Color(0xFF, 0, 0), null, 2);
         graphDef.gprint("req", ConsolFun.MIN, "%10.2lf/s MIN");
         graphDef.gprint("req", ConsolFun.AVERAGE, "%10.2lf/s AVG");
         graphDef.gprint("req", ConsolFun.MAX, "%10.2lf/s MAX");
         graphDef.setFilename(outPath);
 
         // graphDef.setBase(1);
         RrdGraph graph = new RrdGraph(graphDef);
         BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
         graph.render(bi.getGraphics());
     }
 
     /* For simplicity, we declare "throws Exception".
      *Real programs will usually want finer-grained exception handling. */
     public static void main(final String[] args) throws Exception {
 
         if (args.length < 1) {
             System.out.println("Usage: jmxcollector <CONFIGFILE>");
             return;
         }
 
         if (args.length >= 4 && args[0].equals("graph")) {
             graph(args[1], args[2], args[3]);
             return;
         }
 
         run(args[0]);
     }
 
     /**
      * old style properties based config.
      *
      * @param   configFile
      * @param   datasources
      *
      * @return
      *
      * @throws  IOException
      * @throws  MalformedObjectNameException
      */
     private static int loadConfigFromProperties(final String configFile, final List<DataSource> datasources)
         throws IOException, MalformedObjectNameException {
         final List<Connection> connections = new ArrayList<Connection>();
 
         final Properties props = new Properties();
         final FileReader fr = new FileReader(configFile);
         props.load(fr);
         fr.close();
 
         String val;
         int i;
 
         i = 1;
         while ((val = props.getProperty("connection." + i + ".host")) != null) {
             Connection conn = new Connection();
             conn.setHost(val);
             conn.setPort(props.getProperty("connection." + i + ".port"));
             conn.setUser(props.getProperty("connection." + i + ".user"));
             conn.setPassword(props.getProperty("connection." + i + ".password"));
             connections.add(conn);
             i++;
         }
 
         i = 1;
         while ((val = props.getProperty("datasource." + i + ".connection")) != null) {
             DataSource ds = new DataSource();
             ds.setConnection(connections.get(Integer.valueOf(val) - 1));
             ds.setBeanName(new ObjectName(props.getProperty("datasource." + i + ".bean")));
             ds.setAttributeName(props.getProperty("datasource." + i + ".attribute"));
 
             String[] parts = props.getProperty("datasource." + i + ".rrd").split(":", 2);
             ds.setRrdPath(parts[0]);
             ds.setRrdDSName(parts[1]);
             datasources.add(ds);
             i++;
         }
 
         return connections.size();
 
     }
 
     private static String stripQuotes(String str) {
         if (str.startsWith("\"")) {
             str = str.substring(1);
         }
 
         if (str.endsWith("\"")) {
             str = str.substring(0, str.length() - 1);
         }
 
         return str;
     }
 
     private static String replaceVariables(final String inp, final Connection conn) {
         return inp.replaceAll("%h", conn.getHost()).replaceAll("%4p",
                 conn.getPort().substring(conn.getPort().length() - 4));
     }
 
     private static int loadConfigFromConfigFile(final String configFile, final List<DataSource> datasources)
         throws IOException, MalformedObjectNameException {
 
         int numberOfConnections = 0;
 
         final FileReader fr = new FileReader(configFile);
         final BufferedReader br = new BufferedReader(fr);
 
         String line;
         String[] parts;
         Connection conn = null;
         DataSource ds;
         while ((line = br.readLine()) != null) {
             if (line.trim().startsWith("#") || line.trim().isEmpty()) {
 
                 // comment
                 continue;
             }
 
             if (!line.startsWith(" ") && !line.startsWith("\t")) {
                 line = line.trim();
 
                 // connection definition
                 parts = line.split("[@:]", 4);
                 conn = new Connection();
                 conn.setHost(parts[2]);
                 conn.setPort(parts[3]);
                 conn.setUser(parts[0]);
                 conn.setPassword(parts[1]);
                 numberOfConnections++;
             } else {
                 line = line.trim();
 
                 // datasource definition (uses most recently defined connection)
                 parts = line.split("\\s*=\\s*", 2);
                 ds = new DataSource();
                 ds.setConnection(conn);
 
                 if (parts[1].startsWith("GAUGE:")) {
                     ds.setRrdDSType(DsType.GAUGE);
                     parts[1] = parts[1].substring(6);
                 } else {
                     ds.setRrdDSType(DsType.DERIVE);
                 }
 
                 final int dot = parts[1].lastIndexOf('.');
                 ds.setBeanName(new ObjectName(replaceVariables(stripQuotes(parts[1].substring(0, dot)), conn)));
                 ds.setAttributeName(parts[1].substring(dot + 1));
 
                 final String[] pathDSName = parts[0].split(":", 2);
                 ds.setRrdPath(replaceVariables(pathDSName[0], conn));
                 ds.setRrdDSName(pathDSName[1]);
                 datasources.add(ds);
             }
         }
 
         fr.close();
 
         return numberOfConnections;
 
     }
 
     public static void run(final String configFile) throws IOException, MalformedObjectNameException, MBeanException,
         AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
 
         final List<DataSource> datasources = new ArrayList<DataSource>();
         int numberOfConnections;
 
         if (configFile.endsWith(".properties")) {
             numberOfConnections = loadConfigFromProperties(configFile, datasources);
         } else {
             numberOfConnections = loadConfigFromConfigFile(configFile, datasources);
         }
 
         LOG.info("Loaded config from " + configFile + " with " + numberOfConnections + " connections and "
                 + datasources.size() + " datasources");
 
         for (DataSource ds : datasources) {
             createRrdFile(ds.getRrdPath(), ds.getRrdDSName(), ds.getRrdDSType());
         }
 
         final RrdDbPool pool = RrdDbPool.getInstance();
         pool.setCapacity(datasources.size() * 2);
 
         for (DataSource dataSource : datasources) {
             dataSource.setRrdDb(pool.requestRrdDb(dataSource.getRrdPath()));
         }
 
         final long interval = 2000;
         int j;
 
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(numberOfConnections,
                 new DaemonThreadFactory("Updater"));
 
         j = 1;
         for (final DataSource dataSource : datasources) {
 
             final int dataSourceId = j;
             final Runnable updater = new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         final long currentCount = updateCount.incrementAndGet();
 
                         final MBeanServerConnection mbsc = getMBeanServerConnection(dataSource.getConnection());
                         if (mbsc == null) {
                             return;
                         }
 
                         Object attr = null;
                         try {
                             attr = mbsc.getAttribute(dataSource.getBeanName(), dataSource.getAttributeName());
                         } catch (ConnectException ce) {
                             LOG.error("ConnectException while trying to get attribute " + dataSource.getAttributeName()
                                     + " from " + dataSource.getBeanName().getCanonicalName(), ce);
 
                             // remove connection to force reconnect
                             mBeanServerConnections.remove(dataSource.getConnection());
                             return;
                         } catch (InstanceNotFoundException infe) {
                             LOG.error("InstanceNotFoundException while trying to get attribute "
                                     + dataSource.getAttributeName() + " from "
                                     + dataSource.getBeanName().getCanonicalName(), infe);
                             return;
                         }
 
                         final RrdDb rrd = dataSource.getRrdDb();
                         final Sample sample;
 
                         sample = rrd.createSample();
 
                         double val = 0;
                         if (attr instanceof Integer) {
                             val = (Integer) attr;
                         } else if (attr instanceof Long) {
                             val = ((Long) attr).intValue();
                         } else if (attr instanceof Float) {
                             val = ((Float) attr);
                         } else if (attr instanceof Double) {
                             val = ((Double) attr);
                         } else {
                             throw new IllegalArgumentException("Unsupported type " + attr + " for attribute "
                                     + dataSource.getAttributeName() + " for datasource " + dataSourceId);
                         }
 
                         sample.setValue(dataSource.getRrdDSName(), val);
                         try {
                             sample.update();
                         } catch (IllegalArgumentException iae) {
                             LOG.error("Dropping sample of datasource " + dataSourceId, iae);
                         }
                     } catch (final Throwable ex) {
 
                         // catch all. never fail. that would cancel the scheduler.
                         LOG.error("Unexpected exception while trying to update from datasource " + dataSourceId, ex);
                     }
                 }
             };
 
            executor.scheduleWithFixedDelay(updater, 0, interval, TimeUnit.MILLISECONDS);
             j++;
         }
 
         int i = 0;
         while (true) {
             sleep(60000);
             LOG.info("Heartbeat " + i + " (" + updateCount.get() + " updates)");
             i++;
         }
     }
 
     private static MBeanServerConnection getMBeanServerConnection(final Connection conn) throws IOException {
         MBeanServerConnection mbsc = mBeanServerConnections.get(conn);
         if (mbsc == null) {
 
             JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + conn.getHost() + ":"
                         + conn.getPort() + "/jmxrmi");
             Map<String, Object> env = new HashMap<String, Object>();
 
             if (conn.getUser() != null) {
                 String[] credentials = new String[] {conn.getUser(), conn.getPassword()};
                 env.put("jmx.remote.credentials", credentials);
             }
 
             try {
                 LOG.info("Trying to connect to " + conn.getHost() + ":" + conn.getPort());
 
                 JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
                 LOG.info("Trying to get an MBeanServerConnection");
                 mbsc = jmxc.getMBeanServerConnection();
                 mBeanServerConnections.put(conn, mbsc);
             } catch (IOException ioe) {
                 LOG.error("Failed to connect to JMX service URL " + url, ioe);
                 return null;
             }
         }
 
         return mbsc;
     }
 
     private static void createRrdFile(final String path, final String dsName, final DsType dsType) throws IOException {
         final RrdDbPool pool = RrdDbPool.getInstance();
 
         if (!(new File(path)).exists()) {
             LOG.info("Creating new RRD file " + path);
 
             RrdDef def = new RrdDef(path, 2);
             def.addDatasource(dsName, dsType, 90, 0, Double.NaN);
 
             // 2sec resolution for the last 4 hours
             def.addArchive(ConsolFun.AVERAGE, 0.5, 1, 30 * 60 * 4);
 
             // 10sec resolution for the last 24 hours
             def.addArchive(ConsolFun.AVERAGE, 0.5, 5, 600 * 24);
 
             // 1min resolution for the last week
             def.addArchive(ConsolFun.AVERAGE, 0.5, 30, 60 * 24 * 7);
 
             // 1 hour resolution for the last 365 days
             def.addArchive(ConsolFun.AVERAGE, 0.5, 30 * 60, 24 * 365);
 
             RrdDb rrd = pool.requestRrdDb(def);
             pool.release(rrd);
         }
     }
 
     private static void sleep(final long millis) {
         try {
             Thread.sleep(millis);
         } catch (InterruptedException e) {
             LOG.error("Sleep was interrupted", e);
         }
     }
 }
