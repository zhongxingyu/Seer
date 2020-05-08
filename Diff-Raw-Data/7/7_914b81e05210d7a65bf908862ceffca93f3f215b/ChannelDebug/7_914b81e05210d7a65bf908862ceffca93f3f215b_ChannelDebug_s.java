 package channeldebug;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 public class ChannelDebug {
 
     private final String USER_AGENT = "Mozilla/5.0";
     private URL url;
     private final static String SCALAR_KEY = "scalar";
 
     public ChannelDebug(String host, int port, String channel)
     {
         try {
         url = new URL(String.format("%s:%s/%s", host, port, channel));
         } catch(MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void log(Object object)
     {
         try {
             sendPost(object);
         }catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     // HTTP POST request
     private void sendPost(Object object) throws Exception {
 
         HttpURLConnection con = (HttpURLConnection) url.openConnection();
 
         //add reuqest header
         con.setRequestMethod("POST");
         con.setRequestProperty("User-Agent", USER_AGENT);
         con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
         con.setRequestProperty("Accept", "application/json");
         con.setRequestProperty("Content-Type", "application/json");
 
         Gson gson = new GsonBuilder().serializeNulls().create();
         //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
         Object normalisedObject = wrap(normalise(object));
         System.out.println(normalisedObject);
         String urlParameters = gson.toJson(normalisedObject);
 
         // Send post request
         con.setDoOutput(true);
         DataOutputStream wr = new DataOutputStream(con.getOutputStream());
         System.out.println(String.format(gson.toJson(normalisedObject)));
         wr.writeBytes(urlParameters);
         wr.flush();
         wr.close();
 
         int responseCode = con.getResponseCode();
         System.out.println("\nSending 'POST' request to URL : " + url);
         System.out.println("Post parameters : " + urlParameters);
         System.out.println("Response Code : " + responseCode);
 
         BufferedReader in = new BufferedReader(
                 new InputStreamReader(con.getInputStream()));
         String inputLine;
         StringBuffer response = new StringBuffer();
 
         while ((inputLine = in.readLine()) != null) {
             response.append(inputLine);
         }
         in.close();
 
         //print result
         System.out.println(response.toString());
 
     }
 
     private Object normalise(Object object)
     {
         if(isPrimitive(object)) {
             Map<String, Object> map = new HashMap<String, Object>();
             map.put(SCALAR_KEY, object);
             return map;
         }
         try {
             Map<String, Object> map = new HashMap<String, Object>();
             Map<String, Object> properties = new HashMap<String, Object>();
             Map<String, Object> statics = new HashMap<String, Object>();
             Map<String, Object> constants = new HashMap<String, Object>();
             for(Field field : object.getClass().getDeclaredFields()) {
                 field.setAccessible(true);
                 int modifiers = field.getModifiers();
                 Object fieldValue = field.get(object);
 
                 Map<String, Object> fieldMap;
                 if(Modifier.isFinal(modifiers)) {
                     fieldMap = constants;
                 } else if(Modifier.isStatic(modifiers)) {
                     fieldMap = statics;
                 } else {
                     fieldMap = properties;
                 }
 
 
                 if(fieldValue == null) {
                     fieldMap.put(field.getName(), null);
                 } else if(isPrimitive(field.get(object))) {
                     fieldMap.put(field.getName(), field.get(object));
                 } else {
                     fieldMap.put(field.getName(), normalise(field.get(object)));
                 }
             }
             map.put("class", getClassChain(object.getClass()));
             map.put("properties", properties);
             map.put("static", statics);
             map.put("constants", constants);
             map.put("methods", getMethods(object.getClass()));
             return map;
         }catch(Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private Object wrap(Object object)
     {
         Map<String, Object> wrapper = new HashMap<String, Object>();
         List<Object> args = new LinkedList<Object>();
         args.add(object);
         wrapper.put("handler", "object");
         wrapper.put("args", args);
         wrapper.put("stacktrace", new LinkedList<Object>());
         return wrapper;
     }
 
     private Object getClassChain(Class<?> clazz)
     {
         List<String> superClasses = new LinkedList<String>();
         do {
             superClasses.add(clazz.getName());
             clazz = clazz.getSuperclass();
        } while(!clazz.equals(Object.class));
         superClasses.add(Object.class.getName());
         return superClasses;
     }
 
     private Object getMethods(Class<?> clazz)
     {
         Map<String, List<String>> methodArguments = new HashMap<String, List<String>>();
         for(Method method : clazz.getDeclaredMethods()) {
             List<String> parameters = new LinkedList<String>();
             for(Class<?> parameterType : method.getParameterTypes()) {
                 parameters.add(parameterType.getName());
             }
             methodArguments.put(method.getName(), parameters);
         }
         return methodArguments;
     }
 
     private boolean isPrimitive(Object o)
     {
         return isWrapperType(o.getClass());
     }
 
     private static boolean isWrapperType(Class<?> clazz)
     {
         return getWrapperTypes().contains(clazz);
     }
 
     private static Set<Class<?>> getWrapperTypes()
     {
         Set<Class<?>> ret = new HashSet<Class<?>>();
         ret.add(Boolean.class);
         ret.add(Character.class);
         ret.add(Byte.class);
         ret.add(Short.class);
         ret.add(Integer.class);
         ret.add(Long.class);
         ret.add(Float.class);
         ret.add(Double.class);
         ret.add(Void.class);
         ret.add(String.class);
         return ret;
     }
 
 
 
 }
