 package be.cegeka.eventualizr.web.test.infrastructure;
 
 import java.io.IOException;
 
 import be.cegeka.eventualizr.web.api.ObjectMapperProvider;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class JsonHelper {
 	
 	private static final ObjectMapper MAPPER = new ObjectMapperProvider().getContext(null);
 
     private JsonHelper() { /* singleton */ }
     
     public static String asJson(Object object) throws IOException {
         return MAPPER.writeValueAsString(object);
     }
     
     public static <T> T fromJson(String json, Class<T> klass) throws IOException {
         return MAPPER.readValue(json, klass);
     }
     
    @SuppressWarnings("unchecked")
	public static <T> T fromJson(String json, TypeReference<T> reference) throws IOException {
        return (T) MAPPER.readValue(json, reference);
     }
 
 }
