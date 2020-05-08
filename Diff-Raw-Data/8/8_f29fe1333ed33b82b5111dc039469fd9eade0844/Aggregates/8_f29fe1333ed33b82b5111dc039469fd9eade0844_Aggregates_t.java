 package com.imaginea.qctree.measures;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableUtils;
 
 import com.imaginea.qctree.Row;
 
 public class Aggregates implements Writable {
 
   private final Map<String, Aggregable> aggregates;
 
   public Aggregates() {
     aggregates = new HashMap<String, Aggregable>();
     aggregates.put(Average.class.getSimpleName(), new Average());
     aggregates.put(Sum.class.getSimpleName(), new Sum());
     aggregates.put(Maximum.class.getSimpleName(), new Maximum());
     aggregates.put(Minimum.class.getSimpleName(), new Minimum());
     aggregates.put(Count.class.getSimpleName(), new Count());
   }
 
   public void addAggregate(Aggregable aggr) {
     aggregates.put(aggr.getClass().getSimpleName(), aggr);
   }
 
   public void compute(List<Row> rows) {
     for (Aggregable aggr : aggregates.values()) {
       aggr.aggregate(rows);
     }
   }
 
   public Map<String, Aggregable> get() {
     return aggregates;
   }
 
   @Override
   public void readFields(DataInput in) throws IOException {
    for(int i = 0; i < aggregates.size(); ++i) {
      String key = WritableUtils.readString(in);
      Aggregable aggregable = aggregates.get(key);
      aggregable.readFields(in);
    }
   }
 
   @Override
   public void write(DataOutput out) throws IOException {
     for (Entry<String, Aggregable> aggr : aggregates.entrySet()) {
       WritableUtils.writeString(out, aggr.getKey());
       aggr.getValue().write(out);
     }
   }
 
   public void accumalate(Aggregates other) {
     for (Entry<String, Aggregable> aggr : aggregates.entrySet()) {
       Aggregable otherAggr = other.aggregates.get(aggr.getKey());
       aggr.getValue().accumalate(otherAggr);
     }
   }
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder();
     for (Entry<String, Aggregable> aggr : aggregates.entrySet()) {
       sb.append(aggr.getKey()).append('=');
       sb.append(aggr.getValue().toString());
       sb.append('\n');
     }
     return sb.substring(0, sb.length() - 1);
   }
 }
