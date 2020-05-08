 package org.polyforms.repository.jpa.binder;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.persistence.Parameter;
 import javax.persistence.Query;
 
 abstract class AbstractParameterBinder<T> implements ParameterBinder<T>, ParameterKey<T> {
     private final List<ParameterMatcher<T>> parameterMatchers = new ArrayList<ParameterMatcher<T>>();
     private final Map<Method, Map<T, Integer>> parameterMatchingCache = new HashMap<Method, Map<T, Integer>>();
 
     protected AbstractParameterBinder() {
         addParameterMatcher(new TypedParameterMatcher<T>(this));
     }
 
    protected final void addParameterMatcher(final ParameterMatcher<T> parameterMatcher) {
         this.parameterMatchers.add(parameterMatcher);
     }
 
     public void bind(final Query query, final Method method, final Set<Parameter<?>> parameters,
             final Object[] arguments) {
         final Map<T, Integer> argumentMap = matchParameters(method, parameters);
         for (final Entry<T, Integer> entry : argumentMap.entrySet()) {
             setParameter(query, entry.getKey(), arguments[entry.getValue()]);
         }
     }
 
     private Map<T, Integer> matchParameters(final Method method, final Set<Parameter<?>> parameters) {
         if (!parameterMatchingCache.containsKey(method)) {
             for (final ParameterMatcher<T> parameterMatcher : parameterMatchers) {
                 final Map<T, Integer> parameterMap = parameterMatcher.match(method, parameters);
                 if (parameterMap != null) {
                     parameterMatchingCache.put(method, parameterMap);
                     break;
                 }
             }
         }
         return parameterMatchingCache.get(method);
     }
 
     protected abstract void setParameter(Query query, T key, Object value);
 }
