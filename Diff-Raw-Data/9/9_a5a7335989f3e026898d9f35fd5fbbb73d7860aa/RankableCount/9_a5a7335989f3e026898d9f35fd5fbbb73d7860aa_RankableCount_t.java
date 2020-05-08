 package com.mapr.demo.storm.util;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import backtype.storm.tuple.Tuple;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 /**
  * This class wraps an objects and its associated count, including any additional data fields.
  *
  * This class can be used, for instance, to track the number of occurrences of an object in a Storm topology.
  *
  */
 public class RankableCount implements Rankable<RankableCount>, Serializable {
     private final Object obj;
     private final long count;
     private final ImmutableList<Object> fields;
 
     public RankableCount(Object obj, long count, Object... otherFields) {
         if (obj == null) {
             throw new IllegalArgumentException("The object must not be null");
         }
         if (count < 0) {
             throw new IllegalArgumentException("The count must be >= 0");
         }
         this.obj = obj;
         this.count = count;
         fields = ImmutableList.copyOf(otherFields);
 
     }
 
     /**
      * Construct a new instance based on the provided {@link Tuple}.
      *
      * This method expects the object to be ranked in the first field (index 0) of the provided tuple, and the number of
      * occurrences of the object (its count) in the second field (index 1). Any further fields in the tuple will be
      * extracted and tracked, too. These fields can be accessed via {@link RankableCount#getFields()}.
      *
      * @param tuple  The tuple to convert into a Rankable.
      * @return A usable Rankable object
      */
     public static Rankable from(Tuple tuple) {
         List<Object> otherFields = Lists.newArrayList(tuple.getValues());
         Object obj = otherFields.remove(0);
         Long count = (Long) otherFields.remove(0);
         return new RankableCount(obj, count, otherFields.toArray());
     }
 
     public Object getObject() {
         return obj;
     }
 
     public long getCount() {
         return count;
     }
 
     /**
      * @return an immutable list of any additional data fields of the object (may be empty but will never be null)
      */
     public List<Object> getFields() {
         return fields;
     }
 
     public int compareTo(RankableCount other) {
        //int r = Long.compare(this.getCount(), other.getCount());
        long r = this.getCount() - other.getCount();
         if (r == 0) {
             // we try to not return 0 if objects are different
             return this.obj.hashCode() - this.obj.hashCode();
        } else if( r > 0 ) {
            return 1;
         } else {
            return -1;
         }
     }
 
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (!(o instanceof RankableCount)) {
             return false;
         }
         RankableCount other = (RankableCount) o;
         return obj.equals(other.obj) && count == other.count;
     }
 
     public int hashCode() {
         int result = 17;
         int countHash = (int) (count ^ (count >>> 32));
         result = 31 * result + countHash;
         result = 31 * result + obj.hashCode();
         return result;
     }
 
     public String toString() {
         StringBuilder buf = new StringBuilder();
         buf.append("[key=");
         buf.append(obj);
         buf.append(", count=");
         buf.append(count);
         buf.append(", fields={");
         String sep = "";
         for (Object field : fields) {
             buf.append(sep);
             buf.append(field);
             sep = ",";
         }
         buf.append("}]");
         return buf.toString();
     }
 }
