 package utils;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonParser;
 import play.data.binding.Global;
 import play.data.binding.TypeBinder;
 
 import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
 
 /**
  * This binder automatically binds values to JSON
  */
 @Global
 public class GsonBinder implements TypeBinder<JsonArray> {
 
     @Override
    public Object bind(String name, Annotation[] annotations, String value, Class aClass, Type type) throws Exception {
         return new JsonParser().parse(value);
     }
 }
