 package com.github.drashid.status;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.type.TypeReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import redis.clients.jedis.Jedis;
 
 import com.github.drashid.service.impl.RedisService;
 import com.github.drashid.status.health.ServerHealth;
 import com.github.drashid.status.metric.MeterInfo;
 import com.github.drashid.status.metric.MetricData;
 import com.github.drashid.status.metric.TimerInfo;
 import com.github.drashid.utils.MachineUtils;
 import com.github.drashid.utils.json.JsonUtils;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.yammer.metrics.HealthChecks;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Gauge;
 import com.yammer.metrics.core.HealthCheck.Result;
 import com.yammer.metrics.core.Meter;
 import com.yammer.metrics.core.Metric;
 import com.yammer.metrics.core.MetricName;
 import com.yammer.metrics.core.Timer;
 
 @Singleton
 public class ServerStatusGateway {
 
   private static final String SERVER_HEALTH_KEY = "_health_";
 
   private static final String TIMER_HASH_KEY = "_timers_";
 
   private static final String MACHINE_CODE = MachineUtils.getMachineName();
 
   private static final Logger LOG = LoggerFactory.getLogger(ServerStatusGateway.class);
   
   @Inject
   private RedisService redis;
 
   public void pushHealth() {
     Jedis conn = redis.getConnection();
     try {
       Map<String, Result> results = HealthChecks.runHealthChecks();
       Map<String, String> map = Maps.newHashMap();
       map.put(MACHINE_CODE, JsonUtils.toJsonString(new ServerHealth(MACHINE_CODE, results)));
 
       conn.hmset(SERVER_HEALTH_KEY, map);
     } catch (Exception e) {
       LOG.error("Error pushing health checks!", e);
     } finally {
       redis.returnConnection(conn);
     }
   }
 
   public Map<String, ServerHealth> getHealthChecks() throws JsonProcessingException, IOException {
     Jedis conn = redis.getConnection();
     try {
       String[] keys = conn.hkeys(SERVER_HEALTH_KEY).toArray(new String[] {});
       if (keys.length == 0) {
         return Maps.newHashMap();
       }
 
       List<String> values = conn.hmget(SERVER_HEALTH_KEY, keys);
       Map<String, ServerHealth> toRet = new HashMap<String, ServerHealth>(keys.length);
       for (int i = 0; i < keys.length; i++) {
         toRet.put(keys[i], JsonUtils.toObject(values.get(i), ServerHealth.class));
       }
       return toRet;
     } finally {
       redis.returnConnection(conn);
     }
   }
 
   public void pushMetrics() {
     Jedis conn = redis.getConnection();
     try {
       List<MetricData> metricInfos = Lists.newArrayList();
       for (Entry<MetricName, Metric> entry : Metrics.defaultRegistry().allMetrics().entrySet()) {
         String metricName = getFullName(entry.getKey());
         
         if (entry.getValue() instanceof Timer) {
           metricInfos.add(new TimerInfo(MACHINE_CODE, metricName, (Timer)entry.getValue()));
         }else if(entry.getValue() instanceof Meter) {
           metricInfos.add(new MeterInfo(MACHINE_CODE, metricName, (Meter)entry.getValue()));
         }else if(entry.getValue() instanceof Gauge) {
          
         }
       }
       Map<String, String> nodeTimerMap = Maps.newHashMap();
       nodeTimerMap.put(MACHINE_CODE, JsonUtils.toJsonString(metricInfos, new TypeReference<List<MetricData>>(){}));
       conn.hmset(TIMER_HASH_KEY, nodeTimerMap);
     } catch (Exception e) {
       LOG.error("Error pushing metrics data!", e);
     } finally {
       redis.returnConnection(conn);
     }
   }
 
   public Map<String, List<MetricData>> getMetrics() throws JsonProcessingException, IOException {
     Jedis conn = redis.getConnection();
     try {
       String[] keys = conn.hkeys(TIMER_HASH_KEY).toArray(new String[] {});
       if (keys.length == 0) {
         return Maps.newHashMap();
       }
 
       List<String> values = conn.hmget(TIMER_HASH_KEY, keys);
       Map<String, List<MetricData>> toRet = new HashMap<String, List<MetricData>>(keys.length);
       for (int i = 0; i < keys.length; i++) {
         toRet.put(keys[i], JsonUtils.toObject(values.get(i), new TypeReference<List<MetricData>>(){}));
       }
       return toRet;
     } finally {
       redis.returnConnection(conn);
     }
   }
 
   private static String getFullName(MetricName name) {
     return name.getGroup() + "." + name.getType() + "." + name.getName();
   }
 
   public void removeMetrics(String machine) {
     Jedis conn = redis.getConnection();
     try {
       conn.hdel(TIMER_HASH_KEY, machine);
       conn.hdel(SERVER_HEALTH_KEY, machine);
     } finally {
       redis.returnConnection(conn);
     }
   }
 
 }
