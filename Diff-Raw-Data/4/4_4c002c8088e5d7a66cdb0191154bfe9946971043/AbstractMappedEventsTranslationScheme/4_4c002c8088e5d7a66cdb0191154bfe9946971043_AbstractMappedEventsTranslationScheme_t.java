 package com.agileapes.couteau.context.spring.event.impl;
 
 import com.agileapes.couteau.context.contract.Event;
 import com.agileapes.couteau.context.spring.error.EventTranslationException;
 import com.agileapes.couteau.context.spring.event.TranslationScheme;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ApplicationEvent;
 import org.springframework.util.ReflectionUtils;
 import org.springframework.util.StringUtils;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (6/29/13, 4:51 PM)
  */
 public abstract class AbstractMappedEventsTranslationScheme implements TranslationScheme, ApplicationContextAware {
 
     private ClassLoader classLoader;
     private final Map<Class<? extends Event>, Class<? extends ApplicationEvent>> classMap = new ConcurrentHashMap<Class<? extends Event>, Class<? extends ApplicationEvent>>();
 
     protected abstract Class<?> mapEvent(Class<? extends Event> eventClass, ClassLoader classLoader) throws ClassNotFoundException;
 
     private Class<? extends ApplicationEvent> getTargetEvent(Class<? extends Event> eventClass) throws ClassNotFoundException {
         if (classMap.containsKey(eventClass)) {
             return classMap.get(eventClass);
         }
         final Class<? extends ApplicationEvent> targetEvent = mapEvent(eventClass, classLoader).asSubclass(ApplicationEvent.class);
         classMap.put(eventClass, targetEvent);
         return targetEvent;
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         classLoader = applicationContext.getClassLoader();
     }
 
     @Override
     public ApplicationEvent translate(Event originalEvent) throws EventTranslationException {
         final Class<? extends ApplicationEvent> eventClass;
         try {
             eventClass = getTargetEvent(originalEvent.getClass());
         } catch (ClassNotFoundException e) {
             throw new EventTranslationException("Failed to locate target class for event", e);
         }
         final ApplicationEvent applicationEvent;
         try {
             applicationEvent = eventClass.getConstructor(Object.class).newInstance(originalEvent.getSource());
         } catch (Exception e) {
             throw new EventTranslationException("Failed to instantiate event from " + eventClass.getCanonicalName());
         }
         for (Method method : originalEvent.getClass().getMethods()) {
             if (!GenericTranslationScheme.isGetter(method)) {
                 continue;
             }
             final String fieldName = StringUtils.uncapitalize(method.getName().substring(method.getName().startsWith("get") ? 3 : 2));
             final Field field = ReflectionUtils.findField(eventClass, fieldName);
             if (field == null || !field.getType().isAssignableFrom(method.getReturnType())) {
                 continue;
             }
             field.setAccessible(true);
             try {
                field.set(applicationEvent, method.invoke(originalEvent));
             } catch (Exception e) {
                 throw new EventTranslationException("Failed to set property: " + fieldName, e);
             }
         }
         return applicationEvent;
     }
 
     @Override
     public void fillIn(Event originalEvent, ApplicationEvent translated) throws EventTranslationException {
         for (Method method : originalEvent.getClass().getMethods()) {
             if (!method.getName().matches("set[A-Z].*") || !Modifier.isPublic(method.getModifiers())
                     || !method.getReturnType().equals(void.class) || method.getParameterTypes().length != 1) {
                 continue;
             }
             final String propertyName = StringUtils.uncapitalize(method.getName().substring(3));
             final Field field = ReflectionUtils.findField(translated.getClass(), propertyName);
             if (field == null || !method.getParameterTypes()[0].isAssignableFrom(field.getType())) {
                 continue;
             }
            field.setAccessible(true);
             try {
                 method.invoke(originalEvent, field.get(translated));
             } catch (Exception e) {
                 throw new EventTranslationException("Failed to set property on original event: " + propertyName, e);
             }
         }
     }
 
 }
