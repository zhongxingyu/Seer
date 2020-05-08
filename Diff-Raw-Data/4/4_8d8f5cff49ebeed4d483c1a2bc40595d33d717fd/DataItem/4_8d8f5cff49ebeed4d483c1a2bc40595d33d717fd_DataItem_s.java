 package org.vsegda.data;
 
 import com.google.appengine.api.datastore.Key;
 import org.vsegda.util.TimeUtil;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 /**
  * @author Roman Elizarov
  */
 @PersistenceCapable
 public class DataItem {
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 
     // non-persistent!!!
     private transient DataStream stream;
 
 	@Persistent
 	private long streamId;
 
 	@Persistent
 	private double value;
 
 	@Persistent
 	private long timeMillis;
 
     public DataItem() {}
 
     public DataItem(String line, long now) {
         String[] tokens = line.split(",");
         if (tokens.length < 2 || tokens.length > 3)
             throw new IllegalArgumentException("Invalid line format: " + line);
         String streamCode = tokens[0];
         try {
             streamId = Long.parseLong(streamCode);
         } catch (NumberFormatException e) {
             // it means that this is a stream tag
             stream = new DataStream();
             stream.setTag(streamCode);
         }
         try {
             value = Double.parseDouble(tokens[1]);
             timeMillis = tokens.length < 3 ? now : TimeUtil.parseTime(tokens[2], now);
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Invalid line format: " + line, e);
         }
 	}
 
     public DataItem(long streamId, double value, long timeMillis) {
         this.streamId = streamId;
         this.value = value;
         this.timeMillis = timeMillis;
     }
 
     public Key getKey() {
         return key;
     }
 
     public void setKey(Key key) {
         this.key = key;
     }
 
     public long getStreamId() {
         return streamId;
     }
 
     public void setStreamId(long streamId) {
         this.streamId = streamId;
     }
 
     public DataStream getStream() {
         return stream == null ? new DataStream(streamId) : stream;
     }
 
     public void setStream(DataStream stream) {
         this.stream = stream;
     }
 
     /**
      * Returns stream id or tag of the stream if defined.
      */
     public String getStreamCode() {
         return stream != null && stream.getTag() != null ? stream.getTag() : String.valueOf(streamId);
     }
 
     public double getValue() {
 		return value;
 	}
 
 	public void setValue(double value) {
 		this.value = value;
 	}
 
 	public long getTimeMillis() {
 		return timeMillis;
 	}
 
 	public void setTimeMillis(long timeMillis) {
 		this.timeMillis = timeMillis;
 	}
 
     public String getTime() {
         return TimeUtil.formatDateTime(timeMillis);
     }
 
     public String getAgo() {
         return TimeUtil.formatDateTimeDifference(getTimeMillis(), System.currentTimeMillis());
     }
 
     public String getFormatClass() {
         return TimeUtil.getFormatClass(getTimeMillis(), System.currentTimeMillis());
     }
 
     public boolean isRecent() {
         return timeMillis >= System.currentTimeMillis() - DataArchive.RECENT_TIME_INTERVAL;
     }
 
     @Override
 	public String toString() {
         return getStreamCode() + "," + value + "," + TimeUtil.formatDateTime(timeMillis);
 	}
 }
