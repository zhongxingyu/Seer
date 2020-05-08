 package nl.wes;
 
 import backtype.storm.Config;
 import backtype.storm.StormSubmitter;
 import backtype.storm.generated.AlreadyAliveException;
 import backtype.storm.generated.InvalidTopologyException;
 import backtype.storm.generated.StormTopology;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.tuple.Fields;
 import com.netflix.curator.RetryPolicy;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.framework.recipes.shared.SharedCount;
 import com.netflix.curator.retry.ExponentialBackoffRetry;
 import nl.wes.bolts.FatFusionBolt;
 import nl.wes.bolts.ReliablePowerBolt;
 import nl.wes.bolts.SensorServerOutputBolt;
 import nl.wes.bolts.VirtualPowerBolt;
 import nl.wes.spouts.RemoteSensorSpout;
 import org.apache.log4j.Logger;
 
 public class Main {
 
     private final static Logger logger = Logger.getLogger(Main.class);
 
     public static void main(String[] args) {
 
         String zookeeperConnectionString = "";
         String sensorServerAddress = "";
         String sensorServerPort = "";
         String sensorServerListeningPort = "";
         int numberOfSpouts = -1;
         int numberOfBolts = -1;
         int numberOfOutputBolts = -1;
         int numberOfSensorsPerSpout = -1;
         int topologyWorkers = -1;
         String type = "";
         int maxNumberOfOutstandingRequests = -1;
         boolean debugFlag = false;
 
         if (args.length < 12) {
             logger.error("Required arguments: \"" +
                     "zookeeperConnectionString " +
                     "sensorServerAddress " +
                     "sensorServerPort " +
                     "sensorServerListeningPort " +
                     "numberOfSpouts " +
                     "numberOfBolts " +
                     "numberOfOutputBolts " +
                     "numberOfSensorsPerSpout " +
                     "topologyWorkers " +
                     "type " +
                     "maxNumberOfOutstandingRequests\"" +
                     "debugFlag");
             System.exit(-1);
         } else {
             zookeeperConnectionString = args[0];
             sensorServerAddress = args[1];
             sensorServerPort = args[2];
             sensorServerListeningPort = args[3];
             numberOfSpouts = Integer.parseInt(args[4]);
             numberOfBolts = Integer.parseInt(args[5]);
             numberOfOutputBolts = Integer.parseInt(args[6]);
             numberOfSensorsPerSpout = Integer.parseInt(args[7]);
             topologyWorkers = Integer.parseInt(args[8]);
             type = args[9];
             maxNumberOfOutstandingRequests = Integer.parseInt(args[10]);
             if (args[11].toLowerCase().equals("true")) {
                 debugFlag = true;
             }
         }
 
         try {
             initializeSensorIdCounters(zookeeperConnectionString);
         } catch (Exception e) {
             logger.error("Error initializing counters, exiting.", e);
             System.exit(-2);
         }
 
         StormTopology topology = createTopology(type, numberOfSensorsPerSpout, numberOfSpouts, numberOfBolts,
                 numberOfOutputBolts);
 
         Config config = new Config();
         config.setDebug(debugFlag);
         //config.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 1);
         config.put(Config.TOPOLOGY_WORKERS, topologyWorkers);
 
         // ZooKeeper connection String used for communication with Zookeeper and Kafka (e.g., "10.0.0.1:2181")
         config.put("zk-connection", zookeeperConnectionString);
         // Connection details for the sensor server
         config.put("sensor-server-address", sensorServerAddress);
         config.put("sensor-server-port", sensorServerPort);
         config.put("sensor-server-listening-address", sensorServerListeningPort);
         config.put("max-outstanding-requests", maxNumberOfOutstandingRequests + "");
 
         try {
             StormSubmitter.submitTopology("test", config, topology);
         } catch (AlreadyAliveException e) {
             e.printStackTrace();
         } catch (InvalidTopologyException e) {
             e.printStackTrace();
         }
     }
 
     private static StormTopology createTopology(String type, int numberOfSensorsPerSpout, int numberOfSpouts,
                                                 int numberOfBolts, int numberOfOutputBolts) {
         TopologyBuilder builder = new TopologyBuilder();
 
         if (type.equals("simple")) {
 
             builder.setSpout("voltage-sensor", new RemoteSensorSpout("voltage", numberOfSensorsPerSpout), numberOfSpouts);
 
             builder.setBolt("sensor-server-output", new SensorServerOutputBolt(), numberOfOutputBolts)
                     .noneGrouping("voltage-sensor");
 
         } else if (type.equals("virtual")) {
 
             builder.setSpout("voltage-sensor", new RemoteSensorSpout("voltage", numberOfSensorsPerSpout), numberOfSpouts);
             builder.setSpout("current-sensor", new RemoteSensorSpout("current", numberOfSensorsPerSpout), numberOfSpouts);
 
            builder.setBolt("virtual-power-sensor", new VirtualPowerBolt(), numberOfBolts)
                     .fieldsGrouping("voltage-sensor", new Fields("id"))
                     .fieldsGrouping("current-sensor", new Fields("id"));
 
             builder.setBolt("sensor-server-output", new SensorServerOutputBolt(), numberOfOutputBolts)
                     .noneGrouping("virtual-power-sensor");
 
         } else if (type.equals("reliable")) {
 
             builder.setSpout("voltage-sensor", new RemoteSensorSpout("voltage", numberOfSensorsPerSpout), numberOfSpouts);
             builder.setSpout("current-sensor", new RemoteSensorSpout("current", numberOfSensorsPerSpout), numberOfSpouts);
             builder.setSpout("power-sensor", new RemoteSensorSpout("power", numberOfSensorsPerSpout), numberOfSpouts);
 
            builder.setBolt("virtual-power-sensor", new VirtualPowerBolt(), numberOfBolts)
                     .fieldsGrouping("voltage-sensor", new Fields("id"))
                     .fieldsGrouping("current-sensor", new Fields("id"));
 
            builder.setBolt("reliable-power-sensor", new ReliablePowerBolt(), numberOfBolts)
                     .fieldsGrouping("virtual-power-sensor", new Fields("id"))
                     .fieldsGrouping("power-sensor", new Fields("id"));
 
             builder.setBolt("sensor-server-output", new SensorServerOutputBolt(), numberOfOutputBolts)
                     .noneGrouping("reliable-power-sensor");
 
         } else if (type.equals("fat")) {
 
             builder.setSpout("voltage-sensor", new RemoteSensorSpout("voltage", numberOfSensorsPerSpout), numberOfSpouts);
             builder.setSpout("current-sensor", new RemoteSensorSpout("current", numberOfSensorsPerSpout), numberOfSpouts);
             builder.setSpout("power-sensor", new RemoteSensorSpout("power", numberOfSensorsPerSpout), numberOfSpouts);
 
             builder.setBolt("reliable-power-sensor", new FatFusionBolt(), numberOfBolts)
                     .fieldsGrouping("voltage-sensor", new Fields("id"))
                     .fieldsGrouping("current-sensor", new Fields("id"))
                     .fieldsGrouping("power-sensor", new Fields("id"));
 
             builder.setBolt("sensor-server-output", new SensorServerOutputBolt(), numberOfOutputBolts)
                     .noneGrouping("reliable-power-sensor");
 
         } else {
             logger.error("Type " + type + " does not exist, valid options are: simple, virtual, reliable, or fat");
             System.exit(-2);
         }
         return builder.createTopology();
     }
 
     /**
      * All of the sensor need a unique id, however, id's need to be the same between the different kind
      * of sensors, since the id used to to synchronize on (e.g., data from voltage sensor 1 is fused with
      * data from current sensor 1 to produce a virtual power sensor with an id of 1).
      *
      * A global counter based on ZooKeeper is used to assign id's from 1 to the specified number of
      * sensors. The SharedCount recipe of Apache curator is used for this. This method will initialize the
      * counters for every type of sensor to 0. The mock sensors will the use the counter to obtain id's.
      *
      * @param zookeeperConnectionString The String needed to connect to the ZooKeeper system.
      */
     private static void initializeSensorIdCounters(String zookeeperConnectionString) throws Exception {
         // Initialize Curator for communication with ZooKeeper
         RetryPolicy retryPolicy = new ExponentialBackoffRetry(500, 10);
         CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
         client.start();
 
         String[] paths = {
                 "/sensor/current",
                 "/sensor/voltage",
                 "/sensor/power"
         };
 
         // initialize nodes for the sensor types if these don't exist yet
         for (String path : paths) {
             if (client.checkExists().forPath(path) == null) {
                 client.create().creatingParentsIfNeeded().forPath(path);
             }
         }
 
         // Set all of the counters to 0
         for (String path : paths) {
             SharedCount sharedCount = new SharedCount(client, path, 0);
             sharedCount.start();
             sharedCount.setCount(0);
             sharedCount.close();
         }
 
         // Close the Curator client
         client.close();
     }
 }
