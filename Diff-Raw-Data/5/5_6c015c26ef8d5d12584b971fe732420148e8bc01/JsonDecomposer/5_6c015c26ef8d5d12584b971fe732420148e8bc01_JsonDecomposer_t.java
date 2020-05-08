 package us.yuxin.sa.transformer;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 public class JsonDecomposer {
   public static final String CATEGORY_POSTFIX_KEY = "__cp";
 
   ObjectMapper mapper;
 
   protected DateTimeFormatter timeFmt;
   protected AtomicInteger serial;
   protected AtomicInteger oidSerial;
 
   Transformer transformer;
   Map<String, Object> params;
   Map<String, Object> lastValue;
 
   byte[] lastKey;
   byte[] lastBinaryKey;
 
   ByteBuffer keyBuffer;
   boolean categoryPostfix;
 
   public JsonDecomposer() {
     this(null);
   }
 
 
   public JsonDecomposer(Transformer transformer) {
     this.transformer = transformer;
 
     mapper = new ObjectMapper();
 
     timeFmt = DateTimeFormat.forPattern("YYYYMMddHHmmssSSS");
     oidSerial = new AtomicInteger(0);
     serial = new AtomicInteger(0);
     params = new HashMap<String, Object>();
 
     categoryPostfix = true;
 
     if (categoryPostfix) {
       keyBuffer = ByteBuffer.allocate(16);
    } else {
      keyBuffer = ByteBuffer.allocate(12);
     }
   }
 
 
   public void setTransformer(Transformer transformer) {
     this.transformer = transformer;
   }
 
 
   public Map<String, Object> readValue(byte[] text) throws IOException {
     lastKey = null;
     lastBinaryKey = null;
 
     lastValue = mapper.readValue(text, Map.class);
 
     if (lastValue != null) {
       params.clear();
       params.put("raw", text);
 
       if (transformer != null) {
         try {
           transformer.transform(lastValue, params);
         } catch (Exception e) {
           // TODO Logging error info into map.
           lastValue = null;
           e.printStackTrace();
         }
       }
       createRowId();
     }
 
     return lastValue;
   }
 
 
   private void createRowId() {
     if (lastValue == null) {
       return;
     }
 
     Long ots = null;
     Object ov;
 
     ov = lastValue.get("logTimestamp");
     if (ov == null) {
       return;
     }
 
     if (ov instanceof Long) {
       ots = (Long) ov;
     } else if (ov instanceof Integer) {
       // logger.warn("Invalid logTimestamp(int) - raw-log:" + new String(event.getBody()));
       ots = ((Integer) ov).longValue();
     } else if (ov instanceof String) {
       // logger.warn("Invalid logTimestamp(string) - raw-log:" + new String(event.getBody()));
       ots = Long.parseLong((String) ov);
     }
 
     if (ots == null) {
       // logger.warn("Invalid logTimestamp(empty) - raw-log:" + new String(event.getBody()));
       // return new Text("9999");
       return;
     }
 
     Integer oid = (Integer) lastValue.get("oid");
     if (oid == null)
       oid = oidSerial.incrementAndGet();
 
     Integer ser = serial.incrementAndGet();
 
     keyBuffer.rewind();
     keyBuffer.putLong(ots);
     keyBuffer.putShort((short)(ser & 0xffff));
     keyBuffer.putShort((short)(oid & 0xffff));
 
     String cp = null;
     if (categoryPostfix) {
       cp = (String)lastValue.get(CATEGORY_POSTFIX_KEY);
       if (cp == null) {
         cp = (String)params.get(CATEGORY_POSTFIX_KEY);
       }
 
       if (cp == null) {
         cp = "____";
       }
 
       // TODO fill short category postfix to least 4 byte
       lastKey = String.format("%s.%04d%04d%-4.4s",
         timeFmt.print(ots), oid % 10000, ser % 10000, cp).getBytes();
       keyBuffer.put(cp.getBytes(), 0, 4);
     } else {
       lastKey = String.format("%s.%04d%04d",
         timeFmt.print(ots), oid % 10000, ser % 10000).getBytes();
     }
     lastBinaryKey = keyBuffer.array();
   }
 
 
   public void start(Properties conf) {
     if (transformer != null) {
       transformer.setup(conf);
     }
     start();
   }
 
 
   public void start() {
     if (transformer != null)
       transformer.start();
   }
 
 
   public void stop() {
     if (transformer != null)
       transformer.stop();
   }
 
 
   public byte[] getKey() {
     return lastKey;
   }
 
 
   public byte[] getBinaryKey() {
     return lastBinaryKey;
   }
 }
