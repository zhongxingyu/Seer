 package com.github.drashid.status.metric;
 
 import com.yammer.metrics.core.Gauge;
 
 public class GaugeInfo implements MetricData {
 
  private MetricType type = MetricType.GAUGE;
 
   private String machine;
 
   private String name;
 
   private Object value;
 
   public GaugeInfo(String machineCode, String metricName, Gauge<?> value) {
     this.value = value.value();
     this.machine = machineCode;
     this.name = metricName;
   }
 
   @Override
   public MetricType getType() {
     return type;
   }
 
   @Override
   public String getMachine() {
     return machine;
   }
 
   @Override
   public String getName() {
     return name;
   }
 
   public Object getValue() {
     return value;
   }
 
 }
