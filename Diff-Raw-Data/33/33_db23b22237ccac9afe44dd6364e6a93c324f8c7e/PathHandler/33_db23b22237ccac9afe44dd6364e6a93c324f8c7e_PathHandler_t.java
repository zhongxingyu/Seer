 package io.loli.restloli.core.init;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.ws.rs.Path;
 
 /**
  * 从class中读取path配置并写入配置map
  * 
  * @author choco
  * 
  */
 public class PathHandler {
     /**
      * 读取单个类获取配置
      * 
      * @param clazz
      * @return 配置map
      */
     public static Map<? extends AnnotationConfig, ? extends Method> handle(
             Class<?> clazz) {
         String rootPath = clazz.getAnnotation(Path.class).value();
         Map<AnnotationConfig, Method> map = new HashMap<AnnotationConfig, Method>();
         if (rootPath == null) {
             throw new NullPointerException("path不能为空");
         } else {
             rootPath = filterPath(rootPath);
             for (Method method : clazz.getDeclaredMethods()) {
                 String path = null;
                 if (method.getAnnotations().length > 0) {
                     try {
                         path = method.getAnnotation(Path.class).value();
                     } catch (NullPointerException e) {
                        path = "";
                     }
                     path = filterPath(path);
                     String fullPath = rootPath.concat(path);
                     map.put(new AnnotationConfig()
                             .setPathConfig(new PathConfig(fullPath)), method);
                 }
             }
         }
         return map;
     }
 
     /**
      * 更新配置map
      * 
      * @param configMap
      * @return
      */
     public static Map<? extends AnnotationConfig, ? extends Method> handle(
             Map<AnnotationConfig, Method> configMap) {
         for (Entry<AnnotationConfig, Method> entry : configMap.entrySet()) {
             Method method = entry.getValue();
             Class<?> clazz = method.getDeclaringClass();
             String rootPath = clazz.getAnnotation(Path.class).value();
             if (rootPath == null) {
                 throw new NullPointerException("path不能为空");
             } else {
                 rootPath = filterPath(rootPath);
 
                 String path = method.getAnnotation(Path.class).value();
                 if (path == null) {
                     configMap.put(new AnnotationConfig()
                             .setPathConfig(new PathConfig(path)), method);
                 } else if (method.getAnnotations().length > 0) {
                     path = filterPath(path);
                     String fullPath = rootPath.concat(path);
                     AnnotationConfig config = entry.getKey();
                     configMap.remove(config);
                     configMap.put(new AnnotationConfig()
                             .setPathConfig(new PathConfig(fullPath)), method);
                 }
             }
         }
         return configMap;
     }
 
     /**
      * 给path开头增加"/",结尾删除"/"
      * 
      * @param path
      * @return 更新后的path
      */
     private static String filterPath(String path) {
         if (path.length() == 0)
             return path;
         if (path.charAt(0) != '/') {
             path = "/".concat(path);
         }
         if (path.charAt(path.length() - 1) == '/') {
             path = path.substring(0, path.length() - 1);
         }
         return path;
     }
 }
