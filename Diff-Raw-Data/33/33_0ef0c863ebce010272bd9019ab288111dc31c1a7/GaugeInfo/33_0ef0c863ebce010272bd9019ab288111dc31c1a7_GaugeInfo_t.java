 package com.github.drashid.status.metric;
 
 import com.yammer.metrics.core.Gauge;
 
 public class GaugeInfo implements MetricData {
 
  private MetricType type;
 
   private String machine;
 
   private String name;
 
   private Object value;
 
   public GaugeInfo(String machineCode, String metricName, Gauge<?> value) {
     this.value = value.value();
     this.machine = machineCode;
     this.name = metricName;
    this.type = MetricType.GAUGE;
  }

  GaugeInfo() {

  }

  public void setType(MetricType type) {
    this.type = type;
  }

  public void setMachine(String machine) {
    this.machine = machine;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(Object value) {
    this.value = value;
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
