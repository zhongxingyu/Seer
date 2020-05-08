 package org.thoughtworks.orc.internal.util;
 
 
 import com.google.inject.Singleton;
 
 import javax.servlet.http.HttpServletRequest;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 import java.util.Map;
 import java.util.stream.Collectors;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static java.util.Arrays.asList;
 
 @Singleton
 public class FormPicker {
 
     public static <T> T pickupObject(HttpServletRequest request, Class<T> tClass) {
         return createObj(tClass, request.getParameterMap(), "");
     }
 
     public static String pickupSingle(HttpServletRequest request, String name) {
         return request.getParameter(name);
     }
 
     public static String[] pickupSet(HttpServletRequest request, String name) {
         return request.getParameterValues(name);
     }
 
     protected static <O> O createObj(Class<O> clazz, Map<String, String[]> values, String prefix) {
         if (prefix.length() > 0 && !values.keySet().stream().anyMatch(key -> key.startsWith(prefix + "."))) return null;
         final O instance = createInstance(clazz);
         asList(clazz.getDeclaredMethods()).stream()
                 .filter(m -> m.getName().startsWith("set") && m.getParameterCount() == 1)
                 .forEach(setter -> {
                     final Class<?> p0Clazz = setter.getParameterTypes()[0];
                     final String p0Name = propertyName(prefix, setter.getName());
                     final Object property = createProperty(clazz, p0Clazz, values, p0Name);
                     if (property != null) {
                         invokeSetter(setter, instance, property);
                     }
                 });
         return instance;
     }
 
     private static Object createProperty(Class<?> oClass, Class<?> pClass, Map<String, String[]> values, String fullName) {
         final String value = values.get(fullName) != null ? values.get(fullName)[0] : null;
         switch (pClass.getCanonicalName()) {
             case "java.lang.String":
                 return value == null ? null : value;
             case "java.lang.Boolean":
                 return value == null ? null : "true".equalsIgnoreCase(value);
             case "java.lang.Integer":
                 return value == null ? null : Integer.parseInt(value);
             case "java.lang.Double":
                 return value == null ? null : Double.parseDouble(value);
             case "java.util.List":
                final List<?> result = createList(getGenericType(oClass, fullName), values, fullName);
                return result.size() > 0 ? result : null;
             default:
                 return createObj(pClass, values, fullName);
         }
     }
 
     protected static Class<?> getGenericType(Class<?> oClass, String fullName) {
         final ParameterizedType genericType;
         try {
             genericType = (ParameterizedType) oClass.getDeclaredField(fullName).getGenericType();
             return Class.forName(genericType.getActualTypeArguments()[0].getTypeName());
         } catch (NoSuchFieldException e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
     private static List<?> createList(Class<?> lClass, Map<String, String[]> values, String fullName) {
         return newArrayList(0, 1, 2, 3, 4, 5).stream()
                 .map(i -> {
                     return createObj(lClass, values, fullName + "[" + i + "]");
                 }).filter(o -> o != null).collect(Collectors.<Object>toList());
     }
 
     private static <O> O createInstance(Class<O> clazz) {
         try {
             return clazz.newInstance();
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
     private static void invokeSetter(Method setter, Object instance, Object property) {
         try {
             setter.invoke(instance, property);
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
     private static String propertyName(String prefix, String setterName) {
         final String name0 = setterName.replace("set", "");
         final String parent = prefix.length() > 0 ? prefix + "." : "";
         return parent + name0.substring(0, 1).toLowerCase() + name0.substring(1);
     }
 }
