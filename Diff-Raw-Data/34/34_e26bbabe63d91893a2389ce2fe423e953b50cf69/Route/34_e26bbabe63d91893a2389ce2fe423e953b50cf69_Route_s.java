 package org.apache.struts2.freeroute.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * 配置一个路由
  *
  * @author bastengao
  * @date 12-12-16 15:15
  */
 @Target({ElementType.METHOD, ElementType.TYPE})
 @Retention(RetentionPolicy.RUNTIME)
 public @interface Route {
     /**
      * 映射的路径
      *
      * @return
      */
     String value() default "";
 
     /**
      * 映射的 http method, 默认不限制
      *
      * @return
      */
    MethodType method() default MethodType.NONE;
 
     /**
      * 指定的参数
      *
      * @return
      */
     String [] params() default {};
 }
