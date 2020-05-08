 package com.maestrodev;
 
import static java.lang.String.*;
import static org.apache.commons.lang3.exception.ExceptionUtils.*;
 import static org.fusesource.stomp.client.Constants.*;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang3.StringUtils;
 import org.fusesource.hawtbuf.Buffer;
 import org.fusesource.stomp.client.BlockingConnection;
 import org.fusesource.stomp.client.Stomp;
 import org.fusesource.stomp.codec.StompFrame;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 /**
  * Main Class for Maestro Plugins written in Java.
  *
  */
 public class MaestroWorker 
 {
     private static Logger logger = Logger.getLogger(MaestroWorker.class.getName());
 
     private JSONObject workitem;
     private Map<String, Object> stompConfig = new HashMap<String, Object>();
     
     /**
      * Helper that sends cancel message that stops composition execution.
      * 
      */
      public void cancel() {
         try{
             
             String [] fields = {"__cancel__"};
             String [] values = {String.valueOf(true)};
             BlockingConnection connection = sendFieldsWithValues(fields, values);
 
             closeConnectionAndCleanup(connection);
         }catch(Exception e){
             logger.log(Level.SEVERE, "Error sending cancel message", e);
         }
     }
      
      /**
      * Puts the current task run in a waiting state or allows it to continue
      * 
      * @param waiting - Will I wait or won't I?
      */
     public void setWaiting(boolean waiting) {
         try{
             
             String [] fields = {"__waiting__"};
             String [] values = {String.valueOf(waiting)};
             BlockingConnection connection = sendFieldsWithValues(fields, values);
 
             closeConnectionAndCleanup(connection);
         }catch(Exception e){
             logger.log(Level.SEVERE, "Error setting waiting to " + waiting, e);
         }
     }
     
     
     /**
      * Helper that sends output strings to server for persistence.
      * 
      * @param output - Message to be persisted for the associated TaskExecution
      */
     public void writeOutput(String output){
         try{
             
             String [] fields = {"__output__","__streaming__"};
             String [] values = {output, String.valueOf(true)};
             BlockingConnection connection = sendFieldsWithValues(fields, values);
 
             closeConnectionAndCleanup(connection);
         }catch(Exception e){
             logger.log(Level.SEVERE, "Error writing output: " + output, e);
         }
     }
     
     private BlockingConnection sendFieldsWithValues(String [] fields, String [] values) throws Exception {
         BlockingConnection connection = this.getConnection();
         if(fields.length != values.length){
             throw new Exception("Mismatched Field and Value Sets fields.length != values.length" );
         }
         
         for(int ii = 0 ; ii < fields.length ; ++ii){
             this.workitem.put(fields[ii], values[ii]);
         }
         
         this.sendCurrentWorkitem(connection);
         
         return connection;
     }
     
     private void sendCurrentWorkitem(BlockingConnection connection) throws IOException{
 
         Object queue = this.stompConfig.get("queue");
         if ( queue == null )
         {
             logger.log( Level.SEVERE, "Missing Stomp Configuration. Make Sure Queue is Set" );
             return;
         }
 
         StompFrame frame = new StompFrame(SEND);
         frame.addHeader(DESTINATION, StompFrame.encodeHeader(queue.toString()));
         Buffer buffer = new Buffer(this.workitem.toJSONString().getBytes());
         frame.content(buffer);
 
         connection.send(frame);
         
         try {
             Thread.sleep(500);
         } catch (InterruptedException ex) {
             logger.log(Level.SEVERE, "Sleep interrupted", ex);
         }
         
     }
     
     private BlockingConnection getConnection()throws IOException, URISyntaxException{
 
         Object h = this.stompConfig.get( "host" );
         Object p = this.stompConfig.get( "port" );
 
         if ( ( h == null ) || ( p == null ) )
         {
             logger.log( Level.SEVERE, "Missing Stomp Configuration. Make Sure Host and Port Are Set" );
             return null;
         }
 
         Stomp stomp = new Stomp( h.toString(), Integer.parseInt( p.toString() ) );
         BlockingConnection connection = stomp.connectBlocking();
         
         return connection;
     }
 
     private void closeConnectionAndCleanup(BlockingConnection connection) throws IOException{
         if (connection != null) {
           connection.suspend();
           connection.close();
         }
         
         this.workitem.remove("__output__");
         this.workitem.remove("__streaming__");
         this.workitem.remove("__cancel__");
         if(this.workitem.get("__waiting__") != null && Boolean.getBoolean(
             this.workitem.get("__waiting__").toString()) == false){
             this.workitem.remove("__waiting__");
         }
     }
     
     /**
      * Helper method for setting the error field
      * 
      * @param error - Error message
      */
     public void setError(String error){
        getFields().put("__error__", error);
     }
     
     /**
      * Helper method for getting the error field
      */
     public String getError(){
         return getField( "__error__" );
     }
 
     /**
     * Helper method for getting a string field
      * 
      * @param field key to get value for
      * @return field value
      */
     public String getField(String field){
        if(getFields().get(field) == null){
             return null;
         }
        return getFields().get(field).toString();
     }
     
     public Map perform(String methodName, Map workitem) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException{
         try{
             JSONParser parser = new JSONParser();
             String json = JSONObject.toJSONString(workitem);
             setWorkitem((JSONObject)parser.parse(json));
 
             Method method = getClass().getMethod(methodName);
             method.invoke(this);
         
         } catch (Exception e) {
            String msg = format("Task %s failed: %s ", methodName, getStackTrace( e ));
            this.writeOutput(msg);
            this.setError(msg);
         }
         return getWorkitem();             
     }
             
             
     
     /**
      * Helper method for getting the fields
      * 
      * @return fields set
      */
     public JSONObject getFields(){
         return ((JSONObject)getWorkitem().get("fields"));
     }
     
     /**
      * Helper method for setting fields
      * @param name string key field name
     * @param value value to apply to field
      * 
      */
    @SuppressWarnings( "unchecked" )
    public void setField(String name, Object value){
        getFields().put(name, value);
     }
     
     /**
      * getter for accessing the Workitem
      * 
      * @return Map of Workitem values
      */
     public JSONObject getWorkitem() {
         return workitem;
     }
 
     /**
      * setter for overwriting all of the Workitem values
      * 
      * @param workitem 
      */
     public void setWorkitem(JSONObject workitem) {
         this.workitem = workitem;
     }
     
     public Map<String, Object> getStompConfig() {
         return stompConfig;
     }
 
     public void setStompConfig(Map<String, Object> stompConfig) {
         this.stompConfig = stompConfig;
     }
 
     
     /*
      * Database 
      */
     
     public void updateFieldsInRecord(String model, String nameOrId, String field, String value) {
         try{
             
             String [] fields = {"__persist__", "__update__", "__model__", "__record_id__", "__record_field__", "__record_value__"};
             String [] values = {String.valueOf(true), String.valueOf(true), model, nameOrId, field, value};
             BlockingConnection connection = sendFieldsWithValues(fields, values);
 
             closeConnectionAndCleanup(connection, fields);
         }catch(Exception e){
             logger.log(Level.SEVERE, "Error updating fields in record, field: " + field + ", value: " + value, e);
         }
     }
 
     private void closeConnectionAndCleanup(BlockingConnection connection, String [] fields) throws Exception {
         this.closeConnectionAndCleanup(connection);
         for(String field : fields){
             this.workitem.remove(field);
         }
     }
 
     void createRecordWithFields(String model, String[] recordFields, String[] recordValues) {
          try{
             
             String [] fields = {"__persist__", "__create__", "__model__", "__record_fields__", "__record_values__"};
             String [] values = {String.valueOf(true), String.valueOf(true), model, StringUtils.join(recordFields, ","), StringUtils.join(recordValues, ",")};
             BlockingConnection connection = sendFieldsWithValues(fields, values);
 
             closeConnectionAndCleanup(connection, fields);
         }catch(Exception e){
             logger.log( Level.SEVERE, "Error creating record, fields: " + StringUtils.join( recordFields, "," )
                 + ", values: " + StringUtils.join( recordValues, "," ), e );
         }
     }
 
     void deleteRecord(String model, String nameOrId) {
         try{
             
             String [] fields = {"__persist__", "__delete__", "__model__", "__name__"};
             String [] values = {String.valueOf(true), String.valueOf(true), model, nameOrId};
             BlockingConnection connection = sendFieldsWithValues(fields, values);
 
             closeConnectionAndCleanup(connection, fields);
         }catch(Exception e){
             logger.log(Level.SEVERE, "Error deleting record: " + model + " - " + nameOrId, e);
         }
     }
 
    /*
     * End Database 
     */
 }
