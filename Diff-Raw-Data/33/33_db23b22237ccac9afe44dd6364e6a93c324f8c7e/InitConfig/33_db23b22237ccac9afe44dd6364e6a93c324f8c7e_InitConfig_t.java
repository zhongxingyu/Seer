 package io.loli.restloli.core.servlet;
 
 import io.loli.restloli.core.LoliClassLoader;
 import io.loli.restloli.core.init.AnnotationConfig;
 import io.loli.restloli.core.init.AnnotationTypeHandler;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 
 /**
  * Servlet初始化时读取配置, 这是配置的Bean类
  * 
  * @author choco
  * 
  */
 public class InitConfig {
     // 包名
     private String packageName;
 
     private Map<AnnotationConfig, Method> configMap;
 
     public String getPackageName() {
         return packageName;
     }
 
     public void setPackageName(String packageName) {
         this.packageName = packageName;
     }
 
     /**
      * 加载config
      * 
      * @param config
      * @throws InitConfigException
      *             当没有获取到正确的参数时, 会抛出异常
      */
     public void loadConfig(ServletConfig config) {
         packageName = config
                 .getInitParameter("io.loli.restloli.core.init.PackageName");
         if (packageName != null && packageName != "") {
             this.loadPackage(packageName);
         } else {
             throw new InitConfigException("ServletConfig 加载错误, 请检查web.xml");
         }
     }
 
     /**
      * 加载指定包中的类并生成配置map
      * 
     * @param packageName 包名
      */
     private void loadPackage(String packageName) {
         Set<Class<?>> classes = LoliClassLoader.getClasses(packageName);
         this.configMap = new AnnotationTypeHandler().handle(classes);
     }
 
     public Map<AnnotationConfig, Method> getConfigMap() {
         return configMap;
     }
 
     public void setConfigMap(Map<AnnotationConfig, Method> configMap) {
         this.configMap = configMap;
     }
 }
