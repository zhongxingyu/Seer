 package com.socialcomputing.junit.rules;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * @author "Jonathan Dray <jonathan@social-computing.com>"
  * 
  * Annotation to mark test method that requires a servlet container to run 
  * with some guice ressources registered
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.METHOD})
 public @interface GuiceWebResource {
     
     /**
      * Array of {@link com.google.inject.servlet.GuiceServletContextListener} to add to the
      * application server servlet context listener
      */
     Class<? extends GuiceServletContextListener>[] guiceListeners();
 }
