 package org.rosenvold.spring.convention.interfacemappers;
 
 import org.rosenvold.spring.convention.InterfaceToImplementationMapper;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Kristian Rosenvold
  */
 public class UserSpecifiedMapper implements InterfaceToImplementationMapper {
 
     private final Map<String, String> interfaceToImplementations = new HashMap<String, String>();
 
     public UserSpecifiedMapper put( String interfaceName, String beanClass){
         interfaceToImplementations.put( interfaceName, beanClass);
         return this;
     }
     public UserSpecifiedMapper put( Class interfaceClass, Class beanClass){
         return put( interfaceClass.getName(), beanClass.getName());
     }
 
     public String getBeanClassName(Class aClass) {
         return interfaceToImplementations.get( aClass.getName());
     }
 }
