 package com.pms.service.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 @Target({ ElementType.METHOD, ElementType.TYPE })
 @Retention(RetentionPolicy.RUNTIME)
 public @interface RoleValidate {
 
     String roleID() default "";
 
     String desc() default "";
   
     String[] value() default {};
 }
