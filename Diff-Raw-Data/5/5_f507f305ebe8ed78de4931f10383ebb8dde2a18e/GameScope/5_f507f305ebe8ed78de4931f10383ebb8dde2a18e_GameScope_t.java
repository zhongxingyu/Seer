 package com.github.colorlines.domainimpl;
 
 import com.google.inject.Key;
 import com.google.inject.Provider;
 import com.google.inject.Scope;
 
 import javax.inject.Singleton;
 import java.util.Map;
 
 import static com.google.common.collect.Maps.newHashMap;
 
 /**
  * @author Stanislav Kurilin
  */
 @Singleton
 public class GameScope implements Scope {
    private final Map<Key, Provider> currentScope = newHashMap();
 
     public void reset() {
         currentScope.clear();
     }
 
     @Override
     public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
         return new Provider<T>() {
             @Override
             public T get() {
                 if (!currentScope.containsKey(key)) {
                     currentScope.put(key, unscoped);
                 }
                return (T) currentScope.get(key).get();
             }
         };
     }
 }
