 package com.movile.cassandra;
 
 
 import me.prettyprint.cassandra.connection.HConnectionManager;
 import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
 import me.prettyprint.cassandra.serializers.BytesArraySerializer;
 import me.prettyprint.cassandra.serializers.LongSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.cassandra.service.CassandraHostConfigurator;
 import me.prettyprint.cassandra.service.ExhaustedPolicy;
 import me.prettyprint.cassandra.service.FailoverPolicy;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.HConsistencyLevel;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.factory.HFactory;
 
 import org.apache.log4j.Logger;
 
 import com.movile.utils.AppProperties;
 
 /**
  * @author J.P. Eiti Kimura (eiti.kimura@movile.com)
  */
 public class CassandraBase {
 
     protected static Logger log = Logger.getLogger("cassandra");
 
     protected static final String KEYSPACE = "Company";
     protected static final String COLUMNFAMILY_EMP = "Employees";
     
     protected static StringSerializer stringSerializer = StringSerializer.get();
     protected static LongSerializer longSerializer = LongSerializer.get();
     protected static BytesArraySerializer byteArraySerializer = BytesArraySerializer.get();
 
     /**
      * Cassandra/Hector objects to Cassandra Comunication
      */
     protected Cluster cluster;
 
     protected Keyspace keyspace;
 
     public CassandraBase() {
         String hosts = AppProperties.getDefaultInstance().getString("cassandra.ips", "127.0.0.1:9106");
         String clusterName = AppProperties.getDefaultInstance().getString("cassandra.clusterName", "sbs01_cluster");
         int maxActive = AppProperties.getDefaultInstance().getInt("cassandra.maxActive", 50);
         int maxIdle = AppProperties.getDefaultInstance().getInt("cassandra.maxIdle", 5);
         int maxWaitTime = 1000 * AppProperties.getDefaultInstance().getInt("cassandra.maxWaitTime", 30);
         int reconnectInterval = AppProperties.getDefaultInstance().getInt("cassandra.reconnectInterval", 5);
         boolean autoDiscoveryHosts = AppProperties.getDefaultInstance().getBoolean("cassandra.autoDiscoveryHosts", false);
 
         // cassandra host and pool configurations
         CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(hosts);
 
         hostConfigurator.setMaxWaitTimeWhenExhausted(maxWaitTime);
         hostConfigurator.setExhaustedPolicy(ExhaustedPolicy.WHEN_EXHAUSTED_GROW);
         hostConfigurator.setMaxActive(maxActive);
         hostConfigurator.setCassandraThriftSocketTimeout(maxWaitTime);
         hostConfigurator.setMaxIdle(maxIdle);
 
         hostConfigurator.setRetryDownedHosts(true);
         hostConfigurator.setRetryDownedHostsDelayInSeconds(reconnectInterval);
 
         hostConfigurator.setAutoDiscoverHosts(autoDiscoveryHosts);
         hostConfigurator.setAutoDiscoveryDelayInSeconds(60);
 
         cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator);
 
         // set the consistency level
         ConfigurableConsistencyLevel consistenceLevel = new ConfigurableConsistencyLevel();
         consistenceLevel.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
         
         keyspace = HFactory.createKeyspace(KEYSPACE, cluster, consistenceLevel, FailoverPolicy.ON_FAIL_TRY_ALL_AVAILABLE);
 
     }
 
 
     /**
      * Get information about cassandra cluster
      * @return current status
      */
     public String getClusterInformation() {
 
         StringBuilder sb = new StringBuilder();
         try {
             HConnectionManager conn = cluster.getConnectionManager();
 
             sb.append("Cluster Stats [name=").append(cluster.getName()).append(", ");
             sb.append("activePools=").append(conn.getActivePools()).append(", ");
             sb.append("statusPerPool=").append(conn.getStatusPerPool()).append(", ");
             sb.append("hosts=").append(conn.getHosts()).append(", ");
             sb.append("downnedHosts=").append(conn.getDownedHosts()).append("];");
         } catch (Exception e) {
             log.error("Error trying to log the Cassandra cluster stats: " + e.toString(), e);
         }
         return sb.toString();
     }
 
     /**
      * Finalizes the hector connection pool
      */
     public void shutdown() {
         cluster.getConnectionManager().shutdown();
     }
 }
