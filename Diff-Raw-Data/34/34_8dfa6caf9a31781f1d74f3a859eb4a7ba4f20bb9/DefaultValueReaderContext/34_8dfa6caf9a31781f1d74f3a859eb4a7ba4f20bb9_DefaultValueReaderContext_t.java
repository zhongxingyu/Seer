 package com.agileapes.couteau.context.value.impl;
 
 import com.agileapes.couteau.context.impl.AbstractTypeSpecificContext;
 import com.agileapes.couteau.context.value.ValueReader;
 import com.agileapes.couteau.context.value.ValueReaderContext;
 
 /**
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (2013/6/30, 14:06)
  */
 public class DefaultValueReaderContext extends AbstractTypeSpecificContext<ValueReader> implements ValueReaderContext {
 
    public DefaultValueReaderContext() {
        ready();
    }

     private ValueReader getValueReader(Class<?> type, boolean mandatory) {
         for (ValueReader reader : getBeans()) {
             if (reader.canRead(type)) {
                 return reader;
             }
         }
         if (mandatory) {
             throw new IllegalArgumentException("No value reader registered for type: " + type.getCanonicalName());
         }
         return null;
     }
 
     @Override
     public boolean canRead(Class<?> type) {
         return getValueReader(type, false) != null;
     }
 
     @Override
     public <E> E read(String text, Class<E> type) {
         return getValueReader(type, true).read(text, type);
     }
 
 }
