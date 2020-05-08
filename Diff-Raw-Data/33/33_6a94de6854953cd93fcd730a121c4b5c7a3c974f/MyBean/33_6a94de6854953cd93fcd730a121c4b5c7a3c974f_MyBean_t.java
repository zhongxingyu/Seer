 /**
  * 
  */
 package org.jboss.solderbug;
 
 import javax.enterprise.inject.spi.InjectionPoint;
 import javax.inject.Inject;
 
 /**
  * @author antoine
  * 
  */
 
 @MyQualifier
 public class MyBean {
 
     @Inject
     InjectionPoint ip;
 
    @Inject
    MyBean(InjectionPoint ip) {
        this.ip = ip;
    }

     public String saySomething() {
 
         String value = "";
 
         if (ip != null)
             value = ip.getAnnotated().getAnnotation(MyQualifier.class).value();
         return "Hello CDI World " + value;
     }
 
 }
