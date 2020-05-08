 package ru.finam.bustard;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 import java.util.*;
 
 public class Config {
     private final Multimap<Class<?>, String> eventTypes = HashMultimap.create();
     private final Multimap<String, Class<?>> eventsOnBinding = HashMultimap.create();
     private final Map<SubscriberKey, Executor> executors = new HashMap<SubscriberKey, Executor>();
     private final Map<String, Executor> executorsByQualifier = new HashMap<String, Executor>();
     private final List<Executor> executorList = new ArrayList<Executor>();
 
     public Config() {
     }
 
     public void attachExecutors(Executor... executors) {
         Collections.addAll(executorList, executors);
     }
 
     public void addExecuteQualifier(String qualifierName, Class<? extends Executor> executorType) {
         boolean added = false;
         for (Executor executor : executorList) {
             if (isAssignable(executorType, executor.getClass())) {
                 if (added) {
                     throw new RuntimeException("Two executors for one qualifier: " + qualifierName);
                 }
                 executorsByQualifier.put(qualifierName, executor);
                 added = true;
             }
         }
         if (!added) {
             throw new IllegalStateException("No executors for: " + qualifierName +
                     ". Attach executor before initialize.");
         }
     }
 
     private boolean isAssignable(Class<?> superType, Class<?> type) {
         while (type != superType && type != Object.class) {
             type = type.getSuperclass();
         }
         return type == superType;
     }
 
     public void put(Class<?> listenerType, String eventTypeName,
                     String topic, String qualifierName, boolean eventOnBinding) {
         String key = ChannelKey.get(eventTypeName, topic);
 
         eventTypes.put(listenerType, key);
         if (qualifierName != null) {
             Executor executor = executorsByQualifier.get(qualifierName);
             executors.put(new SubscriberKey(listenerType, key), executor);
         }
         if (eventOnBinding) {
             eventsOnBinding.put(key, listenerType);
         }
     }
 
     public Collection<String> findEventTypesFor(Class<?> subscriberType) {
         Collection<String> result = new ArrayList<String>();
         while(subscriberType != Object.class) {
             result.addAll(eventTypes.get(subscriberType));
             subscriberType = subscriberType.getSuperclass();
         }
         return result;
     }
 
     public Executor findExecutorFor(Class<?> subscriberType, String channelKey) {
        Class<?> type = subscriberType;
        Executor executor = null;
        while (executor == null && type != Object.class) {
            executor = executors.get(new SubscriberKey(type, channelKey));
            type = type.getSuperclass();
        }
        return executor;
     }
 
     public boolean isEventOnBinding(Class<?> subscriberType, String key) {
         Collection<Class<?>> classes = eventsOnBinding.get(key);
         return classes != null && classes.contains(subscriberType);
     }
 
     public boolean needToSave(String channelKey) {
         return eventsOnBinding.containsKey(channelKey);
     }
 
 
     private static class SubscriberKey {
         private final Class<?> subscriber;
         private final String channelKey;
 
         private SubscriberKey(Class<?> subscriberType, String channelKey) {
             Preconditions.checkNotNull(subscriberType, "subscriberType");
             Preconditions.checkNotNull(channelKey, "channelKey");
 
             this.subscriber = subscriberType;
             this.channelKey = channelKey;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             SubscriberKey that = (SubscriberKey) o;
 
             return subscriber == that.subscriber &&
                     channelKey.equals(that.channelKey);
         }
 
         @Override
         public int hashCode() {
             int result = subscriber.hashCode();
             result = 31 * result + channelKey.hashCode();
             return result;
         }
     }
 }
