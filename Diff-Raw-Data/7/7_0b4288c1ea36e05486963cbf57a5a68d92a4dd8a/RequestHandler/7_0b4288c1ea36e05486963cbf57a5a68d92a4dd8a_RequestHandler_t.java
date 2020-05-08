 package io.loli.restloli.core.servlet;
 
 import io.loli.restloli.core.init.AnnotationConfig;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * 处理request的类
  * 
  * @author choco
  * 
  */
 public class RequestHandler {
     private Map<AnnotationConfig, Method> configMap;
 
     public RequestHandler(InitConfig initConfig) {
         this.configMap = initConfig.getConfigMap();
     }
 
     public Object[] methodParamGenerate(HttpServletRequest request,
             Method method) {
         // TODO 生成方法参数
         return new Object[] {};
     }
 
     public void service(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         String pathInfo = request.getPathInfo();
        if (pathInfo.endsWith("/")) {
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }
         String httpMethod = request.getMethod();
         // if request url matches method url
         boolean flag = false;
         for (Entry<AnnotationConfig, Method> entry : configMap.entrySet()) {
             AnnotationConfig config = entry.getKey();
             Method method = entry.getValue();
             // TODO Response处理
             if (config.getPathConfig().getPath().equals(pathInfo)
                     && config.getHttpTypeConfig().getHttpType().toString()
                             .equals(httpMethod)) {
                 if (!flag) {
                     flag = true;
                 }
                 Object responseObj = null;
                 try {
                     responseObj = method.invoke(method.getDeclaringClass()
                             .newInstance(),
                             methodParamGenerate(request, method));
                 } catch (IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException | InstantiationException e) {
                     throw new RuntimeException(e);
                 }
                 if (responseObj instanceof String) {
                     response.setContentType("text/plain");
                     response.getOutputStream().print((String) responseObj);
                 }
             } else {
                 continue;
             }
         }
         if (!flag) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            ;
         }
     }
 
 }
