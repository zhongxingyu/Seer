 package hadoopSameContent;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class CheckSameContentJobMapper extends Mapper<LongWritable, Text, Text, Text> {
 
     /*
      * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN,
      * org.apache.hadoop.mapreduce.Mapper.Context)
      * 
      * Convert the input to a canonical JSON string if possible Retrieve the
      * input file this data came from Return the pair {Canonical Data, file
      * origin}
      */
     @Override
     public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
         IdentifyingRecordSubset id = (getIdentifyingRequestHashFromJson(value.toString(), context));
 
         if (id != null) {
            context.write(new Text(id.getIdentifyingHash()), value);
         }
     }
 
     /*
      * Process the JSON node provided, loading it into a TreeMap to provide a
      * consistent order. If nested objects are encountered they will be
      * processed recursively to independently canonicalize the nested object
      * contents giving a fully canonical form.
      * 
      * TODO: Does not currently handle JSON Array types
      */
     private IdentifyingRecordSubset getIdentifyingRequestHashFromJson(JsonNode rootNode, IdentifyingRecordSubset id) {
 
         // Iterate over all nodes at this level of the JSON Tree
         Iterator<Entry<String, JsonNode>> fields = rootNode.getFields();
         while (fields.hasNext()) {
             Entry<String, JsonNode> field = fields.next();
 
             // Handle recursion cases
             if (field.getValue().isObject()) {
                 // Detected a nested object, so make a recursive call to
                 // determine the object's canonical form before storing it as a
                 // string at this level of the tree.
                 id = getIdentifyingRequestHashFromJson(field.getValue(), id);
             } else {
                 // Base case
                 if (field.getKey().equals("event_type") && 
                         !((field.getValue().toString().equals("esVDNAAppUserActionEvent")) 
                             || (field.getValue().toString().equals("\"esVDNAAppUserActionEvent\"")))) {
                     return null;
                 } else if (field.getKey().equals("client_ip")){
                     id.setClientIP(field.getValue().toString());
                 } else if (field.getKey().equals("timestamp")){
                     id.setTimestamp(field.getValue().toString());
                 } else if (field.getKey().equals("url")){
                     id.setURL(field.getValue().toString());
                 } else{
                     continue;
                 }
             }
         }
 
         return id;
     }
 
     /*
      * Top level wrapper for:
      * 
      * @see getIdentifyingRequestHashFromJson(JsonNode)
      * 
      * Uses the context from the map task to update JSON or TEXT counters
      * respectively and parse the initial string into a root JSON object if
      * possible. This allows the main canonicalization function to remain
      * decoupled from this class for potential re-use. If it can be parsed it
      * will pass the generated JsonNode object to the canonicaliseJson(JsonNode)
      * method for canonicalization.
      */
     private IdentifyingRecordSubset getIdentifyingRequestHashFromJson(String from, Context context) throws JsonParseException, IOException {
         JsonFactory factory = new JsonFactory();
         ObjectMapper mapper = new ObjectMapper(factory);
         JsonParser jp;
         jp = factory.createJsonParser(from);
         JsonNode rootNode;
 
         try {
             rootNode = mapper.readTree(jp); // Attempt to parse into Json.
 
             // If this point is reached then the data is Json so return its
             // canonical form.
             context.getCounter(HadoopCountersEnum.JSON_LINES).increment(1);
             IdentifyingRecordSubset id = new IdentifyingRecordSubset();
             return getIdentifyingRequestHashFromJson(rootNode, id);
         } catch (JsonProcessingException e) {
             // If it could not read a JSON Structure then treat as a string.
             context.getCounter(HadoopCountersEnum.TEXT_LINES).increment(1);
             // return from;
             return null; // If not JSON ignore the line
         }
     }
 
     public static String SHA1(String text) {
         MessageDigest md;
         try {
             md = MessageDigest.getInstance("SHA-1");
             byte[] sha1hash = new byte[40];
             md.update(text.getBytes("iso-8859-1"), 0, text.length());
             sha1hash = md.digest();
             return convToHex(sha1hash);
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
             return text;
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             return text;
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
             System.err.println(text);
             return text;
         }
     }
 
     private static String convToHex(byte[] data) {
         StringBuilder buf = new StringBuilder();
         for (int i = 0; i < data.length; i++) {
             int halfbyte = (data[i] >>> 4) & 0x0F;
             int two_halfs = 0;
             do {
                 if ((0 <= halfbyte) && (halfbyte <= 9))
                     buf.append((char) ('0' + halfbyte));
                 else
                     buf.append((char) ('a' + (halfbyte - 10)));
                 halfbyte = data[i] & 0x0F;
             } while (two_halfs++ < 1);
         }
         return buf.toString();
     }
 
     private class IdentifyingRecordSubset {
         private String clientIP;
         private String timestamp;
         private String url;
 
         public void setClientIP(String clientIP){
             this.clientIP = clientIP;
         }
 
         public void setTimestamp(String timestamp){
             this.timestamp = timestamp;
         }
 
         public void setURL(String url){
             this.url = url;
         }
 
         public String getIdentifyingHash(){
             if (clientIP == null || timestamp == null || url == null){
                 System.err.println("Fields required for hash not present in json, ignoring record");
                 return null;
             }else{
                 return SHA1(clientIP + timestamp + url);
             }
         }
     }
 }
