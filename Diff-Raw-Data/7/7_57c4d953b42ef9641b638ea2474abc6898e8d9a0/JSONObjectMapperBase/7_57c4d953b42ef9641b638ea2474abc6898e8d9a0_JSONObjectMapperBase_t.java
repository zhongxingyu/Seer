 package org.slstudio.acs.hms.messaging.mapper;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.jackson.annotate.JsonAutoDetect;
 import org.codehaus.jackson.annotate.JsonMethod;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.type.TypeReference;
 import org.slstudio.acs.hms.exception.MessagingException;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: chandler
  * Date: 13-5-11
  * Time: 12:23
  */
 public abstract class JSONObjectMapperBase<T> implements IObjectMapper {
     private static final Log log = LogFactory.getLog(JSONObjectMapperBase.class);
 
     private ObjectMapper mapper = null;
 
     public JSONObjectMapperBase() {
         mapper = new ObjectMapper();
         mapper.setVisibility(JsonMethod.GETTER, JsonAutoDetect.Visibility.ANY);
         mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, false);
         mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
         mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
 
     }
 
     @SuppressWarnings("unchecked")
     public T toObject(String str) throws MessagingException {
         try {
           
            return  (T)mapper.readValue(str, getTypeReference());
         } catch ( Exception exp){
             log.error("convert string:" + str + " to object type error", exp);
             throw new MessagingException("convert string to object error", exp);
         }
     }
 
     public String toString(Object obj) throws MessagingException{
         String jsonString = null;
         try {
             jsonString = mapper.writeValueAsString(obj);
             log.debug("convert json object type:" + obj.getClass().getName() + " to string:" + jsonString);
         } catch (IOException exp) {
             throw new MessagingException("convert json object to string error", exp);
         }
         return jsonString;
     }
 
     protected abstract TypeReference getTypeReference();
 
 }
