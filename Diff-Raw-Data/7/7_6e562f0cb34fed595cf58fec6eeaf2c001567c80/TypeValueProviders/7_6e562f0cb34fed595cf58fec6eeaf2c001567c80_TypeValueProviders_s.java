 package com.ramodage.provider;
 
 import org.apache.log4j.Logger;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: rixonmathew
  * Date: 20/01/13
  * Time: 11:43 PM
  */
 public class TypeValueProviders {
 
     private final static Logger LOG = Logger.getLogger(TypeValueProviders.class);
     private static final Map<String,ValueProvider> valueProviders;
 
     static {
         valueProviders = new HashMap<String, ValueProvider>();
         valueProviders.put("int",new IDValueProvider());
         valueProviders.put("String",new StringValueProvider());
         valueProviders.put("Date",new DateValueProvider());
         valueProviders.put("BigDecimal",new BigDecimalValueProvider());
         valueProviders.put("Timestamp",new TimestampValueProvider());
     }
 
 
     public static ValueProvider valueProviderFor(String type){
         if (!valueProviders.containsKey(type)) {
             LOG.debug("Invalid value type specified "+type);
         }
         return valueProviders.get(type);
     }
 }
