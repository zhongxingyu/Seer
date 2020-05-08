 package com.dottydingo.hyperion.service.translation;
 
 import net.sf.cglib.beans.BeanMap;
 
 /**
  */
 public class DefaultFieldMapper <C,P> implements FieldMapper<C,P>
 {
     private String clientFieldName;
     private String persistentFieldName;
     protected BeanMap clientBeanMap;
     protected BeanMap persistentBeanMap;
     protected ValueConverter valueConverter;
 
     public DefaultFieldMapper(String name, BeanMap clientBeanMap, BeanMap persistentBeanMap)
     {
         this(name,name,clientBeanMap,persistentBeanMap,null);
     }
 
     public DefaultFieldMapper(String clientFieldName, String persistentFieldName, BeanMap clientBeanMap,
                               BeanMap persistentBeanMap, ValueConverter valueConverter)
     {
         this.clientFieldName = clientFieldName;
         this.persistentFieldName = persistentFieldName;
         this.clientBeanMap = clientBeanMap;
         this.persistentBeanMap = persistentBeanMap;
         this.valueConverter = valueConverter;
     }
 
     @Override
     public String getClientFieldName()
     {
         return clientFieldName;
     }
 
     public String getPersistentFieldName()
     {
         return persistentFieldName;
     }
 
     @Override
     public void convertToClient(P persistentObject, C clientObject, TranslationContext context)
     {
         Object persistentValue = persistentBeanMap.get(persistentObject,getPersistentFieldName());
 
         if(valueConverter != null)
         {
             persistentValue = valueConverter.convertToClientValue(persistentValue,context);
         }
 
         clientBeanMap.put(clientObject,getClientFieldName(),persistentValue);
 
     }
 
     @Override
     public void convertToPersistent(C clientObject, P persistentObject, TranslationContext context)
     {
         Object clientValue = clientBeanMap.get(clientObject,getClientFieldName());
         if(valueConverter != null)
         {
             clientValue = valueConverter.convertToPersistentValue(clientValue,context);
         }
        persistentBeanMap.put(persistentObject,getPersistentFieldName(),clientValue);
     }
 }
